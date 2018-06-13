package edu.yale.sml.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.logic.LogicHelper;

public class CasNetIdFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CasNetIdFilter.class);
    private String serverName;


    public CasNetIdFilter() {
        super();
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        final String var = serverName + request.getContextPath() + "/pages/index.xhtml";
        final String ticket = req.getParameter("ticket");

        if (ticket == null || ticket.isEmpty()) {
            throw new ServletException("Failure to log in");
        }
        logger.debug("Sending " + var );
        final String service = URLEncoder.encode(var);
        final String param = "ticket=" + ticket + "&service=" + service;

        try {
            final List<String> userList = LogicHelper.getCASUser(Constants.CAS_VALIDATE_URL, new StringBuffer(param));
            if ( userList.size() < 3 ) {
		throw new IOException ("Error with User List from CAS: [" + userList.get(0) + ","+userList.get(1)+"]   " + var + "  " + param);
            }
            logger.debug("Received from cas: {}", userList.size());
            final String user = userList.get(2).trim();
            request.getSession().setAttribute(Constants.NETID, user);
            logger.trace("Saved user in session={}", user);
        } catch (UnknownHostException e) {
            logger.error("Error finding server or service.", e);
            throw new UnknownHostException("Error contacting CAS server.");
        } catch (IOException e) {
            logger.debug("Exception finding/validating ticket, possibly due to CAS server reachability issue.");
            throw new IOException(e);
        }
        chain.doFilter(req, res);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
       serverName = filterConfig.getInitParameter("serverName");
    }
}
