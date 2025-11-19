package com.hdsoft.models;

import java.util.List;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Module_code001;
import com.hdsoft.Repositories.Submodule_code001;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class API_Configuration
{	
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(API_Configuration.class);
	
	@Autowired
	public Sysconfig sys;

	public JsonObject ModuleDesc_DD(Module_code001 Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{	
			 String sql = "select MODULE_DESC from module_code001";
		 
			 List<String> obj = Jdbctemplate.queryForList(sql, String.class);
			 
			 details.add("Report_code_DD", new Gson().toJsonTree(obj));
				  
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
	
	public JsonObject Retreive_submodule_data(Submodule_code001 Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			  String sql = "select distinct(SUBMODULE_DESC) from submodule_code001 where MODULE_NAME =?";

			  List<String> obj = Jdbctemplate.queryForList(sql,new Object[] {Info.getMODULE_NAME()}, String.class);
						  
			  details.add("SubModule_Desc", new Gson().toJsonTree(obj));
			  
			  details.addProperty("Result", "Success");
			  details.addProperty("Message", "details found"); 
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving event001 <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Retreive_Events(String Module, String Submodule) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			  String sql = "select EVTCODE from event001 where MODULEID =? and SUBMODULE=? and STATUS=?";

			  List<String> obj = Jdbctemplate.queryForList(sql,new Object[] { Module,Submodule, "1" }, String.class);
						   
			  details.add("events", new Gson().toJsonTree(obj));
			  
			  details.addProperty("Result", "Success");
			  details.addProperty("Message", "details found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving event001 <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 System.out.println(details);
		 
		 return details;
	}
}
