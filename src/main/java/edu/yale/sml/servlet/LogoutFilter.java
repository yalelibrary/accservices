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

//TODO should this be made abstract?
public class LogoutFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(LogoutFilter.class);

    public LogoutFilter() {
        super();
    }

    /*
     * Removes NetId attribute from Session. Note it does not invalidate the session completely (TODO check why)
     * 
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if (request.getSession().getAttribute("netid") != null) {
            try {
                String user = request.getSession().getAttribute("netid").toString();
                request.getSession().removeAttribute("netid");
                request.getSession().setAttribute("loggedout", "true");
                logger.debug("[LogoutFilter] -- logged out : " + user);
            } catch (Exception e1) {
                e1.printStackTrace();
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