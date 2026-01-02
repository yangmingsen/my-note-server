package top.yms.note.conpont.queue;

import top.yms.note.conpont.queue.channel.MessagePriority;

public interface IMessage {
    /**
     * 获取目的地。
     * <p>不一定会使用，目前是使用 instanceof 来判断目的地的</p>
     * @return
     */
    String getTarget();

    /**
     * 获取body数据
     * @return
     */
    Object getBody();


    MessagePriority getPriority();
}
