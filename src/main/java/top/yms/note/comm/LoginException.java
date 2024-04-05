package top.yms.note.comm;

/**
 * Created by yangmingsen on 2022/10/15.
 */
public class LoginException extends RuntimeException {
    private ErrorCode errorCode;

    public LoginException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }
    public LoginException() {
        super();
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
