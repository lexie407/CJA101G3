package com.toiukha.participant.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * ParticipantVO 的複合主鍵類別
 */
public class ParticipantId implements Serializable {
    
    private Integer actId;
    private Integer memId;
    
    public ParticipantId() {
    }
    
    public ParticipantId(Integer actId, Integer memId) {
        this.actId = actId;
        this.memId = memId;
    }
    
    public Integer getActId() {
        return actId;
    }
    
    public void setActId(Integer actId) {
        this.actId = actId;
    }
    
    public Integer getMemId() {
        return memId;
    }
    
    public void setMemId(Integer memId) {
        this.memId = memId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantId that = (ParticipantId) o;
        return Objects.equals(actId, that.actId) && Objects.equals(memId, that.memId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(actId, memId);
    }
    
    @Override
    public String toString() {
        return "ParticipantId{" +
                "actId=" + actId +
                ", memId=" + memId +
                '}';
    }
} 