package com.toiukha.forum.model;

import java.io.Serializable;
import java.sql.Date;

public class ArticleVO implements Serializable{
	
	private static final long serialVersionUID = -1848561202771945566L; //序列化判斷用
	
	private Integer artId;
	private Byte artCat;
	private Byte artSta;
	private Integer artHol;
	private Integer artLike;
	private String artTitle;
	private String artCon;
	private Date artCreTime;

	public ArticleVO(Integer artId, Byte artCat, Byte artSta, Integer artHol, Integer artLike, String artTitle,
			String artCon, Date artCreTime) {
		super();
		this.artId = artId;
		this.artCat = artCat;
		this.artSta = artSta;
		this.artHol = artHol;
		this.artLike = artLike;
		this.artTitle = artTitle;
		this.artCon = artCon;
		this.artCreTime = artCreTime;
	}

	public ArticleVO() {
		super();
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
	}

	public Byte getArtCat() {
		return artCat;
	}

	public void setArtCat(Byte artCat) {
		this.artCat = artCat;
	}

	public Byte getArtSta() {
		return artSta;
	}

	public void setArtSta(Byte artSta) {
		this.artSta = artSta;
	}

	public Integer getArtHol() {
		return artHol;
	}

	public void setArtHol(Integer artHol) {
		this.artHol = artHol;
	}

	public Integer getArtLike() {
		return artLike;
	}

	public void setArtLike(Integer artLike) {
		this.artLike = artLike;
	}

	public String getArtTitle() {
		return artTitle;
	}

	public void setArtTitle(String artTitle) {
		this.artTitle = artTitle;
	}

	public String getArtCon() {
		return artCon;
	}

	public void setArtCon(String artCon) {
		this.artCon = artCon;
	}

	public Date getArtCreTime() {
		return artCreTime;
	}

	public void setArtCreTime(Date artCreTime) {
		this.artCreTime = artCreTime;
	}

}
