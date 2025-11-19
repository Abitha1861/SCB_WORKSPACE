package com.hdsoft.common;

import java.io.File;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class Mail_Service implements Database
{
	
	
	public static String Mail_user = Active_Mode.contains("local") ? "@.com" : "";				
	public static String Mail_Pass = Active_Mode.contains("local") ? "@" : "";		
		
	public static JavaMailSender getJavaMailSender() 
	{
	       JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	       
	       if(Active_Mode.contains("local"))
	       {
		       mailSender.setHost("mail.hdsoftwaresystems.com");   
		       mailSender.setPort(465);
		       
		       mailSender.setUsername(Mail_user);
		       mailSender.setPassword(Mail_Pass);
		       
		       Properties props = mailSender.getJavaMailProperties();
		       props.put("mail.transport.protocol", "smtp");
		       props.put("mail.smtp.starttls.enable","true");
		       props.put("mail.smtp.auth", "true"); 
		       props.put("mail.debug", "true");
		       
		       props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  
	       }
	       else
	       {
	    	   mailSender.setHost("smtp.office365.com");   
		       mailSender.setPort(587);
		       
		       mailSender.setUsername(Mail_user);
		       mailSender.setPassword(Mail_Pass);
		       
		       Properties props = mailSender.getJavaMailProperties();
		       props.put("mail.transport.protocol", "smtp");
		       props.put("mail.smtp.starttls.enable","true");	      
		       props.put("mail.smtp.auth", "true"); 
		       props.put("mail.debug", "true");
		      
		     
		       props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  
	       }
	           
	       return mailSender;
	}
	
	public boolean SendingMail(String To_mail, String Subject, String Message_Text) 
	{	
		try
		{
			 JavaMailSender EmailSender = getJavaMailSender();
			
			 MimeMessage message = EmailSender.createMimeMessage(); 
			 
			 message.setFrom(Mail_user);
			 
			 message.addRecipient(Message.RecipientType.TO, new InternetAddress(To_mail));
			
			 message.setSubject(Subject); 
	 		
			 message.setContent(Message_Text, "text/html");
	 		
	 		 EmailSender.send(message);
	 		
	 		 return true;      
		}
		catch(Exception e)
		{  
			 return false;      
		}   
	}
	 
	public boolean SendingMail(String[] To_mail, String Subject, String Message_Text) 
	{	
		try
		{
			 JavaMailSender EmailSender = getJavaMailSender();
			
			 MimeMessage message = EmailSender.createMimeMessage(); 
			 
			 message.setFrom(Mail_user);
			 
			 InternetAddress[] mailAddress_TO = new InternetAddress [To_mail.length] ;
			 
			 for(int i=0;i<mailAddress_TO.length;i++)
			 {
				 mailAddress_TO[i] = new InternetAddress(To_mail[i]);
			 }
			 
			 message.addRecipients(Message.RecipientType.TO, mailAddress_TO);
			
			 message.setSubject(Subject); 
	 		
			 message.setContent(Message_Text, "text/html");
	 		
	 		 EmailSender.send(message);
	 		
	 		 return true;      
		}
		catch(Exception e)
		{  
			 return false;      
		}   
	}
	
	public boolean sendMailWithAttachment(String[] To_mail, String subject, String body, MultipartFile Attachment) 
	{
		 JavaMailSender EmailSender = getJavaMailSender();
		
		 MimeMessage message = EmailSender.createMimeMessage();

		   try
		   {
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
	
				InternetAddress[] mailAddress_TO = new InternetAddress [To_mail.length] ;
				 
				 for(int i=0;i<mailAddress_TO.length;i++)
				 {
					 mailAddress_TO[i] = new InternetAddress(To_mail[i]);
				 }
				 
				helper.setFrom(Mail_user);
				helper.setTo(mailAddress_TO); 
				helper.setSubject(subject);
				helper.setText(body);
				
				File_handling f = new File_handling();
	
				FileSystemResource file = new FileSystemResource(f.ConvertMultipartFileToFile(Attachment));
				helper.addAttachment(file.getFilename(), file);
				
				EmailSender.send(message);

				 return true;      
			}
			catch(Exception e)
			{  
				 return false;      
			}    		
	}
	
	public boolean sendMailWithAttachment(String To_mail, String subject, String body, File file) 
	{
		 JavaMailSender EmailSender = getJavaMailSender();
		
		 MimeMessage message = EmailSender.createMimeMessage();

		   try
		   {
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
	
				helper.setFrom(Mail_user);
				helper.setTo(new InternetAddress(To_mail)); 
				helper.setSubject(subject);
				helper.setText(body,true);
				helper.addAttachment(file.getName(), file);
				
				EmailSender.send(message);

				 return true;      
			}
			catch(Exception e)
			{  
				 return false;      
			}    		
	}

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
}