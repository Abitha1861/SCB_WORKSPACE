package com.hdsoft.Repositories;

public class Domain 

{
	private String Suborgcode;
    private String DOMAIN;
    private String APICODE;
    private String API_NAME;
    private String status;
	public String getSuborgcode() {
		return Suborgcode;
	}
	public void setSuborgcode(String suborgcode) {
		Suborgcode = suborgcode;
	}
	public String getDOMAIN() {
		return DOMAIN;
	}
	public void setDOMAIN(String dOMAIN) {
		DOMAIN = dOMAIN;
	}
	public String getAPICODE() {
		return APICODE;
	}
	public void setAPICODE(String aPICODE) {
		APICODE = aPICODE;
	}
	public String getAPI_NAME() {
		return API_NAME;
	}
	public void setAPI_NAME(String aPI_NAME) {
		API_NAME = aPI_NAME;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
    
}
