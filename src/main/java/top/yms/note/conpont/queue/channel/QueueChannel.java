package top.yms.note.conpont.queue.channel;

import top.yms.note.conpont.queue.IMessage;

import java.util.concurrent.TimeUnit;

public interface QueueChannel {
    /**
     * 无阻塞发送一个消息
     * @param iMessage
     * @return
     */
    boolean offer(IMessage iMessage);

    /**
     * 接收一个消息：若是无数据会阻塞
     * @return
     */
    IMessage takeFromHigh() throws Exception;

    IMessage takeFromMedium()  throws Exception;

    IMessage takeFromLow()  throws Exception;

    IMessage pollFromMedium(long timeout, TimeUnit unit);

    IMessage pollFromLow();

}
