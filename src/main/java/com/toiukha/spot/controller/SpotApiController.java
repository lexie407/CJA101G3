package com.toiukha.spot.controller;

import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.service.GoogleApiService;
import com.toiukha.spot.model.SpotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * 景點API控制器
 * 提供前台和後台的景點相關API
 */
@RestController
@RequestMapping("/api/spot/v2")  // 使用新的版本路徑避免衝突
public class SpotApiController {

    private static final Logger log = LoggerFactory.getLogger(SpotApiController.class);

    @Autowired
    private SpotService spotService;
    
    @Autowired
    private GoogleApiService googleApiService;

    /**
     * 獲取Google Maps API配置
     */
    @GetMapping("/maps/config")
    public ResponseEntity<?> getGoogleMapsConfig() {
        Map<String, Object> config = new HashMap<>();
        try {
            String apiKey = googleApiService.getApiKey();
            boolean isAvailable = googleApiService.isApiAvailable();
            String mapsApiUrl = googleApiService.getMapsApiUrl("places,geometry");
            
            config.put("available", isAvailable);
            config.put("hasApiKey", apiKey != null);
            config.put("apiKey", apiKey);
            config.put("mapsApiUrl", mapsApiUrl);
            config.put("defaultCenter", Map.of("lat", 23.8, "lng", 121.0)); // 台灣中心點
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("獲取Google Maps配置失敗", e);
            config.put("available", false);
            config.put("message", "Google Maps API配置載入失敗");
            return ResponseEntity.ok(config);
        }
    }

    /**
     * 根據關鍵字搜尋公開景點
     * @param keyword 搜索關鍵字
     * @return 符合搜索條件的景點列表
     */
    @GetMapping("/public/search")
    public ResponseEntity<?> searchPublicSpots(@RequestParam String keyword) {
        return ResponseEntity.ok(spotService.searchPublicSpots(keyword));
    }

    /**
     * 根據ID列表獲取景點資料
     * @param spotIds 景點ID列表
     * @return 景點資料列表
     */
    @GetMapping("/public/list-by-ids")
    public ResponseEntity<?> getSpotsByIds(@RequestParam List<Integer> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "景點ID不能為空"));
        }
        try {
            List<SpotVO> spots = spotService.getSpotsByIds(spotIds);
            return ResponseEntity.ok(Map.of("success", true, "data", spots));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicSpotDetail(@PathVariable Integer id) {
        try {
            SpotVO spot = spotService.findById(id);
            if (spot == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("spotId", spot.getSpotId());
            response.put("spotName", spot.getSpotName());
            response.put("spotLoc", spot.getSpotLoc());
            response.put("spotLat", spot.getSpotLat());
            response.put("spotLng", spot.getSpotLng());
            response.put("spotDesc", spot.getSpotDesc());
            response.put("pictureUrls", spot.getPictureUrls());
            
            // 如果有座標，添加到地圖資料中
            if (spot.getSpotLat() != null && spot.getSpotLng() != null) {
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("lat", spot.getSpotLat());
                mapData.put("lng", spot.getSpotLng());
                response.put("mapData", mapData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "獲取景點詳情失敗"));
        }
    }

    /**
     * Google Places Autocomplete 建議關鍵字
     * @param input 使用者輸入
     * @return 建議字串清單
     */
    @GetMapping("/google-suggest")
    public ResponseEntity<?> getGoogleSuggest(@RequestParam String input) {
        try {
            var suggestions = googleApiService.getPlaceAutocompleteSuggestions(input);
            return ResponseEntity.ok(Map.of("success", true, "suggestions", suggestions));
        } catch (Exception e) {
            log.error("Google Suggest 失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "取得建議失敗"));
        }
    }
} 