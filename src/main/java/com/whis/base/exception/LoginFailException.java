package com.whis.base.exception;

/**
 * Created by dd on 1/27/16.
 */
public class LoginFailException extends BaseException {
    public LoginFailException() {
        this("login fail");
    }

    public LoginFailException(String message) {
        super(ERROR_LOGIN, message);
    }
}
