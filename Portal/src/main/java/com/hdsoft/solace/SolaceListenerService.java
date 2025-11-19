package com.hdsoft.solace;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
//@Service
public class SolaceListenerService 
{
	//private JdbcTemplate Jdbctemplate;
    //private final ListenerControlService listenerControlService = null;
    //private final Map<String, SolaceQueueListenerJMS> listeners = new HashMap<>();
    //private ConnectionFactory connectionFactory;
    //private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    //private static final Logger logger = LogManager.getLogger(SolaceListenerService.class);
    
    //@Autowired
    //private ListenerControlService listenerControlService;
    
    /*@Autowired
 	public void setJdbctemplate(HikariDataSource Datasource) 
 	{
     	Jdbctemplate = new JdbcTemplate(Datasource);
 	}*/
    
	/*
	 * @Scheduled(cron = "0/30 * * * * *") public void init() throws
	 * InterruptedException { //monitorAndControlListeners();
	 * 
	 * //Thread.sleep(Long.MAX_VALUE);
	 * 
	 * // monitorAndControlListeners(); }
	 */
    
   // @Autowired
	//public void setJdbctemplate(HikariDataSource Datasource) 
	//{
    	//Jdbctemplate = new JdbcTemplate(Datasource);
	//}
    
    //@Autowired
    //private HikariDataSource dataSource; 
    
   /* public SolaceListenerService(ListenerControlService listenerControlService, ConnectionFactory connectionFactory) 
    {
    	//this.Jdbctemplate = new JdbcTemplate(Datasource);
        //this.listenerControlService = listenerControlService;
        this.connectionFactory = connectionFactory;
    }*/
    
    //@Autowired
    ///public SolaceListenerService(HikariDataSource Datasource) 
	/*
	 * { System.out.println("constructer calling");
	 * 
	 * this.Jdbctemplate = new JdbcTemplate(Datasource); }
	 */
    
    //@Autowired
   // private ListenerControlService listenerControlService;

   /* @PostConstruct
    public void init() 
    {
    	 System.out.println("init method calling");
    	//this.Jdbctemplate = new JdbcTemplate(dataSource); 
        // Automatically start monitoring and controlling listeners for all queues
        monitorAndControlListeners();
    }
 */
    /*
    public void monitorAndControlListeners() 
    {
        scheduler.scheduleAtFixedRate(() -> {
        	
            try 
            {
            	logger.debug(">>>>>> Checking db status <<<<<<<<<");
            	
            	String sql2 = "SELECT SERVICECD as queue_name FROM solace001";
   			 
    	        List<String> info = Jdbctemplate.queryForList(sql2, String.class);
    	        
    	        System.out.println(info.get(0));
    	        
                // Fetch the queue names and their statuses from the database
                Map<String, Boolean> queueStatusMap = listenerControlService.getQueueStatusMap();

                System.out.println(queueStatusMap.size());
                
                for (Map.Entry<String, Boolean> entry : queueStatusMap.entrySet()) 
                {
                    String queueName = entry.getKey();
                    boolean isActive = entry.getValue();

                    logger.debug("queueName " +queueName+" isActive "+isActive);
                    
                    // Initialize listener if it doesn't exist yet
                    if (!listeners.containsKey(queueName)) 
                    {
                        listeners.put(queueName, new SolaceQueueListenerJMS(Jdbctemplate, connectionFactory, queueName));
                    }

                    SolaceQueueListenerJMS listener = listeners.get(queueName);

                    // Start or stop listener based on status
                    if (isActive) 
                    {
                    	logger.debug("Listener for queue " + queueName + " is ON");
                    	
                        listener.startListener();
                    } 
                    else 
                    {
                    	logger.debug("Listener for queue " + queueName + " is OFF");
                    	
                        listener.stopListener();
                    }
                }
            } catch (Exception e) 
            {
            	logger.debug("Error controlling listeners: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS); // Check the DB every 10 seconds
    } */
}
