package com.hdsoft.configuration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SameSiteCookieFilter implements Filter 
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException 
    {
      
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Add SameSite attribute to all cookies
        httpResponse.addHeader("Set-Cookie", "Path=/; SameSite=Lax");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
       
    }
}

