package com.hdsoft.controllers;

import java.io.IOException;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Dashboard;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.common.Token_System;
import com.hdsoft.models.Adminstration;
import com.hdsoft.models.Authorize;
import com.hdsoft.models.ONECERT_CREATION;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class Datavision_Admin 
{
	public JdbcTemplate Jdbctemplate;
	
	 private static final Logger logger = LogManager.getLogger(Datavision_Admin.class);
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource){
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Adminstration ad;
	
	@Autowired
	public Authorize ud;
	 
	@Autowired
	public Menu_Generation MG;
	
	@Autowired
	public Sysconfig sys;
	
	 @Autowired
	 public Session_Model Session;
	 
	 @Autowired
		public ONECERT_CREATION OC;
		
		@Autowired
		public Token_System tk;
	 
	@RequestMapping(value = {"/Datavision/Admin/User_register"}, method = RequestMethod.GET)
    public ModelAndView User_Register(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "USERREG";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/User_reg");
	    	 
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
	
	@RequestMapping(value = {"/Datavision/User_Registration"}, method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String User_Registration_(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		JsonObject details = new JsonObject();
   	 
   	 	if(Session.IsSessionValid(session))
   	 	{
   	 		JsonObject InaddPropertys = new JsonObject();
	   
			InaddPropertys.addProperty("torgcd", request.getParameter("torgcd") != null ? request.getParameter("torgcd") : "");System.out.println(request.getParameter("torgcd"));
			InaddPropertys.addProperty("tuserid", request.getParameter("tuserid") != null ? request.getParameter("tuserid") : "");System.out.println(request.getParameter("tuserid"));
			InaddPropertys.addProperty("tusernme", request.getParameter("tusernme") != null ? request.getParameter("tusernme") : "");
			InaddPropertys.addProperty("tbirthdate", request.getParameter("tbirthdate") != null ? request.getParameter("tbirthdate") : "");
			InaddPropertys.addProperty("tmobile", request.getParameter("tmobile") != null ? request.getParameter("tmobile") : "");
			InaddPropertys.addProperty("temail", request.getParameter("temail") != null ? request.getParameter("temail") : "");
			InaddPropertys.addProperty("tcomaddr", request.getParameter("tcomaddr") != null ? request.getParameter("tcomaddr") : "");
			InaddPropertys.addProperty("tconfirmpwd", request.getParameter("tconfirmpwd") != null ? request.getParameter("tconfirmpwd") : "");
			InaddPropertys.addProperty("trolecd", request.getParameter("trolecd") != null ? request.getParameter("trolecd") : "");
			InaddPropertys.addProperty("tregdate", request.getParameter("tregdate") != null ? request.getParameter("tregdate") : "");
			InaddPropertys.addProperty("branchcd", request.getParameter("branchcd") != null ? request.getParameter("branchcd") : "");
			 
			InaddPropertys.addProperty("hashedPassword", request.getParameter("hashedPassword") != null ? request.getParameter("hashedPassword") : "");
			InaddPropertys.addProperty("randomSalt", request.getParameter("randomSalt") != null ? request.getParameter("randomSalt") : "");
			
			String suborgcode = sys.getSuborgcode();
			
			InaddPropertys.addProperty("mode", "I");
			InaddPropertys.addProperty("suborgcode", suborgcode);
			
			if(session.getAttribute("sesUserId").toString().trim() != null || session.getAttribute("sesUserId").toString().trim() != "" )
			{ 
				InaddPropertys.addProperty("sesUserId", session.getAttribute("sesUserId").toString().trim() ) ;
				InaddPropertys.addProperty("sesMcontDate", session.getAttribute("sesMcontDate").toString().trim() ) ;
				InaddPropertys.addProperty("sesDomainID", session.getAttribute("sesDomainID").toString().trim() ) ;
			}
			 
			InaddPropertys.addProperty("LOG_DOMAIN_ID", session.getAttribute("sesDomainID").toString());
			InaddPropertys.addProperty("LOG_USER_ID", session.getAttribute("sesUserId").toString());
			InaddPropertys.addProperty("LOG_USER_IP", request.getRemoteAddr());
			InaddPropertys.addProperty("programid", "userregistration");
			
			logger.debug("<<<<<<<<<<<<<<<<<<< Entering Into User Registeration >>>>>>>>>>>>>>>>>>>");
			
			String User_Id = InaddPropertys.get("tuserid").getAsString();
			
			String pgid = InaddPropertys.get("programid").getAsString();
			
			String AUTHQMAINPK = sys.getSuborgcode() + "|" + User_Id;
	         
			JsonObject outaddPropertys = ad.userqueuecheck(pgid,AUTHQMAINPK);
			
			String val = outaddPropertys.get("Result").getAsString();
			
			if(val.equals("Success")) 
			{
				details.addProperty("Result", "Success");
				details.addProperty("Message", "Record Inserted Successfully !!");
			}
			else
			{
				details = outaddPropertys;

				return details.toString();
			}
			outaddPropertys = ad.checkuser(User_Id);
			
			
			
			if(val.equals("Success")) 
			{
				details.addProperty("Result", "Success");
				details.addProperty("Message", "Record Inserted Successfully !!");
			}
			else
			{
				details = outaddPropertys;

				return details.toString();
			}
			
		    outaddPropertys = ad.updateValues(InaddPropertys);
			val = outaddPropertys.get("Result").getAsString();
			
			if(val.equals("Success")) {
				details.addProperty("Result", "Success");
				details.addProperty("Message", "Record Inserted Successfully !!");
			}else{
				details.addProperty("Result", "Failed");
				details.addProperty("Message", "Something Went Wrong !!");
			}
			 
			System.out.println("<<<<<<<<<<<<<<<<< End Of Main Step >>>>>>>>>>>>>>>");

   	 	}
   	 else
   	 {
   		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
   	 }
   	 
   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
     response.setHeader("Pragma","no-cache");
     response.setHeader("Expires","0");

   	 	
	     return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/Password_Reset"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Password_Reset_(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 JsonObject details = new JsonObject();
   	 
   	JsonObject InaddPropertys = new JsonObject();

   	 if(Session.IsSessionValid(session))
	     {
	 		 try
	 		 {
	 			InaddPropertys.addProperty("torgcd", request.getParameter("torgcd"));
	 			InaddPropertys.addProperty("tuserid", request.getParameter("tuserid"));
	 			InaddPropertys.addProperty("hashedPassword", request.getParameter("hashedPassword"));
	 			InaddPropertys.addProperty("randomSalt", request.getParameter("randomSalt"));
	 			
	 			JsonObject resultDTO = ad.updateValues_Password(InaddPropertys, session);
	 			
	 			String val = resultDTO.get("sucFlg").getAsString();
	 			
	 			if(val.equals("1"))
				{
					details.addProperty("Result", "Success");
					details.addProperty("Message", "Succesfully Updated !!");
				}
				else
				{
					details.addProperty("Result", "Failed");
					details.addProperty("Message", "Something went Wrong !!");
				}	
			}
			catch (Exception e)
			{
				details.addProperty("Result", "Failed");
				details.addProperty("Message", e.getLocalizedMessage()); e.printStackTrace();
			}
	     }
   	 else
   	 {
   		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
   	 }
   	 
   	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma","no-cache");
    response.setHeader("Expires","0");

   	 
	     return details.toString();
    } 
	 @RequestMapping(value = {"/Datavision/Info/Get_auth001_Info"}, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Get_auth001_Info(Model model , HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, ParseException 
     {	 
    	 JsonObject details = new JsonObject();
    	 
    	 if(Session.IsSessionValid(session))
 	     {
    		 String pgmid = request.getParameter("pgmid");
        	 
        	 details = ad.Get_auth001_Info(pgmid);
 	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 }
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");


	     return details.toString();
     }
     
     @RequestMapping(value = {"/Datavision/Info/pk/Get_auth003_Info"}, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Get_auth003_Info_by_pk(Model model , HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, ParseException 
     {	 
    	 JsonObject details = new JsonObject();
    	 
    	 if(Session.IsSessionValid(session))
 	     {
    		 String pk = request.getParameter("pk");
    		 
    		 logger.debug("PRIMARY KEY ::::::::::::::::"+pk);
        	 
        	 details = ad.Get_auth003_Info_by_pk(pk);	
 	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 }
    	
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return details.toString();
     }
     
     @RequestMapping(value = {"/Datavision/Suggestions/User_Id_Check"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Suggestions_User_Id(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	JsonArray User_Ids = new JsonArray();
    	if(Session.IsSessionValid(session))
	    {
    		String Search_Word = request.getParameter("term");
     		System.out.println(Search_Word);
     		User_Ids =  ad.User_Id_Suggestions(Search_Word, request); 
	    }
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

    	
		return User_Ids.toString();
     } 
     
     @RequestMapping(value = {"/Datavision/Suggestions/User_Id"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Suggestions_UserId(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject User_Ids = new JsonObject();
    	
    	if(Session.IsSessionValid(session))
	    {
    		String Search_Word = request.getParameter("user");
    		
    		System.out.println(Search_Word);

    		User_Ids =  ad.User_Id_Check(Search_Word, request); 
	    }else {
	    	User_Ids.addProperty("Result", "Failed");
			User_Ids.addProperty("Message", "Please relogin for security concern");
	    }
    	
    	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

    	
		return User_Ids.toString();
     } 
     @RequestMapping(value = {"/Datavision/Suggestions/User_Id/Test"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Suggestions_User_Id_Test(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	JsonArray User_Ids = new JsonArray();
    	
    	System.out.println("Calling API.....");
    	
    	
    		String Search_Word = request.getParameter("term");
     		
     		User_Ids =  ad.User_Id_Suggestions(Search_Word, request); 
	 
     		System.out.println("User_Ids ::....."+User_Ids);
 		
     	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
   	     response.setHeader("Pragma","no-cache");
   	     response.setHeader("Expires","0");

     		
		return User_Ids.toString();
     } 
     
     @RequestMapping(value = {"/Datavision/Suggestions/ORGCODE"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Suggestions_ORGCODE(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	JsonArray Org_Codes = new JsonArray();
    	
    	if(Session.IsSessionValid(session))
	    {
    		String Search_Word = request.getParameter("term");
     		Org_Codes =  ad.Org_Code_Suggestions(Search_Word, request); 
	    }
    	
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

    	
		return Org_Codes.toString();
     } 
     
     @RequestMapping(value = {"/Datavision/Suggestions/BRANCHCODE_Value"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Suggestions_BRANCHCODE(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject Branch_Codes = new JsonObject();
    	
    	if(Session.IsSessionValid(session))
	    {
    		
    		Branch_Codes = ad.All_Branch_Code();
    		
    		logger.debug(Branch_Codes);
    		
	    }
    	
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

    	
		return Branch_Codes.toString();
     } 
     
     @RequestMapping(value = {"/Datavision/Suggestions/ROLECODE"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Suggestions_ROLECODE(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	JsonArray Branch_Codes = new JsonArray();
    	
    	if(Session.IsSessionValid(session))
	    {
    		String Search_Word = request.getParameter("term");
     		
     		Branch_Codes =  ad.Role_Code(Search_Word, request); 
	    }
 		
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

		return Branch_Codes.toString();
     } 
     
     @RequestMapping(value = {"/Datavision/QUEUECHECKER"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String QUEUECHECKER(HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject details = new JsonObject();
 
    	 if(Session.IsSessionValid(session))
	     {
	    	 JsonObject formDTO = new JsonObject();
	    	 
	    	 JsonObject resultDTO  = new JsonObject();
					 
			 try
			 {
				 formDTO.addProperty("tuserid", request.getParameter("tuserid"));
				 formDTO.addProperty("torgcd", sys.getSyscode());
				 formDTO.addProperty("pgmID", request.getParameter("pgmID"));
				
				 resultDTO = ad.QUEUECHECKER(formDTO);
				 
				 logger.debug(resultDTO);
				 details.addProperty("mode", resultDTO.get("mode").getAsString());
				 resultDTO = ad.QUEUECHECKER2(formDTO);
				
				 logger.debug(resultDTO);
				 
									
				details.addProperty("STATUS", resultDTO.get("STATUS").getAsString());
				details.addProperty("SucFlg", resultDTO.get("SucFlg").getAsString());
				
				details.addProperty("Result", "Success");
				details.addProperty("Message", "Success");					
			}
			catch(Exception e)
			{
				details.addProperty("Result", "Failed");
				details.addProperty("Message", e.getLocalizedMessage());
				
				logger.debug("Exception when Queue checker ::::: "+e.getLocalizedMessage());
			}
	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 }
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	    return details.toString();
     }
     
     @RequestMapping(value = {"/Datavision/Block_Unblock_User"}, method = RequestMethod.POST , produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Block_Unblock_User(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject details = new JsonObject();
    	 
    	 if(Session.IsSessionValid(session))
	     {
	    	 JsonObject InaddPropertys = new JsonObject();
	   
	    	 InaddPropertys.addProperty("tuserid", request.getParameter("tuserid"));
	    	 InaddPropertys.addProperty("tbublock", request.getParameter("tbublock"));
	    	 InaddPropertys.addProperty("mode", request.getParameter("mode"));
	    	   	
	    	 String suborgcode = sys.getSuborgcode();
	    	 
	    	 
	    	 InaddPropertys.addProperty("suborgcode", suborgcode);
	    	 
	    	 if(session.getAttribute("sesUserId").toString().trim() != null || session.getAttribute("sesUserId").toString().trim() != "" )
	    	 { 
	    		 InaddPropertys.addProperty("sesUserId", session.getAttribute("sesUserId").toString().trim() ) ;
	    		 InaddPropertys.addProperty("sesMcontDate", session.getAttribute("sesMcontDate").toString().trim() ) ;
	    		 InaddPropertys.addProperty("sesDomainID", session.getAttribute("sesDomainID").toString().trim() ) ;
			 }
	    	 
	    	 InaddPropertys.addProperty("userIp", session.getAttribute("sesUserId").toString());
	    	 InaddPropertys.addProperty("programid", "unblockuserid");
	
	    	 JsonObject outaddPropertys = ad.updateValues_for_UnblockUser(InaddPropertys);
	    	 String val = outaddPropertys.get("Result").getAsString();
	    	 if(val.equals("Success")) 
	    	 {
	    		 details.addProperty("Result", "Success");
				 details.addProperty("Message", "Record updated Successfully !!");
	    	 }
	    	 else
	    	 {
	    		 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "Something Went Wrong !!");
	    	 }
	    	 
	    	 System.out.println("<<<<<<<<<<<<<<<<< End Of Main Step >>>>>>>>>>>>>>>");
	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 }
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return details.toString();
     }
     
     @RequestMapping(value = {"/Datavision/Admin/Authorize"}, method = RequestMethod.GET)
     public ModelAndView Setting_Approval(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/Admn_Appr"); 
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, "AUTHORIZATION", "ADMINAPR"));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	   
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }
    
     @RequestMapping(value = {"Datavision/Admin_Approval"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Admin_Approval(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject details = new JsonObject();
 
    	 if(Session.IsSessionValid(session))
	     {
	    	 JsonObject formDTO = new JsonObject();
	    	 
	    	 JsonObject resultDTO  = new JsonObject();
					 
			 try
			 {
				formDTO.addProperty("txtArgs", request.getParameter("txtArgs"));
				formDTO.addProperty("LOG_USER_IP", request.getRemoteAddr());
				
				logger.debug(formDTO);
				
				resultDTO = ud.updateValues(formDTO);
				logger.debug(resultDTO);
				String val = resultDTO.get("sucFlg").getAsString();
				String val1 = resultDTO.get("result").getAsString();
				
				logger.debug(val+"****"+val1);
				
				logger.debug(resultDTO);
				
				if(val.equals("1") && !val1.equals("ERROR"))
				{
					
					logger.debug(resultDTO);
					
					logger.debug(details);
					
					details.addProperty("Result", "Success");
					details.addProperty("Message", "Succesfully Updated !!");
				}
				else
				{
					details.addProperty("Result", "Failed");
					details.addProperty("Message", resultDTO.get("result").getAsString()); 
				}
			}
			catch(Exception e)
			{
				details.addProperty("sucFlag", "0");
				
				details.addProperty("Result", "Failed");
				details.addProperty("Message", e.getLocalizedMessage());
				
				logger.debug("Exception when Admin Approval ::::: "+e.getLocalizedMessage());
			}
	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 }
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	    return details.toString();
     }
     
     @RequestMapping(value = {"/Datavision/validateAdmPgmId"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String validateAdmPgmId(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject details = new JsonObject();
    	 
    	
    	 if(Session.IsSessionValid(session))
	     {
	    	 String auguments = request.getParameter("auguments");
	 		
	    	 logger.debug(auguments);
	    	 JsonObject res = ad.validateAdmPgmId(auguments);
	    	 
	    	 logger.debug("PGMUD :::::::::::: "+res.get("PGMIDs"));
	    	 
	    	 String val = res.get("sucFlg").getAsString();
	    	 logger.debug(val);
	    	 
	    	 if(val.equals("1"))
	    	 {
	    		 details.addProperty("sucFlg", "1");
	    		 
	    		 details.add("PGMID1", res.get("PGMIDs"));
	    		 logger.debug("PGMUD :::::::::::: "+details);
	    		 details.addProperty("CATEGORY", res.get("CATEGORY").getAsString());
	    		 
	    		 details.addProperty("Result", "Success");
				 details.addProperty("Message", "Record updated Successfully!");
	    	 }
	    	 else
	    	 {
	    		 details.addProperty("Result", "Failed");
			     details.addProperty("Message", "SOMEthing went Wrong !!!");
	    	 }
	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 } 
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return details.toString();
     }
     
     @RequestMapping(value = {"/Datavision/ADMIN/ResetPass"}, method = RequestMethod.GET)
     public ModelAndView Profile_Reset(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "PASSRESET";
	     
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/reset_pwd");
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	   
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }
     
     @RequestMapping(value = {"/Datavision/ADMIN/ProfUpload"}, method = RequestMethod.GET)
     public ModelAndView Profile_Image(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "PROIMG";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Admin/Prof_Imge_Upload"); 
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	   
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }
	 
	 @RequestMapping(value = {"/Datavision/Profile_Image_Upload"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Profile_Image_Upload(@RequestParam("Image") CommonsMultipartFile Image, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 JsonObject details = new JsonObject();
    	 
    	 JsonObject InaddPropertys = new JsonObject();

    	 if(Session.IsSessionValid(session))
	     {
	 		 try
	 		 {
	 			InaddPropertys.addProperty("hiddomainId", session.getAttribute("sesDomainID").toString());
	 			InaddPropertys.addProperty("hiduserId", session.getAttribute("sesUserId").toString());
	 			InaddPropertys.addProperty("LOG_DOMAIN_ID", session.getAttribute("sesDomainID").toString());
	 			InaddPropertys.addProperty("LOG_USER_ID", session.getAttribute("sesUserId").toString());
	 			
	 			logger.debug(InaddPropertys);
	 				
	 			JsonObject resultDTO = ad.updateValues(Image,  InaddPropertys);
	 			
	 			logger.debug(resultDTO);
	 			
	 			
	 			
	 			if(resultDTO.get("sucFlag").getAsString().equals("1"))
				{
					
					String Photo = ad.loadImage(session.getAttribute("sesDomainID").toString(), session.getAttribute("sesUserId").toString(), request);
					 
					session.setAttribute("sess_user_photo", Photo);
					
					details.addProperty("Result", "Success");
					details.addProperty("Message", "Succesfully Updated !!");
				}
				else
				{
					details.addProperty("Result", "Failed");
					details.addProperty("Message", "Something went Wrong !!");
				}	
			}
			catch (Exception e)
			{
				details.addProperty("Result", "Failed");
				details.addProperty("Message", e.getLocalizedMessage()); e.printStackTrace();
			}
	     }
    	 else
    	 {
    		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
    	 }
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return details.toString();
     }
	 @RequestMapping(value = {"/Datavision/Admin/User_Password_Reset"}, method = RequestMethod.GET)
     public ModelAndView Setting_Uswer_Passrst(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "PASSRESET";
	     
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/Pwd_Reset"); 
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	   
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }


	@RequestMapping(value = {"/Datavision/ADMIN/Block_unblock"}, method = RequestMethod.GET)
	public ModelAndView Block_unblock_User(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	{	 
		     ModelAndView mv = new ModelAndView();
		     
		     String PARENT = "AUTHORIZATION", CHILD = "BLKUNBLK";
		     
		     if(Session.IsSessionValid(session))
		     {
		    	 mv.addObject("Title", "Block / Unblock User");
		     
		    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
		     	 
		     	 mv.setViewName("Datavision/Admin/Blk_Unblk_User"); 
		     }
		     else
		     {
		    	 mv.setViewName("redirect:/login");
		     }
	
		     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		     response.setHeader("Pragma","no-cache");
		     response.setHeader("Expires","0");
	
		     return mv;
	}
	
	@RequestMapping(value = {"/Datavision/API_Configuration"}, method = RequestMethod.GET)
	public ModelAndView Configuration_API_Configurations(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	{	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "CONFIG", CHILD = "APICONFIG";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	  mv.setViewName("Datavision/Configuration/API_Config");
		     
	    	  mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	
	     return mv;
	}
	


	
	@RequestMapping(value = { "/Datavision/Event_Run" }, method = RequestMethod.GET)
    public ModelAndView Configuration_Event_Run(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "SUPTECH", CHILD = "EVERUN";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Configuration/Evnt_Run");  
	    	 
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
	
	@RequestMapping(value = { "/Datavision/Event_Creation" }, method = RequestMethod.GET)
    public ModelAndView Configuration_Event_Creation(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "CONFIG", CHILD = "EVECRE";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Configuration/Evnt_Cret");  
	    	 
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
	
	
	
    
	 @RequestMapping(value = {"/Datavision/Admin/Password_Reset_For_Users"}, method = RequestMethod.GET)
     public ModelAndView Setting_Uswers_Passrst(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "PASSRESET_USER";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Admin/Pwd_Rest_users"); 
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	   
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }
	 
	 
	 
	 
	
}
	 
	 



