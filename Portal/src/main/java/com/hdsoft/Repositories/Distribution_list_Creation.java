package com.hdsoft.Repositories;


public class Distribution_list_Creation 
{
	private String Distribution_Id;
	private String Distribution_Name;
	private String User_Id;
	private String User_Name;
	private String Mobile_No;
	private String Email_Id;
	private String Status;
	private String Seq_no;
	private int Total_list_Size;
	private String[] User_Ids;
	private String[] User_Names;
	private String[] Mobile_Nos;
	private String[] Email_Ids;
	private String[] Statuses;
	private String[] Deleted_list;
	
	public String getDistribution_Id() {
		return Distribution_Id;
	}
	public String getSeq_no() {
		return Seq_no;
	}
	public int getTotal_list_Size() {
		return Total_list_Size;
	}
	public void setTotal_list_Size(int total_list_Size) {
		Total_list_Size = total_list_Size;
	}
	public String[] getDeleted_list() {
		return Deleted_list;
	}
	public void setDeleted_list(String[] deleted_list) {
		Deleted_list = deleted_list;
	}
	public void setSeq_no(String seq_no) {
		Seq_no = seq_no;
	}
	public void setDistribution_Id(String distribution_Id) {
		Distribution_Id = distribution_Id;
	}
	public String getDistribution_Name() {
		return Distribution_Name;
	}
	public void setDistribution_Name(String distribution_Name) {
		Distribution_Name = distribution_Name;
	}
	public String getUser_Id() {
		return User_Id;
	}
	public void setUser_Id(String user_Id) {
		User_Id = user_Id;
	}
	public String getUser_Name() {
		return User_Name;
	}
	public void setUser_Name(String user_Name) {
		User_Name = user_Name;
	}
	public String getMobile_No() {
		return Mobile_No;
	}
	public void setMobile_No(String mobile_No) {
		Mobile_No = mobile_No;
	}
	public String getEmail_Id() {
		return Email_Id;
	}
	public void setEmail_Id(String email_Id) {
		Email_Id = email_Id;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public String[] getUser_Ids() {
		return User_Ids;
	}
	public void setUser_Ids(String[] user_Ids) {
		User_Ids = user_Ids;
	}
	public String[] getUser_Names() {
		return User_Names;
	}
	public void setUser_Names(String[] user_Names) {
		User_Names = user_Names;
	}
	public String[] getMobile_Nos() {
		return Mobile_Nos;
	}
	public void setMobile_Nos(String[] mobile_Nos) {
		Mobile_Nos = mobile_Nos;
	}
	public String[] getEmail_Ids() {
		return Email_Ids;
	}
	public void setEmail_Ids(String[] email_Ids) {
		Email_Ids = email_Ids;
	}
	public String[] getStatuses() {
		return Statuses;
	}
	public void setStatuses(String[] statuses) {
		Statuses = statuses;
	}
}
