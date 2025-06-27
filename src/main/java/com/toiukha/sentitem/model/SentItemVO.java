package com.toiukha.sentitem.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "sentitem")
public class SentItemVO implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SENTITEMID")
	private Integer sentItemId;

	@Column(name = "ITEMID")
	private Integer itemId;

	@Column(name = "STOREID")
	private Integer storeId;

	@Column(name = "MEMID")
	private Integer memId;

	@Column(name = "ITEMSTATUS")
	private Integer itemStatus;

	@Column(name = "SCOREMEM")
	private Integer scoreMem;

	@Column(name = "CONTENTMEM")
	private String contentMem;

	@Transient
	private String ticketName;

	public SentItemVO() {
		super();
	}

	public SentItemVO(Integer sentItemId, Integer itemId, Integer storeId, Integer memId, Integer itemStatus,
			Integer scoreMem, String contentMem) {
		super();
		this.sentItemId = sentItemId;
		this.itemId = itemId;
		this.storeId = storeId;
		this.memId = memId;
		this.itemStatus = itemStatus;
		this.scoreMem = scoreMem;
		this.contentMem = contentMem;
	}

	public Integer getSentItemId() {
		return sentItemId;
	}

	public void setSentItemId(Integer sentItemId) {
		this.sentItemId = sentItemId;
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

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(Integer itemStatus) {
		this.itemStatus = itemStatus;
	}

	public Integer getScoreMem() {
		return scoreMem;
	}

	public void setScoreMem(Integer scoreMem) {
		this.scoreMem = scoreMem;
	}

	public String getContentMem() {
		return contentMem;
	}

	public void setContentMem(String contentMem) {
		this.contentMem = contentMem;
	}

	public String getTicketName() {
		return ticketName;
	}

	public void setTicketName(String ticketName) {
		this.ticketName = ticketName;
	}

}