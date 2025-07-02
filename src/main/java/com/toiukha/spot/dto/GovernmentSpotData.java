package com.toiukha.spot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 政府觀光資料 DTO
 * 用於接收台灣政府觀光 JSON 資料
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GovernmentSpotData {
    
    @JsonProperty("Id")
    private String id;
    
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("Zone")
    private String zone;
    
    @JsonProperty("Toldescribe")
    private String description;
    
    @JsonProperty("Description")
    private String shortDescription;
    
    @JsonProperty("Tel")
    private String tel;
    
    @JsonProperty("Add")
    private String address;
    
    @JsonProperty("Zipcode")
    private String zipcode;
    
    @JsonProperty("Region")
    private String region;
    
    @JsonProperty("Town")
    private String town;
    
    @JsonProperty("Travellinginfo")
    private String travelingInfo;
    
    @JsonProperty("Opentime")
    private String openTime;
    
    @JsonProperty("Ticketinfo")
    private String ticketInfo;
    
    @JsonProperty("Parkinginfo")
    private String parkingInfo;
    
    @JsonProperty("Website")
    private String website;
    
    @JsonProperty("Email")
    private String email;
    
    @JsonProperty("Px")
    private String longitude; // 經度
    
    @JsonProperty("Py")
    private String latitude;  // 緯度

    // 無參建構子
    public GovernmentSpotData() {}

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getTravelingInfo() {
        return travelingInfo;
    }

    public void setTravelingInfo(String travelingInfo) {
        this.travelingInfo = travelingInfo;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getTicketInfo() {
        return ticketInfo;
    }

    public void setTicketInfo(String ticketInfo) {
        this.ticketInfo = ticketInfo;
    }

    public String getParkingInfo() {
        return parkingInfo;
    }

    public void setParkingInfo(String parkingInfo) {
        this.parkingInfo = parkingInfo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * 檢查經緯度是否有效
     * @return true 如果經緯度都不為空且不為0
     */
    public boolean hasValidCoordinates() {
        try {
            double lat = Double.parseDouble(latitude != null ? latitude : "0");
            double lng = Double.parseDouble(longitude != null ? longitude : "0");
            return lat != 0.0 && lng != 0.0 && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 取得經度的數值
     * @return 經度數值，無效時返回 null
     */
    public Double getLongitudeValue() {
        try {
            return longitude != null ? Double.parseDouble(longitude) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 取得緯度的數值
     * @return 緯度數值，無效時返回 null
     */
    public Double getLatitudeValue() {
        try {
            return latitude != null ? Double.parseDouble(latitude) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "GovernmentSpotData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", town='" + town + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
} 