package com.toiukha.forum.article.entity;

import com.toiukha.forum.util.Debug;
import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "articlepictures")
public class ArticlePictures implements Serializable{
	
	private static final long serialVersionUID = -3667221507871383864L; //序列化判斷用

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PICID", updatable = false)
	private Integer picId;

	@Column(name = "ARTID")
	private Integer artId;

	@Lob
	@Column(name = "PICTURE", columnDefinition = "LONGTEXT")
	private String picture;

	@Column(name = "PICCRETIME", insertable = false, updatable = false)
	private Date picCreateTime;

	//圖片的MIME類型，例如image/jpeg, image/png等
	private String mimeType;

	public ArticlePictures() {
		super();
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

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public Date getPicCreateTime() {
		return picCreateTime;
	}

	public void setPicCreateTime(Date picCreateTime) {
		this.picCreateTime = picCreateTime;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
