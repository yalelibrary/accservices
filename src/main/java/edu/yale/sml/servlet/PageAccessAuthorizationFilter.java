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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.yale.sml.persistence.AdminDAO;
import edu.yale.sml.persistence.AdminHibernateDAO;
import org.slf4j.Logger;

import static  org.slf4j.LoggerFactory.getLogger;


/**
 * Talks to AdminDAO to find if the user (netid) is Admin to access secure pages e.g. admin.xhtml;
 * specify in web.xml
 */
public class PageAccessAuthorizationFilter implements Filter {

    private static final Logger logger = getLogger(PageAccessAuthorizationFilter.class);

    FilterConfig filterConfig;

    public PageAccessAuthorizationFilter() {
        super();
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String netId = "";
        if (request.getSession().getAttribute(Constants.NETID) != null) {
            netId = request.getSession().getAttribute(Constants.NETID).toString();
        }
        AdminDAO adminDAO = new AdminHibernateDAO();
        String adminCode = adminDAO.findByNetId(netId);

        if (adminCode == null || !adminCode.equals(Constants.ADMIN_CODE)) {
            logger.info("No permission to proceed. Has the session expired?");
            HttpServletResponse response = (HttpServletResponse) res;
            try {
                response.sendRedirect(new PropertiesConfiguration("messages.properties").getString("admin_filter_redirect"));
            } catch (ConfigurationException e) {
                logger.error("Error configuring", e);
                response.sendRedirect(Constants.PERMISSIOSN_PAGE);
            } catch (IllegalStateException f) {
                logger.error("Error", f);
            }
        } else if (adminCode.equals(Constants.ADMIN_CODE)) {
            logger.trace("Authorized. Continuing down the filter chain.");
            chain.doFilter(req, res);
        }
    }

    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }
}