package edu.yale.sml.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionNetIdFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(SessionNetIdFilter.class);

    private FilterConfig filterConfig;

    public SessionNetIdFilter() {
        super();
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String netId = "";
        if (request.getSession().getAttribute(Constants.NETID) != null) {
            netId = request.getSession().getAttribute(Constants.NETID).toString();
        }

        if (netId.isEmpty() && request.getSession().getAttribute(Constants.LOGGED_OUT) != null) {
            request.getSession().setAttribute(Constants.LOGGED_OUT, null);
            request.getSession().setAttribute(Constants.NETID, Constants.NULL_NETID);
        }

        if (netId.isEmpty()) {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            try {
                httpResponse.sendRedirect(new PropertiesConfiguration("messages.properties").getString("index_url"));
            } catch (Exception e) {
                logger.error("Net Id null.", e.getMessage());
            }
            chain.doFilter(req, res);
            return;
        } else {
            if (request.getSession().getAttribute(Constants.NETID).equals(Constants.NULL_NETID)) {
                //ignore
            } else {
                chain.doFilter(req, res);
            }
        }
    }

    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }
}