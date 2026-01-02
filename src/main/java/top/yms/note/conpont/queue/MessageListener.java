package top.yms.note.conpont.queue;

public interface MessageListener {

    boolean support(IMessage message);

    void onMessage(IMessage message);
}
