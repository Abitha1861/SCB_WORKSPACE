package com.hdsoft.models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.hdsoft.solace.QueueConsumerJNDI;
import com.hdsoft.Repositories.Lookup001;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_CASH 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_CASH(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptect_CASH() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_CASH.class);
	
	
	@RequestMapping(value = {"/Datavision/cash/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestHeader("APICODE") String APICODE, @RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
 	 	JsonObject details = new JsonObject();
		  
		try
		{
			Common_Utils util = new Common_Utils();
			
			JsonObject js = util.StringToJsonObject(Message);
			
			JsonObject header = js.get("header").getAsJsonObject();
			
			String applNm = header.has("applNm") && !header.get("applNm").isJsonNull() ? header.get("applNm").getAsString() : "";
			
			String pmtDrctn = header.has("pmtDrctn")? header.get("pmtDrctn").getAsString() : "";
			
			String pmtTp = header.has("pmtTp") ? header.get("pmtTp").getAsString() : "";
			
			String subPmtTp = header.has("subPmtTp") ? header.get("subPmtTp").getAsString() : "";
			
			BOT_Suptect_CASH Cash = new BOT_Suptect_CASH(Jdbctemplate);
			
			if(applNm.equalsIgnoreCase("STS"))			
			{
				QueueConsumerJNDI qs = new QueueConsumerJNDI(Jdbctemplate);
					
				boolean isvalid = qs.validateCash001(Message, "jhfjfj");
				
				if(isvalid)
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						return Cash.OutgoingFundTransfer_STS("RTS197", "", Message).toString();
					}
				}
				

			}
			else if (applNm.equalsIgnoreCase("DOTOPAL")) 
			{
				if(pmtDrctn.equalsIgnoreCase("O"))
				{
					Cash.OutgoingFundTransfer_DOTOPAL("RTS197", "", Message);
				}
				else if(pmtDrctn.equalsIgnoreCase("I"))
				{
					Cash.IncomingFundTransfer_DOTOPAL("RTS195", "", Message);
				}
			}
			else 
			{
				if(pmtTp.equalsIgnoreCase("RTGS"))
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						Cash.OutgoingFundTransfer_SCPAY_Rtgs("RTS197", "", Message);
					}
					else if (pmtDrctn.equalsIgnoreCase("I")) 
					{
						Cash.IncomingFundTransfer_SCPAY_Rtgs("RTS195", "", Message);	
					}
				}
				else if (pmtTp.equalsIgnoreCase("TT")) 
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						Cash.OutgoingFundTransfer_SCPAY_TT("RTS197", "", Message);	
					}
					else if (pmtDrctn.equalsIgnoreCase("I")) 
					{
						Cash.IncomingFundTransfer_SCPAY_TT("RTS195", "", Message);		
					}
				}
				else if (pmtTp.equalsIgnoreCase("ACH")) 
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						Cash.OutgoingFundTransfer_SCPAY_ACH("RTS197", "", Message);	
					}
					else if (pmtDrctn.equalsIgnoreCase("I")) 
					{
						Cash.IncomingFundTransfer_SCPAY_ACH("RTS195", "", Message);			
					}
				}
				else if (pmtTp.equalsIgnoreCase("BT")) 
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						Cash.OutgoingFundTransfer_SCPAY_BT("RTS197", "", Message);
					}
				}
				else if (pmtTp.equalsIgnoreCase("IBFT")) 
				{
					if(subPmtTp.equalsIgnoreCase("OC"))
					{
						Cash.OutgoingFundTransfer_SCPAY_FAST("RTS197", "", Message);	
					}
					else if (subPmtTp.equalsIgnoreCase("IC")) 
					{
						Cash.IncomingFundTransfer_SCPAY_FAST("RTS195", "", Message);			
					}
				}
			}
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/cash_scpay"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		JsonObject details = new JsonObject();
   	 	
   	    Common_Utils util = new Common_Utils();
   	 
   	 	JsonObject data = util.StringToJsonObject(MESSAGE);
   	 	
	   	 String Apicode = request.getHeader("Apicode");  // Extract Apicode from header
	     String PRODUCT = request.getHeader("PRODUCT");  // Extract PRODUCT from header
	     
	 	
   	 	if(Apicode.equalsIgnoreCase("RTS197") && PRODUCT.equalsIgnoreCase("RTGS"))
   	 	{
   	  	 	details = OutgoingFundTransfer_SCPAY_Rtgs("RTS197", "", data.toString());
   	 	}
   	 	if(Apicode.equalsIgnoreCase("RTS195") && PRODUCT.equalsIgnoreCase("RTGS"))
	 	{
   	 		details = IncomingFundTransfer_SCPAY_Rtgs("RTS195", "", data.toString());
	 	}
   	 	if(Apicode.equalsIgnoreCase("RTS197") && PRODUCT.equalsIgnoreCase("TT"))
	 	{
   	 		details = OutgoingFundTransfer_SCPAY_TT("RTS197", "", data.toString());
	 	}
   	 	if(Apicode.equalsIgnoreCase("RTS195") && PRODUCT.equalsIgnoreCase("TT"))
	 	{
   	 		details = IncomingFundTransfer_SCPAY_TT("RTS195", "", data.toString());
	 	}
   		if(Apicode.equalsIgnoreCase("RTS197") && PRODUCT.equalsIgnoreCase("ACH"))
	 	{
   	 		details = OutgoingFundTransfer_SCPAY_ACH("RTS197", "", data.toString());
	 	}
   	 	if(Apicode.equalsIgnoreCase("RTS195") && PRODUCT.equalsIgnoreCase("ACH"))
	 	{
   	 		details = IncomingFundTransfer_SCPAY_ACH("RTS195", "", data.toString());
	 	}
   	 	if(Apicode.equalsIgnoreCase("RTS197") && PRODUCT.equalsIgnoreCase("BT"))
	 	{
	  	 	details = OutgoingFundTransfer_SCPAY_BT("RTS197", "", data.toString());
	 	}

//   	 	details = OutgoingFundTransfer_SCPAY_TT("RTS197", "", data.toString());
//   	 	details = OutgoingFundTransfer_SCPAY_Rtgs("RTS197", "", data.toString());
//   	 	details = IncomingFundTransfer_SCPAY_TT("RTS195", "", data.toString());
//   		details = IncomingFundTransfer_SCPAY_Rtgs("RTS195", "", data.toString());
	 	
   	 	return details.toString();

    }

	public JsonObject IncomingFundTransfer_DOTOPAL(String INFO1, String INFO2, String INFO3) //RTS195
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject header = data_.get("header").getAsJsonObject();
			 
			 JsonArray data = data_.get("data").getAsJsonArray();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = header.get("sysrefNb").getAsString();
			 String transactionDate = "";
			 String transferChannel =  header.get("instgChanl").getAsString();
			 String subCategoryTransferChannel = ""; //non mandatory
			 String recipientName = "";
			 String senderAccountNumber = "";
			 String recipientIdentificationType = "";
			 String recipientIdentificationNumber = "";  
			 String senderCountry = "";   
			 String serviceCategory = "";  
			 String serviceSubCategory = "";  
			 String senderName = "";
			 String senderBankOrFspCode = "";  
			 String senderAccountOrWalletNumber = ""; 
			 String currency = "";
			 String orgAmount = "";
			 String usdAmount = "0";
			 String tzsAmount = "";
			 String purposes = "";  
			 String senderInstruction = "";

			 if(data.size() > 0)
			 {				 
				 JsonObject js = data.get(0).getAsJsonObject();
				 
				 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
				 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
				 
				 JsonObject cdtr = js.get("cdtr").getAsJsonObject();
				 recipientName  = cdtr.get("nm").getAsString();
				 
				 JsonObject cdtrAcct = js.get("cdtrAcct").getAsJsonObject();
				 senderAccountNumber  = cdtrAcct.get("id").getAsString();
				 
				 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
				 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
				 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
				 JsonArray cstmrDoc = DBIT.has("cstmrDoc") ? DBIT.get("cstmrDoc").getAsJsonArray() : new JsonArray();
				 
				 if(cstmrDoc.size()>0)
				 {
					 JsonObject in = cstmrDoc.get(0).getAsJsonObject();
					 
					 recipientIdentificationType = in.has("nm")  ? in.get("nm").getAsString() : "";
					 recipientIdentificationNumber = in.has("val")  ? in.get("val").getAsString() : "";
				 }
				  
					 JsonObject billAndMrchntTxInf = js.get("billAndMrchntTxInf").getAsJsonObject();
					 
					 if(util.isNullOrEmpty(recipientIdentificationNumber) && billAndMrchntTxInf.has("resdtIdntyCardNb"))
					 {
						 recipientIdentificationNumber = billAndMrchntTxInf.get("resdtIdntyCardNb").getAsString();
					 }
					 if(util.isNullOrEmpty(recipientIdentificationNumber) && billAndMrchntTxInf.has("cstmrPsptNb"))
					 {
						 recipientIdentificationNumber = billAndMrchntTxInf.get("cstmrPsptNb").getAsString();
					 }
					 if(util.isNullOrEmpty(recipientIdentificationNumber) && billAndMrchntTxInf.has("ntlId"))
					 {
						 recipientIdentificationNumber = billAndMrchntTxInf.get("ntlId").getAsString();
					 }
					 
					 JsonObject dbtr = js.get("dbtr").getAsJsonObject();
					 senderCountry = dbtr.get("ctryOfRes").getAsString();
					 
					 JsonObject rgltryDocVrfctnInf = js.get("rgltryDocVrfctnInf").getAsJsonObject();
					 JsonArray xchgCtrlRptgInfo = rgltryDocVrfctnInf.get("xchgCtrlRptgInfo").getAsJsonArray();
					 if(xchgCtrlRptgInfo.size()>0)
					 {
						 serviceCategory = xchgCtrlRptgInfo.get(0).getAsJsonObject().get("BOPCtgy").getAsString();
						 serviceSubCategory = xchgCtrlRptgInfo.get(0).getAsJsonObject().get("BOPSubCtgy").getAsString();
					 }
					 
					 senderName = dbtr.get("nm").getAsString();
					 
					 JsonObject dbtrAgt = js.get("dbtrAgt").getAsJsonObject();
					 senderBankOrFspCode = dbtrAgt.get("BICFI").getAsString();
					 
					 JsonObject dbtrAcct = js.get("dbtrAcct").getAsJsonObject();
					 senderAccountOrWalletNumber = dbtrAcct.get("id").getAsString();
					 
					 JsonObject amt = js.get("amt").getAsJsonObject();
					 currency = amt.get("intrbkSttlmAmtCcy").getAsString();
					 
					 orgAmount = amt.get("instdAmt").getAsString();
					 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
					 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
					 
					 purposes = rgltryDocVrfctnInf.has("BOPTxCd1") ? rgltryDocVrfctnInf.get("BOPTxCd1").getAsString() : "";

					 if(util.isNullOrEmpty(purposes) && rgltryDocVrfctnInf.has("BOPTxCd2"))
					 {
						 purposes = rgltryDocVrfctnInf.has("BOPTxCd2") ? rgltryDocVrfctnInf.get("BOPTxCd2").getAsString() : "";
					 }

					 if(util.isNullOrEmpty(purposes) && rgltryDocVrfctnInf.has("BOPTxRemarks1"))
					 {
						 purposes = rgltryDocVrfctnInf.has("BOPTxRemarks1") ? rgltryDocVrfctnInf.get("BOPTxRemarks1").getAsString() : "";
					 }

					 if(util.isNullOrEmpty(purposes) && rgltryDocVrfctnInf.has("BOPTxRemarks2"))
					 {
						 purposes = rgltryDocVrfctnInf.has("BOPTxRemarks2") ? rgltryDocVrfctnInf.get("BOPTxRemarks2").getAsString() : "";
					 }
  
					 JsonObject rmtInf = js.get("rmtInf").getAsJsonObject();
					 JsonArray instrInf = rmtInf.get("instrInf").getAsJsonArray();
					 
					 if(instrInf.size()>0)
					 {
						 senderInstruction = instrInf.get(0).getAsJsonObject().get("desc").getAsString();
					 }
			 }
			 
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
			 
			 transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";	
			
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
		
			 if(transferChannel.equals("3"))
			 {
				purposes = "22103";  
			 }
			 else
			 {
				//sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				//Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				//purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "";
				 
				 purposes = "";
			 }
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", senderCountry }, new Lookup001_mapper());
				
			 senderCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
		 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
		 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
				
			 serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "5" : "5";
				
			 serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "" : "";
			
			 orgAmount = util.isNullOrEmpty(orgAmount) ? "0.00" : orgAmount; 
			 usdAmount = util.isNullOrEmpty(usdAmount) ? "0.00" : usdAmount; 
			 tzsAmount = util.isNullOrEmpty(tzsAmount) ? "0.00" : tzsAmount;  
			
			 sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
				   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
				   "and rownum = ?";
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "Y", recipientIdentificationNumber, "1", "1"  }, new Lookup002_mapper());

			 if(Info.size() > 0 && Info.get(0).getCOLUMN3().equals("C"))  //corporate
			 {
				 sql = "select trim(SCIREFERENCENO) from d3_ebbs_tb where masterno = ? and trim(SCIREFERENCENO) is not null";
				 
				 List<String> out = Jdbctemplate.queryForList(sql, new Object[] { recipientIdentificationNumber }, String.class);
				 
				 if(out.size() == 0)
				 {
					 sql = "select trim(SCILEID) from D14_EBBS_TB D14 where masterno = ? and trim(SCILEID) is not null";
					 
					 out = Jdbctemplate.queryForList(sql, new Object[] { recipientIdentificationNumber }, String.class);
				 }
				 
				 if(out.size() > 0)
				 {
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> out2 = Jdbctemplate.queryForList(sql, new Object[] { out.get(0) }, String.class);
					 
					 recipientIdentificationType = "12";
					 recipientIdentificationNumber = out2.size() > 0 ? out2.get(0) : "NA";
				 }
				 else
				 {
					 recipientIdentificationType = "12";
					 recipientIdentificationNumber = "NA";
				 }
			 }
			 else if(Info.size() > 0 && !Info.get(0).getCOLUMN3().equals("C")) // Individual
			 {
				 recipientIdentificationType = Info.get(0).getCOLUMN1();
				 recipientIdentificationNumber = Info.get(0).getCOLUMN2();
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", recipientIdentificationType }, new Lookup001_mapper());
					
				 recipientIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
			 }
			 else
			 {
				 recipientIdentificationType = "1";
				 recipientIdentificationNumber = "NA";
			 }
			 
			 if(senderBankOrFspCode.length() == 3)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }
			 else if(senderBankOrFspCode.length() == 6)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }
			 else if(senderBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode.substring(0, 8);
				 
				 if(senderBankOrFspCode.length() == 8)
				 {
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
					 
					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
				 }
			 }
			 else if(senderBankOrFspCode.length() == 8)
			 {
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
				 
				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
			 }
			 else 
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }
			 
			
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
			 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
			 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
			 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "incomingBankFundsTransfer", count, reportingDate, transactionId,
						 transactionDate, transferChannel, subCategoryTransferChannel, recipientName, senderAccountNumber,
						 recipientIdentificationType, recipientIdentificationNumber, senderCountry, senderName, senderBankOrFspCode,
						 senderAccountOrWalletNumber, serviceCategory, serviceSubCategory, currency, orgAmount, usdAmount,
						 tzsAmount, purposes, senderInstruction});
 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "incomingBankFundsTransfer"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "incomingBankFundsTransfer", "serial", "reportingDate", 
					 "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel", "recipientName", "senderAccountNumber", 
					 "recipientIdentificationType", "recipientIdentificationNumber", "senderCountry", "senderName", "senderBankOrFspCode",
					 "senderAccountOrWalletNumber", "serviceCategory", "serviceSubCategory", "currency", "orgAmount", "usdAmount", "tzsAmount",
					 "purposes", "senderInstruction"});
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "incomingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH" });	 
		     
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
			 
			 logger.error("Error processing IncomingFundTransfer_DOTOPAL: {}", e.getMessage()); 
		 }
		
		 return details;
	}
	
	public JsonObject OutgoingFundTransfer_DOTOPAL(String INFO1, String INFO2, String INFO3) //RTS197
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject header = data_.get("header").getAsJsonObject();
			 
			 JsonArray data = data_.get("data").getAsJsonArray();
			 		 
			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = "";
			 String transactionDate = "";
			 String transferChannel = "";
			 String subCategoryTransferChannel = "";
			 String senderAccountNumber = "";
			 String senderIdentificationType = "";
			 String senderIdentificationNumber = "";
			 String recipientName = "";
			 String recipientMobileNumber = "";
			 String recipientCountry = "";
			 String recipientBankOrFspCode = "";
			 String recipientAccountOrWalletNumber = "";
			 String serviceChannel = "";
			 String serviceCategory = "";
			 String serviceSubCategory = "";		
			 String orgAmount = "";
			 String currency = "";
			 String usdAmount = "";
			 String tzsAmount = "";
			 String purposes = "";
			 String senderInstruction = "";
			 String transactionPlace = "";

			 if(data.size() > 0)
			 {
				 transactionId = header.get("sysrefNb").getAsString();
				 transferChannel =  header.get("instgChanl").getAsString();
				 
				 JsonObject js = data.get(0).getAsJsonObject();
				 
				 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
				 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
				 
				 JsonObject dbtrAcct = js.get("dbtrAcct").getAsJsonObject();
				 senderAccountNumber = dbtrAcct.get("id").getAsString();
				 
				 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
				 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
				 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
				 JsonArray cstmrDoc = DBIT.has("cstmrDoc") ? DBIT.get("cstmrDoc").getAsJsonArray() : new JsonArray();
				 
				 if(cstmrDoc.size()>0)
				 {
					 JsonObject in = cstmrDoc.get(0).getAsJsonObject();
					 
					 senderIdentificationType = in.has("nm")  ? in.get("nm").getAsString() : "";
					 senderIdentificationNumber =  in.has("val")  ? in.get("val").getAsString() : "";
				 }
				 
				 JsonObject billAndMrchntTxInf = js.get("billAndMrchntTxInf").getAsJsonObject();
				 
				 if(util.isNullOrEmpty(senderIdentificationNumber) && billAndMrchntTxInf.has("resdtIdntyCardNb"))
				 {
					 senderIdentificationNumber = billAndMrchntTxInf.get("resdtIdntyCardNb").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(senderIdentificationNumber) && billAndMrchntTxInf.has("cstmrPsptNb"))
				 {
					 senderIdentificationNumber = billAndMrchntTxInf.get("cstmrPsptNb").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(senderIdentificationNumber) && billAndMrchntTxInf.has("ntlId"))
				 {
					 senderIdentificationNumber = billAndMrchntTxInf.get("ntlId").getAsString();
				 }
					 
				 JsonObject cdtr = js.get("cdtr").getAsJsonObject();
				 recipientName = cdtr.get("nm").getAsString();
				 
				 recipientCountry = cdtr.get("ctryOfRes").getAsString();
	       
				 JsonObject cdtrAgt = js.get("cdtrAgt").getAsJsonObject();
				 recipientBankOrFspCode = cdtrAgt.get("BICFI").getAsString();
				 
				 JsonObject cdtrAcct = js.get("cdtrAcct").getAsJsonObject();
				 recipientAccountOrWalletNumber = cdtrAcct.get("id").getAsString();
				 
				 JsonObject chanl = js.get("chanl").getAsJsonObject();
				 serviceChannel = chanl.get("instgChanl").getAsString();
				 
				 JsonObject rgltryDocVrfctnInf = js.get("rgltryDocVrfctnInf").getAsJsonObject();
				 JsonArray xchgCtrlRptgInfo = rgltryDocVrfctnInf.get("xchgCtrlRptgInfo").getAsJsonArray();
				 
				 if(xchgCtrlRptgInfo.size()>0)
				 {
					 serviceCategory = xchgCtrlRptgInfo.get(0).getAsJsonObject().get("BOPCtgy").getAsString();
					 serviceSubCategory = xchgCtrlRptgInfo.get(0).getAsJsonObject().get("BOPSubCtgy").getAsString();
				 }
				 
				 JsonObject amt = js.get("amt").getAsJsonObject();
				 currency = amt.get("intrbkSttlmAmtCcy").getAsString();
				 
				 orgAmount = amt.get("instdAmt").getAsString();
				 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
				 
				 if(amt.has("dbtAmtBaseCcyEqvt"))
				 {
					 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
				 }
				 else if(amt.has("baseCcy"))
				 {
					 tzsAmount = amt.get("baseCcy").getAsString();
				 }
					 
				 JsonObject purp = js.get("purp").getAsJsonObject();
				 
				 if(purp.has("cd1"))
				 {
					 purposes = purp.get("cd1").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(purposes) && purp.has("sndrPurpDesc1"))
				 {
					 purposes = purp.get("sndrPurpDesc1").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(purposes) && purp.has("cd2"))
				 {
					 purposes = purp.get("cd2").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(purposes) && purp.has("sndrPurpDesc2"))
				 {
					 purposes = purp.get("sndrPurpDesc2").getAsString();
				 }
				 
				 JsonObject rmtInf = js.get("rmtInf").getAsJsonObject();
				 JsonArray instrInf = rmtInf.get("instrInf").getAsJsonArray();
				 
				 if(instrInf.size()>0)
				 {
					 senderInstruction = instrInf.get(0).getAsJsonObject().get("desc").getAsString();
				 }
					 
				 JsonObject dbtrAgt =  js.get("dbtrAgt").getAsJsonObject();
				 JsonObject pstlAdr = dbtrAgt.get("pstlAdr").getAsJsonObject();
				 transactionPlace = pstlAdr.get("ctry").getAsString();
			 }

			String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
				
			transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";	
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
				
			recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
				
			serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "5" : "5";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
				
			serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "5" : "5";
				
			serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "" : "";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", transactionPlace }, new Lookup001_mapper());
				
			transactionPlace = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			
			orgAmount = util.isNullOrEmpty(orgAmount) ? "0.00" : orgAmount; 
			usdAmount = util.isNullOrEmpty(usdAmount) ? "0.00" : usdAmount;  
			tzsAmount = util.isNullOrEmpty(tzsAmount) ? "0.00" : tzsAmount; 
			
			if(transferChannel.equals("3"))
			{
				purposes = "32103";  
			}
			else
			{
				purposes = "";
			}
			
			sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
					   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
					   "and rownum = ?";
				 
			 Info = Jdbctemplate.query(sql, new Object[] { "Y", senderIdentificationNumber, "1", "1"  }, new Lookup002_mapper());

			 if(Info.size() > 0 && Info.get(0).getCOLUMN3().equals("C"))  //corporate
			 {
				 sql = "select trim(SCIREFERENCENO) from d3_ebbs_tb where masterno = ? and trim(SCIREFERENCENO) is not null";
				 
				 List<String> out = Jdbctemplate.queryForList(sql, new Object[] { senderIdentificationNumber }, String.class);
				 
				 if(out.size() == 0)
				 {
					 sql = "select trim(SCILEID) from D14_EBBS_TB D14 where masterno = ? and trim(SCILEID) is not null";
					 
					 out = Jdbctemplate.queryForList(sql, new Object[] { senderIdentificationNumber }, String.class);
				 }
				 
				 if(out.size() > 0)
				 {
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> out2 = Jdbctemplate.queryForList(sql, new Object[] { out.get(0) }, String.class);
					 
					 senderIdentificationType = "12";
					 senderIdentificationNumber = out2.size() > 0 ? out2.get(0) : "NA";
				 }
				 else
				 {
					 senderIdentificationType = "12";
					 senderIdentificationNumber = "NA";
				 }
			 }
			 else if(Info.size() > 0 && !Info.get(0).getCOLUMN3().equals("C")) // Individual
			 {
				 senderIdentificationType = Info.get(0).getCOLUMN1();
				 senderIdentificationNumber = Info.get(0).getCOLUMN2();
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
					
				 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
			 }
			 else
			 {
				 senderIdentificationType = "1";
				 senderIdentificationNumber = "NA";
			 }
		
			 if(recipientBankOrFspCode.length() == 3)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			 else if(recipientBankOrFspCode.length() == 6)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }
			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
			 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
			
			
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
					") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
					 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
					 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
					 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
					 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
		 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
					 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
					 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
					 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
					 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH" });	 
		    
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
			 
			 logger.error("Error processing OutgoingFundTransfer_DOTOPAL: "+e.getLocalizedMessage());  
		 }
		
		 return details;
	}
	
	public JsonObject OutgoingFundTransfer_STS(String INFO1, String INFO2, String INFO3) //RTS011
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject header = data_.get("header").getAsJsonObject();
			 
			 JsonArray data = data_.get("data").getAsJsonArray();
			 	 
			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = "";
			 String transactionDate = "";
			 String transferChannel = "";
			 String subCategoryTransferChannel = "";
			 String senderAccountNumber = "";
			 String senderIdentificationType = "";
			 String senderIdentificationNumber = "";
			 String recipientName = "";
			 String recipientMobileNumber = "";
			 String recipientCountry = "";
			 String recipientBankOrFspCode = "";
			 String recipientAccountOrWalletNumber = "";
			 String serviceChannel = "";
			 String serviceCategory = "";
			 String serviceSubCategory = "";		
			 String orgAmount = "";
			 String currency = "";
			 String usdAmount = "";
			 String tzsAmount = "";
			 String purposes = "";
			 String senderInstruction = "";
			 String transactionPlace = "218";

			 if(data.size() > 0)
			 {
				 JsonObject js = data.get(0).getAsJsonObject();
				 
				 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
				 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
				 
				 JsonObject dbtrAcct = js.get("dbtrAcct").getAsJsonObject();
				 senderAccountNumber = dbtrAcct.get("id").getAsString();
				 
				 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
				 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
				 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
				 JsonArray cstmrDoc = DBIT.has("cstmrDoc") ? DBIT.get("cstmrDoc").getAsJsonArray() : new JsonArray();
				 
				 if(cstmrDoc.size()>0)
				 {
					 JsonObject in = cstmrDoc.get(0).getAsJsonObject();
					 
					 senderIdentificationType = in.has("nm")  ? in.get("nm").getAsString() : "";
					 senderIdentificationNumber =  in.has("val")  ? in.get("val").getAsString() : "";
				 }
				 
				 JsonObject billAndMrchntTxInf = js.get("billAndMrchntTxInf").getAsJsonObject();
				 
				 if(util.isNullOrEmpty(senderIdentificationNumber) && billAndMrchntTxInf.has("resdtIdntyCardNb"))
				 {
					 senderIdentificationNumber = billAndMrchntTxInf.get("resdtIdntyCardNb").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(senderIdentificationNumber) && billAndMrchntTxInf.has("cstmrPsptNb"))
				 {
					 senderIdentificationNumber = billAndMrchntTxInf.get("cstmrPsptNb").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(senderIdentificationNumber) && billAndMrchntTxInf.has("ntlId"))
				 {
					 senderIdentificationNumber = billAndMrchntTxInf.get("ntlId").getAsString();
				 }
				 
				 JsonObject chanl = js.get("chanl").getAsJsonObject();
				 serviceChannel = chanl.get("instgChanl").getAsString();
				 
				 JsonObject cdtr = js.get("cdtr").getAsJsonObject();
				 recipientName = cdtr.get("nm").getAsString();
				 
				 recipientCountry = cdtr.get("ctryOfRes").getAsString(); //
					 
				 if(util.isNullOrEmpty(recipientCountry))
				 {
					 JsonObject cdtrAgt = js.get("cdtrAgt").getAsJsonObject();
					 recipientCountry = cdtrAgt.get("cdtrAgt").getAsString();
				 }
				  
				 if(js.has("instdAgt"))
				 {
					 JsonObject instdAgt = js.get("instdAgt").getAsJsonObject();
					 recipientBankOrFspCode = instdAgt.get("BICFI").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode) && js.has("cdtrAgt"))
				 {
					 JsonObject cdtrAgt = js.get("cdtrAgt").getAsJsonObject();
					 recipientBankOrFspCode = cdtrAgt.get("BICFI").getAsString();
				 }
				 

				 JsonObject cdtrAcct = js.get("cdtrAcct").getAsJsonObject();
				 recipientAccountOrWalletNumber = cdtrAcct.get("id").getAsString();
				 
				 if(util.isNullOrEmpty(recipientAccountOrWalletNumber) && cdtrAcct.has("IBAN"))
				 {
					 recipientAccountOrWalletNumber = cdtrAcct.get("IBAN").getAsString();
				 }
					 
				 JsonObject rgltryDocVrfctnInf = js.get("rgltryDocVrfctnInf").getAsJsonObject();
				 JsonArray xchgCtrlRptgInfo = rgltryDocVrfctnInf.get("xchgCtrlRptgInfo").getAsJsonArray();
				 
				 if(xchgCtrlRptgInfo.size()>0)
				 {
					 serviceCategory = xchgCtrlRptgInfo.get(0).getAsJsonObject().get("BOPCtgy").getAsString();
					 serviceSubCategory = xchgCtrlRptgInfo.get(0).getAsJsonObject().get("BOPSubCtgy").getAsString();
				 }
					
				 JsonObject amt = js.get("amt").getAsJsonObject();
				 currency = amt.get("intrbkSttlmAmtCcy").getAsString();
				 
				 orgAmount = amt.get("instdAmt").getAsString();
				 
				 if(amt.has("cdtAmtUSDEqvt"))
				 {
					 usdAmount = amt.get("cdtAmtUSDEqvt").getAsString();
				 }
				 
				 if(util.isNullOrEmpty(usdAmount) && amt.has("dbtAmtUSDEqvt"))
				 {
					 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
				 }
				 
				 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
				 
				 //JsonObject purp = js.get("purp").getAsJsonObject();
					 
				 if(rgltryDocVrfctnInf.has("BOPTxCd1"))
				 {
					 purposes = rgltryDocVrfctnInf.has("BOPTxCd1") ? rgltryDocVrfctnInf.get("BOPTxCd1").getAsString() : "";
				 }
				 
				 if(util.isNullOrEmpty(purposes) && rgltryDocVrfctnInf.has("BOPTxCd2"))
				 {
					 purposes = rgltryDocVrfctnInf.has("BOPTxCd2") ? rgltryDocVrfctnInf.get("BOPTxCd2").getAsString() : "";
				 }
				 
				 if(util.isNullOrEmpty(purposes) && rgltryDocVrfctnInf.has("BOPTxRemarks1"))
				 {
					 purposes = rgltryDocVrfctnInf.has("BOPTxRemarks1") ? rgltryDocVrfctnInf.get("BOPTxRemarks1").getAsString() : "";
				 }
				 
				 if(util.isNullOrEmpty(purposes) && rgltryDocVrfctnInf.has("BOPTxRemarks2"))
				 {
					 purposes = rgltryDocVrfctnInf.has("BOPTxRemarks2") ? rgltryDocVrfctnInf.get("BOPTxRemarks2").getAsString() : "";
				 }
					 
				 JsonObject rmtInf = js.get("rmtInf").getAsJsonObject();
				 JsonArray instrInf = rmtInf.get("instrInf").getAsJsonArray();
				 		 
				 if(instrInf.size()>0)
				 {
					 senderInstruction = instrInf.get(0).getAsJsonObject().get("desc").getAsString();
				 }
			 }
			
			 transactionId = header.get("sysrefNb").getAsString();
			 
			 transferChannel =  header.get("instgChanl").getAsString();
			 
			 if(header.has("instgSubChanl"))
			 {
				 subCategoryTransferChannel = header.get("instgSubChanl").getAsString();
			 }

			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
			 
			 transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";	
				
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
				
			recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";

			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
		 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
				
			serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "5" : "5";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", transactionPlace }, new Lookup001_mapper());
				
			transactionPlace = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			
			orgAmount = util.isNullOrEmpty(orgAmount) ? "0.00" : orgAmount; 
			usdAmount = util.isNullOrEmpty(usdAmount) ? "0.00" : usdAmount;  
			tzsAmount = util.isNullOrEmpty(tzsAmount) ? "0.00" : tzsAmount; 
			
			if(transferChannel.equals("3"))
			{
				purposes = "32103";  
			}
			else
			{
				purposes = "";
			}
			
			sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
					   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
					   "and rownum = ?";
				 
			 Info = Jdbctemplate.query(sql, new Object[] { "Y", senderIdentificationNumber, "1", "1"  }, new Lookup002_mapper());

			 if(Info.size() > 0 && Info.get(0).getCOLUMN3().equals("C"))  //corporate
			 {
				 sql = "select trim(SCIREFERENCENO) from d3_ebbs_tb where masterno = ? and trim(SCIREFERENCENO) is not null";
				 
				 List<String> out = Jdbctemplate.queryForList(sql, new Object[] { senderIdentificationNumber }, String.class);
				 
				 if(out.size() == 0)
				 {
					 sql = "select trim(SCILEID) from D14_EBBS_TB D14 where masterno = ? and trim(SCILEID) is not null";
					 
					 out = Jdbctemplate.queryForList(sql, new Object[] { senderIdentificationNumber }, String.class);
				 }
				 
				 if(out.size() > 0)
				 {
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> out2 = Jdbctemplate.queryForList(sql, new Object[] { out.get(0) }, String.class);
					 
					 senderIdentificationType = "12";
					 senderIdentificationNumber = out2.size() > 0 ? out2.get(0) : "NA";
				 }
				 else
				 {
					 senderIdentificationType = "12";
					 senderIdentificationNumber = "NA";
				 }
			 }
			 else if(Info.size() > 0 && !Info.get(0).getCOLUMN3().equals("C")) // Individual
			 {
				 senderIdentificationType = Info.get(0).getCOLUMN1();
				 senderIdentificationNumber = Info.get(0).getCOLUMN2();
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
					
				 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
			 }
			 else
			 {
				 senderIdentificationType = "1";
				 senderIdentificationNumber = "NA";
			 }
			 
			 if(recipientBankOrFspCode.length() == 3)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			 else if(recipientBankOrFspCode.length() == 6)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }

			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
	
			 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
			
			int count = 0;
			 
			String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			count++;
			
			sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
					 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
					 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
					 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
					 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
			 				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
					 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
					 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
					 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
					 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH" });	 
		
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
			 
			 logger.debug("Exception in OutgoingFundTransfer_STS :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	//---------------------------------SCPAY--------------------------------------
	
	public JsonObject OutgoingFundTransfer_SCPAY_Rtgs(String INFO1, String INFO2, String INFO3) //RTS197 //DONE
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject header = data_.get("header").getAsJsonObject();
			 
			 String payment_type = header.get("pmtTp").getAsString();
			 
		
			 JsonObject data = data_.get("data").getAsJsonObject();
			 		 
			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = "";
			 String transactionDate = "";
			 String transferChannel = header.get("instgChanl").getAsString();
			 String subCategoryTransferChannel = ""; //non mandatory
			 String senderAccountNumber = "";
			 String senderIdentificationType = ""; 
			 String senderIdentificationNumber = ""; 
			 String recipientName = "";
			 String recipientMobileNumber = "NA";
			 String recipientCountry = "";
			 String recipientBankOrFspCode = ""; 
			 String recipientAccountOrWalletNumber = "";
			 String serviceChannel = "";
			 String serviceCategory = "";
			 String serviceSubCategory = "";		
			 String orgAmount = "";
			 String currency = "";
			 String usdAmount = "";
			 String tzsAmount = "";
			 String purposes = "";
			 String senderInstruction = "NA";
			 String transactionPlace = "";

			 String Identityno = ""; boolean corp = false;
			 
			 if(data.size() > 0)
			 {
				 JsonObject js = data;
				
				 //JsonObject pmtId = js.get("pmtId").getAsJsonObject();
				
				 transactionId  = header.has("sysRefNb") ? header.get("sysRefNb").getAsString() : "";
				
				 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
				 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
				 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
				 
				 JsonObject dbtr = js.get("dbtr").getAsJsonObject();
				
				 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.getAsJsonObject("dbtrAcct") : new JsonObject();
				 senderAccountNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "NA";

				 
				 if(util.isNullOrEmpty(senderAccountNumber))
				 {
					 senderAccountNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
				 }
				  
				 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject(); 
				 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : ""; 

				 JsonObject pstlAdr = dbtr.has("pstlAdr") ? dbtr.get("pstlAdr").getAsJsonObject() : new JsonObject();
				 recipientCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";

				JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
				recipientAccountOrWalletNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";

				if(util.isNullOrEmpty(recipientAccountOrWalletNumber))
				 {
					recipientAccountOrWalletNumber = cdtrAcct.has("IBAN") ? cdtrAcct.get("IBAN").getAsString() : "";
				 }
				
				 	
				JsonObject chanl = js.has("chanl") ? js.get("chanl").getAsJsonObject() : new JsonObject();
				serviceChannel = chanl.has("instgChanl") ? chanl.get("instgChanl").getAsString() : "";
 
				 if(serviceChannel.equalsIgnoreCase("IBK"))
				 {
					 serviceChannel = "Internet banking";
				 }
				 else if (serviceChannel.equalsIgnoreCase("RCP") || serviceChannel.equalsIgnoreCase("SIC") || serviceChannel.equalsIgnoreCase("SCC")) 
				 {
					serviceChannel = "Branch";
				 }
				 else 
				 {
					serviceChannel = "Online payment";
				 }
				 
				 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
				 
				 
				 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
				
				 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
				 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
				 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
				 String casaSgmntIdr = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
				 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
				 
				 if(casaSgmntIdr.equals("C"))
				 { 
					 Identityno = cstmr.has("sciLeId") ? cstmr.get("sciLeId").getAsString() : "";
					 corp = true;
				 }
				 else
				 {
					 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
				 }
				 
				 serviceSubCategory = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
				 
				 if(util.isNullOrEmpty(serviceSubCategory))
				 {
					 JsonObject CRDT = enqryInf.has("CRDT") ? enqryInf.get("CRDT").getAsJsonObject() : new JsonObject();	
					 serviceSubCategory = CRDT.has("casaSgmntIdr") ? CRDT.get("casaSgmntIdr").getAsString() : "";
				 }
				 
				 JsonObject  amt = js.get("amt").getAsJsonObject();
				 currency = amt.get("intrBkSttlmAmtCcy").getAsString();
				 
				 orgAmount = amt.get("intrBkSttlmAmt").getAsString();
				 
				 usdAmount = amt.get("cdtAmtUSDEqvt").getAsString();
				 
				 if(util.isNullOrEmpty(usdAmount))
				 {
					 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
				 }
				 
				 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
				 
				
				 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
				
				 
				 JsonObject rmtInf  = js.get("rmtInf").getAsJsonObject();
				 JsonArray instrInf  = rmtInf.has("instrInf") ? rmtInf.get("instrInf").getAsJsonArray()  : new JsonArray(); 
				 if(instrInf.size()>0)
				 {
					 JsonObject jsonObj = instrInf.get(0).getAsJsonObject();
					  senderInstruction = jsonObj.has("desc")  ? jsonObj.get("desc").getAsString()  : "NA"; 
				 }	
				 
				 senderInstruction = util.isNullOrEmpty(senderInstruction) ? "NA" : senderInstruction ;
				 
				 //String clrMsgTp = header.has("clrMsgTp") ? header.get("clrMsgTp").getAsString() : "";
				 JsonObject cdtrAgt = js.has("cdtrAgt") ? js.get("cdtrAgt").getAsJsonObject() : new JsonObject();
				 
				 //if(clrMsgTp.equalsIgnoreCase("PACS.008%") || clrMsgTp.equalsIgnoreCase("MT103"))
				 //{					 
					 recipientBankOrFspCode = cdtrAgt.has("BICFI") ? !cdtrAgt.get("BICFI").isJsonNull() ? cdtrAgt.get("BICFI").getAsString() : ""  : "";
				 //}
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode))
				 {
					 recipientBankOrFspCode = cdtrAgt.has("clrSysMmbId") ? !cdtrAgt.get("clrSysMmbId").isJsonNull() ? cdtrAgt.get("clrSysMmbId").getAsString() : ""  : "";
				 }	
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode))
				 {
					 JsonObject instdAgt = js.has("instdAgt") ? js.get("instdAgt").getAsJsonObject() : new JsonObject();
					 
					 recipientBankOrFspCode = instdAgt.has("BICFI") ? !instdAgt.get("BICFI").isJsonNull() ? instdAgt.get("BICFI").getAsString() : ""  : "";
				 }
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode))
				 {
					 //if(clrMsgTp.equalsIgnoreCase("PACS.009%") || clrMsgTp.equalsIgnoreCase("MT202"))
					 //{
						 recipientBankOrFspCode = cdtr.has("orgId") ? !cdtr.get("orgId").isJsonNull() ? cdtr.get("orgId").getAsString() : ""  : "";
					 //}
				 }
			 }
			 
			 if(payment_type.equalsIgnoreCase("RTGS"))
			 {
				 transferChannel  = "SWIFT";
				 serviceCategory = "Cross border Payments";
				 serviceSubCategory = "Payments";
			 }
			 
			String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
				
			transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";	
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
				
			recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
				
			serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
				
			serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "9" : "9";
				
			serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

			Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

			purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "32103";
			
			purposes = transferChannel.equals("3") ? "32103" : "";
			
			transactionPlace = "218";
			
			if(corp) // corporate client
			{
				 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
				 
				 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
				 
				 senderIdentificationType = "12";
				 senderIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
			}
			else // Individual client
			{
				sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
						   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
						   "and rownum = ?";
					 
				Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
				
				if(Info.size() > 0)
				{
					 senderIdentificationType = Info.get(0).getCOLUMN1();
					 senderIdentificationNumber = Info.get(0).getCOLUMN2();
					
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
						
					 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
				}
				else
				{
					 senderIdentificationType = "1";
					 senderIdentificationNumber = "NA";
				}
			}
					
			 if(recipientBankOrFspCode.length() == 3)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			 else if(recipientBankOrFspCode.length() == 6)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }

			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
	
			 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
			
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
					") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
					 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
					 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
					 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
					 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
		 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
					 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
					 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
					 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
					 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_SC_rtgs" });	 
		    
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
			 
			 logger.error("Error processing OutgoingFundTransfer_SCPAY: "+e.getLocalizedMessage());  
		 }
		
		 return details;
	}
	
	public JsonObject OutgoingFundTransfer_SCPAY_TT(String INFO1, String INFO2, String INFO3) //RTS197 //DONE
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject header = data_.get("header").getAsJsonObject();
			 
			 String payment_type = header.get("pmtTp").getAsString();
			 
			 String subPmtTp = header.has("subPmtTp") ? header.get("subPmtTp").getAsString() : "";
		
			 JsonObject data = data_.get("data").getAsJsonObject();
			 		 
			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = "";
			 String transactionDate = "";
			 String transferChannel = header.get("instgChanl").getAsString();
			 String subCategoryTransferChannel = ""; //non mandatory
			 String senderAccountNumber = "";
			 String senderIdentificationType = "";
			 String senderIdentificationNumber = "";
			 String recipientName = "";
			 String recipientMobileNumber = "NA";
			 String recipientCountry = "";
			 String recipientBankOrFspCode = ""; 
			 String recipientAccountOrWalletNumber = "";
			 String serviceChannel = "";
			 String serviceCategory = "";
			 String serviceSubCategory = "";		
			 String orgAmount = "";
			 String currency = "";
			 String usdAmount = "";
			 String tzsAmount = "";
			 String purposes = "";
			 String senderInstruction = "NA";
			 String transactionPlace = "";

			 String Identityno = ""; boolean corp = false;
			 
			 if(data.size() > 0)
			 {
				 JsonObject js = data;
				 JsonObject js_Header = header;
				
				  transactionId = js_Header.has("sysRefNb") ? js_Header.get("sysRefNb").getAsString() : "";

				
				 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
				 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
				 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
				 
				 int j = 0;
				 JsonObject txSttlmInf = js.has("txSttlmInf") ? js.get("txSttlmInf").getAsJsonObject() : new JsonObject();
				 JsonArray cdtSttlmAcct = txSttlmInf.has("cdtSttlmAcct") ? txSttlmInf.get("cdtSttlmAcct").getAsJsonArray() : new JsonArray();
				 JsonObject acct = (cdtSttlmAcct.size() > j) ? cdtSttlmAcct.get(j).getAsJsonObject() : new JsonObject();		
				 
				 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
				 
				 if(subPmtTp.equalsIgnoreCase("IR"))
				 {
					 senderAccountNumber = acct.has("acctId") ? acct.get("acctId").getAsString() : "";					 
				 }
				 else 
				 {
					 senderAccountNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";
				 }
				 
				 if(util.isNullOrEmpty(senderAccountNumber))
				 {
					 senderAccountNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
				 }
				 				 
				 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject();
				 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "NA";

				 JsonObject pstlAdr = cdtr.has("pstlAdr") ? cdtr.get("pstlAdr").getAsJsonObject() : new JsonObject();
				 recipientCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";

				 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
				 
				 j = 0;
				 
				 JsonArray dbtSttlmAcct = txSttlmInf.has("dbtSttlmAcct") ? txSttlmInf.get("dbtSttlmAcct").getAsJsonArray() : new JsonArray();
				 JsonObject acct1 = (dbtSttlmAcct.size() > j) ? dbtSttlmAcct.get(j).getAsJsonObject() : new JsonObject();		
				 
				 if(subPmtTp.equalsIgnoreCase("IR"))
				 {
					 recipientAccountOrWalletNumber = acct1.has("acctId") ? acct.get("acctId").getAsString() : "";					 
				 }
				 else 
				 {
					 recipientAccountOrWalletNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";
				 }
				 
				if(util.isNullOrEmpty(recipientAccountOrWalletNumber))
				 {
					recipientAccountOrWalletNumber = cdtrAcct.has("IBAN") ? cdtrAcct.get("IBAN").getAsString() : "";
				 }
				
				 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
				 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
				 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
				 String casaSgmntIdr = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
				 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
				 
				 if(casaSgmntIdr.equals("C"))
				 { 
					 Identityno = cstmr.has("sciLeId") ? cstmr.get("sciLeId").getAsString() : "";
					 corp = true;
				 }
				 else
				 {
					 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
				 }
				 	 
				  JsonObject chanl = js.has("chanl") ? js.get("chanl").getAsJsonObject() : new JsonObject();
				  serviceChannel = chanl.has("instgChanl") ? chanl.get("instgChanl").getAsString() : "";

				 if(serviceChannel.equalsIgnoreCase("IBK"))
				 {
					 serviceChannel = "Internet banking";
				 }
				 else if (serviceChannel.equalsIgnoreCase("RCP") || serviceChannel.equalsIgnoreCase("SIC") || serviceChannel.equalsIgnoreCase("SCC")) 
				 {
					serviceChannel = "Branch";
				 }
				 else 
				 {
					serviceChannel = "Online payment";
				 }
				 
				 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
				 
				 String pmtTp = js_Header.get("pmtTp").getAsString();
				 
				 if(pmtTp.equals("TT") && subPmtTp.equals("OT")) 
				 {
					 serviceSubCategory = "Customer to Business (C2B)";
				 }
				 else if(pmtTp.equals("TT") && subPmtTp.equals("FT")) 
				 {
					 serviceSubCategory = "Business to Business (B2B)";
				 }
				 
				 JsonObject  amt = js.get("amt").getAsJsonObject();
				 currency = amt.has("intrbkSttlmAmtCcy") ? amt.get("intrbkSttlmAmtCcy").getAsString(): "";
				 
				 orgAmount = amt.has("intrBkSttlmAmt") ? amt.get("intrBkSttlmAmt").getAsString(): "";
				 
				 usdAmount = amt.has("cdtAmtUSDEqvt") ? amt.get("cdtAmtUSDEqvt").getAsString():"";
				 
				 if(util.isNullOrEmpty(usdAmount))
				 {
					 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
				 }
				 
				 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
				 
				 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
				 
				 String clrMsgTp = header.has("clrMsgTp") ? header.get("clrMsgTp").getAsString(): "";
				 
				 JsonObject cdtrAgt = js.has("cdtrAgt") ? js.get("cdtrAgt").getAsJsonObject() : new JsonObject();
		 
				 if(clrMsgTp.equalsIgnoreCase("PACS.008%") || clrMsgTp.equalsIgnoreCase("MT103"))
				 {					 			 
					 recipientBankOrFspCode = cdtrAgt.has("BICFI") ? !cdtrAgt.get("BICFI").isJsonNull() ? cdtrAgt.get("BICFI").getAsString() : ""  : "";
				 }
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode))
				 {
					 recipientBankOrFspCode = cdtrAgt.has("clrSysMmbId") ? !cdtrAgt.get("clrSysMmbId").isJsonNull() ? cdtrAgt.get("clrSysMmbId").getAsString() : ""  : "";
				 }	
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode))
				 {
					 JsonObject instdAgt = js.has("instdAgt") ? js.get("instdAgt").getAsJsonObject() : new JsonObject();					 
					 recipientBankOrFspCode = instdAgt.has("BICFI") ? !instdAgt.get("BICFI").isJsonNull() ? instdAgt.get("BICFI").getAsString() : ""  : "";
				 }
				 
				 if(util.isNullOrEmpty(recipientBankOrFspCode))
				 {
					 if(clrMsgTp.equalsIgnoreCase("PACS.009%") || clrMsgTp.equalsIgnoreCase("MT202"))
					 {
						 recipientBankOrFspCode = cdtr.has("orgId") ? !cdtr.get("orgId").isJsonNull() ? cdtr.get("orgId").getAsString() : ""  : "";
					 }
				 }
				 
				 JsonObject rmtInf = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();
				 
				 JsonArray instrInf  = rmtInf.has("instrInf") ? rmtInf.get("instrInf").getAsJsonArray()  : new JsonArray(); 
				 
				 if(instrInf.size()>0)
				 {
					 JsonObject jsonObj = instrInf.get(0).getAsJsonObject();
					 senderInstruction = jsonObj.has("desc") ? jsonObj.get("desc").getAsString()  : "NA"; 	  
					  
				 }	
				 
				 if(util.isNullOrEmpty(senderInstruction)) 
				 {
					 senderInstruction = rmtInf.has("Ustrd") ? rmtInf.get("Ustrd").getAsString() : "NA";
				 }
			 }
			 
			 if(payment_type.equalsIgnoreCase("TT"))
			 {
				 transferChannel  = "SWIFT";
				 serviceCategory = "Cross border Payments";
			 }

			String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
				
			transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";	
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
				
			recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
				
			serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
				
			serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";
				
			serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			
			sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

			Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

			purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "32103";
			
			purposes = transferChannel.equals("3") ? "32103" : "";
			
			transactionPlace = "218";
				
			if(corp) // corporate client
			{
				 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
				 
				 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
				 
				 senderIdentificationType = "12";
				 senderIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
			}
			else // Individual client
			{
				sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
						   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
						   "and rownum = ?";
					 
				Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
				
				if(Info.size() > 0)
				{
					 senderIdentificationType = Info.get(0).getCOLUMN1();
					 senderIdentificationNumber = Info.get(0).getCOLUMN2();
					
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
						
					 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
				}
				else
				{
					 senderIdentificationType = "1";
					 senderIdentificationNumber = "NA";
				}
			}
			
			 if(recipientBankOrFspCode.length() == 3)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			 else if(recipientBankOrFspCode.length() == 6)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
			 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }

			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
	
			 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
			
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
					") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
					 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
					 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
					 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
					 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
		 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
					 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
					 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
					 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
					 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_sc_TT" });	 
		    
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
			 
			 logger.error("Error processing OutgoingFundTransfer_SCPAY: "+e.getLocalizedMessage());  
		 }
		
		 return details;
	}
	
	public JsonObject IncomingFundTransfer_SCPAY_Rtgs(String INFO1, String INFO2, String INFO3) //RTS195 //DONE
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject header = data_.get("header").getAsJsonObject();
			 
			 String payment_type = header.get("pmtTp").getAsString();
			 
			 JsonObject data =  data_.get("data").getAsJsonObject();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String transactionId = "";
			 String transactionDate = "";
			 String transferChannel =  header.get("instgChanl").getAsString();
			 String subCategoryTransferChannel = ""; //non mandatory
			 String recipientName = "";
			 String senderAccountNumber = "";
			 String recipientIdentificationType = ""; //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
			 String recipientIdentificationNumber = "";   //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
			 String senderCountry = "";
			 String serviceCategory = "";
			 String serviceSubCategory = "";  
			 String senderName = "";
			 String senderBankOrFspCode = "";  
			 String senderAccountOrWalletNumber = ""; 
			 String currency = "";
			 String orgAmount = "";
			 String usdAmount = "";
			 String tzsAmount = "";
			 String purposes = "";  
			 String senderInstruction = "NA";

			 String Identityno = ""; boolean corp = false;
			 
			 if(data.size() > 0)
			 {				 
				 JsonObject js = data;
				 
				 transactionId  = header.has("sysRefNb") ? header.get("sysRefNb").getAsString() : "";
				 
				 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
				 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
				 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
				 			
				 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject(); 
				 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "NA";

				 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
				 senderAccountNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";
				 
				 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
				 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
				 JsonObject CRDT = enqryInf.has("CRDT") ? enqryInf.get("CRDT").getAsJsonObject() : new JsonObject();
				 String casaSgmntIdr = CRDT.has("casaSgmntIdr") ? CRDT.get("casaSgmntIdr").getAsString() : "";
				 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
				 JsonObject cdtAcctSNASInf = cstmr.has("cdtAcctSNASInf") ? cstmr.get("cdtAcctSNASInf").getAsJsonObject() : new JsonObject();	

				 if(casaSgmntIdr.equals("C"))
				 { 
					 Identityno = cdtAcctSNASInf.has("sciLeId") ? cdtAcctSNASInf.get("sciLeId").getAsString() : "";
					 corp = true;
				 }
				 else
				 {
					 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
				 }
				 
				 JsonObject dbtr = js.has("dbtr") ? js.get("dbtr").getAsJsonObject() : new JsonObject();
				 JsonObject pstlAdr = dbtr.has("pstlAdr") ? dbtr.get("pstlAdr").getAsJsonObject() : new JsonObject();
				 senderCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";

				 senderName = dbtr.has("nm") ? dbtr.get("nm").getAsString() : "";

				 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();

				 if(util.isNullOrEmpty(senderName))
				 {
					 senderName = DBIT.has("cstmrNm") ? DBIT.get("cstmrNm").getAsString() : "" ;
				 }
				 
				 JsonObject instdAgt = js.has("instdAgt") ? js.get("instdAgt").getAsJsonObject() : new JsonObject();					 
				 senderBankOrFspCode = instdAgt.has("BICFI") ? !instdAgt.get("BICFI").isJsonNull() ? instdAgt.get("BICFI").getAsString() : ""  : "";

				 JsonObject dbtrAgt = js.has("dbtrAgt") ? js.get("dbtrAgt").getAsJsonObject() : new JsonObject();
				 
				 if(util.isNullOrEmpty(senderBankOrFspCode))
				 {				
					 senderBankOrFspCode = dbtrAgt.has("BICFI") ? !dbtrAgt.get("BICFI").isJsonNull() ? dbtrAgt.get("BICFI").getAsString() : ""  : "";

				 }
				 				 
				 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
				 senderAccountOrWalletNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";

				 
				 if(util.isNullOrEmpty(senderAccountOrWalletNumber))
				 {
					 senderAccountOrWalletNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
				 }
				 
				 serviceSubCategory = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
				 if(util.isNullOrEmpty(serviceSubCategory))
				 {
					 serviceSubCategory = CRDT.has("casaSgmntIdr") ? CRDT.get("casaSgmntIdr").getAsString() : "";
				 }
				 
				 JsonObject  amt = js.get("amt").getAsJsonObject();
				 currency = amt.get("intrBkSttlmAmtCcy").getAsString();
				 
				 orgAmount = amt.get("instdAmt").getAsString();
				 
				 if(util.isNullOrEmpty(orgAmount))
				 {
					 orgAmount = amt.get("intrBkSttlmAmt").getAsString();
				 }
				 
				 usdAmount = amt.get("cdtAmtUSDEqvt").getAsString();
				 
				 tzsAmount = amt.get("intrBkSttlmAmt").getAsString();
				 
				 if(util.isNullOrEmpty(tzsAmount))
				 {
					 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
				 }
				 
				 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
				 	 
				 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
				 
				 JsonObject rmtInf  = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();
				 
				 JsonArray instrInf  = rmtInf.has("instrInf") ? rmtInf.get("instrInf").getAsJsonArray()  : new JsonArray();
					 
				 if(instrInf.size()>0)
				 {
					 JsonObject jsonObj = instrInf.get(0).getAsJsonObject(); 
					 senderInstruction = jsonObj.has("desc")  ? jsonObj.get("desc").getAsString()  : ""; 
					 			 
				 }
				 senderInstruction = util.isNullOrEmpty(senderInstruction) ? "NA" : senderInstruction;
			
			 }
			 
			 if(payment_type.equalsIgnoreCase("RTGS"))
			 {
				 transferChannel  = "SWIFT";
				 serviceCategory = "Internet banking";
				 serviceSubCategory = "Payments";
			 }
			 
			 if(util.isNullOrEmpty(serviceCategory))
			 {
				 serviceCategory = "Cross border Payments";
				 if(serviceSubCategory.equalsIgnoreCase("c"))
				 {
					 serviceSubCategory = "Business to Business (B2B)";
				 }
				 else 
				 {
					 serviceSubCategory = "Customer to Business (C2B)";
				 }
			 }
			 
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());			 
			 
			 transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", senderCountry }, new Lookup001_mapper());
				
			 senderCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
		 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
		 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
				
			 serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "9" : "9";
				
			 serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
						
			sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

			Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

			purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "22103";
			
			purposes = transferChannel.equals("3") ? "22103" : "";
			
			if(corp) // corporate client
			{
				 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
				 
				 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
				 
				 recipientIdentificationType = "12";
				 recipientIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
			}
			else // Individual client
			{
				sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
						   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
						   "and rownum = ?";
					 
				Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
				
				if(Info.size() > 0)
				{
					 recipientIdentificationType = Info.get(0).getCOLUMN1();
					 recipientIdentificationNumber = Info.get(0).getCOLUMN2();
					
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", recipientIdentificationType }, new Lookup001_mapper());
						
					 recipientIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
				}
				else
				{
					recipientIdentificationType = "1";
					recipientIdentificationNumber = "NA";
				}
			}
			
			 if(senderBankOrFspCode.length() == 3)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }
			 else if(senderBankOrFspCode.length() == 6)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }
			 else if(senderBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode.substring(0, 8);
				 
				 if(senderBankOrFspCode.length() == 8)
				 {
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
					 
					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
				 }
			 }
			 else if(senderBankOrFspCode.length() == 8)
			 {
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
				 
				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
			 }
			 else 
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }
	
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 count++;
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
			 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
			 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
			 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "incomingBankFundsTransfer", count, reportingDate, transactionId,
						 transactionDate, transferChannel, subCategoryTransferChannel, recipientName, senderAccountNumber,
						 recipientIdentificationType, recipientIdentificationNumber, senderCountry, senderName, senderBankOrFspCode,
						 senderAccountOrWalletNumber, serviceCategory, serviceSubCategory, currency, orgAmount, usdAmount,
						 tzsAmount, purposes, senderInstruction});
 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "incomingBankFundsTransfer"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "incomingBankFundsTransfer", "serial", "reportingDate", 
					 "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel", "recipientName", "senderAccountNumber", 
					 "recipientIdentificationType", "recipientIdentificationNumber", "senderCountry", "senderName", "senderBankOrFspCode",
					 "senderAccountOrWalletNumber", "serviceCategory", "serviceSubCategory", "currency", "orgAmount", "usdAmount", "tzsAmount",
					 "purposes", "senderInstruction"});
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "incomingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_SC_rtgs" });	 
		     
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
			 
			 logger.error("Error processing IncomingFundTransfer_SCPAY", e.getMessage()); 
		 }
		
		 return details;
	}
	
	 public JsonObject IncomingFundTransfer_SCPAY_TT(String INFO1, String INFO2, String INFO3) //RTS195 //DONE
		{ 
			 JsonObject details = new JsonObject();
			 
			 try
			 {
				 Common_Utils util = new Common_Utils();
				 
				 JsonObject data_ = util.StringToJsonObject(INFO3);
				
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 		
				 JsonObject header = data_.get("header").getAsJsonObject();
				 
				 String payment_type = header.get("pmtTp").getAsString();
				 
				// JsonArray data = data_.get("data").getAsJsonArray();
				 
				 JsonObject data =  data_.get("data").getAsJsonObject();
				 
				 String reportingDate = util.getCurrentReportDate();
				 String transactionId = "";
				 String transactionDate = "";
				 String transferChannel =  header.get("instgChanl").getAsString();
				 String subCategoryTransferChannel = ""; //non mandatory
				 String recipientName = "";
				 String senderAccountNumber = "";
				 String recipientIdentificationType = "1"; //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
				 String recipientIdentificationNumber = "1";   //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
				 String senderCountry = "";
				 String serviceCategory = "";
				 String serviceSubCategory = "";  
				 String senderName = "";
				 String senderBankOrFspCode = "";  
				 String senderAccountOrWalletNumber = ""; 
				 String currency = "";
				 String orgAmount = "";
				 String usdAmount = "";
				 String tzsAmount = "";
				 String purposes = "";  
				 String senderInstruction = "NA";

				 String Identityno = ""; boolean corp = false;
				 
				 if(data.size() > 0)
				 {				 
					 JsonObject js = data;
					 JsonObject js_Header = header;
					 
//					 JsonObject pmtId = js_Header.get("header").getAsJsonObject();
					 
					 transactionId  =js_Header.has("sysRefNb") ? js_Header.get("sysRefNb").getAsString() : "";
					 
					 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
					 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
					 					 
					 
					 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject();
					 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "";

					 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";

					 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
					 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
					 JsonObject CRDT = enqryInf.has("CRDT") ? enqryInf.get("CRDT").getAsJsonObject() : new JsonObject();
					 String casaSgmntIdr = CRDT.has("casaSgmntIdr") ? CRDT.get("casaSgmntIdr").getAsString() : "";
					 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
					 JsonObject cdtAcctSNASInf = cstmr.has("cdtAcctSNASInf") ? cstmr.get("cdtAcctSNASInf").getAsJsonObject() : new JsonObject();	

					 if(casaSgmntIdr.equals("C"))
					 { 
						 Identityno = cdtAcctSNASInf.has("sciLeId") ? cdtAcctSNASInf.get("sciLeId").getAsString() : "";
						 corp = true;
					 }
					 else
					 {
						 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
					 }
					 
					 JsonObject dbtr = js.has("dbtr") ? js.get("dbtr").getAsJsonObject() : new JsonObject();
					 JsonObject pstlAdr = dbtr.has("pstlAdr") ? dbtr.get("pstlAdr").getAsJsonObject() : new JsonObject();

					 senderCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";
					 senderName = dbtr.has("nm") ? dbtr.get("nm").getAsString() : "NA";

					 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
					 
					 if(util.isNullOrEmpty(senderName))
					 {
						 senderName = DBIT.has("cstmrNm") ? DBIT.get("cstmrNm").getAsString() : "NA" ;
					 }
						
					 JsonObject instdAgt = js.has("instdAgt") ? js.get("instdAgt").getAsJsonObject() : new JsonObject();					 
					 senderBankOrFspCode = instdAgt.has("BICFI") ? !instdAgt.get("BICFI").isJsonNull() ? instdAgt.get("BICFI").getAsString() : ""  : "";

					 
					 JsonObject dbtrAgt = js.has("dbtrAgt") ? js.get("dbtrAgt").getAsJsonObject() : new JsonObject();
					 if(util.isNullOrEmpty(senderBankOrFspCode))
					 {				
						 senderBankOrFspCode = dbtrAgt.has("BICFI") ? !dbtrAgt.get("BICFI").isJsonNull() ? dbtrAgt.get("BICFI").getAsString() : ""  : "";
					 }
						 
					 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountOrWalletNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";
					 
					 if(util.isNullOrEmpty(senderAccountOrWalletNumber))
					 {
						 senderAccountOrWalletNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
					 }			 
					 
					 JsonObject  amt = js.get("amt").getAsJsonObject();
					 
					 currency = amt.get("intrBkSttlmAmtCcy").getAsString();
					 				 
					 orgAmount = amt.has("intrBkSttlmAmt") ? amt.get("intrBkSttlmAmt").getAsString(): "";
					 
					 usdAmount = amt.has("cdtAmtUSDEqvt") ? amt.get("cdtAmtUSDEqvt").getAsString():"";
					 
					 if(util.isNullOrEmpty(usdAmount))
					 {
						 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
					 }
					 
					 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
					 					
					 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
					 
					 purposes = purp.has("rcvrPurpCd") ? purp.get("rcvrPurpCd").getAsString() : "";
					 
					 JsonObject rmtInf = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();
					 
					 JsonArray instrInf  = rmtInf.has("instrInf") ? rmtInf.get("instrInf").getAsJsonArray()  : new JsonArray();
						 
					 if(instrInf.size()>0)
					 {
						 JsonObject instrObj = instrInf.size() > 0 ? instrInf.get(0).getAsJsonObject() : new JsonObject();
						 senderInstruction = instrObj.has("desc") ? instrObj.get("desc").getAsString() : "";
					 }
					 
					 senderInstruction = util.isNullOrEmpty(senderInstruction) ? "NA" : senderInstruction;
					 
					 if(util.isNullOrEmpty(senderInstruction)) 
					 {
						 senderInstruction = rmtInf.has("Ustrd") ? rmtInf.get("Ustrd").getAsString():"NA";					 
					 }
					
					 String pmtTp  = js_Header.get("pmtTp").getAsString();
					 String subPmtTp  = js_Header.get("subPmtTp").getAsString();
					 
					 if(pmtTp.equals("TT") && subPmtTp.equals("IT"))
					 {
						 serviceSubCategory = "Customer to Business";
					 }
					 else if(pmtTp.equals("TT") && subPmtTp.equals("FI")) {
						 serviceSubCategory = "Business to Business";
					 }
					 
					 if(payment_type.equalsIgnoreCase("TT"))
					 {
						 transferChannel  = "SWIFT";
						 serviceCategory = "Cross border Payments";
					 }
				 }
				 
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());			 
				 
				 transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "3" : "3";
				 		
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
					
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", senderCountry }, new Lookup001_mapper());
					
				 senderCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			 
				 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
			 
				 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
					
				 serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";
					
				 serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
							
				sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "22103";
				
				purposes = transferChannel.equals("3") ? "22103" : "";

				if(corp) // corporate client
				{
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
					 
					 recipientIdentificationType = "12";
					 recipientIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
				}
				else // Individual client
				{
					sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
							   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
							   "and rownum = ?";
						 
					Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
					
					if(Info.size() > 0)
					{
						 recipientIdentificationType = Info.get(0).getCOLUMN1();
						 recipientIdentificationNumber = Info.get(0).getCOLUMN2();
						
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", recipientIdentificationType }, new Lookup001_mapper());
							
						 recipientIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
					}
					else
					{
						recipientIdentificationType = "1";
						recipientIdentificationNumber = "NA";
					}
				}
				
				 if(senderBankOrFspCode.length() == 3)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
				 else if(senderBankOrFspCode.length() == 6)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
			 else if(senderBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode.substring(0, 8);
				 
				 if(senderBankOrFspCode.length() == 8)
				 {
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
					 
					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
				 }
			 }
				 
			 else if(senderBankOrFspCode.length() == 8)
			 {
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
				 
				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
			 }
			 else 
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }

				
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "incomingBankFundsTransfer", count, reportingDate, transactionId,
							 transactionDate, transferChannel, subCategoryTransferChannel, recipientName, senderAccountNumber,
							 recipientIdentificationType, recipientIdentificationNumber, senderCountry, senderName, senderBankOrFspCode,
							 senderAccountOrWalletNumber, serviceCategory, serviceSubCategory, currency, orgAmount, usdAmount,
							 tzsAmount, purposes, senderInstruction});
	 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "incomingBankFundsTransfer"});
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
					 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "incomingBankFundsTransfer", "serial", "reportingDate", 
						 "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel", "recipientName", "senderAccountNumber", 
						 "recipientIdentificationType", "recipientIdentificationNumber", "senderCountry", "senderName", "senderBankOrFspCode",
						 "senderAccountOrWalletNumber", "serviceCategory", "serviceSubCategory", "currency", "orgAmount", "usdAmount", "tzsAmount",
						 "purposes", "senderInstruction"});
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "incomingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_sc_TT" });	 
			     
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
				 
				 logger.error("Error processing IncomingFundTransfer_SCPAY", e.getMessage()); 
			 }
			
			 return details;
		}
	 
	 
	 public JsonObject IncomingFundTransfer_SCPAY_ACH(String INFO1, String INFO2, String INFO3) //RTS195 //DONE
	 { 
			 JsonObject details = new JsonObject();
			 
			 try
			 {
				 Common_Utils util = new Common_Utils();
				 
				 JsonObject data_ = util.StringToJsonObject(INFO3);
				
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 		
				 JsonObject header = data_.get("header").getAsJsonObject();
				 
				 String payment_type = header.get("pmtTp").getAsString();
				 
				// JsonArray data = data_.get("data").getAsJsonArray();
				 
				 JsonObject data =  data_.get("data").getAsJsonObject();
				 
				 String reportingDate = util.getCurrentReportDate();
				 String transactionId = "";
				 String transactionDate = "";
				 String transferChannel =  header.get("instgChanl").getAsString();
				 String subCategoryTransferChannel = ""; //non mandatory
				 String recipientName = "";
				 String senderAccountNumber = "";
				 String recipientIdentificationType = ""; //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
				 String recipientIdentificationNumber = "";   //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
				 String senderCountry = "";
				 String serviceCategory = "";
				 String serviceSubCategory = "";  
				 String senderName = "";
				 String senderBankOrFspCode = "";  
				 String senderAccountOrWalletNumber = ""; 
				 String currency = "";
				 String orgAmount = "";
				 String usdAmount = "";
				 String tzsAmount = "";
				 String purposes = "";  
				 String senderInstruction = "NA";

				 String Identityno = ""; boolean corp = false;
				 
				 if(data.size() > 0)
				 {				 
					 JsonObject js = data;
					 JsonObject js_Header = header;
					 
//					 JsonObject pmtId = js_Header.get("header").getAsJsonObject();
					 
					 transactionId  = js_Header.has("sysRefNb") ? js_Header.get("sysRefNb").getAsString() : "";
					 
					 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
					 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
					 	
					 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject();
					 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "";

					 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";
					 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
					 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
					 JsonObject CRDT = enqryInf.has("CRDT") ? enqryInf.get("CRDT").getAsJsonObject() : new JsonObject();
					 String casaSgmntIdr = CRDT.has("casaSgmntIdr") ? CRDT.get("casaSgmntIdr").getAsString() : "";
					 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
					 JsonObject cdtAcctSNASInf = cstmr.has("cdtAcctSNASInf") ? cstmr.get("cdtAcctSNASInf").getAsJsonObject() : new JsonObject();	

					 if(casaSgmntIdr.equals("C"))
					 { 
						 Identityno = cdtAcctSNASInf.has("sciLeId") ? cdtAcctSNASInf.get("sciLeId").getAsString() : "";
						 corp = true;
					 }
					 else
					 {
						 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
					 }
					 
					 JsonObject dbtr = js.has("dbtr") ? js.get("dbtr").getAsJsonObject() : new JsonObject();
					 JsonObject pstlAdr = dbtr.has("pstlAdr") ? dbtr.get("pstlAdr").getAsJsonObject() : new JsonObject();

					 senderCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";
					 senderName = dbtr.has("nm") ? dbtr.get("nm").getAsString() : "NA";

					 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
					 
					 if(util.isNullOrEmpty(senderName))
					 {
						 senderName = DBIT.has("cstmrNm") ? DBIT.get("cstmrNm").getAsString() : "NA" ;
					 }
					
					 JsonObject instdAgt = js.has("instdAgt") ? js.get("instdAgt").getAsJsonObject() : new JsonObject();					 
					 senderBankOrFspCode = instdAgt.has("BICFI") ? !instdAgt.get("BICFI").isJsonNull() ? instdAgt.get("BICFI").getAsString() : ""  : "";

					 
					 JsonObject dbtrAgt = js.has("dbtrAgt") ? js.get("dbtrAgt").getAsJsonObject() : new JsonObject();
					 if(util.isNullOrEmpty(senderBankOrFspCode))
					 {				
						 senderBankOrFspCode = dbtrAgt.has("BICFI") ? !dbtrAgt.get("BICFI").isJsonNull() ? dbtrAgt.get("BICFI").getAsString() : ""  : "";
					 }
										 
					 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountOrWalletNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";
					 
					 if(util.isNullOrEmpty(senderAccountOrWalletNumber))
					 {
						 senderAccountOrWalletNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
					 }
					  
					 JsonObject  amt = js.get("amt").getAsJsonObject();
					 currency = amt.get("intrBkSttlmAmtCcy").getAsString();
					
					 orgAmount = amt.has("intrBkSttlmAmt") ? amt.get("intrBkSttlmAmt").getAsString():"";
					
					 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
					 
					 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
						 
					 JsonObject purp  =  js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
					 
					 purposes = purp.has("rcvrPurpCd") ? purp.get("rcvrPurpCd").getAsString() : "";
					 		
					 JsonObject rmtInf = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();
					 
					 JsonArray instrInf  = rmtInf.has("instrInf") ? rmtInf.get("instrInf").getAsJsonArray()  : new JsonArray();
						 
					 if(instrInf.size()>0)
					 {
						 JsonObject instrObj = instrInf.size() > 0 ? instrInf.get(0).getAsJsonObject() : new JsonObject();
						 senderInstruction = instrObj.has("desc") ? instrObj.get("desc").getAsString() : "NA";
					 }
					 
					 senderInstruction = util.isNullOrEmpty(senderInstruction) ? "NA" : senderInstruction;
					
					 if(payment_type.equalsIgnoreCase("ACH"))
					 {
						 transferChannel  = "EFT";
						 serviceCategory = "Internet banking";
						 serviceSubCategory = "Payments";
					 }
					 
				 }
				 
				 
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());			 
				 
				 transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "7" : "7";
				 			
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
					
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", senderCountry }, new Lookup001_mapper());
					
				 senderCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			 
				 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
			 
				 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
					
				 serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "9" : "9";
					
				 serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
							
				sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "22103";
				
				purposes = transferChannel.equals("3") ? "22103" : "";

				if(corp) // corporate client
				{
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
					 
					 recipientIdentificationType = "12";
					 recipientIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
				}
				else // Individual client
				{
					sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
							   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
							   "and rownum = ?";
						 
					Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
					
					if(Info.size() > 0)
					{
						 recipientIdentificationType = Info.get(0).getCOLUMN1();
						 recipientIdentificationNumber = Info.get(0).getCOLUMN2();
						
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", recipientIdentificationType }, new Lookup001_mapper());
							
						 recipientIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
					}
					else
					{
						recipientIdentificationType = "1";
						recipientIdentificationNumber = "NA";
					}
				}
				
				 if(senderBankOrFspCode.length() == 3)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
				 else if(senderBankOrFspCode.length() == 6)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
				 else if(senderBankOrFspCode.length() == 11)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode.substring(0, 8);
					 
					 if(senderBankOrFspCode.length() == 8)
					 {
						 
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
						 
						 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
					 }
				 }
				 else if(senderBankOrFspCode.length() == 8)
				 {
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
					 
					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
				 }
				 else 
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
				
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "incomingBankFundsTransfer", count, reportingDate, transactionId,
							 transactionDate, transferChannel, subCategoryTransferChannel, recipientName, senderAccountNumber,
							 recipientIdentificationType, recipientIdentificationNumber, senderCountry, senderName, senderBankOrFspCode,
							 senderAccountOrWalletNumber, serviceCategory, serviceSubCategory, currency, orgAmount, usdAmount,
							 tzsAmount, purposes, senderInstruction});
	 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "incomingBankFundsTransfer"});
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
					 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "incomingBankFundsTransfer", "serial", "reportingDate", 
						 "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel", "recipientName", "senderAccountNumber", 
						 "recipientIdentificationType", "recipientIdentificationNumber", "senderCountry", "senderName", "senderBankOrFspCode",
						 "senderAccountOrWalletNumber", "serviceCategory", "serviceSubCategory", "currency", "orgAmount", "usdAmount", "tzsAmount",
						 "purposes", "senderInstruction"});
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "incomingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_sc_ACH" });	 
			     
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
				 
				 logger.error("Error processing IncomingFundTransfer_SCPAY", e.getMessage()); 
			 }
			
			 return details;
		}
	 
		public JsonObject OutgoingFundTransfer_SCPAY_ACH(String INFO1, String INFO2, String INFO3) //RTS197 //DONE
		{ 
			 JsonObject details = new JsonObject();
			 
			 try
			 {
				 Common_Utils util = new Common_Utils();
				 
				 JsonObject data_ = util.StringToJsonObject(INFO3);
				
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 		
				 JsonObject header = data_.get("header").getAsJsonObject();
				 
				 String payment_type = header.get("pmtTp").getAsString();
				 
				 JsonObject data = data_.get("data").getAsJsonObject();
				 		 
				 String reportingDate = util.getCurrentReportDate();
				 String transactionId = "";
				 String transactionDate = "";
				 String transferChannel = header.get("instgChanl").getAsString();
				 String subCategoryTransferChannel = ""; //non mandatory
				 String senderAccountNumber = "";
				 String senderIdentificationType = "1";//confusing
				 String senderIdentificationNumber = "1";//confusing
				 String recipientName = "";
				 String recipientMobileNumber = "NA";
				 String recipientCountry = "";
				 String recipientBankOrFspCode = ""; 
				 String recipientAccountOrWalletNumber = "";
				 String serviceChannel = "";
				 String serviceCategory = "";
				 String serviceSubCategory = "";		
				 String orgAmount = "";
				 String currency = "";
				 String usdAmount = "";
				 String tzsAmount = "";
				 String purposes = "";
				 String senderInstruction = "NA";
				 String transactionPlace = "";

				 String Identityno = ""; boolean corp = false;
				 
				 if(data.size() > 0)
				 {
					 JsonObject js = data;
					 JsonObject js_Header = header;
					
					 transactionId = js_Header.has("sysRefNb") ? js_Header.get("sysRefNb").getAsString() : "";

					 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
					 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
					 
					 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";
					 
					 if(util.isNullOrEmpty(senderAccountNumber))
					 {
						 senderAccountNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
					 }
					 				 
					 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject();
					 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "NA";

					 JsonObject pstlAdr = cdtr.has("pstlAdr") ? cdtr.get("pstlAdr").getAsJsonObject() : new JsonObject();
					 recipientCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";

						
					 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
					 recipientAccountOrWalletNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";
					if(util.isNullOrEmpty(recipientAccountOrWalletNumber))
					 {
						recipientAccountOrWalletNumber = cdtrAcct.has("IBAN") ? cdtrAcct.get("IBAN").getAsString() : "";
					 }
					
					 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
					 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
					 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
					 String casaSgmntIdr = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
					 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
					 
					 if(casaSgmntIdr.equals("C"))
					 { 
						 Identityno = cstmr.has("sciLeId") ? cstmr.get("sciLeId").getAsString() : "";
						 corp = true;
					 }
					 else
					 {
						 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
					 }
					 	 
					  JsonObject chanl = js.has("chanl") ? js.get("chanl").getAsJsonObject() : new JsonObject();
					  serviceChannel = chanl.has("instgChanl") ? chanl.get("instgChanl").getAsString() : "";

					 
					 if(serviceChannel.equalsIgnoreCase("IBK"))
					 {
						 serviceChannel = "Internet banking";
					 }
					 else if (serviceChannel.equalsIgnoreCase("RCP") || serviceChannel.equalsIgnoreCase("SIC") || serviceChannel.equalsIgnoreCase("SCC")) 
					 {
						serviceChannel = "Branch";
					 }
					 else 
					 {
						serviceChannel = "Online payment";
					 }
					 
					 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
					 
					 JsonObject  amt = js.get("amt").getAsJsonObject();
					 
					 currency = amt.has("intrbkSttlmAmtCcy") ? amt.get("intrbkSttlmAmtCcy").getAsString(): "";
					 
					 orgAmount = amt.has("intrBkSttlmAmt") ? amt.get("intrBkSttlmAmt").getAsString(): "";
					 
					 usdAmount = amt.has("cdtAmtUSDEqvt") ? amt.get("cdtAmtUSDEqvt").getAsString():"";
					 
					 if(util.isNullOrEmpty(usdAmount))
					 {
						 usdAmount = amt.get("dbtAmtUSDEqvt").getAsString();
					 }
					 
					 tzsAmount = amt.get("dbtAmtBaseCcyEqvt").getAsString();
					 
					
					 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
					 
					 //String clrMsgTp = header.has("clrMsgTp") ? header.get("clrMsgTp").getAsString(): "";
					 
					 JsonObject cdtrAgt = js.has("cdtrAgt") ? js.get("cdtrAgt").getAsJsonObject() : new JsonObject();

					 					 			 
					recipientBankOrFspCode = cdtrAgt.has("BICFI") ? !cdtrAgt.get("BICFI").isJsonNull() ? cdtrAgt.get("BICFI").getAsString() : ""  : "";
					 
					 
					 if(util.isNullOrEmpty(recipientBankOrFspCode))
					 {
						 recipientBankOrFspCode = cdtrAgt.has("clrSysMmbId") ? !cdtrAgt.get("clrSysMmbId").isJsonNull() ? cdtrAgt.get("clrSysMmbId").getAsString() : ""  : "";
					 }	
					 
					 if(util.isNullOrEmpty(recipientBankOrFspCode))
					 {
						 JsonObject instdAgt = js.has("instdAgt") ? js.get("instdAgt").getAsJsonObject() : new JsonObject();					 
						 recipientBankOrFspCode = instdAgt.has("BICFI") ? !instdAgt.get("BICFI").isJsonNull() ? instdAgt.get("BICFI").getAsString() : ""  : "";
					 }
					 
					 if(util.isNullOrEmpty(recipientBankOrFspCode))
					 { 
						 recipientBankOrFspCode = cdtr.has("orgId") ? !cdtr.get("orgId").isJsonNull() ? cdtr.get("orgId").getAsString() : ""  : "";
					 }
					 
					 JsonObject rmtInf = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();
					 JsonArray instrInf  = rmtInf.has("instrInf") ? rmtInf.get("instrInf").getAsJsonArray()  : new JsonArray(); 
					 if(instrInf.size()>0)
					 {
						 JsonObject jsonObj = instrInf.get(0).getAsJsonObject();
						 senderInstruction = jsonObj.has("desc") ? jsonObj.get("desc").getAsString()  : "NA"; 	  
						  
					 }	
					
				 }
				 
				 if(payment_type.equalsIgnoreCase("ACH"))
				 {
					 transferChannel  = "EFT";
					 serviceCategory = "Internet banking";
					 serviceSubCategory = "Payments";
				 }

				String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
					
				transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "7" : "7";	
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
					
				recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
				
				sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
					
				serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";
				
				sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
					
				serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "9" : "9";
					
				serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
					
				currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "32103";
				
				purposes = transferChannel.equals("3") ? "32103" : "";

				transactionPlace = "218";
					
				if(corp) // corporate client
				{
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
					 
					 senderIdentificationType = "12";
					 senderIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
				}
				else // Individual client
				{
					sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
							   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
							   "and rownum = ?";
						 
					Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
					
					if(Info.size() > 0)
					{
						 senderIdentificationType = Info.get(0).getCOLUMN1();
						 senderIdentificationNumber = Info.get(0).getCOLUMN2();
						
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
							
						 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
					}
					else
					{
						 senderIdentificationType = "1";
						 senderIdentificationNumber = "NA";
					}
				}
				
				 if(recipientBankOrFspCode.length() == 3)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

					 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
				 }
				 else if(recipientBankOrFspCode.length() == 6)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

					 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
				 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }
			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
	
			 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
				
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
						 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
						 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
						 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
						 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
			 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
							+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
							+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
							") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
						 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
						 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
						 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
						 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_sc_ACH" });	 
			    
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
				 
				 logger.error("Error processing OutgoingFundTransfer_SCPAY: "+e.getLocalizedMessage());  
			 }
			
			 return details;
		}

		public JsonObject OutgoingFundTransfer_SCPAY_BT(String INFO1, String INFO2, String INFO3) //RTS197 
		{ 
			 JsonObject details = new JsonObject();
			 
			 try
			 {
				 Common_Utils util = new Common_Utils();
				 
				 JsonObject data_ = util.StringToJsonObject(INFO3);
				
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 		
				 JsonObject header = data_.get("header").getAsJsonObject();
				 
				 JsonObject data = data_.get("data").getAsJsonObject();
				 		 
				 String reportingDate = util.getCurrentReportDate();
				 String transactionId = "";
				 String transactionDate = "";
				 String transferChannel = "Internal Fund Transfer (IFT)";
				 String subCategoryTransferChannel = ""; //non mandatory
				 String senderAccountNumber = "";
				 String senderIdentificationType = "";
				 String senderIdentificationNumber = "";
				 String recipientName = "";
				 String recipientMobileNumber = "NA";
				 String recipientCountry = "";
				 String recipientBankOrFspCode = ""; 
				 String recipientAccountOrWalletNumber = "";
				 String serviceChannel = "";
				 String serviceCategory = "";
				 String serviceSubCategory = "";		
				 String orgAmount = "";
				 String currency = "";
				 String usdAmount = "";
				 String tzsAmount = "";
				 String purposes = "";
				 String senderInstruction = "NA";
				 String transactionPlace = "";

				 String Identityno = ""; boolean corp = false;
				 
				 if(data.size() > 0)
				 {
					 JsonObject js = data;
					 JsonObject js_Header = header;
						
					 transactionId = js_Header.has("msgId") ? js_Header.get("msgId").getAsString() : "";
					
					 JsonObject sttlmDtTmInf = js.get("sttlmDtTmInf").getAsJsonObject();
					 transactionDate = sttlmDtTmInf.get("prcgDtTm").getAsString();
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
					 
					 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";
					 
					 if(util.isNullOrEmpty(senderAccountNumber))
					 {
						 senderAccountNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
					 }
					 				 
					 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject();
					 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "NA";

					 JsonObject pstlAdr = cdtr.has("pstlAdr") ? cdtr.get("pstlAdr").getAsJsonObject() : new JsonObject();
					 recipientCountry = pstlAdr.has("ctry") ? pstlAdr.get("ctry").getAsString() : "";

						
					 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
					 recipientAccountOrWalletNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";
					
					 if(util.isNullOrEmpty(recipientAccountOrWalletNumber))
					 {
						recipientAccountOrWalletNumber = cdtrAcct.has("IBAN") ? cdtrAcct.get("IBAN").getAsString() : "";
					 }
					 	 
			
					  JsonObject chanl = js.has("chanl") ? js.get("chanl").getAsJsonObject() : new JsonObject();
					  serviceChannel = chanl.has("instgChanl") ? chanl.get("instgChanl").getAsString() : "";

					 
					 if(serviceChannel.equalsIgnoreCase("IBK"))
					 {
						 serviceChannel = "Internet banking";
					 }
					 else if (serviceChannel.equalsIgnoreCase("RCP") || serviceChannel.equalsIgnoreCase("SIC") || serviceChannel.equalsIgnoreCase("SCC")) 
					 {
						serviceChannel = "Branch";
					 }
					 else 
					 {
						serviceChannel = "Online payment";
					 }
					 
					
					 JsonObject  amt = js.get("amt").getAsJsonObject();
					 currency = amt.has("intrbkSttlmAmtCcy") ? amt.get("intrbkSttlmAmtCcy").getAsString(): "";
					 
					 orgAmount = amt.has("intrBkSttlmAmt") ? amt.get("intrBkSttlmAmt").getAsString(): "";
					 
					 usdAmount = amt.has("cdtAmtUSDEqvt") ? amt.get("cdtAmtUSDEqvt").getAsString():"";
					 
					 if(util.isNullOrEmpty(usdAmount))
					 {
						 usdAmount = amt.has("dbtAmtUSDEqvt") ? amt.get("dbtAmtUSDEqvt").getAsString():"";
					 }
					 
					 tzsAmount =  amt.has("dbtAmtBaseCcyEqvt") ? amt.get("dbtAmtBaseCcyEqvt").getAsString():"";
					 
					 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
					 
					 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
					 
					
					 JsonObject cdtrAgt = js.has("cdtrAgt") ? js.get("cdtrAgt").getAsJsonObject() : new JsonObject();
					 recipientBankOrFspCode = cdtrAgt.has("BICFI") ? !cdtrAgt.get("BICFI").isJsonNull() ? cdtrAgt.get("BICFI").getAsString() : ""  : "";
					 
					 
					 if(util.isNullOrEmpty(recipientBankOrFspCode))
					 {
						 recipientBankOrFspCode = cdtrAgt.has("clrSysMmbId") ? !cdtrAgt.get("clrSysMmbId").isJsonNull() ? cdtrAgt.get("clrSysMmbId").getAsString() : ""  : "";
					 }	
					 
					 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
					 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
					 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
					 String casaSgmntIdr = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
					 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
					 
					 if(casaSgmntIdr.equals("C"))
					 { 
						 Identityno = cstmr.has("sciLeId") ? cstmr.get("sciLeId").getAsString() : "";
						 corp = true;
					 }
					 else
					 {
						 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
					 }
					
					 if(util.isNullOrEmpty(serviceSubCategory))
					 {
						 if(casaSgmntIdr.equalsIgnoreCase("C"))
						 {
							 serviceCategory = "Internal account transfer";
							 serviceSubCategory = "";
						 }
					 }
					
					 JsonObject rmtInf = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();					
					 if(util.isNullOrEmpty(senderInstruction)) 
					 {
						 senderInstruction = rmtInf.has("Ustrd") ? rmtInf.get("Ustrd").getAsString() : "NA";
					 }				 
					
				 }
				 
				
				String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
					
				transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "10" : "10";	
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
					
				recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
				
				sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
					
				serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";
				
				sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
					
				serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "11" : "11";
					
				serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "" : "";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
					
				currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "32103";
				
				purposes = transferChannel.equals("3") ? "32103" : "";
					
				transactionPlace = "218";
				
				if(corp) // corporate client
				{
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
					 
					 senderIdentificationType = "12";
					 senderIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
				}
				else // Individual client
				{
					sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
							   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
							   "and rownum = ?";
						 
					Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
					
					if(Info.size() > 0)
					{
						 senderIdentificationType = Info.get(0).getCOLUMN1();
						 senderIdentificationNumber = Info.get(0).getCOLUMN2();
						
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
							
						 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
					}
					else
					{
						 senderIdentificationType = "1";
						 senderIdentificationNumber = "NA";
					}
				}
				
				 if(recipientBankOrFspCode.length() == 3)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

					 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
				 }
				 else if(recipientBankOrFspCode.length() == 6)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

					 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
				 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }

			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
	
				 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
							
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
						 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
						 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
						 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
						 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
			 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
							+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
							+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
							") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
						 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
						 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
						 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
						 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_sc_BT" });	 
			    
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
				 
				 logger.error("Error processing OutgoingFundTransfer_SCPAY: "+e.getLocalizedMessage());  
			 }
			
			 return details;
		}
		
		//-------------------------------FAST------------------------------------------
		
		public JsonObject IncomingFundTransfer_SCPAY_FAST(String INFO1, String INFO2, String INFO3) //RTS195
		{ 
			 JsonObject details = new JsonObject();
			 
			 try
			 {
				 Common_Utils util = new Common_Utils();
				 
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 JsonObject data_ = util.StringToJsonObject(INFO3);
				
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 		
				 JsonObject header = data_.get("header").getAsJsonObject();
				 
				 //String payment_type = header.get("pmtTp").getAsString();
				 
				 JsonObject data =  data_.get("data").getAsJsonObject();
				 
				 String reportingDate = util.getCurrentReportDate();
				 String transactionId = "";
				 String transactionDate = "";
				 String transferChannel =  "";
				 String subCategoryTransferChannel = ""; //non mandatory
				 String recipientName = "";
				 String senderAccountNumber = "";
				 String recipientIdentificationType = ""; //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
				 String recipientIdentificationNumber = "";   //If value is C - then need to inform vendor to look up from SCI. If value is not equal to C, then need to inform Vendor to look up from EBBS
				 String senderCountry = "";
				 String serviceCategory = "";
				 String serviceSubCategory = "";  
				 String senderName = "";
				 String senderBankOrFspCode = "";  
				 String senderAccountOrWalletNumber = ""; 
				 String currency = "";
				 String orgAmount = "";
				 String usdAmount = "";
				 String tzsAmount = "";
				 String purposes = "";  
				 String senderInstruction = "NA";

				 
				 transactionDate = header.has("txRcvdDtTm") ? header.get("txRcvdDtTm").getAsString() : "";
				 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
				 		
				 transferChannel = "TIPS";
				 
				 senderCountry = "TZ";
				 
				 serviceCategory = "Internet banking";
				 
				 serviceSubCategory = "Payments";
				 
				 String Identityno = ""; boolean corp = false;
				 
				 if(data.size() > 0)
				 {				 
					 JsonObject js = data;
					 
					 JsonObject pmtId = js.has("pmtId") ? js.get("pmtId").getAsJsonObject() : new JsonObject();
					 
					 transactionId  = pmtId.has("instrId") ? pmtId.get("instrId").getAsString().replaceAll("[^a-zA-Z0-9]", "") : "";
					 				
					// transactionId = transactionId.replaceAll("[^a-zA-Z0-9]", "");
					 	 
					 transactionDate = convertHktToTanzaniaDate(transactionDate);
									
					 JsonObject cdtr = js.has("cdtr") ? js.get("cdtr").getAsJsonObject() : new JsonObject(); 
					 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : "NA";
					 
					 JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";

					 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
					 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
					 JsonObject CRDT = enqryInf.has("CRDT") ? enqryInf.get("CRDT").getAsJsonObject() : new JsonObject();
					 String casaSgmntIdr = CRDT.has("casaSgmntIdr") ? CRDT.get("casaSgmntIdr").getAsString() : "";
					 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
					 JsonObject cdtAcctSNASInf = cstmr.has("cdtAcctSNASInf") ? cstmr.get("cdtAcctSNASInf").getAsJsonObject() : new JsonObject();	

					 if(casaSgmntIdr.equals("C"))
					 { 
						 Identityno = cdtAcctSNASInf.has("sciLeId") ? cdtAcctSNASInf.get("sciLeId").getAsString() : "";
						 corp = true;
					 }
					 else
					 {
						 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
					 }
					 
					 JsonObject dbtr = js.has("dbtr") ? js.get("dbtr").getAsJsonObject() : new JsonObject();
					 senderName = dbtr.has("nm") ? dbtr.get("nm").getAsString() : "";

					
					 JsonObject dbtrAgt = js.has("dbtrAgt") ? js.get("dbtrAgt").getAsJsonObject() : new JsonObject();									
					 senderBankOrFspCode = dbtrAgt.has("BICFI") ? !dbtrAgt.get("BICFI").isJsonNull() ? dbtrAgt.get("BICFI").getAsString() : ""  : "";

					 if(util.isNullOrEmpty(senderBankOrFspCode))
					 {
						 senderBankOrFspCode = dbtrAgt.has("clrSysMmbId") ? !dbtrAgt.get("clrSysMmbId").isJsonNull() ? dbtrAgt.get("clrSysMmbId").getAsString() : ""  : "";
					 }
					 
					 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.get("dbtrAcct").getAsJsonObject() : new JsonObject();
					 senderAccountOrWalletNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "";
 
					 if(util.isNullOrEmpty(senderAccountOrWalletNumber))
					 {
						 senderAccountOrWalletNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
					 }
					 
					 JsonObject  amt = js.get("amt").getAsJsonObject();
					 currency = amt.get("intrBkSttlmAmtCcy").getAsString();
					 
					 orgAmount = amt.get("intrBkSttlmAmt").getAsString();
					 
					 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();
					 purposes = purp.has("sndrPurpCd") ? purp.get("sndrPurpCd").getAsString() : "";
					 
					 JsonObject rmtInf = js.has("rmtInf") ? js.get("rmtInf").getAsJsonObject() : new JsonObject();
					 JsonArray ustrd = rmtInf.has("ustrd")? rmtInf.get("ustrd").getAsJsonArray() : new JsonArray();
					 
					 senderInstruction = ustrd.size() > 0 ? ustrd.get(0).getAsString() : "NA";

				 }
				 	 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
				 
				 usdAmount = rates.get("usd").getAsString();
				 tzsAmount = rates.get("tzs").getAsString();
				 
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());			 
				 
				 transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";
				 	
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
					
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";				
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", senderCountry }, new Lookup001_mapper());
					
				 senderCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			 
				 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
			 
				 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
					
				 serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "9" : "9";
					
				 serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
							
				sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "22103";
				
				purposes = transferChannel.equals("3") ? "22103" : "";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";   //INCOME FAST CASH
				 
				Info = Jdbctemplate.query(sql, new Object[] { "FSP", senderBankOrFspCode }, new Lookup001_mapper());
			 
				senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN3() : senderBankOrFspCode;

				if(corp) // corporate client
				{
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
					 
					 recipientIdentificationType = "12";
					 recipientIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
				}
				else // Individual client
				{
					sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
							   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
							   "and rownum = ?";
						 
					Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
					
					if(Info.size() > 0)
					{
						 recipientIdentificationType = Info.get(0).getCOLUMN1();
						 recipientIdentificationNumber = Info.get(0).getCOLUMN2();
						
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", recipientIdentificationType }, new Lookup001_mapper());
							
						 recipientIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
					}
					else
					{
						recipientIdentificationType = "1";
						recipientIdentificationNumber = "NA";
					}
				}
				
				 if(senderBankOrFspCode.length() == 3)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
				 else if(senderBankOrFspCode.length() == 6)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
				 }
			 else if(senderBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode.substring(0, 8);
				 
				 if(senderBankOrFspCode.length() == 8)
				 {
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
					 
					 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
				 }
			 }
			 else if(senderBankOrFspCode.length() == 8)
			 {
				 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)"; 
				 
				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", senderBankOrFspCode, senderBankOrFspCode + "XXX"}, new Lookup001_mapper());
				 
				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : senderBankOrFspCode;
			 }
			 else 
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", senderBankOrFspCode }, new Lookup001_mapper());

				 senderBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : senderBankOrFspCode;
			 }


				
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "incomingBankFundsTransfer", count, reportingDate, transactionId,
							 transactionDate, transferChannel, subCategoryTransferChannel, recipientName, senderAccountNumber,
							 recipientIdentificationType, recipientIdentificationNumber, senderCountry, senderName, senderBankOrFspCode,
							 senderAccountOrWalletNumber, serviceCategory, serviceSubCategory, currency, orgAmount, usdAmount,
							 tzsAmount, purposes, senderInstruction});
	 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "incomingBankFundsTransfer"});
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
					 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "incomingBankFundsTransfer", "serial", "reportingDate", 
						 "transactionId", "transactionDate", "transferChannel", "subCategoryTransferChannel", "recipientName", "senderAccountNumber", 
						 "recipientIdentificationType", "recipientIdentificationNumber", "senderCountry", "senderName", "senderBankOrFspCode",
						 "senderAccountOrWalletNumber", "serviceCategory", "serviceSubCategory", "currency", "orgAmount", "usdAmount", "tzsAmount",
						 "purposes", "senderInstruction"});
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "incomingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_SC_Fast" });	 
			     
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
				 
				 logger.error("Error processing IncomingFundTransfer_SCPAY", e.getMessage()); 
			 }
			
			 return details;
		}
		
		public JsonObject OutgoingFundTransfer_SCPAY_FAST(String INFO1, String INFO2, String INFO3) //RTS197
		{ 
			 JsonObject details = new JsonObject();
			 
			 try
			 {
				 Common_Utils util = new Common_Utils();
				 
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 JsonObject data_ = util.StringToJsonObject(INFO3);
				
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 		
				 JsonObject header = data_.get("header").getAsJsonObject();
				 
				// String payment_type = header.get("pmtTp").getAsString();
				 
			
				 JsonObject data = data_.get("data").getAsJsonObject();
				 		 
				 String reportingDate = util.getCurrentReportDate();
				 String transactionId = "";
				 String transactionDate = "";
				 String transferChannel = "";
				 String subCategoryTransferChannel = ""; //non mandatory
				 String senderAccountNumber = "";
				 String senderIdentificationType = "";
				 String senderIdentificationNumber = "";
				 String recipientName = "";
				 String recipientMobileNumber = "";
				 String recipientCountry = "";
				 String recipientBankOrFspCode = ""; 
				 String recipientAccountOrWalletNumber = "";
				 String serviceChannel = "";
				 String serviceCategory = "";
				 String serviceSubCategory = "";		
				 String orgAmount = "";
				 String currency = "";
				 String usdAmount = "";
				 String tzsAmount = "";
				 String purposes = "";
				 String senderInstruction = "";
				 String transactionPlace = "";
				 
				 transferChannel = "TIPS";
				 
				 recipientMobileNumber = "NA";
				 
				 recipientCountry = "TZ";
				 
				 serviceChannel = "Internet banking";
				 
				 serviceSubCategory = "Payments";
				 
				 senderInstruction = "NA";
				 
				 transactionPlace = "TZ";

				 String Identityno = ""; boolean corp = false;
				 
				 if(data.size() > 0)
				 {
					 JsonObject js = data;
					
					 JsonObject pmtId = js.has("pmtId")? js.get("pmtId").getAsJsonObject() : new JsonObject();				
					 transactionId  = pmtId.has("instrId") ? pmtId.get("instrId").getAsString().replaceAll("[^a-zA-Z0-9]", "") : "";
					 					 
					 transactionDate = header.has("txRcvdDtTm")? header.get("txRcvdDtTm").getAsString() : "";
		//			 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
					if (!transactionDate.contains(".")) 
					{
					    transactionDate += ".000";
					}
					
					transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");


					transactionDate = convertHktToTanzaniaDate(transactionDate);
					
					
					 JsonObject cbs = js.has("cbs") ? js.get("cbs").getAsJsonObject() : new JsonObject();	
					 JsonObject enqryInf = cbs.has("enqryInf") ? cbs.get("enqryInf").getAsJsonObject() : new JsonObject();	
					 JsonObject DBIT = enqryInf.has("DBIT") ? enqryInf.get("DBIT").getAsJsonObject() : new JsonObject();
					 String casaSgmntIdr = DBIT.has("casaSgmntIdr") ? DBIT.get("casaSgmntIdr").getAsString() : "";
					 JsonObject cstmr = js.has("cstmr") ? js.get("cstmr").getAsJsonObject() : new JsonObject();	
					 
					 if(casaSgmntIdr.equals("C"))
					 { 
						 Identityno = cstmr.has("sciLeId") ? cstmr.get("sciLeId").getAsString() : "";
						 corp = true;
					 }
					 else
					 {
						 Identityno = cstmr.has("mstrNb") ? cstmr.get("mstrNb").getAsString() : "";
					 }
					 
					 JsonObject cdtr = js.has("dbtr") ? js.get("dbtr").getAsJsonObject() : new JsonObject(); 
					 recipientName = cdtr.has("nm") ? cdtr.get("nm").getAsString() : ""; 

					 JsonObject dbtrAcct = js.has("dbtrAcct") ? js.getAsJsonObject("dbtrAcct") : new JsonObject();
					 senderAccountNumber = dbtrAcct.has("id") ? dbtrAcct.get("id").getAsString() : "NA";

					 if(util.isNullOrEmpty(senderAccountNumber))
					 {
						 senderAccountNumber = dbtrAcct.has("IBAN") ? dbtrAcct.get("IBAN").getAsString() : "";
					 }
					
					 JsonObject cdtrAgt = js.has("cdtrAgt") ? js.get("cdtrAgt").getAsJsonObject() : new JsonObject();
					 recipientBankOrFspCode = cdtrAgt.has("BICFI") ? !cdtrAgt.get("BICFI").isJsonNull() ? cdtrAgt.get("BICFI").getAsString() : ""  : "";

					 if(util.isNullOrEmpty(recipientBankOrFspCode))
					 {
						 recipientBankOrFspCode = cdtrAgt.has("clrSysMmbId") ? !cdtrAgt.get("clrSysMmbId").isJsonNull() ? cdtrAgt.get("clrSysMmbId").getAsString() : ""  : "";
					 }	
					 
					JsonObject cdtrAcct = js.has("cdtrAcct") ? js.get("cdtrAcct").getAsJsonObject() : new JsonObject();
					recipientAccountOrWalletNumber = cdtrAcct.has("id") ? cdtrAcct.get("id").getAsString() : "";
					
					if(util.isNullOrEmpty(recipientAccountOrWalletNumber))
					{
						recipientAccountOrWalletNumber = cdtrAcct.has("IBAN") ? cdtrAcct.get("IBAN").getAsString() : "";
					}
					
					if(util.isNullOrEmpty(recipientAccountOrWalletNumber))
					{
						recipientAccountOrWalletNumber = cdtrAcct.has("prxyId") ? cdtrAcct.get("prxyId").getAsString() : "";
					}
 
					 JsonObject  amt = js.has("amt")? js.get("amt").getAsJsonObject():new JsonObject();
					 currency = amt.has("intrBkSttlmAmtCcy")? amt.get("intrBkSttlmAmtCcy").getAsString():"";
					 
					 orgAmount = amt.has("intrBkSttlmAmt")? amt.get("intrBkSttlmAmt").getAsString() : "";
					 
					 JsonObject purp  = js.has("purp") ? js.get("purp").getAsJsonObject() : new JsonObject();				 
					 purposes = purp.has("sndrPurpCd") ? !purp.get("sndrPurpCd").isJsonNull() ? purp.get("sndrPurpCd").getAsString() : "32103"  : "32103";

					 
					 JsonObject rmtInf  = js.has("rmtInf")? js.get("rmtInf").getAsJsonObject() : new JsonObject();
					 senderInstruction = rmtInf.has("ustrd") ? !rmtInf.get("ustrd").isJsonNull() ? rmtInf.get("ustrd").getAsString() : "NA"  : "NA";
					 
				 }
			 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
				 
				 usdAmount = rates.get("usd").getAsString();
				 tzsAmount = rates.get("tzs").getAsString();
				 				 
				String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH001", transferChannel }, new Lookup001_mapper());
					
				transferChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";	
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", recipientCountry }, new Lookup001_mapper());
					
				recipientCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
				
				sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH003", serviceChannel }, new Lookup001_mapper());
					
				serviceChannel = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "6" : "6";
				
				sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?) and lower(COLUMN4) = lower(?)";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CASH004", serviceCategory, serviceSubCategory }, new Lookup001_mapper());
					
				serviceCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "9" : "9";
					
				serviceSubCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
					
				currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN7=?";  

				Info = Jdbctemplate.query(sql, new Object[] { "CASH007", purposes }, new Lookup001_mapper());

				purposes = Info.size() > 0 ? Info.get(0).getCOLUMN2() : "32103";
				
				purposes = transferChannel.equals("3") ? "32103" : "";
				
				sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";   // OUTGOING FAST CASH
				 
				Info = Jdbctemplate.query(sql, new Object[] { "FSP", recipientBankOrFspCode }, new Lookup001_mapper());
			 
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN3() : recipientBankOrFspCode;
				
				transactionPlace = "218";
						
				if(corp) // corporate client
				{
					 sql = "select LMP_INC_NUM_TEXT from p44_le_main_profile_tb where LMP_SYS_GEN_LE_ID = ?";
					 
					 List<String> Info2 = Jdbctemplate.queryForList(sql, new Object[] { Identityno }, String.class);
					 
					 senderIdentificationType = "12";
					 senderIdentificationNumber = Info2.size() > 0 ? Info2.get(0) : "NA";
				}
				else // Individual client
				{
					sql = "select w.UNIQUEID2 identification_type, w.UNIQUEID1 identification_value, w.RELATIONSHIPTYPE from D10_EBBS_TB w "+
							   "where w.RELATIONSHIPNO in (select e.RELATIONSHIPNO from D14_EBBS_TB e where e.PRIMARYFLAG = ? and e.MASTERNO = ? and rownum=?) "+
							   "and rownum = ?";
						 
					Info = Jdbctemplate.query(sql, new Object[] { "Y", Identityno, "1", "1"  }, new Lookup002_mapper());
					
					if(Info.size() > 0)
					{
						 senderIdentificationType = Info.get(0).getCOLUMN1();
						 senderIdentificationNumber = Info.get(0).getCOLUMN2();
						
						 sql = "select * from lookup001 where COLUMN12=? and COLUMN4=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { "CASH006", senderIdentificationType }, new Lookup001_mapper());
							
						 senderIdentificationType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "1";
					}
					else
					{
						 senderIdentificationType = "1";
						 senderIdentificationNumber = "NA";
					}
				}			
				
				if(recipientBankOrFspCode.length() == 3)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

					 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
				 }
				 else if(recipientBankOrFspCode.length() == 6)
				 {
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN3=?";  

					 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode.substring(0, 3) }, new Lookup001_mapper());

					 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;
				 }
			else if(recipientBankOrFspCode.length() == 11)
			 {
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode.substring(0, 8);

				 if(recipientBankOrFspCode.length() == 8)
				 {
					sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
					Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
					recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
				 }
			 }

			else if(recipientBankOrFspCode.length() == 8)
			 {
				sql = "select * from lookup001 where COLUMN12=? and COLUMN1 in (?,?)";  
				Info = Jdbctemplate.query(sql, new Object[] { "BOT_BIC", recipientBankOrFspCode, recipientBankOrFspCode + "XXX" }, new Lookup001_mapper());
				recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN1() : recipientBankOrFspCode;
			 }
			 else
			 {	 
				 sql = "select * from lookup001 where COLUMN12=? and COLUMN1=?";  

				 Info = Jdbctemplate.query(sql, new Object[] { "CASH008", recipientBankOrFspCode }, new Lookup001_mapper());

				 recipientBankOrFspCode = Info.size() > 0 ? Info.get(0).getCOLUMN2() : recipientBankOrFspCode;		 
			 }
	
			 
			 	senderIdentificationNumber = !util.isNullOrEmpty(senderIdentificationNumber)? senderIdentificationNumber : "NA";			 
				
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
						+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
						+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
						") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outgoingBankFundsTransfer", count,reportingDate,
						 transactionId, transactionDate, transferChannel, subCategoryTransferChannel, senderAccountNumber, 
						 senderIdentificationType, senderIdentificationNumber, recipientName, recipientMobileNumber, recipientCountry,
						 recipientBankOrFspCode, recipientAccountOrWalletNumber, serviceChannel, serviceCategory, serviceSubCategory, 
						 currency, orgAmount, usdAmount, tzsAmount, purposes, senderInstruction, transactionPlace});
			 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outgoingBankFundsTransfer"});
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
							+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
							+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26,COLUMN27\r\n" + 
							") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outgoingBankFundsTransfer", "serial","reportingDate", "transactionId", "transactionDate", "transferChannel", 
						 "subCategoryTransferChannel", "senderAccountNumber", "senderIdentificationType", "senderIdentificationNumber",
						 "recipientName", "recipientMobileNumber", "recipientCountry", "recipientBankOrFspCode", 
						 "recipientAccountOrWalletNumber", "serviceChannel", "serviceCategory", "serviceSubCategory",
						 "currency", "orgAmount", "usdAmount", "tzsAmount", "purposes", "senderInstruction", "transactionPlace" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "outgoingBankFundsTransfer", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CASH_SC_rtgs" });	 
			    
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
				 
				 logger.error("Error processing OutgoingFundTransfer_SCPAY: "+e.getLocalizedMessage());  
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
	
	private class Lookup002_mapper implements RowMapper<Lookup001>  
    {
    	Common_Utils util = new Common_Utils();
    	
		public Lookup001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Lookup001 Info = new Lookup001(); 
			
			Info.setCOLUMN1(util.ReplaceNull(rs.getString("identification_type")));
			Info.setCOLUMN2(util.ReplaceNull(rs.getString("identification_value")));
			Info.setCOLUMN3(util.ReplaceNull(rs.getString("RELATIONSHIPTYPE")));
			
			return Info;
		}
    }
}
