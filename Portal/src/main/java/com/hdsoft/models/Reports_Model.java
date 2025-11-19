package com.hdsoft.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.Repositories.Audit_report001;
import com.hdsoft.Repositories.Domain;
import com.hdsoft.Repositories.RTS003;
import com.hdsoft.Repositories.RTS005;
import com.hdsoft.Repositories.RTS006;
import com.hdsoft.Repositories.Report_Details;
import com.hdsoft.Repositories.Users00001_hst;
import com.hdsoft.Repositories.Users0001;
import com.hdsoft.Repositories.web_service_001;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class Reports_Model {
	
public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(Reports_Model.class);
	
public JsonObject Rtsis_Reportings() {
		
		JsonObject json = new JsonObject();
		
		
		try {
			
			String sql = "Select * from RTS006";
			
			List<RTS006> obj = Jdbctemplate.query(sql,new Rtsis_mapping());
			
			
			
			json.add("Info", new Gson().toJsonTree(obj));
            	
			json.addProperty("result", obj.size() != 0 ? "success" : "failed");
			json.addProperty("stscode", obj.size() != 0 ? "200" : "400");
			json.addProperty("message", obj.size() != 0 ? "Product Info's Retrieved !!" : "Product Info's Not Retrieved !!"); 
	
		}
		catch(Exception e) {
			
			json.addProperty("result", "failed");
			json.addProperty("stscode", "500");
			json.addProperty("message",e.getLocalizedMessage());
			
			
			
		}
		
		return json;
	}
 
public JsonObject Batch_ID( RTS006 info) 

{

	JsonObject details = new JsonObject();
	
	String APICODE = info.getAPICODE();
	
	 //String API_CODE = info.getAPICODE();
    
       int startIndex = APICODE.indexOf('-') + 1;
      
       int endIndex = APICODE.lastIndexOf(')');

       if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) {
          
       	APICODE = APICODE.substring(startIndex, endIndex);
           
       } 
       
	try

	{
		  System.out.println(APICODE);
		  
		  String sql ="SELECT distinct(batchid) FROM rts006 where apicode = ? ORDER BY CAST(batchid AS INT) DESC";

		  List<String> obj = Jdbctemplate.queryForList(sql,new Object[]{APICODE}, String.class);

		  details.add("events", new Gson().toJsonTree(obj));

		  details.addProperty("Result", "Success");

		  details.addProperty("Message", "details found");

	 }

	 catch(Exception e)

	 {

		 details.addProperty("Result", "Failed");

		 details.addProperty("Message", e.getLocalizedMessage()); 

		 logger.debug(">>>>>>>>>>> Exception occurs when retrieving batchid from rts006 <<<<<<<<<<<<<<<"+e.getLocalizedMessage());

	 }

	 return details;

}

public JsonArray Get_API_Codes(String term) 
{
	JsonArray Event_Codes = new JsonArray();
	
	try
	{
		/* String sql = "SELECT * FROM webservice001 WHERE servicecd IN (select apicode from rts006 where apicode like upper(?) or apicode like lower(?))";
		
		 List<web_service_001> API_Info = Jdbctemplate.query(sql, new Object[] { "%"+term+"%", "%"+term+"%"}, new API_Mapper() );
		*/
		
		 String sql = "select * from webservice001 where METHOD =? and (SERVICECD LIKE upper(?) or SERVICECD LIKE lower(?) or SERVNAME LIKE upper(?) or SERVNAME LIKE lower(?)) AND servicecd NOT IN (?) ";
			
		 List<web_service_001> API_Info = Jdbctemplate.query(sql, new Object[] { "POST", "%"+term+"%", "%"+term+"%", "%"+term+"%", "%"+term+"%", "RTS999"}, new API_Mapper() );
		 
		 for(int i=0; i<API_Info.size();i++)
			 {
			// String CHCODE = API_Info.get(i).getCHCODE();
			 String SERVNAME = API_Info.get(i).getSERVNAME();
			 String SERVICECD = API_Info.get(i).getSERVICECD();
			 
			 JsonObject Informations = new JsonObject();

			 Informations.addProperty("label",  " ("+SERVNAME+ "-"+ SERVICECD+")");
			 Informations.addProperty("id", SERVNAME+"|"+ SERVICECD);
			 
			 Event_Codes.add(Informations);
			 
			 }
		 
	 }
	 catch(Exception e)
	 {
		 logger.debug("Exception in Get_API_Codes of report filter :::: "+e.getLocalizedMessage());
	 }
	
	 return Event_Codes;
}

public JsonObject Get_Event_Report(String REPORTSL, String StartSl, String EndSl) 
{
	JsonObject details = new JsonObject();
	
	System.out.println("REPORTSL :" +REPORTSL);
	System.out.println("StartSl :" +StartSl);
	
	try
	{	
		JsonArray details_list = new JsonArray();
		
		 Common_Utils util = new Common_Utils();
		
		     
			String sql = "select COLUMN3 from REPORT002 where COLUMN1=? and SERIAL = ?  order by COLUMN4";
			 
			 List<String> Headers = Jdbctemplate.queryForList(sql, new Object[] { "H", REPORTSL}, String.class);
			 
			 System.out.println("Headers :"+Headers);
			  
			 for(int z = 0; z < Headers.size(); z++)
			 {
				 JsonObject Js = new JsonObject();
				
				 String Header = Headers.get(z);
				 
 			 ArrayList<String> Columns = new ArrayList<String>();   
 			 
 			 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN3=?";
 			
			 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "C", Header }, new Report_Mapper());
			 
			 System.out.println("Reports :" +Reports.size());
			 
			 for(int i=0; i<Reports.size();i++)
			 {
				 String COLUMN1 = Reports.get(i).getCOLUMN1();
				 
				 if(COLUMN1.equals("C"))
				 {
					 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
					 
					 for(int j=3; j<=100;j++) // from column 4 to column 40
					 {
						 String Column_value = columns.get("COLUMN"+j).getAsString();
						 
						 System.out.println("Column_value :" +Column_value);
						 
						 if(!util.isNullOrEmpty(Column_value))  
						 {  
							 Columns.add("COLUMN"+j);
							 
						 }
					 }	
				 }
				 
			 }
			 
			 JsonArray Table_Columns = new JsonArray();  
			 
			 System.out.println("size :" +Reports.size());
			 
			 for(int i=0; i<Reports.size();i++)
			 {
				 String COLUMN1 = Reports.get(i).getCOLUMN1();
				 
				 System.out.println("COLUMN1 :"+COLUMN1);
				 
				 if(COLUMN1.equals("C"))
				 {
					 JsonElement jsonElement = new Gson().toJsonTree(Reports.get(i));
					 
					 JsonObject Columns_details = jsonElement.getAsJsonObject();
					 
					 for(int j=0; j<Columns.size(); j++)
		 			 {
						 if(Columns_details.has(Columns.get(j)))
						 {
							 JsonObject Column_Info = new JsonObject();  JsonObject Column_Info2 = new JsonObject();
							 
							 Column_Info.addProperty("sTitle", Columns_details.get(Columns.get(j)).getAsString());
							 Column_Info.addProperty("mData", util.Replace_Special_Characters(Columns_details.get(Columns.get(j)).getAsString()));
							 
							 Column_Info2.addProperty("label", Columns_details.get(Columns.get(j)).getAsString());
							 Column_Info2.addProperty("name", util.Replace_Special_Characters(Columns_details.get(Columns.get(j)).getAsString()));
							 
							 Table_Columns.add(Column_Info);
							
						 }
		 			 }
					 
					 Js.add("Columns_details", Table_Columns);   
					
					 System.out.println("Table_Columns :"+Table_Columns);
				 }
				 else if(COLUMN1.equals("H"))
				 {
					 Js.add("Heading_details", new Gson().toJsonTree(Reports.get(i))); 
				 }
			 }
 			
			 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN3=? and COLUMN4 between ? and ? order by cast(COLUMN4 as int)";  
	 			
			 Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "D", Header, StartSl,EndSl}, new Report_Mapper());
			 
			 List<Report_Details> Data_Reports = new ArrayList<Report_Details>();
			 
			 for(int i=0; i<Reports.size();i++)
			 {
				 Data_Reports.add(Reports.get(i));
			 }
			 
			 JsonElement jsonElement = new Gson().toJsonTree(Data_Reports);
			 
			 JsonArray Report_details = jsonElement.getAsJsonArray();
			 
			 String Report_details_= Report_details.toString();
			 
			 for(int i=Table_Columns.size()-1; i>=0; i--)
			 {
				  JsonObject Column_Info = Table_Columns.get(i).getAsJsonObject();
				  
				  String Replace_from = Columns.get(i);   
				  
				  String Replace_by = Column_Info.get("mData").getAsString();  
				  
				  Report_details_ = Report_details_.replaceAll(Replace_from, Replace_by);
			 }
			 
			 Report_details = util.StringToJsonArray(Report_details_); 
			 
			 Js.add("Report_details", Report_details);   ///Event_Run/Generate_Report
			 
			 details_list.add(Js);	 
 		}
 			 
			details.add("Headers", new Gson().toJsonTree(Headers));	 
			
			details.add("details_list", details_list);	 
			
			details.addProperty("REPORTSL", REPORTSL);
			details.addProperty("StartSl", StartSl);
	 			
		details.addProperty("Result", details_list.size() !=0 ? "Success" : "Failed");
		details.addProperty("Message", details_list.size() !=0 ? "Report data retrieved Successfully" : "No data found from the Event");
		System.out.println(details.get("Message").getAsString());
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage());  
		 
		 logger.debug(">>>>>>>>>>> Exception occurs while retrieving data <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
	 }
	
	 return details;
}


public JsonObject Report_filter(RTS006 info) 
{
	
	JsonObject json = new JsonObject();
	
	try {
		
			Common_Utils util = new Common_Utils();
		
			String APICODE = info.getAPICODE();
	     
	        int startIndex = APICODE.indexOf('-') + 1;
	       
	        int endIndex = APICODE.lastIndexOf(')');

	        if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) 
	        {
	        	
	        	APICODE = APICODE.substring(startIndex, endIndex);
	            
	        } 
	     
	     String BATCHID = info.getBATCHID();
	        
		 String FDATE = util.Convert_Date_Format(info.getFDATE(), "yyyy-MM-dd", "dd-MMM-yyyy");
		 
		 String TDATE = util.Convert_Date_Format(info.getTDATE(), "yyyy-MM-dd", "dd-MMM-yyyy");
		 
		 String STATUS = info.getSTATUS();
		 
		 String RESPDESC = info.getRESPDESC();
		 
		 System.out.println("APICODE" + APICODE);
		 System.out.println("BATCHID" + BATCHID);
		 System.out.println("FDATE" + FDATE);
		 System.out.println("TDATE" + TDATE);
		 System.out.println("STATUS" +STATUS);
		 System.out.println("RESPDESC" +RESPDESC);
		 
		 List<RTS006> obj =  new ArrayList<RTS006>();
		 List<RTS005> obj1 =  new ArrayList<RTS005>();
	
		 if(!APICODE.isEmpty() && !FDATE.isEmpty() && TDATE.isEmpty()&& !BATCHID.isEmpty()){
	            
	    	 String sql = "Select * from RTS006 WHERE APICODE = ? and REQDATE = ? and batchid = ? order by reqtime desc";
	         
	    	 obj = Jdbctemplate.query(sql, new Object[]{APICODE,FDATE,BATCHID}, new Rtsis_mapping());
	         
		 }
		 else if (APICODE.equalsIgnoreCase("COUNTRY") || APICODE.equalsIgnoreCase("FINANCE") || APICODE.equalsIgnoreCase("TRADE") || APICODE.equalsIgnoreCase("ACBS") || APICODE.equalsIgnoreCase("MARCIS") || APICODE.equalsIgnoreCase("GEMS") || APICODE.equalsIgnoreCase("CLIENT COVERAGE") || APICODE.equalsIgnoreCase("FM") || APICODE.equalsIgnoreCase("CASH") || APICODE.equalsIgnoreCase("EBBS") || APICODE.equalsIgnoreCase("CADM") || APICODE.equalsIgnoreCase("APARTA")) 
		 {
			 
			 if (APICODE.equalsIgnoreCase("Client Coverage")) 
			 {
				 APICODE = "CC";
			 }

			 String sql = "SELECT * FROM DOMAIN001  WHERE DOMAIN = ?";  
			 
			 List<Domain> Info = Jdbctemplate.query(sql, new Object[] { APICODE }, new Domain001Mapper());			 	 
			 
			 List<String> apiCodes = new ArrayList<>();
			 
			 for (Domain d : Info) 
			 {
			     apiCodes.add(d.getAPICODE());
			 }
			 
			 if (!apiCodes.isEmpty()) 
			 {
				 if (APICODE.equalsIgnoreCase("ACBS")) 
				 {
					 
					    sql = "SELECT * FROM RTS006 " +
					            "WHERE APICODE IN (SELECT APICODE FROM DOMAIN001 WHERE DOMAIN = ?) " +
					            "AND REQDATE BETWEEN TO_DATE(?, 'DD-Mon-YYYY') AND TO_DATE(?, 'DD-Mon-YYYY') " +
					            "AND PAYTYPE <> ? " +
					            "AND REPORTSERIAL IN (SELECT REPORT_SERIAL FROM RTS003 WHERE source_type = ?)";

					    Object[] params = new Object[] { APICODE,   FDATE,    TDATE,    "ARCHIVED",  "ACBS"  };
					      
					    
					    obj = Jdbctemplate.query(sql, params, new Rtsis_mapping());
				 }
				 else if (APICODE.equalsIgnoreCase("CASH")) 
				 {
					    sql = "SELECT * FROM RTS006 " +
					            "WHERE APICODE IN (SELECT APICODE FROM DOMAIN001 WHERE DOMAIN = ?) " +
					            "AND REQDATE BETWEEN TO_DATE(?, 'DD-Mon-YYYY') AND TO_DATE(?, 'DD-Mon-YYYY') " +
					            "AND PAYTYPE <> ? " +
					            "AND REPORTSERIAL IN (SELECT REPORT_SERIAL FROM RTS003 WHERE source_type LIKE ?)";

					    Object[] params = new Object[] { APICODE,   FDATE,    TDATE,    "ARCHIVED",  "CASH%"  };
					      
					    
					    obj = Jdbctemplate.query(sql, params, new Rtsis_mapping());

				 }
				 else if (APICODE.equalsIgnoreCase("TRADE")) 
				 {
					    sql = "SELECT * FROM RTS006 " +
					            "WHERE APICODE IN (SELECT APICODE FROM DOMAIN001 WHERE DOMAIN = ?) " +
					            "AND REQDATE BETWEEN TO_DATE(?, 'DD-Mon-YYYY') AND TO_DATE(?, 'DD-Mon-YYYY') " +
					            "AND PAYTYPE <> ? " +
					            "AND REPORTSERIAL IN (SELECT REPORT_SERIAL FROM RTS003 WHERE source_type LIKE ?)";

					    Object[] params = new Object[] { APICODE,   FDATE,    TDATE,    "ARCHIVED",  "Trade%"  };
					      
					    
					    obj = Jdbctemplate.query(sql, params, new Rtsis_mapping());

				 }
				 
				 else if (APICODE.equalsIgnoreCase("EBBS")) 
				 {
					    sql = "SELECT * FROM RTS006 " +
					            "WHERE APICODE IN (SELECT APICODE FROM DOMAIN001 WHERE DOMAIN = ?) " +
					            "AND REQDATE BETWEEN TO_DATE(?, 'DD-Mon-YYYY') AND TO_DATE(?, 'DD-Mon-YYYY') " +
					            "AND PAYTYPE <> ? " +
					            "AND REPORTSERIAL IN (SELECT REPORT_SERIAL FROM RTS003 WHERE source_type = ?)";

					    Object[] params = new Object[] { APICODE,   FDATE,    TDATE,    "ARCHIVED",  "EBBS"  };
					      
					    
					    obj = Jdbctemplate.query(sql, params, new Rtsis_mapping());

				 }
				 
				 else if (APICODE.equalsIgnoreCase("FM")) 
				 {
					    sql = "SELECT * FROM RTS006 " +
					            "WHERE APICODE IN (SELECT APICODE FROM DOMAIN001 WHERE DOMAIN = ?) " +
					            "AND REQDATE BETWEEN TO_DATE(?, 'DD-Mon-YYYY') AND TO_DATE(?, 'DD-Mon-YYYY') " +
					            "AND PAYTYPE <> ? " +
					            "AND REPORTSERIAL IN (SELECT REPORT_SERIAL FROM RTS003 WHERE source_type = ?)";

					    Object[] params = new Object[] { APICODE,   FDATE,    TDATE,    "ARCHIVED",  "FM"  };
					      
					    
					    obj = Jdbctemplate.query(sql, params, new Rtsis_mapping());

				 }
				 else 
				 {
					    sql = "SELECT * FROM RTS006 WHERE APICODE IN (SELECT APICODE FROM DOMAIN001 WHERE DOMAIN = ?) " +
						          "AND reqdate >= TO_DATE(?, 'DD-Mon-YYYY') " + 
						          "AND reqdate <= TO_DATE(?, 'DD-Mon-YYYY') AND PAYTYPE <> ?" + 
						          "ORDER BY reqtime DESC";			    
						    
						 Object[] params = new Object[] { APICODE, FDATE, TDATE , "ARCHIVED" };

						 obj = Jdbctemplate.query(sql, params, new Rtsis_mapping());
				 }
			 } 		 			 
		 } 
		 else if(!APICODE.isEmpty() && !FDATE.isEmpty() && !TDATE.isEmpty() )  
		 {
			 
			  String sql = "Select * from RTS006 WHERE APICODE = ? and to_date(REQDATE) BETWEEN ? AND ? order by reqtime desc";
	         
	    	  obj = Jdbctemplate.query(sql, new Object[]{APICODE,FDATE,TDATE}, new Rtsis_mapping());
	         
		 }
		 /*else if(!APICODE.isEmpty() && !FDATE.isEmpty() && !BATCHID.isEmpty() )  {
			 
			  String sql = "Select * from RTS006 WHERE APICODE = ? and batchid = ? and REQDATE = ?";
	         
	    	  obj = Jdbctemplate.query(sql, new Object[]{APICODE,BATCHID, FDATE}, new Rtsis_mapping());
	         
		 }*/
		 else if(!APICODE.isEmpty() && !BATCHID.matches("Select") && STATUS.isEmpty()) {
			 
			 String sql = "Select * from RTS006 where (apicode like upper(?) or apicode like lower(?)) and batchid = ? order by reqtime desc";
			 
			 obj = Jdbctemplate.query(sql, new Object[]{"%"+APICODE+"%","%"+APICODE+"%",BATCHID}, new Rtsis_mapping());
			
		 }else if(!APICODE.isEmpty() && !BATCHID.isEmpty() && !STATUS.isEmpty() && !RESPDESC.matches("Select")) {
			 
			  String sql = "Select * from RTS006 WHERE APICODE = ? and STATUS = ? and RESPDESC = ? and batchid = ? order by reqtime desc";
	         
	    	  obj = Jdbctemplate.query(sql, new Object[]{APICODE,STATUS,RESPDESC,BATCHID}, new Rtsis_mapping());
	    	  
		 }else if(!APICODE.isEmpty() && BATCHID.matches("Select") && STATUS.isEmpty() && RESPDESC.matches("Select")) {
			 
			  String sql = "Select * from RTS006 WHERE APICODE = ? order by reqtime desc";
	         
	    	  obj = Jdbctemplate.query(sql, new Object[]{APICODE}, new Rtsis_mapping());
	    	  
		 }else if(STATUS.equalsIgnoreCase("Pending") && !APICODE.isEmpty() && RESPDESC.matches("Select")) {
			  
			  String sql = "Select * from RTS005 WHERE APICODE = ?";
	         
	    	  obj1 = Jdbctemplate.query(sql, new Object[]{APICODE}, new Rtsismapping());
	    	  
	 		  json.add("Info1", new Gson().toJsonTree(obj1));
	 		  
		 }

		 
		 for(int i=0; i<obj.size(); i++)
			{ 
				 String ReportSl = obj.get(i).getREPORTSERIAL();
				 String StartSl = obj.get(i).getSTARTSL();
				 String EndSl = obj.get(i).getENDSL();
				
				/*String action  = "<div class=\"dropdown dropdown-action\">\r\n" + 
						"<a href=\"#\" class=\"action-icon dropdown-toggle\" data-bs-toggle=\"dropdown\" aria-expanded=\"false\"><i class=\"material-icons\">more_vert</i></a>\r\n" + 
						"<div class=\"dropdown-menu dropdown-menu-right\">\r\n" + 
						"<a class=\"dropdown-item\" href=\"#\" data-bs-toggle=\"modal\" data-bs-target=\"#view_product\" onclick=\"view_option('"+ReportSl+"','"+StartSl+"','"+EndSl+"')\"><i class=\"fa fa-pencil m-r-5\"></i>View</a>\r\n" + 
						"</div>\r\n" + 
						"</div>";*/
				 String action  ="<a class=\"dropdown-item\" href=\"#\" data-bs-toggle=\"modal\" data-bs-target=\"#view_product\" onclick=\"view_option('"+ReportSl+"','"+StartSl+"','"+EndSl+"')\">View</a>" ; 
							
				
				obj.get(i).setACTION(action);
			}
		
		 json.add("Info", new Gson().toJsonTree(obj));
		
		 if (json.has("Info1") && json.getAsJsonArray("Info1").size() != 0) {
			    
			 	json.addProperty("result", "pending");
			    json.addProperty("stscode", "201");
			    json.addProperty("message", "Report pending !!");
		
		 } else if (json.has("Info") && json.getAsJsonArray("Info").size() != 0) {
			    
			 	json.addProperty("result", "success");
			    json.addProperty("stscode", "200");
			    json.addProperty("message", "Report Retrieved");
		
		 } else {
			    
			 	json.addProperty("result", "failed");
			    json.addProperty("stscode", "400");
			    json.addProperty("message", "Report Not Found !!");
		}
		 
		
	}
	catch(Exception e) {
		
		json.addProperty("result", "failed");
		json.addProperty("stscode", "500");
		json.addProperty("message",e.getLocalizedMessage());
		
		logger.debug("Exception in Handle_Request of Report filter :::: "+e.getLocalizedMessage()); 
		
	}
	
	return json;
}

public JsonObject User_Reportings() {
	
	JsonObject json = new JsonObject();
	
	
	try {
		
		String sql = "Select * from users00001";
		
		List<Users0001> obj = Jdbctemplate.query(sql,new Users0001_mapping());
		
		
		
		json.add("Info", new Gson().toJsonTree(obj));
        	
		json.addProperty("result", obj.size() != 0 ? "success" : "failed");
		json.addProperty("stscode", obj.size() != 0 ? "200" : "400");
		json.addProperty("message", obj.size() != 0 ? "Product Info's Retrieved !!" : "Product Info's Not Retrieved !!"); 

	}
	catch(Exception e) {
		
		json.addProperty("result", "failed");
		json.addProperty("stscode", "500");
		json.addProperty("message",e.getLocalizedMessage());
		
		
		
	}
	
	return json;
}



public JsonObject Get_User_list_Report()
{
	JsonObject details = new JsonObject();
	
	try 
	{
			
		 String SQL = "Select rownum, USERSCD, UNAME, SUBORGCODE, ROLECD,\r\n"
		 		+ "(select bb.ULOGIN_IN_DATE from USERS008 bb where bb.ulogin_user_id = aa.userscd) as Last_Login_date,\r\n"
		 		+ "USERTYPE as ACCOUNT_TYPE, \r\n"
		 		+ "upper(USERSTS) as STATUS from USERS00001 aa" ;
		 
		 List<Users0001> Info = Jdbctemplate.query(SQL, new Users_list_mapping());
		
		 details.add("User_list_report",  new Gson().toJsonTree(Info));
		 
		 details.addProperty("Result", Info.size() !=0 ? "Success" : "Failed");
		 details.addProperty("Message", Info.size() !=0 ? "User list report Found !!" : "User list report Not Found !!");
		 
		 }
		 catch(Exception e)
		 {
			 
	     details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage()); 
		 
		 logger.debug("Exception in User list report :::: "+e.getLocalizedMessage()); 
		 }
	
	 return details;
}

public JsonObject Get_Audit_Report()
{
	JsonObject details = new JsonObject();
	try 
	{
			
		 //String SQL = "select ROWNUM, aa.userscd, aa.uname, null as Request_Type, null as Old_value, null as New_value, EDATE as Action_date_time FROM users0001 aa where aa.ROLECD = 'ADMIN'";
		 
		 String SQL = "select * FROM audit_report001 order by ACTIONDATETIME DESC ";
		 
		
		 List<Audit_report001> Info = Jdbctemplate.query(SQL, new Audit_report_map());
		
		 details.add("Audit_report",  new Gson().toJsonTree(Info));
		 
		 details.addProperty("Result", Info.size() !=0 ? "Success" : "Failed");
		 details.addProperty("Message", Info.size() !=0 ? "Audit Report Found !!" : "Audit Report Not Found !!");
		 
		 System.out.println(details);
		 	
		 }
		 catch(Exception e)
		 {
			 
	     details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage()); 
		 
		 logger.debug("Exception in Audit Report :::: "+e.getLocalizedMessage()); 
		 }
	
	 return details;
}


public JsonObject Get_Security_Report()
{
	JsonObject details = new JsonObject();
	
	try 
	{		
		 String SQL = "select ROWNUM, aa.userscd, ROLECD uname, null as Request_Type, null as Old_value, null as New_value FROM users00001 aa";
		 
		 List<Users0001> Info = Jdbctemplate.query(SQL, new Security_report_mapping());
		
		 details.add("Security_report",  new Gson().toJsonTree(Info));
		 
		 details.addProperty("Result", Info.size() !=0 ? "Success" : "Failed");
		 details.addProperty("Message", Info.size() !=0 ? "Security report Found !!" : "Security report Not Found !!");
		 
		 System.out.println(details);
		 	
		 }
		 catch(Exception e)
		 {
			 
	     details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage()); 
		 
		 logger.debug("Exception in Audit Report :::: "+e.getLocalizedMessage()); 
		 }
	
	 return details;
}


public JsonObject User_Del_Reportings() {
    JsonObject json = new JsonObject();
    
    try {
        String sql = "SELECT TO_CHAR(w.REGDATE, 'YYYY-MM-DD') REGDATE, ROLECD, USERSCD, ADATE FROM users00001_hst w ORDER BY ADATE DESC";
        
         List<Users00001_hst> obj = Jdbctemplate.query(sql, new Users00001_hst_mapping());
        
        json.add("Info", new Gson().toJsonTree(obj));
        json.addProperty("result", !obj.isEmpty() ? "success" : "failed");
        json.addProperty("stscode", !obj.isEmpty() ? "200" : "400");
        json.addProperty("message", !obj.isEmpty() ? "Info's Retrieved !!" : "Info's Not Retrieved !!");

    } catch (Exception e) {
        json.addProperty("result", "failed");
        json.addProperty("stscode", "500");
        json.addProperty("message", e.getLocalizedMessage());
    }
    
    return json;
}

//-------------------------------------------------


public JsonArray Get_API_Codes_DOMAIN(String term , String domain) 
{
	JsonArray Event_Codes = new JsonArray();
	
	try
	{
		String Domain_001 = "";
		
		if(domain.equalsIgnoreCase("Country Report"))
		{
			Domain_001 = "COUNTRY";
		}
		else if (domain.equalsIgnoreCase("Finance Report")) 
		{
			Domain_001 = "FINANCE";
		}
		else if (domain.equalsIgnoreCase("Trade Report")) 
		{
			Domain_001 = "TRADE";
		}
		else if (domain.equalsIgnoreCase("ACBS Report")) 
		{
			Domain_001 = "ACBS";
		}
		else if (domain.equalsIgnoreCase("Marcis Report")) 
		{
			Domain_001 = "MARCIS";
		}
		else if (domain.equalsIgnoreCase("Gems Report")) 
		{
			Domain_001 = "GEMS";
		}
		else if (domain.equalsIgnoreCase("Client Coverage Report")) 
		{
			Domain_001 = "CC";
		}
		else if (domain.equalsIgnoreCase("Fm Report")) 
		{
			Domain_001 = "FM";
		}
		else if (domain.equalsIgnoreCase("Cash Report")) 
		{
			Domain_001 = "CASH";
		}
		else if (domain.equalsIgnoreCase("Ebbs Report")) 
		{
			Domain_001 = "EBBS";
		}	
		else if (domain.equalsIgnoreCase("Cadm Report")) 
		{
			Domain_001 = "CADM";
		}
		else if (domain.equalsIgnoreCase("Aparta Report")) 
		{
			Domain_001 = "APARTA";
		}	
		else 
		{
			Domain_001 = "COUNTRY";
		}
		 
		 String sql = "SELECT * FROM DOMAIN001  WHERE DOMAIN = ? ";
		 
		 List<Domain> API_Info = Jdbctemplate.query(sql, new Object[] { Domain_001 } , new Domain001Mapper());
		 
		 for(int i=0; i<API_Info.size();i++)
			 {
			 
				 String SERVNAME = API_Info.get(i).getAPI_NAME();
				 String SERVICECD = API_Info.get(i).getAPICODE();
				 
				 JsonObject Informations = new JsonObject();
	
				 Informations.addProperty("label",  " ("+SERVNAME+ "-"+ SERVICECD+")");
				 Informations.addProperty("id", SERVNAME+"|"+ SERVICECD);
				 
				 Event_Codes.add(Informations);
			 
			 }
		 
	 }
	 catch(Exception e)
	 {
		 logger.debug("Exception in Get_API_Codes of report filter :::: "+e.getLocalizedMessage());
	 }
	
	 return Event_Codes;
}


public class Audit_report_map implements RowMapper<Audit_report001>{

	Common_Utils utils = new Common_Utils();
	public Audit_report001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Audit_report001 u1 = new Audit_report001();
		
		u1.setROWNUM(rowNum+1+"");
		u1.setADMINID(utils.ReplaceNull(rs.getString("ADMINID")));
		u1.setUSERBANKID(utils.ReplaceNull(rs.getString("USERBANKID")));
		u1.setREQTYPE(utils.ReplaceNull(rs.getString("REQTYPE")));
		u1.setOLDVALUE(utils.ReplaceNull(rs.getString("OLDVALUE")));
		u1.setNEWVALUE(utils.ReplaceNull(rs.getString("NEWVALUE")));
		u1.setACTIONDATETIME(utils.ReplaceNull(rs.getString("ACTIONDATETIME")));
		
		return u1;
	}
	
}

public class Users00001_hst_mapping implements RowMapper<Users00001_hst>
{
	Common_Utils utils = new Common_Utils();
	
	public Users00001_hst mapRow(ResultSet rs, int rowNum) throws SQLException 
	{
		Users00001_hst u1 = new Users00001_hst();
		
		u1.setROWNUM(rowNum+1+"");
		u1.setREGDATE(utils.ReplaceNull(rs.getString("REGDATE")));
		u1.setROLECD(utils.ReplaceNull(rs.getString("ROLECD")));
		u1.setUSERSCD(utils.ReplaceNull(rs.getString("USERSCD")));
		u1.setADATE(utils.ReplaceNull(rs.getString("ADATE")));
		
		return u1;
	}
	
}


public class Rtsis_mapping implements RowMapper<RTS006>{

	Common_Utils utils = new Common_Utils();
	public RTS006 mapRow(ResultSet rs, int rowNum) throws SQLException {
		RTS006 u1 = new RTS006();
		
		u1.setRowNum(rowNum+1);
		u1.setREQTIME(utils.ReplaceNull(rs.getString("REQTIME")));
		u1.setREFNO(utils.ReplaceNull(rs.getString("REFNO")));
		u1.setBATCHID(utils.ReplaceNull(rs.getString("BATCHID")));
		u1.setAPICODE(utils.ReplaceNull(rs.getString("APICODE")));
		u1.setREPORTSERIAL(utils.ReplaceNull(rs.getString("REPORTSERIAL")));
		u1.setSTARTSL(utils.ReplaceNull(rs.getString("STARTSL")));
		u1.setENDSL(utils.ReplaceNull(rs.getString("ENDSL")));
		u1.setBOTREFNO(utils.ReplaceNull(rs.getString("BOTREFNO")));
		u1.setSTATUS(utils.ReplaceNull(rs.getString("STATUS")));
		u1.setRESCODE(utils.ReplaceNull(rs.getString("RESCODE")));
		u1.setRESPDESC(utils.ReplaceNull(rs.getString("RESPDESC")));
		u1.setREQDATE(utils.ReplaceNull(rs.getString("REQDATE")));
		
		return u1;
	}
	
}

public class Rtsismapping implements RowMapper<RTS005>{

	Common_Utils utils = new Common_Utils();
	public RTS005 mapRow(ResultSet rs, int rowNum) throws SQLException {
		RTS005 u1 = new RTS005();
		
		u1.setRowNum(rowNum+1);
		u1.setSUBORGCODE(utils.ReplaceNull(rs.getString("SUBORGCODE")));
		u1.setSYSCODE(utils.ReplaceNull(rs.getString("SYSCODE")));
		u1.setPAYTYPE(utils.ReplaceNull(rs.getString("PAYTYPE")));
		u1.setREQDATE(utils.ReplaceNull(rs.getString("REQDATE")));
		u1.setREFNO(utils.ReplaceNull(rs.getString("REFNO")));
		u1.setREQSL(utils.ReplaceNull(rs.getString("REQSL")));
		u1.setBATCHID(utils.ReplaceNull(rs.getString("BATCHID")));
		u1.setAPICODE(utils.ReplaceNull(rs.getString("APICODE")));
		u1.setREPORTSERIAL(utils.ReplaceNull(rs.getString("REPORTSERIAL")));
		u1.setSTARTSL(utils.ReplaceNull(rs.getString("STARTSL")));
		u1.setENDSL(utils.ReplaceNull(rs.getString("ENDSL")));
		u1.setSTATUS(utils.ReplaceNull(rs.getString("STATUS")));
		
		return u1;
	}
	
}

private class API_Mapper implements RowMapper<web_service_001> 
{
	public web_service_001 mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		web_service_001 API = new web_service_001();  

		API.setSUBORGCODE(rs.getString("SUBORGCODE"));
		API.setCHCODE(rs.getString("CHCODE"));
		API.setSERVICECD(rs.getString("SERVICECD"));
		API.setSERVNAME(rs.getString("SERVNAME"));
		API.setFORMAT(rs.getString("FORMAT"));
		API.setPROTOCOL(rs.getString("PROTOCOL"));
		API.setMETHOD(rs.getString("METHOD"));
		API.setCHTYPE(rs.getString("CHTYPE"));
		API.setURI(rs.getString("URI"));
		API.setPAYLOAD(rs.getString("PAYLOAD"));
		API.setSIGNPAYLOAD(rs.getString("SIGNPAYLOAD"));
		API.setHEADERID(rs.getString("HEADERID"));
		API.setFLOW(rs.getString("FLOW"));
		API.setJOBREQ(rs.getString("JOBREQ"));
		
		return API;
	}
 }


public class Users0001_mapping implements RowMapper<Users0001>{

	Common_Utils utils = new Common_Utils();
	public Users0001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users0001 u1 = new Users0001();
		u1.setRowNum(rowNum+1);
		u1.setDOB(utils.ReplaceNull(rs.getString("DOB")));
		u1.setBRNHCD(utils.ReplaceNull(rs.getString("BRANCHCD")));
		u1.setEMAILID(utils.ReplaceNull(rs.getString("EMAILID")));
		u1.setMOBILENO(utils.ReplaceNull(rs.getString("MOBILENO")));
		u1.setREGDATE(utils.ReplaceNull(rs.getString("REGDATE")));
		u1.setROLECD(utils.ReplaceNull(rs.getString("ROLECD")));
		u1.setSUBORGCODE(utils.ReplaceNull(rs.getString("SUBORGCODE")));
		u1.setUNAME(utils.ReplaceNull(rs.getString("UNAME")));
		u1.setUSERSCD(utils.ReplaceNull(rs.getString("USERSCD")));
		u1.setVERIFY(utils.ReplaceNull(rs.getString("VERIFY")));
		u1.setVERIFY1(utils.ReplaceNull(rs.getString("VERIFY1")));
		u1.setAUSER(utils.ReplaceNull(rs.getString("AUSER")));
		u1.setADATE(rs.getDate("ADATE"));
		u1.setCUSER(utils.ReplaceNull(rs.getString("CUSER")));
		u1.setCDATE(rs.getDate("CDATE"));
		u1.setEUSER(utils.ReplaceNull(rs.getString("EUSER")));
		u1.setEDATE(rs.getDate("EDATE"));
		
		return u1;
	}
	
}

private class Report_Mapper implements RowMapper<Report_Details> 
{
	Common_Utils util = new Common_Utils(); 
	
	public Report_Details mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		Report_Details obj = new Report_Details();  

		obj.setCOLUMN1(util.ReplaceNull(rs.getString("COLUMN1")));
		obj.setCOLUMN2(util.ReplaceNull(rs.getString("COLUMN2")));
		obj.setCOLUMN3(util.ReplaceNull(rs.getString("COLUMN3")));
		obj.setCOLUMN4(util.ReplaceNull(rs.getString("COLUMN4")));
		obj.setCOLUMN5(util.ReplaceNull(rs.getString("COLUMN5")));
		obj.setCOLUMN6(util.ReplaceNull(rs.getString("COLUMN6")));
		obj.setCOLUMN7(util.ReplaceNull(rs.getString("COLUMN7")));
		obj.setCOLUMN8(util.ReplaceNull(rs.getString("COLUMN8")));
		obj.setCOLUMN9(util.ReplaceNull(rs.getString("COLUMN9")));
		obj.setCOLUMN10(util.ReplaceNull(rs.getString("COLUMN10")));
		obj.setCOLUMN11(util.ReplaceNull(rs.getString("COLUMN11")));
		obj.setCOLUMN12(util.ReplaceNull(rs.getString("COLUMN12")));
		obj.setCOLUMN13(util.ReplaceNull(rs.getString("COLUMN13")));
		obj.setCOLUMN14(util.ReplaceNull(rs.getString("COLUMN14")));
		obj.setCOLUMN15(util.ReplaceNull(rs.getString("COLUMN15")));
		obj.setCOLUMN16(util.ReplaceNull(rs.getString("COLUMN16")));
		obj.setCOLUMN17(util.ReplaceNull(rs.getString("COLUMN17")));
		obj.setCOLUMN18(util.ReplaceNull(rs.getString("COLUMN18")));
		obj.setCOLUMN19(util.ReplaceNull(rs.getString("COLUMN19")));
		obj.setCOLUMN20(util.ReplaceNull(rs.getString("COLUMN20")));
		obj.setCOLUMN21(util.ReplaceNull(rs.getString("COLUMN21")));
		obj.setCOLUMN22(util.ReplaceNull(rs.getString("COLUMN22")));
		obj.setCOLUMN23(util.ReplaceNull(rs.getString("COLUMN23")));
		obj.setCOLUMN24(util.ReplaceNull(rs.getString("COLUMN24")));
		obj.setCOLUMN25(util.ReplaceNull(rs.getString("COLUMN25")));
		obj.setCOLUMN26(util.ReplaceNull(rs.getString("COLUMN26")));
		obj.setCOLUMN27(util.ReplaceNull(rs.getString("COLUMN27")));
		obj.setCOLUMN28(util.ReplaceNull(rs.getString("COLUMN28")));
		obj.setCOLUMN29(util.ReplaceNull(rs.getString("COLUMN29")));
		obj.setCOLUMN30(util.ReplaceNull(rs.getString("COLUMN30")));
		obj.setCOLUMN31(util.ReplaceNull(rs.getString("COLUMN31")));
		obj.setCOLUMN32(util.ReplaceNull(rs.getString("COLUMN32")));
		obj.setCOLUMN33(util.ReplaceNull(rs.getString("COLUMN33")));
		obj.setCOLUMN34(util.ReplaceNull(rs.getString("COLUMN34")));
		obj.setCOLUMN35(util.ReplaceNull(rs.getString("COLUMN35")));
		obj.setCOLUMN36(util.ReplaceNull(rs.getString("COLUMN36")));
		obj.setCOLUMN37(util.ReplaceNull(rs.getString("COLUMN37")));
		obj.setCOLUMN38(util.ReplaceNull(rs.getString("COLUMN38")));
		obj.setCOLUMN39(util.ReplaceNull(rs.getString("COLUMN39")));
		obj.setCOLUMN40(util.ReplaceNull(rs.getString("COLUMN40")));
		obj.setCOLUMN41(util.ReplaceNull(rs.getString("COLUMN41")));
		obj.setCOLUMN42(util.ReplaceNull(rs.getString("COLUMN42")));
		obj.setCOLUMN43(util.ReplaceNull(rs.getString("COLUMN43")));
		obj.setCOLUMN44(util.ReplaceNull(rs.getString("COLUMN44")));
		obj.setCOLUMN45(util.ReplaceNull(rs.getString("COLUMN45")));
		obj.setCOLUMN46(util.ReplaceNull(rs.getString("COLUMN46")));
		obj.setCOLUMN47(util.ReplaceNull(rs.getString("COLUMN47")));
		obj.setCOLUMN48(util.ReplaceNull(rs.getString("COLUMN48")));
		obj.setCOLUMN49(util.ReplaceNull(rs.getString("COLUMN49")));
		obj.setCOLUMN50(util.ReplaceNull(rs.getString("COLUMN50")));
		obj.setCOLUMN51(util.ReplaceNull(rs.getString("COLUMN51")));
		obj.setCOLUMN52(util.ReplaceNull(rs.getString("COLUMN52")));
		obj.setCOLUMN53(util.ReplaceNull(rs.getString("COLUMN53")));
		obj.setCOLUMN54(util.ReplaceNull(rs.getString("COLUMN54")));
		obj.setCOLUMN55(util.ReplaceNull(rs.getString("COLUMN55")));
		obj.setCOLUMN56(util.ReplaceNull(rs.getString("COLUMN56")));
		obj.setCOLUMN57(util.ReplaceNull(rs.getString("COLUMN57")));
		obj.setCOLUMN58(util.ReplaceNull(rs.getString("COLUMN58")));
		obj.setCOLUMN59(util.ReplaceNull(rs.getString("COLUMN59")));
		obj.setCOLUMN60(util.ReplaceNull(rs.getString("COLUMN60")));
		obj.setCOLUMN61(util.ReplaceNull(rs.getString("COLUMN61")));
		obj.setCOLUMN62(util.ReplaceNull(rs.getString("COLUMN62")));
		obj.setCOLUMN63(util.ReplaceNull(rs.getString("COLUMN63")));
		obj.setCOLUMN64(util.ReplaceNull(rs.getString("COLUMN64")));
		obj.setCOLUMN65(util.ReplaceNull(rs.getString("COLUMN65")));
		obj.setCOLUMN66(util.ReplaceNull(rs.getString("COLUMN66")));
		obj.setCOLUMN67(util.ReplaceNull(rs.getString("COLUMN67")));
		obj.setCOLUMN68(util.ReplaceNull(rs.getString("COLUMN68")));
		obj.setCOLUMN69(util.ReplaceNull(rs.getString("COLUMN69")));
		obj.setCOLUMN70(util.ReplaceNull(rs.getString("COLUMN70")));
		obj.setCOLUMN71(util.ReplaceNull(rs.getString("COLUMN71")));
		obj.setCOLUMN72(util.ReplaceNull(rs.getString("COLUMN72")));
		obj.setCOLUMN73(util.ReplaceNull(rs.getString("COLUMN73")));
		obj.setCOLUMN74(util.ReplaceNull(rs.getString("COLUMN74")));
		obj.setCOLUMN75(util.ReplaceNull(rs.getString("COLUMN75")));
		obj.setCOLUMN76(util.ReplaceNull(rs.getString("COLUMN76")));
		obj.setCOLUMN77(util.ReplaceNull(rs.getString("COLUMN77")));
		obj.setCOLUMN78(util.ReplaceNull(rs.getString("COLUMN78")));
		obj.setCOLUMN79(util.ReplaceNull(rs.getString("COLUMN79")));
		obj.setCOLUMN80(util.ReplaceNull(rs.getString("COLUMN80")));
		obj.setCOLUMN81(util.ReplaceNull(rs.getString("COLUMN81")));
		obj.setCOLUMN82(util.ReplaceNull(rs.getString("COLUMN82")));
		obj.setCOLUMN83(util.ReplaceNull(rs.getString("COLUMN83")));
		obj.setCOLUMN84(util.ReplaceNull(rs.getString("COLUMN84")));
		obj.setCOLUMN85(util.ReplaceNull(rs.getString("COLUMN85")));
		obj.setCOLUMN86(util.ReplaceNull(rs.getString("COLUMN86")));
		obj.setCOLUMN87(util.ReplaceNull(rs.getString("COLUMN87")));
		obj.setCOLUMN88(util.ReplaceNull(rs.getString("COLUMN88")));
		obj.setCOLUMN89(util.ReplaceNull(rs.getString("COLUMN89")));
		obj.setCOLUMN90(util.ReplaceNull(rs.getString("COLUMN90")));
		obj.setCOLUMN91(util.ReplaceNull(rs.getString("COLUMN91")));
		obj.setCOLUMN92(util.ReplaceNull(rs.getString("COLUMN92")));
		obj.setCOLUMN93(util.ReplaceNull(rs.getString("COLUMN93")));
		obj.setCOLUMN94(util.ReplaceNull(rs.getString("COLUMN94")));
		obj.setCOLUMN95(util.ReplaceNull(rs.getString("COLUMN95")));
		obj.setCOLUMN96(util.ReplaceNull(rs.getString("COLUMN96")));
		obj.setCOLUMN97(util.ReplaceNull(rs.getString("COLUMN97")));
		obj.setCOLUMN98(util.ReplaceNull(rs.getString("COLUMN98")));
		obj.setCOLUMN99(util.ReplaceNull(rs.getString("COLUMN99")));
		obj.setCOLUMN100(util.ReplaceNull(rs.getString("COLUMN100")));
		
		
		return obj;
	}
}


public class Users_list_mapping implements RowMapper<Users0001>{

	Common_Utils utils = new Common_Utils();
	public Users0001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users0001 u1 = new Users0001();
		
		u1.setROWNUM(utils.ReplaceNull(rs.getString("ROWNUM")));
		u1.setUSERSCD(utils.ReplaceNull(rs.getString("USERSCD")));
		u1.setUNAME(utils.ReplaceNull(rs.getString("UNAME")));
		u1.setSUBORGCODE(utils.ReplaceNull(rs.getString("SUBORGCODE")));
		u1.setROLECD(utils.ReplaceNull(rs.getString("ROLECD")));
		u1.setLast_Login_date(utils.ReplaceNull(rs.getString("Last_Login_date")));
		u1.setACCOUNT_TYPE(utils.ReplaceNull(rs.getString("ACCOUNT_TYPE")));
		u1.setSTATUS(utils.ReplaceNull(rs.getString("STATUS")));
		
		return u1;
	}
	
}

public class Audit_report_mapping implements RowMapper<Users0001>{

	Common_Utils utils = new Common_Utils();
	public Users0001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users0001 u1 = new Users0001();
		
		u1.setROWNUM(utils.ReplaceNull(rs.getString("ROWNUM")));
		u1.setUSERSCD(utils.ReplaceNull(rs.getString("USERSCD")));
		u1.setUNAME(utils.ReplaceNull(rs.getString("UNAME")));
		u1.setRequest_Type(utils.ReplaceNull(rs.getString("Request_Type")));
		u1.setOld_value(utils.ReplaceNull(rs.getString("Old_value")));
		u1.setNew_value(utils.ReplaceNull(rs.getString("New_value")));
		u1.setACTION_DATE_TIME(utils.ReplaceNull(rs.getString("ACTION_DATE_TIME")));
		
		return u1;
	}
	
}


public class Security_report_mapping implements RowMapper<Users0001>{

	Common_Utils utils = new Common_Utils();
	public Users0001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users0001 u1 = new Users0001();
		
		u1.setROWNUM(utils.ReplaceNull(rs.getString("ROWNUM")));
		u1.setUSERSCD(utils.ReplaceNull(rs.getString("USERSCD")));
		u1.setUNAME(utils.ReplaceNull(rs.getString("UNAME")));
		u1.setRequest_Type(utils.ReplaceNull(rs.getString("Request_Type")));
		u1.setOld_value(utils.ReplaceNull(rs.getString("Old_value")));
		u1.setNew_value(utils.ReplaceNull(rs.getString("New_value")));
		
		return u1;
	}
	
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


