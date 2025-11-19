package com.hdsoft.models;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class Sysconfig 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(Sysconfig.class);
	
	public String getSuborgcode() 
	{
		String Suborgcode = "";
		
		try
		{
			String Sql = "select suborgcode from sysconf001";
			
			List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			if(result.size() != 0) 
			{
				Suborgcode = result.get(0);
			}
			else
			{
				logger.debug(">>>> suborgcode not configured in sysconf001>>>>");
			}
		}
		catch(Exception ex)
		{
			Suborgcode = "Technical issue in the database, please contact database admin";
			
			logger.debug(">>>> Exception in getSuborgcode >>>>"+ex.getLocalizedMessage());
		}	
		
		return Suborgcode;
	}
	
	public String getSyscode() 
	{
		String SYSCODE = "";
		
		try
		{
			String Sql = "select SYSCODE from sysconf001";
			
			List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			if(result.size() != 0) 
			{
				SYSCODE = result.get(0);
			}
			else
			{
				logger.debug(">>>> syscode not configured in sysconf001>>>>");
			}
		}
		catch(Exception ex)
		{
			logger.debug(">>>> Exception in getSyscode >>>>"+ex.getLocalizedMessage());
		}	
		
		return SYSCODE;
	}
	
	public boolean getDBStatus() 
	{
		boolean out = false;
		
		try
		{
			String Sql = "select sysdate from dual";
			
			List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			if(result.size() != 0) 
			{
				out = true;
			}
		}
		catch(Exception ex)
		{
			//Suborgcode = "Technical issue in the database, please contact database admin";
			
			logger.debug(">>>> Exception in getDBStatus >>>>"+ex.getLocalizedMessage());
		}	
		
		return out;
	}
	
	public String getHostAddress()
	{
	    try 
	    {
	        return InetAddress.getLocalHost().getHostAddress(); 
	    } 
	    catch(UnknownHostException e) 
	    {
	        e.printStackTrace();
	        
	        return "UNKNOWN";
	    }
	    catch(Exception ex)
	    {
	    	ex.printStackTrace();
	        
	        return "UNKNOWN";
	    }
	}
}
