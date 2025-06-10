package com.toiukha.notification.model;
import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
public class NotificationVO implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "NOTIID", insertable = false, updatable = false)
	private Integer notiId;
	@Column(name = "NOTITITLE", nullable = false, length = 50)
	private String notiTitle;
	@Column(name = "NOTICONT", nullable = false, length = 500)
	private String notiCont;
	@Column(name = "NOTICREATEDAT", insertable = false, updatable = false)
	private Timestamp notiCreatedAt;
	@Column(name = "MEMID", nullable = false)
	private Integer memId;
	@Column(name = "NOTISTATUS", insertable = false)
	private Byte notiStatus;
	@Column(name = "ADMINID", insertable = false)
	private Integer adminId;
	@Column(name = "NOTIUPDATEDAT", insertable = false)
	private Timestamp notiUpdatedAt;
	@Column(name = "NOTISENDAT", nullable = false)
	private Timestamp notiSendAt;
	
	public NotificationVO() {
		super();
	}

	public NotificationVO(Integer notiId, String notiTitle, String notiCont, Integer memId, Byte notiStatus,
			Integer adminId, Timestamp notiUpdatedAt, Timestamp notiSendAt) {
		this.notiId = notiId;
		this.notiTitle = notiTitle;
		this.notiCont = notiCont;
		this.memId = memId;
		this.notiStatus = notiStatus;
		this.adminId = adminId;
		this.notiUpdatedAt = notiUpdatedAt;
		this.notiSendAt = notiSendAt;
	}
	
	

	public NotificationVO(String notiTitle, String notiCont, Integer memId, Integer adminId, Timestamp notiSendAt) {
		this.notiTitle = notiTitle;
		this.notiCont = notiCont;
		this.memId = memId;
		this.adminId = adminId;
		this.notiSendAt = notiSendAt;
	}

	public Integer getNotiId() {
		return notiId;
	}

	public void setNotiId(Integer notiId) {
		this.notiId = notiId;
	}

	public String getNotiTitle() {
		return notiTitle;
	}

	public void setNotiTitle(String notiTitle) {
		this.notiTitle = notiTitle;
	}

	public String getNotiCont() {
		return notiCont;
	}

	public void setNotiCont(String notiCont) {
		this.notiCont = notiCont;
	}

	public Timestamp getNotiCreatedAt() {
		return notiCreatedAt;
	}

	public void setNotiCreatedAt(Timestamp notiCreatedAt) {
		this.notiCreatedAt = notiCreatedAt;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Byte getNotiStatus() {
		return notiStatus;
	}

	public void setNotiStatus(Byte notiStatus) {
		this.notiStatus = notiStatus;
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public Timestamp getNotiUpdatedAt() {
		return notiUpdatedAt;
	}

	public void setNotiUpdatedAt(Timestamp notiUpdatedAt) {
		this.notiUpdatedAt = notiUpdatedAt;
	}

	public Timestamp getNotiSendAt() {
		return notiSendAt;
	}

	public void setNotiSendAt(Timestamp notiSendAt) {
		this.notiSendAt = notiSendAt;
	}
	
}
