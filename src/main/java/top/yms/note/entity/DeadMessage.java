package top.yms.note.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import top.yms.note.conpont.queue.IMessage;

import java.util.Date;

@Document("dead_message")
public class DeadMessage {
    @Id
    private String id;

    private String msgId;

    private IMessage iMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public IMessage getiMessage() {
        return iMessage;
    }

    public void setiMessage(IMessage iMessage) {
        this.iMessage = iMessage;
    }
}
