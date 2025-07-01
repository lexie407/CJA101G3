package com.toiukha.spot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.spot.model.ApiResponse;
import com.toiukha.spot.model.SpotFavoriteVO;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotFavoriteService;

import jakarta.servlet.http.HttpSession;

/**
 * 景點收藏 REST API 控制器
 * 提供景點收藏相關的 RESTful API
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@RestController
@RequestMapping("/api/spot/favorites")
public class SpotFavoriteRestController {

    @Autowired
    private SpotFavoriteService spotFavoriteService;

    // ========== 收藏操作 API ==========

    /**
     * 新增收藏
     * POST /api/spot/favorites/{spotId}
     */
    @PostMapping("/{spotId}")
    public ResponseEntity<ApiResponse<String>> addFavorite(
            @PathVariable Integer spotId,
            HttpSession session) {
        
        try {
            // 從session獲取會員ID (實際專案中需要從登入狀態獲取)
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }

            spotFavoriteService.addFavorite(memId, spotId);
            return ResponseEntity.ok(ApiResponse.success("已加入收藏", "收藏成功"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 取消收藏
     * DELETE /api/spot/favorites/{spotId}
     */
    @DeleteMapping("/{spotId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            @PathVariable Integer spotId,
            HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }

            spotFavoriteService.removeFavorite(memId, spotId);
            return ResponseEntity.ok(ApiResponse.success("已取消收藏", "取消收藏成功"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 切換收藏狀態
     * POST /api/spot/favorites/{spotId}/toggle
     */
    @PostMapping("/{spotId}/toggle")
    public ResponseEntity<ApiResponse<String>> toggleFavorite(
            @PathVariable Integer spotId,
            HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }

            String message = spotFavoriteService.toggleFavorite(memId, spotId);
            return ResponseEntity.ok(ApiResponse.success(message, message));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    // ========== 查詢 API ==========

    /**
     * 檢查收藏狀態
     * GET /api/spot/favorites/{spotId}/status
     */
    @GetMapping("/{spotId}/status")
    public ResponseEntity<ApiResponse<Boolean>> getFavoriteStatus(
            @PathVariable Integer spotId,
            HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            boolean isFavorited = spotFavoriteService.isFavorited(memId, spotId);
            
            return ResponseEntity.ok(ApiResponse.success("查詢成功", isFavorited));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 查詢會員收藏的景點列表
     * GET /api/spot/favorites/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getMyFavorites(HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }

            List<SpotVO> favoriteSpots = spotFavoriteService.getFavoriteSpots(memId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", favoriteSpots));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 查詢會員收藏記錄（含收藏時間）
     * GET /api/spot/favorites/my/records
     */
    @GetMapping("/my/records")
    public ResponseEntity<ApiResponse<List<SpotFavoriteVO>>> getMyFavoriteRecords(HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }

            List<SpotFavoriteVO> favoriteRecords = spotFavoriteService.getFavoriteRecords(memId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", favoriteRecords));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 查詢景點收藏數量
     * GET /api/spot/favorites/{spotId}/count
     */
    @GetMapping("/{spotId}/count")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount(@PathVariable Integer spotId) {
        
        try {
            Long count = spotFavoriteService.getFavoriteCount(spotId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", count));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 批量查詢景點收藏狀態
     * GET /api/spot/favorites/status/batch?spotIds=1,2,3
     */
    @GetMapping("/status/batch")
    public ResponseEntity<ApiResponse<Map<Integer, Boolean>>> getBatchFavoriteStatus(
            @RequestParam List<Integer> spotIds,
            HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            Map<Integer, Boolean> statusMap = spotFavoriteService.getFavoriteStatusMap(memId, spotIds);
            
            return ResponseEntity.ok(ApiResponse.success("查詢成功", statusMap));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    /**
     * 查詢會員收藏總數
     * GET /api/spot/favorites/my/count
     */
    @GetMapping("/my/count")
    public ResponseEntity<ApiResponse<Long>> getMyFavoriteCount(HttpSession session) {
        
        try {
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }

            Long count = spotFavoriteService.getMemberFavoriteCount(memId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", count));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試"));
        }
    }

    // ========== 輔助方法 ==========

    /**
     * 從 Session 獲取會員ID
     * TODO: 暫時使用測試會員ID，供測試使用
     */
    private Integer getMemIdFromSession(HttpSession session) {
        // TODO: 暫時關閉登入檢查，供測試使用
        // 優先檢查原有會員系統
        // Object memberInfo = session.getAttribute("member");
        // if (memberInfo != null) {
        //     // 原有會員系統的處理
        //     return 1; // 根據實際會員VO結構調整
        // }
        
        // 檢查OAuth2 Google用戶
        // String userEmail = (String) session.getAttribute("userEmail");
        // if (userEmail != null) {
        //     // 使用email的hashCode作為臨時ID（僅供景點收藏功能使用）
        //     return Math.abs(userEmail.hashCode());
        // }
        
        // 使用測試會員ID (測試用)
        return 10;
    }
} 