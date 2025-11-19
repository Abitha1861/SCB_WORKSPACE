package com.hdsoft.models;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.google.gson.JsonObject;
import com.hdsoft.common.File_handling;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.security.*;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.credentials.Provider;
import io.minio.credentials.CertificateIdentityProvider;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@Component
public class MinIo_Modal 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public MinIo_Modal(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public MinIo_Modal() { }
	
	private static final Logger logger = LogManager.getLogger(MinIo_Modal.class);
	
	public JsonObject Upload_File(String Path) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 File_handling fp = new File_handling();
			
			 String SERVICECD = "MIN001";
			
			 Webservice_Modal Ws = new Webservice_Modal(Jdbctemplate);
			 
			 JsonObject js = Ws.Get_MinIo_Info(SERVICECD);
			 
			 JsonObject Headers = js.get("Headers").getAsJsonObject();
			 
		 	 String stsEndpoint = js.get("URI").getAsString().trim();
			 String bucketName = Headers.has("bucketName") ? Headers.get("bucketName").getAsString().trim() : "";
			 String objectName = fp.GetFileName(Path);
			 String fileName = Path;
			 String passwd = Headers.has("passKey") ? Headers.get("passKey").getAsString().trim() : "";  
			 
			 logger.debug("bucketName : "+bucketName);   
			 logger.debug("objectName : "+objectName);   
			 logger.debug("fileName : "+fileName);   
			 
			 String KeyStoreCert = Headers.has("KeyStore") ? Headers.get("KeyStore").getAsString().trim() : "";  
			 String TrustStoreCert = Headers.has("TrustStore") ? Headers.get("TrustStore").getAsString().trim() : "";  
			 
			 String TrustStorePass = Headers.has("TrustStorePass") ? Headers.get("TrustStorePass").getAsString().trim() : "";  
			 
		     KeyStore ks=KeyStore.getInstance("JKS");
		     ks.load(new FileInputStream(KeyStoreCert), passwd.toCharArray());
		   
		     FileInputStream myKeys = new FileInputStream(TrustStoreCert);
		     KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		     trustStore.load(myKeys, TrustStorePass.toCharArray());
		    
		     TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		     trustManagerFactory.init(trustStore);
		     TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		    
		     KeyManagerFactory keyManagerFactory =  KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		     keyManagerFactory.init(ks, passwd.toCharArray());
		     KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
		    
		     SSLContext sslContext = SSLContext.getInstance("TLS");
		     sslContext.init(keyManagers, trustManagers, null);
		    
		     SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		     
		     X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
		    
		     Provider provider = new CertificateIdentityProvider(stsEndpoint, sslSocketFactory, trustManager, null, null);

		     MinioClient minioClient =  MinioClient.builder()
						                 .endpoint(stsEndpoint)
						                 .credentialsProvider(provider)
						                 .build();
	         
	         FileInputStream inputStream = readContentIntoByteArray(fileName);
	         
	         File file = new File(fileName);
	         
	         long Filebytes = file.length();
	         
	         long time1 = System.currentTimeMillis();
	         
	         minioClient.putObject(
	         	       PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
	         	    		   inputStream, Filebytes, -1)
	         	           .build());
	         	   
	         inputStream.close();
	         
	         long time2 = System.currentTimeMillis();
	         
	         logger.debug("file is successfully uploaded to minIO, Time Taken : "+(((double)(time2-time1))/1000));    
	         
	         details.addProperty("Time_Taken", (((double)(time2-time1))/1000));
	 
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "file is successfully uploaded to minIO");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Upload_File :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Download_File(String Path) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 File_handling fp = new File_handling();
			
			 String SERVICECD = "MIN002";
			
			 Webservice_Modal Ws = new Webservice_Modal(Jdbctemplate);
			 
			 JsonObject js = Ws.Get_MinIo_Info(SERVICECD);
			 
			 JsonObject Headers = js.get("Headers").getAsJsonObject();
			 
		 	 String stsEndpoint = js.get("URI").getAsString().trim();
			 String bucketName = Headers.has("bucketName") ? Headers.get("bucketName").getAsString().trim() : "";
			 String objectName = fp.GetFileName(Path);
			 String fileName = Path;
			 String passwd = Headers.has("passKey") ? Headers.get("passKey").getAsString().trim() : "";  
			
			 logger.debug("bucketName : "+bucketName);   
			 logger.debug("objectName : "+objectName);   
			 logger.debug("fileName : "+fileName);   
			 
			 String KeyStoreCert = Headers.has("KeyStore") ? Headers.get("KeyStore").getAsString().trim() : "";  
			 String TrustStoreCert = Headers.has("TrustStore") ? Headers.get("TrustStore").getAsString().trim() : "";  
			 
			 String TrustStorePass = Headers.has("TrustStorePass") ? Headers.get("TrustStorePass").getAsString().trim() : "";  
			 
		     KeyStore ks=KeyStore.getInstance("JKS");
		     ks.load(new FileInputStream(KeyStoreCert), passwd.toCharArray());
		   
		     FileInputStream myKeys = new FileInputStream(TrustStoreCert);
		     KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		     trustStore.load(myKeys, TrustStorePass.toCharArray());
		    
		     TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		     trustManagerFactory.init(trustStore);
		     TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		    
		     KeyManagerFactory keyManagerFactory =  KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		     keyManagerFactory.init(ks, passwd.toCharArray());
		     KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
		    
		     SSLContext sslContext = SSLContext.getInstance("TLS");
		     sslContext.init(keyManagers, trustManagers, null);
		    
		     SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		     
		     X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
		    
		     Provider provider = new CertificateIdentityProvider(stsEndpoint, sslSocketFactory, trustManager, null, null);

		     MinioClient minioClient = MinioClient.builder()
			                .endpoint(stsEndpoint)
			                .credentialsProvider(provider)
			                .build();
			        
	         long time1 = System.currentTimeMillis();
	        
	         minioClient.downloadObject(
	                DownloadObjectArgs.builder()
	                    .bucket(bucketName)
	                    .object(objectName)
	                    .filename(fileName)
	                    .build());
	        
			 long time2 = System.currentTimeMillis();
			
	         logger.debug("file is successfully downloaded from minIO, Time Taken : "+(((double)(time2-time1))/1000));    
	         
	         details.addProperty("Time_Taken", (((double)(time2-time1))/1000));
	 
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "file is successfully uploaded to minIO");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Download_File :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Download_File(String FilePath, String FileName) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String SERVICECD = "MIN002";
			
			 Webservice_Modal Ws = new Webservice_Modal(Jdbctemplate);
			 
			 JsonObject js = Ws.Get_MinIo_Info(SERVICECD);
			 
			 JsonObject Headers = js.get("Headers").getAsJsonObject();
			 
		 	 String stsEndpoint = js.get("URI").getAsString().trim();
			 String bucketName = Headers.has("bucketName") ? Headers.get("bucketName").getAsString().trim() : "";
			 String objectName = FileName.substring(FileName.indexOf(bucketName)).replace(bucketName, ""); 
			 String fileName = FilePath + FilenameUtils.getName(FileName);
			 String passwd = Headers.has("passKey") ? Headers.get("passKey").getAsString().trim() : "";  
			 
			 if(FileName.contains("edmpidsifs"))
			 {
				 objectName = FileName;
			 }
			 
			 logger.debug("bucketName : "+bucketName);   
			 logger.debug("objectName : "+objectName);   
			 logger.debug("fileName : "+fileName);   
			 
			 String KeyStoreCert = Headers.has("KeyStore") ? Headers.get("KeyStore").getAsString().trim() : "";  
			 String TrustStoreCert = Headers.has("TrustStore") ? Headers.get("TrustStore").getAsString().trim() : "";  
			 
			 String TrustStorePass = Headers.has("TrustStorePass") ? Headers.get("TrustStorePass").getAsString().trim() : "";  
			 
		     KeyStore ks=KeyStore.getInstance("JKS");
		     ks.load(new FileInputStream(KeyStoreCert), passwd.toCharArray());
		   
		     FileInputStream myKeys = new FileInputStream(TrustStoreCert);
		     KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		     trustStore.load(myKeys, TrustStorePass.toCharArray());
		    
		     TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		     trustManagerFactory.init(trustStore);
		     TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		    
		     KeyManagerFactory keyManagerFactory =  KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		     keyManagerFactory.init(ks, passwd.toCharArray());
		     KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
		    
		     SSLContext sslContext = SSLContext.getInstance("TLS");
		     sslContext.init(keyManagers, trustManagers, null);
		    
		     SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		     
		     X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
		    
		     Provider provider = new CertificateIdentityProvider(stsEndpoint, sslSocketFactory, trustManager, null, null);

		     MinioClient minioClient = MinioClient.builder()
			                .endpoint(stsEndpoint)
			                .credentialsProvider(provider)
			                .build();
			        
	         long time1 = System.currentTimeMillis();
	        
	         minioClient.downloadObject(
	                DownloadObjectArgs.builder()
	                    .bucket(bucketName)
	                    .object(objectName)
	                    .filename(fileName)
	                    .build());
	        
			 long time2 = System.currentTimeMillis();
		
			 Double time_taken = (double) ((time2-time1)/1000);
			 
	         logger.debug(">>> file is successfully downloaded from minIO <<<");
	         
	         logger.debug("file Name  :: "+FileName); 
	         logger.debug("downloaded path  :: "+fileName); 
	         logger.debug("Time Taken :: "+time_taken);
	         
	         details.addProperty("Time_Taken", time_taken);
	 
	         details.addProperty("FilePath", fileName);
	         
	         Delete_File(objectName); 
	         
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "200");
			 details.addProperty("message", "file is successfully downloaded from minIO");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "500");
			 details.addProperty("message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Download_File from MinIO :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	public JsonObject Delete_File(String objectName) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String SERVICECD = "MIN002";
			
			 Webservice_Modal Ws = new Webservice_Modal(Jdbctemplate);
			 
			 JsonObject js = Ws.Get_MinIo_Info(SERVICECD);
			 
			 JsonObject Headers = js.get("Headers").getAsJsonObject();
			 
		 	 String stsEndpoint = js.get("URI").getAsString().trim();
			 String bucketName = Headers.has("bucketName") ? Headers.get("bucketName").getAsString().trim() : "";
			 //String objectName = FileName.substring(FileName.indexOf(bucketName)).replace(bucketName, ""); 
			 //String fileName = FilePath + FilenameUtils.getName(FileName);
			 String passwd = Headers.has("passKey") ? Headers.get("passKey").getAsString().trim() : "";  
		 
			 logger.debug(">>>> Deleting the file : <<<<<<");   
			 
			 logger.debug("bucketName : "+bucketName);   
			 logger.debug("objectName : "+objectName);   
			// logger.debug("fileName : "+fileName);   
			 
			 String KeyStoreCert = Headers.has("KeyStore") ? Headers.get("KeyStore").getAsString().trim() : "";  
			 String TrustStoreCert = Headers.has("TrustStore") ? Headers.get("TrustStore").getAsString().trim() : "";  
			 
			 String TrustStorePass = Headers.has("TrustStorePass") ? Headers.get("TrustStorePass").getAsString().trim() : "";  
			 
		     KeyStore ks=KeyStore.getInstance("JKS");
		     ks.load(new FileInputStream(KeyStoreCert), passwd.toCharArray());
		   
		     FileInputStream myKeys = new FileInputStream(TrustStoreCert);
		     KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		     trustStore.load(myKeys, TrustStorePass.toCharArray());
		    
		     TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		     trustManagerFactory.init(trustStore);
		     TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		    
		     KeyManagerFactory keyManagerFactory =  KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		     keyManagerFactory.init(ks, passwd.toCharArray());
		     KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
		    
		     SSLContext sslContext = SSLContext.getInstance("TLS");
		     sslContext.init(keyManagers, trustManagers, null);
		    
		     SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		     
		     X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
		    
		     Provider provider = new CertificateIdentityProvider(stsEndpoint, sslSocketFactory, trustManager, null, null);

		     MinioClient minioClient = MinioClient.builder()
			                .endpoint(stsEndpoint)
			                .credentialsProvider(provider)
			                .build();
			        
	         long time1 = System.currentTimeMillis();
	  
	         minioClient.removeObject(
	                    RemoveObjectArgs.builder()
	                    .bucket(bucketName)
	                    .object(objectName)
	                    .build()
	            );
	        
			 long time2 = System.currentTimeMillis();
		
	         logger.debug(">>> file is successfully deleted from minIO <<<");
	         
	         logger.debug("Object path  :: "+objectName); 
	         logger.debug("Time Taken :: "+(((double)(time2-time1))/1000));
	         
	         details.addProperty("Time_Taken", (((double)(time2-time1))/1000));
	         
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "200");
			 details.addProperty("message", "file is successfully deleted from minIO");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "500");
			 details.addProperty("message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Delete_File from MinIO :::: "+e.getLocalizedMessage());
		 }
		 
		 return details;
	}
	
	
	
	private static FileInputStream readContentIntoByteArray(String fileName) throws Exception
	{
		  return readContentIntoByteArray(new File(fileName));
	}
	
	private static FileInputStream readContentIntoByteArray(File file) throws Exception
	{
		FileInputStream fileInputStream = new FileInputStream(file);
		return fileInputStream;
	}
	
}
