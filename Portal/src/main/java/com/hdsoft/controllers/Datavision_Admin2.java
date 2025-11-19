package com.hdsoft.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.hdsoft.common.Common_Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Users00001;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.common.Token_System;
import com.hdsoft.models.Adminstration;
import com.hdsoft.models.Authorize;
import com.hdsoft.models.ONECERT_CREATION;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

//MUKESH LAST FILE UPDATE  - 26-09-2024 01:05 PM
//MUKESH LAST FILE UPDATE  - 26-09-2024 02:22 PM
//MUKESH LAST FILE UPDATE  - 21-10-2024 06:35 PM
//MUKESH LAST FILE UPDATE  - 22-10-2024 03:43 PM
//MUKESH LAST FILE UPDATE  - 23-10-2024 07:18 PM

@Controller
public class Datavision_Admin2 
{
	public JdbcTemplate Jdbctemplate;
	
	
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
	 
							  
	@RequestMapping(value = {"/Datavision/Admin/Account_Creation"}, method = RequestMethod.GET)
    public ModelAndView Account_Creation(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "ACCCRTN";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/Account_Creation");
	    	 
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
	
	 //Not in Use
	 @RequestMapping(value = {"/Datavision/OneCert/accounts/create/application"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Account_creation(Users00001 Info,  HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	    {	 
		     JsonObject details = new JsonObject();
		     
		     details = OC.Account_creation(Info, session); //ReqType': "APPLICATION"
		  
		     return details.toString();
	    }
	 
	 	
	 
	//List of Account shown in account update screen
	 @RequestMapping(value = {"/Datavision/Acount_update/list_of_account"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String List_of_acc_DD(HttpServletRequest request, HttpServletResponse response) throws IOException 
	    {	 
		     JsonObject details = new JsonObject();
		     
		     details = OC.List_of_acc_DD(); 
		     		     
		     return details.toString();
	    }
	 
	 
	 @RequestMapping(value = {"/Datavision/Acount_update/submodule_Module/Data_retrieve"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Retrieve_modulecode_data_EC(@ModelAttribute Users00001 Info, HttpServletRequest request, HttpServletResponse response) 
	    {	 
		   	JsonObject details = new JsonObject();
		   	   	
		   	details = OC.Retreive_submodule_data(Info);
		   	
		   	return details.toString();  	
	    }
	 
	 @RequestMapping(value = {"/Datavision/Acount_update/Audit_trail_report/insert"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Audit_trail_report(@ModelAttribute Users00001 Info, HttpServletRequest request, HttpServletResponse response, HttpSession session) 
	    {	 
		   	JsonObject details = new JsonObject();
		   	   	
		   	details = OC.Audit_trail_Report(Info, session);
		   	
		   	return details.toString();  	
	    }
	 
	 
	 /*
	 //Token Authorization	 
	 @RequestMapping(value = {"/Datavision/Token"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
       public @ResponseBody String Generate_Token(@RequestHeader("Authorization") String Authorization,  @RequestBody String Body_MSG, HttpServletRequest request,HttpServletResponse response) 
       {
		 
		 Common_Utils utils = new Common_Utils();
		 
   	     JsonObject details = new JsonObject();  
   	     
   	     JsonObject Request = utils.StringToJsonObject(Body_MSG);
   	     
   	     System.out.println("Token_Body_MSG " + Body_MSG);
   	     
   	     String client_id = Request.get("client_id").getAsString();
   	     String client_secret = Request.get("client_secret").getAsString();
   	     String channel_code = Request.get("channel_code").getAsString();
   	     
   	     details = OC.Generate_Datavision_Token(client_id, client_secret, "SCB", channel_code, Authorization);
   	
	      return details.toString();
      }
      */
      
	 
	//Token Authorization	 
		 @RequestMapping(value = {"/Datavision/Token"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	       public @ResponseBody ResponseEntity<String> Generate_URL_Token(
	    		  @RequestHeader(value = "Authorization", required=false) String Authorization,
	    		  @RequestParam("grant_type") String grant_type, @RequestParam("client_id") String client_id,
	    		  @RequestParam("client_secret") String client_secret,
	    		  HttpServletRequest request, HttpServletResponse response) 
	       {
			 			 			 
	   	     JsonObject details = new JsonObject();  
	   	     	   	     
	   	     String Suborg = sys.getSuborgcode();
	   	     String channel_code = "DVRTSIS"; //Onecert team only pass the following value thats why we were passing here default(grant_type, client_id, client_secret)
	   	    
	   	   
	   	     details = OC.Generate_Datavision_Token(client_id, client_secret, Suborg, channel_code, grant_type);
	   	
			   		  
		   		  if(!grant_type.equals("client_credentials")) {
			    	  
			    	  details.addProperty("status", "Failure");
					  details.addProperty("statusCode", "401");
					  details.addProperty("statusMessage", "Invalid Grant type passed!.");
	    		  
		   		     return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED);
	    		  
		    	  }else {	
	   	     		  		      
			    	  if(details.has("access_token")){
			    		  
			    		  return new ResponseEntity<String>(details.toString(), HttpStatus.OK);
			    	  }
			    	  
			    	  if(details.get("statusMessage").getAsString().equals("Invalid Client ID OR Invalid Client Secret!.")){ //Invalid Client ID OR Invalid Client Secret!.
			    		  
			    		  return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED);
			    	  }
			    	  		    	  
			    	  if(details.get("statusMessage").getAsString().equals("500")){//Invalid Grant type passed!
			    		  
			    		  return new ResponseEntity<String>(details.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
			    	  }
		    	  
		    	  }
				  
			return null;
	      }
		 
		 
	 @RequestMapping(value = {"/Datavision/onecert/testConnection"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody ResponseEntity<String> Onecert_testConnection(
	    		@RequestHeader("Authorization") String Authorization,
	    		@RequestHeader(value = "ReqType", required=false) String ReqType, HttpServletRequest request, HttpServletResponse response) throws IOException 
	    {	 
		 
		 JsonObject details = new JsonObject();
	     
	     JsonObject Headers_obj = new JsonObject();
	     
	     Common_Utils util = new Common_Utils();
	     
	     Headers_obj.addProperty("ReqType", util.ReplaceNull(ReqType));
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
			 if(flag == 1)
	    	 {
				 details.addProperty("status",  "Success");
				 details.addProperty("statusCode", "200"); 
				 details.addProperty("statusMessage", "Service is reachable and test connection is successful."); 
				 
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
				 
	    	 }
			 else if(flag == 0)
	    	 {
				 //details.addProperty("message", "token expired !!");
				 details.addProperty("status",  "Failure");
				 details.addProperty("statusCode", "401"); 
				 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
				 
				 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
				  
	    	 }
	    	 else
	    	 {
	    		 //details.addProperty("message", "invalid token !!");
	    		 details.addProperty("status",  "Failure");
				 details.addProperty("statusCode", "401"); 
				 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
				 
				 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
	    		 
	    	 }
		 
      }
		    		 		     
	 //creating user registration in Datavision Application
	 @RequestMapping(value = {"/Datavision/accounts/createAccount/Application"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Onecert_Account_creation(@RequestBody String Body_MSG, @RequestHeader(value = "ReqType", required=false) String ReqType,
	    		HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	    {	 
		 
		     JsonObject details = new JsonObject();
		     
		     JsonObject Headers_obj = new JsonObject();
		     
		     Common_Utils util = new Common_Utils();
		     
		     ReqType = util.ReplaceNull(ReqType);
		     
		     Headers_obj.addProperty("ReqType", util.ReplaceNull(ReqType));  
		     
		     String sesUserId = "";
		     
		     if(ReqType.equalsIgnoreCase("APPLICATION"))
		     {
		    	 try {
		    		 sesUserId = (String)session.getAttribute("sesUserId");
		    	 }
		    	 catch(Exception ex)  {   }
		     }
		     else
		     {
		    	 try {
			    	 String sql = "select userid from channel001 where chcode = ?";
					 
					 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
					 
					 sesUserId = lst.size() > 0 ? lst.get(0) : "";
			      }
		    	 catch(Exception ex)  {   }
		     }
		     
		     Headers_obj.addProperty("sesUserId", sesUserId);
		     
			 details = OC.Onecert_Account_creation(Body_MSG, Headers_obj); 
				
	    	 return details.toString();  
		    		 		     
	    }
	 
	 //ONECERT ACCOUNT CREATION
	 @RequestMapping(value = {"/Datavision/accounts/createAccount"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody ResponseEntity<String> Onecert_Account_creation(@RequestHeader("Authorization") String Authorization, @RequestBody String Body_MSG, @RequestHeader(value = "ReqType", required=false) String ReqType,  HttpServletRequest request, HttpServletResponse response) throws IOException 
	    {	 
		 	 System.out.println("Body_MSG - " +Body_MSG);
		 
		     JsonObject details = new JsonObject();
		     
		     JsonObject Headers_obj = new JsonObject();
		     
		     Common_Utils util = new Common_Utils();
		     
		     Headers_obj.addProperty("ReqType", util.ReplaceNull(ReqType));
		     
		     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
			 
			 if(flag == 1)
	    	 {
				 details = OC.Onecert_Account_creation(Body_MSG, Headers_obj); 
				 
				 if(details.get("StatusCode").getAsString().equals("200")){ //User account " + Acc_Nm + " got successfully created!.
			    		
					 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
				 }
				 if(details.get("StatusCode").getAsString().equals("404")){ //Application does not contain the requested entitlement " + roleName + " to perform create.
			    		
					 return new ResponseEntity<String>(details.toString(), HttpStatus.NOT_FOUND); 
				 }
				 if(details.get("StatusCode").getAsString().equals("400")){ //User account " + Acc_Nm + " already exists!.
			    		
					 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
				 }
				  
	    	 }
			 else if(flag == 0)
	    	 {
				//details.addProperty("message", "token expired !!");
				 details.addProperty("status",  "Failure");
				 details.addProperty("statusCode", "401"); 
				 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
				 
				 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
	    	 }
	    	 else
	    	 {
	    		//details.addProperty("message", "invalid token !!");
	    		 details.addProperty("status",  "Failure");
				 details.addProperty("statusCode", "401"); 
				 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
				 
				 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
	    	 }
			 		
			return null; 
	    }
	 
	
	@RequestMapping(value = {"/Datavision/Admin/Account_Update"}, method = RequestMethod.GET)
    public ModelAndView Update_Account(Model model, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "ACCUPD";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/Account_Update");
	    	 
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
	
	
	/* update_or_insert in Users00001*/	
	@RequestMapping(value = {"/Datavision/Account_update/update_or_insert"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Insert_All_data_report005_from_Report_field_validation(@ModelAttribute Users00001 Info ,@RequestHeader(value = "ReqType", required=false) String ReqType, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     JsonObject details = new JsonObject();
	     
	     Common_Utils util = new Common_Utils();
	     
	     ReqType = util.ReplaceNull(ReqType);
	     
	     String sesUserId = "";
	     
	     if(ReqType.equalsIgnoreCase("APPLICATION"))
	     {
	    	 try {
	    		 sesUserId = (String)session.getAttribute("sesUserId");
	    	 }
	    	 catch(Exception ex)  {   }
	     }
	     else
	     {
	    	 try {
		    	 String sql = "select userid from channel001 where chcode = ?";
				 
				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
				 
				 sesUserId = lst.size() > 0 ? lst.get(0) : "";
		    	 }
	    	 catch(Exception ex)  {   }
	     }
	     
	     details = OC.Insert_Field_validataion_data(Info, sesUserId);
	     								     
	     return details.toString();
    }
	
	@RequestMapping(value = { "/Datavision/Admin/Add_Entitlement"}, method = RequestMethod.GET)
    public ModelAndView Add_Entitlement(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "ADMINISTRATOR", CHILD = "ADDENT";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Admin/Add_Entitlement");
	    	 
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
	
	@RequestMapping(value = {"/HDPAY/Suggestions/Unblock/User_Id"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Suggestions_User_Id1(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		
	   	JsonArray User_Ids = new JsonArray();
	   	
	   	if(Session.IsSessionValid(session))
		    {
	   		String Search_Word = request.getParameter("term");
	    		
	    		User_Ids =  ud.User_Id_Unblock_Suggestions(Search_Word, request); 
		    }
	   	
			return User_Ids.toString();
    } 
	
	@RequestMapping(value = {"/Datavision/roles/addEntitlement"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> roles_addEntitlement(@RequestHeader("Authorization") String Authorization, @RequestBody String Body_MSG,  HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
	 	 //System.out.println("Body_MSG - " +Body_MSG);
	 
	     JsonObject details = new JsonObject();
	     
	     JsonObject Headers_obj = new JsonObject();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.Add_Entitlement(Body_MSG, Headers_obj);
			 
			 if(details.get("StatusCode").getAsString().equals("200")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 if(details.get("StatusCode").getAsString().equals("404")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.NOT_FOUND); 
			 }
			 if(details.get("StatusCode").getAsString().equals("400")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 }
			 
    	 }
		 else if(flag == 0)
    	 {
			//details.addProperty("message", "token expired !!");
			 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
		 	     	     	  
	     return null;
	     
    }
	
	
	@RequestMapping(value = {"/Datavision/roles/addEntitlement/application"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String roles_addEntitlement_application (HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
       JsonObject User_Ids = new JsonObject();
   	
   	   if(Session.IsSessionValid(session))
	    {
   		String Search_Word = request.getParameter("user");
   		
   		System.out.println(Search_Word);

   		User_Ids =  OC.Rolechg_User_Id_Check(Search_Word, request); 
	    }else {
	    	User_Ids.addProperty("Result", "Failed");
			User_Ids.addProperty("Message", "Please relogin for security concern");
	    }
   	
		return User_Ids.toString();
    }
	
	@RequestMapping(value = {"/Datavision/{Enable_disable}/{acc_name}"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> Enable_disable_user(@RequestHeader("Authorization") String Authorization, @PathVariable("Enable_disable") String Enable_disable, @PathVariable("acc_name") String acc_name, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 	 
	     JsonObject details = new JsonObject();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.Enable_Disable_user(Enable_disable, acc_name);
			 
			 if(details.get("StatusCode").getAsString().equals("200")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 if(details.get("StatusCode").getAsString().equals("400")){  
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 } 
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
		 	     	     	  	     	     	     	     	  
	     return null;
	     
    }
	
	@RequestMapping(value = {"/Datavision/deleteAccount/{acc_name}"}, method = RequestMethod.DELETE,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String Authorization, @PathVariable("acc_name") String acc_name, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 	 
	     JsonObject details = new JsonObject();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.deleteAccount(acc_name);
			 
			 if(details.get("StatusCode").getAsString().equals("200")){ //User account " + Acc_Nm + " got successfully created!.
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 if(details.get("StatusCode").getAsString().equals("400")){ //User account " + Acc_Nm + " already exists!.
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 } 
    	 }
		 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
		 	     	     	  	     	     	     	     	  	  
	     return null;    
    }
	
	//ULA API Reports
	//All Accounts
	@RequestMapping(value = {"/Datavision/accounts/all"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> List_of_Account(@RequestHeader("Authorization") String Authorization,HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 	 
	     JsonObject details = new JsonObject();
	     
	     
	     
	     int currentPage = 0; //Integer.parseInt(str_currentPage);
	     int pageSize = 0;  //Integer.parseInt(str_pageSize);
	     
	     try
	     {
	    	 String str_currentPage = request.getParameter("currentPage").toString();
		     String str_pageSize = request.getParameter("pageSize").toString();
		     
	    	 currentPage = Integer.parseInt(str_currentPage);
	    	 pageSize = Integer.parseInt(str_pageSize);
	     }
	     catch(Exception ex)
	     {
	    	 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "400"); 
			 details.addProperty("statusMessage", "Invalid currentpage or pageSize"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST);
	     }
	     
	     //System.out.println("currentPage -" + currentPage);
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.List_of_Account(currentPage, pageSize);
			 
			 if(details.has("accounts")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 if(details.has("StatusCode") && details.get("StatusCode").getAsString().equals("400")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 }
    	 }
		 else if(flag == 0)
    	 {
			//details.addProperty("message", "token expired !!");
			 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
		 	     	     	  	     	     	     	     	  	  	     	  
	     return null;
	     
    }
	
	//Single Accounts
	@RequestMapping(value = {"/Datavision/accounts/single/{acc_name}"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> Single_Account(@RequestHeader("Authorization") String Authorization, @PathVariable("acc_name") String acc_name, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 	 
	     JsonObject details = new JsonObject();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.Single_Account(acc_name);
			 
			 if(details.has("accessRoles")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 else if(details.get("StatusCode").getAsString().equals("404")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.NOT_FOUND); 
			 }
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
	  
	     return null;
	     
    }
	
	//All Entitlement
	@RequestMapping(value = {"/Datavision/entitlement/all"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> all_entitlement(@RequestHeader("Authorization") String Authorization, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 	 
	     JsonObject details = new JsonObject();
	     
	     String currentPage = request.getParameter("currentPage").toString();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.all_entitlement(currentPage);
			 
			 if(details.has("entitlements")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 else if(details.get("StatusCode").getAsString().equals("404")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.NOT_FOUND); 
			 }
			 else if(details.get("StatusCode").getAsString().equals("400")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 }
    	 }
		 else if(flag == 0)
    	 {
			//details.addProperty("message", "token expired !!");
			 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
	     	  
	     return null;
	     
    }
	
	//Single Entitlement
	@RequestMapping(value = {"/Datavision/entitlement/single/{Entitlement_name}"}, method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> Single_entitlement(@RequestHeader("Authorization") String Authorization, @PathVariable("Entitlement_name") String Entitlement_name,HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 	 
	     JsonObject details = new JsonObject();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.Single_entitlement(Entitlement_name);
			
			 if(details.get("StatusCode").getAsString().equals("200")){
				 
				 details.remove("StatusCode");
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 else if(details.get("StatusCode").getAsString().equals("404")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.NOT_FOUND); 
			 }
			 else if(details.get("StatusCode").getAsString().equals("400")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 }
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
	     	  
	     return null;
	     
    }
		
	//Remove Entitlement
	@RequestMapping(value = {"/Datavision/entitlement/remove"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> Remove_entitlement(@RequestHeader("Authorization") String Authorization, @RequestBody String Body_MSG,  HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
	 	 //System.out.println("Body_MSG - " +Body_MSG);
	 
	     JsonObject details = new JsonObject();
	     
	     JsonObject Headers_obj = new JsonObject();
	     
	     int flag = tk.ValidateJWTToken("SCB", "DVRTSIS", Authorization);
		 
		 if(flag == 1)
    	 {
			 details = OC.Remove_entitlement(Body_MSG, Headers_obj);
			 
			 if(details.get("StatusCode").getAsString().equals("200")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.OK); 
			 }
			 if(details.get("StatusCode").getAsString().equals("404")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.NOT_FOUND); 
			 }
			 if(details.get("StatusCode").getAsString().equals("400")){ 
		    		
				 return new ResponseEntity<String>(details.toString(), HttpStatus.BAD_REQUEST); 
			 }
    	 }
		 else if(flag == 0)
    	 {
			//details.addProperty("message", "token expired !!");
			 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
    	 else
    	 {
    		//details.addProperty("message", "invalid token !!");
    		 details.addProperty("status",  "Failure");
			 details.addProperty("statusCode", "401"); 
			 details.addProperty("statusMessage", "Access Token validation has failed. Invalid Access Token!"); 
			 
			 return new ResponseEntity<String>(details.toString(), HttpStatus.UNAUTHORIZED); 
    	 }
	     	     	  
	     return null;
	     
    }
	
	@RequestMapping(value = {"/Datavision/ROLE_QUEUECHECKER"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String QUEUECHECKER_Role(HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	 JsonObject details = new JsonObject();

   	 	 if(Session.IsSessionValid(session))
	     {
	    	 JsonObject formDTO = new JsonObject();
	    	 
	    	 JsonObject resultDTO  = new JsonObject();
					 
			 try
			 {
				 formDTO.addProperty("tuserid", request.getParameter("tuserid"));
				 formDTO.addProperty("torgcd", request.getParameter("torgcd"));
				 formDTO.addProperty("pgmID", request.getParameter("pgmID"));
				
				 resultDTO = ad.QUEUECHECKER(formDTO);
				 
				 details.addProperty("mode", resultDTO.get("mode").getAsString());
				 resultDTO = ad.QUEUECHECKER2(formDTO);
				
							
				details.addProperty("STATUS", resultDTO.get("STATUS").getAsString());
				details.addProperty("ROLECODE", resultDTO.get("ROLECODE").getAsString());
				details.addProperty("SucFlg", resultDTO.get("SucFlg").getAsString());
				
				details.addProperty("Result", "Success");
				details.addProperty("Message", "Success");					
			}
			catch(Exception e)
			{
				details.addProperty("Result", "Failed");
				details.addProperty("Stscode", "HP005");
				details.addProperty("Message", "User Not Found");
				
				
				
				//logger.debug("Exception when Queue checker ::::: "+e.getLocalizedMessage());
			}
	     }
   	 else
   	 {
   		 details.addProperty("Result", "Failed");
			 details.addProperty("Message", "Please re-login for security concerns");
   	 }
   	 
	    return details.toString();
    }

	 @RequestMapping(value = {"/Datavision/Role_Update_User"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Suggestions_User_Id2(@RequestHeader(value = "ReqType", required=false) String ReqType, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	    {	 
			
		   	JsonObject User_Ids = new JsonObject();
		   	
		   	if(Session.IsSessionValid(session))
			 {
		   		   String userid = request.getParameter("tuserid");
		   		   String rolecd = request.getParameter("trolecd");
		    		
		   		   Common_Utils util = new Common_Utils();
			     
			       ReqType = util.ReplaceNull(ReqType);
			       
			       String sesUserId = "";
				     
				     if(ReqType.equalsIgnoreCase("APPLICATION"))
				     {
				    	 try {
				    		 sesUserId = (String)session.getAttribute("sesUserId");
				    	 }
				    	 catch(Exception ex)  {   }
				     }
				     else
				     {
				    	 try {
					    	 String sql = "select userid from channel001 where chcode = ?";
							 
							 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
							 
							 sesUserId = lst.size() > 0 ? lst.get(0) : "";
					    	 }
				    	 catch(Exception ex)  {   }
				     }
			     
		    	   User_Ids =  ud.Update_Role_Code(userid, rolecd, sesUserId); 
		    }
		   	
				return User_Ids.toString();
	    } 
  
	 
	 // suggestion
	 
	 @RequestMapping(value = {"/Datavision/suggestions/AccUpdate"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Suggestions_APIcode_Retrieve(@RequestParam("term") String term ,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	    {	 
			 JsonArray details = new JsonArray();
	   	   	
		   	 details = OC.Get_Account_Codes(term);  
		   	 
		   	 System.out.println("details"+details);
		   	
		   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		     response.setHeader("Pragma","no-cache");
		     response.setHeader("Expires","0");
		     
		   	 return details.toString();  	
	    }
	 
	 @RequestMapping(value = {"/Datavision/Role_Delete_User"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Suggestions_User_Id3(@RequestHeader(value = "ReqType", required=false) String ReqType, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	    {	 
			
		   	JsonObject User_Ids = new JsonObject();
		   	
		   	if(Session.IsSessionValid(session))
			 {
		   		   String userid = request.getParameter("tuserid");
		   		   //String rolecd = request.getParameter("trolecd");
		   		   
		   		Common_Utils util = new Common_Utils();
			     
			     ReqType = util.ReplaceNull(ReqType);
			     
		   		 String sesUserId = "";
			     
			     if(ReqType.equalsIgnoreCase("APPLICATION"))
			     {
			    	 try {
			    		 sesUserId = (String)session.getAttribute("sesUserId");
			    	 }
			    	 catch(Exception ex)  {   }
			     }
			     else
			     {
			    	 try {
				    	 String sql = "select userid from channel001 where chcode = ?";
						 
						 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
						 
						 sesUserId = lst.size() > 0 ? lst.get(0) : "";
				    	 }
			    	 catch(Exception ex)  {   }
			     }
		    		
		    		User_Ids =  ud.Delete_Code(userid, sesUserId); 
		    }
		   	
		   	     
		   	System.out.println("result"+User_Ids.toString());
				return User_Ids.toString();
	    } 
	
		 
	
}
	 

