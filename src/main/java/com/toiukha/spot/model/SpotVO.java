package com.toiukha.spot.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 景點實體類
 * 對應資料庫 spot 表格
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
    @Column(name = "SPOTID", updatable = false)
    private Integer spotId;
    
    @Column(name = "SPOTNAME", nullable = false, length = 100)
    @NotEmpty(message = "景點名稱不能為空")
    @Size(min = 2, max = 100, message = "景點名稱長度必須在2到100字元之間")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s\\-()（）.]+$", 
             message = "景點名稱只能包含中英文、數字、空白及常用符號")
    private String spotName;
    
    @Column(name = "CRTID", nullable = false, updatable = false)
    @NotNull(message = "建立者ID不可為空")
    @Positive(message = "建立者ID必須為正數")
    private Integer crtId;
    
    @Column(name = "SPOTLOC", nullable = false, length = 200)
    @NotEmpty(message = "景點地址不能為空")
    @Size(min = 5, max = 200, message = "景點地址長度必須在5到200字元之間")
    private String spotLoc;
    
    @Column(name = "SPOTLAT")
    @DecimalMin(value = "-90.0", message = "緯度值必須在-90到90之間")
    @DecimalMax(value = "90.0", message = "緯度值必須在-90到90之間")
    private Double spotLat;
    
    @Column(name = "SPOTLNG")
    @DecimalMin(value = "-180.0", message = "經度值必須在-180到180之間")
    @DecimalMax(value = "180.0", message = "經度值必須在-180到180之間")
    private Double spotLng;
    
    @Column(name = "SPOTSTATUS", nullable = false)
    @NotNull(message = "景點狀態不可為空")
    @Min(value = 0, message = "景點狀態只能是0, 1, 2, 3")
    @Max(value = 3, message = "景點狀態只能是0(待審核), 1(上架), 2(退回), 3(下架)")
    private Byte spotStatus;
    
    @Column(name = "SPOTDESC", length = 500)
    @NotEmpty(message = "景點描述不能為空")
    @Size(min = 3,max = 500, message = "景點描述長度必須在3到500字元之間")
    private String spotDesc;
    
    @UpdateTimestamp
    @Column(name = "spotupdatedat")
    private LocalDateTime spotUpdatedAt;
    
    @CreationTimestamp
    @Column(name = "spotcreatedat", updatable = false)
    private LocalDateTime spotCreateAt;
    
    // 新增欄位用於政府資料整合
    @Column(name = "SPOTGOVID", length = 50)
    private String govtId; // 政府資料ID (如 C1_313020000G_000026)
    
    @Column(name = "SPOTZONE", length = 100)
    private String zone; // 區域分類
    
    @Column(name = "SPOTREGION", length = 50) 
    private String region; // 縣市
    
    @Column(name = "SPOTTOWN", length = 50)
    private String town; // 鄉鎮區
    
    @Column(name = "SPOTZIP", length = 10)
    private String zipcode; // 郵遞區號
    
    @Column(name = "SPOTTEL", length = 50)
    private String tel; // 電話
    
    @Column(name = "SPOTEMAIL", length = 100)
    private String email; // 電子郵件
    
    @Column(name = "SPOTWEB", length = 255)
    private String website; // 官方網站
    
    @Column(name = "SPOTTRAVELINFO", columnDefinition = "TEXT")
    private String travelingInfo; // 交通資訊
    
    @Column(name = "SPOTOPENTIME", columnDefinition = "TEXT") 
    private String openingTime; // 開放時間
    
    @Column(name = "SPOTTICKETINFO", columnDefinition = "TEXT")
    private String ticketInfo; // 票價資訊
    
    @Column(name = "SPOTPARKINFO", columnDefinition = "TEXT")
    private String parkingInfo; // 停車資訊
    
    @Column(name = "SPOTGPLACEID", length = 255)
    private String googlePlaceId; // Google Places ID
    
    @Column(name = "SPOTGRATING")
    private Double googleRating; // Google 評分
    
    @Column(name = "SPOTGTOTALRT")
    private Integer googleTotalRatings; // Google 評分總數
    
    @Column(name = "SPOTPICURLS", columnDefinition = "TEXT")
    private String pictureUrls; // 圖片URL (JSON格式儲存)

    @Column(name = "spotauditremark")
    private String spotAuditRemark;

    // 建構方法
    public SpotVO() {
        super();
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

    public Integer getCrtId() {
        return crtId;
    }

    public void setCrtId(Integer crtId) {
        this.crtId = crtId;
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

    // 業務方法
    /**
     * 檢查景點是否為上架狀態
     * @return true 如果景點為上架狀態
     */
    public boolean isActive() {
        return spotStatus != null && spotStatus == 1;
    }

    /**
     * 取得狀態文字描述
     * @return 狀態文字
     */
    public String getStatusText() {
        if (spotStatus == null) {
            return "未知";
        }
        switch (spotStatus) {
            case 0: return "待審核";
            case 1: return "上架";
            case 2: return "退回";
            case 3: return "下架";
            default: return "未知";
        }
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

    // 新增欄位的 Getter 和 Setter 方法
    public String getGovtId() {
        return govtId;
    }

    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
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
     * 從 pictureUrls (JSON字串) 中獲取第一張圖片的URL
     * @return 第一張圖片的URL，如果沒有或解析失敗則返回 null
     */
    public String getFirstPictureUrl() {
        if (pictureUrls == null || pictureUrls.trim().isEmpty()) {
            return null;
        }

        if (!pictureUrls.trim().startsWith("[")) {
            // 如果不是JSON陣列，則假設它不是有效的多圖片格式
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<String> urls = objectMapper.readValue(pictureUrls, new TypeReference<List<String>>() {});
            if (urls != null && !urls.isEmpty()) {
                return urls.get(0);
            }
        } catch (JsonProcessingException e) {
            // 實際應用中建議紀錄錯誤
            return null; 
        }
        return null;
    }

    /**
     * 檢查是否有 Google 評分資訊
     * @return true 如果有 Google 評分
     */
    public boolean hasGoogleRating() {
        return googleRating != null && googleRating > 0;
    }
    
    /**
     * 檢查是否來自政府資料
     * @return true 如果有政府資料 ID
     */
    public boolean isFromGovernmentData() {
        return govtId != null && !govtId.trim().isEmpty();
    }

    public String getSpotAuditRemark() {
        return spotAuditRemark;
    }

    public void setSpotAuditRemark(String spotAuditRemark) {
        this.spotAuditRemark = spotAuditRemark;
    }
} 