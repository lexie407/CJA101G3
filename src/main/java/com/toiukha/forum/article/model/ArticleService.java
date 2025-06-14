package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.Article;

import java.sql.Date;
import java.util.List;

public interface ArticleService {
    List<Article> getAll();
//    List<ArticleDTO> getAllDTO();


    Article getArticleById(Integer id);
//    ArticleDTO getDTOById(Integer id);

    Article add(Article artVO);
    Article add(Byte artCat, Byte artSta, Integer artHol
            , String artTitle, String artCon);

    Article update(Integer artId, Byte artCat, Byte artSta, Integer artHol, Integer artLike
            , String artTitle, String artCon, Date artCreTime);
    Article updateBasic(Integer artId, Byte artCat, Byte artSta, String artTitle, String artCon);


    void update(Article artVO);
    void delete(Integer artId);

}
