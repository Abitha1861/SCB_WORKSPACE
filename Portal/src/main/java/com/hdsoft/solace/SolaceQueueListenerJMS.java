package com.hdsoft.solace;

import java.util.Hashtable;
import java.util.List;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.models.Webservice_Modal;
import com.solacesystems.jms.SupportedProperty;

@Component
public class SolaceQueueListenerJMS 
{
	/*private JdbcTemplate Jdbctemplate;
    private Connection connection;
    private Session session;
    private MessageConsumer messageConsumer;
  
    private static final Logger logger = LogManager.getLogger(SolaceQueueListenerJMS.class);
    
    public SolaceQueueListenerJMS(JdbcTemplate Jdbctemplate, ConnectionFactory connectionFactory, String queueName) throws JMSException 
    {
    	try
    	{
    		 this.Jdbctemplate = Jdbctemplate;
            //this.connection = connectionFactory.createConnection();
            //this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //Queue queue = session.createQueue(queueName);
            //this.messageConsumer = session.createConsumer(queue);
            
    		JsonObject webdt = Get_Solace_Info(queueName);
        	
        	if(webdt.get("Result").getAsString().equalsIgnoreCase("success"))
        	{
        		JsonObject Headers = webdt.get("Headers").getAsJsonObject();
        		
        		String host = webdt.get("HOST").getAsString();
                String vpnName = Headers.get("VPNname").getAsString();
                String username = Headers.get("Username").getAsString();
               
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
                connectionFactory = (ConnectionFactory) initialContext.lookup(CONNECTION_FACTORY_JNDI_NAME);
                
                this.connection = connectionFactory.createConnection();
               
                this.session = connection.createSession(false, SupportedProperty.SOL_CLIENT_ACKNOWLEDGE);
                
                logger.debug("Connected to the Solace Message VPN "+vpnName+" with client username "+ username);
                
                // Lookup the queue.
                Queue queue = (Queue) initialContext.lookup(QUEUE_JNDI_NAME);
                
                // From the session, create a consumer for the destination.
                this.messageConsumer = session.createConsumer(queue);

                logger.debug("QueueConsumer is connecting to Solace messaging at "+ host);
                
                logger.debug("Listening the Queue "+ QUEUE_NAME);
        	}
    	}
    	catch(Exception ex)
    	{
    		logger.debug("Exception in SolaceQueueListenerJMS "+ ex.getLocalizedMessage());
    	}
    }

    public void startListener() throws JMSException 
    {
        connection.start();
        
        messageConsumer.setMessageListener(new MessageListener() {
        	
            @Override
            public void onMessage(Message message) {
                try 
                {
                    if (message instanceof TextMessage) 
                    {
                        String text = ((TextMessage) message).getText();
                        System.out.println("Received from queue: " + text);
                    } 
                    else 
                    {
                        System.out.println("Received: " + message);
                    }
                } 
                catch (JMSException e)
                {
                    e.printStackTrace();
                }
            }
        });
        
        System.out.println("Listener started for queue.");
    }

    public void stopListener() throws JMSException 
    {
        if (messageConsumer != null) 
        {
            messageConsumer.close();
        }
        
        if (session != null) 
        {
            session.close();
        }
        if (connection != null) 
        {
            connection.close();
        }
        
        System.out.println("Listener stopped for queue.");
    }
    
    public JsonObject Get_Solace_Info(String SERVICECD) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "Select * from webservice001 where CHCODE=? and SERVICECD=?";
			 
			 List<web_service_001> API_Info = Jdbctemplate.query(sql, new Object[] { "SOLACE", SERVICECD }, new Webservice_Modal().new API_Mapper() );
			 
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
	}*/
}

