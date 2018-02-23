package com.whis.base.exception;

/**
 * Created by dd on 1/27/16.
 */
public class AuthFailException extends BaseException {
    public AuthFailException() {
        this("auth fail");
    }

    public AuthFailException(String message) {
        super(ERROR_AUTH_FAIL, message);
    }
}
