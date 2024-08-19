package top.yms.note.enums;

public enum NoteOpTypeEnum {
    ADD("add"),
    UPDATE("upd"),
    DELETE("del"),
    Destroy("destroy")
    ;
    private final String name;

    private NoteOpTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NoteOpType{" +
                "name='" + name + '\'' +
                '}';
    }
}
