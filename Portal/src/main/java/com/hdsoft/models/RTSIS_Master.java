package com.hdsoft.models;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hdsoft.Repositories.Report_Details;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;

@Component
@Controller
public class RTSIS_Master 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	@Autowired
	public Webservice_Modal Ws;
	
	@Autowired
	public Webservice_call_Modal Wsc;
	
	@Autowired
	public RTSIS_API_Modal RAM;
	
	private static final Logger logger = LogManager.getLogger(RTSIS_Master.class);
	
	 @Autowired
	 public Sysconfig sys;
	
	public JsonObject Procedure_call()
	{
		JsonObject Report_Serial = new JsonObject();
		
		Common_Utils utils = new Common_Utils();
				
		final String serialprocedurecall = "{CALL PACK_ASSETS.PROC_LOAN(?,?,?,?,?)}";
		                                            
			
		Map<String, Object> cbsresultMap = Jdbctemplate.call(new CallableStatementCreator() {
	 
				public CallableStatement createCallableStatement(Connection connection) throws SQLException {

					CallableStatement CS = connection.prepareCall(serialprocedurecall);
					    CS.setString(1, "");
						CS.setString(2, "");
						CS.setString(3, "");
						CS.registerOutParameter(4, Types.VARCHAR);
						CS.registerOutParameter(5, Types.VARCHAR); 
	
					return CS;
				}
				}, get_procedurecall());
		
		 String Serial = utils.ReplaceNull(cbsresultMap.get("O_SERIAL"));
		 String Errmsg = utils.ReplaceNull(cbsresultMap.get("O_ERRMSG"));
		 
		 Report_Serial.addProperty("Report_Serial", Serial); 
		 Report_Serial.addProperty("Errmsg", Errmsg); 
		 
		return Report_Serial;
		
	}
	
	@RequestMapping(value = {"/datavision/Test/RTSIS"}, method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String Channel_Configuration(@RequestParam("REPORTSL") String REPORTSL, @RequestParam("API_CD") String API_CD, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception 
    {	 
       JsonObject details = new JsonObject();
        
       details = Construt_data(REPORTSL, API_CD);
        
        RAM.RTSIS_API_CALL(API_CD);
        
        return details.toString();
    }
	
	
	
	public JsonObject Construt_data(String REPORTSL, String API_CD)
	{
		JsonObject details = new JsonObject();
		
		try
		{
			 Common_Utils utils = new Common_Utils();
			 
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
			 
			 sql = "select distinct(COLUMN2) from REPORT002 where SERIAL = ? and COLUMN1=?";
	 			
			 List<String> Parents = Jdbctemplate.queryForList(sql, new Object[] { REPORTSL, "H" }, String.class);
			
			 JsonArray Head_details = new JsonArray();
			 
			 for(String Head : Parents)
			 {
				 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=?";
		 			
				 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "C", Head }, new Report_Mapper());
				 
				 int total_columns = 0;  JsonArray Keys = new JsonArray(); JsonArray values = new JsonArray();
				 
				 for(int i=0; i<Reports.size();i++)
				 {
					 JsonObject columns = new Gson().toJsonTree(Reports.get(i)).getAsJsonObject();
					 
					 for(int j=4; j<=100;j++) 
					 {
						 String Column_value = columns.get("COLUMN"+j).getAsString();
						 
						 if(!utils.isNullOrEmpty(Column_value))  
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
			 
			 JsonObject js = utils.StringToJsonObject(PAYLOAD);
			 
			 List<String> Avl_elements = utils.get_keys_as_list(js);
			 			 
			 for(int i=0; i<Head_details.size(); i++)
			 {
				 JsonObject hdtl = Head_details.get(i).getAsJsonObject();
				 
				 String Head_Name = hdtl.get("Name").getAsString();
				 
				 if(Avl_elements.contains(Head_Name))
				 {				 
					 sql = "select * from REPORT002 where SERIAL = ? and COLUMN1=? and COLUMN2=? order by COLUMN3";
			 			
					 List<Report_Details> Reports = Jdbctemplate.query(sql, new Object[] { REPORTSL, "D", Head_Name }, new Report_Mapper());
					 
					 JsonArray txn_dtl = new Gson().toJsonTree(Reports).getAsJsonArray();
					 
					 JsonArray Head_Keys = hdtl.get("Keys").getAsJsonArray();
					 JsonArray Head_Values = hdtl.get("Values").getAsJsonArray();
				 				
					 JsonArray hdf = new JsonArray();
					 
					 for(int j=0; j<txn_dtl.size(); j++)
					 {
						 JsonObject dtls = txn_dtl.get(j).getAsJsonObject();
						 
						 if(dtls.get("COLUMN2").getAsString().equalsIgnoreCase(Head_Name))
						 {
							 JsonObject record = new JsonObject();
							 
							 for(int x=0; x<Head_Keys.size(); x++)
							 {
								 if(dtls.has(Head_Keys.get(x).getAsString()))
								 {
									 String key = Head_Values.get(x).getAsString();
									 String value = dtls.get(Head_Keys.get(x).getAsString()).getAsString();
									 
									 record.addProperty(key, value);
								 }
							 }
							 
							 hdf.add(record); 
						 }	 
					 }
					 
					 data.add(Head_Name, hdf);
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
     
     
     public List<SqlParameter> get_procedurecall()
   		{
   			List<SqlParameter> inParamMap = new ArrayList<SqlParameter>();
   		
   			inParamMap.add(new SqlParameter("INFO1" , Types.VARCHAR));
   			inParamMap.add(new SqlParameter("INFO2" , Types.VARCHAR));
   			inParamMap.add(new SqlParameter("INFO3" , Types.VARCHAR));
   			inParamMap.add(new SqlOutParameter("O_SERIAL" , Types.VARCHAR));
   			inParamMap.add(new SqlOutParameter("O_ERRMSG" , Types.VARCHAR));
   			
   			return inParamMap;
   		}
   	
}
