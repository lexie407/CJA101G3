package com.toiukha.forum.model;

import java.io.Serializable;

public class ArticlePicturesVO implements Serializable{
	
	private static final long serialVersionUID = -3667221507871383864L; //序列化判斷用
	
	private Integer picId;
	private Integer artId;
	private byte[] picture;

	public ArticlePicturesVO() {
		super();
	}

	public ArticlePicturesVO(Integer picId, Integer artId, byte[] picture) {
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
