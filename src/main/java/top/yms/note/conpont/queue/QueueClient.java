package top.yms.note.conpont.queue;

public interface QueueClient {

    /**
     * send message .  ok true or false if send fail.
     * @param message
     * @return
     */
    boolean send(IMessage message);

    /**
     * receive message.  阻塞方式获取一个message.
     * @return
     */
    IMessage receive();
}
