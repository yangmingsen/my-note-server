package top.yms.note.conpont.queue.imsg;

import top.yms.note.conpont.queue.IMessage;
import top.yms.note.conpont.queue.channel.MessagePriority;

public class AbstractMessage implements IMessage {

    private String msgId;

    private String target;

    private Object body;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public MessagePriority getPriority() {
        return MessagePriority.LOW;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public Object getBody() {
        return body;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
