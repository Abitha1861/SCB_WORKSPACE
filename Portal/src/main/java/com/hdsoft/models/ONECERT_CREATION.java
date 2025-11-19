package com.hdsoft.models;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Users00001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Token_System;
import com.zaxxer.hikari.HikariDataSource;


//MUKESH LAST FILE UPDATE  - 26-09-2024 01:05 PM
//MUKESH LAST FILE UPDATE  - 26-09-2024 02:22 PM
//MUKESH LAST FILE UPDATE  - 27-09-2024 04:05 PM
//MUKESH LAST FILE UPDATE  - 21-10-2024 06:35 PM
//MUKESH LAST FILE UPDATE  - 22-10-2024 03:43 PM
//MUKESH LAST FILE UPDATE  - 23-10-2024 07:18 PM

//IAM Generic Admin IDs for User Management

@Component
public class ONECERT_CREATION {
	
	
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Token_System tk;
	
	public JsonObject Account_creation(Users00001 Info, HttpSession session) 
	   {
		
		JsonObject details = new JsonObject();
		
		Common_Utils utils = new Common_Utils();
		
		try
		{	
			 String curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			 LocalDateTime now = LocalDateTime.now().withNano(0);
		     Timestamp Curr_date = Timestamp.valueOf(now);
				
		     String ACDESC = "";
		     
		     if(Info.getUSERTYPE().equalsIgnoreCase("Generic"))
		     {
		    	 ACDESC = "IAM Generic Admin IDs for User Management";
		     }
			
			 String sql = "Insert into Users00001(SUBORGCODE, USERSCD, UNAME, USERTYPE, USERSTS, ROLECD, REGDATE, REGTYPE,ACDESC) values(?,?,?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] {Info.getSUBORGCODE(), Info.getUSERSCD(), Info.getUNAME(), Info.getUSERTYPE(), Info.getUSERSTS(),
					 								Info.getROLECD(), curr_date, "APPLICATION", ACDESC});
			 
			 String sesUserName = (String)session.getAttribute("sesUserName");
			 
             sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
			 
			 Jdbctemplate.update(sql, new Object[] { sesUserName,Info.getUSERSCD(), "Create", "NA", "NA", "NA", Curr_date });
			 
			 details.addProperty("StatusCode", "200");
			 details.addProperty("Status", "Success");
			 details.addProperty("StatusMessage", "User account " + Info.getUSERSCD() + " is successfully created");
			 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", e.getLocalizedMessage());   	
			 
		 }
		
		 return details;
	}
	
	
	public JsonObject Onecert_Account_creation(String Body_MSG, JsonObject Headers) 
	   {
		
		JsonObject details = new JsonObject();
		
		Common_Utils utils = new Common_Utils();
				
		try
		{	
			String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
	        Timestamp curr_date = Timestamp.valueOf(now);
			
			
			JsonObject Request = utils.StringToJsonObject(Body_MSG);	
			
			System.out.println("Headers - " + Headers);
			System.out.println("Request - " + Request);
						
			String Suborg = sys.getSuborgcode();
			String Acc_Nm = Request.get("accountName").getAsString();
			String Acc_Own = Request.get("accountOwner").getAsString();
			String Acc_Type = Request.get("accountType").getAsString();
			String EmlAd = Request.get("emailAddress").getAsString();
			String Acc_sts = Request.get("accountStatus").getAsString();
			String Acc_Des = Request.get("accountDescription").getAsString();
			//String Is_prvl = Request.get("isPrivileged").getAsString();
			//String Lst_lgn = Request.get("lastLogin").getAsString();
			JsonArray roles = Request.get("roles").getAsJsonArray();
			String roleName = "";
			if(roles.size() > 0)
			{
				roleName = roles.get(0).getAsJsonObject().get("roleName").getAsString();
			}
			
			// To find out the request whether is send FROM APPLICATION or API SERVICE 
			String REGTYPE = Headers.get("ReqType").getAsString();  //
			
			String sesUserId = Headers.has("sesUserId") ? Headers.get("sesUserId").getAsString() : ""; 
			
			if(REGTYPE.equals("APPLICATION"))
			{
				REGTYPE = "APPLICATION";
			}
			else
			{
				REGTYPE = "API SERVICE";
			}
			
			String sql_cnt = "select count(*) from Users00001 where USERSCD = ?";
			 
			int count = Jdbctemplate.queryForObject(sql_cnt, new Object[] {Acc_Nm}, Integer.class);
			
			 if(count == 0)
			 {
				 String sql = "Insert into Users00001(SUBORGCODE, USERSCD, UNAME, USERTYPE, EMAILADD, USERSTS, ROLECD, REGDATE, REGTYPE, ACDESC) values(?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] {Suborg, Acc_Nm, Acc_Own, Acc_Type, EmlAd, Acc_sts, roleName, Curr_date, REGTYPE, Acc_Des });
						 	
				 details.addProperty("StatusCode", "200");
				 details.addProperty("Status", "Success");
				 details.addProperty("StatusMessage", "User account " + Acc_Nm + " got successfully created!.");
				 
                 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { sesUserId, Acc_Nm, "Create", "NA", "NA", "NA", curr_date});
				 
				 
				 //String sql_cnt1 = "select count(distinct(USERID_ROLE)) from menu005 where USERID_ROLE = ?";
				 
				//int rolenm_cnt = Jdbctemplate.queryForObject(sql_cnt1, new Object[] {roleName}, Integer.class);
					
				 
				 //if(rolenm_cnt == 1) {				 
				 
				/*	 
				 
				 }
				 else {
					//Application does not contain the requested entitlement CAPTURE_ENTITLEMENT_NAME_HERE to perform create.
					 
					 details.addProperty("StatusCode", "404");
					 details.addProperty("Status", "Failure");
					 details.addProperty("StatusMessage", "Application does not contain the requested entitlement " + roleName + " to perform create.");
				 }
				 */
			 }
			 else 
			 {
				 //Duplicate accountName
				 details.addProperty("StatusCode", "400");
				 details.addProperty("Status", "Failure");
				 details.addProperty("StatusMessage", "User account " + Acc_Nm + " already exists!.");
				 
			 }
			
			 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
		 }
		
		 return details;
	}
	
	public JsonObject Insert_Field_validataion_data(Users00001 Info, String sesUserId) 
	   {
		
		JsonObject details = new JsonObject();
		
		Common_Utils utils = new Common_Utils();
		
		//   , SUBORGCODE, REPORT_FIELD_CODE, REPORT_FIELD_NAME, REPORT_FIELD_SIZE, REPORT_FIELD_VMETHOD, REPORT_FIELD_CATEGORY, REPORT_FIELD_FETCH_REQD
		try
		{	
			
			String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
	        Timestamp curr_date = Timestamp.valueOf(now);
			
			
			String sql = "select count(*) from Users00001 where USERSCD = ? ";
			
			int coutn = Jdbctemplate.queryForObject(sql, new Object[] {Info.getUSERSCD()}, Integer.class);
			
			
			if(coutn != 0) {
				
				//System.out.println(i);
				
                 sql = "select * from Users00001 where USERSCD = ?";
				 
				 List<Users00001> old=Jdbctemplate.query(sql, new Object[] {Info.getUSERSCD()},new Users00001_Audit_trail_Mapper() );
				 			
				 sql = "update Users00001 set UNAME = ?, USERTYPE = ?, EMAILADD = ?, ROLECD = ?, USERSTS = ? where SUBORGCODE = ? and USERSCD = ?";
				 
				 Jdbctemplate.update(sql, new Object[] {Info.getUNAME(),Info.getUSERTYPE(),Info.getEMAILADD(),Info.getROLECD(),Info.getUSERSTS(),"SCB",Info.getUSERSCD()});
				 
                 if(old.size() !=0 && !old.get(0).getEMAILADD().equals(Info.getEMAILADD())) {
					 
					 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] {sesUserId,Info.getUSERSCD(), "Modify", "Email", old.get(0).getEMAILADD(), Info.getEMAILADD(),  curr_date});
					 
				 }
				  if(old.size() !=0 && !old.get(0).getUNAME().equals(Info.getUNAME())) {
                	 
                     sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { sesUserId ,Info.getUSERSCD(), "Modify", "Account Name", old.get(0).getUNAME(), Info.getUNAME(),  curr_date});
					 
				 }
				 if(old.size() !=0 && !old.get(0).getUSERTYPE().equals(Info.getUSERTYPE())) {
                	 
                     sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] {sesUserId,Info.getUSERSCD(), "Modify", "Account Type", old.get(0).getUSERTYPE(), Info.getUSERTYPE(),  curr_date});
					 
					 
					 
				 }
				  if(old.size() !=0 && !old.get(0).getROLECD().equals(Info.getROLECD())) {
                     
                	 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] {sesUserId,Info.getUSERSCD(), "Modify", "Role code", old.get(0).getROLECD(), Info.getROLECD(),  curr_date});
					 
					 
					 
				 }
				 if(old.size() !=0 && !old.get(0).getUSERSTS().equals(Info.getUSERSTS())) {
                	 
                     sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] {sesUserId,Info.getUSERSCD(), "Modify", "Account Status", old.get(0).getUSERSTS(), Info.getUSERSTS(),  curr_date});
					 
				 }
				
				 details.addProperty("Result", "Success");
				 details.addProperty("Message", "Account Updated Successfully!!");
				 
			}else {
				
				 //sql = "Insert into Users00001(SUBORGCODE, REPORT_FIELD_CODE, REPORT_FIELD_NAME, REPORT_FIELD_SIZE, REPORT_FIELD_VMETHOD, REPORT_FIELD_CATEGORY) values(?,?,?,?,?,?)";
				 
				 //Jdbctemplate.update(sql, new Object[] {"SCB",Info.getREPORT_FIELD_CODE(),Info.getREPORT_FIELD_NAME(),Info.getREPORT_FIELD_SIZE(),Info.getREPORT_FIELD_VMETHOD(),Info.getREPORT_FIELD_CATEGORY()});
				 				 
				 details.addProperty("Result", "Success");
				 details.addProperty("Message", "Account Added Successfully!!");
				
			}
				
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());   	
			 
		 }
		
		
		 return details;
	}
	
	
	public JsonObject Add_Entitlement(String Body_MSG, JsonObject Headers) 
	   {
		
		JsonObject details = new JsonObject();
		
		Common_Utils utils = new Common_Utils();
				
		try
		{	
			String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
	        Timestamp curr_date = Timestamp.valueOf(now);
			
			
			JsonObject Request = utils.StringToJsonObject(Body_MSG);	
									
			String Suborg = sys.getSuborgcode();
			String Acc_Nm = Request.get("accountName").getAsString();
			JsonArray roles = Request.get("roles").getAsJsonArray();
			String roleName = "";
			if(roles.size() > 0)
			{
				roleName = roles.get(0).getAsJsonObject().get("roleName").getAsString();
			}
			
			String sql_cnt = "select count(*) from Users00001 where USERSCD = ?";
			 
			int count = Jdbctemplate.queryForObject(sql_cnt, new Object[] {Acc_Nm}, Integer.class);
			
			 if(count != 0)
			 {
				    String sql_rolechk = "select count(distinct(USERID_ROLE)) from menu005 where USERID_ROLE = ?";
				 
				    int roleck_cnt = Jdbctemplate.queryForObject(sql_rolechk, new Object[] {roleName}, Integer.class);
					
				 
					 if(roleck_cnt == 1) {
						 
						    String sql_cnt2  = "select count(*) from Users00001 where USERSCD = ? and ROLECD = ?";
						 
							int role_cnt = Jdbctemplate.queryForObject(sql_cnt2, new Object[] {Acc_Nm, roleName}, Integer.class);
							
							 if(role_cnt == 0)
							 {
								 
                                 String sql = "select * from Users00001 where USERSCD = ?";
								 
								 List<Users00001> old=Jdbctemplate.query(sql, new Object[] {Acc_Nm},new Users00001_Audit_trail_Mapper() );
								 
								 
								 
								  sql = "Update Users00001 set ROLECD = ? where SUBORGCODE = ? and  USERSCD = ?";
								 
								 Jdbctemplate.update(sql, new Object[] {roleName, Suborg, Acc_Nm});
								 
                                  if(old.size() !=0 && !old.get(0).getROLECD().equals(roleName)) {
                                	  
                                	 sql = "select userid from channel001 where chcode = ?";
                     				 
                      				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
                      				 
                      				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
									 
									 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
									 
									 Jdbctemplate.update(sql, new Object[] {sesUserId,Acc_Nm, "Modify", "Role code", old.get(0).getROLECD(),roleName,  curr_date});
									 
									 
								 }
								 
										 	
								 details.addProperty("StatusCode", "200");
								 details.addProperty("Status", "Success");
								 details.addProperty("StatusMessage", "Entitlement " + roleName + " added to the user account " + Acc_Nm +" successfully.");
							 }
							 else 
							 {
								 
								//Duplicate accountName
								 details.addProperty("StatusCode", "400");
								 details.addProperty("Status", "Failure");
								 details.addProperty("StatusMessage", "User account " + Acc_Nm + " already associated with the requested entitlement "+roleName);
							 }
						 
					 }					 
					 else {
						 
						//Application does not contain the requested entitlement CAPTURE_ENTITLEMENT_NAME_HERE to perform create.
						 
						 details.addProperty("StatusCode", "400");
						 details.addProperty("Status", "Failure");
						 details.addProperty("StatusMessage", "Application does not contain the requested entitlement " + roleName + " to perform create.");
						 
					 }
				 
			 }
			 else 
			 {
				 //Not found accountName
				 details.addProperty("StatusCode", "404");
				 details.addProperty("Status", "Failure");
				 details.addProperty("StatusMessage", "User account " + Acc_Nm + " doesn't exist to add entitlement.");
			 }
			
			 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
		 }
		
		 return details;
	}

	public JsonObject Rolechg_User_Id_Check(String Search_Word, HttpServletRequest request) 
	{   	
		JsonObject User_Ids = new JsonObject();
		
		try
		{
			String sql = "SELECT COUNT(USERSCD) FROM USERS00001 where USERSCD = ?";                                                
			
			int count = Jdbctemplate.queryForObject(sql, new Object[] {Search_Word}, Integer.class);
			
			if(count != 0) {
				
				User_Ids.addProperty("Result", "Success");
				User_Ids.addProperty("Message", "Record  Available In Database");
				
				
			}else {
				User_Ids.addProperty("Result", "Failed");
				User_Ids.addProperty("Message", "Record Not Available In Database");
				
				
			}
			
			
		}
		catch(Exception ex)
		{
			User_Ids.addProperty("Result", "Failed");
			User_Ids.addProperty("Message", ex.getLocalizedMessage());
		}
		
		return User_Ids;
	}
	
	
	//Enable  Disable User
	public JsonObject Enable_Disable_user(String Enable_disable, String acc_name){
		
		JsonObject details = new JsonObject();
				
		String Suborg = sys.getSuborgcode();
		
		Common_Utils utils = new Common_Utils();
		
		try
		{	
			String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
	        Timestamp curr_date = Timestamp.valueOf(now);
			
			
			String sql_user  = "select count(*) from users00001 where USERSCD = ?";
			 
			int user_cnt = Jdbctemplate.queryForObject(sql_user, new Object[] {acc_name}, Integer.class);
			
			 if(user_cnt != 0)  //registered users
			 {			
				System.out.println("registered Users " + Enable_disable + " "+ acc_name);
				  
				String sql_cnt  = "select count(*) from users012 where upwdinv_user_id = ? ";
				 
				int cnt = Jdbctemplate.queryForObject(sql_cnt, new Object[] {acc_name}, Integer.class);
				
				 if(cnt == 0)  //Not Present in users012
				 {					 
						if(Enable_disable.equals("disable")) 
						{				 
							 String sql = "Insert into users012(UPWDINV_USER_ID, SUBORGCODE, UPWDINV_INVALID_COUNT) values(?,?,?)";
							 
							 Jdbctemplate.update(sql, new Object[] {acc_name, Suborg, "5"});
							 
                             sql = "select * from Users00001 where USERSCD = ?";
							 
							 List<Users00001> old=Jdbctemplate.query(sql, new Object[] {acc_name},new Users00001_Audit_trail_Mapper() );
							 
							 String sql1 = "Update users00001 set USERSTS = 'Disabled' where SUBORGCODE = ? and USERSCD = ?";
							 
							 Jdbctemplate.update(sql1, new Object[] {Suborg, acc_name});
							 
                             if(!old.get(0).getROLECD().equals("Disabled")) {
                            	 
                            	 sql = "select userid from channel001 where chcode = ?";
                				 
                 				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
                 				 
                 				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
								 
								 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
								 
								 Jdbctemplate.update(sql, new Object[] {sesUserId,acc_name, "Modify", "Role code", old.get(0).getROLECD(),"Disabled",  curr_date});
							 }
							 else {
								 
							 }
									 	
							 details.addProperty("StatusCode", "200");
							 details.addProperty("Status", "Success");
							 details.addProperty("StatusMessage", "User account " + acc_name + " is disabled.");
						 
						 }
						
						 if(Enable_disable.equals("enable")) 
						 {
							 details.addProperty("StatusCode", "400");
							 details.addProperty("Status", "Failure");
							 details.addProperty("StatusMessage", "User account " + acc_name + " requested to enable is not disabled!.");
						 }			
				 }
				 else  //Present in users012 
				 { 
					    String sql_cnt2  = "select count(*) from users012 where upwdinv_user_id = ? and upwdinv_invalid_count >= ? ";
					 
						int cnt2 = Jdbctemplate.queryForObject(sql_cnt2, new Object[] {acc_name, "5"}, Integer.class);
					 
						if(cnt2 == 0) {  //Not Present in users012 with upwdinv_invalid_count >=5
														
							 String sql1 = "delete FROM users012 where upwdinv_user_id = ? and SUBORGCODE = ?";
							 Jdbctemplate.update(sql1, new Object[] {acc_name, Suborg});
							 
                             sql1 = "select * from Users00001 where USERSCD = ?";
							 
							 List<Users00001> old=Jdbctemplate.query(sql1, new Object[] {acc_name},new Users00001_Audit_trail_Mapper() );
							 
							 
							 
							 String sql2 = "Update users00001 set USERSTS = 'Active' where SUBORGCODE = ? and USERSCD = ?";
							 
							 Jdbctemplate.update(sql2, new Object[] {Suborg, acc_name});
							 
							 if(!old.get(0).getROLECD().equals("Active")) {
								 	
								 String sql = "select userid from channel001 where chcode = ?";
                				 
                 				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
                 				 
                 				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
                 				 
								sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
									 
									 Jdbctemplate.update(sql, new Object[] { sesUserId,acc_name, "Modify", "Role code", old.get(0).getROLECD(),"Active",  curr_date});
									 
									 
								 }
								 else {
									 
								 }
								 
					 
							 if(Enable_disable.equals("disable")) 
							 {	
								 String sql = "Insert into users012(UPWDINV_USER_ID, SUBORGCODE, UPWDINV_INVALID_COUNT) values(?,?,?)";
								 
								 Jdbctemplate.update(sql, new Object[] {acc_name, Suborg, "5"});
								 
                                 sql = "select * from Users00001 where USERSCD = ?";
								 
								 List<Users00001> olds=Jdbctemplate.query(sql, new Object[] {acc_name},new Users00001_Audit_trail_Mapper() );
								 
								 
								 String sql3 = "Update users00001 set USERSTS = 'Disabled' where SUBORGCODE = ? and USERSCD = ?";
								 
								 Jdbctemplate.update(sql3, new Object[] {Suborg, acc_name});
								 
                                 if(!olds.get(0).getROLECD().equals("Disabled")) {
                                	 
                                	 sql = "select userid from channel001 where chcode = ?";
                     				 
                      				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
                      				 
                      				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
									 
									 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
									 
									 Jdbctemplate.update(sql, new Object[] {sesUserId,acc_name, "Modify", "Role code", olds.get(0).getROLECD(),"Disabled",  curr_date});
									 
									 
								 }
								 else {
									 
								 }		 	
								 details.addProperty("StatusCode", "200");
								 details.addProperty("Status", "Success");
								 details.addProperty("StatusMessage", "User account " + acc_name + " is disabled.");
							 
							 }
							
							 if(Enable_disable.equals("enable")) 
							 {
								 
								 details.addProperty("StatusCode", "400");
								 details.addProperty("Status", "Failure");
								 details.addProperty("StatusMessage", "User account " + acc_name + " requested to enable is not disabled!.");
							 }	
					
						}
						else //Present in users012 with upwdinv_invalid_count >=5
						{
							 
							 if(Enable_disable.equals("disable")) 
							 {	
								 
								 details.addProperty("StatusCode", "400");
								 details.addProperty("Status", "Failure");
								 details.addProperty("StatusMessage", "User account " + acc_name + " requested to disable is already in disabled status!.");
							 
							 }
							 
							 if(Enable_disable.equals("enable")) 
							 {
								 
								 String sql = "delete FROM users012 where upwdinv_user_id = ? and SUBORGCODE = ?";
								 
								 Jdbctemplate.update(sql, new Object[] {acc_name, Suborg});
								 
                                 sql = "select * from Users00001 where USERSCD = ?";
								 
								 List<Users00001> older=Jdbctemplate.query(sql, new Object[] {acc_name},new Users00001_Audit_trail_Mapper() );
								 
								 
								 String sql2 = "Update users00001 set USERSTS = 'Active' where SUBORGCODE = ? and USERSCD = ?";
								 
								 Jdbctemplate.update(sql2, new Object[] {Suborg, acc_name});
								 
                                 if(!older.get(0).getROLECD().equals("Active")) {
                                	 
                                	 sql = "select userid from channel001 where chcode = ?";
                     				 
                      				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
                      				 
                      				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
									 
									 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
									 
									 Jdbctemplate.update(sql, new Object[] { sesUserId ,acc_name, "Modify", "Role code", older.get(0).getROLECD(),"Active",  curr_date});
									 
									 
								 }
								 else {
									 
								 }
										 	
								 details.addProperty("StatusCode", "200");
								 details.addProperty("Status", "Success");
								 details.addProperty("StatusMessage", "User account " + acc_name + " is enabled.");
							 }	
							
						}
					 
				 }
				 
			 }else //Non registered users
			 {
				 details.addProperty("StatusCode", "400");
				 details.addProperty("Status", "Failure");
				 details.addProperty("StatusMessage", "User account " + acc_name + " doesn't exist to " + Enable_disable);
			 }	
				 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
		 }
		
		 return details;
	}
	
	
	
	
public JsonObject deleteAccount( String acc_name){
			
		JsonObject details = new JsonObject();
				
		String Suborg = sys.getSuborgcode();
		
		Common_Utils utils = new Common_Utils();
		
			try
			{	
				//String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
				
				LocalDateTime now = LocalDateTime.now().withNano(0);
		        Timestamp curr_date = Timestamp.valueOf(now);
				
				
				String sql_user  = "select count(*) from users00001 where USERSCD = ? and USERSTS=?";
				
				int del_cnt = Jdbctemplate.queryForObject(sql_user, new Object[] {acc_name, "Deleted" }, Integer.class);
				
				if(del_cnt != 0)  //registered users
				{
					details.addProperty("StatusCode", "400");
					details.addProperty("Status", "Failure");
					details.addProperty("StatusMessage", "User account already deleted");
					
					return details;
				}
				
				sql_user  = "select count(*) from users00001 where USERSCD = ?";
				 
				int user_cnt = Jdbctemplate.queryForObject(sql_user, new Object[] {acc_name}, Integer.class);
				
				 if(user_cnt != 0)  //registered users
				 {
					 Common_Utils util = new Common_Utils();
					 
					 String sql = "insert into users00001_hst (select * from users00001 where USERSCD=?)";
					 
					 Jdbctemplate.update(sql, new Object[] { acc_name });  
					 
					 sql = "update users00001_hst set USERSTS=?, ADATE=? where USERSCD = ? and SUBORGCODE = ?";
					 
					 Jdbctemplate.update(sql, new Object[] { "Deleted", util.get_oracle_Timestamp(), acc_name, Suborg});
					 
					 sql = "delete from users00001 where USERSCD = ? and SUBORGCODE = ?";
					 
					 Jdbctemplate.update(sql, new Object[] { acc_name, Suborg});
					 
					 sql = "select userid from channel001 where chcode = ?";
     				 
      				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
      				 
      				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
					 
					 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { sesUserId ,acc_name, "Delete", "", "","",  curr_date});
								 	
					 details.addProperty("StatusCode", "200");
					 details.addProperty("Status", "Success");
					 details.addProperty("StatusMessage", "User account " + acc_name + " got deleted successfully.");
					 
				 }else //Non registered users
				 {
					 details.addProperty("StatusCode", "400");
					 details.addProperty("Status", "Failure");
					 details.addProperty("StatusMessage", "User account " + acc_name + " doesn't exist to perform delete operation");
				 }	
					 
			 }
			 catch(Exception e)
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
			 }
			
			 return details;
}
	
	

public JsonObject List_of_Account(int currentPage, int pageSize){
	
	
	JsonObject details = new JsonObject();
	
	 JsonArray accounts = new JsonArray();
	 
	  
		try
		{	
			if(currentPage ==0)
			{
				details.addProperty("StatusCode", "400");
				details.addProperty("Status", "Failure");
				details.addProperty("StatusMessage", "Invalid current page value, currentPage value must to start from 1.");
			}
			else 
			{
				//table_count
				String sql_user  = "select count(*) FROM users00001 order by USERSCD asc";
				 
				 int table_cnt = Jdbctemplate.queryForObject(sql_user, Integer.class);
				
				 if(table_cnt != 0)  //registered users
				 {
					 					 
					 //JSON_DATA
					 String sql =  "select USERSCD, UNAME, USERTYPE, USERSTS, EMAILADD, LASTLOGINDT, ROLECD, ACDESC, rnum, CEIL(rnum / ?) AS SegregatedGroup  FROM (select USERSCD, UNAME, USERTYPE, USERSTS, EMAILADD,\r\n"
					 		+ "NVL(TO_CHAR((select ULOGIN_IN_DATE FROM users008 where ULOGIN_USER_ID = USERSCD),'DD-MON-YYYY HH:MM:SS'),'01-Jan-1970 00:00:00') as LASTLOGINDT,\r\n"
					 		+ "ROLECD, ACDESC, rownum as rnum FROM users00001 order by USERSCD asc) Where CEIL(rnum / ?) = ?";  //IAM Generic Admin IDs for User Management
					 
					 List<Users00001> list_acc = Jdbctemplate.query(sql, new Object[]{pageSize, pageSize, currentPage}, new list_of_account_mapper());
					 					 
					 System.out.println("currentPage - " + currentPage + " pageSize - " + pageSize);
					 
					 String sql_cnt = "select count(*) "
					 				+ "FROM (select rownum as rnum FROM users00001 order by USERSCD asc) "
					 				+ "Where CEIL(rnum / ?) = ?";
					 
					 int Next_data_cnt = Jdbctemplate.queryForObject(sql_cnt, new Object[]{pageSize, currentPage + 1}, Integer.class);
					 
					 
					 System.out.println("Next_data_cnt " +Next_data_cnt);
						 
					 for(int index = 0; index<list_acc.size();index++)
						{
						     if(list_acc.get(index).getUSERSTS().equalsIgnoreCase("removed") || list_acc.get(index).getUSERSTS().equalsIgnoreCase("deleted"))
						     {
						    	  continue;
						     }
						     
							 JsonObject acc_detl = new JsonObject();
							
							 JsonArray accessRoles = new JsonArray();
							 
							 JsonObject roleName = new JsonObject();
							
							acc_detl.addProperty("accountName", list_acc.get(index).getUSERSCD());
							acc_detl.addProperty("accountOwner", list_acc.get(index).getUNAME());
							acc_detl.addProperty("accountType", list_acc.get(index).getUSERTYPE());
							acc_detl.addProperty("accountStatus", list_acc.get(index).getUSERSTS());
							acc_detl.addProperty("accountDescription", list_acc.get(index).getACDESC());
							acc_detl.addProperty("emailAddress", list_acc.get(index).getEMAILADD());
							
							//isPrivileged "Yes" or  "No"
							String isprivil  = list_acc.get(index).getROLECD().equals("ADMIN")  ?  "Yes" : "No" ;
							
							//System.out.println("isprivil -" + isprivil);
							 								
							acc_detl.addProperty("isPrivileged", isprivil);
							acc_detl.addProperty("lastLogin", list_acc.get(index).getLASTLOGINDT());
							roleName.addProperty("roleName", list_acc.get(index).getROLECD());
							accessRoles.add(roleName);
							acc_detl.add("accessRoles", accessRoles);
							accounts.add(acc_detl);
								
						}
					 					 
					 details.addProperty("totalCount", table_cnt);
					 details.addProperty("currentPage", currentPage);
					 details.addProperty("hasNext", Next_data_cnt == 0  ? "N" : "Y");
					 details.add("accounts", accounts);
					 
					 
				 }
			}
			
				 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage());   	 //e.getLocalizedMessage()
		 }
		
		 return details;
}

public JsonObject List_of_Account_bkp(String currentPage){
	
	
	JsonObject details = new JsonObject();
	
	 JsonArray accounts = new JsonArray();
	 	 				
		try
		{	
			if(currentPage.equals("0"))
			{
				details.addProperty("StatusCode", "400");
				details.addProperty("Status", "Failure");
				details.addProperty("StatusMessage", "Invalid current page value, currentPage value must to start from 1.");
			}
			else 
			{
				 //table_count
				 String sql_user  = "select count(*) FROM users00001 where USERSTS = 'Active' order by USERSCD asc";
				 
				 int table_cnt = Jdbctemplate.queryForObject(sql_user, Integer.class);
				
				 if(table_cnt != 0)  //registered users
				 {
					 //strt_no
					 String sql_strt_no  = "select (? * 10) - 9 as strt_no FROM dual";
				     int strt_no_cnt = Jdbctemplate.queryForObject(sql_strt_no, new Object[]{currentPage}, Integer.class);
				     
				     //end_no
					 String sql_end_no  = "select (? * 10) as end_no FROM dual";
				     int end_no_cnt = Jdbctemplate.queryForObject(sql_end_no, new Object[]{currentPage}, Integer.class);
				     
				     //strt_no
					 String sql_nxt_strt_no  = "select ((? +1) * 10) - 9 as strt_no FROM dual";
				     int nxt_strt_no_cnt = Jdbctemplate.queryForObject(sql_nxt_strt_no, new Object[]{currentPage}, Integer.class);
				     
				     //end_no
					 String nxt_sql_end_no  = "select ((? +1) * 10) as end_no FROM dual";
				     int nxt_end_no_cnt = Jdbctemplate.queryForObject(nxt_sql_end_no, new Object[]{currentPage}, Integer.class);
					 
					 //hasNext "Y" or  "N"
					 String hasNext_sql = "select decode(count(*),0,'N','Y') as hasNext FROM (select rownum as rnum,aa.* from Users00001 aa"
					 					+ " where USERSTS = 'Active') where rnum between ? and ?"; 
					 
					 String hasNext_val = Jdbctemplate.queryForObject(hasNext_sql,new Object[]{nxt_strt_no_cnt, nxt_end_no_cnt}, String.class);
					 
					 
					 //JSON_DATA
					 String sql =  "select * FROM (select USERSCD, UNAME, USERTYPE, USERSTS,"
					 		+ "NVL(TO_CHAR((select ULOGIN_IN_DATE FROM users008 where ULOGIN_USER_ID = USERSCD),'DD-MON-YYYY HH:MM:SS'),'01-Jan-1970 00:00:00') as LASTLOGINDT,"
					 		+ "ROLECD, ACDESC, rownum as rnum FROM users00001 where USERSTS = 'Active' order by USERSCD asc)"
					 		+ "where rnum between ? and ?";
					 
					 List<Users00001> list_acc = Jdbctemplate.query(sql, new Object[]{strt_no_cnt, end_no_cnt}, new list_of_account_mapper());
					 					 
					 System.out.println("currentPage - " + currentPage + " nxt_strt_no_cnt - " + nxt_strt_no_cnt + " nxt_end_no_cnt - " + nxt_end_no_cnt);
						 
					 for(int index = 0; index<list_acc.size();index++)
						{
						 
							 JsonObject acc_detl = new JsonObject();
							
							 JsonArray accessRoles = new JsonArray();
							 
							 JsonObject roleName = new JsonObject();
							
							    acc_detl.addProperty("account_serial_no", index);
								acc_detl.addProperty("accountName", list_acc.get(index).getUSERSCD());
								acc_detl.addProperty("accountOwner", list_acc.get(index).getUNAME());
								acc_detl.addProperty("accountType", list_acc.get(index).getUSERTYPE());
								acc_detl.addProperty("accountStatus", list_acc.get(index).getUSERSTS());
								acc_detl.addProperty("accountDescription", list_acc.get(index).getACDESC());
								
								//isPrivileged "Yes" or  "No"
								String isprivil  = list_acc.get(index).getROLECD().equals("ADMIN")  ?  "Yes" : "No" ;
								
								//System.out.println("isprivil -" + isprivil);
								 								
								acc_detl.addProperty("isPrivileged", isprivil);
								acc_detl.addProperty("lastLogin", list_acc.get(index).getLASTLOGINDT());
								roleName.addProperty("roleName", list_acc.get(index).getROLECD());
								accessRoles.add(roleName);
								acc_detl.add("accessRoles", accessRoles);
								accounts.add(acc_detl);
								
						}
					 					 
					 details.addProperty("totalCount", table_cnt);
					 details.addProperty("currentPage", currentPage);
					 details.addProperty("hasNext", hasNext_val);
					 details.add("accounts", accounts);
					 
				 }
			}
			
				 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage());   	 //e.getLocalizedMessage()
		 }
		
		 return details;
}

public JsonObject Single_Account(String acc_name){
	
	JsonObject details = new JsonObject();
	
	//JsonArray accounts = new JsonArray();
				
		try
		{	
			String sql_user  = "select count(*) from users00001 where USERSCD = ?";
			 
			int user_cnt = Jdbctemplate.queryForObject(sql_user, new Object[] {acc_name}, Integer.class);
			
			 if(user_cnt != 0)  //registered users
			 {
				 				 				 
				 //JSON_DATA
				 String sql =  "select * FROM (select USERSCD, UNAME, USERTYPE, USERSTS, EMAILADD,\r\n"
				 		+ "NVL(TO_CHAR((select ULOGIN_IN_DATE FROM users008 where ULOGIN_USER_ID = USERSCD),'DD-MON-YYYY HH:MM:SS'),'01-Jan-1970 00:00:00') as LASTLOGINDT,\r\n"
				 		+ " ROLECD, ACDESC, rownum as rnum FROM users00001 where USERSCD = ?  order by USERSCD asc)";
				 
				 List<Users00001> list_acc = Jdbctemplate.query(sql, new Object[]{acc_name}, new Single_account_mapper());
				 					 					 
				 for(int index = 0; index<list_acc.size();index++)
					{
					 
						 if(list_acc.get(index).getUSERSTS().equalsIgnoreCase("removed") || list_acc.get(index).getUSERSTS().equalsIgnoreCase("deleted"))
					     {
					    	  continue;
					     }
					 
					    //isPrivileged "Yes" or  "No"
						String isprivil  = list_acc.get(index).getROLECD().equals("ADMIN")  ?  "Yes" : "No" ; //System.out.println("isprivil -" + isprivil);
					 
						 //JsonObject acc_detl = new JsonObject();
						
						 JsonArray accessRoles = new JsonArray();
						 
						 JsonObject roleName = new JsonObject();
						
						 	details.addProperty("accountName", list_acc.get(index).getUSERSCD());
						 	details.addProperty("accountOwner", list_acc.get(index).getUNAME());
						 	details.addProperty("accountType", list_acc.get(index).getUSERTYPE());
						 	details.addProperty("accountStatus", list_acc.get(index).getUSERSTS());
						 	details.addProperty("emailAddress", list_acc.get(index).getEMAILADD());	
						 	details.addProperty("accountDescription", list_acc.get(index).getACDESC());							
							details.addProperty("isPrivileged", isprivil);
							details.addProperty("lastLogin", list_acc.get(index).getLASTLOGINDT());
							roleName.addProperty("roleName", list_acc.get(index).getROLECD());
							accessRoles.add(roleName);
							details.add("accessRoles", accessRoles);
							//accounts.add(acc_detl);
							
					}
				 					 
				 //details.add("accounts", accounts);
				 
				 
			 }else //Non registered users
			 {
				 details.addProperty("StatusCode", "404");
				 details.addProperty("Status", "Failure");
				 details.addProperty("StatusMessage", "User account not found with the account name : " + acc_name);
				 
			 }	
				 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
		 }
		
		 return details;
}




public JsonObject all_entitlement(String currentPage){
	
	
	JsonObject details = new JsonObject();
	
	 JsonArray entitlements = new JsonArray();
	 				
		try
		{	
			if(currentPage.equals("0"))
			{
				details.addProperty("StatusCode", "400");
				details.addProperty("Status", "Failure");
				details.addProperty("StatusMessage", "Invalid current page value, currentPage value must to start from 1.");
						
			}
			else 
			{
				//strt_no
				 String sql_strt_no  = "select (? * 10) - 9 as strt_no FROM dual";
			     int strt_no_cnt = Jdbctemplate.queryForObject(sql_strt_no, new Object[]{currentPage}, Integer.class);
			     
			     //end_no
				 String sql_end_no  = "select (? * 10) as end_no FROM dual";
			     int end_no_cnt = Jdbctemplate.queryForObject(sql_end_no, new Object[]{currentPage}, Integer.class);
			     
			     //strt_no
				 String sql_nxt_strt_no  = "select ((? +1) * 10) - 9 as strt_no FROM dual";
			     int nxt_strt_no_cnt = Jdbctemplate.queryForObject(sql_nxt_strt_no, new Object[]{currentPage}, Integer.class);
			     
			     //end_no
				 String nxt_sql_end_no  = "select ((? +1) * 10) as end_no FROM dual";
			     int nxt_end_no_cnt = Jdbctemplate.queryForObject(nxt_sql_end_no, new Object[]{currentPage}, Integer.class);
			     
				 System.out.println("currentPage - " + currentPage + " nxt_strt_no_cnt - " + nxt_strt_no_cnt + " nxt_end_no_cnt - " + nxt_end_no_cnt);
				
			   //Total_cnt
				String sql_user  = "select count(*)  FROM (\r\n"
								+ "select rownum as rnum, ROLECD, ROLE_DESC from (\r\n"
								+ "select distinct ROLECD,\r\n"
								+ "case when ROLECD = 'ADMIN' then 'ACCESS MANAGEMENT ACTIVITY' \r\n"
						 		+ "     when ROLECD = 'CHKR' then 'UPLOAD MODULE APPROVER' \r\n"
						 		+ "     when ROLECD = 'MAKR' then 'UPLOAD MODULE MAKER'\r\n"
						 		+ "     when ROLECD = 'APPSUPPORT' then 'PSS SUPPORT MEMBER FOR APPLICATION'\r\n"
						 		+ "     when ROLECD = 'REPORTVIEWER' then 'USERS HAVE ACCESS TO REPORT'\r\n"
						 		+ "     when ROLECD = 'DASHBRDVIEWER' then 'USERS HAVE ACCESS TO DASHBOARD'\r\n"
								+ "    Else null\r\n"
								+ "    End as ROLE_DESC  \r\n"
								+ "FROM users00001 where USERSTS = 'Active' order by ROLECD asc)) ";
						
					 
				 int table_cnt = Jdbctemplate.queryForObject(sql_user, Integer.class);
					
			     
				//hasNext
				String sql_has_nxt = "select count(*) FROM (\r\n"
									+ "select rownum as rnum, ROLECD, ROLE_DESC from (\r\n"
									+ "select distinct ROLECD,\r\n"
									+ "case when ROLECD = 'ADMIN' then 'ACCESS MANAGEMENT ACTIVITY' \r\n"
							 		+ "     when ROLECD = 'CHKR' then 'UPLOAD MODULE APPROVER' \r\n"
							 		+ "     when ROLECD = 'MAKR' then 'UPLOAD MODULE MAKER'\r\n"
							 		+ "     when ROLECD = 'APPSUPPORT' then 'PSS SUPPORT MEMBER FOR APPLICATION'\r\n"
							 		+ "     when ROLECD = 'REPORTVIEWER' then 'USERS HAVE ACCESS TO REPORT'\r\n"
							 		+ "     when ROLECD = 'DASHBRDVIEWER' then 'USERS HAVE ACCESS TO DASHBOARD'\r\n"
									+ "    Else null\r\n"
									+ "    End as ROLE_DESC  \r\n"
									+ "FROM users00001 where USERSTS = 'Active' order by ROLECD asc)) where rnum between ? and ?";
					 
				 int hasNext = Jdbctemplate.queryForObject(sql_has_nxt, new Object[]{nxt_strt_no_cnt, nxt_end_no_cnt}, Integer.class);
				 
				//hasNext "Y" or  "N"
				 String hasNext_count  = hasNext == 0  ?  "N" : "Y" ;
				
				 if(table_cnt != 0)  //registered users
				 {
															 
					 //JSON_DATA
					 String sql = "select ROLECD, ROLE_DESC FROM (\r\n"
						 		+ "select rownum as rnum, ROLECD, ROLE_DESC from (\r\n"
						 		+ "select distinct ROLECD,\r\n"
						 		+ "case when ROLECD = 'ADMIN' then 'ACCESS MANAGEMENT ACTIVITY' \r\n"
						 		+ "     when ROLECD = 'CHKR' then 'UPLOAD MODULE APPROVER' \r\n"
						 		+ "     when ROLECD = 'MAKR' then 'UPLOAD MODULE MAKER'\r\n"
						 		+ "     when ROLECD = 'APPSUPPORT' then 'PSS SUPPORT MEMBER FOR APPLICATION'\r\n"
						 		+ "     when ROLECD = 'REPORTVIEWER' then 'USERS HAVE ACCESS TO REPORT'\r\n"
						 		+ "     when ROLECD = 'DASHBRDVIEWER' then 'USERS HAVE ACCESS TO DASHBOARD'\r\n"
						 		+ "    Else null\r\n"
						 		+ "    End as ROLE_DESC  \r\n"
						 		+ "FROM users00001 where USERSTS = 'Active' order by ROLECD asc)) where rnum between ? and ?";
					 
								
					 
					 List<Users00001> list_Entil = Jdbctemplate.query(sql, new Object[]{strt_no_cnt, end_no_cnt}, new list_of_Entitlement_mapper());
					 					 
						 
					 for(int index = 0; index<list_Entil.size();index++)
						{
						 
							 JsonObject Entitl_detl = new JsonObject();
							 
							//isPrivileged "Yes" or  "No"
						     String isprivil  = list_Entil.get(index).getROLECD().equals("ADMIN")  ?  "Yes" : "No" ;			//System.out.println("isprivil -" + isprivil);
							
							 Entitl_detl.addProperty("entitlementName", list_Entil.get(index).getROLECD());
							 Entitl_detl.addProperty("entitlementDescription", list_Entil.get(index).getROLE_DESC());
							 Entitl_detl.addProperty("entitlementOwner", "1228482");  // Default value of Alex Moshi														
							 Entitl_detl.addProperty("isPrivileged", isprivil);
							 entitlements.add(Entitl_detl);
						}
					 					 
					 details.addProperty("totalCount", table_cnt);
					 details.addProperty("currentPage", currentPage);
					 details.addProperty("hasNext", hasNext_count);
					 details.add("entitlements", entitlements);
					 
				 }
			}
			
				 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage());   	 //e.getLocalizedMessage()
		 }
		
		 return details;
}


public JsonObject Single_entitlement(String Entitlement_name){
	
	JsonObject details = new JsonObject();
					
		try
		{	
			String sql_user = "select count(*) FROM (\r\n"
			 		+ "select rownum as rnum, ROLECD, ROLE_DESC from (\r\n"
			 		+ "select distinct ROLECD,\r\n"
			 		+ "case when ROLECD = 'ADMIN' then 'ACCESS MANAGEMENT ACTIVITY' \r\n"
			 		+ "     when ROLECD = 'CHKR' then 'UPLOAD MODULE APPROVER' \r\n"
			 		+ "     when ROLECD = 'MAKR' then 'UPLOAD MODULE MAKER'\r\n"
			 		+ "     when ROLECD = 'APPSUPPORT' then 'PSS SUPPORT MEMBER FOR APPLICATION'\r\n"
			 		+ "     when ROLECD = 'REPORTVIEWER' then 'USERS HAVE ACCESS TO REPORT'\r\n"
			 		+ "     when ROLECD = 'DASHBRDVIEWER' then 'USERS HAVE ACCESS TO DASHBOARD'\r\n"
			 		+ "    Else null\r\n"
			 		+ "    End as ROLE_DESC  \r\n"
			 		+ "FROM users00001 where USERSTS = 'Active' order by ROLECD asc)) where ROLECD = ?";
			 
			int user_cnt = Jdbctemplate.queryForObject(sql_user, new Object[] {Entitlement_name}, Integer.class);
			
			 if(user_cnt != 0)  //registered users
			 {
				//JSON_DATA
				 String sql = "select ROLECD, ROLE_DESC FROM (\r\n"
					 		+ "select rownum as rnum, ROLECD, ROLE_DESC from (\r\n"
					 		+ "select distinct ROLECD,\r\n"
					 		+ "case when ROLECD = 'ADMIN' then 'ACCESS MANAGEMENT ACTIVITY' \r\n"
					 		+ "     when ROLECD = 'CHKR' then 'UPLOAD MODULE APPROVER' \r\n"
					 		+ "     when ROLECD = 'MAKR' then 'UPLOAD MODULE MAKER'\r\n"
					 		+ "     when ROLECD = 'APPSUPPORT' then 'PSS SUPPORT MEMBER FOR APPLICATION'\r\n"
					 		+ "     when ROLECD = 'REPORTVIEWER' then 'USERS HAVE ACCESS TO REPORT'\r\n"
					 		+ "     when ROLECD = 'DASHBRDVIEWER' then 'USERS HAVE ACCESS TO DASHBOARD'\r\n"
					 		+ "    Else null\r\n"
					 		+ "    End as ROLE_DESC  \r\n"
					 		+ "FROM users00001 where USERSTS = 'Active' order by ROLECD asc)) where ROLECD = ?";
				 
				 
				 List<Users00001> single_Entil = Jdbctemplate.query(sql, new Object[]{Entitlement_name}, new Single_Entitlement_mapper());
				 					 
					 
				 for(int index = 0; index<single_Entil.size();index++)
					{
					 						 
						//isPrivileged "Yes" or  "No"
					     String isprivil  = single_Entil.get(index).getROLECD().equals("ADMIN")  ?  "Yes" : "No" ;			//System.out.println("isprivil -" + isprivil);
						
					     details.addProperty("StatusCode", "200");
					     details.addProperty("entitlementName", single_Entil.get(index).getROLECD());
					     details.addProperty("entitlementDescription", single_Entil.get(index).getROLE_DESC());
					     details.addProperty("entitlementOwner", "1228482");  // Default value of Alex Moshi														
					     details.addProperty("isPrivileged", isprivil);
					}
				 					 				 
				 
			 }else //Non registered users
			 {
				 details.addProperty("StatusCode", "404");
				 details.addProperty("Status", "Failure");
				 details.addProperty("StatusMessage", "Entitlement not found with the entitlement name : " + Entitlement_name);
				 
			 }	
				 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("StatusCode", "400");
			 details.addProperty("Result", "Failed");
			 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
		 }
		
		 return details;
}

public JsonObject Remove_entitlement(String Body_MSG, JsonObject Headers) 
{
	
	JsonObject details = new JsonObject();
	
	Common_Utils utils = new Common_Utils();
			
	try
	{	
		
		String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
		
		LocalDateTime now = LocalDateTime.now().withNano(0);
        Timestamp curr_date = Timestamp.valueOf(now);
		
		
		JsonObject Request = utils.StringToJsonObject(Body_MSG);	
								
		String Suborg = sys.getSuborgcode();
		String Acc_Nm = Request.get("accountName").getAsString();
		JsonArray accessRoles = Request.get("accessRoles").getAsJsonArray();
		String roleName = "";
		
		
		if(accessRoles.size() > 0)
		{
			roleName = accessRoles.get(0).getAsJsonObject().get("roleName").getAsString();
		}
		
		System.out.println("Suborg "+Suborg+" "+"Acc_Nm "+Acc_Nm+" "+"roleName "+roleName+" ");
		
		String sql_cnt = "select count(*) from Users00001 where USERSCD = ?";
		 
		int count = Jdbctemplate.queryForObject(sql_cnt, new Object[] {Acc_Nm}, Integer.class);
		
		 if(count != 0)
		 {
			    String sql_rolechk = "select count(distinct(USERID_ROLE)) from menu005 where USERID_ROLE = ?";
			 
			    int roleck_cnt = Jdbctemplate.queryForObject(sql_rolechk, new Object[] {roleName}, Integer.class);
				
			 
				 if(roleck_cnt == 1) {
					 
					    String sql_cnt2  = "select count(*) from Users00001 where USERSCD = ? and ROLECD = ?"; 
					 
						int role_cnt = Jdbctemplate.queryForObject(sql_cnt2, new Object[] {Acc_Nm, roleName}, Integer.class);
						
						 if(role_cnt == 0)
						 {
							 //Duplicate accountName
							 details.addProperty("StatusCode", "400");
							 details.addProperty("Status", "Failure");
							 details.addProperty("StatusMessage", "User account " + Acc_Nm + " does not associated with the "+roleName+ " entitlement to remove!");
						 }
						 else 
						 {
							 String sql_cnt3  = "select count(*) from Users00001 where USERSCD = ? and ROLECD = ? and USERSTS in ('Removed', 'Deleted')";  
							 
							 int role_cnt3 = Jdbctemplate.queryForObject(sql_cnt3, new Object[] {Acc_Nm, roleName}, Integer.class);
							 
							 if (role_cnt3 == 0) {
								 
								 Common_Utils util = new Common_Utils();
								 
								 String sql = "insert into users00001_hst (select * from users00001 where USERSCD=?)";
								 
								 Jdbctemplate.update(sql, new Object[] { Acc_Nm }); 
								 
                                 sql = "select * from Users00001 where USERSCD = ?";
								 
								 List<Users00001> older=Jdbctemplate.query(sql, new Object[] {Acc_Nm},new Users00001_Audit_trail_Mapper() );
								 
								 
								 sql = "update users00001_hst set USERSTS=?, ADATE=? where USERSCD = ? and SUBORGCODE = ?";
								 
								 Jdbctemplate.update(sql, new Object[] { "Deleted", util.get_oracle_Timestamp(), Acc_Nm, Suborg});
								 
                                 if(!older.get(0).getROLECD().equals("Removed")) {
									 
                                	 sql = "select userid from channel001 where chcode = ?";
                    				 
                     				 List<String> lst =  Jdbctemplate.queryForList(sql, new Object[] { "DVRTSIS" }, String.class);
                     				 
                     				 String sesUserId = lst.size() > 0 ? lst.get(0) : "";
                     				
									 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
									 
									 Jdbctemplate.update(sql, new Object[] {sesUserId,Acc_Nm, "Modify", "Role code", older.get(0).getROLECD(),"Active",  curr_date});
									 
									 
								 }
								 else {
									 
								 }
								 
								 sql = "delete from users00001 where USERSCD = ? and SUBORGCODE = ?";
								 
								 Jdbctemplate.update(sql, new Object[] { Acc_Nm, Suborg});
								 
								 //sql = "Update Users00001 set USERSTS = 'Deleted' where SUBORGCODE = ? and  USERSCD = ? and ROLECD = ? and USERSTS = 'Active' ";
								 
								 //Jdbctemplate.update(sql, new Object[] {Suborg, Acc_Nm, roleName});
										 	
								 details.addProperty("StatusCode", "200");
								 details.addProperty("Status", "Success");
								 details.addProperty("StatusMessage", "Entitlement " + roleName + " removed from the user account " + Acc_Nm +" successfully.");
								 
								 
							 }else {
								 
								 //Duplicate accountName
								 details.addProperty("StatusCode", "400");
								 details.addProperty("Status", "Failure");
								 details.addProperty("StatusMessage", "User account " + Acc_Nm + " does not associated with the "+roleName+ " entitlement to remove!");
								 
							 }
							 							 
						 }
					 
				 }					 
				 else {
					 
					//Application does not contain the requested entitlement CAPTURE_ENTITLEMENT_NAME_HERE to perform create.
					 
					 details.addProperty("StatusCode", "400");
					 details.addProperty("Status", "Failure");
					 details.addProperty("StatusMessage", "Application does not contain the requested entitlement " + roleName + " to remove entitlement.");
					 
				 }
			 
		 }
		 else 
		 {
			 //Not found accountName
			 details.addProperty("StatusCode", "404");
			 details.addProperty("Status", "Failure");
			 details.addProperty("StatusMessage", "User account " + Acc_Nm + " doesn't exist to remove entitlement.");
		 }
		
		 
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("StatusMessage", "Technical Issue" + e.getLocalizedMessage() );   	 //e.getLocalizedMessage()
	 }
	
	 return details;
}


public JsonObject Generate_Datavision_Token(String client_id, String client_secret, String SUBORGCODE, String channel_code, String grant_type) 
{
	JsonObject details = new JsonObject();

	try 
	{
		if(!grant_type.equals("client_credentials")) {
	    	  
	    	  details.addProperty("status", "Failure");
			  details.addProperty("statusCode", "401");
			  details.addProperty("statusMessage", "Invalid Grant type passed!.");
	    	  	     
		}else {

		String sql = "select count(*) from channel001 where SUBORGCODE = ? and CHCODE = ? and USERID=? and HASHPWD=? "; //and SECRETKEY = ?

		int Count = Jdbctemplate.queryForObject(sql,new Object[] {SUBORGCODE, channel_code, client_id, client_secret}, Integer.class); // Secret_key

		System.out.println("SUBORGCODE, channel_code, client_id, client_secret" + SUBORGCODE + channel_code + client_id + client_secret);
		
		if (Count != 0) {
		
			String Token = tk.getJWTToken(SUBORGCODE, channel_code);

			details.addProperty("access_token", Token);
			details.addProperty("scope", "onecertonboard");
			details.addProperty("token_type", "Bearer");
			details.addProperty("expires_in", "10800");
			
		}
		else {
			
			String sql1 = "select count(*) from channel001 where USERID=? and HASHPWD=? ";

			int Count1 = Jdbctemplate.queryForObject(sql1,new Object[] {client_id, client_secret}, Integer.class);

			if (Count1 == 0) {
				
				details.addProperty("status", "Failure");
				details.addProperty("statusCode", "401");
				details.addProperty("statusMessage", "Invalid Client ID OR Invalid Client Secret!.");								
			}
			
		}
		
	  }
		
	} 
	catch (Exception e)
	{
		details.addProperty("status", "Failure");
		details.addProperty("statusCode", "500");
		details.addProperty("statusMessage", "Service is not reachable!.");
		//details.addProperty("message", e.getLocalizedMessage());

	}

	return details;
}


public JsonObject List_of_acc_DD()
{
	JsonObject details = new JsonObject();
	
	try
	{	
		 String sql = "select USERSCD FROM users00001 where USERSTS in ('Active', 'Inactive') order by USERSCD ";
	 
		 List<String> obj = Jdbctemplate.queryForList(sql, String.class);
		 
		 details.add("Accounts_DD", new Gson().toJsonTree(obj));
			  
		 details.addProperty("Result", "Success");
		 details.addProperty("Message", "details found");	 
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage());   	
		 
	 }
	
	 return details;
}


public JsonObject Retreive_submodule_data(Users00001 Info) 
{
	JsonObject details = new JsonObject();
		
	try
	{
		 String sql = "select UNAME, USERTYPE, EMAILADD, ROLECD, USERSTS, ACDESC FROM users00001 where USERSTS in ('Active', 'Inactive') and USERSCD = ?";
		
		 List<Users00001> obj = Jdbctemplate.query(sql, new Object[] { Info.getUSERSCD() }, new Users00001Mapper());
		 
		 //System.out.println(Info.getUSERSCD());
		
		 if(obj.size() != 0)
			 {
			 details.addProperty("UNAME", obj.get(0).getUNAME());
			 details.addProperty("USERTYPE", obj.get(0).getUSERTYPE());
			 details.addProperty("EMAILADD", obj.get(0).getEMAILADD());
			 details.addProperty("ROLECD", obj.get(0).getROLECD());
			 details.addProperty("USERSTS", obj.get(0).getUSERSTS());
			 details.addProperty("ACDESC", obj.get(0).getACDESC());
			 }
		 
		 details.addProperty("Result", obj.size() != 0 ? "Success" : "Failed");
		 details.addProperty("Message", obj.size() != 0 ? "Submodule data Found" : "Submodule data Not Found");
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage()); 
		 
	 }
	
	 return details;
	
		
}


public JsonObject Audit_trail_Report(Users00001 Info, HttpSession session) 
{
	JsonObject details = new JsonObject();
		
	try
	{
		String sesUserId = session.getAttribute("sesUserId").toString();
		
		 String sql1 = "Insert into AUDIT_TRIAL(ADMIN_ID, USER_ID, REQTYPE, FIELD_NAME, OLD_VALUE, NEW_VALUE, ACTION_DT) values(?,?,?,?,?,?,?)";
		 
		 Jdbctemplate.update(sql1, new Object[] {sesUserId, Info.getUSERSCD1(),"Modify", Info.getFIELD_NAME(),
				     Info.getOLD_VALUE(), Info.getNEW_VALUE(), Info.getACTION_DT()});
		 
		 String sql2 = "Insert into AUDIT_TRIAL(ADMIN_ID, USER_ID, REQTYPE, FIELD_NAME, OLD_VALUE, NEW_VALUE, ACTION_DT) values(?,?,?,?,?,?,?)";
		 
		 Jdbctemplate.update(sql2, new Object[] {sesUserId, Info.getUSERSCD2(),"Modify", Info.getFIELD_NAME(),
				 							     Info.getOLD_VALUE(), Info.getNEW_VALUE(), Info.getACTION_DT()});
				 			   
		 				 
		 details.addProperty("Result", "Success");
		 details.addProperty("Message", "Account Added Successfully!!");
	 }
	 catch(Exception e)
	 {
		 details.addProperty("Result", "Failed");
		 details.addProperty("Message", e.getLocalizedMessage()); 
		 
	 }
	
	 return details;
	
		
}

public JsonArray Get_Account_Codes(String term) 
{
	JsonArray Event_Codes = new JsonArray();
	
	System.out.println("TERM"+term);
	
	try
	{
			 
		 String sql = "select USERSCD from users00001 where USERSCD LIKE ?";
		
		 List<String> API_Info = Jdbctemplate.queryForList(sql, new Object[] { term+"%" }, String.class);
		 
		
		 for(int i=0; i<API_Info.size();i++)
			 {
			 String CHCODE = API_Info.get(i);
			 
			 JsonObject Informations = new JsonObject();

			 Informations.addProperty("label", CHCODE );
			 Informations.addProperty("id", CHCODE);
			 
			 Event_Codes.add(Informations);
			 }
		 
		System.out.println(Event_Codes);
	 }
	 catch(Exception e)
	 {
		 System.out.println("Exception in Get_API_Codes :::: "+e.getLocalizedMessage());
	 }
	
	 return Event_Codes;
}

	
public class list_of_account_mapper implements RowMapper<Users00001>{

	 Common_Utils util = new Common_Utils();
	 
	public Users00001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users00001 u1 = new Users00001();
		
		u1.setUSERSCD(rs.getString("USERSCD"));
		u1.setUNAME(rs.getString("UNAME"));
		u1.setUSERTYPE(rs.getString("USERTYPE"));
		u1.setUSERSTS(rs.getString("USERSTS"));
		u1.setLASTLOGINDT(rs.getString("LASTLOGINDT"));
		u1.setROLECD(rs.getString("ROLECD"));
		u1.setEMAILADD(rs.getString("EMAILADD"));
		u1.setACDESC(util.ReplaceNull(rs.getString("ACDESC"))); 
		
		return u1;
	}
	
}

public class Single_account_mapper implements RowMapper<Users00001>{
	
	Common_Utils util = new Common_Utils();
	
	public Users00001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users00001 u1 = new Users00001();
		
		u1.setUSERSCD(rs.getString("USERSCD"));
		u1.setUNAME(rs.getString("UNAME"));
		u1.setUSERTYPE(rs.getString("USERTYPE"));
		u1.setUSERSTS(rs.getString("USERSTS"));
		u1.setLASTLOGINDT(rs.getString("LASTLOGINDT"));
		u1.setROLECD(rs.getString("ROLECD"));
		u1.setEMAILADD(rs.getString("EMAILADD"));
		u1.setACDESC(util.ReplaceNull(rs.getString("ACDESC"))); 
		
		return u1;
	}
	
}


public class list_of_Entitlement_mapper implements RowMapper<Users00001>{

	public Users00001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users00001 u1 = new Users00001();
		
		u1.setROLECD(rs.getString("ROLECD"));
		u1.setROLE_DESC(rs.getString("ROLE_DESC"));

		return u1;
	}
	
}

public class Single_Entitlement_mapper implements RowMapper<Users00001>{

	public Users00001 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Users00001 u1 = new Users00001();
		
		u1.setROLECD(rs.getString("ROLECD"));
		u1.setROLE_DESC(rs.getString("ROLE_DESC"));
		
		return u1;
	}
	
}


public class Users00001Mapper implements RowMapper<Users00001> 
{
	Common_Utils util = new Common_Utils();
	
	public Users00001 mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		Users00001 Info = new Users00001();  
		
		try
		{
			Info.setUNAME(util.ReplaceNull(rs.getString("UNAME")));
			Info.setUSERTYPE(util.ReplaceNull(rs.getString("USERTYPE")));
			Info.setEMAILADD(util.ReplaceNull(rs.getString("EMAILADD")));
			Info.setROLECD(util.ReplaceNull(rs.getString("ROLECD")));
			Info.setUSERSTS(util.ReplaceNull(rs.getString("USERSTS"))); 
			Info.setACDESC(util.ReplaceNull(rs.getString("ACDESC")));
		}
		catch(Exception ex)
		{
			System.out.println("Exception in report003Mapper "+ex.getLocalizedMessage());
		}

		return Info;
	}
 }

public class Users00001_Audit_trail_Mapper implements RowMapper<Users00001> 
{
	Common_Utils util = new Common_Utils();
	
	public Users00001 mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		Users00001 Info = new Users00001();  
		
		try
		{
			Info.setUNAME(util.ReplaceNull(rs.getString("UNAME")));
			Info.setUSERTYPE(util.ReplaceNull(rs.getString("USERTYPE")));
			Info.setEMAILADD(util.ReplaceNull(rs.getString("EMAILADD")));
			Info.setROLECD(util.ReplaceNull(rs.getString("ROLECD")));
			Info.setUSERSTS(util.ReplaceNull(rs.getString("USERSTS"))); 
			Info.setACDESC(util.ReplaceNull(rs.getString("ACDESC"))); 
		}
		catch(Exception ex)
		{
			System.out.println("Exception in report003Mapper "+ex.getLocalizedMessage());
		}

		return Info;
	}
 }

	

}
