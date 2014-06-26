package edu.yale.sml.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LogoutFilter.class);

    public LogoutFilter() {
        super();
    }

    /*
     * Removes NetId attribute from Session. Note it does not invalidate the session completely
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if (request.getSession().getAttribute(Constants.NETID) != null) {
            try {
                String user = request.getSession().getAttribute(Constants.NETID).toString();
                request.getSession().removeAttribute(Constants.NETID);
                request.getSession().setAttribute(Constants.LOGGED_OUT, "true");
                logger.debug("Logged out={}", user);
            } catch (Exception e) {
                logger.error("Error filter", e);
            }
        }
        chain.doFilter(req, res);
    }

    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}