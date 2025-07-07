package com.toiukha.order.model;

import java.sql.Timestamp;
import java.util.List;

import com.toiukha.item.model.ItemVO;
import com.toiukha.orderitems.model.OrderItemsVO;

public class OrderWithItemsDTO {
    private Integer ordId;
    private Integer memId;
    private Integer ordSta;
    private Timestamp creDate;
    private List<OrderItemDetailDTO> orderItems;
    private Integer originalTotal;    // 原價總計
    private Integer couponDiscount;   // 優惠券折抵金額
    private Integer finalTotal;       // 實付金額
    
    public OrderWithItemsDTO() {
        super();
    }
    
    public OrderWithItemsDTO(OrderVO order, List<OrderItemDetailDTO> orderItems) {
        this.ordId = order.getOrdId();
        this.memId = order.getMemId();
        this.ordSta = order.getOrdSta();
        this.creDate = order.getCreDate();
        this.orderItems = orderItems;
    }
    
    // 計算訂單總計
    public Integer getOrderTotal() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }
        
        return orderItems.stream()
            .mapToInt(item -> {
                Integer price = (item.getDiscPrice() != null && item.getDiscPrice() > 0) 
                    ? item.getDiscPrice() 
                    : item.getOriPrice();
                return price * item.getOrdTotal();
            })
            .sum();
    }
    
    // Getters and Setters
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
    
    public List<OrderItemDetailDTO> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemDetailDTO> orderItems) {
        this.orderItems = orderItems;
    }
    
    public Integer getOriginalTotal() { return originalTotal; }
    public void setOriginalTotal(Integer originalTotal) { this.originalTotal = originalTotal; }
    public Integer getCouponDiscount() { return couponDiscount; }
    public void setCouponDiscount(Integer couponDiscount) { this.couponDiscount = couponDiscount; }
    public Integer getFinalTotal() { return finalTotal; }
    public void setFinalTotal(Integer finalTotal) { this.finalTotal = finalTotal; }
    
    // 內部類別：訂單項目詳細資訊
    public static class OrderItemDetailDTO {
        private Integer ordId;
        private Integer itemId;
        private Integer ordTotal;
        private Integer oriPrice;
        private Integer discPrice;
        private Integer score;
        private String content;
        private ItemVO item; // 商品資訊
        
        public OrderItemDetailDTO() {
            super();
        }
        
        public OrderItemDetailDTO(OrderItemsVO orderItem, ItemVO item) {
            this.ordId = orderItem.getOrdId();
            this.itemId = orderItem.getItemId();
            this.ordTotal = orderItem.getOrdTotal();
            this.oriPrice = orderItem.getOriPrice();
            this.discPrice = orderItem.getDiscPrice();
            this.score = orderItem.getScore();
            this.content = orderItem.getContent();
            this.item = item;
        }
        
        // 計算項目小計
        public Integer getItemSubtotal() {
            Integer price = (discPrice != null && discPrice > 0) ? discPrice : oriPrice;
            return price * ordTotal;
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
        
        public ItemVO getItem() {
            return item;
        }
        
        public void setItem(ItemVO item) {
            this.item = item;
        }
    }
    
    /**
     * 計算原價總計、折扣、實付金額
     * @param couponDiscount 優惠券折抵金額（可為null）
     */
    public void calculateTotals(Integer couponDiscount) {
        // 原價總計
        int oriTotal = 0;
        if (orderItems != null) {
            for (OrderItemDetailDTO item : orderItems) {
                oriTotal += (item.getOriPrice() != null ? item.getOriPrice() : 0) * (item.getOrdTotal() != null ? item.getOrdTotal() : 0);
            }
        }
        this.originalTotal = oriTotal;
        this.couponDiscount = (couponDiscount != null) ? couponDiscount : 0;
        this.finalTotal = getOrderTotal() - this.couponDiscount;
    }
} 