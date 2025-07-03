package com.toiukha.advertisment.model;

import java.io.*;
import java.sql.Timestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/*
 * 註1: classpath必須有javax.persistence-api-x.x.jar 
 * 註2: Annotation可以添加在屬性上，也可以添加在getXxx()方法之上
 */

	@Entity	//要加上@Entity才能成為JPA的一個Entity類別
	@Table(name = "advertisment")	//代表這個class是對應到資料庫的實體table，目前對應的table是advertisment 
	public class AdVO implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	// 廣告狀態常數定義
	public static final byte STATUS_PENDING = 0;    // 待審核
	public static final byte STATUS_APPROVED = 1;   // 已審核通過
	public static final byte STATUS_REJECTED = 2;   // 審核拒絕
	public static final byte STATUS_INACTIVE = 3;   // 已停用
	
	private Integer adId;
    private Integer storeId;
	private String adTitle;
	private byte[] adImage;
	private Byte adStatus;	// TINYINT 對應 Java 的 Byte
	private Timestamp adCreatedTime;
	private Timestamp adStartTime;
	private Timestamp adEndTime;
	
	public AdVO() {	//必需有一個不傳參數建構子(JavaBean基本知識)
	}
	
	public AdVO(Integer adId, String adTitle, byte[] adImage, Byte adStatus, Timestamp adCreatedTime,
			Timestamp adStartTime, Timestamp adEndTime) {
		this.adId = adId;
//        this.storeVO = storeVO;
		this.adTitle = adTitle;
		this.adImage = adImage;
		this.adStatus = adStatus;
		this.adCreatedTime = adCreatedTime;
		this.adStartTime = adStartTime;
		this.adEndTime = adEndTime;
	}



	@Id	//@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@Column(name = "ADID")	//@Column指這個屬性是對應到資料庫Table的哪一個欄位   //【非必要，但當欄位名稱與屬性名稱不同時則一定要用】
	@GeneratedValue(strategy = GenerationType.IDENTITY) //@GeneratedValue的generator屬性指定要用哪個generator //【strategy的GenerationType, 有四種值: AUTO, IDENTITY, SEQUENCE, TABLE】 
	public Integer getAdId() {
		return adId;
	}

	public void setAdId(Integer adId) {
		this.adId = adId;
	}
	

    @Column(name = "STOREID") 
    @NotNull(message = "商店: 請勿空白") // STORE 不可為空
	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	@Column(name = "ADTITLE")
	@NotEmpty(message="廣告描述: 請勿空白")
    @Size(min = 10, max = 200, message = "廣告描述: 長度必需在{min}到{max}之間")
	public String getAdTitle() {
		return adTitle;
	}

	public void setAdTitle(String adTitle) {
		this.adTitle = adTitle;
	}

	@Column(name = "ADIMAGE")
    // @NotNull(message = "廣告圖片: 請勿空白")
	public byte[] getAdImage() {
		return adImage;
	}

	public void setAdImage(byte[] adImage) {
		this.adImage = adImage;
	}

	@Column(name = "ADSTATUS")
    @NotNull(message = "廣告狀態: 請勿空白") // 不可為空
    @Min(value = 0, message = "廣告狀態: 數值需為有效範圍 (0-3)")
    @Max(value = 3, message = "廣告狀態: 數值需為有效範圍 (0-3)")
	public Byte getAdStatus() {
		return adStatus;
	}

	public void setAdStatus(Byte adStatus) {
		this.adStatus = adStatus;
	}

    @Column(name = "ADCREATEDTIME")
//    @NotNull(message = "廣告創建時間: 請勿空白")
//    @PastOrPresent(message = "廣告創建時間: 必須是今日或之前") // 暫時註解，避免更新時的驗證問題
	public Timestamp getAdCreatedTime() {
		return adCreatedTime;
	}

	public void setAdCreatedTime(Timestamp adCreatedTime) {
		this.adCreatedTime = adCreatedTime;
	}

	@Column(name = "ADSTARTTIME")
//    @NotNull(message = "廣告開始時間: 請勿空白") // 不可為空
//    @FutureOrPresent(message = "廣告開始時間: 必須是今日或之後") // 暫時註解，避免更新時的驗證問題
	public Timestamp getAdStartTime() {
		return adStartTime;
	}

	public void setAdStartTime(Timestamp adStartTime) {
		this.adStartTime = adStartTime;
	}

	@Column(name = "ADENDTIME")
//    @NotNull(message = "廣告結束時間: 請勿空白") // 不可為空
//    @Future(message = "廣告結束時間: 必須是以後") // 暫時註解，避免更新時的驗證問題
	public Timestamp getAdEndTime() {
		return adEndTime;
	}

	public void setAdEndTime(Timestamp adEndTime) {
		this.adEndTime = adEndTime;
	}	
	
}