package com.hdsoft.controllers;

import java.io.IOException;

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
import org.springframework.web.servlet.ModelAndView;

import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class User_Deletion {
	
	public JdbcTemplate Jdbctemplate;
	
	 private static final Logger logger = LogManager.getLogger(Datavision_Admin.class);
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource){
		Jdbctemplate = new JdbcTemplate(Datasource);
	}

	

	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Session_Model Session;
	
	@Autowired
	public Menu_Generation MG;
	
	@RequestMapping(value = {"/Datavision/Admin/User_Deletion_Module"}, method = RequestMethod.GET)
    public ModelAndView Add_Entitlement(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "USRDEL";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("/Datavision/Admin/User_Deletion_Screen");
	    	 
	    	 mv.addObject("SUBORGCODE", sys.getSuborgcode());
	    	 
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
}
