package com.toiukha.forum.model;

import java.io.Serializable;
import java.sql.Date;

public class ArticleReportVO implements Serializable {
	
	private static final long serialVersionUID = -3948224166836453055L; //序列化判斷用
	
	private Integer artRepId;
	private Integer memId;
	private Integer artId;
	private String repCat;
	private String repDes;
	private Byte repSta;
	private Date repTime;
	private Date revTime;
	private String remark;

	public ArticleReportVO(Integer artRepId, Integer memId, Integer artId, String repCat, String repDes, Byte repSta,
			Date repTime, Date revTime, String remark) {
		super();
		this.artRepId = artRepId;
		this.memId = memId;
		this.artId = artId;
		this.repCat = repCat;
		this.repDes = repDes;
		this.repSta = repSta;
		this.repTime = repTime;
		this.revTime = revTime;
		this.remark = remark;
	}

	public ArticleReportVO() {
		super();
		// TODO Auto-generated constructor stub
	}

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

	public Date getRepTime() {
		return repTime;
	}

	public void setRepTime(Date repTime) {
		this.repTime = repTime;
	}

	public Date getRevTime() {
		return revTime;
	}

	public void setRevTime(Date revTime) {
		this.revTime = revTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
