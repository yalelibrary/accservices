package edu.yale.sml.util;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

//Wrapper for CAS. 
/**
 * @see also http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/filter/DelegatingFilterProxy.html
 * 
 * 
 * 
 */
@Deprecated
public class CASAuthProxy implements Servlet
{

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public ServletConfig getServletConfig()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getServletInfo()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroy()
    {
        // TODO Auto-generated method stub
    }

}