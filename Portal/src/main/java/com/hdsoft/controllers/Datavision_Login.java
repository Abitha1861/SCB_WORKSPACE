package com.hdsoft.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.hdsoft.common.Menu_Generation;
import com.hdsoft.models.Adminstration;
import com.hdsoft.models.DASHBOARD;
import com.hdsoft.models.Login_Model;
import com.hdsoft.models.Session_Model;
import com.hdsoft.models.Sysconfig;
import com.hdsoft.utils.Comvalidator2;
import com.hdsoft.utils.EncryptDecrypt;
import com.hdsoft.utils.HMACMD5;
import com.hdsoft.utils.Operlog2;

@Controller
public class Datavision_Login 
{
	 @Autowired
	 public com.hdsoft.models.ForgetPassword fp;
	
	 @Autowired
	 public Adminstration ad;
	
	 @Autowired
	 public Login_Model login;
	 
	 @Autowired
	 public Operlog2 opr;
	 
	 @Autowired
	 public Comvalidator2 cvalid;
	 
	 @Autowired
	 public Menu_Generation MG;
	 
	 @Autowired
	 public Sysconfig sys;
	 
	 @Autowired
	 public Session_Model Session;
	 
	 @Autowired
	 public DASHBOARD dashboard;
		
	 private static final Logger logger = LogManager.getLogger(Datavision_Login.class);
		
     @RequestMapping(value = { "/", "/Datavision", "/login", "/Datavision/login" }, method = RequestMethod.GET)
     public ModelAndView login(Model model , HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("redirect:/Datavision/Dashboard");   
	     }
	     else
	     {
	    	 mv.addObject("SUBORGCODE" , sys.getSuborgcode());
	    	 mv.addObject("SYSCODE" , sys.getSyscode());
	    	 mv.addObject("DBSTATUS", sys.getDBStatus());
	    	
	    	 mv.setViewName("Datavision/Login/Login");
	     }
	    
	     //Technical issue in the database, please contact database admin
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }   
     
     
     @RequestMapping(value = { "/Datavision/Dashboard" }, method = RequestMethod.GET)
     public ModelAndView Dashboard(Model model , HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
    	 ModelAndView mv = new ModelAndView();
	     
	     String PARENT = "DASHBOARD", CHILD = "REQ_MON";
	   
	     if(Session.IsSessionValid(session))
	     {
	    	 mv.setViewName("Datavision/Dashboard/Req_Mon");
	    	 
	    	 mv.addObject("SUBORGCODE", sys.getSuborgcode());
	    	 
	    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
	     }
	     else
	     {
	    	 mv.setViewName("redirect:/login");
	     }
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     }  
        
     @RequestMapping(value = { "/Datavision/login/validate"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String login_process(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
	    
	     details = login.getInternetIntranetDetail(request, response);
	     
	     if(details.get("intflag").getAsString().equals("F"))
	     {
	    	 details.addProperty("Result", "Failed");
	    	 details.addProperty("Message", "Login Failure-Invalid Internet-Intranet Configuration");	 
	    	 
	    	 logger.debug("Login Failure-Invalid Internet-Intranet Configuration");
	    	 
	    	 return details.toString();
	     }
	     else
	     {
			 JsonObject Inputs = new JsonObject();
			 
			 String INTFLAG =  "0"; 
			 String txtDomainId = request.getParameter("txtDomainId");
			 String txtUserId = request.getParameter("txtUserId");
			 String hashedPassword = request.getParameter("hashedPassword");
			 String randomSalt = request.getParameter("randomSalt");
			 
			 Inputs.addProperty("INTFLAG",  INTFLAG);
			 Inputs.addProperty("txtDomainId",  txtDomainId);
			 Inputs.addProperty("txtUserId",  txtUserId);
			 Inputs.addProperty("hashedPassword", hashedPassword);
			 Inputs.addProperty("CIP", (String) request.getRemoteAddr());
			 Inputs.addProperty("SIP",  (String) request.getLocalAddr());
			 
			 details = login.updateValues(Inputs, randomSalt); 
			 
			 if(details.get("Result").getAsString().equals("Success"))
			 {
				 if(details.get("sucFlg").getAsString().equals("1"))
				 {	
					    String domainId = details.get("sesDomainID").getAsString();
						String userId = details.get("sesUserId").getAsString();
																 
						String curr_date = details.get("sesMcontDate").getAsString();
						String suboffcdsess = details.get("suboffcdsess").getAsString();
								
						String sesUserName = details.get("sesUserName").getAsString();
						String sesUserId = details.get("sesUserId").getAsString();
										
						String sesRole = details.get("sesRole").getAsString();
						String sesMcontDate = details.get("sesMcontDate").getAsString();
						
						String sesbaseSubUnits = details.get("sesbaseSubUnits").getAsString();
						String sesCustCode = details.get("sesCustCode").getAsString();
						
						String sesCustName = details.get("sesCustName").getAsString();
						String sesEmail = details.get("sesEmail").getAsString();
						
						String sesForcePwd = details.get("sesForcePwd").getAsString();
						String sesForceUserPwd = details.get("sesForceUserPwd").getAsString();
						
						String sesPinReset = details.get("sesPinReset").getAsString();
						
						boolean flag = false;
						
						JsonObject map = login.getSessionInfo(domainId, userId);
						
						String sessId = map.get("ULOGIN_SESSION_ID").getAsString();
						String sess_logout =  map.get("ULOGIN_OUT_DATE").getAsString();;
						
						JsonObject map1 = login.getSessionInterval(curr_date);
						
						String session_timeout = map1.get("SYS_AUTO_LOGOUT").getAsString();
						String mult_sess_allow = map1.get("SYS_MULT_ALLOW").getAsString();
						
						int interval = 300;
						
						try 
						{
							interval = Integer.parseInt(session_timeout);
						} 
						catch (Exception e) 
						{
							 details.addProperty("Result", "Failed");
					    	 details.addProperty("Message", e.getLocalizedMessage());	 
					    	 
					    	 logger.debug("Exception when interval :::::: "+e.getLocalizedMessage());
						}

						if(sessId != null && sess_logout == null)
						{
							String value = (String) session.getServletContext().getAttribute(sessId);
							
							if (value != null)
							{
								String[] array = value.split("\\|");
								
								if(array[0].equals(domainId) && array[1].equals(userId))
								{
									long time1 = Long.parseLong(array[2]);
									long time2 = Calendar.getInstance().getTimeInMillis();

									if(session_timeout != null && !(session_timeout.equals(""))) 
									{
										int final_time = (int) (time2 - time1) / 1000;
										
										if (final_time > interval) 
										{
											flag = true;
											//updateLogoutDetails(resultDTO, request);
										}
										else 
										{
											if (mult_sess_allow != null && mult_sess_allow.equals("N")) 
											{
												session.setAttribute("ses_remain_time",(interval - final_time) / 60+ "");
											}
										}
									}
								}
								else 
								{
									flag = true;
								}
							} 
							else 
							{
								flag = true;
							}
						} 
						else 
						{
							flag = true;
						}
						
						if(flag || mult_sess_allow.equals("Y")) 
						{
							logger.debug("SessionID:" + session.getId());
							logger.debug("Maximum Inactive Interval Value : "+ interval);
							
							session = request.getSession(true);
							
							session.setMaxInactiveInterval(45 * 60 * 1000);
							
							logger.debug("Maximum Inactive Interval Value Applies: "+ session.getMaxInactiveInterval());
							
							
							long current_access = Calendar.getInstance().getTimeInMillis();
							String acc_time = String.valueOf(current_access);
							String browser = request.getHeader("User-Agent");
							
							String value = userId + request.getRemoteAddr() + browser;
							session.setAttribute("clientval", value);
							
							String key = String.valueOf(acc_time);
							session.setAttribute("acc_time", key);
							
							String hmacmd5 = HMACMD5.string2md5HMA(key, value);
							
							String sessionId = session.getId();
							
							JsonObject infoo = new JsonObject();
							
							infoo.addProperty("SUBORGCODE", sys.getSuborgcode());
							infoo.addProperty("USER_ID", sesUserId);
							infoo.addProperty("SESSION_ID", sessionId);
							infoo.addProperty("IP", request.getRemoteAddr());
							infoo.addProperty("STATUS", "1"); 

							String status = login.UpdateSignInoutDetails(infoo).get("USER_STATUS").getAsString();
							
							if(status.equals("1") && mult_sess_allow.equals("N")) 
							{
								session.setAttribute("ses_remain_time", "");
								
							}

							session.setAttribute("sesSessionID", sessionId);
							session.setAttribute("sesDomainID", domainId);
							session.setAttribute("suboffcdsess", suboffcdsess);
							session.setAttribute("SYSTEMCURR", "DJF"); 
									
							session.setAttribute("sesUserId", userId);
							
							session.setAttribute("sesUserName", sesUserName);
							session.setAttribute("sessionIP", request.getRemoteAddr());
							session.setAttribute("sessionUserAgent", request.getHeader("User-Agent"));
							session.setAttribute("sessionUserCookie", hmacmd5);
							
							session.setAttribute(sessionId, domainId + "|"+ sesUserId + "|"+ System.currentTimeMillis());
							session.setAttribute("sesMcontDate", sesMcontDate);
							session.setAttribute("sesbaseSubUnits", sesbaseSubUnits);
							session.setAttribute("sesCustCode", sesCustCode);
							session.setAttribute("sesCustName", sesCustName);

							session.setAttribute("sesDepBnk", "024");
							session.setAttribute("sesDepBrn", "415100");
							session.setAttribute("sesSubCustCode", "DEALR1");
							
							
							session.setAttribute("sesRole", sesRole);
							session.setAttribute("sesEmail", sesEmail);
							session.setAttribute("sesForcePwd", sesForcePwd);
							session.setAttribute("sesForceUserPwd", sesForceUserPwd);
							
							if(sesPinReset != null )
							{
								session.setAttribute("sesPinReset", sesPinReset);
							}
							else 
							{
								session.setAttribute("sesPinReset", "0");
							}
							
							session.setAttribute("cnt", "0");
							
							Date userlogintime = null;
							
							userlogintime = cvalid.fetchUserLoginTime(sesUserId, domainId);
							
							DateFormat df = new java.text.SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
							
							String current_time = df.format(userlogintime);
							
							session.setAttribute("sessionUserLoginTime", current_time);
							
							
							String Photo = login.loadImage(domainId, userId, request).get("Photo").getAsString();
							
							session.setAttribute("sess_user_photo", Photo);
							
							
							JsonObject Menu_details = MG.Get_Menus(sys.getSuborgcode(), sesRole, request);
							
							logger.debug("Menu details :: "+Menu_details);
							
							session.setAttribute("Menu_details", Menu_details.toString());

							session.setAttribute("ACCESS_GRANTED", "1");
							
							logger.debug("Navigated to Landing page");
							
							details.addProperty("Action", request.getContextPath()+"/Datavision/Dashboard");
							
							details.addProperty("Result", "Success");
							details.addProperty("Message", "ACCESS_GRANTED");
					
							logger.debug(">>>>>>>>>>> ACCESS_GRANTED <<<<<<<<<<<<<<<");
						} 
						else 
						{
							logger.debug("Login Failure-Redirected to Login page");
							
							details.addProperty("Result", "Failed");
							details.addProperty("Message", "Login Failed");
							 
							logger.debug(">>>>>>>>>>> Login Failure-Redirected to Login page <<<<<<<<<<<<<<<");
						}
					} 
				    else 
				    { 
						if(details.get("errMsg").getAsString().equalsIgnoreCase("User ID is Locked Contact System Administrator")) 
						{
							logger.debug("User ID is Locked");
							
							
						}
						
						details.addProperty("Result", "Failed");
						details.addProperty("Message", "User ID is Locked");
					}
			 }
	     }  
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }   
     
     @RequestMapping(value = { "/Datavision/AD/login/validate"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String login_adprocess(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
	    
	     details = login.getInternetIntranetDetail(request, response);
	     
	     if(details.get("intflag").getAsString().equals("F"))
	     {
	    	 details.addProperty("Result", "Failed");
	    	 details.addProperty("Message", "Login Failure-Invalid Internet-Intranet Configuration");	 
	    	 
	    	 logger.debug("Login Failure-Invalid Internet-Intranet Configuration");
	    	 
	    	 return details.toString();
	     }
	     else
	     {
    	 	
			 JsonObject Inputs = new JsonObject();
			 
			 String INTFLAG =  "0"; 
			 String txtDomainId = request.getParameter("txtDomainId");
			 String txtUserId = request.getParameter("txtUserId");
			 String hashedPassword = request.getParameter("hashedPassword");
			 String randomSalt = request.getParameter("randomSalt");
			 
			 Inputs.addProperty("INTFLAG",  INTFLAG);
			 Inputs.addProperty("txtDomainId",  txtDomainId);
			 Inputs.addProperty("txtUserId",  txtUserId);
			 Inputs.addProperty("hashedPassword", hashedPassword);
			 Inputs.addProperty("CIP", (String) request.getRemoteAddr());
			 Inputs.addProperty("SIP",  (String) request.getLocalAddr());
			 
			 details = login.updateValues_AD(Inputs, randomSalt); 
			 
			 if(details.get("Result").getAsString().equals("Success"))
			 {
				 if(details.get("sucFlg").getAsString().equals("1"))
				 {	
					    String domainId = details.get("sesDomainID").getAsString();
						String userId = details.get("sesUserId").getAsString();
																 
						String curr_date = details.get("sesMcontDate").getAsString();
						String suboffcdsess = details.get("suboffcdsess").getAsString();
								
						String sesUserName = details.get("sesUserName").getAsString();
						String sesUserId = details.get("sesUserId").getAsString();
										
						String sesRole = details.get("sesRole").getAsString();
						String sesMcontDate = details.get("sesMcontDate").getAsString();
						
						String sesbaseSubUnits = details.get("sesbaseSubUnits").getAsString();
						String sesCustCode = details.get("sesCustCode").getAsString();
						
						String sesCustName = details.get("sesCustName").getAsString();
						String sesEmail = details.get("sesEmail").getAsString();
						
						String sesForcePwd = details.get("sesForcePwd").getAsString();
						String sesForceUserPwd = details.get("sesForceUserPwd").getAsString();
						
						String sesPinReset = details.get("sesPinReset").getAsString();
						
						logger.debug("DomainId:" + domainId);
						logger.debug("Userid:" + userId);
						logger.debug("CBD:" + curr_date);
						
						boolean flag = false;
						
						JsonObject map = login.getSessionInfo(domainId, userId);
						
						String sessId = map.get("ULOGIN_SESSION_ID").getAsString();
						String sess_logout =  map.get("ULOGIN_OUT_DATE").getAsString();;
						
						JsonObject map1 = login.getSessionInterval(curr_date);
						
						String session_timeout = map1.get("SYS_AUTO_LOGOUT").getAsString();
						String mult_sess_allow = map1.get("SYS_MULT_ALLOW").getAsString();
						
						int interval = 300;
						
						try 
						{
							interval = Integer.parseInt(session_timeout);
						} 
						catch (Exception e) 
						{
							 details.addProperty("Result", "Failed");
					    	 details.addProperty("Message", e.getLocalizedMessage());	 
					    	 
					    	 logger.debug("Exception when interval :::::: "+e.getLocalizedMessage());
						}

						if(sessId != null && sess_logout == null)
						{
							String value = (String) session.getServletContext().getAttribute(sessId);
							
							if (value != null)
							{
								String[] array = value.split("\\|");
								
								if(array[0].equals(domainId) && array[1].equals(userId))
								{
									long time1 = Long.parseLong(array[2]);
									long time2 = Calendar.getInstance().getTimeInMillis();

									if(session_timeout != null && !(session_timeout.equals(""))) 
									{
										int final_time = (int) (time2 - time1) / 1000;
										
										if (final_time > interval) 
										{
											flag = true;
											//updateLogoutDetails(resultDTO, request);
										}
										else 
										{
											if (mult_sess_allow != null && mult_sess_allow.equals("N")) 
											{
												session.setAttribute("ses_remain_time",(interval - final_time) / 60+ "");
											}
										}
									}
								}
								else 
								{
									flag = true;
								}
							} 
							else 
							{
								flag = true;
							}
						} 
						else 
						{
							flag = true;
						}
						
						if(flag || mult_sess_allow.equals("Y")) 
						{
							logger.debug("SessionID:" + session.getId());
							logger.debug("Maximum Inactive Interval Value : "+ interval);
							
							session = request.getSession(true);
							
							session.setMaxInactiveInterval(45 * 60 * 1000);
							
							logger.debug("Maximum Inactive Interval Value Applies: "+ session.getMaxInactiveInterval());
							
							long current_access = Calendar.getInstance().getTimeInMillis();
							String acc_time = String.valueOf(current_access);
							String browser = request.getHeader("User-Agent");
							
							String value = userId + request.getRemoteAddr() + browser;
							session.setAttribute("clientval", value);
							
							String key = String.valueOf(acc_time);
							session.setAttribute("acc_time", key);
							
							String hmacmd5 = HMACMD5.string2md5HMA(key, value);
							
							String sessionId = session.getId();
							
							JsonObject infoo = new JsonObject();
							
							infoo.addProperty("SUBORGCODE", sys.getSuborgcode());
							infoo.addProperty("USER_ID", sesUserId);
							infoo.addProperty("SESSION_ID", sessionId);
							infoo.addProperty("IP", request.getRemoteAddr());
							infoo.addProperty("STATUS", "1"); 

							String status = login.UpdateSignInoutDetails(infoo).get("USER_STATUS").getAsString();
							
							if(status.equals("1") && mult_sess_allow.equals("N")) 
							{
								session.setAttribute("ses_remain_time", "");
							}

							session.setAttribute("sesSessionID", sessionId);
							session.setAttribute("sesDomainID", domainId);
							session.setAttribute("suboffcdsess", suboffcdsess);
							session.setAttribute("SYSTEMCURR", "TZS"); 
									
							session.setAttribute("sesUserId", userId);
							
							session.setAttribute("sesUserName", sesUserName);
							session.setAttribute("sessionIP", request.getRemoteAddr());
							session.setAttribute("sessionUserAgent", request.getHeader("User-Agent"));
							session.setAttribute("sessionUserCookie", hmacmd5);
							
							session.setAttribute(sessionId, domainId + "|"+ sesUserId + "|"+ System.currentTimeMillis());
							session.setAttribute("sesMcontDate", sesMcontDate);
							session.setAttribute("sesbaseSubUnits", sesbaseSubUnits);
							session.setAttribute("sesCustCode", sesCustCode);
							session.setAttribute("sesCustName", sesCustName);

							session.setAttribute("sesDepBnk", "024");
							session.setAttribute("sesDepBrn", "415100");
							session.setAttribute("sesSubCustCode", "DEALR1");
							
							
							session.setAttribute("sesRole", sesRole);
							session.setAttribute("sesEmail", sesEmail);
							session.setAttribute("sesForcePwd", sesForcePwd);
							session.setAttribute("sesForceUserPwd", sesForceUserPwd);
							
							if(sesPinReset != null )
							{
								session.setAttribute("sesPinReset", sesPinReset);
							}
							else 
							{
								session.setAttribute("sesPinReset", "0");
							}
							
							session.setAttribute("cnt", "0");
							
							Date userlogintime = null;
							
							userlogintime = cvalid.fetchUserLoginTime(sesUserId, domainId);
							
							DateFormat df = new java.text.SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
							
							String current_time = df.format(userlogintime);
							
							session.setAttribute("sessionUserLoginTime", current_time);
								
							String Photo = login.loadImage(domainId, userId, request).get("Photo").getAsString();
							
							session.setAttribute("sess_user_photo", Photo);
							
							JsonObject Menu_details = MG.Get_Menus(sys.getSuborgcode(), sesRole, request);
							
							logger.debug("Menu details :: "+Menu_details);
							
							session.setAttribute("Menu_details", Menu_details.toString());

							session.setAttribute("ACCESS_GRANTED", "1");
							
							logger.debug("Navigated to Landing page");
							
							details.addProperty("Action", request.getContextPath()+"/Datavision/Dashboard");
							
							details.addProperty("Result", "Success");
							details.addProperty("Message", "ACCESS_GRANTED");
					
							logger.debug(">>>>>>>>>>> ACCESS_GRANTED <<<<<<<<<<<<<<<");
						} 
						else 
						{
							logger.debug("Login Failure-Redirected to Login page");
							
							details.addProperty("Result", "Failed");
							details.addProperty("Message", "Login Failed");
							 
							logger.debug(">>>>>>>>>>> Login Failure-Redirected to Login page <<<<<<<<<<<<<<<");
						}
					} 
				    else 
				    { 
						if(details.get("errMsg").getAsString().equalsIgnoreCase("User ID is Locked Contact System Administrator")) 
						{
							logger.debug("User ID is Locked");	
						}
						
						details.addProperty("Result", "Failed");
						details.addProperty("Message", "User ID is Locked");
					}
			 }
	     }  
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }   
     
     @RequestMapping(value = { "/Datavision/getcurrenttime"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String getcurrenttime(Model model ,HttpServletRequest request,HttpServletResponse response) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
	     
	     details = login.fetchCurrentTime();
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }   
     
     @RequestMapping(value = { "/Datavision/Encrypt"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String Encrypt(@RequestParam("EncStr") String password, @RequestParam("ciphertext") String encryptionKey, HttpServletRequest request,HttpServletResponse response) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
	     
    	 EncryptDecrypt enc = new EncryptDecrypt();
    	 
    	 String finalHash = enc.doEncrypt( password, encryptionKey);
    	 
    	 finalHash = finalHash.replaceAll("\\s+", "");
    	 
    	 details.addProperty("Result",  "Success");
    	 details.addProperty("final_String", finalHash);
	     
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }   
     
     @RequestMapping(value = { "/Datavision/updatePageEntry"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String updatePageEntry(HttpServletRequest request,HttpServletResponse response) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
	     
    	 String sesDomainID = request.getParameter("sesDomainID");
    	 String sesUserId = request.getParameter("sesUserId");
    	 String sessionIP = request.getParameter("sessionIP");
    	 String sessionUserAgent = request.getParameter("sessionUserAgent");
    	 String sesRole = request.getParameter("sesRole");
    	 String sessionUserLoginTime = request.getParameter("sessionUserLoginTime");
    	 String PROGRAMID = request.getParameter("PROGRAMID");
    	 
    	 JsonObject input_value = new JsonObject();
    	 
    	 input_value.addProperty("sesDomainID", sesDomainID != null ? sesDomainID : "") ;
    	 input_value.addProperty("sesUserId", sesUserId != null ? sesUserId : "");
    	 input_value.addProperty("sessionIP", sessionIP != null ? sessionIP : "");
    	 input_value.addProperty("sessionUserAgent", sessionUserAgent != null ? sessionUserAgent : "");
    	 input_value.addProperty("sesRole", sesRole != null ? sesRole : "");		
    	 input_value.addProperty("sessionUserLoginTime", sessionUserLoginTime != null ? sessionUserLoginTime : "");		
    	 input_value.addProperty("PROGRAMID", PROGRAMID != null ? PROGRAMID : "");
 		
    	 details = opr.updatePageEntry(input_value);
    	  
    	 
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }   
     
     @RequestMapping(value = { "/Datavision/updatePageExit"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String updatePageExit(HttpServletRequest request,HttpServletResponse response) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
	     
    	 String sesDomainID = request.getParameter("sesDomainID");
    	 String sesUserId = request.getParameter("sesUserId");
    	 String sessionUserLoginTime = request.getParameter("sessionUserLoginTime");
    	 String PROGRAMID = request.getParameter("PROGRAMID");
    	 String OPERLOGSL = request.getParameter("OPERLOGSL");
 
    	 JsonObject input_value = new JsonObject();
    	 
    	 input_value.addProperty("sesDomainID", sesDomainID != null ? sesDomainID : "") ;
    	 input_value.addProperty("sesUserId", sesUserId != null ? sesUserId : "");	
    	 input_value.addProperty("sessionUserLoginTime", sessionUserLoginTime != null ? sessionUserLoginTime : "");		
    	 input_value.addProperty("PROGRAMID", PROGRAMID != null ? PROGRAMID : "");
    	 input_value.addProperty("OPERLOGSL", OPERLOGSL != null ? PROGRAMID : "");
 		
    	 details = opr.updatePageExit(input_value);
    	  
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }   
     
     @RequestMapping(value = { "/Datavision/getCurrency"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String getCurrency(HttpServletRequest request,HttpServletResponse response) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
 		
    	 details = cvalid.getCurrency();
    	  
    	 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
	     return details.toString();
     }
     
     @RequestMapping(value = { "/Datavision/logout"}, method = RequestMethod.GET)
     public ModelAndView logout(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     try
	     {
	    	 JsonObject Info = new JsonObject();
	 	    
		     Info.addProperty("SUBORGCODE", sys.getSuborgcode());
		     Info.addProperty("USER_ID",  (String)session.getAttribute("sesUserId"));
		     Info.addProperty("SESSION_ID", (String)session.getAttribute("sesSessionID"));
		     Info.addProperty("IP", (String)session.getAttribute("sessionIP"));
		     Info.addProperty("IP2", request.getRemoteAddr());
		     Info.addProperty("STATUS", "2");
		     
		     logger.debug("logout info >>>> "+Info);
		     
			 login.UpdateSignInoutDetails(Info);
		   
		     session.invalidate();
	     }
	     catch(Exception ex)
	     {
	    	logger.debug("Exception in logout >>>> "+ex.getLocalizedMessage()); 
	     }
	     
	     mv.setViewName("redirect:/Datavision/login");
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     } 
     
     @RequestMapping(value = { "/Datavision/auto/logout"}, method = RequestMethod.GET)
     public ModelAndView auto_logout(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
     {	 
	     ModelAndView mv = new ModelAndView();
	     
	     mv.setViewName("redirect:/Datavision/login");
	     
	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");

	     return mv;
     } 
     
     @RequestMapping(value = { "/Datavision/auto/logout"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     public @ResponseBody String logout2(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, SQLException 
     {	 
    	 JsonObject details = new JsonObject();
    	 
    	 try
	     {
	    	 JsonObject Info = new JsonObject();
	    	 
		     Info.addProperty("SUBORGCODE", (String)request.getParameter("sesDomain"));
		     Info.addProperty("USER_ID",  (String)request.getParameter("sesUserId"));
		     Info.addProperty("SESSION_ID", (String)request.getParameter("sesSessionID"));
		     Info.addProperty("IP", (String)request.getParameter("sessionIP"));
		     Info.addProperty("IP2", request.getRemoteAddr());
		     Info.addProperty("STATUS", "2");
		     
		     logger.debug("logout info >>>> "+Info);
		     
			 login.UpdateSignInoutDetails(Info);
		   
		     session.invalidate();
	     }
	     catch(Exception ex)
	     {
	    	logger.debug("Exception in logout >>>> "+ex.getLocalizedMessage()); 
	     }
 	     
 	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	     response.setHeader("Pragma","no-cache");
	     response.setHeader("Expires","0");
	     
 	     return details.toString();
     }
     
     /* --------------------------------------------------------------------------------- */
	 
		@RequestMapping(value = { "/getotp"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	     public @ResponseBody String getotp(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, SQLException 
	     {	 
	    	 JsonObject details = new JsonObject();
	 		 JsonObject js1 = new JsonObject();
	 		 
	 		 js1.addProperty("token",request.getParameter("token"));
	 		 js1.addProperty("type",request.getParameter("type"));
	 		 js1.addProperty("username",request.getParameter("username"));
	 		 js1.addProperty("unique",request.getParameter("unique"));
	 		 js1.addProperty("sendthrough",request.getParameter("sendthrough"));
	 		 
	 		 logger.debug(js1);
	 		 
	 	     details = fp.getToken(js1);
	 	     
	 	    logger.debug(details);
	 	     
	 	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		     response.setHeader("Pragma","no-cache");
		     response.setHeader("Expires","0");
		     
	 	     return details.toString();
	     }
	 	
		
		@RequestMapping(value = { "/validateotp"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String validate(Model model ,HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException, SQLException 
	    {	 
	   	 JsonObject details = new JsonObject();
			 JsonObject js1 = new JsonObject();
			 
			 js1.addProperty("token",request.getParameter("token"));
			 js1.addProperty("type",request.getParameter("type"));
			 js1.addProperty("otp",request.getParameter("otp"));
			 js1.addProperty("unique",request.getParameter("unique"));
			 js1.addProperty("sendthrough",request.getParameter("sendthrough"));
			 
			 logger.debug(js1);
			 
		     details = fp.Validate(js1);
		     
		    logger.debug(details);
		     
		     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		     response.setHeader("Pragma","no-cache");
		     response.setHeader("Expires","0");
		    
		     return details.toString();
	    }
		
		@RequestMapping(value = {"/Forget_Password_Reset"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Forget_Password_Reset(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	    {	 
	   	 JsonObject details = new JsonObject();
	   	 JsonObject InaddPropertys = new JsonObject();
		 		 try
		 		 {
		 			InaddPropertys.addProperty("torgcd", request.getParameter("txtDomainId"));
		 			InaddPropertys.addProperty("tuserid", request.getParameter("tuserid"));
		 			InaddPropertys.addProperty("hashedPassword", request.getParameter("hashedPassword"));//randomSalt
		 			InaddPropertys.addProperty("randomSalt", request.getParameter("randomSalt"));
		 			
		 			InaddPropertys.add("prepassword", fp.prepassword(InaddPropertys));
		 			
		 			logger.debug(InaddPropertys);
		 			
		 			JsonObject resultDTO = ad.updateValues_Password(InaddPropertys, session);
		 			
		 			String val = resultDTO.get("sucFlg").getAsString();
		 			
		 			logger.debug(val);
		 			
		 			if(val.equals("1"))
					{
						details.addProperty("Result", "Success");
						details.addProperty("Message", "Succesfully Updated !!");
						
						JsonObject b = fp.addDetails(InaddPropertys);
						
						details.add("addDetails", b);
						
						logger.debug(b);
					}
					else
					{
						details.addProperty("Result", "Failed");
						details.addProperty("Message", "Something went Wrong !!");
					}	
				}
				catch (Exception e)
				{
					details.addProperty("Result", "Failed");
					details.addProperty("Message", e.getLocalizedMessage()); e.printStackTrace();
				}
	   	 
		 		 response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			     response.setHeader("Pragma","no-cache");
			     response.setHeader("Expires","0");
			     
		         return details.toString();
	    }
	 	
	    @RequestMapping(value = {"/Datavision/ForgetPassword"}, method = RequestMethod.GET)
	     public ModelAndView ForgetPassword(Model model , HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException 
	     {	 
	 	     ModelAndView mv = new ModelAndView();
	 	     
	 	     mv.setViewName("Datavision/Login/Frgt_Pwd");
	 	     
	 	     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); 
	 	     response.setHeader("Pragma","no-cache");
	 	     response.setHeader("Expires","0");

	 	     return mv;
	     }
	    
	    //-------------------------------------------------
	 	@RequestMapping(value = {"/Dashboard/Success_fail_2"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	    public @ResponseBody String Success_fail_2(@RequestParam("fromDate") String fromDate , @RequestParam("toDate") String toDate, HttpServletRequest request, HttpServletResponse response) 
	    {	 
		   	JsonObject details = new JsonObject();
		   	
		    details = dashboard.Success_fail_2(fromDate , toDate);  
		    
		    System.out.println("fromDate = Success_fail_2" + fromDate);
		    

		    System.out.println("toDate = Success_fail_2" + toDate);
		    
		    System.out.println("details = Success_fail_2" + details);
			   		
		   	return details.toString();  	
	    }	
	 	
	     @RequestMapping(value = {"/Dashboard/Success_fail"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	     public @ResponseBody String Success_fail(HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	     {	 
	    	 JsonObject details = new JsonObject();
			  
	    	 details = dashboard.Success_fail();  

		     return details.toString();
	     }
	     
	     @RequestMapping(value = {"/Dashboard/overall"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	     public @ResponseBody String overall(HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	     {	 
	    	 JsonObject details = new JsonObject();
			  
	    	 details = dashboard.overall_card();  
	    	 
		     return details.toString();
	     }
	     
	     @RequestMapping(value = {"/Dashboard/overall_two"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	     public @ResponseBody String overall_two(@RequestParam("fromDate") String fromDate , @RequestParam("toDate") String toDate, HttpServletRequest request,HttpServletResponse response, HttpSession session) 
	     {	 
	    	 JsonObject details = new JsonObject();
	    	 
	    	 details = dashboard.overall_card_two(fromDate , toDate);  
	    	 
		     return details.toString();
	     }
	     
	     @RequestMapping(value = { "/Datavision/Details" }, method = RequestMethod.GET)
	     public ModelAndView Details(Model model , HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	     {	 
	    	 ModelAndView mv = new ModelAndView();
		     
		     String PARENT = "DASHBOARD", CHILD = "Details";
		   
		     if(Session.IsSessionValid(session))
		     {
		    	 mv.setViewName("Datavision/Dashboard/Details");
		    	 
		    	 mv.addObject("SUBORGCODE", sys.getSuborgcode());
		    	 
		    	 mv.addObject("Menu", MG.Get_Menus_HTML(session, PARENT, CHILD));
		     }
		     else
		     {
		    	 mv.setViewName("redirect:/login");
		     }
		     
		     response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		     response.setHeader("Pragma","no-cache");
		     response.setHeader("Expires","0");

		     return mv;
	     }
	     
	 	
}