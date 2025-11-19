/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 *  Solace JMS 1.1 Examples: TopicSubscriber
 */

package com.hdsoft.solace;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.models.Webservice_Modal;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class TopicSubscriber_bk
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public TopicSubscriber_bk(JdbcTemplate Jdbc)
	{ 
		Jdbctemplate = Jdbc;
	}
	
	public TopicSubscriber_bk() { }
	
	private static final Logger logger = LogManager.getLogger(TopicSubscriber_bk.class);
	
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
							new TopicSubscriber_bk(Jdbctemplate).run(args);
						} 
						catch (Exception e) 
						{
							logger.debug("Exception in SolaceInfo :::: "+e.getLocalizedMessage());
						}
				  }
			 };
				  
			 t1.start();
				
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Topic Subscribed");  	 
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
      	  
        	JsonObject webdt = Get_Solace_Info(CHCODE, SERVICECD);
        	
        	if(webdt.get("Result").getAsString().equalsIgnoreCase("success"))
        	{
        		JsonObject Headers = webdt.get("Headers").getAsJsonObject();
        		
        		String host = webdt.get("HOST").getAsString();
                String vpnName = Headers.get("VPNname").getAsString();
                String username = Headers.get("Username").getAsString();
                //String password = Headers.get("Username").getAsString();
                
                String TOPIC_NAME = webdt.get("TOPIC").getAsString();
                
                SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
                
                connectionFactory.setHost(host);
                connectionFactory.setVPN(vpnName);
                connectionFactory.setUsername(username);
               //connectionFactory.setPassword(password);
                
                if(Headers.has("AuthenticationScheme") && Headers.get("AuthenticationScheme").getAsString().equalsIgnoreCase("AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE"))
                {
                	 if(Headers.has("AuthenticationScheme")) connectionFactory.setAuthenticationScheme(Headers.get("AuthenticationScheme").getAsString());
                     if(Headers.has("SSLKeyStore")) connectionFactory.setSSLKeyStore(Headers.get("SSLKeyStore").getAsString());
                     if(Headers.has("SSLKeyStorePassword")) connectionFactory.setSSLKeyStorePassword(Headers.get("SSLKeyStorePassword").getAsString());
                     if(Headers.has("SSLKeyStoreFormat")) connectionFactory.setSSLKeyStoreFormat(Headers.get("SSLKeyStoreFormat").getAsString());

                     if(Headers.has("SSLTrustStore")) connectionFactory.setSSLTrustStore(Headers.get("SSLTrustStore").getAsString());
                     if(Headers.has("SSLTrustStorePassword")) connectionFactory.setSSLTrustStorePassword(Headers.get("SSLTrustStorePassword").getAsString());
                     if(Headers.has("SSLTrustStoreFormat")) connectionFactory.setSSLTrustStoreFormat(Headers.get("SSLTrustStoreFormat").getAsString());
                }
                else
                {
                	 if(Headers.has("Password")) connectionFactory.setPassword(Headers.get("Password").getAsString());
                }
                               
                Connection connection = connectionFactory.createConnection();

                final CountDownLatch latch = new CountDownLatch(1);
                
                // Create a non-transacted, Auto ACK session.
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                logger.debug("Connected to Solace Message VPN '%s' with client username '%s'.%n", vpnName, username);

                // Create the subscription topic programmatically
                Topic topic = session.createTopic(TOPIC_NAME);

                // Create the message consumer for the subscription topic
                MessageConsumer messageConsumer = session.createConsumer(topic);

                // Use the anonymous inner class for receiving messages asynchronously
                messageConsumer.setMessageListener(new MessageListener() 
                {
                    @Override
                    public void onMessage(Message message) {
                        try 
                        {
                            if (message instanceof TextMessage)
                            {
                            	logger.debug("TextMessage received: '%s'%n", ((TextMessage) message).getText());
                            } 
                            else
                            {
                            	logger.debug("Message received.");
                            }
                            
                            logger.debug("Message Content:%n%s%n", SolJmsUtility.dumpMessage(message));
                            
                            Insert_Request_001(webdt, SolJmsUtility.dumpMessage(message), "");
                            
                            latch.countDown(); // unblock the main thread
                        } 
                        catch (JMSException ex) 
                        {
                        	logger.debug("Error processing incoming message.");
                            ex.printStackTrace();
                        }
                    }
                });

                connection.start();
                
                logger.debug("Awaiting message...");
          
                latch.await();

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
}
