package top.yms.note.comm;

public enum NoteSystemErrorCode implements ErrorCode{
    E_400000(400000, "数据库更新失败"),
    E_400001(400001, "AES加密异常"),
    E_400002(400002, "AES解密异常"),


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
