package com.toiukha.forum.article.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "articlecollection")
@IdClass(ArticleCollection.CompositePK.class)
public class ArticleCollection implements Serializable{
	
	private static final long serialVersionUID = 4769605764256291718L;  //序列化判斷用

	@Id
	@Column(name = "MEMID")
	private Integer memId;

	@Id
	@Column(name = "ARTID")
	private Integer artId;

	@Column(name = "COLTIME", nullable = false)
	private Date colTime;

	// 要特別加上對複合主鍵物件的 getter / setter
	public CompositePK getCompositePK() {
		return new CompositePK(memId, artId);
	}

	public void setCompositePK(CompositePK key) {
		this.memId = key.getMemId();
		this.artId = key.getArtId();
	}

	// 複合主鍵物件
	// 需要宣告一個有包含複合主鍵屬性的類別，並一定要實作 java.io.Serializable 介面
	public static class CompositePK implements Serializable {
		private static final long serialVersionUID = 1L;
		private Integer memId;
		private Integer artId;

		public CompositePK() {
			super();
		}

		public CompositePK(Integer memId, Integer artId) {
			this.memId = memId;
			this.artId = artId;
		}

		public Integer getMemId() {
			return memId;
		}

		public Integer getArtId() {
			return artId;
		}

		public void setMemId(Integer memId) {
			this.memId = memId;
		}

		public void setArtId(Integer artId) {
			this.artId = artId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CompositePK that = (CompositePK) o;
			return Objects.equals(memId, that.memId) && Objects.equals(artId, that.artId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(memId, artId);
		}
	}

	public ArticleCollection(Integer memId, Integer artId, Date colTime) {
		super();
		this.memId = memId;
		this.artId = artId;
		this.colTime = colTime;
	}

	public ArticleCollection() {
		super();
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

	public Date getColTime() {
		return colTime;
	}

	public void setColTime(Date colTime) {
		this.colTime = colTime;
	}

}
