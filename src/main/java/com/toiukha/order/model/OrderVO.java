package com.toiukha.order.model;

import java.sql.Timestamp;

import org.springframework.format.annotation.DateTimeFormat;

import com.toiukha.orderitems.model.OrderItemsVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Transient;
import java.util.List;
import com.toiukha.orderitems.model.OrderItemsVO;

@Entity
@Table(name = "orders")
public class OrderVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ORDID")
	private Integer ordId;

	@NotNull(message = "會員編號: 請勿空白")
	@Column(name = "MEMID")
	private Integer memId;

	@NotNull(message = "訂單狀態: 請勿空白")
	@Column(name = "ORDSTA")
	private Integer ordSta;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//	@NotNull(message="訂單日期: 請勿空白")
	@Column(name = "CREDATE")
	private Timestamp creDate;

	@Transient
	private List<OrderItemsVO> orderItems;

	public List<OrderItemsVO> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItemsVO> orderItems) {
		this.orderItems = orderItems;
	}


	public OrderVO() {
		super();
	}

	public OrderVO(Integer ordId, Integer memId, Integer ordSta, Timestamp creDate) {
		super();
		this.ordId = ordId;
		this.memId = memId;
		this.ordSta = ordSta;
		this.creDate = creDate;
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

	public Integer getOrdSta() {
		return ordSta;
	}

	public void setOrdSta(Integer ordSta) {
		this.ordSta = ordSta;
	}

	public Timestamp getCreDate() {
		return creDate;
	}

	public void setCreDate(Timestamp creDate) {
		this.creDate = creDate;
	}
}
