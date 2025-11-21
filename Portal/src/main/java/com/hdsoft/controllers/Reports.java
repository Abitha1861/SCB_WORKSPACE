package com.hdsoft.controllers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.jasper.tagplugins.jstl.core.If;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hdsoft.Repositories.FIU_Menu;
import com.hdsoft.Repositories.RTS003;
import com.hdsoft.Repositories.RTS006;
import com.hdsoft.Repositories.Users0001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.File_handling;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.FIU_EDMP;
import com.hdsoft.models.FileIT_Modal;
import com.hdsoft.models.Reports_Model;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class Reports
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource){
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Reports_Model rm;
	
	@Autowired
	public FIU_EDMP FIU_EDMP;
	
	@Autowired
	public Menu_Generation MG;
	
	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Session_Model Session;
	 
	@Autowired
	public FileIT_Modal FileIT;
	
    @RequestMapping(value = {"Datavision/Rtsis/Reports/View"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Dashboard_User_Reports(@ModelAttribute RTS006 info,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	 JsonObject details = new JsonObject();
		  
   	 	 if(Session.IsSessionValid(session))
	     {
   	 		details = rm.Rtsis_Reportings();  
   		 
   	 		System.out.println("RETRIEVED"+ details);
	     
	     }
   	 
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
		 return details.toString();
    }
    
    @RequestMapping(value = {"/Datavision/Rtsis_Reports"}, method = RequestMethod.GET)
    public ModelAndView RTSIS_REPORT(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "RTSISREP";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Reports/Rtsis_rpts");  
	    	 
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
    
    @RequestMapping(value = {"/Datavision/Rtsis/Report_filter"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String ReportFilter(@ModelAttribute RTS006 info,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 JsonObject details = new JsonObject();
		  
   	 if(Session.IsSessionValid(session))
	 {
   		 details = rm.Report_filter(info);  
   		 
   	 }
   	 
   	 if (details.has("Info") && details.getAsJsonArray("Info").size() == 0) {
       
   		 details.remove("Info");
     
   	 }
	     return details.toString();
    }
	
    @RequestMapping(value = {"/Datavision/User_Reports"}, method = RequestMethod.GET)
    public ModelAndView User_Report_Dashboard(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "USERREPORT";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("Datavision/Reports/User_Rpts"); 
	    	 
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
    
    @RequestMapping(value = {"/Datavision/User/Reports/View"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Dashboard_User_Reports(@ModelAttribute Users0001 info,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 JsonObject details = new JsonObject();
		  
   	 if(Session.IsSessionValid(session))
	     {
   		 details = rm.User_Reportings();  
   		 
   		 System.out.println("RETRIEVED"+ details);
	     
   	 }
   	 
   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
     response.setHeader("Pragma","no-cache");
     response.setHeader("Expires","0");
	 return details.toString();
    }
    
    @RequestMapping(value = {"/Datavision/Reports/Upload"}, method = RequestMethod.GET)
    public ModelAndView RTSIS_REPORT_Upload(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "REPUPL";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Reports/Report_upload");  
	    	 
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
    
    @RequestMapping(value = {"/Datavision/Reports/Upload"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Upload_File_Report(@ModelAttribute RTS003 Info,  HttpServletRequest request, HttpServletResponse response,HttpSession session) throws IllegalStateException, IOException, ParseException 
    {	 
	   	 JsonObject details = new JsonObject();
	   	 
	   	 try
	   	 {
	   		 if(Info.getFile().equalsIgnoreCase("true")) 
			 {	 
				 File_handling f = new File_handling();  Common_Utils utils = new Common_Utils();
				 
				 String sql = "select MTYPEPARAM from prop001 where MODULEID=?";
					
				 List<String> result = Jdbctemplate.queryForList(sql, new Object[] { Info.getSource_type() }, String.class);
				 
				 String DestPath = result.size() !=0 ? result.get(0) : "/opt/apps/download/others/";  
				 
				 File file =  f.ConvertMultipartFileToFile(Info.getAttachments()[0], DestPath);
				 
				 String RefId = utils.Generate_Random_String(8);
				 
				 String Date = utils.getCurrentDate("dd-MMM-yyyy");
				 
				 String Report_Type = Info.getReport_Type();
				 
				 String Extension = FilenameUtils.getExtension(file.getPath());
				 
				 sql = "select suborgcode from sysconf001";
					
				 result = Jdbctemplate.queryForList(sql, String.class);
				 
				 sql = "insert into fileit001(SUBORGCODE, CHCODE, PAYTYPE, REQDATE, REQREFNO, REQTIME, FILETYPE, SRCPATH, DSTPATH, REMARKS, STATUS, RESCODE, RESPDESC) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { result.get(0), Info.getSource_type(), "RISK_REPORT", utils.getCurrentDate("dd-MMM-yyyy"), RefId, utils.get_oracle_Timestamp(), Extension, file.getPath(), file.getPath(), "", "SUCCESS", "200", "FILE DOWNLOADED SUCCESSFULLY" });
					
				 if(Report_Type.equals("R1"))
				 {
					 details = FileIT.Store_RiskView_ReportData(file.getPath(), RefId, Date, Info.getSource_type());
				 }
				 else if(Report_Type.equals("R2"))
				 {
					 details = FileIT.Store_PAST_DUE_DAYS_RECON(file.getPath(), RefId, Date, Info.getSource_type());
				 }
				 else if(Report_Type.equals("R3"))
				 {
					 details = FileIT.Store_CDS_ReportData(file.getPath(), RefId, Date, Info.getSource_type());
				 }
				 else if(Report_Type.equals("R4"))
				 {
					 details = FileIT.Store_TREASURY_BONDS(file.getPath(), RefId, Date, Info.getSource_type());
				 }
			 }
	   	 }
	   	 catch(Exception ex)
	   	 {
	   		 details.addProperty("result", "failed");
			 details.addProperty("stscode", "401");
			 details.addProperty("message", "Techincal issue, try again later");
			 
			 System.out.println(ex.getLocalizedMessage());
	   	 }
	   	 		
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }
    
//    @RequestMapping(value = {"/Datavision/Configuration/Batch_id1"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody String Batch_id(@ModelAttribute RTS006 info , HttpServletRequest request, HttpServletResponse response) 
//    {	 
//		
//	   	JsonObject details = new JsonObject();
//	   	
//	   	details = rm.Batch_ID(info); 
//	   	
//	   	System.out.println("Events" + details);
//	   	
//	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
//	     response.setHeader("Pragma","no-cache");
//	     response.setHeader("Expires","0");
//	   	
//	   	return details.toString();  	
//    }
    
//    @RequestMapping(value = {"/Datavision/suggestions/APIcode1"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody String Suggestions_APIcode_Retrieve(@RequestParam("term") String term ,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
//    {	 
//		 JsonArray details = new JsonArray();
//   	   	
//	   	 details = rm.Get_API_Codes(term);
//	   	 
//	   	 System.out.println(term);
//	   	 
//	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
//	     response.setHeader("Pragma","no-cache");
//	     response.setHeader("Expires","0");
//	   	
//	   	 return details.toString();  	
//    
//    }
    
    @RequestMapping(value = {"/Datavision/retrieve_data"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Select_Client(@RequestParam("REPORTSERIAL") String REPORTSERIAL ,@RequestParam("STARTSL") String StartSl, @RequestParam("ENDSL") String EndSl, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     JsonObject details = new JsonObject();
	   
	     System.out.println("REPORTSERIAL :"+REPORTSERIAL);
	     System.out.println("REPORTSERIAL :"+StartSl);
	     
	     details = rm.Get_Event_Report(REPORTSERIAL,StartSl,EndSl);
	    
	     return details.toString();
    }
    
    
    
    @RequestMapping(value = {"/Datavision/User_list_report"}, method = RequestMethod.GET)
    public ModelAndView USER_LIST_REPORT(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "USER_LIST_REPORT";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Reports/User_list_report");  
	    	 
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
    
    
    //USER LIST REPORT
    @RequestMapping(value = {"/Datavision/Report/list_of_users"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String user_list_Report(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	
	     JsonObject details = new JsonObject();
	   	   	
	   	 details = rm.Get_User_list_Report();
	   	
	   	 return details.toString();  
   	 
    }	
    
    @RequestMapping(value = {"/Datavision/Audit_report"}, method = RequestMethod.GET)
    public ModelAndView AUDIT_REPORT(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "AUDIT_REPORT";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Reports/Audit_report");  
	    	 
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
    
    
    @RequestMapping(value = {"/Datavision/Report/Audit_report"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Audit_Report(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	
	     JsonObject details = new JsonObject();
	   	   	
	   	 details = rm.Get_Audit_Report();
	   	
	   	 return details.toString();  
   	 
    }	
    
    
    @RequestMapping(value = {"/Datavision/Security_report"}, method = RequestMethod.GET)
    public ModelAndView Security_report(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "SECURITY_REPORT";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Reports/Security_report");  
	    	 
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
    
    
    @RequestMapping(value = {"/Datavision/Report/Security_report"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Security_report(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	
	     JsonObject details = new JsonObject();
	   	   	
	   	 details = rm.Get_Security_Report();
	   	
	   	 return details.toString();  
   	 
    }
    
    @RequestMapping(value = {"/Datavision/User_Del_Reports"}, method = RequestMethod.GET)
    public ModelAndView USER_Del_LIST_REPORT(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "REPORTS", CHILD = "USERDELREPORT";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Reports/User_Deletion");  
	    	 
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
    
    @RequestMapping(value = {"/Datavision/Report/del_of_users"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String user_del_Report(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	
	     JsonObject details = new JsonObject();
	   	   	
	   	 details = rm.User_Del_Reportings();
	   	
	   	 return details.toString();  
   	 
    }
    
    
    //----------------------------------------------------------------------


    @RequestMapping(value = {"/Datavision/suggestions/Api_domain"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Suggestions_APIcode_Domain(@RequestParam("term") String term ,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		 JsonArray details = new JsonArray();
   	   	
		 String CHILD = (String) session.getAttribute("CHILD");  // Retrieve CHILD from the session

	   	 details = rm.Get_API_Codes_DOMAIN(term , CHILD);
	   	 
	   	 System.out.println(term);
	   	 
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	   	
	   	 return details.toString();  	
    
    }
    
    
  /*  @RequestMapping(value = {"/Dashboard/Country_Reports", "/Dashboard/FINANCE_Reports" , "/Dashboard/TRADE_Reports" ,
    		"/Dashboard/ACBS_Reports" , "/Dashboard/MARCIS_Reports" , "/Dashboard/GEMS_Reports" , "/Dashboard/CC_Reports" ,
    		"/Dashboard/FM_Reports" , "/Dashboard/CASH_Reports" , "/Dashboard/CADM_Reports" , "/Dashboard/APARTA_Reports" ,"/Dashboard/EBBS_Reports"}, method = RequestMethod.GET)
   */
    @RequestMapping(value = {"/Dashboard/{report_type}"}, method = RequestMethod.GET)
  
    public ModelAndView Detail_domain(@PathVariable("report_type") String report_type, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        
        
        ModelAndView mv = new ModelAndView();

        String uri = report_type;
        String PARENT = "DASHBOARD";
        String CHILD = "";
  
      if (uri.contains("Country_Reports")) 
      {
          CHILD = "Country Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
          
 	   	  
      } 
      else if (uri.contains("FINANCE_Reports")) 
      {
          CHILD = "Finance Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
      	   	
 	  
      }
      else if (uri.contains("TRADE_Reports")) 
      {
          CHILD = "Trade Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
          
         
      }
      else if (uri.contains("ACBS_Reports")) 
      {
          CHILD = "ACBS Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
    
         
      }
      else if (uri.contains("MARCIS_Reports")) 
      {
          CHILD = "Marcis Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
  
      }
      else if (uri.contains("GEMS_Reports")) 
      {
          CHILD = "Gems Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
  
        
      }
      else if (uri.contains("CC_Reports")) 
      {
          CHILD = "Client Coverage Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
   
      }
      else if (uri.contains("FM_Reports")) 
      {
          CHILD = "Fm Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");
     
        
      }
      else if (uri.contains("CASH_Reports")) 
      {
          CHILD = "Cash Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");

      }
      else if (uri.contains("EBBS_Reports")) 
      {
          CHILD = "Ebbs Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");    
      }
      
      else if (uri.contains("CADM_Reports")) 
      {
          CHILD = "Cadm Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");

      }
      else if (uri.contains("APARTA_Reports")) 
      {
          CHILD = "Aparta Report";
          mv.setViewName("/Datavision/Dashboard/Country_detail");

       
      }
      else 
      {
          mv.setViewName("redirect:/Datavision/login");
          return mv;
      }
      
      
        session.setAttribute("CHILD", CHILD);

 
        if (Session.IsSessionValid(session, PARENT, CHILD)) 
        {

            mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
        } 
        
        else 
        {
            mv.setViewName("redirect:/Datavision/login");
        }

             
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        return mv;
    }
    
    
    @RequestMapping(value = {"/Datavision/Report_Generator/FIU_Report"}, method = RequestMethod.GET)
    public ModelAndView FIU_Reporting_Person(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	 	 if(Session.IsSessionValid(session))
		 {
	 		 mv.addObject("Title", "FIU REPORT");
	 		
			 mv.addObject("logo_header_color", "dark2");
			 mv.addObject("body_color", "");
			 mv.addObject("nav_color", "white");
			 mv.addObject("sidebar_color", "dark2");
			 
			 mv.addObject("active_main_menu", "Report Generator");
			 mv.addObject("active_sub_menu", "FIU_Report");
			 
			 mv.setViewName("Datavision/Reports/FIU_Report");
			 
			 mv.addObject("Menu", MG.Get_Menus_HTML(session, "REPORT_GENERATOR", "FIU_REPORT"));
			 
			 System.out.println(MG.Get_Menus_HTML(session, "REPORT_GENERATOR", "FIU_REPORT"));
		}
		else
		{
			 mv.setViewName("redirect:/Datavision");
		}
    
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
    }

    @RequestMapping(value = {"/Datavision/Report_Generator/Report/Download"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String FIU_Reporting_Download(@ModelAttribute FIU_Menu Info, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, ParseException 
    {	 
   	 JsonObject details = new JsonObject();
   	 
	   Info.setI_userid((String)session.getAttribute("sesUserId"));
	  	
	   Gson gson = new Gson();
	   String infoJson = gson.toJson(Info);
	   System.out.println(infoJson); 	   
	
	   details = FIU_EDMP.get_file_Id(Info);
   	 
	     return details.toString();
    }
    
    
 /*   @RequestMapping(value = "/Datavision/FIU_REPORT/REPORT_STORE", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {
    	JsonObject details = new JsonObject();
    	
    	System.out.println("ehjdfwehsdswbdjwbd");
    	
    	System.out.println(Message);
//    	
//    	 JsonParser parser = new JsonParser();
//	   	 JsonObject json = parser.parse(Message).getAsJsonObject();
//	
//	   	 int start = json.has("start") ? json.get("start").getAsInt() : 0;
//	   	 int length = json.has("length") ? json.get("length").getAsInt() : 10;   	
//	   	 
//	     details = FIU_EDMP.store_report002_fiu(Message,start,length);
    	details = FIU_EDMP.store_report002_fiu(Message);

    	return details.toString();
    }
  */
    
    @RequestMapping(value = "/Datavision/FIU_REPORT/REPORT_STORE", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException
    {
    	JsonObject details = new JsonObject();
    	
    	System.out.println(Message);
    	
             try {
            	
            	 JsonParser parser = new JsonParser();
            	 JsonObject json = parser.parse(Message).getAsJsonObject();
 
            	 int start = json.has("start") ? json.get("start").getAsInt() : 0;
            	 int length = json.has("length") ? json.get("length").getAsInt() : 10;
            	
                 details = FIU_EDMP.store_report002_fiu(Message,start,length);
              
                 System.out.println(details.toString());
                 
             }
             catch (NumberFormatException e) {
                 details.addProperty("Result", "failed");
                 details.addProperty("Message", "Invalid pagination parameters");
                 details.addProperty("Stscode", "400");
    	
             }
 
    	return details.toString();
    }
    
    //------------------------------------------------------------------------------------------------------------
    
    
//  @RequestMapping(value = {"/Dashboard/Country_Reports"}, method = RequestMethod.GET)
//  public ModelAndView COUNTRY_DETAIL(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
//  {	 
//	     ModelAndView mv = new ModelAndView();
//	   
//	     String PARENT = "DASHBOARD", CHILD = "Country_Detail";
//	     
//	     if(Session.IsSessionValid(session, PARENT, CHILD))
//	     {
//	    	 mv.setViewName("/Datavision/Dashboard/Country_detail");  
//	    	 
//	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
//	     }
//	     else
//	     {
//	    	 mv.setViewName("redirect:/Datavision/login");
//	     }
//	     
//	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
//	     response.setHeader("Pragma","no-cache");
//	     response.setHeader("Expires","0");
//
//	     return mv;
//  }
//  
  
    
    
}


