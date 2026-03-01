package top.yms.note.msgcd;

public class CustomErrorCode implements ErrorCode{

    private  int code;
    private  String desc;

    public CustomErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getDesc() {
        return null;
    }
}
