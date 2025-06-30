package com.toiukha.forum.article.model;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.dto.ArticleSearchCriteria;
import com.toiukha.forum.article.entity.Article;
import org.springframework.data.domain.Page;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public interface ArticleService {
    /******************* 實體 *******************/

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



    /******************* DTO *******************/

    // 取得前台首頁文章的DTO列表(有分頁功能)
    Page<ArticleDTO> getAllPagedDTO(int page, int size, String sortBy, ArticleServiceImpl.SortDirection order, byte artSta);

    List<ArticleDTO> getAllDTO(String sortBy, String sortDirection);

    // 根據關鍵字搜尋文章，並返回排序後 ArticleDTO 列表
    List<ArticleDTO> searchDTO(String keyword, String sortBy, String sortDirection);

    ArticleDTO getDTOById(Integer id);

    // 依照會員、單一分類跟狀態的查詢
    List<ArticleDTO> findByHolAndCatAndSta(Integer artHol, Byte artCat, List<Byte> artStaList);

    // 依照會員、多個分類跟狀態的查詢
    List<ArticleDTO> findByHolAndCatInAndSta(Integer artHol, List<Byte> artCatList, List<Byte> artStaList);
}
