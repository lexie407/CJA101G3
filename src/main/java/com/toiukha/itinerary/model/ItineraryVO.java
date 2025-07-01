package com.toiukha.itinerary.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "Itinerary")
public class ItineraryVO {

	@Id // 標記這個屬性是主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 設定主鍵的生成策略為資料庫自增（Auto Increment）
    @Column(name = "ITNID", nullable = false) // 對應資料庫欄位 "itnId"，設定為不可為空
    private Integer itnId; // 行程編號 (PK AI)

    @Column(name = "ITNNAME", nullable = false, length = 255) // 對應資料庫欄位 "itnName"，不可為空，長度為255
    private String itnName; // 行程名稱

    @Column(name = "CRTID", nullable = false) // 對應資料庫欄位 "crtId"，不可為空 (FK)
    private Integer crtId; // 建立會員

    @Column(name = "ITNDESC", length = 500) // 對應資料庫欄位 "itnDesc"，長度為500
    private String itnDesc; // 行程描述

    @Column(name = "ISPUBLIC", nullable = false) // 對應資料庫欄位 "isPublic"，不可為空
    private Byte isPublic; // 是否公開 (0=私人, 1=公開)

    @Column(name = "ITNSTATUS", nullable = false) // 對應資料庫欄位 "itnStatus"，不可為空
    private Byte itnStatus; // 行程狀態 (0=下架, 1=上架)
    
    @Column(name = "ITNCREATEDAT", insertable = false, updatable = false)
    private Timestamp ItnCreateDat;
    
    @Column(name = "ITNUPDATEDAT")
    private Timestamp ItnUpdateDat;

	public Integer getItnId() {
		return itnId;
	}

	public void setItnId(Integer itnId) {
		this.itnId = itnId;
	}

	public String getItnName() {
		return itnName;
	}

	public void setItnName(String itnName) {
		this.itnName = itnName;
	}

	public Integer getCrtId() {
		return crtId;
	}

	public void setCrtId(Integer crtId) {
		this.crtId = crtId;
	}

	public String getItnDesc() {
		return itnDesc;
	}

	public void setItnDesc(String itnDesc) {
		this.itnDesc = itnDesc;
	}

	public Byte getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Byte isPublic) {
		this.isPublic = isPublic;
	}

	public Byte getItnStatus() {
		return itnStatus;
	}

	public void setItnStatus(Byte itnStatus) {
		this.itnStatus = itnStatus;
	}

	public Timestamp getItnCreateDat() {
		return ItnCreateDat;
	}

	public void setItnCreateDat(Timestamp itnCreateDat) {
		ItnCreateDat = itnCreateDat;
	}

	public Timestamp getItnUpdateDat() {
		return ItnUpdateDat;
	}

	public void setItnUpdateDat(Timestamp itnUpdateDat) {
		ItnUpdateDat = itnUpdateDat;
	}
    
	
}
