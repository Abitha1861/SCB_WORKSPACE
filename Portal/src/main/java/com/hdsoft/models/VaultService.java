package com.hdsoft.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.models.Webservice_Modal.Header_Mapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VaultService implements Database
{
	private static final Logger logger = LogManager.getLogger(VaultService.class);
	
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Webservice_Modal WB;
	
	@Autowired
	public Webservice_call_Modal WCB;
	
	@Autowired
	public Sysconfig sys;
	
	 // Scheduled to run daily at 1 a.m.
    @Scheduled(cron = "0 0 1 * * *")
    public void checkAndRotateSecrets() 
    {
        try 
        {               
        	Common_Utils util = new Common_Utils();
        	
            String query = "SELECT APICODE, CURRENT_SECRET, NEXT_ROTATION_DATE, nvl(ACTION_JOB, '') ACTION_JOB FROM HASHICORP_MANAGEMENT WHERE trunc(NEXT_ROTATION_DATE) <= trunc(sysdate+3)";
            
            List<Map<String, Object>> secretsDueForRotation = Jdbctemplate.queryForList(query);

            for(Map<String, Object> secret : secretsDueForRotation) 
            {
                 String APICODE =  (String) secret.get("APICODE");
                 String ACTION_JOB = (String) secret.get("ACTION_JOB");
                 
                 if(APICODE.equalsIgnoreCase("DB"))
                 {
                	 String token = getVaultToken_For_DB();
                	 
                	 boolean Pass_Rotated = RotatePassword_For_DB(token);
                	 
                	 logger.debug("HasiCorp DB Pass_Rotated result >>>>>>>> "+Pass_Rotated);
                 }
                 else
                 {
                	 String Token_Code = APICODE + "-H001";
                     String Access_Code = APICODE + "-H002";
                     String Rotate_Code = APICODE + "-H003";
                    
                     logger.debug("HasiCorp password valut process started for "+APICODE);
                     
                     boolean Pass_Rotated = RotatePassword(Token_Code, Rotate_Code);
                     
                     if(Pass_Rotated)
                     {
                    	 JsonObject js = AccessPassword(Token_Code, Access_Code);
                    	 
                    	 if(js.has("data"))
                    	 {
                    		 String newSecret = js.get("data").getAsJsonObject().get("password").getAsString();
                    		 
                    		 String updateSql = "UPDATE HASHICORP_MANAGEMENT SET STATUS=?, PREVIOUS_SECRET = CURRENT_SECRET, CURRENT_SECRET = ?, EXPIRATION_DATE = trunc(sysdate) + ROTATION_INTERVAL,\r\n" + 
                               		"NEXT_ROTATION_DATE = trunc(sysdate-5) + ROTATION_INTERVAL, LAST_UPDATED = CURRENT_TIMESTAMP WHERE APICODE = ?";
                               
                             Jdbctemplate.update(updateSql, new Object[] { "SUCCESS", newSecret, APICODE });
                             
                             if(!util.isNullOrEmpty(ACTION_JOB))
                             {
                            	 ACTION_JOB = ACTION_JOB.replaceAll("~password~", newSecret);
                            	 
                            	 Jdbctemplate.update(ACTION_JOB);
                             }
                    	 }
                    	 else
                    	 {                		 
                    		 String updateSql = "UPDATE HASHICORP_MANAGEMENT SET STATUS=?, LAST_UPDATED = CURRENT_TIMESTAMP WHERE APICODE = ?";
                               
                             Jdbctemplate.update(updateSql, new Object[] { "PASSWORD ROTATED BUT FAILED DURING PASSWORD ACCESS API CALL", APICODE });
                    	 }
                     }
                     else
                     {
                    	 String updateSql = "UPDATE HASHICORP_MANAGEMENT SET STATUS=?, LAST_UPDATED = CURRENT_TIMESTAMP WHERE APICODE = ?";
                         
                         Jdbctemplate.update(updateSql, new Object[] { "FAILED DURING PASSWORD ROTATED API CALL", APICODE });
                     }
                 }   
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public String getVaultToken(String TokenCode) 
    {
    	String token = "";
    	
    	try
    	{
    		 Common_Utils util = new Common_Utils();
    		 
    		 String SERVCD = TokenCode;
    	
    		 JsonObject Webdtl = WB.Get_Webserice_Info(SERVCD);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 if(api_out.get("Response_Code").getAsInt() != 200)
			 {
				 api_out = WCB.Send_https_Request(Webdtl);
			 }
			 
			 int Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 JsonObject auth = Res.get("auth").getAsJsonObject();
				 
				 token = auth.get("client_token").getAsString();
			 }
            
			 System.out.println("valut token >>>> "+token);
    	}
    	catch(Exception ex)
    	{
    		logger.debug("exception in getVaultToken >>>> "+ex.getLocalizedMessage());
    	}
    	
        return token;
    }

    public JsonObject AccessPassword(String TokenCode, String SERVCD) 
    {
    	JsonObject details = new JsonObject();
    	
    	try
    	{
    		 logger.debug("HasiCorp AccessPassword process started for "+SERVCD);
    		
    		 Common_Utils util = new Common_Utils();
    		 
    		 String token = getVaultToken(TokenCode);
    		 
    		 JsonObject Webdtl = WB.Get_Webserice_Info(SERVCD);
    		 
    		 JsonArray O_Headers = Webdtl.get("Headers").getAsJsonArray();

 			 String Headers_str = O_Headers.toString();

 			 Headers_str = Headers_str.replace("~Token~", token);
 			
 			 O_Headers = util.StringToJsonArray(Headers_str);

	 		 Webdtl.add("Headers", O_Headers);
				 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 if(api_out.get("Response_Code").getAsInt() != 200)
			 {
				 api_out = WCB.Send_https_Request(Webdtl);
			 }
			 
			 int Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 JsonObject data = Res.get("data").getAsJsonObject();
				 				 
				 details.add("data", data);
			 }
    	}
    	catch(Exception ex)
    	{
    		logger.debug("exception in AccessPassword >>>> "+ex.getLocalizedMessage());
    	}
    	
    	return details;
    }

    public boolean RotatePassword(String TokenCode, String SERVCD) 
    {
    	boolean flag = false;
    	
    	try
    	{
    		 logger.debug("HasiCorp RotatePassword process started for "+SERVCD);
    		
    		 Common_Utils util = new Common_Utils();
    		 
    		 String token = getVaultToken(TokenCode);
    		 
    		 JsonObject Webdtl = WB.Get_Webserice_Info(SERVCD);
    		 
    		 JsonArray O_Headers = Webdtl.get("Headers").getAsJsonArray();

 			 String Headers_str = O_Headers.toString();

 			 Headers_str = Headers_str.replace("~Token~", token);
 			
 			 O_Headers = util.StringToJsonArray(Headers_str);

	 		 Webdtl.add("Headers", O_Headers);
				 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 if(api_out.get("Response_Code").getAsInt() != 204)
			 {
				 api_out = WCB.Send_https_Request(Webdtl);
			 }
			 
			 int Response_Code = api_out.get("Response_Code").getAsInt();
			  
			 if(Response_Code == 204)
			 {
				 flag = true;
			 }
    	}
    	catch(Exception ex)
    	{
    		logger.debug("exception in RotatePassword >>>> "+ex.getLocalizedMessage());
    	}
    	
    	return flag;
    }
    
    public String getVaultToken_For_DB() 
    {
    	String token = "";
    	
    	try
    	{
    		 Common_Utils util = new Common_Utils();
    		 
    		 JsonObject Webdtl = new JsonObject();
    		 
    		 String PAYLOAD = "", URI = "";
    		 
    		 if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_APP"))  // UAT
    		 {
    			 URI = "https://vault-dev.sc.net:8200/v1/ccib/auth/approle/login";
    			 
    			 PAYLOAD = "{\"role_id\":\"e8be4a08-254d-e92b-b8d6-c8e37f8d85b3\",\"secret_id\": \"00c8d797-b452-644e-b113-aee86502060e\"}";
    		 }
    		 
    		 if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_ACCT1")) // PROD
    		 {
    			 URI = "https://vault.global.standardchartered.com:8200/v1/ccib/auth/approle/login";
    			 
    			 PAYLOAD = "{\"role_id\":\"e340f219-607b-1caf-60a6-67da9de9b17a\",\"secret_id\": \"6420df5b-b5d8-e2bc-edef-864667ade908\"}";
    		 }
    		 
    		 //Webdtl.addProperty("SUBORGCODE", sys.getSuborgcode());
    		 Webdtl.addProperty("CHCODE", "DB");
    		 Webdtl.addProperty("SERVICECD", "DB-H001");
			 Webdtl.addProperty("SERVNAME", "DB-HASHICORP-Token");
			 Webdtl.addProperty("FORMAT", "JSON");
			 Webdtl.addProperty("PROTOCOL", "REST");	 
			 Webdtl.addProperty("METHOD", "POST");
			 Webdtl.addProperty("CHTYPE", "PAYMENT GATEWAY");
			 Webdtl.addProperty("URI", URI);
			 Webdtl.addProperty("PAYLOAD", PAYLOAD);
			 Webdtl.addProperty("SIGNPAYLOAD", "");
			 Webdtl.addProperty("HEADERID", "");
			 Webdtl.addProperty("FLOW", "O");
			 
			 JsonArray Headers = new JsonArray();
			 
			 JsonObject Header_details = new JsonObject();
			 
			 Header_details.addProperty("Key", "Accept");
			 Header_details.addProperty("Value", "*/*");
			
			 Headers.add(Header_details);
			 
			 Header_details.addProperty("Key", "Content-Type");
			 Header_details.addProperty("Value", "application/json");
			 
			 Headers.add(Header_details);
			 
			 Webdtl.add("Headers", Headers);
		 
			 Webservice_call_Modal WCB = new Webservice_call_Modal();
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 if(api_out.get("Response_Code").getAsInt() != 200)
			 {
				 api_out = WCB.Send_https_Request(Webdtl);
			 }
			 
			 int Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 JsonObject auth = Res.get("auth").getAsJsonObject();
				 
				 token = auth.get("client_token").getAsString();
			 }
            
			 System.out.println("valut token >>>> "+token);
    	}
    	catch(Exception ex)
    	{
    		logger.debug("exception in getVaultToken >>>> "+ex.getLocalizedMessage());
    	}
    	
        return token;
    }
    
    public JsonObject AccessPassword_For_DB(String token) 
    {
    	JsonObject details = new JsonObject();
    	
    	try
    	{
    		 logger.debug("HasiCorp AccessPassword process started for DB");
    		
    		 Common_Utils util = new Common_Utils();
    		 
    		 JsonObject Webdtl = new JsonObject();
    		 
    		 String PAYLOAD = "", URI = "";
    		 
    		 if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_APP"))  // UAT
    		 {
    			 URI = "https://vault-dev.sc.net:8200/v1/ccib/uklvaddbs293.uk.standardchartered.com/pdbd_dvs_rw.uk.standardchartered.com/static-creds/pdbd_dvs_rw.uk.standardchartered.com_dvs_tz_rtsis_app";
    		 }
    		 
    		 if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_ACCT1")) // PROD
    		 {
    			 URI = "https://vault.global.standardchartered.com:8200/v1/ccib/tzlpdprts01.tz.standardchartered.com/pdbp_dvs_rw.tz.standardchartered.com/static-creds/pdbp_dvs_rw.tz.standardchartered.com_dvs_tz_rtsis_acct1";
    		 }
    		 
    		 //Webdtl.addProperty("SUBORGCODE", sys.getSuborgcode());
    		 Webdtl.addProperty("CHCODE", "DB");
    		 Webdtl.addProperty("SERVICECD", "DB-H002");
			 Webdtl.addProperty("SERVNAME", "DB-HASHICORP-AccessPassword");
			 Webdtl.addProperty("FORMAT", "JSON");
			 Webdtl.addProperty("PROTOCOL", "REST");	 
			 Webdtl.addProperty("METHOD", "GET");
			 Webdtl.addProperty("CHTYPE", "PAYMENT GATEWAY");
			 Webdtl.addProperty("URI", URI);
			 Webdtl.addProperty("PAYLOAD", PAYLOAD);
			 Webdtl.addProperty("SIGNPAYLOAD", "");
			 Webdtl.addProperty("HEADERID", "");
			 Webdtl.addProperty("FLOW", "O");
			 
			 JsonArray Headers = new JsonArray();
			 
			 JsonObject Header_details = new JsonObject();
			 
			 Header_details.addProperty("Key", "Accept");
			 Header_details.addProperty("Value", "*/*");
			
			 Headers.add(Header_details);
			 
			 Header_details.addProperty("Key", "X-Vault-Token");
			 Header_details.addProperty("Value", token);
			 
			 Headers.add(Header_details);
			 
			 Webdtl.add("Headers", Headers);
			 
			 Webservice_call_Modal WCB = new Webservice_call_Modal();
    		 	 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 if(api_out.get("Response_Code").getAsInt() != 200)
			 {
				 api_out = WCB.Send_https_Request(Webdtl);
			 }
			 
			 int Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 JsonObject data = Res.get("data").getAsJsonObject();
				 				 
				 details.add("data", data);
			 }
    	}
    	catch(Exception ex)
    	{
    		logger.debug("exception in AccessPassword >>>> "+ex.getLocalizedMessage());
    	}
    	
    	return details;
    }
    
    public boolean RotatePassword_For_DB(String token) 
    {
    	boolean flag = false;
    	
    	try
    	{
    		 logger.debug("HasiCorp RotatePassword process started for DB");
    		
    		 Common_Utils util = new Common_Utils();
    		 
    		 JsonObject Webdtl = new JsonObject();
    		 
    		 String PAYLOAD = "", URI = "";
    		 
    		 if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_APP"))  // UAT
    		 {
    			 URI = "https://vault-dev.sc.net:8200/v1/ccib/uklvaddbs293.uk.standardchartered.com/pdbd_dvs_rw.uk.standardchartered.com/rotate-role/pdbd_dvs_rw.uk.standardchartered.com_dvs_tz_rtsis_app";
    		 }
    		 
    		 if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_ACCT1")) // PROD
    		 {
    			 URI = "https://vault.global.standardchartered.com:8200/v1/ccib/tzlpdprts01.tz.standardchartered.com/pdbp_dvs_rw.tz.standardchartered.com/rotate-role/pdbp_dvs_rw.tz.standardchartered.com_dvs_tz_rtsis_acct1";
    		 }
    		 
    		 //Webdtl.addProperty("SUBORGCODE", sys.getSuborgcode());
    		 Webdtl.addProperty("CHCODE", "DB");
    		 Webdtl.addProperty("SERVICECD", "DB-H003");
			 Webdtl.addProperty("SERVNAME", "DB-HASHICORP-RotatePassword");
			 Webdtl.addProperty("FORMAT", "JSON");
			 Webdtl.addProperty("PROTOCOL", "REST");	 
			 Webdtl.addProperty("METHOD", "POST");
			 Webdtl.addProperty("CHTYPE", "PAYMENT GATEWAY");
			 Webdtl.addProperty("URI", URI);
			 Webdtl.addProperty("PAYLOAD", PAYLOAD);
			 Webdtl.addProperty("SIGNPAYLOAD", "");
			 Webdtl.addProperty("HEADERID", "");
			 Webdtl.addProperty("FLOW", "O");
			 
			 JsonArray Headers = new JsonArray();
			 
			 JsonObject Header_details = new JsonObject();
			 
			 Header_details.addProperty("Key", "Accept");
			 Header_details.addProperty("Value", "*/*");
			
			 Headers.add(Header_details);
			 
			 Header_details.addProperty("Key", "X-Vault-Token");
			 Header_details.addProperty("Value", token);
			 
			 Headers.add(Header_details);
			 
			 Webdtl.add("Headers", Headers);
			 
			 Webservice_call_Modal WCB = new Webservice_call_Modal();
				 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 if(api_out.get("Response_Code").getAsInt() != 204)
			 {
				 api_out = WCB.Send_https_Request(Webdtl);
			 }
			 
			 int Response_Code = api_out.get("Response_Code").getAsInt();
			  
			 if(Response_Code == 204)
			 {
				 flag = true;
			 }
    	}
    	catch(Exception ex)
    	{
    		logger.debug("exception in RotatePassword >>>> "+ex.getLocalizedMessage());
    	}
    	
    	return flag;
    }
    
    public void Update_DB_Password(HikariConfig config, String Password) 
    {
        try 
        {               
        	HikariDataSource datasource = new HikariDataSource(config);
        	
        	JdbcTemplate Jdbctemplate = new JdbcTemplate(datasource);
        	
        	String updateSql = "UPDATE HASHICORP_MANAGEMENT SET STATUS=?, PREVIOUS_SECRET = CURRENT_SECRET, CURRENT_SECRET = ?, EXPIRATION_DATE = trunc(sysdate) + ROTATION_INTERVAL,\r\n" + 
               		"NEXT_ROTATION_DATE = trunc(sysdate-5) + ROTATION_INTERVAL, LAST_UPDATED = CURRENT_TIMESTAMP WHERE APICODE = ? and CURRENT_SECRET <> ?";
               
            int count = Jdbctemplate.update(updateSql, new Object[] { "SUCCESS", Password, "DB", Password }); 
            
            logger.debug("No of records updated in Update_DB_Password function >>>> "+count);
        } 
        catch (Exception ex) 
        {
        	logger.debug("exception in Update_DB_Password >>>> "+ex.getLocalizedMessage());
        }
    }

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
    
    
}
