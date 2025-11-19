
package com.hdsoft.models;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Lookup001;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.solace.TopicPublisher;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Suptect_CADD implements Database
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Suptect_CADD(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Suptect_CADD() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Suptect_CADD.class);


	@RequestMapping(value = {"/Datavision/Channel_push"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody String Test_Service( @RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 
		JsonObject details = new JsonObject();
		  
		try
		{
			details = Channel_records("RTS189", "", Message);
		}
		catch(Exception ex) 
		{
			details.addProperty("err", ex.getLocalizedMessage());
		}
			
 	 	return details.toString();
    }
	
	@RequestMapping(value = {"/Datavision/CADD/validation"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody String Test_Service1(@RequestBody String Message, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
    {	 

			Channel_records_validation("RTS189", "", Message);

			return Message;
    }
	
	//----------
	
	public JsonObject Channel_records(String INFO1, String INFO2, String INFO3) //RTS189
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
			 
			 String sql = "select suborgcode from sysconf001";
				
			 List<String> result = Jdbctemplate.queryForList(sql, String.class);
			 
			 String O_SERIAL = Generate_Suptech_Serial().get("Serial").getAsString();
			 
			 String Batch_id = Generate_Batch_Serial().get("Serial").getAsString();
		
			 String SUBORGCODE = result.size() !=0 ? result.get(0) : "";  
			 
			 JsonObject acctAccessUpdatesReq = data.get("acctAccessUpdatesReq").getAsJsonObject();
			 
			 JsonObject acctAccessUpdatesReqPayload = acctAccessUpdatesReq.get("acctAccessUpdatesReqPayload").getAsJsonObject();
			 
			 JsonObject acctAccessUpdatesReqPayloadDt = acctAccessUpdatesReqPayload.get("acctAccessUpdatesReqPayloadDt").getAsJsonObject();
			 
			 String CustomerAccessData = acctAccessUpdatesReqPayloadDt.get("CustomerAccessData").getAsString();		 
			 
			 String resultt  = decompressToString(CustomerAccessData);
			
			 JsonObject data_1 = util.XMLToJsonObject(resultt);
			 
			 JsonObject customerAcctAccessList = data_1.has("customerAcctAccessList")? data_1.get("customerAcctAccessList").getAsJsonObject() : new JsonObject();
			
			 JsonArray customerAcctAccessArray;
			 
			 JsonObject customerAcctAccess;
			 
			 int count = 0;

			 if(customerAcctAccessList.has("customerAcctAccess") && customerAcctAccessList.get("customerAcctAccess").isJsonArray()) 
			 {
				 customerAcctAccessArray = customerAcctAccessList.getAsJsonArray("customerAcctAccess");
				 
				 for (int i = 0; i < customerAcctAccessArray.size(); i++) 
				 {
				    customerAcctAccess = customerAcctAccessArray.get(i).getAsJsonObject();
				    		
					 String reportingDate = util.getCurrentReportDate();
					 String branchCode = "008300";  //"8400";  //005083
					 String customerIdentificationNumber = customerAcctAccess.get("customeridentificationnumber").getAsString();
					 String accountNumber = customerAcctAccess.get("accountnumber").getAsString();
					 String customerCategory = "4"; 
					 String subscribedChannel = "2";
					 String subscriptionDate = "";
					 String lastTransactionDate = ""; 
					 String channelStatus = customerAcctAccess.get("channelstatus").getAsString();

					 lastTransactionDate = "";
							 
					 if(customerAcctAccess.has("subscriptiondate")) 
					 {
						subscriptionDate = convertHktToTanzaniaDate(customerAcctAccess.get("subscriptiondate").getAsString());
						 
						sql = "insert into LOOKUP001 (SUBORGCODE, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12)\r\n" + 
								"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";  
						 
						Jdbctemplate.update(sql, new Object[] { SUBORGCODE, customerIdentificationNumber , accountNumber, subscriptionDate, "", "", "", "", "", "", "", "RTS189", "CADM" });

						lastTransactionDate = "<null>";
					 }
					 else if (customerAcctAccess.has("unsubscriptiondate")) 
					 {
						lastTransactionDate =  convertHktToTanzaniaDate(customerAcctAccess.get("unsubscriptiondate").getAsString());

						sql = "update LOOKUP001 set COLUMN4 =  ? where COLUMN1 = ? and COLUMN2 = ?";
						 
						Jdbctemplate.update(sql, new Object[] { lastTransactionDate , customerIdentificationNumber , accountNumber });
						 
						sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1 = ? and COLUMN2 = ?";  
						 
						List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CADM", customerIdentificationNumber , accountNumber }, new Lookup001_mapper());
							
						subscriptionDate = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "<null>" : "<null>";	

					 }
					 
					 count++;
					 
					 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13) " +
						      "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					 																						
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "channelInformation", count, reportingDate, branchCode, customerIdentificationNumber, accountNumber, customerCategory, subscribedChannel, subscriptionDate, lastTransactionDate, channelStatus });

				 }
			 }
			 else 
			 {
				 customerAcctAccess = customerAcctAccessList.has("customerAcctAccess")? customerAcctAccessList.get("customerAcctAccess").getAsJsonObject() : new JsonObject();			
								 
				 String reportingDate = util.getCurrentReportDate();
				 String branchCode = "008300"; //"8400";
				 String customerIdentificationNumber = customerAcctAccess.get("customeridentificationnumber").getAsString();
				 String accountNumber = customerAcctAccess.get("accountnumber").getAsString();
				 String customerCategory = "4"; 
				 String subscribedChannel = "2";
				 String subscriptionDate = "";
				 String lastTransactionDate = ""; 
				 String channelStatus = customerAcctAccess.get("channelstatus").getAsString();

				 lastTransactionDate = "";

				 if (customerAcctAccess.has("subscriptiondate")) 
				 {
					subscriptionDate = convertHktToTanzaniaDate(customerAcctAccess.get("subscriptiondate").getAsString());
					 
					sql = "insert into LOOKUP001 (SUBORGCODE, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12)\r\n" + 
							"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";  
					 
					Jdbctemplate.update(sql, new Object[] { SUBORGCODE, customerIdentificationNumber , accountNumber, subscriptionDate, "", "", "", "", "", "", "", "RTS189", "CADM" });

					lastTransactionDate = "<null>";
				 }
				 
				 else if (customerAcctAccess.has("unsubscriptiondate")) 
				 {
					lastTransactionDate =  convertHktToTanzaniaDate(customerAcctAccess.get("unsubscriptiondate").getAsString());

					sql = "update LOOKUP001 set COLUMN4 =  ? where COLUMN1 = ? and COLUMN2 = ?";
					 
					Jdbctemplate.update(sql, new Object[] { lastTransactionDate , customerIdentificationNumber , accountNumber });
					 
					sql = "select * from lookup001 where COLUMN11=? and COLUMN12=? and COLUMN1 = ? and COLUMN2 = ?";  
					 
					List<Lookup001> Info = Jdbctemplate.query(sql, new Object[] { INFO1, "CADM", customerIdentificationNumber , accountNumber }, new Lookup001_mapper());
						
					subscriptionDate = Info.size() > 0 ? !util.isNullOrEmpty(Info.get(0).getCOLUMN3()) ? Info.get(0).getCOLUMN3() : "<null>" : "<null>";	

				 }

				 
				 count++;
				 
				 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13) " +
					      "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 																						
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "D", INFO1, "channelInformation", count, reportingDate, branchCode, customerIdentificationNumber, accountNumber, customerCategory, subscribedChannel, subscriptionDate, lastTransactionDate, channelStatus });

			 }
			 
			 if(count > 0)				 
			 {
			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE, SERIAL, COLUMN1, COLUMN2, COLUMN3, COLUMN4, COLUMN5, COLUMN6, COLUMN7, COLUMN8, COLUMN9, COLUMN10, COLUMN11, COLUMN12, COLUMN13) " +
				      "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "C", INFO1, "channelInformation", "serial", "reportingDate", "branchCode", "customerIdentificationNumber", "accountNumber", "customerCategory", "subscribedChannel", "subscriptionDate", "lastTransactionDate", "channelStatus" });

			 
			 sql = "INSERT INTO REPORT002(SUBORGCODE,SERIAL,COLUMN1,COLUMN2,COLUMN3) values(?,?,?,?,?)";
				
			 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, O_SERIAL, "H", INFO1, "channelInformation"});
			 
			 
			 sql = "INSERT INTO RTS003(ID,SUBORGCODE,MODULE,SUBMODULE,SERVICECD,NO_OF_RECORDS,CREATED_BY,CREATED_ON,IS_PUSHED,REPORT_SERIAL,SOURCE_TYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			 
		     Jdbctemplate.update(sql, new Object[] { Batch_id, SUBORGCODE, "Others", "channelInformation", INFO1, count, "SYSTEM", util.getCurrentDate("dd-MMM-yyyy"), "0", O_SERIAL, "CADD" });	 

			 		     
		     Request_Dispatcher(Batch_id, O_SERIAL, INFO1, SUBORGCODE);
		     
		     details.addProperty("Serial", O_SERIAL);
	         details.addProperty("Batch_id", Batch_id);
	        
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Batch created successfully");    
			 }
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in channelInformation :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	


	public List<String> Channel_records_validation(String INFO1, String INFO2, String INFO3) //RTS189
	{ 
		List<String> validation = new ArrayList<String>();
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject data = util.XMLToJsonObject(INFO3);
					 
			 System.out.println(data);
				 
			 JsonObject acctAccessUpdatesReq = data.get("acctAccessUpdatesReq").getAsJsonObject();
			 
			 JsonObject acctAccessUpdatesReqPayload = acctAccessUpdatesReq.get("acctAccessUpdatesReqPayload").getAsJsonObject();
			 
			 JsonObject acctAccessUpdatesReqPayloadDt = acctAccessUpdatesReqPayload.get("acctAccessUpdatesReqPayloadDt").getAsJsonObject();
			 
			 String CustomerAccessData = acctAccessUpdatesReqPayloadDt.get("CustomerAccessData").getAsString();
			 
			 JsonObject header = acctAccessUpdatesReq.has("header") ? acctAccessUpdatesReq.get("header").getAsJsonObject() : new JsonObject();
			 
			 JsonObject originationDetails = header.has("originationDetails") ? header.get("originationDetails").getAsJsonObject() : new JsonObject();
			 		 
			 String trackingId = originationDetails.has("trackingId") ? originationDetails.get("trackingId").getAsString() : "";

			 String resultt  = decompressToString(CustomerAccessData);		 
		 
			 JsonObject data_1 = util.XMLToJsonObject(resultt);
			 			
			 JsonObject customerAcctAccessList = data_1.has("customerAcctAccessList")? data_1.get("customerAcctAccessList").getAsJsonObject() : new JsonObject();
				
			 JsonArray customerAcctAccessArray;
			 
			 JsonObject customerAcctAccess;
			 
			 String customerIdentificationNumber = "";
			 String accountNumber = "";
			 String subscriptionDate = "";
			 String lastTransactionDate = ""; 
			 String channelStatus = "";

			 
			 if (customerAcctAccessList.has("customerAcctAccess") && customerAcctAccessList.get("customerAcctAccess").isJsonArray()) 
			 {
				 customerAcctAccessArray = customerAcctAccessList.getAsJsonArray("customerAcctAccess");
				 
				 for (int i = 0; i < customerAcctAccessArray.size(); i++) 
				 {
				    customerAcctAccess = customerAcctAccessArray.get(i).getAsJsonObject();
				    		
					  customerIdentificationNumber = customerAcctAccess.get("customeridentificationnumber").getAsString();
					  accountNumber = customerAcctAccess.get("accountnumber").getAsString();
					  subscriptionDate = customerAcctAccess.has("subscriptiondate")? customerAcctAccess.get("subscriptiondate").getAsString() : "";
					  lastTransactionDate = customerAcctAccess.has("unsubscriptiondate")? customerAcctAccess.get("unsubscriptiondate").getAsString() : ""; 
					  channelStatus = customerAcctAccess.get("channelstatus").getAsString();

				 }
			}
			 
			 else 
			 {

				 customerAcctAccess = customerAcctAccessList.has("customerAcctAccess")? customerAcctAccessList.get("customerAcctAccess").getAsJsonObject() : new JsonObject();			
								 
				  customerIdentificationNumber = customerAcctAccess.get("customeridentificationnumber").getAsString();
				  accountNumber = customerAcctAccess.get("accountnumber").getAsString();
				  subscriptionDate = customerAcctAccess.has("subscriptiondate")? customerAcctAccess.get("subscriptiondate").getAsString() : "";
				  lastTransactionDate = customerAcctAccess.has("unsubscriptiondate")? customerAcctAccess.get("unsubscriptiondate").getAsString() : ""; 
				  channelStatus = customerAcctAccess.get("channelstatus").getAsString();
				 
			 }
			 
			 validation.add(0, trackingId);

			 if(util.isNullOrEmpty(customerIdentificationNumber)) validation.add("Error: Customer Identification Number is required");	
			 if(util.isNullOrEmpty(accountNumber)) validation.add("Error: Account Number is required");		
			 if(util.isNullOrEmpty(channelStatus)) validation.add("Error: Channel Status is required");	
			 if (util.isNullOrEmpty(subscriptionDate) && util.isNullOrEmpty(lastTransactionDate)) 
			 {
				 validation.add("Error: Both subscriptionDate and unsubscriptionDate are empty");		
			 }

		 }
		 catch(Exception e)
		 {
			 validation.add("Mandatory elements are missing in the payload");
			 
			 logger.debug("Exception in Channel_records_validation :::: "+e.getLocalizedMessage());
		 }
		
		 return validation;
	}
	
	public static String decompressToString(String compressed) throws Exception 
	{
        byte[] result = new byte[] {};
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(compressed.getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gzipIS = new GZIPInputStream(bis)) 
        {
	        byte[] buffer = new byte[1024];
	        int len;
	        while ((len = gzipIS.read(buffer)) != -1) 
	        {
	            bos.write(buffer, 0, len);
			}
	        result = bos.toByteArray();
		}
        return new String(result, "UTF-8");
	}
	
	public JsonObject Send_Solace_ErrorRespone(String API_Name, String Message, List<String> validation) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
			Common_Utils util = new Common_Utils();
			 
			String current_date = util.getCurrentReportDate();
		 
		 	SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMyyyyHHmm");
            Date date = inputFormat.parse(current_date);
            
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.of("+08:00"));
                     
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            
            DateTimeFormatter outputFormat2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
                        
            String formattedDate = zonedDateTime.format(outputFormat);
            String formattedDate2 = zonedDateTime.format(outputFormat2);
            		 
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("acknowledgeConsumptionReq");
            rootElement.setAttribute("xmlns:dataCons", "http://www.sc.com/DataVision/v1/DataConsumptionConfirmation");
            rootElement.setAttribute("xmlns:ns", "http://www.sc.com/SCBML-1");
            doc.appendChild(rootElement);

            Element header = doc.createElement("ns:header");
            rootElement.appendChild(header);

            Element messageDetails = doc.createElement("ns:messageDetails");
            header.appendChild(messageDetails);

            Element messageVersion = doc.createElement("ns:messageVersion");
            messageVersion.appendChild(doc.createTextNode("1.0"));
            messageDetails.appendChild(messageVersion);

            Element messageType = doc.createElement("ns:messageType");
            messageDetails.appendChild(messageType);

            Element typeName = doc.createElement("ns:typeName");
            typeName.appendChild(doc.createTextNode("Referential:DataConsumptionConfirmation"));
            messageType.appendChild(typeName);

            Element subType = doc.createElement("ns:subType");
            subType.setAttribute("subTypeScheme", "");
            messageType.appendChild(subType);

            Element subTypeName = doc.createElement("ns:subTypeName");
            subTypeName.appendChild(doc.createTextNode("acknowledgeConsumption"));
            subType.appendChild(subTypeName);

            Element multiMessage = doc.createElement("ns:multiMessage");
            messageDetails.appendChild(multiMessage);

            Element multiMessageKnown = doc.createElement("ns:multiMessageKnown");
            multiMessage.appendChild(multiMessageKnown);

            Element multiMessageCount = doc.createElement("ns:multiMessageCount");
            multiMessageCount.appendChild(doc.createTextNode("1"));
            multiMessageKnown.appendChild(multiMessageCount);

            Element messagePosition = doc.createElement("ns:messagePosition");
            messagePosition.appendChild(doc.createTextNode("1"));
            multiMessageKnown.appendChild(messagePosition);

            Element messageOrder = doc.createElement("ns:messageOrder");
            messageOrder.appendChild(doc.createTextNode("FIFO"));
            multiMessageKnown.appendChild(messageOrder);

            Element originationDetails = doc.createElement("ns:originationDetails");
            header.appendChild(originationDetails);

            Element messageSender = doc.createElement("ns:messageSender");
            originationDetails.appendChild(messageSender);

            messageSender.appendChild(doc.createTextNode("MACS"));

            Element senderDomain = doc.createElement("ns:senderDomain");
            originationDetails.appendChild(senderDomain);

            Element domainName = doc.createElement("ns:domainName");
            domainName.appendChild(doc.createTextNode("Referential"));
            senderDomain.appendChild(domainName);

            Element subDomainName = doc.createElement("ns:subDomainName");
            subDomainName.setAttribute("subdomainNameScheme", "");
            senderDomain.appendChild(subDomainName);

            Element subDomainType = doc.createElement("ns:subDomainType");
            subDomainType.appendChild(doc.createTextNode("MACS"));
            subDomainName.appendChild(subDomainType);

            Element countryCode = doc.createElement("ns:countryCode");
            countryCode.appendChild(doc.createTextNode("TZ"));
            originationDetails.appendChild(countryCode);

            Element messageTimestamp = doc.createElement("ns:messageTimestamp");
            messageTimestamp.appendChild(doc.createTextNode(formattedDate));
            originationDetails.appendChild(messageTimestamp);

            Element initiatedTimestamp = doc.createElement("ns:initiatedTimestamp");
            initiatedTimestamp.appendChild(doc.createTextNode(formattedDate));
            originationDetails.appendChild(initiatedTimestamp);

            Element trackingId = doc.createElement("ns:trackingId");
            trackingId.appendChild(doc.createTextNode(validation.get(0)));
            originationDetails.appendChild(trackingId);

            Element captureSystem = doc.createElement("ns:captureSystem");
            captureSystem.appendChild(doc.createTextNode("SCI"));
            header.appendChild(captureSystem);

            Element process = doc.createElement("ns:process");
            header.appendChild(process);

            Element processName = doc.createElement("ns:processName");
            processName.appendChild(doc.createTextNode("acknowledgeConsumption"));
            process.appendChild(processName);

            Element eventType = doc.createElement("ns:eventType");
            eventType.appendChild(doc.createTextNode("Notify"));
            process.appendChild(eventType);

            Element payload = doc.createElement("dataCons:acknowledgeConsumptionReqPayload");
            rootElement.appendChild(payload);

            Element payloadFormat = doc.createElement("ns:payloadFormat");
            payloadFormat.appendChild(doc.createTextNode("XML"));
            payload.appendChild(payloadFormat);

            Element payloadVersion = doc.createElement("ns:payloadVersion");
            payloadVersion.appendChild(doc.createTextNode("1.0"));
            payload.appendChild(payloadVersion);

            Element acknowledgeConsumptionReq = doc.createElement("dataCons:acknowledgeConsumptionReq");
            payload.appendChild(acknowledgeConsumptionReq);

            Element messages = doc.createElement("Messages");
            acknowledgeConsumptionReq.appendChild(messages);

            Element messageInfo = doc.createElement("MessageInfo");
            messages.appendChild(messageInfo);

            Element messageTrackingId = doc.createElement("trackingId");
            messageTrackingId.appendChild(doc.createTextNode(validation.get(0)));
            messageInfo.appendChild(messageTrackingId);

            Element processDate = doc.createElement("processDate");
            processDate.appendChild(doc.createTextNode(formattedDate2));
            messageInfo.appendChild(processDate);

            Element publisherId = doc.createElement("publisherId");
            publisherId.appendChild(doc.createTextNode("MACS"));
            messageInfo.appendChild(publisherId);

            Element subscriberId = doc.createElement("subscriberId");
            subscriberId.appendChild(doc.createTextNode("CADM"));
            messageInfo.appendChild(subscriberId);

            Element responseMessageDetails = doc.createElement("responseMessageDetails");
            
            for (int i = 1; i < validation.size(); i++) 
            {
                String description = validation.get(i);

                Element responseMessage = doc.createElement("responseMessage");

                Element code = doc.createElement("code");
                code.appendChild(doc.createTextNode("NACK"));
                responseMessage.appendChild(code);

                Element statusCode = doc.createElement("statusCode");
                statusCode.appendChild(doc.createTextNode("Rejected"));
                responseMessage.appendChild(statusCode);

                Element statusDescription = doc.createElement("statusDescription");
                statusDescription.appendChild(doc.createTextNode(description));  // Add the validation message here
                responseMessage.appendChild(statusDescription);

                responseMessageDetails.appendChild(responseMessage);	            
            }

	            messageInfo.appendChild(responseMessageDetails);

	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	            DOMSource source = new DOMSource(doc);
	            StreamResult result = new StreamResult(new StringWriter());
	            transformer.transform(source, result);
	            
				String[] args = new String[] { "SOLACE", "SOL011", result.getWriter().toString() };
				 
				new TopicPublisher(Jdbctemplate).run(args);
				 
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Send_Solace_ErrorRespone :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Send_Solace_Respone(String API_Name, String Message , List<String> validation) 
	{ 
		 JsonObject details = new JsonObject();
		 
		 try
		 {
				Common_Utils util = new Common_Utils();
				 
				String current_date = util.getCurrentReportDate();
			 
			 	SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMyyyyHHmm");
	            Date date = inputFormat.parse(current_date);
	            
	            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.of("+08:00"));
	                       
	            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	            
	            DateTimeFormatter outputFormat2 = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
	            	            
	            String formattedDate = zonedDateTime.format(outputFormat);
	            String formattedDate2 = zonedDateTime.format(outputFormat2);
	            
	            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	            Document doc = docBuilder.newDocument();

	            Element rootElement = doc.createElement("acknowledgeConsumptionReq");
	            rootElement.setAttribute("xmlns:dataCons", "http://www.sc.com/DataVision/v1/DataConsumptionConfirmation");
	            rootElement.setAttribute("xmlns:ns", "http://www.sc.com/SCBML-1");
	            doc.appendChild(rootElement);

	            Element header = doc.createElement("ns:header");
	            rootElement.appendChild(header);

	            Element messageDetails = doc.createElement("ns:messageDetails");
	            header.appendChild(messageDetails);

	            Element messageVersion = doc.createElement("ns:messageVersion");
	            messageVersion.appendChild(doc.createTextNode("1.0"));
	            messageDetails.appendChild(messageVersion);

	            Element messageType = doc.createElement("ns:messageType");
	            messageDetails.appendChild(messageType);

	            Element typeName = doc.createElement("ns:typeName");
	            typeName.appendChild(doc.createTextNode("Referential:DataConsumptionConfirmation"));
	            messageType.appendChild(typeName);

	            Element subType = doc.createElement("ns:subType");
	            subType.setAttribute("subTypeScheme", "");
	            messageType.appendChild(subType);

	            Element subTypeName = doc.createElement("ns:subTypeName");
	            subTypeName.appendChild(doc.createTextNode("acknowledgeConsumption"));
	            subType.appendChild(subTypeName);

	            Element multiMessage = doc.createElement("ns:multiMessage");
	            messageDetails.appendChild(multiMessage);

	            Element multiMessageKnown = doc.createElement("ns:multiMessageKnown");
	            multiMessage.appendChild(multiMessageKnown);

	            Element multiMessageCount = doc.createElement("ns:multiMessageCount");
	            multiMessageCount.appendChild(doc.createTextNode("1"));
	            multiMessageKnown.appendChild(multiMessageCount);

	            Element messagePosition = doc.createElement("ns:messagePosition");
	            messagePosition.appendChild(doc.createTextNode("1"));
	            multiMessageKnown.appendChild(messagePosition);

	            Element messageOrder = doc.createElement("ns:messageOrder");
	            messageOrder.appendChild(doc.createTextNode("FIFO"));
	            multiMessageKnown.appendChild(messageOrder);

	            Element originationDetails = doc.createElement("ns:originationDetails");
	            header.appendChild(originationDetails);

	            Element messageSender = doc.createElement("ns:messageSender");
	            originationDetails.appendChild(messageSender);

	            messageSender.appendChild(doc.createTextNode("MACS"));

	            Element senderDomain = doc.createElement("ns:senderDomain");
	            originationDetails.appendChild(senderDomain);

	            Element domainName = doc.createElement("ns:domainName");
	            domainName.appendChild(doc.createTextNode("Referential"));
	            senderDomain.appendChild(domainName);

	            Element subDomainName = doc.createElement("ns:subDomainName");
	            subDomainName.setAttribute("subdomainNameScheme", "");
	            senderDomain.appendChild(subDomainName);

	            Element subDomainType = doc.createElement("ns:subDomainType");
	            subDomainType.appendChild(doc.createTextNode("MACS"));
	            subDomainName.appendChild(subDomainType);

	            Element countryCode = doc.createElement("ns:countryCode");
	            countryCode.appendChild(doc.createTextNode("TZ"));
	            originationDetails.appendChild(countryCode);

	            Element messageTimestamp = doc.createElement("ns:messageTimestamp");
	            messageTimestamp.appendChild(doc.createTextNode(formattedDate));
	            originationDetails.appendChild(messageTimestamp);

	            Element initiatedTimestamp = doc.createElement("ns:initiatedTimestamp");
	            initiatedTimestamp.appendChild(doc.createTextNode(formattedDate));
	            originationDetails.appendChild(initiatedTimestamp);

	            Element trackingId = doc.createElement("ns:trackingId");
	            trackingId.appendChild(doc.createTextNode(validation.get(0)));
	            originationDetails.appendChild(trackingId);

	            Element captureSystem = doc.createElement("ns:captureSystem");
	            captureSystem.appendChild(doc.createTextNode("SCI"));
	            header.appendChild(captureSystem);

	            Element process = doc.createElement("ns:process");
	            header.appendChild(process);

	            Element processName = doc.createElement("ns:processName");
	            processName.appendChild(doc.createTextNode("acknowledgeConsumption"));
	            process.appendChild(processName);

	            Element eventType = doc.createElement("ns:eventType");
	            eventType.appendChild(doc.createTextNode("Notify"));
	            process.appendChild(eventType);

	            Element payload = doc.createElement("dataCons:acknowledgeConsumptionReqPayload");
	            rootElement.appendChild(payload);

	            Element payloadFormat = doc.createElement("ns:payloadFormat");
	            payloadFormat.appendChild(doc.createTextNode("XML"));
	            payload.appendChild(payloadFormat);

	            Element payloadVersion = doc.createElement("ns:payloadVersion");
	            payloadVersion.appendChild(doc.createTextNode("1.0"));
	            payload.appendChild(payloadVersion);

	            Element acknowledgeConsumptionReq = doc.createElement("dataCons:acknowledgeConsumptionReq");
	            payload.appendChild(acknowledgeConsumptionReq);

	            Element messages = doc.createElement("Messages");
	            acknowledgeConsumptionReq.appendChild(messages);

	            Element messageInfo = doc.createElement("MessageInfo");
	            messages.appendChild(messageInfo);

	            Element messageTrackingId = doc.createElement("trackingId");
	            messageTrackingId.appendChild(doc.createTextNode(validation.get(0)));
	            messageInfo.appendChild(messageTrackingId);

	            Element processDate = doc.createElement("processDate");
	            processDate.appendChild(doc.createTextNode(formattedDate2));
	            messageInfo.appendChild(processDate);

	            Element publisherId = doc.createElement("publisherId");
	            publisherId.appendChild(doc.createTextNode("MACS"));
	            messageInfo.appendChild(publisherId);

	            Element subscriberId = doc.createElement("subscriberId");
	            subscriberId.appendChild(doc.createTextNode("CADM"));
	            messageInfo.appendChild(subscriberId);

	            Element responseMessageDetails = doc.createElement("responseMessageDetails");
	            messageInfo.appendChild(responseMessageDetails);

	            Element responseMessage = doc.createElement("responseMessage");
	            responseMessageDetails.appendChild(responseMessage);

	            Element code = doc.createElement("code");
	            code.appendChild(doc.createTextNode("ACK"));
	            responseMessage.appendChild(code);

	            Element statusCode = doc.createElement("statusCode");
	            statusCode.appendChild(doc.createTextNode("RECEIVED"));
	            responseMessage.appendChild(statusCode);

	            Element statusDescription = doc.createElement("statusDescription");
	            statusDescription.appendChild(doc.createTextNode("Message is Good"));
	            responseMessage.appendChild(statusDescription);

	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	            DOMSource source = new DOMSource(doc);
	            StreamResult result = new StreamResult(new StringWriter());
	            transformer.transform(source, result);
				 
				 String[] args = new String[] { "SOLACE", "SOL011", result.getWriter().toString() };
				 
				 
				 new TopicPublisher(Jdbctemplate).run(args);
				 
				 details.addProperty("result", "success");
				 details.addProperty("stscode", "HP00");
				 details.addProperty("message", "Batch created successfully");    
		 }
		 catch(Exception e)
		 {
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug("Exception in Send_Solace_Respone :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
			
	public static String convertHktToTanzaniaDate(String inputDateStr) {
	       try 
	        {
	        	
	            String timeWithoutTimezone = inputDateStr.replace("HKT", "").trim();
	            
	            SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMyyyyHHmm");
	            
	            TimeZone hktTimeZone = TimeZone.getTimeZone("Asia/Hong_Kong");
	            inputFormat.setTimeZone(hktTimeZone);
	            
	            Date date = inputFormat.parse(timeWithoutTimezone);
	            
	            SimpleDateFormat outputFormat = new SimpleDateFormat("ddMMyyyyHHmm");
	            TimeZone tanzaniaTimeZone = TimeZone.getTimeZone("Africa/Dar_es_Salaam");
	            outputFormat.setTimeZone(tanzaniaTimeZone);
	            
	            return outputFormat.format(date);

	        } 
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	            return null;
	        }
	    }
	
	public JsonObject Request_Dispatcher(String BatchId, String REPORTSL, String SERVICD, String SUBORGCODE)  
	{
		JsonObject details = new JsonObject();
		
		try
		{
			Common_Utils utils = new Common_Utils();
		
			String Sql = "select limitsl from RTS004 where apicode = ? and status=?";
			
			int Limit = Jdbctemplate.queryForObject(Sql, new Object[] { SERVICD, "1"}, Integer.class);
			
			String dataCount = "select count(*) from report002 where serial = ? and COLUMN1 = ? and COLUMN2=?";
			
			int Total_dataCount = Jdbctemplate.queryForObject(dataCount, new Object[] { REPORTSL, "H", SERVICD }, Integer.class);
			
			int Total_Records=0;
			
			if(Total_dataCount > 1) 
			{
				String sql1 = "select count(*) from report002 w where serial = ? and COLUMN1 = ? and  COLUMN3 = (select COLUMN3 from report002 where serial = w.serial and COLUMN1 = ? and column4 = (select min(column4) from report002 where serial = w.serial and column1 = ?))";
			 
				Total_Records = Jdbctemplate.queryForObject(sql1, new Object[] { REPORTSL, "D", "H", "H" }, Integer.class);
			}
			else
			{
			    String sql = "select count(*) from REPORT002 where SERIAL = ? and COLUMN1 = ? and COLUMN2 = ?";
			
			    Total_Records = Jdbctemplate.queryForObject(sql, new Object[] { REPORTSL, "D", SERVICD }, Integer.class);
			}
			
			int limit = Limit;  int total = Total_Records;
			
			int StartSl = 1;   int EndSl = total < limit ? total : limit;
			
			do
			{
				 if(EndSl == total && StartSl > total) 
				 {
					 break;
				 }
				 
				 String Reqsl = Generate_Report_Serial().get("Serial").getAsString(); 
				 
				 String Refno = Reqsl;   
				  
				 String sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
					
				 List<String> result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", new Sysconfig().getHostAddress() } , String.class);
					 
				 String Status = result.size() !=0 ? result.get(0) : "Q";   // get Queue name Q or Q1
				 
				 sql = "Insert into RTS005(suborgcode,syscode,paytype,reqdate,reqsl,refno,batchid,apicode,reportserial,startsl,endsl,status) values (?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "DV", "RTSIS", utils.getCurrentDate("dd-MMM-yyyy"), Reqsl , Refno, BatchId, SERVICD, REPORTSL, StartSl, EndSl, Status } );
				 
				 if(Active_Mode.equals("local"))
				 {
					 sql = "Insert into RTS005(suborgcode,syscode,paytype,reqdate,reqsl,refno,batchid,apicode,reportserial,startsl,endsl,status) values (?,?,?,?,?,?,?,?,?,?,?,?)";
				 
					 Jdbctemplate.update(sql, new Object[] { SUBORGCODE, "DV", "RTSIS", utils.getCurrentDate("dd-MMM-yyyy"), Reqsl+1 , Refno, BatchId, SERVICD, REPORTSL, StartSl, EndSl, "E" } );
				 }
				 
				 StartSl = EndSl+1;
				 
				 EndSl = (EndSl + limit) > total ? total : EndSl + limit;
				 
				 logger.debug(">>>>>>>>>>> Data inserted into RTS005 for the Report serial "+REPORTSL + "/" + StartSl + "/" + EndSl+" <<<<<<<<<<<<<<<");	
				 
			}while(EndSl <= total);
			
			details.addProperty("result", "success");
			details.addProperty("stscode", "HP00");
			details.addProperty("message", "Data added to the Queue !!");
			 
			logger.debug(">>>>>>>>>>> Data inserted into RTS005 for the Report serial "+REPORTSL+" <<<<<<<<<<<<<<<");	
		}
		catch(Exception e)
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP200");
			 details.addProperty("message", e.getLocalizedMessage()); 
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Report_Splitter <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		}
		
		return details;
	}
	
	public JsonObject Generate_Report_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select seq_report.nextval from dual";
			   
			 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
			 
			 details.addProperty("Serial", SL);
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Serial generated Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Generate_Suptech_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select RTSIS_REPORT_SERIAL.Nextval from dual";
			   
			 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
			 
			 details.addProperty("Serial", SL);
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Serial generated Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	public JsonObject Generate_Batch_Serial() 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 String sql = "select rts003_id_seq.Nextval from dual";
			   
			 int SL = Jdbctemplate.queryForObject(sql, Integer.class);
			 
			 details.addProperty("Serial", SL);
			
			 details.addProperty("Result", "Success");
			 details.addProperty("Message", "Serial generated Successfully !!");
		 }
		 catch(Exception e)
		 {
			 details.addProperty("Result", "Failed");
			 details.addProperty("Message", e.getLocalizedMessage());  	
			 
			 logger.debug("Exception in Generate_Serial from REQ001 :::: "+e.getLocalizedMessage());
		 }
		
		 return details;
	}
	
	private class Lookup001_mapper implements RowMapper<Lookup001>  
    {
    	Common_Utils util = new Common_Utils();
    	
		public Lookup001 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Lookup001 Info = new Lookup001(); 
			
			Info.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			Info.setCOLUMN1(util.ReplaceNull(rs.getString("COLUMN1")));
			Info.setCOLUMN2(util.ReplaceNull(rs.getString("COLUMN2")));
			Info.setCOLUMN3(util.ReplaceNull(rs.getString("COLUMN3")));
			Info.setCOLUMN4(util.ReplaceNull(rs.getString("COLUMN4")));
			Info.setCOLUMN5(util.ReplaceNull(rs.getString("COLUMN5")));
			Info.setCOLUMN6(util.ReplaceNull(rs.getString("COLUMN6")));
			Info.setCOLUMN7(util.ReplaceNull(rs.getString("COLUMN7")));
			Info.setCOLUMN8(util.ReplaceNull(rs.getString("COLUMN8")));
			Info.setCOLUMN9(util.ReplaceNull(rs.getString("COLUMN9")));
			Info.setCOLUMN10(util.ReplaceNull(rs.getString("COLUMN10")));
			Info.setCOLUMN11(util.ReplaceNull(rs.getString("COLUMN11")));
			Info.setCOLUMN12(util.ReplaceNull(rs.getString("COLUMN12")));
			
			return Info;
		}
    }
	
	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
