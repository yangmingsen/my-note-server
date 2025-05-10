package top.yms.note.msgcd;

/**
 * Created by yangmingsen on 2024/10/19.
 */
public enum ComponentErrorCode implements ErrorCode{

    E_204000(204000, "未找到note元数据信息"),
    E_204001(204001, "当前文件不支持文本内容获取"),
    ;

    private final int code;
    private final String desc;

    ComponentErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

}
