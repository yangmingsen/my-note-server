package top.yms.note.comm;

public enum ExpendType {
    WX_OTHER_DATA("微信其他账单"),
    WX_NORMAL_DATA("微信正常账单"),
    ALI_NORMAL_DATA("支付宝正常账单"),
    ALI_OTHER_DATA("支付宝其他账单"),
    ALI_TMP_DATA("支付宝零时账单"),
    ;

    private String dataType;

    ExpendType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
