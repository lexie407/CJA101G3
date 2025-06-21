package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.dto.ArticleDTO;
//import com.toiukha.forum.article.model.ArticlePicturesServiceImpl;
import com.toiukha.forum.article.model.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


// 取得全部文章 DTO
    @GetMapping("/articles")
    public List<ArticleDTO> getAllArticles(
            @RequestParam(defaultValue = "artId") String sortBy,
            @RequestParam(name = "order", defaultValue = "ASC") String sortDirection) {
        // 直接返回 ArticleDTO 列表
        return articleService.getAllDTO(sortBy, sortDirection);
    }

    // 新增：取得單篇文章 DTO
    @GetMapping("/article/{artId}")
    public ArticleDTO getArticleDTO(@PathVariable Integer artId) {
        return articleService.getDTOById(artId);
    }

}