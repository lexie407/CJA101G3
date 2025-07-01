package com.toiukha.itnReport.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity	//要加上@Entity才能成為JPA的一個Entity類別
@Table(name = "memreport")
public class ItnRptVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id	//@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@GeneratedValue(strategy = GenerationType.IDENTITY) //@GeneratedValue的generator屬性指定要用哪個generator //【strategy的GenerationType, 有四種值: AUTO, IDENTITY, SEQUENCE, TABLE】 
	@Column(name = "REPID")
	private Integer repId;
	
	@Column(name = "MEMID")
	@NotNull(message="員工編號: 請勿空白")
	private Integer memId;
	
	@Column(name = "ORDID")
	private Integer ordId;
	
	@Column(name="ITEMID")
	private Integer itemId;
	
	@Column(name = "REPREASON")
	@NotEmpty(message="員工職位: 請勿空白")
	@Size(min=1,max=200,message="檢舉理由請提供{min}到{max}字數說明")

	private String repReason;
	
	@Column(name = "REPSTATUS", nullable = false)
	private Byte repStatus;
	
	@Column(name = "REPAT", nullable = false)
	private LocalDateTime repAt;
	
	@Column(name = "REPIMG")
	private byte[] repImg;
	
	@Column(name = "ADMINID")
	private Integer adminId;
	
	@Column(name = "RPTPROCTIME")
	private LocalDateTime rptProcTime;

	
	public ItnRptVO(Integer repId, Integer memId, Integer ordId, Integer itemId, String repReason, Byte repStatus,
			LocalDateTime repAt, byte[] repImg, Integer adminId, LocalDateTime rptProcTime) {
		this.repId = repId;
		this.memId = memId;
		this.ordId = ordId;
		this.itemId = itemId;
		this.repReason = repReason;
		this.repStatus = repStatus;
		this.repAt = repAt;
		this.repImg = repImg;
		this.adminId = adminId;
		this.rptProcTime = rptProcTime;
	}

	public ItnRptVO() {
	}

	
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

	public Byte getRepStatus() {
		return repStatus;
	}

	public void setRepStatus(Byte repStatus) {
		this.repStatus = repStatus;
	}

	public LocalDateTime getRepAt() {
		return repAt;
	}

	public void setRepAt(LocalDateTime repAt) {
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

	public LocalDateTime getRptProcTime() {
		return rptProcTime;
	}

	public void setRptProcTime(LocalDateTime rptProcTime) {
		this.rptProcTime = rptProcTime;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
