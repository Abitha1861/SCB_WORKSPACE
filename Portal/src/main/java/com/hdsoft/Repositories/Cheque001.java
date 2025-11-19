package com.hdsoft.Repositories;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;

public class Cheque001 
{
    private String suborgcode;
    private String chequeno;
    private Date chequedate;
    private Date aparta_date;
    private Date rta_date;
    private Blob rta_alert;
    private Clob aparta_file;
    private String cheque_type;

    public String getCheque_type() {
		return cheque_type;
	}

	public void setCheque_type(String cheque_type) {
		this.cheque_type = cheque_type;
	}

	// Getters and setters for all fields
    public String getSuborgcode() {
        return suborgcode;
    }

    public void setSuborgcode(String suborgcode) {
        this.suborgcode = suborgcode;
    }

    public String getChequeno() {
        return chequeno;
    }

    public void setChequeno(String chequeno) {
        this.chequeno = chequeno;
    }

    public Date getChequedate() {
        return chequedate;
    }

    public void setChequedate(Date chequedate) {
        this.chequedate = chequedate;
    }

    public Date getAparta_date() {
        return aparta_date;
    }

    public void setAparta_date(Date aparta_date) {
        this.aparta_date = aparta_date;
    }

    public Date getRta_date() {
        return rta_date;
    }

    public void setRta_date(Date rta_date) {
        this.rta_date = rta_date;
    }

    public Blob getRta_alert() {
        return rta_alert;
    }

    public void setRta_alert(Blob rta_alert) {
        this.rta_alert = rta_alert;
    }

    public Clob getAparta_file() {
        return aparta_file;
    }

    public void setAparta_file(Clob aparta_file) {
        this.aparta_file = aparta_file;
    }
}
