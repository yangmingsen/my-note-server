package top.yms.note.conpont.queue.channel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteExpireCacheService;
import top.yms.note.conpont.queue.IMessage;
import top.yms.note.entity.DeadMessage;
import top.yms.note.exception.ComponentException;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.repo.DeadMessageRepository;
import top.yms.note.utils.IdWorker;

import javax.annotation.Resource;

public abstract class AbstractQueueChannel implements QueueChannel {

    private final static Logger log = LoggerFactory.getLogger(AbstractQueueChannel.class);

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

    protected int getMaxReEnqueueTime() {
        return maxReEnqueueTime;
    }

    protected IdWorker getIdWorker() {
        return idWorker;
    }

    protected NoteExpireCacheService getNoteExpireCacheService() {
        return noteExpireCacheService;
    }

    protected DeadMessageRepository getDeadMessageRepository() {
        return deadMessageRepository;
    }

    public boolean offer(IMessage iMessage) {
        String msgId = iMessage.getMsgId();
        //加msgId
        if (StringUtils.isBlank(msgId)) {
            msgId = "[queue]"+getIdWorker().nextId();
            iMessage.setMsgId(msgId);
        }
        Object o = getNoteExpireCacheService().find(msgId);
        if (o != null) {
            int count = (int)o;
            count = count+1;
            if (count > getMaxReEnqueueTime()) {
                log.info("msgId={}, 超过最大重入队列次数: {}, 进入死亡队列", msgId, getMaxReEnqueueTime());
                DeadMessage deadMessage = new DeadMessage();
                deadMessage.setMsgId(msgId);
                deadMessage.setiMessage(iMessage);
                getDeadMessageRepository().save(deadMessage);
                return true;
            }
            getNoteExpireCacheService().update(msgId, count, 120);
        } else {
            getNoteExpireCacheService().add(msgId, 1, 120);
        }
        if (iMessage.getPriority() == MessagePriority.HIGH) {
            return offer2HighQueue(iMessage);
        } else if (iMessage.getPriority() == MessagePriority.MEDIUM) {
            return offer2MediumQueue(iMessage);
        } else if (iMessage.getPriority() == MessagePriority.LOW) {
            return offer2LowQueue(iMessage);
        } else {
            throw new ComponentException(CommonErrorCode.E_200214);
        }
    }

    abstract boolean offer2HighQueue(IMessage iMessage);

    abstract boolean offer2MediumQueue(IMessage iMessage);

    abstract boolean offer2LowQueue(IMessage iMessage);


}
