package com.hdsoft.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.RTS003;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.Configuration_Modal1;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.models.Upload_Excel_Model;
import com.hdsoft.models.Upload_Excel_Multiparent;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class Upload_Excel_Controller {
	
	public JdbcTemplate Jdbctemplate;
	
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
	 public Upload_Excel_Model upd;
	 
	 @Autowired
	 public Upload_Excel_Multiparent upd_mult;
	
	
	@RequestMapping(value = {"/Datavision/Upload_Excel"}, method = RequestMethod.GET)
    public ModelAndView Configuration_Upload_Excel(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "SUPTECH", CHILD = "UPLOAD";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Configuration/Uplod_xcel");
		     
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
	
	@RequestMapping(value = {"/Datavision/Excel_File_Upload"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Upload_File_Report(@ModelAttribute RTS003 Info,  HttpServletRequest request, HttpServletResponse response,HttpSession session) 
    {	 
	   	 JsonObject details = new JsonObject();
	   	 
		 System.out.println("Retrieve"+Info.getFile());
	   	
		 String sign = "select SIGNPAYLOAD from webservice001 where SERVICECD=?";
		 
		 String SignPay=Jdbctemplate.queryForObject(sign, new Object[] {Info.getServicecd()}, String.class);
		 
		 System.out.println("SignPay"+SignPay);
		 
		 if(SignPay != null) {
			 
			 details = upd_mult.Project_Excel_Upload(Info,session);
			 
		 }
		 else {
	         	details = upd.Project_Excel_Upload(Info,session);
		 }
		
	   	
	     System.out.println("Output"+details);
	   	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }
}
