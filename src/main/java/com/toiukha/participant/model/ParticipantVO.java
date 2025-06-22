package com.toiukha.participant.model;

import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.members.model.MembersVO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 參加者資料 VO，對應資料庫表格 participant
 * 使用複合主鍵 (ACTID, MEMID)
 */
@Entity
@Table(name = "participant")
@IdClass(ParticipantId.class)
public class ParticipantVO {
    
    @Id
    @Column(name = "ACTID", nullable = false)
    private Integer actId;

    @Id
    @Column(name = "MEMID", nullable = false)
    private Integer memId;

    /** 對應的活動 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTID", insertable = false, updatable = false)
    private ActVO actVO;

    /** 會員實體關聯 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMID", insertable = false, updatable = false)
    private MembersVO member;

    @Column(name = "MEMTYPE")
    private String memType;

    @Column(name = "JOINTIME")
    private LocalDateTime joinTime;

    @Column(name = "JOINSTATUS")
    private Byte joinStatus;

    // ===== Constructor =====

    public ParticipantVO() {
        super();
    }

    // ===== Getter / Setter =====

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

    public ActVO getActVO() {
        return actVO;
    }

    public void setActVO(ActVO actVO) {
        this.actVO = actVO;
    }

    public MembersVO getMember() {
        return member;
    }

    public void setMember(MembersVO member) {
        this.member = member;
    }

    public String getMemType() {
        return memType;
    }

    public void setMemType(String memType) {
        this.memType = memType;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public Byte getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(Byte joinStatus) {
        this.joinStatus = joinStatus;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "ParticipantVO{" +
                "actId=" + actId +
                ", memId=" + memId +
                ", memType='" + memType + '\'' +
                ", joinTime=" + joinTime +
                ", joinStatus=" + joinStatus +
                '}';
    }
}
