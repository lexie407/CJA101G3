package com.toiukha.forum.controller;

import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticleService;
import com.toiukha.forum.util.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// 文章相關的頁面Controller
@Controller
@RequestMapping("/forum")
public class ForumPageController {

    @Autowired
    ArticleService articleService;

    @GetMapping
    public String index() {
        // 返回 forum-index.html 頁面
        return "front-end/forum/forum-index";
    }

    // 新增文章的處理
    @PostMapping("/article/insert")
    public String insertArticle(@ModelAttribute Article art) {
        if (art == null) {
            Debug.log("文章資料為空，無法新增文章");
            return "redirect:/forum/article/edit"; // 或返回錯誤頁面
        }
        // 假設你已經有 setArtCon、setArtTitle 等 getter/setter
        Debug.log("文章標題：" + art.getArtTitle(),
                "文章分類：" + art.getArtCat(),
                "文章上架狀態：" + art.getArtSta(),
                "文章內容：" + art.getArtCon());

        // 存進資料庫
        articleService.add(art.getArtCat(),art.getArtSta(),art.getArtHol(),art.getArtTitle(),art.getArtCon()); // 假設你有 service 層
        return "redirect:/forum"; // 或返回成功頁面
    }

//    @PostMapping("/article/insert")
//    public String insertArticle(
//            @RequestParam("artTitle") String artTitle,
//            @RequestParam("artCat") Byte artCat,
//            @RequestParam("artHol") Integer artHol,
//            @RequestParam("artCon") String artCon
//    ) {
//        // 呼叫 Service 儲存
////        articleServiceImpl.add(artCat, (byte)1, 1, artTitle, artCon);
//        Debug.log("artTitle: " + artTitle,
//                "artCat: " + artCat,
//                "artHol: " + artHol,
//                "artCon: " + artCon);
//
//        //新增文章，預設上架
//        articleService.add(artCat, (byte)1, artHol, artTitle, artCon);
//        // 新增成功後導回文章列表
//        return "redirect:/forum";
//    }


    @GetMapping("/article")
    public String article() {
        return "front-end/forum/forum-article";
    }

    // 導向新增文章頁面
    @GetMapping("/article/edit")
    public String editArticle(){
        return "front-end/forum/add-article";

    }

}
