package com.toiukha.coupon.model;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="COUPON")
public class CouponVO implements Serializable{
	
//	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="COUID")
	private Integer couId;
	
	@Column(name="STOREID")
	private Integer storeId;
	
	@Column(name="STARTTIME")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime startTime;
	
	@Column(name="ENDTIME")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime endTime;
	
	@Column(name="DISCVALUE")
	@Min(value = 1, message = "需介於100到99999字元之間")
	@Max(value = 99999, message = "需介於100到99999字元之間")
	@NotNull(message = "折扣: 請勿空白")
	private Integer discValue;
	
	@Column(name="COUNAME")
	@Size(min = 1, max = 50, message = "優惠券需介於1到50字元之間")
	private String couName;
	
	
	public CouponVO() {
		super();
	}


	public CouponVO(Integer couId, Integer storeId, LocalDateTime startTime, LocalDateTime endTime, Integer discValue,
			String couName) {
		super();
		this.couId = couId;
		this.storeId = storeId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.discValue = discValue;
		this.couName = couName;
	}


	public Integer getCouId() {
		return couId;
	}


	public void setCouId(Integer couId) {
		this.couId = couId;
	}


	public Integer getStoreId() {
		return storeId;
	}


	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}


	public LocalDateTime getStartTime() {
		return startTime;
	}


	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}


	public LocalDateTime getEndTime() {
		return endTime;
	}


	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}


	public Integer getDiscValue() {
		return discValue;
	}


	public void setDiscValue(Integer discValue) {
		this.discValue = discValue;
	}


	public String getCouName() {
		return couName;
	}


	public void setCouName(String couName) {
		this.couName = couName;
	}
	
	
}
