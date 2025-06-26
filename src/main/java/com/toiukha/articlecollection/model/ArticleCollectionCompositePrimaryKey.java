package com.toiukha.articlecollection.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ArticleCollectionCompositePrimaryKey implements Serializable {

	@Column(name = "MEMID")
	private Integer memId;
	
	@Column(name = "ARTID")
	private Integer artId;
	
	public ArticleCollectionCompositePrimaryKey() {
		super();
	}
	
	public ArticleCollectionCompositePrimaryKey(Integer memId, Integer artId) {
		super();
		this.memId = memId;
		this.artId = artId;
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
	
	@Override
	public int hashCode() {
		return Objects.hash(artId, memId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArticleCollectionCompositePrimaryKey other = (ArticleCollectionCompositePrimaryKey) obj;
		return Objects.equals(artId, other.artId) && Objects.equals(memId, other.memId);
	}
	
}
