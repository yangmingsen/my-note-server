package top.yms.note.conpont.crawler.impl;

import top.yms.note.conpont.queue.channel.MessagePriority;
import top.yms.note.conpont.queue.imsg.AbstractMessage;

public class NetworkNoteMessage extends AbstractMessage {

    @Override
    public MessagePriority getPriority() {
        return MessagePriority.HIGH;
    }
}
