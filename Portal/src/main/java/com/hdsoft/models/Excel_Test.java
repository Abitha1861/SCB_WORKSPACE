package com.hdsoft.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import com.hdsoft.hdpay.models.Configuration_Modal1.Event001_Mapper;
import com.hdsoft.common.Common_Utils;
//import com.hdsoft.models.RTSIS_AutoMan_Modal.Report_Mapper;
import com.hdsoft.Repositories.Event_Creation;
import com.hdsoft.Repositories.web_service_001;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class Excel_Test 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Webservice_Modal Ws;
	
	@Autowired
	public Sysconfig Sys;
	 
	public void Excel_Format_Download(String SERVICECD, String NoOfRecords, HttpServletResponse response,HttpSession session )
	{	
		 try
		 {
			 XSSFWorkbook workbook = new XSSFWorkbook(); 
			 
			 Common_Utils utils = new Common_Utils();
			 
			 String created_by=utils.ReplaceNull(session.getAttribute("sesUserId"));
			 
			 String sql = "Select * from webservice001 where SERVICECD=?";
			 
			 List<web_service_001> API_Info = Jdbctemplate.query(sql, new Object[] { SERVICECD }, Ws.new API_Mapper());
			 
			 JsonObject js = utils.StringToJsonObject(API_Info.get(0).getPAYLOAD());
			 
			 List<String> Avl_elements = utils.get_keys_as_list(js);
			
			 //String sign = "select SIGNPAYLOAD from webservice001 where SERVICECD=?";
			 
			 //String SignPay=Jdbctemplate.queryForObject(sign, new Object[] {SERVICECD}, String.class);
			 
			 if(!utils.isNullOrEmpty(API_Info.get(0).getSIGNPAYLOAD())) {
				 
				 
				 Excel_data_for_Multiparent( SERVICECD,NoOfRecords,response,session);
				 
				 
			 }
			 else {
			 
			 
			 XSSFSheet general_information_sheet = workbook.createSheet(API_Info.get(0).getSERVNAME());
			 
			 for(String Head_Name : Avl_elements)
			 {
				 if(js.has(Head_Name) && js.get(Head_Name).isJsonArray() && js.get(Head_Name).getAsJsonArray().size()!=0)
				 {	
					 JsonObject elements = js.get(Head_Name).getAsJsonArray().get(0).getAsJsonObject();
					 
					 JsonArray Key_Pair = utils.get_key_vals(elements);
					
					 System.out.println("Key_Pair >>>>> "+Key_Pair);
					 
					 JsonArray Serials = new JsonArray();  
					 
					 for(int j=1; j<=Integer.parseInt(NoOfRecords); j++)
					 {
						 Serials.add(j);
					 }
					 
					 Create_Columns(workbook, general_information_sheet, Key_Pair, Serials);
					  
					 autoSizeColumns(workbook);
				 }
			 }
			 
			 String ModuleSubModule="select * from event001 where evtcode=?";
	         
			 List<Event_Creation> obj = Jdbctemplate.query(ModuleSubModule, new Object[] { SERVICECD }, new Event001_Mapper());
				
	           String Module=obj.get(0).getModule();
	           String Submodule=obj.get(0).getSubmodule();
	           String CurrDateTime=utils.getCurrentDateTime();
	          
	           String rts003 = "SELECT RTS003_ID_SEQ.NEXTVAL FROM DUAL";
			   Long serialValue = Jdbctemplate.queryForObject(rts003, Long.class);

	           final String ExcelSheetStore = "Insert into RTS003 (id, suborgcode, module, submodule, servicecd, no_of_records, created_by, created_on) values(?,?,?,?,?,?,?,?)";
	           GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
               Jdbctemplate.update(new PreparedStatementCreator() {
				    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				        PreparedStatement statement = con.prepareStatement(ExcelSheetStore, new String[]{"id"});
				       
				        statement.setLong(1, serialValue);
				        statement.setString(2, Sys.getSuborgcode());
				        statement.setString(3, Module);
				        statement.setString(4, Submodule);
				        statement.setString(5, SERVICECD);
				        statement.setString(6, NoOfRecords);
				        statement.setString(7, created_by);
				        statement.setString(8, CurrDateTime);
				        
				        return statement;
				    }
				}, generatedKeyHolder);

			Number Key = generatedKeyHolder.getKey();
			 
			 System.out.println("Key"+Key);  
			 String filename = Submodule +"_"+ new Common_Utils().getCurrentDate() +"_"+Key+".xlsx";
			 System.out.println("Filename"+filename);
		     response.setContentType("application/vnd.ms-excel");  
		     response.setHeader("Content-Disposition","attachment; filename=\"" + filename + "\"");   
		     response.flushBuffer();
		     
		     String path="select mtypeparam from prop001 where moduleid=? and MTYPE=?";
		     
		     List<String> HEADERID = Jdbctemplate.queryForList(path, new Object[] {"RTSIS" , "ExcelPAth"} , String.class);
		     
		     String filePath = HEADERID.size() !=0 ? HEADERID.get(0) : "\\tmp" + File.separator + filename;
		     
		     //System.out.println("Filepath"+filePath);
		     
		     String upd = "UPDATE rts003 SET is_uploaded=?, is_pushed=?, path=? WHERE id=?";
		     
		     Jdbctemplate.update(upd, new Object[] {0, 0, filePath,  Key});
		     
		     //String upd = "UPDATE rts003 SET is_pushed=? WHERE id=?";
		     
		     //Jdbctemplate.update(upd, new Object[] { 0, Key});
		     
		     File file = new File(filePath);
		     	  
		    try (FileOutputStream fileOut = new FileOutputStream(file)) {
		      workbook.write(fileOut);
		  } catch (IOException e) {
		      e.printStackTrace(); 
		  }

		  
		  response.setContentType("application/vnd.ms-excel");
		  response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		  response.setContentLength((int) file.length());

		  try (FileInputStream fileInputStream = new FileInputStream(file);
		       OutputStream responseOutputStream = response.getOutputStream()) {
		      int bytesRead;
		      byte[] buffer = new byte[4096];
		      while ((bytesRead = fileInputStream.read(buffer)) != -1) {
		          responseOutputStream.write(buffer, 0, bytesRead);
		      }
		  } catch (IOException e) {
		      e.printStackTrace(); 
		  }
            workbook.write(response.getOutputStream()); 
		    
           
		 }
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	}
	
	private void Create_Columns(XSSFWorkbook workbook, XSSFSheet spreadsheet, JsonArray Columns, JsonArray Serials)
	{
		try
		{
			 XSSFRow row = spreadsheet.createRow(0);	
			 
			 XSSFCell cell;
			 cell = row.createCell(0);
			 cell.setCellValue("Serial");
			
			 System.out.println("JsonArray Columns"+Columns);
			 
			  	 
			 for(int i=0;i<Columns.size(); i++)
			 {
				 JsonObject js = Columns.get(i).getAsJsonObject();
				 
				 String Column_Name = js.get("Key").getAsString();
				
				 cell = row.createCell(i+1);
				 cell.setCellValue(Column_Name);
			 }
			 
			 HashMap<String, List<String>> arrays = new HashMap<String, List<String>>();
			 
			 for(int i=0;i<Serials.size(); i++)
			 {
				 row = spreadsheet.createRow(i+1);
				 
				 cell = row.createCell(0);
				 cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				 cell.setCellValue(Serials.get(i).getAsString());
				 
				 for(int j=1;j<Columns.size(); j++)
				 {
					 JsonObject js = Columns.get(j-1).getAsJsonObject();
					 
					 String Constrains = js.get("Value").getAsString();
					 
					 String[] arr = Constrains.split("\\|");
					 
					 cell = row.createCell(j);
					 
					 if(arr.length == 2 && arr[0].toUpperCase().startsWith("D")) 
					 {
						 List<String> list = new ArrayList<String>();
						 
						 if(!arrays.containsKey(arr[0]))
						 {
							 list = get_drop_downs(spreadsheet.getSheetName(), arr[0]);
							 
							 arrays.put(arr[0], list);
						 }
						 else
						 {
							 list = arrays.get(arr[0]);
						 }
						 
						 String[] array = list.toArray(new String[list.size()]);
						 
						 if(array.length !=0)
						 {
							 cell.setCellValue("SELECT");
							 
							 XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) row.getSheet());
							 XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(array);
							 
							 CellRangeAddressList addressList = new CellRangeAddressList(i+1, i+1, j, j);  
							
							 XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
							 validation.setShowErrorBox(true);
							 validation. createErrorBox("ERROR MEESAGE:Invalid Data", "Please provide valid data in the dropdown list.");

							 spreadsheet.addValidationData(validation); 
						 }
						 else
						 {
							 cell.setCellValue("");
						 }
					 } 
					 else if(arr.length == 1 && arr[0].equalsIgnoreCase("currency")) 
					 {
						 CellStyle cellStyle = workbook.createCellStyle();
						 
						 DataFormat format = workbook.createDataFormat();
						
						 cellStyle.setDataFormat(format.getFormat("0.00"));
						 
						 cell.setCellStyle(cellStyle);
					 }
					 else if(arr.length == 1 && arr[0].equalsIgnoreCase("date")) 
					 {
						 CellStyle cellStyle = workbook.createCellStyle();
						 
						 CreationHelper createHelper = workbook.getCreationHelper();
						 
						 cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("ddMMyyyy"));
						
						 cell.setCellStyle(cellStyle);
					 } 
					 else
					 {
						 cell.setCellValue("");
					 }
				 }
			 }
		}
		catch(Exception e)
		{
			 e.printStackTrace();
		}
	}
	
	public List<String> get_drop_downs(String Module, String Column)
	{
		List<String> values = new ArrayList<String>();
		
		try
		{
			String sql = "select w.COLUMN2 from LOOKUP001 w where w.COLUMN1=?";
			
			values = Jdbctemplate.queryForList(sql, new Object[] { Column }, String.class);	
		}
		catch(Exception e)
		{
			values = new ArrayList<String>();
		}
		
		return values;
	}
	
	public void autoSizeColumns(Workbook workbook) 
	{
	    int numberOfSheets = workbook.getNumberOfSheets();
	    
	    for (int i = 0; i < numberOfSheets; i++) 
	    {
	        Sheet sheet = workbook.getSheetAt(i);
	        
	        if(sheet.getPhysicalNumberOfRows() > 0) 
	        {
	            Row row = sheet.getRow(sheet.getFirstRowNum());
	            Iterator<Cell> cellIterator = row.cellIterator();
	            
	            while (cellIterator.hasNext()) 
	            {
	                Cell cell = cellIterator.next();
	                int columnIndex = cell.getColumnIndex();
	                sheet.autoSizeColumn(columnIndex);
	            }
	        }
	    }
	}
	
	public class Event001_Mapper implements RowMapper<Event_Creation> 
    {
		public Event_Creation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Event_Creation Info = new Event_Creation();  
			
			Info.setModule(rs.getString("Moduleid"));
			Info.setSubmodule(rs.getString("Submodule"));
			
			return Info;
		}
    }
    
    
    public JsonObject Excel_data_for_Multiparent(String SERVICECD, String NoOfRecords,HttpServletResponse response,HttpSession session)
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils utils = new Common_Utils();
			 
			 String created_by=utils.ReplaceNull(session.getAttribute("sesUserId"));
			  
			 JsonObject data = new JsonObject();
			 
			 JsonObject data1 = new JsonObject();
			 
		     String webservice001 = "select payload from webservice001 where servicecd=?";
             
             String payload=Jdbctemplate.queryForObject(webservice001,new Object[] {SERVICECD},String.class);
             
             String sign = "select signpayload from webservice001 where servicecd=?";
             
             String signpayload=Jdbctemplate.queryForObject(sign,new Object[] {SERVICECD},String.class);
             
             JsonObject primary=utils.StringToJsonObject(signpayload);
                          
             JsonArray primarykeys = primary.getAsJsonArray("primary");
           
             JsonObject json=utils.StringToJsonObject(payload);
             
             JsonArray keys=utils.get_keys_from_Json(json);
             
             List<String> Avl_elements = utils.get_keys_as_list(json);
                   
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
						 primaries_Array.add(primaries_Object);
						 
					 }
					 else {
							System.out.println("Not a same value");
						} 
					 
				 }
				 
				 
				
			 }
			  
			 XSSFWorkbook workbook = new XSSFWorkbook(); 
			 
			 XSSFSheet general_information_sheet = workbook.createSheet(keys.get(0).getAsString());
			 
			 JsonArray jr = new JsonArray();
			 
			 JsonObject members=new JsonObject();
			 
			
			 Final.addAll(primaries_Array);
			 
			 
			 for(int i=0; i<Array_Members.size(); i++)
			 {
				 
				 String Array_Member = Array_Members.get(i).getAsString();
				 
				 JsonObject js = ArrayFamily.get(Array_Member).getAsJsonObject();
			     
				 jr=utils.get_key_vals(js);
				 
				
				 Final.addAll(jr);
				 
				 
								
				 XSSFSheet dynamicSheet = workbook.createSheet(Array_Member);
				
				 
				 JsonArray Serials = new JsonArray(); 
				 
				 for(int j=1; j<=Integer.parseInt(NoOfRecords); j++)
				 {
					 Serials.add(j);
				 }
				 
				 
				 System.out.println("Final >>>> "+Final);
				 
				 
				 Create_Columns(workbook, dynamicSheet, Final, Serials);
				
				 for (JsonElement element : jr) { 
				        Final.remove(element);
				    }
				 
				 autoSizeColumns(workbook);
				
			 }
			
			 
			 List<String> Avl_elements2 = utils.get_keys_as_list(data1);
			 
			 
			
			 
			 
			  
			 
			 for(String Head_Name : Avl_elements2)
			 {
		    	 if(data1.has(Head_Name) && data1.get(Head_Name).isJsonArray() && data1.get(Head_Name).getAsJsonArray().size()!=0)
				 {	
					 JsonObject elements = data1.get(Head_Name).getAsJsonArray().get(0).getAsJsonObject();
					 
					 JsonArray Key_Pair1 = utils.get_key_vals(elements);
					 
					 
						
					 JsonArray Serials = new JsonArray();  
					 
					 for(int j=1; j<=Integer.parseInt(NoOfRecords); j++)
					 {
						 Serials.add(j);
					 }
					 
					 System.out.println(":::::::::::::::::::::::::::::::::::::Key_Pair1"+Key_Pair1);
					 
					 Create_Columns(workbook, general_information_sheet, Key_Pair1, Serials);
					 
					 
					 autoSizeColumns(workbook);
				 }
			 }
			 
			
			 
			
			 String ModuleSubModule="select * from event001 where evtcode=?";
	         List<Event_Creation> obj = Jdbctemplate.query(ModuleSubModule, new Object[] { SERVICECD }, new Event001_Mapper());
				
	           String Module=obj.get(0).getModule();
	           String Submodule=obj.get(0).getSubmodule();
	           String CurrDateTime=utils.getCurrentDateTime();
	          
	           
	           String sql = "SELECT RTS003_ID_SEQ.NEXTVAL FROM DUAL";
			   Long serialValue = Jdbctemplate.queryForObject(sql, Long.class);

	           final String ExcelSheetStore = "Insert into RTS003 (id, suborgcode, module, submodule, servicecd, no_of_records, created_by, created_on) values(?,?,?,?,?,?,?,?)";
	           GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
               Jdbctemplate.update(new PreparedStatementCreator() {
				    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				        PreparedStatement statement = con.prepareStatement(ExcelSheetStore, new String[]{"id"});
				       
				        statement.setLong(1, serialValue);
				        statement.setString(2, Sys.getSuborgcode());
				        statement.setString(3, Module);
				        statement.setString(4, Submodule);
				        statement.setString(5, SERVICECD);
				        statement.setString(6, NoOfRecords);
				       
				        statement.setString(7, created_by);
				        statement.setString(8, CurrDateTime);
				        
				        return statement;
				    }
				}, generatedKeyHolder);

			Number Key = generatedKeyHolder.getKey();
			 
			 System.out.println("Key"+Key);  
			 String filename = Submodule +"_"+ new Common_Utils().getCurrentDate() +"_"+Key+".xlsx";
			 System.out.println("Filename"+filename);
		     response.setContentType("application/vnd.ms-excel");  
		     response.setHeader("Content-Disposition","attachment; filename=\"" + filename + "\"");   
		     response.flushBuffer();
		     
		     String path="select mtypeparam from prop001 where moduleid=?";
		     String HEADERID = Jdbctemplate.queryForObject(path, new Object[] {"RTSIS" } , String.class);
		     String filePath = HEADERID + File.separator + filename;
		     
		     String upd = "UPDATE rts003 SET is_uploaded=?, is_pushed=?, path=? WHERE id=?";
		     Jdbctemplate.update(upd, new Object[] {0, 0, filePath,  Key});
	
		     File file = new File(filePath);
		     	
		  
		    try (FileOutputStream fileOut = new FileOutputStream(file)) {
		      workbook.write(fileOut);
		  } catch (IOException e) {
		      e.printStackTrace(); 
		  }

		  
		  response.setContentType("application/vnd.ms-excel");
		  response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		  response.setContentLength((int) file.length());

		  try (FileInputStream fileInputStream = new FileInputStream(file);
		       OutputStream responseOutputStream = response.getOutputStream()) {
		      int bytesRead;
		      byte[] buffer = new byte[4096];
		      while ((bytesRead = fileInputStream.read(buffer)) != -1) {
		          responseOutputStream.write(buffer, 0, bytesRead);
		      }
		  } catch (IOException e) {
		      e.printStackTrace(); 
		  }
            workbook.write(response.getOutputStream()); 
		    
           
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
			 
		
		
		return details;
	}
}
			 