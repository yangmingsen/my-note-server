package top.yms.note.conpont.queue.imsg;

import top.yms.note.conpont.queue.channel.MessagePriority;

public class DelKeyMessage extends AbstractMessage {

    public MessagePriority getPriority() {
        return MessagePriority.HIGH;
    }

    public String getKey() {
        return (String)getTarget();
    }
}
