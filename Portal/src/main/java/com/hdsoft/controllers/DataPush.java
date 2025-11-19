package com.hdsoft.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Request_001;
import com.hdsoft.Repositories.Response_001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.models.Request_Modal;
import com.hdsoft.models.Response_Modal;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.models.Webservice_Modal;
import com.hdsoft.models.Webservice_call_Modal;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class DataPush 
{	
	 public JdbcTemplate Jdbctemplate;
	
	 @Autowired
	 public void setJdbctemplate(HikariDataSource Datasource) 
	 {
		 Jdbctemplate = new JdbcTemplate(Datasource);
	 }
	
	 @Autowired
	 public Request_Modal Req_Modal;
	
	 @Autowired
	 public Response_Modal Res_Modal;
	
	 @Autowired
	 public Webservice_Modal Ws;
	
	 @Autowired
	 public Webservice_call_Modal Wsc;
	 
	 @Autowired
	 public Sysconfig sys;
	 
	 private static final Logger logger = LogManager.getLogger(DataPush.class);
		 
	 @RequestMapping(value = {"/Datavision/SampleData/Push"}, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Bill_request_GEPG(@RequestBody String Body_MSG, HttpServletRequest request, @RequestHeader(value = "Accept", required=true) String Accept, @RequestHeader(value = "Content-Type", required=true) String Content_Type,
     @RequestHeader(value = "CHCODE", required=true) String CHCODE, @RequestHeader(value = "TOKEN_ID", required=true) String TOKEN_ID, @RequestHeader(value = "API_ID", required=true) String API_ID,
     @RequestHeader(value = "SENDER_ID", required=true) String SENDER_ID, @RequestHeader(value = "FSPINFO_ID", required=true) String FSPINFO_ID,HttpServletResponse response )
     {	 
		  JsonObject details = new JsonObject();
		  
		  JsonObject Headers = new JsonObject();
	
		  Common_Utils util = new Common_Utils();
			 
		  Headers.addProperty("Accept", util.ReplaceNull(Accept));
		  Headers.addProperty("Content-Type", util.ReplaceNull(Content_Type));
		  Headers.addProperty("API_ID", util.ReplaceNull(API_ID));
		  Headers.addProperty("TOKEN_ID", util.ReplaceNull(TOKEN_ID));
		  Headers.addProperty("CHCODE", util.ReplaceNull(CHCODE));
		  Headers.addProperty("SENDER_ID", util.ReplaceNull(SENDER_ID));
		  Headers.addProperty("FSPINFO_ID", util.ReplaceNull(FSPINFO_ID));
		  
		  logger.debug(">>>>>>>> New Sample data push Request Received <<<<<<<<");
		  
		  logger.debug(">>>>>>>> Request Body <<<<<<<< "+Body_MSG);
		  
		  logger.debug(">>>>>>>> Request Headers <<<<<<<< "+Headers.toString());

		  details = Handle_Request(Body_MSG, Headers, request, response);
		  
		  logger.debug(">>>>>>>> Response <<<<<<<< "+details);
		  
		  response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		  response.setHeader("Pragma","no-cache");
		  response.setHeader("Expires","0");

		 
	      return details.toString();
     }
	 
	 public JsonObject Handle_Request(String request, JsonObject Headers, HttpServletRequest req,HttpServletResponse response)
	 {
		JsonObject details = new JsonObject();  
		
		try
		{
			//JsonObject Request = new Common_Utils().StringToJsonObject(request);
			
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
			
			List<String> Suborg2 = Jdbctemplate.queryForList(Sql, new Object[] { Headers.get("CHCODE").getAsString(), Headers.get("TOKEN_ID").getAsString() } , String.class);
			
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
			

			request = request.replaceAll("\\n", "");
			request = request.replaceAll("\\r", "");
			
			Common_Utils util = new Common_Utils();
			
			String SUBORGCODE = Suborg.get(0);
			String CHCODE = Headers.get("CHCODE").getAsString(); 
			String TOKEN_ID = Headers.get("TOKEN_ID").getAsString(); 
			String API_ID = Headers.get("API_ID").getAsString(); 
			
			JsonObject Token_details = Generate_Token(SUBORGCODE, CHCODE, TOKEN_ID, response); 
			
			int Response_Code = Token_details.get("Response_Code").getAsInt();

			String token = "ttt";
			
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
				
				//return details;
			}
			
			JsonObject webservice_details = Ws.Get_Webserice_Info(sys.getSyscode(), CHCODE, API_ID);
			
			if(webservice_details.get("Result").getAsString().equals("Failed")) 
			{
				return webservice_details;
			}
			
			JsonArray O_Headers = webservice_details.get("Headers").getAsJsonArray();

			String Headers_str = O_Headers.toString();

			Headers_str = Headers_str.replace("~Token~", token);
			Headers_str = Headers_str.replace("~Sender~", Headers.get("SENDER_ID").getAsString());
			Headers_str = Headers_str.replace("~fspInformationId~", Headers.get("FSPINFO_ID").getAsString());
			Headers_str = Headers_str.replace("~date~", util.getCurrentDateTime());

			O_Headers = util.StringToJsonArray(Headers_str);

			webservice_details.addProperty("PAYLOAD", request);
			webservice_details.add("Headers", O_Headers);
			
			JsonObject API_Response = Wsc.Okhttp_Send_Rest_Request(webservice_details); /**** Sending API Request ****/

			logger.debug(">>>>>> API ResponseCode :::: " + API_Response.get("Response_Code").getAsInt() + " <<<<<<");
			logger.debug(">>>>>> API Response     :::: " + (API_Response.has("Response") ? API_Response.get("Response").getAsString() : "") + " <<<<<<"); 	
			
			details.addProperty("result", API_Response.get("Result").getAsString());
			details.addProperty("responseCode", API_Response.get("Response_Code").getAsInt() ); 
			details.addProperty("response", (API_Response.has("Response") ? API_Response.get("Response").getAsString() : ""));  	
		}
		catch(Exception e)
		{
			details.addProperty("result", "failed");
			details.addProperty("stscode", "HP06"); 
			details.addProperty("message", e.getLocalizedMessage());  	
			
			logger.debug("Exception in Handle_Request :::: "+e.getLocalizedMessage()); 
		}
		
		 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

		
		 return details;	
	 }
	 
	 public JsonObject Generate_Token(String SUBORGCODE, String CHCODE, String SERVICECD,HttpServletResponse response) 
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

				if (form_object.has("grant_type")) 
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

				// logger.debug("Request :::: "+webservice_details);
				// logger.debug("params :::: "+params);

				String URI = webservice_details.get("URI").getAsString();
				String O_Headers = webservice_details.get("Headers").getAsJsonArray().toString();

				Request_001 Request_001 = new Request_001(SUBORGCODE, CHCODE, CHCODE, SERVICECD, "O", ReqRefID, URI, "", "", O_Headers, Params.toString(), sys.getSuborgcode(), "");

				Req_Modal.Insert_Request_001(Request_001); /*** Insert Inward Request to Request_001 ****/

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

				Res_Modal.Insert_Response_001(Response); /*** Insert outward Response to Response_001 ****/

				Response_001 Inward_Response = new Response_001(SUBORGCODE, SYSCODE, CHCODE, SERVICECD, "I", ReqRefID, "", "", "", "", details.toString(), SYSCODE, "");

				Res_Modal.Insert_Response_001(Inward_Response); /*** Insert Inward Response to Response_001 ****/

				details = API_Response;
			} 
			catch (Exception e) 
			{
				details.addProperty("Result", "Failed");
				details.addProperty("Response_Code", "500");
				details.addProperty("Message", e.getLocalizedMessage());

				logger.debug("Exception in Generate_Token :::: " + e.getLocalizedMessage());
			}

			 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		     response.setHeader("Pragma","no-cache");
		     response.setHeader("Expires","0");

			
			return details;
		}

						 
}
