package com.hdsoft.models;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.FMAPICallLog;
import com.hdsoft.Repositories.Lookup001;
import com.hdsoft.Repositories.Prop001;
import com.hdsoft.common.Common_Utils;
import com.opencsv.CSVReader;
import com.zaxxer.hikari.HikariDataSource;
	
@Controller
@Component
public class BOT_Suptect_FM 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
			
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_FM.class);
	
	@Autowired
	public RTSIS_AutoMan_Modal AutoMan;
	
	@Autowired
	public Webservice_Modal WB;
	
	@Autowired
	public Webservice_call_Modal WCB;
	
	@Autowired
	public Sysconfig Sys;
	
	//@Async
	@Scheduled(cron = "0 0/15 * * * *")   //Every 15 mins
    public void FM_Thread()  
    {
		 try 
		 {
			 String sql = "select count(*) from prop001 where chcode = ? and mtypeparam = ? and userid = ?";  // checking the active environment
			 
			 int count = Jdbctemplate.queryForObject(sql, new Object[] { "ENV", new Sysconfig().getHostAddress(), "1" }, Integer.class);
			 
			 if(count == 1)
			 {
				 sql = "select * from fm_api_call_log002 where status=?";
				 
				 List<Map<String, Object>> apis = Jdbctemplate.queryForList(sql, new Object[] { "1" });

	             for(Map<String, Object> api : apis) 
	             {
	                 String FM_CODE =  (String) api.get("FM_CODE");
	                 String API_CODE = (String) api.get("API_CODE");
	                 String API_NAME =  (String) api.get("API_NAME");
	                 
	                 String SQL = "select * from fm_api_call_log001 x where fm_code = ? and api_code = ? and rescode = ? and\r\n" + 
	                 		"end_time = (select max(end_time) from fm_api_call_log001 y where y.fm_code = x.fm_code and y.api_code = x.api_code and y.rescode = x.rescode) and rownum=?";
	                
	                 List<FMAPICallLog> Products = Jdbctemplate.query(SQL, new Object[] { FM_CODE, API_CODE, "200", "1" }, new FMAPICallLogMapper());
	                 
	                 for(FMAPICallLog info : Products)
	                 {
	                	 FM_MASTER(API_CODE, info.getEndTime(), FM_CODE, API_NAME); 
	                 }
	             }
			 }	 
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in FM_Thread :::: "+e.getLocalizedMessage());
		 }
    }
	
	@Scheduled(cron = "0 0 0 * * *")  // Every night 12 AM
    public void ifemquotes_fm_Thread()  
    {
		 try 
		 {
			 String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";

			 List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "ifem_fm" }, String.class);

			 if(prop.size() > 0)
			 {
				 String filepath = prop.get(0);

				 Ifemquotes("RTS131", "", filepath);

			 }
			 
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in ifemquotes_fm_Thread :::: "+e.getLocalizedMessage());
		 }
    }
	
	@RequestMapping(value = {"/Datavision/ifemquotes"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String ifem_data(HttpServletRequest request,  HttpServletResponse response,  HttpSession session) throws IOException 
	 {		
		 try 
		 {
			 String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";

			 List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "ifem_fm" }, String.class);

			 if(prop.size() > 0)
			 {
				 String filepath = prop.get(0);

				 Ifemquotes("RTS131", "", filepath);

			 }
			 
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception in ifemquotes_fm_Thread :::: "+e.getLocalizedMessage());
		 }
		 return null;     
    }
	
	public JsonObject Ifemquotes(String INFO1, String INFO2, String INFO3) // RTS131 - DONE
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
		 
			int count = 1;	 
			 
	        File folder = new File(INFO3);
	        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
	
	        if (files != null && files.length > 0) 
	        {
	            for (File file : files) 
	            {
	                try (CSVReader csvReader = new CSVReader(new FileReader(file))) 
	                {
	                    List<String[]> rows = csvReader.readAll();

	                    for (int i = 1; i < rows.size(); i++) 
	                    { 
	                        String[] row = rows.get(i);
			 
	                        String reportingDate = util.getCurrentReportDate();
	                        String bankCode = util.isNullOrEmpty(row[2].trim()) ? "SCBLTZTXXXX" : row[2].trim();
	                        String currency = util.isNullOrEmpty(row[3].trim()) ? "834" : row[3].trim();
	                        String tzsBidPrice = util.isNullOrEmpty(row[4].trim()) ? "0.00" : row[4].trim();
	                        String tzsAskPrice = util.isNullOrEmpty(row[5].trim()) ? "0.00" : row[5].trim();
	                        
	           			    String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
	        			 
		        			List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
		        					
		        			currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
                        
							sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8 , COLUMN9) values\r\n" + 
									   "(?,?,?,?,?,?,?,?,?,?,?)";
						 
							Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "ifemQuotes", count,reportingDate, bankCode, currency, tzsBidPrice, tzsAskPrice});

							count++ ;
	                    }
	                }
    
	    	        if(count > 0)
	    	        {
	    	        
	    			 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8 ,COLUMN9) values\r\n" + 
	    					   "(?,?,?,?,?,?,?,?,?,?,?)";
	    		 
	    			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "ifemQuotes", "serial" , "reportingDate", "bankCode", "currency", "tzsBidPrice", "tzsAskPrice" });
	    			 
	    			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
	    				
	    			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "ifemQuotes"});
	    			 
	    			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
	    			 
	    		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "others", "ifemQuotes", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
	    		
	    		     AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
	    	        }
	                                
	                try 
	                {
                        
                        String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";
                  
	           			 List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "Ifem_Bk" }, String.class);
	                  			 
	          			 if(prop.size() > 0)
	          			 {
	          				String targetFolderPath = prop.get(0);                       
	                  
		                    File backupDir = new File(targetFolderPath);
		                 
		                    if (backupDir.exists() && backupDir.isDirectory()) 
		                    {
		                        Path sourcePath = file.toPath();
		                        Path targetPath = new File(backupDir, file.getName()).toPath();
		                        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		                    } 
		                    else 
		                    {
		                    	logger.warn("Backup directory does not exist for ifemQuotes. File will not be moved.");
		                    }
		          		 }
	          			 else 
	          			 {
	          				 logger.warn("Path is not defined on porp001 table for ifemQuotes file backup");
						 }
	          			 
	                } 
	                catch (IOException moveEx) 
	                {
	                    logger.debug("Failed to move file: " + file.getName() + " -> " + moveEx.getMessage());
	                }              

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
			 
			 logger.debug("Exception in ifemQuotes :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject FM_MASTER(String INFO1, String INFO2, String INFO3, String API_NAME)
	{
		JsonObject details = new JsonObject();
		
		try
		{
			String tm = "select TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS.FF3') from dual";
       	 
			String to_date = Jdbctemplate.queryForObject(tm, String.class);
			
			String from_date = INFO2 ; 
			
			INFO2 = from_date + "|" +to_date;
       	 
			if(INFO1.equalsIgnoreCase("RTS135")) details = TbondTransaction(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS129")) details = forexTransaction(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS133")) details = ibcmTransaction(INFO1, INFO2, INFO3);
			if(INFO1.equalsIgnoreCase("RTS071")) details = interbankLoanPayable(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS067")) details = DepositWithdrawalData(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS085")) details = boughtForwardExchangeData(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS017")) details = interbankLoansReceivable(INFO1, INFO2, INFO3);
			if(INFO1.equalsIgnoreCase("RTS005")) details = invDebtSecuritiesData(INFO1, INFO2, INFO3);  
			if(INFO1.equalsIgnoreCase("RTS107")) details = currencySwapData(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS099")) details = securitiesSoldData(INFO1, INFO2, INFO3);	 	 
			if(INFO1.equalsIgnoreCase("RTS101")) details = securitiesPurchasedData(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS087")) details = SoldForwardExchangeData(INFO1, INFO2, INFO3); 
			if(INFO1.equalsIgnoreCase("RTS109")) details = interestRateSwapData(INFO1, INFO2, INFO3);  
			
			String O_SERIAL = Generate_api_Serial().get("Serial").getAsString();
			
			String Status = details.has("status") ? details.get("status").getAsString() : "F";
			
			String totalpages = details.has("totalpages") ? details.get("totalpages").getAsString() : "0";
			
			String rescode = details.has("rescode") ? details.get("rescode").getAsString() : "501";
			
			String sql = "insert into fm_api_call_log001(SUBORGCODE,REQSL,REQDATE,FM_CODE,API_NAME, API_CODE,START_TIME,END_TIME,PAGESIZE,STATUS,RESCODE) "+
		                  "values(?,?,sysdate,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { details.get("suborgcode").getAsString(), O_SERIAL,  INFO3, API_NAME, INFO1, convertToTimestamp(from_date), convertToTimestamp(to_date),
					 totalpages, Status , rescode });
		}
		catch(Exception ex)
		{
			 logger.debug("Exception in FM_MASTER :::: "+ex.getLocalizedMessage());
		}
		
		return details;
	}

	public JsonObject TbondTransaction(String INFO1, String INFO2, String INFO3) //RTS135  FM007 verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //

			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3);
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //

			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false; 
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
	
				 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
						 
				 Response_Code = api_out.get("Response_Code").getAsInt();
				 
				 String Response = api_out.get("Response").getAsString();
				 
				 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
				 {
					 flag = true;
					 
					 JsonObject Res = util.StringToJsonObject(Response);
					 
					 totalPages = Res.get("totalPages").getAsLong();   //
					 
					 JsonArray content = Res.get("content").getAsJsonArray();
					
					 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
					
					 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
					 
					 int count = 0;
					 
					 for(int i=0; i<content.size(); i++)
					 {
						 JsonObject js = content.get(i).getAsJsonObject();
						 
						 count++;
						 
						 String reportingDate = getMember(js, "ReportingDate");
						 String tradeDate = getMember(js, "trdeDate"); 
						 String sellerName = getMember(js, "sellerName"); 
						 String buyerName = getMember(js, "BuyerName"); 
						 String tbondAuctionNumber = getMember(js, "tbondAuctionNumber"); 
						 String ISIN = getMember(js, "ISIN"); 
						 String issueDate = getMember(js, "issueDate"); 
						 String maturityDate = getMember(js, "maturityDate"); 
						 String tzsAmount = getMember(js, "tzsAmount"); 
						 String yield = getMember(js, "yield"); 
						 String couponRate = getMember(js, "couponRate"); 
						 String price = getMember(js, "price"); 
						 String tenure = getMember(js, "tenure");  // tneue%365 and refer D143
						 
						 if(util.isNullOrEmpty(price))
						 {
							 price = "0"; 
						 }
						 
						 if(util.isNullOrEmpty(issueDate))
						 {
							 issueDate = getMember(js, "issue_date"); 
						 }
						 
						 if(!util.isNumeric(tzsAmount))
						 {
							 String Input_Notional_Amount = getMember(js, "Input_Notional_Amount");
							 String Input_Notional_Amount_Currency = getMember(js, "Input_Notional_Amount_Currency");
							 
							 if(!util.isNullOrEmpty(Input_Notional_Amount) && !util.isNullOrEmpty(Input_Notional_Amount_Currency))
							 {
								 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
								 
								 JsonObject rates = fx.find_exchangeRate(issueDate, Input_Notional_Amount, Input_Notional_Amount_Currency);
								 
								 tzsAmount = rates.get("tzs").getAsString();
							 }
						 }
						 		 
						 issueDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd");
						 tradeDate = util.Convert_BOT_Date_Format(tradeDate, "yyyy-MM-dd'T'HH:mm:ss");
						 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd");
						 
						 maturityDate = convertToEndOfDay(maturityDate);
						 
						 yield = util.TwoDecimals(yield);
						 couponRate = util.TwoDecimals(couponRate);
						 tenure = util.isNullOrEmpty(tenure) ? "0" : tenure;
						 
						 String sql = "select (?/365) from dual";
						 
						 Double tnu = Jdbctemplate.queryForObject(sql, new Object[] {tenure }, Double.class);
						 
						 if(tnu <= 2)
						 {
							 tenure = "1";
						 }
						 else if(tnu <= 5)
						 {
							 tenure = "2";
						 }
						 else if(tnu <= 7)
						 {
							 tenure = "3";
						 }
						 else if(tnu <= 10)
						 {
							 tenure = "4";
						 }
						 else if(tnu <= 15)
						 {
							 tenure = "5";
						 }
						 else if(tnu <= 20)
						 {
							 tenure = "6";
						 }
						 else //if(tnu <= 25)
						 {
							 tenure = "7";
						 }
						 
						 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "tbondTransaction", count, reportingDate, tradeDate, sellerName,
								 buyerName, tbondAuctionNumber,  ISIN, issueDate, maturityDate, tzsAmount,yield, couponRate, price, tenure  });
						 
					 }
					 
					 if(count > 0)
					 {
						 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "tbondTransaction", "serial", "reportingDate", "tradeDate", "sellerName",
								 "buyerName", "tbondAuctionNumber",  "ISIN", "issueDate", "maturityDate", "tzsAmount", "yield", "couponRate", "price", "tenure"  });
						 
						 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
							
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "tbondTransaction"});
						 
						 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
						 
					     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Banks Data", "tbondTransaction", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
						
						 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
						 
						 details.addProperty("Serial", O_SERIAL);
				         details.addProperty("Batch_id", Batch_id);
					 }
				 }
				 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in TbondTransaction :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject forexTransaction(String INFO1, String INFO2, String INFO3) //FM005 RTS129  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false; 
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String Input_SCI_LEID = getMember(js, "Input_SCI_LEID"); 
					 String input_Counterparty_segment = getMember(js, "input_Counterparty_segment");
					 String input_counterparty_long_name = getMember(js, "input_counterparty_long_name");
					 String input_booking_entity_name = getMember(js, "input_booking_entity_name");
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String transactionDate = getMember(js, "transactionDate"); 
					 String valueDate = getMember(js, "valueDate"); 
					 String marketType = getMember(js, "marketType"); 
					 String sellerName = getMember(js, "sellerName"); 
					 String buyerName = getMember(js, "BuyerName"); 
					 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "SEGMENTCODE", input_Counterparty_segment); //cc
					 String buyerEconomicActivity = ST.FindElementFromFileIT("SCI", "LOANECONOMICACTIVITY", "LEID", Input_SCI_LEID); //cc; 
					 String buyerCountry = getMember(js, "buyerCountry");  
					 String currency = getMember(js, "currency"); 
					 String exchangeRate = getMember(js, "exchangeRate"); 
					 String orgAmountOffered = getMember(js, "orgAmountOffered"); 
					 String tzsAmountOffered = getMember(js, "tzsAmountOffered"); 
					 String usdAmountOffered = getMember(js, "usdAmountOffered"); 
					 String orgAmountSold = getMember(js, "orgAmountSold"); 
					 String tzsAmountSold = getMember(js, "tzsAmountSold");
					 String usdAmountSold = getMember(js, "usdAmountSold");
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountOffered, currency);
					 
					 usdAmountOffered = rates.get("usd").getAsString();
					 tzsAmountOffered = rates.get("tzs").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountSold, currency);
					 
					 usdAmountSold = rates.get("usd").getAsString();
					 tzsAmountSold = rates.get("tzs").getAsString();
					 
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
					 valueDate = util.Convert_BOT_Date_Format(valueDate, "yyyy-MM-dd'T'HH:mm:ss");
					 
					 valueDate = convertToEndOfDay(valueDate);
					 
					 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";   //get the updated lov lst from abitha
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "BIC", input_counterparty_long_name }, new Lookup001_mapper());
						
					 sellerName = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "SCBLTZTXXXX";
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BIC", input_booking_entity_name }, new Lookup001_mapper());
						
					 buyerName = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "SCBLTZTXXXX";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 			
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", buyerCountry }, new Lookup001_mapper());
						
					 buyerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					 			 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "forexTransaction", count, reportingDate, transactionDate, valueDate, marketType, sellerName,
							 buyerName, sectorSnaClassification,  buyerEconomicActivity, buyerCountry, currency, exchangeRate,orgAmountOffered, tzsAmountOffered, usdAmountOffered, orgAmountSold,
							 tzsAmountSold, usdAmountSold}); 
				 }
				 
				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "forexTransaction", "serial", "reportingDate", "transactionDate", "valueDate", "marketType", "sellerName",
							 "buyerName", "sectorSnaClassification",  "buyerEconomicActivity", "buyerCountry", "currency", "exchangeRate","orgAmountOffered", "tzsAmountOffered", "usdAmountOffered", "orgAmountSold",
							 "tzsAmountSold", "usdAmountSold" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "forexTransaction"});
					 
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					 
				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Banks Data", "forexTransaction", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					
					 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 	CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in forexTransaction :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject ibcmTransaction(String INFO1, String INFO2, String INFO3) //RTS133  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
				 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String transactionDate = getMember(js, "transactionDate"); 
					 String lenderName = getMember(js, "lenderName"); 
					 String borrowerName = getMember(js, "borrowerName"); 
					 String transactionType = getMember(js, "transactionType"); 
					 String tzsAmount = getMember(js, "NotionalAmount");  
					 String tenure = getMember(js, "tenure"); 
					 String interestRate = getMember(js, "interestRate"); 
					 
					 String input_lenderName = getMember(js, "input_lenderName"); 
					 String input_borrowerName = getMember(js, "input_borrowerName"); 
				
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
					 
					 tenure = util.isNullOrEmpty(tenure) ? "0" : tenure;		 
					 tenure = Integer.parseInt(tenure) < 0 ? "0" : tenure;
					 
					 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";   //get the updated lov lst from abitha
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "BIC", input_lenderName }, new Lookup001_mapper());
						
					 lenderName = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "SCBLTZTXXXX";
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "BIC", input_borrowerName }, new Lookup001_mapper());
						
					 borrowerName = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "SCBLTZTXXXX";
					 
					 /*sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "FM001", transactionType }, new Lookup001_mapper());
						
					 transactionType = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : ""; */
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "ibcmTransaction", count, reportingDate, transactionDate, lenderName, borrowerName, transactionType,
							 tzsAmount, tenure,  interestRate}); 
				 }
				 
				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "ibcmTransaction", "serial", "reportingDate", "transactionDate", "lenderName", "borrowerName", "transactionType",
							 "tzsAmount", "tenure",  "interestRate"});
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "ibcmTransaction"});
					 
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					 
				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Other Banks Data", "ibcmTransaction", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					
					AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in ibcmTransaction :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject interbankLoanPayable(String INFO1, String INFO2, String INFO3) //RTS071 FM003 verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String lenderName = getMember(js, "LenderName"); 
					 String accountNumber = getMember(js, "accountNumber"); 
					 String lenderCountry = getMember(js, "lenderCountry"); 
					 String borrowingType = getMember(js, "borrowing_type"); 
					 String transactionDate = getMember(js, "TransactionDate"); 
					 String disbursementDate = getMember(js, "disbursementDate"); 
					 String maturityDate = getMember(js, "maturityDate"); 
					 String currency = getMember(js, "currency"); 
					 String orgAmountOpening = getMember(js, "orgAmountOpening"); 
					 String usdAmountOpening = "0"; 
					 String tzsAmountOpening = "0"; 
					 String orgAmountRepayment = getMember(js, "orgAmountRepayment"); 
					 String usdAmountRepayment = "0";  
					 String tzsAmountRepayment = "0"; 
					 String orgAmountClosing = getMember(js, "orgAmountClosing"); 
					 String usdAmountClosing = "0"; 
					 String tzsAmountClosing = "0"; 
					 String tenureDays = getMember(js, "TenureDays"); 
					 String annualInterestRate = getMember(js, "annualInterestRate"); 
					 String interestRateType = getMember(js, "interestRateType"); 
					 
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd'T'HH:mm:ss");
					 disbursementDate = util.Convert_BOT_Date_Format(disbursementDate, "yyyy-MM-dd'T'HH:mm:ss");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd'T'HH:mm:ss");
					 
					 maturityDate = convertToEndOfDay(maturityDate);
					 
					 tenureDays = util.isNullOrEmpty(tenureDays) ? "0" : tenureDays;
					 tenureDays = Integer.parseInt(tenureDays) < 0 ? "0" : tenureDays;
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountOpening, currency);
					 
					 usdAmountOpening = rates.get("usd").getAsString();
					 tzsAmountOpening = rates.get("tzs").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountRepayment, currency);
					 
					 usdAmountRepayment = rates.get("usd").getAsString();
					 tzsAmountRepayment = rates.get("tzs").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountClosing, currency);
					 
					 usdAmountClosing = rates.get("usd").getAsString();
					 tzsAmountClosing = rates.get("tzs").getAsString();
					 
					 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 			
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", lenderCountry }, new Lookup001_mapper());
						
					 lenderCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "interbankLoanPayable", count, reportingDate, lenderName, accountNumber, lenderCountry, borrowingType,
							 transactionDate, disbursementDate,  maturityDate, currency, orgAmountOpening,usdAmountOpening, tzsAmountOpening, orgAmountRepayment, usdAmountRepayment,
							 tzsAmountRepayment,orgAmountClosing,usdAmountClosing,tzsAmountClosing,tenureDays,annualInterestRate,interestRateType});
				 }
				
				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "interbankLoanPayable", "serial", "reportingDate", "lenderName", "accountNumber", "lenderCountry", "borrowingType","transactionDate","disbursementDate",
							 "maturityDate", "currency",  "orgAmountOpening", "usdAmountOpening", "tzsAmountOpening", "orgAmountRepayment", "usdAmountRepayment", "tzsAmountRepayment", "orgAmountClosing", "usdAmountClosing","tzsAmountClosing","tenureDays","annualInterestRate","interestRateType" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "interbankLoanPayable"});
					
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				     
					 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liability", "interbankLoanPayable", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					
				     AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
				     details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			
			 logger.debug("Exception in interbankLoanPayable :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
		
	public JsonObject DepositWithdrawalData(String INFO1, String INFO2, String INFO3) //RTS067 FM004 verified
	{ 
		 JsonObject details = new JsonObject();

		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //

				 JsonArray content = Res.get("content").getAsJsonArray();

				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();

				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();

				 int count = 0;

				 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();

					 count++;

					 String Commission_Amt_input =  getMember(js, "Commission_Amt_input"); 
					 String Commission_Amt_Currency_input =  getMember(js, "Commission_Amt_Currency_input"); 
					 String Input_SCI_LEID = getMember(js, "Input_SCI_LEID");
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String accountNumber = getMember(js, "accountNumber"); 
					 String accountName = getMember(js, "accountName"); 
					 String customerCategory = getMember(js, "customerCategory"); 
					 String branchCode = "008300"; //getMember(js, "branchCode"); 
					 String district = getMember(js, "district");  //lov
					 String region = getMember(js, "region");  //lov
					 String accountProductName = getMember(js, "accountProductName"); 
					 String accountType = getMember(js, "accountType");  //lov
					 String accountSubType = getMember(js, "accountSubType");  //lov
					 String depositCategory = getMember(js, "depositcategory");  //lov
					 String depositAccountStatus = getMember(js, "depositAccountStatus");  //lov
					 String clientIdentificationNumber = getMember(js, "clientIdentificationNumber"); 
					 String customerCountry = getMember(js, "customerCountry");   //lov
					 String clientType = getMember(js, "clientType");  //lov
					 String relationshipType = "";  // //lov
					 String transactionUniqueRef = getMember(js, "transactionUniqueRef"); 
					 String timeStamp = getMember(js, "time_Stamp"); 
					 String serviceChannel = getMember(js, "serviceChannel");  //lov
					 String currency = getMember(js, "currency");  //lov
					 String transactionType = getMember(js, "transactionType");  //lov
					 String orgTransactionAmount = getMember(js, "orgTransactionAmount"); 
					 String usdTransactionAmount = getMember(js, "usdTransactionAmount"); 
					 String tzsTransactionAmount = getMember(js, "tzsTransactionAmount"); 
					 String transactionPurposes = getMember(js, "transactionPurposes"); 
					 String sectorSnaClassification = ST.FindElementFromFileIT("SCI", "SNA", "LEID", Input_SCI_LEID); //cc
					 String lienNumber = "0"; 
					 String orgAmountLien = "0";
					 String usdAmountLien = "0"; 
					 String tzsAmountLien = "0";
					 String contractDate = getMember(js, "contractDate"); 
					 String maturityDate = getMember(js, "maturityDate"); 
					 String annualInterestRate = getMember(js, "annualInterestRate"); 
					 String interestRateType = getMember(js, "interestRateType");   //lov
					 String orgInterestAmount = getMember(js, "orgInterestAmount"); 
					 String usdInterestAmount = getMember(js, "usdInterestAmount"); 
					 String tzsInterestAmount = getMember(js, "tzsInterestAmount"); 
					 
					 annualInterestRate = util.TwoDecimals(annualInterestRate);
					 
					 if(!Commission_Amt_Currency_input.equals("TZS"))
					 {
						 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
						 
						 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), Commission_Amt_input, Commission_Amt_Currency_input);
						 
						 Commission_Amt_input = rates.get("tzs").getAsString();
					 }
					 
					 String sql = "select (?-?) from dual";
					 
					 orgInterestAmount = Jdbctemplate.queryForObject(sql, new Object[] { Commission_Amt_input, orgTransactionAmount }, String.class);
					 
					 orgInterestAmount = Math.abs(Double.parseDouble(orgInterestAmount))+"";
					 
					 relationshipType = "1";  //Need to work
					 //sectorSnaClassification = "1";
					 					 
					 contractDate = util.Convert_BOT_Date_Format(contractDate, "yyyy-MM-dd'T'HH:mm:ss");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd'T'HH:mm:ss");
					 timeStamp = util.Convert_BOT_Date_Format(timeStamp, "yyyy-MM-dd'T'HH:mm:ss");
					 
					 contractDate = convertToEndOfDay(contractDate);
					 maturityDate = convertToEndOfDay(maturityDate);
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgTransactionAmount, currency);
					 
					 tzsTransactionAmount = rates.get("tzs").getAsString();
					 usdTransactionAmount = rates.get("usd").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountLien, currency);
					 
					 tzsAmountLien = rates.get("tzs").getAsString();
					 usdAmountLien = rates.get("usd").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgInterestAmount, currency);
					 
					 tzsInterestAmount = rates.get("tzs").getAsString();
					 usdInterestAmount = rates.get("usd").getAsString();
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 			
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", customerCountry }, new Lookup001_mapper());
						
					 customerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";

					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,"
					 		+ " COLUMN20, COLUMN21, COLUMN22,COLUMN23, COLUMN24,COLUMN25,COLUMN26,COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34,COLUMN35,COLUMN36,COLUMN37,COLUMN38,COLUMN39,COLUMN40,COLUMN41) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "depositWithdrawalData", count, reportingDate, accountNumber, accountName, customerCategory, branchCode,district,region,accountProductName,
							 accountType, accountSubType,  depositCategory, depositAccountStatus, clientIdentificationNumber, customerCountry,clientType, relationshipType, transactionUniqueRef, timeStamp,serviceChannel,currency,
                             transactionType,orgTransactionAmount,usdTransactionAmount,tzsTransactionAmount,transactionPurposes,sectorSnaClassification,lienNumber,orgAmountLien,usdAmountLien,tzsAmountLien,contractDate,maturityDate,
							 annualInterestRate,interestRateType,orgInterestAmount,usdInterestAmount,tzsInterestAmount});
				 }

				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,"
						 		+ " COLUMN20, COLUMN21, COLUMN22,COLUMN23, COLUMN24,COLUMN25,COLUMN26,COLUMN27,COLUMN28,COLUMN29,COLUMN30, COLUMN31, COLUMN32, COLUMN33, COLUMN34,COLUMN35,COLUMN36,COLUMN37,COLUMN38,COLUMN39,COLUMN40,COLUMN41) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "depositWithdrawalData", "Serial", "reportingDate", "accountNumber", "accountName", "customerCategory", "branchCode","district","region","accountProductName",
							 "accountType", "accountSubType",  "depositCategory", "depositAccountStatus", "clientIdentificationNumber", "customerCountry","clientType", "relationshipType", "transactionUniqueRef", "timeStamp","serviceChannel","currency",
                             "transactionType","orgTransactionAmount","usdTransactionAmount","tzsTransactionAmount","transactionPurposes","sectorSnaClassification","lienNumber","orgAmountLien","usdAmountLien","tzsAmountLien","contractDate","maturityDate",
							 "annualInterestRate","interestRateType","orgInterestAmount","usdInterestAmount","tzsInterestAmount"});

					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "depositWithdrawalData"});

					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Liability", "depositWithdrawalData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 

 					 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);

					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  

			 logger.debug("Exception in DepositWithdrawalData :::: "+e.getLocalizedMessage());
		 }

		 return details;

	}
		
	public JsonObject boughtForwardExchangeData(String INFO1, String INFO2, String INFO3) //RTS085  verified
	{ 
		 JsonObject details = new JsonObject();

		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();

			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //

				 JsonArray content = Res.get("content").getAsJsonArray();

				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();

				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();

				 int count = 0;

				 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();

					 count++;
					 
					 String Input_SCI_LEID = getMember(js, "Input_SCI_LEID");

					 String reportingDate = getMember(js, "ReportingDate");
					 String counterpartName = getMember(js, "counterpartyName"); 
					 String relationshipType = getMember(js, "RelationshipType");   //lov
					 String currencyA = getMember(js, "currencyA"); 
					 String currencyB = getMember(js, "currencyB"); 
					 String orgAmountCurrencyA = getMember(js, "orgAmountCurrencyA"); 
					 String exchangeRateCurrencyAB = getMember(js, "exchangeRateCurrencyAB"); 
					 String orgAmountCurrencyB = getMember(js, "orgAmountCurrencyB"); 
					 String tzsExchangeRateCurrencyA = getMember(js, "tzsExchangeRateCurrencyA"); 
					 String tzsExchangeRateCurrencyB = getMember(js, "tzsExchangeRateCurrencyB"); 
					 String tzsAmountCurrencyA = getMember(js, "tzsAmountCurrencyA"); 
					 String tzsAmountCurrencyB = getMember(js, "tzsAmountCurrencyB"); 
					 String transactionDate = getMember(js, "transactionDate"); 
					 String valueDate = getMember(js, "valueDate"); 
					 String counterpartCrRating = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", Input_SCI_LEID); //cc;
					 boolean ratingStatus = true; 
					 String crRatingCounterSeller = ST.FindElementFromFileIT("SCI", "CRRATING", "LEID", Input_SCI_LEID); //cc;
					 String gradesUnratedSeller = "";
					 String transactionType = getMember(js, "transactionType"); 
					 String counterpartCountry = getMember(js, "counterpartCountry"); 
					 String pastDueDays = getMember(js, "pastDueDays"); 
					 String allowanceProbableLoss = getMember(js, "allowanceProbableLoss"); 
					 String botProvision = "0";
					 
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd");
					 valueDate = util.Convert_BOT_Date_Format(valueDate, "yyyy-MM-dd");
					 
					 valueDate = convertToEndOfDay(valueDate);
					 
					 //counterpartCrRating = "1";
					 //ratingStatus = "true";
					 //crRatingCounterSeller = "1";
					 //gradesUnratedSeller = "";
					 relationshipType = "1";
					 pastDueDays = "0";  //Need to work
					 allowanceProbableLoss = "0";  //Need to work
 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountCurrencyA, currencyA);
					 
					 tzsAmountCurrencyA = rates.get("tzs").getAsString();
					 tzsExchangeRateCurrencyA = rates.get("tzsrate").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountCurrencyB, currencyB);
					 
					 tzsAmountCurrencyB = rates.get("tzs").getAsString();
					 exchangeRateCurrencyAB = rates.get("tzsrate").getAsString();
					 tzsExchangeRateCurrencyB = rates.get("tzsrate").getAsString();
					 
					 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterpartCountry }, new Lookup001_mapper());
						
					 counterpartCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currencyA, "CUR" }, new Lookup001_mapper());
							
					 currencyA = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currencyB, "CUR" }, new Lookup001_mapper());
							
					 currencyB = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25,COLUMN26,COLUMN27) "
					 		+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "boughtForwardExchangeData", count, reportingDate, counterpartName, relationshipType, currencyA, currencyB,
							 orgAmountCurrencyA, exchangeRateCurrencyAB,  orgAmountCurrencyB, tzsExchangeRateCurrencyA, tzsExchangeRateCurrencyB, tzsAmountCurrencyA,tzsAmountCurrencyB, transactionDate, valueDate, counterpartCrRating,
							 ratingStatus, transactionType,counterpartCountry,crRatingCounterSeller,gradesUnratedSeller,pastDueDays,allowanceProbableLoss,botProvision});	
				 }

				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25,COLUMN26,COLUMN27) "
						 		+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "boughtForwardExchangeData", "Serial", "reportingDate", "counterpartName", "relationshipType", "currencyA", "currencyB",
								 "orgAmountCurrencyA", "exchangeRateCurrencyAB",  "orgAmountCurrencyB", "tzsExchangeRateCurrencyA", "tzsExchangeRateCurrencyB", "tzsAmountCurrencyA", "tzsAmountCurrencyB", "transactionDate", "valueDate", "counterpartCrRating",
								 "ratingStatus", "transactionType","counterpartCountry","crRatingCounterSeller","gradesUnratedSeller","pastDueDays","allowanceProbableLoss","botProvision"});	
 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "boughtForwardExchangeData"});

					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OffBalance", "boughtForwardExchangeData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 

					 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);

					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
			 
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  

			 logger.debug("Exception in boughtForwardExchangeData :::: "+e.getLocalizedMessage());
		 }

		 return details;

	}	
	
	public JsonObject interbankLoansReceivable(String INFO1, String INFO2, String INFO3) //RTS017  FM002 verified
	{ 
		 JsonObject details = new JsonObject();

		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			 
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString(); 
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //

				 JsonArray content = Res.get("content").getAsJsonArray();

				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();

				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();

				 int count = 0;

				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();

					 count++;

					 String reportingDate = getMember(js, "ReportingDate");
					 String borrowersInstitutionCode = getMember(js, "borrowingInstitutionCode"); 
					 String borrowersCountry = getMember(js, "borrowersCountry"); 
					 String relationshipType = getMember(js, "RelationshipType"); 
					 String ratingStatus = getMember(js, "RatingStatus"); 
					 String externalRatingCorrespondentBorrower = getMember(js, "externalRatingCorrespondentBorrower");  //cc
					 String gradesUnratedBorrower = getMember(js, "gradesUnratedBorrower");   //cc
					 String loanNumber = getMember(js, "Loan_number");
					 String loanType = getMember(js, "LoanType"); 
					 String issueDate = getMember(js, "Issue_Date"); 
					 String loanMaturityDate = getMember(js, "loanMaturityDate"); 
					 String currency = getMember(js, "currency"); 
					 String orgLoanAmount = getMember(js, "orgLoanAmount"); 
					 String usdLoanAmount = getMember(js, "usdLoanAmount"); 
					 String tzsLoanAmount = getMember(js, "TzsLoanAmount"); 
					 
					 String orgAccruedInterestAmount = "0"; 
					 String usdAccruedInterestAmount = "0"; 
					 String tzsAccruedInterestAmount = "0"; 
					 
					 String orgSuspendedInterest = getMember(js, "orgSuspendedInterest"); 
					 String usdSuspendedInterest = getMember(js, "usdSuspendedInterest"); 
					 String tzsSuspendedInterest = getMember(js, "tzsSuspendedInterest"); 
					 
					 String interestRate = getMember(js, "interestRate"); 
					 String pastDueDays = getMember(js, "pastDueDays");
					 String allowanceProbableLoss = getMember(js, "allowanceProbableLoss"); 
					 String assetClassification = getMember(js, "assetClassificationCategory"); 
					 String botProvision = "0";
					 
					 String Commission_Amt_input = getMember(js, "Commission_Amt_input"); 
					 String Input_Commission_Amt_Currency = getMember(js, "Input_Commission_Amt_Currency"); 
					 String Input_Trade_Id = getMember(js, "Input_Trade_Id"); 
					 String Input_SCI_LEID = getMember(js, "Input_SCI_LEID"); 
					 
					 interestRate = util.TwoDecimals(interestRate);
					 
					 String sql = "select (?-?) from dual";
					 
					 orgAccruedInterestAmount = Jdbctemplate.queryForObject(sql, new Object[] { Commission_Amt_input, orgLoanAmount }, String.class);
									 
					 issueDate = util.Convert_BOT_Date_Format(issueDate, "yyyy-MM-dd'T'HH:mm:ss");
					 loanMaturityDate = util.Convert_BOT_Date_Format(loanMaturityDate, "yyyy-MM-dd'T'HH:mm:ss");
					
					 borrowersInstitutionCode = "SCBLTZTXXXX";
					 ratingStatus = "true";
					 externalRatingCorrespondentBorrower = "1";
					 gradesUnratedBorrower = "1"; //temp
					 loanType = "1";
					 pastDueDays = "0";
					 relationshipType = "1";
					 allowanceProbableLoss = "0";
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgLoanAmount, currency);
					 
					 tzsLoanAmount = rates.get("tzs").getAsString();
					 usdLoanAmount = rates.get("usd").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAccruedInterestAmount, Input_Commission_Amt_Currency);
					 
					 tzsAccruedInterestAmount = rates.get("tzs").getAsString();
					 usdAccruedInterestAmount = rates.get("usd").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgSuspendedInterest, currency);
					 
					 tzsSuspendedInterest = rates.get("tzs").getAsString();
					 usdSuspendedInterest = rates.get("usd").getAsString();
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", borrowersCountry }, new Lookup001_mapper());
						
					 borrowersCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";   
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "FM003", assetClassification }, new Lookup001_mapper());
						
					 assetClassification = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "1";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25,COLUMN26,COLUMN27,COLUMN28,COLUMN29,COLUMN30) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "interbankLoansReceivable", count, reportingDate, borrowersInstitutionCode, borrowersCountry, relationshipType, ratingStatus, externalRatingCorrespondentBorrower,
							 gradesUnratedBorrower, loanNumber, loanType,  issueDate, loanMaturityDate, currency, orgLoanAmount,usdLoanAmount, tzsLoanAmount, orgAccruedInterestAmount, usdAccruedInterestAmount, tzsAccruedInterestAmount, orgSuspendedInterest, usdSuspendedInterest, tzsSuspendedInterest, interestRate, pastDueDays,allowanceProbableLoss,
							 assetClassification,botProvision});
				}

				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25,COLUMN26,COLUMN27,COLUMN28,COLUMN29,COLUMN30) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "interbankLoansReceivable", "serial", "reportingDate", "borrowersInstitutionCode", "borrowersCountry", "relationshipType", "ratingStatus", "externalRatingCorrespondentBorrower",
							 "gradesUnratedBorrower", "loanNumber", "loanType",  "issueDate", "loanMaturityDate", "currency", "orgLoanAmount", "usdLoanAmount", "tzsLoanAmount", "orgAccruedInterestAmount", "usdAccruedInterestAmount", "tzsAccruedInterestAmount", "orgSuspendedInterest", "usdSuspendedInterest", "tzsSuspendedInterest", "interestRate", "pastDueDays","allowanceProbableLoss",
							 "assetClassification","botProvision"});

					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";

					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "interbankLoansReceivable"});

					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "assets", "interbankLoansReceivable", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 

					 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);

					 details.addProperty("Serial", O_SERIAL);
					 details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  

			 logger.debug("Exception in interbankLoansReceivable :::: "+e.getLocalizedMessage());
		 }

		 return details;
	}
		
	public JsonObject invDebtSecuritiesData(String INFO1, String INFO2, String INFO3) //RTS005  FM001 verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
				 
				 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
				
				 Response_Code = api_out.get("Response_Code").getAsInt();
				 
				 String Response = api_out.get("Response").getAsString();
				 
				 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
				 {
					 flag = true;
					 
					 JsonObject Res = util.StringToJsonObject(Response);
					 
					 totalPages = Res.get("totalPages").getAsLong();   //
					 
					 JsonArray content = Res.get("content").getAsJsonArray();
					
					 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
					
					 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
					 
					 int count = 0;
					 
					 BOT_Stitching_Logics ST = new BOT_Stitching_Logics(Jdbctemplate);
					 
					 for(int i=0; i<content.size(); i++)
					 {
						 JsonObject js = content.get(i).getAsJsonObject();
						 
						 count++;
						 
						 String reportingDate = getMember(js, "ReportingDate");
						 String SecurityNumber = getMember(js, "SecurityNumber"); 
						 String SecurityType = getMember(js, "SecurityType"); 
						 String SecurityIssuerName = getMember(js, "SecurityIssuerName");
						 String ratingStatus = getMember(js, "Rating_Status");
						 String ExternalIssuerRating = getMember(js, "ExternalIssuerRating");   // get the mappimg from abitha
						 String gradesUnratedBanks = getMember(js, "gradesUnratedBanks");   // dependency with cc
						 String SecurityIssuerCountry = getMember(js, "SecurityIssuerCountry"); 
						 String SnaIssuerSector = getMember(js, "sectorSnaClassification");    //Stitching done
						 String Currency = getMember(js, "Currency"); 
						 String OrgCostValueAmount = getMember(js, "OrgCostValueAmount"); 
						 String usdCostValueAmount = getMember(js, "usdCostValueAmount"); 
						 String tzsCostValueAmount = getMember(js, "tzsCostValueAmount"); 
						 String OrgFaceValueAmount = getMember(js, "OrgFaceValueAmount"); 
						 String usdFaceValueAmount = getMember(js, "usdFaceValueAmount"); 
						 String tzsFaceValueAmount = getMember(js, "tzsFaceValueAmount"); 
						 String OrgFairValueAmount = getMember(js, "OrgFairValueAmount");
						 String usdFairValueAmount = getMember(js, "usdFairValueAmount");
						 String tzsFairValueAmount = getMember(js, "tzsFairValueAmount");
						 String interestRate = getMember(js, "interestRate");
						 String PurchaseDate = getMember(js, "PurchaseDate");
						 String ValueDate = getMember(js, "ValueDate");
						 String MaturityDate = getMember(js, "MaturityDate");  //MaturityDate
						 String TradingIntent = getMember(js, "TradingIntent");
						 String securityEncumbaranceStatus = ST.find_Security_encumbarance(SecurityNumber); // Stitching done
						 String pastDueDays = getMember(js, "pastDueDays"); // refer strich doc 
						 String AllowanceProbableLoss = getMember(js, "AllowanceProbableLoss"); // refer strich doc 
						 String BotProvision = "0";
						 String assetClassificationCategory = getMember(js, "assetClassificationCategory");  // get the mapping from abitha
						 
						 String Input_Industry_Sector = getMember(js, "Input_Industry_Sector"); 
						 
						 OrgCostValueAmount = util.isNullOrEmpty(OrgCostValueAmount) ? "0" : OrgCostValueAmount;
						 OrgFaceValueAmount = util.isNullOrEmpty(OrgFaceValueAmount) ? "0" : OrgFaceValueAmount;
						 OrgFairValueAmount = util.isNullOrEmpty(OrgFairValueAmount) ? "0" : OrgFairValueAmount;
						 
						 if(!util.isNullOrEmpty(ratingStatus) && ratingStatus.equalsIgnoreCase("true"))
						 {
							 gradesUnratedBanks = "";
							 
							 String sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";   
		
							 List<Lookup001>  Info = Jdbctemplate.query(sql, new Object[] { INFO1, "FM002", ExternalIssuerRating }, new Lookup001_mapper());
								
							 ExternalIssuerRating = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "1";
						 }
						 
						 pastDueDays = "0";
						 AllowanceProbableLoss = "0";
						 
						 interestRate = util.TwoDecimals(interestRate);
						 
						 PurchaseDate = util.Convert_BOT_Date_Format(PurchaseDate, "yyyy-MM-dd'T'HH:mm:ss");
						 ValueDate = util.Convert_BOT_Date_Format(ValueDate, "yyyy-MM-dd'T'HH:mm:ss");
						 MaturityDate = util.Convert_BOT_Date_Format(MaturityDate, "yyyy-MM-dd'T'HH:mm:ss");
						 
						 MaturityDate = convertToEndOfDay(MaturityDate);
						 ValueDate = convertToEndOfDay(ValueDate);
						 
						 if(util.isNullOrEmpty(MaturityDate))
						 {
							 MaturityDate = getMember(js, "MaturityDate");
						 }
						 
						 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
						 
						 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), OrgCostValueAmount, Currency);
						 
						 usdCostValueAmount = rates.get("usd").getAsString();
						 tzsCostValueAmount = rates.get("tzs").getAsString();
						 
						 rates = fx.find_exchangeRate(util.getCurrentDate(), OrgFaceValueAmount, Currency);
						 
						 usdFaceValueAmount = rates.get("usd").getAsString();
						 tzsFaceValueAmount = rates.get("tzs").getAsString();
						 
						 rates = fx.find_exchangeRate(util.getCurrentDate(), OrgFairValueAmount, Currency);
						 
						 usdFairValueAmount = rates.get("usd").getAsString();
						 tzsFairValueAmount = rates.get("tzs").getAsString();
						  	 
						 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
						 
						 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", SecurityIssuerCountry }, new Lookup001_mapper());
							
						 SecurityIssuerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
						 
						 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN2=?";   
						 
						 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "FM003", assetClassificationCategory }, new Lookup001_mapper());
							
						 assetClassificationCategory = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "1";
						 
						 sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN3=?";   
						 
						 Info = Jdbctemplate.query(sql, new Object[] { INFO1, "FM005", Input_Industry_Sector }, new Lookup001_mapper());
							
						 SnaIssuerSector = Info.size() > 0 ?  Info.get(0).getCOLUMN1() : "1";
						 
						 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { Currency, "CUR" }, new Lookup001_mapper());
								
						 Currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
						 
						 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32, COLUMN33) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
						 Jdbctemplate.update(sql, new Object[] {SUBORGCODE, O_SERIAL, "D", INFO1, "invDebtSecuritiesData", count, reportingDate, SecurityNumber, SecurityType, SecurityIssuerName, ratingStatus, ExternalIssuerRating, gradesUnratedBanks, SecurityIssuerCountry, SnaIssuerSector, Currency, OrgCostValueAmount, usdCostValueAmount, tzsCostValueAmount, OrgFaceValueAmount, usdFaceValueAmount, tzsFaceValueAmount, OrgFairValueAmount, usdFairValueAmount, tzsFairValueAmount, interestRate, PurchaseDate, ValueDate, MaturityDate, TradingIntent, securityEncumbaranceStatus, pastDueDays, AllowanceProbableLoss, BotProvision, assetClassificationCategory});
					 }
					 
					 if(count > 0)
					 {
						 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31, COLUMN32, COLUMN33) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "invDebtSecuritiesData", "serial", "reportingDate", "securityNumber", "securityType", "securityIssuerName", "ratingStatus", "externalIssuerRating", "gradesUnratedBanks", "securityIssuerCountry", "sectorSnaClassification", "currency", "orgCostValueAmount", "usdCostValueAmount", "tzsCostValueAmount", "orgFaceValueAmount", "usdFaceValueAmount", "tzsFaceValueAmount", "orgFairValueAmount", "usdFairValueAmount", "tzsFairValueAmount", "interestRate", "purchaseDate", "valueDate", "maturityDate", "tradingIntent", "securityEncumbaranceStatus", "pastDueDays", "allowanceProbableLoss", "botProvision", "assetClassificationCategory"});
						 
						 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
							
						 Jdbctemplate.update(sql, new Object[] {SUBORGCODE, O_SERIAL, "H", INFO1, "invDebtSecuritiesData"});
						 
						 sql = "INSERT INTO RTS003(ID, SUBORGCODE, MODULE, SUBMODULE, SERVICECD, NO_OF_RECORDS, CREATED_BY, CREATED_ON, IS_PUSHED, REPORT_SERIAL, SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
						 
					     Jdbctemplate.update(sql, new Object[] {Batch_id, SUBORGCODE, "Assets Data ", "invDebtSecuritiesData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
						
						 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
						 
						 details.addProperty("Serial", O_SERIAL);
				         details.addProperty("Batch_id", Batch_id);
					 }
				 }
				 
				 CurrentPage++;  //
				 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in invDebtSecuritiesData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
		
	public JsonObject currencySwapData(String INFO1, String INFO2, String INFO3) //FM013 RTS107  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String contractDate = getMember(js, "ContractDate"); 
					 String contractNumber = getMember(js, "contractNumber"); 
					 String maturityDate = getMember(js, "MaturityDate"); 
					 String counterpartName = getMember(js, "counterpartyName"); 
					 String relationshipType = getMember(js, "RelationshipType"); 
					 String ratingStatus = getMember(js, "RatingStatus");    
					 String crRatingCounterPart = getMember(js, "crRatingCounterPart"); 
					 String gradesUnratedCounterPart = getMember(js, "gradesUnratedCustomer"); 
					 
					 String currencyA = getMember(js, "currencyA"); 
					 String orgAmountCurrencyA = getMember(js, "orgAmountCurrencyA"); 
					 String tzsExchangeRateCurrencyA = getMember(js, "tzsExchangeRateCurrencyA"); 
					 String tzsAmountCurrencyA = getMember(js, "tzsAmountCurrencyA");
					 String tzsFowardExchangeRateCurrencyA = getMember(js, "tzsFowardExchangeRateCurrencyA");
					 String tzsFowardCurrencyA = getMember(js, "tzsAmountCurrencyA21");

					 String currencyB = getMember(js, "currencyB"); 
					 String orgAmountCurrencyB = getMember(js, "orgAmountCurrencyB"); 
					 String tzsExchangeRateCurrencyB = getMember(js, "tzsExchangeRateCurrencyB"); 
					 String tzsFowardExchangeRateCurrencyB = getMember(js, "tzsFowardExchangeRateCurrencyB");
					 String tzsAmountCurrencyB = getMember(js, "tzsAmountCurrencyB");
					 String tzsFowardCurrencyB = getMember(js, "tzsAmountCurrencyB22");
					 
					 String spotExchangeRateCurrencyAB = getMember(js, "spotExchangeRateCurrencyAB"); 
					 String fowardExchangeRate = getMember(js, "forwardExchangeRate");
					 String transactionDate = getMember(js, "transactionDate");
					 String valueDate = getMember(js, "valueDate");
					 String transactionType = getMember(js, "transactionType");
					 String sectorSnaClassification = getMember(js, "sectorSnaClassification");
					 
					 if(!util.isNullOrEmpty(transactionDate))
					 {
						 transactionDate = transactionDate.contains("-") ? util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd") : transactionDate;
					 }
					 
					 if(!util.isNullOrEmpty(contractDate))
					 {
						 contractDate = contractDate.contains("-") ? util.Convert_BOT_Date_Format(contractDate, "yyyy-MM-dd'T'HH:mm:ss") : contractDate;
					 }
					 
					 if(!util.isNullOrEmpty(valueDate))
					 {
						 valueDate = valueDate.contains("-") ? util.Convert_BOT_Date_Format(valueDate, "yyyy-MM-dd'T'HH:mm:ss") : valueDate;
					 }
					 
					 if(!util.isNullOrEmpty(maturityDate))
					 {
						 maturityDate = maturityDate.contains("-") ? util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd'T'HH:mm:ss") : maturityDate;
					 }
					 
					 maturityDate = convertToEndOfDay(maturityDate);
					 valueDate = convertToEndOfDay(valueDate);
					 contractDate = convertToEndOfDay(contractDate);
					 
					 relationshipType = "1";
					 ratingStatus = "true";
					 crRatingCounterPart = "1";
					 gradesUnratedCounterPart = "";
					 sectorSnaClassification = "1";
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountCurrencyA, currencyA);
					 
					 tzsAmountCurrencyA = rates.get("tzs").getAsString(); 
					 tzsExchangeRateCurrencyA = rates.get("tzsrate").getAsString(); 
					 tzsFowardExchangeRateCurrencyA = tzsExchangeRateCurrencyA;
					 tzsFowardCurrencyA = currencyA;
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountCurrencyB, currencyB);
					 
					 tzsAmountCurrencyB = rates.get("tzs").getAsString(); 
					 tzsExchangeRateCurrencyB = rates.get("tzsrate").getAsString(); 
					 tzsFowardExchangeRateCurrencyB = tzsExchangeRateCurrencyB; 
					 tzsFowardCurrencyB = currencyB;
					 					 
					 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currencyA, "CUR" }, new Lookup001_mapper());
							
					 currencyA = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currencyB, "CUR" }, new Lookup001_mapper());
							
					 currencyB = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { tzsFowardCurrencyA, "CUR" }, new Lookup001_mapper());
							
					 tzsFowardCurrencyA = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { tzsFowardCurrencyB, "CUR" }, new Lookup001_mapper());
							
					 tzsFowardCurrencyB = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,"
						 		+ "COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "currencySwapData", count, reportingDate, contractDate, contractNumber, maturityDate, counterpartName,
							 relationshipType, ratingStatus,  crRatingCounterPart, gradesUnratedCounterPart, currencyA, currencyB,orgAmountCurrencyA, spotExchangeRateCurrencyAB, orgAmountCurrencyB, tzsExchangeRateCurrencyA,
							 tzsAmountCurrencyA, tzsAmountCurrencyB, tzsExchangeRateCurrencyB, fowardExchangeRate, tzsFowardExchangeRateCurrencyA, tzsFowardExchangeRateCurrencyB, transactionDate, tzsFowardCurrencyA,
							 tzsFowardCurrencyB, valueDate, transactionType, sectorSnaClassification});
				 }
				 
				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,"
					 		+ "COLUMN22, COLUMN23, COLUMN24, COLUMN25, COLUMN26, COLUMN27, COLUMN28, COLUMN29, COLUMN30, COLUMN31) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "currencySwapData", "serial", "reportingDate", "contractDate", "contractNumber", "maturityDate", "counterpartName",
							 "relationshipType", "ratingStatus",  "crRatingCounterPart", "gradesUnratedCounterPart", "currencyA", "currencyB", "orgAmountCurrencyA", "spotExchangeRateCurrencyAB", "orgAmountCurrencyB", "tzsExchangeRateCurrencyA",
							 "tzsAmountCurrencyA", "tzsAmountCurrencyB", "tzsExchangeRateCurrencyB", "fowardExchangeRate", "tzsFowardExchangeRateCurrencyA", "tzsFowardExchangeRateCurrencyB", "transactionDate", "tzsFowardCurrencyA",
							 "tzsFowardCurrencyB", "valueDate", "transactionType", "sectorSnaClassification"});
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "currencySwapData"});
					 
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					 
				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OffBalance", "currencySwapData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					
					 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in currencySwapData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
		
	public JsonObject securitiesSoldData(String INFO1, String INFO2, String INFO3) //FM012 RTS099  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String relationshipType = getMember(js, "RelationshipType"); //lov
					 String valueDate = getMember(js, "ValueDate"); 
					 String maturityDate = getMember(js, "maturityDate");
					 String buyerName = getMember(js, "BuyerName"); 
					 String buyerCountry = getMember(js, "buyerCountry");  //lov
					 String ratingStatus = getMember(js, "RatingStatus"); 
					 String crRatingCounterCustomer = getMember(js, "crRatingCounterCustomer");   //lov
					 String gradesUnratedCustomer = getMember(js, "gradesUnratedCustomer");  //lov
					 String currency = getMember(js, "currency"); 
					 String orgSoldAmount = getMember(js, "orgSoldAmount"); 
					 String usdSoldAmount = getMember(js, "usdSoldAmount"); 
					 String tzsSoldAmount = getMember(js, "tzsSoldAmount"); 
					 String orgRepurchaseAmount = getMember(js, "orgRepurchaseAmount"); 
					 String usdRepurchaseAmount = getMember(js, "usdRepurchaseAmount"); 
					 String tzsRepurchaseAmount = getMember(js, "tzsRepurchaseAmount"); 
					 String sectorSnaClassification = getMember(js, "sectorSnaClassification");
					 String pastDueDays = getMember(js, "pastDueDays");
					 String allowanceProbableLoss = getMember(js, "allowanceProbableLoss");
					 String botProvision = "0";
					 
					 relationshipType = "1";
					 ratingStatus = "true";
					 crRatingCounterCustomer = "1";
					 gradesUnratedCustomer = "";
					 sectorSnaClassification = "1";
					 pastDueDays = "0";
					 allowanceProbableLoss = "0";
					
					 valueDate = util.Convert_BOT_Date_Format(valueDate, "yyyy-MM-dd");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd");
					 
					 valueDate = convertToEndOfDay(valueDate);
					 maturityDate = convertToEndOfDay(maturityDate);
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgSoldAmount, currency);
					 
					 usdSoldAmount = rates.get("usd").getAsString();
					 tzsSoldAmount = rates.get("tzs").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgRepurchaseAmount, currency);
					 
					 usdRepurchaseAmount = rates.get("usd").getAsString();
					 tzsRepurchaseAmount = rates.get("tzs").getAsString();
				
					 String sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", buyerCountry }, new Lookup001_mapper());
						
					 buyerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] {SUBORGCODE, O_SERIAL, "D", INFO1, "securitiesSoldData", count, reportingDate, relationshipType, valueDate, maturityDate, buyerName, buyerCountry, ratingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency, orgSoldAmount, usdSoldAmount, tzsSoldAmount, orgRepurchaseAmount, usdRepurchaseAmount, tzsRepurchaseAmount, sectorSnaClassification, pastDueDays, allowanceProbableLoss, botProvision});
					 
				 }
				 
				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14, COLUMN15, COLUMN16, COLUMN17, COLUMN18, COLUMN19, COLUMN20, COLUMN21, COLUMN22, COLUMN23, COLUMN24) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] {SUBORGCODE, O_SERIAL, "C", INFO1, "securitiesSoldData", "serial", "reportingDate", "relationshipType", "valueDate", "maturityDate", "buyerName", "buyerCountry", "ratingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgSoldAmount", "usdSoldAmount", "tzsSoldAmount", "orgRepurchaseAmount", "usdRepurchaseAmount", "tzsRepurchaseAmount", "sectorSnaClassification", "pastDueDays", "allowanceProbableLoss", "botProvision"});
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3) values(?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] {SUBORGCODE, O_SERIAL, "H", INFO1, "securitiesSoldData"});
					 
					 sql = "INSERT INTO RTS003(ID, SUBORGCODE, MODULE, SUBMODULE, SERVICECD, NO_OF_RECORDS, CREATED_BY, CREATED_ON, IS_PUSHED, REPORT_SERIAL, SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					 
				     Jdbctemplate.update(sql, new Object[] {Batch_id, SUBORGCODE, "OffBalance", "securitiesSoldData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					
					AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in securitiesSoldData :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
		
	public JsonObject securitiesPurchasedData(String INFO1, String INFO2, String INFO3) //FM011 RTS101
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				 
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				 
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String sellerName = getMember(js, "SellerName"); 
					 String relationshipType = getMember(js, "RelationshipType");  //lov
					 String transactionDate = getMember(js, "transactionDate");
					 String valueDate = getMember(js, "valueDate");
					 String maturityDate = getMember(js, "maturityDate"); 
					 String sellerCountry = getMember(js, "sellerCountry");   //lov
					 String ratingStatus = getMember(js, "ratingStatus");  
					 String crRatingCounterCustomer = getMember(js, "crRatingCounterSeller");  //lov
					 String gradesUnratedCustomer = getMember(js, "gradesUnratedSeller");  //lov
					 String currency = getMember(js, "currency"); 
					 String orgPurchasedAmount = getMember(js, "orgPurchasedAmount"); 
					 String usdPurchasedAmount = getMember(js, "usdPurchasedAmount"); 
					 String tzsPurchasedAmount = getMember(js, "tzsPurchasedAmount"); 
					 String orgResaleAmount = getMember(js, "orgResaleAmount"); 
					 String usdResaleAmount = getMember(js, "usdResaleAmount"); 
					 String tzsResaleAmount = getMember(js, "tzsResaleAmount"); 
					 String sectorSnaClassification = getMember(js, "sectorSnaClassification"); 
					 String pastDueDays = getMember(js, "pastDueDays"); 
					 String allowanceProbableLoss = getMember(js, "allowanceProbableLoss"); 
					 String botProvision = "0"; 
					// String InputToDeriveSnaSector = getMember(js, "InputToDeriveSnaSector"); //added
					 
					 relationshipType = "1";
					 ratingStatus = "true";
					 crRatingCounterCustomer = "1";
					 gradesUnratedCustomer = "";
					 sectorSnaClassification = "1";
					 pastDueDays = "0";
					 allowanceProbableLoss = "0";
					
					 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd");
					 valueDate = util.Convert_BOT_Date_Format(valueDate, "yyyy-MM-dd");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd");
					 
					 maturityDate = convertToEndOfDay(maturityDate);
					 valueDate = convertToEndOfDay(valueDate);
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgPurchasedAmount, currency);
					 
					 usdPurchasedAmount = rates.get("usd").getAsString();
					 tzsPurchasedAmount = rates.get("tzs").getAsString();
					 
					 rates = fx.find_exchangeRate(util.getCurrentDate(), orgResaleAmount, currency);
					 
					 usdResaleAmount = rates.get("usd").getAsString();
					 tzsResaleAmount = rates.get("tzs").getAsString();

					 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", sellerCountry }, new Lookup001_mapper());
						
					 sellerCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "securitiesPurchasedData", count, reportingDate, sellerName, relationshipType, transactionDate, valueDate,
							 maturityDate, sellerCountry,  ratingStatus, crRatingCounterCustomer, gradesUnratedCustomer, currency,orgPurchasedAmount, usdPurchasedAmount, tzsPurchasedAmount,
							 orgResaleAmount, usdResaleAmount, tzsResaleAmount, sectorSnaClassification, pastDueDays, allowanceProbableLoss, botProvision});
				 }
				 
				 if(count > 0)
				 { 
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "securitiesPurchasedData", "serial", "reportingDate", "sellerName", "relationshipType", "transactionDate", "valueDate",
							 "maturityDate", "sellerCountry",  "ratingStatus", "crRatingCounterCustomer", "gradesUnratedCustomer", "currency", "orgPurchasedAmount", "usdPurchasedAmount", "tzsPurchasedAmount",
							 "orgResaleAmount", "usdResaleAmount", "tzsResaleAmount", "sectorSnaClassification", "pastDueDays", "allowanceProbableLoss", "botProvision"   });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "securitiesPurchasedData"});
					 
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				     
					 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OffBalance", "securitiesPurchasedData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					 
					 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 	CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in securitiesPurchasedData :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}	
			
	public JsonObject SoldForwardExchangeData(String INFO1, String INFO2, String INFO3) //FM010 RTS087  verified
	{ 
			JsonObject details = new JsonObject();
			
			try
			{
				 Common_Utils util = new Common_Utils();
				 
				 String Sql = "select suborgcode from sysconf001";
					
				 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
				
				 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
				 
				 long totalPages = 0;  //
				 
				 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
				 
				 String URL = Webdtl.get("URI").getAsString(); //
				 
				 if(!util.isNullOrEmpty(INFO2))
				 {
					 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
					 
					 String[] dates = INFO2.split("\\|");
					 
					 logger.debug("APICODE >>>> "+INFO1);
					 logger.debug("FMCODE >>>> "+INFO3);
					 logger.debug("fromdate >>> "+dates[0]);
					 logger.debug("todate >>> "+dates[1]);
					 
					 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
					 
					 Webdtl.addProperty("PAYLOAD", js.toString());
					 
					 logger.debug("updated paytload >>> "+js.toString());
				 }
				 
				 JsonObject meta = getAttributes(URL);  //
				 
				 URL = meta.get("baseURL").getAsString();  //
				 int size = meta.get("size").getAsInt();   //
				 int CurrentPage = meta.get("page").getAsInt();  //
				 
				 int Response_Code; boolean flag = false;
				 
				 do 
				 {
					 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
					 
					 Webdtl.addProperty("URI", NewURL);
				 
				 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
				
				 Response_Code = api_out.get("Response_Code").getAsInt();
				 
				 String Response = api_out.get("Response").getAsString();
			
				if(Response_Code == 200 && !util.isNullOrEmpty(Response))
				{
					 flag = true;
					
					 JsonObject Res = util.StringToJsonObject(Response);
					 
					 totalPages = Res.get("totalPages").getAsLong();   //
					 
					 JsonArray content = Res.get("content").getAsJsonArray();
					 
					 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
					 
					 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
					 
					 int count = 0;
					 
					 for(int i=0; i<content.size(); i++)
					 {
						 JsonObject js = content.get(i).getAsJsonObject();
						 
						 count++;
						
						 String reportingDate = getMember(js, "ReportingDate");
						 String counterpartName = getMember(js, "counterpartyName"); 
						 String relationshipType = getMember(js, "RelationshipType");  //lov
						 String currencyA = getMember(js, "currencyA");  //lov
						 String currencyB = getMember(js, "currencyB");  //lov
						 String orgAmountCurrencyA = getMember(js, "orgAmountCurrencyA"); 
						 String exchangeRateCurrencyAB = getMember(js, "exchangeRateCurrencyB"); 
						 String orgAmountCurrencyB = getMember(js, "orgAmountCurrencyB"); 			
						 String tzsExchangeRateCurrencyA = getMember(js, "tzsExchangeRateCurrencyA"); 
						 String tzsExchangeRateCurrencyB = getMember(js, "tzsExchangeRateCurrencyB"); 
						 String tzsAmountCurrencyA = getMember(js, "tzsAmountCurrencyA"); 
						 String tzsAmountCurrencyB = getMember(js, "tzsAmountCurrencyB"); 
						 String transactionDate = getMember(js, "transactionDate"); 
						 String valueDate = getMember(js, "valueDate"); 
						 String transactionType = getMember(js, "transactionType"); 
						 String counterpartCountry = getMember(js, "counterpartCountry");  //lov
						 String ratingStatus = getMember(js, "crRatingCounterSeller"); 
						 String crRatingCounterCustomer = getMember(js, "crRatingCounterCustomer");  //lov
						 String gradesUnratedCustomer = getMember(js, "gradesUnratedSeller");  //lov
						 String pastDueDays = getMember(js, "pastDueDays"); 
						 String allowanceProbableLoss = getMember(js, "allowanceProbableLoss"); 
						 String botProvision = "0";
						 
						 relationshipType = "1";
						 ratingStatus = "true";
						 crRatingCounterCustomer = "1";
						 gradesUnratedCustomer = "";
						 pastDueDays = "0";
						 allowanceProbableLoss = "0";
						
						 transactionDate = util.Convert_BOT_Date_Format(transactionDate, "yyyy-MM-dd");
						 valueDate = util.Convert_BOT_Date_Format(valueDate, "yyyy-MM-dd");
						 
						 valueDate = convertToEndOfDay(valueDate);
						 
						 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
						 
						 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountCurrencyA, currencyA);
						 
						 tzsAmountCurrencyA = rates.get("tzs").getAsString();
						 tzsExchangeRateCurrencyA = rates.get("tzsrate").getAsString();
						 
						 rates = fx.find_exchangeRate(util.getCurrentDate(), orgAmountCurrencyB, currencyB);
						 
						 tzsAmountCurrencyB = rates.get("tzs").getAsString();
						 tzsExchangeRateCurrencyB = rates.get("tzsrate").getAsString();
						 
						 exchangeRateCurrencyAB = tzsExchangeRateCurrencyB;
						 
						 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
						 
						 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterpartCountry }, new Lookup001_mapper());
							
						 counterpartCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
						 
						 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { currencyA, "CUR" }, new Lookup001_mapper());
								
						 currencyA = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
						 
						 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
						 
						 Info = Jdbctemplate.query(sql, new Object[] { currencyB, "CUR" }, new Lookup001_mapper());
								
						 currencyB = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
				
						 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25,COLUMN26) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "soldForwardExchangeData", count, reportingDate, counterpartName, relationshipType, currencyA, currencyB,
								 orgAmountCurrencyA, exchangeRateCurrencyAB,  orgAmountCurrencyB, tzsExchangeRateCurrencyA, tzsExchangeRateCurrencyB, tzsAmountCurrencyA,tzsAmountCurrencyB, transactionDate, valueDate, transactionType, counterpartCountry, ratingStatus,
								 crRatingCounterCustomer, gradesUnratedCustomer,pastDueDays,allowanceProbableLoss,botProvision});
					 }
				 
					 if(count > 0)
					 {		 
						 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24,COLUMN25,COLUMN26) values\r\n" + 
								   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "soldForwardExchangeData", "serial", "reportingDate", "counterpartName", "relationshipType", "currencyA", "currencyB",
								 "orgAmountCurrencyA", "exchangeRateCurrencyAB",  "orgAmountCurrencyB", "tzsExchangeRateCurrencyA", "tzsExchangeRateCurrencyB", "tzsAmountCurrencyA", "tzsAmountCurrencyB", "transactionDate", "valueDate", "transactionType" , "counterpartCountry", "ratingStatus",
								 "crRatingCounterCustomer", "gradesUnratedCustomer",  "pastDueDays", "allowanceProbableLoss", "botProvision"});
						 
						 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
						 
						 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "soldForwardExchangeData"});
						 
						 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					     
						 Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OffBalance", "soldForwardExchangeData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
						 
						 AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
						 
						 details.addProperty("Serial", O_SERIAL);
						 details.addProperty("Batch_id", Batch_id);    
					 }
				}
				CurrentPage++;  //
				 
				 } while (CurrentPage <= totalPages);  //
				 
				 details.addProperty("suborgcode", SUBORGCODE);
				 details.addProperty("totalpages", totalPages);
				 details.addProperty("status", flag ? "S" : "F");
				 details.addProperty("rescode", flag ? 200 : Response_Code);
				
				details.addProperty("result", "success");
				details.addProperty("stscode", "HP00");
				details.addProperty("message", "Batch created successfully");  
			}
			catch(Exception e)
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06");
				details.addProperty("message", e.getLocalizedMessage());  
				
				logger.debug("Exception in soldForwardExchangeData :::: "+e.getLocalizedMessage());
			}
			
			return details;
	}		
			
	public JsonObject interestRateSwapData(String INFO1, String INFO2, String INFO3) //FM014 RTS109  verified
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 long totalPages = 0; //
			 
			 JsonObject Webdtl = WB.Get_Webserice_Info(INFO3); //
			 
			 String URL = Webdtl.get("URI").getAsString(); //
			 
			 if(!util.isNullOrEmpty(INFO2))
			 {
				 String PAYLOAD = Webdtl.get("PAYLOAD").getAsString(); //
				 
				 String[] dates = INFO2.split("\\|");
				 
				 logger.debug("APICODE >>>> "+INFO1);
				 logger.debug("FMCODE >>>> "+INFO3);
				 logger.debug("fromdate >>> "+dates[0]);
				 logger.debug("todate >>> "+dates[1]);
				 
				 JsonObject js = ConstructFMPayload(PAYLOAD, dates[0], dates[1]);
				 
				 Webdtl.addProperty("PAYLOAD", js.toString());
				 
				 logger.debug("updated paytload >>> "+js.toString());
			 }
			 
			 JsonObject meta = getAttributes(URL);  //
			 
			 URL = meta.get("baseURL").getAsString();  //
			 int size = meta.get("size").getAsInt();   //
			 int CurrentPage = meta.get("page").getAsInt();  //
			 
			 int Response_Code; boolean flag = false;
			 
			 do 
			 {
				 String NewURL = URL + "?page="+CurrentPage+"&size="+size;  //
				 
				 Webdtl.addProperty("URI", NewURL);  //
			 
			 JsonObject api_out = WCB.Okhttp_Send_Rest_Request(Webdtl);
			
			 Response_Code = api_out.get("Response_Code").getAsInt();
			 
			 String Response = api_out.get("Response").getAsString();
			 
			 if(Response_Code == 200 && !util.isNullOrEmpty(Response))
			 {
				 flag = true;
				 
				 JsonObject Res = util.StringToJsonObject(Response);
				 
				 totalPages = Res.get("totalPages").getAsLong();   //
				 
				 JsonArray content = Res.get("content").getAsJsonArray();
				
				 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
				
				 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
				 
				 int count = 0;
				 
				 for(int i=0; i<content.size(); i++)
				 {
					 JsonObject js = content.get(i).getAsJsonObject();
					 
					 count++;
					 
					 String reportingDate = getMember(js, "ReportingDate");
					 String contractDate = getMember(js, "ContractDate"); 
					 String contractNumber = getMember(js, "contractNumber"); 
					 String maturityDate = getMember(js, "MaturityDate"); 
					 String counterpartName = getMember(js, "counterpartyName"); 
					 String counterpartCountry = getMember(js, "counterpartCountry");  //lov
					 String relationshipType = getMember(js, "RelationshipType");   //lov
					 String ratingStatus = getMember(js, "RatingStatus"); 
					 String crRatingCounterpart = getMember(js, "crRatingCounterPart");   //lov
					 String gradesUnratedCounterPart = getMember(js, "gradesUnratedCustomer"); //newly added   //lov
					 String currency = getMember(js, "currency"); 
					 String orgContractAmount = getMember(js, "orgContractAmount"); 
					 String tzsContractAmount = getMember(js, "tzsContractAmount"); 
					 String fixedInterestRate = getMember(js, "fixedInterestRate"); 
					 String floatInterestRate = getMember(js, "floatInterestRate"); 
					 String liborRate = getMember(js, "LiborRate"); 
					 String taxRate = getMember(js, "taxRate"); 
					 String arrangementFee = getMember(js, "arrangementFee"); 
					 String sectorSnaClassification = getMember(js, "sectorSnaClassification"); 
					 String tradingIntent = getMember(js, "tradingIntent"); 
					 
					 FxSpot_Modal fx = new FxSpot_Modal(Jdbctemplate);
					 
					 JsonObject rates = fx.find_exchangeRate(util.getCurrentDate(), orgContractAmount, currency);
					 
					 tzsContractAmount = rates.get("tzs").getAsString();
					 
					 contractDate = util.Convert_BOT_Date_Format(contractDate, "yyyy-MM-dd");
					 maturityDate = util.Convert_BOT_Date_Format(maturityDate, "yyyy-MM-dd");
					 
					 contractDate = convertToEndOfDay(contractDate);
					 maturityDate = convertToEndOfDay(maturityDate);
					 
					 floatInterestRate = fixedInterestRate;
					 liborRate = floatInterestRate;
					 relationshipType = "1";
					 ratingStatus = "true";
					 crRatingCounterpart = "1";
					 gradesUnratedCounterPart = "";
					 sectorSnaClassification= "1";
					 
					 String sql = "select * from lookup001 where COLUMN12=? and COLUMN2=?";  
					 
					 List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { "COUNTRY", counterpartCountry }, new Lookup001_mapper());
						
					 counterpartCountry = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "" : "";
					 
					 sql = "select * from lookup001 where COLUMN2=? and COLUMN12=?";  
					 
					 Info = Jdbctemplate.query(sql, new Object[] { currency, "CUR" }, new Lookup001_mapper());
							
					 currency = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN1()) ? Info.get(0).getCOLUMN1() : "834" : "834";
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,"
					 		+ "COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "interestRateSwapData", count,reportingDate, 
							 contractDate, contractNumber, maturityDate, counterpartName, counterpartCountry, relationshipType, ratingStatus, 
							 crRatingCounterpart, gradesUnratedCounterPart, currency, orgContractAmount, tzsContractAmount, fixedInterestRate, floatInterestRate, liborRate, taxRate, arrangementFee, sectorSnaClassification, tradingIntent });
					 
				 }
				 
				 if(count > 0)
				 {
					 String sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4,COLUMN5,COLUMN6,COLUMN7,COLUMN8,COLUMN9,COLUMN10, COLUMN11, COLUMN12, COLUMN13, COLUMN14,COLUMN15,COLUMN16,COLUMN17,COLUMN18,COLUMN19,COLUMN20,COLUMN21,COLUMN22,COLUMN23,COLUMN24) values\r\n" + 
							   "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "interestRateSwapData", "serial", "reportingDate", "contractDate", "contractNumber", "maturityDate", "counterpartName", "counterpartCountry", "relationshipType", 
							 "ratingStatus", "crRatingCounterpart", "gradesUnratedCounterPart", "currency", "orgContractAmount", "tzsContractAmount", "fixedInterestRate", "floatInterestRate", "liborRate", "taxRate", "arrangementFee", 
							 "sectorSnaClassification", "tradingIntent" });
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "interestRateSwapData"});
					 
					 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
					 
				     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "OffBalance", "interestRateSwapData", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "FM" });	 		 		 
					
				     AutoMan.Request_Dispatcher(Batch_id, O_SERIAL, INFO1);
					 
					 details.addProperty("Serial", O_SERIAL);
			         details.addProperty("Batch_id", Batch_id);
				 }
			 }
			 
			 	CurrentPage++;  //
			 
			 } while (CurrentPage <= totalPages);  //
			 
			 details.addProperty("suborgcode", SUBORGCODE);
			 details.addProperty("totalpages", totalPages);
			 details.addProperty("status", flag ? "S" : "F");
			 details.addProperty("rescode", flag ? 200 : Response_Code);
			
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in interestRateSwapData :::: "+e.getLocalizedMessage());
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
			
			if(Name.equalsIgnoreCase("ReportingDate"))
			{
				if(val.length() == 14)
				{
					val = val.substring(0, 12);
				}
			}
		}
		catch(Exception ex)
		{
			logger.debug("Exception while parsing the element "+Name+" from the Jsonobject "+js);
		}
		
		return val;
	}
	
	public JsonObject getAttributes(String url)
	{
		JsonObject details = new JsonObject();
		
		String baseURL = "";  // Base URL
        String page = "0";     // Page number
        String size = "0";     // Size value
        
		try
		{
		    //String url = "https://uklvadfdp009a.pi.dev.net:12000/dqsl/predefined-query/query?page=1&size=10";
		    String regex = "^(http[^>]*?)\\?page=([0-9]+)&size=([0-9]+)$";

	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(url);

	        if(matcher.matches()) 
	        {
	            baseURL = matcher.group(1);  // Base URL
	            page = matcher.group(2);     // Page number
	            size = matcher.group(3);     // Size value
	        }   
		}
		catch(Exception ex)
		{
			logger.debug("Exception in getAttributes :::: "+ex.getLocalizedMessage());
		}
		
		details.addProperty("baseURL", baseURL);
	    details.addProperty("page", page);
	    details.addProperty("size", size); 
	        
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
	
	public JsonObject Generate_api_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select REG_SEQ.Nextval from dual";
			   
			 String SL = Jdbctemplate.queryForObject(sql, String.class);
			 
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
	
	public JsonObject ConstructFMPayload(String Payload, String FromDate, String ToDate)
	{
		JsonObject details= new JsonObject();
		
		try 
        {
			System.out.println("fromdate before >>>> "+FromDate);
			System.out.println("ToDate before >>>> "+ToDate);
			
			Common_Utils util = new Common_Utils();
			
			JsonObject js = util.StringToJsonObject(Payload);
			
			if(js.has("queryParameters"))
			{
				 details.addProperty("queryName", js.get("queryName").getAsString());
				
				 JsonArray queryParameters = new JsonArray();
						 
				 JsonArray jr = js.get("queryParameters").getAsJsonArray();
				 
				 for(int i=0; i<jr.size(); i++)
				 {
					 JsonObject jk = jr.get(i).getAsJsonObject();
					 
					 String parameterName = jk.get("parameterName").getAsString();
					 String parameterValue = jk.get("parameterValue").getAsString();
					 
					 if(parameterName.equalsIgnoreCase("pCurrentDateTime1hourbefore"))
					 {
						 parameterValue = convertTimestamp(FromDate);  //convertTimestamp(INFO2);
					 }
					 
					 if(parameterName.equalsIgnoreCase("pCurrentDateTime"))
					 {
						 parameterValue = convertTimestamp(ToDate);
					 }
					 
					 JsonObject jkk = new JsonObject();
					 
					 jkk.addProperty("parameterName", parameterName);
					 jkk.addProperty("parameterValue", parameterValue);
					 
					 queryParameters.add(jkk);
				 }
				 
				 details.add("queryParameters", queryParameters);
			}
		} 
	    catch (Exception e) 
	    {
	    	logger.debug("ConstructFMPayload: " + e.getLocalizedMessage());
	    }
		
		return details;
	}
	
	public String convertTimestamp(String inputTimestamp) 
	{
	    String out = "";

	    try 
	    {
	        // Normalize fractional seconds to 3 digits
	        if (inputTimestamp.contains("."))
	        {
	            String[] parts = inputTimestamp.split("\\.");
	            String base = parts[0];
	            String fraction = parts[1];

	            // Pad or trim the fractional part to 3 digits
	            if (fraction.length() > 3) {
	                fraction = fraction.substring(0, 3);
	            } else {
	                fraction = String.format("%-3s", fraction).replace(' ', '0');
	            }

	            inputTimestamp = base + "." + fraction;
	        } 
	        else 
	        {
	            // Add .000 if no fractional seconds
	            inputTimestamp += ".000";
	        }

	        // Use formatter with 3-digit milliseconds
	        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	        LocalDateTime localDateTime = LocalDateTime.parse(inputTimestamp, inputFormatter);

	        out = localDateTime.atOffset(ZoneOffset.UTC)
	                           .with(ChronoField.MILLI_OF_SECOND, 0)
	                           .toInstant()
	                           .toString();
	    } 
	    catch (Exception e) 
	    {
	    	logger.debug("Error parsing timestamp: " + e.getMessage());
	    }

	    return out;
	}
	
	public Timestamp convertToTimestamp(String timestampString) 
	{
        try 
        {
            DateTimeFormatter formatter;

            if (timestampString.contains(".")) 
            {
                String fractionalPart = timestampString.split("\\.")[1];
               
                // Check if the fractional part has 2 or 3 digits
                
                if (fractionalPart.length() == 1) 
                {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                } 
                else if (fractionalPart.length() == 2) 
                {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");
                } 
                else 
                {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                }
            } 
            else 
            {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            }

            // Convert String to LocalDateTime
            LocalDateTime localDateTime = LocalDateTime.parse(timestampString, formatter);

            // Convert LocalDateTime to SQL Timestamp
            return Timestamp.valueOf(localDateTime);
        } 
        catch (Exception e) 
        {
        	logger.debug("Error parsing timestamp: " + e.getMessage());
        	
            return null; 
        }
    }
	
	public static String convertToEndOfDay(String inputDateTime)
	{
        try
        {          
            if (inputDateTime == null || inputDateTime.length() < 8)
            {
                throw new Exception("Invalid input format.");
            }
 
            String datePart = inputDateTime.substring(0, 8);
            
            return datePart + "2359";
        }
        catch (Exception e)
        {
            logger.debug("Error: " + e.getMessage());
            
            return "";
        }
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
	
	public class FMAPICallLogMapper implements RowMapper<FMAPICallLog> 
	{
	    Common_Utils util = new Common_Utils();

	    @Override
	    public FMAPICallLog mapRow(ResultSet rs, int rowNum) throws SQLException 
	    {
	        FMAPICallLog log = new FMAPICallLog();

	        log.setSuborgcode(util.ReplaceNull(rs.getString("suborgcode")));
	        log.setReqsl(util.ReplaceNull(rs.getString("reqsl")));
	        log.setReqdate(util.ReplaceNull(rs.getString("reqdate")));
	        log.setFmCode(util.ReplaceNull(rs.getString("fm_code")));
	        log.setApiName(util.ReplaceNull(rs.getString("api_name")));
	        log.setApiCode(util.ReplaceNull(rs.getString("api_code")));
	        log.setStartTime(util.ReplaceNull(rs.getString("start_time")));
	        log.setEndTime(util.ReplaceNull(rs.getString("end_time")));
	        log.setPagenum(util.ReplaceNull(rs.getString("pagenum")));
	        log.setPagesize(util.ReplaceNull(rs.getString("pagesize")));
	        log.setNoOfRecords(util.ReplaceNull(rs.getString("no_of_records")));
	        log.setStatus(util.ReplaceNull(rs.getString("status")));
	        log.setRescode(util.ReplaceNull(rs.getString("rescode")));
	        log.setRespdesc(util.ReplaceNull(rs.getString("respdesc")));

	        return log;
	    }
	}
}
