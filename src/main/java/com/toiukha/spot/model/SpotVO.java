package com.toiukha.spot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 景點實體類
 * 對應資料庫中的spot表
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Entity
@Table(name = "spot")
public class SpotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SPOTID")
    private Integer spotId;

    @NotEmpty(message = "景點名稱不能為空")
    @Size(min = 2, max = 100, message = "景點名稱長度必須在2-100之間")
    @Column(name = "SPOTNAME")
    private String spotName;

    @Column(name = "SPOTDESC", columnDefinition = "TEXT")
    private String spotDesc;

    @Column(name = "SPOTLOC")
    private String spotLoc;

    @Column(name = "SPOTLAT")
    private Double spotLat;

    @Column(name = "SPOTLNG")
    private Double spotLng;

    @Transient
    private byte[] spotPic;

    @Column(name = "SPOTSTATUS")
    private Byte spotStatus;

    @Column(name = "SPOTCREATEDAT")
    private LocalDateTime spotCreateAt;

    @Column(name = "SPOTUPDATEDAT")
    private LocalDateTime spotUpdatedAt;

    @Column(name = "CRTID")
    private Integer crtId;

    @Column(name = "SPOTGOVID")
    private String govtId;

    @Column(name = "SPOTREGION")
    private String region;

    @Transient
    private String firstPictureUrl;

    // 審核備註
    @Column(name = "SPOTAUDITREMARK")
    private String spotAuditRemark;

    // 對應資料庫欄位的屬性
    @Column(name = "SPOTZONE")
    private String zone;
    
    @Column(name = "SPOTTOWN")
    private String town;
    
    @Column(name = "SPOTZIP")
    private String zipcode;
    
    @Column(name = "SPOTTEL")
    private String tel;
    
    @Column(name = "SPOTEMAIL")
    private String email;
    
    @Column(name = "SPOTWEB")
    private String website;
    
    @Column(name = "SPOTTRAVELINFO")
    private String travelingInfo;
    
    @Column(name = "SPOTOPENTIME")
    private String openingTime;
    
    @Column(name = "SPOTTICKETINFO")
    private String ticketInfo;
    
    @Column(name = "SPOTPARKINFO")
    private String parkingInfo;

    // Google Places相關欄位
    @Column(name = "SPOTGPLACEID")
    private String googlePlaceId;
    
    @Column(name = "SPOTGRATING")
    private Double googleRating;
    
    @Column(name = "SPOTGTOTALRT")
    private Integer googleTotalRatings;
    
    @Column(name = "SPOTPICURLS")
    private String pictureUrls;

    // 預設建構子
    public SpotVO() {
    }

    // Getter 和 Setter 方法
    public Integer getSpotId() {
        return spotId;
    }

    public void setSpotId(Integer spotId) {
        this.spotId = spotId;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public String getSpotDesc() {
        return spotDesc;
    }

    public void setSpotDesc(String spotDesc) {
        this.spotDesc = spotDesc;
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

    public byte[] getSpotPic() {
        return spotPic;
    }

    public void setSpotPic(byte[] spotPic) {
        this.spotPic = spotPic;
    }

    public Byte getSpotStatus() {
        return spotStatus;
    }

    public void setSpotStatus(Byte spotStatus) {
        this.spotStatus = spotStatus;
    }

    public LocalDateTime getSpotCreateAt() {
        return spotCreateAt;
    }

    public void setSpotCreateAt(LocalDateTime spotCreateAt) {
        this.spotCreateAt = spotCreateAt;
    }

    public LocalDateTime getSpotUpdatedAt() {
        return spotUpdatedAt;
    }

    public void setSpotUpdatedAt(LocalDateTime spotUpdatedAt) {
        this.spotUpdatedAt = spotUpdatedAt;
    }

    public Integer getCrtId() {
        return crtId;
    }

    public void setCrtId(Integer crtId) {
        this.crtId = crtId;
    }

    public String getGovtId() {
        return govtId;
    }

    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getFirstPictureUrl() {
        return firstPictureUrl;
    }

    public void setFirstPictureUrl(String firstPictureUrl) {
        this.firstPictureUrl = firstPictureUrl;
    }

    public String getSpotAuditRemark() {
        return spotAuditRemark;
    }

    public void setSpotAuditRemark(String spotAuditRemark) {
        this.spotAuditRemark = spotAuditRemark;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTravelingInfo() {
        return travelingInfo;
    }

    public void setTravelingInfo(String travelingInfo) {
        this.travelingInfo = travelingInfo;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
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

    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }

    public Double getGoogleRating() {
        return googleRating;
    }

    public void setGoogleRating(Double googleRating) {
        this.googleRating = googleRating;
    }

    public Integer getGoogleTotalRatings() {
        return googleTotalRatings;
    }

    public void setGoogleTotalRatings(Integer googleTotalRatings) {
        this.googleTotalRatings = googleTotalRatings;
    }

    public String getPictureUrls() {
        return pictureUrls;
    }

    public void setPictureUrls(String pictureUrls) {
        this.pictureUrls = pictureUrls;
    }
    
    /**
     * 檢查是否有有效的座標資訊
     * @return true 如果有完整的經緯度資訊
     */
    public boolean hasValidCoordinates() {
        return spotLat != null && spotLng != null 
            && spotLat >= -90 && spotLat <= 90
            && spotLng >= -180 && spotLng <= 180;
    }

    /**
     * 取得狀態文字描述
     * @return 狀態文字描述
     */
    public String getStatusText() {
        switch (this.spotStatus) {
            case 0: return "待審核";
            case 1: return "上架";
            case 2: return "退回";
            case 3: return "下架";
            default: return "未知狀態";
        }
    }

    /**
     * 判斷是否來自政府資料
     * @return true 如果來自政府資料
     */
    public boolean isFromGovernmentData() {
        return this.govtId != null && !this.govtId.isEmpty();
    }

    /**
     * 判斷是否有 Google 評分
     * @return true 如果有 Google 評分
     */
    public boolean hasGoogleRating() {
        return this.googleRating != null && this.googleRating > 0;
    }

    /**
     * 判斷是否為上架狀態
     * @return true 如果為上架狀態
     */
    public boolean isActive() {
        return this.spotStatus == 1;
    }

    @Override
    public String toString() {
        return "SpotVO{" +
                "spotId=" + spotId +
                ", spotName='" + spotName + '\'' +
                ", spotStatus=" + spotStatus +
                ", region='" + region + '\'' +
                '}';
    }
} 