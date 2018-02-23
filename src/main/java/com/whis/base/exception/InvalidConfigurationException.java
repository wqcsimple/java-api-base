package com.whis.base.exception;


/**
 * Created by dd on 1/27/16.
 */
public class InvalidConfigurationException extends BaseException {
    public InvalidConfigurationException(String message) {
        super(ERROR_INVALID_CONFIGURATION,  message);
    }
}
