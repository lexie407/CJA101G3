package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.model.ArticleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ArticleController {

    private final ArticleServiceImpl articleServiceImpl;

    // 使用建構子注入 ArticleService
    @Autowired
    public ArticleController(ArticleServiceImpl articleServiceImpl) {
        this.articleServiceImpl = articleServiceImpl;
    }

    @GetMapping("/articles")
    public List<ArticleDTO> getAllArticles(
            @RequestParam(defaultValue = "artId") String sortBy,
            @RequestParam(name = "order", defaultValue = "ASC") String sortDirection) {
        // 直接返回 ArticleDTO 列表
        return articleServiceImpl.getAllDTO(sortBy, sortDirection);
    }


}
