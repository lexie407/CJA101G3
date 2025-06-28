package com.toiukha.forum.article.model;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.dto.ArticleSearchCriteria;
import com.toiukha.forum.article.entity.Article;
import org.springframework.data.domain.Page;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public interface ArticleService {
    List<Article> getAll();
//    List<ArticleDTO> getAllDTO();
//    根據文章類別找資料
    List<Article> getByCategory(Byte artCat);
    // 根據文章標題找
    List<Article> getByTitle(String title);
    // 根據會員編號找
    List<Article> getByAuthorId(Integer artHol);
    // 根據文章上下架狀態找
    List<Article> getByStatus(Byte artSta);

    // 進階查詢
    List<Article> searchArticlesByCriteria(ArticleSearchCriteria criteria);


    Article getArticleById (Integer id);
//    ArticleDTO getDTOById(Integer id);

    Article add(Article artVO);
    Article add(Byte artCat, Byte artSta, Integer artHol
            , String artTitle, String artCon);

    Article update(Integer artId, Byte artCat, Byte artSta, Integer artHol, Integer artLike
            , String artTitle, String artCon, Timestamp artCreTime);
    Article updateBasic(Integer artId, Byte artCat, Byte artSta, String artTitle, String artCon);


    void update(Article artVO);
    void delete(Integer artId);

    Page<ArticleDTO> getAllPagedDTO(int page, int size, String sortBy, ArticleServiceImpl.SortDirection order);

    List<ArticleDTO> getAllDTO(String sortBy, String sortDirection);

    ArticleDTO getDTOById(Integer id);
}
