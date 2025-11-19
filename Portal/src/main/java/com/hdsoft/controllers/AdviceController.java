package com.hdsoft.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.web_service_001;

@Controller
@ControllerAdvice
public class AdviceController 
{
	 @ExceptionHandler(NoHandlerFoundException.class)
	 public ModelAndView handle(Exception ex) 
	 {	
		 ModelAndView mv = new ModelAndView();
		 
		 mv.setViewName("redirect:/404");
		 
		 return mv;
	 }
	 
	 @RequestMapping(value = {"/404"}, method = RequestMethod.GET)
     public ModelAndView NotFoudPage(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {
		 ModelAndView mv = new ModelAndView();
		 
		 mv.setViewName("Datavision/Login/404");
		 
         return mv;
     } 
}
