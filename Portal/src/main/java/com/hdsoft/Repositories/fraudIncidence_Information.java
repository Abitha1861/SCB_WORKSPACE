package com.hdsoft.Repositories;

public class fraudIncidence_Information 
{
	private String pyid;
    private String pystatusWork;
    private String pxcreateDatetime;
    private String dateofattemp;
    private String productName;
    private String attemptedtransaction;
    private String classification;
    private String queryDetails;
    private String clientLeid;
    private String clientName;
    private String redressAmount;
    private String redresscurrency;
    private String originatingCountryName;
    private String fraudByRootCauseKeyword;
    
    public String getPyid() {
		return pyid;
	}
	public void setPyid(String pyid) {
		this.pyid = pyid;
	}
	public String getAttemptedtransaction() {
		return attemptedtransaction;
	}
	public void setAttemptedtransaction(String attemptedtransaction) {
		this.attemptedtransaction = attemptedtransaction;
	}
	public String getDateofattemp() {
		return dateofattemp;
	}
	public String getRedresscurrency() {
		return redresscurrency;
	}
	public void setRedresscurrency(String redresscurrency) {
		this.redresscurrency = redresscurrency;
	}
	public void setDateofattemp(String dateofattemp) {
		this.dateofattemp = dateofattemp;
	}
	public String getPystatusWork() {
		return pystatusWork;
	}
	public void setPystatusWork(String pystatusWork) {
		this.pystatusWork = pystatusWork;
	}
	public String getPxcreateDatetime() {
		return pxcreateDatetime;
	}
	public void setPxcreateDatetime(String pxcreateDatetime) {
		this.pxcreateDatetime = pxcreateDatetime;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification = classification;
	}
	public String getQueryDetails() {
		return queryDetails;
	}
	public void setQueryDetails(String queryDetails) {
		this.queryDetails = queryDetails;
	}
	public String getClientLeid() {
		return clientLeid;
	}
	public void setClientLeid(String clientLeid) {
		this.clientLeid = clientLeid;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getRedressAmount() {
		return redressAmount;
	}
	public void setRedressAmount(String redressAmount) {
		this.redressAmount = redressAmount;
	}
	public String getOriginatingCountryName() {
		return originatingCountryName;
	}
	public void setOriginatingCountryName(String originatingCountryName) {
		this.originatingCountryName = originatingCountryName;
	}
	public String getFraudByRootCauseKeyword() {
		return fraudByRootCauseKeyword;
	}
	public void setFraudByRootCauseKeyword(String fraudByRootCauseKeyword) {
		this.fraudByRootCauseKeyword = fraudByRootCauseKeyword;
	}
}
