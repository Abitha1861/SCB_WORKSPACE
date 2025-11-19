package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.FILEIT002;
import com.hdsoft.Repositories.FILEIT003;
import com.hdsoft.Repositories.FILEIT005;
import com.hdsoft.common.Common_Utils;
import com.opencsv.CSVReader;
import com.zaxxer.hikari.HikariDataSource;

@Component
@Controller
public class FileIT_Modal 
{
	protected JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public FileIT_Modal() {}
	
	public FileIT_Modal(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	static class ColumnInfo 
	{
        String name;
        int position;
        String type;
        String TableName;

        ColumnInfo(String name, int position, String type, String TableName) 
        {
            this.name = name;
            this.position = position;
            this.type = type;
            this.TableName = TableName;
        }
    }
	
	private static final Logger logger = LogManager.getLogger(FileIT_Modal.class);
	
	@RequestMapping(value = {"/Datavision/FileIT/download"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String FileIT_download(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
		  
   	    Common_Utils util = new Common_Utils();
   	    
   	    try
   	    {
   	    	JsonObject js = util.StringToJsonObject(MESSAGE);
   	     
	   	    String sql = "select MTYPEPARAM from prop001 where CHCODE = ? and MODULEID = ?";
			
			List<String> FilePaths = Jdbctemplate.queryForList(sql, new Object[] { "FILEIT", js.get("Source").getAsString() }, String.class);
			
			String DstPath = FilePaths.size() > 0 ? FilePaths.get(0) : "OTHERS";
			
			MinIo_Modal MinIo = new MinIo_Modal(Jdbctemplate);
			
			details = MinIo.Download_File(DstPath, js.get("SrcFileName").getAsString());  
   	    }
   	    catch(Exception ex)
   	    {
   	    	details.addProperty("err", ex.getLocalizedMessage());
   	    }
   	  
   	 	return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/CC/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
		  
   	    Common_Utils util = new Common_Utils();
   	    
   	    JsonObject js = util.StringToJsonObject(MESSAGE);
   	 
   	    details = Store_CC_EBBS_File_data(js.get("Source").getAsString(), js.get("Filepath").getAsString());
   	 		
   	    //ALL_SCI_P56LSPAPPRLMTPROFILE.dat
   	    
   	 	return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/CC/test/New"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service2(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
		  
   	    Common_Utils util = new Common_Utils();
   	    
   	    JsonObject js = util.StringToJsonObject(MESSAGE);
   	 
   	    details = Store_CC_EBBS_File_data_New(js.get("Source").getAsString(), js.get("Filepath").getAsString());
   	 		
   	    //ALL_SCI_P56LSPAPPRLMTPROFILE.dat
   	    
   	 	return details.toString();
    }
	
	public JsonObject Store_CC_EBBS_File_data_New(String Source, String Filepath) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String Extension = FilenameUtils.getExtension(Filepath);
			 
			 if(Extension.equals("Z"))
			 {
				 Filepath = unzipZFile(Filepath, FilenameUtils.getFullPath(Filepath));
				 
				 logger.debug("unzipped file path :::: "+Filepath);
			 }
			 
			 String fileName = FilenameUtils.getBaseName(Filepath) + "." + FilenameUtils.getExtension(Filepath);
			 
			 logger.debug("final fileName :::: "+fileName);
			 
			 String FileFormat = getfileformat(fileName);  
			
			 logger.debug("final FileFormat :::: "+FileFormat);
			 
			 int batchSize = 500;  String date_format = Source.equals("EBBS") ? "TO_DATE(trim(?), 'YYYY-MM-DD')" : "to_date(trim(?), 'YYYYMMDD')";
			
			 List<ColumnInfo> columns = Jdbctemplate.query("SELECT COLUMN_NAME, COLUMN_POS, COLUMN_TYPE, TABLENAME FROM fileit002 WHERE CHCODE=? and FILEFORMAT = ? ORDER BY COLUMN_POS", new Object[]{ Source, FileFormat },
	         (rs, rowNum) ->   new ColumnInfo( rs.getString("COLUMN_NAME"),  rs.getInt("COLUMN_POS"),  rs.getString("COLUMN_TYPE"), rs.getString("TABLENAME")) );
 
	         StringJoiner colNames = new StringJoiner(", ");
	         StringJoiner valPlaceholders = new StringJoiner(", ");
		        
	         String tableName = "";
	         
	         for(ColumnInfo col : columns) 
	         {
	            colNames.add(col.name);
	            
	            if ("D".equalsIgnoreCase(col.type)) 
	            {
	                valPlaceholders.add(date_format);
	            } 
	            else 
	            {
	                valPlaceholders.add("?");
	            }
	            
	            tableName = col.TableName;
	         }
	         
	         String insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, colNames, valPlaceholders);
	         
	         logger.debug("Generated SQL: " + insertSQL);
	         
	         List<String[]> batch = new ArrayList<>();
	         
	         int totalInserted = 0;
	         
	         try(BufferedReader reader = new BufferedReader(new FileReader(Filepath))) 
	         {
	             String line;
	             
	             int lineNo = 0; String delimeter = "<tab>";

	             while ((line = reader.readLine()) != null)
	             {
	                 lineNo++;
	                 
	                 line = line.replaceAll("\\|", "<tab>");
	                 line = line.replaceAll("§", "<tab>");
	                 line = line.replaceAll("\\§", "<tab>");
	                 line = line.replaceAll("Â", "");
	                 line = line.replaceAll("\\Â", "");
	                 
	                // if(lineNo == 1) {
	                	 
	                	// if(!line.contains("§"))  delimeter = "\\|";
	                	 
	                	 //continue;	 
	                // }
	                 
	                 //line = line.replaceAll("Â", "");
			    	
	                // String[] dataFields = line.split(delimeter, -1);
	                  
	                // String[] values = Arrays.copyOfRange(dataFields, 1, dataFields.length);
	                 
	                 String[] dataFields = line.split(delimeter, -1);
	                  
	                 String[] values = {};
	                 
	                 if(!Source.equalsIgnoreCase("EBBS"))
	                 {
	                	 values = Arrays.copyOfRange(dataFields, 1, dataFields.length);
	                 }
	                 else 
	                 {
	                	 values = Arrays.copyOfRange(dataFields, 0, dataFields.length);
					 }
	                 
	                 //logger.debug("values length "+values.length);
	                 //logger.debug("columns length "+columns.size());
	                 
	                 if(values.length != columns.size()) 
	                 {
	                	 logger.debug("{} : Skipping line {}: Expected {} values, found {}, datas are {}", fileName, lineNo, columns.size(), values.length, line);
	                	 
	                     continue;
	                 }
	                 
	                 batch.add(values);

	                 if (batch.size() == batchSize) 
	                 {
	                     totalInserted += executeBatch(insertSQL, columns, batch);
	                     batch.clear();
	                 }
	             }

	             // Final batch
	             if(!batch.isEmpty()) 
	             {
	                 totalInserted += executeBatch(insertSQL, columns, batch);
	             }
	         }

	         logger.debug("Total records inserted: " + totalInserted);
			 
			 details.addProperty("Result", totalInserted !=0 ? "Success" : "Failed");
			 details.addProperty("Message", totalInserted !=0 ? "Data inserted successfully !!" : "Data not inserted");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Get_Webserice_Info :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	private int executeBatch(String sql, List<ColumnInfo> columns, List<String[]> batch) 
	{
		try
		{
			Jdbctemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				
	            @Override
	            public void setValues(PreparedStatement ps, int i) throws SQLException 
	            {
	                String[] row = batch.get(i);
	                
	                for (int j = 0; j < columns.size(); j++) 
	                {
	                    String val = row[j].trim();
	                    ps.setString(j + 1, val.isEmpty() ? null : val);
	                }
	            }
	
	            @Override
	            public int getBatchSize() {
	                return batch.size();
	            }
	        });
		}
		catch(Exception ex)
		{
			logger.debug("exception in executeBatch "+ex.getLocalizedMessage());
		}	
		
		return batch.size();
    }
	
	public JsonObject Store_CC_EBBS_File_data(String Source, String Filepath) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 String Extension = FilenameUtils.getExtension(Filepath);
			 
			 logger.debug("file path :::: "+Filepath);
			 
			 if(Extension.equals("Z"))
			 {
				 Filepath = unzipZFile(Filepath, FilenameUtils.getFullPath(Filepath));
				 
				 logger.debug("unzipped file path :::: "+Filepath);
			 }
			 
			 String fileName = FilenameUtils.getBaseName(Filepath) + "." + FilenameUtils.getExtension(Filepath);
			 
			 logger.debug("final fileName :::: "+fileName);
			 
			 String FileFormat = getfileformat(fileName);  
			 			 
			 logger.debug("final FileFormat :::: "+FileFormat);
			 
			 String sql = "Select * from fileit002 where FILEFORMAT=? order by COLUMN_SL";
			 
			 List<FILEIT002> Info = Jdbctemplate.query(sql, new Object[] { FileFormat }, new FileIT002_Mapper() );
			 
			 sql = "Select * from fileit005 where FILEFORMAT=?";
			 
			 List<FILEIT005> Info2 = Jdbctemplate.query(sql, new Object[] { FileFormat }, new FileIT005_Mapper() );
			 
			 boolean delta = false;
			 
			 if(Info2.size() !=0)
			 {
				 sql = "select count(*) from fileit003 t where PURPCODE = (select distinct(PURPCODE) from fileit002 where CHCODE = t.chcode and FILEFORMAT = ?)";
				 
				 int count = Jdbctemplate.queryForObject(sql, new Object[] { FileFormat }, Integer.class);
				 
				 String DeltaCheck = Info2.get(0).getDUPCHECK();
				 
				 delta = count != 0 && DeltaCheck.equalsIgnoreCase("1") ? true : false;
			 }
			 
			 StringBuilder dsql = new StringBuilder("select count(*) from fileit003 where SUBORGCODE=? and CHCODE=? and PURPCODE=?");
			 
			 StringBuilder sqlBuilder = new StringBuilder("INSERT INTO fileit003 (SUBORGCODE,INDATE,INTIME,CHCODE,PURPCODE,RTYPE,");
		     
			 StringBuilder placeholdersBuilder = new StringBuilder(" VALUES (?,?,?,?,?,?,");

			 String fIndex = "0", CHCODE = "", PURPCODE = ""; int Totalcolumns = 0;
			 
			 JsonArray Js = new Gson().toJsonTree(Info).getAsJsonArray();
			 
			 for (int i = 0; i < Info.size(); i++) 
			 {
	            sqlBuilder.append("column").append(i + 1);
	            placeholdersBuilder.append("?");
	            
	            if(Info.get(i).getCOLUMN_ISPRIM().equals("1"))
	            {
	            	dsql.append(" and column").append(i + 1).append("=?");
	            }
	            
	            if (i < Info.size() - 1) 
	            {
	                sqlBuilder.append(", ");
	                placeholdersBuilder.append(", ");
	              
	                fIndex = util.isNullOrEmpty(Info.get(i).getFROMINDEX()) ? "0" : Info.get(i).getFROMINDEX() ;
	                CHCODE = Info.get(i).getCHCODE();
	                PURPCODE = Info.get(i).getPURPCODE();
	                Totalcolumns = Integer.parseInt(Info.get(i).getTOTCOLUMNS());
	            }
			 }
			 
			 sqlBuilder.append(")");
		     placeholdersBuilder.append(")");
		     
		     sql = sqlBuilder.toString() + placeholdersBuilder.toString();
		     
		     BufferedReader br = new BufferedReader(new FileReader(Filepath));
		         
			 String line = "";
			 
			 int i = 0;
			 
			 String date = util.getCurrentDate("dd-MMM-yyyy");
			 
			 Timestamp Timestamp = util.get_oracle_Timestamp();
			 
			 while ((line = br.readLine()) != null) 
             {
				 //String stsEndpoint = line; 
		            
				 String stsEndpoint = new String(line.getBytes("ISO-8859-1"), StandardCharsets.UTF_8);
	
				 if(i >= Integer.parseInt(fIndex)) 
				 {
					 stsEndpoint = stsEndpoint.replaceAll("\\|", "<tab>");
			    	 stsEndpoint = stsEndpoint.replaceAll("§", "<tab>");
			    	 stsEndpoint = stsEndpoint.replaceAll("\\§", "<tab>");
			    	 stsEndpoint = stsEndpoint.replaceAll("Â", "");
			    	 stsEndpoint = stsEndpoint.replaceAll("\\Â", "");
			    	 stsEndpoint = stsEndpoint.concat("<tab>");
			    	 stsEndpoint = stsEndpoint.replaceAll("><", "> <");
			    	 
			    	 String[] data = stsEndpoint.split("<tab>");
			    	 			
			    	 if(data.length != Totalcolumns) 
			    	 {
			    		 stsEndpoint = stsEndpoint.replaceAll("\\?", "<tab>");
			    		 stsEndpoint = stsEndpoint.replaceAll("><", "> <");
			    		 
			    		 data = stsEndpoint.split("<tab>");
			    	 }
			    	 
			    	 if(data.length == Totalcolumns) 
			    	 {
			    		 List<Object> values = new ArrayList<Object>();  
			    		 List<Object> dvalues = new ArrayList<Object>(); 
			    		 List<Object> Lbvalues = new ArrayList<Object>();
				    	 
				    	 values.add(SUBORGCODE);
				    	 values.add(date);
				    	 values.add(Timestamp);
				    	 values.add(CHCODE);
				    	 values.add(PURPCODE);
				    	 values.add("D");
				    	 
				    	 Lbvalues.add(SUBORGCODE);
				    	 Lbvalues.add(date);
				    	 Lbvalues.add(Timestamp);
				    	 Lbvalues.add(CHCODE);
				    	 Lbvalues.add(PURPCODE);
				    	 Lbvalues.add("H");
				    	 
				    	 if(delta)
				    	 {
				    		 dvalues.add(SUBORGCODE);
				    		 dvalues.add(CHCODE);
				    	 	 dvalues.add(PURPCODE);
				    	 }
				    	 
				    	 for(int j=0; j < Js.size(); j++)
				    	 {
				    		 JsonObject ji = Js.get(j).getAsJsonObject(); 
				    		 
				    		 int index = Integer.parseInt(ji.get("COLUMN_POS").getAsString()) - 1;
				    		 
				    		 String data_ = data[index];
				    		 
				    		 values.add(data_);
				    		 
				    		 Lbvalues.add(ji.get("COLUMN_NAME").getAsString());
				    		 
				    		 if(ji.get("COLUMN_ISPRIM").getAsString().equals("1") && delta)
				    		 {
				    			 dvalues.add(data_);
				    		 }
				    	 }
				    	 
				         Object[] objectArray = values.toArray();   
				         Object[] dobjectArray = dvalues.toArray(); 
				         Object[] LobjectArray = Lbvalues.toArray();
				         
				         try
				         {
				        	 //logger.debug("checking query "+dsql);
				        	 
				        	 //logger.debug(new Gson().toJson(dobjectArray));
				        	 
				        	 if(i == Integer.parseInt(fIndex))
				        	 {
				        		 String Dquery = "select count(*) from fileit003 where SUBORGCODE=? and CHCODE=? and PURPCODE=?";
				        		 
				        		 int count = Jdbctemplate.queryForObject(Dquery, new Object[] { SUBORGCODE , CHCODE, PURPCODE }, Integer.class);
					        	 
				        		 logger.debug("head count >>> "+count);
				        		 
					        	 if(count == 0)
					        	 {
					        		 Jdbctemplate.update(sql, LobjectArray); 
					        	 }	
				        	 }
				        	 else
				        	 {
				        		 if(delta)
				        		 {
				        			 int count = Jdbctemplate.queryForObject(dsql.toString(), dobjectArray, Integer.class);
				        			 
				        			 logger.debug("data count >>> "+count);
						        	 
						        	 if(count == 0)
						        	 {
						        		 logger.debug("insert query "+sql);
							        	 
							        	 logger.debug(new Gson().toJson(objectArray));
							        	 
						        		 int out = Jdbctemplate.update(sql, objectArray); 
						        		 
						        		 logger.debug(">>> inserted out "+out+" <<<");
						        	 }	
				        		 }
				        		 else
				        		 {
				        			 int out = Jdbctemplate.update(sql, objectArray); 
					        		 
					        		 logger.debug(">>> File : "+fileName+" ,  Row "+(i+1)+" inserted out "+out+" <<<");
				        		 }
				        	 } 
				         }
				         catch(Exception ex) {  logger.debug("Exception when inserting data :::: "+ex.getLocalizedMessage()); } 
			    	 }
			    	 else
			    	 {
			    		 logger.debug("Array size   ::: "+data.length);
				    	 logger.debug("Totalcolumns ::: "+Totalcolumns);
				    	 
			    		 logger.debug("Non Replaced Row ::: "+stsEndpoint);
			    	 }
				 }
				 
				 i++;
             }
			 
			 br.close();
			 
			 /*if(Info2.size() !=0)
			 {
				 Common_Utils utils = new Common_Utils();
				 
				 String CleanupQuery = Info2.get(0).getQUERY1();
				 String CleanupQuery2 = Info2.get(0).getQUERY2();
				 String CleanupQuery3 = Info2.get(0).getQUERY3();
				 
				 if(!utils.isNullOrEmpty(CleanupQuery))
				 {
					 int out = Jdbctemplate.update(CleanupQuery); 
	        		 
	        		 logger.debug(">>> Cleanup Query out "+out+" <<<");
				 }
				 
				 if(!utils.isNullOrEmpty(CleanupQuery2))
				 {
					 int out = Jdbctemplate.update(CleanupQuery2); 
	        		 
	        		 logger.debug(">>> Cleanup Query out "+out+" <<<");
				 }
				 
				 if(!utils.isNullOrEmpty(CleanupQuery3))
				 {
					 int out = Jdbctemplate.update(CleanupQuery3); 
	        		 
	        		 logger.debug(">>> Cleanup Query out "+out+" <<<");
				 }
			 }*/
			 
			 //details.addProperty("Result", API_Info.size()!=0 ? "Success" : "Failed");
			 //details.addProperty("Message", API_Info.size()!=0 ? "API Configuration Details Found !!" : "API Configuration Details Not Found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Get_Webserice_Info :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Store_CDS_ReportData(String FilePath, String RefId, String Date, String Source)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 
			/* String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype)";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "CDS_REPORT"}, String.class);
			 
			 if(Information.size() == 0)
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "CDB report is not found");
				 
				 logger.debug(">>>>>> CDB report is not found <<<<<<<<");
				 
				 return details;
			 }*/
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 			 
			 File file = new File(FilePath); 
			 			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 if(file.exists() && !file.isDirectory()) 
			 { 
				 	List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    logger.debug("<<<< CDS_REPORT content >>>");
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 11) 
					    	{
						    	 records.add(Arrays.asList(values));
						    	 
						    	 logger.debug(Arrays.asList(values));
					    	}
					    }
					}
					
					sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", "", "CDS_REPORT" });
					
					sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12, column13, column14, column15) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					 
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", "", "CDS_REPORT", "serial", "InstrumentType", "HoldingStatus", "Maturity", "MaturityDate", "EffectiveDate", "DTM", "Bond_Auction_Number", "Holding_Number", "FaceValue", "Price_100", "Coupon_Rate" });
					 					
					for(int i=1; i<records.size(); i++)
					{  
						String InstrumentType = records.get(i).get(0).trim();
						String HoldingStatus = records.get(i).get(1).trim();
						String Maturity = records.get(i).get(2).trim();
						String MaturityDate = records.get(i).get(3).trim();
						String EffectiveDate = records.get(i).get(4).trim();
						String DTM = records.get(i).get(5).trim();
						String Bond_Auction_Number = records.get(i).get(6).trim();
						String Holding_Number = records.get(i).get(7).trim();
						String FaceValue = records.get(i).get(8).trim();
						String Price_100 = records.get(i).get(9).trim();
						String Coupon_Rate = records.get(i).get(10).trim();
						
						sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12, column13, column14, column15) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						 
						Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", "", "CDS_REPORT", i+1, InstrumentType, HoldingStatus, Maturity, MaturityDate, EffectiveDate, DTM, Bond_Auction_Number, Holding_Number, FaceValue, Price_100, Coupon_Rate });				 				
					}
					
					if(records.size() == 0)
					{
						logger.debug(">>>>>> CDS REPORT file content is empty or not able to retrieve records <<<<<<<<");
						
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "CDS REPORT file content is empty or not able to retrieve records", Date, RefId });
					}
					else
					{
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS READY", Date, RefId });
						
						logger.debug(">>>>>> CDS REPORT file is ready for consumption >>>> Serial >>>> "+O_SERIAL);
					}
					
					details.addProperty("result",  records.size() > 0 ? "success" : "failed");
					details.addProperty("stscode", records.size() > 0 ? "200" : "400");
					details.addProperty("message", records.size() > 0 ? "CDS REPORT file is ready" : "CDS REPORT file content is empty or not able to retrieve records");
			 }
			 else
			 {
				 sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
					
				 Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS NOT ACCESSIBLE", Date, RefId });
					
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "401");
				 details.addProperty("message", "CDS REPORT file is not accessible");
				 
				 logger.debug(">>>>>> CDS REPORT file is not accessible <<<<<<<<"); 
			 }
			 
			 return details;
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "500");
			 details.addProperty("message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in Store_CDS_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject Store_RiskView_ReportData(String FilePath, String RefId, String Date, String Source)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			// System.out.println(FilePath);
			 
			/* String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype)";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "CDS_REPORT"}, String.class);
			 
			 if(Information.size() == 0)
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "CDB report is not found");
				 
				 logger.debug(">>>>>> CDB report is not found <<<<<<<<");
				 
				 return details;
			 }*/
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 File file = new File(FilePath); 
			 			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			  
			 if(file.exists() && !file.isDirectory()) 
			 { 
				 	List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    logger.debug("<<<< RISK VIEW REPORT Content >>>");
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 13) 
					    	{
						    	 records.add(Arrays.asList(values));
						    	 
						    	 logger.debug(Arrays.asList(values));
					    	}
					    }
					}
					
					sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", "", "RISK_VIEW_REPORT" });
					
					sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12, column13, column14, column15, column16, column17) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					 
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", "", "RISK_VIEW_REPORT", "serial", "ods", "tp_system_cd", "unique_id", "inst_id", "txn_currency_cd", "ecl_amt_tcy", "ecl_amt_lcy", "ecl_allowance_USD", "stage", "cg_grade_orig", "client_business", "ps_product_id", "Asset_Classification" });
					 	
					for(int i=1; i<records.size(); i++)
					{  
						String ods = records.get(i).get(0).trim();
						String tp_system_cd = records.get(i).get(1).trim();
						String unique_id = records.get(i).get(2).trim();
						String inst_id = records.get(i).get(3).trim();
						String txn_currency_cd = records.get(i).get(4).trim();
						String ecl_amt_tcy = records.get(i).get(5).trim();
						String ecl_amt_lcy = records.get(i).get(6).trim();
						String ecl_allowance_USD = records.get(i).get(7).trim();
						String stage = records.get(i).get(8).trim();
						String cg_grade_orig = records.get(i).get(9).trim();
						String client_business = records.get(i).get(10).trim();
						String ps_product_id = records.get(i).get(10).trim();
						String Asset_Classification = records.get(i).get(10).trim();
						
						sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL,column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12, column13, column14, column15, column16, column17) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						 
						Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", "", "RISK_VIEW_REPORT", i+1, ods,	tp_system_cd,	unique_id,	inst_id,	txn_currency_cd,	ecl_amt_tcy,	ecl_amt_lcy,	ecl_allowance_USD,	stage,	cg_grade_orig,	client_business,	ps_product_id,	Asset_Classification });				 				
					}
					
					if(records.size() == 0)
					{
						logger.debug(">>>>>> RISK VIEW REPORT file content is empty or not able to retrieve records <<<<<<<<");
						
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "RISK VIEW REPORT file content is empty or not able to retrieve records", Date, RefId });
					}
					else
					{
						/*sql = "select count(*) from fileit001 set where REQDATE=? and REQREFNO=?";
						 
						int count = Jdbctemplate.queryForObject(sql, new Object[] { Date, RefId }, Integer.class);
						 
						if(count == 0)
						{*/
							sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
							
							Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS READY", Date, RefId });
							
							logger.debug(">>>>>> RISK VIEW REPORT file is ready for consumption >>>> Serial >>>> "+O_SERIAL);
						/*}
						else
						{
							
						}*/
					}
					
					details.addProperty("result",  records.size() > 0 ? "success" : "failed");
					details.addProperty("stscode", records.size() > 0 ? "200" : "400");
					details.addProperty("message", records.size() > 0 ? "RISK VIEW REPORT file is ready" : "RISK VIEW REPORT file content is empty or not able to retrieve records");
			 }
			 else
			 {
				/* sql = "select count(*) from fileit001 set where REQDATE=? and REQREFNO=?";
				 
				 int count = Jdbctemplate.queryForObject(sql, new Object[] { Date, RefId }, Integer.class);
				 
				 if(count == 0)
				 {*/
					 sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
					 Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS NOT ACCESSIBLE", Date, RefId });
				 //}
				 
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "401");
				 details.addProperty("message", "Risk View REPORT file is not accessible");
				 
				 logger.debug(">>>>>> RISK VIEW REPORT file is not accessible <<<<<<<<"); 
			 }
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "500");
			 details.addProperty("message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in Store_CDS_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject Store_PAST_DUE_DAYS_RECON(String FilePath, String RefId, String Date, String Source)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			/* String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype)";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "CDS_REPORT"}, String.class);
			 
			 if(Information.size() == 0)
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "CDB report is not found");
				 
				 logger.debug(">>>>>> CDB report is not found <<<<<<<<");
				 
				 return details;
			 }*/
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 			 
			 File file = new File(FilePath); 
			 			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 if(file.exists() && !file.isDirectory()) 
			 { 
				 	List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    logger.debug("<<<< PAST_DUE_DAYS_TLM_RECON_FINAL_REPORT content >>>");
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 58) 
					    	{
						    	 records.add(Arrays.asList(values));
						    	 
						    	 logger.debug(Arrays.asList(values));
					    	}
					    }
					}
					
					sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", "", "PAST_DUE_DAYS_TLM_RECON_FINAL_REPORT" });
					
					sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12, column13, column14, column15, column16, column17, column18, column19, column20, column21, column22, column23, column24, column25, column26, column27, column28, column29, column30, column31, column32, column33, column34, column35, column36, column37, column38, column39, column40, column41, column42, column43, column44, column45, column46, column47, column48, column49, column50, column51, column52, column53, column54, column55, column56, column57, column58, column59, column60, column61, column62) "
						     + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					
						Jdbctemplate.update(sql, new Object[] { 
								SUBORGCODE, O_SERIAL, "C", "", "PAST_DUE_DAYS_TLM_RECON_FINAL_REPORT", 
						    "serial", "Local_Acc_No", "Account_Name", "Short_No", "ITEM_ID", "CCY", "Amount", "USD_Equ", "C_D", "Rate", "Value_Date", "Book_Date", "Ageing", "Life_Span", "Age_After_Life_Span", "MaturityDate", "Coding_Date", "Ageing2", "System_Entry_Date", "Source", "TRT", "Reference_1", "Reference_2", "Reference_3", "Reference_4", "Ref_4_Overflow", "Note", "Legacy_Note", "Narrative", "Reference_5", "Country_Comments", "Target_date_of_clearance", "Item_Status", "Age_Based_on_Maturity_Date", "Source_Name", "Message_Feed_Id", "PRODUCT_CODE", "DEPARTMENT_CODE", "Custom_String_11", "Custom_String_15", "Custom_String_17", "Risk_Flag", "Reason_Code", "Reason_Code_Description", "Custom_String_1", "Custom_String_2", "Custom_String_3", "Custom_String_4", "Custom_String_5", "Custom_String_6", "Custom_String_7", "Custom_String_8", "Custom_String_9", "Custom_String_12", "Custom_String_13", "Custom_String_14", "Custom_String_16", "Custom_String_19", "Adjustment_Flag"
					});
						
					for(int i=1; i<records.size(); i++)
					{  
						String LocalAccNo = records.get(i).get(0).trim();
						String accountName = records.get(i).get(1).trim();
						String shortNo = records.get(i).get(2).trim();
						String itemId = records.get(i).get(3).trim();
						String ccy = records.get(i).get(4).trim();
						String amount = records.get(i).get(5).trim();
						String usdEqu = records.get(i).get(6).trim();
						String cd = records.get(i).get(7).trim();
						String rate = records.get(i).get(8).trim();
						String valueDate = records.get(i).get(9).trim();
						String bookDate = records.get(i).get(10).trim();
						String ageing = records.get(i).get(11).trim();
						String lifeSpan = records.get(i).get(12).trim();
						String ageAfterLifeSpan = records.get(i).get(13).trim();
						String maturityDate = records.get(i).get(14).trim();
						String codingDate = records.get(i).get(15).trim();
						String ageingBasedOnCodingDate = records.get(i).get(16).trim();
						String systemEntryDate = records.get(i).get(17).trim();
						String source = records.get(i).get(18).trim();
						String trt = records.get(i).get(19).trim();
						String reference1 = records.get(i).get(20).trim();
						String reference2 = records.get(i).get(21).trim();
						String reference3 = records.get(i).get(22).trim();
						String reference4 = records.get(i).get(23).trim();
						String ref4Overflow = records.get(i).get(24).trim();
						String note = records.get(i).get(25).trim();
						String legacyNote = records.get(i).get(26).trim();
						String narrative = records.get(i).get(27).trim();
						String reference5 = records.get(i).get(28).trim();
						String countryComments = records.get(i).get(29).trim();
						String targetDateOfClearance = records.get(i).get(30).trim();
						String itemStatus = records.get(i).get(31).trim();
						String ageBasedOnMaturityDate = records.get(i).get(32).trim();
						String sourceName = records.get(i).get(33).trim();
						String messageFeedId = records.get(i).get(34).trim();
						String productCode = records.get(i).get(35).trim();
						String departmentCode = records.get(i).get(36).trim();
						String customString11 = records.get(i).get(37).trim();
						String customString15 = records.get(i).get(38).trim();
						String customString17 = records.get(i).get(39).trim();
						String riskFlag = records.get(i).get(40).trim();
						String reasonCode = records.get(i).get(41).trim();
						String reasonCodeDescription = records.get(i).get(42).trim();
						String customString1 = records.get(i).get(43).trim();
						String customString2 = records.get(i).get(44).trim();
						String customString3 = records.get(i).get(45).trim();
						String customString4 = records.get(i).get(46).trim();
						String customString5 = records.get(i).get(47).trim();
						String customString6 = records.get(i).get(48).trim();
						String customString7 = records.get(i).get(49).trim();
						String customString8 = records.get(i).get(50).trim();
						String customString9 = records.get(i).get(51).trim();
						String customString12 = records.get(i).get(52).trim();
						String customString13 = records.get(i).get(53).trim();
						String customString14 = records.get(i).get(54).trim();
						String customString16 = records.get(i).get(55).trim();
						String customString19 = records.get(i).get(56).trim();
						String adjustmentFlag = records.get(i).get(57).trim();
						
						sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12, column13, column14, column15, column16, column17, column18, column19, column20, column21, column22, column23, column24, column25, column26, column27, column28, column29, column30, column31, column32, column33, column34, column35, column36, column37, column38, column39, column40, column41, column42, column43, column44, column45, column46, column47, column48, column49, column50, column51, column52, column53, column54, column55, column56, column57, column58, column59, column60, column61, column62) "
							     + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						
						Jdbctemplate.update(sql, new Object[] { 
						    SUBORGCODE, O_SERIAL, "D", "", "PAST_DUE_DAYS_TLM_RECON_FINAL_REPORT", i+1, LocalAccNo, accountName,
							shortNo, itemId, ccy, amount, usdEqu, cd, rate, valueDate, bookDate, ageing, lifeSpan,
							ageAfterLifeSpan, maturityDate, codingDate, ageingBasedOnCodingDate, systemEntryDate,
							source, trt, reference1, reference2, reference3, reference4, ref4Overflow, note, legacyNote,
							narrative, reference5, countryComments, targetDateOfClearance, itemStatus,
							ageBasedOnMaturityDate, sourceName, messageFeedId, productCode, departmentCode,
							customString11, customString15, customString17, riskFlag, reasonCode, reasonCodeDescription,
							customString1, customString2, customString3, customString4, customString5, customString6,
							customString7, customString8, customString9, customString12, customString13, customString14,
							customString16, customString19, adjustmentFlag });
					}
					
					if(records.size() == 0)
					{
						logger.debug(">>>>>> PAST DUE DAYS TLM RECON FINAL REPORT file content is empty or not able to retrieve records <<<<<<<<");
						
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "PAST DUE DAYS TLM RECON FINAL REPORT file content is empty or not able to retrieve records", Date, RefId });
					}
					else
					{
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS READY", Date, RefId });
						
						logger.debug(">>>>>> PAST DUE DAYS TLM RECON FINAL REPORT is ready for consumption >>>> Serial >>>> "+O_SERIAL);
					}
					
					details.addProperty("result",  records.size() > 0 ? "success" : "failed");
					details.addProperty("stscode", records.size() > 0 ? "200" : "400");
					details.addProperty("message", records.size() > 0 ? "PAST DUE DAYS TLM RECON FINAL REPORT file is ready" : "PAST DUE DAYS TLM RECON FINAL REPORT file content is empty or not able to retrieve records");
			 }
			 else
			 {
				 sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
					
				 Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS NOT ACCESSIBLE", Date, RefId });
					
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "401");
				 details.addProperty("message", "PAST DUE DAYS TLM RECON FINAL REPORT file is not accessible");
				 
				 logger.debug(">>>>>> PAST DUE DAYS TLM RECON FINAL REPORT file is not accessible <<<<<<<<"); 
			 }
			 
			 return details;
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "500");
			 details.addProperty("message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in Store_PAST_DUE_DAYS_TLM_RECON_FINAL_REPORT_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject Store_TREASURY_BONDS(String FilePath, String RefId, String Date, String Source)
	{
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			/* String sql = "select DSTPATH from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype)";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "CDS_REPORT"}, String.class);
			 
			 if(Information.size() == 0)
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "CDB report is not found");
				 
				 logger.debug(">>>>>> CDB report is not found <<<<<<<<");
				 
				 return details;
			 }*/
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 			 
			 File file = new File(FilePath); 
			 			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 if(file.exists() && !file.isDirectory()) 
			 { 
				 	List<List<String>> records = new ArrayList<List<String>>();
					
					try (CSVReader csvReaderr = new CSVReader(new FileReader(file));) 
					{
					    String[] values = null;
					    
					    logger.debug("<<<< TREASURY_BONDS_REPORT content >>>");
					    
					    while ((values = csvReaderr.readNext()) != null)
					    {
					    	if(values.length == 8) 
					    	{
						    	 records.add(Arrays.asList(values));
						    	 
						    	 logger.debug(Arrays.asList(values));
					    	}
					    }
					}
					
					sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
					
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", "", "REASURY_BONDS_REPORT" });
					
					sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", "", "REASURY_BONDS_REPORT", "serial", "Name", "Ticker", "Coupon", "Maturity", "Currency", "Issue_Date", "Identifier", "ISIN"});
					 					
					for(int i=1; i<records.size(); i++)
					{  
						String Name = records.get(i).get(0).trim();
						String Ticker = records.get(i).get(1).trim();
						String Coupon = records.get(i).get(2).trim();
						String Maturity = records.get(i).get(3).trim();
						String Currency = records.get(i).get(4).trim();
						String Issue_Date = records.get(i).get(5).trim();
						String Identifier = records.get(i).get(6).trim();
						String ISIN = records.get(i).get(7).trim();
						
						sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, column1, column2, column3, column4, column5, column6, column7, column8, column9, column10, column11, column12) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						 
						Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", "", "REASURY_BONDS_REPORT", i+1, Name, Ticker, Coupon, Maturity, Currency, Issue_Date, Identifier, ISIN});							
					}
					
					if(records.size() == 0)
					{
						logger.debug(">>>>>> TREASURY_BONDS_REPORT REPORT file content is empty or not able to retrieve records <<<<<<<<");
						
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "TREASURY BONDS REPORT file content is empty or not able to retrieve records", Date, RefId });
					}
					else
					{
						sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
						
						Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS READY", Date, RefId });
						
						logger.debug(">>>>>> TREASURY_BONDS_REPORT is ready for consumption >>>> Serial >>>> "+O_SERIAL);
					}
					
					details.addProperty("result",  records.size() > 0 ? "success" : "failed");
					details.addProperty("stscode", records.size() > 0 ? "200" : "400");
					details.addProperty("message", records.size() > 0 ? "TREASURY_BONDS_REPORT file is ready" : "TREASURY BONDS REPORT file content is empty or not able to retrieve records");
			 }
			 else
			 {
				 sql = "update fileit001 set REMARKS=?, RESPDESC=? where REQDATE=? and REQREFNO=?";
					
				 Jdbctemplate.update(sql, new Object[] { O_SERIAL, "FILE IS NOT ACCESSIBLE", Date, RefId });
					
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "401");
				 details.addProperty("message", "TREASURY_BONDS_REPORT file is not accessible");
				 
				 logger.debug(">>>>>> TREASURY_BONDS_REPORT file is not accessible <<<<<<<<"); 
			 }
			 
			 return details;
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "500");
			 details.addProperty("message", e.getLocalizedMessage());
			 
			 logger.debug("Exception in Store_TREASURY_BONDS_REPORT_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public String unzipZFile(String zFilePath, String outputDirectory) 
	{
		String outputFile = zFilePath.replaceFirst("\\.Z$", "");

        try 
        { 
        	logger.debug("zFilePath: " + zFilePath);
        	logger.debug("outputDirectory: " + outputDirectory);
        	
        	InputStream fileIn = new FileInputStream(zFilePath);
            ZCompressorInputStream zIn = new ZCompressorInputStream(fileIn);
            OutputStream out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int n;
            while ((n = zIn.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            
            logger.debug("File uncompressed successfully: " + outputFile);
            
            zIn.close();
            out.close();
            
            return outputFile; 

        } 
        catch (IOException e) 
        {
        	logger.debug("Error reading or writing file: " + e.getMessage());
            return null;  
        }
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
	
	public String getfileformat(String filename)
	{
		String out = filename;
		
		try
		{
	        String regex = "([^>]*?)_[0-9]{8}[^>]*?.";
	    
	        Pattern pattern = Pattern.compile(regex);
	     
	        Matcher matcher = pattern.matcher(filename);
	        
	        if (matcher.matches()) 
	        {
	        	out = matcher.group(1);
	        } 
		}
		catch(Exception e)
		{
			 
		}
		
		return out;
	}
	
	

	private class FileIT002_Mapper implements RowMapper<FILEIT002> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public FILEIT002 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			FILEIT002 obj_002 = new FILEIT002();  
	
			try
			{
				obj_002.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
				obj_002.setCHCODE(util.ReplaceNull(rs.getString("CHCODE")));
				obj_002.setPURPOSE(util.ReplaceNull(rs.getString("PURPOSE")));
				obj_002.setFILEFORMAT(util.ReplaceNull(rs.getString("FILEFORMAT")));
				obj_002.setPURPCODE(util.ReplaceNull(rs.getString("PURPCODE")));
				obj_002.setFROMINDEX(util.ReplaceNull(rs.getString("FROMINDEX")));
				obj_002.setCOLUMN_NAME(util.ReplaceNull(rs.getString("COLUMN_NAME")));
				obj_002.setCOLUMN_POS(util.ReplaceNull(rs.getString("COLUMN_POS")));
				obj_002.setCOLUMN_ISPRIM(util.ReplaceNull(rs.getString("COLUMN_ISPRIM")));
				obj_002.setCOLUMN_TYPE(util.ReplaceNull(rs.getString("COLUMN_TYPE")));
				obj_002.setCOLUMN_SL(util.ReplaceNull(rs.getString("COLUMN_SL")));
				obj_002.setTOTCOLUMNS(util.ReplaceNull(rs.getString("TOTCOLUMNS")));
				//obj_002.setTABLENAME(util.ReplaceNull(rs.getString("TABLENAME")));
			}
			catch(Exception ex) { logger.debug("exception in FileIT002_Mapper "+ex.getLocalizedMessage()); }
			
			return obj_002;
		}
	}
	
	private class FileIT005_Mapper implements RowMapper<FILEIT005> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public FILEIT005 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			FILEIT005 obj_002 = new FILEIT005();  
	
			obj_002.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			obj_002.setCHCODE(util.ReplaceNull(rs.getString("CHCODE")));
			obj_002.setFILEFORMAT(util.ReplaceNull(rs.getString("FILEFORMAT")));
			obj_002.setPURPCODE(util.ReplaceNull(rs.getString("PURPCODE")));
			obj_002.setDUPCHECK(util.ReplaceNull(rs.getString("DUPCHECK")));
			obj_002.setQUERY1(util.ReplaceNull(rs.getString("QUERY1")));
			obj_002.setQUERY2(util.ReplaceNull(rs.getString("QUERY2")));
			obj_002.setQUERY3(util.ReplaceNull(rs.getString("QUERY3")));
			
			return obj_002;
		}
	}
	
	private class FileIT003_Mapper implements RowMapper<FILEIT003> 
	{
		Common_Utils util = new Common_Utils(); 
		
		public FILEIT003 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			FILEIT003 obj_003 = new FILEIT003();  
	
			obj_003.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			obj_003.setCHCODE(util.ReplaceNull(rs.getString("CHCODE")));
			obj_003.setINDATE(util.ReplaceNull(rs.getString("INDATE")));
			obj_003.setINTIME(util.ReplaceNull(rs.getString("INTIME")));
			obj_003.setPURPCODE(util.ReplaceNull(rs.getString("PURPCODE")));
			obj_003.setCOLUMN1(util.ReplaceNull(rs.getString("COLUMN1")));
			obj_003.setCOLUMN2(util.ReplaceNull(rs.getString("COLUMN2")));
			obj_003.setCOLUMN3(util.ReplaceNull(rs.getString("COLUMN3")));
			obj_003.setCOLUMN4(util.ReplaceNull(rs.getString("COLUMN4")));
			obj_003.setCOLUMN5(util.ReplaceNull(rs.getString("COLUMN5")));
			obj_003.setCOLUMN6(util.ReplaceNull(rs.getString("COLUMN6")));
			obj_003.setCOLUMN7(util.ReplaceNull(rs.getString("COLUMN7")));
			obj_003.setCOLUMN8(util.ReplaceNull(rs.getString("COLUMN8")));
			obj_003.setCOLUMN9(util.ReplaceNull(rs.getString("COLUMN9")));
			obj_003.setCOLUMN10(util.ReplaceNull(rs.getString("COLUMN10")));
					
			
			return obj_003;
		}
	}
}
