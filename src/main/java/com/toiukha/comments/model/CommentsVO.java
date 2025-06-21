package com.toiukha.comments.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

	@Entity
	@Table(name = "comments")
	public class CommentsVO {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "COMMID", insertable = false, updatable = false)
		private Integer commId;

		@Column(name = "COMMCAT", updatable = false, nullable = false)
		private Byte commCat;

		@Column(name = "COMMSTA", insertable = false)
		private Byte commSta;

		@Column(name = "COMMHOL", updatable = false, nullable = false)
		private Integer commHol;

		@Column(name = "COMMART", updatable = false, nullable = false)
		private Integer commArt;

		@Column(name = "COMMLIKE", insertable = false, nullable = false)
		private Integer commLike;

		@Column(name = "COMMCON", nullable = false, length = 150)
		private String commCon;

		@Column(name = "COMMCRETIME", insertable = false, updatable = false)
		private Timestamp commCreTime;

		@Column(name = "COMMIMG")
		private byte[] commImg;

		public CommentsVO() {
			super();
		}

		// 新增用
		public CommentsVO(Byte commCat, Integer commHol, Integer commArt, String commCon, byte[] commImg) {
			super();
			this.commCat = commCat;
			this.commHol = commHol;
			this.commArt = commArt;
			this.commCon = commCon;
			this.commImg = commImg;
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

		public Timestamp getCommCreTime() {
			return commCreTime;
		}

		public void setCommCreTime(Timestamp commCreTime) {
			this.commCreTime = commCreTime;
		}

		public byte[] getCommImg() {
			return commImg;
		}

		public void setCommImg(byte[] commImg) {
			this.commImg = commImg;
		}

	}

