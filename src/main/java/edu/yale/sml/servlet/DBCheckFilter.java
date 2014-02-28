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

    FilterConfig filterConfig; // noop
    final static Logger logger = LoggerFactory.getLogger("edu.yale.sml.servlet.DBCheckFilter");

    public DBCheckFilter() {
        super();
    }

    public void destroy() {
    }

    /**
     * Used to check access connection to database. ideally this should be part of application startup? since it will be hit everytime there's a request for page.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String netid = "";
        if (request.getSession().getAttribute("netid") != null) {
            netid = request.getSession().getAttribute("netid").toString();
        }
        // just hit the database to see if the connection to mysql database is alive; o/wise throw exceptiion.
        AdminDAO adminDAO = new AdminHibernateDAO();
        try {
            adminDAO.findByNetId(netid);
        } catch (HibernateException t) {
            logger.debug("Error validating connection to SQL Database");
            throw t;
        }
        chain.doFilter(req, res); // not called if exception is thrown
    }

    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }
}