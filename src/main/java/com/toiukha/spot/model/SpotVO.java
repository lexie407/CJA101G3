package com.toiukha.spot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 景點實體類
 * 對應資料庫中的spot表
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Data
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
    @Column(name = "SPOTNAME", nullable = false)
    private String spotName;

    @Column(name = "SPOTLOC")
    private String spotLoc;

    @Column(name = "SPOTLAT")
    private Double spotLat;

    @Column(name = "SPOTLNG")
    private Double spotLng;

    @Column(name = "SPOTDESC", length = 500)
    private String spotDesc;

    @Column(name = "SPOTREGION")
    private String region;

    @Column(name = "SPOTGOVID")
    private String govtId;

    @Column(name = "SPOTSTATUS")
    private Byte spotStatus;

    @Column(name = "SPOTAUDITREMARK")
    private String spotAuditRemark;

    @Column(name = "SPOTCREATEDAT")
    private LocalDateTime spotCreateAt;

    @Column(name = "SPOTUPDATEDAT")
    private LocalDateTime spotUpdatedAt;

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

    @Column(name = "SPOTGPLACEID")
    private String googlePlaceId;
    
    @Column(name = "SPOTGRATING")
    private Double googleRating;
    
    @Column(name = "SPOTGTOTALRT")
    private Integer googleTotalRatings;
    
    @Column(name = "SPOTPICURLS")
    private String firstPictureUrl;

    @Column(name = "CRTID")
    private Integer crtId;

    @Column(name = "CREATOR_TYPE")
    private Byte creatorType;

    @Column(name = "SPOTVIEWCNT")
    private Integer spotViewCount;

    @Column(name = "SPOTFAVCNT")
    private Integer spotFavoriteCount;

    @Column(name = "SPOTCAT")
    private String spotCategory;

    @Column(name = "SPOTSUBCAT")
    private String spotSubCategory;

    @Column(name = "SPOTLASTSYNC")
    private LocalDateTime spotLastSync;

    // 移除的舊欄位對應
    @Transient
    private String pictureUrls; // 對應到 firstPictureUrl
    
    @Transient
    private String rejectReason; // 這個欄位在新結構中已移除
    
    @Transient
    private String rejectRemark; // 這個欄位在新結構中已移除

    @PrePersist
    protected void onCreate() {
        spotCreateAt = LocalDateTime.now();
        spotUpdatedAt = LocalDateTime.now();
        if (spotViewCount == null) {
            spotViewCount = 0;
        }
        if (spotFavoriteCount == null) {
            spotFavoriteCount = 0;
        }
        if (creatorType == null) {
            creatorType = (byte) 1; // 預設為會員建立
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        spotUpdatedAt = LocalDateTime.now();
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

    public String getSpotDesc() {
        return spotDesc;
    }

    public void setSpotDesc(String spotDesc) {
        this.spotDesc = spotDesc;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGovtId() {
        return govtId;
    }

    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }

    public Byte getSpotStatus() {
        return spotStatus;
    }

    public void setSpotStatus(Byte spotStatus) {
        this.spotStatus = spotStatus;
    }

    public String getSpotAuditRemark() {
        return spotAuditRemark;
    }

    public void setSpotAuditRemark(String spotAuditRemark) {
        this.spotAuditRemark = spotAuditRemark;
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

    public String getFirstPictureUrl() {
        return firstPictureUrl;
    }

    public void setFirstPictureUrl(String firstPictureUrl) {
        this.firstPictureUrl = firstPictureUrl;
    }

    public Integer getCrtId() {
        return crtId;
    }

    public void setCrtId(Integer crtId) {
        this.crtId = crtId;
    }

    public Byte getCreatorType() {
        return creatorType;
    }

    public void setCreatorType(Byte creatorType) {
        this.creatorType = creatorType;
    }

    public Integer getSpotViewCount() {
        return spotViewCount;
    }

    public void setSpotViewCount(Integer spotViewCount) {
        this.spotViewCount = spotViewCount;
    }

    public Integer getSpotFavoriteCount() {
        return spotFavoriteCount;
    }

    public void setSpotFavoriteCount(Integer spotFavoriteCount) {
        this.spotFavoriteCount = spotFavoriteCount;
    }

    public String getSpotCategory() {
        return spotCategory;
    }

    public void setSpotCategory(String spotCategory) {
        this.spotCategory = spotCategory;
    }

    public String getSpotSubCategory() {
        return spotSubCategory;
    }

    public void setSpotSubCategory(String spotSubCategory) {
        this.spotSubCategory = spotSubCategory;
    }

    public LocalDateTime getSpotLastSync() {
        return spotLastSync;
    }

    public void setSpotLastSync(LocalDateTime spotLastSync) {
        this.spotLastSync = spotLastSync;
    }

    // 向後相容的方法
    public String getPictureUrls() {
        return firstPictureUrl;
    }

    public void setPictureUrls(String pictureUrls) {
        this.firstPictureUrl = pictureUrls;
    }

    public String getRejectReason() {
        return null; // 新結構中已移除此欄位
    }

    public void setRejectReason(String rejectReason) {
        // 新結構中已移除此欄位，不做任何操作
    }

    public String getRejectRemark() {
        return null; // 新結構中已移除此欄位
    }

    public void setRejectRemark(String rejectRemark) {
        // 新結構中已移除此欄位，不做任何操作
    }

    // 業務邏輯方法
    public boolean hasValidCoordinates() {
        return spotLat != null && spotLng != null 
            && spotLat >= -90 && spotLat <= 90 
            && spotLng >= -180 && spotLng <= 180;
    }

    public String getStatusText() {
        if (spotStatus == null) return "未知";
        switch (spotStatus) {
            case 0: return "待審核";
            case 1: return "上架";
            case 2: return "下架";
            default: return "未知";
        }
    }

    public boolean isFromGovernmentData() {
        return govtId != null && !govtId.trim().isEmpty();
    }

    public boolean hasGoogleRating() {
        return googleRating != null && googleRating > 0;
    }

    public boolean isActive() {
        return spotStatus != null && spotStatus == 1;
    }

    @Override
    public String toString() {
        return "SpotVO{" +
                "spotId=" + spotId +
                ", spotName='" + spotName + '\'' +
                ", spotLoc='" + spotLoc + '\'' +
                ", spotStatus=" + spotStatus +
                ", region='" + region + '\'' +
                '}';
    }
} 