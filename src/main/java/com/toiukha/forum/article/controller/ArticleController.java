package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.dto.ArticleDTO;
//import com.toiukha.forum.article.model.ArticlePicturesServiceImpl;
import com.toiukha.forum.article.model.ArticleService;
//import org.hibernate.query.SortDirection;
import com.toiukha.forum.article.model.ArticleServiceImpl;
import com.toiukha.forum.util.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 文章相關的 RESTful API Controller
@RestController
public class ArticleController {

    private final ArticleService articleService;
//    private final ArticlePicturesServiceImpl apService;

    // 使用建構子注入 ArticleService
    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
//        this.apService = apService;
    }

// 新增：取得單篇文章 DTO
    @GetMapping("/article/{artId}")
    public ArticleDTO getArticleDTO(@PathVariable Integer artId) {
        Debug.log();
        return articleService.getDTOById(artId);
    }



// 取得全部文章 DTO(自訂排序)
    @GetMapping("/articles")
    public List<ArticleDTO> getAllArticles(
            @RequestParam(defaultValue = "ARTICLE_ID") String sortBy,
            @RequestParam(name = "order", defaultValue = "ASC") String sortDirection) {
        // 直接返回 ArticleDTO 列表
        return articleService.getAllDTO(sortBy, sortDirection);
    }


// 取得全部文章 DTO(支援分頁和自訂排序)
    @GetMapping("/articles/paged")
    public ResponseEntity<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "artCreTime") String sortBy,
            @RequestParam(name = "order",defaultValue = "DESC") ArticleServiceImpl.SortDirection dir
    ) {
        Page<ArticleDTO> articlePage = articleService.getAllPagedDTO(page, size, sortBy, dir);

        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());
        response.put("totalPages", articlePage.getTotalPages());
        response.put("currentPage", articlePage.getNumber());
        response.put("hasNext", articlePage.hasNext());

        return ResponseEntity.ok(response);
    }


}