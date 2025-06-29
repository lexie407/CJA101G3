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
import com.toiukha.like.model.LikeService;
import com.toiukha.like.model.LikeVO;
import com.toiukha.forum.article.entity.Article;
import java.sql.Timestamp;

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

    @Autowired
    private LikeService likeService;

// 取得單篇文章 DTO
    @GetMapping("/article/{artId:\\d+}")
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


    // 查詢某會員是否對某篇文章按過讚
    @PostMapping("/article/isLiked")
    public Integer isLiked(
            @RequestParam("docId") Integer docId,
            @RequestParam("memId") Integer memId) {
        LikeVO like = likeService.getOneLike(docId);
        if (like != null && like.getMemId().equals(memId) && like.getLikeSta() != null) {
            return like.getLikeSta().intValue(); // 1:已按讚, 0:未按讚
        }
        return 0; // 預設未按讚
    }

    // 處理文章按讚功能，同時更新文章的 artLike 欄位
    @PostMapping("/article/like")
    public ResponseEntity<Map<String, Object>> likeArticle(
            @RequestParam("docId") Integer docId,
            @RequestParam("memId") Integer memId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 先取得文章原本的讚數
            Article article = articleService.getArticleById(docId);
            if (article == null) {
                response.put("success", false);
                response.put("message", "找不到文章");
                return ResponseEntity.ok(response);
            }
            
            Integer originalLikeCount = article.getArtLike() != null ? article.getArtLike() : 0;
            
            // 檢查用戶是否已經按過讚
            LikeVO existingLike = likeService.getOneLike(docId);
            boolean wasLiked = false;
            
            if (existingLike != null && existingLike.getMemId().equals(memId)) {
                wasLiked = existingLike.getLikeSta() != null && existingLike.getLikeSta().equals(Byte.valueOf((byte) 1));
            }
            
            // 處理按讚邏輯
            LikeVO likeVO = new LikeVO();
            likeVO.setDocId(docId);
            likeVO.setParDocId(null); // 文章按讚時 parDocId 為 null
            likeVO.setMemId(memId);
            likeVO.setDocType(Byte.valueOf((byte) 0)); // 0 表示文章
            
            Integer Id = null;
            Byte sta = null;
            
            if (existingLike != null) {
                Id = existingLike.getLikeId();
                sta = existingLike.getLikeSta();
            }
            
            // 如果找到了資料且狀態為0，則更新為1；否則更新為0或新增
            if (Id != null && sta != null && sta.equals(Byte.valueOf((byte) 0))) {
                likeVO.setLikeSta(Byte.valueOf((byte) 1));
                likeVO.setLikeId(Id);
                likeVO.setLikeTime(getNowTime());
                likeService.eidtOne(likeVO);
            } else if (Id != null && sta != null && sta.equals(Byte.valueOf((byte) 1))) {
                // 如果Id不為空且狀態為1，表示要取消讚
                likeVO.setLikeSta(Byte.valueOf((byte) 0));
                likeVO.setLikeId(Id);
                likeVO.setLikeTime(getNowTime());
                likeService.eidtOne(likeVO);
            } else {
                // 第一次點讚
                likeVO.setLikeSta(Byte.valueOf((byte) 1));
                likeVO.setLikeTime(getNowTime());
                likeService.eidtOne(likeVO);
            }
            
            // 計算新的總讚數
            Integer newTotalLikeCount;
            if (wasLiked) {
                // 原本已按讚，現在收回讚，所以要減1
                newTotalLikeCount = originalLikeCount - 1;
            } else {
                // 原本未按讚，現在按讚，所以要加1
                newTotalLikeCount = originalLikeCount + 1;
            }
            
            // 確保讚數不會變成負數
            if (newTotalLikeCount < 0) {
                newTotalLikeCount = 0;
            }
            
            // 更新文章的 artLike 欄位
            article.setArtLike(newTotalLikeCount);
            articleService.update(article);
            
            response.put("success", true);
            response.put("likeCount", newTotalLikeCount);
            response.put("wasLiked", wasLiked);
            response.put("message", wasLiked ? "收回讚成功" : "按讚成功");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "按讚失敗: " + e.getMessage());
            System.err.println("文章按讚時發生錯誤: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    Timestamp getNowTime() {
        Date date = new Date();
        Timestamp now = new Timestamp(date.getTime());
        return now;
    }

}