package com.toiukha.item.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "item")
public class ItemVO implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ITEMID")
	private Integer itemId;

	@Column(name = "STOREID")
//	@NotNull(message = "廠商編號: 請勿空白")
	private Integer storeId;

	@Column(name = "ITEMNAME")
	@NotEmpty(message = "商品名稱: 請勿空白")
	@Size(min = 1, max = 50, message = "商品名稱長度需介於1到50字元之間")
	private String itemName;

	@Column(name = "STOCKQUANTITY")
	@NotNull(message = "庫存量: 請勿空白")
	@Min(value = 0, message = "庫存量需大於等於0")
	@Max(value = 99999, message = "庫存量需小於等於99999")
	private Integer stockQuantity;


	@Column(name = "ITEMSTATUS")
//	@NotNull(message = "狀態: 請勿空白")
	private Integer itemStatus;

	@Column(name = "CREAT")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Timestamp creAt;

	@Column(name = "UPDAT")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Timestamp updAt;

	@Column(name = "ITEMIMG")
	private byte[] itemImg;

	@Column(name = "ITEMINFO")
	@Size(min = 1, max = 300, message = "商品說明長度需介於1到300字元之間")
	private String itemInfo;

	@Column(name = "REPCOUNT")
	private Integer repCount;
	

	@Min(value = 100, message = "商品價錢需介於100到99999")
	@Max(value = 99999, message = "商品價錢需介於100到99999")
	@NotNull(message = "價格: 請勿空白")
	@Column(name = "ITEMPRICE")
	private Integer itemPrice;
	
	@Column(name ="DISCPRICE")
//	@NotNull(message = "促銷價格: 請勿空白")
	private Integer discPrice;

//	@NotNull(message = "開始時間: 請勿空白")
	@Column(name ="STATIME")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime staTime;
	
//	@NotNull(message = "結束時間: 請勿空白")
	@Column(name="ENDTIME")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime endTime;
	
	public ItemVO() {
		super();
	}

	public ItemVO(Integer itemId, Integer storeId, String itemName, Integer stockQuantity, Integer itemStatus,
			Timestamp creAt, Timestamp updAt, byte[] itemImg, Integer repCount) {
		super();
		this.itemId = itemId;
		this.storeId = storeId;
		this.itemName = itemName;
		this.stockQuantity = stockQuantity;
		this.itemStatus = itemStatus;
		this.creAt = creAt;
		this.updAt = updAt;
		this.itemImg = itemImg;
		this.repCount = repCount;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer stoId) {
		this.storeId = stoId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public Integer getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(Integer itemStatus) {
		this.itemStatus = itemStatus;
	}

	public Timestamp getCreAt() {
		return creAt;
	}

	public void setCreAt(Timestamp creAt) {
		this.creAt = creAt;
	}

	public Timestamp getUpdAt() {
		return updAt;
	}

	public void setUpdAt(Timestamp updAt) {
		this.updAt = updAt;
	}

	public byte[] getItemImg() {
		return itemImg;
	}

	public void setItemImg(byte[] itemImg) {
		this.itemImg = itemImg;
	}

	public String getItemInfo() {
		return itemInfo;
	}

	public void setItemInfo(String itemInfo) {
		this.itemInfo = itemInfo;
	}

	public Integer getRepCount() {
		return repCount;
	}

	public void setRepCount(Integer repCount) {
		this.repCount = repCount;
	}

	public Integer getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(Integer itemPrice) {
		this.itemPrice = itemPrice;
	}

	public Integer getDiscPrice() {
		return discPrice;
	}

	public void setDiscPrice(Integer discPrice) {
		this.discPrice = discPrice;
	}

	public LocalDateTime getStaTime() {
		return staTime;
	}

	public void setStaTime(LocalDateTime staTime) {
		this.staTime = staTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}


	
}