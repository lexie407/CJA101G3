package com.toiukha.forum.points.model;

import java.io.Serializable;
import java.sql.Date;

public class PointChangesVO implements Serializable{
	
	private static final long serialVersionUID = -1020277113653549271L;  //序列化判斷用
	
	private Integer chaId;
	private Date chaTime;
	private Integer memId;
	private Integer amount;
	private String chaDescription;
	private Byte chaCat;

	public PointChangesVO(Integer chaId, Date chaTime, Integer memId, Integer amount, String chaDescription,
			Byte chaCat) {
		super();
		this.chaId = chaId;
		this.chaTime = chaTime;
		this.memId = memId;
		this.amount = amount;
		this.chaDescription = chaDescription;
		this.chaCat = chaCat;
	}

	public PointChangesVO() {
		super();
	}

	public Integer getChaId() {
		return chaId;
	}

	public void setChaId(Integer chaId) {
		this.chaId = chaId;
	}

	public Date getChaTime() {
		return chaTime;
	}

	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getChaDescription() {
		return chaDescription;
	}

	public void setChaDescription(String chaDescription) {
		this.chaDescription = chaDescription;
	}

	public Byte getChaCat() {
		return chaCat;
	}

	public void setChaCat(Byte chaCat) {
		this.chaCat = chaCat;
	}

}
