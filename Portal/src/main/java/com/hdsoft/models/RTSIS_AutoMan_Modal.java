package com.hdsoft.models;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;
import com.hdsoft.common.Database;
import com.hdsoft.Repositories.RTS005;
import com.hdsoft.Repositories.Report_Details;
import com.hdsoft.Repositories.Request_001;
import com.hdsoft.Repositories.Response_001;
import com.zaxxer.hikari.HikariDataSource;
import java.time.LocalDate;
import java.util.Base64;


@Component
public class RTSIS_AutoMan_Modal implements Database
{
	protected JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	private static final Logger logger = LogManager.getLogger(RTSIS_AutoMan_Modal.class);
	
	@Autowired
	public Sysconfig sys;
	
	@Autowired
	public Webservice_call_Modal Wsc;
	
	@Autowired
	public Webservice_Modal Ws;
	
	@Autowired
	public RTSIS_API_Modal RTSIS;
	
	@Autowired
	public Request_Modal Req_Modal; 
	
	@Autowired
	public Response_Modal Res_Modal;
	
	public JsonObject Request_Dispatcher(String BatchId, String REPORTSL, String SERVICD)  
	{
		JsonObject details = new JsonObject();
		
		try
		{
			Common_Utils utils = new Common_Utils();
			
			String SUBORG = sys.getSuborgcode();
			String SYSCODE = sys.getSyscode();
			String PAYTYPE = "RTSIS";
			
			String Sql = "select limitsl from RTS004 where apicode = ? and status=?";
			
			int Limit = Jdbctemplate.queryForObject(Sql, new Object[] { SERVICD, "1"}, Integer.class);
			
			String dataCount = "select count(*) from report002 where serial = ? and COLUMN1 = ? and COLUMN2=?";
			
			int Total_dataCount = Jdbctemplate.queryForObject(dataCount, new Object[] { REPORTSL, "H", SERVICD }, Integer.class);
			
			int Total_Records=0;
			
			if(Total_dataCount > 1) {
				
				String sql1 = "select count(*) from report002 w where serial = ? and COLUMN1 = ? and  COLUMN3 = (select COLUMN3 from report002 where serial = w.serial and COLUMN1 = ? and column4 = (select min(column4) from report002 where serial = w.serial and column1 = ?))";
			 
				Total_Records = Jdbctemplate.queryForObject(sql1, new Object[] { REPORTSL, "D", "H", "H" }, Integer.class);
				
				System.out.println("<<<<<<<<<<<<<<<<<<Greater than one>>>>>>>>>>>>>>>>>>>>>>>>>>");
			
			}
			else
			{
			  String sql = "select count(*) from REPORT002 where SERIAL = ? and COLUMN1 = ? and COLUMN2 = ?";
			
			  Total_Records = Jdbctemplate.queryForObject(sql, new Object[] { REPORTSL, "D", SERVICD }, Integer.class);
			  
			  System.out.println("<<<<<<<<<<<<<<<<<<Less than one>>>>>>>>>>>>>>>>>>>>>>>>>>");
			
			}
			
			int limit = Limit;  int total = Total_Records;
			
			int StartSl = 1;   int EndSl = total < limit ? total : limit;
			
			System.out.println("StartSl"+StartSl+" "+"EndSl"+EndSl+" "+"total"+total+" "+"REPORTSL"+REPORTSL);
			
			do
			{
				 if(EndSl == total && StartSl > total) 
				 {
					 break;
				 }
				 
				 String Reqsl = Req_Modal.Generate_Report_Serial().get("Serial").getAsString(); 
				 
				 String Refno = Reqsl;   
				  
				 String sql = "select MTYPE from prop001 where CHCODE=? and MTYPEPARAM=?";
					
				 List<String> result = Jdbctemplate.queryForList(sql, new Object[] { "ENV", new Sysconfig().getHostAddress() } , String.class);
					 
				 String Status = result.size() !=0 ? result.get(0) : "Q";   // get Queue name Q or Q1
				 
				 sql = "Insert into RTS005(suborgcode,syscode,paytype,reqdate,reqsl,refno,batchid,apicode,reportserial,startsl,endsl,status) values (?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				 Jdbctemplate.update(sql, new Object[] { SUBORG, SYSCODE, PAYTYPE, utils.getCurrentDate("dd-MMM-yyyy"), Reqsl , Refno, BatchId, SERVICD, REPORTSL, StartSl, EndSl, Status } );
				 
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
	
	public JsonObject Request_Processer(RTS005 Job) 
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 JsonObject Js = Convert_Table_data(Job.getREPORTSERIAL(), Job.getSTARTSL(), Job.getENDSL(), Job.getAPICODE());
				 
			 if(Js.has("data"))
			 {
				 Js.add("data", getsignature(Js.get("data").getAsJsonObject()));
				 
				 logger.debug(">>>>>>>>>>> Final format Json Data <<<<<<<<<<<<<<<");
				 
				 logger.debug(Js.get("data").getAsJsonObject());
				 
				 String Json_Data = Js.get("data").getAsJsonObject().toString();
				 
				 details = Call_RTSIS_API(Job.getSUBORGCODE(), Job.getPAYTYPE(), Json_Data, Job.getAPICODE(), Job.getREPORTSERIAL(), Job.getSTARTSL(), Job.getENDSL(), Job.getBATCHID(), Job.getREFNO());
			 }
			 else
			 {
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP06");
				 details.addProperty("message", "Issue while converting from table data to Json data"); 
				
				 logger.debug(">>>>>>>>>>> Issue while converting from table data to Json data "+Job.getREPORTSERIAL()+" "+ Job.getSTARTSL() +" "+Job.getENDSL()+ "<<<<<<<<<<<<<<<");
			 }
		}
		catch(Exception e)
		{
			 details.addProperty("result", "failed");
			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", e.getLocalizedMessage());  
			 
			 logger.debug(">>>>>>>>>>> Exception occurs Creating User Journey <<<<<<<<<<<<<<<"+e.getLocalizedMessage());
		}
		
		return details;
	}
	
	public JsonObject Convert_Table_data(String REPORTSL, String STARTSL, String ENDSL, String API_CD)
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();  
			 
			 JsonObject webservice_details = Ws.Get_Webserice_Info(API_CD);
			 
			 String sql = "select PAYLOAD from webservice001 where SERVICECD=?";
			 
			 List<String> Service = Jdbctemplate.queryForList(sql, new Object[] { API_CD }, String.class);

			 if(Service.size() == 0)
			 {
				 logger.debug("Error in Get_Webserice_Info for API ID "+API_CD+" :::: "+webservice_details);
				 	  
				 details.addProperty("result", "failed");
				 details.addProperty("stscode", "HP200");
				 details.addProperty("message", "Webservice details not found for API ID "+API_CD);
				 
				 return details;
			 }
			 
			 JsonObject data = new JsonObject();
			 
			 sql = "select COLUMN3 from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=? order by COLUMN4";
	 			
			 List<String> Parents = Jdbctemplate.queryForList(sql, new Object[] {REPORTSL, "H", API_CD }, String.class);
			 
			 if(Parents.size() > 1)
			 {
				 details = Convert_Table_data_for_Multiparent(REPORTSL, STARTSL, ENDSL, API_CD, Parents);
				 
				 return details;
			 }
			
			 JsonArray Head_details = new JsonArray();
			
			 for(String Head : Parents)
			 {
				 sql = "select * from REPORT002 where SERIAL=? and COLUMN1=? and COLUMN2=? and COLUMN3=?";
		 			
				 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "C", API_CD, Head }, new Report_Mapper());
				 
				 int total_columns = 0;  JsonArray Keys = new JsonArray(); JsonArray values = new JsonArray();
				 
				 for(int i=0; i<Reports.size();i++)
				 {
					 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
					 
					 for(int j=5; j<=100;j++) 
					 {
						 String Column_value = columns.get("COLUMN"+j).getAsString();
						 
						 if(!util.isNullOrEmpty(Column_value))  
						 {  
							 total_columns++;  
							 
							 Keys.add("COLUMN"+j);
							 values.add(Column_value);
						 }				 
					 }						
				 }
				
				 JsonObject Js = new JsonObject();
				 
				 Js.addProperty("Name", Head);
				 Js.addProperty("Size", total_columns);
				 Js.add("Keys", Keys);
				 Js.add("Values", values);
				 
				 Head_details.add(Js);
				 
			 }
			 
			 String PAYLOAD = webservice_details.get("PAYLOAD").getAsString();
			 
			 JsonObject js = util.StringToJsonObject(PAYLOAD);
			
			 List<String> Avl_elements = util.get_keys_as_list(js);
			 
			 for(int i=0; i<Head_details.size(); i++)
			 {
				 JsonObject hdtl = Head_details.get(i).getAsJsonObject();
				 
				 String Head_Name = hdtl.get("Name").getAsString();
				
				 JsonArray hdf = new JsonArray();
				 
				 if(js.has(Head_Name) && js.get(Head_Name).isJsonArray() && js.get(Head_Name).getAsJsonArray().size()!=0 && Avl_elements.contains(Head_Name))
				 {				 
					 JsonArray jsa = js.get(Head_Name).getAsJsonArray();
					 
					 JsonObject Sampay = jsa.get(0).getAsJsonObject();
					 
					 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=? and COLUMN3=? and COLUMN4 between ? and ? order by COLUMN4";
			 			
					 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "D", API_CD, Head_Name, STARTSL, ENDSL }, new Report_Mapper());
					 
					 JsonArray txn_dtl = new Gson().toJsonTree(Reports).getAsJsonArray();
					 
					 JsonArray Head_Keys = hdtl.get("Keys").getAsJsonArray();
					 JsonArray Head_Values = hdtl.get("Values").getAsJsonArray();
				 	
					 for(int j=0; j<txn_dtl.size(); j++)
					 {
						 JsonObject dtls = txn_dtl.get(j).getAsJsonObject();
						 
						 if(dtls.get("COLUMN3").getAsString().equalsIgnoreCase(Head_Name))
						 {
							 JsonObject record = new JsonObject();
							 
							 for(int x=0; x<Head_Keys.size(); x++)
							 {
								 String Head_Key = Head_Keys.get(x).getAsString();
								 
								 if(dtls.has(Head_Key))
								 {
									 String key = Head_Values.get(x).getAsString();
									 
									 String type = Sampay.get(key).getAsString();
									 
									
									 if(dtls.get(Head_Key).getAsString().contains("<null>") || dtls.get(Head_Key).getAsString().contains("NILL"))
									 {
										 record.add(key, null);
									 }
									 else if(type.contains("number"))
									 {
										 if(util.isNullOrEmpty(dtls.get(Head_Key).getAsString()))
										 {
											 record.addProperty(key, "0");
										 }
										 else
										 {
											 record.addProperty(key, dtls.get(Head_Key).getAsNumber());
										 }
									 }
									 else if(type.contains("currency"))
									 {
										 String val = util.TwoDecimals(dtls.get(Head_Key).getAsString());
										 
										 val = util.isNullOrEmpty(val) ? "0" : val;
										 
										 JsonObject tmp = new JsonObject(); 
										 
										 tmp.addProperty("val", val);
										 
										 record.addProperty(key, tmp.get("val").getAsNumber());
									 }
									 else if(type.contains("boolean"))
									 {
										 String val = dtls.get(Head_Key).getAsString();
										 
										 if(val.equalsIgnoreCase("true") ||  val.equals("1"))
										 {
											 record.addProperty(key, true);
										 }
										 else
										 {
											 record.addProperty(key, false);
										 }
									 }
									 else if(type.contains("<null>") || type.toUpperCase().contains("NILL"))
									 {
										 record.add(key, null);
									 }
									 else
									 {										
										 record.addProperty(key, dtls.get(Head_Key).getAsString());
									 }
								 }
							 }
							 
							 hdf.add(record); 
						 }	 
					 }
					 
					 data.add(Head_Name, hdf);
				 }	
			 }
			 
			 for(String Avl_element :  Avl_elements)
			 {
				 if(!data.has(Avl_element) && js.has(Avl_element))
				 {
					 data.addProperty(Avl_element, js.get(Avl_element).getAsString());
				 }
			 }
			 
			 details.add("data", data);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Json Data formed successfully !!");
		}
		catch(Exception ex)
		{
			 details.addProperty("result", "failed");
 			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", ex.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Construt_data :::: "+ex.getLocalizedMessage()); 
		}
		
		return details;
	}
	
	public JsonObject Convert_Table_data_for_Multiparent(String REPORTSL, String STARTSL, String ENDSL, String API_CD, List<String> Parents)
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils util = new Common_Utils();
			 
			 JsonObject webservice_details = Ws.Get_Webserice_Info(API_CD);
			 
			 JsonObject data = new JsonObject();
			 
			 JsonArray Head_details = new JsonArray();
			 
			 String MainParent = Parents.size() > 0 ? Parents.get(0) : "";
			 
			 String sql = "select * from REPORT002 where SERIAL=? and COLUMN1=? and COLUMN2=? and COLUMN3=?";
	 			
			 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "C", API_CD, MainParent }, new Report_Mapper());
			 
			 JsonObject Js = get_Object_prop(Reports, MainParent);
			 
			 //System.out.println("Js"+Js);
			 
			 Head_details.add(Js); 
			
			 String PAYLOAD = webservice_details.get("PAYLOAD").getAsString();
			 
			// System.out.println("Payload"+PAYLOAD);
			 
			 String SIGNPAYLOAD = webservice_details.get("SIGNPAYLOAD").getAsString();
			 
			// System.out.println("SIGNPAYLOAD"+SIGNPAYLOAD);
			 
			 JsonObject primary_js = util.StringToJsonObject(SIGNPAYLOAD); 
			 
			// System.out.println("primary_js"+primary_js);
			 
			 JsonArray Pri_Members = primary_js.get("primary").getAsJsonArray();
			 
			 JsonObject jss = new JsonObject();
			 
			 JsonObject js = util.StringToJsonObject(PAYLOAD);  
			 
			 List<String> Avl_elements = util.get_keys_as_list(js);
			 
			 for(String Avl_element : Avl_elements)  
			 {
				 if(!Avl_element.equalsIgnoreCase("signature"))
				 {
					 jss = js.get(Avl_element).getAsJsonArray().get(0).getAsJsonObject();
					
					 break;
				 }
			 }
			 
			 JsonObject ArrayFamily = new JsonObject();
			 
			 JsonArray Inside_elements = util.find_key_and_types(jss);
			 
			 JsonArray Own_Members = new JsonArray();  JsonArray Array_Members = new JsonArray();
			 
			 for(int j=0; j<Inside_elements.size(); j++)
			 {
				 JsonObject k = Inside_elements.get(j).getAsJsonObject();
				 
				 String Key = k.get("Key").getAsString();
				 String Type = k.get("Type").getAsString();
				 
				 if(Type.equalsIgnoreCase("json_array") || Type.equalsIgnoreCase("json_object")) 
				 {
					 Array_Members.add(Key);
					 
					 if(jss.has(Key) && Type.equalsIgnoreCase("json_array"))
					 {
						 JsonObject arr = jss.get(Key).getAsJsonArray().get(0).getAsJsonObject();
						 
						 ArrayFamily.add(Key, arr);
					 }
				 }
				 else
				 {
					 Own_Members.add(Key);
				 }
			 }
			 
			 for(int i=0; i<Head_details.size(); i++)
			 {
				 JsonObject hdtl = Head_details.get(i).getAsJsonObject();
				 
				 String Head_Name = hdtl.get("Name").getAsString();
				
				// System.out.println("Head_Name is >>>> "+Head_Name);
				 
				 JsonArray hdf = new JsonArray();
				 
				 if(js.has(Head_Name) && js.get(Head_Name).isJsonArray() && js.get(Head_Name).getAsJsonArray().size()!=0 && Avl_elements.contains(Head_Name))
				 {				
					 JsonArray jsa = js.get(Head_Name).getAsJsonArray();
					 
					 JsonObject Sampay = jsa.get(0).getAsJsonObject();
					 
					 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=? and COLUMN3=? and COLUMN4 between ? and ? order by COLUMN4";
			 			
					 Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "D", API_CD, Head_Name, STARTSL, ENDSL }, new Report_Mapper());
					 
					 JsonArray txn_dtl = new Gson().toJsonTree(Reports).getAsJsonArray();
					 
					 JsonArray Head_Keys = hdtl.get("Keys").getAsJsonArray();
					 JsonArray Head_Values = hdtl.get("Values").getAsJsonArray();
				 	
					 for(int j=0; j<txn_dtl.size(); j++)
					 {
						 String Prime_Query = "";
						 
						 JsonObject dtls = txn_dtl.get(j).getAsJsonObject();
						 
						 if(dtls.get("COLUMN3").getAsString().equalsIgnoreCase(Head_Name))
						 {
							 JsonObject record = new JsonObject();
							 
							 for(int x=0; x<Head_Keys.size(); x++)
							 {
								 String Head_Key = Head_Keys.get(x).getAsString();
								 
								 if(dtls.has(Head_Key))
								 {
									 String key = Head_Values.get(x).getAsString();
									 
									 String type = Sampay.get(key).getAsString();
									
									 if(dtls.get(Head_Key).getAsString().contains("<null>") || dtls.get(Head_Key).getAsString().contains("NILL"))
									 {
										 record.add(key, null);
									 }
									 else if(type.contains("number"))
									 {
										 if(util.isNullOrEmpty(dtls.get(Head_Key).getAsString()))
										 {
											 record.addProperty(key, "0");
										 }
										 else
										 {
											 record.addProperty(key, dtls.get(Head_Key).getAsNumber());
										 }
									 }
									 else if(type.contains("currency"))
									 {
										 String val = util.TwoDecimals(dtls.get(Head_Key).getAsString());
										 
										 val = util.isNullOrEmpty(val) ? "0" : val;
										 
										 JsonObject tmp = new JsonObject(); 
										 
										 tmp.addProperty("val", val);
										 
										 record.addProperty(key, tmp.get("val").getAsNumber());
									 }
									 else if(type.contains("boolean"))
									 {
										 String val = dtls.get(Head_Key).getAsString();
										 
										 if(val.equalsIgnoreCase("true") ||  val.equals("1"))
										 {
											 record.addProperty(key, true);
										 }
										 else
										 {
											 record.addProperty(key, false);
										 }
									 }
									 else if(type.contains("<null>") || type.toUpperCase().contains("NILL"))
									 {
										 record.add(key, null);
									 }
									 else
									 {										
										 record.addProperty(key, dtls.get(Head_Key).getAsString());
									 }
									 
									 for(int kk = 0; kk < Pri_Members.size(); kk++)
									 {
										 if(Pri_Members.get(kk).getAsString().equals(key))
										 {
											 Prime_Query = Prime_Query + Head_Key + "='" + dtls.get(Head_Key).getAsString() + "' and ";
										 }
									 }
								 }
								 
								 if(x == Head_Keys.size()-1) 
								 {
									 for(int xx=0; xx<Array_Members.size(); xx++ )
									 {
										 String Array_Member = Array_Members.get(xx).getAsString();
										 
										 sql = "select * from REPORT002 where SERIAL = ? and COLUMN2=? and COLUMN3=? and COLUMN1=? order by COLUMN4";
								 			
										 Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, API_CD, Array_Member, "C" }, new Report_Mapper());
										 
										 JsonObject Member_Prop =  get_Object_prop(Reports, Array_Member);
										 
										 JsonArray SubKeys = Member_Prop.get("Keys").getAsJsonArray();
										 JsonArray SubValues = Member_Prop.get("Values").getAsJsonArray();
										
										 sql = "select * from REPORT002 where SERIAL = ? and COLUMN2=? and COLUMN3=? and "+Prime_Query+" COLUMN1=? order by COLUMN4";
								 			
										 Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, API_CD, Array_Member, "D" }, new Report_Mapper());
										
										 JsonArray sub_dtl = new Gson().toJsonTree(Reports).getAsJsonArray();
										 
										 JsonObject subarr = ArrayFamily.get(Array_Member).getAsJsonObject(); 
										 
										 JsonArray final_members = new JsonArray();
										 
										 for(int z=0; z<sub_dtl.size(); z++)
										 {
											 JsonObject sub_detail = sub_dtl.get(z).getAsJsonObject();
											 
											 JsonObject intrn_members = new JsonObject();
													 
											 for(int y=0; y<SubKeys.size(); y++)
											 {
												 String Member = SubKeys.get(y).getAsString();
												 String MemberVal = SubValues.get(y).getAsString();
												 
												 if(sub_detail.has(Member) && subarr.has(MemberVal))
												 {
													 String type = subarr.get(MemberVal).getAsString();
													 
													 if(sub_detail.get(Member).getAsString().contains("<null>") || sub_detail.get(Member).getAsString().contains("NILL"))
													 {
														 intrn_members.add(MemberVal, null);
													 }
													 else if(type.contains("number"))
													 {
														 if(util.isNullOrEmpty(sub_detail.get(Member).getAsString()))
														 {
															 intrn_members.addProperty(MemberVal, "0");
														 }
														 else
														 {
															 intrn_members.addProperty(MemberVal, sub_detail.get(Member).getAsNumber());
														 } 
													 }
													 else if(type.contains("currency"))
													 {
														 String val = util.TwoDecimals(sub_detail.get(Member).getAsString()); /** Need to integrate **/
														 
														 val = util.isNullOrEmpty(val) ? "0" : val;
														 
														 JsonObject tmp = new JsonObject(); 
														 
														 tmp.addProperty("val", val);
														 
														 intrn_members.addProperty(MemberVal, tmp.get("val").getAsNumber());
													 }
													 else if(type.contains("boolean"))
													 {
														 String val = sub_detail.get(Member).getAsString();
														 
														 if(val.equalsIgnoreCase("true") ||  val.equals("1"))
														 {
															 intrn_members.addProperty(MemberVal, true);
														 }
														 else
														 {
															 intrn_members.addProperty(MemberVal, false);
														 }
													 }
													 else if(type.contains("<null>") || type.toUpperCase().contains("NILL"))
													 {
														 intrn_members.add(MemberVal, null);
													 }
													 else
													 {										
														 intrn_members.addProperty(MemberVal, sub_detail.get(Member).getAsString());
													 }
												 }
											 }
											 
											 final_members.add(intrn_members);
											
										 }
											
										 record.add(Array_Member, final_members);									   
									 }									
								 }
							 }
							 
							 hdf.add(record); 
						 }	 
					 }
					 
					 data.add(Head_Name, hdf);
				 }	
			 }
			 
			 for(String Avl_element :  Avl_elements)
			 {
				 if(!data.has(Avl_element) && js.has(Avl_element))
				 {
					 data.addProperty(Avl_element, js.get(Avl_element).getAsString());
				 }
			 }
			 
			 details.add("data", data);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00");
			 details.addProperty("message", "Json Data formed successfully !!");
		}
		catch(Exception ex)
		{
			 details.addProperty("result", "failed");
 			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", ex.getLocalizedMessage()); 
			 
			 logger.debug("Exception in Construt_data :::: "+ex.getLocalizedMessage()); 
		}
		
		return details;
	}
	
	public JsonObject get_Object_prop(List<Report_Details> Reports, String Parent_Name)
	{
		JsonObject details = new JsonObject();  
		
		try
		{
			Common_Utils util = new Common_Utils();
			
			int total_columns = 0;  JsonArray Keys = new JsonArray(); JsonArray values = new JsonArray();
			 
			 for(int i=0; i<Reports.size();i++)
			 {
				 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
				 
				 for(int j=5; j<=100;j++) 
				 {
					 String Column_value = columns.get("COLUMN"+j).getAsString();
					 
					 if(!util.isNullOrEmpty(Column_value))  
					 {  
						 total_columns++;  
						 
						 Keys.add("COLUMN"+j);
						 values.add(Column_value);
					 }
				 }					
			 }
			 
			 details.addProperty("Name", Parent_Name);
			 details.addProperty("Size", total_columns);
			 details.add("Keys", Keys);
			 details.add("Values", values);
			 
			 details.addProperty("result", "success");
			 details.addProperty("stscode", "HP00"); 
			 details.addProperty("message", "success");  
		}
		catch(Exception ex)
		{
			 details.addProperty("result", "failed");
 			 details.addProperty("stscode", "HP06");
			 details.addProperty("message", ex.getLocalizedMessage()); 
			 
			 logger.debug("Exception in get_Object_prop :::: "+ex.getLocalizedMessage()); 
		}
		
		return details;
		
	}
	
	public JsonObject Call_RTSIS_API(String SUBORGCODE, String PAYTYPE, String Json_data, String API_ID, String REPORTSL, String STARTSL, String ENDSL, String batchid, String Refno)
	{
		JsonObject details = new JsonObject();  
		
		try
		{
			Common_Utils util = new Common_Utils();
			
			JsonObject Token_details = RTSIS.Generate_Token(SUBORGCODE, PAYTYPE, "RTS999"); 
			
			int Response_Code = Token_details.get("Response_Code").getAsInt();

			String token = "";   
			
			if(Response_Code == 200)
			{
				String res = Token_details.get("Response").getAsString();

				JsonObject Response = util.StringToJsonObject(res);

				token = Response.get("access_token").getAsString();
			} 
			else 
			{
				String CurrentDate = util.getCurrentDate("dd-MMM-yyyy");
		        
				String sql = "Insert into RTS006(suborgcode,syscode,paytype,reqdate,reqtime,refno,batchid,apicode,reportserial,startsl,endsl,status,rescode,respdesc) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				Jdbctemplate.update(sql, new Object[] { SUBORGCODE, sys.getSyscode(), PAYTYPE, CurrentDate, util.get_oracle_Timestamp(),  Refno, batchid, API_ID, REPORTSL, STARTSL, ENDSL, "FAILED", "500", "RTSIS token timeout" }); 
			
				String ReqRefID = batchid+ "/" +REPORTSL+"/"+ STARTSL +"/"+ENDSL;
				
				Request_001 Request_001 = new Request_001(SUBORGCODE, PAYTYPE, PAYTYPE, API_ID, "O", ReqRefID, "", "", "",  "", Json_data, "Datavision", "");

				Req_Modal.Insert_Request_001(Request_001);
				
				String Response = Token_details.toString();
				
				Response_001 Response_ = new Response_001(SUBORGCODE, PAYTYPE, PAYTYPE, API_ID, "O", ReqRefID, "", "", "", "", Response, "Datavision", "");

				Res_Modal.Insert_Response_001(Response_);  
				
				logger.debug(PAYTYPE+" token generation failed !!"); 
				
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", PAYTYPE+" token generation failed !!"); 
				
				return details;
			}
			
			JsonObject webservice_details = Ws.Get_Webserice_Info(sys.getSyscode(), PAYTYPE, API_ID);
			
			if(webservice_details.get("Result").getAsString().equals("Failed")) 
			{
				details.addProperty("result", "failed");
				details.addProperty("stscode", "HP06"); 
				details.addProperty("message", "api configurations not found for the service "+API_ID); 
				
				return details;
			}
			
			JsonArray O_Headers = webservice_details.get("Headers").getAsJsonArray();

			String Headers_str = O_Headers.toString();

			Headers_str = Headers_str.replace("~Token~", token);
			Headers_str = Headers_str.replace("~date~", util.getCurrentDateTime());  
			Headers_str = Headers_str.replace("~fspInformationId~", Refno);
			
			O_Headers = util.StringToJsonArray(Headers_str);
			
			if(API_ID.equals("RTS019"))
			{
				String BOTREFNO = getexisting_loanrefno(Json_data);
				
				if(!util.isNullOrEmpty(BOTREFNO))
				{
					JsonObject Header_details = new JsonObject();
					 
					Header_details.addProperty("Key", "informationId");
					Header_details.addProperty("Value", BOTREFNO);
					
					O_Headers.add(Header_details);
				}
			}
			
			if(API_ID.equals("RTS193"))
			{
				String BOTREFNO = getexisting_acrefno(Json_data);
				
				if(!util.isNullOrEmpty(BOTREFNO))
				{
					JsonObject Header_details = new JsonObject();
					 
					Header_details.addProperty("Key", "informationId");
					Header_details.addProperty("Value", BOTREFNO);
					
					O_Headers.add(Header_details);
				}
			}
			
			webservice_details.addProperty("PAYLOAD", Json_data);
			webservice_details.add("Headers", O_Headers);
			
			String ReqBody =  webservice_details.get("PAYLOAD").getAsString();
			String URI =  webservice_details.get("URI").getAsString();
			
			String ReqRefID = batchid+ "/" +REPORTSL+"/"+ STARTSL +"/"+ENDSL;
			
			Request_001 Request_001 = new Request_001(SUBORGCODE, PAYTYPE, PAYTYPE, API_ID, "O", ReqRefID, URI, "", "",  Headers_str, ReqBody, "Datavision", "");

			Req_Modal.Insert_Request_001(Request_001);
			
			JsonObject API_Response = Wsc.Okhttp_Send_Rest_Request(webservice_details); 

			String Response_code = API_Response.get("Response_Code").getAsString();
			
			String Response = API_Response.get("Response").getAsString();
			
			String informationId = "";
			
			if((Response_code.equals("200") || Response_code.equals("201") || Response_code.equals("202")) && Response.contains("rtsisInformation"))
			{
				JsonObject js = util.StringToJsonObject(Response);
				
				JsonObject rtsisInformation = js.get("rtsisInformation").getAsJsonObject();
				
				String informationDescription = rtsisInformation.get("informationDescription").getAsString().toUpperCase();
				informationId = rtsisInformation.get("informationId").getAsString();
			    			
				String sql = "Insert into RTS006(suborgcode,syscode,paytype,reqdate,reqtime,refno,batchid,apicode,reportserial,startsl,endsl,status,rescode,respdesc,botrefno) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				Jdbctemplate.update(sql, new Object[] { SUBORGCODE, sys.getSyscode(), PAYTYPE, util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(), Refno, batchid, API_ID, REPORTSL, STARTSL, ENDSL, "SUCCESS", Response_code, informationDescription, informationId }); 
			}
			else if(Response.contains("rtsisInformation"))
			{
				String informationCode = Response_code; String informationDescription = Response;
				
				JsonObject js = util.StringToJsonObject(Response);
				
				JsonObject rtsisInformation = js.get("rtsisInformation").getAsJsonObject();
				
				informationDescription = rtsisInformation.get("informationDescription").getAsString();
				
				String sql = "Insert into RTS006(suborgcode,syscode,paytype,reqdate,reqtime,refno,batchid,apicode,reportserial,startsl,endsl,status,rescode,respdesc) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				Jdbctemplate.update(sql, new Object[] { SUBORGCODE, sys.getSyscode(), PAYTYPE, util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(),  Refno, batchid, API_ID, REPORTSL, STARTSL, ENDSL, "FAILED", informationCode, informationDescription }); 
			}
			else if(Response_code.equals("500"))
			{
				String sql = "Insert into RTS006(suborgcode,syscode,paytype,reqdate,reqtime,refno,batchid,apicode,reportserial,startsl,endsl,status,rescode,respdesc) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				Jdbctemplate.update(sql, new Object[] { SUBORGCODE, sys.getSyscode(), PAYTYPE, util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(),  Refno, batchid, API_ID, REPORTSL, STARTSL, ENDSL, "FAILED", Response_code, "timeout" }); 
			}
			else
			{
				String sql = "Insert into RTS006(suborgcode,syscode,paytype,reqdate,reqtime,refno,batchid,apicode,reportserial,startsl,endsl,status,rescode,respdesc) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				 
				Jdbctemplate.update(sql, new Object[] { SUBORGCODE, sys.getSyscode(), PAYTYPE, util.getCurrentDate("dd-MMM-yyyy"), util.get_oracle_Timestamp(),  Refno, batchid, API_ID, REPORTSL, STARTSL, ENDSL, "FAILED", Response_code, "failed" }); 
			}
			
			Response_001 Response_ = new Response_001(SUBORGCODE, PAYTYPE, PAYTYPE, API_ID, "O", ReqRefID, URI, "", "", Headers_str, Response, "Datavision", "");

			Res_Modal.Insert_Response_001(Response_);  
			
			if(API_ID.equals("RTS019")) UpdateLoanLog(ReqRefID, informationId);
			
			details.addProperty("result", API_Response.get("Result").getAsString());
			details.addProperty("stscode", Response_Code); 
			details.addProperty("message", Response);  	
		}
		catch(Exception e)
		{
			details.addProperty("result", "failed");
			details.addProperty("stscode", "HP06"); 
			details.addProperty("message", e.getLocalizedMessage());  	
			
			logger.debug("Exception in Handle_Request :::: "+e.getLocalizedMessage()); 
		}
		
		return details;	
	}
	
	public String getexisting_loanrefno(String Payload) 
	{
		String BOTREFNO = "";
		
		 try
		 {
			 String Loannumber = RegexValue(Payload, "\"loanNumber\"\\s*:\\s*\"([^>]*?)\"");
			 
			 String Sql = "select BOTREFNO from LOAN_API_CALL_LOG where LOANNUMBER=? and BOTREFNO is not null order by PUSHEDON desc";
			 
		     List<String> out = Jdbctemplate.queryForList(Sql, new Object[] { Loannumber }, String.class);	  
		     
		     if(out.size() > 0) BOTREFNO = out.get(0);
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in getexisting_loanrefno :::: "+e.getLocalizedMessage());
		 }
		 
		 return BOTREFNO;
	}
	
	public String getexisting_acrefno(String Payload) 
	{
		String BOTREFNO = "";
		
		 try
		 {
			 String Acnumber = RegexValue(Payload, "\"accountNumber\"\\s*:\\s*\"([^>]*?)\"");
			 
			 String Sql = "select BOTREFNO from AC_API_CALL_LOG where ACNUMBER=? and BOTREFNO is not null order by PUSHEDON desc";
			 
		     List<String> out = Jdbctemplate.queryForList(Sql, new Object[] { Acnumber }, String.class);	  
		     
		     if(out.size() > 0) BOTREFNO = out.get(0);
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in getexisting_acrefno :::: "+e.getLocalizedMessage());
		 }
		 
		 return BOTREFNO;
	}
	
	public void UpdateLoanLog(String ReqRefID, String BOTREFNO) //RTS019
	{
		 try
		 {
			 String Sql = "update LOAN_API_CALL_LOG set BOTREFNO=? where REFNO=?";
			 
		     Jdbctemplate.update(Sql, new Object[] { BOTREFNO, ReqRefID });	  
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in UpdateLoanLog :::: "+e.getLocalizedMessage());
		 }
	}
	
	public JsonObject getsignature(JsonObject js)
	{
		JsonObject out = js;

		try
		{
			Common_Utils util = new Common_Utils();
			
			JsonArray keys = util.get_keys_from_Json(js);
			
			String input = "";
			
			for(int i=0; i< keys.size(); i++)
			{
				if(js.get(keys.get(i).getAsString()).isJsonArray())
				{
					input = js.get(keys.get(i).getAsString()).getAsJsonArray().toString();
				}
			}
			
			String sql = "select MTYPEPARAM from prop001 where CHCODE=? and MODULEID=?";
			
			List<String> Key_Path = Jdbctemplate.queryForList(sql, new Object[] { "RTSIS", "BOT_PRIVATE" }, String.class);
			
			if(Key_Path.size() == 0) 
			{
				logger.debug("Exception in getsignature :::: private key path is not availble"); 
				
				return out;
			}
			
			File file = new File(Key_Path.get(0));
			 
			String body = FileUtils.readFileToString(file, "UTF-8");

			Reader reader = new StringReader(body);

			PemReader pemReader = new PemReader(reader);

			PemObject pemObject = pemReader.readPemObject();

			byte[] keyContentAsBytesFromBC = pemObject.getContent();

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyContentAsBytesFromBC);

			PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(spec);

			byte[] data = input.getBytes("UTF8");

			Signature instance = Signature.getInstance("SHA256WithRSA");
			instance.initSign(key);
			instance.update(data);

			byte[] signatureBytes = instance.sign();

			String Sign_data = Base64.getEncoder().encodeToString(signatureBytes);
			
			String out_data = out.toString().replace("~signature~", Sign_data);
			
			out = util.StringToJsonObject(out_data);
			
			logger.debug("Before Encryption ::: " + input);
			
			logger.debug("After Encryption :::" + Sign_data);
		} 
		catch (Exception ex)
		{
			logger.debug("Exception while generating token >>>>"+ex.getLocalizedMessage());
		}

		return out;
	}
	
	public String RegexValue(String input, String regex) 
	{
    	String uuid = "";
    	
		try
		{
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(input);

	        if(matcher.find()) 
	        {
	            uuid = matcher.group(1);
	        } 
	        else 
	        {
	        	logger.debug("string not found in the input string.");
	        }
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in RegexValue  :::: "+e.getLocalizedMessage());
		 }
		
		 return uuid;
	}
	
	private class Report_Mapper implements RowMapper<Report_Details> 
    {
    	Common_Utils util = new Common_Utils(); 
   	
		public Report_Details mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			Report_Details obj = new Report_Details();  

			obj.setCOLUMN1(util.ReplaceNull(rs.getString("COLUMN1")));
			obj.setCOLUMN2(util.ReplaceNull(rs.getString("COLUMN2")));
			obj.setCOLUMN3(util.ReplaceNull(rs.getString("COLUMN3")));
			obj.setCOLUMN4(util.ReplaceNull(rs.getString("COLUMN4")));
			obj.setCOLUMN5(util.ReplaceNull(rs.getString("COLUMN5")));
			obj.setCOLUMN6(util.ReplaceNull(rs.getString("COLUMN6")));
			obj.setCOLUMN7(util.ReplaceNull(rs.getString("COLUMN7")));
			obj.setCOLUMN8(util.ReplaceNull(rs.getString("COLUMN8")));
			obj.setCOLUMN9(util.ReplaceNull(rs.getString("COLUMN9")));
			obj.setCOLUMN10(util.ReplaceNull(rs.getString("COLUMN10")));
			obj.setCOLUMN11(util.ReplaceNull(rs.getString("COLUMN11")));
			obj.setCOLUMN12(util.ReplaceNull(rs.getString("COLUMN12")));
			obj.setCOLUMN13(util.ReplaceNull(rs.getString("COLUMN13")));
			obj.setCOLUMN14(util.ReplaceNull(rs.getString("COLUMN14")));
			obj.setCOLUMN15(util.ReplaceNull(rs.getString("COLUMN15")));
			obj.setCOLUMN16(util.ReplaceNull(rs.getString("COLUMN16")));
			obj.setCOLUMN17(util.ReplaceNull(rs.getString("COLUMN17")));
			obj.setCOLUMN18(util.ReplaceNull(rs.getString("COLUMN18")));
			obj.setCOLUMN19(util.ReplaceNull(rs.getString("COLUMN19")));
			obj.setCOLUMN20(util.ReplaceNull(rs.getString("COLUMN20")));
			obj.setCOLUMN21(util.ReplaceNull(rs.getString("COLUMN21")));
			obj.setCOLUMN22(util.ReplaceNull(rs.getString("COLUMN22")));
			obj.setCOLUMN23(util.ReplaceNull(rs.getString("COLUMN23")));
			obj.setCOLUMN24(util.ReplaceNull(rs.getString("COLUMN24")));
			obj.setCOLUMN25(util.ReplaceNull(rs.getString("COLUMN25")));
			obj.setCOLUMN26(util.ReplaceNull(rs.getString("COLUMN26")));
			obj.setCOLUMN27(util.ReplaceNull(rs.getString("COLUMN27")));
			obj.setCOLUMN28(util.ReplaceNull(rs.getString("COLUMN28")));
			obj.setCOLUMN29(util.ReplaceNull(rs.getString("COLUMN29")));
			obj.setCOLUMN30(util.ReplaceNull(rs.getString("COLUMN30")));
			obj.setCOLUMN31(util.ReplaceNull(rs.getString("COLUMN31")));
			obj.setCOLUMN32(util.ReplaceNull(rs.getString("COLUMN32")));
			obj.setCOLUMN33(util.ReplaceNull(rs.getString("COLUMN33")));
			obj.setCOLUMN34(util.ReplaceNull(rs.getString("COLUMN34")));
			obj.setCOLUMN35(util.ReplaceNull(rs.getString("COLUMN35")));
			obj.setCOLUMN36(util.ReplaceNull(rs.getString("COLUMN36")));
			obj.setCOLUMN37(util.ReplaceNull(rs.getString("COLUMN37")));
			obj.setCOLUMN38(util.ReplaceNull(rs.getString("COLUMN38")));
			obj.setCOLUMN39(util.ReplaceNull(rs.getString("COLUMN39")));
			obj.setCOLUMN40(util.ReplaceNull(rs.getString("COLUMN40")));
			obj.setCOLUMN41(util.ReplaceNull(rs.getString("COLUMN41")));
			obj.setCOLUMN42(util.ReplaceNull(rs.getString("COLUMN42")));
			obj.setCOLUMN43(util.ReplaceNull(rs.getString("COLUMN43")));
			obj.setCOLUMN44(util.ReplaceNull(rs.getString("COLUMN44")));
			obj.setCOLUMN45(util.ReplaceNull(rs.getString("COLUMN45")));
			obj.setCOLUMN46(util.ReplaceNull(rs.getString("COLUMN46")));
			obj.setCOLUMN47(util.ReplaceNull(rs.getString("COLUMN47")));
			obj.setCOLUMN48(util.ReplaceNull(rs.getString("COLUMN48")));
			obj.setCOLUMN49(util.ReplaceNull(rs.getString("COLUMN49")));
			obj.setCOLUMN50(util.ReplaceNull(rs.getString("COLUMN50")));
			obj.setCOLUMN51(util.ReplaceNull(rs.getString("COLUMN51")));
			obj.setCOLUMN52(util.ReplaceNull(rs.getString("COLUMN52")));
			obj.setCOLUMN53(util.ReplaceNull(rs.getString("COLUMN53")));
			obj.setCOLUMN54(util.ReplaceNull(rs.getString("COLUMN54")));
			obj.setCOLUMN55(util.ReplaceNull(rs.getString("COLUMN55")));
			obj.setCOLUMN56(util.ReplaceNull(rs.getString("COLUMN56")));
			obj.setCOLUMN57(util.ReplaceNull(rs.getString("COLUMN57")));
			obj.setCOLUMN58(util.ReplaceNull(rs.getString("COLUMN58")));
			obj.setCOLUMN59(util.ReplaceNull(rs.getString("COLUMN59")));
			obj.setCOLUMN60(util.ReplaceNull(rs.getString("COLUMN60")));
			obj.setCOLUMN61(util.ReplaceNull(rs.getString("COLUMN61")));
			obj.setCOLUMN62(util.ReplaceNull(rs.getString("COLUMN62")));
			obj.setCOLUMN63(util.ReplaceNull(rs.getString("COLUMN63")));
			obj.setCOLUMN64(util.ReplaceNull(rs.getString("COLUMN64")));
			obj.setCOLUMN65(util.ReplaceNull(rs.getString("COLUMN65")));
			obj.setCOLUMN66(util.ReplaceNull(rs.getString("COLUMN66")));
			obj.setCOLUMN67(util.ReplaceNull(rs.getString("COLUMN67")));
			obj.setCOLUMN68(util.ReplaceNull(rs.getString("COLUMN68")));
			obj.setCOLUMN69(util.ReplaceNull(rs.getString("COLUMN69")));
			obj.setCOLUMN70(util.ReplaceNull(rs.getString("COLUMN70")));
			obj.setCOLUMN71(util.ReplaceNull(rs.getString("COLUMN71")));
			obj.setCOLUMN72(util.ReplaceNull(rs.getString("COLUMN72")));
			obj.setCOLUMN73(util.ReplaceNull(rs.getString("COLUMN73")));
			obj.setCOLUMN74(util.ReplaceNull(rs.getString("COLUMN74")));
			obj.setCOLUMN75(util.ReplaceNull(rs.getString("COLUMN75")));
			obj.setCOLUMN76(util.ReplaceNull(rs.getString("COLUMN76")));
			obj.setCOLUMN77(util.ReplaceNull(rs.getString("COLUMN77")));
			obj.setCOLUMN78(util.ReplaceNull(rs.getString("COLUMN78")));
			obj.setCOLUMN79(util.ReplaceNull(rs.getString("COLUMN79")));
			obj.setCOLUMN80(util.ReplaceNull(rs.getString("COLUMN80")));
			obj.setCOLUMN81(util.ReplaceNull(rs.getString("COLUMN81")));
			obj.setCOLUMN82(util.ReplaceNull(rs.getString("COLUMN82")));
			obj.setCOLUMN83(util.ReplaceNull(rs.getString("COLUMN83")));
			obj.setCOLUMN84(util.ReplaceNull(rs.getString("COLUMN84")));
			obj.setCOLUMN85(util.ReplaceNull(rs.getString("COLUMN85")));
			obj.setCOLUMN86(util.ReplaceNull(rs.getString("COLUMN86")));
			obj.setCOLUMN87(util.ReplaceNull(rs.getString("COLUMN87")));
			obj.setCOLUMN88(util.ReplaceNull(rs.getString("COLUMN88")));
			obj.setCOLUMN89(util.ReplaceNull(rs.getString("COLUMN89")));
			obj.setCOLUMN90(util.ReplaceNull(rs.getString("COLUMN90")));
			obj.setCOLUMN91(util.ReplaceNull(rs.getString("COLUMN91")));
			obj.setCOLUMN92(util.ReplaceNull(rs.getString("COLUMN92")));
			obj.setCOLUMN93(util.ReplaceNull(rs.getString("COLUMN93")));
			obj.setCOLUMN94(util.ReplaceNull(rs.getString("COLUMN94")));
			obj.setCOLUMN95(util.ReplaceNull(rs.getString("COLUMN95")));
			obj.setCOLUMN96(util.ReplaceNull(rs.getString("COLUMN96")));
			obj.setCOLUMN97(util.ReplaceNull(rs.getString("COLUMN97")));
			obj.setCOLUMN98(util.ReplaceNull(rs.getString("COLUMN98")));
			obj.setCOLUMN99(util.ReplaceNull(rs.getString("COLUMN99")));
			obj.setCOLUMN100(util.ReplaceNull(rs.getString("COLUMN100")));
			
			return obj;
		}
    }

	@Override
	public boolean ss(String pass) {
		// TODO Auto-generated method stub
		return false;
	}
}
