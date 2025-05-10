package top.yms.note.exception;

import top.yms.note.msgcd.ErrorCode;

public class NoteSystemException extends RuntimeException {
    private ErrorCode errorCode;

    public NoteSystemException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }
    public NoteSystemException() {
        super();
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
