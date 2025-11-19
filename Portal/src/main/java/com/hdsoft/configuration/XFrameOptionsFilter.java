package com.hdsoft.configuration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class XFrameOptionsFilter implements Filter 
{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
    	HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set X-Frame-Options header
        httpResponse.setHeader("X-Frame-Options", "DENY");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() 
    {
        // Cleanup logic, if needed
    }
}

