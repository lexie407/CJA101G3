package com.toiukha.spot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

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
} 