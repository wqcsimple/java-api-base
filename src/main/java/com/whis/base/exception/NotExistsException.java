package com.whis.base.exception;

/**
 * Created by dayaa on 16/2/2.
 */
public class NotExistsException extends BaseException {

    public NotExistsException() {
        this("not exists");
    }

    public NotExistsException(String message) {
        super(ERROR_NOT_EXISTS, message);
    }
}
