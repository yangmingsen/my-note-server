package top.yms.note.comm;

public enum CommonInfoCode implements ErrorCode{
    PAGE_NUM_MUST_NOT_NULL(1001,"页码不能为空"),
    PAGE_SIZE_MUST_NOT_NULL(1002,"每页显示数量不能为空"),
    PS_OR_PN_NOT_NULL(1003,"ps pn不能为null"),
    OK(2000,"OK"),
    EXPEND_SERACH_CONDITION_NOT_NULL(1004, "expend搜索条件不能为空");

    private int code;
    private String name;

    CommonInfoCode(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return getName();
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "INFO{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
