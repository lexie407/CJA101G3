package com.toiukha.itinerary.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 行程景點關聯複合主鍵類
 * 用於 itnspot 表格的複合主鍵 (ITNID, SPOTID)
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
public class ItnSpotId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer itnId;
    private Integer spotId;

    public ItnSpotId() {
        super();
    }

    public ItnSpotId(Integer itnId, Integer spotId) {
        this.itnId = itnId;
        this.spotId = spotId;
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

    // ========== equals and hashCode ==========

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ItnSpotId that = (ItnSpotId) obj;
        
        return Objects.equals(itnId, that.itnId) && 
               Objects.equals(spotId, that.spotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itnId, spotId);
    }

    @Override
    public String toString() {
        return "ItnSpotId{" +
                "itnId=" + itnId +
                ", spotId=" + spotId +
                '}';
    }
} 