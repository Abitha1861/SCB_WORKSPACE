package com.hdsoft.controllers;

import java.io.IOException;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.Excel_Test;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class Download_Excel_Controller {
	
	public JdbcTemplate Jdbctemplate;
	
	 private static final Logger logger = LogManager.getLogger(Datavision_Admin.class);
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource){
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	 
		@Autowired
		public Menu_Generation MG;
		
		@Autowired
		public Sysconfig sys;
		
		 @Autowired
		 public Session_Model Session;
		
		 @Autowired
		 public Excel_Test ex;
		 
		 
	@RequestMapping(value = {"/Datavision/Download_Excel"}, method = RequestMethod.GET)
    public ModelAndView Configuration_Payment_Gateway(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "SUPTECH", CHILD = "DOWNLOAD";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Configuration/Data_management");
		     
		     mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/Datavision/login");
	     }
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
    }
	
	
	 @RequestMapping(value = {"/Excel-Download/"}, method = RequestMethod.GET)
	 public void User_Register(@RequestParam("SERVICECD") String SERVICECD, @RequestParam("NoOfRecords") String NoOfRecords, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	 {	   
		 if(Session.IsSessionValid(session))
	      {
			
			ex.Excel_Format_Download(SERVICECD, NoOfRecords, response,session);
			
			
	      }
	 }
}


