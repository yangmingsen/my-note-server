package top.yms.note.msgcd;

public enum BusinessErrorCode implements ErrorCode {
    E_204000(204000, "加密内容禁止进入lucene"),
    E_204001(204001, "笔记convert 前置检查未通过"),
    E_204002(204002, "笔记convert失败"),
    E_204003(204003, "未获取到临时访问token"),
    E_204004(204004, "错误临时访问token"),
    E_204005(204005, "请求临时访问token为空"),
    E_204006(204006, "认证失败"),
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
