package com.toiukha.memreport.model;

import java.sql.Timestamp;

public class MemReportVO implements java.io.Serializable {
	private Integer repId;
	private Integer memId;
	private Integer ordId;
	private Integer itemId;
	private String repReason;
	private Integer repStatus;
	private Timestamp repAt;
	private byte[] repImg;
	private Integer adminId;
	private Timestamp rptProcTime;

	public Integer getRepId() {
		return repId;
	}

	public void setRepId(Integer repId) {
		this.repId = repId;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getOrdId() {
		return ordId;
	}

	public void setOrdId(Integer ordId) {
		this.ordId = ordId;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public String getRepReason() {
		return repReason;
	}

	public void setRepReason(String repReason) {
		this.repReason = repReason;
	}

	public Integer getRepStatus() {
		return repStatus;
	}

	public void setRepStatus(Integer repStatus) {
		this.repStatus = repStatus;
	}

	public Timestamp getRepAt() {
		return repAt;
	}

	public void setRepAt(Timestamp repAt) {
		this.repAt = repAt;
	}

	public byte[] getRepImg() {
		return repImg;
	}

	public void setRepImg(byte[] repImg) {
		this.repImg = repImg;
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public Timestamp getRptProcTime() {
		return rptProcTime;
	}

	public void setRptProcTime(Timestamp rptProcTime) {
		this.rptProcTime = rptProcTime;
	}

}
