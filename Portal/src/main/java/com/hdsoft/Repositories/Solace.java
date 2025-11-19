package com.hdsoft.Repositories;

public class Solace 
{
	private String ConnectionId;
	private String Chcode;
	private String Servcd;
	private String Topic;
	private String Queue;
	private boolean IsEnabled;
	private boolean IsRunning;
	private String StartedDate;
	private String StartedTime;
	
	public String getConnectionId() {
		return ConnectionId;
	}
	public void setConnectionId(String connectionId) {
		ConnectionId = connectionId;
	}
	public String getChcode() {
		return Chcode;
	}
	public void setChcode(String chcode) {
		Chcode = chcode;
	}
	public String getServcd() {
		return Servcd;
	}
	public void setServcd(String servcd) {
		Servcd = servcd;
	}
	public String getTopic() {
		return Topic;
	}
	public void setTopic(String topic) {
		Topic = topic;
	}
	public String getQueue() {
		return Queue;
	}
	public void setQueue(String queue) {
		Queue = queue;
	}
	public boolean isIsEnabled() {
		return IsEnabled;
	}
	public void setIsEnabled(boolean isEnabled) {
		IsEnabled = isEnabled;
	}
	public void setIsEnabled(String isEnabled) {
		IsEnabled = isEnabled != null && isEnabled.equals("0") ? true : false;
	}
	public boolean isIsRunning() {
		return IsRunning;
	}
	public void setIsRunning(boolean isRunning) {
		IsRunning = isRunning;
	}
	public String getStartedDate() {
		return StartedDate;
	}
	public void setStartedDate(String startedDate) {
		StartedDate = startedDate;
	}
	public String getStartedTime() {
		return StartedTime;
	}
	public void setStartedTime(String startedTime) {
		StartedTime = startedTime;
	}
}
