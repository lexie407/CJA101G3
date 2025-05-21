package com.toiukha.orderitems;

public class OrderItemsVO {
	private Integer ordId; // 訂單編號
	private Integer itemId; // 商品編號
	private Integer ordTotal; // 購買數量
	private Integer oriPrice; // 原始價格
	private Integer discPrice; // 實際促銷價格
	private Integer score; // 評價分數(會員評價)範圍1-5
	private String content; // 評價內文(會員評價)

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

}
