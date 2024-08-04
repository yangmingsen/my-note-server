package top.yms.note.enums;

public enum NoteTypeEnum {
    File("文件", "1"),
    Directory("目录", "0");


    private String name;
    private String value;

    NoteTypeEnum(String name, String value) {
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

    public static NoteTypeEnum apply(String v) {
        if (v == null) return null;
        for (NoteTypeEnum nt : NoteTypeEnum.values()) {
            if (nt.value.equals(v)) {
                return nt;
            }
        }
        return null;
    }
}
