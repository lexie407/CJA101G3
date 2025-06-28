package com.toiukha.groupactivity.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 活動招募狀態Enum
 * 統一管理所有活動狀態的定義、顯示名稱和CSS類別
 */
public enum ActStatus {
    OPEN((byte)0, "招募中", "recruiting"),
    FULL((byte)1, "成團", "full"), 
    FAILED((byte)2, "未成團", "ended"),
    CANCELLED((byte)3, "團主取消", "ended"),
    FROZEN((byte)4, "系統凍結", "ended"),
    ENDED((byte)5, "活動結束", "ended");
    
    private final byte value;
    private final String displayName;
    private final String cssClass;
    
    ActStatus(byte value, String displayName, String cssClass) {
        this.value = value;
        this.displayName = displayName;
        this.cssClass = cssClass;
    }
    
    public byte getValue() { 
        return value; 
    }
    
    public String getDisplayName() { 
        return displayName; 
    }
    
    public String getCssClass() { 
        return cssClass; 
    }
    
    /**
     * 根據數值取得對應的Enum
     */
    public static ActStatus fromValue(byte value) {
        for (ActStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }
    
    /**
     * 根據數值取得對應的Enum，如果找不到則回傳null
     */
    public static ActStatus fromValueOrNull(Byte value) {
        if (value == null) {
            return null;
        }
        for (ActStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 取得狀態Map供前端使用
     */
    public static Map<Byte, String> getStatusMap() {
        Map<Byte, String> map = new LinkedHashMap<>();
        for (ActStatus status : values()) {
            map.put(status.value, status.displayName);
        }
        return map;
    }
    
    /**
     * 檢查是否為招募中狀態
     */
    public boolean isOpen() {
        return this == OPEN;
    }
    
    /**
     * 檢查是否為成團狀態
     */
    public boolean isFull() {
        return this == FULL;
    }
    
    /**
     * 檢查是否為結束狀態（未成團、取消、凍結、結束）
     */
    public boolean isEnded() {
        return this == FAILED || this == CANCELLED || this == FROZEN || this == ENDED;
    }
    
    /**
     * 檢查是否可以報名
     */
    public boolean canSignUp() {
        return this == OPEN;
    }
} 