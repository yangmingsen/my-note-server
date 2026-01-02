package top.yms.note.conpont.queue.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.conpont.queue.IMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MemoryChannel {

    private final static Logger log = LoggerFactory.getLogger(MemoryChannel.class);

    private final static BlockingQueue<IMessage> queue = new LinkedBlockingQueue<>();

    public static boolean offer(IMessage iMessage) {
        return queue.offer(iMessage);
    }

    public static IMessage take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            log.error("take error", e);
        }
        return null;
    }

}
