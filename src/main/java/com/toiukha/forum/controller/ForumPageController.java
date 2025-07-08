package com.toiukha.forum.controller;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticlePicturesService;
import com.toiukha.forum.article.model.ArticleService;
import com.toiukha.forum.article.model.ArticleStatus;
import com.toiukha.forum.util.Debug;
import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toiukha.forum.article.model.ArticleServiceImpl.ArticleSortField.ARTICLE_CREATINE;
import static com.toiukha.forum.article.model.ArticleServiceImpl.SortDirection.DESC;

// 文章相關的頁面Controller
/* 這個Controller負責處理討論區前台相關的頁面請求，包括首頁、文章新增、文章查詢等功能
    PS.前後端分離的做法，不會有 return string，會通通用 URL 導向，然後 JS 用 fetch 去跟 RESTful API Controller 拿資料。
 */
@Controller
@RequestMapping("/forum")
public class ForumPageController {

    @Autowired
    ArticleService articleService;

    @Autowired
    ArticlePicturesService apService;

    // 從HTML中提取圖片ID
    public List<Integer> getImageIdsFromHtml(String html) {
        List<Integer> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("/forum/artImage/(\\d+)");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            result.add(Integer.parseInt(matcher.group(1)));
        }
        return result;
    }

    // 討論區首頁
    @GetMapping
    public String index(Model model) {
        model.addAttribute("currentPage", "forum");

        // 這不是導向 static 裡的 forum-index.html，
        // 而是會由Thymeleaf解析 /templates/front-end/forum/forum-index.html 並進行渲染
        return "front-end/forum/forum-index";
    }

    // 新增文章的處理與重導向
    @PostMapping("/article/insert")
    public String insertArticle(@ModelAttribute Article art, Model model, HttpSession session) {

        model.addAttribute("currentPage", "forum");
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null) {
            Debug.log("未登入會員，無法新增文章");
            return "redirect:/login";
        }
        if (art == null) {
            Debug.log("文章資料為空，無法新增文章");
            return "redirect:/forum/article/edit";
        }

        // ======= 手動驗證區塊 =======
        List<String> errors = new ArrayList<>();
        if (art.getArtCat() == null) errors.add("請選擇分類");
        if (art.getArtSta() == null) errors.add("請選擇狀態");
        if (art.getArtTitle() == null || art.getArtTitle().trim().isEmpty()) errors.add("標題不可空白");
        if (art.getArtCon() == null || art.getArtCon().trim().isEmpty()) errors.add("內容不可空白");

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("articleForm", art); // 回傳原本輸入的內容
            return "front-end/forum/add-article";
        }

        Debug.log("文章標題：" + art.getArtTitle(),
                "文章分類：" + art.getArtCat(),
                "文章上架狀態：" + art.getArtSta(),
                "文章內容：" + art.getArtCon());

        // 存進資料庫
        Article newArt = articleService.add(art.getArtCat(),art.getArtSta(), member.getMemId(), art.getArtTitle(),art.getArtCon());

        // 若有文章圖片，圖片綁定文章ID
        List<Integer> picIds = getImageIdsFromHtml(art.getArtCon());
        if (!picIds.isEmpty()) {
            apService.bindPicturesToArticle(picIds, newArt.getArtId());
            Debug.log("綁定圖片ID：" + picIds + " 到文章ID：" + newArt.getArtId());
        } else {
            Debug.log("沒有圖片需要綁定到文章");
        }


        // 是Spring MVC的重導向方法，會將請求轉發到指定的URL
        return "redirect:/forum";
    }

    // 只作為導向用，forum-article頁面會由JS取出查詢參數（Query Parameter）從ArticleController的getArticleDTO取得單篇文章內容並更新
    @GetMapping("/article")
    public String article(Model model) {
        model.addAttribute("currentPage", "forum");

        // 用 return 不會導向 static 檔案
        return "front-end/forum/forum-article";
    }

    // 導向新增文章頁面
    @GetMapping("/article/add")
    public String addArticle(Model model){
        model.addAttribute("currentPage", "forum");
        return "front-end/forum/add-article";
    }

    // 導向編輯文章頁面
    @GetMapping("/article/edit")
    public String editArticle(Model model){
        model.addAttribute("currentPage", "forum");

        return "front-end/forum/edit-article";
    }

    // 使用標題關鍵字搜尋文章，因為會查到上下架資料所以另開API
    @GetMapping("searchByTitle")
    public String searchByTitle(
//            @NotBlank(message = "文章標題: 請勿空白")
            @RequestParam("keyword") String title, Model model) {
        model.addAttribute("currentPage", "forum");

        // 參數驗證
        if (title == null || title.isBlank()) {
            model.addAttribute("errorMessage", "請輸入搜尋關鍵字！");
            model.addAttribute("articleList", List.of());
            model.addAttribute("keyword", title);
            return "front-end/forum/search_Page";
        }

        List<ArticleDTO> articles = articleService.searchDTO(title,ARTICLE_CREATINE.name(), DESC.name());

        if (articles.isEmpty()) {
            model.addAttribute("errorMessage", "沒有相符結果");
            model.addAttribute("articleList", List.of());
        } else {
            model.addAttribute("articleList", articles);
        }
        model.addAttribute("keyword", title);
        return "front-end/forum/search_Page";
    }

    // 使用標題關鍵字搜尋文章，因為會查到上下架資料所以另開API
    @GetMapping("search")
    public String search(
            @RequestParam(value = "keyword", required = false) String title,
            @RequestParam(value = "id", required = false) Integer id,
            Model model) {
        model.addAttribute("currentPage", "forum");

        if ((title == null || title.isBlank()) && id == null) {
            model.addAttribute("errorMessage", "請輸入關鍵字或文章 ID！");
            model.addAttribute("articleList", List.of());
            return "front-end/forum/search_Page";
        }

        List<ArticleDTO> results = new ArrayList<>();

        // 如果有提供文章 ID，則查詢單篇文章
        if (id != null) {
            ArticleDTO articleDTO = articleService.getDTOById(id);
            if (articleDTO != null && articleDTO.getArtSta() == ArticleStatus.PUBLISHED.getValue()) {
                results.add(articleDTO);
            }
        } else {
            // 如果沒有提供文章 ID，則根據標題關鍵字搜尋
            results = articleService.searchDTO(title,ARTICLE_CREATINE.name(), DESC.name(),ArticleStatus.PUBLISHED);
        }

        if (results.isEmpty()) {
            model.addAttribute("errorMessage", "沒有相符結果");
        }

        model.addAttribute("articleList", results);
        model.addAttribute("keyword", title);
        return "front-end/forum/search_Page";
    }


    // 會員專屬文章與問題查詢（從 session 取得會員編號）
    @GetMapping("/members/articles")
    public String getMemberArticlesFromSession(HttpSession session, Model model) {
        model.addAttribute("currentPage", "forum"); //先設定是討論區頁面
        
        // 從 session 中取得會員資訊
        Object memberObj = session.getAttribute("member");
        Integer artHol = null;
        
        if (memberObj != null) {
            if (memberObj instanceof MembersVO) {
                // 真實會員物件
                MembersVO member = (MembersVO) memberObj;
                artHol = member.getMemId();
            } else if (memberObj instanceof String && ((String) memberObj).startsWith("DEV_USER_")) {
                // 開發模式假用戶
                String devUser = (String) memberObj;
                artHol = Integer.parseInt(devUser.substring(9));
            }
        }
        
        if (artHol == null) {
            // 測試用，假定是會員編號71
//            artHol = 71; // 假設會員編號為71
            // 回傳空列表
            model.addAttribute("articles", List.of());
            model.addAttribute("questions", List.of());
            // 如果沒有登入或會員編號無效，則顯示錯誤訊息
            model.addAttribute("errorMessage", "未登入或會員編號錯誤");
        }
        
        try {
            // 只查詢狀態為 1（上架）的文章和問題
            List<ArticleDTO> articles = articleService.findByHolAndCatAndSta(artHol, (byte)1, List.of((byte)1));
            List<ArticleDTO> questions = articleService.findByHolAndCatInAndSta(artHol, List.of((byte)2, (byte)3), List.of((byte)1));
            model.addAttribute("articles", articles != null ? articles : List.of());
            model.addAttribute("questions", questions != null ? questions : List.of());
            model.addAttribute("artHol", artHol);
            return "front-end/forum/members-articles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入會員文章時發生錯誤：" + e.getMessage());
            model.addAttribute("articles", List.of());
            model.addAttribute("questions", List.of());
            return "front-end/forum/members-articles";
        }
    }

    // 備註：測試用，允許直接用網址帶會員編號
    @GetMapping("/members/{artHol}/articles")
    public String getMemberArticles(
            @PathVariable("artHol") Integer artHol,
            Model model) {
        try {
            // 只查詢狀態為 1（上架）的文章和問題
            List<ArticleDTO> articles = articleService.findByHolAndCatAndSta(artHol, (byte)1, List.of((byte)1));
            List<ArticleDTO> questions = articleService.findByHolAndCatInAndSta(artHol, List.of((byte)2, (byte)3), List.of((byte)1));
            model.addAttribute("articles", articles != null ? articles : List.of());
            model.addAttribute("questions", questions != null ? questions : List.of());
            model.addAttribute("artHol", artHol);
            return "front-end/forum/members-articles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入會員文章時發生錯誤：" + e.getMessage());
            model.addAttribute("articles", List.of());
            model.addAttribute("questions", List.of());
            return "front-end/forum/members-articles";
        }
    }

}