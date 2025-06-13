package com.toiukha.forum.article.entity;

import java.io.Serializable;

public class ArticlePictures implements Serializable{
	
	private static final long serialVersionUID = -3667221507871383864L; //序列化判斷用
	
	private Integer picId;
	private Integer artId;
	private byte[] picture;

	public ArticlePictures() {
		super();
	}

	public ArticlePictures(Integer picId, Integer artId, byte[] picture) {
		super();
		this.picId = picId;
		this.artId = artId;
		this.picture = picture;
	}

	public Integer getPicId() {
		return picId;
	}

	public void setPicId(Integer picId) {
		this.picId = picId;
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}

}
