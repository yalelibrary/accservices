package edu.yale.sml.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.persistence.AdminDAO;
import edu.yale.sml.persistence.AdminHibernateDAO;

public class DBCheckFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DBCheckFilter.class);

    private FilterConfig filterConfig;

    public DBCheckFilter() {
        super();
    }

    public void destroy() {
    }

    /**
     * Used to check access connection to database.
     * ideally this should be part of application startup? since it will be hit every time there's a request for page.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String netId = "";
        if (request.getSession().getAttribute(Constants.NETID) != null) {
            netId = request.getSession().getAttribute(Constants.NETID).toString();
        }
        // just hit the database to see if the connection to database is alive; o/wise throw exception.
        AdminDAO adminDAO = new AdminHibernateDAO();
        try {
            adminDAO.findByNetId(netId);
        } catch (HibernateException t) {
            logger.error("Error validating connection to database", t);
            throw t;
        }
        chain.doFilter(req, res); // not called if exception is thrown
    }

    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }
}