package top.yms.note.exception;

import top.yms.note.comm.ErrorCode;

/**
 * Created by yangmingsen on 2024/4/6.
 */
public class WangEditorUploadException extends RuntimeException{
    private ErrorCode errorCode;

    public WangEditorUploadException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        this.errorCode = errorCode;
    }
    public WangEditorUploadException() {
        super();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
