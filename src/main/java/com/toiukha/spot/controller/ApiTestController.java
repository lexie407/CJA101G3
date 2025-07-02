package com.toiukha.spot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * API測試控制器
 * 用於測試政府觀光資料API連線
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@RestController
@RequestMapping("/spot/api-test")
public class ApiTestController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 測試政府觀光API連線
     * @return API測試結果
     */
    @GetMapping("/test")
    public Map<String, Object> testGovernmentApi() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String apiUrl = "https://media.taiwan.net.tw/XMLReleaseALL_public/scenic_spot_C_f.json";
            
            result.put("status", "testing");
            result.put("apiUrl", apiUrl);
            result.put("timestamp", System.currentTimeMillis());
            
            String response = restTemplate.getForObject(apiUrl, String.class);
            
            if (response != null && !response.trim().isEmpty()) {
                result.put("success", true);
                result.put("message", "觀光署API連線成功");
                result.put("dataSource", "交通部觀光署官方API");
                result.put("responseSize", String.format("%.2f KB", response.length() / 1024.0));
                result.put("responseLength", response.length());
                
                // 嘗試解析JSON來計算景點數量
                try {
                    com.google.gson.JsonObject jsonObject = new com.google.gson.Gson().fromJson(response, com.google.gson.JsonObject.class);
                    if (jsonObject.has("Infos") && jsonObject.get("Infos").isJsonObject()) {
                        com.google.gson.JsonObject infos = jsonObject.getAsJsonObject("Infos");
                        if (infos.has("Info") && infos.get("Info").isJsonArray()) {
                            com.google.gson.JsonArray infoArray = infos.getAsJsonArray("Info");
                            result.put("spotCount", infoArray.size());
                        }
                    }
                } catch (Exception e) {
                    result.put("spotCount", "無法解析");
                    result.put("parseError", e.getMessage());
                }
                
                // 前200字元預覽
                result.put("preview", response.length() > 200 ? response.substring(0, 200) + "..." : response);
                
            } else {
                result.put("success", false);
                result.put("message", "API回應為空");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "API連線失敗");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 測試簡單回應
     * @return 簡單測試訊息
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello! API測試控制器運作正常！");
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "ok");
        return result;
    }
} 