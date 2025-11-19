package com.hdsoft.models;

import java.util.List;

import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class Session_Model
{
	
public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Sysconfig sys;
	
	private static final Logger logger = LogManager.getLogger(Session_Model.class);
		
	public boolean IsSessionValid(HttpSession session, String PARENT, String CHILD) 
    {	 
		 boolean Result = false;
		 
		 try
		 {
			 logger.debug(">>>> IsSessionValid >>>>>>> "+  session.getAttribute("sesRole").toString() +"---"+ CHILD  +"---"+ PARENT );
			 
			 if(session.getAttribute("sesSessionID") !=null && session.getAttribute("sesUserId") !=null && session.getAttribute("sesDomainID") !=null) 
		     {
	   	 		 String Sql = "select MENU_STATUS from menu005 where USERID_ROLE=? and MENU_HEADER=? and MENU_PARENT_HEADER=? and MENU_STATUS=1 and SYSCODE=?";
	   	 		 
	   	 		 List<String> Count = Jdbctemplate.queryForList(Sql, new Object[] { session.getAttribute("sesRole").toString(), CHILD, PARENT, sys.getSuborgcode() }, String.class);
	   	
	   	 		 if(Count.size() != 0)
	   	 		 {
	   	 			 Result = true;
	   	 		 }
	   	 		 
	   	 		logger.debug("auth county ::: "+Count.size());
	   	 		logger.debug("auth result ::: "+Result);
		     }
		 }
		 catch(Exception ex)
		 {
			 logger.debug(">>>> Exception in Session Model >>>>>>> "+ex.getLocalizedMessage());
		 }
		 
   	 	 return Result;
    }
	
	
	public boolean IsSessionValid(HttpSession session) 
    {	 
		 logger.debug("sesSessionID : "+session.getAttribute("sesSessionID"));
		 logger.debug("sesUserId : "+session.getAttribute("sesUserId"));
		 logger.debug("sesDomainID : "+session.getAttribute("sesDomainID"));
		 
   	 	 if(session.getAttribute("sesSessionID") !=null && session.getAttribute("sesUserId") !=null && session.getAttribute("sesDomainID") !=null) 
	     {
   	 		 return true;
	     }
	     else
	     {
	    	 return false;
	     }
    }
}
