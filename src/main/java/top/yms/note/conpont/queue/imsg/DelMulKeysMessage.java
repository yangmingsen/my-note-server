package top.yms.note.conpont.queue.imsg;

import top.yms.note.conpont.queue.channel.MessagePriority;

public class DelMulKeysMessage extends AbstractMessage{

    public MessagePriority getPriority() {
        return MessagePriority.HIGH;
    }
}
