package com.toiukha.itinerary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 行程實體類
 * 對應資料庫 itinerary 表格
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Entity
@Table(name = "itinerary")
public class ItineraryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITNID", nullable = false)
    private Integer itnId;

    @Column(name = "ITNNAME", nullable = false, length = 255)
    @NotEmpty(message = "行程名稱不能為空")
    @Size(min = 2, max = 255, message = "行程名稱長度必須在2到255字元之間")
    private String itnName;

    @Column(name = "CRTID", nullable = false, updatable = false)
    @NotNull(message = "建立者ID不可為空")
    private Integer crtId;

    @Column(name = "ITNDESC", length = 500)
    @Size(max = 500, message = "行程描述長度不可超過500字元")
    private String itnDesc;

    @Column(name = "ISPUBLIC", nullable = false)
    private Byte isPublic; // 是否公開 (0=私人, 1=公開)

    @Column(name = "ITNSTATUS", nullable = false)
    private Byte itnStatus; // 行程狀態 (0=下架, 1=上架)

    @Column(name = "CREATOR_TYPE", nullable = false)
    private Byte creatorType; // 建立者類型 (1=會員, 2=管理員)

    @Column(name = "ITNCREATEDAT", insertable = false, updatable = false)
    private Timestamp itnCreateDat;

    @Column(name = "ITNUPDATEDAT", insertable = false)
    private Timestamp itnUpdateDat;

    @Transient  // 標記為非持久化屬性
    private Boolean isFavorited;  // 是否已收藏（非資料庫欄位）

    @Transient
    private String creatorDisplayName;

    // 關聯到行程景點
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ItnSpotVO> itnSpots;

    // 建構方法
    public ItineraryVO() {
        super();
    }

    // 新增用建構方法
    public ItineraryVO(String itnName, Integer crtId, String itnDesc) {
        this.itnName = itnName;
        this.crtId = crtId;
        this.itnDesc = itnDesc;
    }

    public ItineraryVO(String itnName, String itnDesc, Integer crtId, Byte isPublic) {
        this.itnName = itnName;
        this.itnDesc = itnDesc;
        this.crtId = crtId;
        this.isPublic = isPublic;
    }

    // ========== Getters and Setters ==========

    public Integer getItnId() {
        return itnId;
    }

    public void setItnId(Integer itnId) {
        this.itnId = itnId;
    }

    public String getItnName() {
        return itnName;
    }

    public void setItnName(String itnName) {
        this.itnName = itnName;
    }

    public Integer getCrtId() {
        return crtId;
    }

    public void setCrtId(Integer crtId) {
        this.crtId = crtId;
    }

    public String getItnDesc() {
        return itnDesc;
    }

    public void setItnDesc(String itnDesc) {
        this.itnDesc = itnDesc;
    }

    public Byte getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Byte isPublic) {
        this.isPublic = isPublic;
    }

    public Byte getItnStatus() {
        return itnStatus;
    }

    public void setItnStatus(Byte itnStatus) {
        this.itnStatus = itnStatus;
    }

    public Byte getCreatorType() {
        return creatorType;
    }

    public void setCreatorType(Byte creatorType) {
        this.creatorType = creatorType;
    }

    public Timestamp getItnCreateDat() {
        return itnCreateDat;
    }

    public void setItnCreateDat(Timestamp itnCreateDat) {
        this.itnCreateDat = itnCreateDat;
    }

    public Timestamp getItnUpdateDat() {
        return itnUpdateDat;
    }

    public void setItnUpdateDat(Timestamp itnUpdateDat) {
        this.itnUpdateDat = itnUpdateDat;
    }

    public Boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public List<ItnSpotVO> getItnSpots() {
        return itnSpots;
    }

    public void setItnSpots(List<ItnSpotVO> itnSpots) {
        this.itnSpots = itnSpots;
    }

    public String getCreatorDisplayName() {
        if (this.creatorDisplayName != null) {
            return this.creatorDisplayName;
        }
        if (creatorType != null && creatorType == 2) {
            return "官方推薦";
        }
        return "會員"; // Default
    }

    /**
     * 格式化建立者顯示名稱
     * @param currentMemId 當前登入會員ID，用於判斷是否為「我的行程」
     * @return 格式化後的建立者名稱
     */
    public String formatCreatorName(Integer currentMemId) {
        if (creatorType == null) {
            return "未知";
        }
        
        if (creatorType == 2) {
            // 官方/管理員建立的行程
            return "官方推薦";
        } else if (creatorType == 1) {
            // 會員建立的行程
            if (currentMemId != null && currentMemId.equals(this.crtId)) {
                return "我的行程";
            } else {
                // 顯示會員 + ID後四位
                String idStr = String.valueOf(this.crtId);
                if (idStr.length() >= 4) {
                    return "會員 " + idStr.substring(idStr.length() - 4);
                } else {
                    return "會員 " + String.format("%04d", this.crtId);
                }
            }
        }
        
        return "未知";
    }

    public void setCreatorDisplayName(String creatorDisplayName) {
        this.creatorDisplayName = creatorDisplayName;
    }

    // ========== 業務方法 ==========

    /**
     * 檢查行程是否為公開狀態
     * @return true 如果行程為公開狀態
     */
    public boolean isPublicItinerary() {
        return isPublic != null && isPublic == 1;
    }

    /**
     * 取得公開狀態文字描述
     * @return 狀態文字
     */
    public String getPublicStatusText() {
        if (isPublic == null) {
            return "未知";
        }
        return isPublic == 1 ? "公開" : "私人";
    }

    /**
     * 取得行程狀態文字描述
     * @return 狀態文字
     */
    public String getStatusText() {
        if (itnStatus == null) {
            return "未知";
        }
        return itnStatus == 1 ? "上架" : "下架";
    }

    /**
     * 取得行程中的景點數量
     * @return 景點數量
     */
    public int getSpotCount() {
        return itnSpots != null ? itnSpots.size() : 0;
    }

    /**
     * 檢查行程是否有景點
     * @return true 如果行程有景點
     */
    public boolean hasSpots() {
        return itnSpots != null && !itnSpots.isEmpty();
    }

    /**
     * 檢查行程是否由管理員建立
     * @return true 如果行程由管理員建立
     */
    public boolean isCreatedByAdmin() {
        return creatorType != null && creatorType == 2;
    }

    /**
     * 取得建立者類型文字描述
     * @return 建立者類型文字
     */
    public String getCreatorTypeText() {
        if (creatorType == null) {
            return "未知";
        }
        return creatorType == 2 ? "管理員" : "會員";
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "ItineraryVO{" +
                "itnId=" + itnId +
                ", itnName='" + itnName + '\'' +
                ", crtId=" + crtId +
                ", itnDesc='" + itnDesc + '\'' +
                ", isPublic=" + isPublic +
                ", itnStatus=" + itnStatus +
                ", itnCreateDat=" + itnCreateDat +
                ", itnUpdateDat=" + itnUpdateDat +
                '}';
    }
} 
