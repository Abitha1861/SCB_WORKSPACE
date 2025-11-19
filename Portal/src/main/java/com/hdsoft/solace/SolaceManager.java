package com.hdsoft.solace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Solace;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class SolaceManager 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(SolaceManager.class);
	
	@Autowired
	public TopicSubscriber_bk Topic;
	
	@Autowired
	public QueueConsumer Queue;
	
	@Autowired
	public QueueConsumerJNDI QueueJNDI;
	
	public static List<Solace> SolaceInfo = new ArrayList<Solace>();
	
	@Scheduled(cron = "0/10 * * * * *") 
    public void Solace_Thread_Management()  
    {
		 try 
		 {
			 Common_Utils utils = new Common_Utils();
			 
			 JsonObject js = new JsonObject();
			 
			 for(int i=0; i<SolaceInfo.size(); i++)
		     {
				 JsonObject jk = new Gson().toJsonTree(SolaceInfo.get(i)).getAsJsonObject();
				 
				 js.add(SolaceInfo.get(i).getServcd(), jk);
		     }
			 
			 String sql = "select * from webservice001 w where CHCODE = ? and SERVICECD in (select SERVICECD from solace001 where CHCODE = w.CHCODE)";
			 
			 List<web_service_001> Info = Jdbctemplate.query(sql, new Object[] { "SOLACE" }, new Service_Mapper());
			 
			 for(int i=0; i<Info.size(); i++)
		     {
				 Solace k = new Solace();
				 
				 k.setConnectionId(utils.Generate_Random_String(12));
				 k.setServcd(Info.get(i).getSERVICECD());
				 k.setChcode(Info.get(i).getCHCODE());
				 k.setStartedDate(utils.getCurrentDateTime());
 
				 if(!js.has(Info.get(i).getSERVICECD()))
				 {
					 sql = "select ISSUBSCRIBED from solace001 where CHCODE=? and SERVICECD=?";
					 
					 List<String> Sub = Jdbctemplate.queryForList(sql, new Object[] { Info.get(i).getCHCODE(), Info.get(i).getSERVICECD() }, String.class);
					 
					 if(Sub.size() != 0 && Sub.get(0).equals("1"))
					 {
						 k.setIsEnabled(true);
						 
						 logger.debug("Before subscribing the topic "+Info.get(i).getSERVICECD());
						 
						 String ConnectionId = utils.Generate_Random_String(12);
						 
						 k.setConnectionId(ConnectionId);
						
						 logger.debug("After subscribing the topic "+Info.get(i).getSERVICECD());
						 
						 sql = "update solace001 set ISSUBSCRIBED = ?, CONNECTID=? where CHCODE=? and SERVICECD=?";
						 
						 Jdbctemplate.update(sql, new Object[] { "1", ConnectionId, Info.get(i).getCHCODE(), Info.get(i).getSERVICECD() });
					 }
					 
					 sql = "select ISSHUTDOWN from solace001 where CHCODE=? and SERVICECD=?";
					 
					 List<String> Shut = Jdbctemplate.queryForList(sql, new Object[] { Info.get(i).getCHCODE(), Info.get(i).getSERVICECD() }, String.class);
					 
					 if(Shut.size() !=0 && Shut.get(0).equals("0"))
					 {				 		 
						 k.setIsRunning(true); 		 
						 
						 QueueJNDI.SolaceInfo(Info.get(i).getCHCODE(), Info.get(i).getSERVICECD());
					 }
					 
					 SolaceInfo.add(k);	 	
				 
					 //System.out.println("serv added "+Info.get(i).getSERVICECD());
				 }
				 else
				 {
					// System.out.println("hiiii else");
					 
					// System.out.println("hiiii else");
					 
					 JsonObject SolInfo = js.get(Info.get(i).getSERVICECD()).getAsJsonObject();
					 
					 sql = "select ISSHUTDOWN from solace001 where CHCODE=? and SERVICECD=?";
					 
					 List<String> Shut = Jdbctemplate.queryForList(sql, new Object[] { Info.get(i).getCHCODE(), Info.get(i).getSERVICECD() }, String.class);
					
					 ///boolean IsEnabled = SolInfo.get("IsEnabled").getAsBoolean();
					 boolean IsRunning = SolInfo.get("IsRunning").getAsBoolean();
					
					 if(Shut.size() !=0)
					 {
					 	 if(Shut.get(0).equals("1") && IsRunning) 
					 	 {
					 		 for(int j=0; j<SolaceInfo.size(); j++)
						     {
					 			 if(SolaceInfo.get(j).getServcd().equals(Info.get(i).getSERVICECD()))
					 			 {
					 				//stop the queue consume
					 				 
					 				SolaceInfo.get(j).setIsRunning(false);
					 				
					 				logger.debug("queue stopped listen "+Info.get(i).getSERVICECD());
					 				
					 				break;
					 			 }
						     }
					 		
					 	 }
					 	 else if(Shut.get(0).equals("0") && !IsRunning) 
					 	 {
					 		for(int j=0; j<SolaceInfo.size(); j++)
						     {
					 			 if(SolaceInfo.get(j).getServcd().equals(Info.get(i).getSERVICECD()))
					 			 {
					 				//start the queue consume
					 				 
					 				SolaceInfo.get(j).setIsRunning(true);
					 				
					 				logger.debug("queue started listen "+Info.get(i).getSERVICECD());
					 				
					 				break;
					 			 }
						     }
					 	 }
					 }
					 
					 sql = "select CONNECTID from solace001 where CHCODE=? and SERVICECD=?";
					 
					 List<String> CONNECTID = Jdbctemplate.queryForList(sql, new Object[] { Info.get(i).getCHCODE(), Info.get(i).getSERVICECD() }, String.class);
					
					 if(CONNECTID.size() > 0)
					 {
						 boolean flag = true;
						 
						 for(int j=0; j<SolaceInfo.size(); j++)
					     {
				 			 if(SolaceInfo.get(j).getServcd().equals(Info.get(i).getSERVICECD()))
				 			 {
				 				if(!SolaceInfo.get(j).getConnectionId().equals(CONNECTID.get(0)))
				 				{
				 					flag = false;
				 					
				 					break;
				 				}
				 				
				 			 }
					     }
						 
						 if(!flag)
						 {
							 logger.debug("Again subscribing the topic "+Info.get(i).getSERVICECD());
							 
							 String ConnectionId = utils.Generate_Random_String(12);
							 
							 k.setConnectionId(ConnectionId);
							 
							// Topic.SolaceInfo(Info.get(i).getCHCODE(), Info.get(i).getSERVICECD());
							 
							 logger.debug("After subscribing the topic "+Info.get(i).getSERVICECD());
							 
							// Thread.sleep(5 * 1000);
							 
							 sql = "update solace001 set ISSUBSCRIBED = ?, CONNECTID=? where CHCODE=? and SERVICECD=?";
							 
							 Jdbctemplate.update(sql, new Object[] { "1", ConnectionId, Info.get(i).getCHCODE(), Info.get(i).getSERVICECD() });
							 
							 for(int j=0; j<SolaceInfo.size(); j++)
						     {
					 			 if(SolaceInfo.get(j).getServcd().equals(Info.get(i).getSERVICECD()))
					 			 {
					 				SolaceInfo.get(j).setConnectionId(ConnectionId);
					 					
					 				break;
					 			 }
						     }
							 
							 QueueJNDI.SolaceInfo(Info.get(i).getCHCODE(), Info.get(i).getSERVICECD());
						 }	
					 }
				 }
		     }
			 
			 Thread.sleep(Long.MAX_VALUE);
		 }
		 catch (Exception e) 
		 {
			 logger.debug("Exception :::: "+e.getLocalizedMessage());
		 }
    }
	
	public class Service_Mapper implements RowMapper<web_service_001> 
    {
    	Common_Utils util = new Common_Utils();
    	
		public web_service_001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			web_service_001 API = new web_service_001();  

			API.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			API.setCHCODE(util.ReplaceNull(rs.getString("CHCODE")));
			API.setSERVICECD(util.ReplaceNull(rs.getString("SERVICECD")));
			API.setSERVNAME(util.ReplaceNull(rs.getString("SERVNAME")));
			API.setFORMAT(util.ReplaceNull(rs.getString("FORMAT")));
			API.setPROTOCOL(util.ReplaceNull(rs.getString("PROTOCOL")));
			API.setMETHOD(util.ReplaceNull(rs.getString("METHOD")));
			API.setCHTYPE(util.ReplaceNull(rs.getString("CHTYPE")));
			API.setURI(util.ReplaceNull(rs.getString("URI")));
			API.setPAYLOAD(util.ReplaceNull(rs.getString("PAYLOAD")));
			API.setSIGNPAYLOAD(util.ReplaceNull(rs.getString("SIGNPAYLOAD")));
			API.setHEADERID(util.ReplaceNull(rs.getString("HEADERID")));
			API.setFLOW(util.ReplaceNull(rs.getString("FLOW")));
			API.setJOBREQ(util.ReplaceNull(rs.getString("JOBREQ")));
			
			return API;
		}
     }
}
