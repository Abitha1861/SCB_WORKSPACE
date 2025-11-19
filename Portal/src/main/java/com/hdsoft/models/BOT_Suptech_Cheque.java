package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.CHEQUE002;
import com.hdsoft.Repositories.Cheque001;
import com.hdsoft.Repositories.Lookup001;
import com.hdsoft.Repositories.Prop001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.zaxxer.hikari.HikariDataSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@Component
public class BOT_Suptech_Cheque implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptech_Cheque(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptech_Cheque() { } 
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptech_Cheque.class);
	
	@Scheduled(cron = "0 0/30 * * * *")   //Every 30 mins
    public void Cheque_Thread()  
    {
		 try 
		 {
			 String sql = "select count(*) from prop001 where chcode = ? and mtypeparam = ? and userid = ?";  // checking the active environment
			 
			 int count = Jdbctemplate.queryForObject(sql, new Object[] { "ENV", new Sysconfig().getHostAddress(), "1" }, Integer.class);
			 
			 if(count == 1)
			 {
				 sql = "SELECT COUNT(*) FROM RTS004 WHERE APICODE = ? AND STATUS = ?";

				 count = Jdbctemplate.queryForObject(sql, new Object[] { "RTS031" , "1" }, Integer.class);

				 if(count != 0)
				 {
					 sql = "SELECT * FROM prop001 WHERE CHCODE=?";

					 List<Prop001> prop = Jdbctemplate.query(sql, new Object[] { "APARTA" }, new Prop001RowMapper());

					 if(prop.size() > 0)
					 {
						 String filepath = prop.get(0).getmTypeParam();

						 readFolderAsString(filepath);
					 }
				 } 
			 } 
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in Cheque_Thread :::: "+e.getLocalizedMessage());
		 }
    }
	
	@Scheduled(cron = "0 0/5 * * * *")   //Every 5 mins
    public void Cheque_push_Thread()  
    {
		 try 
		 {
			 String sql = "select count(*) from prop001 where chcode = ? and mtypeparam = ? and userid = ?";  // checking the active environment
			 
			 int count = Jdbctemplate.queryForObject(sql, new Object[] { "ENV", new Sysconfig().getHostAddress(), "1" }, Integer.class);
			 
			 if(count == 1)
			 {
				 sql = "SELECT COUNT(*) FROM RTS004 WHERE APICODE = ? AND STATUS = ?";

				 count = Jdbctemplate.queryForObject(sql, new Object[] { "RTS031" , "1" }, Integer.class);

				 if(count != 0)
				 {
					 Push_Flag();
				 }
			 } 
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in Cheque_push_Thread :::: "+e.getLocalizedMessage());
		 }
    }
	
	 @RequestMapping(value = {"/Datavision/cheque_fileRead"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Test_Service123(@RequestHeader("filepath") String filepath,  HttpServletRequest request,  HttpServletResponse response,  HttpSession session) throws IOException 
	 {		
		readFolderAsString(filepath);

		return "Processing completed for: " + filepath;       
     }

	@RequestMapping(value = {"/Datavision/RTA_filtering"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String Test_Service1234(@RequestBody String message,  HttpServletRequest request,  HttpServletResponse response,  HttpSession session) throws IOException 
	{
		 RTA_filtering("RTS031", "", message, "");
		 
		 return "SUCCESS" ;
	}
	
	 @RequestMapping(value = {"/Datavision/Push_Flag"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Push_Flag(HttpServletRequest request,  HttpServletResponse response,  HttpSession session) throws IOException 
	 {		
		
		Push_Flag();
		 
		return "DONE";       
     }
	 
	public JsonObject RTA_filtering(String SERVICECODE , String Channel_code , String message , String ACK) //DONE
	{
		JsonObject details = new JsonObject();
		
		try 
		{
			Common_Utils util = new Common_Utils();
			 
			JsonObject data = util.XMLToJsonObject(message);
								
			JsonObject notifyCorporateFinancialTransactionRequest = data.has("notifyCorporateFinancialTransactionRequest") ? data.get("notifyCorporateFinancialTransactionRequest").getAsJsonObject() : new JsonObject();

			JsonObject notifyCorporateFinancialTransactionReqPayload = notifyCorporateFinancialTransactionRequest.has("notifyCorporateFinancialTransactionReqPayload") ? notifyCorporateFinancialTransactionRequest.get("notifyCorporateFinancialTransactionReqPayload").getAsJsonObject() : new JsonObject();

			JsonObject notifyCorporateFinancialTransactionReq = notifyCorporateFinancialTransactionReqPayload.has("notifyCorporateFinancialTransactionReq") ? notifyCorporateFinancialTransactionReqPayload.get("notifyCorporateFinancialTransactionReq").getAsJsonObject() : new JsonObject();

			JsonObject TransactionDetails = notifyCorporateFinancialTransactionReq.has("TransactionDetails") ? notifyCorporateFinancialTransactionReq.get("TransactionDetails").getAsJsonObject() : new JsonObject();

			JsonObject TransactionEntry = TransactionDetails.has("TransactionEntry") ? TransactionDetails.get("TransactionEntry").getAsJsonObject() : new JsonObject();
			
			JsonObject Narrations = TransactionEntry.has("Narrations") ? TransactionEntry.get("Narrations").getAsJsonObject() : new JsonObject();

			String ChannelID = TransactionEntry.has("ChannelID") ? TransactionEntry.get("ChannelID").getAsString() : "";
			
			String CreditDebitFlag = TransactionEntry.has("CreditDebitFlag") ? TransactionEntry.get("CreditDebitFlag").getAsString() : "";
			
			String TrnTypecode = TransactionEntry.has("TrnTypecode") ? TransactionEntry.get("TrnTypecode").getAsString() : "";
			 
			String Narration1 = Narrations.has("Narration1") ? Narrations.get("Narration1").getAsString() : "";

			String chequeno = "";
			String cheque_type = "";
			
			 if ((ChannelID.equalsIgnoreCase("0006") && CreditDebitFlag.equalsIgnoreCase("D") && TrnTypecode.equalsIgnoreCase("OCL")) || 
					    (ChannelID.equalsIgnoreCase("0006") && CreditDebitFlag.equalsIgnoreCase("C") && TrnTypecode.equalsIgnoreCase("CTS")) || 
					    (ChannelID.equalsIgnoreCase("0002") && Narration1.contains("IW RTN")) || 
					    (ChannelID.equalsIgnoreCase("0002") && Narration1.contains("OW RTN"))) 
			{
				 if(ChannelID.equalsIgnoreCase("0006") && CreditDebitFlag.equalsIgnoreCase("D") && TrnTypecode.equalsIgnoreCase("OCL"))
				 {					 
					 chequeno = TransactionEntry.has("ChequeNo") ? TransactionEntry.get("ChequeNo").getAsString() : "";
					 chequeno = chequeno.length() >= 6 ? chequeno.substring(chequeno.length() - 6) : chequeno;
					 cheque_type = "IC";
				 }
				 if(ChannelID.equalsIgnoreCase("0006") && CreditDebitFlag.equalsIgnoreCase("C") && TrnTypecode.equalsIgnoreCase("CTS"))
				 {
					 chequeno = Narrations.has("Narration1") ? Narrations.get("Narration1").getAsString() : "";	
					 cheque_type = "OC";
				 }
				 if((ChannelID.equalsIgnoreCase("0002") && Narration1.contains("IW RTN")) || 
					    (ChannelID.equalsIgnoreCase("0002") && Narration1.contains("OW RTN")))
				 {
					 chequeno = Narrations.has("Narration1") ? Narrations.get("Narration1").getAsString().replaceAll(".*(\\d{6})$", "$1") : "";
					 if (Narration1.contains("IW RTN")) 
					 {
						 cheque_type = "IR";
					 }
					 if (Narration1.contains("OW RTN")) 
					 {
						 cheque_type = "OR";
					 }
				 }
 
				 if(!util.isNullOrEmpty(chequeno))
				 {
					 
					 String sql = "SELECT * FROM CHEQUE001 WHERE chequeno = ?";  
					 
					 List<Cheque001> Info = Jdbctemplate.query(sql, new Object[] { chequeno }, new Cheque001Mapper());
	
					 String rta_date=  util.getCurrentDate("dd-MMM-yyyy"); 
					 
					 byte[] rta_alert = message.getBytes(StandardCharsets.UTF_8); 

					 if(Info.size() == 0)
					 {		
						 String Sql = "select suborgcode from sysconf001";
							
						 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
						
						 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
	
						 sql = "INSERT INTO CHEQUE001(SUBORGCODE, chequeno, chequedate, aparta_date, rta_date, rta_alert, aparta_file, cheque_type) VALUES(?,?,?,?,?,?,?,?)";

						Jdbctemplate.update(sql, new Object[] {SUBORGCODE , chequeno , null , null , rta_date , rta_alert , null , cheque_type});

						sql = "INSERT INTO CHEQUE002(SUBORGCODE, chequeno, rta_flag, ispushed) values(?,?,?,?)";

						Jdbctemplate.update(sql, new Object[] {SUBORGCODE, chequeno, 1, 0});

					 }
					 
					 else 
					 {	

						sql = "UPDATE CHEQUE001 SET rta_date = ?, rta_alert = ?,cheque_type = ?  WHERE chequeno = ?";

						Jdbctemplate.update(sql, new Object[] { rta_date, rta_alert,cheque_type, chequeno});	
						
						sql = "UPDATE CHEQUE002 SET rta_flag = ? where chequeno = ?";

						Jdbctemplate.update(sql, new Object[] {1, chequeno});

					 }				 
				 }
			}
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "RTA_FILTER WORKS successfully");
		} 
		catch (Exception e) 
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 			 
			 logger.error("Error processing RTA_FILTER", e.getMessage()); 
		}
		return details;
	}

    public JsonObject Aparta_filter(String message) 
    {
        JsonObject details = new JsonObject();
        
        try 
        {
        	
		Common_Utils util = new Common_Utils();

		String[] lines = message.split("\n");
		
		String header = lines[0];
		
		for (int i = 1; i < lines.length; i++) 
		{
            String line = lines[i];
		
            String chequeno = line.substring(64, 70);
            
            if(!util.isNullOrEmpty(chequeno))
            	
            {
            	
   			 String sql = "SELECT * FROM CHEQUE001 WHERE chequeno = ?";  

   			 List<Cheque001> Info = Jdbctemplate.query(sql, new Object[] { chequeno }, new Cheque001Mapper());
   			 
   			 String chequedate= util.getCurrentDate("dd-MMM-yyyy"); 
   			 
   			String combinedMessage = header + "\n" + line; 

   			Clob aparta_file = createClobFromString(combinedMessage);

   			 
   			 if(Info.size() == 0)
   			 {		
   	   			 String Sql = "select suborgcode from sysconf001";
 				
   	   			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
   	   			
   	   			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  

   			 	sql = "INSERT INTO CHEQUE001(SUBORGCODE, chequeno, chequedate , aparta_date ,rta_date , rta_alert , aparta_file , cheque_type) values(?,?,?,?,?,?,?,?)";
   							
   				Jdbctemplate.update(sql, new Object[] {SUBORGCODE , chequeno , chequedate , chequedate ,"" , "" , aparta_file , ""});
   				
   				sql = "INSERT INTO CHEQUE002(SUBORGCODE, chequeno, aparta_flag, rta_flag, ispushed) values(?,?,?,?,?)";

   				Jdbctemplate.update(sql, new Object[] {SUBORGCODE, chequeno, 1, "", 0});

   			 }
   			 
   			 else 
   			 {	

   				sql = "UPDATE CHEQUE001 SET chequedate = ?, aparta_date = ? , aparta_file = ?  WHERE chequeno = ?";

   				Jdbctemplate.update(sql, new Object[] { chequedate, chequedate,aparta_file, chequeno});		
   				
				sql = "UPDATE CHEQUE002 SET aparta_flag = ? where chequeno = ?";

				Jdbctemplate.update(sql, new Object[] {1, chequeno});

   			 }				 
   		 }

            
		}
		  	
            details.addProperty("result", "success");
            details.addProperty("stscode", "HP00");
            details.addProperty("message", "Batch created successfully");

        } 
        catch (Exception e) 
        {
            details.addProperty("result", "failed");
            details.addProperty("stscode", "HP06");
            details.addProperty("message", e.getLocalizedMessage());
            logger.error("Error processing Aparta_filter", e);
        }

        return details;
    }

    public JsonObject Push_Flag() 
    {

        JsonObject details = new JsonObject();
        
        try 
        {        	        	
			 String sql = "select * from CHEQUE002 where aparta_flag = ? and rta_flag = ? and ispushed = ?";
			
			 List<CHEQUE002> Info = Jdbctemplate.query(sql, new Object[] { 1 , 1 , 0 }, new Cheque002Mapper());
			 
			for (int i = 0; i < Info.size(); i++) 
			{
				 String chequeno = Info.get(i).getChequeno();
				 		
				 sql = "select * from CHEQUE001 where chequeno = ?";
					
				 List<Cheque001> Info1 = Jdbctemplate.query(sql, new Object[] { chequeno }, new Cheque001Mapper());

				 String cheque_type  = Info1.size() > 0 ? Info1.get(0).getCheque_type() : null;
				 
				 if (cheque_type != null && cheque_type.equalsIgnoreCase("IC")) 
				 {
					 Clob aparta_msg  = Info1.get(0).getAparta_file();
					 
					 String aparta_msg_as_string = aparta_msg.getSubString(1, (int) aparta_msg.length());

					 Blob ebbs_msg = Info1.get(0).getRta_alert();

					 String ebbs_msg_as_string = new String(ebbs_msg.getBytes(1, (int) ebbs_msg.length()), StandardCharsets.UTF_8);

					 InwardCheckClearing("RTS031", aparta_msg_as_string, ebbs_msg_as_string);
				 }
				 if (cheque_type != null && cheque_type.equalsIgnoreCase("OC")) 
				 {

					 Clob aparta_msg  = Info1.get(0).getAparta_file();
					 
					 String aparta_msg_as_string = aparta_msg.getSubString(1, (int) aparta_msg.length());

					 Blob ebbs_msg = Info1.get(0).getRta_alert();

					 String ebbs_msg_as_string = new String(ebbs_msg.getBytes(1, (int) ebbs_msg.length()), StandardCharsets.UTF_8);

					 OutWardCheckClearing("RTS031", aparta_msg_as_string, ebbs_msg_as_string);
	
				}
				 
				 if (cheque_type != null && cheque_type.equalsIgnoreCase("IR")) 
				 {

					 Clob aparta_msg  = Info1.get(0).getAparta_file();
					 
					 String aparta_msg_as_string = aparta_msg.getSubString(1, (int) aparta_msg.length());

					 Blob ebbs_msg = Info1.get(0).getRta_alert();

					 String ebbs_msg_as_string = new String(ebbs_msg.getBytes(1, (int) ebbs_msg.length()), StandardCharsets.UTF_8);
				 
					 InwardReturn("RTS031", aparta_msg_as_string, ebbs_msg_as_string);
				}

				 if (cheque_type != null && cheque_type.equalsIgnoreCase("OR")) 
				 {

					 Clob aparta_msg  = Info1.get(0).getAparta_file();
					 
					 String aparta_msg_as_string = aparta_msg.getSubString(1, (int) aparta_msg.length());

					 Blob ebbs_msg = Info1.get(0).getRta_alert();

					 String ebbs_msg_as_string = new String(ebbs_msg.getBytes(1, (int) ebbs_msg.length()), StandardCharsets.UTF_8);
					 
					 OutWardReturn("RTS031", aparta_msg_as_string, ebbs_msg_as_string);
				}
			 }
			 
	  	
            details.addProperty("result", "success");
            details.addProperty("stscode", "HP00");
            details.addProperty("message", "Batch created successfully");
            
        } 
        
        catch (Exception e) 
        {
            details.addProperty("result", "failed");
            details.addProperty("stscode", "HP06");
            details.addProperty("message", e.getLocalizedMessage());
            logger.error("Error processing Push_Flag", e);
        }

        return details;

	}
    
	public JsonObject InwardCheckClearing(String INFO1, String INFO2, String INFO3) //RTS031 - completed
	{ 
		 JsonObject details = new JsonObject();
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			 
			 System.out.println(data);
			 	
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 			 
			 String reportingDate = util.getCurrentReportDate();
			 String chequeNumber = "";
			 String issuerName = "";
			 String issuerBankerCode =  "";
			 String payeeName = "";
			 String payeeAccountNumber = "";
			 String chequeDate = "";
			 String transactionDate = ""; 
			 String settlementDate = "";  
			 String allowanceProbableLoss = "";
			 String currency = "";  
			 String orgAmountOpening = "";
			 String usdAmountOpening = "";  
			 String tzsAmountOpening = ""; 
			 String orgAmountPayment = "";
			 String usdAmountPayment = "";
			 String tzsAmountPayment = "";
			 String orgAmountBalance = "";
			 String usdAmountBalance = "";  
			 String tzsAmountBalance = "";
			 String botProvision = "";
			 
			 JsonObject notifyCorporateFinancialTransactionRequest = data.has("notifyCorporateFinancialTransactionRequest") ? data.get("notifyCorporateFinancialTransactionRequest").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReqPayload = notifyCorporateFinancialTransactionRequest.has("notifyCorporateFinancialTransactionReqPayload") ? notifyCorporateFinancialTransactionRequest.get("notifyCorporateFinancialTransactionReqPayload").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReq = notifyCorporateFinancialTransactionReqPayload.has("notifyCorporateFinancialTransactionReq") ? notifyCorporateFinancialTransactionReqPayload.get("notifyCorporateFinancialTransactionReq").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionDetails = notifyCorporateFinancialTransactionReq.has("TransactionDetails") ? notifyCorporateFinancialTransactionReq.get("TransactionDetails").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionEntry = TransactionDetails.has("TransactionEntry") ? TransactionDetails.get("TransactionEntry").getAsJsonObject() : new JsonObject();
			
			 JsonObject Account = TransactionEntry.has("Account") ? TransactionEntry.get("Account").getAsJsonObject() : new JsonObject();
			 
			 String[] lines = INFO2.split("\n");

			 String matchedApartaMsg = lines[1];
			 
		 	 chequeNumber = matchedApartaMsg.substring(64, 70);
		     issuerName = Account.has("ShortName") ? Account.get("ShortName").getAsString() : "NA";
		     issuerBankerCode = "SCBLTZTXXXX";
			 payeeName = util.isNullOrEmpty(matchedApartaMsg.substring(94,124).trim()) ? "NA" : matchedApartaMsg.substring(94,124).trim();					 
			 payeeAccountNumber = matchedApartaMsg.substring(38 , 51);
			
			 String tDate = matchedApartaMsg.substring(70, 73);

			 transactionDate = convertJulianToDate(tDate) + "0000";
				
			 settlementDate = transactionDate;
			 
			 allowanceProbableLoss = "0";	
			 			 
			 currency = Account.has("CurrencyCode") ? util.isNullOrEmpty(Account.get("CurrencyCode").getAsString())? "TZS" : Account.get("CurrencyCode").getAsString() : "TZS";
						 
			 orgAmountOpening = TransactionEntry.has("TrnAmount")? TransactionEntry.get("TrnAmount").getAsString():"0.00";

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
	  
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountOpening, currency);
			  
			 usdAmountOpening = rates.get("usd").getAsString();
			 tzsAmountOpening = rates.get("tzs").getAsString();

			 orgAmountPayment = matchedApartaMsg.substring(5, 17).replaceFirst("^0+", "");
			 
			 orgAmountPayment = orgAmountPayment.substring(0, orgAmountPayment.length() - 2) + "." + orgAmountPayment.substring(orgAmountPayment.length() - 2);
		        			    
			 JsonObject rates_Payment = fx.find_exchangeRate(util.getCurrentDate(), orgAmountPayment, currency);
		 	 
			 usdAmountPayment = rates_Payment.get("usd").getAsString();
			 tzsAmountPayment = rates_Payment.get("tzs").getAsString();

			 JsonObject BalanceDetails = TransactionEntry.has("BalanceDetails")? TransactionEntry.get("BalanceDetails").getAsJsonObject(): new JsonObject();
			 orgAmountBalance = BalanceDetails.has("Ledger")? BalanceDetails.get("Ledger").getAsString():"0.00";

			 JsonObject rates_Balance = fx.find_exchangeRate(util.getCurrentDate(), orgAmountBalance, currency);
			 
			 usdAmountBalance = rates_Balance.get("usd").getAsString();
			 tzsAmountBalance = rates_Balance.get("tzs").getAsString();

			 botProvision = "0";

			 int count = 0;
			
			 count++;
			 
			 String sql = "select * from CHEQUE001 where CHEQUENO = ? ";  
			 
			 List<Cheque001> Info = Jdbctemplate.query(sql, new Object[] {  chequeNumber }, new Cheque001Mapper());
			 			 
			 chequeDate = util.Convert_BOT_Date_Format(Info.get(0).getChequedate().toString(), "yyyy-MM-dd");
			 			
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info_1 = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info_1.size() > 0 ? !util.isNullOrEmpty(Info_1.get(0).getCOLUMN1()) ? Info_1.get(0).getCOLUMN1() : "834" : "834";

			 
			  sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL ,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
			 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
			 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
			 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
			  Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "chequesClearing",1, reportingDate, chequeNumber, issuerName,
								 issuerBankerCode, payeeName, payeeAccountNumber, chequeDate, transactionDate,
								 settlementDate, allowanceProbableLoss, currency, orgAmountOpening, usdAmountOpening,
								 tzsAmountOpening, orgAmountPayment, usdAmountPayment, tzsAmountPayment, orgAmountBalance, usdAmountBalance,
								 tzsAmountBalance, botProvision});
			 
			  sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
			  Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "chequesClearing"});
					 
			  sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			   Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "chequesClearing","serial", "reportingDate", "chequeNumber", 
					 "issuerName", "issuerBankerCode", "payeeName", "payeeAccountNumber", "chequeDate", "transactionDate", 
					 "settlementDate", "allowanceProbableLoss", "currency", "orgAmountOpening", "usdAmountOpening",
					 "tzsAmountOpening", "orgAmountPayment", "usdAmountPayment", "tzsAmountPayment", "orgAmountBalance", "usdAmountBalance", "tzsAmountBalance",
					 "botProvision"});
				 
			 sql = "update cheque002 set ispushed =  ? where chequeno = ?";
			 
			 Jdbctemplate.update(sql, new Object[] { 1 , chequeNumber });
			
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets", "chequesClearing", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "InwardCheckClearing" });	 
			 
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
			 			 
			 logger.error("Error processing InwardCheckClearing", e.getMessage()); 
		 }
		 
		 return details;
	}
		
	public JsonObject OutWardCheckClearing(String INFO1, String INFO2, String INFO3) //RTS031
	{ 
		 JsonObject details = new JsonObject();
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 			 
			 String reportingDate = util.getCurrentReportDate();
			 String chequeNumber = "";
			 String issuerName = "";
			 String issuerBankerCode =  "";
			 String payeeName = "";
			 String payeeAccountNumber = "";
			 String chequeDate = "";
			 String transactionDate = ""; 
			 String settlementDate = "";  
			 String allowanceProbableLoss = "";
			 String currency = "";  
			 String orgAmountOpening = "";
			 String usdAmountOpening = "";  
			 String tzsAmountOpening = ""; 
			 String orgAmountPayment = "";
			 String usdAmountPayment = "";
			 String tzsAmountPayment = "";
			 String orgAmountBalance = "";
			 String usdAmountBalance = "";  
			 String tzsAmountBalance = "";
			 String botProvision = "";

			 JsonObject notifyCorporateFinancialTransactionRequest = data.has("notifyCorporateFinancialTransactionRequest") ? data.get("notifyCorporateFinancialTransactionRequest").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReqPayload = notifyCorporateFinancialTransactionRequest.has("notifyCorporateFinancialTransactionReqPayload") ? notifyCorporateFinancialTransactionRequest.get("notifyCorporateFinancialTransactionReqPayload").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReq = notifyCorporateFinancialTransactionReqPayload.has("notifyCorporateFinancialTransactionReq") ? notifyCorporateFinancialTransactionReqPayload.get("notifyCorporateFinancialTransactionReq").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionDetails = notifyCorporateFinancialTransactionReq.has("TransactionDetails") ? notifyCorporateFinancialTransactionReq.get("TransactionDetails").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionEntry = TransactionDetails.has("TransactionEntry") ? TransactionDetails.get("TransactionEntry").getAsJsonObject() : new JsonObject();
			 
			 JsonObject Account = TransactionEntry.has("Account") ? TransactionEntry.get("Account").getAsJsonObject() : new JsonObject();
			 
			 String[] lines = INFO2.split("\n");

			 String matchedApartaMsg = lines[1];
			 
			 chequeNumber = matchedApartaMsg.substring(64, 70); 
			 issuerName = util.isNullOrEmpty(matchedApartaMsg.substring(94,124).trim()) ? "NA" : matchedApartaMsg.substring(94,124).trim();
			 issuerBankerCode = "SCBLTZTXXXX";
			 payeeName = Account.has("ShortName") ? Account.get("ShortName").getAsString() : "NA";				 
			 payeeAccountNumber = matchedApartaMsg.substring(24, 37);
			 
			 String tDate = matchedApartaMsg.substring(70, 73);

			 transactionDate = convertJulianToDate(tDate) + "0000";
				
			 settlementDate = transactionDate;
			 
			 allowanceProbableLoss = "0";
			 			 
			 currency = Account.has("CurrencyCode") ? util.isNullOrEmpty(Account.get("CurrencyCode").getAsString())? "TZS" : Account.get("CurrencyCode").getAsString() : "TZS";

			 orgAmountOpening = TransactionEntry.has("TrnAmount")? TransactionEntry.get("TrnAmount").getAsString() : "0.00";
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			  
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountOpening, currency);
 
			 usdAmountOpening = rates.get("usd").getAsString();
			 tzsAmountOpening = rates.get("tzs").getAsString();

			 orgAmountPayment = matchedApartaMsg.substring(5, 17).replaceFirst("^0+", "");
			 
			 orgAmountPayment = orgAmountPayment.substring(0, orgAmountPayment.length() - 2) + "." + orgAmountPayment.substring(orgAmountPayment.length() - 2);

			 JsonObject rates_Payment = fx.find_exchangeRate(util.getCurrentDate(), orgAmountPayment, currency);
			 
			 usdAmountPayment = rates_Payment.get("usd").getAsString();
			 tzsAmountPayment = rates_Payment.get("tzs").getAsString();

			 JsonObject BalanceDetails = TransactionEntry.has("BalanceDetails")? TransactionEntry.get("BalanceDetails").getAsJsonObject() : new JsonObject();
			 orgAmountBalance = BalanceDetails.has("Ledger")? BalanceDetails.get("Ledger").getAsString() : "0.00";
			
			 JsonObject rates_Balance = fx.find_exchangeRate(util.getCurrentDate(), orgAmountBalance, currency);
			 
			 usdAmountBalance = rates_Balance.get("usd").getAsString();
			 tzsAmountBalance = rates_Balance.get("tzs").getAsString();

			 botProvision = "0";
	 
			 int count = 0;
		 		
			 count++;
			 
			 String sql = "select * from CHEQUE001 where CHEQUENO = ? ";  
			 
			 List<Cheque001> Info = Jdbctemplate.query(sql, new Object[] {  chequeNumber }, new Cheque001Mapper());
			 			 
			 chequeDate = util.Convert_BOT_Date_Format(Info.get(0).getChequedate().toString(), "yyyy-MM-dd");
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info_1 = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info_1.size() > 0 ? !util.isNullOrEmpty(Info_1.get(0).getCOLUMN1()) ? Info_1.get(0).getCOLUMN1() : "834" : "834";
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL ,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
			 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
			 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
			 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "chequesClearing",1, reportingDate, chequeNumber, issuerName,
					 issuerBankerCode, payeeName, payeeAccountNumber, chequeDate, transactionDate,
					 settlementDate, allowanceProbableLoss, currency, orgAmountOpening, usdAmountOpening,
					 tzsAmountOpening, orgAmountPayment, usdAmountPayment, tzsAmountPayment, orgAmountBalance, usdAmountBalance,
					 tzsAmountBalance, botProvision});
 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "chequesClearing"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "chequesClearing","serial", "reportingDate", "chequeNumber", 
					 "issuerName", "issuerBankerCode", "payeeName", "payeeAccountNumber", "chequeDate", "transactionDate", 
					 "settlementDate", "allowanceProbableLoss", "currency", "orgAmountOpening", "usdAmountOpening",
					 "tzsAmountOpening", "orgAmountPayment", "usdAmountPayment", "tzsAmountPayment", "orgAmountBalance", "usdAmountBalance", "tzsAmountBalance",
					 "botProvision"});
	
			 sql = "update cheque002 set ispushed =  ? where chequeno = ?";
				 
			 Jdbctemplate.update(sql, new Object[] { 1 , chequeNumber });
			  			
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets", "chequesClearing", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "OutWardCheckClearing" });	 
		     
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
			 
			 
			 logger.error("Error processing OutWardCheckClearing", e.getMessage()); 
		 }
		
		 return details;
	}
		
	public JsonObject InwardReturn(String INFO1, String INFO2, String INFO3) //RTS031 
	{ 
		 JsonObject details = new JsonObject();
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String chequeNumber = "";
			 String issuerName = "";
			 String issuerBankerCode =  "";
			 String payeeName = "";
			 String payeeAccountNumber = "";
			 String chequeDate = "";
			 String transactionDate = ""; 
			 String settlementDate = "";  
			 String allowanceProbableLoss = "";
			 String currency = "";  
			 String orgAmountOpening = "";
			 String usdAmountOpening = "";  
			 String tzsAmountOpening = ""; 
			 String orgAmountPayment = "";
			 String usdAmountPayment = "";
			 String tzsAmountPayment = "";
			 String orgAmountBalance = "";
			 String usdAmountBalance = "";  
			 String tzsAmountBalance = "";
			 String botProvision = "";

			 JsonObject notifyCorporateFinancialTransactionRequest = data.has("notifyCorporateFinancialTransactionRequest") ? data.get("notifyCorporateFinancialTransactionRequest").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReqPayload = notifyCorporateFinancialTransactionRequest.has("notifyCorporateFinancialTransactionReqPayload") ? notifyCorporateFinancialTransactionRequest.get("notifyCorporateFinancialTransactionReqPayload").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReq = notifyCorporateFinancialTransactionReqPayload.has("notifyCorporateFinancialTransactionReq") ? notifyCorporateFinancialTransactionReqPayload.get("notifyCorporateFinancialTransactionReq").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionDetails = notifyCorporateFinancialTransactionReq.has("TransactionDetails") ? notifyCorporateFinancialTransactionReq.get("TransactionDetails").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionEntry = TransactionDetails.has("TransactionEntry") ? TransactionDetails.get("TransactionEntry").getAsJsonObject() : new JsonObject();
		 
			 JsonObject Account = TransactionEntry.has("Account")  ? TransactionEntry.get("Account").getAsJsonObject() : new JsonObject();
			 
			 String[] lines = INFO2.split("\n");

			 String matchedApartaMsg = lines[1];
			 
			 chequeNumber = matchedApartaMsg.substring(64, 70);
			 issuerName = util.isNullOrEmpty(matchedApartaMsg.substring(94, 124).trim()) ? "NA" : matchedApartaMsg.substring(94, 124).trim();
			 issuerBankerCode = "SCBLTZTXXXX";
			 payeeName = Account.has("ShortName") ? Account.get("ShortName").getAsString() : "NA";		 
			 payeeAccountNumber = matchedApartaMsg.substring(24,37);
			 
			 String tDate = matchedApartaMsg.substring(70, 73); 

			 transactionDate = convertJulianToDate(transactionDate) + "0000";
				
			 settlementDate = transactionDate;

			 allowanceProbableLoss = "0";		
			 
			 currency = Account.has("CurrencyCode") ? util.isNullOrEmpty(Account.get("CurrencyCode").getAsString())? "TZS" : Account.get("CurrencyCode").getAsString() : "TZS";
			 
			 orgAmountOpening = TransactionEntry.has("TrnAmount") ?  TransactionEntry.get("TrnAmount").getAsString() : "0.00";	
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountOpening, currency);
			 
			 usdAmountOpening = rates.get("usd").getAsString();
			 tzsAmountOpening = rates.get("tzs").getAsString();

			 orgAmountPayment = matchedApartaMsg.substring(4,17).replaceFirst("^0+", "");
			 
			 orgAmountPayment = orgAmountPayment.substring(0, orgAmountPayment.length() - 2) + "." + orgAmountPayment.substring(orgAmountPayment.length() - 2);

			 JsonObject rates_Payment = fx.find_exchangeRate(util.getCurrentDate(), orgAmountPayment, currency);
			 
			 usdAmountPayment = rates_Payment.get("usd").getAsString();
			 tzsAmountPayment = rates_Payment.get("tzs").getAsString();
				 				
			 JsonObject BalanceDetails = TransactionEntry.has("BalanceDetails")? TransactionEntry.get("BalanceDetails").getAsJsonObject():new JsonObject();
			 orgAmountBalance = BalanceDetails.has("Ledger")? BalanceDetails.get("Ledger").getAsString():"0.00";

			 JsonObject rates_Balance = fx.find_exchangeRate(util.getCurrentDate(), orgAmountBalance, currency);
			 
			 usdAmountBalance = rates_Balance.get("usd").getAsString();
			 tzsAmountBalance = rates_Balance.get("tzs").getAsString();

			 botProvision = "0";

			 int count = 0;
		 			
			 count++;
			
			 String sql = "select * from CHEQUE001 where CHEQUENO = ? ";  
			 
			 List<Cheque001> Info = Jdbctemplate.query(sql, new Object[] {  chequeNumber }, new Cheque001Mapper());
			 			 
			 chequeDate = util.Convert_BOT_Date_Format(Info.get(0).getChequedate().toString(), "yyyy-MM-dd");
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info_1 = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info_1.size() > 0 ? !util.isNullOrEmpty(Info_1.get(0).getCOLUMN1()) ? Info_1.get(0).getCOLUMN1() : "834" : "834";

			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL ,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
					 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
					 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
					 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 																						
			  Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "chequesClearing",1, reportingDate, chequeNumber, issuerName,
						 issuerBankerCode, payeeName, payeeAccountNumber, chequeDate, transactionDate,
						 settlementDate, allowanceProbableLoss, currency, orgAmountOpening, usdAmountOpening,
						 tzsAmountOpening, orgAmountPayment, usdAmountPayment, tzsAmountPayment, orgAmountBalance, usdAmountBalance,
						 tzsAmountBalance, botProvision});
	 
			  sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
			  Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "chequesClearing"});
				 
			  sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			  Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "chequesClearing","serial", "reportingDate", "chequeNumber", 
					 "issuerName", "issuerBankerCode", "payeeName", "payeeAccountNumber", "chequeDate", "transactionDate", 
					 "settlementDate", "allowanceProbableLoss", "currency", "orgAmountOpening", "usdAmountOpening",
					 "tzsAmountOpening", "orgAmountPayment", "usdAmountPayment", "tzsAmountPayment", "orgAmountBalance", "usdAmountBalance", "tzsAmountBalance",
					 "botProvision"});

			 sql = "update cheque002 set ispushed =  ? where chequeno = ?";
			 
			 Jdbctemplate.update(sql, new Object[] { 1 , chequeNumber });

			  
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets", "chequesClearing", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "InwardReturn" });	 
		     
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
			 
			 
			 logger.error("Error processing InwardReturn", e.getMessage()); 
		 }
		
		 return details;
	}
	
	public JsonObject OutWardReturn(String INFO1, String INFO2, String INFO3) //RTS031 
	{ 
		 JsonObject details = new JsonObject();

		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String chequeNumber = "";
			 String issuerName = "";
			 String issuerBankerCode =  "";
			 String payeeName = "";
			 String payeeAccountNumber = "";
			 String chequeDate = "";
			 String transactionDate = ""; 
			 String settlementDate = "";  
			 String allowanceProbableLoss = "";
			 String currency = "";  
			 String orgAmountOpening = "";
			 String usdAmountOpening = "";  
			 String tzsAmountOpening = ""; 
			 String orgAmountPayment = "";
			 String usdAmountPayment = "";
			 String tzsAmountPayment = "";
			 String orgAmountBalance = "";
			 String usdAmountBalance = "";  
			 String tzsAmountBalance = "";
			 String botProvision = "";

			 JsonObject notifyCorporateFinancialTransactionRequest = data.has("notifyCorporateFinancialTransactionRequest") ? data.get("notifyCorporateFinancialTransactionRequest").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReqPayload = notifyCorporateFinancialTransactionRequest.has("notifyCorporateFinancialTransactionReqPayload") ? notifyCorporateFinancialTransactionRequest.get("notifyCorporateFinancialTransactionReqPayload").getAsJsonObject() : new JsonObject();
			 JsonObject notifyCorporateFinancialTransactionReq = notifyCorporateFinancialTransactionReqPayload.has("notifyCorporateFinancialTransactionReq") ? notifyCorporateFinancialTransactionReqPayload.get("notifyCorporateFinancialTransactionReq").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionDetails = notifyCorporateFinancialTransactionReq.has("TransactionDetails") ? notifyCorporateFinancialTransactionReq.get("TransactionDetails").getAsJsonObject() : new JsonObject();
			 JsonObject TransactionEntry = TransactionDetails.has("TransactionEntry") ? TransactionDetails.get("TransactionEntry").getAsJsonObject() : new JsonObject();

			 JsonObject Account = TransactionEntry.has("Account") ? TransactionEntry.get("Account").getAsJsonObject() : new JsonObject();
			 
			 String[] lines = INFO2.split("\n");

			 String matchedApartaMsg = lines[1];

			 chequeNumber = matchedApartaMsg.substring(64, 70);
			 issuerName = Account.has("ShortName") ? Account.get("ShortName").getAsString() :  "NA";
			 issuerBankerCode = "SCBLTZTXXXX";		
			 payeeName = util.isNullOrEmpty(matchedApartaMsg.substring(94, 124).trim()) ? "NA" : matchedApartaMsg.substring(94, 124).trim();
			 payeeAccountNumber = matchedApartaMsg.substring(38, 51);
			 
			 String tDate = matchedApartaMsg.substring(70, 73); // Extract the Julian date part

			 transactionDate = convertJulianToDate(tDate) + "0000";
				
			 settlementDate = transactionDate;

			 allowanceProbableLoss = "0";
			 
			 currency = Account.has("CurrencyCode") ? util.isNullOrEmpty(Account.get("CurrencyCode").getAsString())? "TZS" : Account.get("CurrencyCode").getAsString() : "TZS";
			 
			 orgAmountOpening = TransactionEntry.has("TrnAmount") ? TransactionEntry.get("TrnAmount").getAsString() : "0.00";

			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);

			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountOpening, currency);
			 
			 usdAmountOpening = rates.get("usd").getAsString();
			 tzsAmountOpening = rates.get("tzs").getAsString();

			 orgAmountPayment = matchedApartaMsg.substring(5, 17).replaceFirst("^0+", "");
			 
			 orgAmountPayment = orgAmountPayment.substring(0, orgAmountPayment.length() - 2) + "." + orgAmountPayment.substring(orgAmountPayment.length() - 2);
			 
			 JsonObject rates_Payment = fx.find_exchangeRate(util.getCurrentDate(), orgAmountPayment, currency);
			 
			 usdAmountPayment = rates_Payment.get("usd").getAsString();
			 tzsAmountPayment = rates_Payment.get("tzs").getAsString();

			 JsonObject BalanceDetails = TransactionEntry.has("BalanceDetails") ? TransactionEntry.get("BalanceDetails").getAsJsonObject() : new JsonObject();
			 orgAmountBalance = BalanceDetails.has("Ledger") ? BalanceDetails.get("Ledger").getAsString() : "0.00";

			 JsonObject rates_Balance = fx.find_exchangeRate(util.getCurrentDate(), orgAmountBalance, currency);
			 
			 usdAmountBalance = rates_Balance.get("usd").getAsString();
			 tzsAmountBalance = rates_Balance.get("tzs").getAsString();

			 botProvision = "0";
			 				 
			 int count = 0;
		 		
			 count++;
			 
			 String sql = "select * from CHEQUE001 where CHEQUENO = ? ";  
			 
			 List<Cheque001> Info = Jdbctemplate.query(sql, new Object[] {  chequeNumber }, new Cheque001Mapper());
			 			 
			 chequeDate = util.Convert_BOT_Date_Format(Info.get(0).getChequedate().toString(), "yyyy-MM-dd");
		 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info_1 = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info_1.size() > 0 ? !util.isNullOrEmpty(Info_1.get(0).getCOLUMN1()) ? Info_1.get(0).getCOLUMN1() : "834" : "834";

			sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL ,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "chequesClearing",1, reportingDate, chequeNumber, issuerName,
					 issuerBankerCode, payeeName, payeeAccountNumber, chequeDate, transactionDate,
					 settlementDate, allowanceProbableLoss, currency, orgAmountOpening, usdAmountOpening,
					 tzsAmountOpening, orgAmountPayment, usdAmountPayment, tzsAmountPayment, orgAmountBalance, usdAmountBalance,
					 tzsAmountBalance, botProvision});
 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "chequesClearing"});
				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7,"
				 		+ " COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18,"
				 		+ " COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24,COLUMN25\r\n" + 
				 		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "chequesClearing","serial", "reportingDate", "chequeNumber", 
					 "issuerName", "issuerBankerCode", "payeeName", "payeeAccountNumber", "chequeDate", "transactionDate", 
					 "settlementDate", "allowanceProbableLoss", "currency", "orgAmountOpening", "usdAmountOpening",
					 "tzsAmountOpening", "orgAmountPayment", "usdAmountPayment", "tzsAmountPayment", "orgAmountBalance", "usdAmountBalance", "tzsAmountBalance",
					 "botProvision"});
	
			 sql = "update cheque002 set ispushed =  ? where chequeno = ?";
			 
			 Jdbctemplate.update(sql, new Object[] { 1 , chequeNumber });
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets", "chequesClearing", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "OutWardReturn" });	 
		     
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
			 
			 
			 logger.error("Error processing OutWardReturn", e.getMessage()); 
		 }
		
		 return details;
	}
	

//------------------------------------------------------------------------------
	
	private Clob createClobFromString(String message) throws SQLException 
	{
	    java.sql.Connection connection = Jdbctemplate.getDataSource().getConnection(); 
	    Clob clob = connection.createClob();
	    clob.setString(1, message);
	    connection.close();
	    return clob;
	}
	
	 public String readFolderAsString(String folderPath) 
	 {
        StringBuilder content = new StringBuilder();
        
        try
        {
        	File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) 
            {
                File[] files = folder.listFiles();
                if (files != null) 
                {
                    content.append("Total files: ").append(files.length).append(System.lineSeparator());

                    for (File file : files) 
                    {
                        if (file.isFile())
                        {
                            content.append("Absolute Path: ").append(file.getAbsolutePath()).append(System.lineSeparator());
                            String Filepath = file.getAbsolutePath();
                            String datas = (readFileAsString(Filepath)) ;
                            Aparta_filter(datas.toString());
                            
                             String sql = "SELECT * FROM prop001 WHERE CHCODE=?";
                       
    	           			 List<Prop001> prop = Jdbctemplate.query(sql, new Object[] { "APARTA_Bk" }, new Prop001RowMapper());
    	                  			 
    	          			 if(prop.size() > 0)
    	          			 {
    	          				String targetFolderPath = prop.get(0).getmTypeParam();
    	                        
    	                        moveFileToFolder(file, targetFolderPath);
    	          			 }                               
                        }
                    }
                } 
                else 
                {
                    content.append("The folder is empty.").append(System.lineSeparator());
                }
            } 
            else 
            {
                content.append("Invalid folder path.").append(System.lineSeparator());
            }
        }
        catch(Exception ex)
        {
        	logger.debug("exception in readFolderAsString : "+ex.getLocalizedMessage());
        }
        
        return content.toString();
    }
	 
	 private void moveFileToFolder(File file, String targetFolderPath) 
	 {
		 try
		 {
			 File targetFile = new File(targetFolderPath, file.getName());

		     if (new File(targetFolderPath).exists()) 
		     {
		         if (file.renameTo(targetFile)) 
		         {
		        	 logger.debug("File moved to: " + targetFile.getAbsolutePath());
		         } 
		         else 
		         {
		        	 logger.debug("Failed to move the file: " + file.getName());
		         }
		     } 
		     else 
		     {
		    	 logger.debug("Target folder does not exist. File not moved: " + file.getName());
		     }
		 }
		 catch(Exception ex)
		 {
			 logger.debug("exception in moveFileToFolder : "+ex.getLocalizedMessage());
		 }
	 }

    public static boolean isLeapYear(int year) 
	{
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

	    public static String convertJulianToDate(String julianDayStr) 
		{
	    	String out = "";
	    	
	    	try
	    	{
	    		int julianDay = Integer.parseInt(julianDayStr);
		        int currentYear = LocalDate.now().getYear();

		        int adjustedJulianDay = julianDay;
		        if (isLeapYear(currentYear) && julianDay >= 60) 
				{
		            adjustedJulianDay--; 
		        }

		        out = LocalDate.of(currentYear, 1, 1)
		                        .plusDays(adjustedJulianDay - 1)  
		                        .format(DateTimeFormatter.ofPattern("ddMMyyyy"));
	    	}
	    	catch(Exception ex)
	    	{
	    		logger.debug("exception in convertJulianToDate : "+ex.getLocalizedMessage());
	    	}
	        
	    	return out;
	    }

    public static String readFileAsString(String filePath) 
	    {
	        StringBuilder content = new StringBuilder();
	        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
	        {
	            String line;
	            while ((line = br.readLine()) != null) 
	            {
	                content.append(line).append(System.lineSeparator());
	            }
	        } 
	        catch (IOException e) 
	        {
	            System.err.println("Error reading file: " + filePath);
	            throw new RuntimeException("Failed to read file: " + filePath, e);
	        }
	        return content.toString();
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
		
   	private class Cheque001Mapper implements RowMapper<Cheque001> {
   	    public Cheque001 mapRow(ResultSet rs, int rowNum) throws SQLException {
   	        Cheque001 cheque001 = new Cheque001();
   	        
   	        // Mapping columns from the ResultSet to the Cheque001 object
   	        cheque001.setSuborgcode(rs.getString("suborgcode"));
   	        cheque001.setChequeno(rs.getString("chequeno"));
   	        cheque001.setChequedate(rs.getDate("chequedate"));
   	        cheque001.setAparta_date(rs.getDate("aparta_date"));
   	        cheque001.setRta_date(rs.getDate("rta_date"));
   	        cheque001.setRta_alert(rs.getBlob("rta_alert"));
   	        cheque001.setAparta_file(rs.getClob("aparta_file"));
   	        cheque001.setCheque_type(rs.getString("cheque_type"));
   	        
   	        return cheque001;
   	    }
   	}

   	private class Cheque002Mapper implements RowMapper<CHEQUE002> {
   	    Common_Utils util = new Common_Utils();
   	    
   	    public CHEQUE002 mapRow(ResultSet rs, int rowNum) throws SQLException {
   	        CHEQUE002 cheque = new CHEQUE002();
   	        
   	        cheque.setSuborgcode(util.ReplaceNull(rs.getString("SUBORGCODE")));
   	        cheque.setChequeno(rs.getString("CHEQUENO"));
   	        cheque.setApartaFlag(rs.getInt("APARTA_FLAG"));
   	        cheque.setRtaFlag(rs.getInt("RTA_FLAG"));
   	        cheque.setIspushed(rs.getInt("ISPUSHED"));
   	        
   	        return cheque;
   	    }
   	}
   	
   	public class Prop001RowMapper implements RowMapper<Prop001> {

   	    @Override
   	    public Prop001 mapRow(ResultSet rs, int rowNum) throws SQLException {
   	        Prop001 prop001 = new Prop001();
   	        prop001.setSubOrgCode(rs.getString("SUBORGCODE"));
   	        prop001.setChCode(rs.getString("CHCODE"));
   	        prop001.setModuleId(rs.getString("MODULEID"));
   	        prop001.setmType(rs.getString("MTYPE"));
   	        prop001.setmTypeParam(rs.getString("MTYPEPARAM"));
   	        prop001.setUserId(rs.getString("USERID"));
   	        return prop001;
   	    }
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
