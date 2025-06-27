package com.toiukha.like.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "likelog")
public class LikeVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LIKEID", insertable = false, updatable = false)
	private Integer likeId;    // 按讚編號 (主鍵 PK, NOT NULL)
	
	@Column(name = "MEMID", nullable = false, updatable = false)
    private Integer memId;      // 會員編號 (外來鍵 FK, NOT NULL)
	
	@Column(name = "DOCTYPE", nullable = false, updatable = false)
    private Byte docType;     // 按讚類型 (0:文章 1:留言, NOT NULL)
	
	@Column(name = "PARDOCID", updatable = false)
    private Integer parDocId;     // 父文件編號
	
	@Column(name = "DOCID", nullable = false, updatable = false)
    private Integer docId;      // 文件編號 (NOT NULL)
	
	@Column(name = "LIKETIME", nullable = false, insertable = false)
    private Timestamp likeTime; // 按讚時間 (NOT NULL, 預設為目前時間)
    
	@Column(name = "LIKESTA", nullable = false)
    private Byte likeSta;   // 按讚狀態 (0:未按讚 1:按讚, NOT NULL)
	
	public LikeVO() {
		super();
	}
	
	public Integer getLikeId() {
		return likeId;
	}
	public void setLikeId(Integer likeId) {
		this.likeId = likeId;
	}
	public Integer getMemId() {
		return memId;
	}
	public void setMemId(Integer memId) {
		this.memId = memId;
	}
	public Byte getDocType() {
		return docType;
	}
	public void setDocType(Byte docType) {
		this.docType = docType;
	}
	public Integer getParDocId() {
		return parDocId;
	}
	public void setParDocId(Integer parDocId) {
		this.parDocId = parDocId;
	}
	public Integer getDocId() {
		return docId;
	}
	public void setDocId(Integer docId) {
		this.docId = docId;
	}
	public Timestamp getLikeTime() {
		return likeTime;
	}
	public void setLikeTime(Timestamp likeTime) {
		this.likeTime = likeTime;
	}
	public Byte getLikeSta() {
		return likeSta;
	}
	public void setLikeSta(Byte likeSta) {
		this.likeSta = likeSta;
	}
	
}
