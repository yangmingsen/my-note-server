package top.yms.note.conpont.queue.channel;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.queue.IMessage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class MemoryQueueChannel implements QueueChannel{

    private final static BlockingQueue<IMessage> highQueue = new ArrayBlockingQueue<>(500);
    private final static BlockingQueue<IMessage> mediumQueue = new ArrayBlockingQueue<>(500);
    private final static BlockingQueue<IMessage> lowQueue = new ArrayBlockingQueue<>(500);

    @Override
    public boolean offer(IMessage iMessage) {
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
