package top.yms.note.enums;

public enum FileTypeEnum {
    WER(100,"wer", "默认Note类型"),
    TXT(101,"txt", "文本文件"),
    MARKDOWN(102,"md", "markdown文件"),
    PDF(103,"pdf", "pdf"),
    MINDMAP(104,"mindmap", "mindmap"),

    UNKNOWN(199,"unknown", "未知文件类型") ;

    FileTypeEnum(int code, String value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    private final int code;
    private final String value;
    private final String desc;

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
    public static FileTypeEnum apply(String value) {
        if (value == null) return null;
        for (FileTypeEnum fte : FileTypeEnum.values()) {
            if (fte.getValue().equals(value)) {
                return fte;
            }
        }
        return null;
    }
}
