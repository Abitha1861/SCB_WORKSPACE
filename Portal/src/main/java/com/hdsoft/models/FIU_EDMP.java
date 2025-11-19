package com.hdsoft.models;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;
import java.text.SimpleDateFormat;

import com.hdsoft.Repositories.FIU_Menu;
import com.hdsoft.common.Common_Utils;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.zaxxer.hikari.HikariDataSource;
	
@Controller
@Component
public class FIU_EDMP 
{
	
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
			
	private static final Logger logger = LogManager.getLogger(FIU_EDMP.class);
	
//	@Async
//	@Scheduled(cron = "0 0 */2 * * *")  // Every 2 hours
//	public JsonObject FIU_EDMP_FILES_READ() 
//	{
//		JsonObject details = new JsonObject();
//		
//	    try 
//	    {
//	        String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";
//
//	        List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "FIU_EDMP" }, String.class);
//
//	        if(prop.size() > 0) 
//	        {
//	            String filepath = prop.get(0);
//	            EDMP_FILE_READ(filepath);
//	        }  
//	        
//	        details.addProperty("result", "successfully read FIU edmp files");
//	        logger.debug("Completed FIU_EDMP_FILES_READ successfully");
//
//	    } 
//	    catch (Exception e) 
//	    {
//	    	logger.error("Error in FIU_EDMP_FILES_READ: " + e.getLocalizedMessage(), e);  
//	        details.addProperty("result", "Failed to read FIU edmp files");
//	    }
//	    
//	    return details;
//	}
//	
//	
//	@Scheduled(cron = "0 0 */6 * * *")  // Every 6 hours
//	public JsonObject FIU_STATIC_FILES_READ() 
//	{
//	    JsonObject details = new JsonObject();
//	    
//	    try 
//	    {
//	        String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";
//	        
//	        List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "FIU_STATIC" }, String.class);
//
//	        if (prop.size() > 0) 
//	        {
//	            String filepath = prop.get(0);
//	            static_fiu(filepath);
//	        }
//	        
//	        details.addProperty("result", "successfully read FIU_STATIC_FILES_READ");	        
//	        logger.debug("successfully read FIU_STATIC_FILES_READ");
//	        
//	    } 
//	    catch (Exception e) 
//	    {
//	    	logger.error("Error in FIU_STATIC_FILES_READ: " + e.getLocalizedMessage(), e);  
//	        details.addProperty("result", "Failed to read FIU_STATIC_FILES_READ");
//	    }
//	    
//	    return details;
//	}

	
	@RequestMapping(value = {"/Datavision/FIU_EDMP"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String FIU_EDMP_(HttpServletRequest request,  HttpServletResponse response,  HttpSession session) throws IOException 
	{
		JsonObject details = new JsonObject();
		
	    try 
	    {
	        String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";

	        List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "FIU_EDMP" }, String.class);

	        if(prop.size() > 0) 
	        {
	            String filepath = prop.get(0);
	            details = EDMP_FILE_READ(filepath);
	        }  

	        details.addProperty("result", "successfully read FIU edmp files");

	    } 
	    catch (Exception ex) 
	    {
	    	details.addProperty("err", ex.getLocalizedMessage());        
	        details.addProperty("result", "Failed to read FIU edmp files");
	    }
	    
	    return details.toString();
	}
	
	@RequestMapping(value = {"/Datavision/FIU_static"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String FIU_static(HttpServletRequest request,  HttpServletResponse response,  HttpSession session) throws IOException 
	 {		
		JsonObject details = new JsonObject();
		 
		 try 
		 {
			
	        String sql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";

	        List<String> prop = Jdbctemplate.queryForList(sql, new Object[] { "FIU_static" }, String.class);

	        if(prop.size() > 0) 
	        {
	            String filepath = prop.get(0);
	            details = static_fiu(filepath);
	        }  
	        
	        details.addProperty("result", "successfully read FIU edmp files");
			 		 
		 }
		 catch (Exception ex) 
		 {
			 details.addProperty("err", ex.getLocalizedMessage());        
		     details.addProperty("result", "Failed to read FIU edmp files");
		 }
		 
		 return details.toString();    
    }

	
	
	public JsonObject EDMP_FILE_READ(String PATH) 
	{
	    JsonObject details = new JsonObject();

	    try 
	    {
	    	Common_Utils util = new Common_Utils();
	    	
			String Sql = "select suborgcode from sysconf001";
				
			List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			boolean isError = false;
			
			String bk_path = null;
					 
	        File folder = new File(PATH);
	        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

	        if (files == null || files.length == 0) 
	        {
	            logger.info("No CSV files found in directory: " + PATH);
	            details.addProperty("result", "no files found");
	            details.addProperty("stscode", "HP01");
	            details.addProperty("message", "No CSV files found in the specified directory.");
	            return details;
	        }

	        logger.info("Found {} CSV files in {}", files.length, PATH);

	        for (File file : files) 
	        {
	        	 String sql = null;	        	
	        	 String Serial = Generate_FIU_Serial().get("Serial").getAsString();	        	 
	        	 String filePath = file.getAbsolutePath(); 

	        	 sql = "select * from fileit001 where srcpath = ?";
	        	 
	        	 List<Map<String, Object>> FILEIT = Jdbctemplate.queryForList(sql, new Object[]{filePath});
	        	         	 
	             if(FILEIT.isEmpty()) 
	 	         {
                   
	 	            logger.info("Processing file: {}", file.getName());	 	

	 	            try {
	 	                List<String[]> rows;
	 	                try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(new CSVParserBuilder().withSeparator('|').build()).build()) 
	 	                {
	 	                    rows = csvReader.readAll();
	 	                    
	 	                }

	 	                if (rows.size() <= 1) 
	 	                {
	 	                   logger.warn("No data rows found in file: {}", file.getName());
	 	                   isError = true;
	 	                   continue;
	 	                    
	 	                   
	 	                }

	 	                if (file.getName().toLowerCase().contains("ctr")) 
	 	                {
	 	                    sql = "INSERT INTO EDMP_CTR_FIU (" +
	 	                            "serial ,seq,t_branch, t_account_currency, t_account_number, t_date_of_transaction, " +
	 	                            "t_transaction_number, t_transmode_code, t_debit_credit, t_transaction_type, " +
	 	                            "t_amount_local, t_foreign_currency_code, t_foreign_amount, t_foregin_exchange_rate, " +
	 	                            "t_purpose_of_transaction, t_name_of_the_beneficiary, t_beneficiary_account_number, " +
	 	                            "t_beneficiary_address, t_name_of_the_sender, t_address_of_the_sender, " +
	 	                            "t_corr_bank_name) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	 	                    
	 	                } 
	 	                else if (file.getName().toLowerCase().contains("eft")) 
	 	                {
	 	                	sql = "INSERT INTO EDMP_EFTR_FIU (" +
	 	                		      "serial, seq, t_transaction_number, t_debit_credit, t_account_currency, " +
	 	                		      "t_purpose_of_transaction, t_date_of_transaction, t_value_date, t_account_number, " +
	 	                		      "t_amount_local, t_foreign_currency_code, t_foreign_amount, t_foregin_exchange_rate, " +
	 	                		      "t_transmode_code, t_transaction_type, t_name_of_the_beneficiary, " +
	 	                		      "t_beneficiary_account_number, t_beneficiary_address, t_sender_account_no, " +
	 	                		      "t_name_of_the_sender, t_address_of_the_sender, t_corr_bank_name" +
	 	                		      ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	 	                    		
	 	                } 
	 	                else 
	 	                {
	 	                    logger.warn("Skipped unknown file type: {}", file.getName());
	 	                    continue;
	 	                }
	 	                
	 	                int seq = 1;
	 	                int batchSize = 500;  // Define the batch size for batch processing
	 	                List<Object[]> batch = new ArrayList<>();  // List to hold the batch of rows

	 	                for (int i = 1; i < rows.size(); i++) 
	 	                {
	 	                    String[] row = rows.get(i);
	 	                    if (row == null || row.length == 0 || (row[0] != null && row[0].trim().isEmpty())) continue;

	 	                    try {
	 	                    	
	 	                    	int expectedColumns;
	 	                    	int paramCount;

	 	                    	if (file.getName().toLowerCase().contains("ctr")) 
	 	                    	{
	 	                    	    expectedColumns = 18; 
	 	                    	    paramCount = 21; 
	 	                    	} 
	 	                    	else 
	 	                    	{
	 	                    	    expectedColumns = 20;
	 	                    	    paramCount = 22;    
	 	                    	}

	 	                    	if (row.length != expectedColumns) 
	 	                    	{
	 	                    	    logger.error("Skipping row {} from file {}: Expected {} columns, found {}", i, file.getName(), expectedColumns, row.length);
	 	                    	    isError = true;
	 	                    	    continue;
  
	 	                    	}

	 	                    	Object[] params = new Object[paramCount];

	 	                    	// Set serial and seq
	 	                        params[0] = Serial;
	 	                        params[1] = seq++;
	 	                        
	 	                        for (int j = 0; j < expectedColumns; j++) 
	 	                        {
	 	                            params[j + 2] = (row[j] != null && !row[j].trim().isEmpty()) ? row[j].trim() : null;
	 	                        }

	 	                        batch.add(params); 
	 	                        
	 	                        
	 	                        if (batch.size() >= batchSize) 
	 	                        {
	 	                            Jdbctemplate.batchUpdate(sql, batch);  // Execute the batch insert
	 	                            batch.clear();  // Clear the batch after executing
	 	                        }
	 	                        
	 	                   
	 	                    } 
	 	                    catch (Exception ex) 
	 	                    {
	 	                        logger.error("Error inserting row {} from file {}: {}", i, file.getName(), ex.getMessage());
	 	                        isError = true;
	 	                    }
	 	                    
	 	                 
	 	        }	 	                                       	 
	   	 
	 	               
	 	                   
	                       if (!batch.isEmpty()) {
	                    	   logger.info("Flushing remaining {} records for file {}", batch.size(), file.getName());
	                    	    Jdbctemplate.batchUpdate(sql, batch);
	                    	    batch.clear();
	                    	}
	        }

	            catch (Exception e) 
	            {
	                logger.error("Error processing file {}: {}", file.getName(), e.getMessage(), e);
	            }
	            
	 	        }
			
 	            	bk_path =  backup_file(file);

 
 	            	if(FILEIT.isEmpty())
 	            	{
 		 	            if(isError)
 		 	            {
 							sql = "insert into fileit001(SUBORGCODE, CHCODE, PAYTYPE, REQDATE, REQREFNO, REQTIME, FILETYPE, SRCPATH, DSTPATH, REMARKS, STATUS, RESCODE, RESPDESC) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
 							
 							Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "FIU", "EDMP_FILE", util.getCurrentDate("dd-MMM-yyyy"), Serial, util.get_oracle_Timestamp(), "CSV", file.getPath(), bk_path, Serial, "SUCCESS", "200", "FAILED" });

 		 	            }
 		 	            else 
 		 	            {
 							sql = "insert into fileit001(SUBORGCODE, CHCODE, PAYTYPE, REQDATE, REQREFNO, REQTIME, FILETYPE, SRCPATH, DSTPATH, REMARKS, STATUS, RESCODE, RESPDESC) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
 							
 							Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "FIU", "EDMP_FILE", util.getCurrentDate("dd-MMM-yyyy"), Serial, util.get_oracle_Timestamp(), "CSV", file.getPath(), bk_path, Serial, "SUCCESS", "200", "FILE DOWNLOADED success" });

 						}
 	            	}

	       
	    }
	        details.addProperty("result", "success");
	        details.addProperty("stscode", "HP00");
	        details.addProperty("message", "All CSV files processed successfully and moved to backup folder.");
	        logger.info("All CSV files processed successfully and moved to backup folder.");

	    } 
	    
	    catch (Exception e) 
	    {
	        details.addProperty("result", "failed");
	        details.addProperty("stscode", "HP06");
	        details.addProperty("message", e.getLocalizedMessage());
	        logger.error("Exception in EDMP_FILE_READ: {}", e.getLocalizedMessage(), e);
	    }

	    return details;
	}
	    
	public JsonObject static_fiu(String PATH) 
	{
	    JsonObject details = new JsonObject();

	    try 
	    {
	        Common_Utils util = new Common_Utils();

	        String Sql = "select suborgcode from sysconf001";
	        List<String> result = Jdbctemplate.queryForList(Sql, String.class);
	        String SUBORGCODE = result.size() != 0 ? result.get(0) : "";  

	        boolean isError = false;

	        File folder = new File(PATH);
	        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

	        if (files == null || files.length == 0) 
	        {
	            logger.info("No EDMP Static files found in directory: " + PATH);            
	            details.addProperty("result", "failed");
	            details.addProperty("message", "No EDMP Static files found in directory: " + PATH);
	            return details;
	        }

	        logger.info("Found {} CSV files in {}", files.length, PATH);

	        for (File file : files) 
	        {
	            String sql = null;

	            String Serial = Generate_FIU_Serial().get("Serial").getAsString();
	            String filePath = file.getAbsolutePath();

	            sql = "SELECT * FROM fileit001 WHERE srcpath = ?";
	            List<Map<String, Object>> FILEIT = Jdbctemplate.queryForList(sql, new Object[]{filePath});

	            if (FILEIT.isEmpty()) 
	            {
	                logger.info("Processing file: {}", file.getName());

	                try 
	                {
	                    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
	                    List<String[]> rows = new ArrayList<>();

	                    for (String line : lines) 
	                    {
	                        String[] cols = line.split("\\|", -1);

	                        // FIXED: SQL expects 50 columns (excluding serial, seq)
	                        int expectedColumns = 50;
	                        int extraColumns = 51; // file may have 1 extra dummy column

	                        if (cols.length < expectedColumns)
	                            cols = Arrays.copyOf(cols, expectedColumns);

	                        if (cols.length > extraColumns)
	                            cols = Arrays.copyOfRange(cols, 0, expectedColumns);

	                        if (cols.length == extraColumns)
	                            cols = Arrays.copyOf(cols, expectedColumns);

	                        rows.add(cols);
	                    }

	                    if (rows.size() <= 1) 
	                    {
	                        logger.warn("No data rows found in file: {}", file.getName());
	                        continue;
	                    }

	                    // FINAL CORRECT SQL INSERT WITH EXACT 52 VALUES
	                    sql = "INSERT INTO static_fiu (\n" +
	                            "serial , seq , account_no, rel_id, currency_cd, category, master_no, primary_fg, gender,\n" +
	                            "first_nm, middle_nm, last_nm, birth_dt, pob_place,\n" +
	                            "inc_state, residence, nationality, id_type, id_no,\n" +
	                            "expiry_dt, issue_dt, issue_country, contact_type, comm_type, phone_no,\n" +
	                            "email, inst_name, branch, acc_holder_nm, acc_type, acc_desc,\n" +
	                            "opened_dt, status_cd, inst_code, entity_nm, business_act, tax_id_no,\n" +
	                            "trade_license_no, est_dt, addr_type, addr, city, country, state,\n" +
	                            "occupation, employer_cd, employer_phone, employer_addr, role\n" +
	                            ") VALUES (\n" +
	                            "?,?,?,?,?,?,?,?,?,\n" +
	                            "?,?,?,?,?,?,?,?,?,\n" +
	                            "?,?,?,?,?,?,?,?,?,\n" +
	                            "?,?,?,?,?,?,?,?,?,\n" +
	                            "?,?,?,?,?,?,?,?,?,?,?\n" +
	                            ")";

	                    int seq = 1;
	                    int batchSize = 500;
	                    List<Object[]> batch = new ArrayList<>();

	                    int expectedColumns = 50; // correct
	                    int paramCount = 52;      // serial + seq + 50 cols

	                    for (int i = 1; i < rows.size(); i++) 
	                    {
	                        String[] row = rows.get(i);

	                        // clean spaces, BOM, quotes
	                        for (int c = 0; c < row.length; c++) 
	                        {
	                            if (row[c] != null) 
	                            {
	                                row[c] = row[c].replace("'", "")
	                                               .replace("\uFEFF", "")
	                                               .trim();

	                                if (row[c].equalsIgnoreCase("NULL") || row[c].isEmpty())
	                                    row[c] = null;
	                            }
	                        }

	                        if (row.length != expectedColumns) 
	                        {
	                            logger.error("Skipping row {}. Expected {} columns, found {}", 
	                                         i, expectedColumns, row.length);
	                            continue;
	                        }

	                        try 
	                        {
	                            Object[] params = new Object[paramCount];
	                            params[0] = Serial;
	                            params[1] = seq++;

	                            for (int j = 0; j < expectedColumns; j++) 
	                            {
	                                params[j + 2] = row[j];
	                            }

	                            batch.add(params);

	                            if (batch.size() >= batchSize) 
	                            {
	                                Jdbctemplate.batchUpdate(sql, batch);
	                                batch.clear();
	                            }
	                        }
	                        catch (Exception ex) 
	                        {
	                            logger.error("Error inserting row {}: {}", i, ex.getMessage());
	                            isError = true;
	                        }
	                    }

	                    if (!batch.isEmpty())
	                        Jdbctemplate.batchUpdate(sql, batch);

	                } 
	                catch (Exception ex) 
	                {
	                    logger.error("Error processing file {}: {}", file.getName(), ex.getMessage());
	                    isError = true;
	                }
	            }
	        }

	        details.addProperty("result", isError ? "failed" : "success");
	        details.addProperty("message", isError ? "Some errors occurred" : "Inserted successfully");

	    } 
	    catch (Exception e) 
	    {
	        logger.error("Unexpected error: {}", e.getMessage());
	        details.addProperty("result", "failed");
	        details.addProperty("message", e.getMessage());
	    }

	    return details;
	}

	                    
//	
//	public JsonObject static_fiu(String PATH) 
//	{
//	    JsonObject details = new JsonObject();
//
//	    try 
//	    	{
//			Common_Utils util = new Common_Utils();
//	    	
//			String Sql = "select suborgcode from sysconf001";
//				
//			List<String> result = Jdbctemplate.queryForList(Sql, String.class);
//			
//			String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
//			 
//			boolean isError = false;
//			
//			String bk_path = null;
//
//
//	        File folder = new File(PATH);
//	        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
//
//	        if (files == null || files.length == 0) 
//	        {
//	            logger.info("No EDMP Static files found in directory: " + PATH);            
//	            details.addProperty("result", "failed");
//	            details.addProperty("message", "No EDMP Static files found in directory: " + PATH);
//	            return details;
//	        } 
//	       
//	            logger.info("Found {} CSV files in {}", files.length, PATH);
//	       
//	
//	        for (File file : files) 
//	        {
//			    String sql = null;
//	        	
//	        	 String Serial = Generate_FIU_Serial().get("Serial").getAsString();
//	        	 
//	        	 String filePath = file.getAbsolutePath(); 
//
//	        	 sql = "select * from fileit001 where srcpath = ?";
//	        	 
//	        	 List<Map<String, Object>> FILEIT = Jdbctemplate.queryForList(sql, new Object[]{filePath});
//	        	         	 
//	             if(FILEIT.isEmpty()) 
//	 	         {
//    
//		            logger.info("Processing file: {}", file.getName());
//		           
//	
//		            try 
//		            {
//		                List<String[]> rows;
//		                try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
//		                        .withCSVParser(new CSVParserBuilder().withSeparator('|').build()).build()) 
//		                {
//		                    rows = csvReader.readAll();
//		                }
//		                
//	
//		                if (rows.size() <= 1) 
//		                {
//		                    logger.warn("No data rows found in file: {}", file.getName());
//		                    isError = true;
//							continue;
//		                }
//	
//		                sql = "INSERT INTO static_fiu (\r\n"
//		                		+ "    serial , seq , account_no, rel_id, currency_cd, category, master_no, primary_fg, gender, \r\n"
//		                		+ "    first_nm, middle_nm, last_nm, birth_dt, pob_place, \r\n"
//		                		+ "    inc_state, residence, nationality, id_type, id_no, \r\n"
//		                		+ "    expiry_dt, issue_dt, issue_country, contact_type, comm_type, phone_no, \r\n"
//		                		+ "    email, inst_name, branch, acc_holder_nm, acc_type, acc_desc, \r\n"
//		                		+ "    opened_dt, status_cd, inst_code, entity_nm, business_act, tax_id_no, \r\n"
//		                		+ "    trade_license_no, est_dt, addr_type, addr, city, country, state, \r\n"
//		                		+ "    occupation, employer_cd, employer_phone, employer_addr, role\r\n"
//		                		+ ")VALUES (\r\n"
//		                        + "    ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?\r\n"
//		                        + "\r\n"
//		                        + ")";
//		                
//		 	            int seq = 1;
//	 	                int batchSize = 500;  // Define the batch size for batch processing
//	 	                List<Object[]> batch = new ArrayList<>();  // List to hold the batch of rows
//
//		                for (int i = 1; i < rows.size(); i++) 
//		                {
//		                    String[] row = rows.get(i);
//		                    if (row == null || row.length == 0 || (row[0] != null && row[0].trim().isEmpty())) continue;
//	
//		                    try 
//		                    {
//		                    	
//		                    	 int expectedColumns = 47; // actual columns in CSV
//		                         int paramCount = 49; 
//		                            
//		                         if (row.length != expectedColumns) 
//		                            {
//		                                logger.error("Skipping row {} from file {}: Expected {} columns, found {}", i, file.getName(), expectedColumns, row.length);
//		                                isError = true;
//		                                continue;
//		                            }
//		                         
//		                         Object[] params = new Object[paramCount];
//		                            params[0] = Serial;
//		                            params[1] = seq++;
//	
//		                            for (int j = 0; j < expectedColumns; j++) 
//		                            {
//		                                params[j + 2] = (row[j] != null && !row[j].trim().isEmpty()) ? row[j].trim() : null;
//		                            }
//	
//		                        batch.add(params); 
//	 	                        
//	 	                        
//	 	                        if (batch.size() >= batchSize) 
//	 	                        {
//	 	                            Jdbctemplate.batchUpdate(sql, batch);  // Execute the batch insert
//	 	                            batch.clear();  // Clear the batch after executing
//	 	                        }
//		                    } 
//		                    catch (Exception ex) 
//		                    {
//		                        logger.error("Error inserting row {} from file {}: {}", i, file.getName(), ex.getMessage());
//								isError = true;
//		                    }
//		                }
//		                
//		                
//		                if (!batch.isEmpty()) 
//	                    {
//	                        logger.info("Flushing remaining {} records for file {}", batch.size(), file.getName());
//	                        Jdbctemplate.batchUpdate(sql, batch);
//	                        batch.clear();
//	                    }
//		                
//		            }
//		            catch (Exception e) 
//		            {
//		            	 logger.error("Error processing static file {}: {}", file.getName(), e.getMessage(), e);
//	                }
//	            }
//							
//	           //  bk_path = backup_file(file);
//
//	             if (FILEIT.isEmpty()) 
//	             {
//	                 if (isError) 
//	                 {
//	                     sql = "insert into fileit001(SUBORGCODE, CHCODE, PAYTYPE, REQDATE, REQREFNO, REQTIME, FILETYPE, SRCPATH, DSTPATH, REMARKS, STATUS, RESCODE, RESPDESC) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
//	                     Jdbctemplate.update(sql, new Object[]{
//	                             SUBORGCODE, "FIU", "EDMP_STATIC", util.getCurrentDate("dd-MMM-yyyy"), Serial,
//	                             util.get_oracle_Timestamp(), "CSV", file.getPath(), bk_path, Serial, "FAILED", "200", "FAILED"
//	                     });
//	                 } 
//	                 else 
//	                 {
//	                     sql = "insert into fileit001(SUBORGCODE, CHCODE, PAYTYPE, REQDATE, REQREFNO, REQTIME, FILETYPE, SRCPATH, DSTPATH, REMARKS, STATUS, RESCODE, RESPDESC) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
//	                     Jdbctemplate.update(sql, new Object[]{
//	                             SUBORGCODE, "FIU", "EDMP_STATIC", util.getCurrentDate("dd-MMM-yyyy"), Serial,
//	                             util.get_oracle_Timestamp(), "CSV", file.getPath(), bk_path, Serial, "SUCCESS", "200", "Static file processed successfully"
//	                     });
//	                 }
//	             }
//	         }
//
//	         details.addProperty("result", "success");
//	         details.addProperty("stscode", "HP00");
//	         details.addProperty("message", "All static files processed successfully and moved to backup folder.");
//	         logger.info("All static files processed successfully and moved to backup folder.");
//
//	     } 
//	     catch (Exception e) 
//	     {
//	         details.addProperty("result", "failed");
//	         details.addProperty("stscode", "HP06");
//	         details.addProperty("message", e.getLocalizedMessage());
//	         logger.error("Exception in EDMP_STATIC_FILE_READ: {}", e.getLocalizedMessage(), e);
//	     }
//
//	     return details;
//	 }
	
 	public JsonObject get_file_Id(FIU_Menu info)
 	{
 		JsonObject details = new JsonObject();
 		
 		try 
 		{					
 			Common_Utils util = new Common_Utils();
 			Gson gson = new Gson();
 		    JsonObject Info = gson.toJsonTree(info).getAsJsonObject();
 		     		
 		    String paytype = !util.isNullOrEmpty(Info.get("i_paytype").getAsString()) ? Info.get("i_paytype").getAsString() : "NA" ;
 		    String user 	= "ADMIN" ;
 		    String fromdate = !util.isNullOrEmpty(Info.get("i_fromdate").getAsString()) ? Info.get("i_fromdate").getAsString() : "" ;
 		    String todate = !util.isNullOrEmpty(Info.get("i_todate").getAsString()) ? Info.get("i_todate").getAsString() : "" ;
 		    String from_amt_str = !util.isNullOrEmpty(Info.get("i_famount").getAsString()) ? Info.get("i_famount").getAsString() : "0" ;
 		    String to_amt_str = !util.isNullOrEmpty(Info.get("i_tamount").getAsString()) ? Info.get("i_tamount").getAsString() : "0" ;
 		    String i_reason = !util.isNullOrEmpty(Info.get("i_reason").getAsString()) ? Info.get("i_reason").getAsString() : "NA" ;
 		    String i_action = !util.isNullOrEmpty(Info.get("i_action").getAsString()) ? Info.get("i_action").getAsString() : "NA" ;
 		    String i_dc = !util.isNullOrEmpty(Info.get("i_dc").getAsString()) ? Info.get("i_dc").getAsString() : "NA" ;
 		    String i_sub_code = !util.isNullOrEmpty(Info.get("i_sub_code").getAsString()) ? Info.get("i_sub_code").getAsString() : "NA" ;
 		  
 		    
 		   Double from_amt = Double.parseDouble(from_amt_str);
 		  Double to_amt   = Double.parseDouble(to_amt_str);
 		  
 		   SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
 		  SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

 		  java.util.Date fromDateUtil = inputFormat.parse(fromdate);
 		  java.util.Date toDateUtil = inputFormat.parse(todate);


 		   
 			String procedureCall = "{CALL PACK_FIU_REPORT1.PROC_FIU_MASTER(?,?,?,?,?,?,?,?,?,?,?)}";
 			
 		      Map<String, Object> resultMap = Jdbctemplate.call(
 		             new CallableStatementCreator() {
 		                 @Override
 		                 public CallableStatement createCallableStatement(Connection connection) throws SQLException {
 		                     CallableStatement cs = connection.prepareCall(procedureCall);

 		                    cs.setString(1, paytype);
 		                    cs.setString(2, user);
 		          		    cs.setDate(3, java.sql.Date.valueOf(outputFormat.format(fromDateUtil)));
 		        		    cs.setDate(4, java.sql.Date.valueOf(outputFormat.format(toDateUtil)));
 		        		    cs.setDouble(5, from_amt);
 		        		    cs.setDouble(6, to_amt);
 		                    cs.setString(7, i_reason);
 		                    cs.setString(8, i_action);
 		                    cs.setString(9, i_dc);
 		                    cs.setString(10, i_sub_code);

 		                     cs.registerOutParameter(11, Types.NUMERIC);
 		                     return cs;
 		                 }
 		             },
 		             getparams() // Assuming this returns the parameter definitions
 		         );
 			
 		       Object outParam = resultMap.get("o_file_id"); // Key name depends on your DB/driver
 	           details.addProperty("Result", "Success");
 	           details.addProperty("File_Id", outParam != null ? outParam.toString() : "NULL");

		
			
		} catch (Exception e) {
 			details.addProperty("Result", "Failed");
 			details.addProperty("Message", e.getMessage());
 			
 			logger.debug("Exception get_file_Id :::::"+e.getLocalizedMessage());
		}		

		 return details;  
 	}
	
	

 	public JsonObject store_report002_fiu(String payload)
 	{
 		
 		JsonObject details = new JsonObject();
				
 		try 
 		{			
 			Common_Utils util = new Common_Utils();
 			
 			
 			JsonObject data = util.StringToJsonObject(payload);
 			
 			
 			String type_trans = data.get("type").getAsString();
 			String mode_trans = data.get("subCode").getAsString();
 			String from_date = data.get("fromDate").getAsString(); 
 			String to_date = data.get("toDate").getAsString();
 			String amt_trans = data.get("amount").getAsString();
 			String d_credit_debit = data.get("direction").getAsString();
 			String t_amt = data.get("i_tamount").getAsString();
 				
 			
 			System.out.println(t_amt);
 			 String Serial = Generate_FIU_Serial().get("Serial").getAsString();
        				
 			 String procedureCall = "{CALL PACK_FIU_REPORT.store_report002_fiu(?,?,?,?,?,?,?,?)}";  
 			 
 			 System.out.println(Serial);
 			 
 			Jdbctemplate.execute((Connection connection) -> {
 	            CallableStatement cs = connection.prepareCall(procedureCall);
 	            cs.setString(1, Serial);
 	            cs.setString(2, type_trans);
 	            cs.setString(3, mode_trans);
 	            cs.setString(4, from_date);
 	            cs.setString(5, to_date);
 	            cs.setString(6, amt_trans);
 	            cs.setString(7, t_amt);            
 	            cs.setString(8, d_credit_debit);
 	            cs.execute();
 	            cs.close();
 	            return null;
 	        });
 			
 			String query = "SELECT COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, "
	                + "COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13 "
	                + "FROM REPORT002_FIU WHERE SERIAL = ? AND COLUMN1 = ?";
 
	       List<Map<String, Object>> rows = Jdbctemplate.queryForList(query, Serial , "C");
 			
 			JsonArray dataArray = new JsonArray();
 			 
 		      for (Map<String, Object> row : rows) {
 		          JsonObject obj = new JsonObject();
 	 
 		          obj.addProperty("REPORTTYPE",          row.get("COLUMN2")  != null ? row.get("COLUMN2").toString()  : "");
 		          obj.addProperty("TRANSACTION_TYPE",    row.get("COLUMN3")  != null ? row.get("COLUMN3").toString()  : "");
 		          obj.addProperty("TRANSACTION_DATE",    row.get("COLUMN4")  != null ? row.get("COLUMN4").toString()  : "");
 		          obj.addProperty("DEBIT_CREDIT",        row.get("COLUMN5")  != null ? row.get("COLUMN5").toString()  : "");
 		          obj.addProperty("TRANSACTION_AMT",     row.get("COLUMN6")  != null ? row.get("COLUMN6").toString()  : "");
 		          obj.addProperty("CURRENCY",            row.get("COLUMN7")  != null ? row.get("COLUMN7").toString()  : "");
 		          obj.addProperty("DEBIT_ACCT",          row.get("COLUMN8")  != null ? row.get("COLUMN8").toString()  : "");
 		          obj.addProperty("CREDIT_ACCT",         row.get("COLUMN9")  != null ? row.get("COLUMN9").toString()  : "");
 		          obj.addProperty("SENDER_NAME",         row.get("COLUMN10") != null ? row.get("COLUMN10").toString() : "");
 		          obj.addProperty("RECEIVER_NAME",       row.get("COLUMN11") != null ? row.get("COLUMN11").toString() : "");
 		          obj.addProperty("SOURCE_BANK",         row.get("COLUMN12") != null ? row.get("COLUMN12").toString() : "");
 		          obj.addProperty("DESTINATION_BANK",    row.get("COLUMN13") != null ? row.get("COLUMN13").toString() : "");
 	 
 		          dataArray.add(obj);
 		      }
 	 	 
 		     details.add("Data", dataArray);
 	 
 	         details.addProperty("Result", "SUCCESS");
 		     details.addProperty("Message", "Report generated successfully");
 		
		} 
 		catch (Exception e) 
 		{
			details.addProperty("Result", "Failed");
 			details.addProperty("Message", e.getMessage());
 			
 			logger.debug("Exception store_report002_fiu :::::"+e.getLocalizedMessage());
		}
 		return details;
 	}

 	public String backup_file(File file)
 	{
 		Path targetPath = null;
 		
     	try 
        {
            String propSql = "SELECT MTYPEPARAM FROM prop001 WHERE CHCODE=?";
            List<String> prop = Jdbctemplate.queryForList(propSql, new Object[]{"FIU_bk"}, String.class);

            if (!prop.isEmpty()) 
            {
                String targetFolderPath = prop.get(0);
                File backupDir = new File(targetFolderPath);
                if (!backupDir.exists()) backupDir.mkdirs();

                Path sourcePath = file.toPath();
                targetPath = new File(backupDir, file.getName()).toPath();

                // Short pause to ensure file lock is released
                Thread.sleep(100);

                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("File moved to backup: {}", targetPath);
                              
            } 
            else 
            {
                logger.warn("Backup path not found in prop001 for CHCODE='FIU_bk'");
            }
            


        } 
        catch (Exception moveEx) 
        {
            logger.error("Failed to move file {} -> {}", file.getName(), moveEx.getMessage());
        }
     	
     	 return targetPath != null ? targetPath.toString() : null;
 	}
	

	
	

	
	
	public JsonObject Generate_FIU_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select fiu_serial.Nextval from dual";
			   
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

 	
 	
 	public List<SqlParameter> getparams()
    {
  	     List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
  	     
		 inParamMap.add(new SqlParameter("i_paytype"  , Types.VARCHAR));
		 inParamMap.add(new SqlParameter("i_userid"   , Types.VARCHAR)); 
		 inParamMap.add(new SqlParameter("i_fromdate" , Types.DATE));
		 inParamMap.add(new SqlParameter("i_todate"   , Types.DATE));
		 inParamMap.add(new SqlParameter("i_famount"  , Types.NUMERIC));
		 inParamMap.add(new SqlParameter("i_tamount"  , Types.NUMERIC));
		 inParamMap.add(new SqlParameter("i_reason"   , Types.VARCHAR));
		 inParamMap.add(new SqlParameter("i_action"   , Types.VARCHAR));
		 inParamMap.add(new SqlParameter("i_dc"       , Types.VARCHAR));
		 inParamMap.add(new SqlParameter("i_sub_code" , Types.VARCHAR));
		 inParamMap.add(new SqlOutParameter("o_filesl", Types.NUMERIC));	
  	    
  	     return inParamMap;
    }
 	
 	

}
