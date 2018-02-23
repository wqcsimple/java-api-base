package com.whis.base.exception;

/**
 * Created by cc on 16/2/28.
 */
public class NotAllowedException extends BaseException {
    public NotAllowedException() {
        this("not allowed");
    }

    public NotAllowedException(String message) {
        super(ERROR_NOT_ALLOWED, message);
    }
}
