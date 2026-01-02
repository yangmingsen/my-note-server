package top.yms.note.conpont.queue.imsg;

import top.yms.note.conpont.queue.channel.MessagePriority;

public class DelHashKeyMessage extends AbstractMessage{

    public MessagePriority getPriority() {
        return MessagePriority.HIGH;
    }

    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
