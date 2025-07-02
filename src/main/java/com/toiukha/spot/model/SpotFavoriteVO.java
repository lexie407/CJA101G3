package com.toiukha.spot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import com.toiukha.members.model.MembersVO;

/**
 * 景點收藏實體類
 * 對應資料庫 favspot 表格
 * 使用複合主鍵 (FAVSPOTID, MEMID)
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Entity
@Table(name = "favspot")
@IdClass(SpotFavoriteId.class)
public class SpotFavoriteVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "FAVSPOTID", nullable = false)
    private Integer favSpotId;

    @Id
    @Column(name = "MEMID", nullable = false)
    private Integer memId;

    @Column(name = "FAVCREATEDAT", nullable = false, updatable = false)
    private Timestamp favCreateAt;

    // 關聯到會員實體
    @ManyToOne
    @JoinColumn(name = "MEMID", insertable = false, updatable = false)
    private MembersVO member;

    // 關聯到景點實體
    @ManyToOne
    @JoinColumn(name = "FAVSPOTID", insertable = false, updatable = false)
    private SpotVO spot;

    public SpotFavoriteVO() {
        super();
    }

    public SpotFavoriteVO(Integer favSpotId, Integer memId) {
        this.favSpotId = favSpotId;
        this.memId = memId;
    }

    @PrePersist
    public void onCreate() {
        if (favCreateAt == null) {
            favCreateAt = new Timestamp(System.currentTimeMillis());
        }
    }

    // ========== Getters and Setters ==========

    public Integer getFavSpotId() {
        return favSpotId;
    }

    public void setFavSpotId(Integer favSpotId) {
        this.favSpotId = favSpotId;
    }

    public Integer getMemId() {
        return memId;
    }

    public void setMemId(Integer memId) {
        this.memId = memId;
    }

    public Timestamp getFavCreateAt() {
        return favCreateAt;
    }

    public void setFavCreateAt(Timestamp favCreateAt) {
        this.favCreateAt = favCreateAt;
    }

    public MembersVO getMember() {
        return member;
    }

    public void setMember(MembersVO member) {
        this.member = member;
    }

    public SpotVO getSpot() {
        return spot;
    }

    public void setSpot(SpotVO spot) {
        this.spot = spot;
    }

    // ========== 便利方法 ==========

    /**
     * 取得景點ID (為了向後相容)
     * @return 景點ID
     */
    public Integer getSpotId() {
        return favSpotId;
    }

    /**
     * 設定景點ID (為了向後相容)
     * @param spotId 景點ID
     */
    public void setSpotId(Integer spotId) {
        this.favSpotId = spotId;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "SpotFavoriteVO{" +
                "favSpotId=" + favSpotId +
                ", memId=" + memId +
                ", favCreateAt=" + favCreateAt +
                '}';
    }

    // ========== equals and hashCode ==========

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SpotFavoriteVO that = (SpotFavoriteVO) obj;
        
        return Objects.equals(memId, that.memId) && 
               Objects.equals(favSpotId, that.favSpotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memId, favSpotId);
    }
} 