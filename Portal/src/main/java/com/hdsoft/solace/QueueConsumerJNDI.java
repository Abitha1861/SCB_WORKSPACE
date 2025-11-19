
package com.hdsoft.solace;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Solace;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.models.BOT_Suptech_ACBS;
import com.hdsoft.models.BOT_Suptech_Cheque;
import com.hdsoft.models.BOT_Suptect_CADD;
import com.hdsoft.models.BOT_Suptect_CASH;
import com.hdsoft.models.BOT_Suptect_EBBS;
import com.hdsoft.models.BOT_Suptect_MARCIS;
import com.hdsoft.models.BOT_Suptect_Trade;
import com.hdsoft.models.FileIT_Modal;
import com.hdsoft.models.MinIo_Modal;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.models.Webservice_Modal;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class QueueConsumerJNDI implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public QueueConsumerJNDI(JdbcTemplate Jdbc)
	{ 
		Jdbctemplate = Jdbc;
	}
	
	public QueueConsumerJNDI() { }

	private static final Logger logger = LogManager.getLogger(QueueConsumerJNDI.class);

	public String CHCODE;
	public String SERVICECD;
	
    public JsonObject SolaceInfo(String CHCODE, String SERVICECD)
    {
    	JsonObject details = new JsonObject();
		
		try
		{
			Thread t1 = new Thread() {
				  
				  public void run() {
					  
					  	String args[] = { CHCODE, SERVICECD } ;
						 
						try 
						{
							 String sql = "select count(*) from prop001 where chcode = ? and mtypeparam = ? and userid = ?";  // checking the active environment
							 
							 int count = Jdbctemplate.queryForObject(sql, new Object[] { "ENV", new Sysconfig().getHostAddress(), "1" }, Integer.class);
							 
							 if(count == 1)
							 {
								 new QueueConsumerJNDI(Jdbctemplate).run(args);
							 }
						} 
						catch (Exception e) 
						{
							logger.debug("Exception in SolaceInfo :::: "+e.getLocalizedMessage());
						}
				  }
			 };
				  
			 t1.start();
		}
		catch(Exception e)
		{
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in SolaceInfo :::: "+e.getLocalizedMessage());
		}
		
		return details;
    }

    public void run(String... args) throws Exception 
    {
    	try
    	{
    		String CHCODE = args[0];  String SERVICECD = args[1];
    		
    		this.CHCODE = CHCODE;  this.SERVICECD = SERVICECD;
      	  
        	JsonObject webdt = Get_Solace_Info(CHCODE, SERVICECD);
        	
        	if(webdt.get("Result").getAsString().equalsIgnoreCase("success"))
        	{
        		JsonObject Headers = webdt.get("Headers").getAsJsonObject();
        		
        		String host = webdt.get("HOST").getAsString();
                String vpnName = Headers.get("VPNname").getAsString();
                String username = Headers.get("Username").getAsString();
                //String password = Headers.get("Username").getAsString();
                
                String QUEUE_NAME = webdt.get("QUEUE").getAsString();
                String QUEUE_JNDI_NAME = QUEUE_NAME; //"/JNDI/" + QUEUE_NAME;
                String CONNECTION_FACTORY_JNDI_NAME = Headers.get("Connectionfactory").getAsString();
                
                Hashtable<String, Object> env = new Hashtable<String, Object>();
              
                env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
               
                env.put(InitialContext.PROVIDER_URL, host);
                env.put(Context.SECURITY_PRINCIPAL, username + '@' + vpnName);
               // env.put(Context.SECURITY_CREDENTIALS, password);
                
                if(Headers.has("AuthenticationScheme") && Headers.get("AuthenticationScheme").getAsString().equalsIgnoreCase("AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE"))
                {
                	 if(Headers.has("AuthenticationScheme")) env.put(SupportedProperty.SOLACE_JMS_AUTHENTICATION_SCHEME, SupportedProperty.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE);
                	
                	// logger.debug("Initializing the key Store "+ Headers.get("SSLKeyStore").getAsString());
                	 
                	 if(Headers.has("SSLKeyStore"))  env.put(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE, Headers.get("SSLKeyStore").getAsString()); 
                	 if(Headers.has("SSLKeyStorePassword")) env.put(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE_PASSWORD, Headers.get("SSLKeyStorePassword").getAsString());
                	 if(Headers.has("SSLKeyStoreFormat")) env.put(SupportedProperty.SOLACE_JMS_SSL_KEY_STORE_FORMAT, Headers.get("SSLKeyStoreFormat").getAsString());
                	 
                	 //logger.debug("key Store Initialization success");
                	 
                    // logger.debug("Initializing the Trust Store "+ Headers.get("SSLTrustStore").getAsString());
                     
                     if(Headers.has("SSLTrustStore")) env.put(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE, Headers.get("SSLTrustStore").getAsString());
                     if(Headers.has("SSLTrustStorePassword")) env.put(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE_PASSWORD, Headers.get("SSLTrustStorePassword").getAsString());
                     if(Headers.has("SSLTrustStoreFormat"))  env.put(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE_FORMAT, Headers.get("SSLTrustStoreFormat").getAsString());
                
                    // logger.debug("Trust Store Initialization success");
                }
               
                if(Headers.has("SSLProtocol"))  env.put(SupportedProperty.SOLACE_JMS_SSL_EXCLUDED_PROTOCOLS, Headers.get("SSLProtocol").getAsString());  //tlsv1,tlsv1.1,tlsv1.2           
                if(Headers.has("SSLCipherSuites"))  env.put(SupportedProperty.SOLACE_JMS_SSL_CIPHER_SUITES, Headers.get("SSLCipherSuites").getAsString());
              
                env.put(SupportedProperty.SOLACE_JMS_DYNAMIC_DURABLES, true);
                
                env.put(SupportedProperty.SOLACE_JMS_JNDI_CONNECT_RETRIES, 1);
                env.put(SupportedProperty.SOLACE_JMS_JNDI_RECONNECT_RETRIES, 20);
                env.put(SupportedProperty.SOLACE_JMS_JNDI_RECONNECT_RETRY_WAIT, 3000);
                env.put(SupportedProperty.SOLACE_JMS_JNDI_CONNECT_RETRIES_PER_HOST, 5);
                env.put(SupportedProperty.SOLACE_JMS_CONSUMER_DISPATCHER_QUEUE_SIZE, 1000);
                
               // env.put(SupportedProperty., value)
                
                logger.debug("connection pool Properties :: "+env);
                
                InitialContext initialContext = new InitialContext(env);
               
                // Lookup the connection factory
                ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup(CONNECTION_FACTORY_JNDI_NAME);
                
                Connection connection = connectionFactory.createConnection();
               
                Session session = connection.createSession(false, SupportedProperty.SOL_CLIENT_ACKNOWLEDGE);
                
                logger.debug("Connected to the Solace Message VPN "+vpnName+" with client username "+ username);
                
                // Lookup the queue.
                Queue queue = (Queue) initialContext.lookup(QUEUE_JNDI_NAME);
                
                // From the session, create a consumer for the destination.
                MessageConsumer messageConsumer = session.createConsumer(queue);

                logger.debug("QueueConsumer is connecting to Solace messaging at "+ host);
                
                logger.debug("Listening the Queue "+ QUEUE_NAME);
   
                connection.start();
                
                messageConsumer.setMessageListener(new MessageListener() 
                {
                    @Override
                    public void onMessage(Message message) {
                        try 
                        {
                        	String MessageText = "";
                        	
                            if(message instanceof TextMessage) 
                            {
                            	MessageText = ((TextMessage) message).getText();
                            	
                            	logger.debug("TextMessage received: "+ MessageText);	
                            } 
                            else 
                            {
                            	MessageText = MessageText.toString();
                            	
                            	logger.debug("Message received.");
                            }
                            
                            String Headers = SolJmsUtility.dumpMessage(message);
                            
                            logger.debug("Message Content "+ Headers);
                            
                            logger.debug("******** Message Header Properties ********");
                            
                            logger.debug("CorrelationID: "+message.getJMSCorrelationID());
                            logger.debug("MessageID: "+message.getJMSMessageID());
                            logger.debug("SenderTimestamp: "+message.getJMSTimestamp());                        
                            
                            logger.debug("*******************************************");
                            
                            String JMSMessageID = message.getJMSMessageID();
                            
                            Insert_Request_001(webdt, MessageText, Headers, JMSMessageID, SERVICECD);
                            
                            message.acknowledge();
                            
                            logger.debug("Message acknowledged "+JMSMessageID);
                            
                            if(SERVICECD.equals("SOL001")) 
                            {
                            	 Insert_FileIT_Job(CHCODE, SERVICECD, MessageText, JMSMessageID);
                            	
                            	 Insert_JMS_Message(SERVICECD, MessageText, JMSMessageID);

                    			 logger.debug(">>>>>> Inserted into job 005 <<<<<<<<");
                            }  
                            else
                            {
                            	Solace_Router(CHCODE, SERVICECD, MessageText, Headers, JMSMessageID);
                            }
                        } 
                        catch(Exception ex) 
                        {
                        	logger.debug("Error processing incoming message.");
                        	
                        	logger.debug(ex.getLocalizedMessage());
                        }
                    }
                });
                
                while (IsRunning())
                {
                	Thread.sleep(5000);
    			}
        	                              
                 // the main thread blocks at the next statement until a message received
               
                //latch.
                //latch.await();
                
                connection.stop();
              
                messageConsumer.close();
                session.close();
                connection.close();
                initialContext.close();
                
                logger.debug("Queue with ServiceID "+SERVICECD+" is stopped listening...");
        	}
        	else
        	{
        		 logger.debug("solace configurations not found >>>> "+SERVICECD);
        	}
    	}
    	catch(JMSException ex)
		{
    		logger.debug("JMSException in solace run method >>>> "+ex.getLocalizedMessage());
		}
    	catch(Exception e)
		{
    		logger.debug("Exception in solace run method >>>> "+e.getLocalizedMessage());
		}
    }
    
    public void Solace_Router(String CHCODE, String SERVICECD, String Message, String Headers, String JMSMessageID) 
	{
		//JsonObject details = new JsonObject();
		
		try
		{
			Common_Utils util = new Common_Utils();
			
			Message = Message.replaceAll("\"\\{", "{");
			Message = Message.replaceAll("\\}\"", "}");
		
			if(Message.contains("IMFTFileNotification"))  //FileIT
			{
				JsonObject js = util.StringToJsonObject(Message);   logger.debug(">>>>> check 1 <<<<<<");
				
				logger.debug(">>>>> final json payload <<<<<< "+js);
				
				String CurrentDate = util.getCurrentDate("dd-MMM-yyyy");
														
				JsonObject IMFTFileNotification = js.get("IMFTFileNotification").getAsJsonObject();
				
				JsonObject Header = IMFTFileNotification.get("Header").getAsJsonObject();
				
				JsonObject Payload = IMFTFileNotification.get("Payload").getAsJsonObject();  logger.debug(">>>>> check 2 <<<<<<");
				
				String Source = Header.get("Source").getAsString();
				String UUID = Header.get("UUID").getAsString();
				
				String SrcFileName = Payload.get("SrcFilePath").getAsString();
				
				if(Source.toUpperCase().contains("PSGL"))
				{
					Source = "PSGL";
				}
				else if(Source.toUpperCase().contains("EBBS"))
				{
					Source = "EBBS";
				}
				else if(Source.toUpperCase().contains("MARCIS"))
				{
					Source = "MARCIS";
				}
				else if(Source.toUpperCase().contains("FM"))
				{
					Source = "FM";
				}
				else if(Source.toUpperCase().contains("SCI"))
				{
					Source = "CC";
				}
				else if(Source.toUpperCase().contains("EDMPIDSIFIS"))   
				{
					Source = "EDMPIDSIFIS";
				}
				
				logger.debug(">>>>> check 3 <<<<<<");
				
				String sql = "select suborgcode from sysconf001";
				
				List<String> result = Jdbctemplate.queryForList(sql, String.class);
				
				String SUBORGCODE = result.size() !=0 ? result.get(0) : ""; 
				
				logger.debug(">>>>> check 4 <<<<<<");
				 
				sql = "select MTYPEPARAM from prop001 where CHCODE = ? and MODULEID = ?";
				
				List<String> FilePaths = Jdbctemplate.queryForList(sql, new Object[] { "FILEIT", Source }, String.class);
				
				String DstPath = FilePaths.size() > 0 ? FilePaths.get(0) : "OTHERS";
				
				logger.debug(">>>>> check 5 <<<<<<");
				 
				MinIo_Modal MinIo = new MinIo_Modal(Jdbctemplate);
				
				JsonObject fpath = MinIo.Download_File(DstPath, SrcFileName);  
					
				logger.debug(">>>>> check 6 <<<<<<");
				
				String FileCategory = Source;
				
				if(fpath.has("FilePath")) //FilePath
				{
					String Filepath = fpath.get("FilePath").getAsString();
					
					if(SrcFileName.toUpperCase().contains("CDS"))
					{
						FileCategory = "CDS_REPORT";
					}
					else if(SrcFileName.toUpperCase().contains("RISK") || Source.toUpperCase().contains("EDMPIDSIFIS"))
					{
						FileCategory = "RISK_REPORT";
					}
					else if(SrcFileName.toUpperCase().contains("PASTDUE"))
					{
						FileCategory = "PASTDUE_REPORT";						
					}
					else if(SrcFileName.toUpperCase().contains("GRID1"))
					{
						FileCategory = "GRID1_REPORT";						
					}
					
					String FILETYPE = FilenameUtils.getExtension(SrcFileName);
					
					logger.debug(">>>>> check 7 <<<<<<");
					 
					try
					{
						sql = "insert into fileit001(SUBORGCODE, CHCODE, PAYTYPE, REQDATE, REQREFNO, REQTIME, FILETYPE, SRCPATH, DSTPATH, REMARKS, STATUS, RESCODE, RESPDESC) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					
						Jdbctemplate.update(sql, new Object[] { SUBORGCODE, Source, FileCategory, CurrentDate, UUID, util.get_oracle_Timestamp(), FILETYPE, SrcFileName, Filepath, "", "SUCCESS", "200", "FILE DOWNLOADED SUCCESSFULLY" });
					}
					catch(Exception ex){ }
					
					logger.debug(">>>>> check 8 <<<<<<");
					
					 if(Source.equals("PSGL"))
					 {
						 String[] apiCodes = {
								    "RTS003", // Equity Investment
								    "RTS041", // Premises, Furniture, and Equipment
								    "RTS043", // Dividends Payable
								    "RTS059", // Accrued Taxes
								    "RTS045", // Share Capital
								    "RTS047", // Other Capital Account
								    "RTS007", // Other Asset
								    "RTS063", // Unearned Income
								    "RTS073", // Other Liabilities
								    "RTS049", // Core Capital Deductions Data
								    "RTS061"  // Subordinated Debt
								};
						 
						  if(Filepath.contains("PL"))  
						  {    
							  apiCodes = new String[] { "RTS115" }; // Income Statement"	  		
						  } 
                          
						  String date = util.getCurrentDate("dd-MMM-yyyy");
						  
						  sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
							
						  result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", new Sysconfig().getHostAddress() } , String.class);
							 
						  String Status = result.size() !=0 ? result.get(0) : "Q"; 
							 
						  for(String APICODE : apiCodes)
						  {
							  String SERIAL = Generate_Reference_Serial().get("Serial").getAsString();
							  
							  sql = "insert into job005(SUBORGCODE, SYSCODE, CHCODE, PAYTYPE, REQDATE, REFNO, REQSL, STATUS, TRANTYPE, SERVCODE) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

							  Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "DV", Source, "RTSIS", date, SERIAL, SERIAL, Status, APICODE, APICODE });
						  }
					 }
					 else if(Source.equals("MARCIS"))
					 {
						 sql = "select count(*) from channel001 where chcode = ? and STATUS = ? and OAUTHVALREQ=?";
						 
						 int count = Jdbctemplate.queryForObject(sql, new Object[] { "MARCIS", "1", "1" }, Integer.class);
							
						 if(count == 0)
						 {
							 logger.debug(">>>>> Marcis file : "+Filepath+" is not Processed due to the Marcis channel was not enabled in datavision side <<<<<<");
						 }
						 else
						 {
							 logger.debug(">>>>> File : "+Filepath+" Processing Started <<<<<<");
							 
							 BOT_Suptect_MARCIS MARCIS = new BOT_Suptect_MARCIS(Jdbctemplate);  
								
							 if(SrcFileName.toLowerCase().contains("loaninformation"))
							 {
								  MARCIS.Marcis_File_Processing("RTS019", "", Filepath);
							 }
							 else if(SrcFileName.toLowerCase().contains("loantransaction"))
							 {
								 MARCIS.Marcis_File_Processing("RTS191", "", Filepath);
							 }
							 else if(SrcFileName.toLowerCase().contains("overdraft"))
							 {
								 MARCIS.Marcis_File_Processing("RTS023", "", Filepath);
							 }
							 else if(SrcFileName.toLowerCase().contains("undrawnbalance"))
							 {
								 MARCIS.Marcis_File_Processing("RTS103", "", Filepath);
							 }
							 else if(SrcFileName.toLowerCase().contains("writeoff"))
							 {
								 MARCIS.Marcis_File_Processing("RTS163", "", Filepath);
							 }
							 
							 logger.debug(">>>>> File : "+Filepath+" Processing Done <<<<<<");
						 }	
					 }
					 else if(Source.equals("CC") || Source.equals("EBBS"))
					 {
						  logger.debug(">>>>> File : "+Filepath+" Processing Started <<<<<<");
						 
						  sql = "select count(*) from prop001 where chcode = ? and moduleid = ? and MTYPEPARAM = ?";
							
						  int count = Jdbctemplate.queryForObject(sql, new Object[] { "EB_CC", "NEW_FN", "1" } , Integer.class);
						  
						  FileIT_Modal FileIT = new FileIT_Modal(Jdbctemplate);
						  
						  if(count == 1)
						  {
							  FileIT.Store_CC_EBBS_File_data_New(Source, Filepath);
						  }
						  else
						  {
							  FileIT.Store_CC_EBBS_File_data(Source, Filepath);
						  }
						
						  logger.debug(">>>>> File : "+Filepath+" Processing Done <<<<<<");
					 }
					 else if(Source.equals("EDMPIDSIFIS"))
					 {
						 logger.debug(">>>>> File : "+Filepath+" Storing Processing Started <<<<<<");
						 
						 String date = util.getCurrentDate("dd-MMM-yyyy");
						 
						 FileIT_Modal FileIT = new FileIT_Modal(Jdbctemplate);
						 
						 FileIT.Store_RiskView_ReportData(Filepath, UUID, date, Source);
					 }
				}
			}
			else if(Message.contains("data") && Message.contains("attributes") && Message.contains("request"))  //MARCIS
			{
				Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				Headers = Headers.toLowerCase();
				
				BOT_Suptect_MARCIS MARCIS = new BOT_Suptect_MARCIS(Jdbctemplate);
				
				String sql = "select count(*) from channel001 where chcode = ? and STATUS = ? and OAUTHVALREQ=?";
				 
				int count = Jdbctemplate.queryForObject(sql, new Object[] { "MARCIS", "1", "1" }, Integer.class);
				
				if(Headers.toLowerCase().contains("loaninformation"))
				{
					 List<String> validation = MARCIS.LoanInformation_validation("RTS019", "", Message);
					
					 if(count == 0)
					 {
						 validation.add("Channel is not enabled");
						 
						 MARCIS.Send_Solace_ErrorRespone("loaninformation", Message, validation);
					 }
					 else if(validation.size() > 0)
					 {
						 logger.debug("MARCIS Error List :::: "+validation);
						 
						 MARCIS.Send_Solace_ErrorRespone("loaninformation", Message, validation);
					 }
					 else
					 {
						 MARCIS.LoanInformation("RTS019", "", Message); //RTS019
						 
						 MARCIS.Send_Solace_Respone("loaninformation", Message);
					 }
				}
				
				if(Headers.toLowerCase().contains("loantransaction"))
				{
					 List<String> validation = MARCIS.LoanTransaction_validation("RTS191", "", Message);
					 
					 if(count == 0)
					 {
						 validation.add("Channel is not enabled");
						 
						 MARCIS.Send_Solace_ErrorRespone("loantransaction", Message, validation);
					 }
					 else if(validation.size() > 0)
					 {
						 logger.debug("MARCIS Error List :::: "+validation);
						 
						 MARCIS.Send_Solace_ErrorRespone("loantransaction", Message, validation);
					 }
					 else
					 {
						 MARCIS.LoanTransaction("RTS191", "", Message); //RTS191
						 
						 MARCIS.Send_Solace_Respone("loantransaction", Message);
					 }
				}
				
				if(Headers.toLowerCase().contains("undrawnbalance"))
				{
					 List<String> validation = MARCIS.undrawnBalanceData_validation("RTS103", "", Message);
					 
					 if(count == 0)
					 {
						 validation.add("Channel is not enabled");
						 
						 MARCIS.Send_Solace_ErrorRespone("undrawnbalance", Message, validation);
					 }
					 else if(validation.size() > 0)
					 {
						 logger.debug("MARCIS Error List :::: "+validation);
						 
						 MARCIS.Send_Solace_ErrorRespone("undrawnbalance", Message, validation);
					 }
					 else
					 {
						 MARCIS.undrawnBalanceData("RTS103", "", Message); //RTS103
						 
						 MARCIS.Send_Solace_Respone("undrawnbalance", Message);
					 }
				}
				
				if(Headers.toLowerCase().contains("writeoff"))
				{
					 List<String> validation = MARCIS.WrittenOffLoans_validation("RTS163", "", Message);
					 
					 if(count == 0)
					 {
						 validation.add("Channel is not enabled");
						 
						 MARCIS.Send_Solace_ErrorRespone("writeoff", Message, validation);
					 }
					 else if(validation.size() > 0)
					 {
						 logger.debug("MARCIS Error List :::: "+validation);
						 
						 MARCIS.Send_Solace_ErrorRespone("writeoff", Message, validation);
					 }
					 else
					 {
						 MARCIS.WrittenOffLoans("RTS163", "", Message); //RTS163
						 
						 MARCIS.Send_Solace_Respone("writeoff", Message);
					 }
				}
				
				if(Headers.toLowerCase().contains("overdraft"))
				{
					 List<String> validation = new ArrayList<String>();
					 
					 if(count == 0)
					 {
						 validation.add("Channel is not enabled");
						 
						 MARCIS.Send_Solace_ErrorRespone("overdraft", Message, validation);
					 }
					 else
					 {		
						 MARCIS.Overdraft("RTS023", "", Message); //RTS163
						 
						 MARCIS.Send_Solace_Respone("overdraft", Message);
					 }
				}
			}
			else if(Message.contains("header") && Message.contains("payload") && Message.contains("ACBS"))  //ACBS
			{
				boolean Dupflag = Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				BOT_Suptech_ACBS ACBS = new BOT_Suptech_ACBS(Jdbctemplate);
				
				JsonObject js = util.StringToJsonObject(Message);
				
				JsonObject payload = js.get("payload").getAsJsonObject();
			
				List<String> validation = new ArrayList<String>();
				
				if(payload.has("loansInformation"))
				{
					JsonObject LoanInformation = payload.get("loansInformation").getAsJsonObject(); 
					
					JsonArray Members = util.get_keys_from_Json(LoanInformation);
					
					if(Members.size() !=0)
					{
						List<String> Js = ACBS.LoanInformation_Validation("RTS019", "", Message); //RTS019
						
						if(Js.size() > 0) validation.addAll(Js);
					}
				}
				
				if(payload.has("loanTransactions"))
				{
					JsonObject LoanTransactions = payload.get("loanTransactions").getAsJsonObject(); 
					
					JsonArray Members = util.get_keys_from_Json(LoanTransactions);
					
					if(Members.size() !=0)
					{
						List<String> Js = ACBS.LoanTransactions_validation("RTS191", "", Message); //RTS191
						
						if(Js.size() > 0) validation.addAll(Js);
					}
				}
				
				if(payload.has("writtenOffLoans"))
				{
					JsonObject WrittenOffLoans = payload.get("writtenOffLoans").getAsJsonObject(); 
					
					JsonArray Members = util.get_keys_from_Json(WrittenOffLoans);
					
					if(Members.size() !=0)
					{
						List<String> Js = ACBS.WrittenOffLoans_validation("RTS163", "", Message); //RTS163
						
						if(Js.size() > 0) validation.addAll(Js);
					}
				}
				
				if(payload.has("undrawnBalances"))
				{
					JsonObject UndrawnBalances = payload.get("undrawnBalances").getAsJsonObject(); 
					
					JsonArray Members = util.get_keys_from_Json(UndrawnBalances);
					
					if(Members.size() !=0)
					{
						List<String> Js = ACBS.undrawnBalanceData_validation("RTS103", "", Message); //RTS103
						
						if(Js.size() > 0) validation.addAll(Js);
					}
				}
				
				String sql = "select count(*) from channel001 where chcode = ? and STATUS = ? and OAUTHVALREQ=?";
				 
				int count = Jdbctemplate.queryForObject(sql, new Object[] { "ACBS", "1", "1" }, Integer.class);
				
				if(Dupflag && count > 0)
				{
					validation.add("Duplicate Message received");
					
					ACBS.Send_Solace_ErrorRespone(Message, validation);
				}
				else if(validation.size() > 0 && count > 0)
				{
					 logger.debug("ACBS Error List :::: "+validation);
					 
					 ACBS.Send_Solace_ErrorRespone(Message, validation);
				}
				else
				{
					boolean res_flag = true;  
					
					if(payload.has("loansInformation"))
					{
						JsonObject LoanInformation = payload.get("loansInformation").getAsJsonObject(); 
						
						JsonArray Members = util.get_keys_from_Json(LoanInformation);
						
						if(Members.size() !=0)
						{
							JsonObject Js = ACBS.LoanInformation("RTS019", "", Message); //RTS019
							
							if(!Js.get("stscode").getAsString().equals("HP00"))
							{
								res_flag = false;
							}
						}
					}
					
					if(payload.has("loanTransactions"))
					{
						JsonObject LoanTransactions = payload.get("loanTransactions").getAsJsonObject(); 
						
						JsonArray Members = util.get_keys_from_Json(LoanTransactions);
						
						if(Members.size() !=0)
						{
							JsonObject Js = ACBS.LoanTransactions("RTS191", "", Message); //RTS191
							
							if(!Js.get("stscode").getAsString().equals("HP00"))
							{
								res_flag = false;
							}
						}
						
					}
					
					if(payload.has("writtenOffLoans"))
					{
						JsonObject WrittenOffLoans = payload.get("writtenOffLoans").getAsJsonObject(); 
						
						JsonArray Members = util.get_keys_from_Json(WrittenOffLoans);
						
						if(Members.size() !=0)
						{
							JsonObject Js = ACBS.WrittenOffLoans("RTS163", "", Message); //RTS163
							
							if(!Js.get("stscode").getAsString().equals("HP00"))
							{
								res_flag = false;
							}
						}
					}
					
					if(payload.has("undrawnBalances"))
					{
						JsonObject UndrawnBalances = payload.get("undrawnBalances").getAsJsonObject(); 
						
						JsonArray Members = util.get_keys_from_Json(UndrawnBalances);
						
						if(Members.size() !=0)
						{
							JsonObject Js = ACBS.undrawnBalanceData("RTS103", "", Message); //RTS103
							
							if(!Js.get("stscode").getAsString().equals("HP00"))
							{
								res_flag = false;
							}
						}
					}
					
					if(res_flag)
					{
						ACBS.Send_Solace_Respone(res_flag, Message);
					}
					else
					{
						List<String> Err = new ArrayList<String>();
						
						Err.add("Json body validation failed");
						
						ACBS.Send_Solace_ErrorRespone(Message, Err);
					}
				}
			}
			else if(Message.contains("header") && Message.contains("pmtTp"))  //cash
			{
				Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				JsonObject js = util.StringToJsonObject(Message);
				
				JsonObject header = js.get("header").getAsJsonObject();
				
				//String applNm = header.has("applNm") ? header.get("applNm").getAsString() : "";
				
				String applNm = header.has("applNm") && !header.get("applNm").isJsonNull() ? header.get("applNm").getAsString() : "";
				
				String pmtDrctn = header.has("pmtDrctn")? header.get("pmtDrctn").getAsString() : "";
				
				String pmtTp = header.has("pmtTp") ? header.get("pmtTp").getAsString() : "";
				
				String subPmtTp = header.has("subPmtTp") ? header.get("subPmtTp").getAsString() : "";
				
				BOT_Suptect_CASH Cash = new BOT_Suptect_CASH(Jdbctemplate);
				
				if(applNm.equalsIgnoreCase("STS"))
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						boolean isValid = validateCash001(Message , JMSMessageID);
						
						if(isValid)
						{
							Cash.OutgoingFundTransfer_STS("RTS197", "", Message);
						}
					}	
				}
				else if (applNm.equalsIgnoreCase("DOTOPAL")) 
				{
					if(pmtDrctn.equalsIgnoreCase("O"))
					{
						Cash.OutgoingFundTransfer_DOTOPAL("RTS197", "", Message);
					}
					else if(pmtDrctn.equalsIgnoreCase("I"))
					{
						Cash.IncomingFundTransfer_DOTOPAL("RTS195", "", Message);
					}
				}
				else 
				{
					 boolean isValid = validateCash001(Message , JMSMessageID);
					
					 String	PRODUCT = header.has("pmtTp") && !header.get("pmtTp").isJsonNull() ? header.get("pmtTp").getAsString() : "";		
						
					 String	UNIQUEVAL = header.has("UETR") && !header.get("UETR").isJsonNull() ? header.get("UETR").getAsString() : "";
						
					 String	txSts = header.has("txSts") && !header.get("txSts").isJsonNull()  ? header.get("txSts").getAsString() : "";
	
					 String sql1 = "select count(*) from cash001 x where PRODUCT =? and UNIQUEVAL = ? and REQDATE = ?" + 
						 		"AND TXN_STATUS in (select TXN_STATUS from cash002 y where y.product = x.product and y.isfinalsts = ?)";
	
					 int count = Jdbctemplate.queryForObject(sql1, new Object[] { PRODUCT , UNIQUEVAL , util.getCurrentDate("dd-MMM-yyyy") , 1 }, Integer.class);
	
					 if(count == 1 && isValid)
					 {
						if(pmtTp.equalsIgnoreCase("RTGS"))
						{
							if(pmtDrctn.equalsIgnoreCase("O"))
							{
								Cash.OutgoingFundTransfer_SCPAY_Rtgs("RTS197", "", Message);
							}
							else if (pmtDrctn.equalsIgnoreCase("I")) 
							{
								Cash.IncomingFundTransfer_SCPAY_Rtgs("RTS195", "", Message);	
							}
						}
						else if (pmtTp.equalsIgnoreCase("TT")) 
						{
							if(pmtDrctn.equalsIgnoreCase("O"))
							{
								Cash.OutgoingFundTransfer_SCPAY_TT("RTS197", "", Message);	
							}
							else if (pmtDrctn.equalsIgnoreCase("I")) 
							{
								Cash.IncomingFundTransfer_SCPAY_TT("RTS195", "", Message);		
							}
						}
						else if (pmtTp.equalsIgnoreCase("ACH")) 
						{
							if(pmtDrctn.equalsIgnoreCase("O"))
							{
								Cash.OutgoingFundTransfer_SCPAY_ACH("RTS197", "", Message);	
							}
							else if (pmtDrctn.equalsIgnoreCase("I")) 
							{
								Cash.IncomingFundTransfer_SCPAY_ACH("RTS195", "", Message);			
							}
						}
						else if (pmtTp.equalsIgnoreCase("BT")) 
						{
							if(pmtDrctn.equalsIgnoreCase("O"))
							{
								Cash.OutgoingFundTransfer_SCPAY_BT("RTS197", "", Message);
							}
						}
						else if (pmtTp.equalsIgnoreCase("IBFT")) 
						{
							if(subPmtTp.equalsIgnoreCase("OC"))
							{
								Cash.OutgoingFundTransfer_SCPAY_FAST("RTS197", "", Message);	
							}
							else if (subPmtTp.equalsIgnoreCase("IC")) 
							{
								Cash.IncomingFundTransfer_SCPAY_FAST("RTS195", "", Message);			
							}
						}
					 }
				}
			}
			else if(SERVICECD.equals("SOL008"))  //EBBS
			{  
				Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				BOT_Suptect_EBBS EBBS = new BOT_Suptect_EBBS(Jdbctemplate);
				
				EBBS.Balace_with_BOT("RTS011", "", Message);
				
				EBBS.Balace_with_Otherbank("RTS013", "", Message);	
			}
			else if(SERVICECD.equals("SOL002"))  //EBBS
			{ 
				Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				BOT_Suptect_EBBS EBBS = new BOT_Suptect_EBBS(Jdbctemplate);
				
				BOT_Suptech_Cheque Cheque = new BOT_Suptech_Cheque(Jdbctemplate);
				
				EBBS.Deposit_and_withdrawl("RTS067", "", Message);			
				
				Cheque.RTA_filtering("RTS031", "", Message, "");
			}
			else if(Message.contains("processingSystemCode") && Message.contains("requestorSystemCode") && Message.contains("notificationID"))  //Trade
			{
				Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				BOT_Suptect_Trade TRADE = new BOT_Suptect_Trade(Jdbctemplate);
				
				TRADE.Trade_Master(Message);
			}
			else if(SERVICECD.equals("SOL011")) //CADD
			{
				Insert_JMS_Message(SERVICECD, Message, JMSMessageID);
				
				BOT_Suptect_CADD CADD = new BOT_Suptect_CADD(Jdbctemplate);
				
				String sql = "select count(*) from channel001 where chcode = ? and STATUS = ? and OAUTHVALREQ=?";
				 
				int count = Jdbctemplate.queryForObject(sql, new Object[] { "CADD", "1", "1" }, Integer.class);
				
				 List<String> validation = CADD.Channel_records_validation("RTS189", "", Message);
				
				 if(validation.size() > 1 && count > 0)
				 {
					 logger.debug("CADD Error List :::: "+validation);
					 
					 CADD.Send_Solace_ErrorRespone("Channel_Records", Message, validation);
				 }
				 else
				 {
					 CADD.Channel_records("RTS189", "", Message);
					 
					 CADD.Send_Solace_Respone("Channel_Records", Message , validation);
				 }				
			}
		}
		catch(Exception e)
		{
    		logger.debug("Exception in solace run method >>>> "+e.getLocalizedMessage());
		}
	}
    
    public boolean validateCash001(String message ,  String JMSMessageID)
    {
    	try
		{
		 	Common_Utils util = new Common_Utils();
		 
			JsonObject js = util.StringToJsonObject(message);
			
			JsonObject header = js.get("header").getAsJsonObject();
			
			String applNm = header.has("applNm") && !header.get("applNm").isJsonNull() ? header.get("applNm").getAsString() : "";
			
			 String Sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(Sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			
			if(applNm.equalsIgnoreCase("STS"))
			{
				 String	UNIQUEVAL = header.has("sysrefNb") && !header.get("sysrefNb").isJsonNull() ? header.get("sysrefNb").getAsString() : "";
					
				 Sql = "INSERT INTO CASH001 ( SUBORGCODE, DOMAIN, PRODUCT, UNIQUEID, UNIQUEVAL, TXN_STATUS,  REQDATE, REQTIME, REQREFNO,  BODY_MSG ) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
				
				 Jdbctemplate.update(Sql, new Object[] { SUBORGCODE, "CASH", "STS", "SYSREFNB", UNIQUEVAL, "NA" ,  util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), JMSMessageID,  message });
					
				 return true;	
			}
			else 
			{
				String	PRODUCT = header.has("pmtTp") && !header.get("pmtTp").isJsonNull() ? header.get("pmtTp").getAsString() : "";		
				
				String	UNIQUEVAL = header.has("UETR") && !header.get("UETR").isJsonNull() ? header.get("UETR").getAsString() : "";
					
				String	txSts = header.has("txSts")&& !header.get("txSts").isJsonNull()  ? header.get("txSts").getAsString() : "";
					
				String	UNIQUEID = "UETR";
				
				String sql = "SELECT COUNT(*) FROM cash002 WHERE PRODUCT = ? AND TXN_STATUS = ? AND isfinalsts = ?";
		
				int count_v1 = Jdbctemplate.queryForObject(sql, new Object[] { PRODUCT , txSts , 1 }, Integer.class);

				if(count_v1 > 0)
				{	
					
					 sql = "INSERT INTO CASH001 ( SUBORGCODE, DOMAIN, PRODUCT, UNIQUEID, UNIQUEVAL, TXN_STATUS,  REQDATE, REQTIME, REQREFNO,  BODY_MSG ) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
					
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "CASH", PRODUCT, UNIQUEID, UNIQUEVAL, txSts ,  util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), JMSMessageID,  message });
						
					 return true;	 
				}
				 
				return false;	
			}
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
			
	        return false;
		}
    }
    
    public JsonObject Get_Solace_Info(String CHCODE, String SERVICECD) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "Select * from webservice001 where CHCODE=? and SERVICECD=?";
			 
			 List<web_service_001> API_Info = Jdbctemplate.query(sql, new Object[] { CHCODE, SERVICECD }, new Webservice_Modal().new API_Mapper() );
			 
			 if(API_Info.size()!=0)
			 {
				 String PAYLOAD = API_Info.get(0).getPAYLOAD();
				 
				 String SIGNPAYLOAD = API_Info.get(0).getSIGNPAYLOAD();
				 
				 details.addProperty("SUBORGCODE", API_Info.get(0).getSUBORGCODE());
				 details.addProperty("CHCODE", API_Info.get(0).getCHCODE());
				 details.addProperty("SERVICECD", API_Info.get(0).getSERVICECD());
				 details.addProperty("SERVNAME", API_Info.get(0).getSERVNAME());
				 details.addProperty("FORMAT", API_Info.get(0).getFORMAT());
				 details.addProperty("PROTOCOL", API_Info.get(0).getPROTOCOL());	 
				 details.addProperty("METHOD", API_Info.get(0).getMETHOD());
				 details.addProperty("CHTYPE", API_Info.get(0).getCHTYPE());
				 details.addProperty("HOST", API_Info.get(0).getURI());
				 details.addProperty("TOPIC", PAYLOAD);
				 details.addProperty("QUEUE", SIGNPAYLOAD);
				 details.addProperty("HEADERID", API_Info.get(0).getHEADERID());
				 details.addProperty("FLOW", API_Info.get(0).getFLOW());
				 
				 sql = "Select * from webservice002 where SERVICECD=? and CHCODE=? and HEADERID=?";
				 
				 List<web_service_002> Header_Info = Jdbctemplate.query(sql, new Object[] { API_Info.get(0).getSERVICECD(), API_Info.get(0).getCHCODE(), API_Info.get(0).getHEADERID() }, new Webservice_Modal().new Header_Mapper() );
				 
				 JsonObject Headers = new JsonObject();
				 
				 for(int i=0;i<Header_Info.size();i++)
				 {
					 Headers.addProperty(Header_Info.get(i).getHEADKEY(), Header_Info.get(i).getHEADVALUE());
				 }
				
				 details.add("Headers", Headers);
			 }
			 
			 details.addProperty("Result", API_Info.size()!=0 ? "Success" : "Failed");
			 details.addProperty("Message", API_Info.size()!=0 ? "API Configuration Details Found !!" : "API Configuration Details Not Found");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Get_Webserice_Info :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
    
    public JsonObject Insert_Request_001(JsonObject Js, String Body, String Headers, String ACK, String SERVICECD) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			
			 if(SERVICECD.equals("SOL001")) 
             {
				 JsonObject js = util.StringToJsonObject(Body);  
					
				 JsonObject IMFTFileNotification = js.get("IMFTFileNotification").getAsJsonObject();
				
				 JsonObject Header = IMFTFileNotification.get("Header").getAsJsonObject();
				
				 ACK = Header.get("UUID").getAsString();
             }
			 else
			 {
				 ACK = util.isNullOrEmpty(ACK) ? util.Generate_Random_String(12) : ACK;
			 }
			 	 
			 String sql = "Insert into request001(SUBORGCODE,CHCODE,PAYTYPE,MSGTYPE,FLOW,REQDATE,REQTIME,UNIREFNO,MSGURL,IP,PORT,HEAD_MSG,BODY_MSG,REQBY,HASHVAL) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			   
			 Jdbctemplate.update(sql, new Object[] { Js.get("SUBORGCODE").getAsString(), Js.get("CHCODE").getAsString(), Js.get("CHCODE").getAsString(), Js.get("SERVICECD").getAsString(), Js.get("FLOW").getAsString(), util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), ACK, 
					 Js.get("HOST").getAsString(),  Js.get("TOPIC").getAsString(), "", Headers, Body, Js.get("CHCODE").getAsString(), ACK });
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Request added Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Insert_Request_001 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
    
    public JsonObject Insert_FileIT_Job(String CHCODE, String SERVICECD, String MessageText, String Refid) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			
			 String sql = "select suborgcode from sysconf001";
			
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : ""; 
			 
			 sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
				
			 result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", new Sysconfig().getHostAddress() } , String.class);
			 
			 String Status = result.size() !=0 ? result.get(0) : "Q"; 
			 
			// Refid = util.isNullOrEmpty(Refid) ? util.Generate_Random_String(12) : Refid;
			 
			 String REQSL = Generate_Serial().get("Serial").getAsString();
			 
			 JsonObject js = util.StringToJsonObject(MessageText);  
												
			 JsonObject IMFTFileNotification = js.get("IMFTFileNotification").getAsJsonObject();
			
			 JsonObject Header = IMFTFileNotification.get("Header").getAsJsonObject();
			
			 String Source = Header.get("Source").getAsString();
			 String UUID = Header.get("UUID").getAsString();
			 
			 if(Source.toUpperCase().contains("PSGL"))
			 {
				Source = "PSGL";
			 } 
			 else if(Source.toUpperCase().contains("EBBS"))
			 {
				Source = "EBBS";
			 }
			 else if(Source.toUpperCase().contains("MARCIS"))
			 {
				Source = "MARCIS";
			 }
			 else if(Source.toUpperCase().contains("FM"))
			 {
				Source = "FM";
			 }
			 else if(Source.toUpperCase().contains("SCI"))
			 {
				Source = "CC";
			 }
			 else if(Source.toUpperCase().contains("EDMPIDSIFIS"))   
			 {
				Source = "EDMPIDSIFIS";
			 }
			 
			 sql = "Insert into job005(SUBORGCODE,SYSCODE,CHCODE,PAYTYPE,TRANTYPE,REASON,REQDATE,REFNO,REQSL,STATUS,SERVCODE) values(?,?,?,?,?,?,?,?,?,?,?)";
			   
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "DV", CHCODE, Source, SERVICECD, "", util.getCurrentDate("dd-MMM-yyyy"), UUID, REQSL, Status, SERVICECD });
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Job added Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Insert_Job_005 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
    
    public boolean Insert_JMS_Message(String SERVICECD, String MessageText, String MessageId) 
	{
    	boolean s = true;
		
		try
		{
			 Common_Utils util = new Common_Utils();
			
			 String SolaceRef = "";
			 
			 if(SERVICECD.equals("SOL001"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"UUID\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 else if(SERVICECD.equals("SOL002"))
			 {
				 SolaceRef = RegexValue(MessageText, "<trackingId>([A-Za-z0-9]+)</trackingId>");
			 }
			 else if(SERVICECD.equals("SOL003"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"id\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 else if(SERVICECD.equals("SOL004"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"notificationID\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 else if(SERVICECD.equals("SOL005"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"sysrefNb\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 else if(SERVICECD.equals("SOL006"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"messageId\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 else if(SERVICECD.equals("SOL007"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"messageId\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 else if(SERVICECD.equals("SOL008"))
			 {
				 SolaceRef = RegexValue(MessageText, "<trackingId>([^>]*?)</trackingId>");
			 }
			 else if(SERVICECD.equals("SOL009"))
			 {
				 SolaceRef = RegexValue(MessageText, "\"notificationID\"\\s*:\\s*\"([^>]*?)\"");
			 }
			 
			 logger.debug("solace refid "+SolaceRef);
			 
			 String sql = "Insert into JMS_MESSAGES(sourcesys,messagedate,messageid,sourcesysid) values(?,?,?,?)";
			   
			 int Status = Jdbctemplate.update(sql, new Object[] {  SERVICECD, util.get_oracle_Timestamp(), MessageId, SolaceRef });
			
			 s = Status != 0 ? false : true;
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in Insert_JMS_Message for the MessageId "+MessageId+" is :::: "+e.getLocalizedMessage());
		 }
		
		 return s;
	}
    
    public String RegexValue(String input, String regex) 
	{
    	String uuid = "";
    	
		try
		{
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(input);

	        if(matcher.find()) 
	        {
	            uuid = matcher.group(1);
	        } 
	        else 
	        {
	        	logger.debug("UUID not found in the input string.");
	        }
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in RegexValue  :::: "+e.getLocalizedMessage());
		 }
		
		 return uuid;
	}
    
    public JsonObject Generate_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select REQ001.nextval from dual";
			   
			 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
			 
			 details.addProperty("Serial", SL);
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Serial generated Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
    
    public JsonObject Generate_Reference_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select refno.Nextval from dual";
			   
			 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
			 
			 details.addProperty("Serial", SL);
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Serial generated Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
    
    public boolean IsRunning()
    {
    	boolean res = false;
    	
    	try
    	{
    		List<Solace> SolaceInfo = SolaceManager.SolaceInfo;
    		
    		for(int i=0; i<SolaceInfo.size(); i++)
		    {
    			if(SolaceInfo.get(i).getServcd().equals(this.SERVICECD))
    			{
    				if(SolaceInfo.get(i).isIsRunning())
    				{
    					res = true;
    					
    					break;
    				}
    			}
		    }
    	}
    	catch(Exception ex)
    	{
    		logger.debug("Exception in IsRunning & chcode is :::: "+this.SERVICECD);
    	}
    	
    	return res;
    }

	public String getCHCODE() {
		return CHCODE;
	}

	public void setCHCODE(String cHCODE) {
		CHCODE = cHCODE;
	}

	public String getSERVICECD() {
		return SERVICECD;
	}

	public void setSERVICECD(String sERVICECD) {
		SERVICECD = sERVICECD;
	}

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
}
