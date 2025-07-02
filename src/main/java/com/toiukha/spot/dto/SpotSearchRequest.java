package com.toiukha.spot.dto;

import jakarta.validation.constraints.Size;

/**
 * 景點搜尋請求物件
 * 用於複合搜尋功能的資料傳輸
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
public class SpotSearchRequest {
    
    @Size(max = 100, message = "搜尋關鍵字長度不可超過100字元")
    private String keyword;
    
    private Byte spotStatus; // 可選的狀態篩選
    
    private Double minLat; // 可選的緯度範圍
    private Double maxLat;
    private Double minLng; // 可選的經度範圍
    private Double maxLng;

    // 建構方法
    public SpotSearchRequest() {
        super();
    }

    public SpotSearchRequest(String keyword) {
        this.keyword = keyword;
    }

    // Getter 和 Setter 方法
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Byte getSpotStatus() {
        return spotStatus;
    }

    public void setSpotStatus(Byte spotStatus) {
        this.spotStatus = spotStatus;
    }

    public Double getMinLat() {
        return minLat;
    }

    public void setMinLat(Double minLat) {
        this.minLat = minLat;
    }

    public Double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(Double maxLat) {
        this.maxLat = maxLat;
    }

    public Double getMinLng() {
        return minLng;
    }

    public void setMinLng(Double minLng) {
        this.minLng = minLng;
    }

    public Double getMaxLng() {
        return maxLng;
    }

    public void setMaxLng(Double maxLng) {
        this.maxLng = maxLng;
    }

    @Override
    public String toString() {
        return "SpotSearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", spotStatus=" + spotStatus +
                ", minLat=" + minLat +
                ", maxLat=" + maxLat +
                ", minLng=" + minLng +
                ", maxLng=" + maxLng +
                '}';
    }
} 