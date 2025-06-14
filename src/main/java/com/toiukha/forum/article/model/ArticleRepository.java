package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.toiukha.forum.article.dto.ArticleDTO;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Query(value = "SELECT ARTID as artId, ARTCAT as artCat, ARTSTA as artSta, m.MEMNAME as mamName, ARTLIKE as artLike, ARTTITLE as artTitle, ARTCON as artCon, ARTCRETIME as artCreTime FROM article a LEFT OUTER JOIN toiukha.members m on m.MEMID = a.ARTHOL WHERE ARTID = ? LIMIT 1", nativeQuery = true)
    ArticleDTO getDTOById(Integer id);

    @Query(value = "SELECT ARTID as artId, ARTCAT as artCat, ARTSTA as artSta, m.MEMNAME as mamName, ARTLIKE as artLike, ARTTITLE as artTitle, ARTCON as artCon, ARTCRETIME as artCreTime FROM article a LEFT OUTER JOIN toiukha.members m on m.MEMID = a.ARTHOL", nativeQuery = true)
    List<ArticleDTO> getAllDTO();
}