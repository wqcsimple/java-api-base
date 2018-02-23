package com.whis.base.controller;

import com.whis.base.common.ErrorResponse;
import com.whis.base.common.Util;
import com.whis.base.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionHandlerController {

    private static Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);

    @ExceptionHandler(value = {Exception.class, RuntimeException.class, Throwable.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ErrorResponse defaultErrorHandler(HttpServletRequest request, Exception e, Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(BaseException.ERROR_IN_INTERCEPTOR, "an error occurred");

        if (ex != null && ex != e)
        {
            errorResponse.setMessage(ex.getMessage());

            logger.error("{}", Util.getStackTrace(ex));
        }

        if (e != null)
        {
            errorResponse.setMessage(e.getMessage());

            if (e instanceof MissingServletRequestParameterException)
            {
                MissingServletRequestParameterException missingServletRequestParameterException = (MissingServletRequestParameterException) e;
                errorResponse.setCode(BaseException.ERROR_PARAM_NOT_SET);
                errorResponse.setMessage(String.format("%s not set", missingServletRequestParameterException.getParameterName()));
                return errorResponse;
            }

            if (e instanceof MethodArgumentTypeMismatchException) {
                MethodArgumentTypeMismatchException methodArgumentTypeMismatchException = (MethodArgumentTypeMismatchException)e;
                errorResponse.setCode(BaseException.ERROR_WRONG_PARAM);

                MethodParameter methodParameter = methodArgumentTypeMismatchException.getParameter();
                RequestParam paramAnnotation = methodParameter.getParameterAnnotation(RequestParam.class);
                errorResponse.setMessage(String.format("param [%s] type error", paramAnnotation == null ? methodParameter.getParameterName() : paramAnnotation.value()));
                return errorResponse;
            }

            if (e instanceof BaseException)
            {
                BaseException baseException = (BaseException)e;
                errorResponse.setCode(baseException.getCode());
                errorResponse.setMessage(baseException.getMessage());
                logger.error("{}", Util.getStackTrace(e, 3));
                return errorResponse;
            }
            else
            {
                logger.error("{}", Util.getStackTrace(e));
            }

        }

        return errorResponse;
    }
}