package com.toiukha.groupactivity.model;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "groupactivity")
public class GroupActivityVO implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTID", updatable = false)
    private Integer actId;

    @Column(name = "ACTNAME")
    private String actName;

    @Column(name = "ACTDESC")
    private String actDesc;

    @Column(name = "IMGPATH")
    private String imgPath;

    @Column(name = "ITNID")
    private Integer itnId;

    @Column(name = "HOSTID")
    private Integer hostId;

    @Column(name = "SIGNUPSTART")
    private Timestamp signupStart;

    @Column(name = "SIGNUPEND")
    private Timestamp signupEnd;

    @Column(name = "MAXCAP")
    private Integer maxCap;

    @Column(name = "SIGNUPCNT")
    private Integer signupCnt;

    @Column(name = "ACTSTART")
    private Timestamp actStart;

    @Column(name = "ACTEND")
    private Timestamp actEnd;

    @Column(name = "ISPUBLIC")
    private Byte isPublic;

    @Column(name = "ALLOWCANCEL")
    private Byte allowCancel;

    @Column(name = "RECRUITSTATUS")
    private Byte recruitStatus;


    public GroupActivityVO() {
        super();
    }


    public GroupActivityVO(Integer actId, String actName, String actDesc, String imgPath, Integer itnId, Integer hostId,
                           Timestamp signupStart, Timestamp signupEnd, Integer maxCap, Integer signupCnt, Timestamp actStart,
                           Timestamp actEnd, Byte isPublic, Byte allowCancel, Byte recruitStatus) {
        super();
        this.actId = actId;
        this.actName = actName;
        this.actDesc = actDesc;
        this.imgPath = imgPath;
        this.itnId = itnId;
        this.hostId = hostId;
        this.signupStart = signupStart;
        this.signupEnd = signupEnd;
        this.maxCap = maxCap;
        this.signupCnt = signupCnt;
        this.actStart = actStart;
        this.actEnd = actEnd;
        this.isPublic = isPublic;
        this.allowCancel = allowCancel;
        this.recruitStatus = recruitStatus;
    }


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
    public void setActDesc(String desc) {
        this.actDesc = desc;
    }
    public String getImgPath() {
        return imgPath;
    }
    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
    public Integer getItnId() {
        return itnId;
    }
    public void setItnId(Integer itnId) {
        this.itnId = itnId;
    }
    public Integer getHostId() {
        return hostId;
    }
    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }
    public Timestamp getSignupStart() {
        return signupStart;
    }
    public void setSignupStart(Timestamp signupStart) {
        this.signupStart = signupStart;
    }
    public Timestamp getSignupEnd() {
        return signupEnd;
    }
    public void setSignupEnd(Timestamp signupEnd) {
        this.signupEnd = signupEnd;
    }
    public Integer getMaxCap() {
        return maxCap;
    }
    public void setMaxCap(Integer maxCap) {
        this.maxCap = maxCap;
    }
    public Integer getSignupCnt() {
        return signupCnt;
    }
    public void setSignupCnt(Integer signupCnt) {
        this.signupCnt = signupCnt;
    }
    public Timestamp getActStart() {
        return actStart;
    }
    public void setActStart(Timestamp actStart) {
        this.actStart = actStart;
    }
    public Timestamp getActEnd() {
        return actEnd;
    }
    public void setActEnd(Timestamp actEnd) {
        this.actEnd = actEnd;
    }
    public Byte getIsPublic() {
        return isPublic;
    }
    public void setIsPublic(Byte isPublic) {
        this.isPublic = isPublic;
    }
    public Byte getAllowCancel() {
        return allowCancel;
    }
    public void setAllowCancel(Byte allowCancel) {
        this.allowCancel = allowCancel;
    }
    public Byte getRecruitStatus() {
        return recruitStatus;
    }
    public void setRecruitStatus(Byte recruitStatus) {
        this.recruitStatus = recruitStatus;
    }



}
