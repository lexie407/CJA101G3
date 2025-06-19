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
	
	private Integer adId;
    private StoreVO storeVO; // <<-- 改為 StoreVO 物件
	private String adTitle;
	private byte[] adImage;
	private Byte adStatus;	// TINYINT 對應 Java 的 Byte
	private Timestamp adCreatedTime;
	private Timestamp adStartTime;
	private Timestamp adEndTime;
	
	public AdVO() {	//必需有一個不傳參數建構子(JavaBean基本知識)
	}
	
	public AdVO(Integer adId, StoreVO storeVO, String adTitle, byte[] adImage, Byte adStatus, Timestamp adCreatedTime,
			Timestamp adStartTime, Timestamp adEndTime) {
		this.adId = adId;
        this.storeVO = storeVO;
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
	
    @ManyToOne
    @JoinColumn(name = "STOREID", referencedColumnName = "STOREID") // 假設 StoreVO 的主鍵欄位也是 STOREID
    @NotNull(message = "商店: 請勿空白") // STORE 不可為空
	public StoreVO getStoreVO() {
		return storeVO;
	}

	public void setStoreVO(StoreVO storeVO) {
		this.storeVO = storeVO;
	}

	@Column(name = "ADTITLE")
	@NotEmpty(message="廣告標題: 請勿空白")
    @Size(min = 2, max = 100, message = "廣告標題: 長度必需在{min}到{max}之間")
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
    @Min(value = 0, message = "廣告狀態: 數值需為有效範圍 (例如 0 或 1)")	// 假設 TINYINT 代表布林或狀態碼
	public Byte getAdStatus() {
		return adStatus;
	}

	public void setAdStatus(Byte adStatus) {
		this.adStatus = adStatus;
	}

    @Column(name = "ADCREATEDTIME")
    @NotNull(message = "廣告創建時間: 請勿空白")
    @PastOrPresent(message = "廣告創建時間: 必須是今日或之前") // 通常創建時間不能是未來
	public Timestamp getAdCreatedTime() {
		return adCreatedTime;
	}

	public void setAdCreatedTime(Timestamp adCreatedTime) {
		this.adCreatedTime = adCreatedTime;
	}

	@Column(name = "ADSTARTTIME")
    @NotNull(message = "廣告開始時間: 請勿空白") // 不可為空
    @FutureOrPresent(message = "廣告開始時間: 必須是今日或之後") // 廣告開始時間通常是未來或現在
	public Timestamp getAdStartTime() {
		return adStartTime;
	}

	public void setAdStartTime(Timestamp adStartTime) {
		this.adStartTime = adStartTime;
	}

	@Column(name = "ADENDTIME")
    @NotNull(message = "廣告結束時間: 請勿空白") // 不可為空
    @Future(message = "廣告結束時間: 必須是以後") // 廣告結束時間通常是未來
	public Timestamp getAdEndTime() {
		return adEndTime;
	}

	public void setAdEndTime(Timestamp adEndTime) {
		this.adEndTime = adEndTime;
	}	
	
}
	
	
	

