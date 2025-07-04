package com.toiukha.spot.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toiukha.spot.dto.GovernmentSpotData;
import com.toiukha.spot.model.SpotVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 政府觀光資料處理服務
 * 使用 Jackson 流式 API 處理大型 JSON 檔案
 * 
 * @author CJA101G3 景點模組開發  
 * @version 1.0
 */
@Service
public class GovernmentDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(GovernmentDataService.class);
    
    private static final int BATCH_SIZE = 1000; // 批次處理大小
    private static final String JSON_FILE_PATH = "classpath:static/vendors/spot/data/scenic_spot_C_f.json";
    
    private static final java.util.Map<String, String> CITY_NAME_MAP;

    static {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("Taipei", "臺北市");
        map.put("NewTaipei", "新北市");
        map.put("Taoyuan", "桃園市");
        map.put("Taichung", "臺中市");
        map.put("Tainan", "臺南市");
        map.put("Kaohsiung", "高雄市");
        map.put("Keelung", "基隆市");
        map.put("Hsinchu", "新竹市");
        map.put("HsinchuCounty", "新竹縣");
        map.put("Miaoli", "苗栗縣");
        map.put("Changhua", "彰化縣");
        map.put("Nantou", "南投縣");
        map.put("Yunlin", "雲林縣");
        map.put("Chiayi", "嘉義市");
        map.put("ChiayiCounty", "嘉義縣");
        map.put("Pingtung", "屏東縣");
        map.put("Yilan", "宜蘭縣");
        map.put("Hualien", "花蓮縣");
        map.put("Taitung", "臺東縣");
        map.put("Penghu", "澎湖縣");
        map.put("Kinmen", "金門縣");
        map.put("Lienchiang", "連江縣");
        CITY_NAME_MAP = java.util.Collections.unmodifiableMap(map);
    }
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private SpotService spotService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 檢查政府資料來源是否可用 (此處為檢查檔案是否存在)
     * @return true 如果檔案存在, false 如果不存在
     */
    public boolean isApiAvailable() {
        try {
            Resource resource = resourceLoader.getResource(JSON_FILE_PATH);
            return resource.exists();
        } catch (Exception e) {
            logger.error("檢查政府資料來源時發生錯誤", e);
            return false;
        }
    }
    
    /**
     * 匯入政府觀光資料 (舊版，不帶參數)
     * @param crtId 建立者ID
     * @return 匯入結果統計
     */
    public ImportResult importGovernmentData(Integer crtId) {
        return importGovernmentData(crtId, -1, null); // -1 表示無限制
    }

    /**
     * 匯入政府觀光資料 (新版，帶參數)
     * @param crtId 建立者ID
     * @param limit 限制匯入筆數 (-1為不限制)
     * @param city  要篩選的城市 (null為不限制)
     * @return 匯入結果統計
     */
    public ImportResult importGovernmentData(Integer crtId, int limit, String city) {
        logger.info("開始匯入政府觀光資料，建立者ID: {}, 上限: {}, 城市: {}", crtId, limit, city == null ? "不限" : city);
        
        ImportResult result = new ImportResult();
        
        try {
            Resource resource = resourceLoader.getResource(JSON_FILE_PATH);
            
            if (!resource.exists()) {
                logger.error("政府資料檔案不存在: {}", JSON_FILE_PATH);
                result.setSuccess(false);
                result.setErrorMessage("政府資料檔案不存在");
                return result;
            }
            
            try (InputStream bomInputStream = new BOMInputStream(resource.getInputStream())) {
                // 如果是全台匯入（沒有指定城市），使用隨機匯入方式
                if (city == null || city.trim().isEmpty()) {
                    processJsonFileWithRandomOrder(bomInputStream, crtId, result, limit);
                } else {
                    processJsonFile(bomInputStream, crtId, result, limit, city);
                }
            }
            
        } catch (IOException e) {
            logger.error("讀取政府資料檔案時發生錯誤", e);
            result.setSuccess(false);
            result.setErrorMessage("讀取政府資料檔案時發生錯誤: " + e.getMessage());
        }
        
        logger.info("政府觀光資料匯入完成。成功: {}, 跳過: {}, 錯誤: {}",
                    result.getSuccessCount(), result.getSkippedCount(), result.getErrorCount());
        
        return result;
    }

    /**
     * 使用隨機順序處理 JSON 檔案 (全台匯入時使用)
     */
    private void processJsonFileWithRandomOrder(InputStream inputStream, Integer crtId, ImportResult result, int limit) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonParser jsonParser = jsonFactory.createParser(reader);
        
        List<GovernmentSpotData> allSpots = new ArrayList<>();
        
        // 首先讀取所有景點資料到記憶體中
        if (navigateToInfoArray(jsonParser)) {
            logger.info("開始讀取所有景點資料...");
            
            while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                try {
                    GovernmentSpotData govData = objectMapper.readValue(jsonParser, GovernmentSpotData.class);
                    allSpots.add(govData);
                } catch (Exception e) {
                    logger.warn("讀取單筆資料時發生錯誤", e);
                    result.incrementErrorCount();
                }
            }
        }
        
        jsonParser.close();
        
        logger.info("讀取完成，共 {} 筆景點資料，開始隨機打亂順序...", allSpots.size());
        
        // 隨機打亂順序
        java.util.Collections.shuffle(allSpots, new java.util.Random());
        
        // 按照隨機順序處理景點
        List<SpotVO> batch = new ArrayList<>();
        int processedCount = 0;
        
        for (GovernmentSpotData govData : allSpots) {
            // 如果已達到匯入上限，則停止
            if (limit != -1 && result.getSuccessCount() >= limit) {
                logger.info("已達到指定的匯入上限: {}", limit);
                break;
            }
            
            try {
                // 轉換為 SpotVO 並加入批次
                SpotVO spot = convertToSpotVO(govData, crtId);
                if (spot != null) {
                    batch.add(spot);
                } else {
                    result.incrementSkippedCount();
                }
                
                // 當批次達到指定大小時進行處理
                if (batch.size() >= BATCH_SIZE) {
                    processBatch(batch, result, limit);
                    batch.clear();
                    
                    processedCount += BATCH_SIZE;
                    logger.info("已處理 {} 筆資料 (隨機順序)", processedCount);
                }
                
            } catch (Exception e) {
                logger.warn("處理單筆資料時發生錯誤", e);
                result.incrementErrorCount();
            }
        }
        
        // 處理剩餘的資料
        if (!batch.isEmpty()) {
            processBatch(batch, result, limit);
            processedCount += batch.size();
            logger.info("處理完成，總計 {} 筆資料 (隨機順序)", processedCount);
        }
    }

    /**
     * 使用流式 API 處理 JSON 檔案
     */
    private void processJsonFile(InputStream inputStream, Integer crtId, ImportResult result, int limit, String city) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonParser jsonParser = jsonFactory.createParser(reader);
        
        List<SpotVO> batch = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        
        // 導航到 Info 陣列
        if (navigateToInfoArray(jsonParser)) {
            
            // 處理陣列中的每個物件
            while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                // 如果已達到匯入上限，則停止
                if (limit != -1 && result.getSuccessCount() >= limit) {
                    logger.info("已達到指定的匯入上限: {}", limit);
                    break;
                }

                try {
                    // 解析單個景點資料
                    GovernmentSpotData govData = objectMapper.readValue(jsonParser, GovernmentSpotData.class);
                    
                    // 如果指定了城市，則進行篩選
                    if (city != null && !city.trim().isEmpty()) {
                        String chineseCityName = CITY_NAME_MAP.get(city);
                        if (chineseCityName == null) {
                            logger.warn("未知的城市名稱對應: {}, 將不進行城市篩選。", city);
                        } else if (!chineseCityName.equals(govData.getRegion())) {
                            continue; // 跳過不符合城市條件的資料
                        }
                    }

                    // 轉換為 SpotVO 並加入批次
                    SpotVO spot = convertToSpotVO(govData, crtId);
                    if (spot != null) {
                        batch.add(spot);
                    } else {
                        result.incrementSkippedCount();
                    }
                    
                    // 當批次達到指定大小時進行處理
                    if (batch.size() >= BATCH_SIZE) {
                        processBatch(batch, result, limit);
                        batch.clear();
                        
                        int count = processedCount.addAndGet(BATCH_SIZE);
                        logger.info("已處理 {} 筆資料", count);
                    }
                    
                } catch (Exception e) {
                    logger.warn("處理單筆資料時發生錯誤", e);
                    result.incrementErrorCount();
                }
            }
            
            // 處理剩餘的資料
            if (!batch.isEmpty()) {
                processBatch(batch, result, limit);
                processedCount.addAndGet(batch.size());
            }
        }
        
        jsonParser.close();
    }

    /**
     * 導航到 JSON 檔案中的 Info 陣列
     */
    private boolean navigateToInfoArray(JsonParser jsonParser) throws IOException {
        while (jsonParser.nextToken() != null) {
            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
                String fieldName = jsonParser.currentName();
                if ("Infos".equals(fieldName)) {
                    jsonParser.nextToken(); // 移動到 Infos 物件
                    while (jsonParser.nextToken() != null) {
                        if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME && 
                            "Info".equals(jsonParser.currentName())) {
                            jsonParser.nextToken(); // 移動到 Info 陣列
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 轉換政府資料為 SpotVO
     */
    private SpotVO convertToSpotVO(GovernmentSpotData govData, Integer crtId) {
        // 驗證必要欄位
        if (govData.getName() == null || govData.getName().trim().isEmpty()) {
            logger.debug("跳過沒有名稱的景點資料: {}", govData.getId());
            return null;
        }
        
        if (govData.getAddress() == null || govData.getAddress().trim().isEmpty()) {
            logger.debug("跳過沒有地址的景點資料: {}", govData.getId());
            return null;
        }

        // 檢查是否已存在相同的政府資料ID
        if (spotService.existsByGovtId(govData.getId())) {
            logger.debug("景點已存在，跳過: {}", govData.getId());
            return null;
        }

        SpotVO spot = new SpotVO();
        
        // 基本資訊
        // 清理景點名稱中的非法字元
        String originalName = govData.getName();
        String sanitizedName = originalName.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s\\-()（）.]", " ").trim();
        spot.setSpotName(sanitizedName);
        
        spot.setSpotLoc(govData.getAddress());
        
        String description = govData.getDescription() != null ? govData.getDescription() : govData.getShortDescription();
        if (description != null && description.length() > 500) {
            description = description.substring(0, 497) + "...";
        }
        spot.setSpotDesc(description);
        
        spot.setCrtId(crtId);
        spot.setSpotStatus((byte) 1); // 預設為上架
        spot.setSpotCreateAt(LocalDateTime.now());
        
        // 政府資料相關欄位
        spot.setGovtId(govData.getId());
        spot.setZone(govData.getZone());
        // 標準化地區名稱 - 將"臺"字轉換為"台"字以保持一致性
        String region = govData.getRegion();
        if (region != null) {
            region = region.replace("臺", "台");
        }
        spot.setRegion(region);
        spot.setTown(govData.getTown());
        spot.setZipcode(govData.getZipcode());
        spot.setTel(govData.getTel());
        spot.setEmail(govData.getEmail());
        spot.setWebsite(govData.getWebsite());
        spot.setTravelingInfo(govData.getTravelingInfo());
        spot.setOpeningTime(govData.getOpenTime());
        spot.setTicketInfo(govData.getTicketInfo());
        spot.setParkingInfo(govData.getParkingInfo());
        
        // 經緯度
        if (govData.hasValidCoordinates()) {
            spot.setSpotLat(govData.getLatitudeValue());
            spot.setSpotLng(govData.getLongitudeValue());
        }
        
        return spot;
    }

    /**
     * 批次處理景點資料，包含儲存至資料庫
     */
    private void processBatch(List<SpotVO> batch, ImportResult result, int limit) {
        if (batch.isEmpty()) {
            return;
        }

        List<SpotVO> toSave = batch;
        int batchSize = batch.size();

        if (limit != -1) {
            int currentSuccessCount = result.getSuccessCount();
            if (currentSuccessCount >= limit) {
                result.incrementSkippedCount(batchSize);
                logger.debug("已達到匯入上限，跳過 {} 筆資料", batchSize);
                return; // 已達到上限，跳過整個批次
            }

            int remainingCapacity = limit - currentSuccessCount;
            if (batchSize > remainingCapacity) {
                toSave = batch.subList(0, remainingCapacity);
                int skipped = batchSize - toSave.size();
                if (skipped > 0) {
                    result.incrementSkippedCount(skipped);
                    logger.debug("批次超過上限，裁切後儲存 {} 筆，跳過 {} 筆", toSave.size(), skipped);
                }
            }
        }

        try {
            if (!toSave.isEmpty()) {
                int successCount = spotService.saveAll(toSave);
                result.incrementSuccessCount(successCount);
            }
        } catch (DataAccessException e) {
            logger.error("批次儲存資料時發生資料庫錯誤", e); 
            result.incrementErrorCount(toSave.size());
        } catch (Exception e) {
            logger.error("批次處理時發生未知錯誤", e);
            result.incrementErrorCount(toSave.size());
        }
    }

    /**
     * 匯入結果統計類別
     */
    public static class ImportResult {
        private boolean success = true;
        private String errorMessage;
        private int successCount = 0;
        private int skippedCount = 0;
        private int errorCount = 0;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void incrementSuccessCount() {
            this.successCount++;
        }

        public void incrementSuccessCount(int count) {
            this.successCount += count;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public void incrementSkippedCount() {
            this.skippedCount++;
        }

        public void incrementSkippedCount(int count) {
            this.skippedCount += count;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void incrementErrorCount() {
            this.errorCount++;
        }

        public void incrementErrorCount(int count) {
            this.errorCount += count;
        }

        public int getTotalProcessed() {
            return successCount + skippedCount + errorCount;
        }

        @Override
        public String toString() {
            return String.format("ImportResult{success=%s, processed=%d, success=%d, skipped=%d, error=%d, message='%s'}", 
                               success, getTotalProcessed(), successCount, skippedCount, errorCount, errorMessage);
        }
    }
} 