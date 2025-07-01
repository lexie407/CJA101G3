package com.toiukha.itnReport.model;

import java.time.LocalDateTime;

/**
 * 訂單商品 DTO - 用於檢舉功能顯示用戶購買記錄
 */
public class OrderItemDTO {
    
    private Integer ordId;          // 訂單編號
    private Integer itemId;         // 商品編號
    private String itemName;        // 商品名稱
    private Integer quantity;       // 購買數量
    private Integer price;          // 購買單價
    private LocalDateTime orderDate; // 訂單日期
    private String orderStatus;     // 訂單狀態
    private byte[] itemImg;         // 商品圖片
    
    public OrderItemDTO() {}
    
    public OrderItemDTO(Integer ordId, Integer itemId, String itemName, Integer quantity, 
                       Integer price, LocalDateTime orderDate, String orderStatus, byte[] itemImg) {
        this.ordId = ordId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.itemImg = itemImg;
    }

    // Getters and Setters
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public byte[] getItemImg() {
        return itemImg;
    }

    public void setItemImg(byte[] itemImg) {
        this.itemImg = itemImg;
    }
} 