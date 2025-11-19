package com.hdsoft.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import javax.sql.DataSource;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Comvalidator_Store;
import com.hdsoft.Repositories.Menu001;
import com.hdsoft.Repositories.Users0001;
import com.hdsoft.Repositories.Users001;
import com.hdsoft.Repositories.login_Info;
import com.hdsoft.common.DTDObject;
import com.hdsoft.common.Transpoter;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class Comvalidator2 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(Comvalidator2.class);
	
	public JsonObject varProdCode(JsonObject Info) 
	{
		JsonObject details = new JsonObject();
		
		String varProdCode = Info.get("varProdCode").getAsString();
		
		try 
		{
			String sql = "Select CUSPRD_PROD_CODE,MPROD_PROD_DESC from cusprd,mprod where cusprd_prod_code=MPROD_PROD_CODE and CUSPRD_PROD_CODE=?";
			
			List<Comvalidator_Store> obj = Jdbctemplate.query(sql,  new Object[] { varProdCode }, new Comvalidator_Mapper() );
			
			if(obj.size() != 0)
			{
				 details.addProperty("CUSPRD_PROD_CODE", obj.get(0).getCUSPRD_PROD_CODE());
				 details.addProperty("MPROD_PROD_DESC", obj.get(0).getMPROD_PROD_DESC());
				 
				 details.addProperty("sucFlg", "1");
			}
			else
			{
				details.addProperty("errMsg", "Row Not Present");
				details.addProperty("sucFlg", "0");
			}
		} 
		catch (Exception e) 
		{
			details.addProperty("sucFlg", "0");
			details.addProperty("errMsg", e.getLocalizedMessage());
			
			logger.debug("varProdCode::" + e.getLocalizedMessage());
		}
		
		return details;
	}

	public JsonObject validateCurrencyCode(JsonObject Info) 
	{
		JsonObject details = new JsonObject();
		
		try 
		{
			details.addProperty(Transpoter.ERROR_KEY, Transpoter.ERROR_ABSENT);
			details.addProperty(Transpoter.FORM_ERROR, Transpoter.FORM_ERROR_ABSENT);
			
			String value = Info.get("CURR_CODE").getAsString();
			
			if(Info.has(Transpoter.CHECK_BLANK) && Info.get(Transpoter.CHECK_BLANK).getAsString().equals(Transpoter.REQUIRED)) 
			{
				if(FormatUtils.isBlank(value)) 
				{
					details.addProperty(Transpoter.ERROR_STATUS, Transpoter.ERROR_PRESENT);
					details.addProperty(Transpoter.ERROR_CODE, Transpoter.FIELD_BLANK);
					
					return details;
				}
			}
			
			if(Info.has(Transpoter.CHECK_LENGTH) && Info.get(Transpoter.CHECK_LENGTH).getAsString().equals(Transpoter.REQUIRED)) 
			{		
				int min_length = Integer.parseInt(Info.get(Transpoter.MIN_LENGTH).getAsString());
				
				int max_length = Integer.parseInt(Info.get(Transpoter.MAX_LENGTH).getAsString());
				
				if(!(FormatUtils.hasMinimumLength(value, min_length) && FormatUtils.hasMaximumLength(value, max_length))) 
				{
					details.addProperty(Transpoter.ERROR_STATUS, Transpoter.ERROR_PRESENT);
					details.addProperty(Transpoter.ERROR_CODE, Transpoter.FIELD_INVALID);
					
					return details;
				}
		     }
	
			String sql = "Select CURR_CODE,CURR_NAME from CURRENCY where CURR_CODE = ?";
			
			List<Comvalidator_Store> obj = Jdbctemplate.query(sql,  new Object[] { value }, new Comvalidator_Mapper2() );
			
			if(obj.size() != 0)
			{
				 details.addProperty("CURR_CODE", obj.get(0).getCURR_CODE());
				 details.addProperty("CURR_NAME", obj.get(0).getCURR_NAME());
				 
				 details.addProperty(Transpoter.ROW_STATUS, Transpoter.ROW_PRESENT);
			}
			else
			{
				 details.addProperty(Transpoter.ROW_STATUS, Transpoter.ROW_ABSENT);
			}	
		} 
		catch(Exception e) 
		{
			details.addProperty(Transpoter.ERROR_STATUS, Transpoter.ERROR_PRESENT);
			details.addProperty(Transpoter.FORM_ERROR, Transpoter.FORM_ERROR_PRESENT);
			
			logger.debug("validateCurrencyCode::" + e.getLocalizedMessage());
		}
		
		return details;
	}

	public JsonObject valCurrCode(JsonObject Info) 
	{
		JsonObject details = new JsonObject();
		
		try 
		{
			String varCurrCode = Info.get("varCurrCode").getAsString();

			String sql = "Select CURR_CODE,CURR_NAME from CURRENCY where CURR_CODE = ?";
			
			List<Comvalidator_Store> obj = Jdbctemplate.query(sql,  new Object[] { varCurrCode }, new Comvalidator_Mapper2() );
			
			if(obj.size() != 0)
			{
				 details.addProperty("CURR_CODE", obj.get(0).getCURR_CODE());
				 details.addProperty("CURR_NAME", obj.get(0).getCURR_NAME());	
				 
				 details.addProperty("sucFlg", "1");
			}
			else
			{
				details.addProperty("sucFlg", "0");
				details.addProperty("errMsg", "Row Not Present");
			}	
		} 
		catch(Exception e) 
		{
			details.addProperty("errMsg", e.getLocalizedMessage());
			details.addProperty("sucFlg", "0");
			
			logger.debug("valCurrCode::" + e.getLocalizedMessage());
		}
		
		return details;
	}

	public JsonObject validateProgramId(JsonObject Info) 
	{
		JsonObject details = new JsonObject();
		
		try 
		{
			String SqlArgs = Info.get("Args").getAsString();
			
			String sql = "SELECT PGM_ID, DESCN  FROM MENU001 WHERE PGM_ID= ? ";
			
			List<Menu001> obj = Jdbctemplate.query(sql,  new Object[] { SqlArgs }, new Menu001_Mapper());
			
			if(obj.size() !=0) 
			{
				details.addProperty("PGM_ID", obj.get(0).getPGM_ID());
				details.addProperty("DESCN", obj.get(0).getDESCN());	
				
				details.addProperty("sucFlg", "1");
			} 
			else 
			{
				details.addProperty("sucFlg", "0");
				details.addProperty("errMsg", "Row Not Present");
			}
		} 
		catch(Exception e) 
		{
			details.addProperty("sucFlg", "0");
			details.addProperty("errMsg", e.getLocalizedMessage());
			
			logger.debug("validateProgramId::" + e.getLocalizedMessage());
		} 
		
		return details;
	}
	
	public JsonObject validateUser(JsonObject Info) 
	{
		JsonObject details = new JsonObject();
		
		try 
		{
			String SqlArgs = Info.get("Args").getAsString();
			
			String sql = "SELECT USERSCD,UNAME from USERS0001 where USER_ID= ? ";
			
			List<Users0001> obj = Jdbctemplate.query(sql, new Object[] { SqlArgs }, new Users0001_Mapper()); 
			
			if(obj.size() !=0) 
			{
				details.addProperty("USERSCD", obj.get(0).getUSERSCD());
				details.addProperty("UNAME", obj.get(0).getUNAME());	
				
				details.addProperty("sucFlg", "1");
			} 
			else 
			{
				details.addProperty("sucFlg", "0");
				details.addProperty("errMsg", "Row Not Present");
			}
		} 
		catch(Exception e) 
		{
			details.addProperty("sucFlg", "0");
			details.addProperty("errMsg", e.getLocalizedMessage());
			
			logger.debug("validateUser::" + e.getLocalizedMessage());
		} 
		
		return details;
	}

	public JsonObject validateUserId(JsonObject Info) 
	{
		JsonObject details = new JsonObject();
		
		try 
		{
			String SqlArgs = Info.get("Args").getAsString();
			
			StringTokenizer parsedArgs = new StringTokenizer(SqlArgs, "|");
			String domainid = parsedArgs.nextToken();
			String userid = parsedArgs.nextToken();

			String sql = "SELECT * from USERS001 WHERE TRIM(suborgcode)=? AND TRIM(USER_ID)=?";
			
			List<Users001> obj = Jdbctemplate.query(sql, new Object[] { domainid, userid }, new Users001_Mapper()); 
		
			if(obj.size() !=0) 
			{
				 details.addProperty("SUBORGCODE", obj.get(0).getSUBORGCODE());
				 details.addProperty("USER_ID", obj.get(0).getUSER_ID());
				 details.addProperty("USER_DOB", obj.get(0).getUSER_DOB());
				 details.addProperty("USER_NAME", obj.get(0).getUSER_NAME());
				 details.addProperty("ROLE_ID", obj.get(0).getROLE_ID());
				 details.addProperty("ADDRESS", obj.get(0).getADDRESS());
				 details.addProperty("TEL_NO", obj.get(0).getTEL_NO());
				 details.addProperty("GSM_NO", obj.get(0).getGSM_NO());
				 details.addProperty("EMAIL_ID", obj.get(0).getEMAIL_ID());
				 details.addProperty("REMARKS", obj.get(0).getREMARKS());
				 details.addProperty("CATEGORY", obj.get(0).getCATEGORY());
				 details.addProperty("MODULE", obj.get(0).getMODULE());
				 details.addProperty("ADMIN_HIER", obj.get(0).getADMIN_HIER());
				 details.addProperty("REGIS_DATE", obj.get(0).getREGIS_DATE());
				 
				 details.addProperty("sucFlg", "1");
			}
			else
			{
				details.addProperty("sucFlg", "0");
				details.addProperty("errMsg", "Row Not Present");
			}
		} 
		catch (Exception e) 
		{
			details.addProperty("sucFlg", "0");
			details.addProperty("errMsg", "Row Not Present");
			
			logger.debug("validateUserId::" + e.getLocalizedMessage());
		} 
		
		return details;
	}

	public Date fetchUserLoginTime(String userId, String domainId) throws SQLException 
	{
		java.util.Date userlogintime = null;
		
		try 
		{
			String sql = "SELECT ULOGIN_IN_DATE FROM USERS008 WHERE ULOGIN_USER_ID=? AND suborgcode=?";
			
			List<Timestamp> obj = Jdbctemplate.queryForList(sql,  new Object[] { userId, domainId }, Timestamp.class);
			
			if(obj.size() != 0)
			{ 
				 userlogintime = obj.get(0);
			}
		} 
		catch(Exception e) 
		{
			logger.debug("fetchUserLoginTime::" + e.getLocalizedMessage());
		}
		
		return userlogintime;
	}

	 public JsonObject getCurrency() throws SQLException
	 {
		 JsonObject details = new JsonObject();
		 
		 String baseCurrency = "";
		 
		 try 
		 {
			String sql = "SELECT TRIM(currencycd) FROM sysconf001";
			
			List<String> obj = Jdbctemplate.queryForList(sql, String.class);
			
			if(obj.size() != 0)
			{ 
				baseCurrency = obj.get(0);
			}
			
			details.addProperty("baseCurrency", baseCurrency);
		 } 
		 catch(Exception e) 
		 {
			logger.debug("fetchUserLoginTime::" + e.getLocalizedMessage());
		 }
		
	     return details;
	 }
	 

	
	 public class Comvalidator_Mapper implements RowMapper<Comvalidator_Store> 
     {
		public Comvalidator_Store mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Comvalidator_Store API = new Comvalidator_Store();  

			API.setCUSPRD_PROD_CODE(rs.getString("CUSPRD_PROD_CODE"));
			API.setMPROD_PROD_DESC(rs.getString("MPROD_PROD_DESC"));
			
			return API;
		}
     }
     
     public class Comvalidator_Mapper2 implements RowMapper<Comvalidator_Store> 
     {
		public Comvalidator_Store mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Comvalidator_Store API = new Comvalidator_Store();  

			API.setCURR_CODE(rs.getString("CURR_CODE"));
			API.setCURR_NAME(rs.getString("CURR_NAME"));
			
			return API;
		}
     }
     
     public class Menu001_Mapper implements RowMapper<Menu001> 
     {
		public Menu001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Menu001 API = new Menu001();  

			API.setPGM_ID(rs.getString("PGM_ID"));
			API.setDESCN(rs.getString("DESCN"));
			
			return API;
		}
     }
     
     public class Users0001_Mapper implements RowMapper<Users0001> 
     {
		public Users0001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Users0001 API = new Users0001();  

			API.setUSERSCD(rs.getString("USERSCD"));
			API.setUNAME(rs.getString("UNAME"));
		
			return API;
		}
     }
     
     public class Users001_Mapper implements RowMapper<Users001> 
     {
		public Users001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Users001 API = new Users001();  

			API.setSUBORGCODE(rs.getString("SUBORGCODE"));
			API.setUSER_ID(rs.getString("USER_ID"));
			API.setUSER_DOB(rs.getString("USER_DOB"));
			API.setUSER_NAME(rs.getString("USER_NAME"));
			API.setROLE_ID(rs.getString("ROLE_ID"));
			API.setADDRESS(rs.getString("ADDRESS"));
			API.setTEL_NO(rs.getString("TEL_NO"));
			API.setGSM_NO(rs.getString("GSM_NO"));
			API.setEMAIL_ID(rs.getString("EMAIL_ID"));
			API.setREMARKS(rs.getString("REMARKS"));
			API.setCATEGORY(rs.getString("CATEGORY"));
			API.setMODULE(rs.getString("MODULE"));
			API.setADMIN_HIER(rs.getString("ADMIN_HIER"));
			API.setREGIS_DATE(rs.getString("REGIS_DATE"));
			
			return API;
		}
     }
			
}