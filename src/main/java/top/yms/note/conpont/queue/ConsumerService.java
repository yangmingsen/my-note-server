package top.yms.note.conpont.queue;

public interface ConsumerService {
    /**
     * 消费一个消息
     * @param message
     */
    void consumer(IMessage iMessage);

}
