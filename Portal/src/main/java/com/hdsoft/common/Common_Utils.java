package com.hdsoft.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Common_Utils 
{
	
	private static final Logger logger = LogManager.getLogger(Common_Utils.class);
	
		public List<String> get_keys_as_list(JsonObject object)
		{
			List<String> KEYS = new ArrayList<String>();
			
			String str = object.toString(); 

	        JsonParser parser = new JsonParser();
	        
	        JsonObject jObj = (JsonObject)parser.parse(str);

	        for(Entry<String, JsonElement> entry: jObj.entrySet()) 
			{
	       	 	KEYS.add(entry.getKey());    	 	
			 }
	        
	        return KEYS;
		}
		
	
		/*public boolean isXMLValid(String test) 
		{
		    try 
		    {
		        SAXParserFactory.newInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader(test)));
		        
		        return true;
		    } 
		    catch(Exception ex) 
		    {
		    	return false;
		    }
		}*/
	
	
	public String Generate_OTP(int size)
	{
		String numbers = "0123456789"; 
		
        char[] otp = new char[size]; 
  
        for (int i = 0; i < size; i++)  otp[i] = numbers.charAt(new Random().nextInt(numbers.length())); 
             
        return String.valueOf(otp); 
	}
	
	public String Generate_Random_String(int size)
	{
		String numbers = "abcdefghijklmnopqrstuvwxyz0123456789"; 
		  
        char[] user_id = new char[size]; 
  
        for (int i = 0; i < size; i++)  user_id[i] = numbers.charAt(new Random().nextInt(numbers.length())); 
        
        return String.valueOf(user_id);
	}
	
	public String Generate_Random_onlyString(int size)
	{
		String numbers = "abcdefghijklmnopqrstuvwxyz"; 
		  
        char[] user_id = new char[size]; 
  
        for (int i = 0; i < size; i++)  user_id[i] = numbers.charAt(new Random().nextInt(numbers.length())); 
        
        return String.valueOf(user_id);
	}
	
	public String Generate_Random_Amount()
	{
		double min = 10.00;
		
		double max = 10000.00;
		
		double diff = max - min;
		
		DecimalFormat formatter = new DecimalFormat("#0.00"); 
		
		double randomValue = min + Math.random( ) * diff;
		
		double tempRes = Math.floor(randomValue * 10);
		
		double finalRes = tempRes/10;
		
        return formatter.format(finalRes);
	}
	
	public JsonArray get_keys_from_Json(JsonObject object)
	{
		JsonArray keys = new JsonArray();
		
		try
		{
			String str = object.toString(); 

	        JsonParser parser = new JsonParser();
	        
	        JsonObject jObj = (JsonObject)parser.parse(str);

	        for (Entry<String, JsonElement> e : jObj.entrySet()) 
	        {
	            keys.add(e.getKey());
	        }
		}
		catch(Exception ex)
		{
			
		}
		
        return keys;
	}
	
	public JsonArray find_key_and_types(JsonObject object)
	{
		JsonArray Pairs = new JsonArray();
		
		String str = object.toString(); 

        JsonParser parser = new JsonParser();
        
        JsonObject jObj = (JsonObject)parser.parse(str);

        List<String> keys = new ArrayList<String>();
        
        for (Entry<String, JsonElement> e : jObj.entrySet()) 
        {
            keys.add(e.getKey());
        }
        
        for(Entry<String, JsonElement> entry: jObj.entrySet()) 
		{
       	 	JsonObject form_datas = new JsonObject();
       	 	
       	 	form_datas.addProperty("Key", entry.getKey());  
       	 	
       	 	if(object.get(entry.getKey()).isJsonObject())
       	 	{
       	 		form_datas.addProperty("Type", "json_object"); 
       	 	}
       	 	else if(object.get(entry.getKey()).isJsonArray())
       	 	{
       	 		form_datas.addProperty("Type", "json_array"); 
       	 	}
       	 	else if(object.get(entry.getKey()).isJsonNull())
       	 	{
       	 		form_datas.addProperty("Type", "null"); 
       	 	}
       	 	else if (object.get(entry.getKey()).isJsonPrimitive()) 
       	 	{
	             if (object.get(entry.getKey()).getAsJsonPrimitive().isBoolean())
	             {
	            	 form_datas.addProperty("Type", "boolean"); 
	             }
	             
	             else if(object.get(entry.getKey()).getAsJsonPrimitive().isString()) 
	             {
	            	 form_datas.addProperty("Type", "string"); 
	             }
            }
       	 
       	 	Pairs.add(form_datas);
		 }
        
        return Pairs;
	}	
	
	
	

	public String ReplaceNull(String Value)
	{
		return Value == null || Value.isEmpty() ? "" : Value.trim();
	}
	
	public String ReplaceNull(Object Value)
	{
		return Value == null ? "" : Value.toString();
	}
	
	public String Replacewhiespaces(String Value)
	{
		Value = Value.replaceAll("\n", "");
		Value = Value.replaceAll("\r", "");
		    
		return Value;
	}
	
	public String Replace_words_from_String_(String Source, String Old_String, String Replace_String)
	{
		if(Source == null || Source.isEmpty())  
		{
			return "";
		}
		else
		{
			Source = Source.replaceAll(Old_String, Replace_String);
			
			return Source;
		}
	}
	
	public String Replace_Special_Characters(String Source)
	{
		Source = Source.replaceAll("[^a-zA-Z0-9]", "_");
		
		Source = Source.replaceAll("\\s+", "_");
	
		return Source;
	}
	
	public boolean isNullOrEmpty(String str)
	{
	     if(str != null && !str.isEmpty())  return false;
	           
	     return true;
	}
	
	public int ParseInt(String str)
	{
		 int out = 0;
		
		 try 
	     {
			 out = Integer.parseInt(str);
	     } 
	     catch(Exception ex) 
	     {
	    	 logger.debug("Exception in ParseInt, Received values is "+str);
	     }
		 
		 return out;
	}
	
		public ArrayList<String> Remove_Dupicates(ArrayList<String> al)
	{
		LinkedHashSet<String> lhs = new LinkedHashSet<String>();
		
		lhs.addAll(al);
		
		al.clear();
		
		al.addAll(lhs);
		
		return al;
	}
	
	
	public String getCurrentDate() throws ParseException   
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date dateWithoutTime = sdf.parse(sdf.format(new Date()));
		
		return sdf.format(dateWithoutTime).toString();
	}
	
	public String getCurrentReportDate() 
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
			
			   TimeZone istTimeZone = TimeZone.getTimeZone("Africa/Dar_es_Salaam");
		        sdf.setTimeZone(istTimeZone);
			
			Date dateWithoutTime = sdf.parse(sdf.format(new Date()));
			
			String dt =  sdf.format(dateWithoutTime).toString();
			
			dt = dt.replaceAll("-", "");
			
			return dt;
		}
		catch(Exception ex)
		{
			return "";
		}
	}
	
	public String AddYearstoDate(String Date, String Format, int Years) 
	{
		try
		{
			String dateString = Date;

	        LocalDate date = LocalDate.parse(dateString);

	        LocalDate newDate = date.plusYears(Years);

	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Format);
	        
			return newDate.format(formatter);
		}
		catch(Exception ex)
		{
			return "";
		}
	}
	
	public String getCurrentYear() throws ParseException   
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		
		Date dateWithoutTime = sdf.parse(sdf.format(new Date()));
		
		return sdf.format(dateWithoutTime).toString();
	}
	
	public String getCurrentDate(String Format) throws ParseException 
	{
		SimpleDateFormat sdf = new SimpleDateFormat(Format);
		
		Date dateWithoutTime = sdf.parse(sdf.format(new Date()));
		
		return sdf.format(dateWithoutTime).toString();
	}
	
	public String Convert_Date_Format(String Date_value, String From_Format, String To_Format) throws ParseException
	{
		if(Date_value != null && !Date_value.isEmpty())  
		{
			SimpleDateFormat format1 = new SimpleDateFormat(From_Format);
		 
		    SimpleDateFormat format2 = new SimpleDateFormat(To_Format);
		    
		    Date date = format1.parse(Date_value);
	    
		    return format2.format(date);
		}
		else
		{
			return "";
		}
	}
	
	public String Convert_BOT_Date_Format(String Date_value, String From_Format) throws ParseException
	{
		String Res = "";
		
		try
		{
			if(Date_value != null && !Date_value.isEmpty())  
			{
				SimpleDateFormat format1 = new SimpleDateFormat(From_Format);
			 
			    SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
			    
			    Date date = format1.parse(Date_value);
		    
			    Res = format2.format(date).replaceAll("-", "");
			}
		}
		catch(Exception ex)
		{
			logger.debug(ex.getLocalizedMessage());
		}
		
		return Res;
	}
	
	public Timestamp Convert_to_timestamp(String Date_value, String From_Format) throws ParseException
	{
		Timestamp Res = null;
		
		try
		{
			if(Date_value != null && !Date_value.isEmpty())  
			{
				 SimpleDateFormat dateFormat = new SimpleDateFormat(From_Format);
			     Date parsedDate = dateFormat.parse(Date_value);
			     Res = new java.sql.Timestamp(parsedDate.getTime());
			}
		}
		catch(Exception ex)
		{
			ex.getLocalizedMessage();
		}
		
		return Res;
	}
	
	public String getCurrentDateTime_add(int addMinuteTime)
	{
		String res = "";
		
		try 
		{
			 Date targetTime = Calendar.getInstance().getTime();
			
			 targetTime = DateUtils.addMinutes(targetTime, addMinuteTime); 
			 
			 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			 
			 res = format1.format(targetTime); 
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		       
		 return res;	 
	}
	
	public String getCurrentDateTime()
	{
		String res = "";
		
		try 
		{
			 Date targetTime = Calendar.getInstance().getTime();
			
			 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			 
			 res = format1.format(targetTime); 
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		       
		 return res;	 
	} 
	
	public String getyesterday(String DATE_FORMAT)  
	{
		String res = "";
		
		try 
		{
			   Calendar cal = Calendar.getInstance();
			   
			   DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			   
			   cal.add(Calendar.DATE, -1);
			   
			   res = dateFormat.format(cal.getTime());
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		
	    return res;
	}
	
	public boolean isvalidDate(String value, String DATE_FORMAT)
	{
		try 
		{
			 DateFormat df = new SimpleDateFormat(DATE_FORMAT);
			 
			 df.setLenient(false);
	         df.parse(value);
	         
	         return true;
		} 
		catch(Exception ex) 
		{
			return false;
		}
	}
	
	public Timestamp String_To_Timestamp(String dateString)
	{
		Timestamp timestamp = null;
		
		try 
		{
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			Date date = formatter.parse(dateString);

			timestamp = new Timestamp(date.getTime());
			
			return timestamp; 
		} 
		catch(Exception ex) 
		{
			return null;
		}
	}
	
	
	
	public Timestamp get_oracle_Timestamp()
	{
		return new java.sql.Timestamp(new java.util.Date().getTime());
	}
	
	public String getCurrentTime_with_seconds()
	{
		SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm:ss a");
		
		return formatDate.format(new Date()).toString(); 
	}
	
	public String getCurrentTime_12_hr()
	{
		SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
		
		return formatDate.format(new Date()).toString(); 
	}
	
	public String Convert_24_Hr_to_12_hr_Time(String _24HourTime)
	{
		try 
		{       
           SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
           SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
           Date _24HourDt = _24HourSDF.parse(_24HourTime);
           
           return _12HourSDF.format(_24HourDt);
	    } 
		catch (Exception e) 
		{
			return "";
	    }
	}
	
	public String Convert_12_Hr_to_24_hr_Time(String _12HourTime)
	{
		try 
		{       
		   SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
           SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
           
           Date _12HourDt = _12HourSDF.parse(_12HourTime);
           
           return _24HourSDF.format(_12HourDt);
	    } 
		catch (Exception e) 
		{
			return "";
	    }
	}
	
	public String Convert_12_hr_and_Concat(String From_Time, String To_Time)
	{
		if(From_Time != null && !From_Time.equals(""))
		{
			if(To_Time != null && !To_Time.equals(""))
			{
				return Convert_24_Hr_to_12_hr_Time(From_Time) + " to "+Convert_24_Hr_to_12_hr_Time(To_Time);
			}
			else
			{
				return Convert_24_Hr_to_12_hr_Time(From_Time);
			}
		}
		else
		{
			return "";
		}
	}

	
	
	public String bytesIntoHumanReadable(long bytes) 
	{
	    long kilobyte = 1024;
	    long megabyte = kilobyte * 1024;
	    long gigabyte = megabyte * 1024;
	    long terabyte = gigabyte * 1024;

	    if ((bytes >= 0) && (bytes < kilobyte)) 
	    {
	        return bytes + " B";
	    }
	    else if ((bytes >= kilobyte) && (bytes < megabyte)) 
	    {
	        return (bytes / kilobyte) + " KB";
	    }
	    else if ((bytes >= megabyte) && (bytes < gigabyte)) 
	    {
	        return (bytes / megabyte) + " MB";
	    } 
	    else if ((bytes >= gigabyte) && (bytes < terabyte))
	    {
	        return (bytes / gigabyte) + " GB";
	    }
	    else if (bytes >= terabyte) 
	    {
	        return (bytes / terabyte) + " TB";
	    } 
	    else 
	    {
	        return bytes + " Bytes";
	    }
	}
	
	
	
	public JsonObject StringToJsonObject(String NormalString)
	{		
		JsonObject obj = new JsonObject();
		
		try
		{
			if(NormalString != null && !NormalString.isEmpty())  
			{
				JsonParser parser = new JsonParser();
				 
				obj = parser.parse(NormalString).getAsJsonObject();
			}
		}
		catch(Exception e)
		{
			obj = new JsonObject();
		}
				
		return obj;	
	}
	
	public JsonArray StringToJsonArray(String Json_String)
	{		
		JsonArray arr = new JsonArray();
		
		try
		{
			if(Json_String != null && !Json_String.isEmpty())  
			{
				JsonParser parser = new JsonParser();
				 
				JsonElement Element = parser.parse(Json_String);
				 
				arr = Element.getAsJsonArray();
			}
		}
		catch(Exception e)
		{
			arr = new JsonArray();
		}

		return arr;	
	}

	public JsonObject XMLToJsonObject(String XMLString)
	{		
		JsonObject obj = new JsonObject();
		
		try
		{
			XMLString = XMLString.replaceAll("\\s*xmlns[^>]*?>", ">");
			XMLString = XMLString.replaceAll("<[^>]*?xml[^>]*?>", "");
			XMLString = XMLString.replaceAll("</[^>]*?:", "</");
			XMLString = XMLString.replaceAll("<[^>]*?:","<");  
			 
			JSONObject json = XML.toJSONObject(XMLString);  
			
			String jsonString = json.toString();  
			
			obj = StringToJsonObject(jsonString);
		}
		catch(Exception e)
		{
			obj = new JsonObject();
		}
	
		return obj;	
	}
	
	public JsonObject XMLToJsonObject2(String xmlString)
	{		
		JsonObject obj = new JsonObject();
		
		try
		{
			//xmlString = xmlString.replaceAll("\\s*xmlns[^>]*?>", ">");
           // xmlString = xmlString.replaceAll("<[^>]*?xml[^>]*?>", "");
           // xmlString = xmlString.replaceAll("</[^>]*?:", "</");
           // xmlString = xmlString.replaceAll("<[^>]*?:","<");  
           // xmlString = xmlString.replaceAll("\\|", "");
            
            // Convert XML String to Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            Document document = builder.parse(input);
            
            // Remove Namespace Prefixes
            removeNamespacePrefixes(document.getDocumentElement());

            // Convert Document to String
            String xmlAsString = convertDocumentToString(document);

            // Convert XML String to JSON
            JSONObject json = XML.toJSONObject(xmlAsString);

            // Output JSON
            String jsonString = json.toString();  
			
			obj = StringToJsonObject(jsonString);
            
	    }
		catch(Exception e)
		{
			System.out.println(e.getLocalizedMessage());
			obj = new JsonObject();
		}
	
		return obj;	
	}
	
	private static void removeNamespacePrefixes(Node node) {
        // Remove namespace prefixes from element and its children
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String localName = element.getLocalName();
            if (localName != null) {
                Node parent = element.getParentNode();
                if (parent != null) {
                    Document doc = element.getOwnerDocument();
                    // Create a new element without namespace
                    Element newElement = doc.createElement(localName);
                    
                    // Copy attributes
                    for (int i = 0; i < element.getAttributes().getLength(); i++) {
                        newElement.setAttribute(
                                element.getAttributes().item(i).getNodeName(),
                                element.getAttributes().item(i).getNodeValue()
                        );
                    }

                    // Copy child nodes
                    while (element.hasChildNodes()) {
                        Node child = element.getFirstChild();
                        element.removeChild(child);
                        newElement.appendChild(child);
                    }

                    // Replace old element with new element
                    parent.replaceChild(newElement, element);

                    // Recursively process child elements
                    for (int i = 0; i < newElement.getChildNodes().getLength(); i++) {
                        removeNamespacePrefixes(newElement.getChildNodes().item(i));
                    }
                }
            }
        }
    }

    private static String convertDocumentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }
    
    public static String escapeXML(String text) {
        if (text == null) {
            return null;
        }

        // Using StringBuilder for efficiency
        StringBuilder escapedText = new StringBuilder(text.length());

        for (char c : text.toCharArray()) {
            switch (c) {
                case '&':
                    escapedText.append("&amp;");
                    break;
                case '<':
                    escapedText.append("&lt;");
                    break;
                case '>':
                    escapedText.append("&gt;");
                    break;
                case '"':
                    escapedText.append("&quot;");
                    break;
                case '\'':
                    escapedText.append("&apos;");
                    break;
                default:
                    escapedText.append(c);
                    break;
            }
        }

        return escapedText.toString();
    }
	
	public String JsonToXML(String Json_String)
	{		
		String xml = "";
		
		try
		{
			JSONObject json = new JSONObject(Json_String);
			
			xml = XML.toString(json);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		return xml;	
	}
	
	public JsonArray SplitStringToJsonArray(String Json_String, String Split_char)
	{		
		JsonArray arr = new JsonArray();
		
		try
		{
			if(Json_String != null && !Json_String.isEmpty())  
			{
				String[] Json_Strings = Json_String.split(Split_char);
				
				for(String Str : Json_Strings)
				{
					arr.add(Str);
				}
			}
		}
		catch(Exception e)
		{
			arr = new JsonArray();
		}

		return arr;	
	}
	
	public String JsonNullRemover(JsonObject details, String Member)
	{		
		String Result = "";
		
		try
		{
			if(!details.get(Member).isJsonNull())  
			{
				Result = details.get(Member).getAsString();
			}
		}
		catch(Exception e)
		{
			e.getStackTrace();
		}
				
		return Result;	
	}
	
	public boolean JsonMemberNullChecker(JsonObject details, String[] arr)
	{		
		boolean Result = false;
		
		try
		{
			int Valid = 0; int Not_Valid = 0;
			
			for(String Member : arr)
			{
				if(details.has(Member) && !details.get(Member).isJsonNull()) 
				{
					Valid++;
				}
				else
				{
					Not_Valid++;
				}
			}
			
			if(Valid !=0 && Not_Valid == 0)
			{
				Result = true;
			}
		}
		catch(Exception e)
		{
			e.getStackTrace();
		}
				
		return Result;	
	}
	
	public JsonArray get_key_vals(JsonObject object)
	{
		JsonArray Pairs = new JsonArray();
		
		try
		{
			String str = object.toString(); 

	        JsonParser parser = new JsonParser();
	        
	        JsonObject jObj = (JsonObject)parser.parse(str);

	        List<String> keys = new ArrayList<String>();
	        
	        for (Entry<String, JsonElement> e : jObj.entrySet()) 
	        {
	            keys.add(e.getKey());
	        }
	        
	        for(Entry<String, JsonElement> entry: jObj.entrySet()) 
			{
	       	 	JsonObject form_datas = new JsonObject();
	       	 
	       	 	form_datas.addProperty("Key", entry.getKey());  
	       	 	form_datas.addProperty("Value", entry.getValue().getAsString());  
	       	 
	       	 	Pairs.add(form_datas);
			}    
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
        return Pairs;
	}
	
	public boolean isJSONValid(String test) 
	{
	    try 
	    {
	        new JSONObject(test);
	    } 
	    catch (JSONException ex) 
	    {
	        try 
	        {
	            new JSONArray(test);
	        } 
	        catch (JSONException ex1) 
	        {
	            return false;
	        }
	    }
	    
	    return true;
	}
	
	
	public String get_pagination_query(int page, int limit)
	{
		limit = limit < 1 ? 1 : limit;
		
		page = page <= 1 ? 0 : (page-1) * limit;
		
		return " limit "+page+" , "+limit;
	}
	
	
	public String Multiparame_sql_statement(String Keys[])
	{
		String out = "";
		 
		for(int i=0;i<Keys.length;i++)
		{
			 out = out+ " "+ Keys[i] + " = ? and "; 
		}
		
		out = out + ",";
		
		out = out.replaceAll("and ," , "");

		return out;
	}
	
	public Object[] Convert_to_object_arrayt(String Values[])
	{
		Object[] obj = new Object[Values.length];
	
		for(int i=0;i<Values.length;i++)
		{
			obj[i] =  Values[i]; 
		}
		
		return obj;
	}
	
	public String Blob_to_string(Blob file)
	{	
		String Res = "";
		
		try 
		{
			 if(file == null) 
			 {
			      return Res;
			 }
			 
			 byte[] bdata = file.getBytes(1, (int) file.length());
			
			 if(bdata == null || bdata.length == 0) 
			 {
			      return Res;
			 }
			
			Res = new String(bdata);
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
 			
 		return Res;	
	}
	
	public String current_Url(HttpServletRequest request)
	{
	    return request.getRequestURL().toString() + request.getQueryString() == null ? "" :  "?" + request.getQueryString() ;
	}
	
	public String getDynamicName(MultipartFile file)
    {
   	  	long Time_Milli = new Date().getTime();
   	  
   	  	String filename =  Time_Milli + "." + FilenameUtils.getExtension(file.getOriginalFilename());
  		  
   	  	return filename;
    }
	 
	 public File ConvertMultipartFileToFile(MultipartFile file) throws IllegalStateException, IOException
     {
		 String File_Name = getDynamicName(file);
		
		 File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+File_Name);
		  
		 file.transferTo(convFile);
		  
		 return convFile;
      }
	 
	 public String TwoDecimals(String Amount)
	 {
		 String Res = "";
		 
		 try
		 {
			 double num = Double.parseDouble(Amount); 
			 
			 DecimalFormat decfor = new DecimalFormat("0.00");  
			 
			 decfor.setRoundingMode(RoundingMode.DOWN);  

			 Res =  decfor.format(num);	 
		 }
		 catch(Exception ex)
		 {
			 ex.getLocalizedMessage();
		 }
		 
		 return Res;
	 }
	 
	 public boolean isAlphanumeric(String str) 
	 {
		    if(isNullOrEmpty(str)) return false;
		    
		    str = str.trim();
		    
	        return str.matches(".*[a-zA-Z0-9].*");
	 }
	 
	 public boolean isNumeric(String str) {
		 
		 	if(isNullOrEmpty(str)) return false;
		 	
		 	str = str.trim();
		 	
	        return str.matches("-?\\d+(\\.\\d+)?");
	    }
	 
	 public boolean isPureText(String str) 
	 {
		 	if(isNullOrEmpty(str)) return false;
		 	
		 	str = str.trim();
		 	
	        return str.matches("[a-zA-Z]+");
	 }
	 
	 public boolean isText(String str) {
		 
		 	if(isNullOrEmpty(str)) return false;
		 	
		 	str = str.trim();
		 	
	        return str.matches("[a-zA-Z\\s]+");
	    }
}
