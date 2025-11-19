package com.hdsoft.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.BOT_Suptech_GEMS;
import com.hdsoft.models.BOT_Suptect_FM;
import com.hdsoft.models.BOT_Suptect_Trade;
import com.hdsoft.models.Reports_Model;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.solace.QueueConsumerJNDI;
import com.hdsoft.solace.TopicPublisher;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class Solace_Management 
{
	public JdbcTemplate Jdbctemplate;
			
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource){
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
		
	@Autowired
	public Reports_Model rm;
	
	@Autowired
	public Menu_Generation MG;
	
	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Session_Model Session;
	 
	@Autowired
	public TopicPublisher Topic;
	
	@Autowired
	public QueueConsumerJNDI Solace;
	
	@Autowired
	public BOT_Suptect_FM FM;
	
	@Autowired
	public BOT_Suptech_GEMS Gems;
	
    @RequestMapping(value = {"/Solace/Topic/Publisher"}, method = RequestMethod.GET)
    public ModelAndView RTSIS_REPORT(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
    {	 
	     ModelAndView mv = new ModelAndView();
	   
	     String PARENT = "SOLACE", CHILD = "TPUBLISH";
	     
	     if(Session.IsSessionValid(session, PARENT, CHILD))
	     {
	    	 mv.setViewName("/Datavision/Solace/Topic_Publisher");  
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/Datavision/login");
	     }
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
    }
	
    
    @RequestMapping(value = {"/Datavision/Solace/Topic/Publish/Message"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Dashboard_User_Reports(@RequestParam("CHCODE") String CHCODE,  @RequestParam("SERVICECD") String SERVICECD,  @RequestParam("MESSAGE") String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
   	 	JsonObject details = new JsonObject();
		  
   	 	if(Session.IsSessionValid(session))
	    {
   	 		if(CHCODE.equalsIgnoreCase("FM"))
   	 		{
   	 			if(SERVICECD.equals("FM001") || SERVICECD.equals("FM001-HST"))
   	 			{
   	 				FM.invDebtSecuritiesData("RTS005", "", SERVICECD);
   	 			}
   	 			else if(SERVICECD.equals("FM002") || SERVICECD.equals("FM002-HST"))
	 			{
   	 				FM.interbankLoansReceivable("RTS017", "", SERVICECD);		
	 			}
	   	 		else if(SERVICECD.equals("FM003") || SERVICECD.equals("FM003-HST"))
	 			{
	   	 			FM.interbankLoanPayable("RTS071", "", SERVICECD);	
	 			}
		   	 	else if(SERVICECD.equals("FM004") || SERVICECD.equals("FM004-HST"))
	 			{
		   	 		FM.DepositWithdrawalData("RTS067", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM005") || SERVICECD.equals("FM005-HST"))
	 			{
	 				FM.forexTransaction("RTS129",  "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM006") || SERVICECD.equals("FM006-HST"))
	 			{
	 				
	 			}
		   	 	else if(SERVICECD.equals("FM007") || SERVICECD.equals("FM007-HST"))
	 			{
	 				FM.TbondTransaction("RTS135", "", SERVICECD);  
	 			}
		   	 	else if(SERVICECD.equals("FM008") || SERVICECD.equals("FM008-HST"))
	 			{
		   	 		FM.ibcmTransaction("RTS133", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM009") || SERVICECD.equals("FM009-HST"))
	 			{
		   	 		FM.boughtForwardExchangeData("RTS085", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM010") || SERVICECD.equals("FM010-HST"))
	 			{
		   	 	    FM.SoldForwardExchangeData("RTS087", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM011") || SERVICECD.equals("FM011-HST"))
	 			{
		   	 		FM.securitiesPurchasedData("RTS101", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM012") || SERVICECD.equals("FM012-HST"))
	 			{
		   	    	FM.securitiesSoldData("RTS099", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM013") || SERVICECD.equals("FM013-HST"))
	 			{
		   	 		FM.currencySwapData("RTS107", "", SERVICECD);
	 			}
		   	 	else if(SERVICECD.equals("FM014") || SERVICECD.equals("FM014-HST"))
	 			{
		   	 		FM.interestRateSwapData("RTS109", "", SERVICECD);
	 			}
   	 		}
   	 		else if(CHCODE.equalsIgnoreCase("GEMS"))
   	 	    {
   	 			if(SERVICECD.equals("GEMS001"))
	 			{
   	 				Gems.complaintStatistics("RTS149", "", SERVICECD);
	 			}
   	 			else if(SERVICECD.equals("GEMS002"))
	 			{
		 			Gems.fraudIncidenceInformation("RTS183", "", SERVICECD);
	 			}
   	 	    }
   	 		else if(CHCODE.equalsIgnoreCase("SOLACE") && (SERVICECD.equals("SOL004") || SERVICECD.equals("SOL009")))
   	 		{
 				String[] ms = MESSAGE.split("\\|");
 			
 			    if(ms.length == 2)
 			    {
   	 				String sql = "select BODY_MSG from request001 where MSGTYPE = ? and REQDATE = ? and UNIREFNO = ?";
   	 				
   	 				List<String> Message = Jdbctemplate.queryForList(sql, new Object[] { SERVICECD, ms[0], ms[1] }, String.class);
   	 			
   	 				BOT_Suptect_Trade TRADE = new BOT_Suptect_Trade(Jdbctemplate);
				
   	 				TRADE.Trade_Master(Message.size() > 0 ? Message.get(0) : "");
 			    }
   	 		}
   	 		else if(CHCODE.equalsIgnoreCase("SOLACE") && (SERVICECD.equals("SOL001")))
	 		{
   	 			  Common_Utils util = new Common_Utils();
   	 		
   	 			  QueueConsumerJNDI Que = new QueueConsumerJNDI(Jdbctemplate);
   	 			
   	 			  JsonObject webdt = Que.Get_Solace_Info(CHCODE, SERVICECD);
   	 			 
   	 			  String Refno = util.Generate_Random_String(15); 
   	 			 
   	 			  Que.Insert_Request_001(webdt, MESSAGE, "", Refno, SERVICECD);
   	 			 
   	 			  Que.Insert_FileIT_Job(CHCODE, SERVICECD, MESSAGE, Refno);
	 		}
   	 		else
   	 		{
   	 			Common_Utils util = new Common_Utils();
   	 		
   	 			String Ref = util.Generate_Random_String(15);
   	 			
   	 			String[] ms = MESSAGE.split("\\|");
   	 		
	   	 		if(ms.length == 2)
	   	 		{
	   	 			new QueueConsumerJNDI(Jdbctemplate).Solace_Router(CHCODE, SERVICECD, ms[0], ms[1], Ref);
	   	 		}
	   	 		else
	   	 		{
	   	 			new QueueConsumerJNDI(Jdbctemplate).Solace_Router(CHCODE, SERVICECD, ms[0], "", Ref);
	   	 		}
   	 		}
   	 				
   	 		details.addProperty("Result", "Success");
		 	details.addProperty("Message", "Message published successfully");
	    }
   	 	else
   	 	{
   	 		details.addProperty("Result", "Failed");
		 	details.addProperty("Message", "Session Expired");
   	 	}
   	 
   	 	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
   	 	response.setHeader("Pragma","no-cache");
   	 	response.setHeader("Expires","0");
   	 	
   	 	return details.toString();
    }
    
}
