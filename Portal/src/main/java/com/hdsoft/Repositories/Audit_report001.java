package com.hdsoft.Repositories;

public class Audit_report001 {
	
	public String getROWNUM() {
		return ROWNUM;
	}

	public void setROWNUM(String rOWNUM) {
		ROWNUM = rOWNUM;
	}

	public String getADMINID() {
		return ADMINID;
	}

	public void setADMINID(String aDMINID) {
		ADMINID = aDMINID;
	}

	public String getUSERBANKID() {
		return USERBANKID;
	}

	public void setUSERBANKID(String uSERBANKID) {
		USERBANKID = uSERBANKID;
	}

	public String getREQTYPE() {
		return REQTYPE;
	}

	public void setREQTYPE(String rEQTYPE) {
		REQTYPE = rEQTYPE;
	}

	public String getATTRIBUTENAME() {
		return ATTRIBUTENAME;
	}

	public void setATTRIBUTENAME(String aTTRIBUTENAME) {
		ATTRIBUTENAME = aTTRIBUTENAME;
	}

	public String getOLDVALUE() {
		return OLDVALUE;
	}

	public void setOLDVALUE(String oLDVALUE) {
		OLDVALUE = oLDVALUE;
	}

	public String getNEWVALUE() {
		return NEWVALUE;
	}

	public void setNEWVALUE(String nEWVALUE) {
		NEWVALUE = nEWVALUE;
	}

	public String getACTIONDATETIME() {
		return ACTIONDATETIME;
	}

	public void setACTIONDATETIME(String aCTIONDATETIME) {
		ACTIONDATETIME = aCTIONDATETIME;
	}

	String ROWNUM,ADMINID, USERBANKID, REQTYPE, ATTRIBUTENAME, OLDVALUE, NEWVALUE, ACTIONDATETIME;

}
