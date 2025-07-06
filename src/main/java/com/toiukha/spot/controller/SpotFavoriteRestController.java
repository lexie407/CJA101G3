package com.toiukha.spot.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Integer spotId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查會員登入狀態
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                response.put("success", false);
                response.put("message", "請先登入");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 檢查景點是否存在
            if (spotId == null) {
                response.put("success", false);
                response.put("message", "景點ID不能為空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 檢查景點是否存在於資料庫
            boolean spotExists = spotFavoriteService.checkSpotExists(spotId);
            if (!spotExists) {
                response.put("success", false);
                response.put("message", "景點不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 執行收藏切換
            String message = spotFavoriteService.toggleFavorite(memId, spotId);
            
            // 獲取最新的收藏數量
            Long favoriteCount = spotFavoriteService.getFavoriteCount(spotId);
            
            // 獲取最新的收藏狀態
            boolean isFavorited = spotFavoriteService.isFavorited(memId, spotId);
            
            // 構建響應數據
            Map<String, Object> data = new HashMap<>();
            data.put("message", message);
            data.put("favoriteCount", favoriteCount);
            data.put("isFavorited", isFavorited);
            
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "操作失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            
            if (favoriteRecords.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("您尚未收藏任何景點", favoriteRecords));
            }
            
            return ResponseEntity.ok(ApiResponse.success("查詢成功", favoriteRecords));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試: " + e.getMessage()));
        }
    }

    /**
     * 查詢景點收藏數量
     * GET /api/spot/favorites/{spotId}/count
     */
    @GetMapping("/{spotId}/count")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount(@PathVariable Integer spotId) {
        
        try {
            Long count = spotFavoriteService.getSpotFavoriteCount(spotId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", count));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試: " + e.getMessage()));
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

            Long count = spotFavoriteService.getFavoriteCount(memId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", count));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試: " + e.getMessage()));
        }
    }

    // ========== 輔助方法 ==========

    /**
     * 從 Session 獲取當前登入的會員ID
     * @param session HTTP Session
     * @return 會員ID，如果未登入則返回null
     */
    private Integer getMemIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        
        Object memberObj = session.getAttribute("member");
        if (memberObj instanceof com.toiukha.members.model.MembersVO) {
            com.toiukha.members.model.MembersVO member = (com.toiukha.members.model.MembersVO) memberObj;
            return member.getMemId();
        }
        
        // 嘗試獲取其他可能的會員屬性名稱
        memberObj = session.getAttribute("membersVO");
        if (memberObj instanceof com.toiukha.members.model.MembersVO) {
            com.toiukha.members.model.MembersVO member = (com.toiukha.members.model.MembersVO) memberObj;
            return member.getMemId();
        }
        
        // 嘗試獲取會員ID屬性
        Object memIdObj = session.getAttribute("memId");
        if (memIdObj instanceof Integer) {
            return (Integer) memIdObj;
        }
        
        return null;
    }
} 