package com.hdsoft.controllers;

import java.io.IOException;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Dashboard;
import com.hdsoft.Repositories.RTS006;
import com.hdsoft.Repositories.Users0001;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.MinIo_Modal;
import com.hdsoft.models.Reports_Model;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.solace.TopicPublisher;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class MinIo_Management 
{
	public JdbcTemplate Jdbctemplate;
			
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource){
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
		
	@Autowired
	public Reports_Model rm;
	
	@Autowired
	public Menu_Generation MG;
	
	@Autowired
	public Sysconfig sys;
	
	 @Autowired
	 public Session_Model Session;
	 
	 @Autowired
	 public MinIo_Modal MinIo;
	
    @RequestMapping(value = {"/MinIo/File/Upload"}, method = RequestMethod.GET)
    public ModelAndView Upload(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "MINIO", CHILD = "FUPLOAD";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/MinIo/File_Upload");  
	    	 
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
    
    @RequestMapping(value = {"/MinIo/File/Download"}, method = RequestMethod.GET)
    public ModelAndView Download(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "MINIO", CHILD = "FDOWNLOAD";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/MinIo/File_Download");  
	    	 
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
	
    
    @RequestMapping(value = {"/Datavision/MinIo/File/Upload"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Dashboard_User_Reports(@RequestParam("CHCODE") String CHCODE,  @RequestParam("SERVICECD") String SERVICECD,  @RequestParam("FILEURL") String FILEURL, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
		  
   	 	if(Session.IsSessionValid(session))
	    {
	   		 details = MinIo.Upload_File(FILEURL);
	    }
   	 
   	 	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
   	 	response.setHeader("Pragma","no-cache");
   	 	response.setHeader("Expires","0");
   	 	
   	 	return details.toString();
    }
    
    @RequestMapping(value = {"/Datavision/MinIo/File/Download"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String file_download(@RequestParam("CHCODE") String CHCODE,  @RequestParam("SERVICECD") String SERVICECD,  @RequestParam("FILEURL") String FILEURL, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
		  
   	 	if(Session.IsSessionValid(session))
	    {
	   		 details = MinIo.Download_File(FILEURL);
	    }
   	 
   	 	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
   	 	response.setHeader("Pragma","no-cache");
   	 	response.setHeader("Expires","0");
   	 	
   	 	return details.toString();
    }
    
}
