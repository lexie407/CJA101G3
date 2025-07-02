package com.toiukha.spot.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 景點收藏複合主鍵類
 * 用於 favspot 表格的複合主鍵 (FAVSPOTID, MEMID)
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
public class SpotFavoriteId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer favSpotId;
    private Integer memId;

    public SpotFavoriteId() {
        super();
    }

    public SpotFavoriteId(Integer favSpotId, Integer memId) {
        this.favSpotId = favSpotId;
        this.memId = memId;
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

    // ========== equals and hashCode ==========

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SpotFavoriteId that = (SpotFavoriteId) obj;
        
        return Objects.equals(favSpotId, that.favSpotId) && 
               Objects.equals(memId, that.memId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(favSpotId, memId);
    }

    @Override
    public String toString() {
        return "SpotFavoriteId{" +
                "favSpotId=" + favSpotId +
                ", memId=" + memId +
                '}';
    }
} 