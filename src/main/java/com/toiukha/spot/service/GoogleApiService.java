package com.toiukha.spot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;

/**
 * Google API統一管理服務
 * 管理Google的7項合併API服務
 * 
 * @author CJA101G3
 * @version 1.0
 */
@Service
public class GoogleApiService {

    @Autowired
    private Environment environment;

    /**
     * 獲取統一的Google API Key
     * @return API Key或null
     */
    public String getApiKey() {
        String apiKey = environment.getProperty("google.api.key");
        
        if (apiKey != null && !apiKey.trim().isEmpty() && 
            !apiKey.equals("YOUR_GOOGLE_API_KEY") && 
            !apiKey.equals("YOUR_API_KEY")) {
            return apiKey;
        }
        
        return null;
    }

    /**
     * 檢查Google API是否可用
     * @return true如果API Key有效
     */
    public boolean isApiAvailable() {
        return getApiKey() != null;
    }

    /**
     * 獲取Google Maps JavaScript API URL
     * @param libraries 需要載入的函式庫 (如: places,geometry)
     * @return 完整的API URL
     */
    public String getMapsApiUrl(String libraries) {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/js");
        url.append("?key=").append(apiKey);
        url.append("&loading=async");
        url.append("&v=beta"); // 使用 beta 版本以支援更多功能
        // 確保 marker library 一定被加進去
        if (libraries != null && !libraries.trim().isEmpty()) {
            if (!libraries.contains("marker")) {
                libraries = libraries + ",marker";
            }
            url.append("&libraries=").append(libraries);
        } else {
            url.append("&libraries=marker");
        }
        return url.toString();
    }

    /**
     * 獲取Google Places API的基礎URL
     * @return Places API URL
     */
    public String getPlacesApiUrl() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        
        return "https://maps.googleapis.com/maps/api/place";
    }

    /**
     * 獲取Google Geocoding API的基礎URL
     * @return Geocoding API URL
     */
    public String getGeocodingApiUrl() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        
        return "https://maps.googleapis.com/maps/api/geocode/json?key=" + apiKey;
    }

    /**
     * 獲取Google Directions API的基礎URL
     * @return Directions API URL
     */
    public String getDirectionsApiUrl() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        
        return "https://maps.googleapis.com/maps/api/directions/json?key=" + apiKey;
    }

    /**
     * 獲取Google Distance Matrix API的基礎URL
     * @return Distance Matrix API URL
     */
    public String getDistanceMatrixApiUrl() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        
        return "https://maps.googleapis.com/maps/api/distancematrix/json?key=" + apiKey;
    }

    /**
     * 獲取Google Roads API的基礎URL
     * @return Roads API URL
     */
    public String getRoadsApiUrl() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        
        return "https://roads.googleapis.com/v1/nearestRoads?key=" + apiKey;
    }

    /**
     * 獲取Google Street View Static API的基礎URL
     * @return Street View API URL
     */
    public String getStreetViewApiUrl() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return null;
        }
        
        return "https://maps.googleapis.com/maps/api/streetview?key=" + apiKey;
    }

    /**
     * 獲取支援的API服務列表
     * @return 服務名稱陣列
     */
    public String[] getSupportedServices() {
        return new String[]{
            "Maps JavaScript API",
            "Places API", 
            "Geocoding API",
            "Directions API",
            "Distance Matrix API",
            "Roads API",
            "Street View Static API"
        };
    }

    /**
     * 呼叫 Google Places Autocomplete API，取得建議關鍵字
     * @param input 使用者輸入
     * @return 建議字串清單
     */
    public java.util.List<java.util.Map<String, String>> getPlaceAutocompleteSuggestions(String input) {
        System.out.println("input=" + input); // debug log
        String apiKey = getApiKey();
        if (apiKey == null || input == null || input.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String url = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/autocomplete/json")
                .queryParam("input", input)
                .queryParam("language", "zh-TW")
                .queryParam("components", "country:tw")
                .queryParam("key", apiKey)
                .toUriString();
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            java.util.Map<String, Object> response = restTemplate.getForObject(url, java.util.Map.class);
            System.out.println("Google API response: " + response); // debug log
            java.util.List<java.util.Map<String, String>> suggestions = new java.util.ArrayList<>();
            if (response != null && response.containsKey("predictions")) {
                java.util.List<?> predictions = (java.util.List<?>) response.get("predictions");
                for (Object obj : predictions) {
                    if (obj instanceof java.util.Map) {
                        java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
                        java.util.Map<?, ?> formatting = (java.util.Map<?, ?>) map.get("structured_formatting");
                        String main = formatting != null && formatting.get("main_text") != null ? formatting.get("main_text").toString() : null;
                        String secondary = formatting != null && formatting.get("secondary_text") != null ? formatting.get("secondary_text").toString() : null;
                        if (main != null) {
                            java.util.Map<String, String> item = new java.util.HashMap<>();
                            item.put("main", main);
                            item.put("secondary", secondary != null ? secondary : "");
                            suggestions.add(item);
                        }
                    }
                }
            }
            return suggestions;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
} 