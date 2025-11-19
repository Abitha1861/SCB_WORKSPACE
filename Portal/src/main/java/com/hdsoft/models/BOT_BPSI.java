package com.hdsoft.models;


import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.util.concurrent.Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;
	
@Controller
@Component
public class BOT_BPSI 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_BPSI(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
		
	public BOT_BPSI() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_BPSI.class);
	
	@Autowired
	public RTSIS_AutoMan_Modal AutoMan;
	
	@Autowired
	public Webservice_Modal WB;
	
	@Autowired
	public Webservice_call_Modal WCB;
	
	@RequestMapping(value = {"/Datavision/bpsi/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
 	 	 JsonObject details = new JsonObject();
		
		 Common_Utils util = new Common_Utils();
		 
		 JsonObject js = util.StringToJsonObject(MESSAGE);
		  
		 String INFO1 = js.get("INFO1").getAsString();
		 String INFO2 = js.get("INFO2").getAsString();
		 String INFO3 = js.get("INFO3").getAsString();
		 
		 details = BPSI_v1(INFO1, INFO2, INFO3);
 	 		
 	 	 return details.toString();
    }
	
	public JsonObject BPSI_v1(String INFO1, String INFO2, String INFO3) 
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : ""; 
			 
			 Sql = "select column1 leid, column2 ccid from fileit003 " +
		               "where purpcode = ? " +
		               "and column1 in (select column1 from fileit003 " +
		               "where purpcode = 'CC003' and column7 in ( " +
		               "'18', '19', 'A1', 'A2', 'A3', 'B1', 'B2', 'F1', 'F2', 'F3', 'F8', 'F9', " +
		               "'G1', 'H1', 'H4', 'K1', 'K2', 'K3', 'M1', 'M2', 'M3', 'M4', 'M5', 'N2', " +
		               "'N3', 'N4', 'N5', 'N6', 'N7', 'N8', 'O1', 'O2', 'O3', 'O4', 'O5', 'O6', " +
		               "'P10', 'P11', 'P12', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7', 'P8', 'P9', 'S1', " +
		               "'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S99') " +
		               "and column11 = ?) and (column1,column2) not in (select column1,column2 from fileit003  where chcode=? and PURPCODE = ?)";
			 
			 List<Map<String, Object>> info = Jdbctemplate.queryForList(Sql, new Object[] { "CC009", "ACTIVE", "BPSI", "DAY0" });

             for(Map<String, Object> dtl : info) 
             {
                  String leid =  (String) dtl.get("leid");
                  String ccid = (String) dtl.get("ccid");
                  
                  JsonObject wb = WB.Get_Webserice_Info("BPSI000");
 				 
 				  JsonObject tkout = WCB.Okhttp_Send_Rest_Request(wb);
 			
 				  if(tkout.get("Response_Code").getAsInt() != 200)
 				  {
 					 tkout = WCB.Send_https_Request(tkout);
 				  }
 				 
 				 int Response_Code = tkout.get("Response_Code").getAsInt();
 				 
 				 String Response = tkout.get("Response").getAsString();
 				 
 				 String token = "";
 				 
 				 if(Response_Code == 200 && Response.contains("jwt")) //uat
 				 {
 					 JsonObject token_info = util.StringToJsonObject(Response);
 					 
 					 token = token_info.get("jwt").getAsString();
 				 }
 				 else if(Response_Code == 200 && Response.contains("access_token")) //prod
 				 {
 					 JsonObject token_info = util.StringToJsonObject(Response);
 					 
 					 token = token_info.get("access_token").getAsString();
 				 }
 				 
 				 String facilityTypeCode = "NA";
 				 String actpricingbfl = "NA";
 				 String collateralvalueCMV = "NA";

 				 String TaxIdentifierType = "NA";
 				 String TaxIdentifierType_LOVDesc = "NA";
 				 String TaxIDNumber = "NA";
 				 String FirstName = "NA";
 				 String MiddleName = "NA";
 				 String FLastName = "NA";
 				 String IdentifierValue = "NA";
 				 String Nationality = "NA";
 				 String PrimaryPhoneType = "NA";

 				 String UNISICCode = "NA";
 				 String ClientSegment = "NA";
 				 String ClientSegment_LOVDesc = "NA";
 				 String LegalEntityType = "NA";
 				 String LegalEntityType_LOVDesc = "NA";
 				 String TradingAsName = "NA";
 				 String LegalEntityID = "NA";
 				 String FirstName2 = "NA";
 				 String MiddleName2 = "NA";
 				 String FLastName2 = "NA";
 				 String IdentifierValue2 = "NA";
 				 String Nationality2 = "NA";
 				 String PrimaryPhoneType2 = "NA";
 				 String AssociationType = "NA";

 				 String[] Services = new String[] {"BPSI001", "BPSI002", "BPSI003"};
 				
 				 for(int z=0; z<Services.length; z++)
 				 {
 					 String SERVCODE = Services[z];
 					 
 					 wb = WB.Get_Webserice_Info(SERVCODE);
 	 				 
 	 				 String PAYLOAD = wb.get("PAYLOAD").getAsString();
 	 				 
 	 				 PAYLOAD = PAYLOAD.replace("~leid~", leid);
 	 				 PAYLOAD = PAYLOAD.replace("~ccid~", ccid);
 	 				
 	 				 wb.addProperty("PAYLOAD", PAYLOAD); 
 	 				 
 	 				 String Headers = wb.get("Headers").getAsJsonArray().toString().replace("~token~", token);
 	 				 
 	 				 wb.add("Headers", util.StringToJsonArray(Headers)); 
 	 				 
 	 				 JsonObject resout = WCB.Okhttp_Send_Rest_Request(wb);
 					
 	 				 if(resout.get("Response_Code").getAsInt() != 200 && resout.get("Response_Code").getAsInt() != 404)
 	 				 {
 	 					 resout = WCB.Send_https_Request(wb);
 	 				 }
 	 				 
 	 				 Response_Code = resout.get("Response_Code").getAsInt();
 	 				 
 	 				 Response = resout.get("Response").getAsString();
 	 				 
 	 				 if(Response_Code == 200)
 	 				 {
 	 					 JsonObject Res = util.StringToJsonObject(Response);
 	 					 
 	 					 JsonArray events = Res.get("events").getAsJsonArray();
 	 					 
 	 					 for(int i=0; i<events.size(); i++)
 	 					 {
 	 						 JsonObject js = events.get(i).getAsJsonObject();
 	 						 
 	 						 JsonObject payload = js.get("payload").getAsJsonObject();
 	 						 
 	 						 String entity = payload.get("entity").getAsString();
 	 						 
 	 						 JsonObject entity_1 = util.StringToJsonObject(entity);
 	 						 
 	 						 logger.debug(SERVCODE+" entity_1 >>>> "+entity_1);
 	 						 
 	 						 try
 	 						 {
 	 							 if(SERVCODE.equals("BPSI001"))	//v1
 	 	 						 {
 	 	 							 JsonArray bcaFacilityDetails = entity_1.get("bcaFacilityDetails").getAsJsonArray();
 	 								
 	 								 if(bcaFacilityDetails.size() > 0) 
 	 								 {
 	 									 JsonObject js_1 = bcaFacilityDetails.size() > 0 ? bcaFacilityDetails.get(0).getAsJsonObject() : new JsonObject();

 	 									 facilityTypeCode = js_1.has("facilityTypeCode") ? js_1.get("facilityTypeCode").getAsString() : ""; // Type of Credit = D43 (6 OTHERS)
 	 									 
 	 									 actpricingbfl = js_1.has("actpricingbfl") ? js_1.get("actpricingbfl").getAsString() : ""; //AnnualInterestRate - numeric
 	 								 }
 	 								 							  
 	 								 JsonArray collateralValue = entity_1.has("collateralValue") ? entity_1.get("collateralValue").getAsJsonArray() : new JsonArray();
 	 								 
 	 								 if(collateralValue.size()>0) 
 	 								 {
 	 									 JsonObject js_3 = collateralValue.size() > 0 ? collateralValue.get(0).getAsJsonObject() : new JsonObject();
 	 									 
 	 									 collateralvalueCMV = js_3.has("collateralvalueCMV") ? js_3.get("collateralvalueCMV").getAsString() : ""; //don't know might be numeric
 	 								 }
 	 	 						 }
 	 							
 	 	 						 //---------------------------------------------------------------------------------------
 	 	 						 
 	 	 						 if(SERVCODE.equals("BPSI002")) //TAX
 	 	 						 {
 	 	 							 JsonObject TaxClassifications = entity_1.has("TaxClassifications") ? entity_1.get("TaxClassifications").getAsJsonObject() : new JsonObject();
 	 								 JsonObject TaxIdentifiers = TaxClassifications.has("TaxIdentifiers") ? TaxClassifications.get("TaxIdentifiers").getAsJsonObject() : new JsonObject();
 	 								 JsonArray TaxIdentifier = TaxIdentifiers.has("TaxIdentifier") ? TaxIdentifiers.get("TaxIdentifier").getAsJsonArray() : new JsonArray();
 	 							
 	 								 if(TaxIdentifier.size() > 0) 
 	 								 {
 	 									  JsonObject js_1 = TaxIdentifier.get(0).getAsJsonObject();

 	 									  TaxIdentifierType = js_1.has("TaxIdentifierType") ? js_1.get("TaxIdentifierType").getAsString() : ""; //dont know
 	 									  TaxIdentifierType_LOVDesc = js_1.has("TaxIdentifierType_LOVDesc") ? js_1.get("TaxIdentifierType_LOVDesc").getAsString() : "";//dont know
 	 									  TaxIDNumber = js_1.has("TaxIDNumber") ? js_1.get("TaxIDNumber").getAsString() : ""; //numeric
 	 								 }
 	 								 
 	 								JsonObject RelatedParties = entity_1.has("RelatedParties") ? entity_1.get("RelatedParties").getAsJsonObject() : new JsonObject();
 	 								JsonObject Individuals = RelatedParties.has("Individuals") ? RelatedParties.get("Individuals").getAsJsonObject() : new JsonObject();
 	 								JsonArray Individual  = Individuals.has("Individual") ? Individuals.get("Individual").getAsJsonArray() : new JsonArray();
 	 								
 	 								if(Individual.size() > 0) 
 	 								{
 	 									 JsonObject js3 = Individual.get(0).getAsJsonObject();

 	 									 FirstName = js3.has("FirstName") ? js3.get("FirstName").getAsString() : "NA";
 	 									
 	 									 MiddleName = js3.has("MiddleName") ? js3.get("MiddleName").getAsString() : "NA";
 	 									
 	 									 FLastName = js3.has("FLastName") ? js3.get("FLastName").getAsString() : "NA";

 	 									 IdentifierValue = js3.has("IdentifierValue") ? js3.get("IdentifierValue").getAsString() : ""; //dont know
 	 									
 	 									 Nationality = js3.has("Nationality") ? js3.get("Nationality").getAsString() : ""; //lov - 218

 	 									 JsonObject Contact = js3.has("Contacts") ? js3.get("Contacts").getAsJsonObject().get("Contact").getAsJsonObject(): new JsonObject(); //PrimaryPhoneType is not there
 	 									
 	 									 PrimaryPhoneType = Contact.has("PrimaryPhoneType") ? Contact.get("PrimaryPhoneType").getAsString() : ""; //alphanumeric- numeric
 	 								}
 	 							 }
 	 	 						 
 	 	 						 if(SERVCODE.equals("BPSI003")) //other
 	 	 						 { 
 	 	 							 JsonObject LegalEntity = entity_1.has("LegalEntity") ? entity_1.get("LegalEntity").getAsJsonObject() :  new JsonObject(); //IndustryClassifications path not avaliable
 	 								 JsonObject IndustryClassifications = LegalEntity.has("IndustryClassifications") ? LegalEntity.get("IndustryClassifications").getAsJsonObject() : new JsonObject();
 	 							
 	 								 UNISICCode = IndustryClassifications.has("UNISICCode") ? IndustryClassifications.get("UNISICCode").getAsString() : ""; //alphanumeric

 	 								 JsonObject LEDetails = LegalEntity.has("LEDetails") ? LegalEntity.get("LEDetails").getAsJsonObject() : new JsonObject();
 	 								
 	 								 ClientSegment = LEDetails.has("ClientSegment") ? LEDetails.get("ClientSegment").getAsString() : ""; //lov  D54 //ClientSegment not available
 	 							
 	 								 ClientSegment_LOVDesc = LEDetails.has("ClientSegment_LOVDesc") ? LEDetails.get("ClientSegment_LOVDesc").getAsString() : ""; //text //ClientSegment_LOVDesc not available
 	 										
 	 								 LegalEntityType  = LEDetails.has("LegalEntityType") ? LEDetails.get("LegalEntityType").getAsString() : ""; //D05 //LegalEntityType not in the path
 	 								
 	 								 LegalEntityType_LOVDesc  = LEDetails.has("LegalEntityType_LOVDesc") ? LEDetails.get("LegalEntityType_LOVDesc").getAsString() : ""; //TEXT	//LegalEntityType_LOVDesc not in the path			
 	 								
 	 								 if(util.isNullOrEmpty(LegalEntityType))
 	 								 {
 	 									JsonObject TradingEntities = entity_1.has("TradingEntities") ? entity_1.get("TradingEntities").getAsJsonObject().get("TradingEntity").getAsJsonObject(): new JsonObject(); //TradingEntities path not available
 	 									
 	 									LegalEntityType = TradingEntities.has("LegalEntityType")? TradingEntities.get("LegalEntityType").getAsString() : "";

 	 									LegalEntityType_LOVDesc  = TradingEntities.has("LegalEntityType_LOVDesc")? TradingEntities.get("LegalEntityType_LOVDesc").getAsString() : "";
 	 								 }
 	 								
 	 								TradingAsName = LEDetails.has("TradingAsName")? LEDetails.get("TradingAsName").getAsString() : ""; //text //TradingAsName not in that path
 	 																
 	 								LegalEntityID  = LEDetails.has("LegalEntityID")? LEDetails.get("LegalEntityID").getAsString() : ""; //dont know
 	 								
 	 								JsonObject Hierarchy = LegalEntity.has("Hierarchy") ? LegalEntity.get("Hierarchy").getAsJsonObject() : new JsonObject(); //Hierarchy is not available
 	 								JsonArray Association = Hierarchy.has("Association") ? Hierarchy.get("Association").getAsJsonArray() : new JsonArray();
 	 								JsonObject js2 = (i >= 0 && i < Association.size()) ? Association.get(i).getAsJsonObject() : new JsonObject();
 	 								
 	 								if(util.isNullOrEmpty(LegalEntityID))
 	 								{
 	 									LegalEntityID  = js2.has("FromEntityID")? js2.get("FromEntityID").getAsString() : "";		
 	 									
 	 									if(util.isNullOrEmpty(LegalEntityID))
 	 									{
 	 										JsonObject TradingEntities = entity_1.has("TradingEntities") ? entity_1.get("TradingEntities").getAsJsonObject().get("TradingEntity").getAsJsonObject(): new JsonObject();
 	 										
 	 										LegalEntityID = TradingEntities.has("TradingEntityID")? TradingEntities.get("TradingEntityID").getAsString() : "";
 	 										
 	 										if(util.isNullOrEmpty(LegalEntityID))
 	 										{										
 	 											LegalEntityID  = TradingEntities.has("IMLegalEntityID")? TradingEntities.get("IMLegalEntityID").getAsString() : "";									
 	 										}								
 	 										
 	 									}								
 	 								}
 	 								
 	 								JsonObject RelatedParties = LegalEntity.get("RelatedParties").getAsJsonObject();
 	 								JsonObject Individuals = RelatedParties.get("Individuals").getAsJsonObject();
 	 								JsonArray Individual  = Individuals.get("Individual").getAsJsonArray();
 	 								JsonObject js3 = (i >= 0 && i < Individual.size()) ? Individual.get(i).getAsJsonObject() : new JsonObject();

 	 								FirstName2 = js3.has("FirstName") ? js3.get("FirstName").getAsString() : "NA";
 	 								
 	 								 MiddleName2 = js3.has("MiddleName") ? js3.get("MiddleName").getAsString() : "NA";

 	 								 FLastName2 = js3.has("FLastName") ? js3.get("FLastName").getAsString() : "NA";
 	 								
 	 								 IdentifierValue2 = js3.has("IdentifierValue") ? js3.get("IdentifierValue").getAsString() : ""; //dont know

 	 								 Nationality2 = js3.has("Nationality") ? js3.get("Nationality").getAsString() : ""; //lov - 218
 	 								
 	 								 JsonObject Contact = js3.has("Contacts") ? js3.get("Contacts").getAsJsonObject().get("Contact").getAsJsonObject(): new JsonObject();
 	 								
 	 								 PrimaryPhoneType2 = Contact.has("PrimaryPhoneType") ? Contact.get("PrimaryPhoneType").getAsString() : ""; //aplhanumeric //PrimaryPhoneType not available
 	 								
 	 								 AssociationType = js2.has("AssociationType") ? js2.get("AssociationType").getAsString() : ""; //dont know //AssociationType not available
 	 	 						 }
 	 		 					 
 	 		 				  } 
 	 						  catch(Exception ex)
 	 						  {
 	 							  logger.debug("Exception in extracting the json elements :::: "+ex.getLocalizedMessage());
 	 						  }
 	 					 }
 	 						 	 	  
 		 			 }
 				} 
 				
 				String sql = "INSERT INTO FILEIT003(SUBORGCODE, CHCODE, INDATE, INTIME, PURPCODE, RTYPE, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, " +
 			             "COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, " +
 			            "COLUMN27, COLUMN28) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

 				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "BPSI", util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), "DAY0", "D", leid, ccid, facilityTypeCode, actpricingbfl, collateralvalueCMV,  TaxIdentifierType, TaxIdentifierType_LOVDesc, TaxIDNumber,
 						FirstName, MiddleName, FLastName, IdentifierValue, Nationality, PrimaryPhoneType, UNISICCode, ClientSegment, ClientSegment_LOVDesc, LegalEntityType,
 						LegalEntityType_LOVDesc, TradingAsName, LegalEntityID, FirstName2, MiddleName2, FLastName2, IdentifierValue2, Nationality2, PrimaryPhoneType2, AssociationType});	 
             } 
             
             String sql = "select count(*) from FILEIT003 where CHCODE=? and PURPCODE=? and RTYPE=?";
             
             int count = Jdbctemplate.queryForObject(sql, new Object[] { "BPSI", "DAY0", "C"}, Integer.class);
             
             if(count == 0)
             {
            	  sql = "INSERT INTO FILEIT003(SUBORGCODE, CHCODE, INDATE, INTIME, PURPCODE, RTYPE, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, " +
			             "COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, " +
			            "COLUMN27, COLUMN28) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

             
            	 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "BPSI", util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), "DAY0", "C", 
            		    "leid", "ccid", "facilityTypeCode", "actpricingbfl", "collateralvalueCMV",  
            		    "TaxIdentifierType", "TaxIdentifierType_LOVDesc", "TaxIDNumber",
            		    "TaxFirstName", "TaxMiddleName", "TaxLastName", "TaxIdentifierValue", "TaxNationality", "TaxPrimaryPhoneType", 
            		    "UNISICCode", "ClientSegment", "ClientSegment_LOVDesc", "LegalEntityType", "LegalEntityType_LOVDesc", "TradingAsName", "LegalEntityID", 
            		    "FirstName", "MiddleName", "LastName", "IdentifierValue",  "Nationality", "PrimaryPhoneType", "AssociationType" });
             }
             
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in BPSI :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}


public JsonObject Generate_Suptech_Serial() 
{
	JsonObject details = new JsonObject();
	
	try
	{
		 String sql = "select RTSIS_REPORT_SERIAL.Nextval from dual";
		   
		 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
		 
		 details.addProperty("Serial", SL);
		
		 details.addProperty("Result", "Success");
		 details.addProperty("Message", "Serial generated Successfully !!");
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage());  	
		 
		 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
	 }
	
	 return details;
}

public JsonObject Generate_Batch_Serial() 
{
	JsonObject details = new JsonObject();
	
	try
	{
		 String sql = "select rts003_id_seq.Nextval from dual";
		   
		 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
		 
		 details.addProperty("Serial", SL);
		
		 details.addProperty("Result", "Success");
		 details.addProperty("Message", "Serial generated Successfully !!");
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage());  	
		 
		 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
	 }
	
	 return details;
}
}
