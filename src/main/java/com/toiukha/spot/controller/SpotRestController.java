package com.toiukha.spot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.toiukha.spot.model.ApiResponse;
import com.toiukha.spot.dto.SpotDTO;
import com.toiukha.spot.dto.SpotSearchRequest;
import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.GoogleApiService;
import com.toiukha.spot.service.GovernmentDataService;
import com.toiukha.spot.dto.SpotMapper;

// 引入新的專用控制器
import com.toiukha.spot.controller.user.SpotUserApiController;
import com.toiukha.spot.controller.admin.SpotAdminApiController;

import jakarta.validation.Valid;

/**
 * 景點REST API控制器 (過渡期相容性控制器)
 * 
 * ⚠️ 此控制器已重構，建議使用新的專用控制器：
 * - 前台查詢：SpotUserApiController (/api/spot/public/*)
 * - 後台管理：SpotAdminApiController (/api/spot/admin/*)
 * 
 * 此控制器保留是為了向下相容性，未來版本可能會移除
 * 
 * @author CJA101G3 景點模組開發
 * @version 2.0 (重構版本)
 * @deprecated 建議使用專用的前台/後台控制器
 */
@RestController
@RequestMapping("/api/spot")
@Deprecated
public class SpotRestController {

    @Autowired
    private SpotService spotService;
    
    @Autowired
    private GoogleApiService googleApiService;

    @Autowired
    private GovernmentDataService governmentDataService;

    @Autowired
    private SpotMapper spotMapper;

    // 注入新的專用控制器
    @Autowired
    private SpotUserApiController spotUserApiController;
    
    @Autowired
    private SpotAdminApiController spotAdminApiController;

    // ========== 1. 景點查詢API (重導向到前台控制器) ==========

    /**
     * 取得所有上架景點 (前台用)
     * @deprecated 請使用 /api/spot/public/list
     * @return API回應包含景點列表
     */
    @GetMapping("/active")
    @Deprecated
    public ResponseEntity<ApiResponse<List<SpotVO>>> getActiveSpots() {
        // 重導向到新的前台控制器
        return spotUserApiController.getActiveSpots();
    }

    /**
     * 根據ID取得景點詳情
     * @deprecated 前台請使用 /api/spot/public/{spotId}，後台請使用 /api/spot/admin/{spotId}
     * @param spotId 景點ID
     * @return API回應包含景點資料
     */
    @GetMapping("/{spotId}")
    @Deprecated
    public ResponseEntity<ApiResponse<SpotVO>> getSpotById(@PathVariable Integer spotId) {
        // 預設重導向到前台控制器 (只能查看上架景點)
        return spotUserApiController.getSpotById(spotId);
    }

    /**
     * 搜尋景點
     * @deprecated 請使用 /api/spot/public/search
     * @param keyword 搜尋關鍵字
     * @return API回應包含搜尋結果
     */
    @GetMapping("/search")
    @Deprecated
    public ResponseEntity<ApiResponse<List<SpotVO>>> searchSpots(@RequestParam String keyword) {
        // 重導向到新的前台控制器
        return spotUserApiController.searchSpots(keyword);
    }

    /**
     * 複合搜尋 (POST方式，支援複雜搜尋條件)
     * @deprecated 請使用 /api/spot/public/search
     * @param searchRequest 搜尋請求物件
     * @return API回應包含搜尋結果
     */
    @PostMapping("/search")
    @Deprecated
    public ResponseEntity<ApiResponse<List<SpotVO>>> advancedSearch(@RequestBody SpotSearchRequest searchRequest) {
        // 重導向到新的前台控制器
        return spotUserApiController.advancedSearch(searchRequest);
    }

    // ========== 2. 景點管理API (重導向到後台控制器) ==========

    /**
     * 取得所有景點 (後台用)
     * @deprecated 請使用 /api/spot/admin/all
     * @return API回應包含所有景點
     */
    @GetMapping("/all")
    @Deprecated
    public ResponseEntity<ApiResponse<List<SpotVO>>> getAllSpots() {
        // 重導向到新的後台控制器
        return spotAdminApiController.getAllSpots();
    }

    /**
     * 根據狀態取得景點
     * @deprecated 請使用 /api/spot/admin/status/{status}
     * @param status 景點狀態
     * @return API回應包含篩選結果
     */
    @GetMapping("/status/{status}")
    @Deprecated
    public ResponseEntity<ApiResponse<List<SpotVO>>> getSpotsByStatus(@PathVariable Byte status) {
        // 重導向到新的後台控制器
        return spotAdminApiController.getSpotsByStatus(status);
    }

    // ========== 3. 景點新增API (重導向到後台控制器) ==========

    /**
     * 新增景點
     * @deprecated 前台投稿請使用 /api/spot/public/submit，後台請使用 /api/spot/admin
     * @param spotDTO 景點資料傳輸物件
     * @return API回應
     */
    @PostMapping
    @Deprecated
    public ResponseEntity<ApiResponse<SpotVO>> addSpot(@Valid @RequestBody SpotDTO spotDTO) {
        // ⚠️ 此端點已過時，無法確定是前台投稿還是後台新增
        // 為了向下相容性，暫時重導向到後台控制器
        // 建議明確使用：
        // - 前台投稿：POST /api/spot/public/submit
        // - 後台新增：POST /api/spot/admin
        return spotAdminApiController.addSpot(spotDTO);
    }

    // ========== 4. 景點更新API (重導向到後台控制器) ==========

    /**
     * 更新景點
     * @deprecated 請使用 /api/spot/admin/{spotId}
     * @param spotId 景點ID
     * @param spotDTO 景點資料傳輸物件
     * @return API回應
     */
    @PutMapping("/{spotId}")
    @Deprecated
    public ResponseEntity<ApiResponse<SpotVO>> updateSpot(
            @PathVariable Integer spotId, 
            @Valid @RequestBody SpotDTO spotDTO) {
        // 重導向到新的後台控制器
        return spotAdminApiController.updateSpot(spotId, spotDTO);
    }

    // ========== 5. 景點刪除API (重導向到後台控制器) ==========

    /**
     * 刪除景點
     * @deprecated 請使用 /api/spot/admin/{spotId}
     * @param spotId 景點ID
     * @return API回應
     */
    @DeleteMapping("/{spotId}")
    @Deprecated
    public ResponseEntity<ApiResponse<Void>> deleteSpot(@PathVariable Integer spotId) {
        // 重導向到新的後台控制器
        return spotAdminApiController.deleteSpot(spotId);
    }

    // ========== 6. 狀態管理API (重導向到後台控制器) ==========

    /**
     * 景點上架
     * @deprecated 請使用 /api/spot/admin/{spotId}/activate
     * @param spotId 景點ID
     * @return API回應
     */
    @PutMapping("/{spotId}/activate")
    @Deprecated
    public ResponseEntity<ApiResponse<Void>> activateSpot(@PathVariable Integer spotId) {
        // 重導向到新的後台控制器
        return spotAdminApiController.activateSpot(spotId);
    }

    /**
     * 景點下架
     * @deprecated 請使用 /api/spot/admin/{spotId}/deactivate
     * @param spotId 景點ID
     * @return API回應
     */
    @PutMapping("/{spotId}/deactivate")
    @Deprecated
    public ResponseEntity<ApiResponse<Void>> deactivateSpot(@PathVariable Integer spotId) {
        // 重導向到新的後台控制器
        return spotAdminApiController.deactivateSpot(spotId);
    }

    // ========== 7. 批量操作API (重導向到後台控制器) ==========

    /**
     * 批量上架景點
     * @deprecated 請使用 /api/spot/admin/batch/activate
     * @param spotIds 景點ID列表
     * @return API回應
     */
    @PutMapping("/batch/activate")
    @Deprecated
    public ResponseEntity<ApiResponse<String>> batchActivate(@RequestBody List<Integer> spotIds) {
        // 重導向到新的後台控制器
        return spotAdminApiController.batchActivate(spotIds);
    }

    /**
     * 批量下架景點
     * @deprecated 請使用 /api/spot/admin/batch/deactivate
     * @param spotIds 景點ID列表
     * @return API回應
     */
    @PutMapping("/batch/deactivate")
    @Deprecated
    public ResponseEntity<ApiResponse<String>> batchDeactivate(@RequestBody List<Integer> spotIds) {
        // 重導向到新的後台控制器
        return spotAdminApiController.batchDeactivate(spotIds);
    }

    // ========== 8. 批次匯入API (重導向到後台控制器) ==========

    /**
     * 批次新增景點 (優化版本)
     * @deprecated 請使用 /api/spot/admin/batch
     * @param spotDTOList 景點資料傳輸物件列表
     * @return API回應
     */
    @PostMapping("/batch")
    @Deprecated
    public ResponseEntity<ApiResponse<SpotService.BatchResult>> addSpotsInBatch(@Valid @RequestBody List<SpotDTO> spotDTOList) {
        // 重導向到新的後台控制器
        return spotAdminApiController.addSpotsInBatch(spotDTOList);
    }

    /**
     * 批次新增景點 (僅儲存，不獲取座標)
     * @deprecated 請使用新的批次匯入API
     * @param spotDTOList 景點資料傳輸物件列表
     * @return API回應
     */
    @PostMapping("/batch/save-only")
    @Deprecated
    public ResponseEntity<ApiResponse<List<SpotVO>>> addSpotsBatchSaveOnly(@Valid @RequestBody List<SpotDTO> spotDTOList) {
        if (spotDTOList == null || spotDTOList.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("景點資料列表不能為空"));
        }
        // 檢查名稱是否重複
        for (SpotDTO spotDTO : spotDTOList) {
            if (spotService.existsBySpotName(spotDTO.getSpotName())) {
                return ResponseEntity.ok(ApiResponse.error("景點名稱已存在: " + spotDTO.getSpotName()));
            }
        }
        // 轉換DTO為VO
        List<SpotVO> spotVOList = spotDTOList.stream()
                .map(spotMapper::toVO)
                .peek(spotVO -> spotVO.setCrtId(getCurrentUserId()))
                .toList();
        // 使用批次儲存方法
        List<SpotVO> savedSpots = spotService.addSpotsInBatch(spotVOList);
        return ResponseEntity.ok(ApiResponse.success("批次新增完成，成功儲存 " + savedSpots.size() + " 個景點", savedSpots));
    }

    // ========== 9. 政府資料匯入API (保留原有功能) ==========

    /**
     * 匯入政府觀光資料
     * @param request 匯入請求
     * @return API回應
     */
    @PostMapping("/government/import")
    public ResponseEntity<ApiResponse<GovernmentDataService.ImportResult>> importGovernmentData(@RequestBody GovernmentImportRequest request) {
        try {
            // 設定預設值
            if (request.getCrtId() == null) {
                request.setCrtId(getCurrentUserId());
            }
            if (request.getBatchSize() == null) {
                request.setBatchSize(50); // 預設批次大小
            }
            
            GovernmentDataService.ImportResult result = governmentDataService.importGovernmentData(
                request.getCrtId(), 
                request.getBatchSize() != null ? request.getBatchSize() : -1,
                null // 不限定城市
            );
            
            return ResponseEntity.ok(ApiResponse.success("政府資料匯入完成", result));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("政府資料匯入失敗: " + e.getMessage()));
        }
    }

    /**
     * 政府資料匯入請求物件
     */
    public static class GovernmentImportRequest {
        private Integer crtId;
        private Integer batchSize;

        public Integer getCrtId() { return crtId; }
        public void setCrtId(Integer crtId) { this.crtId = crtId; }

        public Integer getBatchSize() { return batchSize; }
        public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    }

    // ========== 10. 工具和配置API (保留原有功能) ==========

    /**
     * 取得景點列表（前台行程頁面用）
     * 支持分頁和過濾條件
     * @param limit 返回的最大記錄數
     * @param offset 起始位置
     * @param isPublic 是否公開 (1=公開, 0=私人)
     * @param status 狀態 (1=上架)
     * @return 景點列表
     */
    @GetMapping("/spots")
    public ResponseEntity<List<SpotVO>> getSpots(
            @RequestParam(required = false, defaultValue = "12") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false) Byte isPublic,
            @RequestParam(required = false) Byte status) {
        
        try {
            List<SpotVO> spots;
            
            // 根據參數決定查詢方式 - 只使用狀態過濾
            if (status != null) {
                // 只篩選狀態
                spots = spotService.getSpotsByStatus(status);
            } else {
                // 獲取所有上架景點
                spots = spotService.getActiveSpots();
            }
            
            // 應用分頁
            if (spots.size() > offset) {
                int endIndex = Math.min(offset + limit, spots.size());
                spots = spots.subList(offset, endIndex);
            } else {
                spots = List.of(); // 返回空列表
            }
            
            return ResponseEntity.ok(spots);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * 取得當前使用者ID
     * @return 使用者ID
     */
    private Integer getCurrentUserId() {
        // TODO: 整合 Spring Security
        return 1;
    }

    /**
     * 取得Google Maps配置
     * @return 配置資料
     */
    @GetMapping("/google-maps-config")
    public ResponseEntity<Map<String, Object>> getGoogleMapsConfig() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            // 從GoogleApiService獲取配置
            String apiKey = googleApiService.getApiKey();
            boolean hasValidApiKey = apiKey != null && !apiKey.trim().isEmpty() && !"your-google-maps-api-key".equals(apiKey);
            
            config.put("available", hasValidApiKey);
            config.put("hasApiKey", hasValidApiKey);
            
            if (hasValidApiKey) {
                // 提供API Key給前端使用
                config.put("apiKey", apiKey);
                config.put("mapsApiUrl", "https://maps.googleapis.com/maps/api/js?key=" + apiKey + "&libraries=places,geometry,marker&v=beta&loading=async");
                config.put("message", "Google Maps API 已配置");
            } else {
                config.put("message", "Google Maps API Key 未設定或無效，請檢查 application.properties 中的 google.api.key 設定");
            }
            
            // 提供前端需要的配置
            config.put("defaultCenter", Map.of(
                "lat", 23.8,
                "lng", 121.0
            ));
            config.put("defaultZoom", 8);
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            config.put("available", false);
            config.put("hasApiKey", false);
            config.put("message", "無法載入Google Maps配置: " + e.getMessage());
            return ResponseEntity.ok(config);
        }
    }

    /**
     * 取得匯入統計資訊
     * @deprecated 請使用 /api/spot/admin/stats
     * @return 統計資料
     */
    @GetMapping("/stats")
    @Deprecated
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImportStats() {
        // 重導向到新的後台控制器
        return spotAdminApiController.getAdminStats();
    }


} 