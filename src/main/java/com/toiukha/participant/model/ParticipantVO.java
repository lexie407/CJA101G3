package com.toiukha.participant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.members.model.MembersVO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 參加者資料 VO，對應資料庫表格 participant
 * 使用複合主鍵 (ACTID, MEMID)
 */
@Entity
@Table(name = "participant")
@IdClass(ParticipantId.class)
public class ParticipantVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "ACTID", nullable = false)
    private Integer actId;

    @Id
    @Column(name = "MEMID", nullable = false)
    private Integer memId;

    /** 對應的活動 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTID", insertable = false, updatable = false)
    private ActVO actVO;

    /** 會員實體關聯 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMID", insertable = false, updatable = false)
    private MembersVO memVO;

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

    public MembersVO getMemVO() {
        return memVO;
    }

    public void setMemVO(MembersVO member) {
        this.memVO = member;
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

    /**
     * 團員參加狀態
     * 1 = 已參加(報名成功)
     * 2 = 已剔除(團主剔除)
     * 3 = 已退出(自己退出)
     */
    public enum JoinStatus {
        JOINED((byte)1),
        REMOVED((byte)2),
        QUIT((byte)3);
        private final byte value;
        JoinStatus(byte value) { this.value = value; }
        public byte getValue() { return value; }
        public static JoinStatus fromValue(byte value) {
            for (JoinStatus s : values()) if (s.value == value) return s;
            return null;
        }
    }
}
