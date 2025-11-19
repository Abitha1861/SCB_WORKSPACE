package com.hdsoft.Repositories;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class RTS003 {
	
	public int id;
	public String Report_Type;
	public String suborgcode;
	public String module;
	public String submodule;
	public String servicecd;
	public String no_of_records;
	public String created_by;
	public String created_on;
	public String file;
	private CommonsMultipartFile Attachments[];
	private String source_type;
	public String getSource_type() {
		return source_type;
	}
	public void setSource_type(String source_type) {
		this.source_type = source_type;
	}
	public String getReport_serial() {
		return report_serial;
	}
	public void setReport_serial(String report_serial) {
		this.report_serial = report_serial;
	}
	private String report_serial;
	
	public CommonsMultipartFile[] getAttachments() {
		return Attachments;
	}
	public String getReport_Type() {
		return Report_Type;
	}
	public void setReport_Type(String report_Type) {
		Report_Type = report_Type;
	}
	public void setAttachments(CommonsMultipartFile[] attachments) {
		Attachments = attachments;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSuborgcode() {
		return suborgcode;
	}
	public void setSuborgcode(String suborgcode) {
		this.suborgcode = suborgcode;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getSubmodule() {
		return submodule;
	}
	public void setSubmodule(String submodule) {
		this.submodule = submodule;
	}
	public String getServicecd() {
		return servicecd;
	}
	public void setServicecd(String servicecd) {
		this.servicecd = servicecd;
	}
	public String getNo_of_records() {
		return no_of_records;
	}
	public void setNo_of_records(String no_of_records) {
		this.no_of_records = no_of_records;
	}
	
	public String getCreated_by() {
		return created_by;
	}
	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}
	public String getCreated_on() {
		return created_on;
	}
	public void setCreated_on(String created_on) {
		this.created_on = created_on;
	}
	

}
