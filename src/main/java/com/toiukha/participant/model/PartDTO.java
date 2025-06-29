package com.toiukha.participant.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 參加者資料傳輸物件，用於 API 回傳
 * 不包含關聯物件，避免序列化問題
 */
public class PartDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer actId;
    private Integer memId;
    private String memType;
    private LocalDateTime joinTime;
    private Byte joinStatus;
    private String memName; // 會員姓名

    // ===== Constructor =====
    
    public PartDTO() {
        super();
    }
    
    public PartDTO(Integer actId, Integer memId, String memType,
                   LocalDateTime joinTime, Byte joinStatus) {
        this.actId = actId;
        this.memId = memId;
        this.memType = memType;
        this.joinTime = joinTime;
        this.joinStatus = joinStatus;
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
    
    public String getMemName() {
        return memName;
    }
    
    public void setMemName(String memName) {
        this.memName = memName;
    }
    
    // ===== Utility Methods =====
    
    /**
     * 從 ParticipantVO 轉換為 ParticipantDTO
     */
    public static PartDTO fromVO(PartVO vo) {
        return new PartDTO(
            vo.getActId(),
            vo.getMemId(),
            vo.getMemType(),
            vo.getJoinTime(),
            vo.getJoinStatus()
        );
    }
    
    public static PartDTO fromVO(PartVO vo, String memName) {
        PartDTO dto = fromVO(vo);
        dto.setMemName(memName);
        return dto;
    }
    
    @Override
    public String toString() {
        return "ParticipantDTO{" +
                "actId=" + actId +
                ", memId=" + memId +
                ", memType='" + memType + '\'' +
                ", joinTime=" + joinTime +
                ", joinStatus=" + joinStatus +
                ", memName='" + memName + '\'' +
                '}';
    }
} 