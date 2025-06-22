package com.toiukha.forum.article.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ArticleForm {

    private Integer artId;  // 文章編號

    @NotNull(message = "請選擇文章類別")
    private Byte artCat;    // 文章類別

    @NotNull(message = "會員編號: 請勿空白")
    @Min(value = 1, message = "會員編號: 不能小於{value}")
    private Integer artHol;  // 會員編號

    @NotNull(message = "請選擇文章狀態")
    private Byte artSta;    // 文章狀態

    @NotBlank(message = "文章標題: 請勿空白")
    private String artTitle; // 文章標題

    @NotBlank(message = "文章內容: 請勿空白")
    private String artCon;   // 文章內容

    public Integer getArtId() {return artId;}

    public void setArtId(Integer artId) {this.artId = artId;}

    public Byte getArtCat() {return artCat;}

    public void setArtCat(Byte artCat) {this.artCat = artCat;}

    public Integer getArtHol() {return artHol;}

    public void setArtHol(Integer artHol) {this.artHol = artHol;}

    public Byte getArtSta() {return artSta;}

    public void setArtSta(Byte artSta) {this.artSta = artSta;}

    public String getArtTitle() {return artTitle;}

    public void setArtTitle(String artTitle) {this.artTitle = artTitle;}

    public String getArtCon() {return artCon;}

    public void setArtCon(String artCon) {this.artCon = artCon;}
}
