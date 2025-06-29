package com.toiukha.forum.article.dto;

import java.sql.Timestamp;

public class ArticleDTO {
    private Integer artId;         // 文章編號
    private Byte artCat;           // 文章類別
    private Byte artSta;           // 文章狀態
    private String mamName;        // 會員名字
    private Integer artLike;       // 文章按讚數
    private String artTitle;       // 文章標題
    private String artCon;         // 文章內容
    private Timestamp artCreTime;  // 文章建立時間

    // 無參數建構子（給Thymeleaf 或其他框用）
    public ArticleDTO() {
    }

    public ArticleDTO(Integer artId, Byte artCat, Byte artSta, String mamName,
                      Integer artLike, String artTitle, String artCon, Timestamp artCreTime) {
        this.artId = artId;
        this.artCat = artCat;
        this.artSta = artSta;
        this.mamName = mamName;
        this.artLike = artLike;
        this.artTitle = artTitle;
        this.artCon = artCon;
        this.artCreTime = artCreTime;
    }

    // Getter & Setter
    public Integer getArtId() {
        return artId;
    }

    public void setArtId(Integer artId) {
        this.artId = artId;
    }

    public Byte getArtCat() {
        return artCat;
    }

    public void setArtCat(Byte artCat) {
        this.artCat = artCat;
    }

    public Byte getArtSta() {
        return artSta;
    }

    public void setArtSta(Byte artSta) {
        this.artSta = artSta;
    }

    public String getMamName() {
        return mamName;
    }

    public void setMamName(String mamName) {
        this.mamName = mamName;
    }

    public Integer getArtLike() {
        return artLike;
    }

    public void setArtLike(Integer artLike) {
        this.artLike = artLike;
    }

    public String getArtTitle() {
        return artTitle;
    }

    public void setArtTitle(String artTitle) {
        this.artTitle = artTitle;
    }

    public String getArtCon() {
        return artCon;
    }

    public void setArtCon(String artCon) {
        this.artCon = artCon;
    }

    public Timestamp getArtCreTime() {
        return artCreTime;
    }

    public void setArtCreTime(Timestamp artCreTime) {
        this.artCreTime = artCreTime;
    }
}
