package com.toiukha.groupactivity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 活動資料庫對應物件
 * 純粹的資料庫對應，不包含業務邏輯
 */
@Entity
@Table(name = "groupactivity")
public class ActVO implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTID", updatable = false, nullable = false)
    private Integer actId;

    @Column(name = "ACTNAME")
    private String actName;

    @Column(name = "ACTDESC")
    private String actDesc;

    @JsonIgnore
    @Column(name = "ACTIMG")
    @Lob
    private byte[] actImg;

    @Column(name = "ITNID", nullable = false)
    private Integer itnId;

    @Column(name = "HOSTID", nullable = false)
    private Integer hostId;

    @Column(name = "SIGNUPSTART")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime signupStart;

    @Column(name = "SIGNUPEND")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime signupEnd;

    @Column(name = "MAXCAP")
    private Integer maxCap;

    @Column(name = "SIGNUPCNT")
    private Integer signupCnt;

    @Column(name = "ACTSTART")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime actStart;

    @Column(name = "ACTEND")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime actEnd;

    @Column(name = "ISPUBLIC")
    private Byte isPublic;

    @Column(name = "ALLOWCANCEL")
    private Byte allowCancel;

    @Column(name = "RECRUITSTATUS")
    private Byte recruitStatus;

    // 日期格式化輔助方法
    public String getFormattedSignupStart() {
        return signupStart != null ? signupStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getFormattedSignupEnd() {
        return signupEnd != null ? signupEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getFormattedActStart() {
        return actStart != null ? actStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getFormattedActEnd() {
        return actEnd != null ? actEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    /** 參加者關聯(待補) */
//    @OneToMany(mappedBy = "act", cascade = CascadeType.ALL, orphanRemoval = true)
//     private List<ParticipantVO> participants = new ArrayList<>();

//  老師的範例
//  private DeptVO deptVO;
//  @ManyToOne  (雙向多對一/一對多) 的多對一
//	//【此處預設為 @ManyToOne(fetch=FetchType.EAGER) --> 是指 lazy="false"之意】【注意: 此處的預設值與XML版 (p.127及p.132) 的預設值相反】
//	//【如果修改為 @ManyToOne(fetch=FetchType.LAZY)  --> 則指 lazy="true" 之意】
//	@ManyToOne
//	@JoinColumn(name = "DEPTNO")   // 指定用來join table的column
//	public DeptVO getDeptVO() {
//		return this.deptVO;
//	}
//
//	public void setDeptVO(DeptVO deptVO) {
//		this.deptVO = deptVO;
//	}

    // ===== Contructor =====

    public ActVO() {
        super();
    }


    public ActVO(Integer actId, String actName, String actDesc, byte[] actImg, Integer itnId, Integer hostId,
                 LocalDateTime signupStart, LocalDateTime signupEnd, Integer maxCap, Integer signupCnt, LocalDateTime actStart,
                 LocalDateTime actEnd, Byte isPublic, Byte allowCancel, Byte recruitStatus) {
        super();
        this.actId = actId;
        this.actName = actName;
        this.actDesc = actDesc;
        this.actImg = actImg;
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

    // ===== Getter / Setter =====

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
    public byte[] getActImg() {
        return actImg;
    }
    public void setActImg(byte[] actImg) {
        this.actImg = actImg;
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
    public LocalDateTime getSignupStart() {
        return signupStart;
    }
    public void setSignupStart(LocalDateTime signupStart) {
        this.signupStart = signupStart;
    }
    public LocalDateTime getSignupEnd() {
        return signupEnd;
    }
    public void setSignupEnd(LocalDateTime signupEnd) {
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
    public LocalDateTime getActStart() {
        return actStart;
    }
    public void setActStart(LocalDateTime actStart) {
        this.actStart = actStart;
    }
    public LocalDateTime getActEnd() {
        return actEnd;
    }
    public void setActEnd(LocalDateTime actEnd) {
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

    @Override
    public String toString() {
        return "ActVO [actId=" + actId + ", actName=" + actName + ", actDesc=" + actDesc + ", itnId=" + itnId
                + ", hostId=" + hostId + ", signupStart=" + signupStart + ", signupEnd=" + signupEnd + ", maxCap="
                + maxCap + ", signupCnt=" + signupCnt + ", actStart=" + actStart + ", actEnd=" + actEnd + ", isPublic="
                + isPublic + ", allowCancel=" + allowCancel + ", recruitStatus=" + recruitStatus + "]";
    }
}
