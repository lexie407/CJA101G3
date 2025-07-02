package com.toiukha.spot.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 景點資料傳輸物件
 * 用於API請求和回應的資料傳輸
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
public class SpotDTO {
    
    @NotEmpty(message = "景點名稱不能為空")
    @Size(min = 2, max = 100, message = "景點名稱長度必須在2到100字元之間")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s\\-()（）.]+$", 
             message = "景點名稱只能包含中英文、數字、空白及常用符號")
    private String spotName;
    
    @NotEmpty(message = "景點地址不能為空")
    @Size(min = 5, max = 200, message = "景點地址長度必須在5到200字元之間")
    private String spotLoc;
    
    @DecimalMin(value = "-90.0", message = "緯度值必須在-90到90之間")
    @DecimalMax(value = "90.0", message = "緯度值必須在-90到90之間")
    private Double spotLat;
    
    @DecimalMin(value = "-180.0", message = "經度值必須在-180到180之間")
    @DecimalMax(value = "180.0", message = "經度值必須在-180到180之間")
    private Double spotLng;
    
    @Min(value = 0, message = "景點狀態只能是0(待審核)或1(上架)")
    @Max(value = 1, message = "景點狀態只能是0(待審核)或1(上架)")
    private Byte spotStatus;
    
    @Size(max = 500, message = "景點描述長度不可超過500字元")
    private String spotDesc;

    // 建構方法
    public SpotDTO() {
        super();
    }

    public SpotDTO(String spotName, String spotLoc, Double spotLat, Double spotLng, 
                   Byte spotStatus, String spotDesc) {
        this.spotName = spotName;
        this.spotLoc = spotLoc;
        this.spotLat = spotLat;
        this.spotLng = spotLng;
        this.spotStatus = spotStatus;
        this.spotDesc = spotDesc;
    }

    // Getter 和 Setter 方法
    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public String getSpotLoc() {
        return spotLoc;
    }

    public void setSpotLoc(String spotLoc) {
        this.spotLoc = spotLoc;
    }

    public Double getSpotLat() {
        return spotLat;
    }

    public void setSpotLat(Double spotLat) {
        this.spotLat = spotLat;
    }

    public Double getSpotLng() {
        return spotLng;
    }

    public void setSpotLng(Double spotLng) {
        this.spotLng = spotLng;
    }

    public Byte getSpotStatus() {
        return spotStatus;
    }

    public void setSpotStatus(Byte spotStatus) {
        this.spotStatus = spotStatus;
    }

    public String getSpotDesc() {
        return spotDesc;
    }

    public void setSpotDesc(String spotDesc) {
        this.spotDesc = spotDesc;
    }

    @Override
    public String toString() {
        return "SpotDTO{" +
                "spotName='" + spotName + '\'' +
                ", spotLoc='" + spotLoc + '\'' +
                ", spotLat=" + spotLat +
                ", spotLng=" + spotLng +
                ", spotStatus=" + spotStatus +
                ", spotDesc='" + spotDesc + '\'' +
                '}';
    }
} 