package com.hdsoft.models;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.opencsv.CSVReader;
import com.hdsoft.Repositories.Lookup001;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_Finance implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_Finance(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptect_Finance() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_Finance.class);  //getBalanceInfo(String targetAccount, String FileType)
	
	@RequestMapping(value = {"/Datavision/finance/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestHeader("APICODE") String APICODE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
 	 	JsonObject details = new JsonObject();
		  
		try
		{
			 if(APICODE.equals("RTS003")) 
			 {
				 details = Equity_Investment("RTS003", "", ""); 
			 }
			 else if(APICODE.equals("RTS041")) 
			 {
				 details = Premises_Furniture_and_Equipment("RTS041", "", ""); 
			 }
			 else if(APICODE.equals("RTS043")) 
			 {
				 details = dividendsPayable("RTS043", "", ""); 
			 }
			 else if(APICODE.equals("RTS059")) 
			 {
				 details = accruedTaxes("RTS059", "", ""); 
			 }
			 else if(APICODE.equals("RTS045")) 
			 {
				 details = shareCapital("RTS045", "", ""); 
			 }
			 else if(APICODE.equals("RTS047")) 
			 {
				 details = otherCapitalAccount("RTS047", "", ""); 
			 }
			 else if(APICODE.equals("RTS007")) 
			 {
				 details = otherAsset("RTS007", "", ""); 
			 }
			 else if(APICODE.equals("RTS063")) 
			 {
				 details = unearnedIncome("RTS063", "", ""); 
			 }
			 else if(APICODE.equals("RTS073")) 
			 {
				 details = other_liablities("RTS073", "", ""); 
			 }
			 else if(APICODE.equals("RTS115")) 
			 {
				 details = incomeStatement("RTS115", "", ""); 
			 }
			 else if(APICODE.equals("RTS061")) 
			 {
				 details = subordinatedDebt("RTS061", "", ""); 
			 }
			 else if(APICODE.equals("RTS049")) 
			 {
				 details = coreCapitalDeductionsData("RTS049", "", ""); 
			 }
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	public JsonObject Equity_Investment(String INFO1, String INFO2, String INFO3) //RTS003
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String reportingDate = util.getCurrentReportDate();
			 String investeeName = "Standard Chartered Nominee Limited"; 
			 String investeeCountry = "218";
			 String ratingStatus = "false";
			 String obligorExternalCreditRating = "7";
			 String gradesUnratedBanks = "3";
			 String sectorSnaClassification = "8";
			 String relationship = "2";
			 String numberSharesPurchased= "100";
			 String equityInvestmentCategory = "1";
			 String currency = "834";
			 String orgPurchasePrice = "200000.00";
			 String usdPurchasePrice = "80.00";
			 String tzsPurchasePrice = "200000.00";
			 String orgPurchasedBookValueShares = "200000.00";
			 String usdPurchasedBookValueShares = "80.00";
			 String tzsPurchasedBookValueShares = "200000.00";
			 String orgPurchasedMarketValueShares = "200000.00";
			 String usdPurchasedMarketValueShares = "80.00";
			 String tzsPurchasedMarketValueShares = "200000.00";
			 String numberPaidUpEquityShares = "1";
			 String orgValuePaidUpEquityShares = "200000.00";
			 String usdValuePaidUpEquityShares = "80.00";
			 String tzsValuePaidUpEquityShares = "200000.00";
			 String tradingIntent = "2";
			 String allowanceProbableLoss = "0";
			 String botProvision = "0";
			 String assetClassificationCategory = "1";

			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					      "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32) values\r\n" + 
					      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "equityInvestmentData", "serial", "reportingDate", "investeeName", "investeeCountry", "ratingStatus", "obligorExternalCreditRating",
					"gradesUnratedBanks", "sectorSnaClassification", "relationship", "numberSharesPurchased", "equityInvestmentCategory", "currency", "orgPurchasePrice", "usdPurchasePrice", "tzsPurchasePrice",
					"orgPurchasedBookValueShares", "usdPurchasedBookValueShares", "tzsPurchasedBookValueShares", "orgPurchasedMarketValueShares", "usdPurchasedMarketValueShares", "tzsPurchasedMarketValueShares",
					"numberPaidUpEquityShares", "orgValuePaidUpEquityShares", "usdValuePaidUpEquityShares", "tzsValuePaidUpEquityShares", "tradingIntent", "allowanceProbableLoss", "botProvision", "assetClassificationCategory" });
			 			 			
			sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
	
			Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "equityInvestmentData"});
			 
			sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
				      "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32) values\r\n" + 
				       "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "equityInvestmentData", "1", reportingDate, investeeName, investeeCountry, ratingStatus, obligorExternalCreditRating,
				gradesUnratedBanks, sectorSnaClassification, relationship, numberSharesPurchased, equityInvestmentCategory, currency, orgPurchasePrice, usdPurchasePrice, tzsPurchasePrice,
				orgPurchasedBookValueShares, usdPurchasedBookValueShares, tzsPurchasedBookValueShares, orgPurchasedMarketValueShares, usdPurchasedMarketValueShares, tzsPurchasedMarketValueShares,
				numberPaidUpEquityShares, orgValuePaidUpEquityShares, usdValuePaidUpEquityShares, tzsValuePaidUpEquityShares, tradingIntent, allowanceProbableLoss, botProvision, assetClassificationCategory });
		 			 			 
			sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
	        Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "equityInvestmentData", INFO1, "1", "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });
			 
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
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", e.getLocalizedMessage());    
			 
			 logger.debug("Exception in Equity_Investment :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject Premises_Furniture_and_Equipment(String INFO1, String INFO2, String INFO3) //RTS041
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND001" }, new Lookup001_mapper());
					
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)  
			 {
				 Accounts.add(Info.get(i).getCOLUMN1());
				 Accounts.add(Info.get(i).getCOLUMN6());
			 }
			 
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();

			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 String DepAc = Info.get(i).getCOLUMN6();
				 
				 if(Balance_Sheet.has(Ac) && Balance_Sheet.has(DepAc))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String assetCategory = Info.get(i).getCOLUMN3();
					 String usagePremisesFurnitureEquipment = Info.get(i).getCOLUMN5();
					 String acquisitionDate = util.getCurrentReportDate();
					 String assetType = Info.get(i).getCOLUMN4();
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String usdAmount = "0";
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String disposalDate = util.getCurrentReportDate();  //Check with Team
					 String orgDepreciation = Balance_Sheet.get(DepAc).getAsJsonObject().get("Base_Amount").getAsString();
					 String usdDepreciation = "0";
					 String tzsDepreciation = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String orgAccumDepreciation = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String usdAccumDepreciation = "0";
					 String tzsAccumDepreciation = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String orgImpairmentAmount = "0";
					 String usdImpairmentAmount = "0";
					 String tzsImpairmentAmount = "0";
					 String orgNetBookValue = (Double.parseDouble(orgAmount) - Double.parseDouble(tzsDepreciation))+"";  //orgAmount - tzsDepreciation
					 String usdNetBookValue = "0"; 
					 String tzsNetBookValue = (Double.parseDouble(orgAmount) - Double.parseDouble(tzsDepreciation))+""; //orgAmount - tzsDepreciation
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25,COLUMN26) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 																						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "premisesFurnitureEquipment", count, reportingDate, assetCategory, usagePremisesFurnitureEquipment, acquisitionDate, assetType, 
							currency, orgAmount, usdAmount, tzsAmount, disposalDate, orgDepreciation, usdDepreciation, tzsDepreciation, orgAccumDepreciation,
							usdAccumDepreciation, tzsAccumDepreciation, orgImpairmentAmount, usdImpairmentAmount, tzsImpairmentAmount, orgNetBookValue, usdNetBookValue, tzsNetBookValue });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "premisesFurnitureEquipment"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25,COLUMN26) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "premisesFurnitureEquipment", "serial", "reportingDate", "assetCategory", "usagePremisesFurnitureEquipment", "acquisitionDate", "assetType", 
						"currency", "orgAmount", "usdAmount", "tzsAmount", "disposalDate", "orgDepreciation", "usdDepreciation", "tzsDepreciation", "orgAccumDepreciation",
						"usdAccumDepreciation", "tzsAccumDepreciation", "orgImpairmentAmount", "usdImpairmentAmount", "tzsImpairmentAmount", "orgNetBookValue", "usdNetBookValue", "tzsNetBookValue" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "premisesFurnitureEquipment", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject dividendsPayable(String INFO1, String INFO2, String INFO3) //RTS043
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND008" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
					 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String transactionDate = util.getCurrentReportDate();
					 String dividendType = "1";
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String beneficiaryName = "STANDARD CHARTERED HOLDING AFRICA BV"; //"STANDARD CHARTERED HOLDING(AFRICA)B.V";
					 String beneficiaryCountry = "218";
					 String beneficiaryAccNumber = "3582088442091";
					 String beneficiaryBankCode = "SCBLTZTXXXX";
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "dividendsPayableData", count, reportingDate, transactionDate, dividendType, currency, orgAmount, tzsAmount, beneficiaryName, beneficiaryCountry, beneficiaryAccNumber, beneficiaryBankCode });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "dividendsPayableData"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "dividendsPayableData", "serial", "reportingDate", "transactionDate", "dividendType", "currency", "orgAmount", "tzsAmount", "beneficiaryName", "beneficiaryCountry", "beneficiaryAccNumber", "beneficiaryBankCode" });
			
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Equity Data", "dividendsPayableData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject accruedTaxes(String INFO1, String INFO2, String INFO3) //RTS059
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND002" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
		 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String claimantName = "Tanzania Revenue Authority";
					 String payableCategory = Info.get(i).getCOLUMN4();
					 String transactionDate = util.getCurrentReportDate(); 
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String usdAmount = "0";
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String maturityDate = util.getCurrentReportDate();
					 String sectorSnaClassification = Info.get(i).getCOLUMN4();
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "accruedTaxes", count, reportingDate, claimantName, payableCategory, transactionDate, currency, orgAmount, 
					 usdAmount, tzsAmount, maturityDate, sectorSnaClassification });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "accruedTaxes"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "accruedTaxes", "serial", "reportingDate", "claimantName", "payableCategory", "transactionDate", "currency", "orgAmount", 
				 "usdAmount", "tzsAmount"," maturityDate", "sectorSnaClassification" });
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liabilities Data", "accruedTaxes", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject shareCapital(String INFO1, String INFO2, String INFO3) //RTS045
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND009" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
			 	 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String capitalCategory = Info.get(i).getCOLUMN2();
					 String capitalSubCategory = Info.get(i).getCOLUMN4();
					 String transactionDate = util.getCurrentReportDate(); 
					 String transactionType = "1";
					 String shareholderNames = "STANDARD CHARTERED HOLDING(AFRICA)B.V";
					 String clientType = "2";
					 String shareholderCountry = "158";
					 String numberOfShares = "46091995";
					 String sharePriceBookValue = "1000";
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String sectorSnaClassification = "8";
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "shareCapitalData", count, reportingDate, capitalCategory, capitalSubCategory, transactionDate, transactionType, shareholderNames, 
							 clientType, shareholderCountry, numberOfShares, sharePriceBookValue, currency, orgAmount, tzsAmount, sectorSnaClassification });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "shareCapitalData"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "shareCapitalData", "serial", "reportingDate", "capitalCategory", "capitalSubCategory", "transactionDate", "transactionType", "shareholderNames", 
							 "clientType", "shareholderCountry", "numberOfShares", "sharePriceBookValue", "currency", "orgAmount", "tzsAmount", "sectorSnaClassification" });
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Equity Data", "shareCapitalData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject otherCapitalAccount(String INFO1, String INFO2, String INFO3) //RTS047
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND003" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
			 	 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String transactionDate = util.getCurrentReportDate();
					 String transactionType = "1";
					 String reserveCategory = Info.get(i).getCOLUMN3();
					 String reserveSubCategory = Info.get(i).getCOLUMN5();
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "otherCapitalAccountData", count, reportingDate, transactionDate, transactionType, reserveCategory, reserveSubCategory,
							 currency, orgAmount, tzsAmount });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "otherCapitalAccountData"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "otherCapitalAccountData", "serial", "reportingDate", "transactionDate", "transactionType", "reserveCategory", "reserveSubCategory",
							 "currency", "orgAmount", "tzsAmount" });
					 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Equity Data", "otherCapitalAccountData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject otherAsset(String INFO1, String INFO2, String INFO3) //RTS007
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND004" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
	 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String assetType = Info.get(i).getCOLUMN4();
					 String assetTypeSubCategory = Info.get(i).getCOLUMN5();
					 String transactionDate = util.getCurrentReportDate();
					 String maturityDate = util.getCurrentReportDate();  // check with pmo
					 String debtorName = "NA"; // check with pmo
					 String debtorCountry = "218";
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String usdAmount = "0";
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String sectorSnaClassification = "12";
					 String pastDueDays = "0";
					 String assetClassificationCategory = "1";
					 String allowanceProbableLoss = "268101";
					 String botProvision = "0"; 
					
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "otherAssetData", count, reportingDate, assetType, assetTypeSubCategory, transactionDate, maturityDate,
							 debtorName, debtorCountry, currency, orgAmount, usdAmount, tzsAmount, sectorSnaClassification, pastDueDays, assetClassificationCategory, allowanceProbableLoss, botProvision });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "otherAssetData"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "otherAssetData", "serial", "reportingDate", "assetType", "assetTypeSubCategory", "transactionDate", "maturityDate",
							 "debtorName", "debtorCountry", "currency", "orgAmount", "usdAmount", "tzsAmount", "sectorSnaClassification", "pastDueDays", "assetClassificationCategory", "allowanceProbableLoss", "botProvision" });
					 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "otherAssetData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject unearnedIncome(String INFO1, String INFO2, String INFO3) //RTS063
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND007" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
			 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String unearnedIncomeType = Info.get(i).getCOLUMN3(); //text changed into lookup D178
					 String beneficiaryName = "Various clients";
					 String transactionDate = util.getCurrentReportDate();
					 String currency = "834"; 
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String usdAmount = "0";
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();;
					 String sectorSnaClassification = "12";
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "unearnedIncome", count, reportingDate, unearnedIncomeType, beneficiaryName, transactionDate, currency,
							 orgAmount, usdAmount, tzsAmount, sectorSnaClassification });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "unearnedIncome"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "unearnedIncome", "serial", "reportingDate", "unearnedIncomeType", "beneficiaryName", "transactionDate", "currency",
							 "orgAmount", "usdAmount", "tzsAmount", "sectorSnaClassification" });
					 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liabilities Data", "unearnedIncome", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject other_liablities(String INFO1, String INFO2, String INFO3) //RTS073
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND006" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
			 	 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String liabilityCategory = Info.get(i).getCOLUMN4();
					 String counterpartyName = "Various clients";
					 String counterpartyCountry = "218";
					 String transactionDate = util.getCurrentReportDate();  //check with pmo
					 String valueDate = util.getCurrentReportDate();  //check with pmo
					 String maturityDate = util.getCurrentReportDate(); ;  //check with pmo
					 String currency = "834";
					 String orgAmountOpening = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString(); //check with pmo
					 String usdAmountOpening = "0";
					 String tzsAmountOpening = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString(); //check with pmo
					 String orgAmountPayment = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString(); //check with pmo
					 String usdAmountPayment = "0";
					 String tzsAmountPayment = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString(); //check with pmo
					 String orgAmountBalance = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString(); //check with pmo
					 String usdAmountBalance = "0";
					 String tzsAmountBalance = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString(); //check with pmo
					 String sectorSnaClassification = "12";
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "otherLiabilities", count, reportingDate, liabilityCategory, counterpartyName, counterpartyCountry, transactionDate,
							 valueDate, maturityDate, currency, orgAmountOpening,usdAmountOpening, tzsAmountOpening, orgAmountPayment, usdAmountPayment, tzsAmountPayment, orgAmountBalance, usdAmountBalance, tzsAmountBalance, sectorSnaClassification });
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "otherLiabilities"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10,COLUMN11,COLUMN12,COLUMN13,COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "otherLiabilities", "serial", "reportingDate", "liabilityCategory", "counterpartyName", "counterpartyCountry", "transactionDate",
						"valueDate", "maturityDate", "currency", "orgAmountOpening", "usdAmountOpening", "tzsAmountOpening", "orgAmountPayment", "usdAmountPayment", "tzsAmountPayment", "orgAmountBalance", "usdAmountBalance", "tzsAmountBalance", "sectorSnaClassification" });
					 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liabilities Data", "otherLiabilities", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
	
	public JsonObject incomeStatement(String INFO1, String INFO2, String INFO3) //RTS115
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 JsonObject Balance_Info = getIncomeBalanceSheetInfo(INFO3);  
			 
			 if(!Balance_Info.has("Balance_Sheet2"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet2").getAsJsonObject();
				 
			 String sql = "select distinct(COLUMN4) from lookup001 where COLUMN11=? and COLUMN12=?";  
			 
			 List<String> Types = Jdbctemplate.queryForList(sql, new Object[] { INFO1, "VND015" }, String.class);
			 
			 if(Types.size() > 0)
			 {
				 String reportingDate = util.getCurrentReportDate();
				 String interestIncome = "0";
				 String interestIncomeValue = "0";
				 String interestExpense = "0";
				 String interestExpensesValue = "0";
				 String badDebtsWrittenOffNotProvided = "0";
				 String provisionBadDoubtfulDebts = "0";
				 String impairmentsInvestments = "0";
				 String incomeTaxProvision = "0";
				 String extraordinaryCreditsCharge = "0";
				 String nonCoreCreditsCharges = "1";
				 String nonCoreCreditsChargesValue = "0";
				 String nonInterestIncome = "0";
				 String nonInterestIncomeValue = "0";
				 String nonInterestExpenses = "0";
				 String nonInterestExpensesValue = "0";
				 
				 for(String TYPE : Types)
				 {
					 sql = "select * from lookup001 where COLUMN4=? and COLUMN11=? and COLUMN12=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { TYPE, INFO1, "VND015" }, new Lookup001_mapper());
					 
					 Double val = 0.0; String Lov = "1";
					 
					 for(int i=0; i<Info.size(); i++)
					 {
						  String Balance = getAmount(Balance_Sheet, Info.get(i).getCOLUMN1(), Info.get(i).getCOLUMN3());
						  
						  System.out.println("Balance :: "+Balance);
						  
						  try {
							  val = val + Double.parseDouble(Balance);
							  Lov = Info.get(i).getCOLUMN5();
						  } catch (Exception ex) {}
					 }
					 
					 if(TYPE.equals("interestIncome"))   
					 {
						 interestIncome = Lov;
						 interestIncomeValue = val+"";
					 }
					 
					 if(TYPE.equals("interestExpenses"))   
					 {
						 interestExpense = Lov;
						 interestExpensesValue = val+"";
					 }
					 
					 if(TYPE.equals("badDebtsWrittenOffNotProvided"))   
					 {
						 badDebtsWrittenOffNotProvided = val+"";
					 }
					 
					 if(TYPE.equals("provisionBadDoubtfulDebts"))   
					 {
						 provisionBadDoubtfulDebts = val+"";
					 }
					 
					 if(TYPE.equals("incomeTaxProvision"))   
					 {
						 incomeTaxProvision = val+"";
					 } 
					 
					 if(TYPE.equals("nonInterestIncome"))   
					 {
						 nonInterestIncome = Lov;
						 nonInterestIncomeValue = val+"";
					 }  
					 
					 if(TYPE.equals("nonInterestExpenses"))   
					 {
						 nonInterestExpenses = Lov;
						 nonInterestExpensesValue = val+"";
					 }
				 }
				 
				 int count = 1;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7, column8, column9, column10) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "incomeStatementData", count, reportingDate, badDebtsWrittenOffNotProvided, provisionBadDoubtfulDebts, impairmentsInvestments, incomeTaxProvision, extraordinaryCreditsCharge });
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES( ?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "interestIncomeList", count, reportingDate, interestIncome , interestIncomeValue});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "interestExpenseList", count, reportingDate, interestExpense , interestExpensesValue});

				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES( ?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "nonCoreCreditsChargesList", count, reportingDate, nonCoreCreditsCharges , nonCoreCreditsChargesValue});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "nonInterestIncomeList", count, reportingDate, nonInterestIncome , nonInterestIncomeValue});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "nonInterestExpensesList", count, reportingDate, nonInterestExpenses , nonInterestExpensesValue});
				 
				 if(count > 0)
				 {
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7, column8, column9, column10) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "incomeStatementData", "serial", "reportingDate", "badDebtsWrittenOffNotProvided", "provisionBadDoubtfulDebts", "impairmentsInvestments", "incomeTaxProvision", "extraordinaryCreditsCharge" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES( ?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "interestIncomeList", "serial", "reportingDate", "interestIncome" , "interestIncomeValue"});
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "interestExpenseList", "serial", "reportingDate", "interestExpense" , "interestExpensesValue"});

					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "nonCoreCreditsChargesList", "serial", "reportingDate", "nonCoreCreditsCharges" , "nonCoreCreditsChargesValue" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "nonInterestIncomeList", "serial", "reportingDate", "nonInterestIncome" , "nonInterestIncomeValue"});
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7) VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?)"; 

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "nonInterestExpensesList", "serial", "reportingDate", "nonInterestExpenses" , "nonInterestExpensesValue" });

					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "incomeStatementData", "1" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "interestIncomeList", "2" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "interestExpenseList", "3" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "nonCoreCreditsChargesList", "4" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "nonInterestIncomeList", "5" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "nonInterestExpensesList", "6" });
					 
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					 
				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "profitandloss", "incomeStatementData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
				     
				     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
				}	
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
	
	public JsonObject subordinatedDebt(String INFO1, String INFO2, String INFO3) //RTS061
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND010" }, new Lookup001_mapper());
			 
			 JsonObject Balance_Info = getBalanceSheetInfo(INFO3);  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet2").getAsJsonObject();
			 				 
			 int count = 0;
			 
			 String Main_account = "", AccruedInterestAc = "", RepaymentAc = "", AccruedInterestOutstandingAc = "";
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 if(Info.get(i).getCOLUMN2().equals("Main-Account"))  Main_account = Info.get(i).getCOLUMN1();
				 if(Info.get(i).getCOLUMN2().equals("AccruedInterest"))  AccruedInterestAc = Info.get(i).getCOLUMN1();
				 if(Info.get(i).getCOLUMN2().equals("Repayment"))  RepaymentAc = Info.get(i).getCOLUMN1();
				 if(Info.get(i).getCOLUMN2().equals("AccruedInterestOutstanding"))  AccruedInterestOutstandingAc = Info.get(i).getCOLUMN1();
			 }
			 
			 if(Info.size()>0) 
			 {
				 String reportingDate = util.getCurrentReportDate();
				 String lenderName = "SCB Holding Limited";
				 String country = "73";
				 String sectorSnaClassification = "8";
				 String lenderRelationship = "9";
				 String borrowingPurposes = "2";
				 String currency = "834";
				 String orgAmount = getAmount(Balance_Sheet, Main_account, "TZS");//BALANCE balance from account no 205901
				 String usdAmount = getAmount(Balance_Sheet, Main_account, "USD");//non mandatory
				 String tzsAmount = orgAmount;//BALANCE balance from account no 205901
				 String annualInterestRate = "10.45";//change every 6 month
				 String amortizationType = "1";
				 String interestPricingMethod = "2";
				 String loanContractDate = "010620220000";
				 String loanValueDate = "010620220000";
				 String maturityDate = "010620320000";
				 String transactionDate = "010620320000";
				 String orgAccruedInterestAmount = getAmount(Balance_Sheet, AccruedInterestAc, "TZS"); //pick balance from ac no.491521
				 String usdAccruedInterestAmount = getAmount(Balance_Sheet, AccruedInterestAc, "USD"); //pick balance from ac no.491521
				 String tzsAccruedInterestAmount = orgAccruedInterestAmount;  //pick balance from ac no.491521
				 String orgAmountRepayment = getAmount(Balance_Sheet, RepaymentAc, "TZS"); //BALANCE balance from account no 205901
				 String usdAmountRepayment = getAmount(Balance_Sheet, RepaymentAc, "USD");
				 String tzsAmountRepayment = orgAmountRepayment;//BALANCE balance from account no 205901
				 String orgAccruedInterestOutstandingAmount = getAmount(Balance_Sheet, AccruedInterestOutstandingAc, "TZS"); //pick balance from ac no.262517
				 String usdAccruedInterestOutstandingAmount = getAmount(Balance_Sheet, AccruedInterestOutstandingAc, "USD");//pick balance from ac no.262517
				 String tzsAccruedInterestOutstandingAmount = orgAccruedInterestOutstandingAmount; //pick balance from ac no.262517
				 String orgPrincipalAmountClosing = orgAmount;//BALANCE balance from account no 205901
				 String usdPrincipalAmountClosing = usdAmount;//BALANCE balance from account no 205901
				 String tzsPrincipalAmountClosing = tzsAmount;//BALANCE balance from account no 205901
			
				 count++;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32, COLUMN33) "
				            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

				Jdbctemplate.update(sql, new Object[] {
				    SUBORGCODE, O_SERIAL, "D", INFO1, "subordinatedDebt", count, reportingDate, lenderName, country,
				    sectorSnaClassification, lenderRelationship, borrowingPurposes, currency, orgAmount, usdAmount,
				    tzsAmount, annualInterestRate, amortizationType, interestPricingMethod, loanContractDate,
				    loanValueDate, maturityDate, transactionDate, orgAccruedInterestAmount, usdAccruedInterestAmount,
				    tzsAccruedInterestAmount, orgAmountRepayment, usdAmountRepayment, tzsAmountRepayment,
				    orgAccruedInterestOutstandingAmount, usdAccruedInterestOutstandingAmount,
				    tzsAccruedInterestOutstandingAmount, orgPrincipalAmountClosing, usdPrincipalAmountClosing,
				    tzsPrincipalAmountClosing
				});

			 }
			 
			 System.out.println("check 1");
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "subordinatedDebt"});
				 
				 System.out.println("check 2");
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32, COLUMN33) "
				            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "subordinatedDebt", "serial", "reportingDate", "lenderName", "country", "sectorSnaClassification", "lenderRelationship", "borrowingPurposes", "currency", "orgAmount", "usdAmount", "tzsAmount", "annualInterestRate", "amortizationType", "interestPricingMethod", "loanContractDate", "loanValueDate", "maturityDate", "transactionDate", "orgAccruedInterestAmount", "usdAccruedInterestAmount", "tzsAccruedInterestAmount", "orgAmountRepayment", "usdAmountRepayment", "tzsAmountRepayment", "orgAccruedInterestOutstandingAmount", "usdAccruedInterestOutstandingAmount", "tzsAccruedInterestOutstandingAmount", "orgPrincipalAmountClosing", "usdPrincipalAmountClosing", "tzsPrincipalAmountClosing" });
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liabilities", "subordinatedDebt", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
			 
			 logger.debug("Exception in subordinatedDebt :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject coreCapitalDeductionsData(String INFO1, String INFO2, String INFO3) //RTS049
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 String sql = "select * from lookup001 where COLUMN8=? and COLUMN7=?";  
			 
			 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "VND011" }, new Lookup001_mapper());
			 
			 List<String> Accounts = new ArrayList<String>();
			 
			 for(int i=0; i<Info.size(); i++)   Accounts.add(Info.get(i).getCOLUMN1());
			
			 JsonObject Balance_Info = getBalanceInfo(Accounts, "BS");  
			 
			 if(!Balance_Info.has("Balance_Sheet"))
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP12");
				 details.addProperty("message", "Balance sheet Information not found");    
			 
				 return details;
			 }
			 
			 JsonObject Balance_Sheet = Balance_Info.get("Balance_Sheet").getAsJsonObject();
			 	 
			 int count = 0;
			 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String Ac = Info.get(i).getCOLUMN1();
				 
				 if(Balance_Sheet.has(Ac))
				 {
					 String reportingDate = util.getCurrentReportDate();
					 String transactionDate = util.getCurrentReportDate();
					 String deductionsType = Info.get(i).getCOLUMN2(); //need confirmaion(3 & 5)
					 String currency = "834";
					 String orgAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();
					 String tzsAmount = Balance_Sheet.get(Ac).getAsJsonObject().get("Base_Amount").getAsString();

					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10) "
						      + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "coreCapitalDeductionsData", count, reportingDate, transactionDate, deductionsType, currency, orgAmount, tzsAmount});
				 }
			 }
			 
			 if(count > 0)
			 {
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "coreCapitalDeductionsData"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10) "
					      + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "coreCapitalDeductionsData", "serial","reportingDate", "transactionDate", "deductionsType", "currency", "orgAmount", "tzsAmount"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Equity", "coreCapitalDeductionsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Finance" });	 
			     
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
			 
			 logger.debug("Exception in coreCapitalDeductionsData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public String getAmount(JsonObject Js, String account, String Currency) 
	{ 
		String out = "0";
		
		try
		{
			if(Js.has(account + "|" +Currency))
			{
				JsonObject obj = Js.get(account + "|" +Currency).getAsJsonObject();
				
				out = obj.get("Base_Amount").getAsString();
			}
		}
		catch(Exception ex)
		{
			 logger.debug("Exception in getAmount :::: "+ex.getLocalizedMessage());
		}
	
		return out;
	}
	
	/*public JsonObject getBalanceSheetInfo(String FilePath)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype and u.STATUS=? and u.RESCODE=? and u.RESPDESC=?)";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "PSGL", "SUCCESS", "200", "FILE DOWNLOADED SUCCESSFULLY"}, String.class);
			 
			 if(Information.size() != 0)
			 {
				 FilePath = Information.get(0);
			 }
			 
			 logger.debug(">>>>>> Balance sheet FilePath is "+FilePath+" <<<<<<<<");
			 
			 File file = new File(FilePath); 
			 
			 JsonObject Info = new JsonObject();
			 
			 if(file.exists() && !file.isDirectory()) 
			 { 
				 List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 20 && !util.isNullOrEmpty(values[3])) 
					    	{
						    	 records.add(Arrays.asList(values));
					    	}
					    }
					}
					
					for(int i=1; i<records.size(); i++)
					{
						JsonObject Js = new JsonObject();
			  			  
					    Js.addProperty("Source", records.get(i).get(0).trim());
					    Js.addProperty("Unit", records.get(i).get(1).trim());
					    Js.addProperty("Ledger", records.get(i).get(2).trim());
					    Js.addProperty("Account", records.get(i).get(3).trim());
					    Js.addProperty("Alt_Acct", records.get(i).get(4).trim());
					    Js.addProperty("Deptid", records.get(i).get(6).trim());
					    Js.addProperty("Oper_Unit", records.get(i).get(7).trim());
					    Js.addProperty("Currency", records.get(i).get(11).trim());
					    Js.addProperty("Base_Curr", records.get(i).get(13).trim());
					    Js.addProperty("Year", records.get(i).get(14).trim());
					    Js.addProperty("Period", records.get(i).get(15).trim());
					    Js.addProperty("Base_Amount", records.get(i).get(16).trim().replaceAll(",", ""));
					    Js.addProperty("Tran_Amount", records.get(i).get(17).trim().replaceAll(",", ""));
					    Js.addProperty("Last_Upd_DtTm", records.get(i).get(19).trim());
					  
					    Info.add(records.get(i).get(3).trim(), Js);
					}
					
					details.add("Balance_Sheet", Info); 
					
					if(Info.size() == 0)
					{
						logger.debug(">>>>>> Balance sheet file content is empty or not able to retrieve records <<<<<<<<");
					}
					else
					{
						logger.debug("Balance_Sheet object content >>> "+details);
					}
			 }
			 else
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "Balance sheet file is not accessible");
				 
				 logger.debug(">>>>>> Balance sheet file is not accessible <<<<<<<<");
				 
				 return details;
			 }
				
			details.addProperty("Result", Info.size() != 0 ? "Success" : "Failed");
			details.addProperty("Message", Info.size() != 0 ? "Balance Sheet Info Found" : "Balance Sheet Info Not Found");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in getBalanceSheetInfo :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	*/
	
	public JsonObject getBalanceSheetInfo(String FilePath)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and DSTPATH like ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype and u.DSTPATH = c.DSTPATH and u.STATUS=? and u.RESCODE=? and u.RESPDESC=?) order by REQTIME desc";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "PSGL",  "%BS%", "SUCCESS", "200", "FILE DOWNLOADED SUCCESSFULLY"}, String.class);
			 
			 if(Information.size() != 0)
			 {
				 FilePath = Information.get(0);
			 }
			 
			 logger.debug(">>>>>> BS - Balance sheet FilePath is "+FilePath+" <<<<<<<<");
			 
			 File file = new File(FilePath); 
			 
			 JsonObject Info = new JsonObject();  JsonObject Info2 = new JsonObject();
			 
			 if(file.exists() && !file.isDirectory()) 
			 { 
				 List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 22 && !util.isNullOrEmpty(values[3])) 
					    	{
						    	 records.add(Arrays.asList(values));
					    	}
					    }
					}
					
					for(int i=1; i<records.size(); i++)
					{
						JsonObject Js = new JsonObject();
			  			  
					    Js.addProperty("Source", records.get(i).get(0).trim());
					    Js.addProperty("Unit", records.get(i).get(1).trim());
					    Js.addProperty("Ledger", records.get(i).get(2).trim());
					    Js.addProperty("Account", records.get(i).get(3).trim());
					    Js.addProperty("Alt_Acct", records.get(i).get(4).trim());
					    Js.addProperty("Deptid", records.get(i).get(6).trim());
					    Js.addProperty("Oper_Unit", records.get(i).get(7).trim());
					    Js.addProperty("Currency", records.get(i).get(11).trim());
					    Js.addProperty("Base_Curr", records.get(i).get(13).trim());
					    Js.addProperty("Year", records.get(i).get(14).trim());
					    Js.addProperty("Period", records.get(i).get(15).trim());  
					    Js.addProperty("BASE_DRCR_INDICATOR", records.get(i).get(16).trim()); 
					    Js.addProperty("Base_Amount", records.get(i).get(17).trim().replaceAll(",", ""));
					    Js.addProperty("TRAN DRCR INDICATOR", records.get(i).get(18).trim());     
					    Js.addProperty("Tran_Amount", records.get(i).get(19).trim().replaceAll(",", ""));
					    Js.addProperty("Last_Upd_DtTm", records.get(i).get(21).trim());
					  
					    Info.add(records.get(i).get(3).trim(), Js);
					    Info2.add(records.get(i).get(3).trim() +"|"+ records.get(i).get(11).trim(), Js);
					}
					
					details.add("Balance_Sheet", Info); 
					details.add("Balance_Sheet2", Info2); 
					
					if(Info.size() == 0)
					{
						logger.debug(">>>>>> BS- Balance sheet file content is empty or not able to retrieve records <<<<<<<<");
					}
					else
					{
						logger.debug("BS - Balance_Sheet object content >>> "+details);
					}
			 }
			 else
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "Balance sheet file is not accessible");
				 
				 logger.debug(">>>>>> BS - Balance sheet file is not accessible <<<<<<<<");
				 
				 return details;
			 }
				
			details.addProperty("Result", Info.size() != 0 ? "Success" : "Failed");
			details.addProperty("Message", Info.size() != 0 ? "Balance Sheet Info Found" : "Balance Sheet Info Not Found");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in BS- getBalanceSheetInfo :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject getIncomeBalanceSheetInfo(String FilePath)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and DSTPATH like ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype and u.DSTPATH = c.DSTPATH and u.STATUS=? and u.RESCODE=? and u.RESPDESC=?) order by REQTIME desc";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "PSGL",  "%PL%", "SUCCESS", "200", "FILE DOWNLOADED SUCCESSFULLY"}, String.class);
			 
			 if(Information.size() != 0)
			 {
				 FilePath = Information.get(0);
			 }
			 
			 logger.debug(">>>>>> PL - Balance sheet FilePath is "+FilePath+" <<<<<<<<");
			 
			 File file = new File(FilePath); 
			 
			 JsonObject Info = new JsonObject(); JsonObject Info2 = new JsonObject();
			 
			 //Source,Unit,Ledger,Account,Alt Acct,Product,Deptid,Oper Unit,C/Class,Project ID,Affiliate,Currency,Stat,Base Curr,Year,Period,DR/CR,Base Amount,DR/CR,Trans Amount,Instance,Last Upd DtTm

			 if(file.exists() && !file.isDirectory()) 
			 { 
				 List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 22 && !util.isNullOrEmpty(values[3])) 
					    	{
						    	 records.add(Arrays.asList(values));
					    	}
					    }
					}
					
					for(int i=1; i<records.size(); i++)
					{
						JsonObject Js = new JsonObject();
			  			  
						Js.addProperty("Source", records.get(i).get(0).trim());
					    Js.addProperty("Unit", records.get(i).get(1).trim());
					    Js.addProperty("Ledger", records.get(i).get(2).trim());
					    Js.addProperty("Account", records.get(i).get(3).trim());
					    Js.addProperty("Alt_Acct", records.get(i).get(4).trim());
					    Js.addProperty("Deptid", records.get(i).get(6).trim());
					    Js.addProperty("Oper_Unit", records.get(i).get(7).trim());
					    Js.addProperty("Currency", records.get(i).get(11).trim());
					    Js.addProperty("Base_Curr", records.get(i).get(13).trim());
					    Js.addProperty("Year", records.get(i).get(14).trim());
					    Js.addProperty("Period", records.get(i).get(15).trim());  
					    Js.addProperty("BASE_DRCR_INDICATOR", records.get(i).get(16).trim()); 
					    Js.addProperty("Base_Amount", records.get(i).get(17).trim().replaceAll(",", ""));
					    Js.addProperty("TRAN DRCR INDICATOR", records.get(i).get(18).trim());     
					    Js.addProperty("Tran_Amount", records.get(i).get(19).trim().replaceAll(",", ""));
					    Js.addProperty("Last_Upd_DtTm", records.get(i).get(21).trim());
					  
					    Info.add(records.get(i).get(3).trim(), Js);
					    Info2.add(records.get(i).get(3).trim() +"|"+ records.get(i).get(11).trim(), Js);
					}
					
					details.add("Balance_Sheet", Info); 
					details.add("Balance_Sheet2", Info2);  
					
					if(Info.size() == 0)
					{
						logger.debug(">>>>>> PL- Balance sheet file content is empty or not able to retrieve records <<<<<<<<");
					}
					else
					{
						logger.debug("PL - Balance_Sheet object content >>> "+details);
					}
			 }
			 else
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "Balance sheet file is not accessible");
				 
				 logger.debug(">>>>>> PL - Balance sheet file is not accessible <<<<<<<<");
				 
				 return details;
			 }
				
			details.addProperty("Result", Info.size() != 0 ? "Success" : "Failed");
			details.addProperty("Message", Info.size() != 0 ? "Balance Sheet Info Found" : "Balance Sheet Info Not Found");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in PL - getBalanceSheetInfo :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject getBalanceInfo(List<String> targetAccounts, String FileType) // consolidated balance
	{
		 JsonObject details = new JsonObject(); 
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and DSTPATH like ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype and u.DSTPATH = c.DSTPATH and u.STATUS=? and u.RESCODE=? and u.RESPDESC=?) order by REQTIME desc";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "PSGL", FileType.equals("BS") ? "%BS%" : "%PL%", "SUCCESS", "200", "FILE DOWNLOADED SUCCESSFULLY"}, String.class);
			 
			 String FilePath = Information.size() != 0 ? Information.get(0) : "";
			 
			 logger.debug(">>>>>> "+FileType+" - Balance sheet FilePath is "+FilePath+" <<<<<<<<");
			 
			 File file = new File(FilePath); 
			 
			 JsonObject Info = new JsonObject(); 
			
		     for(String targetAccount : targetAccounts)
		     {
		    	 if(util.isNullOrEmpty(targetAccount)) continue;
		    	 
		    	 if(file.exists() && !file.isDirectory()) 
				 { 
					    boolean isHeader = true; boolean Avl = false;  double totalDebit = 0; double totalCredit = 0;
					 
						try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
						{
						    String[] values = null;
						    
						    while ((values = csvReaderr.readNext()) != null)
						    {
						    	if(values.length == 22 && !util.isNullOrEmpty(values[3])) 
						    	{
						    		if (isHeader) 
						    		{ 
					                    isHeader = false;
					                    continue;
					                }
						    		
						    		 String account = values[3]; 
						             String flag = values[18];  
						             String bal = !util.isNullOrEmpty(values[17]) ? values[17] : "0";
						    		
						             double balance = Double.parseDouble(bal);
						             
						             if(account.equals(targetAccount)) 
						             {
						                    if (flag.equalsIgnoreCase("DR")) 
						                    {
						                        totalDebit += balance;
						                    } 
						                    else if (flag.equalsIgnoreCase("CR")) 
						                    {
						                        totalCredit += balance;
						                    }
						                    
						                    Avl = true;
						             }
						    	}
						    }
						}
						
						if(Avl) 
						{
							double netBalance = totalDebit - totalCredit;
							
							netBalance = Math.abs(netBalance);
							
							JsonObject Js = new JsonObject();
				  			  
						    Js.addProperty("Account", targetAccount);
						    Js.addProperty("totalDebit", util.TwoDecimals(totalDebit+"")); 
						    Js.addProperty("totalCredit", util.TwoDecimals(totalCredit+"")); 
						    Js.addProperty("Base_Amount", util.TwoDecimals(netBalance+""));
						    
						    Info.add(targetAccount, Js);  
						}		
				 }
				 else
				 {
					 details.addProperty("Result", "Failed");
					 details.addProperty("Message", "Balance sheet file is not accessible");
					 
					 logger.debug(">>>>>> "+FileType+" - Balance sheet file is not accessible <<<<<<<<");
					 
					 return details;
				 }
		     }
		     
		     details.add("Balance_Sheet", Info); 
			 	
			details.addProperty("Result", Info.size() != 0 ? "Success" : "Failed");
			details.addProperty("Message", Info.size() != 0 ? "Balance Sheet Info Found" : "Balance Sheet Info Not Found");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in "+FileType+"- getBalanceSheetInfo :::: "+e.getLocalizedMessage());
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
