package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.model.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 文章後台的controller
@Controller
@RequestMapping("/forum/admin")
public class ArticleAdminController {

    private final ArticleService articleService;

    // 使用建構子注入 ArticleService
    @Autowired
    public ArticleAdminController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // 顯示文章後台首頁
    @GetMapping("")
    public String showAdminIndex(Model model) {
        model.addAttribute("message", "歡迎來到文章後台管理系統");
        return "back-end/forum/select_page";
    }

    // 顯示全部文章
    @GetMapping("/listAllAct")
    public String listAllArticles(Model model) {
        model.addAttribute("articleList", articleService.getAll());
        return "back-end/forum/listAllAct";
    }

    // 顯示文章後台的搜尋頁面
    @GetMapping("/selectPage")
    public String showSelectPage(Model model) {
        // 返回文章後台首頁的視圖名稱
        return "back-end/forum/select_page"; // 假設有一個名為 admin/article/index.html 的模板
    }

    // 文章後台的相關方法可以在這裡實現，例如新增文章、編輯文章、刪除文章等
    // 這些方法可以使用Spring MVC的註解來處理HTTP請求，例如@GetMapping, @PostMapping等

    // 例如：
    // @GetMapping("/admin/articles")
    // public String listArticles(Model model) {
    //     // 獲取文章列表並添加到模型中
    //     return "admin/article/list"; // 返回視圖名稱
    // }


}
