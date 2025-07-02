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
    private Byte isPublic; // 公開狀態
    private Boolean isCurrentUserParticipant; // 當前用戶是否已參與

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

    /**
     * 取得RecruitStatus Enum（如果recruitStatus為null則回傳null）
     */
    public ActStatus getRecruitStatusEnum() {
        return ActStatus.fromValueOrNull(recruitStatus);
    }

    /**
     * 設定RecruitStatus Enum
     */
    public void setRecruitStatusEnum(ActStatus status) {
        this.recruitStatus = status != null ? status.getValue() : null;
    }

    /**
     * 取得狀態顯示名稱
     */
    public String getRecruitStatusDisplayName() {
        ActStatus status = getRecruitStatusEnum();
        return status != null ? status.getDisplayName() : "未知狀態";
    }

    /**
     * 取得狀態CSS類別
     */
    public String getRecruitStatusCssClass() {
        ActStatus status = getRecruitStatusEnum();
        return status != null ? status.getCssClass() : "ended";
    }

    /**
     * 檢查是否為招募中狀態
     */
    public boolean isRecruiting() {
        ActStatus status = getRecruitStatusEnum();
        return status != null && status.isOpen();
    }

    /**
     * 檢查是否為成團狀態
     */
    public boolean isFull() {
        ActStatus status = getRecruitStatusEnum();
        return status != null && status.isFull();
    }

    /**
     * 檢查是否可以報名
     */
    public boolean canSignUp() {
        ActStatus status = getRecruitStatusEnum();
        return status != null && status.canSignUp();
    }

    public Byte getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Byte isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsCurrentUserParticipant() {
        return isCurrentUserParticipant;
    }

    public void setIsCurrentUserParticipant(Boolean isCurrentUserParticipant) {
        this.isCurrentUserParticipant = isCurrentUserParticipant;
    }

    // ===== Utility Methods =====
    
    /**
     * 從 ActVO 轉換為 ActCardDTO
     */
    public static ActCardDTO fromVO(ActVO actVO) {
        ActCardDTO dto = new ActCardDTO();
        dto.setActId(actVO.getActId());
        dto.setActName(actVO.getActName());
        dto.setActDesc(actVO.getActDesc());
        dto.setActStart(actVO.getActStart());
        dto.setSignupCnt(actVO.getSignupCnt() != null ? actVO.getSignupCnt() : 0);
        dto.setMaxCap(actVO.getMaxCap());
        dto.setRecruitStatus(actVO.getRecruitStatus());
        dto.setHostId(actVO.getHostId());
        dto.setIsPublic(actVO.getIsPublic());
        dto.setIsCurrentUserParticipant(null); // 預設為 null，需要時另外設定
        return dto;
    }
} 