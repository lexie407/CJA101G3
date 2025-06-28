package com.toiukha.articlecollection.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "articlecollection")
public class ArticleCollectionVO {
	
	@EmbeddedId
	private ArticleCollectionCompositePrimaryKey id;
	
	@Column(name = "COLTIME", insertable = false, updatable = false)
	private Timestamp colTime;
	
	public ArticleCollectionVO() {
		super();
	}
	
	public ArticleCollectionVO(ArticleCollectionCompositePrimaryKey id) {
		super();
		this.id = id;
	}

	public ArticleCollectionCompositePrimaryKey getId() {
		return id;
	}
	public void setId(ArticleCollectionCompositePrimaryKey id) {
		this.id = id;
	}
	public Timestamp getColTime() {
		return colTime;
	}
	public void setColTime(Timestamp colTime) {
		this.colTime = colTime;
	}
	
}
