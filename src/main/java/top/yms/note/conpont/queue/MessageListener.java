package top.yms.note.conpont.queue;

public interface MessageListener {

    boolean support(String identify);

    void onMessage(Object message);
    
}
