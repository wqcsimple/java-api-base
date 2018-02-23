package com.whis.base.exception;

/**
 * Created by dd on 9/8/15.
 */
public class BaseException extends RuntimeException
{
    public static final int ERROR_404 = -404;
    public static final int ERROR_500 = -500;
    public static final int ERROR = -1;
    public static final int ERROR_IN_INTERCEPTOR = -2;
    public static final int ERROR_INVALID_CONFIGURATION = -3;

    public static final int ERROR_PARAM_NOT_SET = 1;
    public static final int ERROR_AUTH_FAIL = 2;
    public static final int ERROR_LOGIN = 3;
    public static final int ERROR_WRONG_PARAM = 4;
    public static final int ERROR_NOT_EXISTS = 5;
    public static final int ERROR_EXISTS = 6;
    public static final int ERROR_NOT_ALLOWED = 7;

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public BaseException(int code, String message)
    {
        super("" + code + ": " + message);
        this.code = code;
        this.message = message;
    }
}
