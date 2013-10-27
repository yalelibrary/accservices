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


// Talks to AdminDAO to find if the user (netid) is Admin to access secure pages e.g. admin.xhtml; 
// specify in web.xml
public class PageAccessAuthorizationFilter implements Filter
{

	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PageAccessAuthorizationFilter.class);
    
    FilterConfig filterConfig; 

	public PageAccessAuthorizationFilter()
	{
		super();
	}

	public void destroy()
	{
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException
	{

		HttpServletRequest request = (HttpServletRequest) req;
		String netid = "";
		if (request.getSession().getAttribute("netid") != null)
		{
			netid = request.getSession().getAttribute("netid").toString();
		}
		AdminDAO adminDAO = new AdminHibernateDAO();		
		String adminCode = adminDAO.findByNetId(netid);
		
		if (adminCode == null || !adminCode.equals("Admin"))
		{
		    logger.debug("AdminFilter. No permission to proceed. has the session expired?");
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            try
            {
                httpResponse.sendRedirect(new PropertiesConfiguration("messages.properties").getString("admin_filter_redirect"));
            }
            catch (ConfigurationException e)
            {
                e.printStackTrace();
                httpResponse.sendRedirect("/powershelf/pages/permissions.xhtml");;
            }
            catch (java.lang.IllegalStateException f)
            {
                f.printStackTrace();
            }

    	}
		else if (adminCode.equals("Admin"))
		{
		    logger.debug("Authorized. Continuing down the filter chain."); 
		    chain.doFilter(req, res);
		}
	}

	public void init(FilterConfig config) throws ServletException
	{
		this.filterConfig = config;
	}	
}