package top.yms.note.enums;

public enum CheckTargetStatus {
    UN_RUN("未执行","1"),
    RUNNING("执行中","2"),
    COMPLETED("执行完成","3"),
    ;

    private String name;
    private String value;

    CheckTargetStatus(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
