package com.toiukha.forum.model;

import java.io.Serializable;
import java.sql.Date;

public class CommentsVO implements Serializable {
	
	
	private static final long serialVersionUID = 1442380844383035473L;  //序列化判斷用
	
	private Integer commId;
	private Byte commCat;
	private Byte commSta;
	private Integer commHol;
	private Integer commArt;
	private Integer commLike;
	private String commCon;
	private Date commCreTime;
	private byte[] commImg;

	public CommentsVO(Integer commId, Byte commCat, Byte commSta, Integer commHol, Integer commArt, Integer commLike,
			String commCon, Date commCreTime, byte[] commImg) {
		super();
		this.commId = commId;
		this.commCat = commCat;
		this.commSta = commSta;
		this.commHol = commHol;
		this.commArt = commArt;
		this.commLike = commLike;
		this.commCon = commCon;
		this.commCreTime = commCreTime;
		this.commImg = commImg;
	}

	public CommentsVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getCommId() {
		return commId;
	}

	public void setCommId(Integer commId) {
		this.commId = commId;
	}

	public Byte getCommCat() {
		return commCat;
	}

	public void setCommCat(Byte commCat) {
		this.commCat = commCat;
	}

	public Byte getCommSta() {
		return commSta;
	}

	public void setCommSta(Byte commSta) {
		this.commSta = commSta;
	}

	public Integer getCommHol() {
		return commHol;
	}

	public void setCommHol(Integer commHol) {
		this.commHol = commHol;
	}

	public Integer getCommArt() {
		return commArt;
	}

	public void setCommArt(Integer commArt) {
		this.commArt = commArt;
	}

	public Integer getCommLike() {
		return commLike;
	}

	public void setCommLike(Integer commLike) {
		this.commLike = commLike;
	}

	public String getCommCon() {
		return commCon;
	}

	public void setCommCon(String commCon) {
		this.commCon = commCon;
	}

	public Date getCommCreTime() {
		return commCreTime;
	}

	public void setCommCreTime(Date commCreTime) {
		this.commCreTime = commCreTime;
	}

	public byte[] getCommImg() {
		return commImg;
	}

	public void setCommImg(byte[] commImg) {
		this.commImg = commImg;
	}

}
