package com.toiukha.articlereport.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "articlereport")
public class ArticlereportVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ARTREPID", insertable=false, updatable=false)
	private Integer artRepId;   // 檢舉編號, PK(NOT NULL)
	
	@Column(name = "MEMID", updatable=false)
    private Integer memId;      // 檢舉人會員編號, FK(NOT NULL)
	
	@Column(name = "ARTID", updatable=false)
    private Integer artId;      // 文章編號, FK(NOT NULL)
	
	@Column(name = "REPCAT", insertable=false, updatable=false)
    private String repCat;      // 檢舉標題, NOT NULL
	
	@Column(name = "REPDES", updatable=false)
    private String repDes;      // 檢舉描述, NOT NULL
	
	@Column(name = "REPSTA", insertable=false)
    private Byte repSta;        // 狀態, (NOT NULL) 0=待處理(預設) 1=檢舉通過 2=檢舉未通過
	
	@Column(name = "REPTIME", insertable=false, updatable=false)
    private Timestamp repTime;  // 檢舉時間, NOT NULL, 預設為目前時間
	
	@Column(name = "REVTIME", insertable=false)
    private Timestamp revTime;  // 審核時間
	
	@Column(name = "REMARK")
    private String remark;      // 審核備註

	public Integer getArtRepId() {
		return artRepId;
	}

	public void setArtRepId(Integer artRepId) {
		this.artRepId = artRepId;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
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

	public Byte getRepSta() {
		return repSta;
	}

	public void setRepSta(Byte repSta) {
		this.repSta = repSta;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
	
}
