package com.toiukha.forum.article.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "articlereport")
public class ArticleReport implements Serializable {
	
	private static final long serialVersionUID = -3948224166836453055L; //序列化判斷用

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ARTREPID",nullable = false)
	private Integer artRepId;

	@Column(name = "MEMID",nullable = false)
	private Integer memId;

	@Column(name = "ARTID",nullable = false)
	private Integer artId;

	@Column(name = "REPCAT",nullable = false)
	private String repCat;

	@Column(name = "REPDES")
	private String repDes;

	@Column(name = "REPSTA",nullable = false)
	private Byte repSta;

	@Column(name = "REPTIME",nullable = false)
	private Date repTime;

	@Column(name = "REVTIME")
	private Date revTime;

	@Column(name = "REMARK")
	private String remark;

	public ArticleReport(Integer artRepId, Integer memId, Integer artId, String repCat, String repDes, Byte repSta,
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

	public ArticleReport() {
		super();
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
