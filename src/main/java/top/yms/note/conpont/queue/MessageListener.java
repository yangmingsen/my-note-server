package top.yms.note.conpont.queue;

/**
 * 统一消息接收
 * <p>注意点： 请不要直接修改message里面的数据，因为可能会导致后续流程的实现者们拿到脏数据（若需要修改请copy一份新的）</p>
 */
public interface MessageListener {

    boolean support(IMessage message);

    void onMessage(IMessage message);
}
