package com.hdsoft.models;

import java.util.Base64;
import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class Test3 {

	public static void main(String[] args)  
	{
		String username = "1361228";
		
		String password = "T21pbnVieXZ0Y0AyNA==";
		
		boolean flag = ADLogin_multiDomainuser3(username, password);
		
		System.out.println("login result >>>> "+flag);
	}
	
	public static boolean ADLogin_multiDomainuser3(String username, String password)
    {
    	boolean auth = false;
 	   
 	   try
 	   {
 		    byte[] decodedBytes = Base64.getDecoder().decode(password);
 		    
 		    password = new String(decodedBytes);
 		    
 		    String ldapurl = "ldaps://UKWATDOM107.zone1.scb.net:636 ldaps://UKARKDOM108.zone1.scb.net:636";
 		    String ldapbase = "OU=Accounts,OU=ITSC,DC=zone1,DC=scb,DC=net";
 		    String KeyStorecertificate =  "/opt/apps/certificates/final-keys/ad-keystore.jks";
 		    String KeyStorepass =  "changeit";
 		    String trustStorecertificate =  "/opt/apps/certificates/final-keys/TrustStore.jks";
		    String trustStorepass =  "changeit";
 		    String keystoreauth =  "true";
 		    String trusttoreauth =  "false";
 		    String sslreq =  "true";
 		    String debugmode =  "true";
 		    
 		    String serviceuser = "svc.DVSN.ADINT.001";
 		    String servicepass =  "1lZcnWCYZZz0yPo9FImcx*je_";
 		  
 		    String servicesearch =  "OU=Service,OU=Accounts,OU=ITSC,DC=zone2,DC=scb,DC=net"; 
 		  
 		    String serviceDN = "CN=" + serviceuser + "," + servicesearch;
 		   
 		    Hashtable<String, String> serviceEnv = new Hashtable<>();
 		    
 		    serviceEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		  	serviceEnv.put(Context.PROVIDER_URL, ldapurl);     
 		  	serviceEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 		  	serviceEnv.put(Context.SECURITY_PRINCIPAL, serviceDN);  
 		  	serviceEnv.put(Context.SECURITY_CREDENTIALS, servicepass);
 	        
 	        if(sslreq.equals("true")) serviceEnv.put(Context.SECURITY_PROTOCOL, "ssl"); 
 	        
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
	 	            
	 	             System.out.println("Login successful for user: " + username);   
 	             }
 	 	         else
 	 	         {
 	 	        	System.out.println("user not found on : Zone 1");   
 	 	         }
 	 	    } 
 	 	    catch (AuthenticationException ae) 
 	 	    {
 	 	    	System.out.println("Authentication failed: " + ae.getMessage());
 	        } 
 	 	    catch (NamingException e) 
 	 	    {
 	 	    	System.out.println("LDAP error: " + e.getMessage());
 	        }   
 	   }
 	   catch(Exception ex)
 	   {
 		  System.out.println(ex.getLocalizedMessage()); 
 	   }
 	   	  
 	   return auth;
    }
	
	public static boolean ADLogin_multiDomainuser_UAT(String username, String password)
    {
    	boolean auth = false;
 	   
 	   try
 	   {
 		    byte[] decodedBytes = Base64.getDecoder().decode(password);
 		    
 		    password = new String(decodedBytes);
 		    
 		    String ldapurl = "ldaps://UKWATDOM107.zone1.scb.net:636 ldaps://UKARKDOM108.zone1.scb.net:636";
 		    String ldapbase = "OU=Accounts,OU=ITSC,DC=zone1,DC=scb,DC=net";
 		    String KeyStorecertificate =  "/opt/apps/certificates/final-keys/ad-keystore.jks";
 		    String KeyStorepass =  "changeit";
 		    String trustStorecertificate =  "/opt/apps/certificates/final-keys/TrustStore.jks";
		    String trustStorepass =  "changeit";
 		    String keystoreauth =  "true";
 		    String trusttoreauth =  "false";
 		    String sslreq =  "true";
 		    String debugmode =  "true";
 		    
 		    String serviceuser = "svc.DVSN.ADINT.001";
 		    String servicepass =  "1lZcnWCYZZz0yPo9FImcx*je_";
 		  
 		    String servicesearch =  "OU=Service,OU=Accounts,OU=ITSC,DC=zone2,DC=scb,DC=net"; 
 		  
 		    String serviceDN = "CN=" + serviceuser + "," + servicesearch;
 		   
 		    Hashtable<String, String> serviceEnv = new Hashtable<>();
 		    
 		    serviceEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		  	serviceEnv.put(Context.PROVIDER_URL, ldapurl);     
 		  	serviceEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
 		  	serviceEnv.put(Context.SECURITY_PRINCIPAL, serviceDN);  
 		  	serviceEnv.put(Context.SECURITY_CREDENTIALS, servicepass);
 	        
 	        if(sslreq.equals("true")) serviceEnv.put(Context.SECURITY_PROTOCOL, "ssl"); 
 	        
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
	 	            
	 	             System.out.println("Login successful for user: " + username);   
 	             }
 	 	         else
 	 	         {
 	 	        	System.out.println("user not found on : Zone 1");   
 	 	         }
 	 	    } 
 	 	    catch (AuthenticationException ae) 
 	 	    {
 	 	    	System.out.println("Authentication failed: " + ae.getMessage());
 	        } 
 	 	    catch (NamingException e) 
 	 	    {
 	 	    	System.out.println("LDAP error: " + e.getMessage());
 	        }   
 	   }
 	   catch(Exception ex)
 	   {
 		  System.out.println(ex.getLocalizedMessage()); 
 	   }
 	   	  
 	   return auth;
    }
}
