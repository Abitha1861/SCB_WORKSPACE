package com.hdsoft.models;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Request_001;
import com.hdsoft.Repositories.Response_001;
import com.hdsoft.Repositories.Token_Info;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.common.Token_System;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class RTSIS_API_Modal implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Webservice_Modal Ws;
	
	@Autowired
	public Webservice_call_Modal Wsc;
	
	@Autowired
	public Request_Modal Req_Modal; 
	
	@Autowired
	public Response_Modal Res_Modal;
	
	@Autowired
	public Token_System tk;

	@Autowired
	public Sysconfig sys;
	 
	private static final Logger logger = LogManager.getLogger(RTSIS_API_Modal.class);

	public JsonObject Call_RTSIS_API(String Json_data, String API_ID)
	{
		JsonObject details = new JsonObject();  
		
		try
		{
			String Token = "RTS999";
			String CHCODE = "RTSIS";
			
			String Sql = "select SUBORGCODE from sysconf001";
			
			List<String> Suborg = Jdbctemplate.queryForList(Sql, String.class);
			
			if(Suborg.size() ==0)
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", "sysconf001 configuration is missing"); 
				
				return details;
			}
			
			Sql = "select SUBORGCODE from webservice001 where CHCODE=? and SERVICECD=?"; 
			
			List<String> Suborg2 = Jdbctemplate.queryForList(Sql, new Object[] { CHCODE, Token } , String.class);
			
			if(Suborg2.size() == 0)
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", "webservice001 token configuration is missing"); 
				
				return details;
			}
			
			if(!Suborg2.get(0).equals(Suborg.get(0)))
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", "sysconf001 & webservice001 configuration are not matched"); 
				
				return details;
			}
		
			Common_Utils util = new Common_Utils();
			
			String SUBORGCODE = Suborg.get(0);
			
			JsonObject Token_details = Generate_Token(SUBORGCODE, CHCODE, Token); 
			
			int Response_Code = Token_details.get("Response_Code").getAsInt();

			String token = "";
			
			if(Response_Code == 200)
			{
				String res = Token_details.get("Response").getAsString();

				JsonObject Response = util.StringToJsonObject(res);

				token = Response.get("access_token").getAsString();
			} 
			else 
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", CHCODE+" token generation failed !!"); 
				
				return details;
			}
			
			JsonObject webservice_details = Ws.Get_Webserice_Info(sys.getSyscode(), CHCODE, API_ID);
			
			if(webservice_details.get("Result").getAsString().equals("Failed")) 
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", "api configurations not found for the service "+API_ID); 
				
				return details;
			}
			
			JsonArray O_Headers = webservice_details.get("Headers").getAsJsonArray();

			JsonObject SERIAL = Req_Modal.Generate_Serial();
			
			String Headers_str = O_Headers.toString();

			Headers_str = Headers_str.replace("~Token~", token);
			Headers_str = Headers_str.replace("~token~", token);
			Headers_str = Headers_str.replace("~fspInformationId~", SERIAL.has("Serial") ? SERIAL.get("Serial").getAsString() : util.Generate_OTP(8));
			Headers_str = Headers_str.replace("~date~", util.getCurrentDateTime());  

			O_Headers = util.StringToJsonArray(Headers_str);
			
			

			webservice_details.addProperty("PAYLOAD", Json_data);
			webservice_details.add("Headers", O_Headers);
			
			JsonObject API_Response = Wsc.Okhttp_Send_Rest_Request(webservice_details); 
			
			Response_Code = API_Response.get("Response_Code").getAsInt() ;
			
			logger.debug(">>>>>> API ResponseCode :::: " + Response_Code + " <<<<<<");
			
			logger.debug(">>>>>> API Response     :::: " + (API_Response.has("Response") ? API_Response.get("Response").getAsString() : "") + " <<<<<<"); 	
			
			details.addProperty("result", API_Response.get("Result").getAsString());
			details.addProperty("stscode", API_Response.get("Response_Code").getAsInt() ); 
			details.addProperty("message", (API_Response.has("Response") ? API_Response.get("Response").getAsString() : ""));  	
		}
		catch(Exception e)
		{
			details.addProperty("result", "failed");
			details.addProperty("stscode", "HP06"); 
			details.addProperty("message", e.getLocalizedMessage());  	
			
			logger.debug("Exception in Handle_Request :::: "+e.getLocalizedMessage()); 
		}
		
		 return details;	
	 }
	 
	 public JsonObject Generate_Token(String SUBORGCODE, String CHCODE, String SERVICECD) 
	 {
			JsonObject details = new JsonObject();

			String SYSCODE = sys.getSyscode();

			try 
			{
				Common_Utils util = new Common_Utils();

				String ReqRefID = System.currentTimeMillis() + "";

				JsonObject webservice_details = Ws.Get_Webserice_Info(SYSCODE, CHCODE, SERVICECD);

				if(webservice_details.get("Result").getAsString().equals("Failed")) 
				{
					return webservice_details;
				}

				String form_data = webservice_details.get("PAYLOAD").getAsString();

				JsonObject form_object = util.StringToJsonObject(form_data);

				HashMap<String, String> params = new HashMap<String, String>();

				if(form_object.has("grant_type")) 
				{
					params.put("grant_type", form_object.get("grant_type").getAsString());
				}
				
				if (form_object.has("username"))
				{
					params.put("username", form_object.get("username").getAsString());
				}
				
				if (form_object.has("password"))
				{
					params.put("password", form_object.get("password").getAsString());
				}

				JsonArray Params = new JsonArray();

				for (Entry<String, String> entry : params.entrySet())
				{
					JsonObject form_datas = new JsonObject();

					form_datas.addProperty("Key", entry.getKey());
					form_datas.addProperty("Value", entry.getValue());

					Params.add(form_datas);
				}

				webservice_details.add("Form_Data", Params);

				String URI = webservice_details.get("URI").getAsString();
				String O_Headers = webservice_details.get("Headers").getAsJsonArray().toString();

				Request_001 Request_001 = new Request_001(SUBORGCODE, CHCODE, CHCODE, SERVICECD, "O", ReqRefID, URI, "", "", O_Headers, Params.toString(), sys.getSuborgcode(), "");

				Req_Modal.Insert_Request_001(Request_001); 

				JsonObject API_Response = new JsonObject();

				int Retry = 0;
				
				boolean flag = true;

				do 
				{
					API_Response = Wsc.Okhttp_Send_Rest_Request(webservice_details);

					if (API_Response.get("Response_Code").getAsInt() == 500)
					{
						flag = false;

						Retry++;
					} 
					else 
					{
						flag = true;
					}
				} while (!flag && Retry < 3);

				logger.debug("Token API_Response :::: " + API_Response);

				Response_001 Response = new Response_001(SUBORGCODE, CHCODE, CHCODE, SERVICECD, "O", ReqRefID, URI, "", "", O_Headers, API_Response.toString(), sys.getSuborgcode(), "");

				Res_Modal.Insert_Response_001(Response); 

				Response_001 Inward_Response = new Response_001(SUBORGCODE, SYSCODE, CHCODE, SERVICECD, "I", ReqRefID, "", "", "", "", details.toString(), SYSCODE, "");

				Res_Modal.Insert_Response_001(Inward_Response); 

				details = API_Response;
			} 
			catch (Exception e) 
			{
				details.addProperty("Result", "Failed");
				details.addProperty("Response_Code", "500");
				details.addProperty("Message", e.getLocalizedMessage());

				logger.debug("Exception in Generate_Token :::: " + e.getLocalizedMessage());
			}

			return details;
	}
			
	public JsonObject RTSIS_API_CALL(String SERVICECD)
	{
		JsonObject details = new JsonObject();
		
		Common_Utils utils = new Common_Utils();
		
		String sql = "select ORGCODE FROM sysconf001";
		 
		final String SUBORGCODE = Jdbctemplate.queryForObject(sql, new Object[] {},String.class);
		
		final String CHCODE  = "Datavision"; 
		final  String PAYTYPE = "RTSIS";  
			
		try{
						
			JsonObject webservice_details = Ws.Get_Webserice_Info(SERVICECD);
				 
			String Payload_data = webservice_details.get("PAYLOAD").getAsString();   
			  
	        String requesturl = webservice_details.get("URI").getAsString(); 		 
	        
	        
			JsonObject Token_details = new JsonObject();                           
			  
			   String token = "";
			   String ERRDESC = "";
			   
			   Token_details = tk.get_stored_token(SUBORGCODE, CHCODE, PAYTYPE);    
			  
			   if(Token_details.get("Result").getAsString().equals("Success"))    
			   {
			 	   token = Token_details.get("token").getAsString();
			   }
			   else
			   {
				   Token_details = Generate_Token();
				   
				   int Response_Code = Token_details.get("Response_Code").getAsInt();
					  
				   if(Response_Code == 200)
				   {
					    String res = Token_details.get("Response").getAsString();
						   
					    JsonObject Response = utils.StringToJsonObject(res);
					   
					    token = Response.get("access_token").getAsString();
					   
					    Token_Info info = new Token_Info(SUBORGCODE, CHCODE, PAYTYPE, token);
					   
					    tk.Check_and_Update_tokens(info);  
				   }
				   else
				   {
					     ERRDESC = "token generation failed";	 
						 
						 logger.debug(">>>>>> "+ERRDESC+" <<<<<<");
						 
				   }
			  }
	        	        				  
			  webservice_details.addProperty("URI", requesturl);                         
						  
			  JsonArray O_Headers = webservice_details.get("Headers").getAsJsonArray();
			  
			  String Headers_str = O_Headers.toString();
			  
			  Headers_str = Headers_str.replace("~Token~", token);
			  
			  webservice_details.add("Headers", O_Headers);
			  
			  webservice_details.addProperty("PAYLOAD", Payload_data);
			  
			  
			  
			
			  
			  String seq_sql ="select RTSIS_REPORT_SERIAL.NEXTVAL from dual";
			  
			  String Seq_no	= Jdbctemplate.queryForObject(seq_sql,new Object[]{}, String.class);
			  
			  String O_FLOW  =  "O"; 
		      String O_API_RefID  = "RTSIS_"+Seq_no;  
		      String O_MSGURL = requesturl; 
		      String O_HEAD_MSG = O_Headers.toString();
		      String O_BODY_MSG = Payload_data;
		      String O_INITATEDBY = "Datavision";
		      String Req_date = utils.getCurrentDate("dd-MMM-yyyy");
		      Timestamp Req_date_with_time = utils.get_oracle_Timestamp();
		      
     						
		      Request_001 Request_001 = new Request_001(SUBORGCODE, CHCODE, PAYTYPE, SERVICECD, O_FLOW, Req_date, Req_date_with_time,  O_API_RefID, O_MSGURL,"", "",  O_HEAD_MSG, O_BODY_MSG, O_INITATEDBY,"");
		      					     	
			  Req_Modal.Insert_Request_001(Request_001); 
			  
			  
 	  
			  JsonObject API_Response = new JsonObject();
	     
			  API_Response = Wsc.Okhttp_Send_Rest_Request(webservice_details); 		
			  
			  logger.debug(">>>>>> Confirm Transfer API_Response :::: "+API_Response+" <<<<<<");
			  
			  Response_001 Response = new Response_001(SUBORGCODE, CHCODE, PAYTYPE, SERVICECD, "O", O_API_RefID , requesturl , "", "", O_HEAD_MSG, API_Response.toString(), "RTSIS", "");
				
			  Res_Modal.Insert_Response_001(Response);
			  
		
			  
		  }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Send Report :::: "+e.getLocalizedMessage()); 
		 }
		
		
	  details.addProperty("result", "success");
	  details.addProperty("stscode", "HP00");
	  details.addProperty("message", "Report Send Successfuly !!");
	 
	  return details;
}
	
	
	public JsonObject Generate_Token()
	{
		JsonObject details = new JsonObject();  
		
		String SUBORGCODE =  sys.getSuborgcode();
		String CHCODE  = "RTSIS";  
		String SERVICECD = "999"; 
		String SYSCODE = sys.getSyscode(); 
		
		try
		{
			 Common_Utils utils = new Common_Utils();
			 
			 String ReqRefID = System.currentTimeMillis()+"";
		
			 Request_001 Inward_Request = new Request_001(SUBORGCODE, SYSCODE, CHCODE, SERVICECD, "I", ReqRefID, "", "", "" , "", "", SYSCODE,  "");
			 
			 Req_Modal.Insert_Request_001(Inward_Request);    
			 	 
			 JsonObject webservice_details = Ws.Get_Webserice_Info(SYSCODE,CHCODE,SERVICECD);
			 
			 if(!webservice_details.get("Result").getAsString().equals("Success"))
			 {  
				 Response_001 Inward_Response = new Response_001(SUBORGCODE, SYSCODE, CHCODE, SERVICECD, "I", ReqRefID , "", "", "", "", webservice_details.toString(), SYSCODE, "");
					
			     Res_Modal.Insert_Response_001(Inward_Response);   
			     
				 return webservice_details; 
			 }
		    
		     String form_data = webservice_details.get("PAYLOAD").getAsString();
		   
		     JsonObject form_object = utils.StringToJsonObject(form_data);
		    
		     HashMap<String,String> params = new HashMap<String,String>();
		     
	           if(form_object.has("grant_type"))  { params.put("grant_type", form_object.get("grant_type").getAsString()); }
	         
	         JsonArray Params = new JsonArray();
	         
	         for(Entry<String, String> entry: params.entrySet()) 
			 {
	        	 JsonObject form_datas = new JsonObject();
	        	 
	        	 form_datas.addProperty("Key", entry.getKey());  
	        	 form_datas.addProperty("Value", entry.getValue());  
	        	 
	        	 Params.add(form_datas);
			 }
	         
	         webservice_details.add("Form_Data", Params);
	         
	        
	         String URI = webservice_details.get("URI").getAsString();  
	         String O_Headers = webservice_details.get("Headers").getAsJsonArray().toString();	         
		           
	         Request_001 Request_001 = new Request_001(SUBORGCODE, CHCODE, CHCODE, SERVICECD, "O", ReqRefID, URI, "", "", O_Headers, Params.toString(), "HDPAY", "");
			 
			 Req_Modal.Insert_Request_001(Request_001);    
			 
			 JsonObject API_Response = new JsonObject();
			 
			 int Retry = 0;  boolean flag = true;
			 
			 do
			 {
				 API_Response = Wsc.Okhttp_Send_Rest_Request(webservice_details); 	
				 
				 if(API_Response.get("Response_Code").getAsInt() == 500)
				 {
					 flag = false; 
					 
					 Retry++;
				 }
				 else
				 {
					 flag = true;
				 }
				 
			 }while(!flag && Retry < 3);
			 
	         logger.debug("Token API_Response :::: "+API_Response);
	         
	         Response_001 Response = new Response_001(SUBORGCODE, CHCODE, CHCODE, SERVICECD, "O", ReqRefID , URI , "", "", O_Headers, API_Response.toString(), "HDPAY", "");
				
		     Res_Modal.Insert_Response_001(Response);  
		     
		     Response_001 Inward_Response = new Response_001(SUBORGCODE, SYSCODE, CHCODE, SERVICECD, "I", ReqRefID , "", "", "", "", details.toString(), SYSCODE, "");
				
		     Res_Modal.Insert_Response_001(Inward_Response);   
		     
		     details =  API_Response;
		}
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Response_Code", "500");
			 details.addProperty("Message", e.getLocalizedMessage());  

			 logger.debug("Exception in Generate_Token :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	  

   
  
} 
