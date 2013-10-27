package edu.yale.sml.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Deprecated
public class LogFilter implements Filter
{

    public LogFilter()
    {
        super();
    }

    public void destroy()
    {
    }

    /**
     * Determines if a ticket has been returned from net id. If so, it moves onto next.
     */

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        chain.doFilter(req, res);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        //
    }

}