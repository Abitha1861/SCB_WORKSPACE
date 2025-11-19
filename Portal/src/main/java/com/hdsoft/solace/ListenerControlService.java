package com.hdsoft.solace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
//@Service
public class ListenerControlService 
{
	/*public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	/*@Autowired
	public ListenerControlService(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
    }

    public Map<String, Boolean> getQueueStatusMap() 
    {
    	try
    	{
    		System.out.println("hiiiiiiiiii");
    		
    		String sql2 = "SELECT SERVICECD as queue_name FROM solace001";
			 
	        List<String> info = Jdbctemplate.queryForList(sql2, String.class);
	        
	        System.out.println(info.get(0));
	        
		    String sql = "SELECT SERVICECD as queue_name, ISSHUTDOWN as status FROM solace001";
		 
	        List<Map<String, Object>> results = Jdbctemplate.queryForList(sql);
	        
	        

	        // Convert results to a map of queue names and active status
	        return results.stream().collect(Collectors.toMap(
	            row -> (String) row.get("queue_name"), 
	            row -> (Integer) row.get("status") == 1
	        ));
    	}
    	catch(Exception ex)
    	{
    		ex.getLocalizedMessage();
    	}
       
    	return null;
    } */
}
