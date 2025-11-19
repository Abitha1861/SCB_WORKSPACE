package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
import com.hdsoft.Repositories.Lookup001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.solace.TopicPublisher;
//import com.hdsoft.Repositories.Lookup001;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptech_ACBS implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptech_ACBS(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptech_ACBS() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptech_ACBS.class);
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(20); // Adjust pool size as needed
	
	@RequestMapping(value = {"/Datavision/acbs/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
				 details = LoanTransactions("RTS191", "", Message); //RTS191
			 }
			 else if(APICODE.equals("RTS103")) 
			 {
				 details = undrawnBalanceData("RTS103", "", Message); //RTS103
			 }
			 else if(APICODE.equals("RTS163")) 
			 {
				 details = WrittenOffLoans("RTS163", "", Message); //RTS163
			 }
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	public JsonObject ACBS_File_Processing(String INFO1, String INFO2, String INFO3) 
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
					 executorService.submit(() -> LoanInformation(INFO1, INFO2, stsEndpoint)); // Submit each job for concurrent processing
				 }
				 
				 i++;
				 
				 logger.debug("LoanInformation  Record "+i+" is processed");
             }
			 
			 if(i==0)  
			 {
				 logger.debug("LoanInformation file is having 0 Record");
			 }
			 
			 br.close();			
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
	
	public JsonObject LoanInformation(String INFO1, String INFO2, String INFO3) //RTS019  verified
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
		
			 String Response = INFO3;
			
			 JsonObject Res = util.StringToJsonObject(Response);
			
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			
			 JsonObject LoanInformation = Payload.get("loansInformation").getAsJsonObject();
			
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			
			 int count = 0;
			
			 JsonObject js = LoanInformation.getAsJsonObject();
				
			 count++;
					
			BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			String LEID = getMember(js, "crRatingBorrower");
			String LIMITID = getMember(js, "collateralPledged");
		
			String reportingDate = util.getCurrentReportDate();
			String customerIdentificationNumber = getMember(js, "customerIdentificationNumber");
			String clientName = getMember(js, "clientName");
			String accountNumber = getMember(js, "accountNumber");
			String borrowerCountry = "218";  // As per ACbS mapping
			boolean ratingStatus = true;
			String crRatingBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", LEID); //cc; ;
			String gradesUnratedBanks = "";
			String categoryBorrower = "1";
			String gender =  "3";  // As per ACbS mapping
			String disability = "";   // As per ACbS mapping
			String clientType = "7"; // As per ACbS mapping
			String clientSubType = "";  // As per ACbS mapping
			String groupName = "NA";
			String groupCode = "NA";
			String relatedParty = "9";  // As per ACbS mapping
			String relationshipCategory = "1";  // As per ACbS mapping
			String loanNumber = getMember(js, "loanNumber");
			String loanType = "2";  // As per ACbS mapping
			String loanEconomicActivity = ST.FindElementFromFileIT("SCI", "LOANECONOMICACTIVITY", "LEID", LEID); //cc;
			String loanPhase = getMember(js, "loanPhase");
			String transferStatus =  "2";  // As per ACbS mapping
			String purposeMortgage = "";  // As per ACbS mapping
			String purposeOtherLoans = "";  // As per ACbS mapping
			String sourceFundMortgage = "";  // As per ACbS mapping
			String amortizationType = getMember(js, "amortizationType");
			String branchCode = "008300";  //"005083";  // As per ACbS mapping
			String loanOfficer = getMember(js, "loanOfficer");
			String loanSupervisor = getMember(js, "loanSupervisor");
			String groupVillageNumber = "NA"; 
			String cycleNumber = "0";
			String loanInstallment = getMemberasInt(js, "loanInstallment");
			String repaymentFrequency = getMember(js, "repaymentFrequency");  //Check with ACBS 
			String currency = getMember(js, "currency");
			String contractDate = getMember(js, "contractDate");
			String orgSanctionAmount = getMember(js, "orgSanctionAmount");
			String usdSanctionAmount = getMember(js, "usdSanctionAmount");
			String tzsSanctionAmount = getMember(js, "tzsSanctionAmount");
			String orgDisbursedAmount = getMember(js, "orgDisbursedAmount");
			String usdDisbursedAmount = getMember(js, "usdDisbursedAmount");
			String tzsDisbursedAmount = getMember(js, "tzsDisbursedAmount");
			String disbursementDate = getMember(js, "disbursementDate");
			String maturityDate = getMember(js, "maturityDate");
			String realEndDate = getMember(js, "realEndDate");
			String orgOutstandingPrincipalAmount = getMember(js, "orgOutstandingPrincipalAmount");
			String usdOutstandingPrincipalAmount = getMember(js, "usdOutstandingPrincipalAmount");
			String tzsOutstandingPrincipalAmount = getMember(js, "tzsOutstandingPrincipalAmount");
			String orgInstallmentAmount = getMember(js, "orgInstallmentAmount");
			String usdInstallmentAmount = getMember(js, "usdInstallmentAmount");
			String tzsInstallmentAmount = getMember(js, "tzsInstallmentAmount");
			String loanInstallmentPaid = getMember(js, "loanInstallmentPaid");
			String gracePeriodPaymentPrincipal = "0";
			String primeLendingRate = getMemberasInt(js, "primeLendingRate");
			String annualInterestRate = getMemberasInt(js, "annualInterestRate");
			String annualEffectiveInterestRate = getMember(js, "effectiveAnnualInterestRate");
			String firstInstallmentPaymentDate = getMember(js, "firstInstallmentPaymentDate");
			String lastPaymentDate = getMember(js, "lastPaymentDate");
			String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", LEID+"|"+LIMITID); //cc;
			String orgCollateralValue = ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT", "LEID|LIMITID", LEID+"|"+LIMITID); //cc;
			String Collateralcurrency = ST.FindElementFromFileIT("SCI", "COLLATERALCURR", "LEID|LIMITID", LEID+"|"+LIMITID); //cc;
			String usdCollateralValue = getMember(js, "usdCollateralValue");
			String tzsCollateralValue = getMember(js, "tzsCollateralValue");
			String loanFlagType = getMember(js, "loanFlagType");
			String restructuringDate = getMember(js, "restructuringDate");  //check with ACBS
			String pastDueDays = getMemberasInt(js, "pastDueDays");
			String pastDueAmount = getMember(js, "pastDueAmount");  
			String internalRiskGroup = getMember(js, "internalRiskGroup"); 
			String orgAccruedInterestAmount = getMember(js, "orgAccruedInterestAmount");
			String usdAccruedInterestAmount = getMember(js, "usdAccruedInterestAmount");
			String tzsAccruedInterestAmount = getMember(js, "tzsAccruedInterestAmount");
			String orgPenaltyChargedAmount = getMember(js, "orgPenaltyChargedAmount");
			String usdPenaltyChargedAmount = getMember(js, "usdPenaltyChargedAmount");
			String tzsPenaltyChargedAmount = getMember(js, "tzsPenaltyChargedAmount");
			String orgPenaltyPaidAmount = getMember(js, "orgPenaltyPaidAmount");
			String usdPenaltyPaidAmount = getMember(js, "usdPenaltyPaidAmount");
			String tzsPenaltyPaidAmount = getMember(js, "tzsPenaltyPaidAmount");
			String orgLoanFeesChargedAmount = getMember(js, "orgLoanFeesChargedAmount");
			String usdLoanFeesChargedAmount = getMember(js, "usdLoanFeesChargedAmount");
			String tzsLoanFeesChargedAmount = getMember(js, "tzsLoanFeesChargedAmount");
			String orgLoanFeesPaidAmount = getMember(js, "orgLoanFeesPaidAmount");
			String usdLoanFeesPaidAmount = getMember(js, "usdLoanFeesPaidAmount");
			String tzsLoanFeesPaidAmount = getMember(js, "tzsLoanFeesPaidAmount");
			String orgToMonthlyPaymentAmount = getMember(js, "orgToMonthlyPaymentAmount");
			String usdToMonthlyPaymentAmount = getMember(js, "usdToMonthlyPaymentAmount");
			String tzsToMonthlyPaymentAmount = getMember(js, "tzsToMonthlyPaymentAmount"); 
			String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID); //cc
			String assetClassificationCategory = ST.find_assetClassification(getMember(js, "assetClassificationCategory"));  //splice
			String negStatusContract = "1"; // As per mapping
			String customerRole = "2"; // As per mapping
			String allowanceProbableLoss = ST.find_Allowable_Probable_Loss(getMember(js, "allowanceProbableLoss"));  // splice ;   //get it frpom common attribue
			String botProvision = "0";
			String tradingIntent = "2"; // As per mapping
			String interestPricingMethod = getMember(js, "interestPricingMethod");  //check with acbs
			String orgSuspendedInterest = getMember(js, "orgSuspendedInterest"); 
			String usdSuspendedInterest = getMember(js, "usdSuspendedInterest"); 
			String tzsSuspendedInterest = getMember(js, "tzsSuspendedInterest"); 
			
			realEndDate= util.isNullOrEmpty(realEndDate) ? "<null>" : realEndDate;
			
			restructuringDate = util.Convert_BOT_Date_Format(restructuringDate, "yyyy-MM-dd'T'HH:mm:ss");
			 
			restructuringDate= util.isNullOrEmpty(restructuringDate) ? "<null>" : restructuringDate;

			interestPricingMethod = "1";  // for unit testing  //get the lookup list from abitha
			
			contractDate = util.Convert_BOT_Date_Format(contractDate, "yyyy-MM-dd'T'HH:mm:ss");			
			disbursementDate = util.Convert_BOT_Date_Format(disbursementDate, "yyyy-MM-dd'T'HH:mm:ss");
			maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd'T'HH:mm:ss");
			firstInstallmentPaymentDate = util.Convert_BOT_Date_Format(firstInstallmentPaymentDate, "yyyy-MM-dd'T'HH:mm:ss");
			lastPaymentDate = util.Convert_BOT_Date_Format(lastPaymentDate, "yyyy-MM-dd'T'HH:mm:ss");
	
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
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgCollateralValue, Collateralcurrency);
			 
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
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgToMonthlyPaymentAmount, currency);
			 
			usdToMonthlyPaymentAmount = rates.get("usd").getAsString();
			tzsToMonthlyPaymentAmount = rates.get("tzs").getAsString();
			
			rates = fx.find_exchangeRate(util.getCurrentDate(), orgSuspendedInterest, currency);
			 
			usdSuspendedInterest = rates.get("usd").getAsString();
			tzsSuspendedInterest = rates.get("tzs").getAsString();
			
			String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS007", loanPhase }, new Lookup001_mapper());
				
			loanPhase = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "1" : "1";
					
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS010", amortizationType }, new Lookup001_mapper());
				
			amortizationType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "3" : "3";
			 
			sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());  
				
			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS019", loanFlagType }, new Lookup001_mapper());
			
			loanFlagType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "2" : "2";
			
			sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and lower(COLUMN2)=lower(?)";  
			 
			Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS020", repaymentFrequency }, new Lookup001_mapper());
			
			repaymentFrequency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "5" : "5";
			
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
					 usdLoanFeesPaidAmount,tzsLoanFeesPaidAmount,orgToMonthlyPaymentAmount,usdToMonthlyPaymentAmount,tzsToMonthlyPaymentAmount,sectorSnaClassification,assetClassificationCategory,negStatusContract,customerRole,allowanceProbableLoss,
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
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "ACBS" });	 
		     
		     StoreLoanLog(SUBORGCODE, INFO1, "ACBS", loanNumber, Batch_id, O_SERIAL);
		     
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
	
	public JsonObject LoanTransactions(String INFO1, String INFO2, String INFO3) //RTS191  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 	
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
					 
			 JsonObject Res = util.StringToJsonObject(INFO3);
			 
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			 
			 JsonObject LoanTransactions = Payload.get("loanTransactions").getAsJsonObject();
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 int count = 0;
			 
			 count++;
		 
			 String reportingDate = util.getCurrentReportDate();
			 String loanNumber = getMember(LoanTransactions, "loanNumber"); 
			 String transactionDate = getMember(LoanTransactions, "transactionDate"); 
			 String loanTransactionType = getMember(LoanTransactions, "loanTransactionType"); 
			 String loanTransactionSubType = getMember(LoanTransactions, "loanTransactionSubType"); 
			 String currency = getMember(LoanTransactions, "currency"); 
			 String orgTransactionAmount = getMember(LoanTransactions, "orgTransactionAmount"); 
			 String usdTransactionAmount = getMember(LoanTransactions, "usdTransactionAmount"); 
			 String tzsTransactionAmount = getMember(LoanTransactions, "tzsTransactionAmount"); 
			 
			 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS017", loanTransactionType }, new Lookup001_mapper());
				
			 loanTransactionType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "1" : "1";
			 
			 loanTransactionSubType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "" : "";
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanTransactionInformation", count, reportingDate, loanNumber, transactionDate, loanTransactionType, loanTransactionSubType,
					 currency, orgTransactionAmount,  usdTransactionAmount, tzsTransactionAmount  });
		   
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanTransactionInformation", "serial", "reportingDate", "loanNumber", "transactionDate", "loanTransactionType", "loanTransactionSubType",
					 "currency", "orgTransactionAmount",  "usdTransactionAmount", "tzsTransactionAmount" });
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanTransactionInformation"});
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assset Data", "loanTransactionInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "ACBS" });	 		 		 
			
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
			 
			logger.debug("Exception in LoanTransactions :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject WrittenOffLoans(String INFO1, String INFO2, String INFO3) //RTS163  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String Response = INFO3;
			 
			 JsonObject Res = util.StringToJsonObject(Response);
			 
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			 
			 JsonObject js = Payload.get("writtenOffLoans").getAsJsonObject();
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 int count = 0;
				 
			 count++;
		 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String LEID = getMember(js, "customerIdentificationNumber");
			 String LIMITID = getMember(js, "collateralPledged");
			 
			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = getMember(js, "customerIdentificationNumber"); 
			 String accountNumber = getMember(js, "accountNumber"); 
			 String borrowerName = getMember(js, "borrowerName"); 
			 String borrowerCountry = getMember(js, "borrowerCountry"); 
			 String clientType = getMember(js, "clientType"); 
			 String loanNumber = getMember(js, "loanNumber"); 
			 String loanType = getMember(js, "loanType"); 
			 String currency = getMember(js, "currency"); 
			 String orgDisbursedAmount = getMember(js, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(js, "usdDisbursedAmount"); 
			 String tzsDisbursedAmount = getMember(js, "tszDisbursedAmount"); 
			 String disbursementDate = getMember(js, "disbursementDate"); 
			 String gracePeriodPaymentPrincipal = getMember(js, "gracePeriodPaymentPrincipal"); 
			 String maturityDate = getMember(js, "maturityDate"); 
			 String orgGrossPaidAmount = getMember(js, "orgGrossPaidAmount"); 
			 String usdGrossPaidAmount = getMember(js, "usdGrossPaidAmount"); 
			 String tzsGrossPaidAmount = getMember(js, "tzsGrossPaidAmount"); 
			 String writtenOffDate = getMember(js, "writtenOffDate"); 
			 String orgOutstandingPrincipalAmount = getMember(js, "orgOutstandingPrincipalAmount"); 
			 String usdOutstandingPrincipalAmount = getMember(js, "usdOutstandingPrincipalAmount"); 
			 String tzsOutstandingPrincipalAmount = getMember(js, "tzsOutstandingPrincipalAmount"); 
			 String annualInterestRate = getMember(js, "annualInterestRate"); 
			 String latestInstallmentPayDate = "<null>";   
			 String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", LEID+"|"+LIMITID); //cc
			 String orgCollateralValue = ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT", "LEID|LIMITID", LEID+"|"+LIMITID); //cc;
			 String Collateralcurrency = ST.FindElementFromFileIT("SCI", "COLLATERALCURR", "LEID|LIMITID", LEID+"|"+LIMITID); //cc;
			 String usdCollateralValue = getMember(js, "usdCollateralValue"); 
			 String tzsCollateralValue = getMember(js, "tzsCollateralValue"); 
			 String pastDueDays = getMemberasInt(js, "pastDueDays");
			 String loanOfficer = getMember(js, "loanOfficer"); 
			 String loanSupervisor = getMember(js, "loanSupervisor"); 
			 
			 pastDueDays = util.ParseInt(pastDueDays) < 0 ? "0" : pastDueDays+""; 
			 			
			 writtenOffDate = util.Convert_BOT_Date_Format(writtenOffDate, "yyyy-MM-dd'T'HH:mm:ss");
			 disbursementDate = util.Convert_BOT_Date_Format(disbursementDate, "yyyy-MM-dd'T'HH:mm:ss");	
			 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd'T'HH:mm:ss");
			 
			 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
			 
			 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgCollateralValue, Collateralcurrency);
			 
			 usdCollateralValue = rates.get("usd").getAsString();
			 tzsCollateralValue = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgDisbursedAmount, currency);
			 
			 usdDisbursedAmount = rates.get("usd").getAsString();
			 tzsDisbursedAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgGrossPaidAmount, currency);
			 
			 usdGrossPaidAmount = rates.get("usd").getAsString();
			 tzsGrossPaidAmount = rates.get("tzs").getAsString();
			 
			 rates = fx.find_exchangeRate(util.getCurrentDate(), orgOutstandingPrincipalAmount, currency);
			 
			 usdOutstandingPrincipalAmount = rates.get("usd").getAsString();
			 tzsOutstandingPrincipalAmount = rates.get("tzs").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", borrowerCountry }, new Lookup001_mapper());
				
			 borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS004", clientType }, new Lookup001_mapper());
				
			 clientType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "3" : "3";
			 
			 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "ACBS006", loanType }, new Lookup001_mapper());
				
			 loanType = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "1" : "1";
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "CUR", currency }, new Lookup001_mapper());
				
			 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
			 
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
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Bank Data", "writtenOffLoans", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "ACBS" });	 		 		 
			
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
				
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			
			 String Response = INFO3;
			
			 JsonObject Res = util.StringToJsonObject(Response);
			
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			
			 JsonObject js = Payload.get("undrawnBalances").getAsJsonObject();
			
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				
			 int count = 0;
				
			 count++;
			
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 String LEID = getMember(js, "customerIdentificationNumber");
			 String LIMITID = getMember(js, "collateralType");
			 
			 String reportingDate = util.getCurrentReportDate();
			 String borrowerName = getMember(js, "borrowerName");
			 String relationshipType = "17" ;   //Default suggested by ACBS  D33 lookup
			 String contractDate = getMember(js, "contractDate");
			 String categoryUndrawnBalance = "2";  //Default suggested by ACBS  D41 lookup
			 boolean ratingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", LEID); //cc; 
			 String gradesUnratedCustomer = ""; 
			 String currency = getMember(js, "currency");  
			 String orgSanctionedAmount = getMember(js, "orgSanctionedAmount");
			 String usdSanctionedAmount = getMember(js, "usdSanctionedAmount");
			 String tzsSanctionedAmount = getMember(js, "tzsSactionedAmount");
			 String orgDisbursedAmount = getMember(js, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(js, "usdDisbursedAmount");
			 String tzsDisbursedAmount = getMember(js, "tzsDisbursedAmount");
			 String orgUnutilisedAmount = getMember(js, "orgUnutilisedAmount");
			 String usdUnutilisedAmount = getMember(js, "usdUnutilisedAmount");
			 String tzsUnutilisedAmount = getMember(js, "tzsUnutilisedAmount");
			 String collateralType = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", LEID+"|"+LIMITID); //cc;
			 String pastDueDays = getMemberasInt(js, "pastDueDays");
			 String allowanceProbableLoss = "0";
			 String botProvision = "0"; 
			 
			 contractDate = util.Convert_BOT_Date_Format(contractDate, "yyyy-MM-dd'T'HH:mm:ss");
			 
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
			
			 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			 currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";		

			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13,"
			 		+ "COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26) values\r\n" +
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "undrawnBalanceData", count,  reportingDate,
					 borrowerName, relationshipType, contractDate, categoryUndrawnBalance, ratingStatus, 
					 crRatingCounterCustomer, gradesUnratedCustomer, currency, orgSanctionedAmount, usdSanctionedAmount,
					 tzsSanctionedAmount, orgDisbursedAmount, usdDisbursedAmount, tzsDisbursedAmount, orgUnutilisedAmount,
					 usdUnutilisedAmount, tzsUnutilisedAmount, collateralType, pastDueDays, allowanceProbableLoss, botProvision });
				
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13,"
		  		+ "COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26) values\r\n" +
				   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "undrawnBalanceData", "serial", "reportingDate", "borrowerName",
				 "relationshipType", "contractDate", "categoryUndrawnBalance", "ratingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency",
				 "orgSanctionedAmount", "usdSanctionedAmount", "tzsSanctionedAmount", "orgDisbursedAmount", "usdDisbursedAmount", "tzsDisbursedAmount",
				 "orgUnutilisedAmount", "usdUnutilisedAmount", "tzsUnutilisedAmount", "collateralType", "pastDueDays", "allowanceProbableLoss", "botProvision" });
		
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "undrawnBalanceData"});
			
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OBS", "undrawnBalanceData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "ACBS" });	 		 		
			
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
	
	public List<String> LoanInformation_Validation(String INFO1, String INFO2, String INFO3)
	{
		 List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Response = INFO3;
			
			 JsonObject Res = util.StringToJsonObject(Response);
			
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			
			 JsonObject LoanInformation = Payload.get("loansInformation").getAsJsonObject();
			
			 JsonObject js = LoanInformation.getAsJsonObject();
				
			String reportingDate = util.getCurrentReportDate();
			String customerIdentificationNumber = getMember(js, "customerIdentificationNumber");
			String clientName = getMember(js, "clientName");
			String accountNumber = getMember(js, "accountNumber");
			String borrowerCountry = "218";  // As per ACbS mapping
			String ratingStatus = getMember(js, "ratingStatus");
			String crRatingBorrower = getMember(js, "crRatingBorrower");
			String gradesUnratedBanks = getMember(js, "gradesUnratedBanks");
			String categoryBorrower = "1";
			String gender =  "3";  // As per ACbS mapping
			String disability = "";   // As per ACbS mapping
			String clientType = "7"; // As per ACbS mapping
			String clientSubType = "";  // As per ACbS mapping
			String groupName = "NA";
			String groupCode = "NA";
			String relatedParty = "9";  // As per ACbS mapping
			String relationshipCategory = "1";  // As per ACbS mapping
			String loanNumber = getMember(js, "loanNumber");
			String loanType = "2";  // As per ACbS mapping
			String loanEconomicActivity = getMember(js, "loanEconomicActivity");  //Take it from client coverage
			String loanPhase = getMember(js, "loanPhase");
			String transferStatus =  "2";  // As per ACbS mapping
			String purposeMortgage = "";  // As per ACbS mapping
			String purposeOtherLoans = "";  // As per ACbS mapping
			String sourceFundMortgage = "";  // As per ACbS mapping
			String amortizationType = getMember(js, "amortizationType");
			String branchCode = "008300";  //"005083";  // As per ACbS mapping
			String loanOfficer = getMember(js, "loanOfficer");
			String loanSupervisor = getMember(js, "loanSupervisor");
			String groupVillageNumber = "NA"; 
			String cycleNumber = "0";
			String loanInstallment = getMemberasInt(js, "loanInstallment");
			String repaymentFrequency = getMember(js, "repaymentFrequency");  //Check with ACBS 
			String currency = getMember(js, "currency");
			String contractDate = getMember(js, "contractDate");
			String orgSanctionAmount = getMember(js, "orgSanctionAmount");
			String usdSanctionAmount = getMember(js, "usdSanctionAmount");
			String tzsSanctionAmount = getMember(js, "tzsSanctionAmount");
			String orgDisbursedAmount = getMember(js, "orgDisbursedAmount");
			String usdDisbursedAmount = getMember(js, "usdDisbursedAmount");
			String tzsDisbursedAmount = getMember(js, "tzsDisbursedAmount");
			String disbursementDate = getMember(js, "disbursementDate");
			String maturityDate = getMember(js, "maturityDate");
			String realEndDate = getMember(js, "realEndDate");
			String orgOutstandingPrincipalAmount = getMember(js, "orgOutstandingPrincipalAmount");
			String usdOutstandingPrincipalAmount = getMember(js, "usdOutstandingPrincipalAmount");
			String tzsOutstandingPrincipalAmount = getMember(js, "tzsOutstandingPrincipalAmount");
			String orgInstallmentAmount = getMember(js, "orgInstallmentAmount");
			String usdInstallmentAmount = getMember(js, "usdInstallmentAmount");
			String tzsInstallmentAmount = getMember(js, "tzsInstallmentAmount");
			String loanInstallmentPaid = getMember(js, "loanInstallmentPaid");
			String gracePeriodPaymentPrincipal = "0";
			String primeLendingRate = getMemberasInt(js, "primeLendingRate");
			String annualInterestRate = getMemberasInt(js, "annualInterestRate");
			String annualEffectiveInterestRate = getMember(js, "effectiveAnnualInterestRate");
			String firstInstallmentPaymentDate = getMember(js, "firstInstallmentPaymentDate");
			String lastPaymentDate = getMember(js, "lastPaymentDate");
			String collateralPledged = getMember(js, "collateralPledged");
			String orgCollateralValue = getMember(js, "orgCollateralValue");
			String usdCollateralValue = getMember(js, "usdCollateralValue");
			String tzsCollateralValue = getMember(js, "tzsCollateralValue");
			String loanFlagType = getMember(js, "loanFlagType");
			String restructuringDate = getMember(js, "restructuringDate");  //check with ACBS
			String pastDueDays = getMemberasInt(js, "pastDueDays");
			String pastDueAmount = getMember(js, "pastDueAmount");  
			String internalRiskGroup = getMember(js, "internalRiskGroup"); 
			String orgAccruedInterestAmount = getMember(js, "orgAccruedInterestAmount");
			String usdAccruedInterestAmount = getMember(js, "usdAccruedInterestAmount");
			String tzsAccruedInterestAmount = getMember(js, "tzsAccruedInterestAmount");
			String orgPenaltyChargedAmount = getMember(js, "orgPenaltyChargedAmount");
			String usdPenaltyChargedAmount = getMember(js, "usdPenaltyChargedAmount");
			String tzsPenaltyChargedAmount = getMember(js, "tzsPenaltyChargedAmount");
			String orgPenaltyPaidAmount = getMember(js, "orgPenaltyPaidAmount");
			String usdPenaltyPaidAmount = getMember(js, "usdPenaltyPaidAmount");
			String tzsPenaltyPaidAmount = getMember(js, "tzsPenaltyPaidAmount");
			String orgLoanFeesChargedAmount = getMember(js, "orgLoanFeesChargedAmount");
			String usdLoanFeesChargedAmount = getMember(js, "usdLoanFeesChargedAmount");
			String tzsLoanFeesChargedAmount = getMember(js, "tzsLoanFeesChargedAmount");
			String orgLoanFeesPaidAmount = getMember(js, "orgLoanFeesPaidAmount");
			String usdLoanFeesPaidAmount = getMember(js, "usdLoanFeesPaidAmount");
			String tzsLoanFeesPaidAmount = getMember(js, "tzsLoanFeesPaidAmount");
			String orgToMonthlyPaymentAmount = getMember(js, "orgToMonthlyPaymentAmount");  //orgToMonthlyPaymentAmount
			String usdToMonthlyPaymentAmount = getMember(js, "usdToMonthlyPaymentAmount");
			String tzsToMonthlyPaymentAmount = getMember(js, "tzsToMonthlyPaymentAmount"); //check with acbs
			String sectorSnaClassification = getMember(js, "sectorSnaClassification");
			String assetClassificationCategory = getMember(js, "assetClassificationCategory");  //get it frpom common attribue
			String negStatusContract = "1"; // As per mapping
			String customerRole = "2"; // As per mapping
			String allowanceProbableLoss = getMember(js, "allowanceProbableLoss");   //get it frpom common attribue
			String botProvision = "0";
			String tradingIntent = "2"; // As per mapping
			String interestPricingMethod = getMember(js, "interestPricingMethod");  //check with acbs
			String orgSuspendedInterest = getMember(js, "orgSuspendedInterest"); 
			String usdSuspendedInterest = getMember(js, "usdSuspendedInterest"); 
			String tzsSuspendedInterest = getMember(js, "tzsSuspendedInterest"); 
			
			if(!util.isAlphanumeric(customerIdentificationNumber)) validation.add("customerIdentificationNumber should be alphanumeric");
			if(!util.isAlphanumeric(clientName)) validation.add("clientName should be alphanumeric");
			if(!util.isAlphanumeric(internalRiskGroup)) validation.add("internalRiskGroup should be alphanumeric");
			if(!util.isAlphanumeric(branchCode)) validation.add("branchCode should be alphanumeric");
			if(!util.isAlphanumeric(loanSupervisor)) validation.add("loanSupervisor should be alphanumeric");
			if(!util.isAlphanumeric(loanOfficer)) validation.add("loanOfficer should be text");
			
			if(!util.isNumeric(accountNumber)) validation.add("accountNumber should be number");
			if(!util.isText(currency)) validation.add("currency should be text");
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
			if(!util.isNumeric(pastDueAmount)) validation.add("pastDueAmount must be numeric");
			if(!util.isNumeric(orgAccruedInterestAmount)) validation.add("orgAccruedInterestAmount must be numeric");
			if(!util.isNumeric(orgPenaltyChargedAmount)) validation.add("orgPenaltyChargedAmount must be numeric");
			if(!util.isNumeric(orgPenaltyPaidAmount)) validation.add("orgPenaltyPaidAmount must be numeric");
			if(!util.isNumeric(orgLoanFeesChargedAmount)) validation.add("orgLoanFeesChargedAmount must be numeric");
			if(!util.isNumeric(orgLoanFeesPaidAmount)) validation.add("orgLoanFeesPaidAmount must be numeric");
			if(!util.isNumeric(orgToMonthlyPaymentAmount)) validation.add("orgToMonthlyPaymentAmount must be numeric");
			if(util.isNullOrEmpty(allowanceProbableLoss)) validation.add("allowanceProbableLoss must not be empty");
			if(!util.isNumeric(botProvision)) validation.add("botProvision must be numeric");
			if(!util.isNumeric(orgSuspendedInterest)) validation.add("orgSuspendedInterest must be numeric");
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in LoanInformation validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public List<String> LoanTransactions_validation(String INFO1, String INFO2, String INFO3)
	{ 
		 List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Response = INFO3;
			 
			 JsonObject Res = util.StringToJsonObject(Response);
			 
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			 
			 JsonObject LoanTransactions = Payload.get("loanTransactions").getAsJsonObject();
			
			 String reportingDate = util.getCurrentReportDate();
			 String loanNumber = getMember(LoanTransactions, "loanNumber"); 
			 String transactionDate = getMember(LoanTransactions, "transactionDate"); 
			 String loanTransactionType = getMember(LoanTransactions, "loanTransactionType"); 
			 String loanTransactionSubType = getMember(LoanTransactions, "loanTransactionSubType"); 
			 String currency = getMember(LoanTransactions, "currency"); 
			 String orgTransactionAmount = getMember(LoanTransactions, "orgTransactionAmount"); 
			 String usdTransactionAmount = getMember(LoanTransactions, "usdTransactionAmount"); 
			 String tzsTransactionAmount = getMember(LoanTransactions, "tzsTransactionAmount"); 
			 
			 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
			
			 if(!util.isText(currency)) validation.add("currency must be text");

			 if(!util.isNumeric(loanNumber)) validation.add("loanNumber must be numeric");
			 if(!util.isNumeric(orgTransactionAmount)) validation.add("orgTransactionAmount must be numeric");
		 }
		 catch(Exception e)
		 {
			logger.debug("Exception in LoanTransactions validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public List<String> WrittenOffLoans_validation(String INFO1, String INFO2, String INFO3)
	{ 
		 List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Response = INFO3;
			 
			 JsonObject Res = util.StringToJsonObject(Response);
			 
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			 
			 JsonObject js = Payload.get("writtenOffLoans").getAsJsonObject();

			 String reportingDate = util.getCurrentReportDate();
			 String customerIdentificationNumber = getMember(js, "customerIdentificationNumber"); 
			 String accountNumber = getMember(js, "accountNumber"); 
			 String borrowerName = getMember(js, "borrowerName"); 
			 String borrowerCountry = getMember(js, "borrowerCountry"); 
			 String clientType = getMember(js, "clientType"); 
			 String loanNumber = getMember(js, "loanNumber"); 
			 String loanType = getMember(js, "loanType"); 
			 String currency = getMember(js, "currency"); 
			 String orgDisbursedAmount = getMember(js, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(js, "usdDisbursedAmount"); 
			 String tzsDisbursedAmount = getMember(js, "tszDisbursedAmount"); 
			 String disbursementDate = getMember(js, "disbursementDate"); 
			 String gracePeriodPaymentPrincipal = getMember(js, "gracePeriodPaymentPrincipal"); 
			 String maturityDate = getMember(js, "maturityDate"); 
			 String orgGrossPaidAmount = getMember(js, "orgGrossPaidAmount"); 
			 String usdGrossPaidAmount = getMember(js, "usdGrossPaidAmount"); 
			 String tzsGrossPaidAmount = getMember(js, "tzsGrossPaidAmount"); 
			 String writtenOffDate = getMember(js, "writtenOffDate"); 
			 String orgOutstandingPrincipalAmount = getMember(js, "orgOutstandingPrincipalAmount"); 
			 String usdOutstandingPrincipalAmount = getMember(js, "usdOutstandingPrincipalAmount"); 
			 String tzsOutstandingPrincipalAmount = getMember(js, "tzsOutstandingPrincipalAmount"); 
			 String annualInterestRate = getMember(js, "annualInterestRate"); 
			 String latestInstallmentPayDate = "<null>";   
			 String collateralPledged = getMember(js, "collateralPledged"); 
			 String orgCollateralValue = getMember(js, "orgCollateralValue"); 
			 String usdCollateralValue = getMember(js, "usdCollateralValue"); 
			 String tzsCollateralValue = getMember(js, "tzsCollateralValue"); 
			 String pastDueDays = getMemberasInt(js, "pastDueDays");
			 String loanOfficer = getMember(js, "loanOfficer"); 
			 String loanSupervisor = getMember(js, "loanSupervisor"); 
			 
			 if(!util.isNumeric(orgCollateralValue)) validation.add("orgCollateralValue should be numeric");
			 if(!util.isNumeric(accountNumber)) validation.add("accountNumber should be number");
			 if(!util.isAlphanumeric(customerIdentificationNumber)) validation.add("customerIdentificationNumber should be alphanumeric");
			 if(!util.isAlphanumeric(loanOfficer)) validation.add("loanOfficer should be alphanumeric");
			 if(!util.isAlphanumeric(loanSupervisor)) validation.add("loanSupervisor should be alphanumeric");
			 if(!util.isAlphanumeric(borrowerName)) validation.add("borrowerName should be alphanumeric");
			 if(!util.isAlphanumeric(loanType)) validation.add("loanType should be text");
			 if(!util.isText(currency)) validation.add("currency must be text");
			 if(!util.isNumeric(loanNumber)) validation.add("loanNumber must be numeric");
			 if(!util.isNumeric(orgDisbursedAmount)) validation.add("orgDisbursedAmount must be numeric");
			 if(!util.isNumeric(gracePeriodPaymentPrincipal)) validation.add("gracePeriodPaymentPrincipal must be numeric");
			 if(!util.isNumeric(orgGrossPaidAmount)) validation.add("orgGrossPaidAmount must be numeric");
			 if(!util.isNumeric(orgOutstandingPrincipalAmount)) validation.add("orgOutstandingPrincipalAmount must be numeric");
			 if(!util.isNumeric(annualInterestRate)) validation.add("annualInterestRate must be numeric");    
		 }
		 catch(Exception e)
		 { 
			 logger.debug("Exception in WrittenOffLoans validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public List<String> undrawnBalanceData_validation(String INFO1, String INFO2, String INFO3) 
	{
		 List<String> validation = new ArrayList<String>();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
				
			 String Response = INFO3;
			
			 JsonObject Res = util.StringToJsonObject(Response);
			
			 JsonObject Payload = Res.get("payload").getAsJsonObject();
			
			 JsonObject js = Payload.get("undrawnBalances").getAsJsonObject();
			
			 String reportingDate = util.getCurrentReportDate();
			 String borrowerName = getMember(js, "borrowerName");
			 String relationshipType = "17" ;   //Default suggested by ACBS  D33 lookup
			 String contractDate = getMember(js, "contractDate");
			 String categoryUndrawnBalance = "2";  //Default suggested by ACBS  D41 lookup
			 String ratingStatus = getMember(js, "ratingStatus");
			 String crRatingCounterCustomer = "7" ; //need to discuss with ACBS team  //D67
			 String gradesUnratedCustomer = "1"; //need to discuss with ACBS team  //D58
			 String currency = getMember(js, "currency");  //Curr
			 String orgSanctionedAmount = getMember(js, "orgSanctionedAmount");
			 String usdSanctionedAmount = getMember(js, "usdSanctionedAmount");
			 String tzsSanctionedAmount = getMember(js, "tzsSactionedAmount");
			 String orgDisbursedAmount = getMember(js, "orgDisbursedAmount");
			 String usdDisbursedAmount = getMember(js, "usdDisbursedAmount");
			 String tzsDisbursedAmount = getMember(js, "tzsDisbursedAmount");
			 String orgUnutilisedAmount = getMember(js, "orgUnutilisedAmount");
			 String usdUnutilisedAmount = getMember(js, "usdUnutilisedAmount");
			 String tzsUnutilisedAmount = getMember(js, "tzsUnutilisedAmount");
			 String collateralType = "2"; //need to discuss with ACBS team  //D42
			 String pastDueDays = getMemberasInt(js, "pastDueDays");
			 String allowanceProbableLoss = "0"; //getMember(js, "allowanceProbableLoss");
			 String botProvision = "0"; //getMember(js, "botProvision");
			 
			 if(!util.isAlphanumeric(borrowerName)) validation.add("borrowerName should be text");
			 if(!util.isPureText(currency)) validation.add("currency should be text");
			 if(!util.isNumeric(orgSanctionedAmount)) validation.add("orgSanctionedAmount must be numeric");
			 if(!util.isNumeric(orgDisbursedAmount)) validation.add("orgDisbursedAmount must be numeric");
			 if(!util.isNumeric(orgUnutilisedAmount)) validation.add("orgUnutilisedAmount must be numeric");
			 if(!util.isNumeric(allowanceProbableLoss)) validation.add("allowanceProbableLoss must be numeric");
			 if(!util.isNumeric(botProvision)) validation.add("botProvision must be numeric"); 
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in undrawnBalanceData validation:::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public JsonObject Send_Solace_Respone(boolean Result, String Message) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject out = new JsonObject();
			 
			 JsonObject data_ = util.StringToJsonObject(Message);
			 
			 JsonObject header_ = data_.get("header").getAsJsonObject();
			 
			 String sourceMessageId = header_.get("messageId").getAsString();
			 
			 JsonObject header = new JsonObject();
			 
			 header.addProperty("messageId", util.Generate_Random_String(20));
			 header.addProperty("messageTimestamp", util.getCurrentDateTime());
			 header.addProperty("messageEvent", "Response");
			 header.addProperty("messageStatus", Result ? "Success" : "failed");
			 header.addProperty("source", "DATAVISION");
			 header.addProperty("sourceMessageId", sourceMessageId);
			 header.addProperty("sourceSystem", "ACBS");
			 
			 JsonObject response = new JsonObject();
			
			 JsonArray responseDescription = new JsonArray();
			 
			 response.addProperty("externalReferenceIdentifier", util.Generate_Random_String(20));
			 response.addProperty("responseCode", "ACK");
			 
			 responseDescription.add("PROCESSED");
			 
			 response.add("responseDescription", responseDescription);
			 
			 out.add("header", header);
			 out.add("response", response);
			 
			 String[] args = new String[] { "SOLACE", "SOL006", out.toString() };
			 
			 new TopicPublisher(Jdbctemplate).run(args);	 
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Response sent successfully");    
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
	
	public JsonObject Send_Solace_ErrorRespone(String Message, List<String> Errors) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject out = new JsonObject();
			 
			 JsonObject data_ = util.StringToJsonObject(Message);
			 
			 JsonObject header_ = data_.get("header").getAsJsonObject();
			 
			 String sourceMessageId = header_.get("messageId").getAsString();
			 
			 JsonObject header = new JsonObject();
			 
			 header.addProperty("messageId", util.Generate_Random_String(20));
			 header.addProperty("messageTimestamp", util.getCurrentDateTime());
			 header.addProperty("messageEvent", "Response");
			 header.addProperty("messageStatus", "Failure");
			 header.addProperty("source", "DATAVISION");
			 header.addProperty("sourceMessageId", sourceMessageId);
			 header.addProperty("sourceSystem", "ACBS");
			 
			 JsonObject response = new JsonObject();
			
			 JsonArray responseDescription = new Gson().toJsonTree(Errors).getAsJsonArray();
			 
			 response.addProperty("externalReferenceIdentifier", util.Generate_Random_String(20));
			 response.addProperty("responseCode", "NACK");
			 
			 responseDescription.add("PROCESSED");
			 
			 response.add("responseDescription", responseDescription);
			 
			 out.add("header", header);
			 out.add("response", response);
			 
			 String[] args = new String[] { "SOLACE", "SOL006", out.toString() };
			 
			 new TopicPublisher(Jdbctemplate).run(args);	 
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Response sent successfully");    
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
	
	public String getMemberasInt(JsonObject js, String Name) 
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
					val = js.get(Name).getAsInt()+"";
				}
			}
		}
		catch(Exception ex)
		{
			logger.debug("Exception while parsing the element "+Name+" from the Jsonobject "+js);
		}
		
		return val;
	}
	
	/*public String getMemberasDouble(JsonObject js, String Name) 
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
					val = js.get(Name).getAsDouble()+"";
				}
			}
		}
		catch(Exception ex)
		{
			logger.debug("Exception while parsing the element "+Name+" from the Jsonobject "+js);
		}
		
		return val;
	}*/

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
