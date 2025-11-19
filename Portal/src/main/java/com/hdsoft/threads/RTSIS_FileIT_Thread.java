package com.hdsoft.threads;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import com.hdsoft.Repositories.Job_005;
import com.hdsoft.models.BOT_Suptech_ACBS;
import com.hdsoft.models.BOT_Suptect_Finance;
import com.hdsoft.models.BOT_Suptect_MARCIS;
import com.hdsoft.models.BOT_Suptect_Trade_DAY0;
import com.hdsoft.models.RTSIS_AutoMan_Modal;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.solace.QueueConsumerJNDI;
import com.hdsoft.utils.FormatUtils;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class RTSIS_FileIT_Thread
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
	 
	 private static final Logger logger = LogManager.getLogger(RTSIS_FileIT_Thread.class);
		
	 private final ExecutorService executorService = Executors.newFixedThreadPool(5); // Adjust pool size as needed
	 
	 private final AtomicBoolean isRunning = new AtomicBoolean(false);
	 
	 @Scheduled(cron = "0 */2 * * * *") 
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
			 List<Job_005> allJobs = Find_Jobs();
			 
			 if(allJobs.isEmpty()) 
			 {
		         logger.info("No jobs found to process.");
		     }
			 else
			 {
				 List<Future<?>> futures = new ArrayList<>();  
				 
				 for(Job_005 job : allJobs) 
	             {
	            	 futures.add(executorService.submit(() -> runWithRetry(job,3))); // // Retry up to 3 times
	             }   
				 
				 // Wait for all submitted jobs to finish
				 for(Future<?> future : futures) 
	             {  
	                 try 
	                 {  
	                     future.get();  // No Timeout for each job  
	                 } 
	                 catch (Exception e) 
	                 {  
	                     logger.error("Job execution error: " + e.getMessage(), e);  
	                 }  
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
	 
	 public void processJob(Job_005 job) 
	 {
		 try 
		 {
			 JsonObject details = new JsonObject();
			 
			 JsonObject Update_details = Update_Job(job);
			 
			 details.addProperty("Update_Job005", Update_details.toString());
			  
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
	
	public JsonObject Method_Finder(Job_005 Job) 
	{
		 JsonObject details = new JsonObject();
		 
		 try 
		 {
			 boolean Status = false;
			
			 if(Job.getPAYTYPE().equals("DAY0"))  
	         {
				 logger.debug(">>>>>>>>>>> Job005 Execution Started for "+Job.getSERVCODE()+ " / " +Job.getREFNO() +" <<<<<<<<<<<<<<<");
				 
				 if(Job.getCHCODE().equalsIgnoreCase("ACBS"))
				 {
					   BOT_Suptech_ACBS ACBS = new BOT_Suptech_ACBS(Jdbctemplate);
					 
					   String FIlepath = Job.getREASON();
					 
					   ACBS.ACBS_File_Processing(Job.getSERVCODE(), "", FIlepath);
				 }
				 
				 if(Job.getCHCODE().equalsIgnoreCase("MARCIS"))
				 {
						 BOT_Suptect_MARCIS MARCIS = new BOT_Suptect_MARCIS(Jdbctemplate);  
						
						 String Filepath = Job.getREASON();
						
						 if(Filepath.toLowerCase().contains("loaninformation") || Job.getSERVCODE().equals("RTS019"))
						 {
							  MARCIS.Marcis_File_Processing("RTS019", "", Filepath);
						 }
						 else if(Filepath.toLowerCase().contains("loantransaction") || Job.getSERVCODE().equals("RTS191"))
						 {
							 MARCIS.Marcis_File_Processing("RTS191", "", Filepath);
						 }
						 else if(Filepath.toLowerCase().contains("overdraft") || Job.getSERVCODE().equals("RTS023"))
						 {
							 MARCIS.Marcis_File_Processing("RTS023", "", Filepath);
						 }
						 else if(Filepath.toLowerCase().contains("undrawnbalance") || Job.getSERVCODE().equals("RTS103"))
						 {
							 MARCIS.Marcis_File_Processing("RTS103", "", Filepath);
						 }
						 else if(Filepath.toLowerCase().contains("writeoff") || Job.getSERVCODE().equals("RTS163"))
						 {
							 MARCIS.Marcis_File_Processing("RTS163", "", Filepath);
						 }
				 }
				 
				 if(Job.getCHCODE().equalsIgnoreCase("TRADE"))
				 {
					  BOT_Suptect_Trade_DAY0 TRADE = new BOT_Suptect_Trade_DAY0(Jdbctemplate); 
					  
					  String Apicode = Job.getSERVCODE();
					  String Filepath = Job.getREASON();
					  
					  if(Apicode.equals("RTS065")) details = TRADE.Outstanding_Acceptance_DAY0(Apicode, "", Filepath);
				   	  if(Apicode.equals("RTS029")) details = TRADE.Customerliabilities_DAY0(Apicode, "", Filepath);
				   	  if(Apicode.equals("RTS077")) details = TRADE.ExportLetterOfCredit_DAY0(Apicode, "", Filepath);
				   	  if(Apicode.equals("RTS081")) details = TRADE.inwardBills_DAY0(Apicode, "", Filepath);
				   	  if(Apicode.equals("RTS075")) details = TRADE.Outstanding_Guarantee_DAY0(Apicode, "", Filepath);
				   	  if(Apicode.equals("RTS083")) details = TRADE.outwardBills_DAY0(Apicode, "", Filepath);
				   	  if(Apicode.equals("RTS079")) details = TRADE.OustandingLetterCredit_DAY0(Apicode, "", Filepath);  		   	 
			   	      if(Apicode.equals("RTS019") && Filepath.toUpperCase().contains("OTP")) details = TRADE.LoanInformation_OTP_DAY0(Apicode, "", Filepath);
			   	      if(Apicode.equals("RTS019") && !Filepath.toUpperCase().contains("OTP")) details = TRADE.LoanInformation_DTP_DAY0(Apicode, "", Filepath);
				 }
				 
				 Status = true;
	         }
			 else  // all other file processing
			 {
				 logger.debug(">>>>>>>>>>> Job005 Execution Started for "+Job.getSERVCODE()+ " / " +Job.getREFNO() +" <<<<<<<<<<<<<<<");
	     	  
				 String APICODE = Job.getSERVCODE();
				 
				 if(APICODE.equals("SOL001"))  // FileIT
				 {
					 String Sql = "select BODY_MSG from request001 where CHCODE=? and MSGTYPE=? and REQDATE=? and UNIREFNO=?";
					 
					 List<String> Info = Jdbctemplate.queryForList(Sql, new Object[] { Job.getCHCODE(), Job.getSERVCODE(), FormatUtils.dynaSQLDate(Job.getREQDATE(), "YYYY-MM-DD"), Job.getREFNO() }, String.class);
					 
					 if(Info.size() !=0)
					 {
						 QueueConsumerJNDI SOL = new QueueConsumerJNDI(Jdbctemplate);
						 
						 SOL.Solace_Router(Job.getCHCODE(), Job.getSERVCODE(), Info.get(0), "", "");
					 }
				 }
				 
				 if(APICODE.equals("RTS003"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.Equity_Investment(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS041"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.Premises_Furniture_and_Equipment(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS043"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.dividendsPayable(APICODE, "", "");
				 }
				  
				 if(APICODE.equals("RTS059"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.accruedTaxes(APICODE, "", "");
				 }
				  
				 if(APICODE.equals("RTS045"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.shareCapital(APICODE, "", "");
				 }
				  
				 if(APICODE.equals("RTS047"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.otherCapitalAccount(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS007"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.otherAsset(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS063"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.unearnedIncome(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS073"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.other_liablities(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS049"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.coreCapitalDeductionsData(APICODE, "", "");
				 }
				 
				 if(APICODE.equals("RTS061"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.subordinatedDebt(APICODE, "", "");
				 }  
			     
				 if(APICODE.equals("RTS115"))  // FileIT
				 {
					 BOT_Suptect_Finance Finance = new BOT_Suptect_Finance(Jdbctemplate);
					 
					 Finance.incomeStatement(APICODE, "", "");
				 } 
			
	     	     Status = true;
			 }
			 
			 details.addProperty("Result", Status ? "Success" : "Failed");
		     details.addProperty("Message", Status ? "Job executed Successfuly !!" : "Job execution failed");
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
			 String Sql = "update job005 set STATUS=? where REFNO=? and REQSL=? and STATUS=?";
				
			 int status = Jdbctemplate.update(Sql, new Object[] { "U", Job.getREFNO(), Job.getREQSL(), "C"});
			 
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
			 String sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
			
			 List<String> result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", sys.getHostAddress() } , String.class);
				 
			 String Status = result.size() !=0 ? result.get(0) : "Q";   // get Queue name Q or Q1
			  
			 String Sql = "Select * from JOB005 where SUBORGCODE=? and PAYTYPE <> ? and STATUS=? and rownum <= ? order by reqsl";
			
			 Threads = Jdbctemplate.query(Sql, new Object[] { sys.getSuborgcode(), "CC",  Status, "5" }, new DThread_Mapper());
			 
			 if(Threads.size() > 0)
			 {
				 Sql = "update JOB005 set STATUS=? where PAYTYPE <> ? and reqsl between ? and ? and STATUS=?";
				 
				 Jdbctemplate.update(Sql, new Object[] { "C", "CC", Threads.get(0).getREQSL(), Threads.get(Threads.size()-1).getREQSL(), Status });
			 }
		}
		catch(Exception e) 
		{
			logger.debug("Exception :::: "+e.getLocalizedMessage());
		} 
		
		return Threads;
	}
	
	public void runWithRetry(Job_005 job, int maxAttempts) 
	{
	    int attempt = 1;
	    
	    while (attempt <= maxAttempts) 
	    {
	        try 
	        {
	            logger.info("Attempt {} for job ID: {}", attempt, job.getREFNO());
	            
	            processJob(job);
	            
	            logger.info("Job ID {} completed successfully on attempt {}", job.getREFNO(), attempt);
	            
	            return; // Job completed successfully, exit loop
	        } 
	        catch (Exception ex) 
	        {
	            logger.warn("Job ID {} failed on attempt {}: {}", job.getREFNO(), attempt, ex.getMessage(), ex);

	            attempt++;
	            
	            if (attempt <= maxAttempts) 
	            {
	                try 
	                {
	                    Thread.sleep(2000); // Optional: Wait 2 seconds before retrying
	                } 
	                catch (InterruptedException ie) 
	                {
	                    Thread.currentThread().interrupt();
	                    
	                    logger.error("Retry sleep interrupted for job ID: {}", job.getREFNO());
	                    
	                    return;
	                }
	            } 
	            else 
	            {
	                logger.error("Job ID {} failed after {} attempts", job.getREFNO(), maxAttempts);
	            }
	        }
	    }
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
