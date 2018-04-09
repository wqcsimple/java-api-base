package com.whis.base.controller;

import com.whis.base.common.ErrorResponse;
import com.whis.base.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by dd on 9/7/15.
 */
@RestController
public class CommonController implements ErrorController
{
    private static Logger logger = LoggerFactory.getLogger(CommonController.class);
    private static final String PATH_ERROR = "/error";

    @RequestMapping(PATH_ERROR)
    public ErrorResponse error(HttpServletRequest request, HttpServletResponse response, Exception e)
    {
        logger.info("request path: {} {}, status: {}", request.getRequestURI(), request.getQueryString(), response.getStatus());

        String message = "an error occurred";

        switch (response.getStatus())
        {
            case 403:
                message = "not authorized";
                break;

            case 404:
                message = "not found";
                break;

            case 500:
                message = "inner server error";
                break;
        }

        return new ErrorResponse(BaseException.ERROR, message);
    }


    @RequestMapping("/error404")
    public ErrorResponse error404(HttpServletRequest request)
    {
        return new ErrorResponse(BaseException.ERROR_404, "not found");
    }

    @RequestMapping("/error500")
    public ErrorResponse error500(HttpServletRequest request)
    {
        return new ErrorResponse(BaseException.ERROR_500, "inner server error");
    }


    @Override
    public String getErrorPath() {
        return PATH_ERROR;
    }
}
