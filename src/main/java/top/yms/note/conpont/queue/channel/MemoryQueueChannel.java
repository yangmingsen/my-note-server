package top.yms.note.conpont.queue.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.queue.IMessage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Primary
@Component(NoteConstants.memoryQueueChannel)
public class MemoryQueueChannel extends AbstractQueueChannel{

    private final static Logger log = LoggerFactory.getLogger(MemoryQueueChannel.class);

    private final static BlockingQueue<IMessage> highQueue = new ArrayBlockingQueue<>(1000);
    private final static BlockingQueue<IMessage> mediumQueue = new ArrayBlockingQueue<>(600);
    private final static BlockingQueue<IMessage> lowQueue = new ArrayBlockingQueue<>(400);


    @Override
    boolean offer2HighQueue(IMessage iMessage) {
        return highQueue.offer(iMessage);
    }

    @Override
    boolean offer2MediumQueue(IMessage iMessage) {
        return mediumQueue.offer(iMessage);
    }

    @Override
    boolean offer2LowQueue(IMessage iMessage) {
        return lowQueue.offer(iMessage);
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
