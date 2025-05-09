package top.yms.note.comm;

public enum BusinessErrorCode implements ErrorCode {
    E_204000(204000, "加密内容禁止进入lucene")
    ;
    private int code;
    private String desc;

    BusinessErrorCode(int code, String desc) {
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
