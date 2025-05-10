package top.yms.note.exception;

import top.yms.note.msgcd.ErrorCode;

/**
 * 自定义的异常类型
 * @author Administrator
 * @version 1.0
 **/
public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        this.errorCode = errorCode;
    }
    public BusinessException() {
        super();
    }


    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
