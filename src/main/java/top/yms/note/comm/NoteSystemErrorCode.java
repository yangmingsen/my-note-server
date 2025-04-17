package top.yms.note.comm;

public enum NoteSystemErrorCode implements ErrorCode{
    E_400000(400000, "数据库更新失败")
    ;
    private int code;
    private String desc;

    NoteSystemErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
