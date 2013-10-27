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

public class SessionNetIdFilter implements Filter
{

    FilterConfig filterConfig; // noop

    public SessionNetIdFilter()
    {
        super();
    }

    public void destroy()
    {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {

        HttpServletRequest request = (HttpServletRequest) req;
        String netid = "";
        if (request.getSession().getAttribute("netid") != null)
        {
            netid = request.getSession().getAttribute("netid").toString();
        }

        if (netid.isEmpty() && request.getSession().getAttribute("loggedout") != null)
        {
            request.getSession().setAttribute("loggedout", null);
            HttpServletResponse httpResponse1 = (HttpServletResponse) res;
            request.getSession().setAttribute("netid", "ghost");
        }

        if (netid.isEmpty())
        {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            try
            {
                httpResponse.sendRedirect(new PropertiesConfiguration("messages.properties").getString("index_url"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            chain.doFilter(req, res);
            return;
        }

        else
        {
            if (request.getSession().getAttribute("netid").equals("ghost"))
            {
                // System.out.println("[SessionNetIdFilter] Skipping chain.doFilter(req, res) & waiting for Javascript to discover user 'ghost' . . .");
            }
            else
            {
                chain.doFilter(req, res); // note the position of chain.doFilter
            }
        }

    }

    public void init(FilterConfig config) throws ServletException
    {
        this.filterConfig = config;
    }
}