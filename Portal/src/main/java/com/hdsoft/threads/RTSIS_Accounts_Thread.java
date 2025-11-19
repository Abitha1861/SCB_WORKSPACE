package com.hdsoft.threads;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Job_005;
import com.hdsoft.models.BOT_Suptech_GEMS;
import com.hdsoft.models.BOT_Suptect_EBBS;
import com.hdsoft.models.RTSIS_AutoMan_Modal;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class RTSIS_Accounts_Thread
{
	 public JdbcTemplate Jdbctemplate;

	 @Autowired
	 public void setJdbctemplate(HikariDataSource Datasource) 
	 {
		Jdbctemplate = new JdbcTemplate(Datasource);
	 }
	
	 @Autowired
	 public RTSIS_AutoMan_Modal RTSIS;
	 
	 @Autowired
	 public Sysconfig sys;
	 
	 private static final Logger logger = LogManager.getLogger(RTSIS_Accounts_Thread.class);
		
	 //@Scheduled(cron = "0/3 * * * * *") 
     public void Callback_Thread_Management()  
     {
		 try 
		 {
			 List<Job_005> All_Jobs = Find_Jobs();
			 
			 for(int i=0; i<All_Jobs.size(); i++)
		     {
				 JsonObject details = new JsonObject();
				 
				 JsonObject Update_details = Update_Job(All_Jobs.get(i));
				 
				 details.addProperty("Update_Job005", Update_details.toString());
				  
				 JsonObject Out_details = Method_Finder(All_Jobs.get(i)); 
	             
	             details.addProperty("Method_Finder", Out_details.toString());
	             
	             logger.debug("Method_Finder out ::: "+Out_details);
	                         	 
	             if(Out_details.get("Result").getAsString().equals("Success"))
	     	     {
	            	 JsonObject Del_details = Delete_Job(All_Jobs.get(i));
	            	 
	            	 details.addProperty("Delete_Job", Del_details.toString());
	            	 
	            	 logger.debug("Delete_Job out ::: "+Del_details);       
	     	     }
	             
	             logger.debug("Dedictated out ::: "+details);   
		     }
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception :::: "+e.getLocalizedMessage());
		 }
     }
	
	public JsonObject Method_Finder(Job_005 Job) 
	{
		 JsonObject details = new JsonObject();
		 
		 try 
		 {
			 boolean Status = false;
			
			 if(Job.getPAYTYPE().equals("RTSIS"))  
	         {
				 logger.debug(">>>>>>>>>>> Job005 Execution Started for "+Job.getSERVCODE()+ " / " +Job.getREFNO() +" <<<<<<<<<<<<<<<");
	     	  
				 String APICODE = Job.getSERVCODE();
				 
	     	     if(APICODE.equals("RTS117"))  //Individual_Information
	     	     {
	     	    	 String RealtionShipNo = Job.getREFNO();
	     	    	 
	     	      	 BOT_Suptect_EBBS EBBS = new BOT_Suptect_EBBS(Jdbctemplate);
	     	    	
	     	    	 EBBS.Individual_Information(APICODE, "", RealtionShipNo);
	     	     }
	     	     
	     	     if(APICODE.equals("RTS119"))  //companyDetails / Personal data_Corporates
	     	     {
	     	    	  String SCI_LEID = Job.getREFNO();
	     	    	 
	     	    	  BOT_Suptech_GEMS CC = new BOT_Suptech_GEMS(Jdbctemplate);
	     	    	  
	     	    	  CC.companyDetails(APICODE, "", SCI_LEID);
	     	     }
	     	     
	     	     if(APICODE.equals("RTS177"))  //trustDetails  / Personal Data_Trust
	     	     {
	     	    	 String SCI_LEID = Job.getREFNO();
	     	    	
	     	    	 BOT_Suptech_GEMS CC = new BOT_Suptech_GEMS(Jdbctemplate);
	     	    	 
	     	    	 CC.trustDetails(APICODE, "", SCI_LEID);
	     	     }
	     	     
	     	     if(APICODE.equals("RTS179"))  //nonProfitOrgInformation / Personal Data_Non Profit
	     	     {
	     	    	 String SCI_LEID = Job.getREFNO();
	     	    	
	     	    	 BOT_Suptech_GEMS CC = new BOT_Suptech_GEMS(Jdbctemplate);
	     	    	 
	     	    	 CC.nonProfitOrgInformation(APICODE, "", SCI_LEID);
	     	     }
	     	     
	     	     Status = true;
	     	 }
			 
			 details.addProperty("Result", Status ? "Success" : "Failed");
		     details.addProperty("Message", Status ? "Payment Gateway Called Successfuly !!" : "Payment Gateway not Called !!");
		 }
		 catch (Exception e) 
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());
			 
			 logger.debug("Exception :::: "+e.getLocalizedMessage());
		 } 
		 
		 return details;
	}
	
	public JsonObject Update_Job(Job_005 Job)
	{
		JsonObject details =  new JsonObject();
		
		try 
		{ 
			 String Sql = "update job005 set STATUS=? where REFNO=? and REQSL=?";
				
			 int status = Jdbctemplate.update(Sql, new Object[] { "U", Job.getREFNO(), Job.getREQSL()});
			 
			 details.addProperty("Result", status == 1 ? "Success" : "Failed");
		     details.addProperty("Message", status == 1 ? "Record Updated Successfuly !!" : "Record not Updated !!");
		}
		catch (Exception e) 
		{
			details.addProperty("Result", "Failed");
			details.addProperty("Message", e.getLocalizedMessage());
			
			logger.debug("Exception :::: "+e.getLocalizedMessage());
		} 
		
		return details;
	}
	
	public JsonObject Delete_Job(Job_005 Job)
	{
		JsonObject details =  new JsonObject();
		
		try 
		{
			String Sql =  "Delete from job005 where PAYTYPE=? and REFNO=? and REQSL=? and STATUS=?";
			
			int status = Jdbctemplate.update(Sql, new Object[] { Job.getPAYTYPE(), Job.getREFNO(), Job.getREQSL(), "U" });	
			
			details.addProperty("Result", status == 1 ? "Success" : "Failed");
			details.addProperty("Message", status == 1 ? "Record Deleted from Job005 Successfuly !!" : "Record Not Deleted !!");
		}
		catch (Exception e) 
		{
			details.addProperty("Result", "Failed");
			details.addProperty("Message", e.getLocalizedMessage());
			
			logger.debug("Exception :::: "+e.getLocalizedMessage());
		} 
		
		return details;
	}
	
	public List<Job_005> Find_Jobs()
	{
		List<Job_005> Threads = new ArrayList<Job_005>();

		try 
		{
			 String Sql = "Select * from JOB005 where SUBORGCODE=? and STATUS=? order by reqsl ";
			
			 Threads = Jdbctemplate.query(Sql, new Object[] { sys.getSuborgcode(), "Q" }, new DThread_Mapper());
		}
		catch(Exception e) 
		{
			logger.debug("Exception :::: "+e.getLocalizedMessage());
		} 
		
		return Threads;
	}

	public class DThread_Mapper implements RowMapper<Job_005> 
    {
		public Job_005 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Job_005 SQL = new Job_005();  
			
			SQL.setSUBORGCODE(rs.getString("SUBORGCODE"));
			SQL.setSYSCODE(rs.getString("SYSCODE"));
			SQL.setCHCODE(rs.getString("CHCODE"));
			SQL.setPAYTYPE(rs.getString("PAYTYPE"));
			SQL.setREQDATE(rs.getString("REQDATE"));
			SQL.setREFNO(rs.getString("REFNO"));
			SQL.setREQSL(rs.getString("REQSL"));
			SQL.setSTATUS(rs.getString("STATUS"));
			SQL.setTRANTYPE(rs.getString("TRANTYPE"));
			SQL.setREASON(rs.getString("REASON"));
			SQL.setREV_REF(rs.getString("REV_REF"));
			SQL.setSERVCODE(rs.getString("SERVCODE"));
			
			return SQL;
		}
    }
}
