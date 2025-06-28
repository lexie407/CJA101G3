package com.toiukha.paymentlog.model;

import java.sql.Timestamp;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paymentlog")
public class PaymentLogVO implements java.io.Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PAYID")
	private Integer payId; // 付款記錄編號 (PK)

	@Column(name = "ORDID")
	private Integer ordId; // 訂單編號 (FK)

	@Column(name = "MEMID")
	private Integer memId; // 會員編號 (FK)

	@Column(name = "COUID")
	private Integer couId; // 優惠券編號 (FK)

	@Column(name = "AMOFIN")
	private Integer amoFin; // 促銷後金額 (NOT NULL)

	@Column(name = "AMOPAID")
	private Integer amoPaid; // 實際付款金額 (NOT NULL)

	@Column(name = "PAIDAT")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Timestamp paidAt; // 付款時間 (DATETIME)

	@Column(name = "PAYSTA")
	private Integer paySta; // 付款狀態 (TINYINT)

	public PaymentLogVO() {
		super();
	}

	public PaymentLogVO(Integer payId, Integer ordId, Integer memId, Integer couId, Integer amoFin, Integer amoPaid,
			Timestamp paidAt, Integer paySta) {
		super();
		this.payId = payId;
		this.ordId = ordId;
		this.memId = memId;
		this.couId = couId;
		this.amoFin = amoFin;
		this.amoPaid = amoPaid;
		this.paidAt = paidAt;
		this.paySta = paySta;
	}

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