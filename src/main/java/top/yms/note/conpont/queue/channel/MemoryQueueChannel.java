package top.yms.note.conpont.queue.channel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteExpireCacheService;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.entity.DeadMessage;
import top.yms.note.repo.DeadMessageRepository;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class MemoryQueueChannel implements QueueChannel{

    private final static Logger log = LoggerFactory.getLogger(MemoryQueueChannel.class);

    private final static BlockingQueue<IMessage> highQueue = new ArrayBlockingQueue<>(1000);
    private final static BlockingQueue<IMessage> mediumQueue = new ArrayBlockingQueue<>(600);
    private final static BlockingQueue<IMessage> lowQueue = new ArrayBlockingQueue<>(400);

    @Resource
    private IdWorker idWorker;

    @Qualifier(NoteConstants.noteExpireTimeCache)
    @Resource
    private NoteExpireCacheService noteExpireCacheService;

    @Resource
    private DeadMessageRepository deadMessageRepository;

    /**
     * 最大重入队列次数
     */
    private int maxReEnqueueTime = 3;

    @Override
    public boolean offer(IMessage iMessage) {
        String msgId = iMessage.getMsgId();
        //加msgId
        if (StringUtils.isBlank(msgId)) {
            iMessage.setMsgId("[queue]"+idWorker.nextId());
        }
        Object o = noteExpireCacheService.find(msgId);
        if (o != null) {
            int count = (int)o;
            count = count+1;
            if (count > maxReEnqueueTime) {
                log.info("msgId={}, 超过最大重入队列次数: {}, 进入死亡队列", msgId, maxReEnqueueTime);
                DeadMessage deadMessage = new DeadMessage();
                deadMessage.setMsgId(msgId);
                deadMessage.setiMessage(iMessage);
                deadMessageRepository.save(deadMessage);
                return true;
            }
            noteExpireCacheService.update(msgId, count, 120);
        } else {
            noteExpireCacheService.add(msgId, 1, 120);
        }
        if (iMessage.getPriority() == MessagePriority.HIGH) {
            return highQueue.offer(iMessage);
        } else if (iMessage.getPriority() == MessagePriority.MEDIUM) {
            return mediumQueue.offer(iMessage);
        } else if (iMessage.getPriority() == MessagePriority.LOW) {
            return lowQueue.offer(iMessage);
        }
        return false;
    }

    @Override
    public IMessage takeFromHigh()  throws Exception {
        return highQueue.take();
    }

    @Override
    public IMessage takeFromMedium()  throws Exception {
        return mediumQueue.take();
    }

    @Override
    public IMessage takeFromLow()  throws Exception {
        return lowQueue.take();
    }

    @Override
    public IMessage pollFromMedium(long timeout, TimeUnit unit) {
        try {
            return mediumQueue.poll(timeout, unit);
        } catch (InterruptedException e) {

        }
        return null;
    }

    @Override
    public IMessage pollFromLow() {
        return lowQueue.poll();
    }

}
