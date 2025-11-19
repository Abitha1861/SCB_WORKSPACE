package com.hdsoft.models;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


import org.apache.logging.log4j.*;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Domain;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;


@Component
public class DASHBOARD 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(DASHBOARD.class);
	
	public JsonObject Success_fail_2(String fromDate , String toDate) 
	{
		JsonObject status = new JsonObject();
		
		try
		{
			
			Common_Utils util = new Common_Utils();
			
			JsonArray teams = new JsonArray();  
			JsonObject details = new JsonObject();
			
			System.out.println(fromDate);
			System.out.println(toDate);
			

			fromDate =  util.Convert_Date_Format(fromDate, "yyyy-MM-dd", "dd-MMM-yyyy");
			toDate = util.Convert_Date_Format(toDate, "yyyy-MM-dd", "dd-MMM-yyyy");

			
			String sql = "SELECT\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS EXCEL_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS EXCEL_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS ACBS_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS ACBS_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS CASH_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS CASH_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS GEMS_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS GEMS_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS TRADE_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS TRADE_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS CC_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS CC_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS EBBS_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS EBBS_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS Finance_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS Finance_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS FM_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS FM_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS CADM_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS CADM_FAIL,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS APARTA_SUCCESS,\r\n"
					+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS APARTA_FAIL\r\n"
					+ "FROM DUAL";

			List<Map<String, Object>> result = Jdbctemplate.queryForList(sql, new Object[] {
				    "COUNTRY", fromDate , toDate , "SUCCESS","ARCHIVED", 
				    "COUNTRY", fromDate , toDate , "SUCCESS","ARCHIVED", 
				    "ACBS", fromDate , toDate , "SUCCESS", "ARCHIVED", "ACBS",
				    "ACBS", fromDate , toDate , "SUCCESS","ARCHIVED", "ACBS",
				    "CASH", fromDate , toDate , "SUCCESS", "ARCHIVED", "CASH%",
				    "CASH", fromDate , toDate , "SUCCESS","ARCHIVED", "CASH%",
				    "GEMS", fromDate , toDate , "SUCCESS", "ARCHIVED", 
				    "GEMS", fromDate , toDate , "SUCCESS","ARCHIVED", 
				    "TRADE", fromDate , toDate , "SUCCESS", "ARCHIVED", "Trade%",
				    "TRADE", fromDate , toDate , "SUCCESS","ARCHIVED", "Trade%",
				    "CC", fromDate , toDate , "SUCCESS", "ARCHIVED", 
				    "CC", fromDate , toDate , "SUCCESS","ARCHIVED", 
				    "EBBS", fromDate , toDate , "SUCCESS", "ARCHIVED", "EBBS",
				    "EBBS", fromDate , toDate , "SUCCESS","ARCHIVED", "EBBS",
				    "FINANCE", fromDate , toDate , "SUCCESS", "ARCHIVED", 
				    "FINANCE", fromDate , toDate , "SUCCESS","ARCHIVED", 
				    "FM", fromDate , toDate , "SUCCESS", "ARCHIVED", "FM",
				    "FM", fromDate , toDate , "SUCCESS","ARCHIVED", "FM",
				    "CADM", fromDate , toDate , "SUCCESS", "ARCHIVED", 
				    "CADM", fromDate , toDate , "SUCCESS","ARCHIVED", 
				    "APARTA", fromDate , toDate , "SUCCESS", "ARCHIVED", 
				    "APARTA", fromDate , toDate , "SUCCESS","ARCHIVED"
				});
			
			for (Map<String, Object> secret : result) 
			{
			    BigDecimal excelSuccess = (BigDecimal) secret.get("EXCEL_SUCCESS");
			    BigDecimal excelFail = (BigDecimal) secret.get("EXCEL_FAIL");
			    BigDecimal acbsSuccess = (BigDecimal) secret.get("ACBS_SUCCESS");
			    BigDecimal acbsFail = (BigDecimal) secret.get("ACBS_FAIL");
			    BigDecimal cashSuccess = (BigDecimal) secret.get("CASH_SUCCESS");
			    BigDecimal cashFail = (BigDecimal) secret.get("CASH_FAIL");
			    BigDecimal gemsSuccess = (BigDecimal) secret.get("GEMS_SUCCESS");
			    BigDecimal gemsFail = (BigDecimal) secret.get("GEMS_FAIL");
			    BigDecimal tradeSuccess = (BigDecimal) secret.get("TRADE_SUCCESS");
			    BigDecimal tradeFail = (BigDecimal) secret.get("TRADE_FAIL");
			    BigDecimal ccSuccess = (BigDecimal) secret.get("CC_SUCCESS");
			    BigDecimal ccFail = (BigDecimal) secret.get("CC_FAIL");
			    BigDecimal ebbsSuccess = (BigDecimal) secret.get("EBBS_SUCCESS");
			    BigDecimal ebbsFail = (BigDecimal) secret.get("EBBS_FAIL");
			    BigDecimal financeSuccess = (BigDecimal) secret.get("Finance_SUCCESS");
			    BigDecimal financeFail = (BigDecimal) secret.get("Finance_FAIL");
			    BigDecimal fmSuccess = (BigDecimal) secret.get("FM_SUCCESS");
			    BigDecimal fmFail = (BigDecimal) secret.get("FM_FAIL");
			    BigDecimal CADM_SUCCESS = (BigDecimal) secret.get("CADM_SUCCESS");
			    BigDecimal CADM_FAIL = (BigDecimal) secret.get("CADM_FAIL");
			    BigDecimal APARTA_SUCCESS = (BigDecimal) secret.get("APARTA_SUCCESS");
			    BigDecimal APARTA_FAIL = (BigDecimal) secret.get("APARTA_FAIL");
			   
			    details.addProperty("EXCEL_SUCCESS", excelSuccess.intValue());
			    details.addProperty("EXCEL_FAIL", excelFail.intValue());
			    details.addProperty("ACBS_SUCCESS", acbsSuccess.intValue());
			    details.addProperty("ACBS_FAIL", acbsFail.intValue());
			    details.addProperty("CASH_SUCCESS", cashSuccess.intValue());
			    details.addProperty("CASH_FAIL", cashFail.intValue());
			    details.addProperty("GEMS_SUCCESS", gemsSuccess.intValue());
			    details.addProperty("GEMS_FAIL", gemsFail.intValue());
			    details.addProperty("TRADE_SUCCESS", tradeSuccess.intValue());
			    details.addProperty("TRADE_FAIL", tradeFail.intValue());
			    details.addProperty("CC_SUCCESS", ccSuccess.intValue());
			    details.addProperty("CC_FAIL", ccFail.intValue());
			    details.addProperty("EBBS_SUCCESS", ebbsSuccess.intValue());
			    details.addProperty("EBBS_FAIL", ebbsFail.intValue());
			    details.addProperty("Finance_SUCCESS", financeSuccess.intValue());
			    details.addProperty("Finance_FAIL", financeFail.intValue());
			    details.addProperty("FM_SUCCESS", fmSuccess.intValue());
			    details.addProperty("FM_FAIL", fmFail.intValue());
			    details.addProperty("CADM_SUCCESS", CADM_SUCCESS.intValue());
			    details.addProperty("CADM_FAIL", CADM_FAIL.intValue());
			    details.addProperty("APARTA_SUCCESS", APARTA_SUCCESS.intValue());
			    details.addProperty("APARTA_FAIL", APARTA_FAIL.intValue());
			}
			


			JsonObject details_2 = new JsonObject();
			
			
			 sql = "SELECT\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS EXCEL_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS ACBS_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS CASH_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS GEMS_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS TRADE_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS CC_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS EBBS_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS FINANCE_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS FM_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS CADM_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*)  FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS Aparta_INPROGRESS\r\n"
			 		+ "FROM DUAL";
			 
			 
			List<Map<String, Object>> result_1 = Jdbctemplate.queryForList(sql, new Object[] {
					"COUNTRY",fromDate , toDate ,  "ACBS",fromDate , toDate , "CASH",fromDate , toDate , "GEMS",fromDate , toDate ,
					"TRADE",fromDate , toDate ,
				    "CC",fromDate , toDate , "EBBS",fromDate , toDate , "FINANCE",fromDate , toDate , "FM",fromDate , toDate,
				    "CADM",fromDate , toDate , "APARTA",fromDate , toDate
				});
			
			Map<String, Object> result_2 = result_1.get(0);
			
			BigDecimal excelInProgress = (BigDecimal) result_2.get("EXCEL_INPROGRESS");
			BigDecimal acbsInProgress = (BigDecimal) result_2.get("ACBS_INPROGRESS");
			BigDecimal cashInProgress = (BigDecimal) result_2.get("CASH_INPROGRESS");
			BigDecimal gemsInProgress = (BigDecimal) result_2.get("GEMS_INPROGRESS");
			BigDecimal tradeInProgress = (BigDecimal) result_2.get("TRADE_INPROGRESS");
			BigDecimal marcisInProgress = (BigDecimal) result_2.get("MARCIS_INPROGRESS");
			BigDecimal ccInProgress = (BigDecimal) result_2.get("CC_INPROGRESS");
			BigDecimal ebbsInProgress = (BigDecimal) result_2.get("EBBS_INPROGRESS");
			BigDecimal financeInProgress = (BigDecimal) result_2.get("FINANCE_INPROGRESS");
			BigDecimal fmInProgress = (BigDecimal) result_2.get("FM_INPROGRESS");
			BigDecimal CADM_INPROGRESS = (BigDecimal) result_2.get("CADM_INPROGRESS");
			BigDecimal Aparta_INPROGRESS = (BigDecimal) result_2.get("Aparta_INPROGRESS");
		
			details_2.addProperty("EXCEL_INPROGRESS", excelInProgress);
			details_2.addProperty("ACBS_INPROGRESS", acbsInProgress);
			details_2.addProperty("CASH_INPROGRESS", cashInProgress);
			details_2.addProperty("GEMS_INPROGRESS", gemsInProgress);
			details_2.addProperty("TRADE_INPROGRESS", tradeInProgress);
			details_2.addProperty("MARCIS_INPROGRESS", marcisInProgress);
			details_2.addProperty("CC_INPROGRESS", ccInProgress);
			details_2.addProperty("EBBS_INPROGRESS", ebbsInProgress);
			details_2.addProperty("FINANCE_INPROGRESS", financeInProgress);
			details_2.addProperty("FM_INPROGRESS", fmInProgress);
			details_2.addProperty("CADM_INPROGRESS", CADM_INPROGRESS);
			details_2.addProperty("Aparta_INPROGRESS", Aparta_INPROGRESS);



			teams.add(details); 
			teams.add(details_2); 
			
			status.add("teams",teams);
			
			
			status.addProperty("result", "success");
			status.addProperty("stscode", "HP00");
			status.addProperty("message", "SUCCESS_FAIL Data Retrieved Successfully !!");
		 }
		 catch(Exception e)
		 {
			 status.addProperty("result", "failed");
			 status.addProperty("stscode", "HP06");
			 status.addProperty("message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in SUCCESS_FAIL Success_fail:::: "+e.getLocalizedMessage());
		 }
		
		 return status;
	}

public JsonObject Success_fail() 
{
	JsonObject status = new JsonObject();
	
	try
	{

		Common_Utils util = new Common_Utils();
		 
		JsonArray teams = new JsonArray();  
		JsonObject details = new JsonObject();
	
		
		String fromDate = util.getCurrentDate("dd-MMM-yyyy");
		String toDate = util.getCurrentDate("dd-MMM-yyyy");
		
		
		String sql = "SELECT\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS EXCEL_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS EXCEL_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS ACBS_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS ACBS_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS CASH_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS CASH_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS GEMS_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS GEMS_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS TRADE_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type LIKE ?)) AS TRADE_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS CC_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS CC_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS EBBS_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS EBBS_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS Finance_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS Finance_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS FM_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ? and reportserial in (select REPORT_SERIAL from rts003 where source_type = ?)) AS FM_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS CADM_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS CADM_FAIL,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC = ? and paytype <> ?) AS APARTA_SUCCESS,\r\n"
				+ "(SELECT COUNT(*) FROM RTS006 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ? AND RESPDESC <> ? and paytype <> ?) AS APARTA_FAIL\r\n"
				+ "FROM DUAL";

		List<Map<String, Object>> result = Jdbctemplate.queryForList(sql, new Object[] {
			    "COUNTRY", fromDate , toDate , "SUCCESS","ARCHIVED", 
			    "COUNTRY", fromDate , toDate , "SUCCESS","ARCHIVED", 
			    "ACBS", fromDate , toDate , "SUCCESS", "ARCHIVED", "ACBS",
			    "ACBS", fromDate , toDate , "SUCCESS","ARCHIVED", "ACBS",
			    "CASH", fromDate , toDate , "SUCCESS", "ARCHIVED", "CASH%",
			    "CASH", fromDate , toDate , "SUCCESS","ARCHIVED", "CASH%",
			    "GEMS", fromDate , toDate , "SUCCESS", "ARCHIVED", 
			    "GEMS", fromDate , toDate , "SUCCESS","ARCHIVED", 
			    "TRADE", fromDate , toDate , "SUCCESS", "ARCHIVED", "Trade%",
			    "TRADE", fromDate , toDate , "SUCCESS","ARCHIVED", "Trade%",
			    "CC", fromDate , toDate , "SUCCESS", "ARCHIVED", 
			    "CC", fromDate , toDate , "SUCCESS","ARCHIVED", 
			    "EBBS", fromDate , toDate , "SUCCESS", "ARCHIVED", "EBBS",
			    "EBBS", fromDate , toDate , "SUCCESS","ARCHIVED", "EBBS",
			    "FINANCE", fromDate , toDate , "SUCCESS", "ARCHIVED", 
			    "FINANCE", fromDate , toDate , "SUCCESS","ARCHIVED", 
			    "FM", fromDate , toDate , "SUCCESS", "ARCHIVED", "FM",
			    "FM", fromDate , toDate , "SUCCESS","ARCHIVED", "FM",
			    "CADM", fromDate , toDate , "SUCCESS", "ARCHIVED", 
			    "CADM", fromDate , toDate , "SUCCESS","ARCHIVED", 
			    "APARTA", fromDate , toDate , "SUCCESS", "ARCHIVED", 
			    "APARTA", fromDate , toDate , "SUCCESS","ARCHIVED"
			});
		
			for (Map<String, Object> secret : result) 
			{
			    BigDecimal excelSuccess = (BigDecimal) secret.get("EXCEL_SUCCESS");
			    BigDecimal excelFail = (BigDecimal) secret.get("EXCEL_FAIL");
			    BigDecimal acbsSuccess = (BigDecimal) secret.get("ACBS_SUCCESS");
			    BigDecimal acbsFail = (BigDecimal) secret.get("ACBS_FAIL");
			    BigDecimal cashSuccess = (BigDecimal) secret.get("CASH_SUCCESS");
			    BigDecimal cashFail = (BigDecimal) secret.get("CASH_FAIL");
			    BigDecimal gemsSuccess = (BigDecimal) secret.get("GEMS_SUCCESS");
			    BigDecimal gemsFail = (BigDecimal) secret.get("GEMS_FAIL");
			    BigDecimal tradeSuccess = (BigDecimal) secret.get("TRADE_SUCCESS");
			    BigDecimal tradeFail = (BigDecimal) secret.get("TRADE_FAIL");
			    BigDecimal ccSuccess = (BigDecimal) secret.get("CC_SUCCESS");
			    BigDecimal ccFail = (BigDecimal) secret.get("CC_FAIL");
			    BigDecimal ebbsSuccess = (BigDecimal) secret.get("EBBS_SUCCESS");
			    BigDecimal ebbsFail = (BigDecimal) secret.get("EBBS_FAIL");
			    BigDecimal financeSuccess = (BigDecimal) secret.get("Finance_SUCCESS");
			    BigDecimal financeFail = (BigDecimal) secret.get("Finance_FAIL");
			    BigDecimal fmSuccess = (BigDecimal) secret.get("FM_SUCCESS");
			    BigDecimal fmFail = (BigDecimal) secret.get("FM_FAIL");
			    BigDecimal CADM_SUCCESS = (BigDecimal) secret.get("CADM_SUCCESS");
			    BigDecimal CADM_FAIL = (BigDecimal) secret.get("CADM_FAIL");
			    BigDecimal APARTA_SUCCESS = (BigDecimal) secret.get("APARTA_SUCCESS");
			    BigDecimal APARTA_FAIL = (BigDecimal) secret.get("APARTA_FAIL");
			   
			    details.addProperty("EXCEL_SUCCESS", excelSuccess.intValue());
			    details.addProperty("EXCEL_FAIL", excelFail.intValue());
			    details.addProperty("ACBS_SUCCESS", acbsSuccess.intValue());
			    details.addProperty("ACBS_FAIL", acbsFail.intValue());
			    details.addProperty("CASH_SUCCESS", cashSuccess.intValue());
			    details.addProperty("CASH_FAIL", cashFail.intValue());
			    details.addProperty("GEMS_SUCCESS", gemsSuccess.intValue());
			    details.addProperty("GEMS_FAIL", gemsFail.intValue());
			    details.addProperty("TRADE_SUCCESS", tradeSuccess.intValue());
			    details.addProperty("TRADE_FAIL", tradeFail.intValue());
			    details.addProperty("CC_SUCCESS", ccSuccess.intValue());
			    details.addProperty("CC_FAIL", ccFail.intValue());
			    details.addProperty("EBBS_SUCCESS", ebbsSuccess.intValue());
			    details.addProperty("EBBS_FAIL", ebbsFail.intValue());
			    details.addProperty("Finance_SUCCESS", financeSuccess.intValue());
			    details.addProperty("Finance_FAIL", financeFail.intValue());
			    details.addProperty("FM_SUCCESS", fmSuccess.intValue());
			    details.addProperty("FM_FAIL", fmFail.intValue());
			    details.addProperty("CADM_SUCCESS", CADM_SUCCESS.intValue());
			    details.addProperty("CADM_FAIL", CADM_FAIL.intValue());
			    details.addProperty("APARTA_SUCCESS", APARTA_SUCCESS.intValue());
			    details.addProperty("APARTA_FAIL", APARTA_FAIL.intValue());
			}
			

			
			JsonObject details_2 = new JsonObject();
		
			
			 sql = "SELECT\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS EXCEL_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS ACBS_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS CASH_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS GEMS_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS TRADE_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS CC_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS EBBS_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS FINANCE_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS FM_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS CADM_INPROGRESS,\r\n"
			 		+ "(SELECT COUNT(*) FROM RTS005 WHERE APICODE IN (select APICODE FROM DOMAIN001 WHERE DOMAIN = ?) AND REQDATE BETWEEN ? AND ?) AS Aparta_INPROGRESS\r\n"
			 		+ "FROM DUAL";
			 
			 
			List<Map<String, Object>> result_1 = Jdbctemplate.queryForList(sql, new Object[] {
					"COUNTRY",fromDate , toDate ,  "ACBS",fromDate , toDate , "CASH",fromDate , toDate , "GEMS",fromDate , toDate ,
					"TRADE",fromDate , toDate ,
				    "CC",fromDate , toDate , "EBBS",fromDate , toDate , "FINANCE",fromDate , toDate , "FM",fromDate , toDate,
				    "CADM",fromDate , toDate , "APARTA",fromDate , toDate
				});
			
			Map<String, Object> result_2 = result_1.get(0);
			
			BigDecimal excelInProgress = (BigDecimal) result_2.get("EXCEL_INPROGRESS");
			BigDecimal acbsInProgress = (BigDecimal) result_2.get("ACBS_INPROGRESS");
			BigDecimal cashInProgress = (BigDecimal) result_2.get("CASH_INPROGRESS");
			BigDecimal gemsInProgress = (BigDecimal) result_2.get("GEMS_INPROGRESS");
			BigDecimal tradeInProgress = (BigDecimal) result_2.get("TRADE_INPROGRESS");
			BigDecimal marcisInProgress = (BigDecimal) result_2.get("MARCIS_INPROGRESS");
			BigDecimal ccInProgress = (BigDecimal) result_2.get("CC_INPROGRESS");
			BigDecimal ebbsInProgress = (BigDecimal) result_2.get("EBBS_INPROGRESS");
			BigDecimal financeInProgress = (BigDecimal) result_2.get("FINANCE_INPROGRESS");
			BigDecimal fmInProgress = (BigDecimal) result_2.get("FM_INPROGRESS");
			BigDecimal CADM_INPROGRESS = (BigDecimal) result_2.get("CADM_INPROGRESS");
			BigDecimal Aparta_INPROGRESS = (BigDecimal) result_2.get("Aparta_INPROGRESS");
		
			details_2.addProperty("EXCEL_INPROGRESS", excelInProgress);
			details_2.addProperty("ACBS_INPROGRESS", acbsInProgress);
			details_2.addProperty("CASH_INPROGRESS", cashInProgress);
			details_2.addProperty("GEMS_INPROGRESS", gemsInProgress);
			details_2.addProperty("TRADE_INPROGRESS", tradeInProgress);
			details_2.addProperty("MARCIS_INPROGRESS", marcisInProgress);
			details_2.addProperty("CC_INPROGRESS", ccInProgress);
			details_2.addProperty("EBBS_INPROGRESS", ebbsInProgress);
			details_2.addProperty("FINANCE_INPROGRESS", financeInProgress);
			details_2.addProperty("FM_INPROGRESS", fmInProgress);
			details_2.addProperty("CADM_INPROGRESS", CADM_INPROGRESS);
			details_2.addProperty("Aparta_INPROGRESS", Aparta_INPROGRESS);

			teams.add(details); 
			teams.add(details_2); 
			
			status.add("teams",teams);
			
			status.addProperty("result", "success");
			status.addProperty("stscode", "HP00");
			status.addProperty("message", "SUCCESS_FAIL Data Retrieved Successfully !!");
	
	}
	 catch(Exception e)
	 {
		 status.addProperty("result", "failed");
		 status.addProperty("stscode", "HP06");
		 status.addProperty("message", e.getLocalizedMessage());  	
		 
		 logger.debug("Exception in SUCCESS_FAIL Success_fail:::: "+e.getLocalizedMessage());
	 }
	
	 return status;
}

public JsonObject overall_card() 
{
	JsonObject details = new JsonObject();
		
	try
	{

		Common_Utils util = new Common_Utils();
		 
		String fromDate = util.getCurrentDate("dd-MMM-yyyy");
		String toDate = util.getCurrentDate("dd-MMM-yyyy");
		
		
		
		String sql = "select\r\n" + 
				"(select COUNT(*) from rts006 where respdesc = ? and reqdate between ? and ? and paytype <> ?) AS SUCCESS,\r\n" + 
				"(select COUNT(*) from rts006 where respdesc <> ? and reqdate between ? and ? and paytype <> ?) AS FAILED,\r\n" + 
				"(select COUNT(*) from rts005  where reqdate between ? and ?) AS INPROGRESS,\r\n" + 
				"(select COUNT(*) from rts006 where respdesc = ? and reqdate between ? and ? and paytype <> ?) AS TIMEOUT\r\n" + 
				"from dual";

			List<Map<String, Object>> result = Jdbctemplate.queryForList(sql, new Object[] {"SUCCESS" , fromDate , toDate , "ARCHIVED" , "SUCCESS" , fromDate , toDate ,"ARCHIVED", fromDate , toDate , "timeout", fromDate , toDate , "ARCHIVED"});
	
			for (Map<String, Object> row : result)
			{
			    BigDecimal success = (BigDecimal) row.get("SUCCESS");
			    BigDecimal failed = (BigDecimal) row.get("FAILED");
			    BigDecimal inProgress = (BigDecimal) row.get("INPROGRESS");
			    BigDecimal timeout = (BigDecimal) row.get("TIMEOUT");

			    details.addProperty("SUCCESS", success.intValue());
			    details.addProperty("FAILED", failed.intValue());
			    details.addProperty("INPROGRESS", inProgress.intValue());
			    details.addProperty("TIMEOUT", timeout.intValue());
			}
			
		
		details.addProperty("result", "success");
		details.addProperty("stscode", "HP00");
		details.addProperty("message", "overall Data Retrieved Successfully !!");
	 }
	 catch(Exception e)
	 {
		 details.addProperty("result", "failed");
		 details.addProperty("stscode", "HP06");
		 details.addProperty("message", e.getLocalizedMessage());  	
		 
		 logger.debug("Exception in overall Success_fail:::: "+e.getLocalizedMessage());
	 }
	
	 return details;
}

public JsonObject overall_card_two(String fromDate , String toDate) 
{
	JsonObject details = new JsonObject();
		
	try
	{

		Common_Utils util = new Common_Utils();

		fromDate =  util.Convert_Date_Format(fromDate, "yyyy-MM-dd", "dd-MMM-yyyy");
		toDate = util.Convert_Date_Format(toDate, "yyyy-MM-dd", "dd-MMM-yyyy");

		
		
		String sql = "select\r\n" + 
				"(select COUNT(*) from rts006 where respdesc = ? and reqdate between ? and ? and paytype <> ?) AS SUCCESS,\r\n" + 
				"(select COUNT(*) from rts006 where respdesc <> ? and reqdate between ? and ? and paytype <> ?) AS FAILED,\r\n" + 
				"(select COUNT(*) from rts005  where reqdate between ? and ?) AS INPROGRESS,\r\n" + 
				"(select COUNT(*) from rts006 where respdesc = ? and reqdate between ? and ? and paytype <> ?) AS TIMEOUT\r\n" + 
				"from dual";

			List<Map<String, Object>> result = Jdbctemplate.queryForList(sql, new Object[] {"SUCCESS" , fromDate , toDate , "ARCHIVED" , "SUCCESS" , fromDate , toDate ,"ARCHIVED", fromDate , toDate , "timeout", fromDate , toDate , "ARCHIVED"});
	
			for (Map<String, Object> row : result)
			{
			    BigDecimal success = (BigDecimal) row.get("SUCCESS");
			    BigDecimal failed = (BigDecimal) row.get("FAILED");
			    BigDecimal inProgress = (BigDecimal) row.get("INPROGRESS");
			    BigDecimal timeout = (BigDecimal) row.get("TIMEOUT");

			    details.addProperty("SUCCESS", success.intValue());
			    details.addProperty("FAILED", failed.intValue());
			    details.addProperty("INPROGRESS", inProgress.intValue());
			    details.addProperty("TIMEOUT", timeout.intValue());
			}
			
		
		details.addProperty("result", "success");
		details.addProperty("stscode", "HP00");
		details.addProperty("message", "overall Data Retrieved Successfully !!");
	 }
	 catch(Exception e)
	 {
		 details.addProperty("result", "failed");
		 details.addProperty("stscode", "HP06");
		 details.addProperty("message", e.getLocalizedMessage());  	
		 
		 logger.debug("Exception in overall Success_fail:::: "+e.getLocalizedMessage());
	 }
	
	 return details;
}


public class Domain001Mapper implements RowMapper<Domain> 
{
    Common_Utils util = new Common_Utils();

    public Domain mapRow(ResultSet rs, int rowNum) throws SQLException {
        Domain domainInfo = new Domain();

        // Map the columns from the result set to the Domain object
        domainInfo.setSuborgcode(util.ReplaceNull(rs.getString("SUBORGCODE")));
        domainInfo.setDOMAIN(util.ReplaceNull(rs.getString("DOMAIN")));
        domainInfo.setAPICODE(util.ReplaceNull(rs.getString("APICODE")));
        domainInfo.setAPI_NAME(util.ReplaceNull(rs.getString("API_NAME")));
        domainInfo.setStatus(util.ReplaceNull(rs.getString("STATUS")));

        return domainInfo;
    }
}

}