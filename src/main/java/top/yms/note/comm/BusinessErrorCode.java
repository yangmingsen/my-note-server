package top.yms.note.comm;

public enum BusinessErrorCode implements ErrorCode {
    E_204000(204000, "加密内容禁止进入lucene"),
    E_204001(204001, "笔记convert 前置检查未通过"),
    E_204002(204002, "笔记convert失败"),
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
