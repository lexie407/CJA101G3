package com.toiukha.itinerary.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;

/**
 * 行程景點關聯實體類
 * 對應資料庫 itnspot 表格
 * 使用複合主鍵 (ITNID, SPOTID)
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Entity
@Table(name = "itnspot")
@IdClass(ItnSpotId.class)
public class ItnSpotVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ITNID", nullable = false)
    private Integer itnId;

    @Id
    @Column(name = "SPOTID", nullable = false)
    private Integer spotId;

    @Column(name = "SEQ", nullable = false)
    private Integer seq;

    // 關聯到行程實體
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITNID", insertable = false, updatable = false)
    @JsonBackReference
    private ItineraryVO itinerary;

    // 關聯到景點實體
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPOTID", insertable = false, updatable = false)
    private com.toiukha.spot.model.SpotVO spot;

    // 建構方法
    public ItnSpotVO() {
        super();
    }

    public ItnSpotVO(Integer itnId, Integer spotId, Integer seq) {
        this.itnId = itnId;
        this.spotId = spotId;
        this.seq = seq;
    }

    // ========== Getters and Setters ==========

    public Integer getItnId() {
        return itnId;
    }

    public void setItnId(Integer itnId) {
        this.itnId = itnId;
    }

    public Integer getSpotId() {
        return spotId;
    }

    public void setSpotId(Integer spotId) {
        this.spotId = spotId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public ItineraryVO getItinerary() {
        return itinerary;
    }

    public void setItinerary(ItineraryVO itinerary) {
        this.itinerary = itinerary;
    }

    public com.toiukha.spot.model.SpotVO getSpot() {
        return spot;
    }

    public void setSpot(com.toiukha.spot.model.SpotVO spot) {
        this.spot = spot;
    }

    // 新增：為相容 Controller 的除錯程式碼，提供 getItnSpotId() 方法
    public Integer getItnSpotId() {
        return spotId;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "ItnSpotVO{" +
                "itnId=" + itnId +
                ", spotId=" + spotId +
                ", seq=" + seq +
                '}';
    }
} 