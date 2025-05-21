package com.toiukha.paymentlog.model;

import java.sql.Timestamp;

public class PaymentLogVO implements java.io.Serializable {
	private Integer payId; // 付款記錄編號 (PK)
	private Integer ordId; // 訂單編號 (FK)
	private Integer memId; // 會員編號 (FK)
	private Integer couId; // 優惠券編號 (FK)
	private Integer amoFin; // 促銷後金額 (NOT NULL)

	private Integer amoPaid; // 實際付款金額 (NOT NULL)
	private Timestamp paidAt; // 付款時間 (DATETIME)
	private Integer paySta; // 付款狀態 (TINYINT)

	// Getter and Setter 方法

	public Integer getPayId() {
		return payId;
	}

	public void setPayId(Integer payId) {
		this.payId = payId;
	}

	public Integer getOrdId() {
		return ordId;
	}

	public void setOrdId(Integer ordId) {
		this.ordId = ordId;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getCouId() {
		return couId;
	}

	public void setCouId(Integer couId) {
		this.couId = couId;
	}

	public Integer getAmoFin() {
		return amoFin;
	}

	public void setAmoFin(Integer amoFin) {
		this.amoFin = amoFin;
	}

	public Integer getAmoPaid() {
		return amoPaid;
	}

	public void setAmoPaid(Integer amoPaid) {
		this.amoPaid = amoPaid;
	}

	public Timestamp getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(Timestamp paidAt) {
		this.paidAt = paidAt;
	}

	public Integer getPaySta() {
		return paySta;
	}

	public void setPaySta(Integer paySta) {
		this.paySta = paySta;
	}

}
