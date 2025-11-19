package com.hdsoft.models;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.COMPLAINTSFRAUDDETAILS;
import com.hdsoft.Repositories.FILEIT003;
import com.hdsoft.Repositories.Lookup001;
import com.hdsoft.Repositories.fraudIncidence_Information;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.zaxxer.hikari.HikariDataSource;

@Component
@Controller
public class BOT_Suptech_GEMS implements Database
{
	protected JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptech_GEMS() {}
	
	public BOT_Suptech_GEMS(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	/* prod
	 * select * from GEMS_ARCH_PROD_01.GEMS_COMPLAINTSFRAUDDETAILS@DATAVISION_LINK.UK.STANDARDCHARTERED.COM;

        select * from GEMS_ARCH_PROD_01.GEMS_SRFRAUDDETAILS@DATAVISION_LINK.UK.STANDARDCHARTERED.COM;

	 */
	
	//private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust pool size as needed
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptech_GEMS.class);
	
	/*@Scheduled(cron = "0 0/10 * * * *")   //Every 10 mins
    public void Account_Information_Thread()  
    {
		 try 
		 {
			 String APICODE = "RTS177";  // Personal data Trust
			 
			 String sql = "select Column1 leid from fileit003 where chcode = ? and rtype = ? and purpcode = ? and lower(column19) like ?\r\n" + 
			 		"and Column1 not in (select IDENTIFIER_VALUE from EBBS_CC_API_CALL_LOG where API_CODE = ?)\r\n" + 
			 		"and ? = (select count(*) from rts004 where apicode in (?) and status = ?) and rownum <= ?";
			 
			 List<String> LEIDs = Jdbctemplate.queryForList(sql, new Object[] { "BPSI", "D", "DAY0", "%trust%", APICODE, "1", APICODE, "1", "50"  }, String.class);
			 
			 for(String LEID : LEIDs)
		     {
				  executorService.submit(() -> trustDetails(APICODE, "", LEID)); 
		     }
			 
			 String APICODE2 = "RTS179";  // Personal data non profit
			 
			 sql = "select Column1 leid from fileit003 where chcode = ? and rtype = ? and purpcode = ? and lower(column19) like ?\r\n" + 
				 		"and Column1 not in (select IDENTIFIER_VALUE from EBBS_CC_API_CALL_LOG where API_CODE = ?)\r\n" + 
				 		"and ? = (select count(*) from rts004 where apicode in (?) and status = ?) and rownum <= ?";
				 
			 LEIDs = Jdbctemplate.queryForList(sql, new Object[] { "BPSI", "D", "DAY0", "%ngo%", APICODE2, "1", APICODE2, "1", "50" }, String.class);
				 
			 for(String LEID : LEIDs)
		     {
				  executorService.submit(() -> nonProfitOrgInformation(APICODE2, "", LEID));  
		     }
			 
			 String APICODE3 = "RTS119"; // Company Information
			 
			 sql = "select Column1 leid from fileit003 where chcode = ? and rtype = ? and purpcode = ? and lower(column19) not like ? and lower(column19) not like ?\r\n" + 
			 		"and Column1 not in (select IDENTIFIER_VALUE from EBBS_CC_API_CALL_LOG where API_CODE = ?)\r\n" + 
			 		"and ? = (select count(*) from rts004 where apicode in (?) and status = ?) and rownum <= ?";
			 
			 LEIDs = Jdbctemplate.queryForList(sql, new Object[] { "BPSI", "D", "DAY0", "%trust%", "%ngo%", APICODE3, "1", APICODE3, "1", "50" }, String.class);
			 
			 for(String LEID : LEIDs)
		     {
				  executorService.submit(() -> companyDetails(APICODE3, "", LEID));  
		     }
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in cc_Thread :::: "+e.getLocalizedMessage());
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
	  }*/
	
	@RequestMapping(value = {"/Datavision/gems/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestHeader("APICODE") String APICODE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
 	 	JsonObject details = new JsonObject();
		  
		try
		{
			 if(APICODE.equals("RTS149")) 
			 {
				 details = complaintStatistics("RTS149", "", "");  // no json/xml body required
			 }
			 else if(APICODE.equals("RTS183")) 
			 {
				 details = fraudIncidenceInformation("RTS183", "", "");  // no json/xml body required
			 }
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	public JsonObject complaintStatistics(String INFO1, String INFO2, String INFO)   //RTS149
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String table = Active_Mode.equals("local") ? "GEMS_COMPLAINTSFRAUDDETAILS" : 
				  			Active_Mode.equals("UAT") ? "GEMS_ARCH_UAT_01.GEMS_COMPLAINTSFRAUDDETAILS@clntcov" : 
				  			"GEMS_ARCH_PROD_01.GEMS_COMPLAINTSFRAUDDETAILS@DATAVISION_LINK.UK.STANDARDCHARTERED.COM";
			 
			 String sql = "select * from "+table;
	 			
			 List<COMPLAINTSFRAUDDETAILS> CF = Jdbctemplate.query(sql, new COMPLAINTSFRAUDDETAILS_mapper());
		
			 sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			
			 logger.debug("<<<<<<< complaintStatistics data >>>>");
			 
			 for(int i=0; i<CF.size();i++)
			 { 
				 logger.debug(new Gson().toJsonTree(CF.get(i)));
				 
				 String reportingDate = util.getCurrentReportDate();
				 String complainantName = CF.get(i).getClientName();
				 String complainantMobile = ""; 
				 String complaintType = CF.get(i).getComplaintType();
				 String occurrenceDate = util.Convert_BOT_Date_Format(CF.get(i).getPxCreatedDateTime(), "yyyy-MM-dd HH:mm:ss");
				 String complaintReportingDate = util.Convert_BOT_Date_Format(CF.get(i).getPxCreatedDateTime(), "yyyy-MM-dd HH:mm:ss");
				 String closureDate = "<null>"; 
				 String agentName = "";
				 String tillNumber = "";
				 String currency = util.isNullOrEmpty(CF.get(i).getRedressCurrency()) ? "TZS" :  CF.get(i).getRedressCurrency();
				 String orgAmount = util.isNullOrEmpty(CF.get(i).getRedressAmount()) ? "0" : CF.get(i).getRedressAmount();
				 String usdAmount = "0";
				 String tzsAmount = "0";
				 String employeeId = CF.get(i).getPxUpdateOperator();
				 String referredComplaints = "9";
				 String complaintStatus = CF.get(i).getComplaintStatus();
				 
				 if(complaintStatus.equalsIgnoreCase("closed")) 
				 {
					complaintStatus = "1";
				 }
				 else 
				 {
					complaintStatus = "2";
				 }
				 
				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
				 
				 usdAmount = rates.get("usd").getAsString();
				 tzsAmount = rates.get("tzs").getAsString();
				 
				 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { complaintType, "GEMS001" }, new Lookup001_mapper());
				
				 complaintType = Info.size() !=0 ? Info.get(0).getCOLUMN3() : null;
				 
				 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
						
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				 
				 int count = i+1;
				 	 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20) " + 
					      "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "complaintStatistics", count, reportingDate, complainantName, complainantMobile, complaintType, occurrenceDate, complaintReportingDate, closureDate, agentName, tillNumber, currency, orgAmount, usdAmount, tzsAmount, employeeId, referredComplaints, complaintStatus});
			 }	
		
			 if(CF.size() > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20) " + 
					     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "complaintStatistics", "serial","reportingDate", "complainantName", "complainantMobile", "complaintType", "occurrenceDate", "complaintReportingDate", "closureDate", "agentName", "tillNumber", "currency", "orgAmount", "usdAmount", "tzsAmount", "employeeId", "referredComplaints", "complaintStatus" });
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "complaintStatistics"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "complaintStatistics", INFO1, CF.size(), "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "GEMS" });	 				
			 }
			 
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
			 
			 logger.debug("Exception in complaintStatistics :::: "+e.getLocalizedMessage());
		 }
		 return details;
	}
	
	public JsonObject fraudIncidenceInformation(String INFO1, String INFO2, String INFO) //RTS183
	{ 
		JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String table = Active_Mode.equals("local") ? "GEMS_SRFRAUDDETAILS" :
				 		    Active_Mode.equals("UAT") ? "GEMS_ARCH_UAT_01.GEMS_SRFRAUDDETAILS@clntcov" :
				 		    "GEMS_ARCH_PROD_01.GEMS_SRFRAUDDETAILS@DATAVISION_LINK.UK.STANDARDCHARTERED.COM"		;
			 
			 String sql = "select * from "+table;
	 			
			 List<fraudIncidence_Information> CF = Jdbctemplate.query(sql, new fraudIncidenceInformation_mapper());
		
			 sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";
				 		 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			
			 for(int i=0; i<CF.size();i++)
			 {
				 String reportingDate = util.getCurrentReportDate();
				 String productProcessInvolved = CF.get(i).getProductName();
				 String discoveryModel = "1"; 
				 String identifiedFraudCauses = CF.get(i).getFraudByRootCauseKeyword();
				 String fraudAmount = util.isNullOrEmpty(CF.get(i).getAttemptedtransaction()) ? "0" : CF.get(i).getAttemptedtransaction();  
				 String classification = "1"; //check with sushil, he told to default ongoing investigation 
				 String fraudType = "2"; 
				 String fraudNature = "3";  
				 String fraudDescription = CF.get(i).getQueryDetails();
				 String accountNumber = CF.get(i).getClientLeid();
				 String branchCode = CF.get(i).getOriginatingCountryName();
				 String actionTaken = "Under Investigation";  // under investigation if open , if it is closed, NA
				 String victimName = CF.get(i).getClientName();
				 String mitigationMeasures = "Enhanced Control Environment"; 
				 String fraudStatus = CF.get(i).getPystatusWork();
				 String occurrenceDate = util.Convert_BOT_Date_Format(CF.get(i).getPxcreateDatetime(), "yyyy-MM-dd HH:mm:ss");
				 String detectionDate = util.Convert_BOT_Date_Format(CF.get(i).getPxcreateDatetime(), "yyyy-MM-dd HH:mm:ss");
				 String concludedInvestigationDate = "<null>";
				 String fspFraudIdentifiedDate = util.Convert_BOT_Date_Format(CF.get(i).getPxcreateDatetime(), "yyyy-MM-dd HH:mm:ss");
				 String empNin = "";
				 String fraudsterName = "NA"; 
				 String fraudsterCountry = "218";
				 
				 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { productProcessInvolved, "GEMS002" }, new Lookup001_mapper());
				 
				 productProcessInvolved = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "8";
		 
				 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";
				 
				 Info = Jdbctemplate.query(sql, new Object[] { identifiedFraudCauses, "GEMS003" }, new Lookup001_mapper());
				 
				 identifiedFraudCauses = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "2";
		 
				 sql = "select * from lookup001 where COLUMN1=? and COLUMN12=?";
				 
				 Info = Jdbctemplate.query(sql, new Object[] { fraudStatus, "GEMS004" }, new Lookup001_mapper());
				 
				 fraudStatus = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "1";
		 
				 int count = i+1;
				 	 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
							"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
							"  COLUMN20, COLUMN21, COLUMN22, COLUMN23) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
																											
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "fraudIncidenceInformation", count,reportingDate, productProcessInvolved, fraudAmount, accountNumber, discoveryModel, identifiedFraudCauses,  classification, fraudType, fraudNature, fraudDescription,  branchCode, actionTaken, victimName, mitigationMeasures, fraudStatus, occurrenceDate, detectionDate, concludedInvestigationDate, fspFraudIdentifiedDate});

				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "employeeInvolvedList", count , reportingDate, productProcessInvolved, fraudAmount, accountNumber, empNin}); 
					
				
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "fraudsterList", count , reportingDate, productProcessInvolved, fraudAmount, accountNumber, fraudsterName,fraudsterCountry}); 
	
			 }	
			 
			 if(CF.size() > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
							"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
							"  COLUMN20, COLUMN21, COLUMN22, COLUMN23) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "fraudIncidenceInformation", "serial","reportingDate", "productProcessInvolved", "fraudAmount", "accountNumber", "discoveryModel", "identifiedFraudCauses", "classification", "fraudType", "fraudNature", "fraudDescription",  "branchCode", "actionTaken", "victimName", "mitigationMeasures", "fraudStatus", "occurrenceDate", "detectionDate", "concludedInvestigationDate", "fspFraudIdentifiedDate"});

				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "employeeInvolvedList", "serial" , "reportingDate", "productProcessInvolved", "fraudAmount", "accountNumber", "empNin" }); 
			
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "fraudsterList", "serial" , "reportingDate", "productProcessInvolved", "fraudAmount", "accountNumber", "fraudsterName", "fraudsterCountry" }); 
			
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "fraudIncidenceInformation", "1"});

				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "employeeInvolvedList", "2"});

				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "fraudsterList", "3"});

				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "fraudIncidenceInformation", INFO1, CF.size(), "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "GEMS" });	 
					
				 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 }
			 
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
			 
			 logger.debug("Exception in fraudstatistics :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject companyDetails(String INFO1, String INFO2, String INFO3) //RTS119
	{ 
		JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 final String Query = "{call PACK_CC.COMPANY_INFORMATION(?,?,?,?,?)}";  
			 
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
			
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, REQDATE, "companyDetails", INFO1, "LEID", INFO3, "1", util.get_oracle_Timestamp() });	 
		     		
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
			
			 logger.debug("Exception in companyDetails:::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject trustDetails(String INFO1, String INFO2, String INFO3) //RTS177
	{ 
		JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 final String Query = "{call PACK_CC.TRUST_DETAILS(?,?,?,?,?)}";  
			 
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
			
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, REQDATE, "trustDetails", INFO1, "LEID", INFO3, "1", util.get_oracle_Timestamp() });	 
		     		
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
			
			 logger.debug("Exception in trustDetails :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject nonProfitOrgInformation(String INFO1, String INFO2, String INFO3) //RTS179
	{ 
		JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 final String Query = "{call PACK_CC.NONPROFIT_ORG_INFORMATION(?,?,?,?,?)}";  
			 
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
			
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, REQDATE, "nonProfitOrgInformation", INFO1, "LEID", INFO3, "1", util.get_oracle_Timestamp() });	 
		     		
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
			
			 logger.debug("Exception in nonProfitOrgInformation :::: "+e.getLocalizedMessage());
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
	
	
	private class COMPLAINTSFRAUDDETAILS_mapper implements RowMapper<COMPLAINTSFRAUDDETAILS> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public COMPLAINTSFRAUDDETAILS mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			COMPLAINTSFRAUDDETAILS COMPLAIN = new COMPLAINTSFRAUDDETAILS();  
	
		
			COMPLAIN.setPyid(util.ReplaceNull(rs.getString("PYID")));
			COMPLAIN.setComplaintType(util.ReplaceNull(rs.getString("COMPLAINTTYPE")));
			COMPLAIN.setPxCreatedDateTime(util.ReplaceNull(rs.getString("PXCREATEDATETIME")));
			COMPLAIN.setClientName(util.ReplaceNull(rs.getString("CLIENTNAME")));
			COMPLAIN.setRedressAmount(util.ReplaceNull(rs.getString("REDRESSAMOUNT")));
			COMPLAIN.setRedressCurrency(util.ReplaceNull(rs.getString("REDRESSCURRENCY")));
			COMPLAIN.setPxUpdateOperator(util.ReplaceNull(rs.getString("PXUPDATEOPERATOR")));
			COMPLAIN.setComplaintStatus(util.ReplaceNull(rs.getString("COMPLAINTSTATUS")));
			COMPLAIN.setFraudByRootCauseKeyword(util.ReplaceNull(rs.getString("FRAUDBYROOTCAUSEKEYWORD")));

			return COMPLAIN;
		}
	
	}
	
	private class fraudIncidenceInformation_mapper implements RowMapper<fraudIncidence_Information> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public fraudIncidence_Information  mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			fraudIncidence_Information fraud = new fraudIncidence_Information();  
	
		
			fraud.setPyid(util.ReplaceNull(rs.getString("PYID")));
			fraud.setClientName(util.ReplaceNull(rs.getString("CLIENTNAME")));
			fraud.setAttemptedtransaction(util.ReplaceNull(rs.getString("ATTEMPTEDTRANSACTION")));
			fraud.setRedressAmount(util.ReplaceNull(rs.getString("REDRESSAMOUNT")));
			fraud.setRedresscurrency(util.ReplaceNull(rs.getString("REDRESSCURRENCY")));
			fraud.setPystatusWork(util.ReplaceNull(rs.getString("STATUSOFATTEMPT")));
			fraud.setPxcreateDatetime(util.ReplaceNull(rs.getString("PXCREATEDATETIME")));
			fraud.setDateofattemp(util.ReplaceNull(rs.getString("DATEOFATTEMPT")));
			fraud.setProductName(util.ReplaceNull(rs.getString("PRODUCTNAME")));
			fraud.setClassification(util.ReplaceNull(rs.getString("CLASSIFICATION")));
			fraud.setQueryDetails(util.ReplaceNull(rs.getString("QUERYDETAILS")));
			fraud.setClientLeid(util.ReplaceNull(rs.getString("CLIENTLEID")));
			fraud.setOriginatingCountryName(util.ReplaceNull(rs.getString("ORIGINATINGCOUNTRYNAME")));
			fraud.setFraudByRootCauseKeyword(util.ReplaceNull(rs.getString("FRAUDBYROOTCAUSEKEYWORD")));

			return fraud;
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
