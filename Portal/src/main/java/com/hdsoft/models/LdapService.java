package com.hdsoft.models;

import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.web_service_001;
import com.hdsoft.Repositories.web_service_002;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
//@Service
public class LdapService {
	
	private static final Logger logger = LogManager.getLogger(LdapService.class);
	
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@RequestMapping(value = {"/Datavision/ldap-user-list/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_listService2(HttpServletRequest request,HttpServletResponse response, HttpSession session) throws Exception 
    {	 
 	 	  return getADUsers().toString();
 	}
	
	@RequestMapping(value = {"/Datavision/ldap-user-find/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service2(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws Exception 
    {	 
 	 	  Common_Utils util = new Common_Utils();
 	 	  
 	 	  JsonObject js = util.StringToJsonObject(MESSAGE);
 	 	  
 	 	  String username = js.get("username").getAsString();
 	 	  
 	 	  return FindUser(username).toString();
 	}
	
	@RequestMapping(value = {"/Datavision/ldap-user-login/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws Exception 
    {	 
 	 	  Common_Utils util = new Common_Utils();
 	 	  
 	 	  JsonObject js = util.StringToJsonObject(MESSAGE);
 	 	  
 	 	  String username = js.get("username").getAsString();
 	 	  String password = js.get("password").getAsString();  
 	 	  
 	 	  boolean res = ADLogin(username, password);
 	 	
 	 	  return res ? "user found" : "user not found";
 	}
	
	@RequestMapping(value = {"/Datavision/ldap-user-login2/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Test_Service2_(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws Exception 
    {	 
 	 	  Common_Utils util = new Common_Utils();
 	 	  
 	 	  JsonObject js = util.StringToJsonObject(MESSAGE);
 	 	  
 	 	  String username = js.get("username").getAsString();
 	 	  String password = js.get("password").getAsString();
 	 	  String usertype = js.get("usertype").getAsString();
 	 	  String Ldtype   = js.get("Ldtype").getAsString();
 	 	  
 	 	  boolean res = false;
 	 	
 	 	  if(Ldtype.equals("1")) 
 	 	  {
 	 		  res = ADLogin_multiDomainuser(username, password, usertype);
 	 	  }
 	 	  else if(Ldtype.equals("2")) 
 	 	  {
 	 		  res = ADLogin_multiDomainuser2(username, password, usertype);
 	 	  }
 	 	  else
 	 	  {
 	 		 res = ADLogin_multiDomainuser3(username, password, usertype);
 	 	  }
 	 	
 	 	  return res ? "user found" : "user not found";
 	}
	
    public JsonObject getADUsers() throws Exception 
    {
    	JsonObject out = new JsonObject();
 	   
    	JsonArray arr = new JsonArray();
    	
 	   try
 	   {
 		   JsonObject js = Get_LDAP_Info("LDAP", "LD001") ;
 		   
 		   JsonObject Headers = js.get("Headers").getAsJsonObject();
 		  
 		    String ldapurl =  js.get("HOST").getAsString();
 		    String ldapbase =  Headers.get("ldapbase").getAsString();
 		    String ldapuserdn =  Headers.get("ldapuserdn").getAsString();
 		    String ldapuserdnpass =  Headers.get("ldapuserdnpass").getAsString();
 		    String KeyStorecertificate =  Headers.get("KeyStorecertificate").getAsString();
 		    String KeyStorepass =  Headers.get("KeyStorepass").getAsString();
 		    String trustStorecertificate =  Headers.get("trustStorecertificate").getAsString();
		    String trustStorepass =  Headers.get("trustStorepass").getAsString();
 		    String keystoreauth =  Headers.get("keystoreauth").getAsString();
 		    String trusttoreauth =  Headers.get("trusttoreauth").getAsString();
 		    String sslreq =  Headers.get("sslreq").getAsString();
 		    String debugmode =  Headers.get("debugmode").getAsString();
 		  
 		    Hashtable<String, String> ldapEnv = new Hashtable<>();
 		    
 	        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 	        ldapEnv.put(Context.PROVIDER_URL, ldapurl);  // Server URL
 	        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 	        ldapEnv.put(Context.SECURITY_PRINCIPAL, ldapuserdn);  // Bind DN
 	        ldapEnv.put(Context.SECURITY_CREDENTIALS, ldapuserdnpass);
 	        
 	       if(sslreq.equals("true")) ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL (because we are using ldaps)
 	        
 	       if(keystoreauth.equals("true"))
 	       {
 	           System.setProperty("javax.net.ssl.keyStore", KeyStorecertificate);
 	           System.setProperty("javax.net.ssl.keyStorePassword", KeyStorepass);
 	       }
 	       
 	       if(trusttoreauth.equals("true"))
	       {
	           System.setProperty("javax.net.ssl.trustStore", trustStorecertificate);
	           System.setProperty("javax.net.ssl.trustStorePassword", trustStorepass);
	       }
 	        
 	      if(debugmode.equals("true")) System.setProperty("javax.net.debug", "ssl,handshake");

 	      try {
 	            // Create the connection
 	            DirContext ctx = new InitialDirContext(ldapEnv);

 	            // Specify the search filter and controls
 	            String searchFilter = "(sAMAccountName=*)";  // Filter to search all users
 	            SearchControls searchControls = new SearchControls();
 	            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);  // Search the entire subtree

 	            // Perform the search on the specified base DN
 	            NamingEnumeration<SearchResult> results = ctx.search(ldapbase, searchFilter, searchControls);

 	            // Iterate through the search results
 	            
 	            while (results.hasMore()) {
 	                SearchResult searchResult = results.next();
 	                logger.debug("Found User: " + searchResult.getNameInNamespace());
 	                
 	               arr.add(searchResult.getNameInNamespace());
 	            }
 	            
 	            // Close the context after done
 	            ctx.close();
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	   }
 	   catch(Exception ex)
 	   {
 		    out.addProperty("Result", ex.getLocalizedMessage());
 	   }
 	   
 	  out.add("users", arr);
 	   	  
 	   return out;
    }
    
    public JsonObject FindUser(String UserId) throws Exception 
    {
    	JsonObject out = new JsonObject();
 	   
    	JsonArray arr = new JsonArray();
    	
 	   try
 	   {
 		   JsonObject js = Get_LDAP_Info("LDAP", "LD001") ;
 		   
 		   JsonObject Headers = js.get("Headers").getAsJsonObject();
 		  
 		    String ldapurl =  js.get("HOST").getAsString();
 		    String ldapbase =  Headers.get("ldapbase").getAsString();
 		    String ldapuserdn =  Headers.get("ldapuserdn").getAsString();
 		    String ldapuserdnpass =  Headers.get("ldapuserdnpass").getAsString();
 		    String KeyStorecertificate =  Headers.get("KeyStorecertificate").getAsString();
 		    String KeyStorepass =  Headers.get("KeyStorepass").getAsString();
 		    String trustStorecertificate =  Headers.get("trustStorecertificate").getAsString();
		    String trustStorepass =  Headers.get("trustStorepass").getAsString();
 		    String keystoreauth =  Headers.get("keystoreauth").getAsString();
 		    String trusttoreauth =  Headers.get("trusttoreauth").getAsString();
 		    String sslreq =  Headers.get("sslreq").getAsString();
 		    String debugmode =  Headers.get("debugmode").getAsString();
 		  
 		    Hashtable<String, String> ldapEnv = new Hashtable<>();
 		    
 	        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 	        ldapEnv.put(Context.PROVIDER_URL, ldapurl);  // Server URL
 	        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 	        ldapEnv.put(Context.SECURITY_PRINCIPAL, ldapuserdn);  // Bind DN
 	        ldapEnv.put(Context.SECURITY_CREDENTIALS, ldapuserdnpass);
 	        
 	       if(sslreq.equals("true")) ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL (because we are using ldaps)
 	        
 	       if(keystoreauth.equals("true"))
 	       {
 	           System.setProperty("javax.net.ssl.keyStore", KeyStorecertificate);
 	           System.setProperty("javax.net.ssl.keyStorePassword", KeyStorepass);
 	       }
 	       
 	       if(trusttoreauth.equals("true"))
	       {
	           System.setProperty("javax.net.ssl.trustStore", trustStorecertificate);
	           System.setProperty("javax.net.ssl.trustStorePassword", trustStorepass);
	       }
 	        
 	      if(debugmode.equals("true")) System.setProperty("javax.net.debug", "ssl,handshake");

 	      try {
 	            // Create the connection
 	            DirContext ctx = new InitialDirContext(ldapEnv);

 	            // Specify the search filter and controls
 	            String searchFilter = "(sAMAccountName="+UserId+")";  // Filter to search all users
 	            
 	            SearchControls searchControls = new SearchControls();
 	            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);  // Search the entire subtree

 	            // Perform the search on the specified base DN
 	            NamingEnumeration<SearchResult> results = ctx.search(ldapbase, searchFilter, searchControls);

 	            // Iterate through the search results
 	            
 	            if(results.hasMore()) 
 	            {
 	                SearchResult searchResult = results.next();
 	                
 	                logger.debug("Found User: " + searchResult.getNameInNamespace());
 	                
 	                out.addProperty("Result", searchResult.getNameInNamespace());
 	            }
 	            else
 	            {
 	            	out.addProperty("Result", "User with ID " + UserId + " not found.");
 	            }
 	            
 	            // Close the context after done
 	            ctx.close();
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	   }
 	   catch(Exception ex)
 	   {
 		   out.addProperty("Result", ex.getLocalizedMessage());
 	   }
 	   
 	 // out.add("users", arr);
 	   	  
 	   return out;
    }
    
    public boolean ADLogin(String username, String password)
    {
    	boolean auth = false;
 	   
 	   try
 	   {
 		    JsonObject js = Get_LDAP_Info("LDAP", "LD001") ;
 		   
 		    JsonObject Headers = js.get("Headers").getAsJsonObject();
 		  
 		    String ldapurl =  js.get("HOST").getAsString();
 		    String ldapbase =  Headers.get("ldapbase").getAsString();
 		    //String ldapuserdn =  Headers.get("ldapuserdn").getAsString();
 		    //String ldapuserdnpass =  Headers.get("ldapuserdnpass").getAsString();
 		    String KeyStorecertificate =  Headers.get("KeyStorecertificate").getAsString();
 		    String KeyStorepass =  Headers.get("KeyStorepass").getAsString();
 		    String trustStorecertificate =  Headers.get("trustStorecertificate").getAsString();
		    String trustStorepass =  Headers.get("trustStorepass").getAsString();
 		    String keystoreauth =  Headers.get("keystoreauth").getAsString();
 		    String trusttoreauth =  Headers.get("trusttoreauth").getAsString();
 		    String sslreq =  Headers.get("sslreq").getAsString();
 		    String debugmode =  Headers.get("debugmode").getAsString();
 		  
 		    String usersearch =  Headers.get("usersearch").getAsString();
 		   
 		    String userDN = "CN=" + username + "," + usersearch;
 		   
 		    Hashtable<String, String> ldapEnv = new Hashtable<>();
 		    
 	        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 	        ldapEnv.put(Context.PROVIDER_URL, ldapurl);  // Server URL
 	        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 	        ldapEnv.put(Context.SECURITY_PRINCIPAL, userDN);  // Bind DN
 	        ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
 	        
 	       if(sslreq.equals("true")) ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL (because we are using ldaps)
 	        
 	       if(keystoreauth.equals("true"))
 	       {
 	           System.setProperty("javax.net.ssl.keyStore", KeyStorecertificate);
 	           System.setProperty("javax.net.ssl.keyStorePassword", KeyStorepass);
 	       }
 	       
 	       if(trusttoreauth.equals("true"))
	       {
	           System.setProperty("javax.net.ssl.trustStore", trustStorecertificate);
	           System.setProperty("javax.net.ssl.trustStorePassword", trustStorepass);
	       }
 	        
 	      if(debugmode.equals("true")) System.setProperty("javax.net.debug", "ssl,handshake");

 	      try 
 	      {
 	            // Create the connection
 	            DirContext ctx = new InitialDirContext(ldapEnv);

 	           // Define search filter and attributes
 	            String searchFilter = "(sAMAccountName=" + username + ")";
 	            SearchControls searchControls = new SearchControls();
 	            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 	            
 	            NamingEnumeration<SearchResult> results = ctx.search(ldapbase, searchFilter, searchControls);

 	            // Close context after search
 	            ctx.close();

 	            // Verify if user is found
 	           auth = results.hasMore();
 	       } 
 	      catch (Exception e) 
 	      {
 	            e.printStackTrace();
 	      }
 	   }
 	   catch(Exception ex)
 	   {
 		   ex.printStackTrace();
 	   }
 	   	  
 	   return auth;
    }
    
    public boolean ADLogin_multiDomainuser(String username, String password, String UserType)  //Not in use
    {
    	boolean auth = false;
 	   
 	   try
 	   {
 		    String[] Zones = new String[] { "LD001-UZ2", "LD001-UZ1", "LD001-UZ3"};
 		    
 		    if(UserType.equalsIgnoreCase("Generic") || UserType.equalsIgnoreCase("Service"))
 		    {
 		    	Zones = new String[] { "LD001-SZ2", "LD001-SZ1", "LD001-SZ3"};
 		    }
 		    
 		    for(String Zone : Zones)
 		    {
 		    	JsonObject js = Get_LDAP_Info("LDAP", Zone) ;
 	 		   
 	 		    JsonObject Headers = js.get("Headers").getAsJsonObject();
 	 		  
 	 		    String ldapurl = js.get("HOST").getAsString();
 	 		    String ldapbase = Headers.get("ldapbase").getAsString();
 	 		    String KeyStorecertificate =  Headers.get("KeyStorecertificate").getAsString();
 	 		    String KeyStorepass =  Headers.get("KeyStorepass").getAsString();
 	 		    String trustStorecertificate =  Headers.get("trustStorecertificate").getAsString();
 			    String trustStorepass =  Headers.get("trustStorepass").getAsString();
 	 		    String keystoreauth =  Headers.get("keystoreauth").getAsString();
 	 		    String trusttoreauth =  Headers.get("trusttoreauth").getAsString();
 	 		    String sslreq =  Headers.get("sslreq").getAsString();
 	 		    String debugmode =  Headers.get("debugmode").getAsString();
 	 		  
 	 		    String usersearch =  Headers.get("usersearch").getAsString();
 	 		   
 	 		    String userDN = "CN=" + username + "," + usersearch;
 	 		   
 	 		    Hashtable<String, String> ldapEnv = new Hashtable<>();
 	 		    
 	 	        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 	 	        ldapEnv.put(Context.PROVIDER_URL, ldapurl);  // Server URL
 	 	        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 	 	        ldapEnv.put(Context.SECURITY_PRINCIPAL, userDN);  // Bind DN
 	 	        ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
 	 	        
 	 	       if(sslreq.equals("true")) ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL (because we are using ldaps)
 	 	        
 	 	       if(keystoreauth.equals("true"))
 	 	       {
 	 	           System.setProperty("javax.net.ssl.keyStore", KeyStorecertificate);
 	 	           System.setProperty("javax.net.ssl.keyStorePassword", KeyStorepass);
 	 	       }
 	 	       
 	 	       if(trusttoreauth.equals("true"))
 		       {
 		           System.setProperty("javax.net.ssl.trustStore", trustStorecertificate);
 		           System.setProperty("javax.net.ssl.trustStorePassword", trustStorepass);
 		       }
 	 	        
 	 	      if(debugmode.equals("true")) System.setProperty("javax.net.debug", "ssl,handshake");

 	 	      try 
 	 	      {
 	 	            // Create the connection
 	 	            DirContext ctx = new InitialDirContext(ldapEnv);

 	 	           // Define search filter and attributes
 	 	            String searchFilter = "(sAMAccountName=" + username + ")";
 	 	            SearchControls searchControls = new SearchControls();
 	 	            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 	 	            
 	 	            NamingEnumeration<SearchResult> results = ctx.search(ldapbase, searchFilter, searchControls);
 	 	            
 	 	            // Close context after search
 	 	            ctx.close();

 	 	            // Verify if user is found
 	 	            auth = results.hasMore();
 	 	           
 	 	            if(auth) break;
 	 	      } 
 	 	      catch (Exception e) 
 	 	      {
 	 	            e.printStackTrace();
 	 	      }
 		   }    
 	   }
 	   catch(Exception ex)
 	   {
 		   ex.printStackTrace();
 	   }
 	   	  
 	   return auth;
    }
    
    public boolean ADLogin_multiDomainuser2(String username, String password, String UserType)  //not in use
    {
    	boolean auth = false;
 	   
 	   try
 	   {
 		    String[] Zones = new String[] { "LD001-UZ2", "LD001-UZ1", "LD001-UZ3"};
 		    
 		    if(UserType.equalsIgnoreCase("Generic") || UserType.equalsIgnoreCase("Service"))
 		    {
 		    	Zones = new String[] { "LD001-SZ2", "LD001-SZ1", "LD001-SZ3"};
 		    }
 		    
 		    for(String Zone : Zones)
 		    {
 		    	JsonObject js = Get_LDAP_Info("LDAP", Zone) ;
 	 		   
 	 		    JsonObject Headers = js.get("Headers").getAsJsonObject();
 	 		  
 	 		    String ldapurl = js.get("HOST").getAsString();
 	 		    String ldapbase = Headers.get("ldapbase").getAsString();
 	 		    String KeyStorecertificate =  Headers.get("KeyStorecertificate").getAsString();
 	 		    String KeyStorepass =  Headers.get("KeyStorepass").getAsString();
 	 		    String trustStorecertificate =  Headers.get("trustStorecertificate").getAsString();
 			    String trustStorepass =  Headers.get("trustStorepass").getAsString();
 	 		    String keystoreauth =  Headers.get("keystoreauth").getAsString();
 	 		    String trusttoreauth =  Headers.get("trusttoreauth").getAsString();
 	 		    String sslreq =  Headers.get("sslreq").getAsString();
 	 		    String debugmode =  Headers.get("debugmode").getAsString();
 	 		  
 	 		    String usersearch =  Headers.get("usersearch").getAsString();
 	 		   
 	 		    String userDN = "CN=" + username + "," + usersearch;
 	 		   
 	 		    Hashtable<String, String> ldapEnv = new Hashtable<>();
 	 		    
 	 	        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 	 	        ldapEnv.put(Context.PROVIDER_URL, ldapurl);  // Server URL
 	 	        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 	 	        ldapEnv.put(Context.SECURITY_PRINCIPAL, userDN);  // Bind DN
 	 	        ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
 	 	        
 	 	       if(sslreq.equals("true")) ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL (because we are using ldaps)
 	 	        
 	 	       if(keystoreauth.equals("true"))
 	 	       {
 	 	           System.setProperty("javax.net.ssl.keyStore", KeyStorecertificate);
 	 	           System.setProperty("javax.net.ssl.keyStorePassword", KeyStorepass);
 	 	       }
 	 	       
 	 	       if(trusttoreauth.equals("true"))
 		       {
 		           System.setProperty("javax.net.ssl.trustStore", trustStorecertificate);
 		           System.setProperty("javax.net.ssl.trustStorePassword", trustStorepass);
 		       }
 	 	        
 	 	      if(debugmode.equals("true")) System.setProperty("javax.net.debug", "ssl,handshake");

 	 	      try 
 	 	      {
 	 	            // Create the connection
 	 	            DirContext ctx = new InitialDirContext(ldapEnv);

 	 	           // Define search filter and attributes
 	 	            String searchFilter = "(sAMAccountName=" + username + ")";
 	 	            SearchControls searchControls = new SearchControls();
 	 	            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 	 	            
 	 	            NamingEnumeration<SearchResult> results = ctx.search(ldapbase, searchFilter, searchControls);
 	 	            
	 	 	          if(results.hasMore()) 
	 	 	          {
	 	                 // If the user is found, we can attempt to bind with the user's credentials
	 	                 SearchResult result = results.next();
	 	                 userDN = result.getNameInNamespace(); // Get the full DN of the found user
	
	 	                 // Now attempt to bind with the user's credentials
	 	                 ldapEnv.put(Context.SECURITY_PRINCIPAL, userDN);
	 	                 ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
	
	 	                 // Create a new context to bind with user credentials
	 	                 DirContext userCtx = new InitialDirContext(ldapEnv);
	 	                 
	 	                 auth = true;
	 	                 
	 	                 logger.debug("Login successful for user: " + username);
	
	 	                 // Close user context
	 	                 userCtx.close();
	 	             } 
	 	 	         else 
	 	 	         {
	 	                 logger.debug("User not found: " + username);
	 	             }

 	 	            // Close context after search
 	 	          ctx.close();
 	 	    } catch (AuthenticationException ae) {
 	 	    	logger.debug("Authentication failed: " + ae.getMessage());
 	        } catch (NamingException e) {
 	        	logger.debug("LDAP error: " + e.getMessage());
 	        }
 		   }    
 	   }
 	   catch(Exception ex)
 	   {
 		   ex.printStackTrace();
 	   }
 	   	  
 	   return auth;
    }
    
    public boolean ADLogin_multiDomainuser3(String username, String password, String UserType) // In use
    {
    	boolean auth = false;
 	   
 	   try
 	   {
 		    String[] Zones = new String[] { "LD001-UZ2", "LD001-UZ1", "LD001-UZ3"};
 		    
 		    for(String Zone : Zones)
 		    {
 		    	JsonObject js = Get_LDAP_Info("LDAP", Zone) ;
 	 		   
 	 		    JsonObject Headers = js.get("Headers").getAsJsonObject();
 	 		  
 	 		    String ldapurl = js.get("HOST").getAsString();
 	 		    String ldapbase = Headers.get("ldapbase").getAsString();
 	 		    String KeyStorecertificate =  Headers.get("KeyStorecertificate").getAsString();
 	 		    String KeyStorepass =  Headers.get("KeyStorepass").getAsString();
 	 		    String trustStorecertificate =  Headers.get("trustStorecertificate").getAsString();
 			    String trustStorepass =  Headers.get("trustStorepass").getAsString();
 	 		    String keystoreauth =  Headers.get("keystoreauth").getAsString();
 	 		    String trusttoreauth =  Headers.get("trusttoreauth").getAsString();
 	 		    String sslreq =  Headers.get("sslreq").getAsString();
 	 		    String debugmode =  Headers.get("debugmode").getAsString();
 	 		    
 	 		    String serviceuser = Headers.has("serviceuser") ? Headers.get("serviceuser").getAsString() : "";
 	 		    String servicepass =  Headers.has("servicepass") ? Headers.get("servicepass").getAsString() : "";
 	 		  
 	 		    //String usersearch =  Headers.get("usersearch").getAsString();
 	 		   
 	 		    String servicesearch =  Headers.has("servicesearch") ? Headers.get("servicesearch").getAsString() : ""; 
 	 		  
 	 		    //String userDN = "CN=" + username + "," + usersearch;
 	 		    
 	 		    String serviceDN = "CN=" + serviceuser + "," + servicesearch;
 	 		   
 	 		    Hashtable<String, String> serviceEnv = new Hashtable<>();
 	 		    
 	 		    serviceEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 	 		  	serviceEnv.put(Context.PROVIDER_URL, ldapurl);     // Server URL
 	 		  	serviceEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 	 		  	serviceEnv.put(Context.SECURITY_PRINCIPAL, serviceDN);  // Bind DN
 	 		  	serviceEnv.put(Context.SECURITY_CREDENTIALS, servicepass);
 	 	        
 	 	        if(sslreq.equals("true")) serviceEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL (because we are using ldaps)
 	 	        
 	 	        if(keystoreauth.equals("true"))
 	 	        {
 	 	            System.setProperty("javax.net.ssl.keyStore", KeyStorecertificate);
 	 	            System.setProperty("javax.net.ssl.keyStorePassword", KeyStorepass);
 	 	        }
 	 	       
 	 	        if(trusttoreauth.equals("true"))
 		        {
 		            System.setProperty("javax.net.ssl.trustStore", trustStorecertificate);
 		            System.setProperty("javax.net.ssl.trustStorePassword", trustStorepass);
 		        }
 	 	        
 	 	        if(debugmode.equals("true")) System.setProperty("javax.net.debug", "ssl,handshake");

 	 	      try 
 	 	      {
 	 	            // Create the connection
 	 	            DirContext serviceContext = new InitialDirContext(serviceEnv);

 	 	           // Define search filter and attributes
 	 	            String searchFilter = "(sAMAccountName=" + username + ")";
 	 	            SearchControls searchControls = new SearchControls();
 	 	            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 	 	            
 	 	            NamingEnumeration<SearchResult> results = serviceContext.search(ldapbase, searchFilter, searchControls);
 	 	            
	 	 	         if(results.hasMore()) 
	 	 	         {
	 	 	        	 SearchResult searchResult = results.next();
		 	             String userDn = searchResult.getNameInNamespace();
		 	             serviceContext.close();
		 	             
		 	             Hashtable<String, String> userEnv = new Hashtable<>(serviceEnv);
		 	             userEnv.put(Context.SECURITY_PRINCIPAL, userDn);
		 	             userEnv.put(Context.SECURITY_CREDENTIALS, password);
	 	 	          
		 	             new InitialDirContext(userEnv);
		 	             
		 	             auth = true;
		 	            
		 	             logger.debug("Login successful for user: " + username);   
	 	             }
	 	 	         else
	 	 	         {
	 	 	        	 logger.debug("user not found on : " + Zone);   
	 	 	         }
	 	 	    } 
	 	 	    catch (AuthenticationException ae) 
	 	 	    {
	 	 	    	logger.debug("Authentication failed: " + ae.getMessage());
	 	        } 
	 	 	    catch (NamingException e) 
	 	 	    {
	 	        	logger.debug("LDAP error: " + e.getMessage());
	 	        }
 		   }    
 	   }
 	   catch(Exception ex)
 	   {
 		   ex.printStackTrace();
 	   }
 	   	  
 	   return auth;
    }
    
    public JsonObject Get_LDAP_Info(String CHCODE, String SERVICECD) 
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
}
