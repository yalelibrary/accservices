package edu.yale.sml.servlet;

import java.io.IOException;
import java.net.URLEncoder;

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

public class CasNetIdFilter implements Filter
{

    final static Logger logger = LoggerFactory.getLogger("edu.yale.sml.servlet.CasNetIdFilter");
    private final String CAS_VALIDATE_URL = "https://secure.its.yale.edu/cas/validate";

    public CasNetIdFilter()
    {
        super();
    }

    // determines if a ticket has been returned from net id .. if so, moves to next


    // Gets NetId and puts it in Session
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {

        HttpServletRequest request = (HttpServletRequest) req;
        String var = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/pages/index.xhtml";
        String ticket = req.getParameter("ticket").toString();

        if (ticket == null || ticket.isEmpty()) // ? todo check this
        {
            throw new ServletException("Failure to log in");
        }

        String service = URLEncoder.encode(var); // N.B. Must url-encode!!!!

        String param = "ticket=" + ticket + "&service=" + service;
 
        try
        {
            final String user;
            user = LogicHelper.getCASUser(CAS_VALIDATE_URL, new StringBuffer(param)).get(2).trim(); 
            request.getSession().setAttribute("netid", user); 
        }

        catch (java.net.UnknownHostException e)
        {
            logger.debug("Error finding server or service.");
            throw new java.net.UnknownHostException("Error contacting CAS server.");
        }
        catch (IOException e)
        {
            logger.debug("Exception finding/validating ticket, possibly due to CAS server reachability issue.");
            throw new IOException(e);
        }
        chain.doFilter(req, res);
    }

    public void destroy()
    {
    }

    public void init(FilterConfig arg0) throws ServletException
    {
    }
}