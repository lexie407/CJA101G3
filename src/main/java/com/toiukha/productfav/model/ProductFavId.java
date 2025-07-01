package com.toiukha.productfav.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * ProductFav 複合主鍵類
 * 用於處理 MEMID 和 ITEMID 的複合主鍵
 */
public class ProductFavId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer memId;
    private Integer itemId;
    
    // 默認構造函數
    public ProductFavId() {
    }
    
    // 帶參數構造函數
    public ProductFavId(Integer memId, Integer itemId) {
        this.memId = memId;
        this.itemId = itemId;
    }
    
    // Getter 和 Setter
    public Integer getMemId() {
        return memId;
    }
    
    public void setMemId(Integer memId) {
        this.memId = memId;
    }
    
    public Integer getItemId() {
        return itemId;
    }
    
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
    
    // equals 方法（複合主鍵必須實現）
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductFavId that = (ProductFavId) obj;
        return Objects.equals(memId, that.memId) && 
               Objects.equals(itemId, that.itemId);
    }
    
    // hashCode 方法（複合主鍵必須實現）
    @Override
    public int hashCode() {
        return Objects.hash(memId, itemId);
    }
    
    // toString 方法
    @Override
    public String toString() {
        return "ProductFavId{" +
                "memId=" + memId +
                ", itemId=" + itemId +
                '}';
    }
} 