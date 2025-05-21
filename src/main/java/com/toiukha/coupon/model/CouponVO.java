package com.toiukha.coupon.model;

import java.sql.Timestamp;

public class CouponVO implements java.io.Serializable {
	private Integer could; // 優惠券活動編號
	private Integer stold; // 廠商編號
	private Timestamp startTime; // 開始時間
	private Timestamp endTime; // 結束時間
	private Integer discValue; // 折扣值
	private String couName; // 優惠券名稱

	public Integer getCould() {
		return could;
	}

	public void setCould(Integer could) {
		this.could = could;
	}

	public Integer getStold() {
		return stold;
	}

	public void setStold(Integer stold) {
		this.stold = stold;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
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
