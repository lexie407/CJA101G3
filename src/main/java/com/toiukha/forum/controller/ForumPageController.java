package com.toiukha.forum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/forum")
public class ForumPageController {

    @GetMapping
    public String index() {
        // 返回 forum-index.html 頁面
        return "front-end/forum/forum-index";
    }

    @GetMapping("/article")
    public String article() {
        return "front-end/forum/forum-article";
    }

    @GetMapping("/article/edit")
    public String editArticle(){
        return "front-end/forum/addArticle";
    }

}
