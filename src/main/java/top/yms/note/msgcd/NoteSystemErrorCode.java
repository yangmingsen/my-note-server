package top.yms.note.msgcd;

public enum NoteSystemErrorCode implements ErrorCode{
    E_400000(400000, "数据库更新失败"),
    E_400001(400001, "AES加密异常"),
    E_400002(400002, "AES解密异常"),
    E_400003(400003, "文件上传失败"),
    E_400004(400004, "文件流处理失败"),
    E_400005(400005, "本地文件删除出错"),
    E_400006(400006, "zip解析异常"),
    E_400007(400007, "压缩解析异常"),


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
