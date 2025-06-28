package com.toiukha.store.model;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;


@Entity
@Table(name = "store")
public class StoreVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column (name= "STOREID", updatable = false)
	private Integer storeId;
	
	@Column (name = "STOREACC", updatable = false)
	@NotEmpty(message = "廠商帳號: 請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$", message = "廠商帳號: 只能是英文字母、數字和_ , 且長度必需在6到10之間")
	private String storeAcc;
	
	
	@Column (name = "STOREPWD")
	@NotEmpty(message = "廠商密碼: 請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{4,10}$", message = "廠商密碼: 只能是英文字母、數字和_ , 且長度必需在4到10之間")
	private String storePwd;
	
	
	@Column (name = "STORENAME")
	@NotEmpty(message = "廠商名稱: 請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$", message = "廠商名稱: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
	private String storeName;
	
	
	@Column(name = "STOREGUI")
	@NotEmpty(message = "統一編號: 請勿空白")
	@Pattern(regexp = "^[0-9]{8}$", message = "統一編號: 請輸入正確的 8 碼數字")
	private String storeGui;
	
	
	@Column(name = "STOREREP")
	@NotEmpty(message = "負責人姓名: 請勿空白")
	@Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z\\s]{2,20}$", message = "負責人姓名: 請輸入 2~20 字的中英文姓名")
	private String storeRep;
	
	
	@Column(name = "STORETEL")
	@NotEmpty(message = "連絡電話: 請勿空白")
	@Pattern(regexp = "^[0-9\\-+#]{6,20}$", message = "連絡電話: 格式不正確，請輸入正確電話號碼")
	private String storeTel;
	
	
	@Column (name= "STOREADDR")
	@NotEmpty(message = "公司地址: 請勿空白")
	@Pattern(regexp = "^[\u4e00-\u9fa50-9\\s\\-巷弄號樓,\\.]{10,200}$", message = "公司地址: 只能包含中文、數字、空白及「- 巷 弄 號 樓 , .」符號，且長度需在10到200字元之間")
	private String storeAddr;
	
	
	@Column(name = "STOREFAX")
	@NotEmpty(message = "傳真號碼: 請勿空白")
	@Pattern(regexp = "^[0-9\\-+#]{6,20}$", message = "傳真號碼: 格式不正確，請輸入有效傳真號碼")
	private String storeFax;
	
	
	@Column (name= "STOREEMAIL")
	@NotEmpty(message = "電子郵件: 請勿空白")
	@Email(message = "電子郵件格式錯誤")
	private String storeEmail;
	
	
	@Column (name= "STOREREGDATE", updatable = false)
	private Timestamp storeRegDate;
	
	
	@Column (name= "STOREUPDATEDAT")
	private Timestamp storeUpdatedAt;
	
	
	@Column (name= "STORESTATUS")
	private Byte storeStatus;
	
	
	@Column (name= "STOREIMG")
	private byte[] storeImg;

	public StoreVO() {
		super();	
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public String getStoreAcc() {
		return storeAcc;
	}

	public void setStoreAcc(String storeAcc) {
		this.storeAcc = storeAcc;
	}

	public String getStorePwd() {
		return storePwd;
	}

	public void setStorePwd(String storePwd) {
		this.storePwd = storePwd;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreGui() {
		return storeGui;
	}

	public void setStoreGui(String storeGui) {
		this.storeGui = storeGui;
	}

	public String getStoreRep() {
		return storeRep;
	}

	public void setStoreRep(String storeRep) {
		this.storeRep = storeRep;
	}

	public String getStoreTel() {
		return storeTel;
	}

	public void setStoreTel(String storeTel) {
		this.storeTel = storeTel;
	}

	public String getStoreAddr() {
		return storeAddr;
	}

	public void setStoreAddr(String storeAddr) {
		this.storeAddr = storeAddr;
	}

	public String getStoreFax() {
		return storeFax;
	}

	public void setStoreFax(String storeFax) {
		this.storeFax = storeFax;
	}

	public String getStoreEmail() {
		return storeEmail;
	}

	public void setStoreEmail(String storeEmail) {
		this.storeEmail = storeEmail;
	}

	public Timestamp getStoreRegDate() {
		return storeRegDate;
	}

	public void setStoreRegDate(Timestamp storeRegDate) {
		this.storeRegDate = storeRegDate;
	}

	public Timestamp getStoreUpdatedAt() {
		return storeUpdatedAt;
	}

	public void setStoreUpdatedAt(Timestamp storeUpdatedAt) {
		this.storeUpdatedAt = storeUpdatedAt;
	}

	public Byte getStoreStatus() {
		return storeStatus;
	}

	public void setStoreStatus(Byte storeStatus) {
		this.storeStatus = storeStatus;
	}

	public byte[] getStoreImg() {
		return storeImg;
	}

	public void setStoreImg(byte[] storeImg) {
		this.storeImg = storeImg;
	}

	
	
}
