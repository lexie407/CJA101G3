package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticlePicturesServiceImpl;
import com.toiukha.forum.article.model.ArticleService;
import com.toiukha.forum.article.model.ArticleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ArticleController {

    private final ArticleService articleService;
    private final ArticlePicturesServiceImpl apService;

    // 使用建構子注入 ArticleService
    @Autowired
    public ArticleController(ArticleService articleService, ArticlePicturesServiceImpl apService) {
        this.articleService = articleService;
        this.apService = apService;
    }


// 瀏覽全部文章
    @GetMapping("/articles")
    public List<ArticleDTO> getAllArticles(
            @RequestParam(defaultValue = "artId") String sortBy,
            @RequestParam(name = "order", defaultValue = "ASC") String sortDirection) {
        // 直接返回 ArticleDTO 列表
        return articleService.getAllDTO(sortBy, sortDirection);
    }

//    @PostMapping("/insert")
//    public String insertArticle(
//            @RequestParam("artTitle") String title,
//            @RequestParam("artHol") Integer memberId,
//            @RequestParam("artCat") Integer category,
//            @RequestParam("artCon") String content) {
//
//        // 1. 新增文章
//        Article article = articleService.add(
//                category.byteValue(),
//                (byte) 1, // 假設文章狀態為上架
//                memberId,
//                title,
//                content
//        );
//
//        // 2. 擷取圖片 ID，綁定文章
//        List<Integer> picIds = extractImageIdsFromHtml(content);
//        apService.bindPicturesToArticle(picIds, article.getArtId());
//
//        return "redirect:/forum/article/list";
//    }

    private List<Integer> extractImageIdsFromHtml(String html) {
        List<Integer> ids = new ArrayList<>();
        Pattern pattern = Pattern.compile("/forum/artImage/(\\d+)");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            ids.add(Integer.parseInt(matcher.group(1)));
        }
        return ids;
    }

    // 廢棄
//    // 上傳圖片資料驗證
//    // 這裡的路徑會對應到 /forum-uploads/article-images/ 資料夾
//    @PostMapping("/forum/upload/articleImage")
//    public Map<String,String> uploadPic(@RequestParam(name = "file") MultipartFile file) {
//        //*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
//        ImageValidator.validate(file);
//        //*************************** 轉給Service做圖片上傳邏輯 ************************/
//        return apService.uploadArticlePicture(file);
//
////        return Map.of("url","/forum-uploads/article-images/ya.png"); //測試用
//    }
//
//    // 輸入格式的錯誤處理，會在ImageValidator丟出IllegalArgumentException時被呼叫
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e){
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//
//    // 圖片操作相關的錯誤處理，例如上傳圖片時檔案寫入失敗
//    @ExceptionHandler(ImageException.class)
//    public ResponseEntity<?> handleImageException(ImageException imageEx) {
//        return ResponseEntity.status(500).body(Map.of("error", "伺服器儲存圖片失敗：" + imageEx.getMessage()));
//    }

}
