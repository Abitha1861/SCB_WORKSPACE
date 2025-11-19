package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.j2objc.annotations.ReflectionSupport;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.Repositories.Account_Info;
import com.hdsoft.Repositories.FILEIT002;
import com.hdsoft.Repositories.FILEIT003;
import com.hdsoft.Repositories.Job_005;
import com.hdsoft.Repositories.Lookup001;
import com.hdsoft.Repositories.individual_info;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_EBBS implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_EBBS(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptect_EBBS() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_EBBS.class);

	//private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust pool size as needed
	
	/*@Scheduled(cron = "0 0/10 * * * *")   //Every 10 mins
    public void Account_Information_Thread()  
    {
		 try 
		 {
			 String APICODE = "RTS137";  // Account_Product_Category
			 
			 String sql = "select distinct ? APICODE, x.COLUMN1 PRODTYPE, x.COLUMN8 accountProductCode, x.COLUMN2 Prodate,\r\n" + 
			 		"y.COLUMN2 currency, x.COLUMN4 accountProductName, x.COLUMN5 accountProductDescription, x.COLUMN11 Status\r\n" + 
			 		"from fileit003 x, fileit003 y where x.purpcode = ? and y.purpcode = ? and  x.column8 = y.column1 \r\n" + 
			 		" and x.rtype = y.rtype and x.rtype = ? and ? = (select status from rts004 where APICODE = ?)\r\n" + 
			 		"and (?, (x.COLUMN1 || x.COLUMN8 || y.COLUMN2)) not in (select API_CODE, IDENTIFIER_VALUE from EBBS_CC_API_CALL_LOG)";
			 
			 List<FILEIT003> Products = Jdbctemplate.query(sql, new Object[] { APICODE, "EB004", "EB005", "D", "1", APICODE, APICODE }, new PROD_Mapper());
			 
			 for(FILEIT003 Data : Products)
		     {
				  String ProductInfo = new Gson().toJsonTree(Data).getAsJsonObject().toString();
				  
				  executorService.submit(() -> AccountProductCategory(APICODE, "", ProductInfo)); 
		     }
			 
			 String APICODE2 = "RTS193";  // Account_Information
			 
			 sql = "select distinct(ACCOUNTNO) from D12_ebbs D12 where (?, D12.ACCOUNTNO) not in (select API_CODE, IDENTIFIER_VALUE from EBBS_CC_API_CALL_LOG)\r\n" + 
			 		"and ACCOUNTNO <> ? and ? = (select status from rts004 where APICODE = ?) and rownum <= ?";
			 
			 List<String> Info = Jdbctemplate.queryForList(sql, new Object[] { APICODE2, "ACCOUNTNO", "1", APICODE2, "50" }, String.class);
			 
			 for(String AccountNo : Info)
		     {
				 executorService.submit(() -> Account_Information(APICODE2, "", AccountNo)); 
		     }
			 
			 String APICODE3 = "RTS117"; // Individual Information
			 
			 sql = "select distinct(RELATIONSHIPNO) from D10_ebbs D10 where (?, D10.RELATIONSHIPNO) not in (select API_CODE, IDENTIFIER_VALUE from EBBS_CC_API_CALL_LOG)\r\n" + 
			 		"and RELATIONSHIPNO <> ? and ? = (select status from rts004 where APICODE = ?) and rownum <= ?";
			 
			 Info = Jdbctemplate.queryForList(sql, new Object[] { APICODE3, "RELATIONSHIPNO", "1", APICODE3, "50" }, String.class);
			 
			 for(String RELATIONSHIPNO : Info)
		     {
				 executorService.submit(() -> Individual_Information(APICODE3, "", RELATIONSHIPNO)); 
		     } 
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in Account_Information_Thread :::: "+e.getLocalizedMessage());
		 }
    }
	
	@PreDestroy
	 public void shutdown() 
	 {
	        logger.info("Shutting down ExecutorService...");
	        
	        executorService.shutdown();
	        
	        try 
	        {
	            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) 
	            {
	                executorService.shutdownNow();
	            }
	        } 
	        catch (InterruptedException e) 
	        {
	            logger.error("Shutdown interrupted: " + e.getMessage(), e);
	            executorService.shutdownNow();
	        }
	  }
	*/
	
	@RequestMapping(value = {"/Datavision/ebbs/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody String Test_Service(@RequestHeader("APICODE") String APICODE, @RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		JsonObject details = new JsonObject();
		  
		try
		{
			 if(APICODE.equals("RTS011")) 
			 {
				 details = Balace_with_BOT("RTS011", "", Message);  //xml payload
			 } 
			 else if(APICODE.equals("RTS013")) 
			 {
				 details = Balace_with_Otherbank("RTS013", "", Message);  //xml payload
			 }
			 else if(APICODE.equals("RTS067")) 
			 {
				 details = Deposit_and_withdrawl("RTS067", "", Message);  //xml payload
			 }
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	public JsonObject Account_Information(String INFO1, String INFO2, String INFO3) //RTS193  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 final String Query = "{call PACK_EBBS.ACCOUNT_INFORMATION(?,?,?,?,?)}";  
			 
			 Map<String, Object> resultMap = Jdbctemplate.call(new CallableStatementCreator() {
	 				
					public CallableStatement createCallableStatement(Connection connection) throws SQLException {
 
						CallableStatement CS = connection.prepareCall(Query);
						
						CS.setString(1, INFO1);
						CS.setString(2, INFO2);
						CS.setString(3, INFO3);
						CS.registerOutParameter(4, Types.INTEGER);
						CS.registerOutParameter(5, Types.VARCHAR);
						
						return CS;
				}
 			 }, get_ProcedureParams());
			 
 			 String REPORTSL = util.ReplaceNull(resultMap.get("O_SERIAL"));
 			 String MESSAGE = util.ReplaceNull(resultMap.get("O_ERRMSG"));
 			 
 			 String Sql = "select suborgcode from sysconf001";
			
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
 			
			 String REQDATE = util.getCurrentDate("dd-MMM-yyyy");
			 
 			 String sql = "Insert into EBBS_CC_API_CALL_LOG(SUBORGCODE,REQDATE,API_NAME,API_CODE,IDENTIFIER_TYPE,IDENTIFIER_VALUE,ISPUSHED,PUSHEDON) values(?,?,?,?,?,?,?,?)";
 			
 			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, REQDATE, "Account Information", INFO1, "AccountNo", INFO3, "1", util.get_oracle_Timestamp() });	 
		     		
			 details.addProperty("Serial", REPORTSL);
	         details.addProperty("Batch_id", MESSAGE);
		 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in Account_Information:::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Individual_Information(String INFO1, String INFO2, String INFO3) //RTS117  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 final String Query = "{call PACK_EBBS.PERSONAL_DATA_INDIVIDUALS(?,?,?,?,?)}";  
			 
			 Map<String, Object> resultMap = Jdbctemplate.call(new CallableStatementCreator() {
	 				
					public CallableStatement createCallableStatement(Connection connection) throws SQLException {
 
						CallableStatement CS = connection.prepareCall(Query);
						
						CS.setString(1, INFO1);
						CS.setString(2, INFO2);
						CS.setString(3, INFO3);
						CS.registerOutParameter(4, Types.INTEGER);
						CS.registerOutParameter(5, Types.VARCHAR);
						
						return CS;
				}
 			 }, get_ProcedureParams());
			 
 			 String REPORTSL = util.ReplaceNull(resultMap.get("O_SERIAL"));
 			 String MESSAGE = util.ReplaceNull(resultMap.get("O_ERRMSG"));
 			 
 			 String Sql = "select suborgcode from sysconf001";
			
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
 			
			 String REQDATE = util.getCurrentDate("dd-MMM-yyyy");
			 
 			 String sql = "Insert into EBBS_CC_API_CALL_LOG(SUBORGCODE,REQDATE,API_NAME,API_CODE,IDENTIFIER_TYPE,IDENTIFIER_VALUE,ISPUSHED,PUSHEDON) values(?,?,?,?,?,?,?,?)";
 			
 			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, REQDATE, "Individual Information", INFO1, "CustomerIdentificationNumber", INFO3, "1", util.get_oracle_Timestamp() });	 
		     		
			 details.addProperty("Serial", REPORTSL);
	         details.addProperty("Batch_id", MESSAGE);
		 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in Account_Information:::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Balace_with_BOT(String INFO1, String INFO2, String INFO3) //RTS011
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject notifyInternalAccountsTransactionRequest = data.get("notifyInternalAccountsTransactionRequest").getAsJsonObject();
			 
			 JsonObject notifyInternalAccountsTransactionReqPayload = notifyInternalAccountsTransactionRequest.get("notifyInternalAccountsTransactionReqPayload").getAsJsonObject();
			 
			 JsonObject notifyInternalAccountsTransactionReq = notifyInternalAccountsTransactionReqPayload.get("notifyInternalAccountsTransactionReq").getAsJsonObject();
			 
			 JsonObject TransactionDetails = notifyInternalAccountsTransactionReq.get("TransactionDetails").getAsJsonObject();
			 
			 JsonObject TransactionEntry = TransactionDetails.get("TransactionEntry").getAsJsonObject();
			 
			 JsonObject Account = TransactionEntry.get("Account").getAsJsonObject();
			 
			 JsonObject TrnReference = TransactionEntry.get("TrnReference").getAsJsonObject();
		 
			 String reportingDate = util.getCurrentReportDate();
			 String accountNumber = Account.get("AccountNo").getAsString();
			 String accountName = Account.get("ShortName").getAsString();
			 String accountType = "1";  //D58
			 String subAccountType = "2"; 
			 String currency = TrnReference.get("CurrencyCode").getAsString();
			 String orgAmount = TransactionEntry.get("TrnAmount").getAsString();
			 String usdAmount = "0";  
			 String tzsAmount = orgAmount; 
			 String transactionDate = TransactionEntry.get("ValueDate").getAsString();
			 String maturityDate = TransactionEntry.get("ValueDate").getAsString();
			 String allowanceProbableLoss = "0";  //extract from splice
			 String botProvision = "0"; 
			
			 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
			 maturityDate = "121220991212"; //util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String Ac_currency = Account.get("CurrencyCode").getAsString();
			
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=? and COLUMN2=?";  
					 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND010", accountNumber, Ac_currency }, new Lookup001_mapper());
					 
			 int count = 0;
			 
			 if(Info.size() > 0)
			 {
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
				 
				 usdAmount = rates.get("usd").getAsString();
				 tzsAmount = rates.get("tzs").getAsString();
				 
				 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
				 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "balanceBOT", count, reportingDate, accountNumber, accountName, accountType, subAccountType, 
						currency, orgAmount, usdAmount, tzsAmount, transactionDate, maturityDate, allowanceProbableLoss, botProvision });
			 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "balanceBOT"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
						   "COLUMN17) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "balanceBOT", "serial", "reportingDate", "accountNumber", "accountName", "accountType", "subAccountType", 
							"currency", "orgAmount", "usdAmount", "tzsAmount", "transactionDate", "maturityDate", "allowanceProbableLoss", "botProvision" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "balanceBOT", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "EBBS" });	 
			     
			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			     
			     details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
		        
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch created successfully");   
			 }
			 else
			 {
				 logger.debug("Account "+accountNumber+" is not matched with Balace_with_BOT api");
				 
				 details.addProperty("Serial", "");
		         details.addProperty("Batch_id", "");
		        
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch is not created");  
			 }   
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
	
	public JsonObject Balace_with_Otherbank(String INFO1, String INFO2, String INFO3) //RTS013
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject notifyInternalAccountsTransactionRequest = data.get("notifyInternalAccountsTransactionRequest").getAsJsonObject();
			 
			 JsonObject notifyInternalAccountsTransactionReqPayload = notifyInternalAccountsTransactionRequest.get("notifyInternalAccountsTransactionReqPayload").getAsJsonObject();
			 
			 JsonObject notifyInternalAccountsTransactionReq = notifyInternalAccountsTransactionReqPayload.get("notifyInternalAccountsTransactionReq").getAsJsonObject();
			 
			 JsonObject TransactionDetails = notifyInternalAccountsTransactionReq.get("TransactionDetails").getAsJsonObject();
			 
			 JsonObject TransactionEntry = TransactionDetails.get("TransactionEntry").getAsJsonObject();
			 
			 JsonObject Account = TransactionEntry.get("Account").getAsJsonObject();
			 
			 JsonObject TrnReference = TransactionEntry.get("TrnReference").getAsJsonObject();
		 
			 String AccountNo = Account.get("AccountNo").getAsString();
			 
			 String Ac_currency = Account.get("CurrencyCode").getAsString();
			
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=? and COLUMN2=?";  
					 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND011", AccountNo, Ac_currency }, new Lookup001_mapper());
					 
			 int count = 0;
			 
			 if(Info.size() > 0)
			 {
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 String reportingDate = util.getCurrentReportDate();
				 String accountNumber = Account.get("AccountNo").getAsString();
				 String accountName = Account.get("ShortName").getAsString();
				 String bankCode = Info.get(0).getCOLUMN5();
				 String country = Info.get(0).getCOLUMN4();
				 String relationshipType = Info.get(0).getCOLUMN3();
				 String accountType = "2";  //D25
				 String subAccountType = ""; 
				 String currency = TrnReference.get("CurrencyCode").getAsString();
				 String orgAmount = TransactionEntry.get("TrnAmount").getAsString();
				 String usdAmount = "0";  
				 String tzsAmount = orgAmount;  
				 String transactionDate = TransactionEntry.get("ValueDate").getAsString();
				 String pastDueDays = "0";  
				 String allowanceProbableLoss = "0";  //extract from splice
				 String assetsClassificationCategory = "1";
				 String contractDate = reportingDate;  
				 String maturityDate = "311220990000";  
				 String externalRatingCorrespondentBank = Info.get(0).getCOLUMN6();
				 String gradesUnratedBanks = Info.get(0).getCOLUMN7();
				 String botProvision = "0"; 
				
				 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
				 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
				 
				 usdAmount = rates.get("usd").getAsString();
				 tzsAmount = rates.get("tzs").getAsString();
				 
				 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				
				 count++;
				 
				// relationshipType = 
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "balanceOtherBank", count, reportingDate, accountNumber, accountName, bankCode, country, relationshipType, accountType, subAccountType, 
						currency, orgAmount, usdAmount, tzsAmount, transactionDate, pastDueDays, allowanceProbableLoss, assetsClassificationCategory, contractDate, maturityDate, externalRatingCorrespondentBank, gradesUnratedBanks, botProvision });
			 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "balanceOtherBank"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "balanceOtherBank", "serial", "reportingDate", "accountNumber", "accountName", "bankCode", "country", "relationshipType", "accountType", "subAccountType", 
							"currency", "orgAmount", "usdAmount", "tzsAmount", "transactionDate", "pastDueDays", "allowanceProbableLoss", "assetsClassificationCategory", "contractDate", "maturityDate", "externalRatingCorrespondentBank", "gradesUnratedBanks", "botProvision" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "balanceOtherBank", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "EBBS" });	 
			     
			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			     
			     details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
		        
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch created successfully");   
			 }
			 else
			 {
				 logger.debug("Account "+AccountNo+" is not matched with Balace with other bank api");
				 
				 details.addProperty("Serial", "");
		         details.addProperty("Batch_id", "");
		        
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch is not created");  
			 }   
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Balace_with_Otherbank :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Deposit_and_withdrawl(String INFO1, String INFO2, String INFO3) //RTS067  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select count(*) from rts004 where APICODE = ? and STATUS = ?";
				
			 int Switch = Jdbctemplate.queryForObject(Sql, new Object[] { INFO1, "1" }, Integer.class);
			
			 if(Switch == 1)
			 {
				 final String Query = "{call PACK_EBBS.DEPOSIT_AND_WITHDRAWL(?,?,?,?,?)}";  
				 
				 Map<String, Object> resultMap = Jdbctemplate.call(new CallableStatementCreator() {
		 				
						public CallableStatement createCallableStatement(Connection connection) throws SQLException {
	 
							CallableStatement CS = connection.prepareCall(Query);
							
							CS.setString(1, INFO1);
							CS.setString(2, INFO2);
							CS.setString(3, INFO3);
							CS.registerOutParameter(4, Types.INTEGER);
							CS.registerOutParameter(5, Types.VARCHAR);
							
							return CS;
					}
	 			 }, get_ProcedureParams());
				 
	 			 String REPORTSL = util.ReplaceNull(resultMap.get("O_SERIAL"));
	 			 String MESSAGE = util.ReplaceNull(resultMap.get("O_ERRMSG"));
	 			 
				 details.addProperty("Serial", REPORTSL);
		         details.addProperty("Batch_id", MESSAGE);
			 
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch created successfully");    
			 }
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in DEPOSIT_AND_WITHDRAWL:::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject AccountProductCategory(String INFO1, String INFO2, String INFO3) //RTS137 verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 	
			 JsonObject info = util.StringToJsonObject(INFO3);
			 	 
			 int count = 0;
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String Status = info.get("COLUMN11").getAsString().trim();
				 
			 if(Status.equals("18")) 
			 {
				 Status = "2";
			 }
			 else
			 {
				 Status = "1";
			 }
			 
			 String Date = info.get("INDATE").getAsString();
			 
			 Date = util.Convert_BOT_Date_Format(Date, "yyyy-MM-dd");
			 
			 String prodType = info.get("COLUMN1").getAsString();  
			 
			 String reportingDate = util.getCurrentReportDate();
			 String accountProductCode = info.get("COLUMN8").getAsString(); 
			 String accountProductName = info.get("COLUMN4").getAsString();
			 String accountProductDescription = info.get("COLUMN4").getAsString();
			 String accountProductType = "";  //D25
			 String accountProductSubType = ""; //no value found
			 String currency = info.get("COLUMN2").getAsString();
			 String accountProductCreationDate = !Status.equals("18") ? Date : "<null>";  
			 String accountProductClosureDate = Status.equals("18") ? Date : "<null>";
			 String accountProductStatus = Status;
			 	 
			 String Identifier = prodType + accountProductCode + currency;
			 
			 String sql = "select * from lookup001 where COLUMN1=? and COLUMN11=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { info.get("COLUMN1").getAsString(), INFO1, "EBBS001" }, new Lookup001_mapper());
				
			 accountProductType = Info.size() > 0 ? Info.get(0).getCOLUMN3() : "9";
			 
			 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
					
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 
			 count++;
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "accountProductCategory", count, reportingDate, accountProductCode, accountProductName, accountProductDescription, accountProductType, 
					 accountProductSubType, currency, accountProductCreationDate, accountProductClosureDate, accountProductStatus });
		 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "accountProductCategory"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "accountProductCategory", "serial", "reportingDate", "accountProductCode", "accountProductName", "accountProductDescription", "accountProductType", 
					 "accountProductSubType", "currency", "accountProductCreationDate", "accountProductClosureDate", "accountProductStatus" });
		
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Banks Data", "accountProductCategory", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "EBBS" });	 
		     
 			 sql = "Insert into EBBS_CC_API_CALL_LOG(SUBORGCODE,REQDATE,API_NAME,API_CODE,IDENTIFIER_TYPE,IDENTIFIER_VALUE,ISPUSHED,PUSHEDON) values(?,?,?,?,?,?,?,?)";
 			
 			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, util.getCurrentDate("dd-MMM-yyyy"), "AccountProductCategory", INFO1, "Account Product", Identifier, "1", util.get_oracle_Timestamp() });	 
		     		
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
			 
			 logger.debug("Exception in AccountProductCategory :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
		
	public String FindElementFromFile(String PURPOSE, String Output_Column, String Input_Type, String Input_Value)  // Stitching logic from ebbs file
	{ 
		 String Out = "";
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select * from fileit002 where CHCODE=? and PURPOSE=? and COLUMN_NAME in (?,?,?)";
				
			 List<FILEIT002> result = Jdbctemplate.query(Sql, new Object[] { "EBBS", PURPOSE, Output_Column, "MASTERNO", "ACCOUNTNO" } , new FileIT002_mapper());
			
			 boolean ac_flag = false, mast_flag = false, inp_flag = false;    FILEIT002 Info = new FILEIT002();
			 	
			 String mast_pos = "0", ac_pos = "0", inp_pos = "0";
			 
			 for(int i=0; i<result.size(); i++)
			 {
				 if(result.get(i).getCOLUMN_NAME().equals("MASTERNO"))
				 {
					 mast_flag = true;
					 
					 mast_pos = result.get(i).getCOLUMN_POS();
				 }
				 else if(result.get(i).getCOLUMN_NAME().equals("ACCOUNTNO"))
				 {
					 ac_flag = true; 
					 
					 ac_pos = result.get(i).getCOLUMN_POS();
				 }
				 else if(result.get(i).getCOLUMN_NAME().equals(Output_Column))
				 {
					 inp_flag = true;
					 
					 inp_pos = result.get(i).getCOLUMN_POS();
					 
					 Info = result.get(i);
				 }
			 }
			 
			 if(inp_flag && mast_flag && ac_flag)
			 {
				 Sql = "select ? from fileit003 where CHCODE=? and PURPCODE=? and (?=? or ?=?)";
				 
				 List<String> info = Jdbctemplate.queryForList(Sql, new Object[] { "COLUMN"+inp_pos,  Info.getCHCODE(), Info.getPURPCODE(), "COLUMN"+mast_pos, Input_Value, "COLUMN"+ac_pos, Input_Value }, String.class);
				 
				 Out = info.size() > 0 ? info.get(0) : "";
			 }
			 else if(inp_flag && mast_flag)
			 {
				 if(Input_Type.equals("MASTERNO"))
				 {
					 Sql = "select ? from fileit003 where CHCODE=? and PURPCODE=? and ?=?";
					 
					 List<String> info = Jdbctemplate.queryForList(Sql, new Object[] { "COLUMN"+inp_pos,  Info.getCHCODE(), Info.getPURPCODE(), "COLUMN"+mast_pos, Input_Value }, String.class);
					 
					 Out = info.size() > 0 ? info.get(0) : "";
				 }
				 else  // input ac num
				 {
					 Sql = "select COLUMN7 from fileit003 where CHCODE=? and PURPCODE=? and COLUMN2=?";
					 
					 List<String> info = Jdbctemplate.queryForList(Sql, new Object[] { Info.getCHCODE(), "EB012", Input_Value }, String.class);
					 
					 String MasterNo = info.size() > 0 ? info.get(0) : "";
					 
					 Sql = "select ? from fileit003 where CHCODE=? and PURPCODE=? and ?=?";
					 
					 info = Jdbctemplate.queryForList(Sql, new Object[] { "COLUMN"+inp_pos,  Info.getCHCODE(), Info.getPURPCODE(), "COLUMN"+mast_pos, MasterNo }, String.class);
					 
					 Out = info.size() > 0 ? info.get(0) : "";
				 }
			 }
			 else if(inp_flag && ac_flag)
			 {
				 if(Input_Type.equals("ACCOUNTNO"))
				 {
					 Sql = "select ? from fileit003 where CHCODE=? and PURPCODE=? and ?=?";
					 
					 List<String> info = Jdbctemplate.queryForList(Sql, new Object[] { "COLUMN"+inp_pos,  Info.getCHCODE(), Info.getPURPCODE(), "COLUMN"+ac_pos, Input_Value }, String.class);
					 
					 Out = info.size() > 0 ? info.get(0) : "";
				 }
				 else // input master num
				 {
					 Sql = "select COLUMN2 from fileit003 where CHCODE=? and PURPCODE=? and COLUMN7=?";
					 
					 List<String> info = Jdbctemplate.queryForList(Sql, new Object[] { Info.getCHCODE(), "EB012", Input_Value }, String.class);
					 
					 String AcNo = info.size() > 0 ? info.get(0) : "";
					 
					 Sql = "select ? from fileit003 where CHCODE=? and PURPCODE=? and ?=?";
					 
					 info = Jdbctemplate.queryForList(Sql, new Object[] { "COLUMN"+inp_pos,  Info.getCHCODE(), Info.getPURPCODE(), "COLUMN"+ac_pos, AcNo }, String.class);
					 
					 Out = info.size() > 0 ? info.get(0) : "";
				 }	 
			 }
			 else
			 {
				  // other possible scenarios if needed
			 } 	 
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in FindElementFromFile :::: "+e.getLocalizedMessage());
		 }
		
		 return Out;
	}
	
	public JsonArray Get_file_Details(String Filepath)
	{ 
		 JsonArray details = new JsonArray();
		 
		 try
		 {
			 String file = Filepath;
			 
			 //String file = Filepath;
			 
			 @SuppressWarnings("resource")
			 BufferedReader br = new BufferedReader(new FileReader(file));
			 
		     String line;
		    
		     while ((line = br.readLine()) != null) 
		     {
		    	 String stsEndpoint = line; //properties.getProperty("stsEndpoint").trim();
				
		    	 stsEndpoint = stsEndpoint.replaceAll("§", "<tab>");
		    	 stsEndpoint = stsEndpoint.replaceAll("Â", "");
		    	 
		    	 //if(stsEndpoint.startsWith("TR") || stsEndpoint.startsWith("HR") || !stsEndpoint.contains("<tab>")) {
		    	//	 continue;
		    	// }
		    	 
		    	 String[] data = stsEndpoint.split("<tab>");
		    	 
		    	 if(data.length == 11)
		    	 {
		    		 JsonObject js = new JsonObject();
			    	 
			    	 js.addProperty("info1", data[0].trim());
			    	 js.addProperty("info2", data[1].trim());
			    	 js.addProperty("info3", data[2].trim());
			    	 js.addProperty("info4", data[3].trim());
			    	 js.addProperty("info5", data[4].trim());
			    	 js.addProperty("info6", data[5].trim());
			    	 js.addProperty("info7", data[6].trim());
			    	 js.addProperty("info8", data[7].trim());
			    	 js.addProperty("info9", data[8].trim());
			    	 js.addProperty("info10", data[9].trim());
			    	 js.addProperty("info11", data[10].trim());
			    	 
			    	 details.add(js);
		    	 }
		    	
		    	 //L<tab>2019-06-24<tab>N<tab>CALL DEPOSITS<tab>N<tab>N<tab>A<tab>400<tab>0 <tab>N<tab>1

		    	 
				 //System.out.println(stsEndpoint);
		     }
				
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in Get_file_Details :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	/*public void updateProperty(String key, String value)
	{
		try {
            // Load the properties file from classpath
            Resource resource = resourceLoader.getResource("classpath:hashicorp.properties"); 
            Properties properties = new Properties();

            // Load current properties
            try (InputStream input = resource.getInputStream()) {
            	
            	//System.out.println(input);
                properties.load(input);
                
                for (Entry<Object, Object> entry : properties.entrySet()) 
                {
                    String key_ = (String) entry.getKey();
                    String value_ = (String) entry.getValue();
                    
                    System.out.println("Key: " + key_ + ", Value: " + value_);
                }
            }

            // Update the property
            properties.setProperty(key, value);

            // Save changes back to the file
            try (FileOutputStream output = new FileOutputStream(resource.getFile())) {
                properties.store(output, "");
            }
            
            resource = resourceLoader.getResource("classpath:hashicorp.properties"); 
            
            try (InputStream input = resource.getInputStream()) {
            	
            	 properties.load(input);
            	 
                for (Entry<Object, Object> entry : properties.entrySet()) 
                {
                    String key_ = (String) entry.getKey();
                    String value_ = (String) entry.getValue();
                    
                    System.out.println("Key: " + key_ + ", Value: " + value_);
                }
            }
             
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	*/
	
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
	
	private class FileIT002_mapper implements RowMapper<FILEIT002>  
    {
    	Common_Utils util = new Common_Utils();
    	
		public FILEIT002 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			FILEIT002 Info = new FILEIT002(); 
			
			Info.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			Info.setCHCODE(util.ReplaceNull(rs.getString("CHCODE")));
			Info.setPURPOSE(util.ReplaceNull(rs.getString("PURPOSE")));
			Info.setFILEFORMAT(util.ReplaceNull(rs.getString("FILEFORMAT")));
			Info.setPURPCODE(util.ReplaceNull(rs.getString("PURPCODE")));
			Info.setFROMINDEX(util.ReplaceNull(rs.getString("FROMINDEX")));
			Info.setCOLUMN_NAME(util.ReplaceNull(rs.getString("COLUMN_NAME")));
			Info.setCOLUMN_POS(util.ReplaceNull(rs.getString("COLUMN_POS")));
			Info.setCOLUMN_ISPRIM(util.ReplaceNull(rs.getString("COLUMN_ISPRIM")));
			Info.setCOLUMN_TYPE(util.ReplaceNull(rs.getString("COLUMN_TYPE")));
			Info.setCOLUMN_SL(util.ReplaceNull(rs.getString("COLUMN_SL")));
			Info.setTOTCOLUMNS(util.ReplaceNull(rs.getString("TOTCOLUMNS")));
			
			return Info;
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
	
	private class PROD_Mapper implements RowMapper<FILEIT003> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public FILEIT003 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			FILEIT003 obj_003 = new FILEIT003();  
			
			obj_003.setAPICODE(util.ReplaceNull(rs.getString("APICODE")));
			obj_003.setINDATE(util.ReplaceNull(rs.getString("PRODATE")));
			obj_003.setCOLUMN1(util.ReplaceNull(rs.getString("PRODTYPE")));
			obj_003.setCOLUMN2(util.ReplaceNull(rs.getString("CURRENCY")));
			obj_003.setCOLUMN4(util.ReplaceNull(rs.getString("ACCOUNTPRODUCTNAME")));
			obj_003.setCOLUMN5(util.ReplaceNull(rs.getString("ACCOUNTPRODUCTDESCRIPTION")));
			obj_003.setCOLUMN8(util.ReplaceNull(rs.getString("ACCOUNTPRODUCTCODE")));
			obj_003.setCOLUMN11(util.ReplaceNull(rs.getString("STATUS")));
					
			return obj_003;
		}
	}
	
	private class Account_Info_mapper implements RowMapper<Account_Info> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public Account_Info mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			 Account_Info Acc = new Account_Info();  
 
			    Acc.setRELATIONSHIPNO(util.ReplaceNull(rs.getString("RELATIONSHIPNO")));
			    Acc.setRELATIONSHIPTYPE(util.ReplaceNull(rs.getString("RELATIONSHIPTYPE")));
			    Acc.setACCOUNTNO(util.ReplaceNull(rs.getString("ACCOUNTNO")));
			    Acc.setPRODUCTCODE(util.ReplaceNull(rs.getString("PRODUCTCODE")));
			    Acc.setACCTCURRENTSTATUS(util.ReplaceNull(rs.getString("ACCTCURRENTSTATUS")));  
			    Acc.setCUSTOMERTYPE(util.ReplaceNull(rs.getString("CUSTOMERTYPE")));
			    Acc.setSEGMENTCODE(util.ReplaceNull(rs.getString("SEGMENTCODE")));
			    Acc.setSMRCODE(util.ReplaceNull(rs.getString("SMRCODE")));
			    Acc.setKYCSTATUSCODE(util.ReplaceNull(rs.getString("KYCSTATUSCODE")));
			    Acc.setCONSTITUTIONCODE(util.ReplaceNull(rs.getString("CONSTITUTIONCODE"))); 
			    Acc.setSCIREFERENCENO(util.ReplaceNull(rs.getString("SCIREFERENCENO")));
			    Acc.setORGAMOUNT(util.ReplaceNull(rs.getString("ORGAMOUNT")));
			    Acc.setUSDAMOUNT(util.ReplaceNull(rs.getString("USDAMOUNT")));
			    Acc.setTZSAMOUNT(util.ReplaceNull(rs.getString("TZSAMOUNT")));
     
			    return Acc;
		}
	}
	
	private class individualInformation_mapper implements RowMapper<individual_info> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public individual_info mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			individual_info ind = new individual_info();  

			ind.setREPORTINGDATE(util.ReplaceNull(rs.getString("REPORTINGDATE")));
			ind.setRELATIONSHIPTYPE(util.ReplaceNull(rs.getString("RELATIONSHIPTYPE")));
			ind.setCUSTOMERIDENTIFICATIONNUMBER(util.ReplaceNull(rs.getString("CUSTOMERIDENTIFICATIONNUMBER")));
			ind.setSEGMENTCODE(util.ReplaceNull(rs.getString("SEGMENTCODE")));
			ind.setCUSTSEGMENTCODE(util.ReplaceNull(rs.getString("CUSTSEGMENTCODE")));
			ind.setFIRSTNAME(util.ReplaceNull(rs.getString("FIRSTNAME")));
			ind.setMIDDLENAMES(util.ReplaceNull(rs.getString("MIDDLENAMES")));
			ind.setFULLNAMES(util.ReplaceNull(rs.getString("FULLNAMES")));
			ind.setPRESENTSURNAME(util.ReplaceNull(rs.getString("PRESENTSURNAME")));
			ind.setBIRTHSURNAME(util.ReplaceNull(rs.getString("BIRTHSURNAME")));
			ind.setGENDER(util.ReplaceNull(rs.getString("GENDER")));
			ind.setMARITALSTATUS(util.ReplaceNull(rs.getString("MARITALSTATUS")));
			ind.setNUMBERSPOUSE(util.ReplaceNull(rs.getString("NUMBERSPOUSE")));
			ind.setNATIONALITY(util.ReplaceNull(rs.getString("NATIONALITY")));
			ind.setCITIZENSHIP(util.ReplaceNull(rs.getString("CITIZENSHIP")));
			ind.setRESIDENCY(util.ReplaceNull(rs.getString("RESIDENCY")));
			ind.setPROFESSION(util.ReplaceNull(rs.getString("PROFESSION")));
			ind.setSECTORSNACLASSIFICATION(util.ReplaceNull(rs.getString("SECTORSNACLASSIFICATION")));
			ind.setFATESTATUS(util.ReplaceNull(rs.getString("FATESTATUS")));
			ind.setSOCIALSTATUS(util.ReplaceNull(rs.getString("SOCIALSTATUS")));
			ind.setEMPLOYMENTSTATUS(util.ReplaceNull(rs.getString("EMPLOYMENTSTATUS")));
			ind.setMONTHLYINCOME(util.ReplaceNull(rs.getString("MONTHLYINCOME")));
			ind.setNUMBERDEPENDANTS(util.ReplaceNull(rs.getString("NUMBERDEPENDANTS")));
			ind.setEDUCATIONLEVEL(util.ReplaceNull(rs.getString("EDUCATIONLEVEL")));
			ind.setAVERAGEMONTHLYEXPENDITURE(util.ReplaceNull(rs.getString("AVERAGEMONTHLYEXPENDITURE")));
			ind.setBIRTHDATE(util.ReplaceNull(rs.getString("BIRTHDATE")));
			ind.setBIRTHCOUNTRY(util.ReplaceNull(rs.getString("BIRTHCOUNTRY")));
			ind.setBIRTHREGION(util.ReplaceNull(rs.getString("BIRTHREGION")));
			ind.setBIRTHDISTRICT(util.ReplaceNull(rs.getString("BIRTHDISTRICT")));
			ind.setIDENTIFICATIONTYPE(util.ReplaceNull(rs.getString("IDENTIFICATIONTYPE")));
			ind.setIDENTIFICATIONNUMBER(util.ReplaceNull(rs.getString("IDENTIFICATIONNUMBER")));
			ind.setISSUANCEDATE(util.ReplaceNull(rs.getString("ISSUANCEDATE")));
			ind.setEXPIRATIONDATE(util.ReplaceNull(rs.getString("EXPIRATIONDATE")));
			ind.setISSUANCEPLACE(util.ReplaceNull(rs.getString("ISSUANCEPLACE")));
			ind.setISSUINGAUTHORITY(util.ReplaceNull(rs.getString("ISSUINGAUTHORITY")));
			ind.setBUSINESSNAME(util.ReplaceNull(rs.getString("BUSINESSNAME")));
			ind.setESTABLISHMENTDATE(util.ReplaceNull(rs.getString("ESTABLISHMENTDATE")));
			ind.setBUSINESSREGISTRATIONNUMBER(util.ReplaceNull(rs.getString("BUSINESSREGISTRATIONNUMBER")));
			ind.setBUSINESSREGISTRATIONDATE(util.ReplaceNull(rs.getString("BUSINESSREGISTRATIONDATE")));
			ind.setBUSINESSLICENSENUMBER(util.ReplaceNull(rs.getString("BUSINESSLICENSENUMBER")));
			ind.setTAXIDENTIFICATIONNUMBER(util.ReplaceNull(rs.getString("TAXIDENTIFICATIONNUMBER")));
			ind.setEMPLOYERNAME(util.ReplaceNull(rs.getString("EMPLOYERNAME")));
			ind.setEMPLOYERREGION(util.ReplaceNull(rs.getString("EMPLOYERREGION")));
			ind.setEMPLOYERDISTRICT(util.ReplaceNull(rs.getString("EMPLOYERDISTRICT")));
			ind.setBUSINESSNATURE(util.ReplaceNull(rs.getString("BUSINESSNATURE")));
			ind.setMAINADDRESS(util.ReplaceNull(rs.getString("MAINADDRESS")));
			ind.setREGION(util.ReplaceNull(rs.getString("REGION")));
			ind.setWARD(util.ReplaceNull(rs.getString("WARD")));
			ind.setCOUNTRY(util.ReplaceNull(rs.getString("COUNTRY")));

			return ind;
		}
	}

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private List<SqlParameter> get_ProcedureParams()
	{
		List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
	    
		inParamMap.add(new SqlParameter("INFO1", Types.VARCHAR));
		inParamMap.add(new SqlParameter("INFO2", Types.VARCHAR));
		inParamMap.add(new SqlParameter("INFO3", Types.CLOB));
		inParamMap.add(new SqlOutParameter("O_SERIAL" , Types.INTEGER));
		inParamMap.add(new SqlOutParameter("O_ERRMSG"  , Types.VARCHAR));
	
		return inParamMap;
	}
}
