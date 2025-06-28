package com.toiukha.orderitems.model;

import java.io.Serializable;

import com.toiukha.order.model.OrderVO;
import com.toiukha.orderitems.model.OrderItemsVO.CompositeDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "orderitems")
@IdClass(CompositeDetail.class)
public class OrderItemsVO implements java.io.Serializable {

	@Id
	@Column(name = "ORDID")
	private Integer ordId; // 訂單編號

	@Id
	@Column(name = "ITEMID")
	private Integer itemId; // 商品編號

	@Column(name = "ORDTOTAL", nullable = false)
	private Integer ordTotal; // 購買數量

	@Column(name = "ORIPRICE")
	private Integer oriPrice; // 原始價格

	@Column(name = "DISCPRICE")
	private Integer discPrice; // 實際促銷價格

	@Column(name = "SCORE")
	private Integer score; // 評價分數(會員評價)範圍1-5

	@Column(name = "CONTENT", length = 200)
	private String content; // 評價內文(會員評價)

	@Transient
	private String itemName;

	@Transient
	private OrderVO orderVO;

	public OrderVO getOrderVO() {
		return orderVO;
	}

	public void setOrderVO(OrderVO orderVO) {
		this.orderVO = orderVO;
	}
	
//	public OrderItemsVO() {
//		super();
//	}
//
//	public OrderItemsVO(Integer ordId, Integer itemId, Integer ordTotal, Integer oriPrice, Integer discPrice,
//			Integer score, String content) {
//		super();
//		this.ordId = ordId;
//		this.itemId = itemId;
//		this.ordTotal = ordTotal;
//		this.oriPrice = oriPrice;
//		this.discPrice = discPrice;
//		this.score = score;
//		this.content = content;
//	}

	public CompositeDetail getCompositeKey() {
		return new CompositeDetail(ordId, itemId);
	}

	public void setCompositeKey(CompositeDetail key) {
		this.ordId = key.getOrdId();
		this.itemId = key.getItemId();
	}

	public Integer getOrdId() {
		return ordId;
	}

	public void setOrdId(Integer ordId) {
		this.ordId = ordId;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public Integer getOrdTotal() {
		return ordTotal;
	}

	public void setOrdTotal(Integer ordTotal) {
		this.ordTotal = ordTotal;
	}

	public Integer getOriPrice() {
		return oriPrice;
	}

	public void setOriPrice(Integer oriPrice) {
		this.oriPrice = oriPrice;
	}

	public Integer getDiscPrice() {
		return discPrice;
	}

	public void setDiscPrice(Integer discPrice) {
		this.discPrice = discPrice;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	static class CompositeDetail implements Serializable {
		private static final long serialVersionUID = 1L;

		private Integer ordId;
		private Integer itemId;

		// 一定要有無參數建構子
		public CompositeDetail() {
			super();
		}

		public CompositeDetail(Integer ordId, Integer itemId) {
			super();
			this.ordId = ordId;
			this.itemId = itemId;
		}

		public Integer getOrdId() {
			return ordId;
		}

		public void setOrdId(Integer ordId) {
			this.ordId = ordId;
		}

		public Integer getItemId() {
			return itemId;
		}

		public void setItemId(Integer itemId) {
			this.itemId = itemId;
		}

		// 一定要 override 此類別的 hashCode() 與 equals() 方法！
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ordId == null) ? 0 : ordId.hashCode());
			result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (obj != null && getClass() == obj.getClass()) {
				CompositeDetail compositeKey = (CompositeDetail) obj;
				if (ordId.equals(compositeKey.ordId) && itemId.equals(compositeKey.itemId)) {
					return true;
				}
			}

			return false;
		}

	}

}
