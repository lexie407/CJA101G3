package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import com.toiukha.forum.article.dto.ArticleDTO;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Integer>, JpaSpecificationExecutor<Article> {

    @Query(value = "SELECT ARTID as artId, ARTCAT as artCat, ARTSTA as artSta, m.MEMNAME as mamName, ARTLIKE as artLike, ARTTITLE as artTitle, ARTCON as artCon, ARTCRETIME as artCreTime FROM article a LEFT OUTER JOIN toiukha.members m on m.MEMID = a.ARTHOL WHERE ARTID = ? LIMIT 1", nativeQuery = true)
    ArticleDTO getDTOById(Integer id);

    @Query(value = "SELECT ARTID as artId, ARTCAT as artCat, ARTSTA as artSta, m.MEMNAME as mamName, ARTLIKE as artLike, ARTTITLE as artTitle, ARTCON as artCon, ARTCRETIME as artCreTime FROM article a LEFT OUTER JOIN toiukha.members m on m.MEMID = a.ARTHOL", nativeQuery = true)
    List<ArticleDTO> getAllDTO();

    // 用原生SQL指令查詢DTO，並使用分頁功能
    // """ 是 Java 15 引入的多行字串語法，可以讓 SQL 語句更易讀
    @Query(value = """
            SELECT ARTID as artId, ARTCAT as artCat, ARTSTA as artSta, m.MEMNAME as mamName, ARTLIKE as artLike, ARTTITLE as artTitle, ARTCON as artCon, ARTCRETIME as artCreTime
            FROM article a
            LEFT JOIN members m ON m.MEMID = a.ARTHOL
            """,
            countQuery = """
            SELECT COUNT(*) FROM article a
            LEFT JOIN members m ON m.MEMID = a.ARTHOL
            """,
            nativeQuery = true)
    Page<ArticleDTO> getAllPagedDTO(Pageable pageable);


    // 根據文章分類查詢
    List<Article> findByArtCat(Byte artCat); // 對應欄位名稱 artCat

    // 方法命名查詢
    List<Article> findByArtHol(Integer artHol); // 根據會員編號查詢

    List<Article> findByArtSta(Byte artSta);

    // 方法命名查詢：模糊比對 LIKE %keyword%
    List<Article> findByArtTitleContaining(String keyword);

    // 給標題查詢用
    List<Article> findByArtHolAndArtCatAndArtStaIn(Integer artHol, Byte artCat, List<Byte> artStaList);

    List<Article> findByArtHolAndArtCatInAndArtStaIn(Integer artHol, List<Byte> artCatList, List<Byte> artStaList);

}