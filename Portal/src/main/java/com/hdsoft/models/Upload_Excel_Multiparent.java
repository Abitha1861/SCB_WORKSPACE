package com.hdsoft.models;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hdsoft.Repositories.RTS003;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class Upload_Excel_Multiparent {
	
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	
	private static final Logger logger = LogManager.getLogger(Upload_Excel_Multiparent.class);
	
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
		 
		 JsonArray values = new JsonArray();
		 
														  
   
  
   
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
	             
	            
	             try (InputStream uploadedFile = new BufferedInputStream(file.getInputStream())) {
	                    
	                    XSSFWorkbook workbook = new XSSFWorkbook(uploadedFile);

	                    
	                    XSSFSheet sheet = workbook.getSheetAt(0);
	                    
	                   
	                    int serialColumnIndex = findColumnIndex(sheet, "serial");

	                    if (serialColumnIndex != -1) {
	                        
	                        

	                        
	                        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	                           
	                        	Row row = sheet.getRow(rowIndex);
	                            
	                        	if (row != null) {
	                            
	                        		Cell cell = row.getCell(serialColumnIndex);
	                                
	                        		if (cell != null) {
	                                   
	                        			Serial = cell.getStringCellValue(); 
	                                    
	                        			
	                                }
	                            }
	                        }
	                       
	                    } 
	                   
	                    JsonObject data = new JsonObject();
	       			 
	       			    JsonObject data1 = new JsonObject();
	       			 
	       		        String webservice001 = "select payload from webservice001 where servicecd=?";
	                    
	                    String payload=Jdbctemplate.queryForObject(webservice001,new Object[] {info.getServicecd()},String.class);
	                    
	                    String sign = "select signpayload from webservice001 where servicecd=?";
	                    
	                    String signpayload=Jdbctemplate.queryForObject(sign,new Object[] {info.getServicecd()},String.class);
	                    
	                    JsonObject primary=utils.StringToJsonObject(signpayload);
	                    
					 
	                    JsonArray primarykeys = primary.getAsJsonArray("primary");

					
	                    
	                    JsonObject json=utils.StringToJsonObject(payload);
	                    
	                    JsonArray keys=utils.get_keys_from_Json(json);
	                    
	                    List<String> Avl_elements = utils.get_keys_as_list(json);
	                    
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
	                 	                    
													  
					 
	       			 for(String Avl_element : Avl_elements) 
	       			 {
	       				 if(!Avl_element.equalsIgnoreCase("signature"))
	       				 {
	       					 data = json.get(Avl_element).getAsJsonArray().get(0).getAsJsonObject();
	       					
	       					 break;
	       				 }
	       			 }
	       			 
			
					 
			
	                 JsonObject ArrayFamily = new JsonObject();
	                    
	                 JsonObject Head_details = new JsonObject();
	       			 
	       			 JsonArray Inside_elements = utils.find_key_and_types(data);
	       			 
		   
			
	       			 JsonArray Own_Members = new JsonArray();  JsonArray Array_Members = new JsonArray();
	       			 
	       			 for(int j=0; j<Inside_elements.size(); j++)
	       			 {
	       				 JsonObject k = Inside_elements.get(j).getAsJsonObject();
	       				 
	       				 String Key = k.get("Key").getAsString();
	       				 String Type = k.get("Type").getAsString();
	       			
	       				 if(Type.equalsIgnoreCase("json_array") || Type.equalsIgnoreCase("json_object")) 
	       				 {
	       					 Array_Members.add(Key);
	       					 
	       					 if(data.has(Key) && Type.equalsIgnoreCase("json_array"))
	       					 {
	       						 JsonObject arr = data.get(Key).getAsJsonArray().get(0).getAsJsonObject();
	       						 
	       						 ArrayFamily.add(Key, arr);
	       						 
	       					 }
	       				 }
	       				 else
	       				 {
	       					
	       					 
	       					 Own_Members.add(Key);
	       					
	       					 Head_details.addProperty(Key, Type);
	       					 
	       				 }
	       			 }
	       			 
	       			
	       			 JsonArray arr= new JsonArray();
	       			 
	       			 arr.add(Head_details);
	       			
	       			 data1.add(keys.get(0).getAsString(), arr);
	       			 
			
			
			
	       			 JsonArray Final = new JsonArray();
	       			 
								  
			
	                 JsonObject primaries_Object = null ;
	       			 
	                 JsonArray primaries_Array = new JsonArray();
	                 
																		
					 
	                 for(int i=0;i<primarykeys.size();i++) {
	       				 
	       				 for(int j=0;j<primarykeys.size();j++) {
	       					 
	       					 
	       					 JsonObject k = Inside_elements.get(i).getAsJsonObject();
	       					 
	       					 String Key = k.get("Key").getAsString();
	       					 String Type = k.get("Type").getAsString();
	       					 String Array_Member1 = primarykeys.get(j).getAsString();
	       					
	       					 
	       					 if(Key.contains(Array_Member1)) {
	       						 primaries_Object = new JsonObject();
	       						 primaries_Object.addProperty("Key", Key);
	       						 primaries_Object.addProperty("Value", Type);
	       						 primaries_Array.add(Key);
	       						
	       						 values = utils.get_key_vals(primaries_Object);
	       						 
	       					 }
	       					 else {
	       							System.out.println("Not a same value");
	       						} 
	       					 
	       				 }
	       				 
	       				 
	       				
	       			 }
	       			  
	       			 
	       			JsonArray jr = new JsonArray();
	       			 
		   
											
			
		   
		   
	       			JsonObject js = new JsonObject();
	       			
	       			JsonObject jsonObj = new JsonObject();
	       			
															
		   
														 
		   
		   
	       			JsonArray arraysss = new JsonArray(); 

		  

		   
		   
		  
		   
	       			for(int i = 0; i < Array_Members.size(); i++) {
	       			    String Array_Member = Array_Members.get(i).getAsString();
	       			    JsonObject js1 = ArrayFamily.get(Array_Member).getAsJsonObject();
	       			    jsonObj.add(Array_Member, js1); 
	       			    
	       			    JsonArray jr1 = new JsonArray();
	       			    jr1.add("Serial"); 
	       			    jr1.addAll(primaries_Array);
	       			    jr1.addAll(utils.get_keys_from_Json(js1)); 
	       			    
	       			    arraysss.add(jr1); 
	       			}

		   
		   
														 

		   
			
	       			 Final.addAll(arraysss);
	       			 
	       			 Values.add("Serial");
	                    
	                 for(int i=0;i<values.size(); i++)
	       			 {
	                    	
	                    	
	                    	js = values.get(i).getAsJsonObject();
	       				 
	       				    String value_Name = js.get("Value").getAsString();
	       				    
	       				    Values.add(value_Name);
	       			 }
	               
					

	                JsonArray Key_Pair1 = new JsonArray();
	       				
			  
			
		       		JsonArray Keyvalues = new JsonArray();
	       			 
	       			List<String> Avl_elements2 = utils.get_keys_as_list(data1);
	       			 
	       			for(String Head_Name : Avl_elements2)
	       			 {
	       		    	 if(data1.has(Head_Name) && data1.get(Head_Name).isJsonArray() && data1.get(Head_Name).getAsJsonArray().size()!=0)
	       				 {	
	       					 JsonObject elements = data1.get(Head_Name).getAsJsonArray().get(0).getAsJsonObject();
	       					 
	       					 Key_Pair1.add("Serial");
	       					 
	       					 Key_Pair1.addAll( utils.get_keys_from_Json(elements));
	       					 
	       				
	       					 
	       					Keyvalues = utils.get_key_vals(elements);
	       					
	       				 }
	       			 }
	       			 
	       			
	       		    for(int i=0;i<Keyvalues.size(); i++)
    			    {
                 	
                 	
                 	 js = Keyvalues.get(i).getAsJsonObject();
    				 
    				    String value_Name = js.get("Value").getAsString();
    				    
    				    Values.add(value_Name);
    			   }
                 
				 
				 
	       		   KeyNames.add(Key_Pair1);
	       		   
	       		   KeyNames.addAll(Final);
		  
	       		   
														
												
												   
			
	       		   String sql="select count(*) from rts003 where id=? and no_of_records= ? and submodule=?";
	                    
	               int count = Jdbctemplate.queryForObject(sql, new Object[] {sequenceNumber,Serial,info.getSubmodule()}, Integer.class);
	                    
				   
					
	               if(count !=0) {
	            	   
					  
	                    	JsonArray allSheetColumns = new JsonArray();
	                    	
	                    	JsonArray sheetNamesOfExcel = new JsonArray();

	                    	for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
	                    		
	                    	     Sheet sheets = workbook.getSheetAt(sheetIndex);
	                    	    
	                    	     String sheets_name=sheets.getSheetName();

	                    	     sheetNamesOfExcel.add(sheets.getSheetName());
	                    	    
	                    	     JsonArray sheetColumnsnew = new JsonArray();

	                    	     Row headerRow = sheet.getRow(0);
	                    	    
	                    	     Row headerRows = sheets.getRow(0);
	                    	     
	                    	     if (headerRows != null) {
	                    	        for (int col = 0; col < headerRows.getLastCellNum(); col++) {
	                    	            Cell cell = headerRows.getCell(col);
	                    	            if (cell != null) {
	                    	                String columnName = cell.getStringCellValue();
	                    	                sheetColumnsnew.add(columnName);
	                    	               
	                    	            }
	                    	        }
	                    	    }
	                    	    

	                    	     allSheetColumns.add(sheetColumnsnew);
	                    	         
	 	                         boolean boolval = KeyNames.equals(allSheetColumns);
	 	                         
					 
					
				   
	 	                         Set<String> uniqueElements = new LinkedHashSet<>();

	 	                 
	 	                         for (JsonElement element : allSheetColumns) {
	 	                        	 
	 	                              JsonArray nestedArray = element.getAsJsonArray();
	 	                              
	 	                         for (JsonElement nestedElement : nestedArray) {
	 	                        	 
	 	                              uniqueElements.add(nestedElement.getAsString());
	 	                             }
	 	                         }

					
	 	                      JsonArray flatArray = new JsonArray();
	 	                      
	 	                      for (String uniqueElement : uniqueElements) {
	 	                    	  
	 	                          flatArray.add(uniqueElement);
	 	                          
	 	                      }

	 	                  System.out.println("boolval"+boolval);
				   
	 	                  
	 	                   if(boolval == true) {
	 	                
	 	                	  							   
					 
	 	                       for(int i=0;i<Serial.length();i++) {
	 	                		   
	 	                		   for(int j=0;j<Values.size();j++) {
	 	                			   
	 	                			  System.out.println("Values"+Values.size());	
	 	                			  
	 	                			 System.out.println("Values_Type"+Values.get(j).getAsString());	
	 	                			  
	 	                			   if(Values.get(j).getAsString().equalsIgnoreCase("date"))
	 	                			   
	 	                			   {
	 	                				   String Column_Name =  flatArray.get(j).getAsString();
	 	                				
	 	                				   int dateColumnIndex = findColumnIndex(sheet, Column_Name);
	 	         	                       
						
	 	                				   if (serialColumnIndex != -1) {
	 	         	                    	  
	 	         	                       for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	 	         	                        	
	 	         	                    	   Row row = sheets.getRow(rowIndex);
	 	         	                        	
																		
									  
	 	         	                           if (row != null) {
	 	         	                        	   
									   
										 
	 	         	                        		Cell cell = row.getCell(dateColumnIndex);

	 	         	                        		
	 	         	                        		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	 	         	                                   
	 	         	                        		   LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	 	         	                        		   
	 	         	                                   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
	 	         	                                   
											   
																													  
	 	         	                                   String formattedDate = date.format(formatter);
	 	         	                                   
	 	         	                                   boolean numeric=isNumeric(formattedDate);
	 	         	                                   
	 	         	                                   if(formattedDate.length()==8 && numeric==true  ) {
	 	         	                                	   
	 	         	                                	 System.out.println("Row " + rowIndex + ", DateValue: " + formattedDate+">>>>>>>>>Final Ans");
	 	         	                                	   
	 	         	                                   }
	 	         	                        			
	 	         	                        			System.out.println("Cell"+cell.getStringCellValue());
														
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
	 	                				   
	 	                				
		 	                				  
		 	                				 String Column_Name =  allSheetColumns.get(j).getAsString();
		 	                				 int NumericColumnIndex = findColumnIndex(sheets, Column_Name);

		 	                				 System.out.println("Numeric"+NumericColumnIndex);
		 	                				 
		 	        	                    if (NumericColumnIndex != -1) {
		 	        	                        
		 	        	                        System.out.println("Column Name"+allSheetColumns.get(j));
		 	        	                        
		 	        	                        for (int rowIndex = 1; rowIndex <= sheets.getLastRowNum(); rowIndex++) {
		 	        	                           
		 	        	                        	Row row = sheets.getRow(rowIndex);
		 	        	                            
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
		 	         	                                	error.addProperty("Column", allSheetColumns.get(j).getAsString());
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
	 	                			
	 	                			JsonObject ExcelData=readExcelData(sheets);
	 	                			 
	 	                			JsonObject ExcelDataa=readExcelData(sheet);
	 	                			
	 	                			
	 	                			
	 	                			int numberOfColumns = headerRow.getLastCellNum();
	 	                			int numberOfColumnss = headerRows.getLastCellNum();
	 	                			
	 	                			
	 	                			int rowCount = 0;
	 	                			
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

	 	                		
	 	                			Jdbctemplate.update(connection -> {
	 	                			    PreparedStatement insertHeadersStatement = connection.prepareStatement(insertHeadersSQL.toString());
	 	                			    insertHeadersStatement.setString(1,sys.getSuborgcode());
	 	                			    insertHeadersStatement.setString(2, Report_Serial);
	 	                			    insertHeadersStatement.setString(3, "C");
	 	                			    insertHeadersStatement.setString(4, info.getServicecd());
	 	                			    insertHeadersStatement.setString(5, Parent);

	 	                			   
	 	                			    for (int i = 0; i < numberOfColumns; i++) {
	 	                			        Cell cell = headerRow.getCell(i);
	 	                			        System.out.println("Headers"+" "+headerRow.getCell(i));
	 	                			        if (cell != null) {
	 	                			            insertHeadersStatement.setString(i + 6, cell.getStringCellValue());
	 	                			          
	 	                			           
	 	                			        } else {
	 	                			            insertHeadersStatement.setString(i + 6, ""); 
	 	                			        }
	 	                			    }
 
	 	                			  
	 	                			    insertHeadersStatement.executeBatch();
	 	                			   
	 	                			   return insertHeadersStatement;
	 	                          });
	                		        
	 	                			StringBuilder sql1 = new StringBuilder("INSERT INTO report002 (suborgcode, serial, column1, column2, column3");

	 	                			for (int i = 4; i < numberOfColumns + 4; i++) {
	 	                			    sql1.append(", column").append(i);
	 	                			}

	 	                			sql1.append(") VALUES (?, ?, ?, ?, ?");

	 	                			for (int i = 4; i < numberOfColumns + 4; i++) {
	 	                			    sql1.append(", ?");
	 	                			}

	 	                			sql1.append(")");

	 	                			
	 	                			

	 	                			for (int rowIndex_ = 1; rowIndex_ <= sheet.getLastRowNum(); rowIndex_++) {
	 	                			    final int rowIndex = rowIndex_;

	 	                			    Jdbctemplate.update(connection -> {
	 	                			        PreparedStatement statement = connection.prepareStatement(sql1.toString());

	 	                			        
	 	                			        statement.setString(1, sys.getSuborgcode());
	 	                			        statement.setString(2, Report_Serial);
	 	                			        statement.setString(3, "D");
	 	                			        statement.setString(4, info.getServicecd());
	 	                			        statement.setString(5, Parent);

	 	                			        Row row = sheet.getRow(rowIndex);
	 	                			       
	 	                			        System.out.println("rowIndex" + rowIndex);

	 	                			        if (row != null && row.getPhysicalNumberOfCells() > 0) {
	 	                			            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
	 	                			                Cell cell = row.getCell(colIndex);

	 	                			                if (cell != null) {
	 	                			                    int parameterIndex = colIndex + 6;

	 	                			                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
	 	                			                        statement.setString(parameterIndex, cell.getStringCellValue());
	 	                			                        
	 	                			                    } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	 	                			                        if (DateUtil.isCellDateFormatted(cell)) {
	 	                			                            LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	 	                			                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
	 	                			                            String formattedDate = date.format(formatter);
	 	                			                            statement.setString(parameterIndex, formattedDate);
	 	                			                            System.out.println("formattedDate: " + formattedDate);
	 	                			                        } else {
	 	                			                            DataFormatter formatter = new DataFormatter();
	 	                			                            String cellValue = formatter.formatCellValue(cell);
	 	                			                            statement.setString(parameterIndex, cellValue);
	 	                			                            System.out.println("cellValue: " + cellValue);
	 	                			                        }
	 	                			                    } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
	 	                			                        statement.setBoolean(parameterIndex, cell.getBooleanCellValue());
	 	                			                        System.out.println("cell.getBooleanCellValue(): " + cell.getBooleanCellValue());
	 	                			                    } else {
	 	                			                    	statement.setString(colIndex + 6, "");
	 	                			                        System.out.println("Unsupported cell type");
	 	                			                    }
	 	                			                }
	 	                			                else {
	 	                			                	 statement.setString(colIndex+6, "");
	 	                			                }
	 	                			            }

	 	                			           
	 	                			        }

	 	                			        return statement;
	 	                			    });
	 	                			}

	 	                			    
	 	                			    System.out.println("Report_Serial"+Report_Serial);
	 	                			    
	 	                			  
																																			 
																																						 
	 	                		      String CurrDateTime=utils.getCurrentDateTime();
	 	                		      
	 	                			  String created_by=utils.ReplaceNull(session.getAttribute("sesUserId"));
										
									  String upd ="update rts003 set is_uploaded=?,uploaded_by=?,uploaded_on=?,report_serial=?,source_type=?,is_pushed=? where id=?";
								   
									  Jdbctemplate.update(upd, new Object[] {1,created_by,CurrDateTime,Report_Serial,"excel",0,sequenceNumber});
										 	
	 	                			  

	 	                			} catch (Exception e) {
	 	                			    e.printStackTrace();
	 	                			   details.addProperty("result","failed ");
	 	                				 details.addProperty("stscode", "HP005");
	 	                				 details.addProperty("Message", e.getMessage()); 
	 	                				 System.out.println( e.getMessage());
	 	                			    System.out.println("Exception: " + e.getMessage());
	 	                			}

	 	                			try {
	 	                				
	 	                				for (int sheetIndex1 = 1; sheetIndex1 < workbook.getNumberOfSheets(); sheetIndex1++) {
	 	   	                    		
	 	   	                    	    Sheet sheets1 = workbook.getSheetAt(sheetIndex1);
	 	   	                    	    
	 	   	                    	    String sheets_name1=sheets1.getSheetName();
	 	   	                    	    
	 	                    	        Row headerRows1 = sheets1.getRow(0);
	 	                				
	 	   	                    	    int numberOfColumnss1 = headerRows1.getLastCellNum();
	 	   	                    	    
		 	                			StringBuilder insertHeadersSQL = new StringBuilder("INSERT INTO report002 (");

			   
		 	                			insertHeadersSQL.append("suborgcode, serial,column1,column2,column3");

		 	                			for (int i = 4; i < numberOfColumnss1+4; i++) {
		 	                			    insertHeadersSQL.append(", column").append(i);
		 	                			  
		 	                			}
		 	                			
		 	                			insertHeadersSQL.append(") VALUES (?, ?, ?, ?, ?");

		 	                			for (int i = 4; i < numberOfColumnss1+4; i++) {
		 	                			    insertHeadersSQL.append(", ?");
		 	                			   
		 	                			}
		 	                			
		 	                			insertHeadersSQL.append(")");

					   
					   
		 	                			Jdbctemplate.update(connection -> {
		 	                			    PreparedStatement insertHeadersStatement = connection.prepareStatement(insertHeadersSQL.toString());
		 	                			    insertHeadersStatement.setString(1,sys.getSuborgcode());
		 	                			    insertHeadersStatement.setString(2, Report_Serial);
		 	                			    insertHeadersStatement.setString(3, "C");
		 	                			    insertHeadersStatement.setString(4, info.getServicecd());
		 	                			    insertHeadersStatement.setString(5, sheets1.getSheetName());

						  
		 	                			    for (int i = 0; i < numberOfColumnss1; i++) {
		 	                			    	
		 	                			        Cell cell = headerRows1.getCell(i);
		 	                			        
		 	                			        System.out.println("Headers"+" "+headerRows1.getCell(i));
		 	                			        
		 	                			        if (cell != null) {
		 	                			        	
		 	                			            insertHeadersStatement.setString(i + 6, cell.getStringCellValue());
		 	                			          
		 	                			        } 
		 	                			        else {
		 	                			        	
		 	                			            insertHeadersStatement.setString(i + 6, ""); 
		 	                			        }
		 	                			    }
	 
						 
		 	                			    insertHeadersStatement.executeBatch();
		 	                			   
		 	                			   return insertHeadersStatement;
		 	                           
		 	                			});
		                		        
		 	                			System.out.println("sheets_name"+sheets_name1);
		 	                			
		 	                			StringBuilder sql1 = new StringBuilder("INSERT INTO report002 (suborgcode, serial, column1, column2, column3");

		 	                			for (int i = 4; i < numberOfColumnss1 + 4; i++) {
		 	                			    sql1.append(", column").append(i);
		 	                			}

		 	                			sql1.append(") VALUES (?, ?, ?, ?, ?");

		 	                			for (int i = 4; i < numberOfColumnss1 + 4; i++) {
		 	                			    sql1.append(", ?");
		 	                			}

		 	                			sql1.append(")");

		 	                			System.out.println("SQL1: " + sql1);

		 	                			for (int rowIndex_ = 1; rowIndex_ <= sheets1.getLastRowNum(); rowIndex_++) {
		 	                			    final int rowIndex = rowIndex_;

		 	                			    Jdbctemplate.update(connection -> {
		 	                			        PreparedStatement statement = connection.prepareStatement(sql1.toString());

		 	                			        
		 	                			        statement.setString(1, sys.getSuborgcode());
		 	                			        statement.setString(2, Report_Serial);
		 	                			        statement.setString(3, "D");
		 	                			        statement.setString(4, info.getServicecd());
		 	                			        statement.setString(5, sheets1.getSheetName());

		 	                			        Row row = sheets1.getRow(rowIndex);
		 	                			       
		 	                			       

		 	                			        if (row != null && row.getPhysicalNumberOfCells() > 0) {
		 	                			            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
		 	                			                Cell cell = row.getCell(colIndex);

		 	                			                if (cell != null) {
		 	                			                    int parameterIndex = colIndex + 6;

		 	                			                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
		 	                			                        statement.setString(parameterIndex, cell.getStringCellValue());
		 	                			                        System.out.println("cell.getStringCellValue(): " + cell.getStringCellValue());
		 	                			                    } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
		 	                			                        if (DateUtil.isCellDateFormatted(cell)) {
		 	                			                            LocalDate date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		 	                			                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		 	                			                            String formattedDate = date.format(formatter);
		 	                			                            statement.setString(parameterIndex, formattedDate);
		 	                			                            System.out.println("formattedDate: " + formattedDate);
		 	                			                        } else {
		 	                			                            DataFormatter formatter = new DataFormatter();
		 	                			                            String cellValue = formatter.formatCellValue(cell);
		 	                			                            statement.setString(parameterIndex, cellValue);
		 	                			                            System.out.println("cellValue: " + cellValue);
		 	                			                        }
		 	                			                    } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
		 	                			                        statement.setBoolean(parameterIndex, cell.getBooleanCellValue());
		 	                			                        System.out.println("cell.getBooleanCellValue(): " + cell.getBooleanCellValue());
		 	                			                    }
		 	                			                    else {
		 	                			                    	statement.setString(colIndex + 6, "");
		 	                			                        System.out.println("Unsupported cell type");
		 	                			                    }
		 	                			                }
		 	                			               else {
		 	                			                	 statement.setString(colIndex+6, "");
		 	                			                }
		 	                			            }

		 	                			          
		 	                			        }

		 	                			        return statement;
		 	                			    });
		 	                			}

		 	                			     
		 	                			  
		 	                			  
	 	                				}
	 	                				
	 	                				
		 	   	                    	    
	 	                				rowCount=rowCount+1;
		 	                			  String header = "Insert into report002 (suborgcode, serial,column1,column2,column3,column4) values (?,?,?,?,?,?)";
		 	                			  Jdbctemplate.update(header, new Object[] { sys.getSuborgcode(), Report_Serial, "H",info.getServicecd(),Parent,rowCount}); 
											 for (int sheetIndex1 = 1; sheetIndex1 < workbook.getNumberOfSheets(); sheetIndex1++) {
													
													Sheet sheets1 = workbook.getSheetAt(sheetIndex1);
													
													String sheets_name1=sheets1.getSheetName();
											  rowCount=rowCount+1;
		 	                			   String headers = "Insert into report002 (suborgcode, serial,column1,column2,column3,column4) values (?,?,?,?,?,?)";
		 	                			   Jdbctemplate.update(headers, new Object[] { sys.getSuborgcode(), Report_Serial, "H",info.getServicecd(),sheets_name1,rowCount});   
		 	                			   String CurrDateTime=utils.getCurrentDateTime();
		 	                			  String created_by=utils.ReplaceNull(session.getAttribute("sesUserId"));
											
										String upd ="update rts003 set is_uploaded=?,uploaded_by=?,uploaded_on=?,report_serial=?,source_type=?,is_pushed=? where id=?";
									    Jdbctemplate.update(upd, new Object[] {1,created_by,CurrDateTime,Report_Serial,"excel",0,sequenceNumber});
											 	
	 	                				
		 	                			}
	 	                			
	 	                			}
	 	                			catch (Exception e) {
		 	                			    e.printStackTrace();
		 	                			   details.addProperty("result","failed ");
		 	                				 details.addProperty("stscode", "HP005");
		 	                				 details.addProperty("Message", e.getMessage()); 
		 	                				 System.out.println( e.getMessage());
		 	                			    System.out.println("Exception: " + e.getMessage());
		 	                			}
   
	 	                			
	 	                	
	 	                			 
	 	                			 
	 	                		}
	 	                		
	 	                		 details.addProperty("result", "success");
	 	                		 details.addProperty("stscode", "HP002");
		                    	 details.addProperty("message", "success");
	 	                	  
		 	                   
	 	            
	 	                   }
	 	                   
	 	                   else {
	 	                	  
	 	                	   
	 	                	     details.addProperty("result", "failed");
	 	                	     details.addProperty("stscode", "HP003");
		                    	 details.addProperty("message", "Column Names is wrong");
		 	                   
	 	                   } 
	 	                  
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
	
	public void insertDataIntoReport002(PreparedStatement insertHeadersStatement, Row headerRow, int numberOfColumns,String servicecd,String submodule,String report) throws SQLException {
	    
	    insertHeadersStatement.setString(1, sys.getSuborgcode());
	    insertHeadersStatement.setString(2, report);
	    insertHeadersStatement.setString(3, "C");
	    insertHeadersStatement.setString(4, servicecd);
	    insertHeadersStatement.setString(5, submodule);

	    System.out.println("count1");
	    for (int i = 0; i < numberOfColumns; i++) {
	        Cell cell = headerRow.getCell(i);
	        if (cell != null) {
	            insertHeadersStatement.setString(i + 6, cell.getStringCellValue());
	           
	        } else {
	            insertHeadersStatement.setString(i + 6, ""); 
	        }
	    }

	    System.out.println("Headers inserted successfully into report002 table!"+insertHeadersStatement);
	    insertHeadersStatement.executeBatch();
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


