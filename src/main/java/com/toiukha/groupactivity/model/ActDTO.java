package com.toiukha.groupactivity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活動資料傳輸物件-進行欄位錯誤驗證
 */
public class ActDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ===== 資料欄位 =====

    private Integer actId;

    @NotBlank(message="活動名稱: 請勿空白")
    @Size(max = 255)
    private String actName;

    @Size(max = 1000, message = "活動描述過長")
    private String actDesc;

    // 前端以 base64 傳入圖片
    // 後端以 byte[] 傳入圖片
    private String actImgBase64; // 前端傳送的 base64 字串
    @JsonIgnore
    private byte[] actImg;

    @NotNull(message="行程編號: 請勿空白")
    private Integer itnId;

    // @NotNull(message="團主編號: 請勿空白")  // 由後端從session設定，無需前端驗證
    private Integer hostId;

    @NotNull(message = "報名開始時間不得為空")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime signupStart;

    @NotNull(message = "報名截止時間不得為空")
    //@Future(message="日期必須是在今日(不含)之後")
    private LocalDateTime signupEnd;

    @NotNull(message = "活動人數最少三人!")
    @Min(3)
    private Integer maxCap;

    //報名人數由程式計算
    private Integer signupCnt;

    @NotNull(message = "活動開始時間不得為空")
    @Future(message="日期必須是在今日(不含)之後")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime actStart;

    @NotNull(message = "活動結束時間不得為空")
    @Future(message="日期必須是在今日(不含)之後")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime actEnd;

    //0:不公開, 1:公開。預設為 0
    private Byte isPublic;

    //0:不允許, 1:允許。預設為 0
    private Byte allowCancel;

    //0 = 招募中，1 = 額滿，2 = 截止，3 = 取消，4 = 凍結，5 = 結束。允許null
    private Byte recruitStatus;

    // 活動標籤（不儲存到資料庫，存入Redis）
    private String actType;  // 活動類型
    private String actCity;  // 活動縣市

    // 行程名稱（顯示用）
    private String itnName;

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

    public String getActImgBase64() {
        return actImgBase64;
    }

    public void setActImgBase64(String actImgBase64) {
        this.actImgBase64 = actImgBase64;
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

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public String getActCity() {
        return actCity;
    }

    public void setActCity(String actCity) {
        this.actCity = actCity;
    }

    public String getItnName() {
        return itnName;
    }

    public void setItnName(String itnName) {
        this.itnName = itnName;
    }

    //活動狀態設定與檢查

    public ActStatus getRecruitStatusEnum() {
        return ActStatus.fromValueOrNull(recruitStatus);
    }

    public void setRecruitStatusEnum(ActStatus status) {
        this.recruitStatus = status != null ? status.getValue() : null;
    }

    public boolean isRecruiting() {
        ActStatus status = getRecruitStatusEnum();
        return status != null && status.isOpen();
    }

    public boolean isFull() {
        ActStatus status = getRecruitStatusEnum();
        return status != null && status.isFull();
    }

    public boolean canSignUp() {
        ActStatus status = getRecruitStatusEnum();
        return status != null && status.canSignUp();
    }

    //活動標籤便利方法
    public ActTag getTypeTag() {
        try {
            return ActTag.valueOf(actType);
        } catch (Exception e) {
            return ActTag.OUTDOOR;
        }
    }

    public ActTag getCityTag() {
        try {
            return ActTag.valueOf(actCity);
        } catch (Exception e) {
            return ActTag.TAIPEI;
        }
    }

    public void setTypeTag(ActTag tag) {
        this.actType = tag != null ? tag.name() : ActTag.OUTDOOR.name();
    }

    public void setCityTag(ActTag tag) {
        this.actCity = tag != null ? tag.name() : ActTag.TAIPEI.name();
    }

    // =======前端特別商業驗證邏輯=======

    //開放招募時需填寫必要欄位
    @AssertTrue(message = "開放招募時需填寫名稱、開始/結束時間與名額")
    public boolean isOpenFieldsValid() {
        if (recruitStatus != null && recruitStatus == ActStatus.OPEN.getValue()) {
            return actName != null && !actName.isBlank()
                    && actStart != null && actEnd != null && maxCap != null;
        }
        return true;
    }

    // 招募時間邏輯-截止期限
    @AssertTrue(message = "截止時間必須晚於開始時間")
    public boolean isSignUpEndAfterStart() {
        if (signupStart != null && signupEnd != null) {
            return signupEnd.isAfter(signupStart);
        }
        return true;
    }

    // 招募時間邏輯-截止期限
    @AssertTrue(message = "報名截止時間必須在活動開始之前")
    public boolean isActStartAfterSignup() {
        if (actStart != null && signupEnd != null) {
            return actStart.isAfter(signupEnd);
        }
        return true;
    }

    // 活動時間邏輯
    @AssertTrue(message = "活動結束時間必須晚於開始時間")
    public boolean isActEndAfterStart() {
        if (actStart != null && actEnd != null) {
            return actEnd.isAfter(actStart);
        }
        return true;
    }

    //人數上限不可小於目前已報名人數
    @AssertTrue(message = "人數上限不可小於目前已報名人數")
    public boolean isMaxCapValid() {
        if (maxCap != null && signupCnt != null) {
            return maxCap >= signupCnt;
        }
        return true;
    }

    //將 DTO 內容轉為 VO 以進行資料庫操作
    public ActVO toVO() {
        ActVO vo = new ActVO();
        vo.setActId(this.actId);
        vo.setActName(this.actName);
        vo.setActDesc(this.actDesc);

        // 處理圖片：如果有 base64 字串，轉換為 byte[]
        if (this.actImgBase64 != null && !this.actImgBase64.isEmpty()) {
            try {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(this.actImgBase64);
                vo.setActImg(imageBytes);
            } catch (Exception e) {
                // 如果 base64 解碼失敗，使用原有的 byte[]
                vo.setActImg(this.actImg);
            }
        } else {
            vo.setActImg(this.actImg);
        }

        vo.setItnId(this.itnId);
        vo.setHostId(this.hostId);
        vo.setSignupStart(this.signupStart);
        vo.setSignupEnd(this.signupEnd);
        vo.setMaxCap(this.maxCap);
        vo.setSignupCnt(this.signupCnt);
        vo.setActStart(this.actStart);
        vo.setActEnd(this.actEnd);
        vo.setIsPublic(this.isPublic);
        vo.setAllowCancel(this.allowCancel);
        vo.setRecruitStatus(this.recruitStatus);
        return vo;
    }
}
