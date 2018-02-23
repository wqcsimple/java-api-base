package com.whis.base.exception;


/**
 * Created by dd on 1/27/16.
 */
public class WrongParamException extends BaseException {
    public WrongParamException(String message) {
        super(ERROR_WRONG_PARAM, message);
    }
}
