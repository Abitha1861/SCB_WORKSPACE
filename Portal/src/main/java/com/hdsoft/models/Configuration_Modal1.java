package com.hdsoft.models;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Distribution_list_Creation;
import com.hdsoft.Repositories.Event_Creation;
import com.hdsoft.Repositories.Job_002;
import com.hdsoft.Repositories.Report_Details;
import com.hdsoft.Repositories.User_Journey_Creation;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;


@Component
public class Configuration_Modal1 
{
	protected JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(Configuration_Modal1.class);
	
	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Webservice_Modal Ws;
	
	@Autowired
	public Webservice_call_Modal Wsc;
	
	@Autowired
	public RTSIS_API_Modal RTSIS;
	 
	public JsonObject User_Journey_Creation(final User_Journey_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select count(*) from event003 where MODULEID=? and UJCODE=?";
			
			 int count = Jdbctemplate.queryForObject(sql, new Object[] { Info.getUser_Journey_Type(), Info.getUser_Journey_Code() }, Integer.class);
			 
			 if(count == 0 )
			 {
				 sql = "insert into event003(syscode,Moduleid,ujcode,ujname,freq1, euser, edate) values(?,?,?,?,?,?,?)";
		
				 Jdbctemplate.update(sql, new Object[] { sys.getSuborgcode(), Info.getUser_Journey_Type(), Info.getUser_Journey_Code(), Info.getUser_Journey_Name(), Info.getFrequency_in_Seconds(), "ADMIN", "21-DEC-2020" });
			 }
			 else
			 {
				 sql = "update event003 set ujname=?,freq1=? where Moduleid=? and ujcode=?";
				 
				 Jdbctemplate.update(sql, new Object[] {Info.getUser_Journey_Name(),Info.getFrequency_in_Seconds(),Info.getUser_Journey_Type(),Info.getUser_Journey_Code() } );
			 }
			 
		     for(int i=0;i<Info.getEvent_Codes().length;i++)
			 {
		    	 sql = "select count(*) from event004 where Moduleid=? and ujcode=? and evtcode=?";
					
				 int cont = Jdbctemplate.queryForObject(sql, new Object[] { Info.getUser_Journey_Type(), Info.getUser_Journey_Code(), Info.getEvent_Codes()[i] }, Integer.class);
				 
				 if(cont == 0)
				 {
					 sql = "insert into event004(syscode,Moduleid,ujcode,ujseq,evtcode) values(?,?,?,?,?)";
					
			     	 Jdbctemplate.update(sql, new Object[] { sys.getSuborgcode(), Info.getUser_Journey_Type(), Info.getUser_Journey_Code(), i+1, Info.getEvent_Codes()[i]});
				 }

			     sql = "select count(*) from event001 where MODULEID=? and EVTCODE=? and EVTNAME=?";
					
				 int cnt = Jdbctemplate.queryForObject(sql, new Object[] { Info.getUser_Journey_Type(), Info.getEvent_Codes()[i],Info.getEvent_Names()[i] }, Integer.class);
				 
				 if(cnt == 0)
				 {
					 sql = "insert into event001(syscode,Moduleid,evtcode,evtname,evtsubtype,euser,edate) values(?,?,?,?,?,?,?)";
					 
					 Jdbctemplate.update(sql, new Object[] { sys.getSuborgcode(), Info.getUser_Journey_Type(), Info.getEvent_Codes()[i], Info.getEvent_Names()[i], "U", "ADMIN", "21-DEC-2020" });		
				 }
			 }
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "User Journey Created Successfully");
			 
			 logger.debug(">>>>>>>>>>> User Journey Created Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); System.out.println(e.getLocalizedMessage());
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Event_Action(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			String sql = "select count(*) from event001 where MODULEID=? and EVTCODE=?";
				
			int count = Jdbctemplate.queryForObject(sql, new Object[] { Info.getModule(), Info.getEvent_Code()}, Integer.class);
			 
			details = count == 0 ? Event_Creation(Info) : Event_Update(Info);
			
			if(!IsNull(Info.getSQL_ID()) && !IsNull(Info.getSQL_SEQ_No()) && !IsNull(Info.getModule()))
			{
				sql = "select count(*) from sql001 where SQLID=? and SQLSEQ=? and MODULEID=?";
				
				count = Jdbctemplate.queryForObject(sql, new Object[] {Info.getSQL_ID(), Info.getSQL_SEQ_No(), Info.getModule()}, Integer.class);
			
				if(count == 0)
				{
					 Insert_sql001_Info(Info);
				}
				else
				{
					Update_sql001_Info(Info);
				}				
			}
			else if(Info.getEvent_Method().equals("JV"))
			{
				sql = "delete from job002 where JOBCODE=?";
				
				Jdbctemplate.update(sql, new Object[] { Info.getJOBCODE() });
				
				Insert_javafn_Info(Info);
			}
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Event_Creation(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String query = "insert into event001(syscode,Moduleid,submodule,evtcode,evtname,evttype,evtsubtype,evtmethod,freq1,freq2,freq3,starttime,endtime,holidaygen,"+
			 		 "weekendgen,sysstatuschk,precheck,status,sqlid,apiid,dlistid,SLATIME,countlimit,duplicatechk,skiptime,smsreq,emailreq,callreq,parentevt,"+
				 	 "euser,edate) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
			 Jdbctemplate.update(query, new Object[] { sys.getSuborgcode(), Info.getModule(),Info.getSubmodule(), Info.getEvent_Code(), Info.getEvent_Name(), Info.getEvent_Type(), Info.getEvent_Sub_Type(),
					Info.getEvent_Method(), Info.getFrequency_Type(), Info.getFrequency_Sub_Type(), Info.getFrequency_in_Time(), Info.getStart_Time(), Info.getEnd_Time(),
					Info.getHoliday_Execution(), Info.getWeekend_Execution(), Info.getSystem_Status_Check(), Info.getPre_Check(), Info.getEnabled_or_diabled(), 
					Info.getSQL_ID(), Info.getAPI_ID(),Info.getNotification_Team_ID(), Info.getSLA_Time(), Info.getSLA_Count_Limit(), Info.getDuplicate_Check(),
					Info.getDuplicate_Skip_Time(), Info.getSMS_Required(), Info.getEmail_Required(), Info.getCall_Required(), Info.getParent_Event(),
				    "ADMIN", "21-DEC-2020" });
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Event Created Successfully");
			 
			 logger.debug(">>>>>>>>>>> Event Created Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Event_Update(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String query = "update event001 set evtname=?,evttype=?,evtsubtype=?,evtmethod=?,freq1=?,freq2=?,freq3=?,starttime=?,endtime=?,holidaygen=?, "+
					 	   " weekendgen=?,sysstatuschk=?,precheck=?,status=?,sqlid=?,apiid=?,dlistid=?,SLATIME=?,countlimit=?,duplicatechk=?,skiptime=?,smsreq=?,emailreq=?,callreq=?,parentevt=? "+
						   " where Moduleid=? and evtcode=?";
			
			 Jdbctemplate.update(query, new Object[] { Info.getEvent_Name(), Info.getEvent_Type(), Info.getEvent_Sub_Type(),
					Info.getEvent_Method(), Info.getFrequency_Type(), Info.getFrequency_Sub_Type(), Info.getFrequency_in_Time(), Info.getStart_Time(), Info.getEnd_Time(),
					Info.getHoliday_Execution(), Info.getWeekend_Execution(), Info.getSystem_Status_Check(), Info.getPre_Check(), Info.getEnabled_or_diabled(), 
					Info.getSQL_ID(), Info.getAPI_ID(),Info.getNotification_Team_ID(), Info.getSLA_Time(), Info.getSLA_Count_Limit(), Info.getDuplicate_Check(),
					Info.getDuplicate_Skip_Time(), Info.getSMS_Required(), Info.getEmail_Required(), Info.getCall_Required(), Info.getParent_Event(),
					Info.getModule(), Info.getEvent_Code() });
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message",  "Event Updated Successfully");
			 
			 logger.debug(">>>>>>>>>>> Event Updated Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Updating  event <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Insert_sql001_Info(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{	
			String query = "insert into sql001(SYSCODE,SQLID,SQLSEQ,SQLTYPE,SQLSUBTYPE,MODULEID,SQLQRY,SQLDESC,ARGUMENT1,ARGUMENT2,ARGUMENT3,ARGUMENT4,ARGUMENT5) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
			Jdbctemplate.update(query, new Object[] { sys.getSuborgcode(), Info.getSQL_ID(), Info.getSQL_SEQ_No(), Info.getSQL_Method(), Info.getSQL_Sub_Method(),Info.getModule(), 
						Info.getSQL_Query(), Info.getSQL_Name(), Info.getCBD(), Info.getAC_NO(), Info.getCUS_No(), Info.getTRAN_REF(), Info.getTrans_Amount()} );
		
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Sql001 Created Successfully");
			 
			 logger.debug(">>>>>>>>>>> Event Created Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Update_sql001_Info(final Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			String sql = "update sql001 set SQLSEQ=?,SQLTYPE=?,SQLSUBTYPE=?,SQLQRY=?,SQLDESC=?,ARGUMENT1=?,ARGUMENT2=?,ARGUMENT3=?,ARGUMENT4=?,ARGUMENT5=? where SQLID=? and MODULEID=?";
			
			Jdbctemplate.update(sql, new Object[] { Info.getSQL_SEQ_No(), Info.getSQL_Method(), Info.getSQL_Sub_Method(), Info.getSQL_Query(), Info.getSQL_Name(),
						Info.getCBD(), Info.getAC_NO(), Info.getCUS_No(), Info.getTRAN_REF(), Info.getTrans_Amount(), Info.getSQL_ID(), Info.getModule() });
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message",  "sql001 Updated Successfully");
			 
			 logger.debug(">>>>>>>>>>> sql001 Updated Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Updating  sql001  <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Insert_javafn_Info(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{	
			String query = "insert into job002(SUBORGCODE,SYSCODE,JOBCODE,METHODNAME,INPUTPARAM,OUTPUTPARAM,PARAMETER1,PARAMETER2,PARAMETER3,PARAMETER4,PARAMETER5) values(?,?,?,?,?,?,?,?,?,?,?)";
			
			Jdbctemplate.update(query, new Object[] { sys.getSuborgcode(), sys.getSyscode(), Info.getJOBCODE(), Info.getMETHODNAME(), Info.getINPUTPARAM(), Info.getOUTPUTPARAM(), 
						Info.getPARAMETER1(), Info.getPARAMETER2(), Info.getPARAMETER3(), Info.getPARAMETER4(), Info.getPARAMETER5() } );
		
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Sql001 Created Successfully");
			 
			 logger.debug(">>>>>>>>>>> Event Created Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Update_javafn_Info(final Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			String sql = "update job002 set METHODNAME=?,INPUTPARAM=?,OUTPUTPARAM=?,PARAMETER1=?,PARAMETER2=?,PARAMETER3=?,PARAMETER4=?,PARAMETER5=? where JOBCODE=?";
			
			Jdbctemplate.update(sql, new Object[] { Info.getMETHODNAME(), Info.getINPUTPARAM(), Info.getOUTPUTPARAM(), Info.getPARAMETER1(), Info.getPARAMETER2(),
						Info.getPARAMETER3(), Info.getPARAMETER4(), Info.getPARAMETER5(), Info.getJOBCODE() });
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message",  "sql001 Updated Successfully");
			 
			 logger.debug(">>>>>>>>>>> sql001 Updated Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Updating  sql001  <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Get_Event001(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select * from event001 where MODULEID=? and EVTCODE=?";
			
			 List<Event_Creation> obj = Jdbctemplate.query(sql, new Object[] { Info.getModule(), Info.getEvent_Code() }, new Event001_Mapper());
			
			 if(obj.size() != 0)
 			 {
				 details.addProperty("Module", obj.get(0).getModule());
				 details.addProperty("Submodule", obj.get(0).getSubmodule());
				 details.addProperty("Event_Code", obj.get(0).getEvent_Code());
				 details.addProperty("Event_Name", obj.get(0).getEvent_Name());
				 details.addProperty("Event_Type", obj.get(0).getEvent_Type());
				 details.addProperty("Event_Sub_Type", obj.get(0).getEvent_Sub_Type());
				 details.addProperty("Event_Method", obj.get(0).getEvent_Method());
				 details.addProperty("Frequency_Type", obj.get(0).getFrequency_Type());
				 details.addProperty("Frequency_Sub_Type", obj.get(0).getFrequency_Sub_Type());
				 details.addProperty("Frequency_in_Time", obj.get(0).getFrequency_in_Time());
				 details.addProperty("Start_Time", obj.get(0).getStart_Time());
				 details.addProperty("End_Time", obj.get(0).getEnd_Time());
				 details.addProperty("Holiday_Execution", obj.get(0).getHoliday_Execution());
				 details.addProperty("Weekend_Execution", obj.get(0).getWeekend_Execution());
				 details.addProperty("System_Status_Check", obj.get(0).getSystem_Status_Check());
				 details.addProperty("Enabled_or_diabled", obj.get(0).getEnabled_or_diabled());
				 details.addProperty("Pre_Check", obj.get(0).getPre_Check());
				 details.addProperty("Parent_Event", obj.get(0).getParent_Event());
				 details.addProperty("Notification_Team_ID", obj.get(0).getNotification_Team_ID());
				 details.addProperty("SLA_Time", obj.get(0).getSLA_Time());
				 details.addProperty("SLA_Count_Limit", obj.get(0).getSLA_Count_Limit());
				 details.addProperty("Duplicate_Check", obj.get(0).getDuplicate_Check());
				 details.addProperty("Duplicate_Skip_Time", obj.get(0).getDuplicate_Skip_Time());
				 details.addProperty("SMS_Required", obj.get(0).getSMS_Required());
				 details.addProperty("Email_Required", obj.get(0).getEmail_Required());
				 details.addProperty("Call_Required", obj.get(0).getCall_Required());
				 details.addProperty("SQL_ID", obj.get(0).getSQL_ID());
				 details.addProperty("API_ID", obj.get(0).getAPI_ID());
				 
				 sql = "select * from sql001 where SYSCODE=? and MODULEID=? and SQLID=?";
					
				 List<Event_Creation> sql_info = Jdbctemplate.query(sql, new Object[] { sys.getSuborgcode(), Info.getModule(), obj.get(0).getSQL_ID() }, new Sql001_Mapper());
				
				 if(sql_info.size() != 0)
	 			 {
					 details.addProperty("SQL_Name", sql_info.get(0).getSQL_Name());
					 details.addProperty("SQL_SEQ_No", sql_info.get(0).getSQL_SEQ_No());
					 details.addProperty("SQL_Method", sql_info.get(0).getSQL_Method());
					 details.addProperty("SQL_Sub_Method", sql_info.get(0).getSQL_Sub_Method());
					 details.addProperty("SQL_Query", sql_info.get(0).getSQL_Query());
					 details.addProperty("CBD", sql_info.get(0).getCBD());
					 details.addProperty("AC_NO", sql_info.get(0).getAC_NO());
					 details.addProperty("CUS_No", sql_info.get(0).getCUS_No());
					 details.addProperty("TRAN_REF", sql_info.get(0).getTRAN_REF());
					 details.addProperty("Trans_Amount", sql_info.get(0).getTrans_Amount());
	 			 }
				 
				 sql = "select * from job002 where SUBORGCODE=? and PARAMETER1=?";
					
				 List<Job_002> Java_Info = Jdbctemplate.query(sql, new Object[] { sys.getSuborgcode(), obj.get(0).getEvent_Code() }, new Job002_Mapper());
				
				 if(Java_Info.size() != 0)
	 			 {
					 details.add("Job_Info", new Gson().toJsonTree(Java_Info));
	 			 }
 			 }
			 
			 details.addProperty("Result", obj.size() != 0 ? "Success" : "Failed");
			 details.addProperty("Message", obj.size() != 0 ? "User Journey Found" : "User Journey Not Found");
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
	
	public JsonObject Get_Fixed_and_NonStop_Events_Event001(String MODULEID) 
	{
		JsonObject details = new JsonObject();
		
		JsonArray Fixed_Events = new JsonArray();
		
		JsonArray Nonstop_Events = new JsonArray();
		
		try
		{
			 String sql = "select * from event001 where MODULEID=? and (EVTSUBTYPE=? or EVTSUBTYPE=?) order by EVTSUBTYPE";
			
			 List<Event_Creation> obj = Jdbctemplate.query(sql, new Object[] { MODULEID, "F", "N" }, new Event001_Mapper());
			
			 for(int i=0;i<obj.size();i++)
			 {
				 JsonObject info = new JsonObject();
				 
				 info.addProperty("Module", obj.get(i).getModule());
				 info.addProperty("Event_Code", obj.get(i).getEvent_Code());
				 info.addProperty("Event_Name", obj.get(i).getEvent_Name());
				 info.addProperty("Event_Type", obj.get(i).getEvent_Type());
				 info.addProperty("Event_Sub_Type", obj.get(i).getEvent_Sub_Type());
				 info.addProperty("Event_Method", obj.get(i).getEvent_Method());
				 info.addProperty("Frequency_Type", obj.get(i).getFrequency_Type());
				 info.addProperty("Frequency_Sub_Type", obj.get(i).getFrequency_Sub_Type());
				 info.addProperty("Frequency_in_Time", obj.get(i).getFrequency_in_Time());
				 info.addProperty("Start_Time", obj.get(i).getStart_Time());
				 info.addProperty("End_Time", obj.get(i).getEnd_Time());
				 info.addProperty("Holiday_Execution", obj.get(i).getHoliday_Execution());
				 info.addProperty("Weekend_Execution", obj.get(i).getWeekend_Execution());
				 info.addProperty("System_Status_Check", obj.get(i).getSystem_Status_Check());
				 info.addProperty("Enabled_or_diabled", obj.get(i).getEnabled_or_diabled());
				 info.addProperty("Pre_Check", obj.get(i).getPre_Check());
				 info.addProperty("Parent_Event", obj.get(i).getParent_Event());
				 info.addProperty("Notification_Team_ID", obj.get(i).getNotification_Team_ID());
				 info.addProperty("SLA_Time", obj.get(i).getSLA_Time());
				 info.addProperty("SLA_Count_Limit", obj.get(i).getSLA_Count_Limit());
				 info.addProperty("Duplicate_Check", obj.get(i).getDuplicate_Check());
				 info.addProperty("Duplicate_Skip_Time", obj.get(i).getDuplicate_Skip_Time());
				 info.addProperty("SMS_Required", obj.get(i).getSMS_Required());
				 info.addProperty("Email_Required", obj.get(i).getEmail_Required());
				 info.addProperty("Call_Required", obj.get(i).getCall_Required());
				 info.addProperty("SQL_ID", obj.get(i).getSQL_ID());
				 info.addProperty("API_ID", obj.get(i).getAPI_ID());
				 
				 if(obj.get(i).getEvent_Sub_Type().equals("N")) 
				 {
					 Nonstop_Events.add(info); 
				 }
				 
				 if(obj.get(i).getEvent_Sub_Type().equals("F")) 
				 {
					 Fixed_Events.add(info); 
				 } 
 			 }
			 
			 details.add("Fixed_Events", Fixed_Events);
			 details.add("Nonstop_Events", Nonstop_Events);
			 
			 details.addProperty("Result", obj.size() != 0 ? "Success" : "Failed");
			 details.addProperty("Message", obj.size() != 0 ? "Events Found" : "Events Not Found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving event001 <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Get_Event003(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select * from event003 where MODULEID=? and UJCODE=?";
			
			 List<Event_Creation> obj = Jdbctemplate.query(sql, new Object[] { Info.getModule(), Info.getEvent_Code() }, new Event001_Mapper());
			
			 if(obj.size() != 0)
 			 {
				 details.addProperty("Module", obj.get(0).getModule());
				 details.addProperty("Event_Code", obj.get(0).getEvent_Code());
				 details.addProperty("Event_Name", obj.get(0).getEvent_Name());
				 details.addProperty("Event_Type", obj.get(0).getEvent_Type());
				 details.addProperty("Event_Sub_Type", obj.get(0).getEvent_Sub_Type());
				 details.addProperty("Event_Method", obj.get(0).getEvent_Method());
				 details.addProperty("Frequency_Type", obj.get(0).getFrequency_Type());
				 details.addProperty("Frequency_Sub_Type", obj.get(0).getFrequency_Sub_Type());
				 details.addProperty("Frequency_in_Time", obj.get(0).getFrequency_in_Time());
				 details.addProperty("Start_Time", obj.get(0).getStart_Time());
				 details.addProperty("End_Time", obj.get(0).getEnd_Time());
				 details.addProperty("Holiday_Execution", obj.get(0).getHoliday_Execution());
				 details.addProperty("Weekend_Execution", obj.get(0).getWeekend_Execution());
				 details.addProperty("System_Status_Check", obj.get(0).getSystem_Status_Check());
				 details.addProperty("Enabled_or_diabled", obj.get(0).getEnabled_or_diabled());
				 details.addProperty("Pre_Check", obj.get(0).getPre_Check());
				 details.addProperty("Parent_Event", obj.get(0).getParent_Event());
				 details.addProperty("Notification_Team_ID", obj.get(0).getNotification_Team_ID());
				 details.addProperty("SLA_Time", obj.get(0).getSLA_Time());
				 details.addProperty("SLA_Count_Limit", obj.get(0).getSLA_Count_Limit());
				 details.addProperty("Duplicate_Check", obj.get(0).getDuplicate_Check());
				 details.addProperty("Duplicate_Skip_Time", obj.get(0).getDuplicate_Skip_Time());
				 details.addProperty("SMS_Required", obj.get(0).getSMS_Required());
				 details.addProperty("Email_Required", obj.get(0).getEmail_Required());
				 details.addProperty("Call_Required", obj.get(0).getCall_Required());
				 details.addProperty("SQL_ID", obj.get(0).getSQL_ID());
				 details.addProperty("API_ID", obj.get(0).getAPI_ID());
 			 }
			 
			 details.addProperty("Result", obj.size() != 0 ? "Success" : "Failed");
			 details.addProperty("Message", obj.size() != 0 ? "User Journey Found" : "User Journey Not Found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving event001 <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonArray Get_Event001_Eventcodes(String term, String module) 
	{
		JsonArray Event_Codes = new JsonArray();
		
		try
		{
			 String sql = "select EVTCODE from event001 where MODULEID=? and (EVTCODE LIKE upper(?) or EVTCODE LIKE lower(?))";
			
			 List<String> obj = Jdbctemplate.queryForList(sql, new Object[] { module, "%"+term+"%", "%"+term+"%"}, String.class);
		 
			 for(int i=0; i<obj.size();i++)
 			 {
				 JsonObject Informations = new JsonObject();

				 Informations.addProperty("label", obj.get(i));
				 
				 Event_Codes.add(Informations);
 			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving Event Codes <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 System.out.println(Event_Codes);
		 
		 return Event_Codes;
	}
	
	public JsonArray Get_User_Journry_Eventcodes_from_event001(String Module_Id, String term) 
	{
		JsonArray Event_Codes = new JsonArray();
		
		try
		{
			 String sql = "select * from event001 where MODULEID=? and EVTSUBTYPE=? and (EVTCODE LIKE upper(?) or EVTCODE LIKE lower(?))";
			
			 List<Event_Creation> obj = Jdbctemplate.query(sql, new Object[] { Module_Id, "U", "%"+term+"%", "%"+term+"%"}, new Event001_Mapper());
		 
			 for(int i=0; i<obj.size();i++)
 			 {
				 JsonObject Informations = new JsonObject();

				 Informations.addProperty("id", obj.get(i).getEvent_Name());
				 Informations.addProperty("label", obj.get(i).getEvent_Code());
				 
				 Event_Codes.add(Informations);
 			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving Event Codes <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return Event_Codes;
	}
	
	public JsonArray Get_User_Journry_Eventcodes(String MODULEID, String UJCODE) 
	{
		JsonArray Event_Codes = new JsonArray();
		
		try
		{
			 String sql = "select * from event003 where MODULEID=? and (UJCODE LIKE upper(?) or UJCODE LIKE lower(?))";
			
			 List<User_Journey_Creation> obj = Jdbctemplate.query(sql, new Object[] { MODULEID, "%"+UJCODE+"%", "%"+UJCODE+"%" }, new Event003_Mapper());
		 
			 for(int i=0; i<obj.size();i++)
 			 {
				 JsonObject Informations = new JsonObject();

				 Informations.addProperty("id", obj.get(i).getUser_Journey_Name());
				 Informations.addProperty("label", obj.get(i).getUser_Journey_Code());
				 
				 Event_Codes.add(Informations);
 			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving Event Codes <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return Event_Codes;
	}
	
	public JsonArray Get_User_Journry_codes(String term, String MODULEID) 
	{
		JsonArray Event_Codes = new JsonArray();
		
		try
		{
			 String sql = "select * from event003 where MODULEID=? and (UJCODE LIKE upper(?) or UJCODE LIKE lower(?))";
			
			 List<User_Journey_Creation> obj = Jdbctemplate.query(sql, new Object[] { MODULEID, "%"+term+"%", "%"+term+"%"}, new Event003_Mapper());
		 
			 for(int i=0; i<obj.size();i++)
 			 {
				 JsonObject Informations = new JsonObject();

				 Informations.addProperty("id", obj.get(i).getUser_Journey_Name());
				 Informations.addProperty("label", obj.get(i).getUser_Journey_Code());
				 
				 Event_Codes.add(Informations);
 			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving Event Codes <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return Event_Codes;
	}
	
	public JsonObject Get_User_Journry_event_details(String MODULEID, String UJCODE) 
	{
		JsonObject details = new JsonObject();
		
		JsonArray Sequences = new JsonArray();
		
		try
		{
			 String sql = "select * from event003 where MODULEID=? and UJCODE=?";
			
			 List<User_Journey_Creation> obj = Jdbctemplate.query(sql, new Object[] { MODULEID, UJCODE}, new Event003_Mapper());
		 
			 if(obj.size() != 0)
 			 {
				 details.addProperty("MODULEID", obj.get(0).getUser_Journey_Type());
				 details.addProperty("UJCODE", obj.get(0).getUser_Journey_Code());
				 details.addProperty("UJNAME", obj.get(0).getUser_Journey_Name());
				 details.addProperty("FREQ1", obj.get(0).getFrequency_in_Seconds());
				 
				 sql = "select t1.MODULEID, t1.UJCODE, t1.UJSEQ, t1.EVTCODE, t2.EVTNAME from event004 t1, event001 t2 where t1.MODULEID = ? and " + 
				 		"t1.UJCODE=? and t2.EVTSUBTYPE = ? and t1.MODULEID = t2.MODULEID and t1.EVTCODE = t2.EVTCODE order by t1.UJSEQ asc";
					
				 List<User_Journey_Creation> Info = Jdbctemplate.query(sql, new Object[] {MODULEID, UJCODE, "U"}, new Event004_Mapper());

				 for(int i=0;i<Info.size();i++)
				 {
					 JsonObject info = new JsonObject();
					 
					 info.addProperty("MODULEID", Info.get(i).getUser_Journey_Type());
					 info.addProperty("UJCODE", Info.get(i).getUser_Journey_Code());
					 info.addProperty("UJSEQ", Info.get(i).getUser_Journey_Code_Seq());
					 info.addProperty("EVTCODE", Info.get(i).getEvent_Code());
					 info.addProperty("EVTNAME", Info.get(i).getEvent_Name());
					 
					 Sequences.add(info);
				 } 
 			 }
			 
			 details.add("Execution_Sequence", Sequences); 
			 
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "User Journey Created Successfully");
			 
			 logger.debug(">>>>>>>>>>> User Journey Created Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Get_User_Journry_event_details(String MODULEID) 
	{
		JsonObject details = new JsonObject();
		
		
		try
		{
			 String sql = "select * from event003 where MODULEID=?";
			
			 List<User_Journey_Creation> obj = Jdbctemplate.query(sql, new Object[] { MODULEID }, new Event003_Mapper());
		 
			 JsonArray User_Journies = new JsonArray();
			 
			 for(int i=0;i<obj.size();i++)
			 {
				 JsonObject User_Journey = new JsonObject();
				 
				 User_Journey.addProperty("MODULEID", obj.get(i).getUser_Journey_Type());
				 User_Journey.addProperty("UJCODE", obj.get(i).getUser_Journey_Code());
				 User_Journey.addProperty("UJNAME", obj.get(i).getUser_Journey_Name());
				 User_Journey.addProperty("FREQ1", obj.get(i).getFrequency_in_Seconds());
				 
				 User_Journies.add(User_Journey);
			 }
			 
			 details.add("User_Journies", User_Journies); 
			 
			 for(int j=0;j<obj.size();j++)
			 {
				 sql = "select t1.MODULEID, t1.UJCODE, t1.UJSEQ, t1.EVTCODE, t2.EVTNAME from event004 t1, event001 t2 where t1.MODULEID = ? and " + 
					 		"t1.UJCODE=? and t2.EVTSUBTYPE = ? and t1.MODULEID = t2.MODULEID and t1.EVTCODE = t2.EVTCODE order by t1.UJSEQ asc";
						
				 List<User_Journey_Creation> Info = Jdbctemplate.query(sql, new Object[] { MODULEID, obj.get(j).getUser_Journey_Code(), "U" }, new Event004_Mapper());

				 JsonArray Sequences = new JsonArray();
					
				 for(int i=0;i<Info.size();i++)
				 {
					 JsonObject info = new JsonObject();
					 
					 info.addProperty("MODULEID", Info.get(i).getUser_Journey_Type());
					 info.addProperty("UJCODE", Info.get(i).getUser_Journey_Code());
					 info.addProperty("UJSEQ", Info.get(i).getUser_Journey_Code_Seq());
					 info.addProperty("EVTCODE", Info.get(i).getEvent_Code());
					 info.addProperty("EVTNAME", Info.get(i).getEvent_Name());
					 
					 Sequences.add(info);
				 } 
				 
				 details.add(obj.get(j).getUser_Journey_Code(), Sequences); 
 			 }

			 details.addProperty("Result", obj.size() !=0 ? "Success" : "Failed");
			 details.addProperty("Message", obj.size() !=0 ? "User Journey Codes Found" : "User Journey Codes Not Found");		
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs finding User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonArray Get_sql001_SQLIDs(String term, String module) 
	{
		JsonArray SQLIDs = new JsonArray();
		
		try
		{
			 String sql = "select distinct(SQLID) from sql001 where MODULEID=? and (SQLID LIKE upper(?) or SQLID LIKE lower(?))";
			
			 List<String> obj = Jdbctemplate.queryForList(sql, new Object[] { module, "%"+term+"%", "%"+term+"%"}, String.class);
		 
			 for(int i=0; i<obj.size();i++)
 			 {
				 JsonObject Informations = new JsonObject();

				 Informations.addProperty("label", obj.get(i));
				 
				 SQLIDs.add(Informations);
 			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving SQLIDs <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return SQLIDs;
	}
	
	public JsonArray Get_alert001_DLISTIDs(String term) 
	{
		JsonArray DLISTIDs = new JsonArray();
		
		try
		{
			 String sql = "select distinct(DLISTID) from alert006 where DLISTID LIKE upper(?) or DLISTID LIKE lower(?)";
			
			 List<String> obj = Jdbctemplate.queryForList(sql, new Object[] { "%"+term+"%", "%"+term+"%"}, String.class);
		 
			 for(int i=0; i<obj.size();i++)
 			 {
				 JsonObject Informations = new JsonObject();

				 Informations.addProperty("label", obj.get(i));
				 
				 DLISTIDs.add(Informations);
 			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug(">>>>>>>>>>> Exception occurs when retrieving DLISTIDs <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return DLISTIDs;
	}
	
	public JsonObject Distribution_list_Creation(final Distribution_list_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select count(*) from alert006 where DLISTID=?";
			
			 int count = Jdbctemplate.queryForObject(sql, new Object[] { Info.getDistribution_Id()}, Integer.class);
			 
			 if(count == 0)
			 {
				 sql = "insert into alert006(syscode,DLISTID,DLISTNAME) values(?,?,?)";
		
				 Jdbctemplate.update(sql, new Object[] { sys.getSuborgcode(), Info.getDistribution_Id(), Info.getDistribution_Name() });
			 }
			 else
			 {			
				 sql = "DELETE FROM alert001 WHERE DLISTID=?";
				 
				 Object[] params = new Object[] { Info.getDistribution_Id() };  
		
				 Jdbctemplate.update(sql, params);
			 } 
			 
		     for(int i=0;i<Info.getUser_Ids().length;i++)
			 {
				 sql = "insert into alert001(SYSCODE,DLISTID,SLNO,USERID,USERNAME,MOBILENO,EMAILID,DISABLED) values(?,?,?,?,?,?,?,?)";
					
			     Jdbctemplate.update(sql, new Object[] { sys.getSuborgcode(), Info.getDistribution_Id(), i+1, Info.getUser_Ids()[i], Info.getUser_Names()[i],
			    		 			Info.getMobile_Nos()[i], Info.getEmail_Ids()[i], Info.getStatuses()[i] });   
			 }  
		    
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", count == 0  ? "Distribution list Created Successfully" : "Distribution list updated Successfully");
			 
			 logger.debug(">>>>>>>>>>> Distribution list Created/Updated Successfully <<<<<<<<<<<<<<<");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating/Updating Distribution_list <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Get_Distribution_list_details(String DLISTID) 
	{
		JsonObject details = new JsonObject();
		
		JsonArray Distribution_list = new JsonArray();
		
		try
		{
			 String sql = "select t1.DLISTID, t2.DLISTNAME, t1.SLNO, t1.USERID, t1.USERNAME, t1.MOBILENO, t1.EMAILID, t1.DISABLED from " + 
				 		  "alert001 t1, alert006 t2 where t1.DLISTID =? and t1.DLISTID = t2.DLISTID order by t1.SLNO";
			
			 List<Distribution_list_Creation> Info = Jdbctemplate.query(sql, new Object[] { DLISTID }, new Distribution_list_Mapper());
		 
			 if(Info.size() != 0)
 			 {
				 details.addProperty("Distribution_Id", Info.get(0).getDistribution_Id());
				 details.addProperty("Distribution_Name", Info.get(0).getDistribution_Name());
					
				 for(int i=0;i<Info.size();i++)
				 {
					 JsonObject info = new JsonObject();
					 
					 info.addProperty("SLNO", Info.get(i).getSeq_no());
					 info.addProperty("User_Id", Info.get(i).getUser_Id());
					 info.addProperty("User_Name", Info.get(i).getUser_Name());
					 info.addProperty("Mobile_No", Info.get(i).getMobile_No());
					 info.addProperty("Email_Id", Info.get(i).getEmail_Id());
					 info.addProperty("Status", Info.get(i).getStatus());
					 
					 Distribution_list.add(info);
				 } 
 			 }
			 
			 details.add("Distribution_list", Distribution_list); 
			 
			 details.addProperty("Result", Info.size() != 0 ? "Success" : "Failed");
			 details.addProperty("Message", Info.size() != 0 ? "Distribution list Found" : "Distribution list not Found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs when getting Distribution list <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Get_Event_Report(String module,String submodule,String eventcode,String batch_id) 
	{
		JsonObject details = new JsonObject();
		
		try
		{	
			JsonArray details_list = new JsonArray();
			
			 Common_Utils util = new Common_Utils();
			
			 String sql = "select * from event001 where MODULEID=? and SUBMODULE=? and EVTCODE=?";
			
			 final List<Event_Creation> obj = Jdbctemplate.query(sql, new Object[] { module, submodule, eventcode }, new Event001_Mapper());
			
			 if(obj.size() ==0)
			 {
				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", "Event001 details not found");  
				 
				 return details;
			 }
			 
			 String Batch_Id = batch_id;
			 
			 String REPORTSL = ""; String MESSAGE = "";
			 
			 if(Batch_Id.equalsIgnoreCase("Select")) 
			 {
				 List<Event_Creation> Sql_info = new ArrayList<Event_Creation>();
				 
				 if(obj.get(0).getEvent_Method().equals("DB"))
				 {
					 sql = "select * from sql001 where MODULEID=? and SQLID=? and SQLTYPE=?";
						
					 Sql_info = Jdbctemplate.query(sql, new Object[] { obj.get(0).getModule(), obj.get(0).getSQL_ID(), "Procedure" }, new Sql001_Mapper());
					
					 if(Sql_info.size() ==0)
					 {
						 details.addProperty("Result", "Failed");
						 details.addProperty("Message", "Procedure details not found in sql001"); 
						 
						 return details;
					 }
					 else
					 {
						 final List<Event_Creation> sql_info = Sql_info;
						 
						 final String Query = "{call "+sql_info.get(0).getSQL_Query()+"(?,?,?,?,?)}";  
						 
						 Map<String, Object> resultMap = Jdbctemplate.call(new CallableStatementCreator() {
				 				
								public CallableStatement createCallableStatement(Connection connection) throws SQLException {
			 
									CallableStatement CS = connection.prepareCall(Query);
									
									CS.setString(1, sql_info.get(0).getARGUMENT1());
									CS.setString(2, sql_info.get(0).getARGUMENT2());
									CS.setString(3, sql_info.get(0).getARGUMENT3());
									CS.registerOutParameter(4, Types.INTEGER);
									CS.registerOutParameter(5, Types.VARCHAR);
									
									return CS;
							}
			 			 }, get_ProcedureParams());
						 
			 			  REPORTSL = util.ReplaceNull(resultMap.get("O_SERIAL"));
			 			  MESSAGE = util.ReplaceNull(resultMap.get("O_ERRMSG"));
					 }
				 }
				 else if(obj.get(0).getEvent_Method().equals("JV"))
				 {
					 sql = "select * from job002 where PARAMETER1=?";
						
					 List<Job_002> Jv_Info = Jdbctemplate.query(sql, new Object[] { eventcode }, new Job002_Mapper());
					
					 if(Jv_Info.size() ==0)
					 {
						 details.addProperty("Result", "Failed");
						 details.addProperty("Message", "Java function details not found"); 
						 
						 return details;
					 }
					 else
					 {
						 BOT_Suptect_Finance m = new BOT_Suptect_Finance(Jdbctemplate);  //Serial
					 
						 Method method = m.getClass().getDeclaredMethod(Jv_Info.get(0).getMETHODNAME(), String.class, String.class, String.class);
					 
						 JsonObject out = (JsonObject)method.invoke(m, Jv_Info.get(0).getPARAMETER1(), Jv_Info.get(0).getPARAMETER2(), Jv_Info.get(0).getPARAMETER3());
						 
						 if(out.has("Serial"))
						 {
							 REPORTSL = out.get("Serial").getAsString();
							 MESSAGE = "S";
						 }
						 else
						 {
							 details.addProperty("Result", out.get("result").getAsString());
							 details.addProperty("Message", out.get("message").getAsString()); 
							 
							 return details;
						 }
					 }
				 }
			 }
			 else
			 {
				 sql = "SELECT REPORT_SERIAL from RTS003 where servicecd=? and is_pushed=? and id=?";
				 
				 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { eventcode, "0", Batch_Id }, String.class);
				 
				 if(Information.size() !=0)
	 			 {
					 REPORTSL = Information.get(0);
					 MESSAGE = "S";
	 			 }
			 }
			 
 			 if(!MESSAGE.equals("S"))
 			 {
 				 details.addProperty("Result", "Failed");
				 details.addProperty("Message", MESSAGE);
				
				 return details;
 			 }
 		 	 
 		     logger.debug("SERVICEID :::: "+eventcode);
 			 logger.debug("REPORTSL  :::: "+REPORTSL);
 			 logger.debug("MESSAGE   :::: "+MESSAGE);
 			 
 			 sql = "select COLUMN3 from REPORT002 where COLUMN1=? and SERIAL = ? and COLUMN2=? order by COLUMN4";
 			 
 			 List<String> Headers = Jdbctemplate.queryForList(sql, new Object[] { "H", REPORTSL, eventcode }, String.class);
 			  
 			 for(int z = 0; z < Headers.size(); z++)
 			 {
 				 JsonObject Js = new JsonObject();
 				
 				 String Header = Headers.get(z);
 				 
	 			 ArrayList<String> Columns = new ArrayList<String>();   
	 			 
	 			 ArrayList<Integer> Access = new ArrayList<Integer>();  
	 			 
	 			 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1 in (?,?,?) and COLUMN2=? and COLUMN3=?";
	 			
				 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "C", "H", "A", eventcode, Header }, new Report_Mapper());
				 
				 for(int i=0; i<Reports.size();i++)
				 {
					 String COLUMN1 = Reports.get(i).getCOLUMN1();
					 
					 if(COLUMN1.equals("C"))
					 {
						 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
						 
						 for(int j=3; j<=100;j++) 
						 {
							 String Column_value = columns.get("COLUMN"+j).getAsString();
							 
							 if(!util.isNullOrEmpty(Column_value))  
							 {  
								 Columns.add("COLUMN"+j);
							 }
						 }	
					 }
					 else if(COLUMN1.equals("A"))
					 {
						 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
						 
						 for(int j=3; j<=100;j++) 
						 {
							 String Column_value = columns.get("COLUMN"+j).getAsString();
							 
							 if(!util.isNullOrEmpty(Column_value) && Column_value.equals("1"))  
							 {  
								 Access.add(j-4);
							 }
						 }	
						 
						 String vals = Access.toString();
						 
						 vals = vals.replaceAll("\\[", "").replaceAll("\\]","");
						
						 Js.addProperty("Access_details", vals); 
					 }
				 }
				 
				 JsonArray Table_Columns = new JsonArray();   JsonArray Editor_Columns = new JsonArray(); 
				 
				 for(int i=0; i<Reports.size();i++)
				 {
					 String COLUMN1 = Reports.get(i).getCOLUMN1();
					 
					 if(COLUMN1.equals("C"))
					 {
						 JsonElement jsonElement = new Gson().toJsonTree(Reports.get(i));
						 
						 JsonObject Columns_details = jsonElement.getAsJsonObject();
						 
						 for(int j=0; j<Columns.size(); j++)
			 			 {
							 if(Columns_details.has(Columns.get(j)))
							 {
								 JsonObject Column_Info = new JsonObject();  JsonObject Column_Info2 = new JsonObject();
								 
								 Column_Info.addProperty("sTitle", Columns_details.get(Columns.get(j)).getAsString());
								 Column_Info.addProperty("mData", util.Replace_Special_Characters(Columns_details.get(Columns.get(j)).getAsString()));
								 
								 Column_Info2.addProperty("label", Columns_details.get(Columns.get(j)).getAsString());
								 Column_Info2.addProperty("name", util.Replace_Special_Characters(Columns_details.get(Columns.get(j)).getAsString()));
								 
								 Table_Columns.add(Column_Info);
								 
								 Editor_Columns.add(Column_Info2);
							 }
			 			 }
						 
						 Js.add("Columns_details", Table_Columns);   
						 Js.add("Edit_columns_details", Editor_Columns);   
					 }
					 else if(COLUMN1.equals("H"))
					 {
						 Js.add("Heading_details", new Gson().toJsonTree(Reports.get(i))); 
					 }
				 }
	 			
				 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=? and COLUMN3=? order by cast(COLUMN4 as int)";  
		 			
				 Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "D", eventcode, Header}, new Report_Mapper());
				 
				 List<Report_Details> Data_Reports = new ArrayList<Report_Details>();
				 
				 for(int i=0; i<Reports.size();i++)
				 {
					 Data_Reports.add(Reports.get(i));
				 }
				 
				 JsonElement jsonElement = new Gson().toJsonTree(Data_Reports);
				 
				 JsonArray Report_details = jsonElement.getAsJsonArray();
				 
				 String Report_details_= Report_details.toString();
				 
				 for(int i=Table_Columns.size()-1; i>=0; i--)
				 {
					  JsonObject Column_Info = Table_Columns.get(i).getAsJsonObject();
					  
					  String Replace_from = Columns.get(i);   
					  
					  String Replace_by = Column_Info.get("mData").getAsString();  
					  
					  Report_details_ = Report_details_.replaceAll(Replace_from, Replace_by);
				 }
				 
				 Report_details = util.StringToJsonArray(Report_details_); 
				 
				 Js.add("Report_details", Report_details);   
				 
				 details_list.add(Js);	 
	 		}
	 			 
 			details.add("Headers", new Gson().toJsonTree(Headers));	 
 			
 			details.add("details_list", details_list);	 
 			
 			details.addProperty("REPORTSL", REPORTSL);
		 			
			details.addProperty("Result", details_list.size() !=0 ? "Success" : "Failed");
			details.addProperty("Message", details_list.size() !=0 ? "Report data generated Successfully" : "No data found from the Event");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Retrieve_and_Push(Event_Creation Info) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			String sql = "select max(SERIAL) from report002 y where y.column2 = ?";
			 
			 List<String> Serials = Jdbctemplate.queryForList(sql, new Object[] { Info.getEvent_Code() }, String.class);
			 
			 if(Serials.size() !=0)
			 {
				 String REPORTSL = Serials.get(0);
				 
				 details = Convert_Table_data(REPORTSL, Info.getEvent_Code()); 
				 
				 if(details.has("data"))
				 {
					 logger.debug(">>>>>>>>>>> Final format Json Data <<<<<<<<<<<<<<<");
					 
					 logger.debug(details.get("data").getAsJsonObject());
					 
					 String Json_Data = details.get("data").getAsJsonObject().toString();
					 
					 details = RTSIS.Call_RTSIS_API(Json_Data, Info.getEvent_Code());
				 }
				 else
				 {
					 details.addProperty("result", "failed");
					 details.addProperty("stscode", "HP06");
					 details.addProperty("message", "Issue while converting from table data to Json data");  
				 }
			 }
		}
		catch(Exception e)
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		}
		
		return details;
	}
	
	public JsonObject Convert_Table_data(String REPORTSL, String API_CD)  
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject webservice_details = Ws.Get_Webserice_Info(API_CD);
			 
			 String sql = "select PAYLOAD from webservice001 where SERVICECD=?";
			 
			 List<String> Service = Jdbctemplate.queryForList(sql, new Object[] { API_CD }, String.class);

			 if(Service.size() == 0)
			 {
				 logger.debug("Error in Get_Webserice_Info for API ID "+API_CD+" :::: "+webservice_details);
				 	  
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP200");
				 details.addProperty("message", "Webservice details not found for API ID "+API_CD);
				 
				 return details;
			 }
			 
			 JsonObject data = new JsonObject();
			 
			 sql = "select distinct(COLUMN3) from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=?";
	 			
			 List<String> Parents = Jdbctemplate.queryForList(sql, new Object[] { REPORTSL, "H", API_CD }, String.class);
			
			 JsonArray Head_details = new JsonArray();
			 
			 for(String Head : Parents)
			 {
				 sql = "select * from REPORT002 where SERIAL=? and COLUMN1=? and COLUMN2=? and COLUMN3=?";
		 			
				 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "C", API_CD, Head }, new Report_Mapper());
				 
				 int total_columns = 0;  JsonArray Keys = new JsonArray(); JsonArray values = new JsonArray();
				 
				 for(int i=0; i<Reports.size();i++)
				 {
					 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
					 
					 for(int j=5; j<=100;j++) 
					 {
						 String Column_value = columns.get("COLUMN"+j).getAsString();
						 
						 if(!util.isNullOrEmpty(Column_value))  
						 {  
							 total_columns++;  
							 
							 Keys.add("COLUMN"+j);
							 values.add(Column_value);
							
						 }
					 }					
				 }
					
				 JsonObject Js = new JsonObject();
				 
				 Js.addProperty("Name", Head);
				 Js.addProperty("Size", total_columns);
				 Js.add("Keys", Keys);
				 Js.add("Values", values);
				 
				 Head_details.add(Js);
			 }
			
			 String PAYLOAD = webservice_details.get("PAYLOAD").getAsString();
			 
			 JsonObject js = util.StringToJsonObject(PAYLOAD);
			 
			 List<String> Avl_elements = util.get_keys_as_list(js);
			 	 
			 for(int i=0; i<Head_details.size(); i++)
			 {
				 JsonObject hdtl = Head_details.get(i).getAsJsonObject();
				 
				 String Head_Name = hdtl.get("Name").getAsString();
				
				 JsonArray hdf = new JsonArray();
				 
				 if(js.has(Head_Name) && js.get(Head_Name).isJsonArray() && js.get(Head_Name).getAsJsonArray().size()!=0 && Avl_elements.contains(Head_Name))
				 {				 
					 JsonArray jsa = js.get(Head_Name).getAsJsonArray();
					 
					 JsonObject Sampay = jsa.get(0).getAsJsonObject();
					 
					 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=? and COLUMN3=? order by COLUMN4";
			 			
					 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "D", API_CD, Head_Name }, new Report_Mapper());
					 
					 JsonArray txn_dtl = new Gson().toJsonTree(Reports).getAsJsonArray();
					 
					 JsonArray Head_Keys = hdtl.get("Keys").getAsJsonArray();
					 JsonArray Head_Values = hdtl.get("Values").getAsJsonArray();
				 	
					 for(int j=0; j<txn_dtl.size(); j++)
					 {
						 JsonObject dtls = txn_dtl.get(j).getAsJsonObject();
						 
						 if(dtls.get("COLUMN3").getAsString().equalsIgnoreCase(Head_Name))
						 {
							 JsonObject record = new JsonObject();
							 
							 for(int x=0; x<Head_Keys.size(); x++)
							 {
								 String Head_Key = Head_Keys.get(x).getAsString();
								 
								 if(dtls.has(Head_Key))
								 {
									 String key = Head_Values.get(x).getAsString();
									 
									 String type = Sampay.get(key).getAsString();
									
									 if(type.equalsIgnoreCase("string"))
									 {
										 record.addProperty(key, dtls.get(Head_Key).getAsString());
									 }
									 else
									 {										
										 record.addProperty(key, dtls.get(Head_Key).getAsNumber());
									 }
								 }
							 }
							 
							 hdf.add(record); 
						 }	 
					 }
					 
					 data.add(Head_Name, hdf);
				 }	
			 }
			 
			 for(String Avl_element :  Avl_elements)
			 {
				 if(!data.has(Avl_element) && js.has(Avl_element))
				 {
					 data.addProperty(Avl_element, js.get(Avl_element).getAsString());
				 }
			 }
			 
			 details.add("data", data);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Json Data formed successfully !!");
		}
		catch(Exception ex)
		{
			 details.addProperty("result", "failed");
 			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", ex.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Construt_data :::: "+ex.getLocalizedMessage()); 
		}
		
		return details;
	}
	
	public JsonObject Batch_ID( String Eventname) 

	{

		JsonObject details = new JsonObject();

		try

		{

			  String sql ="Select id from RTS003 where servicecd=? and is_pushed=?";


			  List<String> obj = Jdbctemplate.queryForList(sql,new Object[] {Eventname,"0" }, String.class);

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
	
	private List<SqlParameter> get_ProcedureParams()
	{
		List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
	    
		inParamMap.add(new SqlParameter("INFO1", Types.VARCHAR));
		inParamMap.add(new SqlParameter("INFO2", Types.VARCHAR));
		inParamMap.add(new SqlParameter("INFO3", Types.VARCHAR));
		inParamMap.add(new SqlOutParameter("O_SERIAL" , Types.INTEGER));
		inParamMap.add(new SqlOutParameter("O_ERRMSG"  , Types.VARCHAR));
	
		return inParamMap;
	}
	
	private class Report_Mapper implements RowMapper<Report_Details> 
    {
    	Common_Utils util = new Common_Utils(); 
   	
		public Report_Details mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Report_Details obj = new Report_Details();  

			obj.setCOLUMN1(util.ReplaceNull(rs.getString("COLUMN1")));
			obj.setCOLUMN2(util.ReplaceNull(rs.getString("COLUMN2")));
			obj.setCOLUMN3(util.ReplaceNull(rs.getString("COLUMN3")));
			obj.setCOLUMN4(util.ReplaceNull(rs.getString("COLUMN4")));
			obj.setCOLUMN5(util.ReplaceNull(rs.getString("COLUMN5")));
			obj.setCOLUMN6(util.ReplaceNull(rs.getString("COLUMN6")));
			obj.setCOLUMN7(util.ReplaceNull(rs.getString("COLUMN7")));
			obj.setCOLUMN8(util.ReplaceNull(rs.getString("COLUMN8")));
			obj.setCOLUMN9(util.ReplaceNull(rs.getString("COLUMN9")));
			obj.setCOLUMN10(util.ReplaceNull(rs.getString("COLUMN10")));
			obj.setCOLUMN11(util.ReplaceNull(rs.getString("COLUMN11")));
			obj.setCOLUMN12(util.ReplaceNull(rs.getString("COLUMN12")));
			obj.setCOLUMN13(util.ReplaceNull(rs.getString("COLUMN13")));
			obj.setCOLUMN14(util.ReplaceNull(rs.getString("COLUMN14")));
			obj.setCOLUMN15(util.ReplaceNull(rs.getString("COLUMN15")));
			obj.setCOLUMN16(util.ReplaceNull(rs.getString("COLUMN16")));
			obj.setCOLUMN17(util.ReplaceNull(rs.getString("COLUMN17")));
			obj.setCOLUMN18(util.ReplaceNull(rs.getString("COLUMN18")));
			obj.setCOLUMN19(util.ReplaceNull(rs.getString("COLUMN19")));
			obj.setCOLUMN20(util.ReplaceNull(rs.getString("COLUMN20")));
			obj.setCOLUMN21(util.ReplaceNull(rs.getString("COLUMN21")));
			obj.setCOLUMN22(util.ReplaceNull(rs.getString("COLUMN22")));
			obj.setCOLUMN23(util.ReplaceNull(rs.getString("COLUMN23")));
			obj.setCOLUMN24(util.ReplaceNull(rs.getString("COLUMN24")));
			obj.setCOLUMN25(util.ReplaceNull(rs.getString("COLUMN25")));
			obj.setCOLUMN26(util.ReplaceNull(rs.getString("COLUMN26")));
			obj.setCOLUMN27(util.ReplaceNull(rs.getString("COLUMN27")));
			obj.setCOLUMN28(util.ReplaceNull(rs.getString("COLUMN28")));
			obj.setCOLUMN29(util.ReplaceNull(rs.getString("COLUMN29")));
			obj.setCOLUMN30(util.ReplaceNull(rs.getString("COLUMN30")));
			obj.setCOLUMN31(util.ReplaceNull(rs.getString("COLUMN31")));
			obj.setCOLUMN32(util.ReplaceNull(rs.getString("COLUMN32")));
			obj.setCOLUMN33(util.ReplaceNull(rs.getString("COLUMN33")));
			obj.setCOLUMN34(util.ReplaceNull(rs.getString("COLUMN34")));
			obj.setCOLUMN35(util.ReplaceNull(rs.getString("COLUMN35")));
			obj.setCOLUMN36(util.ReplaceNull(rs.getString("COLUMN36")));
			obj.setCOLUMN37(util.ReplaceNull(rs.getString("COLUMN37")));
			obj.setCOLUMN38(util.ReplaceNull(rs.getString("COLUMN38")));
			obj.setCOLUMN39(util.ReplaceNull(rs.getString("COLUMN39")));
			obj.setCOLUMN40(util.ReplaceNull(rs.getString("COLUMN40")));
			obj.setCOLUMN41(util.ReplaceNull(rs.getString("COLUMN41")));
			obj.setCOLUMN42(util.ReplaceNull(rs.getString("COLUMN42")));
			obj.setCOLUMN43(util.ReplaceNull(rs.getString("COLUMN43")));
			obj.setCOLUMN44(util.ReplaceNull(rs.getString("COLUMN44")));
			obj.setCOLUMN45(util.ReplaceNull(rs.getString("COLUMN45")));
			obj.setCOLUMN46(util.ReplaceNull(rs.getString("COLUMN46")));
			obj.setCOLUMN47(util.ReplaceNull(rs.getString("COLUMN47")));
			obj.setCOLUMN48(util.ReplaceNull(rs.getString("COLUMN48")));
			obj.setCOLUMN49(util.ReplaceNull(rs.getString("COLUMN49")));
			obj.setCOLUMN50(util.ReplaceNull(rs.getString("COLUMN50")));
			obj.setCOLUMN51(util.ReplaceNull(rs.getString("COLUMN51")));
			obj.setCOLUMN52(util.ReplaceNull(rs.getString("COLUMN52")));
			obj.setCOLUMN53(util.ReplaceNull(rs.getString("COLUMN53")));
			obj.setCOLUMN54(util.ReplaceNull(rs.getString("COLUMN54")));
			obj.setCOLUMN55(util.ReplaceNull(rs.getString("COLUMN55")));
			obj.setCOLUMN56(util.ReplaceNull(rs.getString("COLUMN56")));
			obj.setCOLUMN57(util.ReplaceNull(rs.getString("COLUMN57")));
			obj.setCOLUMN58(util.ReplaceNull(rs.getString("COLUMN58")));
			obj.setCOLUMN59(util.ReplaceNull(rs.getString("COLUMN59")));
			obj.setCOLUMN60(util.ReplaceNull(rs.getString("COLUMN60")));
			obj.setCOLUMN61(util.ReplaceNull(rs.getString("COLUMN61")));
			obj.setCOLUMN62(util.ReplaceNull(rs.getString("COLUMN62")));
			obj.setCOLUMN63(util.ReplaceNull(rs.getString("COLUMN63")));
			obj.setCOLUMN64(util.ReplaceNull(rs.getString("COLUMN64")));
			obj.setCOLUMN65(util.ReplaceNull(rs.getString("COLUMN65")));
			obj.setCOLUMN66(util.ReplaceNull(rs.getString("COLUMN66")));
			obj.setCOLUMN67(util.ReplaceNull(rs.getString("COLUMN67")));
			obj.setCOLUMN68(util.ReplaceNull(rs.getString("COLUMN68")));
			obj.setCOLUMN69(util.ReplaceNull(rs.getString("COLUMN69")));
			obj.setCOLUMN70(util.ReplaceNull(rs.getString("COLUMN70")));
			obj.setCOLUMN71(util.ReplaceNull(rs.getString("COLUMN71")));
			obj.setCOLUMN72(util.ReplaceNull(rs.getString("COLUMN72")));
			obj.setCOLUMN73(util.ReplaceNull(rs.getString("COLUMN73")));
			obj.setCOLUMN74(util.ReplaceNull(rs.getString("COLUMN74")));
			obj.setCOLUMN75(util.ReplaceNull(rs.getString("COLUMN75")));
			obj.setCOLUMN76(util.ReplaceNull(rs.getString("COLUMN76")));
			obj.setCOLUMN77(util.ReplaceNull(rs.getString("COLUMN77")));
			obj.setCOLUMN78(util.ReplaceNull(rs.getString("COLUMN78")));
			obj.setCOLUMN79(util.ReplaceNull(rs.getString("COLUMN79")));
			obj.setCOLUMN80(util.ReplaceNull(rs.getString("COLUMN80")));
			obj.setCOLUMN81(util.ReplaceNull(rs.getString("COLUMN81")));
			obj.setCOLUMN82(util.ReplaceNull(rs.getString("COLUMN82")));
			obj.setCOLUMN83(util.ReplaceNull(rs.getString("COLUMN83")));
			obj.setCOLUMN84(util.ReplaceNull(rs.getString("COLUMN84")));
			obj.setCOLUMN85(util.ReplaceNull(rs.getString("COLUMN85")));
			obj.setCOLUMN86(util.ReplaceNull(rs.getString("COLUMN86")));
			obj.setCOLUMN87(util.ReplaceNull(rs.getString("COLUMN87")));
			obj.setCOLUMN88(util.ReplaceNull(rs.getString("COLUMN88")));
			obj.setCOLUMN89(util.ReplaceNull(rs.getString("COLUMN89")));
			obj.setCOLUMN90(util.ReplaceNull(rs.getString("COLUMN90")));
			obj.setCOLUMN91(util.ReplaceNull(rs.getString("COLUMN91")));
			obj.setCOLUMN92(util.ReplaceNull(rs.getString("COLUMN92")));
			obj.setCOLUMN93(util.ReplaceNull(rs.getString("COLUMN93")));
			obj.setCOLUMN94(util.ReplaceNull(rs.getString("COLUMN94")));
			obj.setCOLUMN95(util.ReplaceNull(rs.getString("COLUMN95")));
			obj.setCOLUMN96(util.ReplaceNull(rs.getString("COLUMN96")));
			obj.setCOLUMN97(util.ReplaceNull(rs.getString("COLUMN97")));
			obj.setCOLUMN98(util.ReplaceNull(rs.getString("COLUMN98")));
			obj.setCOLUMN99(util.ReplaceNull(rs.getString("COLUMN99")));
			obj.setCOLUMN100(util.ReplaceNull(rs.getString("COLUMN100")));
			
			
			return obj;
		}
    }
	
	private boolean IsNull(String str)
	{
		if(str != null && !str.isEmpty())
		{
			return false;
		}
		else
		{
			return true; 
		}
	}
	private class Event001_Mapper implements RowMapper<Event_Creation> 
    {
		public Event_Creation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Event_Creation Info = new Event_Creation();  
			
			Info.setModule(rs.getString("Moduleid"));
			Info.setSubmodule(rs.getString("Submodule"));
			Info.setEvent_Code(rs.getString("evtcode"));
			Info.setEvent_Name(rs.getString("evtname"));
			Info.setEvent_Type(rs.getString("evttype"));
			Info.setEvent_Sub_Type(rs.getString("evtsubtype"));
			Info.setEvent_Method(rs.getString("evtmethod"));
			Info.setFrequency_Type(rs.getString("freq1"));
			Info.setFrequency_Sub_Type(rs.getString("freq2"));
			Info.setFrequency_in_Time(rs.getString("freq3"));
			Info.setStart_Time(rs.getString("starttime"));
			Info.setEnd_Time(rs.getString("endtime"));
			Info.setHoliday_Execution(rs.getString("holidaygen"));
			Info.setWeekend_Execution(rs.getString("weekendgen")); 
			Info.setSystem_Status_Check(rs.getString("sysstatuschk"));
			Info.setEnabled_or_diabled(rs.getString("status"));
			Info.setPre_Check(rs.getString("precheck"));
			Info.setParent_Event(rs.getString("parentevt"));
			Info.setNotification_Team_ID(rs.getString("dlistid"));
			Info.setSLA_Time(rs.getString("slatime"));
			Info.setSLA_Count_Limit(rs.getString("countlimit"));
			Info.setDuplicate_Check(rs.getString("duplicatechk"));
			Info.setDuplicate_Skip_Time(rs.getString("skiptime"));
			Info.setSMS_Required(rs.getString("smsreq"));
			Info.setEmail_Required(rs.getString("emailreq"));
			Info.setCall_Required(rs.getString("callreq"));
			Info.setSQL_ID(rs.getString("sqlid"));
			Info.setAPI_ID(rs.getString("apiid"));
		
			return Info;
		}
    }
    
    private class Event003_Mapper implements RowMapper<User_Journey_Creation> 
    {
		public User_Journey_Creation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			User_Journey_Creation Info = new User_Journey_Creation();  
			
			Info.setUser_Journey_Type(rs.getString("MODULEID"));
			Info.setUser_Journey_Code(rs.getString("UJCODE"));
			Info.setUser_Journey_Name(rs.getString("UJNAME"));
			Info.setFrequency_in_Seconds(rs.getString("FREQ1"));

			return Info;
		}
    }
    
    private class Event004_Mapper implements RowMapper<User_Journey_Creation> 
    {
		public User_Journey_Creation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			User_Journey_Creation Info = new User_Journey_Creation();  
			
			Info.setUser_Journey_Type(rs.getString("MODULEID"));
			Info.setUser_Journey_Code(rs.getString("UJCODE"));
			Info.setUser_Journey_Code_Seq(rs.getString("UJSEQ"));
			Info.setEvent_Code(rs.getString("EVTCODE"));
			Info.setEvent_Name(rs.getString("EVTNAME"));

			return Info;
		}
    }
    
    private class Sql001_Mapper implements RowMapper<Event_Creation>  
    {
		public Event_Creation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Event_Creation Info = new Event_Creation();  
			
			Info.setSQL_ID(rs.getString("SQLID"));
			Info.setSQL_Name(rs.getString("SQLDESC"));
			Info.setSQL_SEQ_No(rs.getString("SQLSEQ"));
			Info.setSQL_Method(rs.getString("SQLTYPE"));
			Info.setSQL_Sub_Method(rs.getString("SQLSUBTYPE"));
			Info.setModule(rs.getString("MODULEID"));
			Info.setSQL_Query(rs.getString("SQLQRY"));
			Info.setCBD(rs.getString("ARGUMENT1"));
			Info.setAC_NO(rs.getString("ARGUMENT2"));
			Info.setCUS_No(rs.getString("ARGUMENT3"));
			Info.setTRAN_REF(rs.getString("ARGUMENT4"));
			Info.setTrans_Amount(rs.getString("ARGUMENT5"));
			Info.setARGUMENT1(rs.getString("ARGUMENT1"));
			Info.setARGUMENT2(rs.getString("ARGUMENT2"));
			Info.setARGUMENT3(rs.getString("ARGUMENT3"));
			Info.setARGUMENT4(rs.getString("ARGUMENT4"));
			Info.setARGUMENT5(rs.getString("ARGUMENT5"));
			
			return Info;
		}
    }
    
    private class Job002_Mapper implements RowMapper<Job_002>  
    {
    	Common_Utils util = new Common_Utils();
    	
		public Job_002 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Job_002 Info = new Job_002(); 
			
			Info.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			Info.setSYSCODE(util.ReplaceNull(rs.getString("SYSCODE")));
			Info.setJOBCODE(util.ReplaceNull(rs.getString("JOBCODE")));
			Info.setMETHODNAME(util.ReplaceNull(rs.getString("METHODNAME")));
			Info.setINPUTPARAM(util.ReplaceNull(rs.getString("INPUTPARAM")));
			Info.setOUTPUTPARAM(util.ReplaceNull(rs.getString("OUTPUTPARAM")));
			Info.setPARAMETER1(util.ReplaceNull(rs.getString("PARAMETER1")));
			Info.setPARAMETER2(util.ReplaceNull(rs.getString("PARAMETER2")));
			Info.setPARAMETER3(util.ReplaceNull(rs.getString("PARAMETER3")));
			Info.setPARAMETER4(util.ReplaceNull(rs.getString("PARAMETER4")));
			Info.setPARAMETER5(util.ReplaceNull(rs.getString("PARAMETER5")));
			
			return Info;
		}
    }
    
    private class Distribution_list_Mapper implements RowMapper<Distribution_list_Creation>  
    {
		public Distribution_list_Creation mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Distribution_list_Creation Info = new Distribution_list_Creation();  
			
			Info.setDistribution_Id(rs.getString("DLISTID"));
			Info.setDistribution_Name(rs.getString("DLISTNAME")); 
			Info.setSeq_no(rs.getString("SLNO"));
			Info.setUser_Id(rs.getString("USERID"));
			Info.setUser_Name(rs.getString("USERNAME"));
			Info.setMobile_No(rs.getString("MOBILENO"));
			Info.setEmail_Id(rs.getString("EMAILID"));
			Info.setStatus(rs.getString("DISABLED"));
			
			return Info;
		}
    }
}
