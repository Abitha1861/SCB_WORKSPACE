package com.hdsoft.models;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class FxSpot_Modal implements Database
{
	protected JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public FxSpot_Modal() {}
	
	public FxSpot_Modal(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	private static final Logger logger = LogManager.getLogger(FxSpot_Modal.class);
	
	 @Scheduled(cron = "0 15 19 * * *", zone = "Africa/Nairobi")
     public void Callback_Thread_Management()  
     { 
		 try 
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Date = util.getCurrentDate();
			 
			 call_exchangeRate(Date);
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception :::: "+e.getLocalizedMessage());
		 }
     }
	 
	@RequestMapping(value = {"/Datavision/fx-api/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String fx_api_test(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	{	 
 	 	JsonObject details = new JsonObject();
		
 	 	try
 	 	{
 	 		 Common_Utils util = new Common_Utils();
 	 		
 	 		 JsonObject Info = util.StringToJsonObject(MESSAGE);
 	 	 	
 	 	 	 String FromDate = Info.get("FromDate").getAsString();
 	 	 	 
 	 	 	 details = call_exchangeRate(FromDate);
 	 	}
 	 	catch(Exception ex)
 	 	{
 	 		details.addProperty("err", ex.getLocalizedMessage());
 	 	}
 	 	
 	 	return details.toString();
	}
	
	@RequestMapping(value = {"/Datavision/Currency-conversion/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	{	 
 	 	JsonObject details = new JsonObject();
		  
 	 	Common_Utils util = new Common_Utils();
 	 	
 	 	JsonObject Info = util.StringToJsonObject(MESSAGE);
 	 	
 	 	details = find_exchangeRate(Info.get("FromDate").getAsString(), Info.get("org").getAsString(), Info.get("SRC_CURR").getAsString());
 	 		
 	 	return details.toString();
	}
	
	public JsonObject call_exchangeRate(String FromDate) //yyyy-MM-dd
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			
			 String FromDate1 = FromDate;
			 
			 FromDate = util.Convert_Date_Format(FromDate, "yyyy-MM-dd", "dd-MMM-yyyy");
			 
			 if(Active_Mode.equals("UAT"))
			 {
				 FromDate = "18-SEP-2024";
			 }
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject wb = Get_Webserice_Info("FXSPOT", "FX001");
			 
			 Webservice_call_Modal wc = new Webservice_call_Modal();
			 
			 JsonObject tkout = wc.Okhttp_Send_Rest_Request(wb);
				
			 if(tkout.get("Response_Code").getAsInt() != 200 || !tkout.toString().contains("token"))
			 {
				 tkout = wc.Send_https_Request(tkout);
			 }
			 
			 int Response_Code = tkout.get("Response_Code").getAsInt();
			 
			 String Response = tkout.get("Response").getAsString();
			 
			 String token = "";
			 
			 if(Response_Code == 200 && Response.contains("token"))
			 {
				 JsonObject token_info = util.StringToJsonObject(Response);
				 
				 token = token_info.get("token").getAsString();
			 }
					
			 wb = Get_Webserice_Info("FXSPOT", "FX002");
			 
			 String URI = wb.get("URI").getAsString();
			 
			 URI = URI.replace("~startdate~", FromDate1);
			 URI = URI.replace("~enddate~", FromDate1);
			 
			 wb.addProperty("URI", URI);  
			 
			 String Headers = wb.get("Headers").getAsJsonArray().toString().replace("~token~", token);
			 
			 wb.add("Headers", util.StringToJsonArray(Headers));  
			 
			 JsonObject out = wc.Okhttp_Send_Rest_Request(wb);
			 
			 if(out.get("Response_Code").getAsInt() != 200 || !out.toString().contains("market_data"))
			 {
				 out = wc.Send_https_Request(tkout);
			 }
			 
			 Response = out.get("Response").getAsString();
					 
			 if(!util.isNullOrEmpty(Response))
			 {
				 JsonObject fxRes = util.StringToJsonObject(Response);
				 
				 details.add("response", fxRes);
				 
				 JsonObject market_data = fxRes.get("market_data").getAsJsonObject();
				 
				 JsonObject data_flow = market_data.get("data_flow").getAsJsonObject();
				 
				 String source_system = data_flow.get("source_system").getAsString();
				 String data_sender = data_flow.get("data_sender").getAsString();
				 
				 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy");
				 Timestamp tmes = util.get_oracle_Timestamp();
				 
				 JsonArray records = market_data.get("records").getAsJsonArray();
				 
				 for(int i=0; i<records.size(); i++)
				 {
					 JsonObject js = records.get(i).getAsJsonObject();
					 
					 String currency = js.get("currency").getAsString();
					 String other_currency = js.get("other_currency").getAsString();
					 				
					 JsonArray timeseries = js.get("timeseries").getAsJsonArray();
					 
					 if(timeseries.size() > 0)
					 {
						 JsonArray rate = timeseries.get(0).getAsJsonObject().get("rate").getAsJsonArray();
						 
						 String BID = "", ASK = "", MID = "", CASH = "";
						 
						 for(int j=0; j<rate.size(); j++)
						 {
							 String Rate = rate.get(j).getAsJsonObject().get("type").getAsString();
							 
							 if(Rate.equals("SC_BID"))
							 {
								 BID = rate.get(j).getAsJsonObject().get("value").getAsString();
							 }
							 
							 if(Rate.equals("SC_ASK"))
							 {
								 ASK = rate.get(j).getAsJsonObject().get("value").getAsString();
							 }
							 
							 if(Rate.equals("SC_MID"))
							 {
								  MID = rate.get(j).getAsJsonObject().get("value").getAsString();
							 }
							 
							 if(Rate.equals("SC_CASH_RATE"))
							 {
								  CASH = rate.get(j).getAsJsonObject().get("value").getAsString();
							 }
						 }
						 
						 String sql = "insert into FXRATE001(SUBORGCODE,EXCHDATE,SOURCESYS,CHCODE,SRC_CURR,DST_CURR,BIDRATE,ASKRATE,MIDRATE,CASHRATE,EXETIME) values(?,?,?,?,?,?,?,?,?,?,?)";
						 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, CurrentDate, source_system, data_sender, currency, other_currency, BID, ASK, MID, CASH, tmes });
					 }
				 }
			 }
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "conversion rate calculated successfully"); 				 
		}
		catch(Exception e)
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs in find_exchangeRate <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		}
		
		return details;
	}
	
	public JsonObject find_exchangeRate(String FromDate, String org, String SRC_CURR) 
	{
		JsonObject details = new JsonObject();
		
		String usd = "0", tzs = "0", TzsRate = "1", UsdRate = "1";
		
		try
		{
			 Common_Utils util = new Common_Utils();
			 
			 org = !util.isNullOrEmpty(org) ? org : "0";
		
			 /* 
			 String FromDate1 = FromDate;
			 
			 FromDate = util.Convert_Date_Format(FromDate, "yyyy-MM-dd", "dd-MMM-yyyy");
			 
			if(Active_Mode.equals("UAT"))
			 {
				 FromDate = "18-SEP-2024";
			 }
			 */
			 
			 SRC_CURR = SRC_CURR.toUpperCase();
			 
			 if(SRC_CURR.equals("USD"))
			 {
				 usd = org;
			 }
			 
			 if(SRC_CURR.equals("TZS"))
			 {
				 tzs = org;
			 }
			 
			 if(!util.isNullOrEmpty(SRC_CURR) && !SRC_CURR.equals("TZS"))
			 {
					 if(!SRC_CURR.equals("USD"))
					 {
						 String sql = "select (? * MIDRATE) || '|' || MIDRATE from FXRATE001 where SRC_CURR=? and DST_CURR =? and EXCHDATE = (select max(EXCHDATE) from FXRATE001)";
					 
						 List<String> out = Jdbctemplate.queryForList(sql, new Object[] { org, "USD", SRC_CURR }, String.class);
						 
						 if(out.size() > 0)
						 {
							 String[] output = out.get(0).split("\\|");
							 
							 usd = output[0];  UsdRate = output[1];
						 }
					 }
					 else
					 {
						 usd = org;
					 }
					 
					 String sql = "select (? * MIDRATE) || '|' || MIDRATE from FXRATE001 where SRC_CURR=? and DST_CURR =? and EXCHDATE = (select max(EXCHDATE) from FXRATE001)";
					 
					 List<String> out = Jdbctemplate.queryForList(sql, new Object[] { org, "TZS", SRC_CURR }, String.class);
					 
					 if(out.size() > 0)
					 {
						 String[] output = out.get(0).split("\\|");
						 
						 tzs = output[0];  TzsRate = output[1];
					 }
			 }
			 else
			 {
				 usd = "0";
				 tzs = org;
			 }
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "conversion rate calculated successfully"); 				 
		}
		catch(Exception e)
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs in find_exchangeRate <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		}
		
		details.addProperty("org", org); 
		details.addProperty("usd", usd);
		details.addProperty("tzs", tzs);
		details.addProperty("tzsrate", TzsRate);
		details.addProperty("usdrate", UsdRate);
		
		logger.debug(">>>>>>>>>>> find_exchangeRate out <<<<<<<<<<<<<<<"+details.toString());
		
		return details;
	}
	
	public JsonObject Get_Webserice_Info(String CHCODE, String SERVICECD) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "Select * from webservice001 where CHCODE=? and SERVICECD=?";
			 
			 List<web_service_001> API_Info = Jdbctemplate.query(sql, new Object[] {  CHCODE, SERVICECD }, new Webservice_Modal().new API_Mapper() );
			 
			 if(API_Info.size()!=0)
			 {
				 String PAYLOAD = API_Info.get(0).getPAYLOAD();
				 
				 PAYLOAD = PAYLOAD.replaceAll("\\n", "");
				 PAYLOAD = PAYLOAD.replaceAll("\\r", "");
				
				 String SIGNPAYLOAD = API_Info.get(0).getSIGNPAYLOAD();
				 
				 SIGNPAYLOAD = SIGNPAYLOAD.replaceAll("\\n", "");
				 SIGNPAYLOAD = SIGNPAYLOAD.replaceAll("\\r", "");
				
				 if(API_Info.get(0).getFORMAT().equalsIgnoreCase("JSON"))
				 {
					 PAYLOAD = PAYLOAD.replaceAll("\\s+", "");
					 SIGNPAYLOAD = SIGNPAYLOAD.replaceAll("\\s+", "");
				 }
				 
				 details.addProperty("SUBORGCODE", API_Info.get(0).getSUBORGCODE());
				 details.addProperty("CHCODE", API_Info.get(0).getCHCODE());
				 details.addProperty("SERVICECD", API_Info.get(0).getSERVICECD());
				 details.addProperty("SERVNAME", API_Info.get(0).getSERVNAME());
				 details.addProperty("FORMAT", API_Info.get(0).getFORMAT());
				 details.addProperty("PROTOCOL", API_Info.get(0).getPROTOCOL());	 
				 details.addProperty("METHOD", API_Info.get(0).getMETHOD());
				 details.addProperty("CHTYPE", API_Info.get(0).getCHTYPE());
				 details.addProperty("URI", API_Info.get(0).getURI());
				 details.addProperty("PAYLOAD", PAYLOAD);
				 details.addProperty("SIGNPAYLOAD", SIGNPAYLOAD);
				 details.addProperty("HEADERID", API_Info.get(0).getHEADERID());
				 details.addProperty("FLOW", API_Info.get(0).getFLOW());
				 
				 sql = "Select * from webservice002 where SERVICECD=? and CHCODE=? and HEADERID=?";
				 
				 List<web_service_002> Header_Info = Jdbctemplate.query(sql, new Object[] { API_Info.get(0).getSERVICECD(), API_Info.get(0).getCHCODE(), API_Info.get(0).getHEADERID() }, new Webservice_Modal().new Header_Mapper() );
				 
				 JsonArray Headers = new JsonArray();
				 
				 for(int i=0;i<Header_Info.size();i++)
				 {
					JsonObject Header_details = new JsonObject();
					 
					Header_details.addProperty("Key", Header_Info.get(i).getHEADKEY());
					Header_details.addProperty("Value", Header_Info.get(i).getHEADVALUE());
					
					Headers.add(Header_details);
				 }
				
				 details.add("Headers", Headers);
			 }
			 
			 details.addProperty("Result", API_Info.size()!=0 ? "Success" : "Failed");
			 details.addProperty("Message", API_Info.size()!=0 ? "API Configuration Details Found !!" : "API Configuration Details Not Found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Get_Webserice_Info :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
}
