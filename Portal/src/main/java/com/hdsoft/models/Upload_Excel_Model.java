package com.hdsoft.models;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.RTS003;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class Upload_Excel_Model {
	
protected JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(Configuration_Modal1.class);
	
	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Webservice_Modal Ws;
	
	@Autowired
	public Webservice_call_Modal Wsc;
	
	@Autowired
	public RTSIS_API_Modal RTSIS;
	 
	

	
	public JsonObject Project_Excel_Upload(RTS003 info,HttpSession session)
	{	
		 JsonObject details = new JsonObject();
		
		 Common_Utils utils = new Common_Utils();
		 
		 JsonArray ExcelNames = new JsonArray();
		 
		 JsonArray KeyNames = new JsonArray();
		 
		 JsonArray Values = new JsonArray();
		 
		 JsonArray validationArray = new JsonArray();
		 
		 System.out.println("Service_code"+info.getServicecd());
		 
		
		 
		 String fileName="";
		 String Sequence="";
		 String Serial="";
		 int  err_count=0;
		 boolean flag=false;
		 
		 try
		 {
			 
			 MultipartFile[] attachments = info.getAttachments();

	           
	            for (MultipartFile file : attachments) {
	                if (!file.isEmpty()) {
	                    fileName = file.getOriginalFilename();
	                    System.out.println("File Name: " + fileName);
			
	                }
	                String[] parts = fileName.split("_");

	                
	                String lastPart = parts[parts.length - 1];

	                int dotIndex = lastPart.indexOf(".");
	                
	                if (dotIndex != -1) {
	                	
	                	Sequence= lastPart.substring(0, dotIndex);
	                } else {
	                    
	                	Sequence=lastPart;
	                }
	                int sequenceNumber = Integer.parseInt(Sequence);
	             System.out.println("Sequence"+sequenceNumber);
	            
	             try (InputStream uploadedFile = new BufferedInputStream(file.getInputStream())) {
	                    
	                    XSSFWorkbook workbook = new XSSFWorkbook(uploadedFile);

	                    
	                    XSSFSheet sheet = workbook.getSheetAt(0);
	                    
	                   
	                    int serialColumnIndex = findColumnIndex(sheet, "serial");

	                    if (serialColumnIndex != -1) {
	                        
	                        System.out.println("Column Name: serial");

	                        
	                        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	                           
	                        	Row row = sheet.getRow(rowIndex);
	                            
	                        	if (row != null) {
	                            
	                        		Cell cell = row.getCell(serialColumnIndex);
	                                
	                        		if (cell != null) {
	                                   
	                        			Serial = cell.getStringCellValue(); 
	                                    
	                        			System.out.println("Row " + rowIndex + ", Value: " + Serial);
	                                }
	                            }
	                        }
	                       
	                    } 
	                   
	                    String webservice001 = "select payload from webservice001 where servicecd=?";
	                    
	                    String payload=Jdbctemplate.queryForObject(webservice001,new Object[] {info.getServicecd()},String.class);
	                  
	                    JsonObject json=utils.StringToJsonObject(payload);
	                  
	                    JsonArray keys=utils.get_keys_from_Json(json);
	             
	                    String Parent_ = "";
	                    
	                    for(int i=0;i<keys.size(); i++)
	       			    {
	                    	String Column_Name = keys.get(i).getAsString();
	       				    
	                    	if(!Column_Name.equalsIgnoreCase("signature"))
	       				    {
	                    		Parent_ = Column_Name;
	                    		
	                    		break;
	       				    }   
	       			   }
	                    
	                    final String Parent = Parent_ ;
	                    
	                    System.out.println("Module_Name"+Parent);
	                    
	                    JsonObject finalJs =   json.get(Parent).getAsJsonArray().get(0).getAsJsonObject();
	                    
	                    JsonArray Final=utils.get_keys_from_Json(finalJs);
	                    
	                    JsonArray values = utils.get_key_vals(finalJs);
	                    
	                    Values.add("Serial");
	                    
	                    for(int i=0;i<values.size(); i++)
	       			    {
	                    	
	                    	
	                    	JsonObject js = values.get(i).getAsJsonObject();
	       				 
	       				    String value_Name = js.get("Value").getAsString();
	       				    
	       				    Values.add(value_Name);
	       			   }
	                    
	                    KeyNames.add("Serial");
	                    
	                    for(int i=0;i<Final.size(); i++)
	       			    {
	                    	String Final_Name = Final.get(i).getAsString();
	       				    
	                    	KeyNames.add(Final_Name);
	                    	
	       			   }
	                   
	                    String sql="select count(*) from rts003 where id=? and no_of_records= ? and submodule=?";
	                    
	                    int count = Jdbctemplate.queryForObject(sql, new Object[] {sequenceNumber,Serial,info.getSubmodule()}, Integer.class);
	                   
	                    System.out.println("KeyNames"+KeyNames);
                        
                        System.out.println("Values"+Values);
                        System.out.println("count"+count);
	                  
                        if(count !=0) {
	                    	 Row headerRow = sheet.getRow(0);

	 	                    if (headerRow != null) {
	 	                       
	 	                        for (int col = 0; col < headerRow.getLastCellNum(); col++) {
	 	                            Cell cell = headerRow.getCell(col);
	 	                            if (cell != null) {
	 	                                String columnName = cell.getStringCellValue();
	 	                               
	 	                               ExcelNames.add(columnName);
	 	                               
	 	                            } 
	 	                        }
	 	                    } 
	 	                    
	 	                   boolean boolval = KeyNames.equals(ExcelNames);
	 	                   
	 	                   System.out.println("boolval"+boolval);
	 	                   
	 	                  System.out.println("No of recors"+Values.size());
	 	                   if(boolval == true) {
	 	                	   
	 	                	   for(int i=0;i<Serial.length();i++) {
	 	                		   
	 	                		   for(int j=0;j<Values.size();j++) {
	 	                			  
	 	                			   if(Values.get(j).getAsString().equalsIgnoreCase("date")){
	 	                				   
	 	                				 
	 	                			    
	 	                				   String Column_Name =  ExcelNames.get(j).getAsString();
	 	                				
	 	                				  int dateColumnIndex = findColumnIndex(sheet, Column_Name);
	 	                				 
	 	         	                      if (serialColumnIndex != -1) {
	 	         	                        
	 	         	                        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	 	         	                           
	 	         	                        	Row row = sheet.getRow(rowIndex);
	 	         	                            
	 	         	                        	if (row != null) {
	 	         	                            
	 	         	                        		Cell cell = row.getCell(dateColumnIndex);
	 	         	                                
	 	         	                        		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	 	         	                                   
	 	         	                        			 LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	 	         	                                   
	 	         	                                   
	 	         	                                   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
	 	         	                                   String formattedDate = date.format(formatter);
	 	         	                                   boolean numeric=isNumeric(formattedDate);
	 	         	                                   System.out.println("numeric"+numeric);
	 	         	                                   if(formattedDate.length()==8 && numeric==true  ) {
	 	         	                                	 System.out.println("Row " + rowIndex + ", DateValue: " + formattedDate+">>>>>>>>>Final Ans");
	 	         	                                	   
	 	         	                                   }
														
	 	         	                                }
	 	         	                        		else 
	 	         	                                   {
	 	         	                                	 flag=true;
	 	         	                                	 err_count++;
	 	         	                                	 JsonObject error = new JsonObject();
	 	         	                                	 error.addProperty("Row", rowIndex);
	 	         	                                	 error.addProperty("Column", ExcelNames.get(j).getAsString());
	 	         	                                	 error.addProperty("Reason", "Not Numeric");
	 	         	                                	 validationArray.add(error);
	 	         	                                 }
	 	         	                            }
	 	         	                        }
	 	         	                      System.out.println("flag"+flag);  
	 	         	                     
	 	  	 	                	   
	 	         	                    
	 	         	                    }
	 	                				
	 	                			   }
	 	                			   
	 	                			   else if(Values.get(j).getAsString().equalsIgnoreCase("number")) {
	 	                				   
	 	                				
		 	                				  
		 	                				 String Column_Name =  ExcelNames.get(j).getAsString();
		 	                				 int NumericColumnIndex = findColumnIndex(sheet, Column_Name);

		 	                				 System.out.println("Numeric"+NumericColumnIndex);
		 	                				 
		 	        	                    if (NumericColumnIndex != -1) {
		 	        	                        
		 	        	                        System.out.println("Column Name"+ExcelNames.get(j));

		 	        	                       
		 	        	                        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
		 	        	                           
		 	        	                        	Row row = sheet.getRow(rowIndex);
		 	        	                            
		 	        	                        	if (row != null) {
		 	        	                            
		 	        	                        		Cell cell = row.getCell(NumericColumnIndex);
		 	        	                                
		 	        	                        		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
		 	        	                        		    double num = cell.getNumericCellValue();
		 	        	                        		    boolean number = isNumeric(num);
		 	        	                        		    if (number) {
		 	        	                        		        flag = false;
		 	        	                        		        
		 	        	                        		        System.out.println("Row " + rowIndex + ", Value: " + num);
		 	        	                        		    } 
		 	        	                        		    
		 	        	                        		} else {
		 	        	                        		    
		 	        	                        		    System.out.println("Row" +rowIndex);
		 	        	                        		    flag = true;
		 	        	                        		    err_count++;
		 	        	                        		   JsonObject error = new JsonObject();
		 	        	                        		    error.addProperty("Row", rowIndex);
		 	         	                                	error.addProperty("Column", ExcelNames.get(j).getAsString());
		 	         	                                	error.addProperty("Reason", "Should be Numeric");
		 	         	                                	validationArray.add(error);
		 	        	                        		    
		 	        	                        		}

		 	        	                            }
		 	        	                        }
		 	        	                       
		 	        	                    } 
		 	                				  
		 	                				  
		 	                			  }   
		 	                			  

	 	                			   
	 	                		   }
	 	                		  
		 	                	   
	 	                	   }
	 	                	  
	 	                		 if(err_count != 0)
	 	                		 {
	 	                			System.out.println("Validation_Array"+validationArray.toString()); 
	 	                			 
	 	                			String jsonarray = validationArray.toString();
	 	                		
			                    	 details.addProperty("Info", jsonarray);
			                    	 details.addProperty("result", "failed");
			                    	 details.addProperty("stscode", "HP001");
			                    	 details.addProperty("message", "Failed");
	 	                		 }
	 	                		if(err_count !=0)
	 	                			break;
	 	                         
	 	                		
	 	                		
	 	                		
	 	                		if(err_count ==0) {
	 	                			
	 	                			JsonObject report_serial=Generate_Report_Serial();
	 	                			String Report_Serial=report_serial.get("Serial").getAsString();
	 	                			System.out.println("CALL"+Report_Serial);
	 	                		
	 	                			
	 	                			 JsonObject ExcelData=readExcelData(sheet);
	 	                			 System.out.println("ExcelData Data"+ExcelData);
	 	                			int numberOfColumns = headerRow.getLastCellNum();
	 	                			System.out.println("header"+numberOfColumns);
	 	                			 try {
	 	                				StringBuilder insertHeadersSQL = new StringBuilder("INSERT INTO report002 (");

	 	                			
	 	                			insertHeadersSQL.append("suborgcode, serial,column1,column2,column3");

	 	                			for (int i = 4; i < numberOfColumns+4; i++) {
	 	                			    insertHeadersSQL.append(", column").append(i);
	 	                			  
	 	                			}
	 	                			insertHeadersSQL.append(") VALUES (?, ?, ?, ?, ?");

	 	                			for (int i = 4; i < numberOfColumns+4; i++) {
	 	                			    insertHeadersSQL.append(", ?");
	 	                			   
	 	                			}
	 	                			insertHeadersSQL.append(")");

	 	                			System.out.println("insertHeadersSQL"+insertHeadersSQL);
	 	                			
	 	                			Jdbctemplate.update(connection -> {
	 	                			    PreparedStatement insertHeadersStatement = connection.prepareStatement(insertHeadersSQL.toString());
	 	                			    insertHeadersStatement.setString(1,sys.getSuborgcode());
	 	                			    insertHeadersStatement.setString(2, Report_Serial);
	 	                			    insertHeadersStatement.setString(3, "C");
	 	                			    insertHeadersStatement.setString(4, info.getServicecd());
	 	                			    insertHeadersStatement.setString(5, Parent);

	 	                			    System.out.println("count1");
	 	                			    for (int i = 0; i < numberOfColumns; i++) {
	 	                			        Cell cell = headerRow.getCell(i);
	 	                			        if (cell != null) {
	 	                			            insertHeadersStatement.setString(i + 6, cell.getStringCellValue());
	 	                			           
	 	                			        } else {
	 	                			            insertHeadersStatement.setString(i + 6, ""); 
	 	                			        }
	 	                			    }
 
	 	                			   System.out.println("Headers inserted successfully into report002 table!");
	 	                			    insertHeadersStatement.executeBatch();
	 	                			   
	 	                			   return insertHeadersStatement;
	 	                          });
	                		        
	 	                			StringBuilder sql1 = new StringBuilder("INSERT INTO report002 (");

	 	                			
	 	                			sql1.append("suborgcode, serial,column1,column2,column3");

	 	                			for (int i = 4; i < numberOfColumns+4; i++) {
	 	                				sql1.append(", column").append(i);
	 	                			  
	 	                			}
	 	                			sql1.append(") VALUES (?, ?, ?, ?, ?");

	 	                			for (int i = 4; i < numberOfColumns+4; i++) {
	 	                				sql1.append(", ?");
	 	                			   
	 	                			}
	 	                			sql1.append(")");

	 	                			    System.out.println("SQL1: " + sql1);

	 	                			    
	 	                			       
	 	                			      for (int rowIndex_ = 1; rowIndex_ <= sheet.getLastRowNum(); rowIndex_++) 
	 	                			      {
	 	                			    	  final int rowIndex = rowIndex_;
	 	                			    	  
	 	                			    	 Jdbctemplate.update(connection -> {
	 	 	                			        PreparedStatement statement = connection.prepareStatement(sql1.toString());
	 	 	                			       int rowCount = 0;
	 	 	                			        statement.setString(1, sys.getSuborgcode());
	 	 	                			        statement.setString(2, Report_Serial);
	 	 	                			        statement.setString(3, "D");
	 	 	                			        statement.setString(4, info.getServicecd());
	 	 	                			        statement.setString(5, Parent);
	 	 	                			        
	 	                			    	    Row row = sheet.getRow(rowIndex);
	 	                			    	   rowCount++;
	 	                			    	   System.out.println("rowIndex"+rowIndex);
	 	                			    	    if (row != null && row.getPhysicalNumberOfCells() > 0) {
	 	                			    	        for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
	 	                			    	                Cell cell = row.getCell(colIndex);
	 	                			    	               System.out.println("colIndex"+colIndex);
	 	                			                    if (cell != null) {
	 	                			                        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
	 	                			                            statement.setString(colIndex + 6, cell.getStringCellValue());
	 	                			                        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	 	                			                            if (DateUtil.isCellDateFormatted(cell)) {
	 	                			                            	LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	 	    	 	         	                                   
	 	    	 	         	                                   
	 	    	 	         	                                   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
	 	    	 	         	                                   String formattedDate = date.format(formatter);
	 	    	 	         	                                   statement.setString(colIndex + 6, formattedDate);
	 	    	 	         	                                  } else {
	 	    	 	         	                                	
	 	    	 	         	                                	DataFormatter formatter = new DataFormatter();
	 	    	 	         	                                	String cellValue = formatter.formatCellValue(cell);
	 	    	 	         	                                	statement.setString(colIndex + 6, cellValue);

	 	    	 	         	                                		 	                			                            }
	 	                			                        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
	 	                			                            statement.setBoolean(colIndex + 6, cell.getBooleanCellValue());
	 	                			                        } else {
	 	                			                        	statement.setString(colIndex + 6, "");
	 	                			                            System.out.println("Err");
	 	                			                        }
	 	                			                    } else {
	 	                			                    	statement.setString(colIndex + 6, "");
	 	                			                        System.out.println("Err1");
	 	                			                    }
	 	                			                      }
	 	                			    	    
	 	                			    	        
	 	                			              
	 	                			    	       
	 	                			                System.out.println(statement+"statement");
	 	                			            }
	 	                			    	
	 	                			    	 
	 	                			    	  
	 	                			    	    
	 	                			    	       return statement;
	 	                			    	   });
	 	                			       }
	 	                			    
											
	 	                			      
	 	                			   String header = "Insert into report002 (suborgcode, serial,column1,column2,column3) values (?,?,?,?,?)";
	 	                			   Jdbctemplate.update(header, new Object[] { sys.getSuborgcode(), Report_Serial, "H",info.getServicecd(),Parent});   
	 	  		
	 	                			  String CurrDateTime=utils.getCurrentDateTime();
	 	                			  String created_by=utils.ReplaceNull(session.getAttribute("sesUserId"));
										
									String upd ="update rts003 set is_uploaded=?,uploaded_by=?,uploaded_on=?,report_serial=?,source_type=? where id=?";
								    Jdbctemplate.update(upd, new Object[] {1,created_by,CurrDateTime,Report_Serial,"excel",sequenceNumber});
										 	
	 	                			  

	 	                			} catch (Exception e) {
	 	                			    e.printStackTrace();
	 	                			    System.out.println("Exception: " + e.getMessage());
	 	                			}

	 	                		        
	 	                			
	 	                	
	 	                			 
	 	                			 
	 	                		}
	 	                		
	 	                		 details.addProperty("result", "success");
	 	                		 details.addProperty("stscode", "HP002");
		                    	 details.addProperty("message", "Success");
	 	                	  
		 	                   
	 	                	   
	 	                   }
	 	                   
	 	                   else {
	 	                	  
	 	                	   
	 	                	     details.addProperty("result", "failed");
	 	                	     details.addProperty("stscode", "HP003");
		                    	 details.addProperty("message", "Column Names is wrong");
		 	                   
	 	                   } 
	 	                  
	                    }
	                    else {
	                    	
	                    	 details.addProperty("result", "failed");
	                    	 details.addProperty("stscode", "HP004");
	                    	 details.addProperty("message", "Invalid excel file");
	                    	
	                    }
	       		
	             }
                           
                          

	            }
	           
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result","failed ");
			 details.addProperty("stscode", "HP005");
			 details.addProperty("Message", e.getMessage()); 
			 System.out.println( e.getMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject Generate_Report_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{  
			
			String sql = "SELECT RTSIS_REPORT_SERIAL.NEXTVAL FROM DUAL";
			Long serialValue = Jdbctemplate.queryForObject(sql, Long.class);

			 
			 System.out.println("Report_Serial"+serialValue);
			 
			 details.addProperty("Serial", serialValue);
			
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
	public JsonObject readExcelData(Sheet sheet) {
	    JsonObject excelData = new JsonObject();

	    for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	        Row row = sheet.getRow(rowIndex);
	        if (row != null) {
	            JsonObject rowData = new JsonObject();

	            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
	                Cell cell = row.getCell(colIndex);
	                String columnName = "Column" + colIndex; 

	                if (cell != null) {
	                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
	                        rowData.addProperty(columnName, cell.getStringCellValue());
	                    } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	                        if (DateUtil.isCellDateFormatted(cell)) {
	                            LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
	                            String formattedDate = date.format(formatter);
	                            rowData.addProperty(columnName, formattedDate);
	                        } else {
	                            double numericValue = cell.getNumericCellValue();
	                            rowData.addProperty(columnName, numericValue);
	                        }
	                    } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
	                        rowData.addProperty(columnName, cell.getBooleanCellValue());
	                    } else {
	                        
	                    }
	                } else {
	                    
	                }
	            }
	            excelData.add("Row" + rowIndex, rowData);
	        }
	    }

	    return excelData;
	}
	
	
	public void insertExcelDataIntoDatabase(Sheet sheet) {
	    try {
	        
	        Row headerRow = sheet.getRow(0);

	        
	        StringBuilder sql = new StringBuilder("INSERT INTO report002 (");
	        for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
	            if (colIndex > 0) {
	                sql.append(", ");
	            }
	            sql.append(headerRow.getCell(colIndex).getStringCellValue());
	        }
	        sql.append(") VALUES (");
	        for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
	            if (colIndex > 0) {
	                sql.append(", ");
	            }
	            sql.append("?");
	        }
	        sql.append(")");

	        Jdbctemplate.update(connection -> {
	            PreparedStatement statement = connection.prepareStatement(sql.toString());
	            
	            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	                Row row = sheet.getRow(rowIndex);
	                if (row != null) {
	                    for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
	                        Cell cell = row.getCell(colIndex);
	                        if (cell != null) {
	                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
	                                statement.setString(colIndex + 1, cell.getStringCellValue());
	                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	                                if (DateUtil.isCellDateFormatted(cell)) {
	                                   
	                                } else {
	                                    statement.setDouble(colIndex + 1, cell.getNumericCellValue());
	                                }
	                            } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
	                                statement.setBoolean(colIndex + 1, cell.getBooleanCellValue());
	                            } else {
	                                
	                            }
	                        } else {
	                           
	                        }
	                    }
	                    statement.addBatch(); 
	                }
	            }
	            statement.executeBatch(); 
	            return statement;
	        });

	        System.out.println("Data inserted successfully into report002 table!");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public static boolean isNumeric(String str) { 
		  try {  
		    Double.parseDouble(str);  
		    return true;
		  } catch(NumberFormatException e){  
		    return false;  
		  }  
		}
	public static boolean isNumeric(int num) {
	    try {
	        Double.parseDouble(String.valueOf(num));
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	public static boolean isNumeric(double num) {
	    return Double.isFinite(num);
	}

	private static int findColumnIndex(Sheet sheet, String columnName) {
        
		System.out.println("columnName"+columnName);
		
		Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                Cell cell = headerRow.getCell(col);
                if (cell != null && columnName.equalsIgnoreCase(cell.getStringCellValue())) {
                    return col;
                }
            }
        }
        return -1; 
    }
	

}
