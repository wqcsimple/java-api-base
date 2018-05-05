package com.whis.base.interceptor;

import com.whis.base.core.CoreConfig;
import com.whis.base.exception.ParamNotSetException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by dd on 9/8/15.
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityInterceptor.class);

    private final Authenticator authenticator;

    @Autowired(required = false)
    public SecurityInterceptor(Authenticator authenticator) {
        this.authenticator = authenticator;

        if (CoreConfig.getInstance().getEnableTokenSecurityInterceptor().get() && this.authenticator == null)
        {
            logger.error("Authenticator not found");
            // throw new InvalidConfigurationException("Authenticator not found");
        }
    }

    private boolean canGuestAccess(String path)
    {
        List<Pattern> patternList = CoreConfig.getInstance().getGuestCanAccessPathPatternList().get();
        return patternList.stream().anyMatch(p -> p.matcher(path).matches());
    }

    public static String getRequiredParam(HttpServletRequest httpServletRequest, String key) throws ParamNotSetException {
        if (!httpServletRequest.getParameterMap().containsKey(key))
        {
            throw new ParamNotSetException(key + " not set");
        }

        return httpServletRequest.getParameter(key);
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception
    {
        if (!CoreConfig.getInstance().getEnableTokenSecurityInterceptor().get()) {
            return true;
        }

        URI uri = new URI(httpServletRequest.getRequestURI());
        String path = uri.getPath();

        if (CoreConfig.getInstance().getDebug().get()) {
            logger.trace("->");
            logger.trace("path: {}", path);
            logger.trace("query: {}", httpServletRequest.getQueryString());
            logger.trace("parameter: ");
            if (!(httpServletRequest instanceof MultipartHttpServletRequest)) {
                httpServletRequest.getParameterMap().forEach((key, value) -> logger.trace("{}: {}", key, value));
            }
            logger.trace("<-");
        }

        if (!canGuestAccess(path) && authenticator != null)
        {
            authenticator.process(httpServletRequest);
        }

        // httpServletRequest.setAttribute("client", getRequiredParam(httpServletRequest, "client"));
        // httpServletRequest.setAttribute("version", getRequiredParam(httpServletRequest, "version"));

        return true;
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception
    {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception
    {

    }

    public interface Authenticator
    {
        void process(HttpServletRequest httpServletRequest);
    }
}
