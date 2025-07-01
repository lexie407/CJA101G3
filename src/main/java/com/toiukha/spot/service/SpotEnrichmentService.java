package com.toiukha.spot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.GooglePlacesService.GooglePlaceInfo;
import com.toiukha.spot.service.GooglePlacesService.GooglePlaceDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
} 