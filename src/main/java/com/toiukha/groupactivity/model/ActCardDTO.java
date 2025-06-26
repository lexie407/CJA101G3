package com.toiukha.groupactivity.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ActCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer actId;
    private String actName;
    private String actDesc;
    private LocalDateTime actStart;
    private Integer signupCnt;
    private Integer maxCap;
    private Byte recruitStatus;
    private Integer hostId; // 雖然卡片沒顯示，但未來可能需要

    public Integer getActId() {
        return actId;
    }

    public void setActId(Integer actId) {
        this.actId = actId;
    }

    public String getActName() {
        return actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public String getActDesc() {
        return actDesc;
    }

    public void setActDesc(String actDesc) {
        this.actDesc = actDesc;
    }

    public LocalDateTime getActStart() {
        return actStart;
    }

    public void setActStart(LocalDateTime actStart) {
        this.actStart = actStart;
    }

    public Integer getSignupCnt() {
        return signupCnt;
    }

    public void setSignupCnt(Integer signupCnt) {
        this.signupCnt = signupCnt;
    }

    public Integer getMaxCap() {
        return maxCap;
    }

    public void setMaxCap(Integer maxCap) {
        this.maxCap = maxCap;
    }

    public Byte getRecruitStatus() {
        return recruitStatus;
    }

    public void setRecruitStatus(Byte recruitStatus) {
        this.recruitStatus = recruitStatus;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }
} 