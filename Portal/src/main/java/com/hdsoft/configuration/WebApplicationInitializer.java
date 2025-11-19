package com.hdsoft.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletRegistration;

public class WebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
{
   @Override
   protected Class<?>[] getRootConfigClasses()
   {
      return null;
   }

   @Override
   protected Class<?>[] getServletConfigClasses()
   {
      return new Class[] { WebConfiguration.class };
   } 
   
   @Override
   protected String[] getServletMappings()
   {
      return new String[] { "/" };
   }
   
   @Override
   protected void customizeRegistration(ServletRegistration.Dynamic registration) 
   {
       boolean done = registration.setInitParameter("throwExceptionIfNoHandlerFound", "true"); 
       
       if(!done) throw new RuntimeException();
   }
}