package com.hdsoft.models;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.logging.log4j.*;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.OkHttpClient.Builder;

@Component
public class Webservice_call_Modal 
{
	private static final Logger logger = LogManager.getLogger(Webservice_call_Modal.class);

	public JsonObject Okhttp_Send_Rest_Request(JsonObject Api_details)
    {
    	JsonObject details = new JsonObject();
    	
    	try
    	{
    		String URL = Api_details.get("URI").getAsString();
			String Method = Api_details.get("METHOD").getAsString();
			String URLParameters = Api_details.get("PAYLOAD").getAsString();

    		JsonArray Headers = Api_details.has("Headers") ? Api_details.get("Headers").getAsJsonArray() : new JsonArray();
    		JsonArray Params = Api_details.has("Form_Data") ? Api_details.get("Form_Data").getAsJsonArray() : new JsonArray();
			
			Headers.Builder header_builder = new Headers.Builder();
			
			for(int i=0;i<Headers.size();i++)
			{
				JsonObject header = Headers.get(i).getAsJsonObject();
				
				String key = header.get("Key").getAsString();
				String value = header.get("Value").getAsString();
				
				header_builder.add(key, value);
			}
			
			header_builder.add("Content-Length", "" + Integer.toString(URLParameters.getBytes().length));
			
			Headers Request_Headers = header_builder.build();
			
    		RequestBody Req_Body = RequestBody.create(new byte[0]);  
    		
    		if(!URLParameters.equals(""))
    		{
    			Common_Utils util = new Common_Utils();
    			
    			if(Api_details.has("Form_Data")) 
    			{
					String uParam = "";
					
					for(int i = 0; i < Params.size() ; i++) 
					{
    					JsonObject Param = Params.get(i).getAsJsonObject();
    					
    					String key = Param.get("Key").getAsString();
    					String value = Param.get("Value").getAsString();
    					
    					uParam = key+"="+value+"&"+uParam;
					}
					
					uParam += "~";
					
					uParam = uParam.replace("&~", "");
					
					MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded"); 
    				
					Req_Body = RequestBody.create(uParam, mediaType);
    			}
    			else if(util.isJSONValid(URLParameters))  
    			{
    				MediaType JSON = MediaType.parse("application/json"); 
    				
    				Req_Body = RequestBody.create(URLParameters, JSON);	
    			}	
    		}

    		logger.debug("Request details :::: "+Api_details.toString());
    		
    		if(Method.equals("GET"))
    		{
    			details = doGetRequest(URL, Method, Request_Headers);
    		}
    		else if(Method.equals("POST"))
    		{
    			details = doPostRequest(URL, Method, Request_Headers, Req_Body, URLParameters);
    		}
    		else if(Method.equals("PUT"))
    		{
    			details = doPutRequest(URL, Method, Request_Headers, Req_Body);
    		}
    		else 
    		{
    			details = doDeleteRequest(URL, Method, Request_Headers, Req_Body);
    		}
    	}
    	catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request :::: "+e.getLocalizedMessage());
    	}
    	
    	return details;
    }
	
	public JsonObject doGetRequest(String URL, String Method, Headers Request_Headers)
	{
		JsonObject details = new JsonObject();
		
		try
		{
			OkHttpClient client = new OkHttpClient().newBuilder()
					.connectTimeout(15, TimeUnit.MINUTES)
					.writeTimeout(15, TimeUnit.MINUTES)
					.readTimeout(15, TimeUnit.MINUTES)
					.callTimeout(15, TimeUnit.MINUTES)
					.build();
    		
			if(URL.contains("https"))
			{
				client = trustAllSslClient(client);
			}
    		
    		Request request = new Request.Builder()
  				  .url(URL)
  				  .headers(Request_Headers)
  				  .build();
    		
    		Response response = client.newCall(request).execute();
    		
    		int Response_Code = response.code();
    		
    		logger.debug("Response_Code :::: "+Response_Code);
    		
    		String Response = response.body().string();
    		
    		logger.debug("Response :::: "+Response);
    		
    		details.addProperty("Request URL", URL);
			details.addProperty("Method", Method);
			details.addProperty("Parameters", "");
			details.addProperty("Response_Code", Response_Code);
			details.addProperty("Response", Response);
			details.addProperty("Result", response.isSuccessful() ? "Success" : "Failed");
			details.addProperty("Message", response.message());
    	}	
		catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Response", "");
    		details.addProperty("Response_Code", 500);
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request :::: "+e.getLocalizedMessage());
    	}
		
		return details;
	}
	
	public JsonObject doPostRequest(String URL, String Method, Headers Request_Headers, RequestBody Req_Body, String URLParameters)
	{
		JsonObject details = new JsonObject();
		
		try
    	{
			OkHttpClient client = new OkHttpClient().newBuilder()
					.connectTimeout(15, TimeUnit.MINUTES)
					.writeTimeout(15, TimeUnit.MINUTES)
					.readTimeout(15, TimeUnit.MINUTES)
					.callTimeout(15, TimeUnit.MINUTES)
					.build();
    		
			try
			{
				if(Request_Headers.get("content-type").contains("application/json") || Request_Headers.get("Content-Type").contains("application/json"))
				{
					client = new OkHttpClient().newBuilder().addNetworkInterceptor(new FixContentTypeInterceptor()).build();
				}
			}
			catch(Exception ex) {} 
		
			if(URL.contains("https"))
			{
				client = trustAllSslClient(client);
			}
			
    		Request request = new Request.Builder()
  				  .url(URL)
  				  .post(Req_Body)
  				  .headers(Request_Headers)
  				  .build();
    	
    		Response response = client.newCall(request).execute();
    		
    		int Response_Code = response.code();
    		
    		logger.debug("Response_Code :::: "+Response_Code);
    		
    		String Response = response.body().string();
    		
    		logger.debug("Response :::: "+Response);
    			
    		details.addProperty("Request URL", URL);
			details.addProperty("Method", Method);
			details.addProperty("Parameters", "");
			details.addProperty("Response_Code", Response_Code);
			details.addProperty("Response", Response);
			details.addProperty("Result", response.isSuccessful() ? "Success" : "Failed");
			details.addProperty("Message", response.message());
    	}	
		catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Response", "");
    		details.addProperty("Response_Code", 500);
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request :::: "+details);
    	}
		
		return details;
	}
	
	public JsonObject doPutRequest(String URL, String Method, Headers Request_Headers, RequestBody Req_Body)
	{
		JsonObject details = new JsonObject();
		
		try
    	{
			OkHttpClient client = new OkHttpClient().newBuilder()
					.connectTimeout(15, TimeUnit.MINUTES)
					.writeTimeout(15, TimeUnit.MINUTES)
					.readTimeout(15, TimeUnit.MINUTES)
					.callTimeout(15, TimeUnit.MINUTES)
					.build();
    		
			try
			{
				if(Request_Headers.get("content-type").contains("application/json") || Request_Headers.get("Content-Type").contains("application/json"))
				{
					client = new OkHttpClient().newBuilder().addNetworkInterceptor(new FixContentTypeInterceptor()).build();
				}
			}
			catch(Exception ex) {} 
			
			if(URL.contains("https"))
			{
				client = trustAllSslClient(client);
			}
    		
    		Request request = new Request.Builder()
  				  .url(URL)
  				  .headers(Request_Headers)
  				  .put(Req_Body)
  				  .build();
    		
    		Response response = client.newCall(request).execute();
    		
    		int Response_Code = response.code();
    		
    		logger.debug("Response_Code :::: "+Response_Code);
    		
    		String Response = response.body().string();
    		
    		logger.debug("Response :::: "+Response);
    			
    		details.addProperty("Request URL", URL);
			details.addProperty("Method", Method);
			details.addProperty("Parameters", "");
			details.addProperty("Response_Code", Response_Code);
			details.addProperty("Response", Response);
			details.addProperty("Result", response.isSuccessful() ? "Success" : "Failed");
			details.addProperty("Message", response.message());
    	}	
		catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Response", "");
    		details.addProperty("Response_Code", 500);
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request :::: "+details);
    	}
		
		return details;
	}
	
	public JsonObject doDeleteRequest(String URL, String Method, Headers Request_Headers, RequestBody Req_Body)
	{
		JsonObject details = new JsonObject();
		
		try
    	{
			OkHttpClient client = new OkHttpClient().newBuilder()
					.connectTimeout(15, TimeUnit.MINUTES)
					.writeTimeout(15, TimeUnit.MINUTES)
					.readTimeout(15, TimeUnit.MINUTES)
					.callTimeout(15, TimeUnit.MINUTES)
					.build();
    		
			try
			{
				if(Request_Headers.get("content-type").contains("application/json") || Request_Headers.get("Content-Type").contains("application/json"))
				{
					client = new OkHttpClient().newBuilder().addNetworkInterceptor(new FixContentTypeInterceptor()).build();
				}
			}
			catch(Exception ex) {} 
			
			if(URL.contains("https"))
			{
				client = trustAllSslClient(client);
			}
    		
    		Request request = new Request.Builder()
  				  .url(URL)
  				  .headers(Request_Headers)
  				  .delete(Req_Body)
  				  .build();
    		
    		Response response = client.newCall(request).execute();
    		
    		int Response_Code = response.code();
    		
    		logger.debug("Response_Code :::: "+Response_Code);
    		
    		String Response = response.body().string();
    		
    		logger.debug("Response :::: "+Response);
    			
    		details.addProperty("Request URL", URL);
			details.addProperty("Method", Method);
			details.addProperty("Parameters", "");
			details.addProperty("Response_Code", Response_Code);
			details.addProperty("Response", Response);
			details.addProperty("Result", response.isSuccessful() ? "Success" : "Failed");
			details.addProperty("Message", response.message());
    	}	
		catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Response", "");
    		details.addProperty("Response_Code", 500);
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request :::: "+details);
    	}
		
		return details;
	}
	
	public JsonObject Send_Okhttp_Request(JsonObject Api_details)
	{
		JsonObject details = new JsonObject();
		
		try
    	{
			String URL = Api_details.get("URI").getAsString();
			String Method = Api_details.get("METHOD").getAsString();
			String URLParameters = Api_details.get("PAYLOAD").getAsString();

    		JsonArray Headers = Api_details.has("Headers") ? Api_details.get("Headers").getAsJsonArray() : new JsonArray();
    		JsonArray Params = Api_details.has("Form_Data") ? Api_details.get("Form_Data").getAsJsonArray() : new JsonArray();
    		
    		Headers.Builder header_builder = new Headers.Builder();
			
			for(int i=0;i<Headers.size();i++)
			{
				JsonObject header = Headers.get(i).getAsJsonObject();
				
				String key = header.get("Key").getAsString();
				String value = header.get("Value").getAsString();
				
				header_builder.add(key, value);
			}
			
			Headers Request_Headers = header_builder.build();
			
    		RequestBody Req_Body = RequestBody.create(new byte[0], null); 
    		
    		Common_Utils util = new Common_Utils();
    		
    		if(!util.isNullOrEmpty(URLParameters))
    		{
    			if(Api_details.has("Form_Data")) 
    			{
    				if(Params.size() !=0 && URL.contains("tips") && URL.contains("token"))
    				{
    					JsonObject Param = Params.get(0).getAsJsonObject();
    					
    					String key = Param.get("Key").getAsString();
    					String value = Param.get("Value").getAsString();
    					
    					MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded"); 
        				
    					Req_Body = RequestBody.create(key+"="+value, mediaType);
    				}
    			}
    			else if(util.isJSONValid(URLParameters))  
    			{
    				MediaType JSON = MediaType.parse("application/json"); 
    				
    				Req_Body = RequestBody.create(new Gson().toJson(URLParameters), JSON);
    			}
    			
    		}
			
    		OkHttpClient client = new OkHttpClient().newBuilder()
					.connectTimeout(2, TimeUnit.MINUTES)
					.writeTimeout(2, TimeUnit.MINUTES)
					.readTimeout(2, TimeUnit.MINUTES)
					.build();
			
			if(Request_Headers.get("content-type").equalsIgnoreCase("application/json") || Request_Headers.get("Content-Type").equalsIgnoreCase("application/json"))
			{
				client = new OkHttpClient().newBuilder().addNetworkInterceptor(new FixContentTypeInterceptor()).build();
			}
			
			if(URL.contains("https"))
			{
				client = trustAllSslClient(client);
			}
			
    		Request request = new Request.Builder()
  				  .url(URL)
  				  .method(Method, Req_Body)
  				  .headers(Request_Headers)
  				  .build();
    	
    		Response response = client.newCall(request).execute();
    		
    		String Response = response.body().string();
    		
    		int Response_Code = response.code();
    		
    		logger.debug("Response :::: "+Response);

    		details.addProperty("Request URL", URL);
			details.addProperty("Method", Method);
			details.addProperty("Parameters", "");
			details.addProperty("Response_Code", Response_Code);
			details.addProperty("Response", Response);
			details.addProperty("Result", response.isSuccessful() ? "Success" : "Failed");
			details.addProperty("Message", response.message());
    	}	
		catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Response", "");
    		details.addProperty("Response_Code", 500);
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request :::: "+e.getLocalizedMessage());
    	}
		
		return details;
	}
	
	public JsonObject Send_https_Request(JsonObject Api_details)
	{
		JsonObject details = new JsonObject();
		
		try
    	{
			String URL = Api_details.get("URI").getAsString();
			String Method = Api_details.get("METHOD").getAsString();
			String URLParameters = Api_details.get("PAYLOAD").getAsString();

    		JsonArray Headers = Api_details.has("Headers") ? Api_details.get("Headers").getAsJsonArray() : new JsonArray();
    		 
    		logger.debug("Request details :::: "+Api_details.toString());
    		
    		int Response_Code = 0; String Response = "";
    		
    		if(URL.contains("https"))
    		{
    			URL httpsURL  = new URL(URL);
        		
    			TrustManager[] trustAllCerts = new TrustManager[] {
    					
    		       new X509TrustManager() {
    		    	   
    		        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }

    				public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {}

    				public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {}

    		       }
    		    };
    			
    			SSLContext sc = SSLContext.getInstance("SSL");
    		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    		  
    		    HostnameVerifier allHostsValid = new HostnameVerifier() {
    		        public boolean verify(String hostname, SSLSession session) {
    		          return true;
    		        }
    		    };
    		     
    		    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    		 
    		    HttpsURLConnection con = (HttpsURLConnection) httpsURL.openConnection();
    		    
    			con.setRequestMethod(Method);
    			
    			for(int i=0;i<Headers.size();i++)
    			{
    				JsonObject header = Headers.get(i).getAsJsonObject();
    				
    				String key = header.get("Key").getAsString();
    				String value = header.get("Value").getAsString();
    				
    				con.setRequestProperty(key, value);
    			}
    			
    			con.setRequestProperty("Content-Length", "" + Integer.toString(URLParameters.getBytes().length));
    		    con.setRequestProperty("Content-Language", "en-US");  
    			
    		    
    			con.setDoOutput(true);
    			
    		    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    		    
    		    byte[] input = URLParameters.getBytes("utf-8");

    		    wr.write(input, 0, input.length);			
    		    wr.flush();
    		    wr.close();
    		    
    		    Response_Code = con.getResponseCode();
    		    
    		    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    		    
    		    String inputLine;
    		    
    		    StringBuffer response = new StringBuffer();

    		    while ((inputLine = in.readLine()) != null) 
    		    {
    		        response.append(inputLine);
    		    }
    		    
    		    in.close();
    		    
    		    Response = response.toString();
    				 
        		logger.debug("Response :::: "+Response);
    		}
    		else
    		{
    			URL httpURL  = new URL(URL);
        		
    		    HttpURLConnection con = (HttpURLConnection) httpURL.openConnection();
    		    
    			con.setRequestMethod(Method);
    			
    			for(int i=0;i<Headers.size();i++)
    			{
    				JsonObject header = Headers.get(i).getAsJsonObject();
    				
    				String key = header.get("Key").getAsString();
    				String value = header.get("Value").getAsString();
    				
    				con.setRequestProperty(key, value);
    			}
    			
    			con.setRequestProperty("Content-Length", "" + Integer.toString(URLParameters.getBytes().length));
    		    con.setRequestProperty("Content-Language", "en-US");  
    			
    		    con.setUseCaches(false);
    		    con.setDoInput(true);
    			con.setDoOutput(true);
    			
    		    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    		    
    		    byte[] input = URLParameters.getBytes("utf-8");

    		    wr.write(input, 0, input.length);			
    		    wr.flush();
    		    wr.close();
    		    
    		    Response_Code = con.getResponseCode();
    		    
    		    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    		    
    		    String inputLine;
    		    
    		    StringBuffer response = new StringBuffer();

    		    while ((inputLine = in.readLine()) != null) 
    		    {
    		        response.append(inputLine);
    		    }
    		    
    		    in.close();
    		    
    		    Response = response.toString();
    				 
        		logger.debug("Response :::: "+Response);
    		}
    		
    		details.addProperty("Request URL", URL);
			details.addProperty("Method", Method);
			details.addProperty("Parameters", "");
			details.addProperty("Response_Code", Response_Code);
			details.addProperty("Response", Response);
			details.addProperty("Result", "Success");
			details.addProperty("Message", "Success");
    	}	
		catch(Exception e)
    	{
    		details.addProperty("Result", "Failed");
    		details.addProperty("Response", "");
    		details.addProperty("Response_Code", 500);
    		details.addProperty("Message", e.getLocalizedMessage());
    		
    		logger.debug("Exception in Send_Rest_request2 :::: "+e.getLocalizedMessage());
    	}
		
		return details;
	}
	
	public final class FixContentTypeInterceptor implements Interceptor 
	{
	    public Response intercept(Interceptor.Chain chain) throws IOException 
	    {
	        Request originalRequest = chain.request();

	        Request fixedRequest = originalRequest.newBuilder()
	                .header("Content-Type", "application/json")
	                .build();
	        
	        return chain.proceed(fixedRequest);
	    }
	}
	
	public static OkHttpClient trustAllSslClient(OkHttpClient client) 
	{
        Builder builder = client.newBuilder();
        builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager)trustAllCerts[0]);
        builder.hostnameVerifier(new HostnameVerifier() 
        {
        	public boolean verify(String hostname, SSLSession session) {	return true;  }
        });
        
        return builder.build();
	}
	    
    private static final TrustManager[] trustAllCerts = new TrustManager[] 
    {
    	    new X509TrustManager() {
    	      
    	        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException { }
  
    	        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException { }

    	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  return new java.security.cert.X509Certificate[]{}; }
    	    }
    };
   
    private static final SSLContext trustAllSslContext;
    	
    static 
    {
	    try 
	    {
	        trustAllSslContext = SSLContext.getInstance("SSL");
	        trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	    } 
	    catch (Exception e) 
	    {
	        throw new RuntimeException(e);
	    }
    }
	    
    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();
}
