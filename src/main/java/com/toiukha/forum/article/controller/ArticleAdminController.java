package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.dto.ArticleForm;
import com.toiukha.forum.article.dto.ArticleSearchCriteria;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticleService;
import com.toiukha.forum.article.model.ArticleStatus;
import com.toiukha.forum.util.ArticleMapper;
import com.toiukha.forum.util.Debug;
import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 文章後台的controller
@Controller
@RequestMapping("/forum/admin")
@Validated
public class ArticleAdminController {

    private final ArticleService articleService;
    private final NotificationService notificationService;

    // 使用建構子注入 ArticleService
    @Autowired
    public ArticleAdminController(ArticleService articleService, NotificationService notificationService) {
        this.articleService = articleService;
        this.notificationService = notificationService;
    }


    /********************** 全Controller共用的ModelAttribute ************************/

    @ModelAttribute("articleSearchCriteria")
    public ArticleSearchCriteria initSearchCriteria() {
        return new ArticleSearchCriteria();
    }

    /********************** Get請求 ************************/

    // 顯示文章後台首頁
    @GetMapping("")
    public String showAdminIndex(Model model) {
        model.addAttribute("message", "歡迎來到文章後台管理系統");
        return "back-end/forum/select_page";
    }

    // 顯示全部文章
    @GetMapping("/listAllArticle")
    public String listAllArticles(Model model) {
        model.addAttribute("articleList", articleService.getAll());
        return "back-end/forum/listArticle";
    }

    // 顯示文章後台的搜尋頁面
    @GetMapping("/selectPage")
    public String showSelectPage(Model model) {
//        model.addAttribute("articleSearchCriteria", criteria);
        // 返回文章後台首頁的視圖名稱
        return "back-end/forum/select_page";
    }

    // 顯示文章編輯頁面
    @PostMapping("/getOne_For_Update")
    public String getOne_For_Update(@RequestParam("artId") String artId, ModelMap model) {
        /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
        /*************************** 2.開始查詢資料 *****************************************/
        Article art = articleService.getArticleById (Integer.valueOf(artId));
        ArticleForm form = ArticleMapper.toForm(art);
        /*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
        model.addAttribute("articleForm", form); //將查詢到的結果塞進Attribute
        model.addAttribute("currentPage", "forum"); //讓左側導覽列知道目前的頁面
        return "back-end/forum/update_art_input"; // 查詢完成後轉交update_art_input.html
    }

    /************************** 更新與刪除  *************************/

    // 處理文章更新的請求
    @PostMapping("/update")
    public String updateArticle(@ModelAttribute("articleForm") @Valid ArticleForm form, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("articleForm", form);
            model.addAttribute("error", "欄位錯誤！");
            return "back-end/forum/update_art_input";
        }

        // 將表單轉成 Entity
        Article art = articleService.getArticleById (form.getArtId());
        ArticleMapper.updateEntity(art, form);
        articleService.update(art);

        return "redirect:/forum/admin/listAllArticle"; //重新導向
    }

    @PostMapping("/delete/{artId}")
    public String deleteArticle(@PathVariable Integer artId) {
        Article art = articleService.getArticleById (artId);
        art.setArtSta(ArticleStatus.UNPUBLISHED.getValue());
        return "redirect:/forum/admin/listAllAct";
    }

    /************************ 文章搜尋  ***************************/

    // 處理依照文章類別搜尋文章的請求
    @PostMapping("/searchByCategory")
    public String searchByCategory(
            /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
            @NotNull(message = "請選擇文章分類")
            @Min(value = 1, message = "請選擇有效的文章分類")
            @Max(value = 3, message = "請選擇有效的文章分類") Byte artCat, Model model) {

        /*************************** 2.開始查詢資料 *****************************************/
        List<Article> articles = articleService.getByCategory(artCat);

        if (articles.isEmpty()) {
            model.addAttribute("errorMessage", "沒有相符結果");
            return "back-end/forum/select_page"; // 回搜尋頁
        }
        /*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
        model.addAttribute("articleList", articles);
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/listArticle";
    }

    // 處理依照文章編號搜尋文章的請求
    @PostMapping("getOne_For_Display")
    public String searchById(
            @NotBlank(message = "文章編號請勿空白")
            @Pattern(regexp = "\\d+", message = "文章編號請輸入正整數")
            @RequestParam("artId") String artId, Model model) {
        Article art = articleService.getArticleById (Integer.valueOf(artId));

        if (art == null) {
            model.addAttribute("errorMessage", "查無此文章");
            return "back-end/forum/select_page"; // 回搜尋頁
        }
        model.addAttribute("articleForm", ArticleMapper.toForm(art));
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/update_art_input";
    }

    // 根據文章標題查詢
    @PostMapping("searchByTitle")
    public String searchByTitle(
            /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
            @NotBlank(message = "文章標題: 請勿空白")
            @RequestParam("keyword") String title, Model model) {
        /*************************** 2.開始查詢資料 *****************************************/
        List<Article> articles = articleService.getByTitle(title);

        if (articles.isEmpty()) {
            model.addAttribute("errorMessage", "沒有相符結果");
            return "back-end/forum/select_page"; // 回搜尋頁
        }
        /*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
        model.addAttribute("articleList", articles);
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/listArticle";
    }

    //根據文章上下架狀態查詢
    @PostMapping("searchByStatus")
    public String searchByStatus(
            /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
            @RequestParam("artSta") Byte artSta, Model model) {
        /*************************** 2.開始查詢資料 *****************************************/
        List<Article> articles = articleService.getByStatus(artSta);
        if (articles.isEmpty()) {
            model.addAttribute("errorMessage", "沒有相符結果");
            return "back-end/forum/select_page";
        }
        /*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
        model.addAttribute("articleList", articles);
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/listArticle";
    }

    //根據會員編號查詢
    @PostMapping("searchByAuthorId")
    public String searchByAuthorId(
            /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
            @NotBlank(message = "會員編號請勿空白")
            @Pattern(regexp = "\\d+", message = "會員編號請輸入正整數")
            @RequestParam("artHol") String artHol, Model model) {
        /*************************** 2.開始查詢資料 *****************************************/
        List<Article> articles = articleService.getByAuthorId(Integer.valueOf(artHol));
        if (articles.isEmpty()) {
            model.addAttribute("errorMessage", "沒有相符結果");
        }
        model.addAttribute("articleList", articles);
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/listArticle";
    }

    // 進階查詢
    @PostMapping("/searchAdvanced")
    public String searchAdvanced(@Valid @ModelAttribute ArticleSearchCriteria criteria, BindingResult result, Model model) {
        /*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
        if (result.hasErrors()) {
            // 為了修正轉型錯誤直接在前端顯示，避免曝露網站內部的技術資訊與提高使用者體驗，自定義錯誤訊息並取代
            // 目前只在會員編號輸入過大時觸發轉型錯誤：Failed to convert property value of type java.lang.String to required type java.lang.Integer for property

            Map<String, String> customMessages = Map.of(
                    "artId", "文章編號錯誤",
                    "artHol", "會員編號錯誤",
                    "artCategory", "請選擇有效的文章分類喔",
                    "artStatus", "請選擇有效的文章狀態喔",
                    "startDate", "起始日期不正確",
                    "endDate", "結束日期不正確"
            );

            // 建立一個新 BindingResult 來重建錯誤（自訂訊息版本）
            BindingResult sanitizedResult = new BeanPropertyBindingResult(criteria, "articleSearchCriteria");

            // 針對各欄位的錯誤，如果有自訂訊息則使用自訂訊息，否則使用預設錯誤訊息
            for (FieldError err : result.getFieldErrors()) {
                Debug.log(err.getField(), err.getDefaultMessage());
                String field = err.getField();

                String safeMessage;

                if (customMessages.containsKey(field) || "typeMismatch".equals(err.getCode())) {
                    safeMessage = customMessages.get(field);
                } else {
                    safeMessage = "欄位錯誤，請重新輸入";
                }
                Debug.log(err.getField(), err.getDefaultMessage(), "safeMessage: " + safeMessage);

                sanitizedResult.rejectValue(field, "safe", safeMessage);
            }

            // 替換原本的 result，讓 thymeleaf 不會顯示原始訊息
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "articleSearchCriteria", sanitizedResult);
            model.addAttribute("articleSearchCriteria", criteria);
            model.addAttribute("errorMessage", "請修正欄位錯誤，再進行查詢");
            return "back-end/forum/select_page";
        }

        if (criteria.getStartTimeAsTimestamp() != null && criteria.getEndTimeAsTimestamp() != null && criteria.getStartTimeAsTimestamp().after(criteria.getEndTimeAsTimestamp())) {
            model.addAttribute("articleSearchCriteria", criteria);
            model.addAttribute("errorMessage", "開始時間需早於結束時間");
            return "back-end/forum/select_page";
        }

        /*************************** 2.開始查詢資料 *****************************************/
        List<Article> articles = articleService.searchArticlesByCriteria(criteria);
        model.addAttribute("articleList", articles);
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/listArticle";
    }

    /********************** 發送通知 ************************/

    @PostMapping("/sendNotification")
    @ResponseBody
    public String sendNotification(@RequestParam("message") String message,
                                   @RequestParam("memberId") Integer memberId,
                                   HttpSession session) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        // 取得 adminId
        Integer adminId = (Integer) session.getAttribute("adminId");
        Debug.log("討論區通知", "發送通知給會員ID: " + memberId + ", 內容: " + message + ", 管理員ID: " + adminId);

        NotificationVO noti = new NotificationVO(
                "討論區通知",     // 通知標題
                message,        // 通知內容
                memberId,       // 通知對象會員
                adminId,           // 管理員發送為 null
                now             // 發送時間
        );

        notificationService.addOneNoti(noti);
        return "OK";
    }


    /********************** 例外處理器 ************************/

    // 驗證失敗的例外處理器
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleException(ConstraintViolationException e, Model model) {

//        Debug.log("handleException處理例外喔" + e);
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();

        // 把所有錯誤訊息收集起來
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        // 裝進StringBuilder(可變字串)
        StringBuilder strBuilder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations ) {
            strBuilder.append(violation.getMessage() + "<br>");
        }

        String message = strBuilder.toString();
        model.addAttribute("errorMessage", message);
        model.addAttribute("articleSearchCriteria", new ArticleSearchCriteria());
        model.addAttribute("currentPage", "forum");
        return "back-end/forum/select_page"; // 回搜尋頁
    }

    // 數值過大或數值不合會拋出的例外
    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormat(NumberFormatException ex, Model model) {
        model.addAttribute("errorMessage", "數字格式錯誤或數值過大，請重新輸入");
        model.addAttribute("currentPage", "forum");
        model.addAttribute("articleSearchCriteria", new ArticleSearchCriteria());
        return "back-end/forum/select_page";
    }

}
