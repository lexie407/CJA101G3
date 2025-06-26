package com.toiukha.forum.article.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDate;

// 搜尋用的class
public class ArticleSearchCriteria {
    @Min(value = 1, message = "文章編號需為正整數")
    @Max(value = 2147483647, message = "文章編號數值過大，請重新輸入")
    private Integer artId;
    @Min(value = 1, message = "會員編號需大於等於{value}")
    @Max(value = 2147483647, message = "會員編號數值過大，請重新輸入")
    private Integer artHol;
    private String artTitle;
    @Min(value = 1, message = "請選擇有效的文章分類")
    @Max(value = 3, message = "請選擇有效的文章分類")
    private Byte artCategory;
    @Min(value = 1, message = "請選擇有效的文章狀態")
    @Max(value = 2, message = "請選擇有效的文章狀態")
    private Byte artStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "起始日期必須是過去的日期")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "結束日期必須是過去的日期")
    private LocalDate  endDate;

    // Timestamp轉換
    // LocalDate轉成當天早上00:00:00
    public Timestamp getStartTimeAsTimestamp() {
        return (startDate != null) ? Timestamp.valueOf(startDate.atStartOfDay()) : null;
    }

    // LocalDate轉成當天晚上00:00:00
    public Timestamp getEndTimeAsTimestamp() {
        return (endDate != null) ? Timestamp.valueOf(endDate.atTime(23, 59, 59)) : null;
    }

    public Integer getArtId() {
        return artId;
    }

    public void setArtId(Integer artId) {
        this.artId = artId;
    }

    public Integer getArtHol() {
        return artHol;
    }

    public void setArtHol(Integer artHol) {
        this.artHol = artHol;
    }

    public Byte getArtCategory() {
        return artCategory;
    }

    public void setArtCategory(Byte artCategory) {
        this.artCategory = artCategory;
    }

    public Byte getArtStatus() {
        return artStatus;
    }

    public void setArtStatus(Byte artStatus) {
        this.artStatus = artStatus;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getArtTitle() {
        return artTitle;
    }

    public void setArtTitle(String artTitle) {
        this.artTitle = artTitle;
    }
}
