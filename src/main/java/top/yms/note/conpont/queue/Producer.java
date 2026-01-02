package top.yms.note.conpont.queue;

public interface Producer {

    boolean send(IMessage message);

}
