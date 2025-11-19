package com.hdsoft.common;

public interface Database 
{
	public static final String Database_Driver      =  "oracle.jdbc.driver.OracleDriver";

	public static final String Local_DB             =  "XE"; 
	public static final String Local_DB_User        =  "dv_scb";
	public static final  String Local_DB_Password   =  "login123";
	public static final String Local_Connection_URL =  "jdbc:oracle:thin:@localhost:1521:"+Local_DB;  

	public static final String UAT_DB              =  "pdbd_dvs_rw.uk.standardchartered.com";
	public static final String UAT_DB_User         =  "RTSIS";
	public static final String UAT_DB_Password     =  "Rtsis_123";
	public static final String UAT_Connection_URL  =  "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=uklvaddbs293.uk.standardchartered.com)(PORT=1621))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=pdbd_dvs_rw.uk.standardchartered.com)))";
	
	public static final String Live_DB              =  "pdbp_dvs_rw.tz.standardchartered.com";
	public static final String Live_DB_User         =  "DVS_TZ_RTSIS_ACCT1";
	public static final String Live_DB_Password     =  "qaTAvXW3wFMXa7p0_NJxtvfl4";
	public static final String Live_Connection_URL  =  "jdbc:oracle:thin:@(DESCRIPTION_LIST=(FAILOVER=on)(DESCRIPTION=(CONNECT_TIMEOUT=5)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=3)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=tzlpdprts01.tz.standardchartered.com)(PORT=1621)))(CONNECT_DATA=(SERVICE_NAME=pdbp_dvs_rw.tz.standardchartered.com)))(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=tzlpdsrts01.tz.standardchartered.com)(PORT=1621)))(CONNECT_DATA=(SERVICE_NAME=pdbp_dvs_rw.tz.standardchartered.com))))"; 
		
	public static final String Active_Mode          =  "local"; 
	
	public static final String DB_Name = Active_Mode.contains("local") ? Local_DB: Active_Mode.contains("UAT") ? UAT_DB : Live_DB;
	public static final String DB_User = Active_Mode.contains("local") ? Local_DB_User: Active_Mode.contains("UAT") ? UAT_DB_User : Live_DB_User;
	public static final String DB_Pass = Active_Mode.contains("local") ? Local_DB_Password: Active_Mode.contains("UAT") ? UAT_DB_Password : Live_DB_Password;
	public static final String Connection_URL = Active_Mode.contains("local") ? Local_Connection_URL: Active_Mode.contains("UAT") ? UAT_Connection_URL : Live_Connection_URL;
	
	public boolean ss(String pass);
}