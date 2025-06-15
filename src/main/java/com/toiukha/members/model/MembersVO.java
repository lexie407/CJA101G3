package com.toiukha.members.model;
	


import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;


@Entity
@Table(name = "members")
public class MembersVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column (name= "MEMID", updatable = false)
	private Integer memId;
	
	@Column (name = "MEMACC", updatable = false)
	@NotEmpty(message = "會員帳號: 請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$", message = "會員帳號: 只能是英文字母、數字和_ , 且長度必需在6到10之間")
	private String memAcc;
	
	@Column (name = "MEMPWD")
	@NotEmpty(message = "會員密碼: 請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{4,10}$", message = "會員密碼: 只能是英文字母、數字和_ , 且長度必需在4到10之間")
	private String memPwd;
	
	@Column (name = "MEMNAME")
	@NotEmpty(message = "會員姓名: 請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$", message = "會員姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
	private String memName;
	
	@Column (name = "MEMGENDER", length = 1, columnDefinition = "CHAR(1)", updatable = false)
	@NotEmpty(message = "會員性別必填")
	@Pattern(regexp = "M|F", message = "會員性別只能是 M 或 F")
	private String memGender;
	
	@Column (name= "MEMBIRTHDATE", updatable = false)
	@NotNull(message = "生日請勿空白")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	@Past(message = "生日必須是過去的日期")
	private Date memBirthDate;
	
	@Column (name= "MEMMOBILE")
	@NotEmpty(message = "手機號碼: 請勿空白")
	@Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式錯誤，請輸入09開頭共10碼的數字")
	private String memMobile;
	
	@Column (name= "MEMEMAIL")
	@NotEmpty(message = "電子郵件: 請勿空白")
	@Pattern(regexp = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,6}$", message = "電子郵件格式錯誤")
	private String memEmail;
	
	@Column (name= "MEMADDR")
	@NotEmpty(message = "地址: 請勿空白")
	@Pattern(regexp = "^[\u4e00-\u9fa50-9\\s\\-巷弄號樓,\\.]{10,200}$", message = "地址: 只能包含中文、數字、空白及「- 巷 弄 號 樓 , .」符號，且長度需在10到200字元之間")
	private String memAddr;
	
	@Column (name= "MEMREGTIME", updatable = false)
	private Timestamp memRegTime;
	
	@Column (name= "MEMPOINT")
	private Integer memPoint;
	
	@Column (name= "MEMUPDATEDAT")
	private Timestamp memUpdatedAt;
	
	@Column (name= "MEMSTATUS")
	private Byte memStatus;
	
	@Column (name= "MEMAVATAR")
	private byte[] memAvatar;
	
	@Column (name= "MEMUSERNAME")
	@NotEmpty(message = "會員暱稱: 請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,50}$", message = "會員暱稱: 只能是中、英文字母、數字和_ , 且長度必需在2到50之間")
	private String memUsername;
	
	@Column (name= "MEMAVATARFRAME")
	private byte[] memAvatarFrame;
	
	@Column (name= "MEMLOGERRCOUNT")
	private Byte memLogErrCount;
	
	@Column (name= "MEMLOGERRTIME")
	private Timestamp memLogErrTime;
	
	@Column (name= "MEMGROUPAUTH")
	private Byte memGroupAuth;
	
	@Column (name= "MEMGROUPPOINT")
	private Byte memGroupPoint;
	
	@Column (name= "MEMSTOREAUTH")
	private Byte memStoreAuth;
	
	@Column (name= "MEMSTOREPOINT")
	private Byte memStorePoint;

	public MembersVO() {
		super();
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public String getMemAcc() {
		return memAcc;
	}

	public void setMemAcc(String memAcc) {
		this.memAcc = memAcc;
	}

	public String getMemPwd() {
		return memPwd;
	}

	public void setMemPwd(String memPwd) {
		this.memPwd = memPwd;
	}

	public String getMemName() {
		return memName;
	}

	public void setMemName(String memName) {
		this.memName = memName;
	}

	public String getMemGender() {
		return memGender;
	}

	public void setMemGender(String memGender) {
		this.memGender = memGender;
	}

	public Date getMemBirthDate() {
		return memBirthDate;
	}

	public void setMemBirthDate(Date memBirthDate) {
		this.memBirthDate = memBirthDate;
	}

	public String getMemMobile() {
		return memMobile;
	}

	public void setMemMobile(String memMobile) {
		this.memMobile = memMobile;
	}

	public String getMemEmail() {
		return memEmail;
	}

	public void setMemEmail(String memEmail) {
		this.memEmail = memEmail;
	}

	public String getMemAddr() {
		return memAddr;
	}

	public void setMemAddr(String memAddr) {
		this.memAddr = memAddr;
	}

	public Timestamp getMemRegTime() {
		return memRegTime;
	}

	public void setMemRegTime(Timestamp memRegTime) {
		this.memRegTime = memRegTime;
	}

	public Integer getMemPoint() {
		return memPoint;
	}

	public void setMemPoint(Integer memPoint) {
		this.memPoint = memPoint;
	}

	public Timestamp getMemUpdatedAt() {
		return memUpdatedAt;
	}

	public void setMemUpdatedAt(Timestamp memUpdatedAt) {
		this.memUpdatedAt = memUpdatedAt;
	}

	public Byte getMemStatus() {
		return memStatus;
	}

	public void setMemStatus(Byte memStatus) {
		this.memStatus = memStatus;
	}

	public byte[] getMemAvatar() {
		return memAvatar;
	}

	public void setMemAvatar(byte[] memAvatar) {
		this.memAvatar = memAvatar;
	}

	public String getMemUsername() {
		return memUsername;
	}

	public void setMemUsername(String memUsername) {
		this.memUsername = memUsername;
	}

	public byte[] getMemAvatarFrame() {
		return memAvatarFrame;
	}

	public void setMemAvatarFrame(byte[] memAvatarFrame) {
		this.memAvatarFrame = memAvatarFrame;
	}

	public Byte getMemLogErrCount() {
		return memLogErrCount;
	}

	public void setMemLogErrCount(Byte memLogErrCount) {
		this.memLogErrCount = memLogErrCount;
	}

	public Timestamp getMemLogErrTime() {
		return memLogErrTime;
	}

	public void setMemLogErrTime(Timestamp memLogErrTime) {
		this.memLogErrTime = memLogErrTime;
	}

	public Byte getMemGroupAuth() {
		return memGroupAuth;
	}

	public void setMemGroupAuth(Byte memGroupAuth) {
		this.memGroupAuth = memGroupAuth;
	}

	public Byte getMemGroupPoint() {
		return memGroupPoint;
	}

	public void setMemGroupPoint(Byte memGroupPoint) {
		this.memGroupPoint = memGroupPoint;
	}

	public Byte getMemStoreAuth() {
		return memStoreAuth;
	}

	public void setMemStoreAuth(Byte memStoreAuth) {
		this.memStoreAuth = memStoreAuth;
	}

	public Byte getMemStorePoint() {
		return memStorePoint;
	}

	public void setMemStorePoint(Byte memStorePoint) {
		this.memStorePoint = memStorePoint;
	}

	
	
}
