package com.toiukha.spot.controller.user;

import com.toiukha.spot.model.ApiResponse;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.dto.SpotSearchRequest;
import com.toiukha.spot.dto.SpotDTO;
import com.toiukha.spot.dto.SpotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 景點前台 API 控制器
 * 專門處理前台用戶的景點查詢和新增需求
 * 只提供上架景點的查詢功能和景點新增功能
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@RestController
@RequestMapping("/api/spot/public")
public class SpotUserApiController {

    @Autowired
    private SpotService spotService;

    @Autowired
    private SpotMapper spotMapper;

    // ========== 1. 前台景點查詢API ==========

    /**
     * 取得所有上架景點 (前台用)
     * 只返回狀態為上架(status=1)的景點
     * @return API回應包含上架景點列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getActiveSpots() {
        try {
            List<SpotVO> activeSpots = spotService.getActiveSpots();
            return ResponseEntity.ok(ApiResponse.success("查詢成功", activeSpots));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    /**
     * 根據ID取得景點詳情 (前台用)
     * 只能查詢上架狀態的景點
     * @param spotId 景點ID
     * @return API回應包含景點詳情
     */
    @GetMapping("/{spotId}")
    public ResponseEntity<ApiResponse<SpotVO>> getSpotById(@PathVariable Integer spotId) {
        try {
            SpotVO spot = spotService.getSpotById(spotId);
            
            // 檢查景點是否存在且為上架狀態
            if (spot == null) {
                return ResponseEntity.ok(ApiResponse.error("景點不存在"));
            }
            
            if (!spot.isActive()) {
                return ResponseEntity.ok(ApiResponse.error("景點尚未上架"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("查詢成功", spot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 2. 前台搜尋API ==========

    /**
     * 關鍵字搜尋 (GET方式)
     * 只搜尋上架狀態的景點
     * @param keyword 搜尋關鍵字
     * @return API回應包含搜尋結果
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SpotVO>>> searchSpots(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("搜尋關鍵字不能為空"));
            }
            
            // 使用現有的搜尋方法，但只返回上架景點
            List<SpotVO> searchResults = spotService.searchSpots(keyword.trim());
            
            return ResponseEntity.ok(ApiResponse.success("搜尋完成，找到 " + searchResults.size() + " 個結果", searchResults));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("搜尋失敗: " + e.getMessage()));
        }
    }

    /**
     * 複合搜尋 (POST方式)
     * 支援複雜搜尋條件，只搜尋上架景點
     * @param searchRequest 搜尋請求物件
     * @return API回應包含搜尋結果
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<SpotVO>>> advancedSearch(@RequestBody SpotSearchRequest searchRequest) {
        try {
            if (searchRequest == null || 
                (searchRequest.getKeyword() == null || searchRequest.getKeyword().trim().isEmpty())) {
                return ResponseEntity.ok(ApiResponse.error("搜尋條件不能為空"));
            }
            
            // 使用現有的搜尋方法，但只返回上架景點
            List<SpotVO> searchResults = spotService.searchSpots(searchRequest.getKeyword().trim());
            
            return ResponseEntity.ok(ApiResponse.success("搜尋完成", searchResults));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("搜尋失敗: " + e.getMessage()));
        }
    }

    // ========== 3. 前台統計API ==========

    /**
     * 取得前台統計資訊
     * 只統計上架景點的資訊
     * @return API回應包含統計資料
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 統計上架景點數量
            List<SpotVO> activeSpots = spotService.getActiveSpots();
            stats.put("totalActiveSpots", activeSpots.size());
            
            return ResponseEntity.ok(ApiResponse.success("統計資料查詢成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("統計資料查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 4. 地圖相關API ==========

    /**
     * 取得地圖用景點資料
     * 只返回有座標且上架的景點
     * @return API回應包含地圖景點資料
     */
    @GetMapping("/map-data")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getMapSpots() {
        try {
            List<SpotVO> activeSpots = spotService.getActiveSpots();
            
            // 過濾出有有效座標的景點
            List<SpotVO> mapSpots = activeSpots.stream()
                .filter(spot -> spot.hasValidCoordinates())
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success("地圖資料查詢成功", mapSpots));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("地圖資料查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 5. 前台景點新增API ==========

    /**
     * 用戶新增景點 (前台用)
     * 用戶新增的景點預設為待審核狀態，需要管理員審核後才能上架
     * @param spotDTO 景點資料傳輸物件
     * @param session HTTP Session
     * @return API回應
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SpotVO>> submitSpot(
            @Valid @RequestBody SpotDTO spotDTO, 
            HttpSession session) {
        try {
            // 檢查會員是否已登入
            Integer currentUserId = getCurrentUserId(session);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }
            
            // 檢查名稱是否重複
            if (spotService.existsBySpotName(spotDTO.getSpotName())) {
                return ResponseEntity.ok(ApiResponse.error("景點名稱已存在，請使用其他名稱"));
            }
            
            // 轉換DTO為VO
            SpotVO spotVO = spotMapper.toVO(spotDTO);
            
            // 設定前台新增的特殊屬性
            spotVO.setCrtId(currentUserId);      // 設定建立者ID
            spotVO.setSpotStatus((byte) 0);      // 預設為待審核狀態
            
            // 新增景點
            SpotVO savedSpot = spotService.addSpot(spotVO);
            
            return ResponseEntity.ok(ApiResponse.success("景點新增成功，已送審中", savedSpot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("景點新增失敗: " + e.getMessage()));
        }
    }

    // ========== 私有輔助方法 ==========

    /**
     * 取得當前登入用戶ID
     * 整合會員模組的登入系統
     * @param session HTTP Session
     * @return 用戶ID，如果未登入則返回null
     */
    private Integer getCurrentUserId(HttpSession session) {
        Object memberObj = session.getAttribute("member");
        if (memberObj instanceof com.toiukha.members.model.MembersVO) {
            com.toiukha.members.model.MembersVO member = (com.toiukha.members.model.MembersVO) memberObj;
            return member.getMemId();
        }
        return null;
    }
} 