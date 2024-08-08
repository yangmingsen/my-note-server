package top.yms.note.config;

public enum NoteOpType {
    ADD("add"),
    UPDATE("upd"),
    DELETE("del"),
    Destroy("destroy")
    ;
    private final String name;

    private NoteOpType(String name) {
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
