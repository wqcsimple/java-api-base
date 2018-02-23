package com.whis.base.exception;


/**
 * Created by dd on 1/27/16.
 */
public class ParamNotSetException extends BaseException {
    public ParamNotSetException(String message) {
        super(ERROR_PARAM_NOT_SET,  message);
    }
}
