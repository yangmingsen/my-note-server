package top.yms.note.conpont.queue.channel;

public enum MessagePriority {
    HIGH("高优先级"),
    MEDIUM("中优先级"),
    LOW("低优先级"),
    ;
    private String name;

    MessagePriority(String name) {
        this.name = name;
    }

}
