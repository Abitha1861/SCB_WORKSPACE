package com.hdsoft.controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Distribution_list_Creation;
import com.hdsoft.Repositories.Event_Creation;
import com.hdsoft.Repositories.Module_code001;
import com.hdsoft.Repositories.Submodule_code001;
import com.hdsoft.Repositories.User_Journey_Creation;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.configuration.WebConfiguration;
import com.hdsoft.models.API_Configuration;
import com.hdsoft.models.BOT_Suptect_Finance;
import com.hdsoft.models.Configuration_Modal;
import com.hdsoft.models.Configuration_Modal1;
import com.hdsoft.models.RTSIS_AutoMan_Modal;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class Datavision_Configuration
{	
	public JdbcTemplate Jdbctemplate;

	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Menu_Generation MG;
	
	@Autowired
	public Configuration_Modal config;
	
	@Autowired
	public Configuration_Modal1 config1;
	
	@Autowired
	public API_Configuration API_config;
	
	@Autowired
	public Sysconfig sys;
	 
	@Autowired
	public RTSIS_AutoMan_Modal am;
	 
	@RequestMapping(value = {"/Datavision/API_Configuration"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Configuration_API_Configurations(@ModelAttribute web_service_001 Info , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     JsonObject details = new JsonObject();
	     
	     details = config.API_Configuration_Action(Info) ;
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	  
	     return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/suggestions/APIcode"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Suggestions_APIcode_Retrieve(@RequestParam("term") String term ,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		 JsonArray details = new JsonArray();
   	   	
	   	 details = config.Get_API_Codes(term);   
	   	
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	 return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Find/API_Service"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Find_Api_Information(@ModelAttribute web_service_001 Info ,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		 JsonObject details = new JsonObject();
	   	   	
	   	 details = config.Find_API_Service(Info.getCHCODE(), Info.getSERVNAME(), Info.getSERVICECD());   
	   	
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	 return details.toString();  	
    }
	

	@RequestMapping(value = {"/Datavision/Distribution_list_Creation"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Distribution_list_Creation(@ModelAttribute Distribution_list_Creation Info ,HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
   	      JsonObject details = new JsonObject();
   	   	
   	      details = config1.Distribution_list_Creation(Info);
   	      
   	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
   	
   	      return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Event/suggestions/DLISTIDs"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Suggestions_DLISTID_Retrieve(@RequestParam("term") String term, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
		 JsonArray details = new JsonArray();
   	   	
	   	 details = config1.Get_alert001_DLISTIDs(term);  
	   	 
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	   	
	   	 return details.toString();  	
    }

	@RequestMapping(value = {"/Datavision/Distribution_list/Retrieve"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Retrieve_DLISTID_Retrieve(@RequestParam("Distribution_Id") String DlistId, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
		 JsonObject details = new JsonObject();
   	   	
	   	 details = config1.Get_Distribution_list_details(DlistId);  
	   	
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	 return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Event_Creation"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Event_Creation(@ModelAttribute Event_Creation Info, @ModelAttribute Distribution_list_Creation info, HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
	   	
	   	if(info.getTotal_list_Size() != 0 && info.getUser_Ids().length !=0) 
	   	{
	   		details = config1.Distribution_list_Creation(info);  
	   	}
	   	
	   	details = config1.Event_Action(Info);
	   	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	
	   	return details.toString();  	
    }	
	
	@RequestMapping(value = {"/Datavision/Event/Retrieve"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Event_Retrieve(@ModelAttribute Event_Creation Info ,HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
	   	   	
	   	details = config1.Get_Event001(Info); 
	   	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	   	
	   	return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/User_Journey/Retrieve"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String User_Journey_Event_Retrieve(@ModelAttribute User_Journey_Creation Info ,HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
	   	   	
	   	details = config1.Get_User_Journry_event_details(Info.getUser_Journey_Type(), Info.getUser_Journey_Code());  
	   	
	    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	    response.setHeader("Pragma","no-cache");
	    response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }
	 
	@RequestMapping(value = {"/Datavision/User_Journey/Find"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String User_Journey_Event_Find(@RequestParam("Module_Id") String module , HttpServletRequest request, HttpServletResponse response) 
    {	  
	   	JsonObject details = new JsonObject();
	   	   	
	   	details = config1.Get_User_Journry_event_details(module);  
	   	
	    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	    response.setHeader("Pragma","no-cache");
	    response.setHeader("Expires","0");
	   	
	   	return details.toString();  	
    }
	 
	@RequestMapping(value = {"/Datavision/Events/Find/Fixed_and_Nonstop"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Fixed_and_Nonstop(@RequestParam("Module_Id") String module , HttpServletRequest request, HttpServletResponse response) 
    {	  
	   	JsonObject details = new JsonObject();
	   	   	
	   	details = config1.Get_Fixed_and_NonStop_Events_Event001(module);  
	   	
	    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	    response.setHeader("Pragma","no-cache");
	    response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Event/suggestions/EventCode"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Suggestions_EventCode_Retrieve(@RequestParam("term") String term, @RequestParam("module") String module, Event_Creation Info ,HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
		 JsonArray details = new JsonArray();
   	   	
	   	 details = config1.Get_Event001_Eventcodes(term, module);  
	   	 
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	 return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Event/suggestions/SQlIDs"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Suggestions_SQlIDs_Retrieve(@RequestParam("term") String term, @RequestParam("module") String module, HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
		 JsonArray details = new JsonArray();
   	   	
	   	 details = config1.Get_sql001_SQLIDs(term, module);  
	   	
	   	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	 return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Configuration/Event_Creation/suggestion_Module"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Get_modulecode_EC(@ModelAttribute Module_code001 Info , HttpServletRequest request, HttpServletResponse response) throws IOException 
    {	 
	     JsonObject details = new JsonObject();
	     
	     details = API_config.ModuleDesc_DD(Info); 
	  
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/Configuration/Event_Creation/submodule_Module/Data_retrieve"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Retrieve_modulecode_data_EC(@ModelAttribute Submodule_code001 Info, HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
	   	   	
	   	details = API_config.Retreive_submodule_data(Info);
	   	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }	
	 
	@RequestMapping(value = {"/Datavision/Configuration/Events_Names/Data_retrieve"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Retrieve_modulecode_data_EC(@RequestParam("MODULE") String MODULE , @RequestParam("SUB_MODULE") String SUB_MODULE, HttpServletRequest request, HttpServletResponse response) 
     {	 
    	JsonObject details = new JsonObject();
    	   	
    	details = API_config.Retreive_Events(MODULE, SUB_MODULE); 
    	
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
    	return details.toString();  	
     }	
	
	@RequestMapping(value = {"/Datavision/Event_Run/Generate_Report"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Event_Run_Generate_Report(@RequestParam("Module") String module,@RequestParam("Submodule") String submodule,@RequestParam("Event_Code") String eventcode,@RequestParam("BATCH_ID") String batch_id  , HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
		
	   	details = config1.Get_Event_Report(module,submodule,eventcode,batch_id);
	   	
	   	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	    response.setHeader("Pragma","no-cache");
	    response.setHeader("Expires","0");
	   	
	   	return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Event_Run/Push_Data"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Event_Run_Push_Date(@ModelAttribute Event_Creation Info, @ModelAttribute Distribution_list_Creation info, HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
	 
	   	details = config1.Retrieve_and_Push(Info);
	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Configuration/Batch_id"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Batch_id(@RequestParam("EVENT_NAME") String EVENT_NAME , HttpServletRequest request, HttpServletResponse response) 
    {	 
	   	JsonObject details = new JsonObject();
	   	
	   	details = config1.Batch_ID(EVENT_NAME); 
	   	
	   	System.out.println("Events" + details);
	   	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	   	return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Request/Dispatcher"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Push_Report(@RequestParam("BatchId") String BatchId, @RequestParam("ReportSl") String ReportSl , @RequestParam("ServiceCd") String ServiceCd ,  HttpServletRequest request, HttpServletResponse response,HttpSession session) 
    {	 
	   	JsonObject details = new JsonObject();
		
	   	//System.out.println(BatchId+ServiceCd+ReportSl);
	   	
	   	 details = am.Request_Dispatcher(BatchId,ReportSl,ServiceCd);
	  	
	   //System.out.println("Output"+details);
	   
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	   	
	   	return details.toString();  	
    }
	
	@RequestMapping(value = {"/Datavision/Dynamic/funtion"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Dynamic_function(@RequestParam("servicd") String servicd, HttpServletRequest request, HttpServletResponse response,HttpSession session) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {	 
	   	 JsonObject details = new JsonObject();
		
	   	// details = am.Request_Dispatcher(BatchId,ReportSl,ServiceCd);
	   	 
	   	// WebConfiguration wb = new WebConfiguration();
		 
		 //BOT_Suptect_Finance m = new BOT_Suptect_Finance(Jdbctemplate);
		 
		 //Method method = m.getClass().getDeclaredMethod("Equity_Investment", String.class, String.class, String.class);
		 
		 //details = (JsonObject)method.invoke(m, servicd, "1", "1");
	   	 
	   	//BOT_Suptect_Finance k = new BOT_Suptect_Finance();
	   	
	   	//details =  k.getBalanceSheetInfo();  
	   	
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	   	
	   	return details.toString();  	
    }
}
