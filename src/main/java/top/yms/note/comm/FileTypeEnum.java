package top.yms.note.comm;

public enum FileTypeEnum {
    WER(100,"wer", "默认Note类型"),
    TXT(101,"txt", "文本文件"),
    MARKDOWN(102,"markdown", "markdown文件"),
    PDF(103,"pdf", "pdf"),

    UNKNOWN(199,"unknown", "未知文件类型") ;

    FileTypeEnum(int code, String value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    private int code;
    private String value;
    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getValue() {
        return value;
    }

    public boolean compare(String str) {
        return getValue().equals(str);
    }
}
