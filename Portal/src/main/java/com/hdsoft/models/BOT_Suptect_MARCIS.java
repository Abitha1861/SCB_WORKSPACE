package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.FileReader;
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
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Token_System;
import com.hdsoft.solace.TopicPublisher;
import com.hdsoft.Repositories.Event_Creation;
import com.hdsoft.Repositories.Lookup001;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_MARCIS 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_MARCIS(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	@Autowired
	Token_System tk = new Token_System() ;
	
	public BOT_Suptect_MARCIS() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_MARCIS.class);
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(20); // Adjust pool size as needed
	
	@RequestMapping(value = {"/Datavision/marcis/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestHeader("APICODE") String APICODE, @RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
 	 	JsonObject details = new JsonObject();
		  
		try
		{
			 if(APICODE.equals("RTS019")) 
			 {
				 details = LoanInformation("RTS019", "", Message); 
			 }
			 else if(APICODE.equals("RTS191")) 
			 {
				 details = LoanTransaction("RTS191", "", Message); //RTS191
			 }
			 else if(APICODE.equals("RTS103")) 
			 {
				 details = undrawnBalanceData("RTS103", "", Message); //RTS103
			 }
			 else if(APICODE.equals("RTS163")) 
			 {
				 details = WrittenOffLoans("RTS163", "", Message); //RTS163
			 }
			 else if(APICODE.equals("RTS023")) 
			 {
				 details = Overdraft("RTS023", "", Message); //RTS023
			 }
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	public JsonObject Marcis_File_Processing(String INFO1, String INFO2, String INFO3) 
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 BufferedReader br = new BufferedReader(new FileReader(INFO3));
	         
			 String line = "";  int i = 0;
			 
			 while ((line = br.readLine()) != null) 
             {
				 String stsEndpoint = line; 
				 
				 if(INFO1.equals("RTS019")) 
				 {
					 executorService.submit(() -> LoanInformation(INFO1, INFO2, stsEndpoint)); 
				 }
				 else if(INFO1.equals("RTS191")) 
				 {
					 executorService.submit(() -> LoanTransaction(INFO1, INFO2, stsEndpoint)); 
				 }
				 else if(INFO1.equals("RTS163")) 
				 {
					 executorService.submit(() -> WrittenOffLoans(INFO1, INFO2, stsEndpoint)); 
				 }
				 else if(INFO1.equals("RTS103")) 
				 {
					 executorService.submit(() -> undrawnBalanceData(INFO1, INFO2, stsEndpoint)); 
				 }
				 else if(INFO1.equals("RTS023")) 
				 {
					 executorService.submit(() -> Overdraft(INFO1, INFO2, stsEndpoint)); 
				 }
				 		 
				 i++;
				 
				 logger.debug(INFO3+" Record "+i+" is processed");
             }
			 
			 if(i==0)  
			 {
				 logger.debug(INFO3+" file is having 0 Record");
			 }
			 
			 br.close();			
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in Marcis_File_Processing :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
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
	
	public JsonObject LoanInformation(String INFO1, String INFO2, String INFO3) //RTS019
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 		
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonObject customerDetails = request.get("customerDetails").getAsJsonObject();
			 
			 JsonObject loanDetails = request.get("loanDetails").getAsJsonObject();
			 
			 JsonArray Collateral_Details = loanDetails.get("Collateral Details").getAsJsonArray();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = customerDetails.get("customerIdentificationNumber").getAsString();
			 String accountNumber = customerDetails.get("accountNumber").getAsString();
			 String clientName = customerDetails.get("clientName").getAsString();
			 String borrowerCountry = customerDetails.get("borrowerCountry").getAsString();
			 String ratingStatus = "false";  //customerDetails.get("ratingStatus").getAsString();
			 String crRatingBorrower = "";  //customerDetails.get("RatingBorrower").getAsString();
			 String gradesUnratedBanks = "1"; //customerDetails.get("RatingBorrower").getAsString();
			 String categoryBorrower = customerDetails.get("borrowerCategory").getAsString();
			 String gender = customerDetails.get("gender").getAsString();
			 String disability = customerDetails.get("disability").getAsString();
			 String clientType = customerDetails.get("clientType").getAsString();
			 String clientSubType = customerDetails.get("clientSubType").getAsString();
			 String groupName = customerDetails.get("groupName").getAsString();
			 String groupCode = customerDetails.get("groupCode").getAsString();
			 String relatedParty = "10"; 
			 String relationshipCategory = "1";
			 String loanNumber = loanDetails.get("loanNumber").getAsString();
			 String loanType = loanDetails.get("loanType").getAsString();
			 String loanEconomicActivity = loanDetails.get("loanEconomicActivity").getAsString();
			 String loanPhase = loanDetails.get("loanPhase").getAsString();
			 String transferStatus = "2";  
			 String purposeMortgage = loanDetails.get("purposeMortgage").getAsString();
			 String purposeOtherLoans = loanDetails.get("purposeOtherLoans").getAsString();
			 String sourceFundMortgage = loanDetails.get("sourceFundMortgage").getAsString();
			 String amortizationType = loanDetails.get("amortizationType").getAsString();
			 String branchCode = "008300"; //loanDetails.get("branchCode").getAsString();
			 String loanOfficer = loanDetails.get("loanOfficer").getAsString();
			 String loanSupervisor = loanDetails.get("loanSupervisor").getAsString();
			 String groupVillageNumber = customerDetails.get("groupVillageNumber").getAsString();
			 String cycleNumber = customerDetails.get("cycleNumber").getAsString();
			 String loanInstallment = loanDetails.get("loanInstallment").getAsString();
			 String repaymentFrequency = loanDetails.get("repaymentFrequency").getAsString();
			 String currency = loanDetails.get("currency").getAsString();
			 String contractDate = loanDetails.get("contractDate").getAsString();
			 String orgSanctionAmount = loanDetails.get("orgSanctionAmount").getAsString();
			 String usdSanctionAmount = loanDetails.get("usdSanctionAmount").getAsString();
			 String tzsSanctionAmount = loanDetails.get("tzsSanctionAmount").getAsString();
			 String orgDisbursedAmount = loanDetails.get("orgDisbursedAmount").getAsString();
			 String usdDisbursedAmount = loanDetails.get("usdDisbursedAmount").getAsString();
			 String tzsDisbursedAmount = loanDetails.get("tzsDisbursedAmount").getAsString();
			 String disbursementDate = loanDetails.get("disbursementDate").getAsString();
			 String maturityDate = loanDetails.get("maturityDate").getAsString();
			 String realEndDate = "<null>";  //loanDetails.get("realEndDate").getAsString();
			 String orgOutstandingPrincipalAmount = loanDetails.get("orgOutstandingPrincipalAmount").getAsString();
			 String usdOutstandingPrincipalAmount = loanDetails.get("usdOutstandingPrincipalAmount").getAsString();
			 String tzsOutstandingPrincipalAmount = loanDetails.get("tzsOutstandingPrincipalAmount").getAsString();
			 String orgInstallmentAmount = loanDetails.get("orgInstallmentAmount").getAsString();
			 String usdInstallmentAmount = loanDetails.get("usdInstallmentAmount").getAsString();
			 String tzsInstallmentAmount = loanDetails.get("tzsInstallmentAmount").getAsString();
			 String loanInstallmentPaid = loanDetails.get("loanInstallmentPaid").getAsString();
			 String gracePeriodPaymentPrincipal = loanDetails.get("gracePeriodPaymentPrincipal").getAsString();
			 String primeLendingRate = loanDetails.get("primeLendingRate").getAsString();
			 String interestPricingMethod = loanDetails.get("interestPricingMethod").getAsString();
			 String annualInterestRate = loanDetails.get("annualInterestRate").getAsString();
			 String annualEffectiveInterestRate = loanDetails.get("effectiveAnnualInterestRate").getAsString();
			 String firstInstallmentPaymentDate = loanDetails.get("firstInstallmentPaymentDate").getAsString();
			 String lastPaymentDate = loanDetails.get("lastPaymentDate").getAsString();		 
			 String loanFlagType = loanDetails.get("loanFlagType").getAsString();
			 String restructuringDate = loanDetails.get("restructuringDate").getAsString();
			 String pastDueDays = loanDetails.get("pastDueDays").getAsString();
			 String pastDueAmount = loanDetails.get("pastDueAmount").getAsString();
			 String internalRiskGroup = loanDetails.get("internalRiskGroup").getAsString();
			 String orgAccruedInterestAmount = loanDetails.get("orgAccruedInterestAmount").getAsString();
			 String usdAccruedInterestAmount = loanDetails.get("usdAccruedInterestAmount").getAsString();
			 String tzsAccruedInterestAmount = loanDetails.get("tzsAccruedInterestAmount").getAsString();
			 String orgPenaltyChargedAmount = loanDetails.get("orgPenaltyChargedAmount").getAsString();
			 String usdPenaltyChargedAmount = loanDetails.get("usdPenaltyChargedAmount").getAsString();
			 String tzsPenaltyChargedAmount = loanDetails.get("tzsPenaltyChargedAmount").getAsString();
			 String orgPenaltyPaidAmount = loanDetails.get("orgPenaltyPaidAmount").getAsString();
			 String usdPenaltyPaidAmount = loanDetails.get("usdPenaltyPaidAmount").getAsString();
			 String tzsPenaltyPaidAmount = loanDetails.get("tzsPenaltyPaidAmount").getAsString(); 
			 String orgLoanFeesChargedAmount = loanDetails.get("orgLoanFeesChargedAmount").getAsString();
			 String usdLoanFeesChargedAmount = loanDetails.get("usdLoanFeesChargedAmount").getAsString();
			 String tzsLoanFeesChargedAmount = loanDetails.get("tzsLoanFeesChargedAmount").getAsString();
			 String orgLoanFeesPaidAmount = loanDetails.get("orgLoanFeesPaidAmount").getAsString();
			 String usdLoanFeesPaidAmount = loanDetails.get("usdLoanFeesPaidAmount").getAsString();
			 String tzsLoanFeesPaidAmount = loanDetails.get("tzsLoanFeesPaidAmount").getAsString();
			 String orgTotMonthlyPaymentAmount = loanDetails.get("orgTotMonthlyPaymentAmount").getAsString();
			 String usdTotMonthlyPaymentAmount = loanDetails.get("usdTotMonthlyPaymentAmount").getAsString();
			 String tzsTotMonthlyPaymentAmount = loanDetails.get("tzsTotMonthlyPaymentAmount").getAsString();
			 String sectorSnaClassification = loanDetails.get("sectorSnaClassification").getAsString();
			 String assetClassificationCategory = loanDetails.get("assetClassificationCategory").getAsString();
			 String negStatusContract = loanDetails.get("negStatusContract").getAsString(); 
			 String customerRole = loanDetails.get("customerRole").getAsString();
			 String allowanceProbableLoss = loanDetails.get("allowanceProbableLoss").getAsString();
			 String botProvision = loanDetails.get("botProvision").getAsString();
			 String tradingIntent = "2"; 
			 String orgSuspendedInterest = loanDetails.get("orgSuspendedInterest").getAsString();
			 String usdSuspendedInterest = loanDetails.get("usdSuspendedInterest").getAsString();
			 String tzsSuspendedInterest = loanDetails.get("tzsSuspendedInterest").getAsString();
			 
			 lastPaymentDate = util.isNullOrEmpty(lastPaymentDate) ? "<null>" : lastPaymentDate; // conditional mandatory
			 
			 loanFlagType = loanFlagType.equalsIgnoreCase("N") ? "2" : "1";
			 
			 negStatusContract = negStatusContract.equalsIgnoreCase("N") ? "1" : "4";
			 
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC001", borrowerCountry }, new Lookup001_mapper());
				
			 borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "218" : "218";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC002", categoryBorrower }, new Lookup001_mapper());
				
			 categoryBorrower = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "2" : "2";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC003", gender }, new Lookup001_mapper());
				
			 gender = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "3" : "3";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC004", clientType }, new Lookup001_mapper());
				
			 clientType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "3" : "3";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC005", clientSubType }, new Lookup001_mapper());
				
			 clientSubType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "" : "";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC006", loanType }, new Lookup001_mapper());
				
			 loanType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC007", loanPhase }, new Lookup001_mapper());
				
			 loanPhase = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC008", purposeMortgage }, new Lookup001_mapper());
				
			 purposeMortgage = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "10" : "10";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC009", purposeOtherLoans }, new Lookup001_mapper());
				
			 purposeOtherLoans = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "10" : "10";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC010", amortizationType }, new Lookup001_mapper());
				
			 amortizationType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "3" : "3";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC011", branchCode }, new Lookup001_mapper());
				
			 branchCode = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "8600" : "8600";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC012", repaymentFrequency }, new Lookup001_mapper());
				
			 repaymentFrequency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "4" : "4";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARCUR", currency }, new Lookup001_mapper());
				
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "834" : "834";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC014", sectorSnaClassification }, new Lookup001_mapper());
				
			 sectorSnaClassification = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "15" : "15";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";   // Refer common attribute
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC015", assetClassificationCategory }, new Lookup001_mapper());
				
			 assetClassificationCategory = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";   // Refer common attribute
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC016", customerRole }, new Lookup001_mapper());
				
			 customerRole = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";   
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC019", interestPricingMethod }, new Lookup001_mapper());
				
			 interestPricingMethod = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";   
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC021", loanEconomicActivity }, new Lookup001_mapper());
				
			 loanEconomicActivity = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "1" : "1";
			 
			 allowanceProbableLoss = util.isNullOrEmpty(allowanceProbableLoss) ? "0.00" : allowanceProbableLoss;
			 
			 restructuringDate = util.isNullOrEmpty(restructuringDate) ? "<null>" : restructuringDate;
			 
			 List<String> validation = new ArrayList<String>();
			 
			 if(!util.isAlphanumeric(customerIdentificationNumber)) validation.add("customerIdentificationNumber should be alphanumeric");
			 
			 if(validation.size() == 0)
			 {
				 int count = 0;
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 count++;
				 
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
					 
				 for(int i=0; i<Collateral_Details.size(); i++)
				 {
					JsonObject js= Collateral_Details.get(i).getAsJsonObject();
					
					 String collateralPledged = js.get("collateralPledged").getAsString();
					 String orgCollateralValue = js.get("orgCollateralValue").getAsString();
					 String usdCollateralValue = js.get("usdCollateralValue").getAsString();
					 String tzsCollateralValue = js.get("tzsCollateralValue").getAsString();
					 
					 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC013", collateralPledged }, new Lookup001_mapper());
						
					 collateralPledged = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "18" : "18";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
						 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
						 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", i+1 , reportingDate, customerIdentificationNumber,  accountNumber, 
						 		collateralPledged, orgCollateralValue, usdCollateralValue, tzsCollateralValue }); 
					 
				 }
					 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanInformation", "1"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged", "2"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "MARCIS" });	 
			     
			     StoreLoanLog(SUBORGCODE, INFO1, "MARCIS", loanNumber, Batch_id, O_SERIAL);
			     
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
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject LoanTransaction(String INFO1, String INFO2, String INFO3) //RTS191
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonArray transactionDetails = request.get("transactionDetails").getAsJsonArray();
			
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 int count = 0;
			 
			 for(int i=0; i<transactionDetails.size(); i++)
			 {
				 JsonObject js = transactionDetails.get(i).getAsJsonObject();
				 
				 count++;
				 
				 String reportingDate = util.getCurrentReportDate();
				 String loanNumber = js.get("loanNumber").getAsString();
				 String transactionDate = js.get("transactionDate").getAsString();
				 String currency = js.get("currency").getAsString();
				 String loanTransactionType = js.get("loanTransactionType").getAsString();
				 String loanTransactionSubType = js.get("loanTransactionSubType").getAsString();
				 String orgTransactionAmount = js.get("orgtransactionAmount").getAsString();
				 String usdTransactionAmount = js.get("usdtransactionAmount").getAsString();
				 String tzsTransactionAmount = js.get("tzstransactionAmount").getAsString();
				 
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARCUR", currency }, new Lookup001_mapper());
					
				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "834" : "834";
				 
				 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC017", loanTransactionType }, new Lookup001_mapper());
					
				 loanTransactionType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "1" : "1";
				 
				 loanTransactionSubType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN6()) ? Info.get(0).getCOLUMN6() : "" : "";
				  	 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanTransactionInformation", count, reportingDate, loanNumber, transactionDate, loanTransactionType, loanTransactionSubType,
						currency, orgTransactionAmount,  usdTransactionAmount, tzsTransactionAmount });
				 
			 }
			 
			 if(count > 0)
			 {
				 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanTransactionInformation", "serial", "reportingDate", "loanNumber", "transactionDate", "loanTransactionType", "loanTransactionSubType",
							"currency", "orgTransactionAmount",  "usdTransactionAmount", "tzsTransactionAmount" });
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanTransactionInformation"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanTransactionInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "MARCIS" });	 		 		 
				
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
			 
			 logger.debug("Exception in Premises_Furniture_and_Equipment :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject WrittenOffLoans(String INFO1, String INFO2, String INFO3) //RTS163  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonObject customerDetails = request.get("customerDetails").getAsJsonObject();
			 
			 JsonObject loanDetails = request.get("loanDetails").getAsJsonObject();
			 
			 JsonArray Collateral_Details = request.get("Collateral Details").getAsJsonArray();
			  
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 int count = 0;
				 
			 count++;
		 
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = getMember(customerDetails, "customerIdentificationNumber"); 
			 String accountNumber = getMember(customerDetails, "accountNumber"); 
			 String borrowerName = getMember(customerDetails, "borrowerName"); 
			 String borrowerCountry = getMember(customerDetails, "borrowerCountry"); 
			 String clientType = getMember(customerDetails, "clientType"); 
			 String loanNumber = getMember(loanDetails, "loanNumber"); 
			 String loanType = getMember(loanDetails, "loanType"); 
			 String currency = getMember(loanDetails, "currency"); 
			 String orgDisbursedAmount = getMember(loanDetails, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(loanDetails, "usdDisbursedAmount"); 
			 String tzsDisbursedAmount = getMember(loanDetails, "tzsDisbursedAmount"); 
			 String disbursementDate = getMember(loanDetails, "disbursementDate"); 
			 String gracePeriodPaymentPrincipal = getMember(loanDetails, "gracePeriodPaymentPrincipal"); 
			 String maturityDate = getMember(loanDetails, "maturityDate"); 
			 String orgGrossPaidAmount = getMember(loanDetails, "orgGrossPaidAmount"); 
			 String usdGrossPaidAmount = getMember(loanDetails, "usdGrossPaidAmount"); 
			 String tzsGrossPaidAmount = getMember(loanDetails, "tzsGrossPaidAmount"); 
			 String writtenOffDate = getMember(loanDetails, "writtenOffDate"); 
			 String orgOutstandingPrincipalAmount = getMember(loanDetails, "orgOutstandingPrincipalAmount"); 
			 String usdOutstandingPrincipalAmount = getMember(loanDetails, "usdOutstandingPrincipalAmount"); 
			 String tzsOutstandingPrincipalAmount = getMember(loanDetails, "tzsOutstandingPrincipalAmount"); 
			 String annualInterestRate = getMember(loanDetails, "annualInterestRate"); 
			 String latestInstallmentPayDate = getMember(loanDetails, "latestInstallmentPayDate");  
			 String pastDueDays = getMember(loanDetails, "pastDueDays");
			 String loanOfficer = getMember(loanDetails, "loanOfficer"); 
			 String loanSupervisor = getMember(loanDetails, "loanSupervisor"); 
			 
			 writtenOffDate = !util.isNullOrEmpty(writtenOffDate) ? writtenOffDate : reportingDate;
			 
			 loanOfficer = !util.isNullOrEmpty(loanOfficer) ? loanOfficer : "NA";
			 
			 latestInstallmentPayDate = !util.isNullOrEmpty(latestInstallmentPayDate) ? latestInstallmentPayDate : "<null>";
			 
			 String collateralPledged = "", orgCollateralValue = "", usdCollateralValue = "", tzsCollateralValue = "";
			 
			 for(int i=0; i<Collateral_Details.size(); i++)
			 {
				JsonObject js= Collateral_Details.get(i).getAsJsonObject();
				
				 collateralPledged = js.get("collateralPledged").getAsString();
				 orgCollateralValue = js.get("orgCollateralValue").getAsString();
				 usdCollateralValue = js.get("usdCollateralValue").getAsString();
				 tzsCollateralValue = js.get("tzsCollateralValue").getAsString();
				 
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC013", collateralPledged }, new Lookup001_mapper());
					
				 collateralPledged = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "18" : "18";				 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC001", borrowerCountry }, new Lookup001_mapper());
				
			 borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "218" : "218";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC004", clientType }, new Lookup001_mapper());
				
			 clientType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "3" : "3";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC006", loanType }, new Lookup001_mapper());
				
			 loanType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARCUR", currency }, new Lookup001_mapper());
				
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "834" : "834";
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13,"
			 		+ "COLUMN14, COLUMN15, COLUMN16,COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27,"
			 		+ "COLUMN28, COLUMN29, COLUMN30,COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "writtenOffLoans", count, reportingDate, customerIdentificationNumber, accountNumber, borrowerName, borrowerCountry,
					 clientType, loanNumber,  loanType, currency,orgDisbursedAmount,usdDisbursedAmount,tzsDisbursedAmount,disbursementDate,gracePeriodPaymentPrincipal,
					 maturityDate,orgGrossPaidAmount,usdGrossPaidAmount,tzsGrossPaidAmount,writtenOffDate,orgOutstandingPrincipalAmount,usdOutstandingPrincipalAmount,
					 tzsOutstandingPrincipalAmount,annualInterestRate,latestInstallmentPayDate,collateralPledged,orgCollateralValue,usdCollateralValue, tzsCollateralValue, pastDueDays,
					 loanOfficer,loanSupervisor });
				   			
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13,"
				 		+ "COLUMN14, COLUMN15, COLUMN16,COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27,"
				 		+ "COLUMN28, COLUMN29, COLUMN30,COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "writtenOffLoans", "serial", "reportingDate", "customerIdentificationNumber", "accountNumber", "borrowerName", "borrowerCountry",
					 "clientType", "loanNumber",  "loanType", "currency","orgDisbursedAmount","usdDisbursedAmount","tzsDisbursedAmount","disbursementDate","gracePeriodPaymentPrincipal",
					 "maturityDate","orgGrossPaidAmount","usdGrossPaidAmount","tzsGrossPaidAmount","writtenOffDate","orgOutstandingPrincipalAmount","usdOutstandingPrincipalAmount",
					 "tzsOutstandingPrincipalAmount","annualInterestRate","latestInstallmentPayDate","collateralPledged","orgCollateralValue", "usdCollateralValue", "tzsCollateralValue","pastDueDays",
					 "loanOfficer","loanSupervisor"});
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "writtenOffLoans"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Bank Data", "writtenOffLoans", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "MARCIS" });	 		 		 
			
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
			 
			logger.debug("Exception in WrittenOffLoans :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject undrawnBalanceData(String INFO1, String INFO2, String INFO3) //RTS103  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonObject customerDetails = request.get("customerDetails").getAsJsonObject();
			 
			 JsonObject loanDetails = request.get("loanDetails").getAsJsonObject();
			 
			 JsonArray Collateral_Details = loanDetails.get("Collateral Details").getAsJsonArray();
			  
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 int count = 0;
				 
			 count++;
			
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = getMember(customerDetails, "customerIdentificationNumber");    		
			 String borrowerName = getMember(customerDetails, "borrowerName");
			 String relationshipType = "16";  //As per mppaing doc def it 16
			 String contractDate = getMember(loanDetails, "contractDate");
			 String categoryUndrawnBalance = "3"; //As per mppaing doc def it 3
			 String ratingStatus = "false";  
			 String crRatingCounterCustomer = "";
			 String gradesUnratedCustomer = "1";
			 String currency = getMember(loanDetails, "currency");
			 String orgSanctionedAmount = getMember(loanDetails, "orgSanctionedAmount");
			 String usdSanctionedAmount = getMember(loanDetails, "usdSanctionedAmount");
			 String tzsSanctionedAmount = getMember(loanDetails, "tzsSanctionedAmount");
			 String orgDisbursedAmount = getMember(loanDetails, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(loanDetails, "usdDisbursedAmount");
			 String tzsDisbursedAmount = getMember(loanDetails, "tzsDisbursedAmount");
			 String orgUnutilisedAmount = getMember(loanDetails, "orgUnutilisedAmount");
			 String usdUnutilisedAmount = getMember(loanDetails, "usdUnutilisedAmount");
			 String tzsUnutilisedAmount = getMember(loanDetails, "tzsUnutilisedAmount");
			 String pastDueDays = "0";   
			 String allowanceProbableLoss = "0";  
			 String botProvision = "0"; 
			  
			 String collateralType = "";
			 
			 for(int i=0; i<Collateral_Details.size(); i++)
			 {
				 JsonObject js= Collateral_Details.get(i).getAsJsonObject();
				
				 collateralType = js.get("collateralPledged").getAsString();
				
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC013", collateralType }, new Lookup001_mapper());
					
				 collateralType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "18" : "18";				 
			 }
			 
			 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARCUR", currency }, new Lookup001_mapper());
			 
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN4()) ? Info.get(0).getCOLUMN4() : "834" : "834";
			 
			 //sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
			 
			 //Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC018", crRatingCounterCustomer }, new Lookup001_mapper());
				
			 //crRatingCounterCustomer = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "1" : "1";
				
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13,"
			 		+ "COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" +
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "undrawnBalanceData", count,  reportingDate,
					 customerIdentificationNumber, borrowerName, relationshipType, contractDate, categoryUndrawnBalance, ratingStatus, 
					 crRatingCounterCustomer, gradesUnratedCustomer, currency, orgSanctionedAmount, usdSanctionedAmount,
					 tzsSanctionedAmount, orgDisbursedAmount, usdDisbursedAmount, tzsDisbursedAmount, orgUnutilisedAmount,
					 usdUnutilisedAmount, tzsUnutilisedAmount, collateralType, pastDueDays, allowanceProbableLoss, botProvision });
				
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13,"
		  		+ "COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" +
				   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "undrawnBalanceData", "serial", "reportingDate", "customerIdentificationNumber", "borrowerName",
				 "relationshipType", "contractDate", "categoryUndrawnBalance", "ratingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency",
				 "orgSanctionedAmount", "usdSanctionedAmount", "tzsSanctionedAmount", "orgDisbursedAmount", "usdDisbursedAmount", "tzsDisbursedAmount",
				 "orgUnutilisedAmount", "usdUnutilisedAmount", "tzsUnutilisedAmount", "collateralType", "pastDueDays", "allowanceProbableLoss", "botProvision"});
		
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "undrawnBalanceData"});
			
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OBS", "undrawnBalanceData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "MARCIS" });	 		 		
			
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
			
			 logger.debug("Exception in undrawnBalanceData:::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Overdraft(String INFO1, String INFO2, String INFO3) //RTS023  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 //JsonObject data_ = util.StringToJsonObject(INFO3);
			 
			 final String Query = "{call PACK_EBBS.OVERDRAFT(?,?,?,?,?)}";  
			 
			 Map<String, Object> resultMap = Jdbctemplate.call(new CallableStatementCreator() {
	 				
					public CallableStatement createCallableStatement(Connection connection) throws SQLException {
 
						CallableStatement CS = connection.prepareCall(Query);
						
						CS.setString(1, INFO1);
						CS.setString(2, "MARCIS");
						CS.setString(3, INFO3);
						CS.registerOutParameter(4, Types.INTEGER);
						CS.registerOutParameter(5, Types.VARCHAR);
						
						return CS;
				}
 			 }, get_ProcedureParams_Overdraft());
			 
 			  String REPORTSL = util.ReplaceNull(resultMap.get("O_SERIAL"));
 			  String MESSAGE = util.ReplaceNull(resultMap.get("O_ERRMSG"));
					
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
			
			 logger.debug("Exception in undrawnBalanceData:::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public List<String> LoanInformation_validation(String INFO1, String INFO2, String INFO3) //RTS019
	{ 
		 List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
				
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonObject customerDetails = request.get("customerDetails").getAsJsonObject();
			 
			 JsonObject loanDetails = request.get("loanDetails").getAsJsonObject();
			 
			 JsonArray Collateral_Details = loanDetails.get("Collateral Details").getAsJsonArray();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = customerDetails.get("customerIdentificationNumber").getAsString();
			 String accountNumber = customerDetails.get("accountNumber").getAsString();
			 String clientName = customerDetails.get("clientName").getAsString();
			 String borrowerCountry = customerDetails.get("borrowerCountry").getAsString();
			 String ratingStatus = "false";  //customerDetails.get("ratingStatus").getAsString();
			 String crRatingBorrower = "";  //customerDetails.get("RatingBorrower").getAsString();
			 String gradesUnratedBanks = "1"; //customerDetails.get("RatingBorrower").getAsString();
			 String categoryBorrower = customerDetails.get("borrowerCategory").getAsString();
			 String gender = customerDetails.get("gender").getAsString();
			 String disability = customerDetails.get("disability").getAsString();
			 String clientType = customerDetails.get("clientType").getAsString();
			 String clientSubType = customerDetails.get("clientSubType").getAsString();
			 String groupName = customerDetails.get("groupName").getAsString();
			 String groupCode = customerDetails.get("groupCode").getAsString();
			 String relatedParty = "10"; 
			 String relationshipCategory = "1";
			 String loanNumber = loanDetails.get("loanNumber").getAsString();
			 String loanType = loanDetails.get("loanType").getAsString();
			 String loanEconomicActivity = loanDetails.get("loanEconomicActivity").getAsString();
			 String loanPhase = loanDetails.get("loanPhase").getAsString();
			 String transferStatus = "2";  
			 String purposeMortgage = loanDetails.get("purposeMortgage").getAsString();
			 String purposeOtherLoans = loanDetails.get("purposeOtherLoans").getAsString();
			 String sourceFundMortgage = loanDetails.get("sourceFundMortgage").getAsString();
			 String amortizationType = loanDetails.get("amortizationType").getAsString();
			 String branchCode = "008300"; //loanDetails.get("branchCode").getAsString();
			 String loanOfficer = loanDetails.get("loanOfficer").getAsString();
			 String loanSupervisor = loanDetails.get("loanSupervisor").getAsString();
			 String groupVillageNumber = customerDetails.get("groupVillageNumber").getAsString();
			 String cycleNumber = customerDetails.get("cycleNumber").getAsString();
			 String loanInstallment = loanDetails.get("loanInstallment").getAsString();
			 String repaymentFrequency = loanDetails.get("repaymentFrequency").getAsString();
			 String currency = loanDetails.get("currency").getAsString();
			 String contractDate = loanDetails.get("contractDate").getAsString();
			 String orgSanctionAmount = loanDetails.get("orgSanctionAmount").getAsString();
			 String usdSanctionAmount = loanDetails.get("usdSanctionAmount").getAsString();
			 String tzsSanctionAmount = loanDetails.get("tzsSanctionAmount").getAsString();
			 String orgDisbursedAmount = loanDetails.get("orgDisbursedAmount").getAsString();
			 String usdDisbursedAmount = loanDetails.get("usdDisbursedAmount").getAsString();
			 String tzsDisbursedAmount = loanDetails.get("tzsDisbursedAmount").getAsString();
			 String disbursementDate = loanDetails.get("disbursementDate").getAsString();
			 String maturityDate = loanDetails.get("maturityDate").getAsString();
			 String realEndDate = "<null>";  //loanDetails.get("realEndDate").getAsString();
			 String orgOutstandingPrincipalAmount = loanDetails.get("orgOutstandingPrincipalAmount").getAsString();
			 String usdOutstandingPrincipalAmount = loanDetails.get("usdOutstandingPrincipalAmount").getAsString();
			 String tzsOutstandingPrincipalAmount = loanDetails.get("tzsOutstandingPrincipalAmount").getAsString();
			 String orgInstallmentAmount = loanDetails.get("orgInstallmentAmount").getAsString();
			 String usdInstallmentAmount = loanDetails.get("usdInstallmentAmount").getAsString();
			 String tzsInstallmentAmount = loanDetails.get("tzsInstallmentAmount").getAsString();
			 String loanInstallmentPaid = loanDetails.get("loanInstallmentPaid").getAsString();
			 String gracePeriodPaymentPrincipal = loanDetails.get("gracePeriodPaymentPrincipal").getAsString();
			 String primeLendingRate = loanDetails.get("primeLendingRate").getAsString();
			 String interestPricingMethod = loanDetails.get("interestPricingMethod").getAsString();
			 String annualInterestRate = loanDetails.get("annualInterestRate").getAsString();
			 String annualEffectiveInterestRate = loanDetails.get("effectiveAnnualInterestRate").getAsString();
			 String firstInstallmentPaymentDate = loanDetails.get("firstInstallmentPaymentDate").getAsString();
			 String lastPaymentDate = loanDetails.get("lastPaymentDate").getAsString();		 
			 String loanFlagType = loanDetails.get("loanFlagType").getAsString();
			 String restructuringDate = loanDetails.get("restructuringDate").getAsString();
			 String pastDueDays = loanDetails.get("pastDueDays").getAsString();
			 String pastDueAmount = loanDetails.get("pastDueAmount").getAsString();
			 String internalRiskGroup = loanDetails.get("internalRiskGroup").getAsString();
			 String orgAccruedInterestAmount = loanDetails.get("orgAccruedInterestAmount").getAsString();
			 String usdAccruedInterestAmount = loanDetails.get("usdAccruedInterestAmount").getAsString();
			 String tzsAccruedInterestAmount = loanDetails.get("tzsAccruedInterestAmount").getAsString();
			 String orgPenaltyChargedAmount = loanDetails.get("orgPenaltyChargedAmount").getAsString();
			 String usdPenaltyChargedAmount = loanDetails.get("usdPenaltyChargedAmount").getAsString();
			 String tzsPenaltyChargedAmount = loanDetails.get("tzsPenaltyChargedAmount").getAsString();
			 String orgPenaltyPaidAmount = loanDetails.get("orgPenaltyPaidAmount").getAsString();
			 String usdPenaltyPaidAmount = loanDetails.get("usdPenaltyPaidAmount").getAsString();
			 String tzsPenaltyPaidAmount = loanDetails.get("tzsPenaltyPaidAmount").getAsString(); 
			 String orgLoanFeesChargedAmount = loanDetails.get("orgLoanFeesChargedAmount").getAsString();
			 String usdLoanFeesChargedAmount = loanDetails.get("usdLoanFeesChargedAmount").getAsString();
			 String tzsLoanFeesChargedAmount = loanDetails.get("tzsLoanFeesChargedAmount").getAsString();
			 String orgLoanFeesPaidAmount = loanDetails.get("orgLoanFeesPaidAmount").getAsString();
			 String usdLoanFeesPaidAmount = loanDetails.get("usdLoanFeesPaidAmount").getAsString();
			 String tzsLoanFeesPaidAmount = loanDetails.get("tzsLoanFeesPaidAmount").getAsString();
			 String orgTotMonthlyPaymentAmount = loanDetails.get("orgTotMonthlyPaymentAmount").getAsString();
			 String usdTotMonthlyPaymentAmount = loanDetails.get("usdTotMonthlyPaymentAmount").getAsString();
			 String tzsTotMonthlyPaymentAmount = loanDetails.get("tzsTotMonthlyPaymentAmount").getAsString();
			 String sectorSnaClassification = loanDetails.get("sectorSnaClassification").getAsString();
			 String assetClassificationCategory = loanDetails.get("assetClassificationCategory").getAsString();
			 String negStatusContract = loanDetails.get("negStatusContract").getAsString(); 
			 String customerRole = loanDetails.get("customerRole").getAsString();
			 String allowanceProbableLoss = loanDetails.get("allowanceProbableLoss").getAsString();
			 String botProvision = loanDetails.get("botProvision").getAsString();
			 String tradingIntent = "2"; 
			 String orgSuspendedInterest = loanDetails.get("orgSuspendedInterest").getAsString();
			 String usdSuspendedInterest = loanDetails.get("usdSuspendedInterest").getAsString();
			 String tzsSuspendedInterest = loanDetails.get("tzsSuspendedInterest").getAsString();
			 
			 lastPaymentDate = util.isNullOrEmpty(lastPaymentDate) ? "<null>" : lastPaymentDate; // conditional mandatory
			 
			 loanFlagType = loanFlagType.equalsIgnoreCase("N") ? "2" : "1";
			 
			 negStatusContract = negStatusContract.equalsIgnoreCase("N") ? "1" : "4";
			 
			 if(!util.isAlphanumeric(customerIdentificationNumber)) validation.add("customerIdentificationNumber should be alphanumeric");
			 if(!util.isAlphanumeric(clientName)) validation.add("clientName should be alphanumeric");
			 if(!util.isAlphanumeric(internalRiskGroup)) validation.add("internalRiskGroup should be alphanumeric");
			 if(!util.isAlphanumeric(branchCode)) validation.add("branchCode should be alphanumeric");
			 if(!util.isAlphanumeric(loanSupervisor)) validation.add("loanSupervisor should be alphanumeric");
			 
			 if(!util.isAlphanumeric(loanOfficer)) validation.add("loanOfficer should be text");
			
			if(!util.isNumeric(accountNumber)) validation.add("accountNumber should be number");
			if(!util.isNumeric(currency)) validation.add("currency should be text");
			if(!util.isNumeric(loanNumber)) validation.add("loanNumber must be numeric");
			if(!util.isNumeric(loanInstallment)) validation.add("loanInstallment must be numeric");
			if(!util.isNumeric(orgSanctionAmount)) validation.add("orgSanctionAmount must be numeric");
			if(!util.isNumeric(usdSanctionAmount)) validation.add("usdSanctionAmount must be numeric");
			if(!util.isNumeric(tzsSanctionAmount)) validation.add("tzsSanctionAmount must be numeric");
			if(!util.isNumeric(orgDisbursedAmount)) validation.add("orgDisbursedAmount must be numeric");
			if(!util.isNumeric(usdDisbursedAmount)) validation.add("usdDisbursedAmount must be numeric");
			if(!util.isNumeric(tzsDisbursedAmount)) validation.add("tzsDisbursedAmount must be numeric");
			if(!util.isNumeric(orgOutstandingPrincipalAmount)) validation.add("orgOutstandingPrincipalAmount must be numeric");
			if(!util.isNumeric(usdOutstandingPrincipalAmount)) validation.add("usdOutstandingPrincipalAmount must be numeric");
			if(!util.isNumeric(tzsOutstandingPrincipalAmount)) validation.add("tzsOutstandingPrincipalAmount must be numeric");
			if(!util.isNumeric(orgInstallmentAmount)) validation.add("orgInstallmentAmount must be numeric");
			if(!util.isNumeric(usdInstallmentAmount)) validation.add("usdInstallmentAmount must be numeric");
			if(!util.isNumeric(tzsInstallmentAmount)) validation.add("tzsInstallmentAmount must be numeric");
			if(!util.isNumeric(loanInstallmentPaid)) validation.add("loanInstallmentPaid must be numeric");
			if(!util.isNumeric(gracePeriodPaymentPrincipal)) validation.add("gracePeriodPaymentPrincipal must be numeric");
			if(!util.isNumeric(primeLendingRate)) validation.add("primeLendingRate must be numeric");
			if(!util.isNumeric(annualInterestRate)) validation.add("annualInterestRate must be numeric");
			if(!util.isNumeric(annualEffectiveInterestRate)) validation.add("annualEffectiveInterestRate must be numeric");
			if(!util.isNumeric(pastDueAmount)) validation.add("pastDueAmount must be numeric");
			if(!util.isNumeric(orgAccruedInterestAmount)) validation.add("orgAccruedInterestAmount must be numeric");
			if(!util.isNumeric(usdAccruedInterestAmount)) validation.add("usdAccruedInterestAmount must be numeric");
			if(!util.isNumeric(tzsAccruedInterestAmount)) validation.add("tzsAccruedInterestAmount must be numeric");
			if(!util.isNumeric(orgPenaltyChargedAmount)) validation.add("orgPenaltyChargedAmount must be numeric");
			if(!util.isNumeric(usdPenaltyChargedAmount)) validation.add("usdPenaltyChargedAmount must be numeric");
			if(!util.isNumeric(tzsPenaltyChargedAmount)) validation.add("tzsPenaltyChargedAmount must be numeric");
			if(!util.isNumeric(orgPenaltyPaidAmount)) validation.add("orgPenaltyPaidAmount must be numeric");
			if(!util.isNumeric(usdPenaltyPaidAmount)) validation.add("usdPenaltyPaidAmount must be numeric");
			if(!util.isNumeric(tzsPenaltyPaidAmount)) validation.add("tzsPenaltyPaidAmount must be numeric");
			if(!util.isNumeric(orgLoanFeesChargedAmount)) validation.add("orgLoanFeesChargedAmount must be numeric");
			if(!util.isNumeric(usdLoanFeesChargedAmount)) validation.add("usdLoanFeesChargedAmount must be numeric");
			if(!util.isNumeric(tzsLoanFeesChargedAmount)) validation.add("tzsLoanFeesChargedAmount must be numeric");
			if(!util.isNumeric(orgLoanFeesPaidAmount)) validation.add("orgLoanFeesPaidAmount must be numeric");
			if(!util.isNumeric(usdLoanFeesPaidAmount)) validation.add("usdLoanFeesPaidAmount must be numeric");
			if(!util.isNumeric(tzsLoanFeesPaidAmount)) validation.add("tzsLoanFeesPaidAmount must be numeric");
			if(!util.isNumeric(orgTotMonthlyPaymentAmount)) validation.add("orgTotMonthlyPaymentAmount must be numeric");
			if(!util.isNumeric(usdTotMonthlyPaymentAmount)) validation.add("usdTotMonthlyPaymentAmount must be numeric");
			if(!util.isNumeric(tzsTotMonthlyPaymentAmount)) validation.add("tzsTotMonthlyPaymentAmount must be numeric");
			if(!util.isNumeric(allowanceProbableLoss)) validation.add("allowanceProbableLoss must be numeric");
			if(!util.isNumeric(botProvision)) validation.add("botProvision must be numeric");
			if(!util.isNumeric(orgSuspendedInterest)) validation.add("orgSuspendedInterest must be numeric");
			if(!util.isNumeric(usdSuspendedInterest)) validation.add("usdSuspendedInterest must be numeric");
			if(!util.isNumeric(tzsSuspendedInterest)) validation.add("tzsSuspendedInterest must be numeric");
		 }
		 catch(Exception e)
		 {
			 validation.add("Mandatory elements are missing in the payload");
			 
			 logger.debug("Exception in LoanInformation_validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public List<String> LoanTransaction_validation(String INFO1, String INFO2, String INFO3) //RTS191
	{ 
		List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonArray transactionDetails = request.get("transactionDetails").getAsJsonArray();
			
			 for(int i=0; i<transactionDetails.size(); i++)
			 {
				 JsonObject js = transactionDetails.get(i).getAsJsonObject();
				 
				 String reportingDate = util.getCurrentReportDate();
				 String loanNumber = js.get("loanNumber").getAsString();
				 String transactionDate = js.get("transactionDate").getAsString();
				 String currency = js.get("currency").getAsString();
				 String loanTransactionType = js.get("loanTransactionType").getAsString();
				 String loanTransactionSubType = js.get("loanTransactionSubType").getAsString();
				 String orgTransactionAmount = js.get("orgtransactionAmount").getAsString();
				 String usdTransactionAmount = js.get("usdtransactionAmount").getAsString();
				 String tzsTransactionAmount = js.get("tzstransactionAmount").getAsString();
				 
				 if(util.isNullOrEmpty(transactionDate)) validation.add("transactionDate should not be empty");	
				 if(!util.isNumeric(currency)) validation.add("currency must be numeric");	
				 if(!util.isNumeric(loanNumber)) validation.add("loanNumber must be numeric");
				 if(!util.isNumeric(orgTransactionAmount)) validation.add("orgTransactionAmount must be numeric");
				 if(!util.isNumeric(usdTransactionAmount)) validation.add("usdTransactionAmount must be numeric");
				 if(!util.isNumeric(tzsTransactionAmount)) validation.add("tzsTransactionAmount must be numeric");			
			 } 
		 }
		 catch(Exception e)
		 {
			 validation.add("Mandatory elements are missing in the payload");
			 
			 logger.debug("Exception in LoanTransaction_validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public List<String> WrittenOffLoans_validation(String INFO1, String INFO2, String INFO3) //RTS163  verified
	{ 
		 List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonObject customerDetails = request.get("customerDetails").getAsJsonObject();
			 
			 JsonObject loanDetails = request.get("loanDetails").getAsJsonObject();
			 
			 JsonArray Collateral_Details = request.get("Collateral Details").getAsJsonArray();
			  
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = getMember(customerDetails, "customerIdentificationNumber"); 
			 String accountNumber = getMember(customerDetails, "accountNumber"); 
			 String borrowerName = getMember(customerDetails, "borrowerName"); 
			 String borrowerCountry = getMember(customerDetails, "borrowerCountry"); 
			 String clientType = getMember(customerDetails, "clientType"); 
			 String loanNumber = getMember(loanDetails, "loanNumber"); 
			 String loanType = getMember(loanDetails, "loanType"); 
			 String currency = getMember(loanDetails, "currency"); 
			 String orgDisbursedAmount = getMember(loanDetails, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(loanDetails, "usdDisbursedAmount"); 
			 String tzsDisbursedAmount = getMember(loanDetails, "tzsDisbursedAmount"); 
			 String disbursementDate = getMember(loanDetails, "disbursementDate"); 
			 String gracePeriodPaymentPrincipal = getMember(loanDetails, "gracePeriodPaymentPrincipal"); 
			 String maturityDate = getMember(loanDetails, "maturityDate"); 
			 String orgGrossPaidAmount = getMember(loanDetails, "orgGrossPaidAmount"); 
			 String usdGrossPaidAmount = getMember(loanDetails, "usdGrossPaidAmount"); 
			 String tzsGrossPaidAmount = getMember(loanDetails, "tzsGrossPaidAmount"); 
			 String writtenOffDate = getMember(loanDetails, "writtenOffDate"); 
			 String orgOutstandingPrincipalAmount = getMember(loanDetails, "orgOutstandingPrincipalAmount"); 
			 String usdOutstandingPrincipalAmount = getMember(loanDetails, "usdOutstandingPrincipalAmount"); 
			 String tzsOutstandingPrincipalAmount = getMember(loanDetails, "tzsOutstandingPrincipalAmount"); 
			 String annualInterestRate = getMember(loanDetails, "annualInterestRate"); 
			 String latestInstallmentPayDate = getMember(loanDetails, "latestInstallmentPayDate");  
			 String pastDueDays = getMember(loanDetails, "pastDueDays");
			 String loanOfficer = getMember(loanDetails, "loanOfficer"); 
			 String loanSupervisor = getMember(loanDetails, "loanSupervisor"); 
			 
			 //writtenOffDate = !util.isNullOrEmpty(writtenOffDate) ? writtenOffDate : reportingDate;
			 
			 loanOfficer = !util.isNullOrEmpty(loanOfficer) ? loanOfficer : "NA";
			 
			 latestInstallmentPayDate = !util.isNullOrEmpty(latestInstallmentPayDate) ? latestInstallmentPayDate : "<null>";
			 
			 String collateralPledged = ""; // orgCollateralValue = "", usdCollateralValue = "", tzsCollateralValue = "";
			 
			 for(int i=0; i<Collateral_Details.size(); i++)
			 {
				 JsonObject js= Collateral_Details.get(i).getAsJsonObject();
				
				 collateralPledged = js.get("collateralPledged").getAsString();
				 //orgCollateralValue = js.get("orgCollateralValue").getAsString();
				 //usdCollateralValue = js.get("usdCollateralValue").getAsString();
				 //tzsCollateralValue = js.get("tzsCollateralValue").getAsString();
				 
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC013", collateralPledged }, new Lookup001_mapper());
					
				 collateralPledged = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "18" : "18";				 
			 }
			 
			 if(!util.isAlphanumeric(customerIdentificationNumber)) validation.add("customerIdentificationNumber should be alphanumeric");
			 if(!util.isNumeric(accountNumber)) validation.add("accountNumber should be numeric");
			 if(!util.isAlphanumeric(loanOfficer)) validation.add("loanOfficer should be alphanumeric");
			 if(!util.isAlphanumeric(loanSupervisor)) validation.add("loanSupervisor should be alphanumeric");
			 if(util.isNullOrEmpty(maturityDate)) validation.add("maturityDate should not be empty");	
			 if(util.isNullOrEmpty(writtenOffDate)) validation.add("writtenOffDate should not be empty");	
			 if(!util.isAlphanumeric(borrowerName)) validation.add("borrowerName should be alphanumeric");
			 
			 if(!util.isAlphanumeric(loanType)) validation.add("loanType should be text");
			 if(!util.isNumeric(pastDueDays)) validation.add("pastDueDays must be numeric");
			 
			 if(!util.isNumeric(currency)) validation.add("currency must be numeric");
			 if(!util.isNumeric(loanNumber)) validation.add("loanNumber must be numeric");
			 if(!util.isNumeric(orgDisbursedAmount)) validation.add("orgDisbursedAmount must be numeric");
			 if(!util.isNumeric(usdDisbursedAmount)) validation.add("usdDisbursedAmount must be numeric");
			 if(!util.isNumeric(tzsDisbursedAmount)) validation.add("tzsDisbursedAmount must be numeric");
			 if(!util.isNumeric(gracePeriodPaymentPrincipal)) validation.add("gracePeriodPaymentPrincipal must be numeric");
			 if(!util.isNumeric(orgGrossPaidAmount)) validation.add("orgGrossPaidAmount must be numeric");
			 if(!util.isNumeric(usdGrossPaidAmount)) validation.add("usdGrossPaidAmount must be numeric");
			 if(!util.isNumeric(tzsGrossPaidAmount)) validation.add("tzsGrossPaidAmount must be numeric");
			 if(!util.isNumeric(orgOutstandingPrincipalAmount)) validation.add("orgOutstandingPrincipalAmount must be numeric");
			 if(!util.isNumeric(usdOutstandingPrincipalAmount)) validation.add("usdOutstandingPrincipalAmount must be numeric");
			 if(!util.isNumeric(tzsOutstandingPrincipalAmount)) validation.add("tzsOutstandingPrincipalAmount must be numeric");
			 if(!util.isNumeric(annualInterestRate)) validation.add("annualInterestRate must be numeric");   
		 }
		 catch(Exception e)
		 {
			 validation.add("Mandatory elements are missing in the payload");
			 
			 logger.debug("Exception in WrittenOffLoans_validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public List<String> undrawnBalanceData_validation(String INFO1, String INFO2, String INFO3) //RTS103  verified
	{
		 List<String> validation = new ArrayList<String>();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(INFO3);
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 JsonObject attributes = data.get("attributes").getAsJsonObject();
			 
			 JsonObject request = attributes.get("request").getAsJsonObject();
			 
			 JsonObject customerDetails = request.get("customerDetails").getAsJsonObject();
			 
			 JsonObject loanDetails = request.get("loanDetails").getAsJsonObject();
			 
			 JsonArray Collateral_Details = loanDetails.get("Collateral Details").getAsJsonArray();
			
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = getMember(customerDetails, "customerIdentificationNumber");    		
			 String borrowerName = getMember(customerDetails, "borrowerName");
			 String relationshipType = "16";  //As per mppaing doc def it 16
			 String contractDate = getMember(loanDetails, "contractDate");
			 String categoryUndrawnBalance = "3"; //As per mppaing doc def it 3
			 String ratingStatus = getMember(loanDetails, "ratingStatus");  // Take it from common attribute
			 String crRatingCounterCustomer = getMember(loanDetails, "crRatingCounterCustomer");
			 String gradesUnratedCustomer = getMember(loanDetails, "gradesUnratedCustomer");
			 String currency = getMember(loanDetails, "currency");
			 String orgSanctionedAmount = getMember(loanDetails, "orgSanctionedAmount");
			 String usdSanctionedAmount = getMember(loanDetails, "usdSanctionedAmount");
			 String tzsSanctionedAmount = getMember(loanDetails, "tzsSanctionedAmount");
			 String orgDisbursedAmount = getMember(loanDetails, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(loanDetails, "usdDisbursedAmount");
			 String tzsDisbursedAmount = getMember(loanDetails, "tzsDisbursedAmount");
			 String orgUnutilisedAmount = getMember(loanDetails, "orgUnutilisedAmount");
			 String usdUnutilisedAmount = getMember(loanDetails, "usdUnutilisedAmount");
			 String tzsUnutilisedAmount = getMember(loanDetails, "tzsUnutilisedAmount");
			 String pastDueDays = "0";   // Refer common attributes
			 String allowanceProbableLoss = "0";   // Refer common attributes
			 String botProvision = "0";   //Refer common attributes
			 
			 //tzsSanctionedAmount = util.isNullOrEmpty(tzsSanctionedAmount) ? "0" : tzsSanctionedAmount;
			 
			 ratingStatus = "1";  // refer common atreibute
			 
			 String collateralType = "";
			 
			 for(int i=0; i<Collateral_Details.size(); i++)
			 {
				 JsonObject js= Collateral_Details.get(i).getAsJsonObject();
				
				 collateralType = js.get("collateralPledged").getAsString();
				
				 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1=?";  
				 
				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "MARC013", collateralType }, new Lookup001_mapper());
					
				 collateralType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "18" : "18";				 
			 }
			 
			 if(!util.isAlphanumeric(customerIdentificationNumber)) validation.add("customerIdentificationNumber should be alphanumeric");

			 if(!util.isAlphanumeric((borrowerName))) validation.add("borrowerName should be text");
			 if(!util.isNumeric(currency)) validation.add("currency should be text");
			 if(!util.isNumeric(orgSanctionedAmount)) validation.add("orgSanctionedAmount must be numeric");
			 if(!util.isNumeric(usdSanctionedAmount)) validation.add("usdSanctionedAmount must be numeric");
			 if(!util.isNumeric(tzsSanctionedAmount)) validation.add("tzsSanctionedAmount must be numeric");
			 if(!util.isNumeric(orgDisbursedAmount)) validation.add("orgDisbursedAmount must be numeric");
			 if(!util.isNumeric(usdDisbursedAmount)) validation.add("usdDisbursedAmount must be numeric");
			 if(!util.isNumeric(tzsDisbursedAmount)) validation.add("tzsDisbursedAmount must be numeric");
			 if(!util.isNumeric(orgUnutilisedAmount)) validation.add("orgUnutilisedAmount must be numeric");
			 if(!util.isNumeric(usdUnutilisedAmount)) validation.add("usdUnutilisedAmount must be numeric");
			 if(!util.isNumeric(tzsUnutilisedAmount)) validation.add("tzsUnutilisedAmount must be numeric");
			 if(!util.isNumeric(allowanceProbableLoss)) validation.add("allowanceProbableLoss must be numeric");
			 if(!util.isNumeric(botProvision)) validation.add("botProvision must be numeric");
		 }
		 catch(Exception e)
		 {
			 validation.add("Mandatory elements are missing in the payload");
			 
			 logger.debug("Exception in undrawnBalanceData_validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
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
	
	public JsonObject Send_Solace_Respone(String API_Name, String Message) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(Message);
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 String Id = data.get("id").getAsString();
			 
			 JsonObject header = new JsonObject();
			 
			 header.addProperty("source_system", "MACS");
			 header.addProperty("UETR", util.Generate_Random_String(20));
			 header.addProperty("sub_system", "");
			 header.addProperty("message_timestamp", util.getCurrentDateTime());
			 header.addProperty("DestinationEnvParam", "");
			 header.addProperty("event_type", API_Name);
			 header.addProperty("message_sender", "MACS");
			 header.addProperty("request_country", "TZ");
			 
			 JsonObject resbody = new JsonObject();
			 JsonObject response = new JsonObject();
			 JsonObject attributes = new JsonObject();
			 JsonObject resdata = new JsonObject();
			 
			 response.addProperty("response-code", "0");
			 response.addProperty("response-description", "Success");
			 
			 attributes.add("response", response);
			 
			 resdata.addProperty("id", Id);
			 resdata.add("attributes", attributes);
			 
			 resbody.add("data", resdata);
			 
			 String[] args = new String[] { "SOLACE", "SOL003", resbody.toString(), header.toString() };
			 
			 new TopicPublisher(Jdbctemplate).run(args);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Send_Solace_Respone :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Send_Solace_ErrorRespone(String API_Name, String Message, List<String> validation) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data_ = util.StringToJsonObject(Message);
			 
			 JsonObject data = data_.get("data").getAsJsonObject();
			 
			 String Id = data.get("id").getAsString();
			 
			 JsonObject header = new JsonObject();
			 
			 header.addProperty("source_system", "MACS");
			 header.addProperty("UETR", util.Generate_Random_String(20));
			 header.addProperty("sub_system", "");
			 header.addProperty("message_timestamp", util.getCurrentDateTime());
			 header.addProperty("DestinationEnvParam", "");
			 header.addProperty("event_type", API_Name);
			 header.addProperty("message_sender", "MACS");
			 header.addProperty("request_country", "TZ");
			 
			 JsonObject resbody = new JsonObject();
			 JsonObject response = new JsonObject();
			 JsonObject attributes = new JsonObject();
			 JsonObject resdata = new JsonObject();
			 
			 response.addProperty("response-code", "1");
			 response.addProperty("response-description", String.join(",", validation));
			
			 attributes.add("response", response);
			 
			 resdata.addProperty("id", Id);
			 resdata.add("attributes", attributes);
			 
			 resbody.add("data", resdata);
			 
			 String[] args = new String[] { "SOLACE", "SOL003", resbody.toString(), header.toString() };
			 
			 new TopicPublisher(Jdbctemplate).run(args);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Send_Solace_Respone :::: "+e.getLocalizedMessage());
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
	
	public String getMember(JsonObject js, String Name) 
	{
		String val = "";
		
		try
		{
			if(js.has(Name))
			{
				if(js.get(Name).isJsonNull())
				{
					return null;
				}
				else
				{
					val = js.get(Name).getAsString();
				}
			}
		}
		catch(Exception ex)
		{
			logger.debug("Exception while parsing the element "+Name+" from the Jsonobject "+js);
		}
		
		return val;
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
	
	private List<SqlParameter> get_ProcedureParams_Overdraft()
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
