package com.toiukha.commentsReport.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "commentreport")
@Entity
public class CommentsReportVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "COMMREPID")
    private Integer commRepId;

	@Column(name = "MEMID", nullable = false, updatable = false)
	private Integer memId;

	@Column(name = "COMMID", nullable = false, updatable = false)
    private Integer commId;

	@Column(name = "REPCAT", nullable = false, updatable = false)
    private String repCat;

	@Column(name = "REPDES", nullable = false, updatable = false)
    private String repDes;

	@Column(name = "RPTSTA", insertable = false, nullable = false)
    private Byte rptSta;

	@Column(name = "REPTIME", insertable = false, updatable = false)
    private Timestamp repTime; //檢舉時間

	@Column(name = "REVTIME",  insertable = false)
    private Timestamp revTime; //審核時間

	@Column(name = "REMARKS",  insertable = false)
    private String remarks;

	public CommentsReportVO() {
		super();
	}
	
	//新增使用
	public CommentsReportVO(Integer memId, Integer commId, String repCat, String repDes) {
		super();
		this.memId = memId;
		this.commId = commId;
		this.repCat = repCat;
		this.repDes = repDes;
	}
	
	//修改狀態用
	public CommentsReportVO(Integer commRepId, Byte rptSta, Timestamp revTime, String remarks) {
		super();
		this.commRepId = commRepId;
		this.rptSta = rptSta;
		this.revTime = revTime;
		this.remarks = remarks;
	}

	public Integer getCommRepId() {
		return commRepId;
	}


	public void setCommRepId(Integer commRepId) {
		this.commRepId = commRepId;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getCommId() {
		return commId;
	}

	public void setCommId(Integer commId) {
		this.commId = commId;
	}

	public String getRepCat() {
		return repCat;
	}

	public void setRepCat(String repCat) {
		this.repCat = repCat;
	}

	public String getRepDes() {
		return repDes;
	}

	public void setRepDes(String repDes) {
		this.repDes = repDes;
	}

	public Byte getRptSta() {
		return rptSta;
	}

	public void setRptSta(Byte rptSta) {
		this.rptSta = rptSta;
	}

	public Timestamp getRepTime() {
		return repTime;
	}

	public void setRepTime(Timestamp repTime) {
		this.repTime = repTime;
	}

	public Timestamp getRevTime() {
		return revTime;
	}

	public void setRevTime(Timestamp revTime) {
		this.revTime = revTime;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
