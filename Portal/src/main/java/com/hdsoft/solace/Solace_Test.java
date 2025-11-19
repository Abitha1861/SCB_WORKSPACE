package com.hdsoft.solace;

import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;

public class Solace_Test 
{
	private static final Logger logger = LogManager.getLogger(SolaceManager.class);
	
   public static void main(String args[]) throws Exception
   {
	   new Solace_Test().run(args);
   }
   
   public void run(String... args) throws Exception 
   {
   		try
   		{
       		String host = "smfs://hk-np1.fs-solace.dev.net:55443";
            String vpnName = "vpn-poc-d1";
            String username = "app-test-user";
           //String password = Headers.get("Username").getAsString();
               
           String QUEUE_NAME = "q-app3-test-conn";
          
           logger.debug("QueueConsumer is connecting to Solace messaging at %s...%n"+ host);

           SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
           
           connectionFactory.setHost(host);
           connectionFactory.setVPN(vpnName);
           connectionFactory.setUsername(username);
          //connectionFactory.setPassword(password);
           
           connectionFactory.setAuthenticationScheme("AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE");
           connectionFactory.setSSLKeyStore("D:\\SCB\\POC\\POC Docs\\app_test_user_poc_certs_2024\\app-test-user.pfx");
           connectionFactory.setSSLKeyStorePassword("changeit");
           connectionFactory.setSSLKeyStoreFormat("PKCS12");

           connectionFactory.setSSLTrustStore("D:\\SCB\\POC\\POC Docs\\app_test_user_poc_certs_2024\\scb_certificate.jks");
           connectionFactory.setSSLTrustStorePassword("changeit");
           connectionFactory.setSSLTrustStoreFormat("JKS");
           
           connectionFactory.setSSLCipherSuites("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"); 
           connectionFactory.setSSLProtocol("TLSV1.2");

           connectionFactory.setDynamicDurables(true);

           Connection connection = connectionFactory.createConnection();

           Session session = connection.createSession(false, SupportedProperty.SOL_CLIENT_ACKNOWLEDGE);

           logger.debug("Connected to the Solace Message VPN '%s' with client username '%s'.%n", vpnName,  username);

           Queue queue = session.createQueue(QUEUE_NAME);
           
           final CountDownLatch latch = new CountDownLatch(1);

           MessageConsumer messageConsumer = session.createConsumer(queue);
           
           connection.start();

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
                        
                      
                        message.acknowledge();
                                           
                        latch.countDown(); // unblock the main thread
                        
                    } 
                    catch(Exception ex) 
                    {
                    	logger.debug("Error processing incoming message.");
                    	
                    	logger.debug(ex.getLocalizedMessage());
                    }
                }
           });
                                    
           // the main thread blocks at the next statement until a message received

           latch.await();
           
           connection.stop();
         
           messageConsumer.close();
           session.close();
           connection.close();
	   	}
	   	catch(Exception e)
		{
	   		logger.debug("Exception in solace run method >>>> "+e.getLocalizedMessage());
		}
   }
   
   
}
