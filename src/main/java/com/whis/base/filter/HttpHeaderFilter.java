package com.whis.base.filter;

import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dd on 30/09/2016.
 */
public class HttpHeaderFilter extends OncePerRequestFilter {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(HttpHeaderFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("in HttpHeaderFilter");
        for (String header : response.getHeaderNames())
        {
            logger.info("{}: {}", header, response.getHeader(header));
        }
    }
}
