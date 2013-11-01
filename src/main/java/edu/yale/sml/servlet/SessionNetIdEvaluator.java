package edu.yale.sml.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

@Deprecated
public class SessionNetIdEvaluator extends HttpServlet
{
    private static final long serialVersionUID = -7145207353136170795L;

    // Deprecated functionality. server-side AJAX handler service responds whether a legit ID exists in
    // session. e.g. with js.sanityCheck(), AND removes appropriate 'ghost' netid
  
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        String data = "";
        if (request.getSession().getAttribute("netid") != null)
        {
            data = (String) request.getSession().getAttribute("netid");
        }
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        if (data.equals("ghost")) // deprecated Javascript usage.
        {
            request.getSession().removeAttribute("netid"); 
            response.getWriter().write("error");
        }
        else
        {
            response.getWriter().write("ok"); 
        }
    }
}