package com.toiukha.spot.service;

import com.toiukha.spot.model.SpotVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 地理編碼服務 - 根據地址獲取座標
 */
@Service
public class GeocodeService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodeService.class);
    
    @Value("${google.api.key:}")
    private String googleApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GeocodeService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 根據地址獲取座標
     * @param address 地址
     * @return 座標數組 [緯度, 經度]，如果失敗返回 null
     */
    public double[] getCoordinatesFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            logger.warn("地址為空，無法進行地理編碼");
            return null;
        }
        
        if (googleApiKey == null || googleApiKey.trim().isEmpty()) {
            logger.error("Google API Key 未設定，無法進行地理編碼");
            logger.error("請檢查 application-local.properties 中的 google.api.key 設定");
            return null;
        }
        
        try {
            // 構建 Google Geocoding API URL
            String encodedAddress = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
            String url = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", encodedAddress)
                .queryParam("key", googleApiKey)
                .queryParam("components", "country:TW") // 限制在台灣搜尋
                .build()
                .toUriString();
            
            logger.info("正在進行地理編碼: {}", address);
            logger.debug("API URL: {}", url.replace(googleApiKey, "***"));
            
            // 發送請求
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null) {
                logger.error("Google Geocoding API 回應為空");
                return null;
            }
            
            logger.debug("API 回應: {}", response);
            
            // 解析回應
            JsonNode root = objectMapper.readTree(response);
            String status = root.path("status").asText();
            
            // 詳細的狀態處理
            switch (status) {
                case "OK":
                    // 繼續處理
                    break;
                case "ZERO_RESULTS":
                    logger.warn("找不到地址對應的座標: {}", address);
                    return null;
                case "OVER_QUERY_LIMIT":
                    logger.error("Google API 查詢限制已達上限，請稍後再試");
                    return null;
                case "REQUEST_DENIED":
                    logger.error("Google API 請求被拒絕，請檢查 API Key 是否正確或是否已啟用 Geocoding API");
                    logger.error("錯誤訊息: {}", root.path("error_message").asText(""));
                    return null;
                case "INVALID_REQUEST":
                    logger.error("無效的 API 請求: {}", root.path("error_message").asText(""));
                    return null;
                case "UNKNOWN_ERROR":
                    logger.error("Google API 發生未知錯誤，請稍後再試");
                    return null;
                default:
                    logger.error("地理編碼失敗，未知狀態: {} - {}", status, root.path("error_message").asText(""));
                    return null;
            }
            
            JsonNode results = root.path("results");
            if (results.isEmpty()) {
                logger.warn("API 回應正常但沒有結果: {}", address);
                return null;
            }
            
            // 取得第一個結果的座標
            JsonNode location = results.get(0)
                .path("geometry")
                .path("location");
            
            double lat = location.path("lat").asDouble();
            double lng = location.path("lng").asDouble();
            
            if (lat == 0.0 && lng == 0.0) {
                logger.warn("獲取到無效座標 (0,0): {}", address);
                return null;
            }
            
            // 檢查座標是否在合理範圍內（台灣附近）
            if (lat < 20.0 || lat > 26.0 || lng < 118.0 || lng > 122.0) {
                logger.warn("獲取到的座標不在台灣範圍內: {} -> [{}, {}]", address, lat, lng);
                // 對於明顯錯誤的座標，返回null而不是接受
                return null;
            }
            
            logger.info("地理編碼成功: {} -> [{}, {}]", address, lat, lng);
            return new double[]{lat, lng};
            
        } catch (Exception e) {
            logger.error("地理編碼過程發生錯誤: {} - {}", address, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 獲取座標（帶重試和多種查詢策略）
     * @param spot 景點物件
     * @return 座標陣列 [緯度, 經度]，失敗時返回 null
     */
    public double[] getCoordinatesForSpot(SpotVO spot) {
        if (spot == null) {
            logger.warn("景點物件為空，無法進行地理編碼");
            return null;
        }

        String name = spot.getSpotName() != null ? spot.getSpotName().trim() : "";
        String address = spot.getSpotLoc() != null ? spot.getSpotLoc().trim() : "";
        
        if (name.isEmpty() && address.isEmpty()) {
            logger.warn("景點名稱和地址都為空，無法進行地理編碼");
            return null;
        }

        // 清理地址
        String cleanAddress = cleanAddress(address);
        double[] coordinates;

        // 策略 1: 名稱 + 清理後的地址 (最高優先級)
        if (!name.isEmpty() && !cleanAddress.isEmpty()) {
            String query = name + ", " + cleanAddress;
            coordinates = getCoordinatesFromAddress(query);
            if (coordinates != null) return coordinates;
        }
        
        // 策略 2: 只用清理後的地址
        if (!cleanAddress.isEmpty()) {
            coordinates = getCoordinatesFromAddress(cleanAddress);
            if (coordinates != null) return coordinates;
        }

        // 策略 3: 簡化地址
        String simplifiedAddress = simplifyAddress(cleanAddress);
        if (!simplifiedAddress.equals(cleanAddress) && !simplifiedAddress.isEmpty()) {
            logger.info("嘗試簡化地址: {}", simplifiedAddress);
            coordinates = getCoordinatesFromAddress(simplifiedAddress);
            if (coordinates != null) return coordinates;
        }

        // 策略 4: 名稱 + 區域
        String regionAddress = extractRegion(cleanAddress);
        if (!name.isEmpty() && !regionAddress.isEmpty() && !regionAddress.equals(cleanAddress)) {
            String query = name + ", " + regionAddress;
            logger.info("嘗試名稱 + 區域: {}", query);
            coordinates = getCoordinatesFromAddress(query);
            if (coordinates != null) return coordinates;
        }
        
        // 策略 5: 只用區域
        if (!regionAddress.isEmpty() && !regionAddress.equals(cleanAddress)) {
            logger.info("嘗試區域地址: {}", regionAddress);
            coordinates = getCoordinatesFromAddress(regionAddress);
            if (coordinates != null) return coordinates;
        }

        logger.warn("所有地理編碼嘗試都失敗: {} / {}", name, address);
        return null;
    }

    /**
     * 清理地址格式
     */
    private String cleanAddress(String address) {
        String cleaned = address.trim()
                .replaceAll("\\s+", " ")  // 多個空白合併為一個
                .replaceAll("台湾", "台灣")  // 統一用繁體
                .replaceAll("台北縣", "新北市")  // 舊地名轉換
                .replaceAll("高雄縣", "高雄市")
                .replaceAll("台中縣", "台中市")
                .replaceAll("台南縣", "台南市")
                .replaceAll("桃園縣", "桃園市");
        
        // 處理郵遞區號位置錯誤的問題
        // 例如: "金門縣890金沙鎮" -> "金門縣金沙鎮"
        cleaned = cleaned.replaceAll("([市縣])\\s*(\\d{3,5})\\s*([區鄉鎮市])", "$1$3");
        
        // 移除開頭的郵遞區號
        cleaned = cleaned.replaceAll("^\\d{3,5}\\s*", "");
        
        // 移除中間的郵遞區號
        cleaned = cleaned.replaceAll("\\s+\\d{3,5}\\s+", " ");
        
        return cleaned.trim();
    }

    /**
     * 簡化地址（移除詳細門牌號）
     */
    private String simplifyAddress(String address) {
        // 移除詳細門牌號，保留到路名
        return address.replaceAll("\\d+號.*$", "")
                .replaceAll("\\d+巷.*$", "")
                .replaceAll("\\d+弄.*$", "")
                .trim();
    }

    /**
     * 提取區域地址（縣市+區域）
     */
    private String extractRegion(String address) {
        // 提取縣市和區的部分
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\S+[市縣]\\S+[區鄉鎮市])");
        java.util.regex.Matcher matcher = pattern.matcher(address);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return address;
    }
    
    /**
     * 檢查 Google API 是否可用
     * @return true 如果可用
     */
    public boolean isGoogleApiAvailable() {
        logger.info("檢查Google API Key狀態:");
        logger.info("  - API Key 是否為null: {}", googleApiKey == null);
        logger.info("  - API Key 長度: {}", googleApiKey != null ? googleApiKey.length() : 0);
        logger.info("  - API Key 前10字符: {}", googleApiKey != null && googleApiKey.length() > 10 ? 
            googleApiKey.substring(0, 10) + "..." : googleApiKey);
        
        boolean available = googleApiKey != null && !googleApiKey.trim().isEmpty();
        logger.info("  - API 可用狀態: {}", available);
        
        return available;
    }
} 