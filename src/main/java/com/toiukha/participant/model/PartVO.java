package com.toiukha.participant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.members.model.MembersVO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 參加者資料 VO，對應資料庫表格 participant
 * 使用複合主鍵 (ACTID, MEMID)
 */
@Entity
@Table(name = "participant")
@IdClass(PartVO.CompositeKey.class)
public class PartVO implements Serializable {
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

    // ===== 複合主鍵內部類別 =====
    
    /**
     * PartVO 的複合主鍵類別
     */
    static class CompositeKey implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer actId;
        private Integer memId;
        
        // 無參數建構函數（必須）
        public CompositeKey() {
        }
        
        public CompositeKey(Integer actId, Integer memId) {
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
        
        // 必須實現 equals() 和 hashCode()
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompositeKey that = (CompositeKey) o;
            return Objects.equals(actId, that.actId) && Objects.equals(memId, that.memId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(actId, memId);
        }
        
        @Override
        public String toString() {
            return "CompositeKey{" +
                    "actId=" + actId +
                    ", memId=" + memId +
                    '}';
        }
    }

    // ===== Constructor =====

    public PartVO() {
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
