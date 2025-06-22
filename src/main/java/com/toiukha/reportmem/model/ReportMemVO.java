package com.toiukha.reportmem.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reportmem")
public class ReportMemVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "RPTID", insertable = false, updatable = false)
	private Integer rptId; //檢舉單號(PK)
	
	@Column(name = "TGTMEMID", nullable = false, updatable = false)
	private Integer tgtMemId;  //被檢舉人編號(FK)
	
	@Column(name = "RPRID", nullable = false, updatable = false)
	private Integer rprId;    //檢舉人編號(FK)
	
	@Column(name = "RPRDESC", nullable = false, length = 500)
	private String rprDesc;   //檢舉描述
	
	@Column(name = "RPTTIME", insertable = false, updatable = false)
	private Timestamp rptTime; //檢舉時間，系統自動產生
	
	@Column(name = "RPTSTATUS", insertable = false, nullable = false)
	private Byte rptStatus;   // 處理狀態（0:待處理, 1:通過, 2:未通過）
	
	@Column(name = "RPTPROCTIME", insertable = false, updatable = false)
	private Timestamp rptPorcTime;  //處理時間，系統自動產生
	
	@Column(name = "ADMINID", nullable = false, updatable = false)
	private Integer adminId;    //管理員編號(FK)
	
	
	// Getter & Setter
	
	public Integer getRptId() {
		return rptId;
	}
	public void setRptId(Integer rptId) {
		this.rptId = rptId;
	}
	public Integer getTgtMemId() {
		return tgtMemId;
	}
	public void setTgtMemId(Integer tgtMemId) {
		this.tgtMemId = tgtMemId;
	}
	public Integer getRprId() {
		return rprId;
	}
	public void setRprId(Integer rprId) {
		this.rprId = rprId;
	}
	public String getRprDesc() {
		return rprDesc;
	}
	public void setRprDesc(String rprDesc) {
		this.rprDesc = rprDesc;
	}
	public Timestamp getRptTime() {
		return rptTime;
	}
	public void setRptTime(Timestamp rptTime) {
		this.rptTime = rptTime;
	}
	public Byte getRptStatus() {
		return rptStatus;
	}
	public void setRptStatus(Byte rptStatus) {
		this.rptStatus = rptStatus;
	}
	public Timestamp getRptPorcTime() {
		return rptPorcTime;
	}
	public void setRptPorcTime(Timestamp rptPorcTime) {
		this.rptPorcTime = rptPorcTime;
	}
	public Integer getAdminId() {
		return adminId;
	}
	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}
	
	
	

}
