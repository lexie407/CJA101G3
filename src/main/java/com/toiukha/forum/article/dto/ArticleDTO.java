package com.toiukha.forum.article.dto;

// 給使用者「讀取」用的資料格式，使用Spring Data JPA 的 Interface-based Projections
// https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html
public interface ArticleDTO {
    Integer getArtId();  //文章編號
    Byte getArtCat();	//文章類別
    Byte getArtSta();	//文章狀態
    String getMamName(); //會員名字
    Integer getArtLike();	//文章按讚數
    String getArtTitle();   //文章標題
    String getArtCon(); //文章內容
    java.sql.Timestamp getArtCreTime();	//文章建立時間
}
