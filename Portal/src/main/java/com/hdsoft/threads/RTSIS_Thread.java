package com.hdsoft.threads;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.RTS005;
import com.hdsoft.models.RTSIS_AutoMan_Modal;
import com.hdsoft.models.Sysconfig;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class RTSIS_Thread
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
	 
	 private static final Logger logger = LogManager.getLogger(RTSIS_Thread.class);
		
	 private final ExecutorService executorService = Executors.newFixedThreadPool(100); // Adjust pool size as needed
	 
	 private final AtomicBoolean isRunning = new AtomicBoolean(false);

	 @Scheduled(cron = "0/5 * * * * *") 
     public void Callback_Thread_Management()  
     {
		 if(isRunning.get())
		 {
		      logger.warn("Previous batch is still running. Skipping this iteration.");
		        
		      return;
		 }

		 isRunning.set(true);
		    
		 try 
		 {
			 List<RTS005> allJobs = Find_Jobs();
			 
		     List<Future<?>> futures = new ArrayList<>();  

             for(RTS005 job : allJobs) 
             {
            	 futures.add(executorService.submit(() -> processJob(job))); // Submit each job for concurrent processing
             }   
             
             for(Future<?> future : futures) 
             {  
                 try 
                 {  
                     future.get(120, TimeUnit.SECONDS);  // Timeout for each job  
                 } 
                 catch (TimeoutException e) 
                 {  
                     logger.warn("Job execution timeout! Moving to next job.");  
                 } 
                 catch (Exception e) 
                 {  
                     logger.error("Job execution error: " + e.getMessage(), e);  
                 }  
             }  
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception :::: "+e.getLocalizedMessage());
		 }
		 finally 
		 {
		     isRunning.set(false);
		 }
     }
	 
	 @PreDestroy
	 public void shutdown() 
	 {
	        logger.info("Shutting down ExecutorService...");
	        
	        executorService.shutdown();
	        
	        try 
	        {
	            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) 
	            {
	                executorService.shutdownNow();
	            }
	        } 
	        catch (InterruptedException e) 
	        {
	            logger.error("Shutdown interrupted: " + e.getMessage(), e);
	            executorService.shutdownNow();
	        }
	  } 
	 
	 public void processJob(RTS005 job) 
	 {
		 try 
		 {
			 JsonObject details = new JsonObject();
			 
			 JsonObject Update_details = Update_Job(job);
			 
			 details.addProperty("Update_RTS005", Update_details.toString());
			  
			 JsonObject Out_details = Method_Finder(job); 
             
             details.addProperty("Method_Finder", Out_details.toString());
             
             logger.debug("Method_Finder out ::: "+Out_details);
                         	 
             if(Out_details.get("Result").getAsString().equals("Success"))
     	     {
            	 JsonObject Del_details = Delete_Job(job);
            	 
            	 details.addProperty("Delete_Job", Del_details.toString());
            	 
            	 logger.debug("Delete_Job out ::: "+Del_details);       
     	     }
             
             logger.debug("Dedictated out ::: "+details);   
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception :::: "+e.getLocalizedMessage());
		 }
	 }
	
	public JsonObject Method_Finder(RTS005 Job) 
	{
		 JsonObject details = new JsonObject();
		 
		 try 
		 {
			 boolean Status = false;
			
			 if(Job.getPAYTYPE().equals("RTSIS"))  
	         {
				 logger.debug(">>>>>>>>>>> Execution Started for "+Job.getREFNO()+ " / " +Job.getREPORTSERIAL()+" / "+ Job.getSTARTSL() +" / "+Job.getENDSL()+ " <<<<<<<<<<<<<<<");
	     	  
	     	     details = RTSIS.Request_Processer(Job);
				 
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
	
	public JsonObject Update_Job(RTS005 Job)
	{
		JsonObject details =  new JsonObject();
		String report = Job.getREPORTSERIAL();
		String start = Job.getSTARTSL();
		String end = Job.getENDSL();
		String reqsl = Job.getREQSL();
	
		try 
		{
			 String Sql = "update RTS005 set STATUS=? where REFNO=? and REQSL=? and REPORTSERIAL=? and STARTSL=? and ENDSL=? and STATUS=?";
			
			 int status = Jdbctemplate.update(Sql, new Object[] { "U", Job.getREFNO(), reqsl, report, start, end, "C" });
			 
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
	
	public JsonObject Delete_Job(RTS005 Job)
	{
		JsonObject details =  new JsonObject();
		String report = Job.getREPORTSERIAL();
		String start = Job.getSTARTSL();
		String end = Job.getENDSL();
		String reqsl = Job.getREQSL();
		
		try 
		{
			String Sql =  "Delete from RTS005 where PAYTYPE=? and REFNO=? and REQSL=? and REPORTSERIAL=? and STARTSL=? and ENDSL=? and STATUS=?";
			
			int status = Jdbctemplate.update(Sql, new Object[] { Job.getPAYTYPE(), Job.getREFNO(), reqsl, report, start, end, "U" });	
			
			details.addProperty("Result", status == 1 ? "Success" : "Failed");
			details.addProperty("Message", status == 1 ? "Record Deleted from RTS005 Successfuly !!" : "Record Not Deleted !!");
		}
		catch (Exception e) 
		{
			details.addProperty("Result", "Failed");
			details.addProperty("Message", e.getLocalizedMessage());
			
			logger.debug("Exception :::: "+e.getLocalizedMessage());
		} 
		
		return details;
	}
	
	public List<RTS005> Find_Jobs()
	{
		List<RTS005> Threads = new ArrayList<RTS005>();

		try 
		{
			 String SUBORGCODE = sys.getSuborgcode();
			 
			 String sql = "SELECT MTYPE from prop001 where chcode = ? and MODULEID = ?";
				 
			 List<String> Info = Jdbctemplate.queryForList(sql, new Object[] { "DV", "BATCH_SIZE" }, String.class);
			  
			 int BATCH_SIZE =  Info.size() !=0  ? Integer.parseInt(Info.get(0)) : 30;  // get batch size
			
			 sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
				
			 List<String> result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", sys.getHostAddress() } , String.class);
				 
			 String Status = result.size() !=0 ? result.get(0) : "Q";   // get Queue name Q or Q1
			 
			 String Sql = "Select * from RTS005 where SUBORGCODE=? and STATUS=? and rownum <= ? order by reqsl";
			
			 Threads = Jdbctemplate.query(Sql, new Object[] { SUBORGCODE, Status, BATCH_SIZE }, new DThread_Mapper());
			 
			 if(Threads.size() > 0)
			 {
				 Sql = "update RTS005 set STATUS=? where SUBORGCODE=? and reqsl between ? and ? and STATUS=?";
				 
				 Jdbctemplate.update(Sql, new Object[] { "C", SUBORGCODE, Threads.get(0).getREQSL(), Threads.get(Threads.size()-1).getREQSL(), Status }); 
			 }
		}
		catch(Exception e) 
		{
			logger.debug("Exception :::: "+e.getLocalizedMessage());
		} 
		
		return Threads;
	}

	 public class DThread_Mapper implements RowMapper<RTS005> 
     {
		public RTS005 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			RTS005 SQL = new RTS005();  
			
			SQL.setSUBORGCODE(rs.getString("SUBORGCODE"));
			SQL.setSYSCODE(rs.getString("SYSCODE"));
			SQL.setPAYTYPE(rs.getString("PAYTYPE"));
			SQL.setREQDATE(rs.getString("REQDATE"));
			SQL.setREFNO(rs.getString("REFNO"));
			SQL.setREQSL(rs.getString("REQSL"));
			SQL.setBATCHID(rs.getString("BATCHID"));
			SQL.setAPICODE(rs.getString("APICODE"));
			SQL.setREPORTSERIAL(rs.getString("REPORTSERIAL"));
			SQL.setSTARTSL(rs.getString("STARTSL"));
			SQL.setENDSL(rs.getString("ENDSL"));
			SQL.setSTATUS(rs.getString("STATUS"));
			
			return SQL;
		}
     }
}
