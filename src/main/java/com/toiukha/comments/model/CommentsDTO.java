package com.toiukha.comments.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class CommentsDTO {

	private Integer commId;
	private Byte commCat;
	private Byte commSta;
	private Integer commHol;
	private Integer commArt;
	private Integer commLike;
	private String commCon;
	private Timestamp commCreTime;
	private byte[] commImg;
	private String holName;
	
	// 私有構造函數，鼓勵使用工廠方法
    private CommentsDTO() {}

    // 靜態工廠方法
    public static CommentsDTO from(CommentsVO commentsVO, String holName) {
    	CommentsDTO dto = new CommentsDTO();
    	
    	// 將 CommentsVO 的資料複製到 CommentsDTO
    	if (commentsVO != null) { // 檢查 commentsVO 是否為 null，避免 NullPointerException
            dto.setCommId(commentsVO.getCommId());
            dto.setCommCat(commentsVO.getCommCat());
            dto.setCommSta(commentsVO.getCommSta());
            dto.setCommHol(commentsVO.getCommHol());
            dto.setCommArt(commentsVO.getCommArt());
            dto.setCommLike(commentsVO.getCommLike());
            dto.setCommCon(commentsVO.getCommCon());
            dto.setCommCreTime(commentsVO.getCommCreTime());
            dto.setCommImg(commentsVO.getCommImg());
    	}

    	// 將 holName 設定到 DTO (這是從其他服務取得的額外資料)
        dto.setHolName(holName);
    	
        return dto;
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
	public String getHolName() {
		return holName;
	}
	public void setHolName(String holName) {
		this.holName = holName;
	}
	
}
