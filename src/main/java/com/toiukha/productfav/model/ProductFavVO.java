package com.toiukha.productfav.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.toiukha.members.model.MembersVO;
import com.toiukha.item.model.ItemVO;

/**
 * ProductFav VO 類
 * 對應 productfav 表，用於管理會員的商品收藏
 */
@Entity
@Table(name = "productfav")
@IdClass(ProductFavId.class)
public class ProductFavVO {

    @Id
    @NotNull
    @Column(name = "MEMID")
    private Integer memId;
    
    @Id
    @NotNull
    @Column(name = "ITEMID")
    private Integer itemId;
    
    @NotNull
    @Column(name = "FAVAT")
    private LocalDateTime favAt;
    
    // 關聯實體（可選）
    @ManyToOne
    @JoinColumn(name = "MEMID", insertable = false, updatable = false)
    private MembersVO member;
    
    @ManyToOne
    @JoinColumn(name = "ITEMID", insertable = false, updatable = false)
    private ItemVO item;
    
    // 默認構造函數
    public ProductFavVO() {
    }
    
    // 帶參數構造函數
    public ProductFavVO(Integer memId, Integer itemId, LocalDateTime favAt) {
        this.memId = memId;
        this.itemId = itemId;
        this.favAt = favAt;
    }
    
    // 完整構造函數
    public ProductFavVO(Integer memId, Integer itemId, LocalDateTime favAt, 
                       MembersVO member, ItemVO item) {
        this.memId = memId;
        this.itemId = itemId;
        this.favAt = favAt;
        this.member = member;
        this.item = item;
    }
    
    // Getter 和 Setter 方法
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
    
    public LocalDateTime getFavAt() {
        return favAt;
    }
    
    public void setFavAt(LocalDateTime favAt) {
        this.favAt = favAt;
    }
    
    public MembersVO getMember() {
        return member;
    }
    
    public void setMember(MembersVO member) {
        this.member = member;
    }
    
    public ItemVO getItem() {
        return item;
    }
    
    public void setItem(ItemVO item) {
        this.item = item;
    }
    
    // toString 方法
    @Override
    public String toString() {
        return "ProductFavVO{" +
                "memId=" + memId +
                ", itemId=" + itemId +
                ", favAt=" + favAt +
                '}';
    }
    
    // equals 和 hashCode 方法（基於複合主鍵）
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductFavVO that = (ProductFavVO) obj;
        return memId.equals(that.memId) && itemId.equals(that.itemId);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(memId, itemId);
    }
}
