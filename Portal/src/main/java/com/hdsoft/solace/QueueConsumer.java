
package com.hdsoft.solace;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Solace;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.models.Webservice_Modal;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class QueueConsumer 
{
	/*public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public QueueConsumer(JdbcTemplate Jdbc)
	{ 
		Jdbctemplate = Jdbc;
	}
	
	public QueueConsumer() { }

	private static final Logger logger = LogManager.getLogger(QueueConsumer.class);

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
							new QueueConsumer(Jdbctemplate).run(args);
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
                
                //String CONNECTION_FACTORY_JNDI_NAME = "/JNDI/CF/GettingStarted";
                
                //setup environment variables for creating of the initial context
                //Hashtable<String, Object> env = new Hashtable<String, Object>();
                //use the Solace JNDI initial context factory
                //env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
          
                // assign Solace message router connection parameters
                //  env.put(InitialContext.PROVIDER_URL, host);
                // env.put(Context.SECURITY_PRINCIPAL, username + '@' + vpnName); // Formatted as user@message-vpn
                //env.put(Context.SECURITY_CREDENTIALS, password);
               
                logger.debug("QueueConsumer is connecting to Solace messaging at %s...%n"+ host);

                SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
                
                connectionFactory.setHost(host);
                connectionFactory.setVPN(vpnName);
                connectionFactory.setUsername(username);
               //connectionFactory.setPassword(password);
                       
                if(Headers.has("AuthenticationScheme") && Headers.get("AuthenticationScheme").getAsString().equalsIgnoreCase("AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE"))
                {
                	 if(Headers.has("AuthenticationScheme")) connectionFactory.setAuthenticationScheme(Headers.get("AuthenticationScheme").getAsString());
                   
                	 logger.debug("Initializing the key Store "+ Headers.get("SSLKeyStore").getAsString());
                	 
                	 if(Headers.has("SSLKeyStore")) connectionFactory.setSSLKeyStore(Headers.get("SSLKeyStore").getAsString());
                     if(Headers.has("SSLKeyStorePassword")) connectionFactory.setSSLKeyStorePassword(Headers.get("SSLKeyStorePassword").getAsString());
                     if(Headers.has("SSLKeyStoreFormat")) connectionFactory.setSSLKeyStoreFormat(Headers.get("SSLKeyStoreFormat").getAsString());

                     logger.debug("Initializing the Trust Store "+ Headers.get("SSLTrustStore").getAsString());
                     
                     if(Headers.has("SSLTrustStore")) connectionFactory.setSSLTrustStore(Headers.get("SSLTrustStore").getAsString());
                     if(Headers.has("SSLTrustStorePassword")) connectionFactory.setSSLTrustStorePassword(Headers.get("SSLTrustStorePassword").getAsString());
                     if(Headers.has("SSLTrustStoreFormat")) connectionFactory.setSSLTrustStoreFormat(Headers.get("SSLTrustStoreFormat").getAsString());
                }
                else
                {
                	if(Headers.has("Password")) connectionFactory.setPassword(Headers.get("Password").getAsString());
                }
                       
                if(Headers.has("SSLCipherSuites")) connectionFactory.setSSLCipherSuites(Headers.get("SSLCipherSuites").getAsString()); 
                if(Headers.has("SSLProtocol")) connectionFactory.setSSLProtocol(Headers.get("SSLProtocol").getAsString());

                connectionFactory.setDynamicDurables(true);

                Connection connection = connectionFactory.createConnection();

                Session session = connection.createSession(false, SupportedProperty.SOL_CLIENT_ACKNOWLEDGE);

                logger.debug("Connected to the Solace Message VPN '%s' with client username '%s'.%n", vpnName,  username);

                Queue queue = session.createQueue(QUEUE_NAME);
                
                //final CountDownLatch latch = new CountDownLatch(1);

                MessageConsumer messageConsumer = session.createConsumer(queue);
                
                connection.start();

                while (IsRunning())
                {
                	logger.debug("checking queue "+getSERVICECD());
                	
	                messageConsumer.setMessageListener(new MessageListener() 
	                {
	                    @Override
	                    public void onMessage(Message message) {
	                        try 
	                        {
	                            if(message instanceof TextMessage) 
	                            {
	                            	logger.debug("TextMessage received: '%s'%n", ((TextMessage) message).getText());	
	                            } 
	                            else 
	                            {
	                            	logger.debug("Message received.");
	                            }
	                            
	                            logger.debug("Message Content:%n%s%n", SolJmsUtility.dumpMessage(message));
	                            
	                            logger.debug("******** Message Header Properties ********");
	                            
	                            logger.debug("CorrelationID: "+message.getJMSCorrelationID());
	                            logger.debug("MessageID: "+message.getJMSMessageID());
	                            logger.debug("SenderTimestamp: "+message.getJMSTimestamp());
	                            logger.debug("ConversationID: "+message.getJMSTimestamp());
	                            
	                            logger.debug("*******************************************");
	                            
	                            Insert_Request_001(webdt, SolJmsUtility.dumpMessage(message), "");
	
	                            message.acknowledge();
	                            
	                           // Thread.sleep(5000);
	                            //latch.countDown(); // unblock the main thread
	                            
	                            //if(!IsRunning()) {
	                            	//break;
	                            //}
	                        } 
	                        catch(Exception ex) 
	                        {
	                        	logger.debug("Error processing incoming message.");
	                        	
	                        	logger.debug(ex.getLocalizedMessage());
	                        }
	                    }
	                });
                }
                                
                // the main thread blocks at the next statement until a message received
               
                //latch.
              //  latch.await();
                
                connection.stop();
              
                messageConsumer.close();
                session.close();
                connection.close();
        	}
        	else
        	{
        		 logger.debug("solace configurations not found >>>> "+SERVICECD);
        	}
    	}
    	catch(Exception e)
		{
    		logger.debug("Exception in solace run method >>>> "+e.getLocalizedMessage());
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
    
    public JsonObject Insert_Request_001(JsonObject Js, String Body, String Headers) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			
			 String sql = "Insert into request001(SUBORGCODE,CHCODE,PAYTYPE,MSGTYPE,FLOW,REQDATE,REQTIME,UNIREFNO,MSGURL,IP,PORT,HEAD_MSG,BODY_MSG,REQBY,HASHVAL) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			   
			 Jdbctemplate.update(sql, new Object[] { Js.get("SUBORGCODE").getAsString(), Js.get("CHCODE").getAsString(), Js.get("CHCODE").getAsString(), Js.get("SERVICECD").getAsString(), Js.get("FLOW").getAsString(), util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), util.Generate_Random_String(12), 
					 Js.get("HOST").getAsString(),  Js.get("TOPIC").getAsString(), "", Headers, Body, Js.get("CHCODE").getAsString(), "" });
			
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
	}*/
}
