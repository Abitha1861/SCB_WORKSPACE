package com.hdsoft.models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.hdsoft.Repositories.FILEIT004;
import com.hdsoft.common.Common_Utils;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@Component
public class BOT_Stitching_Logics 
{
	public JdbcTemplate Jdbctemplate;
	
	@Autowired	
	public void setJdbctemplate(HikariDataSource Datasource) 
	{
		Jdbctemplate = new JdbcTemplate(Datasource);
	}
	
	public BOT_Stitching_Logics(JdbcTemplate Jdbc) 
	{
		Jdbctemplate = Jdbc;
	}
	
	public BOT_Stitching_Logics() { }
	
	private static final Logger logger = LogManager.getLogger(BOT_Stitching_Logics.class);
	
	@RequestMapping(value = {"/Datavision/Stritching-logic/test"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String Test_Service(@RequestBody String MESSAGE, HttpServletRequest request,HttpServletResponse response, HttpSession session) throws IOException 
	{	 
 	 	JsonObject details = new JsonObject();
		  
 	 	Common_Utils util = new Common_Utils();
 	 	
 	 	JsonObject Info = util.StringToJsonObject(MESSAGE);
 	 	
 	    String res = FindElementFromFileIT(Info.get("CHCODE").getAsString(), Info.get("Output_Column").getAsString(), Info.get("Input_name").getAsString(), Info.get("Input_Value").getAsString());
 	 		
 	    details.addProperty("Out", res);
 	    
 	 	return details.toString();
	 }
	
	
	public String FindElementFromFileIT(String CHCODE, String Output_Column, String Input_name, String Input_Value)  
	{ 
		 String Out = "";
		 
		 try
		 {
			 Common_Utils util = new Common_Utils();
			 
			 String Sql = "select * from fileit004 x where CHCODE=? and FIELDNAME=? and PURPOSE = (select PURPOSE from fileit004 y where y.CHCODE = x.CHCODE and y.FIELDNAME = x.FIELDNAME and y.INPUTNAME=?) order by QUERYSL";
				
			 List<FILEIT004> Info = Jdbctemplate.query(Sql, new Object[] { CHCODE, Output_Column, Input_name } , new FileIT004_mapper());
			
			 String Brd_Name = "", Brd_value = "", Default = "";
			 
			 String[] inp_name = Input_name.split("\\|");
			 String[] inp_val = Input_Value.split("\\|");
					 
			 for(int i=0; i<Info.size(); i++)
			 {
				 String sql = Info.get(i).getQUERYSTR();
				 
				 for(int j=0; j<inp_name.length; j++)
				 {
					 if(sql.contains(inp_name[j])) { 
						 sql = sql.replace("~"+inp_name[j]+"~", inp_val[j]);
					 }
				 }
				 
				 if(sql.contains(Brd_Name)) { 
					 sql = sql.replace("~"+Brd_Name+"~", Brd_value);
				 }
				 
				 logger.debug("Query "+Info.get(i).getQUERYSL()+" :: "+sql);
				 
				 List<String> Result = Jdbctemplate.queryForList(sql, String.class);
				 
				 if(Result.size() > 0) 
				 {
					 Brd_Name = Info.get(i).getOUTNAME();
					 Brd_value = Result.get(0);
				 }
				 
				 Default = Info.get(i).getDFT_VAL();
				 
				 logger.debug("Query "+Info.get(i).getQUERYSL()+" out :: "+Brd_value);
			 }
			 
			 if(!util.isNullOrEmpty(Brd_value))
			 {
				 Out = Brd_value;
				 
				 logger.debug("Stitching value for "+Output_Column+" found :: "+Out);
			 }
			 else
			 {
				 Out = Default;
				 
				 logger.debug("Stitching value for "+Output_Column+" not found, hence default to :: "+Default);
			 }
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in FindElementFromFileIT :::: "+e.getLocalizedMessage());
		 }
		
		 return Out;
	}
	

	public String find_Security_encumbarance(String SecurityNumber)
	{
		 String enc_status = "1";
		 
		 try
		 {
			 String sql = "select REMARKS from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype and u.STATUS=? and u.RESPDESC=?)";
			 
			 List<String> Information = Jdbctemplate.queryForList(sql, new Object[] { "GRID1_REPORT", "SUCCESS", "FILE IS READY"}, String.class);
			 
			 if(Information.size() == 0)
			 {
				 logger.debug(">>>>>> GRID1 REPORT is not found on the server <<<<<<<<");
				 
				 return enc_status;
			 }
			 
			 sql = "select REMARKS from fileit001 c where PAYTYPE = ? and REQTIME = (select max(REQTIME) from fileit001 u where u.paytype = c.paytype and u.STATUS=? and u.RESPDESC=?)";
			 
			 List<String> Information2 = Jdbctemplate.queryForList(sql, new Object[] { "CDS_REPORT", "SUCCESS", "FILE IS READY"}, String.class);
			 
			 if(Information2.size() == 0)
			 {
				 logger.debug(">>>>>> CDS REPORT is not found on the server <<<<<<<<");
				 
				 return enc_status;
			 }
			 
			 sql = "select COLUMN8 from report002 where SERIAL=? and COLUMN3=? and COLUMN12=?";
			 
			 List<String> info = Jdbctemplate.queryForList(sql, new Object[] { Information.get(0), "GRID1_REPORT", SecurityNumber }, String.class);
			 
			 String MaturityDate = info.size() > 0 ? info.get(0) : "";
			 
			 sql = "select count(*) from report002 where SERIAL=? and COLUMN3=? and COLUMN6=? and COLUMN8=?";
					 
			 int count = Jdbctemplate.queryForObject(sql, new Object[] { Information2.get(0), "CDS_REPORT", "HELD", MaturityDate }, Integer.class);
			 
			 enc_status = count > 0 ? "1" : "0";
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in Store_CDS_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return enc_status;
	}
	
	public String find_Allowable_Probable_Loss(String Inst_Id)
	{
		 String enc_status = "0";
		 
		 try
		 {
			 
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in Store_CDS_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return enc_status;
	}
	
	public String find_assetClassification(String Inst_Id) //from splice report
	{
		 String enc_status = "1";
		 
		 try
		 {
			 
		 }
		 catch(Exception e)
		 {
			 logger.debug("Exception in Store_CDS_ReportData :::: "+e.getLocalizedMessage());
		 }
		 
		 return enc_status;
	}
	
	private class FileIT004_mapper implements RowMapper<FILEIT004>  
    {
    	Common_Utils util = new Common_Utils();
    	
		public FILEIT004 mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			FILEIT004 Info = new FILEIT004(); 
			   
			Info.setSUBORGCODE(util.ReplaceNull(rs.getString("SUBORGCODE")));
			Info.setCHCODE(util.ReplaceNull(rs.getString("CHCODE")));
			Info.setPURPOSE(util.ReplaceNull(rs.getString("PURPOSE")));
			Info.setFIELDNAME(util.ReplaceNull(rs.getString("FIELDNAME")));
			Info.setINPUTNAME(util.ReplaceNull(rs.getString("INPUTNAME")));
			Info.setQUERYSL(util.ReplaceNull(rs.getString("QUERYSL")));
			Info.setQUERYSTR(util.ReplaceNull(rs.getString("QUERYSTR")));
			Info.setOUTNAME(util.ReplaceNull(rs.getString("OUTNAME")));
			Info.setDFT_VAL(util.ReplaceNull(rs.getString("DFT_VAL")));
			
			return Info;
		}
    }
}
