package com.hdsoft.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.poi.ss.usermodel.*;

import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.Repositories.Lookup001;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_Trade_DAY0 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_Trade_DAY0(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptect_Trade_DAY0() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_Trade_DAY0.class);
	
	@RequestMapping(value = {"/Datavision/Day0Trade/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
   	 	
   	    Common_Utils util = new Common_Utils();
   	 
   	 	JsonObject data = util.StringToJsonObject(MESSAGE);
   	 	
   	 	String Apicode = data.get("Apicode").getAsString();
   	    String FilePath = data.get("FilePath").getAsString();
   	 	
   	    if(Apicode.equals("RTS065")) details = Outstanding_Acceptance_DAY0(Apicode, "", FilePath);
   	    if(Apicode.equals("RTS029")) details = Customerliabilities_DAY0(Apicode, "", FilePath);
   	    if(Apicode.equals("RTS077")) details = ExportLetterOfCredit_DAY0(Apicode, "", FilePath);
   	    if(Apicode.equals("RTS081")) details = inwardBills_DAY0(Apicode, "", FilePath);
   	    if(Apicode.equals("RTS075")) details = Outstanding_Guarantee_DAY0(Apicode, "", FilePath);
   	    if(Apicode.equals("RTS083")) details = outwardBills_DAY0(Apicode, "", FilePath);  
   	    if(Apicode.equals("RTS079")) details = OustandingLetterCredit_DAY0(Apicode, "", FilePath);  
   	 
   	    if(Apicode.equals("RTS019") && FilePath.toUpperCase().contains("OTP")) details = LoanInformation_OTP_DAY0(Apicode, "", FilePath);
   	    if(Apicode.equals("RTS019") && !FilePath.toUpperCase().contains("OTP")) details = LoanInformation_DTP_DAY0(Apicode, "", FilePath);
   	 
   	 	return details.toString();
    }
	
	public JsonObject Outstanding_Acceptance_DAY0(String INFO1, String INFO2, String INFO3) //RTS065 - 
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
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 int count = 0;
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
		         Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
					workbook = new XSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
					workbook = new HSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				  }
				 else 
				 {
					throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
                 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                 	  Row row = sheet.getRow(rowIndex);
                 	                   	   
                 	  String LEID = getCellValueAsString(row.getCell(3));
                 	  
     				 String reportingDate = getCellValueAsString(row.getCell(0));
     				 String acceptanceType = getCellValueAsString(row.getCell(4));
     				 String beneficiaryName = getCellValueAsString(row.getCell(5));
     				 String transactionDate = getCellValueAsString(row.getCell(6));
     				 String currency = getCellValueAsString(row.getCell(7));
     				 String orgAmount = getCellValueAsString(row.getCell(8));
     				 String usdAmount = "0";  
     				 String tzsAmount = getCellValueAsString(row.getCell(10));
     				 String sectorSnaClassification =  ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID);
     				 
     				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
     				 
     				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
     				 
     				 usdAmount = rates.get("usd").getAsString();
     				 tzsAmount = rates.get("tzs").getAsString();
     				 
     				 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
     				 
     				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
     						
     				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
                 	  
     				
      				
     				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     			 
     				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingAcceptances", count, reportingDate, acceptanceType, beneficiaryName, transactionDate, currency, 
     						 orgAmount, usdAmount, tzsAmount, sectorSnaClassification });   				 
                
                 

     				count++;
     			 }
			
     		     				if(count > 0)
     		     				 {
			 
				  String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13) values\r\n" + 
 						 "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 			 
 				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingAcceptances", "serial", "reportingDate", "acceptanceType", "beneficiaryName", "transactionDate", "currency", 
 						"orgAmount", "usdAmount", "tzsAmount", "sectorSnaClassification"});
 				 
 				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
 					
 				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingAcceptances"});
 				 
 				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
 				 
 			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liability", "outstandingAcceptances", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
 			 		 
 			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
 			     
 		         details.addProperty("Serial", O_SERIAL);
 		         details.addProperty("Batch_id", Batch_id);
 		         
			 }
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
			 
			 logger.debug("Exception in Outstanding_Acceptance_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	//ABITHA = 12-11-2024
	//------------------------------------------------------------------------------------------
	
	public JsonObject Customerliabilities_DAY0(String INFO1, String INFO2, String INFO3) //RTS029 - 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
		         Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
					workbook = new XSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
					workbook = new HSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				  }
				 else 
				 {
					throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
                 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                	 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
        			 
                 	  Row row = sheet.getRow(rowIndex);
                 	            
                 	 int count = 1;
                 	 
                 	 String LEID = getCellValueAsString(row.getCell(3));
                 	  
                 	 Sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
        			 
        			 List<Lookup001> Info = Jdbctemplate.query(Sql, new Object[] { LEID, "TradeLMTID" }, new Lookup001_mapper());
        			 
        			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
                 	  
        			 String reportingDate = getCellValueAsString(row.getCell(0));
        			 String draftHolder = getCellValueAsString(row.getCell(4));
        			 String transactionDate = getCellValueAsString(row.getCell(5));
        			 String valueDate = getCellValueAsString(row.getCell(6));
        			 String maturityDate = getCellValueAsString(row.getCell(7));
        			 String currency = getCellValueAsString(row.getCell(8));
        			 String orgAmount = getCellValueAsString(row.getCell(9));
        			 String usdAmount = "0";  
        			 String tzsAmount = getCellValueAsString(row.getCell(11));
        			 String pastDueDays = util.isNullOrEmpty(getCellValueAsString(row.getCell(14))) ? "0" : getCellValueAsString(row.getCell(14));
        			 String assetClassificationCategory = "1";
        			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID);
        			 String allowanceProbableLoss = "0";
        			 String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", LEID+"|"+LimitID);
        			 String botProvision = "0";
                  				 
     				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
     				 
     				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
     				 
     				usdAmount = rates.get("usd").getAsString();
     				tzsAmount = rates.get("tzs").getAsString();
     				 
     				 Sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
     				 
     				 List<Lookup001> Info1 = Jdbctemplate.query(Sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
     						
     				 currency = Info1.size() > 0 ? !util.isNullOrEmpty(Info1.get(0).getCOLUMN1()) ? Info1.get(0).getCOLUMN1() : "834" : "834";
                 	  
    				 
     				 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
     						   "COLUMN17,COLUMN18) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     			 
     				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "customerLiabilities", count, reportingDate, draftHolder, transactionDate, valueDate, maturityDate, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, botProvision, assetClassificationCategory, sectorSnaClassification, allowanceProbableLoss});
     				 
     				 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?)";
     			 
     				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", count, reportingDate, draftHolder, transactionDate, collateralPledged });
     				 
     				 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
     						   "COLUMN17,COLUMN18) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     				 
     		
     					
     				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL,"C", INFO1, "customerLiabilities", "count", "reportingDate", "draftHolder", "transactionDate", "valueDate", "maturityDate", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "botProvision", "assetClassificationCategory", "sectorSnaClassification", "allowanceProbableLoss"});
     				 
     				 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?)";
     			 
     				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL,"C", INFO1, "collateralPledged", "count", "reportingDate", "draftHolder", "transactionDate", "collateralPledged"});
     				 
     				 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
     					
     				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "customerLiabilities","1"});
     				 
     				 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
     					
     				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged","2"});
     				 
     				 //-----------------
     				 
     				 Sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
     				 
     				 Jdbctemplate.update(Sql, new Object[] { Batch_id, SUBORGCODE, "Asset", "customerLiabilities", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
     					 
     				 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
     						
     		         details.addProperty("Serial", O_SERIAL);
     		         details.addProperty("Batch_id", Batch_id);
     				 }
     				
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
     				 
     				 logger.debug("Exception in Customerliabilities_DAY0 :::: "+e.getLocalizedMessage());
     			 }
     			
     			 return details;
     		}

	public JsonObject ExportLetterOfCredit_DAY0(String INFO1, String INFO2, String INFO3) //RTS077 - 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 int count = 0;
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
		         Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
					workbook = new XSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
					workbook = new HSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				  }
				 else 
				 {
					throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
				 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                 	  Row row = sheet.getRow(rowIndex);
                	                   	   
                 	 String LEID = getCellValueAsString(row.getCell(3));
                 	  
     				 
                 	String reportingDate = getCellValueAsString(row.getCell(0));
                 	String openingDate = getCellValueAsString(row.getCell(4));
                 	String maturityDate = getCellValueAsString(row.getCell(5));
                 	String holderName = getCellValueAsString(row.getCell(6));
                 	String relationshipType = getCellValueAsString(row.getCell(7));
                 	String bankRelationshipType = getCellValueAsString(row.getCell(8));	 
                 	boolean ratingStatus = true;
                 	String beneficiaryName = getCellValueAsString(row.getCell(9));
                 	String beneficiaryCountry = getCellValueAsString(row.getCell(10));
                 	String crRatingCounterForeignBank = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", LEID);						 
                 	String gradesUnratedForeignBank = ""; 
                 	String currency = getCellValueAsString(row.getCell(14));
                 	String lcClassification = getCellValueAsString(row.getCell(23));
                 	String orgAmount = getCellValueAsString(row.getCell(15));
                 	String usdAmount = "0";  
                 	String tzsAmount = getCellValueAsString(row.getCell(17));
                 	String pastDueDays = util.isNullOrEmpty(getCellValueAsString(row.getCell(18))) ? "0" : getCellValueAsString(row.getCell(18));
                 	String assetClassificationType = "1"; 
                 	String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID);
                 	String allowanceProbableLoss = "0";
                 	String botProvision = "0";
     				 
     				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
     				 
     				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
     				 
     				 usdAmount = rates.get("usd").getAsString();
     				 tzsAmount = rates.get("tzs").getAsString();
     				 
     				  sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
     				 
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
     				count++;
     			 }
			
     		     				if(count > 0)
     		     				 {
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
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "exportLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
			 		 
			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			     
		         details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);   
			 }
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
			 
			 logger.debug("Exception in ExportLetterOfCredit_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject inwardBills_DAY0(String INFO1, String INFO2, String INFO3) //RTS081 - 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 int count = 0;
			 
		try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
			 Workbook workbook;
			 Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
				 workbook = new XSSFWorkbook(fis); 
				 sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
				 workbook = new HSSFWorkbook(fis); 
				 sheet = workbook.getSheetAt(0);   
				 }
				 else 
				 {
				 throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                 	  Row row = sheet.getRow(rowIndex);
                	                   	   
                 	 String LEID = getCellValueAsString(row.getCell(3));
                 	  
     				      				
             		String reportingDate = getCellValueAsString(row.getCell(0));
             		String openingDate = getCellValueAsString(row.getCell(4));
             		String maturityDate = getCellValueAsString(row.getCell(5));
             		String holderName = getCellValueAsString(row.getCell(6));
             		String relationshipType = "1";
             		String beneficiaryName = getCellValueAsString(row.getCell(8));
             		String beneficiaryCountry = getCellValueAsString(row.getCell(9));
             		boolean ratingStatus = true;
             		String crRatingCounterDrawerBank = getCellValueAsString(row.getCell(10));
             		crRatingCounterDrawerBank = (crRatingCounterDrawerBank == null) ? ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID) : crRatingCounterDrawerBank;
             		String gradesUnratedDrawerBank = ""; 
             		String currency = getCellValueAsString(row.getCell(13));
             		String orgAmount = getCellValueAsString(row.getCell(14));
             		String usdAmount = "0";  
             		String tzsAmount = getCellValueAsString(row.getCell(16));
             		String pastDueDays_1 = getCellValueAsString(row.getCell(17));
             		long pastDueDays = FindPastDueDay(pastDueDays_1);
             		String assetClassificationType = "1";
             		String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID);
             		String allowanceProbableLoss = "0";
             		String botProvision = "0";
     				 
     				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
     				 
     				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
     				 
     				 usdAmount = rates.get("usd").getAsString();
     				 tzsAmount = rates.get("tzs").getAsString();
     				 
     				  sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
     				 
     				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
     						
     				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
                 	  
     				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
     				 
     				 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
     					
     				 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";

     			
      				
     				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
     						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     			 
     				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "inwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterDrawerBank, gradesUnratedDrawerBank, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});

     				count++;
     			 }
			
     		     				if(count > 0)
     		     				 {
              
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "inwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterDrawerBank", "gradesUnratedDrawerBank", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "inwardBillsData"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "inwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
					 
				 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
				 
		         details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
 
			 }
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
			 
			 logger.debug("Exception in inwardBillst_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject Outstanding_Guarantee_DAY0(String INFO1, String INFO2, String INFO3) //RTS075 - 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 int count = 0;
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
				 Sheet sheet;
					 if (INFO3.endsWith(".xlsx")) 
					 {
					 workbook = new XSSFWorkbook(fis); 
					 sheet = workbook.getSheetAt(0);   
					 } 
					 else if (INFO3.endsWith(".xls")) 
					 {
					 workbook = new HSSFWorkbook(fis); 
					 sheet = workbook.getSheetAt(0);   
					 }
					 else 
					 {
					 throw new IllegalArgumentException("Unsupported file type: " + INFO3);
					 }
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                 	  Row row = sheet.getRow(rowIndex);
                	                   	   
                 	 String LEID = getCellValueAsString(row.getCell(3));
                 	  
                  	 sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
        			 
         			 List<Lookup001> Info1 = Jdbctemplate.query(sql, new Object[] { LEID, "TradeLMTID" }, new Lookup001_mapper());
         			 
         			 String LimitID = Info1.size() !=0 ? Info1.get(0).getCOLUMN7() : "";


         			String reportingDate = getCellValueAsString(row.getCell(0));
         			String openingDate = getCellValueAsString(row.getCell(4));
         			String maturityDate = getCellValueAsString(row.getCell(5));
         			String beneficiaryName = getCellValueAsString(row.getCell(6));
         			String relationshipType = "1";
         			String bankRelationshipType = getCellValueAsString(row.getCell(8));
         			String guaranteeTypes = row.getCell(9).getStringCellValue();
         			String collateralTypes = ST.FindElementFromFileIT("SCI", "COLLATERALTYPE", "LEID|LIMITID", LEID+"|"+LimitID);
         			String beneficiaryCountry = getCellValueAsString(row.getCell(11));
         			String counterGuarantorName = getCellValueAsString(row.getCell(12));
         			String counterGuarantorCountry = getCellValueAsString(row.getCell(13));
         			boolean ratingStatus = true;
         			String crRatingCounterGuarantor = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", LEID);
         			String gradesUnratedCounterGuarantor = ""; 
         			String currency = getCellValueAsString(row.getCell(17));
         			String orgAmount = getCellValueAsString(row.getCell(18));
         			String usdAmount = "0"; 
         			String tzsAmount = getCellValueAsString(row.getCell(20));
         			String pastDueDays = getCellValueAsString(row.getCell(21));
         			pastDueDays = (pastDueDays == null) ? "0" : pastDueDays;
         			String assetClassificationType = getCellValueAsString(row.getCell(22));
         			String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID);
         			String allowanceProbableLoss = "0";
         			String botProvision = "0"; 

     				 
     				 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
     				 
     				 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmount, currency);
     				 
     				 usdAmount = rates.get("usd").getAsString();
     				 tzsAmount = rates.get("tzs").getAsString();
     				 
     				  sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
     				 
     				 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
     						
     				 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
                 	  
     				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
     				 
     				 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
     					
     				 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
     				 
     				 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
     				 
     				 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterGuarantorCountry }, new Lookup001_mapper());
     					
     				 counterGuarantorCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
     				
     				 sql = "select * from lookup001 where COLUMN11=? and COLUMN1=? and COLUMN12=?";  
     				 
     				 Info = Jdbctemplate.query(sql, new Object[] { INFO1, guaranteeTypes, "Trade001" }, new Lookup001_mapper());
     						 
     				 guaranteeTypes = Info.size() !=0 ? Info.get(0).getCOLUMN3() : "1";
     				 
     				 collateralTypes = !util.isNullOrEmpty(collateralTypes) ? collateralTypes : "1";
     				 
     			
      				
     				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
     						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
     						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     			 
     				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingGuaranteesData", count, reportingDate, openingDate, maturityDate, beneficiaryName, relationshipType, 
     						bankRelationshipType, guaranteeTypes, collateralTypes, beneficiaryCountry, counterGuarantorName, counterGuarantorCountry, ratingStatus, crRatingCounterGuarantor, gradesUnratedCounterGuarantor, currency, orgAmount,
     						usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision });

     				count++;
     			 }
			
     		     				if(count > 0)
     		     				 {
             
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
						   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27) values\r\n" + 
						   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingGuaranteesData", "serial", "reportingDate", "openingDate", "maturityDate", "beneficiaryName", "relationshipType", 
						"bankRelationshipType", "guaranteeTypes", "collateralTypes", "beneficiaryCountry", "counterGuarantorName", "counterGuarantorCountry", "ratingStatus","crRatingCounterGuarantor", "gradesUnratedCounterGuarantor", "currency", "orgAmount",
						"usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision" });
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingGuaranteesData"});
				 
				 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingGuaranteesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
			 		 
			     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			     
		         details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
 
			 }
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
			 
			 logger.debug("Exception in Outstanding_Guarantee_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	//------------------------------------------------------------------------------------------

	//----------------------------------18_11_2024------------------------------------------
	
	public JsonObject LoanInformation_OTP_DAY0(String INFO1, String INFO2, String INFO3) //RTS019 
	{
		 JsonObject details = new JsonObject();
		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			
			
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
				 Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
				 workbook = new XSSFWorkbook(fis); 
				 sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
				 workbook = new HSSFWorkbook(fis); 
				 sheet = workbook.getSheetAt(0);   
				 }
				 else 
				 {
				 throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
				 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                	 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
                	 
                	 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
        			 
                 	  Row row = sheet.getRow(rowIndex);
                 	                   	   
                 	 String LEID = getCellValueAsString(row.getCell(1));
                 	  
                 	 Sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
        			 
        			 List<Lookup001> Info = Jdbctemplate.query(Sql, new Object[] { LEID, "TradeLMTID" }, new Lookup001_mapper());
        			 
        			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";
                 	  
        			 int count = 1;
        			 
           			 String reportingDate = util.getCurrentReportDate();
        			 String customerIdentificationNumber = getCellValueAsString(row.getCell(1));
        			 String clientName = getCellValueAsString(row.getCell(3));
        			 String accountNumber = getCellValueAsString(row.getCell(2)); 
        			 String borrowerCountry = getCellValueAsString(row.getCell(4));  
        			 boolean ratingStatus = true;
        			 String crRatingBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "BANK_RATING", LEID);
        			 String gradesUnratedBanks = "";
        			 String categoryBorrower = "1";
        			 String gender =  "3";
        			 String disability = "1";  
        			 String clientType = "7"; 
        			 String clientSubType = "7";  
        			 String groupName = "NA";
        			 String groupCode = "NA";
        			 String relatedParty = "9";  
        			 String relationshipCategory = "1";
        			 String loanNumber = getCellValueAsString(row.getCell(17)); 
        			 String loanType = "1";  
        			 String loanEconomicActivity = ST.FindElementFromFileIT("SCI", "LOANECONOMICACTIVITY", "LEID", LEID);  
        			 String loanPhase = "1";
        			 String transferStatus =  "2";  
        			 String purposeMortgage = "";
        			 String purposeOtherLoans = "3";  
        			 String sourceFundMortgage = "";
        			 String amortizationType = "2";
        			 String branchCode = "008300"; //"005083"; 
        			 String loanOfficer = ST.FindElementFromFileIT("SCI", "LOANOFFICER", "LEID", LEID);	
        			 String loanSupervisor = "NA";  
        			 String groupVillageNumber = "NA"; 
        			 String cycleNumber = "0";
        			 String loanInstallment = "1";
        			 String repaymentFrequency = "1";
        			 String currency = getCellValueAsString(row.getCell(33));
        			 String contractDate = util.isNullOrEmpty(getCellValueAsString(row.getCell(34))) ? "<null>" : util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(34)), "DD-MM-YYYY");   
        			 String orgSanctionAmount = getCellValueAsString(row.getCell(35)) ; 
        			 String usdSanctionAmount = "0";
        			 String tzsSanctionAmount = "0";
        			 String orgDisbursedAmount = getCellValueAsString(row.getCell(38)); 
        			 String usdDisbursedAmount = "0";
        			 String tzsDisbursedAmount = "0";
        			 String disbursementDate = util.isNullOrEmpty(getCellValueAsString(row.getCell(41))) ? "<null>" : util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(41)), "DD-MM-YYYY"); 
        			 String maturityDate = util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(42)), "DD-MM-YYYY");
        			 String realEndDate = util.isNullOrEmpty(getCellValueAsString(row.getCell(43))) ? "<null>" : util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(43)), "DD-MM-YYYY");  
        			 String orgOutstandingPrincipalAmount = getCellValueAsString(row.getCell(44));
        			 String usdOutstandingPrincipalAmount = "0";
        			 String tzsOutstandingPrincipalAmount = "0";	
        			 String orgInstallmentAmount = getCellValueAsString(row.getCell(47)); 
        			 String usdInstallmentAmount = "0";
        			 String tzsInstallmentAmount = "0";	
        			 String loanInstallmentPaid = "1";
        			 String gracePeriodPaymentPrincipal = "0";
        			 String primeLendingRate = getCellValueAsString(row.getCell(52));
        			 String annualInterestRate = getCellValueAsString(row.getCell(54));
        			 String annualEffectiveInterestRate = getCellValueAsString(row.getCell(55));
        			 String firstInstallmentPaymentDate = util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(56)), "DD-MM-YYYY");
        			 String lastPaymentDate = util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(57)), "DD-MM-YYYY");
        			 String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", LEID+"|"+LimitID); 	
        			 String orgCollateralValue = ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT",  "LEID|LIMITID", LEID+"|"+LimitID);	
        			 String usdCollateralValue = "0";	
        			 String tzsCollateralValue = "0";	
        			 String loanFlagType = "2";
        			 String restructuringDate = util.isNullOrEmpty(getCellValueAsString(row.getCell(59))) ? "<null>" : util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(59)), "DD-MM-YYYY");
        			 String pastDueDays =  util.isNullOrEmpty(getCellValueAsString(row.getCell(60))) ? "0.0" : getCellValueAsString(row.getCell(60));
        			 String pastDueAmount = util.isNullOrEmpty(getCellValueAsString(row.getCell(61))) ? "0.0" : getCellValueAsString(row.getCell(61));
        			 String internalRiskGroup = ST.FindElementFromFileIT("SCI", "RISKGROUP", "LEID", LEID);
        			 String orgAccruedInterestAmount = getCellValueAsString(row.getCell(63));  
        			 String usdAccruedInterestAmount = "0";	
        			 String tzsAccruedInterestAmount = "0";	
        			 String orgPenaltyChargedAmount = getCellValueAsString(row.getCell(66));	
        			 String usdPenaltyChargedAmount = "0";	
        			 String tzsPenaltyChargedAmount = "0";	
        			 String orgPenaltyPaidAmount = getCellValueAsString(row.getCell(68));
        			 String usdPenaltyPaidAmount = "0";  
        			 String tzsPenaltyPaidAmount = "0";
        			 String orgLoanFeesChargedAmount = "0";	
        			 String usdLoanFeesChargedAmount = "0";
        			 String tzsLoanFeesChargedAmount = "0";
        			 String orgLoanFeesPaidAmount = "0";	 
        			 String usdLoanFeesPaidAmount = "0";
        			 String tzsLoanFeesPaidAmount = "0";
        			 String orgTotMonthlyPaymentAmount = getCellValueAsString(row.getCell(70));
        			 String usdTotMonthlyPaymentAmount = "0";
        			 String tzsTotMonthlyPaymentAmount = "0"; 
        			 String sectorSnaClassification = "12";  
        			 String assetClassificationCategory = "1"; 
        			 String negStatusContract = "1"; 
        			 String customerRole = "1"; 
        			 String allowanceProbableLoss = "0";
        			 String botProvision = "0";
        			 String tradingIntent = "2"; 
        			 String interestPricingMethod = getCellValueAsString(row.getCell(53)); 
        			 String orgSuspendedInterest = "0"; 
        			 String usdSuspendedInterest = "0"; 
        			 String tzsSuspendedInterest = "0";
                  
     				 
				FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
				 
				JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgSanctionAmount, currency);
				 
				Sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(Sql, new Object[] { "TRADEDAY0_001", interestPricingMethod }, new Lookup001_mapper());
					
				interestPricingMethod = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "2" : "2";
	
				Sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
				 
				Info = Jdbctemplate.query(Sql, new Object[] { "COUNTRY", borrowerCountry }, new Lookup001_mapper());
					
				borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "218" : "218";
				
				Sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
				 
				Info = Jdbctemplate.query(Sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
				 
				currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";		
				
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
			 
				Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
				 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
				 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
				 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
				 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
				 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
				 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
				 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanInformation", count, reportingDate, customerIdentificationNumber, accountNumber, clientName, borrowerCountry, ratingStatus, crRatingBorrower, gradesUnratedBanks, categoryBorrower, gender,
						 disability, clientType, clientSubType, groupName, groupCode, relatedParty, relationshipCategory, loanNumber, loanType, loanEconomicActivity,  
						 loanPhase, transferStatus, purposeMortgage, purposeOtherLoans, sourceFundMortgage, amortizationType, branchCode, loanOfficer, loanSupervisor, groupVillageNumber,
						 cycleNumber, loanInstallment, repaymentFrequency, currency, contractDate, orgSanctionAmount, usdSanctionAmount, tzsSanctionAmount, orgDisbursedAmount, usdDisbursedAmount,
						 tzsDisbursedAmount, disbursementDate, maturityDate, realEndDate, orgOutstandingPrincipalAmount, usdOutstandingPrincipalAmount, tzsOutstandingPrincipalAmount,orgInstallmentAmount, usdInstallmentAmount, tzsInstallmentAmount,
						 loanInstallmentPaid, gracePeriodPaymentPrincipal, primeLendingRate, interestPricingMethod, annualInterestRate, annualEffectiveInterestRate, firstInstallmentPaymentDate, lastPaymentDate, loanFlagType,restructuringDate,pastDueDays,pastDueAmount,internalRiskGroup,orgAccruedInterestAmount,usdAccruedInterestAmount,tzsAccruedInterestAmount,
						 orgPenaltyChargedAmount,usdPenaltyChargedAmount,tzsPenaltyChargedAmount,orgPenaltyPaidAmount,usdPenaltyPaidAmount,tzsPenaltyPaidAmount,orgLoanFeesChargedAmount,usdLoanFeesChargedAmount,tzsLoanFeesChargedAmount,orgLoanFeesPaidAmount,
						 usdLoanFeesPaidAmount,tzsLoanFeesPaidAmount,orgTotMonthlyPaymentAmount,usdTotMonthlyPaymentAmount,tzsTotMonthlyPaymentAmount,sectorSnaClassification,assetClassificationCategory,negStatusContract,customerRole,allowanceProbableLoss,
						 botProvision,tradingIntent,orgSuspendedInterest,usdSuspendedInterest,tzsSuspendedInterest });
				
				Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
					 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", count , reportingDate, customerIdentificationNumber,  accountNumber, 
					 		collateralPledged, orgCollateralValue, usdCollateralValue, tzsCollateralValue }); 
	
	     					
				 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
					 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
					 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
					 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
					 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
					 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
					 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
					 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
					 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 		
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanInformation", "serial", "reportingDate", "customerIdentificationNumber", "accountNumber", "clientName", "borrowerCountry", "ratingStatus", "crRatingBorrower", "gradesUnratedBanks", "categoryBorrower", "gender",
						 "disability", "clientType", "clientSubType", "groupName", "groupCode", "relatedParty", "relationshipCategory", "loanNumber", "loanType", "loanEconomicActivity",
						 "loanPhase", "transferStatus", "purposeMortgage", "purposeOtherLoans", "sourceFundMortgage", "amortizationType", "branchCode", "loanOfficer", "loanSupervisor", "groupVillageNumber",
						 "cycleNumber", "loanInstallment", "repaymentFrequency", "currency", "contractDate", "orgSanctionAmount", "usdSanctionAmount", "tzsSanctionAmount", "orgDisbursedAmount", "usdDisbursedAmount",
						 "tzsDisbursedAmount", "disbursementDate", "maturityDate", "realEndDate", "orgOutstandingPrincipalAmount", "usdOutstandingPrincipalAmount", "tzsOutstandingPrincipalAmount","orgInstallmentAmount", "usdInstallmentAmount", "tzsInstallmentAmount",
						 "loanInstallmentPaid", "gracePeriodPaymentPrincipal", "primeLendingRate", "interestPricingMethod", "annualInterestRate", "annualEffectiveInterestRate", "firstInstallmentPaymentDate", "lastPaymentDate", "loanFlagType","restructuringDate","pastDueDays","pastDueAmount","internalRiskGroup","orgAccruedInterestAmount","usdAccruedInterestAmount","tzsAccruedInterestAmount",
						 "orgPenaltyChargedAmount","usdPenaltyChargedAmount","tzsPenaltyChargedAmount","orgPenaltyPaidAmount","usdPenaltyPaidAmount","tzsPenaltyPaidAmount","orgLoanFeesChargedAmount","usdLoanFeesChargedAmount","tzsLoanFeesChargedAmount","orgLoanFeesPaidAmount",
						 "usdLoanFeesPaidAmount","tzsLoanFeesPaidAmount","orgTotMonthlyPaymentAmount","usdTotMonthlyPaymentAmount","tzsTotMonthlyPaymentAmount","sectorSnaClassification","assetClassificationCategory","negStatusContract","customerRole","allowanceProbableLoss",
						 "botProvision","tradingIntent","orgSuspendedInterest","usdSuspendedInterest","tzsSuspendedInterest" });
	     					
	     		 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
					 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "collateralPledged", "serial" , "reportingDate", "customerIdentificationNumber", "accountNumber",
					 		"collateralPledged", "orgCollateralValue", "usdCollateralValue", "tzsCollateralValue" }); 
			 
	     				 //-----------------
	     				 
				 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
					
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanInformation", "1"});
				 
				 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
					
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged", "2"});
				 
				 Sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				 
			     Jdbctemplate.update(Sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanInformation", INFO1, "1", "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
			     
			     StoreLoanLog(SUBORGCODE, INFO1, "TRADE", loanNumber, Batch_id, O_SERIAL);
			     
				 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
					
				 details.addProperty("Serial", O_SERIAL);
		         details.addProperty("Batch_id", Batch_id);
				 
				 }
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
			
			 logger.debug("Exception in LoanInformation :::: "+e.getLocalizedMessage());
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
	
	public JsonObject outwardBills_DAY0(String INFO1, String INFO2, String INFO3) //RTS083 
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
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 int count = 0;
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
		         Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
					workbook = new XSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
					workbook = new HSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				  }
				 else 
				 {
					throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
                 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                 	  Row row = sheet.getRow(rowIndex);
                 	                   	   
                 	 String LEID = getCellValueAsString(row.getCell(1));
                 	                   	  
                 	String reportingDate = getCellValueAsString(row.getCell(0));
                 	String openingDate = getCellValueAsString(row.getCell(4));
                 	String maturityDate = getCellValueAsString(row.getCell(5));
                 	String holderName = getCellValueAsString(row.getCell(6));
                 	String relationshipType = "1";
                 	String beneficiaryName = getCellValueAsString(row.getCell(8));
                 	String beneficiaryCountry = getCellValueAsString(row.getCell(9));
                 	boolean ratingStatus = true;
                 	String crRatingCounterBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", LEID);
                 	String gradesUnratedBorrower = ""; 
                 	String currency = getCellValueAsString(row.getCell(13));
                 	String orgAmount = getCellValueAsString(row.getCell(14));
                 	String usdAmount = "0"; 
                 	String tzsAmount = "0"; 
                 	String pastDueDays =getCellValueAsString(row.getCell(17));
                 	pastDueDays = (pastDueDays == null) ? "0" : pastDueDays;
                 	String assetClassificationType = "1";
                 	String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID); 
                 	String allowanceProbableLoss = "0";
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
			
    				 
        			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
      					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
      					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      		 
      			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outwardBillsData", count, reportingDate, openingDate, maturityDate, holderName, relationshipType, beneficiaryName, beneficiaryCountry, ratingStatus, crRatingCounterBorrower, gradesUnratedBorrower, currency, orgAmount, usdAmount, tzsAmount, pastDueDays, assetClassificationType, sectorSnaClassification, allowanceProbableLoss, botProvision});

      			 count++;
                 }
              
     				if(count > 0)
     				 {
     					
     					 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
     							   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23) values\r\n" + 
     							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
     				 
     					 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outwardBillsData", "serial","reportingDate", "openingDate", "maturityDate", "holderName", "relationshipType", "beneficiaryName", "beneficiaryCountry", "ratingStatus", "crRatingCounterBorrower", "gradesUnratedBorrower", "currency", "orgAmount", "usdAmount", "tzsAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "allowanceProbableLoss", "botProvision"});
     					
     				 //-----------------
     				 
     					 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
     					
     					 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outwardBillsData"});
     					 
     					 Sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
     					 
     					 Jdbctemplate.update(Sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance", "outwardBillsData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
     						 
     					 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
     					 
     			         details.addProperty("Serial", O_SERIAL);
     			         details.addProperty("Batch_id", Batch_id);
		 
			 }
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
			 
			 logger.debug("Exception in outwardBills_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}

	public JsonObject OustandingLetterCredit_DAY0(String INFO1, String INFO2, String INFO3) //RTS079 
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
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 int count = 0;
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
		         Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
					workbook = new XSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
					workbook = new HSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				  }
				 else 
				 {
					throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
                 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                 	  Row row = sheet.getRow(rowIndex);
                 	                   	   
                 	 String LEID = getCellValueAsString(row.getCell(3));
                 	                   	  
			 String reportingDate = getCellValueAsString(row.getCell(0));
			 String lettersCreditType = getCellValueAsString(row.getCell(4));
			 String collateralType = "11"; 
			 String openingDate = getCellValueAsString(row.getCell(6));
			 String maturityDate = getCellValueAsString(row.getCell(8)) ;  
			 String expireDate = getCellValueAsString(row.getCell(7));
			 String holderName = getCellValueAsString(row.getCell(9));
			 String relationshipType = "1";
			 String bankRelationshipType = getCellValueAsString(row.getCell(11));
			 String beneficiaryName = getCellValueAsString(row.getCell(12));
			 String beneficiaryCountry = getCellValueAsString(row.getCell(13));
			 boolean customerRatingStatus = true;
			 String crRatingCounterCustomer = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", LEID); 
			 String gradesUnratedCustomer = ""; 
			 String currency = getCellValueAsString(row.getCell(17));
			 String orgAmount = getCellValueAsString(row.getCell(18));	 
			 String usdAmount = "0";
			 String tzsAmount = "0";  
			 String orgOutstandingMargDepAmount = getCellValueAsString(row.getCell(21)); 
			 String usdOutstandingMargDepAmount = "0";   
			 String tzsOutstandingMargDepAmount = "0"; 
			 String pastDueDays = util.isNullOrEmpty(getCellValueAsString(row.getCell(24))) ? "0" : getCellValueAsString(row.getCell(24));
			 String assetClassificationType = "1"; 
			 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID);
			 String lcClassification = getCellValueAsString(row.getCell(29));  
			 boolean bankRatingStatus = true;
			 String crRatingConfirmingBank = "";  
			 String gradesUnratedConfirmingBank = ""; 
			 String botProvision = "0";  
			 String allowanceProbableLoss = "0"; 
                 
			 if(lcClassification.equalsIgnoreCase("Confirmed"))
			 {
				 lcClassification = "1";
			 }
			 else if(lcClassification.equalsIgnoreCase("UNCONFIRMED"))
			 {
				 lcClassification = "2"; 
			 }
			 else
			 {
				 lcClassification = "2"; 
			 }
			 
			 if(bankRatingStatus)
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
			 
			 if(Info.size() == 0)
			 {
				 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
				 
				 Info = Jdbctemplate.query(sql, new Object[] { lettersCreditType, "Trade002" }, new Lookup001_mapper());
				 
				 lettersCreditType = Info.size() !=0 ? Info.get(0).getCOLUMN4() : "1";
				 
			 }
			 
			 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", beneficiaryCountry }, new Lookup001_mapper());
				
			 beneficiaryCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			
    				 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "outstandingLettersCreditData", count, reportingDate, lettersCreditType, collateralType, openingDate, maturityDate, expireDate, holderName, relationshipType, bankRelationshipType, beneficiaryName, beneficiaryCountry, customerRatingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgAmount, usdAmount, tzsAmount, orgOutstandingMargDepAmount, usdOutstandingMargDepAmount, tzsOutstandingMargDepAmount, pastDueDays, assetClassificationType, sectorSnaClassification, lcClassification, bankRatingStatus, crRatingConfirmingBank, gradesUnratedConfirmingBank, botProvision, allowanceProbableLoss});
			 
			 count++;

              }
     				if(count > 0)
     				 {
     					
			 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16,"+
					   "COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22, COLUMN23,COLUMN24,COLUMN25,COLUMN26, COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34) values\r\n" + 
					   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "outstandingLettersCreditData", "serial","reportingDate", "lettersCreditType", "collateralType", "openingDate", "maturityDate", "expireDate", "holderName", "relationshipType", "bankRelationshipType", "beneficiaryName", "beneficiaryCountry", "customerRatingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgAmount", "usdAmount", "tzsAmount", "orgOutstandingMargDepAmount", "usdOutstandingMargDepAmount", "tzsOutstandingMargDepAmount", "pastDueDays", "assetClassificationType", "sectorSnaClassification", "lcClassification", "bankRatingStatus", "crRatingConfirmingBank", "gradesUnratedConfirmingBank", "botProvision", "allowanceProbableLoss"});
     					
     				 //-----------------
     				 
			 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "outstandingLettersCreditData"});
			 
			 Sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(Sql, new Object[] { Batch_id, SUBORGCODE, "Off Balance Sheet Data", "outstandingLettersCreditData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
     						 
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
			 details.addProperty("Serial", O_SERIAL);
			 details.addProperty("Batch_id", Batch_id);

			 }
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
			 
			 logger.debug("Exception in OustandingLetterCredit_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject LoanInformation_DTP_DAY0(String INFO1, String INFO2, String INFO3) //RTS019 
	{
		JsonObject details = new JsonObject();
		 		
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 
			 
			 
			 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
			 
			 
			 
			 try (FileInputStream fis = new FileInputStream(new File(INFO3))) {
				 Workbook workbook;
		         Sheet sheet;
				 if (INFO3.endsWith(".xlsx")) 
				 {
					workbook = new XSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				 } 
				 else if (INFO3.endsWith(".xls")) 
				 {
					workbook = new HSSFWorkbook(fis); 
					sheet = workbook.getSheetAt(0);   
				  }
				 else 
				 {
					throw new IllegalArgumentException("Unsupported file type: " + INFO3);
				 }
                 
                 for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
                 {   
                	 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
                	 
                	 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
        			 
                 	  Row row = sheet.getRow(rowIndex);
                 	       
                 	 int count = 1;
                 	 
                 	 String LEID = getCellValueAsString(row.getCell(3));
                 	  
                  	 Sql = "select * from lookup001 where COLUMN4=? and COLUMN12=?";  
        			 
         			 List<Lookup001> Info = Jdbctemplate.query(Sql, new Object[] { LEID, "TradeLMTID" }, new Lookup001_mapper());
         			 
         			 String LimitID = Info.size() !=0 ? Info.get(0).getCOLUMN7() : "";

                 	                   	  
				String reportingDate = getCellValueAsString(row.getCell(0));
				String customerIdentificationNumber = LEID;
				String clientName = getCellValueAsString(row.getCell(6));
				String accountNumber = getCellValueAsString(row.getCell(5));
				String borrowerCountry = getCellValueAsString(row.getCell(7));  
				boolean ratingStatus = true;
				String crRatingBorrower = ST.FindElementFromFileIT("SCI", "CRRATING", "BANK_RATING", LEID);
				String gradesUnratedBanks = ""; 
				String categoryBorrower = "1";
				String gender =  "3";  
				String disability = "1";  
				String clientType = "7";  
				String clientSubType = ""; 
				String groupName = "NA"; 
				String groupCode = "NA"; 
				String relatedParty = "9";  
				String relationshipCategory = "1";  
				String loanNumber = getCellValueAsString(row.getCell(20)); 
				String loanType = "1";
				String loanEconomicActivity = ST.FindElementFromFileIT("SCI", "LOANECONOMICACTIVITY", "LEID", LEID);
				String loanPhase = "1";
				String transferStatus =  "2"; 
				String purposeMortgage = ""; 
				String purposeOtherLoans = "";
				String sourceFundMortgage = ""; 
				String amortizationType = "2";
				String branchCode = "008300"; //"005083"; 
				String loanOfficer = ST.FindElementFromFileIT("SCI", "LOANOFFICER", "LEID", LEID); 
				String loanSupervisor = "NA"; 
				String groupVillageNumber = "NA"; 
				String cycleNumber = "0"; 
				String loanInstallment = "1"; 
				String repaymentFrequency = "1"; 
				String currency = getCellValueAsString(row.getCell(36));
				String contractDate = getCellValueAsString(row.getCell(37));
				String orgSanctionAmount = getCellValueAsString(row.getCell(38)) ;
				String usdSanctionAmount = "0"; 
				String tzsSanctionAmount = "0";
				String orgDisbursedAmount = getCellValueAsString(row.getCell(41));
				String usdDisbursedAmount = "0";
				String tzsDisbursedAmount = "0";
				String disbursementDate = getCellValueAsString(row.getCell(44));
				String maturityDate = getCellValueAsString(row.getCell(45));
				String realEndDate = util.isNullOrEmpty(getCellValueAsString(row.getCell(46))) ? "<null>" : util.Convert_BOT_Date_Format(getCellValueAsString(row.getCell(46)), "DD-MM-YYYY");
				String orgOutstandingPrincipalAmount = getCellValueAsString(row.getCell(47));
				String usdOutstandingPrincipalAmount = "0"; 
				String tzsOutstandingPrincipalAmount = "0";
				String orgInstallmentAmount = getCellValueAsString(row.getCell(50));
				String usdInstallmentAmount = "0";
				String tzsInstallmentAmount = "0"; 
				String loanInstallmentPaid = "1";
				String gracePeriodPaymentPrincipal = "0"; 
				String primeLendingRate = getCellValueAsString(row.getCell(55));
				String annualInterestRate = getCellValueAsString(row.getCell(57));
				String annualEffectiveInterestRate = getCellValueAsString(row.getCell(58));
				String firstInstallmentPaymentDate = getCellValueAsString(row.getCell(59));
				String lastPaymentDate = getCellValueAsString(row.getCell(60));
				String collateralPledged = ST.FindElementFromFileIT("SCI", "COLLATERALPLEDGE", "LEID|LIMITID", LEID+"|"+LimitID); 
				String orgCollateralValue =  ST.FindElementFromFileIT("SCI", "COLLATERALAMOUNT",  "LEID|LIMITID", LEID+"|"+LimitID); 
				String usdCollateralValue = "0"; 
				String tzsCollateralValue = "0"; 
				String loanFlagType = "2";
				String restructuringDate = "<null>";
				String pastDueDays = util.isNullOrEmpty(getCellValueAsString(row.getCell(67))) ? "0" : (getCellValueAsString(row.getCell(67))) ;
				String pastDueAmount = util.isNullOrEmpty(getCellValueAsString(row.getCell(68))) ? "0.0" : (getCellValueAsString(row.getCell(68)));  
				String internalRiskGroup = ST.FindElementFromFileIT("SCI", "RISKGROUP", "LEID", LEID);
				String orgAccruedInterestAmount = getCellValueAsString(row.getCell(70));
				String usdAccruedInterestAmount = "0";
				String tzsAccruedInterestAmount = "0";
				String orgPenaltyChargedAmount = getCellValueAsString(row.getCell(73));
				String usdPenaltyChargedAmount = "0"; 
				String tzsPenaltyChargedAmount = "0";  
				String orgPenaltyPaidAmount = getCellValueAsString(row.getCell(76));
				String usdPenaltyPaidAmount = "0";  
				String tzsPenaltyPaidAmount = "0";
				String orgLoanFeesChargedAmount = getCellValueAsString(row.getCell(79));
				String usdLoanFeesChargedAmount = "0";
				String tzsLoanFeesChargedAmount = "0"; 
				String orgLoanFeesPaidAmount = getCellValueAsString(row.getCell(82));
				String usdLoanFeesPaidAmount = "0"; 
				String tzsLoanFeesPaidAmount = "0"; 
				String orgTotMonthlyPaymentAmount = getCellValueAsString(row.getCell(85));
				String usdTotMonthlyPaymentAmount = "0";
				String tzsTotMonthlyPaymentAmount = "0"; 
				String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", LEID); 
				String assetClassificationCategory = util.isNullOrEmpty(getCellValueAsString(row.getCell(89))) ? "1" : getCellValueAsString(row.getCell(89));
				String negStatusContract = "1"; 
				String customerRole = "1";
				String allowanceProbableLoss = "0"; 
				String tradingIntent = "2"; 
				String interestPricingMethod = getCellValueAsString(row.getCell(56));  
				String orgSuspendedInterest = getCellValueAsString(row.getCell(94)); 
				String usdSuspendedInterest = "0";  
				String tzsSuspendedInterest = "0"; 
				String botProvision = "0";
			
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
			
			Sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(Sql, new Object[] { "TRADEDAY0_001", interestPricingMethod }, new Lookup001_mapper());
				
			interestPricingMethod = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "2" : "2";

			
			Sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
			 
			Info = Jdbctemplate.query(Sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
			 
			currency = Info.size() !=0 ? Info.get(0).getCOLUMN1() : "834";
			
			Sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
			 
			Info = Jdbctemplate.query(Sql, new Object[] { "COUNTRY", borrowerCountry }, new Lookup001_mapper());
				
			borrowerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
			
    				 
			Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
			 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
			 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
			 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
			 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
			 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
			 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
			 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
			 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			 																						
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "loanInformation", count, reportingDate, customerIdentificationNumber, accountNumber, clientName, borrowerCountry, ratingStatus, crRatingBorrower, gradesUnratedBanks, categoryBorrower, gender,
					 disability, clientType, clientSubType, groupName, groupCode, relatedParty, relationshipCategory, loanNumber, loanType, loanEconomicActivity,  
					 loanPhase, transferStatus, purposeMortgage, purposeOtherLoans, sourceFundMortgage, amortizationType, branchCode, loanOfficer, loanSupervisor, groupVillageNumber,
					 cycleNumber, loanInstallment, repaymentFrequency, currency, contractDate, orgSanctionAmount, usdSanctionAmount, tzsSanctionAmount, orgDisbursedAmount, usdDisbursedAmount,
					 tzsDisbursedAmount, disbursementDate, maturityDate, realEndDate, orgOutstandingPrincipalAmount, usdOutstandingPrincipalAmount, tzsOutstandingPrincipalAmount,orgInstallmentAmount, usdInstallmentAmount, tzsInstallmentAmount,
					 loanInstallmentPaid, gracePeriodPaymentPrincipal, primeLendingRate, interestPricingMethod, annualInterestRate, annualEffectiveInterestRate, firstInstallmentPaymentDate, lastPaymentDate, loanFlagType,restructuringDate,pastDueDays,pastDueAmount,internalRiskGroup,orgAccruedInterestAmount,usdAccruedInterestAmount,tzsAccruedInterestAmount,
					 orgPenaltyChargedAmount,usdPenaltyChargedAmount,tzsPenaltyChargedAmount,orgPenaltyPaidAmount,usdPenaltyPaidAmount,tzsPenaltyPaidAmount,orgLoanFeesChargedAmount,usdLoanFeesChargedAmount,tzsLoanFeesChargedAmount,orgLoanFeesPaidAmount,
					 usdLoanFeesPaidAmount,tzsLoanFeesPaidAmount,orgTotMonthlyPaymentAmount,usdTotMonthlyPaymentAmount,tzsTotMonthlyPaymentAmount,sectorSnaClassification,assetClassificationCategory,negStatusContract,customerRole,allowanceProbableLoss,
					 botProvision,tradingIntent,orgSuspendedInterest,usdSuspendedInterest,tzsSuspendedInterest });
			 
			 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "collateralPledged", count , reportingDate, customerIdentificationNumber,  accountNumber, 
				 		collateralPledged, orgCollateralValue, usdCollateralValue, tzsCollateralValue }); 


     					
			 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, \r\n" + 
				 		"  COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, \r\n" + 
				 		"  COLUMN31, COLUMN32, COLUMN33, COLUMN34, COLUMN35, COLUMN36, COLUMN37, COLUMN38, COLUMN39, COLUMN40, COLUMN41, \r\n" + 
				 		"  COLUMN42, COLUMN43, COLUMN44, COLUMN45, COLUMN46, COLUMN47, COLUMN48, COLUMN49, COLUMN50, COLUMN51, COLUMN52, \r\n" + 
				 		"  COLUMN53, COLUMN54, COLUMN55, COLUMN56, COLUMN57, COLUMN58, COLUMN59, COLUMN60, COLUMN61, COLUMN62, COLUMN63, \r\n" + 
				 		"  COLUMN64, COLUMN65, COLUMN66, COLUMN67, COLUMN68, COLUMN69, COLUMN70, COLUMN71, COLUMN72, COLUMN73, COLUMN74, \r\n" + 
				 		"  COLUMN75, COLUMN76, COLUMN77, COLUMN78, COLUMN79, COLUMN80, COLUMN81, COLUMN82, COLUMN83, COLUMN84, COLUMN85, \r\n" + 
				 		"  COLUMN86, COLUMN87, COLUMN88, COLUMN89, COLUMN90, COLUMN91, COLUMN92, COLUMN93,COLUMN94,COLUMN95) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 		
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "loanInformation", "serial", "reportingDate", "customerIdentificationNumber", "accountNumber", "clientName", "borrowerCountry", "ratingStatus", "crRatingBorrower", "gradesUnratedBanks", "categoryBorrower", "gender",
					 "disability", "clientType", "clientSubType", "groupName", "groupCode", "relatedParty", "relationshipCategory", "loanNumber", "loanType", "loanEconomicActivity",
					 "loanPhase", "transferStatus", "purposeMortgage", "purposeOtherLoans", "sourceFundMortgage", "amortizationType", "branchCode", "loanOfficer", "loanSupervisor", "groupVillageNumber",
					 "cycleNumber", "loanInstallment", "repaymentFrequency", "currency", "contractDate", "orgSanctionAmount", "usdSanctionAmount", "tzsSanctionAmount", "orgDisbursedAmount", "usdDisbursedAmount",
					 "tzsDisbursedAmount", "disbursementDate", "maturityDate", "realEndDate", "orgOutstandingPrincipalAmount", "usdOutstandingPrincipalAmount", "tzsOutstandingPrincipalAmount","orgInstallmentAmount", "usdInstallmentAmount", "tzsInstallmentAmount",
					 "loanInstallmentPaid", "gracePeriodPaymentPrincipal", "primeLendingRate", "interestPricingMethod", "annualInterestRate", "annualEffectiveInterestRate", "firstInstallmentPaymentDate", "lastPaymentDate", "loanFlagType","restructuringDate","pastDueDays","pastDueAmount","internalRiskGroup","orgAccruedInterestAmount","usdAccruedInterestAmount","tzsAccruedInterestAmount",
					 "orgPenaltyChargedAmount","usdPenaltyChargedAmount","tzsPenaltyChargedAmount","orgPenaltyPaidAmount","usdPenaltyPaidAmount","tzsPenaltyPaidAmount","orgLoanFeesChargedAmount","usdLoanFeesChargedAmount","tzsLoanFeesChargedAmount","orgLoanFeesPaidAmount",
					 "usdLoanFeesPaidAmount","tzsLoanFeesPaidAmount","orgTotMonthlyPaymentAmount","usdTotMonthlyPaymentAmount","tzsTotMonthlyPaymentAmount","sectorSnaClassification","assetClassificationCategory","negStatusContract","customerRole","allowanceProbableLoss",
					 "botProvision","tradingIntent","orgSuspendedInterest","usdSuspendedInterest","tzsSuspendedInterest" });
     			
			 Sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, \r\n" + 
				 		"  COLUMN9, COLUMN10,COLUMN11) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "collateralPledged", "serial" , "reportingDate", "customerIdentificationNumber", "accountNumber",
				 		"collateralPledged", "orgCollateralValue", "usdCollateralValue", "tzsCollateralValue" }); 
				
     				 //-----------------
     				 
			 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "loanInformation", "1"});
			 
			 Sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3,COLUMN4) values(?,?,?,?,?,?)";
				
			 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "collateralPledged", "2"});
			 
			 Sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(Sql, new Object[] { Batch_id, SUBORGCODE, "Assets Data", "loanInformation", INFO1, "1", "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "Trade_DAY0" });	 
     						 
		     StoreLoanLog(SUBORGCODE, INFO1, "TRADE", loanNumber, Batch_id, O_SERIAL);
		     
			 Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
			 
			 details.addProperty("Serial", O_SERIAL);
			 details.addProperty("Batch_id", Batch_id);

			 }
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
			 
			 logger.debug("Exception in LoanInformation_DTP_DAY0 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	
	//------------------------------------------------------------------------------------------
	
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
	
	 public static String getCellValueAsString(Cell cell) {
	        if (cell == null) 
	        {
	            return "";
	        }

	        switch (cell.getCellType()) {
	            case Cell.CELL_TYPE_STRING:
	                return cell.getStringCellValue();
	            case Cell.CELL_TYPE_NUMERIC: 
	                if (DateUtil.isCellDateFormatted(cell)) 
	                {
	                    return cell.getDateCellValue().toString();
	                } 
	                else 
	                {
	                    return String.valueOf(cell.getNumericCellValue());
	                }
	            case Cell.CELL_TYPE_BOOLEAN:
	                return String.valueOf(cell.getBooleanCellValue());
	            case Cell.CELL_TYPE_FORMULA:
	                return String.valueOf(cell.getCellFormula());
	            case Cell.CELL_TYPE_BLANK:
	                return ""; 
	            default:
	                return ""; 
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
}
