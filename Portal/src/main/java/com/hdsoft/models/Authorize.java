package com.hdsoft.models;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Users00001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.utils.EncryptDecrypt;
import com.hdsoft.utils.FormatUtils;
import com.hdsoft.utils.PasswordUtils;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class Authorize 
{
	private static final Logger logger = LogManager.getLogger(Authorize.class);
	private String m_CallStmt = null;
	private String stmt = null;		
	String pgmid;
	String keyval;
	String userid;
	String cbd;
	String Auth_Reject_Flag ;
	String rejcode;
	String rejreason ;
	String calcurrdate;
	String entdby;
	String domainid;
	String authip="";  
	 
	
	@Autowired
	public Adminstration ad;
	
	 @Autowired
	 public Sysconfig sys;
	 
    public JdbcTemplate jdbc;
    
    @Autowired
    public void setJdbcTemplate(HikariDataSource dataSource) {
    	this.jdbc = new JdbcTemplate(dataSource);
    }
	
    public JsonObject updateValues(JsonObject formDTO)
	{
		JsonObject resultDTO = new JsonObject();
	
		resultDTO.addProperty("sucFlg", "0");
		
		String val;	
		
		try
   	 	{
			String SqlArgs = formDTO.get("txtArgs").getAsString();
			
			String[] elements = SqlArgs.split("\\$");
			 
			if(elements.length == 9)
			{
				pgmid = elements[0];
				keyval = elements[1];
				userid = elements[2];
				
				cbd = elements[3];
				
				cbd = FormatUtils.dynaSQLDate(cbd,"DD-MM-YYYY");
				
				Auth_Reject_Flag = elements[4];
				rejcode = elements[5];
				rejreason = elements[6];
				entdby = elements[7];
				domainid = elements[8];
			}
			else
			{
				return resultDTO; 
			}
			JsonObject resultDTO1 = new JsonObject();
			
			logger.debug("PGMID :::::::: "+pgmid);
			
			resultDTO.add("update", resultDTO1);
			
			
			final String authip = formDTO.get("LOG_USER_IP").getAsString(); 

			logger.debug(">>>>>>>>>>> Calling PROC_DYNAAUTH <<<<<<<<<<<<<<<");
			m_CallStmt = "call PROC_DYNAAUTH(?,?,?,?)";
			Map<String, Object> resultMap = jdbc.call(new CallableStatementCreator() {
				
				public CallableStatement createCallableStatement(Connection con) throws SQLException {
					CallableStatement cs = con.prepareCall(m_CallStmt);
					
					cs.setString(1, pgmid);
					cs.setString(2, keyval);
					cs.registerOutParameter(3, Types.VARCHAR);
					cs.registerOutParameter(4, Types.NUMERIC);
					
					return cs;
				}
			}, PROC_DYNAAUTH_params());
			
			val = new Common_Utils().ReplaceNull(resultMap.get("V_RESULT"));
			
			logger.debug("Result for PROC_DYNAAUTH :::::::: "+val);
			
			if(val.equals("1"))
			{
				try
				{
					JsonObject custdto = new JsonObject();
					JsonObject rejectdto= new JsonObject();
					custdto.addProperty("sucFlg","1");
				
					if(Auth_Reject_Flag.equalsIgnoreCase("Auth"))
					{
						if(pgmid.equalsIgnoreCase("ecustuserreg"))
						{							
							custdto.addProperty("sucFlg","0");
							JsonObject object1 = new JsonObject();
							object1.addProperty("keyval",keyval);							
							custdto = UpdateRequestAlert(object1);									
						}
						
						val = custdto.get("sucFlg").getAsString();
						if(pgmid.equalsIgnoreCase("userregistration"))
						{
							logger.debug(">>>>>>>>>>> Calling SP_UPDATEAUTH <<<<<<<<<<<<<<<");
							
							logger.debug("pgmid ::::::::::: "+pgmid);
							logger.debug("keyval ::::::::::: "+keyval);
							logger.debug("userid ::::::::::: "+userid);
							logger.debug("cbd ::::::::::: "+cbd);
							logger.debug("Auth_Reject_Flag ::::::::::: "+Auth_Reject_Flag);
							logger.debug("rejcode ::::::::::: "+rejcode);
							logger.debug("rejreason ::::::::::: "+rejreason);
							logger.debug("authip ::::::::::: "+authip);
							
							stmt= "call SP_UPDATEAUTH(?,?,?,?,?,?,?,?,?)";
							
							Map<String, Object> resultMap1 = jdbc.call(new CallableStatementCreator() {
								
								public CallableStatement createCallableStatement(Connection con) throws SQLException {
									CallableStatement cs = con.prepareCall(stmt);
									
									cs.setString(1, pgmid);
									cs.setString(2, keyval);
									cs.setString(3, userid);
									cs.setString(4, cbd);
									cs.setString(5, Auth_Reject_Flag);
									cs.setString(6, rejcode);
									cs.setString(7, rejreason);
									cs.setString(8, authip);
									cs.registerOutParameter(9, Types.VARCHAR);
									
									return cs;
								}
							}, SP_UPDATEAUTH_params());
							
							
							logger.debug("Result for SP_UPDATEAUTH :::::::: "+resultMap1.get("P_OUT"));
							
							System.out.println("op="+resultMap1.get("P_OUT").toString());
							
							val = resultMap1.get("P_OUT").toString();
							
							if(val.contains("ORA-"))
							{
								resultDTO.addProperty("result", val);
								
								return resultDTO;
							}
							
							resultDTO.addProperty("programid",pgmid);	
							resultDTO.addProperty("AuthRej",Auth_Reject_Flag);	
							resultDTO.addProperty("result",val);
							resultDTO.addProperty("sucFlg", "1");
							
							
							String[] usr_id_info = keyval.split("\\|");
							
							logger.debug(">>>>>>>>>>> Updating Password <<<<<<<<<<<<<<<");
							
							if(usr_id_info.length == 2) 
							{
								updatepasswordAuth(usr_id_info[1]);
							}
							
						}else if(pgmid.equals("unblockuserid"))
						{
							JsonObject json = Update_Unblock_Auth(keyval, Auth_Reject_Flag);
							
							if(json.get("stscode").getAsString().equalsIgnoreCase("MER00")) {
								
								resultDTO.addProperty("programid",pgmid);	
								resultDTO.addProperty("AuthRej",Auth_Reject_Flag);	
								resultDTO.addProperty("result",val);
								resultDTO.addProperty("sucFlg", "1");
								
							}
						}
						else
						{
							System.out.println(resultDTO.get("errMsg"));
							resultDTO.addProperty("result","Error in Interface Process");
						}
						
					}
					
					if(Auth_Reject_Flag.equalsIgnoreCase("Reject"))
					{
						rejectdto.addProperty("sucFlg", "0");
						JsonObject object2 = new JsonObject();
						object2.addProperty("keyval",keyval);
						rejectdto = InsertRejectAlert(object2);
						
						if(pgmid.equalsIgnoreCase("userregistration")) {
							logger.debug(">>>>>>>>>>> Calling SP_UPDATEAUTH <<<<<<<<<<<<<<<");
							
							logger.debug("pgmid ::::::::::: "+pgmid);
							logger.debug("keyval ::::::::::: "+keyval);
							logger.debug("userid ::::::::::: "+userid);
							logger.debug("cbd ::::::::::: "+cbd);
							logger.debug("Auth_Reject_Flag ::::::::::: "+Auth_Reject_Flag);
							logger.debug("rejcode ::::::::::: "+rejcode);
							logger.debug("rejreason ::::::::::: "+rejreason);
							logger.debug("authip ::::::::::: "+authip);
							
							stmt= "call SP_UPDATEAUTH(?,?,?,?,?,?,?,?,?)";
							
							Map<String, Object> resultMap1 = jdbc.call(new CallableStatementCreator() {
								
								public CallableStatement createCallableStatement(Connection con) throws SQLException {
									CallableStatement cs = con.prepareCall(stmt);
									
									cs.setString(1, pgmid);
									cs.setString(2, keyval);
									cs.setString(3, userid);
									cs.setString(4, cbd);
									cs.setString(5, Auth_Reject_Flag);
									cs.setString(6, rejcode);
									cs.setString(7, rejreason);
									cs.setString(8, authip);
									cs.registerOutParameter(9, Types.VARCHAR);
									
									return cs;
								}
							}, SP_UPDATEAUTH_params());
							
							
							logger.debug("Result for SP_UPDATEAUTH :::::::: "+resultMap1.get("P_OUT"));
							
							System.out.println("op="+resultMap1.get("P_OUT").toString());
							
							val = resultMap1.get("P_OUT").toString();
							
							if(val.contains("ORA-"))
							{
								resultDTO.addProperty("result", val);
								
								return resultDTO;
							}
							
							resultDTO.addProperty("programid",pgmid);	
							resultDTO.addProperty("AuthRej",Auth_Reject_Flag);	
							resultDTO.addProperty("result",val);
							resultDTO.addProperty("sucFlg", "1");
						}else if(pgmid.equals("unblockuserid")) {
							
							String sql = "delete auth001 where authq_main_pk = ?";
							
							int auth01count = jdbc.update(sql, new Object[] {keyval});
							
							sql = "delete auth002 where auth_main_pk = ?";
							
							int auth02count = jdbc.update(sql, new Object[] {keyval});
							
							sql = "delete auth003 where authdtl_main_pk = ?";
							
							int auth03count = jdbc.update(sql, new Object[] {keyval});
							
							resultDTO.addProperty("programid",pgmid);	
							resultDTO.addProperty("AuthRej",Auth_Reject_Flag);	
							resultDTO.addProperty("result","Success");
							resultDTO.addProperty("sucFlg", "1");
							
						}
						
						
					}
					
				 }
				 catch(Exception e )
				 {
				    System.out.println(e);
				 }				
			 }
			logger.debug("OVER ALL METHODS :::::: "+resultDTO);
		}
	   	catch (Exception e) 
	   	{
	   		 System.out.println("Exception Occured "+e);
		}
		
   	 	  return resultDTO;
	}
    
	public JsonObject InsertRejectAlert(JsonObject formDTO)
	{
		
		JsonObject resultDTO = new JsonObject();	
		int li;
		try
		{
		     String sqlstr = "INSERT INTO EVENTEXECUTOR (suborgcode,USER_ID,ALERT_GEN_DATE,ALERT_FOR_DIREC,FES_REF_NO,CONTENT,PORTALLP_DEL,FWD_ACTION,EVENT_ID,USER_TYPE)VALUES(?,?,?,?,?,?,?,?,?,?)";
			
		     li = jdbc.update(sqlstr,domainid,entdby,new java.sql.Timestamp (System.currentTimeMillis()),"F","111111111111111",rejreason,0,"R","A03","U");
		     
			
			 
			 if (li != 0)
			 { 
					resultDTO.addProperty("sucFlg", "1");
			 }
			 else
			 {
					resultDTO.addProperty("sucFlg", "0");
					resultDTO.addProperty("errMsg", "Nill");			
			 }
			
		}
		catch(Exception e)
		{
			resultDTO.addProperty("sucFlg","0") ;
			resultDTO.addProperty("errMsg",e.getLocalizedMessage()) ;
			
			logger.debug("Exception :::::"+e.getLocalizedMessage());
		}
		
		return resultDTO;		
	}
    
	public JsonObject Update_Unblock_Auth(String keyval, String Auth_Reject_Flag) {
		
		JsonObject resultDTO = new JsonObject();
		
		try
		{
				String sql = "select a3.authdtl_data_block from auth003 a3 where a3.authdtl_main_pk=? and a3.AUTHDTL_TABLE_NAME = ? and a3.authdtl_entry_date = (select max(a4.authdtl_entry_date) from auth003 a4 where a4.authdtl_main_pk = ?)";
				String li1 = jdbc.queryForObject(sql,new Object[] {keyval,"USERS002",keyval},String.class);
				
				String[] str = li1.split("\\|",-1);
				
				for(int i = 0; i < str.length; i++) {
					System.out.println(str[i]);
				}
				
				if(str[2].equals("0")) {
				
					sql = "Delete from auth001 where AUTHQ_MAIN_PK=?";
					int li = jdbc.update(sql,new Object[] {keyval});
					
					System.out.println(li);
					
					if(li != 0) {
						sql = "Delete from auth002 where AUTH_MAIN_PK=?";
						li = jdbc.update(sql,new Object[] {keyval});
						
						System.out.println(li);
						
						if(li != 0) {
							sql = "Delete from auth003 where AUTHDTL_MAIN_PK=?";
							li = jdbc.update(sql,new Object[] {keyval});
							
							System.out.println(li);
							
							String[] elements = keyval.split("\\|");
							
							String orgcode = elements[0];
							String user_id = elements[1];
							
							sql = "Delete from USERS012 where UPWDINV_USER_ID=? and SUBORGCODE=?";
							li = jdbc.update(sql,new Object[] {user_id,orgcode});
							
							System.out.println(li);
							
						}
					}
					 resultDTO.addProperty("stscode", "MER00");
					 resultDTO.addProperty("Result", "Success");
					 resultDTO.addProperty("Message", "User Unblocked");
					 
				}else if(str[2].equals("1")) {
					
					sql = "Delete from auth001 where AUTHQ_MAIN_PK=?";
					int li = jdbc.update(sql,new Object[] {keyval});
					
					System.out.println(li);
					if(li != 0) {
						sql = "Delete from auth002 where AUTH_MAIN_PK=?";
						li = jdbc.update(sql,new Object[] {keyval});
						
						System.out.println(li);
						if(li != 0) {
							sql = "Delete from auth003 where AUTHDTL_MAIN_PK=?";
							li = jdbc.update(sql,new Object[] {keyval});
							
							System.out.println(li);
							String[] elements = keyval.split("\\|");
							
							String orgcode = elements[0];
							String user_id = elements[1];
							
							sql = "insert into users012(UPWDINV_USER_ID,SUBORGCODE,UPWDINV_INVALID_COUNT) values(?,?,?)";
							li = jdbc.update(sql,new Object[] {user_id,orgcode,3});
							
							System.out.println(li);
							
						}
					}
					
					
					 
					 resultDTO.addProperty("stscode", "MER00");
					 resultDTO.addProperty("Result", "Success");
					 resultDTO.addProperty("Message", "User blocked");
				}else {
					resultDTO.addProperty("stscode", "MER01");
					resultDTO.addProperty("Result", "Failed");
					resultDTO.addProperty("Message", "Data is not there");
				}
			
			
		}
		catch(Exception e)
		{
			resultDTO.addProperty("stscode","MER04");
			
			resultDTO.addProperty("Result","Failed") ;
			
			resultDTO.addProperty("Result",e.getLocalizedMessage()) ;
			
			logger.debug("Exception ::::: "+e.getLocalizedMessage());
			
			return resultDTO;
		}
		return resultDTO;
	}

	
	public JsonObject updatepasswordAuth(final String userid)
	{
		JsonObject resultDTO = new JsonObject();
	
		resultDTO.addProperty("sucFlag", "0");
		
		
		
		logger.debug(userid);
		
		try
		{
			
			
			JsonObject details = ad.Get_users001_Info_by_user_id(userid);
			
			System.out.println("userid :::: "+userid);
			String val = null;
			
			final String hashedpwd = details.get("VERIFY").getAsString();
			
			logger.debug(">>>>>>>>>>> Calling SP_UPDPWD_GEN <<<<<<<<<<<<<<<");
			
			final String CS = "call SP_UPDPWD_GEN(?,?,?,?)";
			Map<String, Object> resultMap = jdbc.call(new CallableStatementCreator() {
				
				public CallableStatement createCallableStatement(Connection con) throws SQLException {
					CallableStatement cs = con.prepareCall(CS);
					
					cs.setString(1, sys.getSuborgcode());
					cs.setString(2, userid);
					cs.setString(3, hashedpwd);
					cs.registerOutParameter(4, Types.VARCHAR);
					
					return cs;
				}
			}, SP_UPDPWD_GEN_params());
			
			
			System.out.println("CS.getString(4) ::: "+resultMap.get("ERRORMSG"));
			
			logger.debug("Result for SP_UPDPWD_GEN ::::: "+resultMap.get("ERRORMSG"));
			val = resultMap.get("ERRORMSG").toString();
			if(val.equalsIgnoreCase("S"))
			{
				resultDTO.addProperty("sucFlag", "1");
				
				logger.debug(">>>>>>>>>>> Password Updated Succesfully <<<<<<<<<<<<<<<");
			}	
		}
		catch(Exception e) 
	   	{
			logger.debug("Exception Occured while calling SP_UPDPWD_GEN :::: "+e.getLocalizedMessage());
		}
		
		 return resultDTO;
	}
	
	public JsonObject UpdateRequestAlert(JsonObject formDTO)
	{
		JsonObject resultDTO = new JsonObject();
		String SqlArgs = (String) formDTO.get("keyval").getAsString();
		StringTokenizer parsedArgs = new StringTokenizer(SqlArgs, "|");
		String domid = parsedArgs.nextToken();
		String useid = parsedArgs.nextToken();
		
		try
		{
			String sqlQuery = "SELECT COUNT(*) RECEXIST from USERS0001 WHERE TRIM(suborgcode)=? AND TRIM(USERSCD)=?";
			String sqlQuery1 = " SELECT PWD_GEN_EMAIL_NUSER FROM SYSCONF005 WHERE PWD_GEN_BY_DOMAIN_ID=? AND PWD_GEN_EFFECTIVE_DATE=(SELECT MAX(PWD_GEN_EFFECTIVE_DATE) FROM SYSCONF005 WHERE PWD_GEN_BY_DOMAIN_ID=? AND PWD_GEN_EFFECTIVE_DATE<=SYSDATE)";
			
			List<String> li = jdbc.queryForList(sqlQuery, new Object[] {domid,useid}, String.class);
			
			
			if(li.size() != 0)
			{
				JsonObject resDTO = new JsonObject();
				String val = resDTO.get("RECEXIST").getAsString();
				if(val.equals("1"))
				{	
					li = jdbc.queryForList(sqlQuery1, new Object[] {domid,domid}, String.class);
					
					
					if(li.size() != 0)
					{
						JsonObject resDTO1 = new JsonObject();
						val = resDTO1.get("PWD_GEN_EMAIL_NUSER").getAsString();
						if(val.equals("1"))
						{
							sqlQuery = "UPDATE  EVENTEXECUTOR SET PORTALLP_DEL = 0 WHERE TRIM(suborgcode) = ? AND TRIM(USER_ID) = ?";		
							li = jdbc.queryForList(sqlQuery, new Object[] {domid,useid}, String.class);
							
							
							if (li.size() > 0)
							{
								resultDTO.addProperty("sucFlg","1");				
							}
						}
						else
						{
							resultDTO.addProperty("sucFlg","0");
						}
					}
					
				}
				else 
				{
					resultDTO.addProperty("sucFlg","0");
				}		
			}
	} 
	catch (Exception e)
	{	
		resultDTO.addProperty("SucFlg","0");
		resultDTO.addProperty("errMsg",e.getLocalizedMessage());
		
		logger.debug("Exception Occured :::: "+e.getLocalizedMessage());
		
	}	
		
		return resultDTO;
	}
	
	public static boolean getAuthenticationResult(HttpServletRequest request)
	{
		boolean result = false;
		
		try 
		{
			String serpath = request.getRequestURL().toString();
			
			String pgmId = serpath.substring(serpath.lastIndexOf("/")+1,serpath.lastIndexOf("."));
			
			if(pgmId.endsWith("conf"))
			{
				pgmId=pgmId.replaceAll("conf","");
			}

			result = true;
		} 
		catch (Exception e) 
		{
			result = false;
		}
		
		return result;
	}
	
	
	public JsonArray User_Id_Unblock_Suggestions(String Search_Word, HttpServletRequest request) 
	{   	
		JsonArray User_Ids = new JsonArray();

		
		try
		{
			String query = "SELECT UPWDINV_USER_ID FROM users012 where UPWDINV_USER_ID LIKE upper(?) or UPWDINV_USER_ID LIKE lower(?)";                                                
			List<String> li = jdbc.queryForList(query,new Object[] {"%"+Search_Word+"%","%"+Search_Word+"%"},String.class);
			
			for(int i = 0 ;i<li.size();i++)
			{
				JsonObject USERSCD = new JsonObject();
				
				USERSCD.addProperty("label", li.get(i));
				
				User_Ids.add(USERSCD); 
			}	
		}
		catch(Exception ex)
		{
			logger.debug("Exception when Loading User_Id ::::: "+ex.getLocalizedMessage());
		}
		
		return User_Ids;
	}
	
	public JsonObject update_userid_unblock_temp(String keyval)
	{
		
		JsonObject resultDTO = new JsonObject();
		
		try
		{
			
			String sql = "Delete from auth001 where AUTHQ_MAIN_PK=?";
			int li = jdbc.update(sql,new Object[] {keyval});
			
			System.out.println(li);
			if(li != 0) {
				sql = "Delete from auth002 where AUTH_MAIN_PK=?";
				li = jdbc.update(sql,new Object[] {keyval});
				
				System.out.println(li);
				if(li != 0) {
					sql = "Delete from auth003 where AUTHDTL_MAIN_PK=?";
					li = jdbc.update(sql,new Object[] {keyval});
					
					System.out.println(li);
					String[] elements = keyval.split("\\|");
					
					if(elements.length == 2)
					 {
						String orgcode = elements[0];
						String user_id = elements[1];
						
						sql = "Delete from USERS012 where UPWDINV_USER_ID=? and SUBORGCODE=?";
						li = jdbc.update(sql,new Object[] {user_id,orgcode});
						System.out.println(li);
					 }
					
				}
			}
			
			
			 
			 resultDTO.addProperty("sucFlg", "1");
			 resultDTO.addProperty("result", "Success");
			 
		}
		catch(Exception e)
		{
			resultDTO.addProperty("sucFlg","0");
			
			resultDTO.addProperty("result",e.getLocalizedMessage()) ;
			
			logger.debug("Exception ::::: "+e.getLocalizedMessage());
			
			return resultDTO;
		}
		return resultDTO;		
	}
	public List<SqlParameter> PROC_DYNAAUTH_params()
	{
		List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
	
		inParamMap.add(new SqlParameter("V_PROG_CODE"  		, Types.VARCHAR));
		inParamMap.add(new SqlParameter("V_KEY"   		, Types.VARCHAR)); 
		inParamMap.add(new SqlOutParameter("V_ERROR_STATUS"       , Types.VARCHAR));
		inParamMap.add(new SqlOutParameter("V_RESULT"     , Types.INTEGER));
		
		return inParamMap;
	}
	public List<SqlParameter> SP_UPDATEAUTH_params()
	{
		List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
	
		inParamMap.add(new SqlParameter("P_PRGNAME"  		, Types.VARCHAR));
		inParamMap.add(new SqlParameter("P_KEY"   		, Types.VARCHAR)); 
		inParamMap.add(new SqlParameter("P_USER"  		, Types.VARCHAR));
		inParamMap.add(new SqlParameter("p_CBD"   		, Types.VARCHAR)); 
		inParamMap.add(new SqlParameter("P_AUTH"  		, Types.VARCHAR));
		inParamMap.add(new SqlParameter("P_REJCODE"   		, Types.VARCHAR)); 
		inParamMap.add(new SqlParameter("P_REJREASON"  		, Types.VARCHAR));
		inParamMap.add(new SqlParameter("P_AUTHIP"   		, Types.VARCHAR)); 
		inParamMap.add(new SqlOutParameter("P_OUT"     , Types.INTEGER));
		
		return inParamMap;
	}
	public List<SqlParameter> SP_UPDPWD_GEN_params()
	{
		List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
	
		inParamMap.add(new SqlParameter("CUSTID"  		, Types.VARCHAR));
		inParamMap.add(new SqlParameter("USERID"   		, Types.VARCHAR)); 
		inParamMap.add(new SqlParameter("HASHED"       , Types.VARCHAR));
		inParamMap.add(new SqlOutParameter("ERRORMSG"     , Types.VARCHAR));
		
		return inParamMap;
	}
	
	public JsonObject Update_Role_Code(String userid, String rolecd, String sesUserId) 
	{   	
		JsonObject User_Ids = new JsonObject();

		
		try
		{
			
			Common_Utils utils = new Common_Utils();
			
			String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
	        Timestamp curr_date = Timestamp.valueOf(now);
			
			
			 String sql = "select * from Users00001 where USERSCD = ?";
			 
			 List<Users00001> old=jdbc.query(sql, new Object[] {userid},new Users00001_Audit_trail_Mapper() );
			
			String query =  "update users00001 set rolecd=? where userscd=? ";                                                
		    
			jdbc.update(query,new Object[] {rolecd,userid});
		    
		    if(!old.get(0).getROLECD().equals(rolecd)) {
				 
				 sql = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
				 
				 jdbc.update(sql, new Object[] { sesUserId ,userid, "Modify", "Role code", old.get(0).getROLECD(),rolecd,  curr_date});	 
			 }
			 
		    User_Ids.addProperty("Result", "Success");
		    User_Ids.addProperty("stscode", "200");
		    User_Ids.addProperty("message", "Updated Successfully!!");
			
			
		}
		catch(Exception ex)
		{
			logger.debug("Exception when Loading User_Id ::::: "+ex.getLocalizedMessage());
		}
		
		return User_Ids;
	}
	
	public JsonObject Delete_Code(String userid, String sesUserId) 
	{   	
		JsonObject User_Ids = new JsonObject();

		
		try
		{
			
			Common_Utils utils = new Common_Utils();
			
			//String Curr_date = utils.getCurrentDate("dd-MMM-yyyy");
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
	        Timestamp curr_date = Timestamp.valueOf(now);
			
			
			System.out.println("Rows affected: " + userid);
			   
			String query = "insert into users00001_hst (select * from users00001 where USERSCD=?)";
	        jdbc.update(query, new Object[]{userid});
            
	        //query = "update users00001_hst set ADATE =? where USERSCD=?";
	        //jdbc.update(query, new Object[]{utils.get_oracle_Timestamp(),userid});
	        
	        query = "update users00001_hst set USERSTS=?, ADATE=? where USERSCD = ?";
			 
	        jdbc.update(query, new Object[] { "Deleted", utils.get_oracle_Timestamp(), userid});
	       	
			query = "DELETE FROM users00001 WHERE userscd = ?";
	        
			int rowsAffected = jdbc.update(query, new Object[]{userid});
	        
	        query = "Insert into audit_report001(ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME) values(?,?,?,?,?,?,?)";
			 
	        jdbc.update(query, new Object[] { sesUserId ,userid, "Delete", "", "","",  curr_date});
			
	        System.out.println("Rows affected: " + rowsAffected);
		   
		    User_Ids.addProperty("Result", "Success");
		    User_Ids.addProperty("Stscode", "200");
		    User_Ids.addProperty("Message", "Deleted Successfully!!");
			
			
		}
		catch(Exception ex)
		{
			logger.debug("Exception when Loading User_Id ::::: "+ex.getLocalizedMessage());
		}
		
		return User_Ids;
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
			}
			catch(Exception ex)
			{
				System.out.println("Exception in report003Mapper "+ex.getLocalizedMessage());
			}

			return Info;
		}
	 }
	
}
