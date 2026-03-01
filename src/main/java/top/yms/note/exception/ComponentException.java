package top.yms.note.exception;

import top.yms.note.msgcd.ComponentErrorCode;
import top.yms.note.msgcd.ErrorCode;

public class ComponentException extends RuntimeException {

    private ErrorCode errorCode;

    public ComponentException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        this.errorCode = errorCode;
    }

    public ComponentException() {
        super();
    }


    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
