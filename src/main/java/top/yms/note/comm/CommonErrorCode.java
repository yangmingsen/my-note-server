package top.yms.note.comm;

/**
 * Created by yangmingsen on 2022/9/30.
 */
public enum CommonErrorCode implements ErrorCode {

    ////////////////////////////////////公用异常编码 //////////////////////////
    E_100101(100101,"传入参数与接口不匹配"),


    ////////////////////////////////////业务异常编码 //////////////////////////
    E_200201(200201, "无法找到对应数据"),
    E_200202(200202, "输入参数不能为空"),
    E_200205(200205,"查询结果大于1"),
    E_200206(200206,"查询结果为空"),
    E_200207(200207,"同步出错"),
    E_200208(200208,"导出异常"),
    E_200209(200209,"导入异常"),
    E_200210(200210,"未登录"),



    E_203000(203000, "Note parentId不能为空"),
    E_203001(203001, "Note Id不能为空"),
    E_203002(203002, "wangEditor上传文件失败"),
    E_203003(203003, "FileStore loadFile失败"),
    E_203004(203004, "FileStore Save失败"),
    E_203005(203005, "url2pdf参数错误"),
    E_203006(203006, "获取pdf响应异常"),
    E_203007(203007, "非法auth对象"),
    E_203008(203008, "mindMap数据保存出错"),


    /**
     * 未知错误
     */
    UNKNOWN(999999,"未知错误");

    private int code;
    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private CommonErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static CommonErrorCode setErrorCode(int code) {
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            if (errorCode.getCode()==code) {
                return errorCode;
            }
        }
        return null;
    }
}
