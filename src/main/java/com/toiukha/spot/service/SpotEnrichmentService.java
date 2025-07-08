package com.toiukha.spot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.model.SpotImgVO;
import com.toiukha.spot.service.GooglePlacesService.GooglePlaceInfo;
import com.toiukha.spot.service.GooglePlacesService.GooglePlaceDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 景點資料豐富化服務
 * 協調政府資料和 Google Places 資料的整合流程
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Service
public class SpotEnrichmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpotEnrichmentService.class);
    
    @Autowired
    private SpotService spotService;
    
    @Autowired
    private GooglePlacesService googlePlacesService;
    
    @Autowired
    private GovernmentDataService governmentDataService;
    
    @Autowired
    private SpotImgService spotImgService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 完整的資料整合流程
     * 1. 匯入政府資料
     * 2. 豐富化景點資訊 (補充 Google Places 資料)
     * 
     * @param crtId 建立者ID
     * @return 整合結果
     */
    public EnrichmentResult completeDataIntegration(Integer crtId) {
        logger.info("開始完整的景點資料整合流程，建立者ID: {}", crtId);
        
        EnrichmentResult result = new EnrichmentResult();
        
        try {
            // 第一步：匯入政府資料
            logger.info("步驟 1: 匯入政府觀光資料");
            GovernmentDataService.ImportResult importResult = governmentDataService.importGovernmentData(crtId);
            
            result.setImportResult(importResult);
            
            if (!importResult.isSuccess()) {
                result.setSuccess(false);
                result.setErrorMessage("政府資料匯入失敗: " + importResult.getErrorMessage());
                return result;
            }
            
            logger.info("政府資料匯入完成，成功匯入 {} 筆", importResult.getSuccessCount());
            
            // 第二步：豐富化資料
            if (importResult.getSuccessCount() > 0) {
                logger.info("步驟 2: 開始豐富化景點資料");
                enrichSpotData(result);
            }
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            logger.error("資料整合流程發生錯誤", e);
            result.setSuccess(false);
            result.setErrorMessage("資料整合流程發生錯誤: " + e.getMessage());
        }
        
        logger.info("景點資料整合完成。結果: {}", result);
        return result;
    }

    /**
     * 豐富化景點資料
     * 為沒有 Google Places 資訊的景點補充資料
     */
    @Async
    public CompletableFuture<Void> enrichSpotDataAsync() {
        EnrichmentResult result = new EnrichmentResult();
        enrichSpotData(result);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 豐富化景點資料的主要邏輯
     */
    private void enrichSpotData(EnrichmentResult result) {
        // 取得需要豐富化的景點 (沒有 Google Place ID 的景點)
        List<SpotVO> spotsToEnrich = spotService.getAllSpots().stream()
            .filter(spot -> spot.getGooglePlaceId() == null || spot.getGooglePlaceId().trim().isEmpty())
            .filter(spot -> spot.hasValidCoordinates() || (spot.getSpotLoc() != null && !spot.getSpotLoc().trim().isEmpty()))
            .toList();
        
        logger.info("找到 {} 個需要豐富化的景點", spotsToEnrich.size());
        
        int processedCount = 0;
        int enrichedCount = 0;
        int errorCount = 0;
        
        for (SpotVO spot : spotsToEnrich) {
            try {
                // 搜尋 Google Places
                GooglePlaceInfo placeInfo = googlePlacesService.searchPlace(
                    spot.getSpotName(), 
                    spot.getSpotLoc(), 
                    spot.getSpotLat(), 
                    spot.getSpotLng()
                );
                
                if (placeInfo != null && placeInfo.getPlaceId() != null) {
                    // 更新基本資訊
                    spot.setGooglePlaceId(placeInfo.getPlaceId());
                    spot.setGoogleRating(placeInfo.getRating());
                    spot.setGoogleTotalRatings(placeInfo.getUserRatingsTotal());
                    
                    // 取得詳細資訊
                    GooglePlaceDetails details = googlePlacesService.getPlaceDetails(placeInfo.getPlaceId());
                    if (details != null) {
                        updateSpotWithGoogleDetails(spot, details);
                    }
                    
                    // 更新時間戳
                    spot.setSpotUpdatedAt(LocalDateTime.now());
                    
                    // 儲存更新
                    spotService.updateSpot(spot);
                    enrichedCount++;
                    
                    logger.debug("景點 {} 豐富化完成", spot.getSpotName());
                } else {
                    logger.debug("景點 {} 未找到對應的 Google Places", spot.getSpotName());
                }
                
                processedCount++;
                
                // 每處理 100 個景點記錄一次進度
                if (processedCount % 100 == 0) {
                    logger.info("已處理 {} / {} 個景點，豐富化成功: {}", processedCount, spotsToEnrich.size(), enrichedCount);
                }
                
                // 避免過於頻繁的 API 調用
                Thread.sleep(100);
                
            } catch (Exception e) {
                logger.warn("豐富化景點 {} 時發生錯誤: {}", spot.getSpotName(), e.getMessage());
                errorCount++;
            }
        }
        
        result.setProcessedCount(processedCount);
        result.setEnrichedCount(enrichedCount);
        result.setErrorCount(errorCount);
        
        logger.info("景點豐富化完成。處理: {}, 成功: {}, 錯誤: {}", processedCount, enrichedCount, errorCount);
    }

    /**
     * 使用 Google Places 詳細資訊更新景點
     */
    private void updateSpotWithGoogleDetails(SpotVO spot, GooglePlaceDetails details) {
        try {
            // 更新營業時間
            if (details.getOpeningHours() != null && !details.getOpeningHours().isEmpty()) {
                String openingHours = String.join("\n", details.getOpeningHours());
                spot.setOpeningTime(openingHours);
            }
            
            // 更新網站 (如果原本沒有)
            if ((spot.getWebsite() == null || spot.getWebsite().trim().isEmpty()) && 
                details.getWebsite() != null && !details.getWebsite().trim().isEmpty()) {
                spot.setWebsite(details.getWebsite());
            }
            
            // 更新電話 (如果原本沒有)
            if ((spot.getTel() == null || spot.getTel().trim().isEmpty()) && 
                details.getPhoneNumber() != null && !details.getPhoneNumber().trim().isEmpty()) {
                spot.setTel(details.getPhoneNumber());
            }
            
            // 處理照片 URL
            if (details.getPhotoReferences() != null && !details.getPhotoReferences().isEmpty()) {
                List<String> photoUrls = new ArrayList<>();
                for (String photoRef : details.getPhotoReferences()) {
                    String photoUrl = googlePlacesService.getPhotoUrl(photoRef, 800);
                    if (photoUrl != null) {
                        photoUrls.add(photoUrl);
                    }
                    // 最多儲存 5 張照片
                    if (photoUrls.size() >= 5) {
                        break;
                    }
                }
                
                if (!photoUrls.isEmpty()) {
                    // 將照片 URL 陣列轉換為 JSON 字串儲存
                    String photoUrlsJson = objectMapper.writeValueAsString(photoUrls);
                    spot.setPictureUrls(photoUrlsJson);
                }
            }
            
        } catch (Exception e) {
            logger.warn("更新景點 Google 詳細資訊時發生錯誤: {}", e.getMessage());
        }
    }

    /**
     * 批次豐富化指定的景點
     * @param spotIds 景點ID列表
     * @return 豐富化結果
     */
    public EnrichmentResult enrichSpecificSpots(List<Integer> spotIds) {
        logger.info("開始批次豐富化 {} 個指定景點", spotIds.size());
        
        EnrichmentResult result = new EnrichmentResult();
        int enrichedCount = 0;
        int errorCount = 0;
        
        for (Integer spotId : spotIds) {
            try {
                SpotVO spot = spotService.getSpotById(spotId);
                if (spot == null) {
                    logger.warn("景點不存在: {}", spotId);
                    errorCount++;
                    continue;
                }
                
                // 搜尋並更新 Google Places 資訊
                GooglePlaceInfo placeInfo = googlePlacesService.searchPlace(
                    spot.getSpotName(), 
                    spot.getSpotLoc(), 
                    spot.getSpotLat(), 
                    spot.getSpotLng()
                );
                
                if (placeInfo != null && placeInfo.getPlaceId() != null) {
                    spot.setGooglePlaceId(placeInfo.getPlaceId());
                    spot.setGoogleRating(placeInfo.getRating());
                    spot.setGoogleTotalRatings(placeInfo.getUserRatingsTotal());
                    
                    GooglePlaceDetails details = googlePlacesService.getPlaceDetails(placeInfo.getPlaceId());
                    if (details != null) {
                        updateSpotWithGoogleDetails(spot, details);
                    }
                    
                    spot.setSpotUpdatedAt(LocalDateTime.now());
                    spotService.updateSpot(spot);
                    enrichedCount++;
                }
                
                Thread.sleep(100); // 避免過於頻繁的 API 調用
                
            } catch (Exception e) {
                logger.error("豐富化景點 {} 時發生錯誤", spotId, e);
                errorCount++;
            }
        }
        
        result.setProcessedCount(spotIds.size());
        result.setEnrichedCount(enrichedCount);
        result.setErrorCount(errorCount);
        result.setSuccess(true);
        
        logger.info("批次豐富化完成。處理: {}, 成功: {}, 錯誤: {}", spotIds.size(), enrichedCount, errorCount);
        return result;
    }

    /**
     * 使用 Google Places API 擴充景點資訊
     * @param spot 景點
     * @return 擴充後的景點
     */
    public Map<String, Object> enrichSpotWithGoogleData(SpotVO spot) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        if (spot == null) {
            logger.warn("嘗試擴充空的景點資訊");
            result.put("error", "景點資訊為空");
            return result;
        }
        
        try {
            // 檢查 API Key 是否有效
            if (!googlePlacesService.isApiKeyValid()) {
                logger.error("Google Places API Key 無效，無法擴充景點資訊");
                result.put("error", "Google Places API Key 無效");
                return result;
            }
            
            // 取得 Google Place 資訊
            GooglePlaceInfo placeInfo = getGooglePlaceInfo(spot);
            
            if (placeInfo == null) {
                logger.warn("無法從 Google Places API 取得景點資訊：{}", spot.getSpotName());
                result.put("error", "無法從 Google Places API 取得景點資訊");
                return result;
            }
            
            // 取得詳細資訊
            GooglePlaceDetails details = getGooglePlaceDetails(placeInfo.getPlaceId());
            
            // 組合結果
            Map<String, Object> data = new HashMap<>();
            data.put("placeId", placeInfo.getPlaceId());
            data.put("name", placeInfo.getName());
            data.put("formattedAddress", placeInfo.getFormattedAddress());
            
            // 評分相關
            if (placeInfo.getRating() != null) {
                data.put("rating", placeInfo.getRating());
                data.put("userRatingsTotal", placeInfo.getUserRatingsTotal());
            }
            
            // 詳細資訊
            if (details != null) {
                data.put("phoneNumber", details.getPhoneNumber());
                data.put("website", details.getWebsite());
                data.put("openingHours", details.getOpeningHours());
                
                // 照片
                if (details.getPhotoReferences() != null && !details.getPhotoReferences().isEmpty()) {
                    data.put("photoReferences", details.getPhotoReferences());
                    
                    // 取得第一張照片的 URL 作為預覽
                    String firstPhotoRef = details.getPhotoReferences().get(0);
                    String photoUrl = googlePlacesService.getPhotoUrl(firstPhotoRef, 400);
                    data.put("previewPhotoUrl", photoUrl);
                }
            }
            
            result.put("success", true);
            result.put("data", data);
            
            logger.info("成功從 Google Places API 擴充景點資訊：{}", spot.getSpotName());
            
        } catch (Exception e) {
            logger.error("擴充景點資訊時發生錯誤：{}", e.getMessage());
            result.put("error", "擴充景點資訊時發生錯誤：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 取得 Google Place 資訊
     * @param spot 景點
     * @return Google Place 資訊
     */
    private GooglePlaceInfo getGooglePlaceInfo(SpotVO spot) {
        try {
            String spotName = spot.getSpotName();
            String address = spot.getSpotLoc();
            Double lat = spot.getSpotLat();
            Double lng = spot.getSpotLng();
            
            logger.debug("嘗試取得景點資訊，景點名稱：{}，地址：{}", spotName, address);
            
            // 使用 Google Places API 搜尋景點
            GooglePlaceInfo placeInfo = googlePlacesService.searchPlace(spotName, address, lat, lng);
            
            if (placeInfo != null) {
                logger.info("成功取得景點資訊：{}", placeInfo.getName());
            } else {
                logger.warn("無法取得景點資訊：{}", spotName);
            }
            
            return placeInfo;
            
        } catch (Exception e) {
            logger.error("取得 Google Place 資訊時發生錯誤：{}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 取得 Google Place 詳細資訊
     * @param placeId Google Place ID
     * @return Google Place 詳細資訊
     */
    private GooglePlaceDetails getGooglePlaceDetails(String placeId) {
        try {
            if (placeId == null || placeId.trim().isEmpty()) {
                logger.warn("Place ID 為空，無法取得詳細資訊");
                return null;
            }
            
            logger.debug("嘗試取得景點詳細資訊，Place ID：{}", placeId);
            
            // 使用 Google Places API 取得詳細資訊
            GooglePlaceDetails details = googlePlacesService.getPlaceDetails(placeId);
            
            if (details != null) {
                logger.info("成功取得景點詳細資訊，Place ID：{}", placeId);
                
                // 記錄取得的資訊
                logger.debug("電話：{}", details.getPhoneNumber());
                logger.debug("網站：{}", details.getWebsite());
                logger.debug("照片數量：{}", 
                    details.getPhotoReferences() != null ? details.getPhotoReferences().size() : 0);
            } else {
                logger.warn("無法取得景點詳細資訊，Place ID：{}", placeId);
            }
            
            return details;
            
        } catch (Exception e) {
            logger.error("取得 Google Place 詳細資訊時發生錯誤：{}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 檢查 Google Places API 是否可用
     * @return 如果 API 可用則返回 true，否則返回 false
     */
    public boolean isGooglePlacesApiAvailable() {
        return googlePlacesService.isApiKeyValid();
    }

    /**
     * 根據景點名稱和地址獲取Google Places資訊
     * @param name 景點名稱
     * @param address 景點地址
     * @return Google Places資訊
     */
    public Map<String, Object> getGooglePlaceInfo(String name, String address) {
        logger.info("獲取景點 '{}' 的Google Places資訊，地址: '{}'", name, address);
        Map<String, Object> result = new HashMap<>();
        
        // 檢查API Key是否設置
        try {
            boolean apiKeyValid = googlePlacesService.isApiKeyValid();
            if (!apiKeyValid) {
                logger.error("Google API Key 無效或未設置");
                result.put("error", "Google API Key 無效或未設置");
                result.put("rating", null);
                result.put("website", "");
                result.put("phoneNumber", "");
                return result;
            }
        } catch (Exception e) {
            logger.error("檢查API Key時發生錯誤", e);
        }
        
        try {
            // 使用現有的GooglePlacesService搜尋景點
            GooglePlaceInfo placeInfo = googlePlacesService.searchPlace(
                name, address, null, null
            );
            
            if (placeInfo != null && placeInfo.getPlaceId() != null) {
                // 基本資訊
                logger.info("成功找到景點: {}, Place ID: {}", placeInfo.getName(), placeInfo.getPlaceId());
                result.put("placeId", placeInfo.getPlaceId());
                result.put("name", placeInfo.getName());
                result.put("rating", placeInfo.getRating()); // 即使為null也包含
                result.put("userRatingsTotal", placeInfo.getUserRatingsTotal());
                result.put("formattedAddress", placeInfo.getFormattedAddress());
                
                // 獲取詳細資訊
                logger.info("正在獲取景點詳細資訊...");
                GooglePlaceDetails details = googlePlacesService.getPlaceDetails(placeInfo.getPlaceId());
                if (details != null) {
                    // 即使為null或空字串也包含這些欄位
                    String website = details.getWebsite() != null ? details.getWebsite() : "";
                    String phone = details.getPhoneNumber() != null ? details.getPhoneNumber() : "";
                    
                    result.put("website", website);
                    result.put("phoneNumber", phone);
                    
                    logger.info("景點詳情 - 網站: {}, 電話: {}", 
                               website.isEmpty() ? "無" : website, 
                               phone.isEmpty() ? "無" : phone);
                    
                    // 處理營業時間
                    if (details.getOpeningHours() != null && !details.getOpeningHours().isEmpty()) {
                        result.put("openingHours", details.getOpeningHours());
                        logger.debug("獲取到營業時間: {} 項", details.getOpeningHours().size());
                    } else {
                        result.put("openingHours", new ArrayList<String>());
                        logger.debug("無營業時間資訊");
                    }
                    
                    // 處理照片URL
                    if (details.getPhotoReferences() != null && !details.getPhotoReferences().isEmpty()) {
                        List<String> photoUrls = new ArrayList<>();
                        for (String photoRef : details.getPhotoReferences()) {
                            String photoUrl = googlePlacesService.getPhotoUrl(photoRef, 800);
                            if (photoUrl != null) {
                                photoUrls.add(photoUrl);
                            }
                            // 最多返回5張照片
                            if (photoUrls.size() >= 5) {
                                break;
                            }
                        }
                        result.put("photoUrls", photoUrls);
                        logger.debug("獲取到照片: {} 張", photoUrls.size());
                    } else {
                        result.put("photoUrls", new ArrayList<String>());
                        logger.debug("無照片資訊");
                    }
                } else {
                    // 如果沒有獲取到詳細資訊，也添加空值
                    logger.warn("找到景點但未能獲取詳細資訊: {}", placeInfo.getName());
                    result.put("website", "");
                    result.put("phoneNumber", "");
                    result.put("openingHours", new ArrayList<String>());
                    result.put("photoUrls", new ArrayList<String>());
                }
                
                logger.info("成功獲取景點 '{}' 的Google Places資訊: {}", name, result);
            } else {
                logger.warn("未找到景點 '{}' 的Google Places資訊", name);
                result.put("message", "未找到景點的Google Places資訊");
                // 添加空值，確保前端能夠處理
                result.put("rating", null);
                result.put("website", "");
                result.put("phoneNumber", "");
                
                // 嘗試直接使用Google搜索URL，作為建議
                String googleSearchUrl = "https://www.google.com/search?q=" + 
                                       java.net.URLEncoder.encode(name + " " + address, "UTF-8");
                result.put("googleSearchUrl", googleSearchUrl);
                logger.info("提供Google搜索URL作為替代: {}", googleSearchUrl);
            }
        } catch (Exception e) {
            logger.error("獲取Google Places資訊時發生錯誤: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
            // 添加空值，確保前端能夠處理
            result.put("rating", null);
            result.put("website", "");
            result.put("phoneNumber", "");
        }
        
        return result;
    }

    /**
     * 豐富化結果統計類別
     */
    public static class EnrichmentResult {
        private boolean success = true;
        private String errorMessage;
        private GovernmentDataService.ImportResult importResult;
        private int processedCount = 0;
        private int enrichedCount = 0;
        private int errorCount = 0;

        // Getter 和 Setter
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public GovernmentDataService.ImportResult getImportResult() { return importResult; }
        public void setImportResult(GovernmentDataService.ImportResult importResult) { this.importResult = importResult; }
        
        public int getProcessedCount() { return processedCount; }
        public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
        
        public int getEnrichedCount() { return enrichedCount; }
        public void setEnrichedCount(int enrichedCount) { this.enrichedCount = enrichedCount; }
        
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

        @Override
        public String toString() {
            return String.format("EnrichmentResult{success=%s, processed=%d, enriched=%d, error=%d, importResult=%s}", 
                               success, processedCount, enrichedCount, errorCount, importResult);
        }
    }

    public void enrichSpotPictureUrlIfNeeded(List<SpotVO> spots) {
        for (SpotVO spot : spots) {
            // 1. 先檢查 spotimg 表是否有圖片（使用者上傳圖優先）
            List<SpotImgVO> spotImages = spotImgService.getImagesBySpotId(spot.getSpotId());
            if (!spotImages.isEmpty()) {
                spot.setFirstPictureUrl(spotImages.get(0).getImgPath());
                continue; // 有上傳圖就直接用，不再補 Google 圖片
            }
            // 2. 沒有上傳圖，才自動補 Google 圖片
            String url = spot.getFirstPictureUrl();
            if (url == null || url.trim().isEmpty() || url.contains("404.png")) {
                GooglePlaceDetails details = null;
                if (spot.getGooglePlaceId() != null && !spot.getGooglePlaceId().isEmpty()) {
                    details = googlePlacesService.getPlaceDetails(spot.getGooglePlaceId());
                } else {
                    Map<String, Object> placeInfo = getGooglePlaceInfo(spot.getSpotName(), spot.getSpotLoc());
                    if (placeInfo != null && placeInfo.get("placeId") != null) {
                        spot.setGooglePlaceId(placeInfo.get("placeId").toString());
                        details = googlePlacesService.getPlaceDetails(spot.getGooglePlaceId());
                    }
                }
                if (details != null && details.getPhotoReferences() != null && !details.getPhotoReferences().isEmpty()) {
                    String photoRef = details.getPhotoReferences().get(0);
                    String photoUrl = googlePlacesService.getPhotoUrl(photoRef, 800);
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        spot.setFirstPictureUrl(photoUrl);
                    }
                }
            }
        }
    }
} 