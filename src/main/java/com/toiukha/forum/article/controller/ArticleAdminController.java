package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.dto.ArticleForm;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticleService;
import com.toiukha.forum.util.ArticleMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// 文章後台的controller
@Controller
@RequestMapping("/forum/admin")
@Validated
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

    // 顯示文章編輯頁面
    @PostMapping("/getOne_For_Update")
    public String getOne_For_Update(@RequestParam("artId") String artId, ModelMap model) {
        /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
        /*************************** 2.開始查詢資料 *****************************************/
        Article art = articleService.getOneById(Integer.valueOf(artId));
        ArticleForm form = ArticleMapper.toForm(art);
        /*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
        model.addAttribute("articleForm", form); //將查詢到的結果塞進Attribute
        model.addAttribute("currentPage", "forum"); //讓左側導覽列知道目前的頁面
        return "back-end/forum/update_art_input"; // 查詢完成後轉交update_art_input.html
    }

    // 處理文章更新的請求
    @PostMapping("/update")
    public String updateArticle(@ModelAttribute("articleForm") @Valid ArticleForm form, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("articleForm", form);
            model.addAttribute("error", "欄位錯誤！");
            return "back-end/forum/update_art_input";
        }

        // 將表單轉成 Entity
        Article art = articleService.getOneById(form.getArtId());
        ArticleMapper.updateEntity(art, form);
        articleService.update(art);

        return "redirect:/forum/admin/listAllAct"; //重新導向
    }



}
