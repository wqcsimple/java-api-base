package com.whis.app.service;

import com.whis.base.interceptor.SecurityInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dd on 08/10/2016.
 */
@Component("CoreAuthenticator")
public class UserAuthenticator implements SecurityInterceptor.Authenticator {

    private static Logger logger = LoggerFactory.getLogger(UserAuthenticator.class);

    @Override
    public void process(HttpServletRequest httpServletRequest) {

    }
}
