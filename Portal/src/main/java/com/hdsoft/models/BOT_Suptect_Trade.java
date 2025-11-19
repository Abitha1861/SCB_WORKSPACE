package com.hdsoft.models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.Repositories.Lookup001;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_Trade implements Database 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_Trade(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptect_Trade() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_Trade.class);
	
	@RequestMapping(value = {"/Datavision/trade/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestHeader("APICODE") String APICODE, @RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
 	 	JsonObject details = new JsonObject();
		  
		try
		{
			details = Trade_Master(Message); 
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	public JsonObject Trade_Master(String INFO3) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			 
			 if(data.has("anchorLimits"))
			 {
				 details  = undrawnBalanceData_LTP("RTS103", "", INFO3);
				 
				 return details;
			 }
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 String countryCode = data.get("countryCode").getAsString();
			 
			 if(!countryCode.equals("TZ"))
			 {
				 logger.debug("Deal refNo "+dealReferenceNo+" Not belongs to the country TZ, Hence Omitted");
				 
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP06");
				 details.addProperty("message", "Deal refNo "+dealReferenceNo+" Not belongs to the country TZ, Hence Omitted");  
				 
				 return details;
			 }
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = "";
			 
			 
			 JsonObject transactionDetails = data.has("transactionDetails") && !data.get("transactionDetails").isJsonNull() ? data.getAsJsonObject("transactionDetails") : new JsonObject();
			 JsonObject miscellaneousStepDetails = transactionDetails.has("miscellaneousStepDetails") && !transactionDetails.get("miscellaneousStepDetails").isJsonNull() ? transactionDetails.getAsJsonObject("miscellaneousStepDetails") : new JsonObject();
			 String miscTypeCode = miscellaneousStepDetails.has("miscTypeCode")? !util.isNullOrEmpty(miscellaneousStepDetails.get("miscTypeCode").getAsString())? miscellaneousStepDetails.get("miscTypeCode").getAsString() :"NA" : "NA";


			 if(data.has("subProduct"))
			 {
				 JsonObject subProduct = data.get("subProduct").getAsJsonObject();  
				 
				 if(subProduct.has("code")) 
				 {
					 subprodcode = subProduct.get("code").getAsString();
				 }
			 }
			 
			 String status_code = data.get("status").getAsJsonObject().get("code").getAsString();
			  
			 if(processingSystemCode.equals("DTP") || processingSystemCode.equals("OTP"))
			 {	
				 String parentdeal = step_code.equalsIgnoreCase("ISS") ? "1" : "0";  
				 
				 //String Dcheck = "select count(*) from TRADE001 where suborgcode=? and SYSCODE=? and CHCODE=? and DEALREFNO=? and PRODCODE=? and STEPCODE=?";
						 
				 //int dcount = Jdbctemplate.queryForObject(Dcheck, new Object[] { SUBORGCODE, processingSystemCode, requestorSystemCode, dealReferenceNo, product_code, step_code }, Integer.class);		 
				 
				 //if(dcount == 0)
				 //{
				     try
				     {
				    	 String sql = "insert into TRADE001(suborgcode,syscode,chcode,paytype,reqdate,reqtime,evntime,parentdeal,notificationid,dealrefNo,stepcode,prodcode,subprodcode,message,status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
				    	 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, processingSystemCode, requestorSystemCode, "RTSIS", CurrentDate, timestamp, evntime, parentdeal, notificationID, dealReferenceNo, step_code, product_code, subprodcode, INFO3, "PENDING" });
				     }
				     catch(Exception ex) {}
				     
					 //}
			 }
			 
			 if(processingSystemCode.equals("DTP") && product_code.equals("BGT") && !subprodcode.equals("SL") && status_code.equals("REL"))
			 {
				 if(step_code.equalsIgnoreCase("ISS"))
				 {
					 Outstanding_Guarantee_BGT_ISS("RTS075", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("AMD"))
				 {
					 Outstanding_Guarantee_BGT_AMD("RTS075", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("PAY"))
				 {
					 Outstanding_Guarantee_BGT_PAY("RTS075", "", INFO3);  
					 
					 if(INFO3.contains("swiftMsgDetails"))  
					 { 
						 outgoingBankFundsTransfer_MT103("RTS197", "", INFO3);
					 }
				 }
				 else if (step_code.equalsIgnoreCase("MSC"))
				 {
				 
					 if (miscTypeCode.equalsIgnoreCase("NC") ||  miscTypeCode.equalsIgnoreCase("PO") ||  miscTypeCode.equalsIgnoreCase("PR") || 
							    miscTypeCode.equalsIgnoreCase("3A") ||  miscTypeCode.equalsIgnoreCase("CL") || miscTypeCode.equalsIgnoreCase("11")) 
					 {

						 Outstanding_Guarantee_BGT_PAY("RTS075", "", INFO3);
					 }

				 }
			 }
			 else  if(processingSystemCode.equals("DTP") && product_code.equals("SHPGT") && subprodcode.equals("SG") && status_code.equals("REL"))
			 {
				 if(step_code.equalsIgnoreCase("ISS"))
				 {
					 Outstanding_Guarantee_SHPGT_ISS("RTS075", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("AMD"))
				 {
					 Outstanding_Guarantee_SHPGT_AMD("RTS075", "", INFO3);
				 }
			 }
			 else  if(processingSystemCode.equals("DTP") && product_code.equals("ELC") && status_code.equals("REL"))
			 {
				 if(step_code.equalsIgnoreCase("ISS"))
				 {
					 ExportLetterOfCredit_ELC_ISS("RTS077", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("AMD"))
				 {
					 ExportLetterOfCredit_ELC_AMD("RTS077", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("MSC"))
				 {
					 if (miscTypeCode.equalsIgnoreCase("CN") ||  miscTypeCode.equalsIgnoreCase("CP")) 
						{
						 	ExportLetterOfCredit_ELC_MSC("RTS077", "", INFO3);
						}					 
				 }
			 }
			 else  if(processingSystemCode.equals("DTP") && product_code.equals("ILC") && status_code.equals("REL"))
			 {
				 if(step_code.equalsIgnoreCase("ISS")) 
				 {
					 OustandingLetterCredit_ILC_ISS("RTS079", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("ACP")) 
				 {
					 Customerliabilities_ACP_AAC_POA("RTS029", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("AAC")) 
				 {
					 OustandingLetterCredit_ILC_AAC("RTS079", "", INFO3);
					 
					 Customerliabilities_ACP_AAC_POA("RTS029", "", INFO3);
					 
					 Outstanding_Acceptance_ILC_AAC("RTS065", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("AMD")) 
				 {
					 OustandingLetterCredit_ILC_AMD("RTS079", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("POA")) 
				 {
					 OustandingLetterCredit_ILC_POA("RTS079", "", INFO3);
					 
					 Customerliabilities_ACP_AAC_POA("RTS029", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("APA")) 
				 {
					 OustandingLetterCredit_ILC_APA("RTS079", "", INFO3);
					 
					 Customerliabilities_APA("RTS029", "", INFO3);
				 }
				 else if (step_code.equalsIgnoreCase("MSC")) //NEW LOGIC
				 {
					 if (miscTypeCode.equalsIgnoreCase("RI") ||  miscTypeCode.equalsIgnoreCase("0A") || miscTypeCode.equalsIgnoreCase("0B") ||
							    miscTypeCode.equalsIgnoreCase("FJ") || miscTypeCode.equalsIgnoreCase("CR") ||  miscTypeCode.equalsIgnoreCase("PP") ||
							    miscTypeCode.equalsIgnoreCase("16"))
								{
						 			OustandingLetterCredit_ILC_MSC("RTS079", "", INFO3);
								}
				}
				 
			 }
			 else  if(processingSystemCode.equals("DTP") && product_code.equals("SBLC") && status_code.equals("REL"))
			 {
				 if(step_code.equalsIgnoreCase("ISS"))
				 {
					 Outstanding_lettercredit_SBLC_ISS("RTS079", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("AMD"))
				 {
					 Outstanding_lettercredit_SBLC_AMD("RTS079", "", INFO3);
				 }
				 else if(step_code.equalsIgnoreCase("PAY"))
				 {
					 Outstanding_Guarantee_SBLC_PAY("RTS075", "", INFO3);
				 }
			 }
			 else  if(processingSystemCode.equals("DTP") && product_code.equals("IMDOC") && status_code.equals("REL"))
			 {
					if (step_code.equalsIgnoreCase("ISS")) 
					{
						inwardBills_IMDOC_ISS("RTS081", "", INFO3);
					}
					else if (step_code.equalsIgnoreCase("AMD")) 
					{
						inwardBills_IMDOC_AMD("RTS081", "", INFO3);
					}
					else if (step_code.equalsIgnoreCase("POA")) 
					{
						inwardBills_IMDOC_POA("RTS081", "", INFO3);
					}
					else if (step_code.equalsIgnoreCase("PAY")) 
					{
						inwardBills_IMDOC_PAY("RTS081", "", INFO3);
					}
			 }  
			 else  if(processingSystemCode.equals("DTP") && product_code.equals("EXDOC") && status_code.equals("REL"))
			 {
					if (step_code.equalsIgnoreCase("ISS")) 
					{
						OutwardBills_EXDOC_ISS("RTS083", "", INFO3);
					}
					else if (step_code.equalsIgnoreCase("AMD")) 
					{
						OutwardBills_EXDOC_AMD("RTS083", "", INFO3);
					}
					else if (step_code.equalsIgnoreCase("POA")) 
					{
						OutwardBills_EXDOC_POA("RTS083", "", INFO3);
					}
					else if (step_code.equalsIgnoreCase("PAY")) 
					{
						OutwardBills_EXDOC_PAY("RTS083", "", INFO3);
					}
			 }
			 else if(processingSystemCode.equals("OTP") && (product_code.equals("IFNS") || product_code.equals("IFNB") || product_code.equals("PSHP") || product_code.equals("VPRP")))
			 {
				 /*if(step_code.equals("NEW"))
				 {
					 details = LoanTransaction_OTP("RTS191", "", INFO3);
				 }*/
				 
				 if(INFO3.contains("financeRequest"))
				 {
					 LoanInformation_OTP("RTS019", "", INFO3); 
					 
					 LoanTransaction_OTP("RTS191", "", INFO3);
				 }
				 
				 if(INFO3.contains("swiftMsgDetails") && (INFO3.contains("MT103") || INFO3.contains("PAC08")))
				 {
					 outgoingBankFundsTransfer_OTP_PARENT("RTS197", "", INFO3);
				 }
				 
			 }
			 
			 /** loan information & loan txn ***/
			 
			 if(processingSystemCode.equals("DTP") && INFO3.contains("financeEventType"))
			 {
				 LoanInformation_DTP("RTS019", "", INFO3);
				 
				 LoanTransaction_DTP("RTS191", "", INFO3);
				 
				 writtenOffLoans_DTP("RTS163", "", INFO3); 
			 }
			 			 
			 //LoanInformation_OTP
				     
			 //details.addProperty("result", "success");
			 //details.addProperty("stscode", "HP00");
			 //details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	/*** Outstanding Guarantee *****/
	
	public JsonObject Outstanding_Guarantee_BGT_ISS(String INFO1, String INFO2, String INFO3) //RTS075
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject guaranteeDetails = data.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			
			 String guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject().get("code").getAsString();
			 
			 String issueDate = guaranteeDetails.get("issueDate").getAsString();
			 String expiryDate = guaranteeDetails.get("expiryDate").getAsString();
			 
			 String bankRelationship_Type = "2";
			 
			 String bcountryCode="";
			 
			 String partyRoleDesc="";
			 
			 String leid ="";
			  
			 String counterGuarantor_Country="";
			  
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 JsonObject party = js.get("party").getAsJsonObject();
				 
				 String name = party.get("name").getAsString();
				 
				 String partyId = party.get("id").getAsString();
				 
				 if(partyRoleCode.equalsIgnoreCase("ISSBK"))
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 String countryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.equals("APPLBK"))   // not in party code
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 counterGuarantor_Country = address.get("countryCode").getAsString();
					 
					 partyRoleDesc = partyRole.get("desc").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.toUpperCase().equals("CUST") || partyId.toUpperCase().equals("CUST")) // not in party code
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 bcountryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 } 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
				
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String beneficiaryName = data.get("customerName").getAsString();
			 String relationshipType = "1";
			 String bankRelationshipType = bankRelationship_Type; 
			 String guaranteeTypes = guaranteeType;
			 String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", leid+"|"+LimitID); // CC
			 String beneficiaryCountry = bcountryCode; //work on lookup value
			 String counterGuarantorName = partyRoleDesc;
			 String counterGuarantorCountry = counterGuarantor_Country; 
			 boolean ratingStatus = true;
			 String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCounterGuarantor = ""; 
			 String currency = guaranteeAmount.get("currency").getAsString(); 
			 String orgAmount = guaranteeAmount.get("value").getAsString();
			 String usdAmount = "0"; 
			 String tzsAmount = guaranteeAmount.get("value").getAsString(); 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 
			 collateralTypes = util.isNullOrEmpty(collateralTypes) ? "2" : collateralTypes;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeType, "Trade001" }, new Lookup001_mapper());
					 
			 guaranteeTypes = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterGuarantorCountry }, new Lookup001_mapper());
				
			 counterGuarantorCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate, openingDate, maturityDate, beneficiaryName, relationshipType, 
					bankRelationshipType, guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, counterGuarantorCountry, ratingStatus, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency, orgAmount,
					usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData", "serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType", 
					"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry", "counterGuarantorName", "counterGuarantorCountry", "ratingStatus","crRatingCounterGuarantor", "gradesUnratedCounterGuarantor", "currency", "orgAmount",
					"usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Outstanding_Guarantess_BGT_ISS :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Outstanding_Guarantee_BGT_AMD(String INFO1, String INFO2, String INFO3) //RTS075
	{ 
		 JsonObject details = new JsonObject();		 
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject guaranteeDetails = data.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			
			 String guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject().get("code").getAsString();
			 
			 String issueDate = guaranteeDetails.has("issueDate") ? guaranteeDetails.get("issueDate").getAsString() : ""; 
			 String expiryDate = guaranteeDetails.has("expiryDate") ? guaranteeDetails.get("expiryDate").getAsString() : "";
			 
			 String bankRelationship_Type = "2";
			 
			 String bcountryCode="";
			 
			 String partyRoleDesc="";
			 
			 String leid ="";
			  
			 String counterGuarantor_Country="";
			  
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 JsonObject party = js.get("party").getAsJsonObject();
				 
				 String name = party.get("name").getAsString();
				 
				 String partyId = party.get("id").getAsString();
				 
				 
				 
				 if(partyRoleCode.equalsIgnoreCase("ISSBK"))
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 String countryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.equals("APPLBK"))   // not in party code
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 counterGuarantor_Country = address.get("countryCode").getAsString();
					 
					 partyRoleDesc = partyRole.get("desc").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.toUpperCase().equals("CUST") || partyId.toUpperCase().equals("CUST")) // not in party code
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 bcountryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 } 
			 }
			 
			 if(util.isNullOrEmpty(issueDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject guaranteeDetails_ = data1.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
					 
					 issueDate = guaranteeDetails_.has("issueDate") ? guaranteeDetails_.get("issueDate").getAsString() : "";
				 }
			 }
			 
			 if(util.isNullOrEmpty(expiryDate))
			 {
				 expiryDate = util.AddYearstoDate(issueDate, "yyyy-MM-dd", 6); 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String beneficiaryName = data.get("customerName").getAsString();
			 String relationshipType = "1";
			 String bankRelationshipType = bankRelationship_Type; 
			 String guaranteeTypes = guaranteeType;
			 String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", leid+"|"+LimitID); // CC
			 String beneficiaryCountry = bcountryCode;
			 String counterGuarantorName = partyRoleDesc;
			 String counterGuarantorCountry = counterGuarantor_Country;//work on lookup value// APPLK NA
			 boolean ratingStatus = true;
			 String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCounterGuarantor = "";  
			 String currency = guaranteeAmount.get("currency").getAsString();
			 String orgAmount = guaranteeAmount.get("value").getAsString();
			 String usdAmount = "0";
			 String tzsAmount = guaranteeAmount.get("value").getAsString(); 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 
			 collateralTypes = util.isNullOrEmpty(collateralTypes) ? "2" : collateralTypes;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeType, "Trade001" }, new Lookup001_mapper());
					 
			 guaranteeTypes = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterGuarantorCountry }, new Lookup001_mapper());
				
			 counterGuarantorCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
		 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate, openingDate, maturityDate, beneficiaryName, relationshipType, 
					bankRelationshipType, guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, ratingStatus, counterGuarantorCountry, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency, orgAmount,
					usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData", "serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType", 
					"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry", "counterGuarantorName", "ratingStatus", "counterGuarantorCountry", "crRatingCounterGuarantor", "gradesUnratedCounterGuarantor", 
					"currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Outstanding_Guarantee_BGT_AMD :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Outstanding_Guarantee_BGT_PAY(String INFO1, String INFO2, String INFO3) //RTS075
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject guaranteeDetails = data.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			
			 String guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject().get("code").getAsString();
			 
			 String issueDate = guaranteeDetails.has("issueDate") ? guaranteeDetails.get("issueDate").getAsString() : ""; 
			 String expiryDate = guaranteeDetails.has("expiryDate") ? guaranteeDetails.get("expiryDate").getAsString() : "";
			 
			 String bankRelationship_Type = "2";
			 
			 String bcountryCode="";
			 
			 String partyRoleDesc="";
			 
			 String leid ="";
			  
			 String counterGuarantor_Country="";
			  
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 JsonObject party = js.get("party").getAsJsonObject();
				 
				 String name = party.get("name").getAsString();
				 
				 String partyId = party.get("id").getAsString();
				 
				
				 
				 if(partyRoleCode.equalsIgnoreCase("ISSBK"))
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 String countryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.equals("APPLBK"))   // not in party code
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 counterGuarantor_Country = address.get("countryCode").getAsString();
					 
					 partyRoleDesc = partyRole.get("desc").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.toUpperCase().equals("CUST") || partyId.toUpperCase().equals("CUST")) // not in party code
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 bcountryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 } 
			 }
			 
			 if(util.isNullOrEmpty(issueDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject guaranteeDetails_ = data1.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
					 
					 issueDate = guaranteeDetails_.has("issueDate") ? guaranteeDetails_.get("issueDate").getAsString() : "";
				 }
			 }
			 
			 if(util.isNullOrEmpty(expiryDate))
			 {
				 expiryDate = util.AddYearstoDate(issueDate, "yyyy-MM-dd", 6); 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String beneficiaryName = data.get("customerName").getAsString();
			 String relationshipType = "1";
			 String bankRelationshipType = bankRelationship_Type; 
			 String guaranteeTypes = guaranteeType;
			 String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", leid+"|"+LimitID); // CC
			 String beneficiaryCountry = bcountryCode; 
			 String counterGuarantorName = partyRoleDesc;
			 String counterGuarantorCountry = counterGuarantor_Country; 
			 boolean ratingStatus = true;
			 String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCounterGuarantor = ""; 
			 String currency = guaranteeAmount.get("currency").getAsString();
			 String orgAmount = guaranteeAmount.get("value").getAsString();
			 String usdAmount = "0";  
			 String tzsAmount = guaranteeAmount.get("value").getAsString();  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 
			 collateralTypes = util.isNullOrEmpty(collateralTypes) ? "2" : collateralTypes;
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeType, "Trade001" }, new Lookup001_mapper());
					 
			 guaranteeTypes = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterGuarantorCountry }, new Lookup001_mapper());
				
			 counterGuarantorCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate, openingDate, maturityDate, beneficiaryName, relationshipType, 
					bankRelationshipType, guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, counterGuarantorCountry, ratingStatus, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency, orgAmount,
					usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData", "serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType", 
					"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry", "counterGuarantorName", "counterGuarantorCountry", "ratingStatus","crRatingCounterGuarantor", "gradesUnratedCounterGuarantor", "currency", "orgAmount",
					"usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Outstanding_Guarantess_ISS_PAY :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	

	public JsonObject outgoingBankFundsTransfer_OTP_PARENT(String INFO1, String INFO2, String INFO3) //RTS197
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String dealReferenceNo = data.has("dealReferenceNo")? util.isNullOrEmpty(data.get("dealReferenceNo").getAsString())? "" : data.get("dealReferenceNo").getAsString() : "";
			 
			 dealReferenceNo = dealReferenceNo.replaceAll("[^a-zA-Z0-9]", "");

			 JsonObject transactionDetails = data.has("transactionDetails")? data.get("transactionDetails").getAsJsonObject() : new JsonObject();		 			
			 JsonObject generalDetails = transactionDetails.has("generalDetails") ? transactionDetails.get("generalDetails").getAsJsonObject()  : new JsonObject();

			 String txnApprovalTime = generalDetails.has("txnApprovalTime") ? util.isNullOrEmpty(generalDetails.get("txnApprovalTime").getAsString()) ? "" : generalDetails.get("txnApprovalTime").getAsString() : "";

			 JsonArray swiftMsgDetails = transactionDetails.has("swiftMsgDetails") && transactionDetails.get("swiftMsgDetails").isJsonArray() ? transactionDetails.get("swiftMsgDetails").getAsJsonArray() : new JsonArray();

			 String res_name = null;
			 String resp_bic = null;
			 String sender_inst = null;
			 String currency_val = null;
			 String org_amt = null;
			 String res_name_value = null;
			 String recAccWallet = null;
			 String reci_country = null;
			 String  cust_value = null;
			 String cust_id = null;
			 
			 
			 for (int j = 0; j < swiftMsgDetails.size(); j++) 
			 {
			    JsonObject swift_detail_code = swiftMsgDetails.get(j).isJsonObject() ? swiftMsgDetails.get(j).getAsJsonObject() : new JsonObject();
			   
			    JsonArray tag = swift_detail_code.has("tag") && swift_detail_code.get("tag").isJsonArray()? swift_detail_code.get("tag").getAsJsonArray() : new JsonArray();

				 for(int i=0; i<tag.size(); i++)
				 {
					 JsonObject tag_name_value = tag.get(i).isJsonObject() ? tag.get(i).getAsJsonObject() : new JsonObject();

					 String name = tag_name_value.has("name") ? (util.isNullOrEmpty(tag_name_value.get("name").getAsString()) ? "" : tag_name_value.get("name").getAsString()) : "";

					 if(name.contains("59"))
					 {
						 res_name_value = tag_name_value.has("value") ? (util.isNullOrEmpty(tag_name_value.get("value").getAsString()) ? "" : tag_name_value.get("value").getAsString()) : "";

						 recAccWallet = res_name_value.split("\\|+").length > 0 ? res_name_value.split("\\|+")[0].replaceAll("[^a-zA-Z0-9]", "") : "";

						 res_name = res_name_value.split("\\|+").length > 1 ? res_name_value.split("\\|+")[1].replaceAll("[^a-zA-Z0-9 ]", "") : "";

						 reci_country = res_name_value.split("\\|+").length > 0  ? res_name_value.split("\\|+")[res_name_value.split("\\|+").length - 1].replaceAll("[^a-zA-Z0-9 ]", "")  : "";

					 }
					 if(name.contains("57"))
					 {
						 resp_bic = tag_name_value.has("value") ? (util.isNullOrEmpty(tag_name_value.get("value").getAsString()) ? "" : tag_name_value.get("value").getAsString().replaceAll("[^a-zA-Z0-9]", "")) : "";

					 }
					 if(name.contains("72"))
					 {
					
						 sender_inst = tag_name_value.has("value") ? (util.isNullOrEmpty(tag_name_value.get("value").getAsString()) ? "" : tag_name_value.get("value").getAsString().replaceAll("[^a-zA-Z0-9]", "")) : "";

					 }
					 if(name.contains("32"))
					 {
						 String input = tag_name_value.has("value") ? (util.isNullOrEmpty(tag_name_value.get("value").getAsString()) ? "" : tag_name_value.get("value").getAsString()) : "";

						 currency_val = input.length() >= 9 ? input.substring(6, 9) : "";

						 org_amt = input.matches(".*\\d+,\\d+.*") ? input.replaceAll(".*?(\\d+,\\d+).*", "$1").replace(",", ".") : "";
					 }
					 if(name.contains("50")) //this is for senderacct_no
					 { 
						 cust_value = tag_name_value.has("value") ? (util.isNullOrEmpty(tag_name_value.get("value").getAsString()) ? "" : tag_name_value.get("value").getAsString()) : "";

						 cust_id = cust_value.split("\\|+").length > 0 ? cust_value.split("\\|+")[0].replaceAll("[^a-zA-Z0-9]", "") : "";

					 }
				 }
			 }
			 
			 
			 String reci_country_lower = !util.isNullOrEmpty(reci_country) ? reci_country.toLowerCase() : "";


			 if (reci_country_lower.contains("tanzani") || reci_country_lower.contains("tz") || reci_country_lower.contains("tzs") || reci_country_lower.contains("dar es salaa")) 
			 {
				 reci_country = "TZ";							  
			 }
			 else 
			 {
				 JsonArray partyDetails = transactionDetails.has("partyDetails") && transactionDetails.get("partyDetails").isJsonArray() ? transactionDetails.get("partyDetails").getAsJsonArray() : new JsonArray();			
					
				 for (int i = 0; i < partyDetails.size(); i++) 
				 {
					 JsonObject add_js = partyDetails.get(i).isJsonObject() ? partyDetails.get(i).getAsJsonObject() : new JsonObject();
					   
					 JsonObject party = add_js.has("party")? add_js.get("party").getAsJsonObject(): new JsonObject() ;
					 
					 String party_name = party.has("name") ? (util.isNullOrEmpty(party.get("name").getAsString()) ? "" : party.get("name").getAsString()) : "";

					 if(party_name.equalsIgnoreCase(res_name))
					 {
						 JsonObject address = add_js.has("address") && add_js.get("address").isJsonObject() ? add_js.get("address").getAsJsonObject() : new JsonObject();

						 String countryCode = address.has("countryCode") ? (util.isNullOrEmpty(address.get("countryCode").getAsString()) ? "" : address.get("countryCode").getAsString()) : "";

						 reci_country = countryCode;
					 }
				 }
			 }
			 
			 
			 String leId = data.has("leId")? data.get("leId").getAsString() : "";
	
			
			 String sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
			 
			 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { leId }, String.class);
			 
			 String senderId_no = (Info2.size() > 0) ? (util.isNullOrEmpty(Info2.get(0)) ? "NA" : Info2.get(0)) : "NA";

			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = dealReferenceNo;
			 String transactionDate = convertHktToTanzaniaDate(util.Convert_BOT_Date_Format(txnApprovalTime, "yyyy-MM-dd'T'HH:mm"));
			 String transferChannel = "3";
			 String subCategoryTransferChannel = "";
			 String senderAccountNumber = cust_id;  //DOUBT
			 String recipientName = res_name;
			 String recipientMobileNumber = "NA";
			 String recipientCountry = reci_country;
			 String serviceChannel = "5";
			 String serviceCategory = "5";
			 String serviceSubCategory = "3";
			 String senderIdentificationType = "12";//need take it from client coverage
			 String senderIdentificationNumber = senderId_no;//need take it from client coverage
			 String recipientBankOrFspCode = resp_bic;
			 String recipientAccountOrWalletNumber = recAccWallet;
			 String orgAmount = org_amt;
			 String currency = currency_val;
			 String usdAmount = "0.00";
			 String tzsAmount = "0.00";
			 String purposes = "360123";
			 String senderInstruction = util.isNullOrEmpty(sender_inst)? "NA" : sender_inst;
			 String transactionPlace = "218";

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
					
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
			 
		 	sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
		 	List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";			

			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
				
			recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";

			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count, reportingDate, transactionId, transactionDate, transferChannel, subCategoryTransferChannel, 
					 senderAccountNumber, recipientName, recipientMobileNumber, recipientCountry, serviceChannel, serviceCategory, serviceSubCategory, senderIdentificationType, senderIdentificationNumber, recipientBankOrFspCode, 
					 recipientAccountOrWalletNumber, orgAmount, currency, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel",
					 "senderAccountNumber", "recipientName", "recipientMobileNumber", "recipientCountry", "serviceChannel", "serviceCategory", "serviceSubCategory", "senderIdentificationType", "senderIdentificationNumber", "recipientBankOrFspCode",
					 "recipientAccountOrWalletNumber", "orgAmount", "currency", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Banks Data", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OutgoingBankfundsTransfer :::: "+e.getLocalizedMessage());
		 }
		
		 return details;

	}
	

	

	public JsonObject outgoingBankFundsTransfer_MT103(String INFO1, String INFO2, String INFO3) //RTS197
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String leId = data.has("leId")? data.get("leId").getAsString() : "";
			 
			 JsonObject transactionDetails = data.has("transactionDetails")? data.get("transactionDetails").getAsJsonObject() : new JsonObject();	
			 
			 String dealReferenceNo = data.has("dealReferenceNo")? util.isNullOrEmpty(data.get("dealReferenceNo").getAsString())? "" : data.get("dealReferenceNo").getAsString() : "";
			 
			 JsonObject stepDetails = transactionDetails.has("stepDetails") ? transactionDetails.get("stepDetails").getAsJsonObject()  : new JsonObject();

			 String transactionApprovalTime = stepDetails.has("transactionApprovalTime") ? util.isNullOrEmpty(stepDetails.get("transactionApprovalTime").getAsString()) ? "" : stepDetails.get("transactionApprovalTime").getAsString() : "";
			 JsonArray invoiceDetails = transactionDetails.has("invoiceDetails") && transactionDetails.get("invoiceDetails").isJsonArray() ? transactionDetails.get("invoiceDetails").getAsJsonArray() : new JsonArray();
			 
			 
			 String accountNo = null ;
			 
			 for(int i=0; i<invoiceDetails.size(); i++)
			 {
				 JsonObject invoicePartyCode = invoiceDetails.get(i).isJsonObject() ? invoiceDetails.get(i).getAsJsonObject() : new JsonObject();

				 JsonObject invoice_Code = invoicePartyCode.has("invoicePartyCode") && invoicePartyCode.get("invoicePartyCode").isJsonObject()? invoicePartyCode.get("invoicePartyCode").getAsJsonObject(): new JsonObject();

				 String code = invoice_Code.has("code") ? util.isNullOrEmpty(invoice_Code.get("code").getAsString()) ? "" : invoice_Code.get("code").getAsString() : "";
				 
				 if(code.equalsIgnoreCase("CUST")||code.equalsIgnoreCase("CST"))
				 {					
					  accountNo = invoicePartyCode.has("accountNo") ? util.isNullOrEmpty(invoicePartyCode.get("accountNo").getAsString()) ? "" : invoicePartyCode.get("accountNo").getAsString() : "";

				 }
			 }
			 
			 JsonArray swiftMsgDetails = transactionDetails.has("swiftMsgDetails") && transactionDetails.get("swiftMsgDetails").isJsonArray() ? transactionDetails.get("swiftMsgDetails").getAsJsonArray() : new JsonArray();
 
			 String res_name = null;
			 String resp_bic = null;
			 String sender_inst = null;
			 String currency_val = null;
			 String org_amt = null;
			 String res_name_value = null;
			 String recAccWallet = null;
			 String reci_country = null;
			 for (int j = 0; j < swiftMsgDetails.size(); j++) 
			 {
			    JsonObject swift_detail_code = swiftMsgDetails.get(j).isJsonObject() ? swiftMsgDetails.get(j).getAsJsonObject() : new JsonObject();
			   
			    JsonArray tag = swift_detail_code.has("tag") && swift_detail_code.get("tag").isJsonArray()? swift_detail_code.get("tag").getAsJsonArray() : new JsonArray();


				 for(int i=0; i<tag.size(); i++)
				 {
					 JsonObject tag_name_value = tag.get(i).isJsonObject() ? tag.get(i).getAsJsonObject() : new JsonObject();

					 String name = tag_name_value.has("name") ? (util.isNullOrEmpty(tag_name_value.get("name").getAsString()) ? "" : tag_name_value.get("name").getAsString()) : "";

					 if(name.contains("59"))
					 {
						 res_name_value = tag_name_value.get("value").getAsString();
						 
						 recAccWallet = res_name_value.split("\\|+")[0].replaceAll("[^a-zA-Z0-9]", "");
						 
						 res_name = res_name_value.split("\\|+")[1].replaceAll("[^a-zA-Z0-9 ]", "");
						 
						 reci_country = res_name_value.split("\\|+")[res_name_value.split("\\|+").length - 1].replaceAll("[^a-zA-Z0-9 ]", "");
					

					 }
					 if(name.contains("57"))
					 {
						 resp_bic = tag_name_value.get("value").getAsString();
					 }
					 if(name.contains("72"))
					 {
						 sender_inst = tag_name_value.get("value").getAsString();
					 }
					 if(name.contains("32"))
					 {
						 String input = tag_name_value.get("value").getAsString();
						 currency_val = input.length() >= 9 ? input.substring(6, 9) : "";
						 
						 org_amt = input.replaceAll(".*?(\\d+,\\d+).*", "$1").replace(",", ".");
					 }
				 }
			 }
			 
			 String reci_country_lower = !util.isNullOrEmpty(reci_country) ? reci_country.toLowerCase() : "";


			 if (reci_country_lower.contains("tanzani") || reci_country_lower.contains("tz") || reci_country_lower.contains("tzs") || reci_country_lower.contains("dar es salaa")) 
			 {
				 reci_country = "TZ";							  
			 }
			 else 
			 {
				 JsonArray partyDetails = transactionDetails.has("partyDetails") && transactionDetails.get("partyDetails").isJsonArray() ? transactionDetails.get("partyDetails").getAsJsonArray() : new JsonArray();			
					
				 for (int i = 0; i < partyDetails.size(); i++) 
				 {
					 JsonObject add_js = partyDetails.get(i).isJsonObject() ? partyDetails.get(i).getAsJsonObject() : new JsonObject();
					   
					 JsonObject party = add_js.has("party")? add_js.get("party").getAsJsonObject(): new JsonObject() ;
					 
					 String party_name = party.has("name") ? (util.isNullOrEmpty(party.get("name").getAsString()) ? "" : party.get("name").getAsString()) : "";

					 if(party_name.equalsIgnoreCase(res_name))
					 {
						 JsonObject address = add_js.get("address").getAsJsonObject();
						 
						 String countryCode = address.has("countryCode") ? (util.isNullOrEmpty(address.get("countryCode").getAsString()) ? "" : address.get("countryCode").getAsString()) : "";

						 reci_country = countryCode;
					 }
				 }
			 }
			
					 
			 String sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
			 
			 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { leId }, String.class);
			 
			 String senderId_no = (Info2.size() > 0) ? (util.isNullOrEmpty(Info2.get(0)) ? "NA" : Info2.get(0)) : "NA";

			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = dealReferenceNo;
			 String transactionDate = util.Convert_BOT_Date_Format(transactionApprovalTime, "yyyy-MM-dd'T'HH:mm");
			 String transferChannel = "3";
			 String subCategoryTransferChannel = "";
			 String senderAccountNumber = accountNo; 
			 String recipientName = res_name;
			 String recipientMobileNumber = "NA";
			 String recipientCountry = reci_country;
			 String serviceChannel = "5";
			 String serviceCategory = "5";
			 String serviceSubCategory = "3";
			 String senderIdentificationType = "12";//need take it from client coverage
			 String senderIdentificationNumber = senderId_no;//need take it from client coverage
			 String recipientBankOrFspCode = resp_bic;
			 String recipientAccountOrWalletNumber = recAccWallet;
			 String orgAmount = org_amt;
			 String currency = currency_val;
			 String usdAmount = "0.00";
			 String tzsAmount = "0.00";
			 String purposes = "360123";
			 String senderInstruction = util.isNullOrEmpty(sender_inst)? "NA" : sender_inst;
			 String transactionPlace = "218";

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
					
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
			 
		 	sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
		 	List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";			

			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
				
			recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";

			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count, reportingDate, transactionId, transactionDate, transferChannel, subCategoryTransferChannel, 
					 senderAccountNumber, recipientName, recipientMobileNumber, recipientCountry, serviceChannel, serviceCategory, serviceSubCategory, senderIdentificationType, senderIdentificationNumber, recipientBankOrFspCode, 
					 recipientAccountOrWalletNumber, orgAmount, currency, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel",
					 "senderAccountNumber", "recipientName", "recipientMobileNumber", "recipientCountry", "serviceChannel", "serviceCategory", "serviceSubCategory", "senderIdentificationType", "senderIdentificationNumber", "recipientBankOrFspCode",
					 "recipientAccountOrWalletNumber", "orgAmount", "currency", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Banks Data", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OutgoingBankfundsTransfer :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	
	public JsonObject Outstanding_Guarantee_SHPGT_ISS(String INFO1, String INFO2, String INFO3) //RTS075
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
				
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject guaranteeDetails = data.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject stepDetails = data.get("transactionDetails").getAsJsonObject().get("stepDetails").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			
			 String guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject().get("code").getAsString();
			 
			 String issueDate = eventTimestamp;
			 String transactionValueDate = stepDetails.get("transactionValueDate").getAsString();
			 String expiryDate = guaranteeDetails.get("issueDate").getAsString();
			 
			 String bankRelationship_Type = "2";
			 
			 String bcountryCode="";
			 
			 String partyRoleDesc="";
			 
			 String leid ="";
			  
			 String counterGuarantor_Country="";
			  
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 JsonObject party = js.get("party").getAsJsonObject();
				 
				 String name = party.get("name").getAsString();
				 
				 String partyId = party.get("id").getAsString();
				 
				 
				 
				 if(partyRoleCode.equalsIgnoreCase("ISSBK"))
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 String countryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.equals("APPLBK"))   // not in party code
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 counterGuarantor_Country = address.get("countryCode").getAsString();
					 
					 partyRoleDesc = partyRole.get("desc").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.toUpperCase().equals("BENE")) // not in party code
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 bcountryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 } 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String beneficiaryName = data.get("customerName").getAsString();
			 String relationshipType = "13";
			 String bankRelationshipType = bankRelationship_Type; 
			 String guaranteeTypes = guaranteeType;
			 String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", leid+"|"+LimitID); // CC
			 String beneficiaryCountry = bcountryCode;
			 String counterGuarantorName = partyRoleDesc;
			 String counterGuarantorCountry = counterGuarantor_Country;
			 boolean ratingStatus = true;
			 String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCounterGuarantor = "";  
			 String currency = guaranteeAmount.get("currency").getAsString();
			 String orgAmount = guaranteeAmount.get("value").getAsString();
			 String usdAmount = "0"; 
			 String tzsAmount = guaranteeAmount.get("value").getAsString();  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 		
			 collateralTypes = util.isNullOrEmpty(collateralTypes) ? "2" : collateralTypes;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeType, "Trade001" }, new Lookup001_mapper());
					 
			 guaranteeTypes = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterGuarantorCountry }, new Lookup001_mapper());
				
			 counterGuarantorCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				  
				 if(partyRoleCode.equals("ISSBK"))
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 crRatingCounterGuarantor = address.get("countryCode").getAsString();
				 }
			 }
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate, openingDate, maturityDate, beneficiaryName, relationshipType, 
					bankRelationshipType, guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, counterGuarantorCountry, ratingStatus, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency, orgAmount,
					usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData", "serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType", 
					"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry", "counterGuarantorName", "counterGuarantorCountry", "ratingStatus","crRatingCounterGuarantor", "gradesUnratedCounterGuarantor", "currency", "orgAmount",
					"usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Outstanding_Guarantess_SHPGT_ISS :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
		
	public JsonObject Outstanding_Guarantee_SHPGT_AMD(String INFO1, String INFO2, String INFO3) // RTS075
	{
		JsonObject details = new JsonObject();

		try 
		{
			Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
				
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject guaranteeDetails = data.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject stepDetails = data.get("transactionDetails").getAsJsonObject().get("stepDetails").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			
			 String guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject().get("code").getAsString();
			 
			 String issueDate = guaranteeDetails.has("issueDate") ? guaranteeDetails.get("issueDate").getAsString() : "";
			 String transactionValueDate = stepDetails.get("transactionValueDate").getAsString();
			 String expiryDate = guaranteeDetails.has("issueDate") ? guaranteeDetails.get("issueDate").getAsString() : "";
			 
			 String bankRelationship_Type = "2";
			 
			 String bcountryCode="";
			 
			 String partyRoleDesc="";
			 
			 String leid ="";
			  
			 String counterGuarantor_Country="";
			  
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 JsonObject party = js.get("party").getAsJsonObject();
				 
				 String name = party.get("name").getAsString();
				 
				 String partyId = party.get("id").getAsString();
				 
				 if(partyRoleCode.equalsIgnoreCase("ISSBK"))
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 String countryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.equals("APPLBK"))   // not in party code
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 counterGuarantor_Country = address.get("countryCode").getAsString();
					 
					 partyRoleDesc = partyRole.get("desc").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.toUpperCase().equals("BENE")) 
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 bcountryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 } 
			 }
			 
			 if(util.isNullOrEmpty(issueDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject guaranteeDetails_ = data1.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
					 
					 issueDate = guaranteeDetails_.has("issueDate") ? guaranteeDetails_.get("issueDate").getAsString() : "";
				 }
			 }
			 
			 if(util.isNullOrEmpty(expiryDate))
			 {
				 expiryDate = util.AddYearstoDate(issueDate, "yyyy-MM-dd", 6); 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String beneficiaryName = data.get("customerName").getAsString();
			 String relationshipType = "13";
			 String bankRelationshipType = bankRelationship_Type; 
			 String guaranteeTypes = guaranteeType;
			 String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", leid+"|"+LimitID); // CC
			 String beneficiaryCountry = bcountryCode; 
			 String counterGuarantorName = partyRoleDesc;
			 String counterGuarantorCountry = counterGuarantor_Country;//work on lookup value// APPLK NA
			 boolean ratingStatus = true;
			 String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCounterGuarantor = "";  
			 String currency = guaranteeAmount.get("currency").getAsString(); //work on lookup value
			 String orgAmount = guaranteeAmount.get("value").getAsString();
			 String usdAmount = "0";  
			 String tzsAmount = guaranteeAmount.get("value").getAsString();  
			 int dateDifference =  Integer.parseInt(FindDateDifference(transactionValueDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  

			 collateralTypes = util.isNullOrEmpty(collateralTypes) ? "2" : collateralTypes;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeType, "Trade001" }, new Lookup001_mapper());
					 
			 guaranteeTypes = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterGuarantorCountry }, new Lookup001_mapper());
				
			 counterGuarantorCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;

			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"
					+ "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n"
					+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate,
							openingDate, maturityDate, beneficiaryName, relationshipType, bankRelationshipType,
							guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, ratingStatus,
							counterGuarantorCountry, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency,
							orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType,
							sectorSnaClassification, allowanceProbableLoss, botProvision });

			sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"
					+ "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n"
					+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData",
					"serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType",
					"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry",
					"counterGuarantorName", "ratingStatus", "counterGuarantorCountry", "crRatingCounterGuarantor",
					"gradesUnratedCounterGuarantor", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays",
					"assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });

			sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";

			Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData" });

			sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

			Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1,
							count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });

			Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);

			details.addProperty("Serial", O_SERIAL);
			details.addProperty("Batch_id", Batch_id);

			details.addProperty("result", "success");
			details.addProperty("stscode", "HP00");
			details.addProperty("message", "Batch created successfully");
		} 
		catch (Exception e) 
		{
			details.addProperty("result", "failed");
			details.addProperty("stscode", "HP06");
			details.addProperty("message", e.getLocalizedMessage());

			logger.debug("Exception in Outstanding_Guarantee_SHPGT_AMD :::: " + e.getLocalizedMessage());
		}

		return details;
	}
	
	/*********** ELC *********/
	
	public JsonObject ExportLetterOfCredit_ELC_ISS(String INFO1, String INFO2, String INFO3) //RTS077 new
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 //Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String evntime = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject exportLcDetails = data.get("transactionDetails").getAsJsonObject().get("exportLCDetails").getAsJsonObject();
			 String confirmation = exportLcDetails.get("lcConfirmed").getAsString();
			 JsonObject lcAmount = exportLcDetails.has("amountDetails") ? exportLcDetails.get("amountDetails").getAsJsonObject().get("lcAmount").getAsJsonObject(): new JsonObject(); //NEW
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String effectiveDate = "";
			 String beneficiaryCountry_1="";
			 String beneficiaryName_1 = "";
			 String orgAmount_1 ="";
			 String crRating_Leid = "";
			 String Relationship_Type = "2";
			 String expiryDate = "";
			
			 effectiveDate = exportLcDetails.get("effectiveDate").getAsString();
			 expiryDate = exportLcDetails.get("expiryDate").getAsString();	
			
			 boolean rating_status = false;
			 
			if(confirmation.equalsIgnoreCase("true"))
			{
				for(int i=0; i<partyDetails.size();i++) 
				{
					JsonObject jr = partyDetails.get(i).getAsJsonObject();
					JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
					String partyRoleCode = partyRole.get("code").getAsString();
					
					if(partyRoleCode.equals("ISSBK")) 
					{		
						JsonObject party = jr.get("party").getAsJsonObject();
						String name = party.get("name").getAsString();
						JsonObject address = jr.get("address").getAsJsonObject();
						String countryCode = address.get("countryCode").getAsString();								
						crRating_Leid = jr.get("leid").getAsString();
						
						rating_status = true;

						if(name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "1"; // Domestic Bank related
						}
						else if (name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "8"; // Foreign Bank related
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "2"; // Domestic Bank unrelated
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "9"; // Foreign Bank unrelated
						} 
						else if(name.toUpperCase().contains("BANK OF TANZANIA")) 
						{
							Relationship_Type = "15"; // Bank of Tanzania
						}
					}
				}	
			} 
			else 
			{
				for(int i = 0; i < partyDetails.size(); i++) 
				{
			        JsonObject jr = partyDetails.get(i).getAsJsonObject();
			        JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
			        String partyRoleCode = partyRole.get("code").getAsString();

			        if(partyRoleCode.equals("ADVBK") || partyRoleCode.equals("ADTBK")) 
			        {
			        	JsonObject party = jr.get("party").getAsJsonObject();
						String name = party.get("name").getAsString();
						JsonObject address = jr.get("address").getAsJsonObject();
						String countryCode = address.get("countryCode").getAsString();								
						crRating_Leid = jr.get("leid").getAsString();
						
						rating_status = true;

						if(name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "1"; // Domestic Bank related
						}
						else if (name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "8"; // Foreign Bank related
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "2"; // Domestic Bank unrelated
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "9"; // Foreign Bank unrelated
						} 
						else if(name.toUpperCase().contains("BANK OF TANZANIA")) 
						{
							Relationship_Type = "15"; // Bank of Tanzania
						}
						
			            rating_status = true;
			        }
				}
			}
						 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
				JsonObject js = partyDetails.get(i).getAsJsonObject();
				JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				String partyRoleCode = partyRole.get("code").getAsString();
				JsonObject party = js.get("party").getAsJsonObject();
				
				if (partyRoleCode.equals("APPL")) 
				{
					String desc = party.get("name").getAsString();
					beneficiaryName_1 = desc;
					JsonObject address = js.get("address").getAsJsonObject();
					beneficiaryCountry_1 = address.get("countryCode").getAsString();
				}
			 }

			 orgAmount_1 = lcAmount.has("value") ? lcAmount.get("value").getAsString() : "";//NEW
						
		 	 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
		 	
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = Relationship_Type;
			 String bankRelationshipType = "1"; //bank said to stamp as 1				 
			 boolean ratingStatus = true;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1;
			 String crRatingCounterForeignBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating_Leid); //cc;						 
			 String gradesUnratedForeignBank = ""; 
			 String currency = lcAmount.has("currency") ? lcAmount.get("currency").getAsString() : "";//NEW-OLD
			 String lcClassification = confirmation.equalsIgnoreCase("Y") ? "1" : "2";
			 String orgAmount = orgAmount_1;
			 String usdAmount = "0";
			 String tzsAmount = "0";  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating_Leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			   
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 int count = 1;
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "exportLettersCreditData", count, reportingDate, 
					 openingDate, maturityDate, holderName,  ratingStatus, relationshipType, bankRelationshipType, beneficiaryName,
					beneficiaryCountry, crRatingCounterForeignBank, gradesUnratedForeignBank, currency, lcClassification, orgAmount, usdAmount, 
					tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "exportLettersCreditData", "serial", "reportingDate", 
					 "openingDate", "maturityDate", "holderName", "ratingStatus", "relationshipType", "bankRelationshipType", "beneficiaryName", 
					 "beneficiaryCountry", "crRatingCounterForeignBank", "gradesUnratedForeignBank", "currency", "lcClassification", 
					 "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", 
					 "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "exportLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "exportLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject ExportLetterOfCredit_ELC_MSC(String INFO1, String INFO2, String INFO3) //RTS077 new
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 //Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String evntime = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject exportLcDetails = data.get("transactionDetails").getAsJsonObject().get("exportLCDetails").getAsJsonObject();
			 String confirmation = exportLcDetails.has("lcConfirmed") ? exportLcDetails.get("lcConfirmed").getAsString() : "false";
			 JsonObject lcOSAmount = exportLcDetails.has("amountDetails") ? exportLcDetails.get("amountDetails").getAsJsonObject().get("lcOSAmount").getAsJsonObject() : new JsonObject(); //NEW
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String effectiveDate = "";
			 String beneficiaryCountry_1="";
			 String beneficiaryName_1 = "";
			 String orgAmount_1 ="";
			 String crRating_Counter_ForeignBank = "";
			 String Relationship_Type = "2";
			 String expiryDate = "";
			
			 effectiveDate = exportLcDetails.get("effectiveDate").getAsString();
			
			 JsonObject transactionDetails = data.has("transactionDetails") && !data.get("transactionDetails").isJsonNull() ? data.getAsJsonObject("transactionDetails") : new JsonObject();
			 JsonObject miscellaneousStepDetails = transactionDetails.has("miscellaneousStepDetails") && !transactionDetails.get("miscellaneousStepDetails").isJsonNull() ? transactionDetails.getAsJsonObject("miscellaneousStepDetails") : new JsonObject();
			 String miscTypeCode = miscellaneousStepDetails.has("miscTypeCode")? !util.isNullOrEmpty(miscellaneousStepDetails.get("miscTypeCode").getAsString())? miscellaneousStepDetails.get("miscTypeCode").getAsString() :"NA" : "NA";
	
			 if(miscTypeCode.equalsIgnoreCase("RI"))
			 {
				 expiryDate= util.getCurrentDate();
			 }
			 else 
			 {
				 expiryDate=exportLcDetails.get("expiryDate").getAsString();
			 }
			 boolean rating_status = false;
			 
			if(confirmation.equalsIgnoreCase("true"))
			{
				for(int i=0; i<partyDetails.size();i++) 
				{
					JsonObject jr = partyDetails.get(i).getAsJsonObject();
					JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
					String partyRoleCode = partyRole.get("code").getAsString();
					
					if(partyRoleCode.equals("ISSBK")) 
					{		
						JsonObject party = jr.get("party").getAsJsonObject();
						String name = party.get("name").getAsString();
						JsonObject address = jr.get("address").getAsJsonObject();
						String countryCode = address.get("countryCode").getAsString();								
						crRating_Counter_ForeignBank = jr.get("leid").getAsString();
						
						rating_status = true;

						if(name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "1"; // Domestic Bank related
						}
						else if (name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "8"; // Foreign Bank related
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "2"; // Domestic Bank unrelated
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "9"; // Foreign Bank unrelated
						} 
						else if(name.toUpperCase().contains("BANK OF TANZANIA")) 
						{
							Relationship_Type = "15"; // Bank of Tanzania
						}
					}
				}	
			} 
			else 
			{
				for(int i = 0; i < partyDetails.size(); i++) 
				{
			        JsonObject jr = partyDetails.get(i).getAsJsonObject();
			        JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
			        String partyRoleCode = partyRole.get("code").getAsString();

			        if(partyRoleCode.equals("ADVBK") || partyRoleCode.equals("ADTBK")) 
			        {
			        	JsonObject party = jr.get("party").getAsJsonObject();
						String name = party.get("name").getAsString();
						JsonObject address = jr.get("address").getAsJsonObject();
						String countryCode = address.get("countryCode").getAsString();								
						crRating_Counter_ForeignBank = jr.get("leid").getAsString();
						
						rating_status = true;

						if(name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "1"; // Domestic Bank related
						}
						else if (name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "8"; // Foreign Bank related
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "2"; // Domestic Bank unrelated
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "9"; // Foreign Bank unrelated
						} 
						else if(name.toUpperCase().contains("BANK OF TANZANIA")) 
						{
							Relationship_Type = "15"; // Bank of Tanzania
						}
						
			            rating_status = true;
			        }
				}
			}

						 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
				JsonObject js = partyDetails.get(i).getAsJsonObject();
				JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				String partyRoleCode = partyRole.get("code").getAsString();
				JsonObject party = js.get("party").getAsJsonObject();
				
				if (partyRoleCode.equals("APPL")) 
				{
					String desc = party.get("name").getAsString();
					beneficiaryName_1 = desc;
					JsonObject address = js.get("address").getAsJsonObject();
					beneficiaryCountry_1 = address.get("countryCode").getAsString();
				}
			 }

		 	 orgAmount_1 = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW
		
		 	 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
		 	
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = Relationship_Type;
			 String bankRelationshipType = "1"; //bank said to stamp as 1				 
			 boolean ratingStatus = true;
			 String crRatingCounterForeignBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating_Counter_ForeignBank); //cc;						 
			 String gradesUnratedForeignBank = ""; 
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1;  
			 String currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 String lcClassification = confirmation.equalsIgnoreCase("Y") ? "1" : "2";
			 String orgAmount = orgAmount_1;
			 String usdAmount = "0";  
			 String tzsAmount = "0";  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating_Counter_ForeignBank); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 int count = 1;
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "exportLettersCreditData", count,reportingDate, openingDate, maturityDate, holderName, ratingStatus, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, crRatingCounterForeignBank,
					 gradesUnratedForeignBank, currency, lcClassification, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "exportLettersCreditData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "ratingStatus", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", 
					 "crRatingCounterForeignBank", "gradesUnratedForeignBank", "currency", "lcClassification", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "exportLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OBS", "exportLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in exportLettersCreditData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject ExportLetterOfCredit_ELC_AMD(String INFO1, String INFO2, String INFO3) //RTS077 new
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 //Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String evntime = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject exportLcDetails = data.get("transactionDetails").getAsJsonObject().get("exportLCDetails").getAsJsonObject();
			 String confirmation = exportLcDetails.has("lcConfirmed") ? exportLcDetails.get("lcConfirmed").getAsString() : "false";
			 JsonObject lcOSAmount = exportLcDetails.has("amountDetails") ? exportLcDetails.get("amountDetails").getAsJsonObject().get("lcOSAmount").getAsJsonObject() : new JsonObject(); //NEW
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String effectiveDate = "";
			 String beneficiaryCountry_1="";
			 String beneficiaryName_1 = "";
			 String orgAmount_1 ="";
			 String crRating_Leid = "";
			 String Relationship_Type = "2";
			 String expiryDate = "";
			
			 effectiveDate = exportLcDetails.get("effectiveDate").getAsString();
			 expiryDate = exportLcDetails.get("expiryDate").getAsString();	
			
			 boolean rating_status = false;
			 
			if(confirmation.equalsIgnoreCase("true"))
			{
				for(int i=0; i<partyDetails.size();i++) 
				{
					JsonObject jr = partyDetails.get(i).getAsJsonObject();
					JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
					String partyRoleCode = partyRole.get("code").getAsString();
					
					if(partyRoleCode.equals("ISSBK")) 
					{		
						JsonObject party = jr.get("party").getAsJsonObject();
						String name = party.get("name").getAsString();
						JsonObject address = jr.get("address").getAsJsonObject();
						String countryCode = address.get("countryCode").getAsString();								
						crRating_Leid = jr.get("leid").getAsString();
						
						rating_status = true;

						if(name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "1"; // Domestic Bank related
						}
						else if (name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "8"; // Foreign Bank related
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "2"; // Domestic Bank unrelated
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "9"; // Foreign Bank unrelated
						} 
						else if(name.toUpperCase().contains("BANK OF TANZANIA")) 
						{
							Relationship_Type = "15"; // Bank of Tanzania
						}
					}
				}	
			} 
			else 
			{
				for(int i = 0; i < partyDetails.size(); i++) 
				{
			        JsonObject jr = partyDetails.get(i).getAsJsonObject();
			        JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
			        String partyRoleCode = partyRole.get("code").getAsString();

			        if(partyRoleCode.equals("ADVBK") || partyRoleCode.equals("ADTBK")) 
			        {
			        	JsonObject party = jr.get("party").getAsJsonObject();
						String name = party.get("name").getAsString();
						JsonObject address = jr.get("address").getAsJsonObject();
						String countryCode = address.get("countryCode").getAsString();								
						crRating_Leid = jr.get("leid").getAsString();
						
						rating_status = true;

						if(name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "1"; // Domestic Bank related
						}
						else if (name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "8"; // Foreign Bank related
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "2"; // Domestic Bank unrelated
						} 
						else if (!name.toUpperCase().contains("STANDARD CHARTERED BANK") && !countryCode.equalsIgnoreCase("TZ")) 
						{
							Relationship_Type = "9"; // Foreign Bank unrelated
						} 
						else if(name.toUpperCase().contains("BANK OF TANZANIA")) 
						{
							Relationship_Type = "15"; // Bank of Tanzania
						}
						
			            rating_status = true;
			        }
				}
			}
	 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
				JsonObject js = partyDetails.get(i).getAsJsonObject();
				JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				String partyRoleCode = partyRole.get("code").getAsString();
				JsonObject party = js.get("party").getAsJsonObject();
				
				if(partyRoleCode.equals("APPL")) 
				{
					String desc = party.get("name").getAsString();
					beneficiaryName_1 = desc;
					JsonObject address = js.get("address").getAsJsonObject();
					beneficiaryCountry_1 = address.get("countryCode").getAsString();
				}
			 }

		 	 orgAmount_1 = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW
					
		 	 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
		 	
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = Relationship_Type;
			 String bankRelationshipType = "1"; //bank said to stamp as 1				 
			 boolean ratingStatus = true;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1;
			 String crRatingCounterForeignBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating_Leid); //cc;						 
			 String gradesUnratedForeignBank = ""; 
			 String currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 String lcClassification = confirmation.equalsIgnoreCase("Y") ? "1" : "2";
			 String orgAmount = orgAmount_1;
			 String usdAmount = "0";  
			 String tzsAmount = "0"; 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating_Leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			  
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "exportLettersCreditData", count, reportingDate, 
					 openingDate, maturityDate, holderName,  ratingStatus, relationshipType, bankRelationshipType, beneficiaryName,
					beneficiaryCountry, crRatingCounterForeignBank, gradesUnratedForeignBank, currency, lcClassification, orgAmount, usdAmount, 
					tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "exportLettersCreditData", "serial", "reportingDate", 
					 "openingDate", "maturityDate", "holderName", "ratingStatus", "relationshipType", "bankRelationshipType", "beneficiaryName", 
					 "beneficiaryCountry", "crRatingCounterForeignBank", "gradesUnratedForeignBank", "currency", "lcClassification", 
					 "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", 
					 "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "exportLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "exportLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}	
	
	/*********** SBLC *********/
	
	public JsonObject Outstanding_lettercredit_SBLC_ISS(String INFO1, String INFO2, String INFO3)//RTS079 new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			  
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject guaranteeDetails = transactionDetails.get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			
			 JsonObject confirmation = guaranteeDetails.get("confirmation").getAsJsonObject();
			 
			 JsonObject outstandingAmount = guaranteeDetails.has("outstandingAmount") ? guaranteeDetails.get("outstandingAmount").getAsJsonObject() : new JsonObject();

			 String party_name = "";
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1="";
			 String crRating="";
			 String marginBalance = "";
			 boolean bankRatingStatus_1 = false;
			 boolean customerRatingStatus_1 = false ;
			 String crRatingConfirmingBank_1 = "";
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 String leid = "";
			 
			 for(int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					String party_id = party.get("id").getAsString();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					
					if(party_id.equalsIgnoreCase("CUST"))
					{
						leid = js.get("leid").getAsString();
						
						party_name = party.get("name").getAsString();
						
						if(party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && countryCode.equalsIgnoreCase("TZ"))
						{
							party_name = "1";
						}
						else if (party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && !countryCode.equalsIgnoreCase("TZ"))
						{
							party_name = "8";
						}
						else if (!party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && countryCode.equalsIgnoreCase("TZ"))
						{
							party_name  = "2";
						}
						else if (!party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && !countryCode.equalsIgnoreCase("TZ"))
						{
							party_name  = "9";
						}
						else if (party_name.equalsIgnoreCase("Bank of Tanzania")) 
						{
							party_name = "15";
						}
					}
					
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					
					String party_role_code = partyRole.get("code").getAsString();
					
					if(party_role_code.equalsIgnoreCase("BNE") || party_role_code.equalsIgnoreCase("BENE") || party_role_code.equalsIgnoreCase("BENI")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
						
						customerRatingStatus_1 = true;	
					}
										
					if (party_role_code.equalsIgnoreCase("CFMBK")) 
					{
						crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
						bankRatingStatus_1 = true;
					}
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for (int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				marginBalance = js.get("marginBalance").getAsString();
			 }
			 
			 String issueDate = guaranteeDetails.get("issueDate").getAsString();
			 String expiryDate = guaranteeDetails.get("expiryDate").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String lettersCreditType = guaranteeType.get("code").getAsString();
			 String collateralType = "11";
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");			 
			 String expireDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = customerName;
			 String relationshipType = "1";
			 String bankRelationshipType = party_name;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1; 
			 boolean customerRatingStatus = customerRatingStatus_1;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc; 
			 String gradesUnratedCustomer = "";
			 String currency = outstandingAmount.has("currency") ? outstandingAmount.get("currency").getAsString() : ""; //NEW
			 String orgAmount = outstandingAmount.has("value") ? outstandingAmount.get("value").getAsString() : "";	//NEW
			 String usdAmount = "0";  
			 String tzsAmount = "0"; 
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0";  
			 String tzsOutstandingMargDepAmount = "0";  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String lcClassification = "2";
			 boolean bankRatingStatus = bankRatingStatus_1; 
			 String crRatingConfirmingBank = crRatingConfirmingBank_1; 
			 String gradesUnratedConfirmingBank = ""; 
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // take it from splice
			 String botProvision = "0";  
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN8=? and COLUMN1=? and COLUMN7=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, lettersCreditType, "VND012" }, new Lookup001_mapper());
					 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "1";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 if(bankRatingStatus)
			 {
				 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in outstandingLettersCreditData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;

	}
	
	public JsonObject Outstanding_lettercredit_SBLC_AMD(String INFO1, String INFO2, String INFO3)//RTS079 new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			  
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject guaranteeDetails = transactionDetails.get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			
			 JsonObject confirmation = guaranteeDetails.get("confirmation").getAsJsonObject();
			 
			 JsonObject outstandingAmount = guaranteeDetails.has("outstandingAmount") ? guaranteeDetails.get("outstandingAmount").getAsJsonObject() : new JsonObject();
			 
			 String party_name = "";
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1="";
			 String leid="";
			 String marginBalance = "";
			 boolean bankRatingStatus_1 = false;
			 boolean customerRatingStatus_1 = false ;
			 String crRatingConfirmingBank_1 = "";
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for(int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					String party_id = party.get("id").getAsString();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					
					if(party_id.equalsIgnoreCase("CUST"))
					{
						leid = js.get("leid").getAsString();
						
						party_name = party.get("name").getAsString();
						
						if(party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && countryCode.equalsIgnoreCase("TZ"))
						{
							party_name = "1";
						}
						else if (party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && !countryCode.equalsIgnoreCase("TZ"))
						{
							party_name = "8";
						}
						else if (!party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && countryCode.equalsIgnoreCase("TZ"))
						{
							party_name  = "2";
						}
						else if (!party_name.equalsIgnoreCase("STANDARD CHARTERED BK TANZANIA") && !countryCode.equalsIgnoreCase("TZ"))
						{
							party_name  = "9";
						}
						else if (party_name.equalsIgnoreCase("Bank of Tanzania")) 
						{
							party_name = "15";
						}
					}
					
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					
					String party_role_code = partyRole.get("code").getAsString();
					
					if(party_role_code.equalsIgnoreCase("BNE") || party_role_code.equalsIgnoreCase("BENE") || party_role_code.equalsIgnoreCase("BENI")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
						
						customerRatingStatus_1 = true;
						
						
					}
										
					if (party_role_code.equalsIgnoreCase("CFMBK")) 
					{
						crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
						bankRatingStatus_1 = true;
					}
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for (int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				marginBalance = js.get("marginBalance").getAsString();
			 }
			 
			 String issueDate = guaranteeDetails.get("issueDate").getAsString();
			 String expiryDate = guaranteeDetails.get("expiryDate").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String lettersCreditType = guaranteeType.get("code").getAsString();
			 String collateralType = "11";
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");			 
			 String expireDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = customerName;
			 String relationshipType = "1";
			 String bankRelationshipType = party_name;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1; 
			 boolean customerRatingStatus = customerRatingStatus_1;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCustomer = ""; 
			 String currency = outstandingAmount.has("currency") ? outstandingAmount.get("currency").getAsString() : ""; //NEW
			 String orgAmount = outstandingAmount.has("value") ? outstandingAmount.get("value").getAsString() : "";	//NEW
			 String usdAmount = "0";  
			 String tzsAmount = "0"; 
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0";  
			 String tzsOutstandingMargDepAmount = "0";  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String lcClassification = "2";
			 boolean bankRatingStatus = bankRatingStatus_1; 
			 String crRatingConfirmingBank = crRatingConfirmingBank_1; 
			 String gradesUnratedConfirmingBank = ""; 
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice
			 String botProvision = "0";  
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN8=? and COLUMN1=? and COLUMN7=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, lettersCreditType, "VND012" }, new Lookup001_mapper());
					 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "1";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 if(bankRatingStatus)
			 {
				 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }

			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in outstandingLettersCreditData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;

	}
	
	public JsonObject Outstanding_Guarantee_SBLC_PAY(String INFO1, String INFO2, String INFO3) //RTS075
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject guaranteeDetails = data.get("transactionDetails").getAsJsonObject().get("guaranteeDetails").getAsJsonObject();
			 
			 JsonObject guaranteeAmount = guaranteeDetails.get("guaranteeAmount").getAsJsonObject();
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			
			 String guaranteeType = guaranteeDetails.get("guaranteeType").getAsJsonObject().get("code").getAsString();
			 
			 String issueDate = guaranteeDetails.get("issueDate").getAsString();
			 String expiryDate = guaranteeDetails.get("expiryDate").getAsString();
			 
			 String bankRelationship_Type = "2";
			 
			 String bcountryCode="";
			 
			 String partyRoleDesc="";
			 
			 String leid ="";
			  
			 String counterGuarantor_Country="";
			  
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 JsonObject party = js.get("party").getAsJsonObject();
				 
				 String name = party.get("name").getAsString();
				 
				 String partyId = party.get("id").getAsString();
				 
				
				 
				 if(partyRoleCode.equalsIgnoreCase("ISSBK"))
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 String countryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.equals("APPLBK"))   // not in party code
				 {
					 leid = js.get("leid").getAsString();
					 
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 counterGuarantor_Country = address.get("countryCode").getAsString();
					 
					 partyRoleDesc = partyRole.get("desc").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !counterGuarantor_Country.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 }
				 
				 if(partyRoleCode.toUpperCase().equals("CUST") || partyId.toUpperCase().equals("CUST")) // not in party code
				 {
					 JsonObject address = js.get("address").getAsJsonObject();
					 
					 bcountryCode = address.get("countryCode").getAsString();
					 
					 if(name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "1"; // Domestic Bank related
					 }
					 else if(name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "8"; // Foreign Bank related
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "2"; // Domestic Bank unrelated
					 }
					 else if(!name.toUpperCase().contains("STANDARD") && !bcountryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationship_Type = "9"; // Foreign Bank unrelated
					 }
					 else if(name.toUpperCase().contains("BANK OF TANZANIA"))
					 {
						 bankRelationship_Type = "15"; // Bank of Tanzania
					 }
				 } 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String beneficiaryName = data.get("customerName").getAsString();
			 String relationshipType = "13";
			 String bankRelationshipType = bankRelationship_Type; 
			 String guaranteeTypes = guaranteeType;
			 String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", leid+"|"+LimitID); // CC
			 String beneficiaryCountry = bcountryCode; 
			 String counterGuarantorName = partyRoleDesc;
			 String counterGuarantorCountry = counterGuarantor_Country;
			 boolean ratingStatus = true;
			 String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", leid); //cc
			 String gradesUnratedCounterGuarantor = "";  
			 String currency = guaranteeAmount.get("currency").getAsString(); //work on lookup value
			 String orgAmount = guaranteeAmount.get("value").getAsString();
			 String usdAmount = "0";  
			 String tzsAmount = guaranteeAmount.get("value").getAsString();  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-MM-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 
			 collateralTypes = util.isNullOrEmpty(collateralTypes) ? "2" : collateralTypes;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeType, "Trade001" }, new Lookup001_mapper());
					 
			 guaranteeType = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate, openingDate, maturityDate, beneficiaryName, relationshipType, 
					bankRelationshipType, guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, counterGuarantorCountry, ratingStatus, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency, orgAmount,
					usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData", "serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType", 
					"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry", "counterGuarantorName", "counterGuarantorCountry", "ratingStatus","crRatingCounterGuarantor", "gradesUnratedCounterGuarantor", "currency", "orgAmount",
					"usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Outstanding_Guarantess_ISS_PAY :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	/**********  ILC ********/
	
	public JsonObject OustandingLetterCredit_ILC_ISS(String INFO1, String INFO2, String INFO3)//RTS079
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 String notificationID = data.get("notificationID").getAsString();
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 String bankRelationshipType_1 = "2";
			 String beneficiaryName_1="";
			 String beneficiaryCountry_1="";
			 boolean customerRatingStatus_1= false;
			 String crRatingConfirmingBank_1="";
			 String marginBalance = "0";
			 boolean bankRatingStatus_1 = false;
			 String crRatingCounterCustomer_1 = "";
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject importLCDetails = transactionDetails.get("importLCDetails").getAsJsonObject();
			 JsonObject lcType =importLCDetails.get("lcType").getAsJsonObject();
			 
			 String effectiveDate=importLCDetails.get("effectiveDate").getAsString();
			 
			 String expiryDate=importLCDetails.get("expiryDate").getAsString();		
			 JsonObject tenorType = importLCDetails.get("tenorType").getAsJsonObject();
			 String tenorDays = importLCDetails.get("tenorDays").getAsString();
			 String code = tenorType.get("code").getAsString();
			 
			 if(code.equalsIgnoreCase("sight")) 
			 {				 
				 code = expiryDate;
			 }
			 else if(code.equalsIgnoreCase("usanc")) 
			 { 
				 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL";
				 
				 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{ expiryDate, tenorDays }, String.class);
				 
				 code = updatedDate;
			 }
			 
			 String lcType_code = lcType.has("code") ? lcType.get("code").getAsString() : "";
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();
				 String name = party.get("name").getAsString();
				 
				 if(id.equalsIgnoreCase("CUST"))
				 {
					 String leid = js.get("leid").getAsString();
					 
					 crRatingCounterCustomer_1 = leid;
					 
					 if(name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationshipType_1 = "1";
					 }
					 else if (name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "8";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "2";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "9";
					 }
					 else if (name.equalsIgnoreCase("Bank of Tanzania")) 
					 {
						 bankRelationshipType_1 = "15";
					 }	
				 }
				 
				 if(partyRoleCode.equals("BNE") || partyRoleCode.equals("BENE") || partyRoleCode.equals("BENI"))
				 { 
					 beneficiaryName_1 = party.get("name").getAsString();
					 
					 beneficiaryCountry_1= countryCode;	
					 
					 customerRatingStatus_1 = true;
				 }
				 
				 if(partyRoleCode.equalsIgnoreCase("CFMBK")) 
				 {
					crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
					bankRatingStatus_1 = true;
				 }
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for(int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(1).getAsJsonObject();
				
				marginBalance = js.get("marginBalance").getAsString();
			 }
			 
			 JsonObject amountDetails = importLCDetails.get("amountDetails").getAsJsonObject();
			 JsonObject lcAmount = amountDetails.get("lcAmount").getAsJsonObject();
			 String Currency = lcAmount.get("currency").getAsString();
			 
			 String value = lcAmount.get("value").getAsString();
			 
			 JsonObject confirmation = importLCDetails.get("confirmation").getAsJsonObject();	 
			 String confirmation_code = confirmation.get("code").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String lettersCreditType = lcType_code; 
			 String collateralType = "11"; //Bank said to stamp as 11.
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(code, "yyyy-MM-dd");
			 String expireDate = util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = "1";//Bank said to stamp as 1.
			 String bankRelationshipType = bankRelationshipType_1;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1;
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRatingCounterCustomer_1); //cc; 
			 String gradesUnratedCustomer = ""; 
			 String currency = Currency;
			 String orgAmount = value;	 
			 String usdAmount = "0";
			 String tzsAmount = "0";  
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0";   
			 String tzsOutstandingMargDepAmount = "0"; 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; //Bank said to stamp as 1.
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRatingCounterCustomer_1); //cc
			 String lcClassification = confirmation_code.equalsIgnoreCase("Y") ? "1" : "2";  
			 boolean bankRatingStatus = bankRatingStatus_1;
			 String crRatingConfirmingBank = crRatingConfirmingBank_1;  
			 String gradesUnratedConfirmingBank = ""; 
			 String botProvision = "0";  
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 
			 if(bankRatingStatus_1)
			 {
				 String sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
			 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 		 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
			 details.addProperty("Serial", O_SERIAL);
			 details.addProperty("Batch_id", Batch_id);
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OustandingLetterCredit_ILC_ISS :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Outstanding_Acceptance_ILC_AAC(String INFO1, String INFO2, String INFO3) //RTS065
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject transactionDetails = data.has("transactionDetails") ? data.get("transactionDetails").getAsJsonObject() : new JsonObject();
			 
			 JsonObject acceptanceDetails = transactionDetails.has("acceptanceDetails") ? transactionDetails.get("acceptanceDetails").getAsJsonObject() : new JsonObject();
			
			 String docInOrder = acceptanceDetails.has("docInOrder") ? acceptanceDetails.get("docInOrder").getAsString() : "";
			 
			 if(docInOrder.equals("false") || docInOrder.equals("N"))
			 {
				 String acceptance_Type= acceptanceDetails.has("acceptanceCategory") ? acceptanceDetails.get("acceptanceCategory").getAsString() : "";
				 
				 JsonObject stepDetails = data.get("transactionDetails").getAsJsonObject().get("stepDetails").getAsJsonObject();
				 String transactionValueDate= stepDetails.has("transactionValueDate") ? stepDetails.get("transactionValueDate").getAsString() : "";
				
				 JsonObject acceptanceAmount = acceptanceDetails.has("acceptanceAmount") ? acceptanceDetails.get("acceptanceAmount").getAsJsonObject() : new JsonObject();
				 String currency_1 = acceptanceAmount.has("currency") ? acceptanceAmount.get("currency").getAsString() : "";
				 
				 String orgValue = acceptanceAmount.has("value") ?  acceptanceAmount.get("value").getAsString() : "";
				 
				 String countryCode="";
				 String beneficiary_Name="";
				 
				 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();	
				 
				 String LEID = "";
				 
				 for(int i=0; i<partyDetails.size(); i++)
				 {
					 JsonObject jr =  partyDetails.get(i).getAsJsonObject();
					 JsonObject partyRole = jr.get("partyRole").getAsJsonObject();
					 String partyRoleCode = partyRole.get("code").getAsString();
					 
					 JsonObject party = jr.get("party").getAsJsonObject();
					 
					 if(partyRoleCode.equals("BENE") || partyRoleCode.equals("BENI"))
					 {
						 beneficiary_Name = party.get("name").getAsString();
						 LEID = jr.get("leid").getAsString();
					 } 
				 } 
				 
				 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
				 
				 String reportingDate = util.getCurrentReportDate();
				 String acceptanceType = acceptance_Type;
				 String beneficiaryName = beneficiary_Name;
				 String transactionDate = util.Convert_BOT_Date_Format(transactionValueDate, "yyyy-mm-dd");  
				 String currency = currency_1; 
				 String orgAmount = orgValue;
				 String usdAmount = "0";  
				 String tzsAmount = "0"; 
				 String sectorSnaClassification =  ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID); //cc  
				 
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
				 
				 usdAmount = rates.get("usd").getAsString();
				 tzsAmount = rates.get("tzs").getAsString();
				 
				 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
						
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				 
				 int count = 1;
				
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingAcceptances", count, reportingDate, acceptanceType, beneficiaryName, transactionDate, currency, 
						 orgAmount, usdAmount, tzsAmount, sectorSnaClassification });
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
						 "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingAcceptances", "serial", "reportingDate", "acceptanceType", "beneficiaryName", "transactionDate", "currency", 
						"orgAmount", "usdAmount", "tzsAmount", "sectorSnaClassification"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingAcceptances"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liability", "outstandingAcceptances", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
			 		 
			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			     
		         details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
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
			 
			 logger.debug("Exception in Outstanding_Acceptance_ILC_AAC :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject OustandingLetterCredit_ILC_AMD(String INFO1, String INFO2, String INFO3) //new
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 String bankRelationshipType_1 = "2";
			 String beneficiaryName_1="";
			 String beneficiaryCountry_1="";
			 boolean customerRatingStatus_1= false;
			 String crRatingConfirmingBank_1="";
			 String crRatingCounterCustomer_1="";
			 String marginBalance = "0";
			 boolean bankRatingStatus_1 = false;
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject importLCDetails = transactionDetails.get("importLCDetails").getAsJsonObject();
			 JsonObject lcType =importLCDetails.get("lcType").getAsJsonObject();
			 
			 String effectiveDate=importLCDetails.get("effectiveDate").getAsString();
			 
			 String expiryDate=importLCDetails.get("expiryDate").getAsString();		
			 JsonObject tenorType = importLCDetails.get("tenorType").getAsJsonObject();
			 String tenorDays = importLCDetails.get("tenorDays").getAsString();
			 String code = tenorType.get("code").getAsString();
			 
			 if(code.equalsIgnoreCase("sight")) 
			 {				 
				 code = expiryDate;
			 }
			 else if(code.equalsIgnoreCase("usanc")) 
			 { 
				 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL";
				 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{expiryDate, tenorDays}, String.class);
				 code = updatedDate;
			 }
			 		 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String lcType_code = lcType.has("code") ? lcType.get("code").getAsString() : "";
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();
				 
				 if(id.equalsIgnoreCase("CUST"))
				 {
					 String leid = js.get("leid").getAsString();
					 crRatingCounterCustomer_1 = leid;
					 
					 String name = party.get("name").getAsString();
					 
					 if(name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationshipType_1 = "1";
					 }
					 else if (name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "8";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "2";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "9";
					 }
					 else if (name.equalsIgnoreCase("Bank of Tanzania")) 
					 {
						 bankRelationshipType_1 = "15";
					 }	
				 }
				 
				 if(partyRoleCode.equals("BNE") || partyRoleCode.equals("BENE") || partyRoleCode.equals("BENI"))
				 {
					 beneficiaryName_1 = party.get("name").getAsString();					 
					 beneficiaryCountry_1= countryCode;
					 customerRatingStatus_1 = true;
				 }
	
				 if(partyRoleCode.equalsIgnoreCase("CFMBK")) 
				 {
					crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
					bankRatingStatus_1 = true;
				 }
			 }
			 
			 JsonObject amountDetails = importLCDetails.has("amountDetails") ? importLCDetails.get("amountDetails").getAsJsonObject(): new JsonObject();	 
			 JsonObject lcOSAmount = amountDetails.has("lcOSAmount") ? amountDetails.get("lcOSAmount").getAsJsonObject(): new JsonObject();
			 String Currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 
			 String value = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for(int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				
				marginBalance = js.get("marginBalance").getAsString();
			 }
			 
			 JsonObject confirmation = importLCDetails.get("confirmation").getAsJsonObject();	 
			 String confirmation_code = confirmation.get("code").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String lettersCreditType = lcType_code; 
			 String collateralType = "11"; //Bank said to stamp as 11.
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(code, "yyyy-MM-dd");  
			 String expireDate =util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = "1";//Bank said to stamp as 1.
			 String bankRelationshipType = bankRelationshipType_1;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1;
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRatingCounterCustomer_1); //cc
			 String gradesUnratedCustomer = "";  
			 String currency = Currency;
			 String orgAmount = value;	 
			 String usdAmount = "0";
			 String tzsAmount = "0";  
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0";   
			 String tzsOutstandingMargDepAmount = "0"; 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; //Bank said to stamp as 1.
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRatingCounterCustomer_1); //cc
			 String lcClassification = confirmation_code.equalsIgnoreCase("Y") ? "1" : "2";  
			 boolean bankRatingStatus = bankRatingStatus_1;
			 String crRatingConfirmingBank = crRatingConfirmingBank_1;  
			 String gradesUnratedConfirmingBank = ""; 
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 
			 if(bankRatingStatus_1)
			 {
				 String sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
			 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 if(util.isNullOrEmpty(lettersCreditType))
			 {
				 sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 JsonObject importLCDetails1 = transactionDetails1.get("importLCDetails").getAsJsonObject();
					 JsonObject lcType1 =importLCDetails1.get("lcType").getAsJsonObject();
					 
					 String lcType_code1 = lcType1.has("code") ? lcType1.get("code").getAsString() : "";
					 
					 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { lcType_code1, "Trade002" }, new Lookup001_mapper());
					 
					 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "7";
				 }
			 }
				
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OustandingLetterCredit_ILC_AMD :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject OustandingLetterCredit_ILC_POA(String INFO1, String INFO2, String INFO3) //new
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 String bankRelationshipType_1 = "2";
			 String beneficiaryName_1 = "";
			 String beneficiaryCountry_1 = "";
			 boolean bankRatingStatus_1 = false;
			 boolean customerRatingStatus_1 = false;
			 String crRatingConfirmingBank_1 = "";
			 String crRatingCounterImporter_1 = "";
			 String marginBalance = "0";
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject importLCDetails = transactionDetails.get("importLCDetails").getAsJsonObject();
			 JsonObject lcType =importLCDetails.get("lcType").getAsJsonObject();
			 
			 String effectiveDate=importLCDetails.get("effectiveDate").getAsString();
			 
			 JsonObject tenorType = importLCDetails.get("tenorType").getAsJsonObject();
			 String tenorDays = importLCDetails.get("tenorDays").getAsString();
			 String code = tenorType.get("code").getAsString();
			 String expiryDate=importLCDetails.get("expiryDate").getAsString();
			 
			 if(code.equalsIgnoreCase("sight")) 
			 {
				 code = expiryDate;
			 }
			 else if(code.equalsIgnoreCase("USANC")) 
			 {
				 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL"; 
				 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{expiryDate, tenorDays}, String.class);
				 code = updatedDate; 
			 }
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String lcType_code = lcType.has("code") ? lcType.get("code").getAsString() : "";
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();
				 
				 if(id.equalsIgnoreCase("CUST"))
				 {
					 String leid = js.get("leid").getAsString();
					 
					 crRatingCounterImporter_1 = leid;
					 
					 String name = party.get("name").getAsString();
					 
					 if(name.equalsIgnoreCase("STANDARD CHARTERED BANK") && countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationshipType_1 = "1";
					 }
					 else if (name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "8";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "2";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "9";
					 }
					 else if (name.equalsIgnoreCase("Bank of Tanzania")) 
					 {
						 bankRelationshipType_1 = "15";
					 }	
				 }
				 
				 if(partyRoleCode.equals("BNE") || partyRoleCode.equals("BENE") || partyRoleCode.equals("BENI"))
				 { 
					 beneficiaryName_1 = party.get("name").getAsString();			 
					 beneficiaryCountry_1= countryCode;
					 customerRatingStatus_1 = true;
				 }
				 
				 if(partyRoleCode.equalsIgnoreCase("CFMBK")) 
				 {
					crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
					bankRatingStatus_1 = true;
				 }
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for(int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				
				marginBalance = js.get("marginBalance").getAsString();
			 }
			
			 JsonObject amountDetails = importLCDetails.has("amountDetails") ? importLCDetails.get("amountDetails").getAsJsonObject(): new JsonObject();	 
			 JsonObject lcOSAmount = amountDetails.has("lcOSAmount") ? amountDetails.get("lcOSAmount").getAsJsonObject(): new JsonObject();
			 String Currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 
			 String value = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW
			 
			 JsonObject confirmation = importLCDetails.get("confirmation").getAsJsonObject();
			 String confirmation_code = confirmation.get("code").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 String lettersCreditType = lcType_code; 
			 String collateralType = "11";//bank said to stamp as 11
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(code, "yyyy-MM-dd") ;  
			 String expireDate =util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = "1";//bank said to stamp as 1
			 String bankRelationshipType = bankRelationshipType_1;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1; 
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRatingCounterImporter_1); //cc; 
			 String gradesUnratedCustomer = "";  
			 String currency = Currency; 
			 String orgAmount = value;	
			 String usdAmount = "0";  
			 String tzsAmount = "0";  
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0";  
			 String tzsOutstandingMargDepAmount = "0";  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRatingCounterImporter_1); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice
			 String lcClassification = confirmation_code.equalsIgnoreCase("Y")? "1" : "2" ;  
			 boolean bankRatingStatus = bankRatingStatus_1;
			 String crRatingConfirmingBank = crRatingConfirmingBank_1;  
			 String gradesUnratedConfirmingBank = ""; 
			 String botProvision = "0";  
			 
			 if(bankRatingStatus_1)
			 {
				 String sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
			 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";

			 if(util.isNullOrEmpty(lettersCreditType))
			 {
				 sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 JsonObject importLCDetails1 = transactionDetails1.get("importLCDetails").getAsJsonObject();
					 JsonObject lcType1 =importLCDetails1.get("lcType").getAsJsonObject();
					 
					 String lcType_code1 = lcType1.has("code") ? lcType1.get("code").getAsString() : "";
					 
					 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { lcType_code1, "Trade002" }, new Lookup001_mapper());
					 
					 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "7";
				 }
			 }
			 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OustandingLetterCredit_ILC_POA :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject OustandingLetterCredit_ILC_AAC(String INFO1, String INFO2, String INFO3) //new
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 String bankRelationshipType_1 = "2";
			 String beneficiaryName_1 = "";
			 String beneficiaryCountry_1 = "";
			 boolean bankRatingStatus_1 = false;
			 boolean customerRatingStatus_1 = false;
			 String crRatingConfirmingBank_1 = "";
			 String crRatingCounterImporter_1 = "";
			 String marginBalance = "0";
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject importLCDetails = transactionDetails.get("importLCDetails").getAsJsonObject();
			 JsonObject lcType =importLCDetails.get("lcType").getAsJsonObject();
			 
			 String effectiveDate=importLCDetails.get("effectiveDate").getAsString();
			 
			 JsonObject tenorType = importLCDetails.get("tenorType").getAsJsonObject();
			 String tenorDays = importLCDetails.get("tenorDays").getAsString();
			 String code = tenorType.get("code").getAsString();
			 String expiryDate=importLCDetails.get("expiryDate").getAsString();
			 
			 if(code.equalsIgnoreCase("sight")) 
			 {
				 code = expiryDate; 
			 }
			 else if(code.equalsIgnoreCase("USANC")) 
			 {
				 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL"; 
				 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{expiryDate, tenorDays}, String.class);
				 code = updatedDate; 
			 }
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String lcType_code = lcType.has("code") ? lcType.get("code").getAsString() : "";
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();
				 
				 if(id.equalsIgnoreCase("CUST"))
				 {
					 String leid = js.get("leid").getAsString();
					 crRatingCounterImporter_1 = leid;
					 
					 String name = party.get("name").getAsString();
					 
					 if(name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationshipType_1 = "1";
					 }
					 else if (name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "8";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "2";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "9";
					 }
					 else if (name.equalsIgnoreCase("Bank of Tanzania")) 
					 {
						 bankRelationshipType_1 = "15";
					 }	
				 }
				 
				 if(partyRoleCode.equals("BNE") || partyRoleCode.equals("BENE") || partyRoleCode.equals("BENI"))
				 { 
					 beneficiaryName_1 = party.get("name").getAsString();			 
					 beneficiaryCountry_1= countryCode;
					 customerRatingStatus_1 = true;
				 }
				 
				 if(partyRoleCode.equalsIgnoreCase("CFMBK")) 
				 {
					crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
					bankRatingStatus_1 = true;
				 }
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for(int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				
				marginBalance = js.get("marginBalance").getAsString();
			 }
			
			 JsonObject amountDetails = importLCDetails.has("amountDetails") ? importLCDetails.get("amountDetails").getAsJsonObject(): new JsonObject();	 
			 JsonObject lcOSAmount = amountDetails.has("lcOSAmount") ? amountDetails.get("lcOSAmount").getAsJsonObject(): new JsonObject();
			 String Currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 
			 String value = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW
			 
			 JsonObject confirmation = importLCDetails.get("confirmation").getAsJsonObject();
			 String confirmation_code = confirmation.get("code").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 String lettersCreditType = lcType_code;  //path not available|lctype object is available but inside there is no field
			 String collateralType = "11";//bank said to stamp as 11
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(code, "yyyy-MM-dd") ;  
			 String expireDate =util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = "1";//bank said to stamp as 1
			 String bankRelationshipType = bankRelationshipType_1;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1; 
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRatingCounterImporter_1); //cc; ;
			 String gradesUnratedCustomer = ""; 
			 String currency = Currency; 
			 String orgAmount = value;	
			 String usdAmount = "0";  
			 String tzsAmount = "0";  
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0";  
			 String tzsOutstandingMargDepAmount = "0";  
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRatingCounterImporter_1); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";  
			 String lcClassification = confirmation_code.equalsIgnoreCase("Y")? "1" : "2" ;  
			 boolean bankRatingStatus = bankRatingStatus_1;
			 String crRatingConfirmingBank = crRatingConfirmingBank_1;  //to work on lookup
			 String gradesUnratedConfirmingBank = ""; 
			 
			 if(bankRatingStatus_1)
			 {
				 String sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
			 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 if(util.isNullOrEmpty(lettersCreditType))
			 {
				 sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 JsonObject importLCDetails1 = transactionDetails1.get("importLCDetails").getAsJsonObject();
					 JsonObject lcType1 =importLCDetails1.get("lcType").getAsJsonObject();
					 
					 String lcType_code1 = lcType1.has("code") ? lcType1.get("code").getAsString() : "";
					 
					 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { lcType_code1, "Trade002" }, new Lookup001_mapper());
					 
					 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "7";
				 }
			 }
			 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OustandingLetterCredit_ILC_AAC :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject OustandingLetterCredit_ILC_APA(String INFO1, String INFO2, String INFO3) //new
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			
			 Timestamp timestamp = util.get_oracle_Timestamp();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			
			 String notificationID = data.get("notificationID").getAsString();
			
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 String bankRelationshipType_1 = "2";
			 String beneficiaryName_1 = "";
			 String beneficiaryCountry_1 = "";
			 boolean bankRatingStatus_1 = false;
			 boolean customerRatingStatus_1 = false;
			 String crRatingConfirmingBank_1 = "";
			 String crRatingCounterImporter_1 = "";
			 String marginBalance = "0";
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject importLCDetails = transactionDetails.get("importLCDetails").getAsJsonObject();
			 JsonObject lcType =importLCDetails.get("lcType").getAsJsonObject();
			
			 String effectiveDate = importLCDetails.get("effectiveDate").getAsString();
			 
			 JsonObject tenorType = importLCDetails.get("tenorType").getAsJsonObject();
			 String tenorDays = importLCDetails.get("tenorDays").getAsString();
			 String code = tenorType.get("code").getAsString();
			 String expiryDate=importLCDetails.get("expiryDate").getAsString();
			 
			 if(code.equalsIgnoreCase("sight")) 
			 {
				 code = expiryDate; 
				 
			 }
			 else if(code.equalsIgnoreCase("USANC")) 
			 {
				 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL"; 
				 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{expiryDate, tenorDays}, String.class);
				 code = updatedDate; 
			 }
			 
			 String lcType_code = lcType.has("code") ? lcType.get("code").getAsString() : "";
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();
				 
				 if(id.equalsIgnoreCase("CUST"))
				 {
					 String name = party.get("name").getAsString();
					 
					 if(name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationshipType_1 = "1";
					 }
					 else if (name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "8";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "2";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "9";
					 }
					 else if (name.equalsIgnoreCase("Bank of Tanzania")) 
					 {
						 bankRelationshipType_1 = "15";
					 }	
				 }
				 
				 if(partyRoleCode.equals("BNE") || partyRoleCode.equals("BENE"))
				 {
					 beneficiaryName_1 = party.get("name").getAsString(); 
					 beneficiaryCountry_1= countryCode;
					 customerRatingStatus_1 = true;
					 bankRatingStatus_1 = true;
				 }
				 
				 if(partyRoleCode.equals("BENE")||partyRoleCode.equals("BENI")) 
				 {
					 String leid = js.get("leid").getAsString();
					 crRatingCounterImporter_1 = leid;
				 }	 
		
				if (partyRoleCode.equalsIgnoreCase("CFMBK")) 
				{
					crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
					bankRatingStatus_1 = true;
				}
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for(int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				
				marginBalance = js.get("marginBalance").getAsString();
			 }
 
			 JsonObject confirmation = importLCDetails.get("confirmation").getAsJsonObject();
			 String confirmation_code = confirmation.get("code").getAsString();

			 JsonObject amountDetails = importLCDetails.has("amountDetails") ? importLCDetails.get("amountDetails").getAsJsonObject(): new JsonObject();	 
			 JsonObject lcOSAmount = amountDetails.has("lcOSAmount") ? amountDetails.get("lcOSAmount").getAsJsonObject(): new JsonObject();
			 String Currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 
			 String value = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW

			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 String lettersCreditType = lcType_code; 
			 String collateralType = "11";//bank said to stamp as 11
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(code, "yyyy-MM-dd");  
			 String expireDate =util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = "1";//bank said to stamp as 1
			 String bankRelationshipType = bankRelationshipType_1;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1;  // to work on lookup
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRatingCounterImporter_1); //cc; ;  
			 String gradesUnratedCustomer = "";  
			 String currency = Currency;
			 String orgAmount = value;	
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0"; 
			 String tzsOutstandingMargDepAmount = "0"; 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1";
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRatingCounterImporter_1); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String lcClassification = confirmation_code.equalsIgnoreCase("Y")? "1" : "2" ;  
			 boolean bankRatingStatus = bankRatingStatus_1;
			 String crRatingConfirmingBank = crRatingConfirmingBank_1;
			 String gradesUnratedConfirmingBank = "";
			 String botProvision = "0";  
			 
			 if(bankRatingStatus_1)
			 {
				 String sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
			 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 if(util.isNullOrEmpty(lettersCreditType))
			 {
				 sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 JsonObject importLCDetails1 = transactionDetails1.get("importLCDetails").getAsJsonObject();
					 JsonObject lcType1 =importLCDetails1.get("lcType").getAsJsonObject();
					 
					 String lcType_code1 = lcType1.has("code") ? lcType1.get("code").getAsString() : "";
					 
					 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { lcType_code1, "Trade002" }, new Lookup001_mapper());
					 
					 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "7";
				 }
			 }
			 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in OustandingLetterCredit_ILC_APA :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject OustandingLetterCredit_ILC_MSC(String INFO1, String INFO2, String INFO3)
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			
			 Timestamp timestamp = util.get_oracle_Timestamp();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			
			 String notificationID = data.get("notificationID").getAsString();
			
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 String bankRelationshipType_1 = "2";
			 String beneficiaryName_1 = "";
			 String beneficiaryCountry_1 = "";
			 boolean bankRatingStatus_1 = false;
			 boolean customerRatingStatus_1 = false;
			 String crRatingConfirmingBank_1 = "";
			 String crRatingCounterImporter_1 = "";
			 String marginBalance = "0";
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject importLCDetails = transactionDetails.get("importLCDetails").getAsJsonObject();
			 JsonObject lcType =importLCDetails.get("lcType").getAsJsonObject();
			 //String lcType_code = lcType.get("code").getAsString();
			 
			 String effectiveDate = importLCDetails.get("effectiveDate").getAsString();
			 
			 JsonObject tenorType = importLCDetails.get("tenorType").getAsJsonObject();
			 String tenorDays = importLCDetails.get("tenorDays").getAsString();
			 String code = tenorType.get("code").getAsString();
			 String expiryDate;
			 
			 JsonObject miscellaneousStepDetails = transactionDetails.has("miscellaneousStepDetails") && !transactionDetails.get("miscellaneousStepDetails").isJsonNull() ? transactionDetails.getAsJsonObject("miscellaneousStepDetails") : new JsonObject();
			 String miscTypeCode = miscellaneousStepDetails.has("miscTypeCode")? !util.isNullOrEmpty(miscellaneousStepDetails.get("miscTypeCode").getAsString())? miscellaneousStepDetails.get("miscTypeCode").getAsString() :"NA" : "NA";

			 if(miscTypeCode.equalsIgnoreCase("RI"))
			 {
				 expiryDate= util.getCurrentDate();
			 }
			 else 
			 {
				 expiryDate=importLCDetails.get("expiryDate").getAsString();
			 }

			 if(code.equalsIgnoreCase("sight")) 
			 {
				 code = expiryDate;
				 
			 }
			 else if(code.equalsIgnoreCase("USANC")) 
			 {
				 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL"; 
				 
				 code = Jdbctemplate.queryForObject(sql, new Object[]{expiryDate, tenorDays}, String.class);
			 }
			 
			 String lcType_code = lcType.has("code") ? lcType.get("code").getAsString() : "";
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();
				 
				 if(id.equalsIgnoreCase("CUST"))
				 {
					 String name = party.get("name").getAsString();
					 
					 if(name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ"))
					 {
						 bankRelationshipType_1 = "1";
					 }
					 else if (name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "8";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "2";
					 }
					 else if (!name.equalsIgnoreCase("STANDARD CHARTERED BANK")&&!countryCode.equalsIgnoreCase("TZ")) 
					 {
						 bankRelationshipType_1 = "9";
					 }
					 else if (name.equalsIgnoreCase("Bank of Tanzania")) 
					 {
						 bankRelationshipType_1 = "15";
					 }	
				 }
				 
				 if(partyRoleCode.equals("BNE") || partyRoleCode.equals("BENE"))
				 {
					 beneficiaryName_1 = party.get("name").getAsString(); 
					 beneficiaryCountry_1= countryCode;
					 customerRatingStatus_1 = true;
				 }
				 
				 if(partyRoleCode.equals("BENE")||partyRoleCode.equals("BENI")) 
				 {
					 String leid = js.get("leid").getAsString();
					 crRatingCounterImporter_1 = leid;
				 }	 
		
				if (partyRoleCode.equalsIgnoreCase("CFMBK")) 
				{
					crRatingConfirmingBank_1 = js.get("partyRiskGrade").getAsString();
					bankRatingStatus_1 = true;
				}
			 }
			 
			 JsonArray marginDetails = transactionDetails.has("marginDetails") ? transactionDetails.get("marginDetails").getAsJsonArray() : new JsonArray();
			 
			 for(int i = 0; i < marginDetails.size(); i++) 
			 {
				JsonObject js = marginDetails.get(0).getAsJsonObject();
				
				marginBalance = js.get("marginBalance").getAsString();
			 }
 
			 JsonObject confirmation = importLCDetails.get("confirmation").getAsJsonObject();
			 String confirmation_code = confirmation.get("code").getAsString();

			 JsonObject amountDetails = importLCDetails.has("amountDetails") ? importLCDetails.get("amountDetails").getAsJsonObject(): new JsonObject();	 
			 JsonObject lcOSAmount = amountDetails.has("lcOSAmount") ? amountDetails.get("lcOSAmount").getAsJsonObject(): new JsonObject();
			 String Currency = lcOSAmount.has("currency") ? lcOSAmount.get("currency").getAsString() : ""; //NEW
			 
			 String value = lcOSAmount.has("value") ? lcOSAmount.get("value").getAsString() : ""; //NEW
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-MM-dd");
			 String lettersCreditType = lcType_code; //path not available|lctype object is available but inside there is no field
			 String collateralType = "11";//bank said to stamp as 11
			 String openingDate = util.Convert_BOT_Date_Format(effectiveDate, "yyyy-MM-dd");
			 String maturityDate =util.Convert_BOT_Date_Format(code, "yyyy-MM-dd") ;  
			 String expireDate =util.Convert_BOT_Date_Format(expiryDate, "yyyy-MM-dd");
			 String holderName = data.get("customerName").getAsString();
			 String relationshipType = "1";//bank said to stamp as 1
			 String bankRelationshipType = bankRelationshipType_1;
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiaryCountry_1; 
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRatingCounterImporter_1); //cc; ;  
			 String gradesUnratedCustomer = "";  
			 String currency = Currency;
			 String orgAmount = value;	
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 String orgOutstandingMargDepAmount = marginBalance; 
			 String usdOutstandingMargDepAmount = "0"; 
			 String tzsOutstandingMargDepAmount = "0"; 
			 int dateDifference =  Integer.parseInt(FindDateDifference(expiryDate, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationType = "1";
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRatingCounterImporter_1); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String lcClassification = confirmation_code.equalsIgnoreCase("Y")? "1" : "2" ;  
			 boolean bankRatingStatus = bankRatingStatus_1;
			 String crRatingConfirmingBank = crRatingConfirmingBank_1;
			 String gradesUnratedConfirmingBank = "";
			 String botProvision = "0";  
			 
			 if(bankRatingStatus_1)
			 {
				 String sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { crRatingConfirmingBank, "SCI001" }, new Lookup001_mapper());
					 
				 crRatingConfirmingBank = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "7";
			 }
			 else
			 {
				 crRatingConfirmingBank = "";
				 gradesUnratedConfirmingBank = "3";
			 }
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);

			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingMargDepAmount, currency);
			 
			 usdOutstandingMargDepAmount = rates.get("usd").getAsString();
			 tzsOutstandingMargDepAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			 
			 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
			 
			 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 if(util.isNullOrEmpty(lettersCreditType))
			 {
				 sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and PRODCODE=? and PARENTDEAL=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, product_code, "1" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 JsonObject importLCDetails1 = transactionDetails1.get("importLCDetails").getAsJsonObject();
					 JsonObject lcType1 =importLCDetails1.get("lcType").getAsJsonObject();
					 
					 String lcType_code1 = lcType1.has("code") ? lcType1.get("code").getAsString() : "";
					 
					 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { lcType_code1, "Trade002" }, new Lookup001_mapper());
					 
					 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "7";
				 }
			 }
			 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in OustandingLetterCredit_ILC_MSC :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Customerliabilities_APA(String INFO1, String INFO2, String INFO3) //RTS029
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
				
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 //String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 JsonObject transactionDetails = data.has("transactionDetails") ? data.get("transactionDetails").getAsJsonObject() : new JsonObject();
			 
			 JsonObject paymentDetails = transactionDetails.has("paymentDetails") ? transactionDetails.get("paymentDetails").getAsJsonObject() : new JsonObject();
			 
			// JsonArray acceptanceSchedule = acceptanceDetails.has("acceptanceSchedule") ? acceptanceDetails.get("acceptanceSchedule").getAsJsonArray() : new JsonArray();

			 JsonObject paymentAmount = paymentDetails.has("paymentAmount") ? paymentDetails.get("paymentAmount").getAsJsonObject() : new JsonObject();

			 String acceptanceValueDate = paymentDetails.has("paymentValueDate") ? paymentDetails.get("paymentValueDate").getAsString() : "";
			 
			 String Matrity_date = acceptanceValueDate;
			 
			 /*if(acceptanceSchedule.size() > 0)
			 {
				 Matrity_date = acceptanceSchedule.get(0).getAsJsonObject().get("maturityDate").getAsString();
			 }
			 
			 if(util.isNullOrEmpty(Matrity_date))
			 {
				 JsonObject presentationDetails = transactionDetails.has("presentationDetails") ? transactionDetails.get("presentationDetails").getAsJsonObject() : new JsonObject();
				 
				 JsonObject acceptanceDates = presentationDetails.has("acceptanceDates") ? presentationDetails.get("acceptanceDates").getAsJsonObject() : new JsonObject();
				 
				 Matrity_date = acceptanceDates.has("maturityDate") ? acceptanceDates.get("maturityDate").getAsString() : "";
			 }
			 */
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String leid = "";
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 if(partyRoleCode.equals("APPL"))
				 { 
					 leid = js.get("leid").getAsString();
				 }
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
		 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
			 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String draftHolder = customerName;
			 String transactionDate = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-mm-dd");
			 String valueDate = util.Convert_BOT_Date_Format(acceptanceValueDate, "yyyy-mm-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(Matrity_date, "yyyy-mm-dd");
			 String currency = paymentAmount.has("currency") ? paymentAmount.get("currency").getAsString() : ""; 
			 String orgAmount = paymentAmount.has("value") ? paymentAmount.get("value").getAsString() : ""; 
			 String usdAmount = "0";
			 String tzsAmount = "0";
			 int dateDifference =  Integer.parseInt(FindDateDifference(Matrity_date, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationCategory = "1"; // bank said to stamp as 1.
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);
			 String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", leid+"|"+LimitID);
			 String botProvision = "0";
			 
			 collateralPledged = util.isNullOrEmpty(collateralPledged) ? "2" : collateralPledged;

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "customerLiabilities", count, reportingDate, draftHolder, transactionDate, valueDate, maturityDate, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, botProvision, assetClassificationCategory, sectorSnaClassification, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", count, reportingDate, draftHolder, transactionDate, collateralPledged });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL,"C", INFO1, "customerLiabilities", "count", "reportingDate", "draftHolder", "transactionDate", "valueDate", "maturityDate", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "botProvision", "assetClassificationCategory", "sectorSnaClassification", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL,"C", INFO1, "collateralPledged", "count", "reportingDate", "draftHolder", "transactionDate", "collateralPledged"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "customerLiabilities","1"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged","2"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Asset", "customerLiabilities", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
					
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Customerliabilities_APA :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Customerliabilities_ACP_AAC_POA(String INFO1, String INFO2, String INFO3) //RTS029
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 //String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 JsonObject transactionDetails = data.has("transactionDetails") ? data.get("transactionDetails").getAsJsonObject() : new JsonObject();
			 
			 JsonObject acceptanceDetails = transactionDetails.has("acceptanceDetails") ? transactionDetails.get("acceptanceDetails").getAsJsonObject() : new JsonObject();
			 
			 JsonArray acceptanceSchedule = acceptanceDetails.has("acceptanceSchedule") ? acceptanceDetails.get("acceptanceSchedule").getAsJsonArray() : new JsonArray();

			 JsonObject acceptanceAmount = acceptanceDetails.has("acceptanceAmount") ? acceptanceDetails.get("acceptanceAmount").getAsJsonObject() : new JsonObject();

			 String acceptanceValueDate = acceptanceDetails.has("acceptanceValueDate") ? acceptanceDetails.get("acceptanceValueDate").getAsString() : "";
			 
			 String Matrity_date = "";
			 
			 if(acceptanceSchedule.size() > 0)
			 {
				 Matrity_date = acceptanceSchedule.get(0).getAsJsonObject().get("maturityDate").getAsString();
			 }
			 
			 if(util.isNullOrEmpty(Matrity_date))
			 {
				 JsonObject presentationDetails = transactionDetails.has("presentationDetails") ? transactionDetails.get("presentationDetails").getAsJsonObject() : new JsonObject();
				 
				 JsonObject acceptanceDates = presentationDetails.has("acceptanceDates") ? presentationDetails.get("acceptanceDates").getAsJsonObject() : new JsonObject();
				 
				 Matrity_date = acceptanceDates.has("maturityDate") ? acceptanceDates.get("maturityDate").getAsString() : "";
			 }
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 String leid = "";
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject partyRole = js.get("partyRole").getAsJsonObject();
				 String partyRoleCode = partyRole.get("code").getAsString();
				 
				 if(partyRoleCode.equals("APPL"))
				 { 
					 leid = js.get("leid").getAsString();
				 }
			 }
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
			 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String draftHolder = customerName;
			 String transactionDate = util.Convert_BOT_Date_Format(eventTimestamp, "yyyy-mm-dd");
			 String valueDate = util.Convert_BOT_Date_Format(acceptanceValueDate, "yyyy-mm-dd");
			 String maturityDate = util.Convert_BOT_Date_Format(Matrity_date, "yyyy-mm-dd");
			 String currency = acceptanceAmount.has("currency") ? acceptanceAmount.get("currency").getAsString() : ""; 
			 String orgAmount = acceptanceAmount.has("value") ? acceptanceAmount.get("value").getAsString() : ""; 
			 String usdAmount = "0";
			 String tzsAmount = "0";
			 int dateDifference =  Integer.parseInt(FindDateDifference(Matrity_date, "yyyy-mm-dd"));
			 int pastDueDays = (dateDifference < 0) ? 0 : dateDifference;
			 String assetClassificationCategory = "1"; // bank said to stamp as 1.
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", leid); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);
			 String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", leid+"|"+LimitID); 
			 String botProvision = "0";
			 
			 collateralPledged = util.isNullOrEmpty(collateralPledged) ? "2" : collateralPledged;

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 
			 int count = 1;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "customerLiabilities", count, reportingDate, draftHolder, transactionDate, valueDate, maturityDate, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, botProvision, assetClassificationCategory, sectorSnaClassification, allowanceProbableLoss});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", count, reportingDate, draftHolder, transactionDate, collateralPledged });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL,"C", INFO1, "customerLiabilities", "count", "reportingDate", "draftHolder", "transactionDate", "valueDate", "maturityDate", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "botProvision", "assetClassificationCategory", "sectorSnaClassification", "allowanceProbableLoss"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL,"C", INFO1, "collateralPledged", "count", "reportingDate", "draftHolder", "transactionDate", "collateralPledged"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "customerLiabilities","1"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged","2"});
			 
			 //-----------------
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Asset", "customerLiabilities", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
					
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Customerliabilities_APA :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	/***********  IMDOC ********/
	
	public JsonObject inwardBills_IMDOC_ISS(String INFO1, String INFO2, String INFO3) //new 
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject collectionDetails = transactionDetails.has("collectionDetails") ? transactionDetails.get("collectionDetails").getAsJsonObject() : new JsonObject();
			 String startingDate = collectionDetails.has("startingDate") ? collectionDetails.get("startingDate").getAsString() : "";
			
			 JsonObject tenorType = collectionDetails.has("tenorType") ? collectionDetails.get("tenorType").getAsJsonObject() : new JsonObject();
			 String code = tenorType.has("code") ? tenorType.get("code").getAsString() : "";
			 
			 String maturityDateString_1 = "";
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 String tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
				 
				 maturityDateString_1 = AddDays(maturityDateString_1, tenorDays);
			 }
			 
			 String customerName = data.get("customerName").getAsString();
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					
					if (party_role_code.equalsIgnoreCase("DWR")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("COLBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 int pastdueday = 0;  String tenorDays = "0";
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 
			     pastdueday = FindPastDueDay(dueDate);
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				
				 String finalDate = AddDays(dueDate, tenorDays);
				 
				 pastdueday = FindPastDueDay(finalDate);
			 }
			 
			 if(util.isNullOrEmpty(startingDate)) 
			 {
				 startingDate = collectionDetails.has("shipmentDate") ? collectionDetails.get("shipmentDate").getAsString() : "";
			 }
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(startingDate, "YYYY-MM-DD");
			 String maturityDate = util.Convert_BOT_Date_Format(maturityDateString_1, "YYYY-MM-DD");
			 String holderName = customerName;
			 String relationshipType = "1"; //bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterDrawerBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc; ;
			 String gradesUnratedDrawerBank = ""; 
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0"; 
			 String tzsAmount = "0";
			 long pastDueDays = pastdueday;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";//bank said to stamp as 0
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "inwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterDrawerBank, gradesUnratedDrawerBank, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "inwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterDrawerBank", "gradesUnratedDrawerBank", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "inwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "inwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in inwardBills_IMDOC_ISS :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public String AddDays(String Date, String Days) 
	{
		 String Out = "";
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL";
			 
			 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{ Date, Days}, String.class);
			 
			 Out = util.Convert_BOT_Date_Format(updatedDate, "yyyy-MM-dd"); 
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in AddDays >>>> "+e.getLocalizedMessage());
		 }
		 
		 return Out;
	}
	
	public String AddDays_NoBOTFormat(String Date, String Days) 
	{
		 String Out = "";
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "SELECT TO_CHAR(TO_DATE(?, 'YYYY-MM-DD') + ?, 'YYYY-MM-DD') AS formatted_date FROM DUAL";
			 
			 String updatedDate = Jdbctemplate.queryForObject(sql, new Object[]{ Date, Days}, String.class);
			 
			 Out = updatedDate;
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in AddDays >>>> "+e.getLocalizedMessage());
		 }
		 
		 return Out;
	}
	
	public int FindPastDueDay(String finalDate) 
	{
		 int pastdueday = 0;
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 int dateDifference =  Integer.parseInt(FindDateDifference(finalDate, "yyyy-mm-dd"));
			 
			 pastdueday = (dateDifference < 0) ? 0 : dateDifference;
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in AddDays >>>> "+e.getLocalizedMessage());
		 }
		 
		 return pastdueday;
	}
	
	public JsonObject inwardBills_IMDOC_AMD(String INFO1, String INFO2, String INFO3) //new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			 
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject collectionDetails = transactionDetails.has("collectionDetails") ? transactionDetails.get("collectionDetails").getAsJsonObject() : new JsonObject();
			 String startingDate = collectionDetails.has("startingDate") ? collectionDetails.get("startingDate").getAsString() : "";
			
			 JsonObject tenorType = collectionDetails.has("tenorType") ? collectionDetails.get("tenorType").getAsJsonObject() : new JsonObject();
			 String code = tenorType.has("code") ? tenorType.get("code").getAsString() : "";
			 
			 String maturityDateString_1 = "";
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 String tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
				 
				 maturityDateString_1 = AddDays(maturityDateString_1, tenorDays);
			 }
			 
			 String customerName = data.get("customerName").getAsString();
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					if (party_role_code.equalsIgnoreCase("DWR")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("COLBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 int pastdueday = 0; String tenorDays = "0";
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 
			     pastdueday = FindPastDueDay(dueDate);
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				
				 String finalDate = AddDays(dueDate, tenorDays);
				 
				 pastdueday = FindPastDueDay(finalDate);
			 }
			 
			 if(util.isNullOrEmpty(startingDate)) 
			 {
				 startingDate = collectionDetails.has("shipmentDate") ? collectionDetails.get("shipmentDate").getAsString() : "";
			 }
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(startingDate, "YYYY-MM-DD");
			 String maturityDate = util.Convert_BOT_Date_Format(maturityDateString_1, "YYYY-MM-DD");
			 String holderName = customerName;
			 String relationshipType = "1";//bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterDrawerBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc; 
			 String gradesUnratedDrawerBank = "";
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0";
			 String tzsAmount = "0";
			 long pastDueDays = (pastdueday <= 0)? 0 : pastdueday;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0"; 
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "inwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterDrawerBank, gradesUnratedDrawerBank, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "inwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterDrawerBank", "gradesUnratedDrawerBank", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "inwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "inwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in inwardBills_IMDOC_AMD :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject inwardBills_IMDOC_POA(String INFO1, String INFO2, String INFO3) 
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			 			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					
					if (party_role_code.equalsIgnoreCase("DWR")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("COLBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = "";
			 String maturityDate = ""; 
			 String holderName = customerName;
			 String relationshipType = "1"; //bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterDrawerBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc; 
			 String gradesUnratedDrawerBank = ""; 
			 String currency = currency_1;
			 String orgAmount = value; 
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 long pastDueDays = 0; 
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0"; //bank said to stamp as 0
			 
			 if(util.isNullOrEmpty(openingDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and STEPCODE=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, "ACP" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.has("transactionDetails") ? data1.get("transactionDetails").getAsJsonObject() : new JsonObject();
					 
					 JsonObject collectionAcceptanceDetails = transactionDetails1.has("collectionAcceptanceDetails") ? transactionDetails1.get("collectionAcceptanceDetails").getAsJsonObject() : new JsonObject();

					 JsonArray acceptanceSchedule = collectionAcceptanceDetails.has("acceptanceSchedule") ? collectionAcceptanceDetails.get("acceptanceSchedule").getAsJsonArray() : new JsonArray();
					 
					 if(acceptanceSchedule.size() > 0)
					 {
						 JsonObject acp = acceptanceSchedule.get(0).getAsJsonObject();
						 
						 openingDate = acp.has("startingDate") ? acp.get("startingDate").getAsString() : ""; 
						 maturityDate = acp.has("maturityDate") ? acp.get("maturityDate").getAsString() : ""; 
						 
						 openingDate = util.Convert_BOT_Date_Format(openingDate, "YYYY-MM-DD");
						 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "YYYY-MM-DD");
					 }
				 }
			 }
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "inwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterDrawerBank, gradesUnratedDrawerBank, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "inwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterDrawerBank", "gradesUnratedDrawerBank", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "inwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "inwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in inwardBills_IMDOC_POA :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject inwardBills_IMDOC_PAY(String INFO1, String INFO2, String INFO3) //new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject collectionDetails = transactionDetails.get("collectionDetails").getAsJsonObject();
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					if (party_role_code.equalsIgnoreCase("DWR")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("COLBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = "";
			 String maturityDate = "";
			 String holderName = customerName;
			 String relationshipType = "1"; //bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterDrawerBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc; 
			 String gradesUnratedDrawerBank = ""; 
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0"; 
			 String tzsAmount = "0";
			 long pastDueDays = 0; 
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0"; //bank said to stamp as 0
			 
			 if(util.isNullOrEmpty(openingDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and STEPCODE in (?,?) order by REQTIME desc";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, "ISS", "AMD" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 
					 JsonObject collectionDetails1 = transactionDetails1.has("collectionDetails") ? transactionDetails1.get("collectionDetails").getAsJsonObject() : new JsonObject();
					 
					 openingDate = collectionDetails1.has("startingDate") ? collectionDetails1.get("startingDate").getAsString() : "";
					 maturityDate = collectionDetails1.has("dueDate") ? collectionDetails1.get("dueDate").getAsString() : "";
					 
					 openingDate = util.Convert_BOT_Date_Format(openingDate, "YYYY-MM-DD");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "YYYY-MM-DD");
				 }
			 }
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "inwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterDrawerBank, gradesUnratedDrawerBank, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "inwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterDrawerBank", "gradesUnratedDrawerBank", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "inwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "inwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in inwardBills_IMDOC_PAY :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
    
	/***********  EXDOC ********/
	
	public JsonObject OutwardBills_EXDOC_ISS(String INFO1, String INFO2, String INFO3) //new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject collectionDetails = transactionDetails.has("collectionDetails") ? transactionDetails.get("collectionDetails").getAsJsonObject() : new JsonObject();
			 String startingDate = collectionDetails.has("startingDate") ? collectionDetails.get("startingDate").getAsString() : "";
			
			 JsonObject tenorType = collectionDetails.has("tenorType") ? collectionDetails.get("tenorType").getAsJsonObject() : new JsonObject();
			 String code = tenorType.has("code") ? tenorType.get("code").getAsString() : "";
			 
			 String maturityDateString_1 = "";
			
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 String tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
				 
				 maturityDateString_1 = AddDays(maturityDateString_1, tenorDays);
			 }
			 
			 String customerName = data.get("customerName").getAsString();
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					
					if (party_role_code.equalsIgnoreCase("DWE")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("DWEBK") || party_role_code.equalsIgnoreCase("RMTBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 int pastdueday = 0;
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 
			     pastdueday = FindPastDueDay(dueDate);
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 String tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				
				 String finalDate = AddDays(dueDate, tenorDays);
				 
				 pastdueday = FindPastDueDay(finalDate);
			 }
			 
			 if(util.isNullOrEmpty(startingDate)) 
			 {
				 startingDate = collectionDetails.has("shipmentDate") ? collectionDetails.get("shipmentDate").getAsString() : "";
			 }
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(startingDate, "YYYY-MM-DD");
			 String maturityDate = util.Convert_BOT_Date_Format(maturityDateString_1, "YYYY-MM-DD");
			 String holderName = customerName;
			 String relationshipType = "1";  //bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc;;
			 String gradesUnratedBorrower = ""; 
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 long pastDueDays = pastdueday;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice
			 String botProvision = "0";//bank said to stamp as 0
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterBorrower, gradesUnratedBorrower, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterBorrower", "gradesUnratedBorrower", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "outwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in outwardBills_EXDOC_ISS :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject OutwardBills_EXDOC_AMD(String INFO1, String INFO2, String INFO3) //new
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject collectionDetails = transactionDetails.has("collectionDetails") ? transactionDetails.get("collectionDetails").getAsJsonObject() : new JsonObject();
			 String startingDate = collectionDetails.has("startingDate") ? collectionDetails.get("startingDate").getAsString() : "";
			
			 JsonObject tenorType = collectionDetails.has("tenorType") ? collectionDetails.get("tenorType").getAsJsonObject() : new JsonObject();
			 String code = tenorType.has("code") ? tenorType.get("code").getAsString() : "";
			 
			 String maturityDateString_1 = "";
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 String tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 maturityDateString_1 = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : "";
				 
				 if(util.isNullOrEmpty(maturityDateString_1))
				 {
					 maturityDateString_1 = AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 }
				 
				 maturityDateString_1 = AddDays(maturityDateString_1, tenorDays);
			 }
			 
			 String customerName = data.get("customerName").getAsString();
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					
					if (party_role_code.equalsIgnoreCase("DWE")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("DWEBK") || party_role_code.equalsIgnoreCase("RMTBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 int pastdueday = 0;
			 
			 if(code.equalsIgnoreCase("SIGHT"))
			 {
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				 
			     pastdueday = FindPastDueDay(dueDate);
			 }
			 else if (code.equalsIgnoreCase("USANC")) 
			 {
				 String tenorDays = collectionDetails.has("tenorDays") ? collectionDetails.get("tenorDays").getAsString() : "0";
				 String dueDate = collectionDetails.has("dueDate") ? collectionDetails.get("dueDate").getAsString() : AddDays_NoBOTFormat(util.getCurrentDate("yyyy-MM-dd"), "180");
				
				 String finalDate = AddDays(dueDate, tenorDays);
				 
				 pastdueday = FindPastDueDay(finalDate);
			 }
			 
			 if(util.isNullOrEmpty(startingDate)) 
			 {
				 startingDate = collectionDetails.has("shipmentDate") ? collectionDetails.get("shipmentDate").getAsString() : "";
			 }
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = util.Convert_BOT_Date_Format(startingDate, "YYYY-MM-DD");
			 String maturityDate = util.Convert_BOT_Date_Format(maturityDateString_1, "YYYY-MM-DD");
			 String holderName = customerName;
			 String relationshipType = "1";//bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc;;
			 String gradesUnratedBorrower = ""; 
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 long pastDueDays = (pastdueday <= 0)? 0 : pastdueday;
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0";//bank said to stamp as 0
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterBorrower, gradesUnratedBorrower, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterBorrower", "gradesUnratedBorrower", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "outwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in OutwardBills_EXOC_AMD :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject OutwardBills_EXDOC_PAY(String INFO1, String INFO2, String INFO3) //new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			 		 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 JsonObject collectionDetails = transactionDetails.get("collectionDetails").getAsJsonObject();
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for (int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					
					if(party_role_code.equalsIgnoreCase("DWE")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("DWEBK") || party_role_code.equalsIgnoreCase("RMTBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			 
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW

			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = "";
			 String maturityDate = "";
			 String holderName = customerName;
			 String relationshipType = "1"; //bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc;;
			 String gradesUnratedBorrower = ""; 
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 long pastDueDays = 0;
			 String assetClassificationType = "1";//bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating);  //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0"; //bank said to stamp as 0
			 
			 if(util.isNullOrEmpty(openingDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and STEPCODE in (?,?) order by REQTIME desc";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, "ISS", "AMD" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.get("transactionDetails").getAsJsonObject();
					 
					 JsonObject collectionDetails1 = transactionDetails1.has("collectionDetails") ? transactionDetails1.get("collectionDetails").getAsJsonObject() : new JsonObject();
					 
					 openingDate = collectionDetails1.has("startingDate") ? collectionDetails1.get("startingDate").getAsString() : "";
					 maturityDate = collectionDetails1.has("dueDate") ? collectionDetails1.get("dueDate").getAsString() : "";
					 
					 openingDate = util.Convert_BOT_Date_Format(openingDate, "YYYY-MM-DD");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "YYYY-MM-DD");
				 }
			 }
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterBorrower, gradesUnratedBorrower, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterBorrower", "gradesUnratedBorrower", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "outwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in inwardBills_IMDOC_PAY :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject OutwardBills_EXDOC_POA(String INFO1, String INFO2, String INFO3) //new
	{
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject step = data.get("step").getAsJsonObject();
			 String step_code = step.get("code").getAsString();
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 String notificationID  = data.get("notificationID").getAsString();
			 JsonObject product = data.get("product").getAsJsonObject();
			 String product_code = product.get("code").getAsString();
			 
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 String customerName = data.get("customerName").getAsString();
			 
			 String beneficiaryName_1 = "";
			 String beneficiarycountry_1 = "";
			 String crRating = "";
			 boolean rating = false;
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 for(int i = 0; i < partyDetails.size(); i++) 
			 {
					JsonObject js = partyDetails.get(i).getAsJsonObject();
					JsonObject party = js.get("party").getAsJsonObject();
					JsonObject address = js.get("address").getAsJsonObject();
					String countryCode = address.get("countryCode").getAsString();
					JsonObject partyRole = js.get("partyRole").getAsJsonObject();
					String party_role_code = partyRole.get("code").getAsString();
					
					if (party_role_code.equalsIgnoreCase("DWE")) 
					{
						beneficiaryName_1 = party.get("name").getAsString();
						beneficiarycountry_1 = countryCode;
					}
					
					if(party_role_code.equalsIgnoreCase("DWEBK") ||party_role_code.equalsIgnoreCase("RMTBK"))
					{
						crRating = js.get("leid").getAsString();
						
						rating = true;
					}
			 }
			  
			 JsonObject collectionSettlementDetails = transactionDetails.has("collectionSettlementDetails") ? transactionDetails.get("collectionSettlementDetails").getAsJsonObject() : new JsonObject();
			 
			 String currency_1 = collectionSettlementDetails.has("drawingCurrency") ? collectionSettlementDetails.get("drawingCurrency").getAsString() : ""; //NEW
			 String value = collectionSettlementDetails.has("drawingBalance") ? collectionSettlementDetails.get("drawingBalance").getAsString() : ""; //NEW
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String reportingDate = util.getCurrentReportDate();
			 String openingDate = ""; 
			 String maturityDate = ""; 
			 String holderName = customerName;
			 String relationshipType = "1"; //bank said to stamp as 1
			 String beneficiaryName = beneficiaryName_1;
			 String beneficiaryCountry = beneficiarycountry_1;
			 boolean ratingStatus = true;
			 String crRatingCounterBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", crRating); //cc;;
			 String gradesUnratedBorrower = ""; 
			 String currency = currency_1;
			 String orgAmount = value;
			 String usdAmount = "0"; 
			 String tzsAmount = "0"; 
			 long pastDueDays = 0; 
			 String assetClassificationType = "1"; //bank said to stamp as 1
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", crRating); //cc
			 String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code);  // splice 
			 String botProvision = "0"; //bank said to stamp as 0
			 
			 if(util.isNullOrEmpty(openingDate))
			 {
				 String sql = "select MESSAGE from trade001 where SYSCODE=? and DEALREFNO=? and STEPCODE=?";
				 
				 List<String> Parent_payload =  Jdbctemplate.queryForList(sql, new Object[] { processingSystemCode, dealReferenceNo, "ACP" }, String.class);
				 
				 if(Parent_payload.size() > 0)
				 {
					 String Payload = Parent_payload.get(0);
					 
					 JsonObject data1 = util.StringToJsonObject(Payload);
					 
					 JsonObject transactionDetails1 = data1.has("transactionDetails") ? data1.get("transactionDetails").getAsJsonObject() : new JsonObject();
					 
					 JsonObject collectionAcceptanceDetails = transactionDetails1.has("collectionAcceptanceDetails") ? transactionDetails1.get("collectionAcceptanceDetails").getAsJsonObject() : new JsonObject();

					 JsonArray acceptanceSchedule = collectionAcceptanceDetails.has("acceptanceSchedule") ? collectionAcceptanceDetails.get("acceptanceSchedule").getAsJsonArray() : new JsonArray();
					 
					 if(acceptanceSchedule.size() > 0)
					 {
						 JsonObject acp = acceptanceSchedule.get(0).getAsJsonObject();
						 
						 openingDate = acp.has("startingDate") ? acp.get("startingDate").getAsString() : ""; 
						 maturityDate = acp.has("maturityDate") ? acp.get("maturityDate").getAsString() : ""; 
						 
						 openingDate = util.Convert_BOT_Date_Format(openingDate, "YYYY-MM-DD");
						 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "YYYY-MM-DD");
					 }
				 }
			 }
			 
			 openingDate = util.isNullOrEmpty(openingDate) ? reportingDate : openingDate;
			 maturityDate = util.isNullOrEmpty(maturityDate) ? reportingDate : maturityDate;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
			 
			 usdAmount = rates.get("usd").getAsString();
			 tzsAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 			  
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			 
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterBorrower, gradesUnratedBorrower, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterBorrower", "gradesUnratedBorrower", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outwardBillsData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "outwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
				 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in inwardBills_IMDOC_POA :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
		
	/**** E01 to E15 ***/
	
	public JsonObject LoanInformation_DTP(String INFO1, String INFO2, String INFO3) //RTS019  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 //String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String subprodcode = "";
			 
			 if(data.has("subProduct"))
			 {
				 JsonObject subProduct = data.get("subProduct").getAsJsonObject();  
				 
				 if(subProduct.has("code")) 
				 {
					 subprodcode = subProduct.get("code").getAsString();
				 }
			 }
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 String Leid = "";
			 String borrowerCountry_1 = ""; 
			 String crRatingBorrower_1 = "";
			 
			 JsonArray partyDetails = data.get("transactionDetails").getAsJsonObject().get("partyDetails").getAsJsonArray();
			 
			 for(int i=0; i<partyDetails.size(); i++)
			 {
				 JsonObject js = partyDetails.get(i).getAsJsonObject();
				 JsonObject party = js.get("party").getAsJsonObject();
				 String id = party.get("id").getAsString();
				 JsonObject address = js.get("address").getAsJsonObject();
				 String countryCode = address.get("countryCode").getAsString();

				 if(id.equalsIgnoreCase("CUST"))
				 {
					 Leid= js.get("leid").getAsString();
					 borrowerCountry_1 = countryCode;
					 crRatingBorrower_1 = js.get("partyRiskGrade").getAsString();
					
				 }
			 }
			 
			 String accountNo_1 = "";
			 
			 JsonArray invoiceDetails = transactionDetails.get("invoiceDetails").getAsJsonArray();
			 
			 for(int j = 0; j < invoiceDetails.size(); j++) 
			 {
				JsonObject js1 = invoiceDetails.get(j).getAsJsonObject();
				JsonObject invoicePartyCode = js1.get("invoicePartyCode").getAsJsonObject();
				String invoice_code = invoicePartyCode.get("code").getAsString();
				
				if(invoice_code.equalsIgnoreCase("CUST")||invoice_code.equalsIgnoreCase("CST"))
				{
					 accountNo_1 = js1.get("accountNo").getAsString();
				}
			 }
			
			 String loanNumber_1 = "";
			 String loanType_1 = "";
			 String currency_1 = "";
			 String contractDate_1 = "";
			 String orgSanctionAmount_1 = "";
			 String maturityDate_1 = "";
			 String realEndDate_1 = "";
			 String orgOutstandingPrincipalAmount_1 = "";
			 String primeLendingRate_1 = "";
			 String interestPricingMethod_1  = "";
			 String annualInterestRate_1 = "";
			 String restructuringDate_1 = "";
			 String pastDueDays_1 = "";
			 String pastDueAmount_1 = "";
			 String orgAccruedInterestAmount_1 = "";
			 String orgPenaltyChargedAmount_1 = "";
			 String orgPenaltyPaidAmount_1 = "";
			 String orgLoanFeesPaidAmount_1 = "";
			 String orgSuspendedInterest_1 = "";
			 JsonArray financeDetails = transactionDetails.get("financeDetails").getAsJsonArray();
			 
			 for (int k = 0; k < financeDetails.size(); k++) 
			 {
				JsonObject js3 = financeDetails.get(k).getAsJsonObject();
				String financeNo = js3.get("financeNo").getAsString();
				loanNumber_1 = dealReferenceNo.concat(financeNo);
				loanType_1 = js3.get("financeType").getAsString();
				JsonObject financeAmount = js3.get("financeAmount").getAsJsonObject();
				currency_1 = financeAmount.get("curr").getAsString();
				contractDate_1 = financeAmount.get("financeEffectiveDate").getAsString();
				orgSanctionAmount_1 = financeAmount.get("amount").getAsString();
				maturityDate_1 = financeAmount.get("financeMaturityDate").getAsString();
				realEndDate_1 = js3.get("financeEventDate").getAsString();
				orgOutstandingPrincipalAmount_1 = financeAmount.get("balAmount").getAsString();
				
				JsonObject interestDetails = js3.get("interestDetails").getAsJsonObject();
				primeLendingRate_1 = interestDetails.get("fundRate").getAsString();
				annualInterestRate_1 = interestDetails.get("totalIntRate").getAsString();
				
				JsonObject interestAmount = js3.has("interestAmount") ? js3.get("interestAmount").getAsJsonObject() : new JsonObject();
				orgAccruedInterestAmount_1 = interestAmount.has("revBal") ? interestAmount.get("revBal").getAsString() : "0";
				
				orgSuspendedInterest_1 = interestAmount.has("iisBal") ? interestAmount.get("iisBal").getAsString() : "0";
				
				JsonObject paymentDetails = js3.has("paymentDetails") ? js3.get("paymentDetails").getAsJsonObject() : new JsonObject();
				orgPenaltyPaidAmount_1 = paymentDetails.has("pdInterestPaymentAmount") ? paymentDetails.get("pdInterestPaymentAmount").getAsString() : "0";
				
				interestPricingMethod_1 = financeAmount.get("financeIntType").getAsString();
				
				orgPenaltyChargedAmount_1 = financeAmount.get("penaltyAmount").getAsString();
				
				orgLoanFeesPaidAmount_1 = financeAmount.get("totalPaidAmount").getAsString();
				
				if(js3.has("extensionDetails"))
				{
					JsonObject extensionDetails =  js3.get("extensionDetails").getAsJsonObject();
					
					restructuringDate_1 = extensionDetails.get("newDate").getAsString();
					
					restructuringDate_1 = util.Convert_BOT_Date_Format(restructuringDate_1, "yyyy-MM-dd");
				}
				else
				{
					restructuringDate_1 = "<null>";
				}
				
				pastDueDays_1 = financeAmount.get("pastDueDays").getAsString();
				pastDueAmount_1 = financeAmount.get("pastdueAmount").getAsString();
			}
			 
			String assetClassification = "";
			 
			if(!util.isNullOrEmpty(pastDueDays_1))
			{
				 int interval = Integer.parseInt(pastDueDays_1);
				 
				 if(interval <= 0 && interval <= 30)
				 {
					 assetClassification = "1";
				 }
				 else if(interval >= 31 && interval <= 90)
				 {
					 assetClassification = "2";
				 }
				 else if(interval >= 91 && interval <= 180)
				 {
					 assetClassification = "3";
				 }
				 else if(interval >= 181 && interval <= 360)
				 {
					 assetClassification = "4";
				 }
				 else 
				 {
					 assetClassification = "5";
				 }
			}
			
			String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { Leid, "TradeLMTID" }, new Lookup001_mapper());
			 
			String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			
			BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			
			String reportingDate = util.getCurrentReportDate();
			String customerIdentificationNumber = Leid;
			String clientName = data.get("customerName").getAsString();
			String accountNumber = accountNo_1;
			String borrowerCountry = borrowerCountry_1;  
			boolean ratingStatus = true;
			String crRatingBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "BANK_RATING", crRatingBorrower_1); //cc;
			String gradesUnratedBanks = ""; 
			String categoryBorrower = "1"; //bank said to stamp as 1
			String gender =  "3";  //bank said to stamp as 3 - not applicable
			String disability = "1";   //not applicable
			String clientType = "7";   //bank said to stamp as 7 - corporates
			String clientSubType = "";   //bank said to stamp as 7 - corporates,but it didn't have subtype.
			String groupName = "NA"; //not applicable
			String groupCode = "NA"; //not applicable
			String relatedParty = "9";  //bank said to stamp as 9 - unrelated
			String relationshipCategory = "1";  //bank said to stamp as 1 - direct
			String loanNumber = loanNumber_1; 
			String loanType = "1"; //  bank said to stamp as 1
			String loanEconomicActivity = ST.FindElementFromFileIT("SCI", "LOANECONOMICACTIVITY", "LEID", Leid); //cc;
			String loanPhase = "1"; //bank said to stamp as 1 - Existing
			String transferStatus =  "2";  //bank said to stamp as 2 - not transfer
			String purposeMortgage = ""; //not applicable
			String purposeOtherLoans = "";  //bank said to stamp as 3 - Business
			String sourceFundMortgage = "";  //not applicable
			String amortizationType = "2"; //bank said to stamp as 2 - bullet loan
			String branchCode = "008300"; //"005083"; //bank said to stamp as 005083
			String loanOfficer = ST.FindElementFromFileIT("SCI", "LOANOFFICER", "LEID", Leid); //Refer to Client Journey based on SCI LEID
			String loanSupervisor = "NA";  // not availble in CC
			String groupVillageNumber = "NA";  //not applicable
			String cycleNumber = "0";  //not applicable  
			String loanInstallment = "1";  //bank said to stamp as 1 - bullet
			String repaymentFrequency = "1"; //bank said to stamp as 1 - bullet
			String currency = currency_1;
			String contractDate = util.Convert_BOT_Date_Format(contractDate_1, "yyyy-MM-dd");
			String orgSanctionAmount = orgSanctionAmount_1 ;
			String usdSanctionAmount = "0"; //vendor need to map based on orgSanctionAmount
			String tzsSanctionAmount = "0"; //vendor need to map based on orgSanctionAmount
			String orgDisbursedAmount = orgSanctionAmount_1;
			String usdDisbursedAmount = "0"; //vendor need to map based on orgDisbursedAmount
			String tzsDisbursedAmount = "0"; //vendor need to map based on orgDisbursedAmount
			String disbursementDate = util.Convert_BOT_Date_Format(contractDate_1, "yyyy-MM-dd");
			String maturityDate = util.Convert_BOT_Date_Format(maturityDate_1, "yyyy-MM-dd");
			String realEndDate = util.isNullOrEmpty(realEndDate_1) ? "<null>" : util.Convert_BOT_Date_Format(realEndDate_1, "yyyy-MM-dd");
			String orgOutstandingPrincipalAmount = orgOutstandingPrincipalAmount_1;
			String usdOutstandingPrincipalAmount = "0"; //vendor need to map based on orgOutstandingPrincipalAmount
			String tzsOutstandingPrincipalAmount = "0"; //vendor need to map based on orgOutstandingPrincipalAmount
			String orgInstallmentAmount = orgSanctionAmount_1;
			String usdInstallmentAmount = "0"; //vendor need to map based on orgInstallmentAmount
			String tzsInstallmentAmount = "0"; //vendor need to map based on orgInstallmentAmount
			String loanInstallmentPaid = "1"; //bank said to stamp as 1
			String gracePeriodPaymentPrincipal = "";  //not applicable
			String primeLendingRate = primeLendingRate_1;
			String annualInterestRate = annualInterestRate_1;
			String annualEffectiveInterestRate = annualInterestRate_1;
			String firstInstallmentPaymentDate = util.Convert_BOT_Date_Format(maturityDate_1, "yyyy-MM-dd");
			String lastPaymentDate = util.Convert_BOT_Date_Format(maturityDate_1, "yyyy-MM-dd");
			String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", Leid+"|"+LimitID); //Refer to Client Journey based on SCI LEID
			String orgCollateralValue =  ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT",  "LEID|LIMITID", Leid+"|"+LimitID); //Refer to Client Journey based on SCI LEID
			String usdCollateralValue = "0"; 
			String tzsCollateralValue = "0"; 
			String loanFlagType = "2"; //bank said to stamp as 2 -  not restructured
			String restructuringDate = "<null>"; //restructuringDate_1;
			String pastDueDays = util.isNullOrEmpty(pastDueDays_1) ? "0" : Integer.parseInt(pastDueDays_1)+"" ;
			String pastDueAmount = pastDueAmount_1;  
			String internalRiskGroup = ST.FindElementFromFileIT("SCI", "RISKGROUP", "LEID", Leid); //Refer to Client Journey based on SCI LEID
			String orgAccruedInterestAmount = orgAccruedInterestAmount_1;
			String usdAccruedInterestAmount = "0"; //vendor need to map based on orgAccruedInterestAmount
			String tzsAccruedInterestAmount = "0"; //vendor need to map based on orgAccruedInterestAmount
			String orgPenaltyChargedAmount = orgPenaltyChargedAmount_1;
			String usdPenaltyChargedAmount = "0";  //vendor need to map based on orgPenaltyChargedAmount
			String tzsPenaltyChargedAmount = "0";  //vendor need to map based on orgPenaltyChargedAmount
			String orgPenaltyPaidAmount = orgPenaltyPaidAmount_1;
			String usdPenaltyPaidAmount = "0";  //vendor need to map based on orgPenaltyPaidAmount
			String tzsPenaltyPaidAmount = "0";  //vendor need to map based on orgPenaltyPaidAmount
			String orgLoanFeesChargedAmount = (Double.parseDouble(orgSanctionAmount_1) + Double.parseDouble(orgAccruedInterestAmount_1))+""; //financeAmount/ amount + interestAmount/ revBal
			String usdLoanFeesChargedAmount = "0"; //vendor need to map based on orgLoanFeesChargedAmount
			String tzsLoanFeesChargedAmount = "0"; //vendor need to map based on orgLoanFeesChargedAmount
			String orgLoanFeesPaidAmount = orgLoanFeesPaidAmount_1;
			String usdLoanFeesPaidAmount = "0"; //vendor need to map based on orgLoanFeesPaidAmount
			String tzsLoanFeesPaidAmount = "0"; //vendor need to map based on orgLoanFeesPaidAmount
			String orgTotMonthlyPaymentAmount = orgOutstandingPrincipalAmount_1;
			String usdTotMonthlyPaymentAmount = "0"; //vendor need to map based on orgTotMonthlyPaymentAmount
			String tzsTotMonthlyPaymentAmount = "0"; //vendor need to map based on orgTotMonthlyPaymentAmount
			String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", Leid); //Refer to Client Journey based on SCI LEID
			String assetClassificationCategory = assetClassification;
			String negStatusContract = "1"; //bank said to stamp as 1  - no negative
			String customerRole = "1"; //bank said to stamp as 1  - main debtor
			String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(dealReferenceNo+product_code+step_code); 
			String tradingIntent = "2"; //bank said to stamp as 2 - held to maturity
			String interestPricingMethod = interestPricingMethod_1;  
			String orgSuspendedInterest = orgSuspendedInterest_1; 
			String usdSuspendedInterest = "0";  
			String tzsSuspendedInterest = "0"; 
			String botProvision = "0"; //bank said to stamp as 0
			
			collateralPledged = util.isNullOrEmpty(collateralPledged) ? "2" : collateralPledged;
			
			FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgSanctionAmount, currency);
			 
			usdSanctionAmount = rates.get("usd").getAsString();
			tzsSanctionAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgDisbursedAmount, currency);
			 
			usdDisbursedAmount = rates.get("usd").getAsString();
			tzsDisbursedAmount = rates.get("tzs").getAsString();  
			 
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingPrincipalAmount, currency);
			 
			usdOutstandingPrincipalAmount = rates.get("usd").getAsString();
			tzsOutstandingPrincipalAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgInstallmentAmount, currency);
			 
			usdInstallmentAmount = rates.get("usd").getAsString();
			tzsInstallmentAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgCollateralValue, currency);
			 
			usdCollateralValue = rates.get("usd").getAsString();
			tzsCollateralValue = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgAccruedInterestAmount, currency);
			 
			usdAccruedInterestAmount = rates.get("usd").getAsString();
			tzsAccruedInterestAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgPenaltyChargedAmount, currency);
			 
			usdPenaltyChargedAmount = rates.get("usd").getAsString();
			tzsPenaltyChargedAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgPenaltyPaidAmount, currency);
			 
			usdPenaltyPaidAmount = rates.get("usd").getAsString();
			tzsPenaltyPaidAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgLoanFeesChargedAmount, currency);
			 
			usdLoanFeesChargedAmount = rates.get("usd").getAsString();
			tzsLoanFeesChargedAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgLoanFeesPaidAmount, currency);
			 
			usdLoanFeesPaidAmount = rates.get("usd").getAsString();
			tzsLoanFeesPaidAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgTotMonthlyPaymentAmount, currency);
			 
			usdTotMonthlyPaymentAmount = rates.get("usd").getAsString();
			tzsTotMonthlyPaymentAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgSuspendedInterest, currency);
			 
			usdSuspendedInterest = rates.get("usd").getAsString();
			tzsSuspendedInterest = rates.get("tzs").getAsString();
			
			sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", borrowerCountry }, new Lookup001_mapper());
				
			borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			
			int count = 1;
			
			sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
			 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
			 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
			 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
			 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
			 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
			 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
			 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
			 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanInformation", count, reportingDate, customerIdentificationNumber, accountNumber, clientName, borrowerCountry, ratingStatus, crRatingBorrower, gradesUnratedBanks, categoryBorrower, gender,
					 disability, clientType, clientSubType, groupName, groupCode, relatedParty, relationshipCategory, loanNumber, loanType, loanEconomicActivity,  
					 loanPhase, transferStatus, purposeMortgage, purposeOtherLoans, sourceFundMortgage, amortizationType, branchCode, loanOfficer, loanSupervisor, groupVillageNumber,
					 cycleNumber, loanInstallment, repaymentFrequency, currency, contractDate, orgSanctionAmount, usdSanctionAmount, tzsSanctionAmount, orgDisbursedAmount, usdDisbursedAmount,
					 tzsDisbursedAmount, disbursementDate, maturityDate, realEndDate, orgOutstandingPrincipalAmount, usdOutstandingPrincipalAmount, tzsOutstandingPrincipalAmount,orgInstallmentAmount, usdInstallmentAmount, tzsInstallmentAmount,
					 loanInstallmentPaid, gracePeriodPaymentPrincipal, primeLendingRate, interestPricingMethod, annualInterestRate, annualEffectiveInterestRate, firstInstallmentPaymentDate, lastPaymentDate, loanFlagType,restructuringDate,pastDueDays,pastDueAmount,internalRiskGroup,orgAccruedInterestAmount,usdAccruedInterestAmount,tzsAccruedInterestAmount,
					 orgPenaltyChargedAmount,usdPenaltyChargedAmount,tzsPenaltyChargedAmount,orgPenaltyPaidAmount,usdPenaltyPaidAmount,tzsPenaltyPaidAmount,orgLoanFeesChargedAmount,usdLoanFeesChargedAmount,tzsLoanFeesChargedAmount,orgLoanFeesPaidAmount,
					 usdLoanFeesPaidAmount,tzsLoanFeesPaidAmount,orgTotMonthlyPaymentAmount,usdTotMonthlyPaymentAmount,tzsTotMonthlyPaymentAmount,sectorSnaClassification,assetClassificationCategory,negStatusContract,customerRole,allowanceProbableLoss,
					 botProvision,tradingIntent,orgSuspendedInterest,usdSuspendedInterest,tzsSuspendedInterest });
		  
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
				 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
				 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
				 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
				 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
				 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
				 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
				 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 		
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanInformation", "serial", "reportingDate", "customerIdentificationNumber", "accountNumber", "clientName", "borrowerCountry", "ratingStatus", "crRatingBorrower", "gradesUnratedBanks", "categoryBorrower", "gender",
					 "disability", "clientType", "clientSubType", "groupName", "groupCode", "relatedParty", "relationshipCategory", "loanNumber", "loanType", "loanEconomicActivity",
					 "loanPhase", "transferStatus", "purposeMortgage", "purposeOtherLoans", "sourceFundMortgage", "amortizationType", "branchCode", "loanOfficer", "loanSupervisor", "groupVillageNumber",
					 "cycleNumber", "loanInstallment", "repaymentFrequency", "currency", "contractDate", "orgSanctionAmount", "usdSanctionAmount", "tzsSanctionAmount", "orgDisbursedAmount", "usdDisbursedAmount",
					 "tzsDisbursedAmount", "disbursementDate", "maturityDate", "realEndDate", "orgOutstandingPrincipalAmount", "usdOutstandingPrincipalAmount", "tzsOutstandingPrincipalAmount","orgInstallmentAmount", "usdInstallmentAmount", "tzsInstallmentAmount",
					 "loanInstallmentPaid", "gracePeriodPaymentPrincipal", "primeLendingRate", "interestPricingMethod", "annualInterestRate", "annualEffectiveInterestRate", "firstInstallmentPaymentDate", "lastPaymentDate", "loanFlagType","restructuringDate","pastDueDays","pastDueAmount","internalRiskGroup","orgAccruedInterestAmount","usdAccruedInterestAmount","tzsAccruedInterestAmount",
					 "orgPenaltyChargedAmount","usdPenaltyChargedAmount","tzsPenaltyChargedAmount","orgPenaltyPaidAmount","usdPenaltyPaidAmount","tzsPenaltyPaidAmount","orgLoanFeesChargedAmount","usdLoanFeesChargedAmount","tzsLoanFeesChargedAmount","orgLoanFeesPaidAmount",
					 "usdLoanFeesPaidAmount","tzsLoanFeesPaidAmount","orgTotMonthlyPaymentAmount","usdTotMonthlyPaymentAmount","tzsTotMonthlyPaymentAmount","sectorSnaClassification","assetClassificationCategory","negStatusContract","customerRole","allowanceProbableLoss",
					 "botProvision","tradingIntent","orgSuspendedInterest","usdSuspendedInterest","tzsSuspendedInterest" });
		
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "collateralPledged", "serial" , "reportingDate", "customerIdentificationNumber", "accountNumber",
				 		"collateralPledged", "orgCollateralValue", "usdCollateralValue", "tzsCollateralValue" }); 
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", 1 , reportingDate, customerIdentificationNumber,  accountNumber, 
				 		collateralPledged, orgCollateralValue, usdCollateralValue, tzsCollateralValue }); 
			 			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanInformation", "1"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged", "2"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		     
		     StoreLoanLog(SUBORGCODE, INFO1, "TRADE", loanNumber, Batch_id, O_SERIAL);
		     
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
				
			 details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in LoanInformation_DTP :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject LoanTransaction_DTP(String INFO1, String INFO2, String INFO3) //RTS191
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 //String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String subprodcode = "";
			 
			 if(data.has("subProduct"))
			 {
				 JsonObject subProduct = data.get("subProduct").getAsJsonObject();  
				 
				 if(subProduct.has("code")) 
				 {
					 subprodcode = subProduct.get("code").getAsString();
				 }
			 }
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject(); 
			 			 
			 JsonArray financeDetails = transactionDetails.get("financeDetails").getAsJsonArray();
			 
			 String loanNumber_1 = "", financeEventDate_1 = "", financeEventType = "", curr="", amount="";
			 
			 if(financeDetails.size() > 0)
			 {
				 JsonObject financeDetails_data = financeDetails.get(0).getAsJsonObject();
				 
				 String financeNo = financeDetails_data.get("financeNo").getAsString();
				 
				 loanNumber_1 = dealReferenceNo + financeNo;
				 
				 financeEventDate_1 = financeDetails_data.get("financeEventDate").getAsString();
				 
				 financeEventType = financeDetails_data.get("financeEventType").getAsString();
				 
				 if(financeDetails_data.has("financeAmount"))
				 {
					 JsonObject financeAmount = financeDetails_data.get("financeAmount").getAsJsonObject();
					 
					 curr = financeAmount.get("curr").getAsString();
					 amount = financeAmount.get("amount").getAsString();
				 }
			 }
			 
			 String loanTransactiontype = "", loanTransactionsubType = ""; 
			 
			 if(financeEventType.equals("E00") || financeEventType.equals("E01")) 
			 {
				 loanTransactiontype = "1";
				 loanTransactionsubType = "1";
			 }
			 else if(financeEventType.equals("E02") || financeEventType.equals("E15")) 
			 {
				 loanTransactiontype = "1";
				 loanTransactionsubType = "3";
			 }
			 else if(financeEventType.equals("E03")) 
			 {
				 loanTransactiontype = "1";
				 loanTransactionsubType = "2";
			 }
			 else if(financeEventType.equals("E04")) 
			 {
				 loanTransactiontype = "1";
				 loanTransactionsubType = "3";
			 }
			 else if(financeEventType.equals("E06")) 
			 {
				 loanTransactiontype = "2";
				 loanTransactionsubType = "";
			 }
			 else if(financeEventType.equals("E07")) 
			 {
				 loanTransactiontype = "1";
				 loanTransactionsubType = "1";
			 }
			 else if(financeEventType.equals("E14")) 
			 {
				 loanTransactiontype = "1";
				 loanTransactionsubType = "3";
			 }
			 
			 financeEventDate_1 = util.Convert_BOT_Date_Format(financeEventDate_1, "yyyy-MM-dd");
			 
			 String reportingDate = util.getCurrentReportDate();
			 String loanNumber = loanNumber_1;
			 String transactionDate = financeEventDate_1;
			 String loanTransactionType =  loanTransactiontype;
			 String loanTransactionSubType = loanTransactionsubType;
			 String currency = curr;
			 String orgTransactionAmount = amount;
			 String usdTransactionAmount = "0";  //Need to convert it by using Splice system 
			 String tzsTransactionAmount = "0";  //Need to convert it by using Splice system
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			 currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";
			
			// sql = "insert into TRADE001(suborgcode,syscode,chcode,paytype,reqdate,reqtime,evntime,parentdeal,notificationid,dealrefNo,stepcode,prodcode,subprodcode,message,status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			// Jdbctemplate.update(sql, new Object[] { SUBORGCODE, processingSystemCode, requestorSystemCode, "RTSIS", CurrentDate, timestamp, evntime, parentdeal, notificationID, dealReferenceNo, step_code, product_code, subprodcode, INFO3.getBytes(), "PENDING"});
			
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanTransactionInformation", count, reportingDate, loanNumber, transactionDate, loanTransactionType, loanTransactionSubType,
					currency, orgTransactionAmount,  usdTransactionAmount, tzsTransactionAmount });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanTransactionInformation", "serial", "reportingDate", "loanNumber", "transactionDate", "loanTransactionType", "loanTransactionSubType",
						"currency", "orgTransactionAmount",  "usdTransactionAmount", "tzsTransactionAmount" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanTransactionInformation"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanTransactionInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	/******** OTP **************/
	
	public JsonObject LoanInformation_OTP(String INFO1, String INFO2, String INFO3) //RTS019  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			 
			 JsonObject generalDetails = transactionDetails.get("generalDetails").getAsJsonObject();
			 
			 String txnApprovalTime = generalDetails.get("txnApprovalTime").getAsString();
			 
			 String customerLEId = transactionDetails.get("customerLEId").getAsString();
			 
			 String customerName = transactionDetails.get("customerName").getAsString();
			 
			 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
			 
			 JsonObject partyDetails1 = partyDetails.get(0).getAsJsonObject();
			 
			 JsonObject address = partyDetails1.get("address").getAsJsonObject();
			 
			 String countryCode = address.get("countryCode").getAsString();
			 
			 JsonObject product = generalDetails.get("product").getAsJsonObject();
			 
			 String desc = product.get("desc").getAsString();
			 
			 JsonObject financeRequest = transactionDetails.get("financeRequest").getAsJsonObject();
			 
			 JsonArray financeDetails = financeRequest.get("financeDetails").getAsJsonArray();
			 
			 JsonObject finance = financeDetails.size() > 0 ? financeDetails.get(0).getAsJsonObject() : new JsonObject();
			 
			 String finEffectiveDate = "";
			 
			 if(finance.has("finEffectiveDate")) {
				 
				 finEffectiveDate = finance.get("finEffectiveDate").getAsString();
			 }
			 
			 JsonObject finStatus = finance.has("finStatus") ? finance.get("finStatus").getAsJsonObject() : new JsonObject();
			 
			 String fin_code = finStatus.get("code").getAsString();
			 
			 if(fin_code.equals("02") || fin_code.equals("2") || fin_code.equals("04") || fin_code.equals("4")) {
				 
				 fin_code = txnApprovalTime ;
				 
			 }else {
				 
				 fin_code = "";	 
			 }
			 
			 String dealRefNo = financeRequest.get("dealRefNo").getAsString();
			 
			 String dealRefNo_trunc = dealRefNo.substring(0, dealRefNo.length() - 5);
			 
			 String docFinId = finance.has("docFinId") ? finance.get("docFinId").getAsString() : "";
			 
			 String docFinNo = finance.has("docFinNo") ? finance.get("docFinNo").getAsString() : "";  //financeDetails1.get("docFinNo").getAsString();
			 
			 String finCcy = finance.has("finCcy") ? finance.get("finCcy").getAsString() : "";  //financeDetails1.get("finCcy").getAsString();
			 
			 String finAmt = finance.has("finAmt") ? finance.get("finAmt").getAsString() : "";  //financeDetails1.get("finAmt").getAsString();
			 
			 JsonArray accountingDetails = transactionDetails.get("accountingDetails").getAsJsonArray();
			 
			 JsonObject accountingDetails1 = accountingDetails.get(0).getAsJsonObject();
			 
			 String accNumber = accountingDetails1.get("accNumber").getAsString();
			 
			 String autodebit = (finance.has("autodebit") && !finance.get("autodebit").isJsonNull()) ? finance.get("autodebit").getAsString() : "";
			 
			 if(autodebit.equalsIgnoreCase("Y") && finance.has("autodebitPrincipalAcct")) {
				 
				 autodebit = finance.get("autodebitPrincipalAcct").getAsString();
				 
			 }else {
				 
				 autodebit = accNumber;
			 }
			 
			 String maturity_Date = finance.get("maturityDate").getAsString();
			 
			 int past_due_days = 0 ;
			 
			 if(fin_code == "05") {
				 
				 String sql = "SELECT TO_DATE(?, 'YYYY-MM-DD') - TO_DATE(?, 'YYYY-MM-DD') AS days FROM DUAL";
				 			 
				 String days = Jdbctemplate.queryForObject(sql, new Object[]{maturity_Date, txnApprovalTime}, String.class);
				 
				 past_due_days = Integer.parseInt(days);
				 
				 if( past_due_days < 0) {
					
					 past_due_days = 0;
				 
				 }else {
					 
					 past_due_days = Integer.parseInt(days);
					 
				 }
			 }
			 
			 String finOSAmt = finance.get("finOSAmt").getAsString();
			 
			 String past_amount ="";
			 
			 if(fin_code == "05") {
				 
				 past_amount = finOSAmt;
			
			 }else {
				 
				 past_amount = "0" ;
				 
			 }
			 
			 JsonArray interestDetails = finance.get("interestDetails").getAsJsonArray();
			 
			 JsonObject interestDetails1 = interestDetails.get(0).getAsJsonObject();
			 
			 String floatingIntRate = interestDetails1.get("floatingIntRate").getAsString();
			 
			 if(floatingIntRate.equalsIgnoreCase("N")) {
				
				 floatingIntRate = "2";
			 
			 }else if(floatingIntRate.equalsIgnoreCase("Y")) {
				
				 floatingIntRate = "1";
			 }
			 
			 JsonObject whenToCollect = interestDetails1.get("whenToCollect").getAsJsonObject();
			 
			 String whenToCollect_code = whenToCollect.get("code").getAsString();
			 
			 String Amount ="";
			 
			 if(whenToCollect_code.contains("LATR")) {
				 
				 Amount = "0";
				 
			 }else if(interestDetails1.has("intAmt")){
				 
				 Amount = interestDetails1.get("intAmt").getAsString();
				 
			 }
			 
			 String OrgLoanFeesPaid = "";
			 
			 if(interestDetails1.has("intAmt")) {
				 
				 OrgLoanFeesPaid = interestDetails1.get("intAmt").getAsString();
				 
			 }
			 
			 String baseRate = interestDetails1.get("baseRate").getAsString();
			 
			 String intRate = interestDetails1.get("intRate").getAsString();
			 
			 JsonObject intType = interestDetails1.get("intType").getAsJsonObject();
			 
			 String intType_code = intType.get("code").getAsString();
			 
			 String OrgPenalty = "";
			 
			 if(intType_code.contains("DIN")&& interestDetails1.has("intAmt")) {
				 
				 OrgPenalty = interestDetails1.get("intAmt").getAsString();
				 
			 }
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			//String customerIdentificationNumber_1 = data.get("leid").getAsString();
			//String clientName_1 = data.get("customerName").getAsString();
			
			String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { customerLEId, "TradeLMTID" }, new Lookup001_mapper());
			 
			String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			
			BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			
			String reportingDate = util.Convert_BOT_Date_Format(txnApprovalTime, "yyyy-MM-dd");
			String customerIdentificationNumber = customerLEId;
			String clientName = customerName;
			String accountNumber = autodebit;  
			String borrowerCountry = countryCode;  
			boolean ratingStatus = true;
			String crRatingBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "BANK_RATING", customerLEId); //cc;
			String gradesUnratedBanks = ""; //not applicable
			String categoryBorrower = "1";
			String gender =  "3"; //not applicable
			String disability = "1";  
			String clientType = "7"; 
			String clientSubType = "7";  
			String groupName = "";  //not applicable
			String groupCode = "";  //not applicable
			String relatedParty = "9";  
			String relationshipCategory = "1";
			String loanNumber = dealRefNo_trunc + docFinId + "-" + docFinNo ; 
			String loanType = "1";  
			String loanEconomicActivity = ST.FindElementFromFileIT("SCI", "LOANECONOMICACTIVITY", "LEID", customerLEId); //cc; 
			String loanPhase = "1";
			String transferStatus =  "2";  
			String purposeMortgage = "";  //not applicable
			String purposeOtherLoans = "3";  
			String sourceFundMortgage = "";  //not applicable
			String amortizationType = "2";
			String branchCode = "008300"; //"005083"; 
			String loanOfficer = ST.FindElementFromFileIT("SCI", "LOANOFFICER", "LEID", customerLEId);	
			String loanSupervisor = "NA";  
			String groupVillageNumber = "";//not applicable 
			String cycleNumber = "0";//not applicable
			String loanInstallment = "1";   //numeric field
			String repaymentFrequency = "1";
			String currency = finCcy;
			String contractDate = util.isNullOrEmpty(finEffectiveDate) ? "<null>" : util.Convert_BOT_Date_Format(finEffectiveDate, "yyyy-MM-dd");   
			String orgSanctionAmount = finAmt ; 
			String usdSanctionAmount = "0"; // to convert if currency is not USD
			String tzsSanctionAmount = "0";	// 
			String orgDisbursedAmount = finAmt; 
			String usdDisbursedAmount = "0";    // to convert if currency is not USD
			String tzsDisbursedAmount = "0";   // 
			String disbursementDate = util.isNullOrEmpty(finEffectiveDate) ? "<null>" : util.Convert_BOT_Date_Format(finEffectiveDate, "yyyy-MM-dd"); 
			String maturityDate = util.Convert_BOT_Date_Format(maturity_Date, "yyyy-MM-dd");
			String realEndDate = util.isNullOrEmpty(fin_code) ? "<null>" : util.Convert_BOT_Date_Format(fin_code, "yyyy-MM-dd");  
			String orgOutstandingPrincipalAmount = finOSAmt;
			String usdOutstandingPrincipalAmount = "0";  //to convert if currency is not USD
			String tzsOutstandingPrincipalAmount = "0";	
			String orgInstallmentAmount = finAmt; 
			String usdInstallmentAmount = "0";	//to convert if currency is not USD
			String tzsInstallmentAmount = "0";	
			String loanInstallmentPaid = "1";
			String gracePeriodPaymentPrincipal = ""; 	//not applicable
			String primeLendingRate = baseRate;
			String annualInterestRate = intRate;
			String annualEffectiveInterestRate = intRate;
			String firstInstallmentPaymentDate = util.Convert_BOT_Date_Format(maturity_Date, "yyyy-MM-dd");
			String lastPaymentDate = util.Convert_BOT_Date_Format(maturity_Date, "yyyy-MM-dd");
			String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", customerLEId+"|"+LimitID); 	
			String orgCollateralValue = ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT",  "LEID|LIMITID", customerLEId+"|"+LimitID);	
			String usdCollateralValue = "0";	
			String tzsCollateralValue = "0";	
			String loanFlagType = "2";
			String restructuringDate = "<null>";  //util.Convert_BOT_Date_Format(maturity_Date, "yyyy-MM-dd"); 
			String pastDueDays = Integer.toString(past_due_days);
			String pastDueAmount = past_amount;  
			String internalRiskGroup = ST.FindElementFromFileIT("SCI", "RISKGROUP", "LEID", customerLEId); //cc
			String orgAccruedInterestAmount = Amount;  
			String usdAccruedInterestAmount = "0";	
			String tzsAccruedInterestAmount = "0";	
			String orgPenaltyChargedAmount = "0";	
			String usdPenaltyChargedAmount = "0";	
			String tzsPenaltyChargedAmount = "0";	
			String orgPenaltyPaidAmount = "0"; 	//to ask
			String usdPenaltyPaidAmount = "0";  
			String tzsPenaltyPaidAmount = "0";
			String orgLoanFeesChargedAmount = Amount;	
			String usdLoanFeesChargedAmount = "0";
			String tzsLoanFeesChargedAmount = "0";
			String orgLoanFeesPaidAmount = OrgLoanFeesPaid;	 
			String usdLoanFeesPaidAmount = "0";
			String tzsLoanFeesPaidAmount = "0";
			String orgTotMonthlyPaymentAmount = "0";
			String usdTotMonthlyPaymentAmount = "0";
			String tzsTotMonthlyPaymentAmount = "0"; 
			String sectorSnaClassification = "12";  
			String assetClassificationCategory = "1"; 
			String negStatusContract = "1"; 
			String customerRole = "1"; 
			String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(docFinId + "-" + docFinNo); 
			String botProvision = "0";
			String tradingIntent = "2"; 
			String interestPricingMethod = floatingIntRate; 
			String orgSuspendedInterest = "0"; 
			String usdSuspendedInterest = "0"; 
			String tzsSuspendedInterest = "0"; 
			
			collateralPledged = util.isNullOrEmpty(collateralPledged) ? "2" : collateralPledged;
			
			FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgSanctionAmount, currency);
			 
			usdSanctionAmount = rates.get("usd").getAsString();
			tzsSanctionAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgDisbursedAmount, currency);
			 
			usdDisbursedAmount = rates.get("usd").getAsString();
			tzsDisbursedAmount = rates.get("tzs").getAsString();  
			 
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingPrincipalAmount, currency);
			 
			usdOutstandingPrincipalAmount = rates.get("usd").getAsString();
			tzsOutstandingPrincipalAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgInstallmentAmount, currency);
			 
			usdInstallmentAmount = rates.get("usd").getAsString();
			tzsInstallmentAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgCollateralValue, currency);
			 
			usdCollateralValue = rates.get("usd").getAsString();
			tzsCollateralValue = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgAccruedInterestAmount, currency);
			 
			usdAccruedInterestAmount = rates.get("usd").getAsString();
			tzsAccruedInterestAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgPenaltyChargedAmount, currency);
			 
			usdPenaltyChargedAmount = rates.get("usd").getAsString();
			tzsPenaltyChargedAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgPenaltyPaidAmount, currency);
			 
			usdPenaltyPaidAmount = rates.get("usd").getAsString();
			tzsPenaltyPaidAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgLoanFeesChargedAmount, currency);
			 
			usdLoanFeesChargedAmount = rates.get("usd").getAsString();
			tzsLoanFeesChargedAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgLoanFeesPaidAmount, currency);
			 
			usdLoanFeesPaidAmount = rates.get("usd").getAsString();
			tzsLoanFeesPaidAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgTotMonthlyPaymentAmount, currency);
			 
			usdTotMonthlyPaymentAmount = rates.get("usd").getAsString();
			tzsTotMonthlyPaymentAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgSuspendedInterest, currency);
			 
			usdSuspendedInterest = rates.get("usd").getAsString();
			tzsSuspendedInterest = rates.get("tzs").getAsString();
			
			sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", borrowerCountry }, new Lookup001_mapper());
				
			borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			
			int count = 1;
			
			sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
			 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
			 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
			 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
			 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
			 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
			 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
			 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
			 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanInformation", count, reportingDate, customerIdentificationNumber, accountNumber, clientName, borrowerCountry, ratingStatus, crRatingBorrower, gradesUnratedBanks, categoryBorrower, gender,
					 disability, clientType, clientSubType, groupName, groupCode, relatedParty, relationshipCategory, loanNumber, loanType, loanEconomicActivity,  
					 loanPhase, transferStatus, purposeMortgage, purposeOtherLoans, sourceFundMortgage, amortizationType, branchCode, loanOfficer, loanSupervisor, groupVillageNumber,
					 cycleNumber, loanInstallment, repaymentFrequency, currency, contractDate, orgSanctionAmount, usdSanctionAmount, tzsSanctionAmount, orgDisbursedAmount, usdDisbursedAmount,
					 tzsDisbursedAmount, disbursementDate, maturityDate, realEndDate, orgOutstandingPrincipalAmount, usdOutstandingPrincipalAmount, tzsOutstandingPrincipalAmount,orgInstallmentAmount, usdInstallmentAmount, tzsInstallmentAmount,
					 loanInstallmentPaid, gracePeriodPaymentPrincipal, primeLendingRate, interestPricingMethod, annualInterestRate, annualEffectiveInterestRate, firstInstallmentPaymentDate, lastPaymentDate, loanFlagType,restructuringDate,pastDueDays,pastDueAmount,internalRiskGroup,orgAccruedInterestAmount,usdAccruedInterestAmount,tzsAccruedInterestAmount,
					 orgPenaltyChargedAmount,usdPenaltyChargedAmount,tzsPenaltyChargedAmount,orgPenaltyPaidAmount,usdPenaltyPaidAmount,tzsPenaltyPaidAmount,orgLoanFeesChargedAmount,usdLoanFeesChargedAmount,tzsLoanFeesChargedAmount,orgLoanFeesPaidAmount,
					 usdLoanFeesPaidAmount,tzsLoanFeesPaidAmount,orgTotMonthlyPaymentAmount,usdTotMonthlyPaymentAmount,tzsTotMonthlyPaymentAmount,sectorSnaClassification,assetClassificationCategory,negStatusContract,customerRole,allowanceProbableLoss,
					 botProvision,tradingIntent,orgSuspendedInterest,usdSuspendedInterest,tzsSuspendedInterest });
		  
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
				 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
				 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
				 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
				 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
				 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
				 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
				 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 		
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanInformation", "serial", "reportingDate", "customerIdentificationNumber", "accountNumber", "clientName", "borrowerCountry", "ratingStatus", "crRatingBorrower", "gradesUnratedBanks", "categoryBorrower", "gender",
					 "disability", "clientType", "clientSubType", "groupName", "groupCode", "relatedParty", "relationshipCategory", "loanNumber", "loanType", "loanEconomicActivity",
					 "loanPhase", "transferStatus", "purposeMortgage", "purposeOtherLoans", "sourceFundMortgage", "amortizationType", "branchCode", "loanOfficer", "loanSupervisor", "groupVillageNumber",
					 "cycleNumber", "loanInstallment", "repaymentFrequency", "currency", "contractDate", "orgSanctionAmount", "usdSanctionAmount", "tzsSanctionAmount", "orgDisbursedAmount", "usdDisbursedAmount",
					 "tzsDisbursedAmount", "disbursementDate", "maturityDate", "realEndDate", "orgOutstandingPrincipalAmount", "usdOutstandingPrincipalAmount", "tzsOutstandingPrincipalAmount","orgInstallmentAmount", "usdInstallmentAmount", "tzsInstallmentAmount",
					 "loanInstallmentPaid", "gracePeriodPaymentPrincipal", "primeLendingRate", "interestPricingMethod", "annualInterestRate", "annualEffectiveInterestRate", "firstInstallmentPaymentDate", "lastPaymentDate", "loanFlagType","restructuringDate","pastDueDays","pastDueAmount","internalRiskGroup","orgAccruedInterestAmount","usdAccruedInterestAmount","tzsAccruedInterestAmount",
					 "orgPenaltyChargedAmount","usdPenaltyChargedAmount","tzsPenaltyChargedAmount","orgPenaltyPaidAmount","usdPenaltyPaidAmount","tzsPenaltyPaidAmount","orgLoanFeesChargedAmount","usdLoanFeesChargedAmount","tzsLoanFeesChargedAmount","orgLoanFeesPaidAmount",
					 "usdLoanFeesPaidAmount","tzsLoanFeesPaidAmount","orgTotMonthlyPaymentAmount","usdTotMonthlyPaymentAmount","tzsTotMonthlyPaymentAmount","sectorSnaClassification","assetClassificationCategory","negStatusContract","customerRole","allowanceProbableLoss",
					 "botProvision","tradingIntent","orgSuspendedInterest","usdSuspendedInterest","tzsSuspendedInterest" });
		
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "collateralPledged", "serial" , "reportingDate", "customerIdentificationNumber", "accountNumber",
				 		"collateralPledged", "orgCollateralValue", "usdCollateralValue", "tzsCollateralValue" }); 
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", 1 , reportingDate, customerIdentificationNumber,  accountNumber, 
				 		collateralPledged, orgCollateralValue, usdCollateralValue, tzsCollateralValue }); 
			 			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanInformation", "1"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged", "2"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		     
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
				
			 details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in LoanInformation :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject LoanTransaction_OTP(String INFO1, String INFO2, String INFO3) //RTS075
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String notificationID = data.get("notificationID").getAsString();
			 
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject(); 
			 
			 JsonObject generalDetails = transactionDetails.get("generalDetails").getAsJsonObject(); 
			 
			 JsonObject financeRequest = transactionDetails.get("financeRequest").getAsJsonObject();
			 
			 JsonArray financeDetails = financeRequest.get("financeDetails").getAsJsonArray();
			 
			 JsonObject financeDetails_data = financeDetails.get(0).getAsJsonObject();
			 
			 String dealRefNo = financeRequest.get("dealRefNo").getAsString();
			 
			 String docFinNo = financeDetails_data.get("docFinNo").getAsString();
			 String docFinId = financeDetails_data.get("docFinId").getAsString();
			 
			 String refno = dealRefNo.substring(0, dealRefNo.length() - 5) + docFinId + "-" + docFinNo;
			 
			 String txnApprovalTime = generalDetails.get("txnApprovalTime").getAsString();
			 
			 txnApprovalTime = util.Convert_BOT_Date_Format(txnApprovalTime, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String reportingDate = util.getCurrentReportDate();
			 String loanNumber = refno;
			 String transactionDate = convertHktToTanzaniaDate(txnApprovalTime);
			 String loanTransactionType =  generalDetails.get("product").getAsJsonObject().get("code").getAsString();
			 String loanTransactionSubType = subprodcode;
			 String currency = financeDetails_data.get("finCcy").getAsString();
			 String orgTransactionAmount = financeDetails_data.get("finAmt").getAsString();
			 String usdTransactionAmount = "0";  //Need to convert it by using Splice system 
			 String tzsTransactionAmount = financeDetails_data.get("finAmt").getAsString();  //Need to convert it by using Splice system
			 
			 loanTransactionType = "2";  //Need to discuss with Trade team
			 loanTransactionSubType = ""; //Need to discuss with Trade team
			 
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			 currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";
			 
			 String parentdeal = step_code.equalsIgnoreCase("NEW") ? "1" : "0";
			 
			// sql = "insert into TRADE001(suborgcode,syscode,chcode,paytype,reqdate,reqtime,evntime,parentdeal,notificationid,dealrefNo,stepcode,prodcode,subprodcode,message,status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			// Jdbctemplate.update(sql, new Object[] { SUBORGCODE, processingSystemCode, requestorSystemCode, "RTSIS", CurrentDate, timestamp, evntime, parentdeal, notificationID, dealReferenceNo, step_code, product_code, subprodcode, INFO3.getBytes(), "PENDING"});
			
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanTransactionInformation", count, reportingDate, loanNumber, transactionDate, loanTransactionType, loanTransactionSubType,
					currency, orgTransactionAmount,  usdTransactionAmount, tzsTransactionAmount });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanTransactionInformation", "serial", "reportingDate", "loanNumber", "transactionDate", "loanTransactionType", "loanTransactionSubType",
						"currency", "orgTransactionAmount",  "usdTransactionAmount", "tzsTransactionAmount" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanTransactionInformation"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanTransactionInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
//		 		 
//		     StoreLoanLog(SUBORGCODE, INFO1, "TRADE", loanNumber, Batch_id, O_SERIAL);
//		     
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject undrawnBalanceData_LTP(String INFO1, String INFO2, String INFO3) //RTS103
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
			 
			 Timestamp timestamp = util.get_oracle_Timestamp();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			
			 String notificationID = data.get("notificationID").getAsString();
			
			 String currency_1 = "";
			 String orgSanctionedAmount_1 = "";
			 String usdSanctionedAmount_1 = "";
			 String orgDisbursedAmount_1 = "";
			 String usdDisbursedAmount_1 = "";
			 String orgUnutilisedAmount_1 = "";
			 String usdUnutilisedAmount_1 = "";
			 String contractDate_1 = "";
			 
			 JsonObject anchorLimits = data.get("anchorLimits").getAsJsonObject();
			 String borrow_name = anchorLimits.get("anchorName").getAsString();

			 String customerIdentificationNumber_1 = anchorLimits.get("anchorLEId").getAsString();
			 
			 //JsonObject custLimitDetails = tradeLimits.get("custLimitDetails").getAsJsonObject();
			 
			 JsonArray outerLimits = anchorLimits.get("outerLimits").getAsJsonArray();
			 
			 for (int i = 0; i < outerLimits.size(); i++) 
			 {
				JsonObject js = outerLimits.get(i).getAsJsonObject();
				currency_1 = js.get("limitCcy").getAsString();
				orgSanctionedAmount_1 = js.get("limitAmt").getAsString();
				usdSanctionedAmount_1 = js.get("limitAmtUSD").getAsString();
				orgDisbursedAmount_1 = js.get("utilAmt").getAsString();
				usdDisbursedAmount_1 = js.get("utilAmtUSD").getAsString();
				orgUnutilisedAmount_1 = js.get("availAmt").getAsString();
				usdUnutilisedAmount_1 = js.get("availAmtUSD").getAsString();
				contractDate_1 = js.get("activationDate").getAsString();
			 }
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { customerIdentificationNumber_1, "TradeLMTID" }, new Lookup001_mapper());
				 
			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
			 
			 String reportingDate = util.getCurrentReportDate();
			 String borrowerName = borrow_name; 
			 String relationshipType = "17"; //bank asked to map as others 17
			 String contractDate = util.Convert_BOT_Date_Format(contractDate_1, "yyyy-MM-dd"); //; 
			 String categoryUndrawnBalance = "4"; //bank asked to map as others 4
			 boolean ratingStatus = true;//Y if there is any risk grade from SCI
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", customerIdentificationNumber_1);  //cc; 
			 String gradesUnratedCustomer = "";//Not Applicable
			 String customerIdentificationNumber = customerIdentificationNumber_1;
			 String currency = currency_1;
			 String orgSanctionedAmount = orgSanctionedAmount_1;
			 String usdSanctionedAmount = usdSanctionedAmount_1;
			 String tzsSanctionedAmount = "";//Vendor to convert
			 String orgDisbursedAmount = orgDisbursedAmount_1;
			 String usdDisbursedAmount = usdDisbursedAmount_1;
			 String tzsDisbursedAmount = "0";//Vendor to convert
			 String orgUnutilisedAmount = orgUnutilisedAmount_1;
			 String usdUnutilisedAmount = usdUnutilisedAmount_1;
			 String tzsUnutilisedAmount = "0";//Vendor to convert
			 String collateralType = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", customerIdentificationNumber_1+"|"+LimitID);
			 String pastDueDays = "0";//vendor need to map
			 String allowanceProbableLoss = "0";//vendor need to map
			 String botProvision = "0";//bank said to stamp as 0
		 
			 collateralType = util.isNullOrEmpty(collateralType) ? "2" : collateralType;
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgSanctionedAmount, currency);
				 
			 usdSanctionedAmount = rates.get("usd").getAsString();
			 tzsSanctionedAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgDisbursedAmount, currency);
			 
			 usdDisbursedAmount = rates.get("usd").getAsString();
			 tzsDisbursedAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgUnutilisedAmount, currency);
			 
			 usdUnutilisedAmount = rates.get("usd").getAsString();
			 tzsUnutilisedAmount = rates.get("tzs").getAsString();
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			 currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";
			
			 int count = 1;
			 	 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27\r\n" + 
			 		") values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "undrawnBalanceData", count, reportingDate, borrowerName, relationshipType, contractDate, categoryUndrawnBalance, ratingStatus, crRatingCounterCustomer, gradesUnratedCustomer, customerIdentificationNumber, currency, orgSanctionedAmount, usdSanctionedAmount, tzsSanctionedAmount, orgDisbursedAmount, usdDisbursedAmount, tzsDisbursedAmount, orgUnutilisedAmount, usdUnutilisedAmount, tzsUnutilisedAmount, collateralType, pastDueDays, allowanceProbableLoss, botProvision });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27\r\n" + 
				 		") values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "undrawnBalanceData", "serial", "reportingDate", "borrowerName", "relationshipType", "contractDate", "categoryUndrawnBalance", "ratingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "customerIdentificationNumber", "currency", "orgSanctionedAmount", "usdSanctionedAmount", "tzsSanctionedAmount", "orgDisbursedAmount", "usdDisbursedAmount", "tzsDisbursedAmount", "orgUnutilisedAmount", "usdUnutilisedAmount", "tzsUnutilisedAmount", "collateralType", "pastDueDays", "allowanceProbableLoss", "botProvision" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "undrawnBalanceData"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "undrawnBalanceData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
		 		 
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
	         details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in undrawnBalanceData_LTP :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}	
	
	public JsonObject writtenOffLoans_DTP(String INFO1, String INFO2, String INFO3) //RTS163
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.StringToJsonObject(INFO3);
			
			 String processingSystemCode = data.get("processingSystemCode").getAsString();
			 String requestorSystemCode = data.get("requestorSystemCode").getAsString();
			 
			 String eventTimestamp = data.get("eventTimestamp").getAsString();
			 
			 Timestamp evntime = util.Convert_to_timestamp(eventTimestamp, "yyyy-MM-dd'T'HH:mm:ss");
			
			 String step_code = data.get("step").getAsJsonObject().get("code").getAsString();
			 String product_code = data.get("product").getAsJsonObject().get("code").getAsString();
			 //String subprodcode = data.get("subProduct").getAsJsonObject().get("code").getAsString();
			 
			 String subprodcode = "";
			 
			 if(data.has("subProduct"))
			 {
				 JsonObject subProduct = data.get("subProduct").getAsJsonObject();  
				 
				 if(subProduct.has("code")) 
				 {
					 subprodcode = subProduct.get("code").getAsString();
				 }
			 }
			 
			 String notificationID = data.get("notificationID").getAsString();
			 String dealReferenceNo = data.get("dealReferenceNo").getAsString();
			 
			 JsonObject transactionDetails = data.get("transactionDetails").getAsJsonObject();
			
			 JsonObject dealDetails = transactionDetails.has("dealDetails") ? transactionDetails.get("dealDetails").getAsJsonObject() : new JsonObject();
			 
			 JsonObject customer = dealDetails.has("customer") ? dealDetails.get("customer").getAsJsonObject() : new JsonObject();
			 
			 String accountNo_1 = "";
			
			 if(customer.has("writeOff") && customer.get("writeOff").getAsString().equals("Y"))
			 {
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String CurrentDate = util.getCurrentDate("dd-MMM-yyyy") ;
				 
				 Timestamp timestamp = util.get_oracle_Timestamp();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 if(transactionDetails.has("invoiceDetails"))
				 {
					 JsonArray invoiceDetails = transactionDetails.get("invoiceDetails").getAsJsonArray();
					 
					 for(int j = 0; j < invoiceDetails.size(); j++) 
					 {
						JsonObject js1 = invoiceDetails.get(j).getAsJsonObject();
						JsonObject invoicePartyCode = js1.get("invoicePartyCode").getAsJsonObject();
						String invoice_code = invoicePartyCode.get("code").getAsString();
						
						if(invoice_code.equalsIgnoreCase("CUST")||invoice_code.equalsIgnoreCase("CST"))
						{
							 accountNo_1 = js1.get("accountNo").getAsString();
						}
					 }
				 }
				 
				 String customerName = data.get("customerName").getAsString();
				 String countryCode = "", leid= "";
				 JsonArray partyDetails = transactionDetails.get("partyDetails").getAsJsonArray();
				 
				 for (int i = 0; i < partyDetails.size(); i++) 
				 {
						JsonObject js = partyDetails.get(i).getAsJsonObject();
						JsonObject party = js.get("party").getAsJsonObject();
						JsonObject address = js.get("address").getAsJsonObject();
					    countryCode = address.get("countryCode").getAsString();
						JsonObject partyRole = js.get("partyRole").getAsJsonObject();
						String party_role_code = partyRole.get("code").getAsString();
						
						if(party_role_code.equalsIgnoreCase("CUST") || party_role_code.equalsIgnoreCase("CST") || party_role_code.equalsIgnoreCase("APPL"))
						{
							leid = js.get("leid").getAsString();
						}
				 }
				 
				 //String writeOff = "";
				 //JsonObject dealDetails = transactionDetails.get("dealDetails").getAsJsonObject();
					 
				 String financeNo = "";
				 String financeType = "";
				 String curr = "";
				 String amount = "";
				 String financeEffectiveDate = "";
				 String financeEventDate = "";
				 String financeMaturityDate ="";
				 double principlePaidAmount = 0;
				 double interestPaidAmount = 0;
				 String pastDueDays_1 = "";
				 String financeMaturityDate_1 = "";
				 String totalIntRate = "";
				 String balAmount = "";
				 double orgGrossPaidAmount_1 = 0;
				 JsonArray financeDetails = transactionDetails.get("financeDetails").getAsJsonArray();
				 
				 for (int j = 0; j < financeDetails.size(); j++) 
				 {
					JsonObject js2 = financeDetails.get(j).getAsJsonObject();
					financeNo = js2.has("financeNo") ? js2.get("financeNo").getAsString() : "";
					financeType = js2.has("financeType") ? js2.get("financeType").getAsString() : "";
					financeEventDate = js2.has("financeEventDate") ? js2.get("financeEventDate").getAsString() : "";
					String financeEventType = js2.has("financeEventType") ? js2.get("financeEventType").getAsString() : "";

					JsonObject financeAmount = js2.has("financeAmount") ? js2.get("financeAmount").getAsJsonObject() : new JsonObject();
					curr = financeAmount.has("curr") ? financeAmount.get("curr").getAsString() : "";
					principlePaidAmount = financeAmount.has("principlePaidAmount") ? financeAmount.get("principlePaidAmount").getAsDouble() : 0.0;
					interestPaidAmount = financeAmount.has("interestPaidAmount") ? financeAmount.get("interestPaidAmount").getAsDouble() : 0.0;
					orgGrossPaidAmount_1 = principlePaidAmount + interestPaidAmount;
					pastDueDays_1 = financeAmount.has("pastDueDays") ? financeAmount.get("pastDueDays").getAsString() : "";
					balAmount = financeAmount.has("balAmount") ? financeAmount.get("balAmount").getAsString() : "";
					amount = financeAmount.has("amount") ? financeAmount.get("amount").getAsString() : "";
					financeMaturityDate = financeAmount.has("financeMaturityDate") ? financeAmount.get("financeMaturityDate").getAsString() : "";
					financeEffectiveDate = financeAmount.has("financeEffectiveDate") ? financeAmount.get("financeEffectiveDate").getAsString() : "";

					JsonObject autoDebitonMaturity_JS = js2.has("autoDebitonMaturity") ? js2.get("autoDebitonMaturity").getAsJsonObject() : new JsonObject();
					
					String autoDebitonMaturity = autoDebitonMaturity_JS.has("value") ? autoDebitonMaturity_JS.get("value").getAsString() : "";

					JsonObject interestDetails = js2.has("interestDetails") ? js2.get("interestDetails").getAsJsonObject() : new JsonObject();
					totalIntRate = interestDetails.has("totalIntRate") ? interestDetails.get("totalIntRate").getAsString() : "";

				    if(autoDebitonMaturity.equalsIgnoreCase("true")||autoDebitonMaturity.equalsIgnoreCase("y")||financeEventType.equalsIgnoreCase("E07"))
				    {
				    	  financeMaturityDate_1 = financeAmount.get("financeMaturityDate").getAsString();
				    }
				 }
				 
				 String sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { leid, "TradeLMTID" }, new Lookup001_mapper());
				 
				 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
				 
				 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
				 
				 String reportingDate = util.getCurrentReportDate();
				 String customerIdentificationNumber = leid; //leid
				 String accountNumber = accountNo_1;
				 String borrowerName = customerName;
				 String borrowerCountry = countryCode;
				 String clientType = "7";
				 String loanNumber = notificationID.concat(financeNo);
				 String loanType = "1";
				 String currency = curr;
				 String orgDisbursedAmount = amount;
				 String usdDisbursedAmount = "0"; 
				 String tzsDisbursedAmount = "0"; 
				 String disbursementDate = util.Convert_BOT_Date_Format(financeEffectiveDate, "YYYY-MM-DD");
				 String gracePeriodPaymentPrincipal = util.Convert_BOT_Date_Format(financeEventDate, "YYYY-MM-DD");//vendor should calculate
				 String maturityDate = util.Convert_BOT_Date_Format(financeMaturityDate, "YYYY-MM-DD");;
				 String orgGrossPaidAmount = Double.toString(orgGrossPaidAmount_1);
				 String usdGrossPaidAmount = "0"; 
				 String tzsGrossPaidAmount = reportingDate; 
				 String writtenOffDate = util.getCurrentReportDate(); 
				 String orgOutstandingPrincipalAmount = balAmount;
				 String usdOutstandingPrincipalAmount = "0"; 
				 String tzsOutstandingPrincipalAmount = "0"; 
				 String annualInterestRate = totalIntRate;
				 String latestInstallmentPayDate = util.Convert_BOT_Date_Format(financeMaturityDate_1, "YYYY-MM-DD");
				 String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", leid+"|"+LimitID);
				 String orgCollateralValue = ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT", "LEID|LIMITID", leid+"|"+LimitID); 
				 String usdCollateralValue = "0"; 
				 String tzsCollateralValue = "0";  
				 String pastDueDays = Integer.parseInt(pastDueDays_1) < 0 ? "0" : Integer.parseInt(pastDueDays_1)+"";
				 String loanOfficer = ST.FindElementFromFileIT("SCI", "LOANOFFICER", "LEID", leid); 
				 String loanSupervisor = "NA"; 
				
				 collateralPledged = util.isNullOrEmpty(collateralPledged) ? "2" : collateralPledged;
				 orgCollateralValue = util.isNullOrEmpty(orgCollateralValue) ? "0" : orgCollateralValue;
				 
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgDisbursedAmount, currency);
					 
				 usdDisbursedAmount = rates.get("usd").getAsString();
				 tzsDisbursedAmount = rates.get("tzs").getAsString();  //
				 
				 rates = fx.find_exchangeRate(util.getCurrentDate(), orgGrossPaidAmount, currency);
				 
				 usdGrossPaidAmount = rates.get("usd").getAsString();
				 tzsGrossPaidAmount = rates.get("tzs").getAsString();
				 
				 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingPrincipalAmount, currency);
				 
				 usdOutstandingPrincipalAmount = rates.get("usd").getAsString();
				 tzsOutstandingPrincipalAmount = rates.get("tzs").getAsString();
				 
				 sql = "select * from lookup001 where COLUMN12=? and lower(COLUMN2)=lower(?)";  
			  
			     Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", borrowerCountry }, new Lookup001_mapper());
					
				borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] {"CUR", currency }, new Lookup001_mapper());
					
				currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "834" : "834";
				 
				 int count = 1;
				 	 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35)\r\n" + 
					 		"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "writtenOffLoans", count, reportingDate, customerIdentificationNumber, accountNumber, borrowerName, borrowerCountry, clientType, loanNumber, loanType, currency, orgDisbursedAmount, usdDisbursedAmount, tzsDisbursedAmount, disbursementDate, gracePeriodPaymentPrincipal, maturityDate, orgGrossPaidAmount, usdGrossPaidAmount, tzsGrossPaidAmount, writtenOffDate, orgOutstandingPrincipalAmount, usdOutstandingPrincipalAmount, tzsOutstandingPrincipalAmount, annualInterestRate, latestInstallmentPayDate, collateralPledged, orgCollateralValue, usdCollateralValue, tzsCollateralValue, pastDueDays, loanOfficer, loanSupervisor});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35)\r\n" + 
				 		"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "writtenOffLoans", "serial","reportingDate", "customerIdentificationNumber", "accountNumber", "borrowerName", "borrowerCountry", "clientType", "loanNumber", "loanType", "currency", "orgDisbursedAmount", "usdDisbursedAmount", "tzsDisbursedAmount", "disbursementDate", "gracePeriodPaymentPrincipal", "maturityDate", "orgGrossPaidAmount", "usdGrossPaidAmount", "tzsGrossPaidAmount", "writtenOffDate", "orgOutstandingPrincipalAmount", "usdOutstandingPrincipalAmount", "tzsOutstandingPrincipalAmount", "annualInterestRate", "latestInstallmentPayDate", "collateralPledged", "orgCollateralValue", "usdCollateralValue", "tzsCollateralValue", "pastDueDays", "loanOfficer", "loanSupervisor"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "writtenOffLoans"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "writtenOffLoans", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade" });	 
			 		 
			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			     
		         details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
		        
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch created successfully"); 
			 }
			 else
			 {
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "trigger point is not matched");
			 }    
		 }
		 catch(Exception e)
		 {
			
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in writtenOffLoans_DTP :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}	
	
	
	public String convertHktToTanzaniaDate(String inputDateStr) 
	{
       try 
       {
        	
            String timeWithoutTimezone = inputDateStr.replace("HKT", "").trim();
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMyyyyHHmm");
            
            TimeZone hktTimeZone = TimeZone.getTimeZone("Asia/Hong_Kong");
            inputFormat.setTimeZone(hktTimeZone);
            
            Date date = inputFormat.parse(timeWithoutTimezone);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("ddMMyyyyHHmm");
            TimeZone tanzaniaTimeZone = TimeZone.getTimeZone("Africa/Dar_es_Salaam");
            outputFormat.setTimeZone(tanzaniaTimeZone);
            
            return outputFormat.format(date);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            
            return null;
        }
    }

	public String FindDateDifference(String Date, String Format) 
	{
		String Bal = "0";
		
		try
		{
			 String sql = "select trunc(sysdate) - to_date(?, ?) from dual";  //"yyyy-mm-dd"
			 
			 Bal = Jdbctemplate.queryForObject(sql, new Object[] {Date, Format}, String.class);
		}
		catch(Exception ex)
		{
			 logger.debug("Exception in FindDateDifference :::: "+ex.getLocalizedMessage());
		}
		
		return Bal;
	}
	
	public JsonObject Request_Dispatcher(String BatchId, String REPORTSL, String SERVICD, String SUBORGCODE)  
	{
		JsonObject details = new JsonObject();
		
		try
		{
			Common_Utils utils = new Common_Utils();
		
			String Sql = "select limitsl from RTS004 where apicode = ? and status=?";
			
			int Limit = Jdbctemplate.queryForObject(Sql, new Object[] { SERVICD, "1"}, Integer.class);
			
			String dataCount = "select count(*) from report002 where serial = ? and COLUMN1 = ? and COLUMN2=?";
			
			int Total_dataCount = Jdbctemplate.queryForObject(dataCount, new Object[] { REPORTSL, "H", SERVICD }, Integer.class);
			
			int Total_Records=0;
			
			if(Total_dataCount > 1) 
			{
				String sql1 = "select count(*) from report002 w where serial = ? and COLUMN1 = ? and  COLUMN3 = (select COLUMN3 from report002 where serial = w.serial and COLUMN1 = ? and column4 = (select min(column4) from report002 where serial = w.serial and column1 = ?))";
			 
				Total_Records = Jdbctemplate.queryForObject(sql1, new Object[] { REPORTSL, "D", "H", "H" }, Integer.class);
				
				System.out.println("<<<<<<<<<<<<<<<<<<Greater than one>>>>>>>>>>>>>>>>>>>>>>>>>>");
			}
			else
			{
			    String sql = "select count(*) from REPORT002 where SERIAL = ? and COLUMN1 = ? and COLUMN2 = ?";
			
			    Total_Records = Jdbctemplate.queryForObject(sql, new Object[] { REPORTSL, "D", SERVICD }, Integer.class);
			  
			    System.out.println("<<<<<<<<<<<<<<<<<<Less than one>>>>>>>>>>>>>>>>>>>>>>>>>>");
			}
			
			int limit = Limit;  int total = Total_Records;
			
			int StartSl = 1;   int EndSl = total < limit ? total : limit;
			
			System.out.println("StartSl "+StartSl+" "+"EndSl "+EndSl+" "+"total "+total+" "+"REPORTSL "+REPORTSL);
			
			do
			{
				 if(EndSl == total && StartSl > total) 
				 {
					 break;
				 }
				 
				 String Reqsl = Generate_Report_Serial().get("Serial").getAsString(); 
				 
				 String Refno = Reqsl;   
				  
				 String sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
					
				 List<String> result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", new Sysconfig().getHostAddress() } , String.class);
					 
				 String Status = result.size() !=0 ? result.get(0) : "Q";   // get Queue name Q or Q1
				 
				 sql = "Insert into RTS005(suborgcode,syscode,paytype,reqdate,reqsl,refno,batchid,apicode,reportserial,startsl,endsl,status) values (?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "DV", "RTSIS", utils.getCurrentDate("dd-MMM-yyyy"), Reqsl , Refno, BatchId, SERVICD, REPORTSL, StartSl, EndSl, Status } );
				 
				 if(Active_Mode.equals("local"))
				 {
					 sql = "Insert into RTS005(suborgcode,syscode,paytype,reqdate,reqsl,refno,batchid,apicode,reportserial,startsl,endsl,status) values (?,?,?,?,?,?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "DV", "RTSIS", utils.getCurrentDate("dd-MMM-yyyy"), Reqsl+1 , Refno, BatchId, SERVICD, REPORTSL, StartSl, EndSl, "E" } );
				 }
				 
				 StartSl = EndSl+1;
				 
				 EndSl = (EndSl + limit) > total ? total : EndSl + limit;
				 
				 logger.debug(">>>>>>>>>>> Data inserted into RTS005 for the Report serial "+REPORTSL + "/" + StartSl + "/" + EndSl+" <<<<<<<<<<<<<<<");	
				 
			}while(EndSl <= total);
			
			details.addProperty("result", "success");
			details.addProperty("stscode", "HP00");
			details.addProperty("message", "Data added to the Queue !!");
			 
			logger.debug(">>>>>>>>>>> Data inserted into RTS005 for the Report serial "+REPORTSL+" <<<<<<<<<<<<<<<");	
		}
		catch(Exception e)
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP200");
			 details.addProperty("message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Report_Splitter <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		}
		
		return details;
	}
	
	public void StoreLoanLog(String SUBORGCODE, String APICODE, String SOURCESYS, String LoanNumber, String Batch_id, String O_SERIAL) //RTS019
	{
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Refno = Batch_id + "/" + O_SERIAL + "/" + "1/1";
			 
			 String Sql = "INSERT INTO LOAN_API_CALL_LOG(suborgcode,reqdate,api_code,srcsystem,loannumber,refno,ispushed,pushedon) VALUES(?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, util.getCurrentDate("dd-MMM-yyyy"), APICODE, SOURCESYS, LoanNumber, Refno, "1", util.get_oracle_Timestamp() });	  
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in StoreLoanLog :::: "+e.getLocalizedMessage());
		 }
	}
	
	
	public JsonObject Generate_Report_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select seq_report.nextval from dual";
			   
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
	
	private class Lookup001_mapper implements RowMapper<Lookup001>  
    {
    	Common_Utils util = new Common_Utils();
    	
		public Lookup001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Lookup001 Info = new Lookup001(); 
			
			Info.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			Info.setCOLUMN1(util.ReplaceNull(rs.getString("COLUMN1")));
			Info.setCOLUMN2(util.ReplaceNull(rs.getString("COLUMN2")));
			Info.setCOLUMN3(util.ReplaceNull(rs.getString("COLUMN3")));
			Info.setCOLUMN4(util.ReplaceNull(rs.getString("COLUMN4")));
			Info.setCOLUMN5(util.ReplaceNull(rs.getString("COLUMN5")));
			Info.setCOLUMN6(util.ReplaceNull(rs.getString("COLUMN6")));
			Info.setCOLUMN7(util.ReplaceNull(rs.getString("COLUMN7")));
			Info.setCOLUMN8(util.ReplaceNull(rs.getString("COLUMN8")));
			Info.setCOLUMN9(util.ReplaceNull(rs.getString("COLUMN9")));
			Info.setCOLUMN10(util.ReplaceNull(rs.getString("COLUMN10")));
			Info.setCOLUMN11(util.ReplaceNull(rs.getString("COLUMN11")));
			Info.setCOLUMN12(util.ReplaceNull(rs.getString("COLUMN12")));
			
			return Info;
		}
    }

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
}
