package com.toiukha.spot.controller;

import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.service.GeocodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 景點地理編碼控制器
 * 用於批量更新景點座標
 */
@RestController
@RequestMapping("/api/spot/geocode")
public class SpotGeocodeController {

    private static final Logger logger = LoggerFactory.getLogger(SpotGeocodeController.class);

    @Autowired
    private SpotService spotService;

    @Autowired
    private GeocodeService geocodeService;

    /**
     * 為單個景點更新座標
     * @param spotId 景點ID
     * @return 更新結果
     */
    @PostMapping("/update/{spotId}")
    public ResponseEntity<Map<String, Object>> updateSpotCoordinates(@PathVariable Integer spotId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            SpotVO spot = spotService.getSpotById(spotId);
            if (spot == null) {
                response.put("success", false);
                response.put("message", "景點不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 檢查是否已有座標
            if (spot.hasValidCoordinates()) {
                response.put("success", true);
                response.put("message", "景點已有有效座標");
                response.put("coordinates", new double[]{spot.getSpotLat(), spot.getSpotLng()});
                return ResponseEntity.ok(response);
            }

            // 使用地址和名稱進行地理編碼
            double[] coordinates = geocodeService.getCoordinatesForSpot(spot);
            
            if (coordinates != null) {
                spot.setSpotLat(coordinates[0]);
                spot.setSpotLng(coordinates[1]);
                spotService.updateSpot(spot);
                
                response.put("success", true);
                response.put("message", "座標更新成功");
                response.put("coordinates", coordinates);
                logger.info("景點 {} 座標更新成功: [{}, {}]", spot.getSpotName(), coordinates[0], coordinates[1]);
            } else {
                response.put("success", false);
                response.put("message", "無法獲取座標，請檢查地址是否正確");
            }

        } catch (Exception e) {
            logger.error("更新景點座標時發生錯誤: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "更新過程發生錯誤: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 批量更新所有沒有座標的景點
     * @param limit 限制更新數量（避免API配額用盡）
     * @return 更新結果
     */
    @PostMapping("/batch-update")
    public ResponseEntity<Map<String, Object>> batchUpdateCoordinates(
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (!geocodeService.isGoogleApiAvailable()) {
            response.put("success", false);
            response.put("message", "Google API Key 未設定，無法進行地理編碼");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 獲取所有沒有座標的景點
            List<SpotVO> spotsWithoutCoordinates = spotService.getSpotsWithoutCoordinates();
            
            if (spotsWithoutCoordinates.isEmpty()) {
                response.put("success", true);
                response.put("message", "所有景點都已有座標");
                response.put("totalProcessed", 0);
                return ResponseEntity.ok(response);
            }

            int processedCount = 0;
            int successCount = 0;
            int errorCount = 0;

            // 限制處理數量
            int actualLimit = Math.min(limit, spotsWithoutCoordinates.size());
            
            for (int i = 0; i < actualLimit; i++) {
                SpotVO spot = spotsWithoutCoordinates.get(i);
                processedCount++;
                
                try {
                    logger.info("正在處理景點 {}/{}: {} - {}", 
                        processedCount, actualLimit, spot.getSpotName(), spot.getSpotLoc());
                    
                    double[] coordinates = geocodeService.getCoordinatesForSpot(spot);
                    
                    if (coordinates != null) {
                        spot.setSpotLat(coordinates[0]);
                        spot.setSpotLng(coordinates[1]);
                        spotService.updateSpot(spot);
                        successCount++;
                        
                        logger.info("景點 {} 座標更新成功: [{}, {}]", 
                            spot.getSpotName(), coordinates[0], coordinates[1]);
                    } else {
                        errorCount++;
                        logger.warn("景點 {} 無法獲取座標: {}", spot.getSpotName(), spot.getSpotLoc());
                    }
                    
                    // 避免API配額用盡，每次請求間隔1秒
                    if (i < actualLimit - 1) {
                        Thread.sleep(1000);
                    }
                    
                } catch (Exception e) {
                    errorCount++;
                    logger.error("處理景點 {} 時發生錯誤: {}", spot.getSpotName(), e.getMessage());
                }
            }

            response.put("success", true);
            response.put("message", String.format("批量更新完成：處理 %d 個，成功 %d 個，失敗 %d 個", 
                processedCount, successCount, errorCount));
            response.put("totalProcessed", processedCount);
            response.put("successCount", successCount);
            response.put("errorCount", errorCount);
            response.put("remainingCount", spotsWithoutCoordinates.size() - processedCount);

        } catch (Exception e) {
            logger.error("批量更新座標時發生錯誤: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "批量更新過程發生錯誤: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 獲取沒有座標的景點列表
     * @return 景點列表
     */
    @GetMapping("/without-coordinates")
    public ResponseEntity<Map<String, Object>> getSpotsWithoutCoordinates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<SpotVO> spots = spotService.getSpotsWithoutCoordinates();
            response.put("success", true);
            response.put("data", spots);
            response.put("count", spots.size());
        } catch (Exception e) {
            logger.error("獲取無座標景點列表時發生錯誤: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "獲取列表失敗: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 檢查地理編碼服務狀態
     * @return 服務狀態
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGeocodeStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("googleApiAvailable", geocodeService.isGoogleApiAvailable());
        response.put("message", geocodeService.isGoogleApiAvailable() ? 
            "地理編碼服務可用" : "Google API Key 未設定");
        
        return ResponseEntity.ok(response);
    }
} 