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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.HashMap;

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
        map.put("TaoyuanCounty", "桃園市");
        map.put("Taichung", "臺中市");
        map.put("TaichungCity", "臺中市");
        map.put("Tainan", "臺南市");
        map.put("TainanCity", "臺南市");
        map.put("Kaohsiung", "高雄市");
        map.put("KaohsiungCity", "高雄市");
        map.put("Keelung", "基隆市");
        map.put("Hsinchu", "新竹市");
        map.put("HsinchuCounty", "新竹縣");
        map.put("Miaoli", "苗栗縣");
        map.put("MiaoliCounty", "苗栗縣");
        map.put("Changhua", "彰化縣");
        map.put("ChanghuaCounty", "彰化縣");
        map.put("Nantou", "南投縣");
        map.put("NantouCounty", "南投縣");
        map.put("Yunlin", "雲林縣");
        map.put("YunlinCounty", "雲林縣");
        map.put("Chiayi", "嘉義市");
        map.put("ChiayiCounty", "嘉義縣");
        map.put("Pingtung", "屏東縣");
        map.put("PingtungCounty", "屏東縣");
        map.put("Yilan", "宜蘭縣");
        map.put("YilanCounty", "宜蘭縣");
        map.put("Hualien", "花蓮縣");
        map.put("HualienCounty", "花蓮縣");
        map.put("Taitung", "臺東縣");
        map.put("TaitungCounty", "臺東縣");
        map.put("Penghu", "澎湖縣");
        map.put("PenghuCounty", "澎湖縣");
        map.put("Kinmen", "金門縣");
        map.put("KinmenCounty", "金門縣");
        map.put("Lienchiang", "連江縣");
        map.put("LienchiangCounty", "連江縣");
        CITY_NAME_MAP = java.util.Collections.unmodifiableMap(map);
    }
    
    @Autowired
    private SpotService spotService;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
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
            
            // 如果指定了城市，先檢查城市代碼是否有效
            if (city != null && !city.trim().isEmpty()) {
                String chineseCityName = CITY_NAME_MAP.get(city);
                if (chineseCityName == null) {
                    logger.warn("無效的城市代碼: {}，將使用原始代碼進行匯入", city);
                    
                    // 輸出所有有效的城市代碼，以便調試
                    logger.info("有效的城市代碼列表:");
                    CITY_NAME_MAP.forEach((key, value) -> {
                        logger.info("  {} -> {}", key, value);
                    });
                } else {
                    logger.info("準備匯入 {} ({}) 的景點資料", chineseCityName, city);
                }
            }
            
            try (InputStream bomInputStream = new BOMInputStream(resource.getInputStream())) {
                // 如果是全台匯入（沒有指定城市），使用隨機匯入方式
                if (city == null || city.trim().isEmpty()) {
                    processJsonFileWithRandomOrder(bomInputStream, crtId, result, limit);
                } else {
                    processJsonFile(bomInputStream, crtId, result, limit, city);
                }
            }
            
            // 匯入完成後，記錄結果
            logger.info("匯入完成，成功: {}, 跳過: {}, 錯誤: {}", 
                        result.getSuccessCount(), result.getSkippedCount(), result.getErrorCount());
            
            // 如果成功數量為0，可能是有問題
            if (result.getSuccessCount() == 0) {
                logger.warn("匯入完成但沒有成功匯入任何資料，可能是資料源問題或篩選條件過嚴");
                if (city != null && !city.trim().isEmpty()) {
                    logger.warn("請檢查城市代碼 {} 是否正確，以及資料源中是否有對應的資料", city);
                    
                    // 檢查是否有類似的城市代碼
                    CITY_NAME_MAP.keySet().stream()
                        .filter(key -> key.toLowerCase().contains(city.toLowerCase()) || 
                                city.toLowerCase().contains(key.toLowerCase()))
                        .forEach(key -> logger.info("可能相關的城市代碼: {} -> {}", key, CITY_NAME_MAP.get(key)));
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
                    processBatch(batch, result);
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
            processBatch(batch, result);
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
        AtomicInteger cityMatchCount = new AtomicInteger(0); // 計數符合城市條件的資料
        
        // 導航到 Info 陣列
        if (navigateToInfoArray(jsonParser)) {
            logger.info("開始處理景點資料，城市篩選: {}", city != null ? city : "全部");
            
            // 獲取城市的中文名稱，用於篩選
            String targetChineseCityName = CITY_NAME_MAP.get(city);
            if (targetChineseCityName == null) {
                logger.warn("未知的城市名稱對應: {}, 將嘗試使用原始代碼進行篩選", city);
            } else {
                logger.info("將篩選城市: {} ({})", targetChineseCityName, city);
            }
            
            // 處理陣列中的每個物件
            while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                // 如果已達到匯入上限，則停止
                if (limit != -1 && result.getSuccessCount() >= limit) {
                    logger.info("已達到匯入上限 {} 筆，停止處理", limit);
                    break;
                }
                
                try {
                    GovernmentSpotData govData = objectMapper.readValue(jsonParser, GovernmentSpotData.class);
                    processedCount.incrementAndGet();
                    
                    // 如果指定了城市，則進行篩選
                    if (city != null && !city.trim().isEmpty()) {
                        // 獲取地址中的城市名稱
                        String address = govData.getAddress();
                        if (address == null || address.trim().isEmpty()) {
                            continue; // 跳過沒有地址的資料
                        }
                        
                        // 嘗試多種方式匹配城市
                        boolean cityMatched = false;
                        
                        // 1. 直接使用中文名稱匹配
                        if (targetChineseCityName != null) {
                            String normalizedAddress = normalizeCity(address);
                            String normalizedTargetCity = normalizeCity(targetChineseCityName);
                            
                            if (normalizedAddress.contains(normalizedTargetCity)) {
                                cityMatched = true;
                                logger.debug("城市匹配成功(中文名稱): {} 包含 {}", normalizedAddress, normalizedTargetCity);
                            }
                        }
                        
                        // 2. 使用城市代碼匹配（針對某些特殊情況）
                        if (!cityMatched) {
                            String region = govData.getRegion();
                            if (region != null && !region.trim().isEmpty()) {
                                String normalizedRegion = normalizeCity(region);
                                
                                // 檢查是否有任何城市名稱匹配
                                for (Map.Entry<String, String> entry : CITY_NAME_MAP.entrySet()) {
                                    String normalizedCityName = normalizeCity(entry.getValue());
                                    if (normalizedRegion.contains(normalizedCityName)) {
                                        if (entry.getKey().equalsIgnoreCase(city)) {
                                            cityMatched = true;
                                            logger.debug("城市匹配成功(地區代碼): {} 匹配 {}", normalizedRegion, city);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 如果城市不匹配，則跳過
                        if (!cityMatched) {
                            continue;
                        }
                        
                        // 計數符合城市條件的資料
                        cityMatchCount.incrementAndGet();
                    }
                    
                    // 轉換為SpotVO
                    SpotVO spot = convertToSpotVO(govData, crtId);
                    
                    if (spot != null) {
                        batch.add(spot);
                        
                        // 當批次達到一定大小時，進行批次插入
                        if (batch.size() >= BATCH_SIZE) {
                            processBatch(batch, result);
                            batch.clear();
                        }
                    } else {
                        result.incrementSkippedCount();
                    }
                    
                } catch (Exception e) {
                    logger.error("處理景點資料時發生錯誤", e);
                    result.incrementErrorCount();
                }
            }
            
            // 處理剩餘的批次
            if (!batch.isEmpty()) {
                processBatch(batch, result);
            }
            
            logger.info("景點資料處理完成，總處理: {}, 城市匹配: {}, 成功: {}, 跳過: {}, 錯誤: {}",
                        processedCount.get(), cityMatchCount.get(), result.getSuccessCount(), 
                        result.getSkippedCount(), result.getErrorCount());
            
            // 如果篩選了城市但沒有找到匹配的資料，輸出警告
            if (city != null && !city.trim().isEmpty() && cityMatchCount.get() == 0) {
                logger.warn("未找到任何符合城市 {} 的資料，請檢查城市代碼是否正確", city);
            }
        }
        
        jsonParser.close();
    }
    
    /**
     * 標準化城市名稱，用於比較
     */
    private String normalizeCity(String cityName) {
        if (cityName == null) return "";
        // 將「臺」統一為「台」
        String normalized = cityName.replace("臺", "台").toLowerCase();
        // 移除「縣」「市」等後綴，以便更靈活匹配
        normalized = normalized.replace("縣", "").replace("市", "");
        return normalized;
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
     * 解析單個景點資料
     */
    private GovernmentSpotData parseSpotData(JsonParser jsonParser) throws IOException {
        return objectMapper.readValue(jsonParser, GovernmentSpotData.class);
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
        String cleanedName = originalName.replaceAll("[\\r\\n\\t]", "").trim();
        spot.setSpotName(cleanedName);
        
        // 地址處理
        String address = govData.getAddress();
        spot.setSpotLoc(address);
        
        // 更精確的地區判斷
        String region = determineRegion(address, govData.getRegion());
        spot.setRegion(region);
        
        // 描述
        String description = govData.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            // 清理描述中的HTML標籤和特殊字元
            description = description.replaceAll("<[^>]*>", "")
                                    .replaceAll("&nbsp;", " ")
                                    .replaceAll("\\s+", " ")
                                    .trim();
            spot.setSpotDesc(description);
        } else {
            spot.setSpotDesc("暫無描述");
        }
        
        // 座標
        try {
            // 檢查經緯度是否可用
            if (govData.hasValidCoordinates()) {
                spot.setSpotLat(govData.getLatitudeValue());
                spot.setSpotLng(govData.getLongitudeValue());
            }
        } catch (Exception e) {
            logger.debug("無法解析座標: {}", govData.getId());
        }
        
        // 圖片URL - 政府資料中暫無圖片欄位，跳過圖片處理
        // 如需圖片功能，請先在 GovernmentSpotData 中添加對應的 @JsonProperty 欄位
        
        // 電話
        spot.setTel(govData.getTel());
        
        // 開放時間
        spot.setOpeningTime(govData.getOpenTime());
        
        // 政府資料ID
        spot.setGovtId(govData.getId());
        
        // API 匯入的景點直接設為上架狀態
        spot.setSpotStatus((byte) 1);  // 1 = 上架
        
        // 建立者ID
        spot.setCrtId(crtId);
        
        // 建立時間
        spot.setSpotCreateAt(LocalDateTime.now());
        
        return spot;
    }
    
    /**
     * 更精確地判斷景點所屬地區
     * 
     * @param address 景點地址
     * @param originalRegion 原始地區資訊
     * @return 標準化的地區名稱
     */
    private String determineRegion(String address, String originalRegion) {
        if (address == null || address.trim().isEmpty()) {
            return originalRegion != null ? originalRegion : "";
        }
        
        // 縣市對照表（按照地址前綴匹配順序排序，較長的前綴優先匹配）
        String[][] regionPrefixes = {
            {"台北市", "臺北市", "臺北"},
            {"新北市", "新北"},
            {"桃園市", "桃園縣", "桃園"},
            {"台中市", "臺中市", "臺中"},
            {"台南市", "臺南市", "臺南"},
            {"高雄市", "高雄"},
            {"基隆市", "基隆"},
            {"新竹市"},
            {"新竹縣"},
            {"苗栗縣", "苗栗"},
            {"彰化縣", "彰化"},
            {"南投縣", "南投"},
            {"雲林縣", "雲林"},
            {"嘉義市"},
            {"嘉義縣"},
            {"屏東縣", "屏東"},
            {"宜蘭縣", "宜蘭"},
            {"花蓮縣", "花蓮"},
            {"台東縣", "臺東縣", "台東", "臺東"},
            {"澎湖縣", "澎湖"},
            {"金門縣", "金門"},
            {"連江縣", "馬祖", "連江"}
        };
        
        // 特殊地區關鍵字（用於更精確地識別特定縣市）
        Map<String, String[]> specialKeywords = new HashMap<>();
        specialKeywords.put("花蓮縣", new String[]{"花蓮", "秀林", "新城", "吉安", "壽豐", "鳳林", "光復", "豐濱", "瑞穗", "萬榮", "玉里", "卓溪", "富里"});
        specialKeywords.put("台東縣", new String[]{"台東", "臺東", "成功", "關山", "卑南", "鹿野", "池上", "東河", "長濱", "太麻里", "綠島", "蘭嶼", "延平", "海端", "達仁", "金峰", "大武"});
        
        // 首先嘗試從地址前綴判斷縣市
        String normalizedAddress = address.replace("臺", "台");
        for (String[] prefixes : regionPrefixes) {
            String region = prefixes[0];
            for (String prefix : prefixes) {
                if (normalizedAddress.startsWith(prefix)) {
                    // 對於花蓮和台東，進行額外檢查
                    if (region.equals("花蓮縣") || region.equals("台東縣")) {
                        // 檢查是否包含另一個縣市的特殊關鍵字
                        String oppositeRegion = region.equals("花蓮縣") ? "台東縣" : "花蓮縣";
                        String[] oppositeKeywords = specialKeywords.get(oppositeRegion);
                        
                        boolean containsOppositeKeyword = false;
                        for (String keyword : oppositeKeywords) {
                            if (normalizedAddress.contains(keyword)) {
                                containsOppositeKeyword = true;
                                break;
                            }
                        }
                        
                        if (containsOppositeKeyword) {
                            logger.info("地址「{}」包含{}的關鍵字，修正地區為{}", address, oppositeRegion, oppositeRegion);
                            return oppositeRegion;
                        }
                    }
                    return region;
                }
            }
        }
        
        // 如果前綴匹配失敗，嘗試在地址中查找縣市名稱
        for (String[] prefixes : regionPrefixes) {
            String region = prefixes[0];
            for (String prefix : prefixes) {
                if (normalizedAddress.contains(prefix)) {
                    // 同樣對花蓮和台東進行特殊處理
                    if (region.equals("花蓮縣") || region.equals("台東縣")) {
                        // 檢查是否更明確地包含該縣市的特殊關鍵字
                        String[] keywords = specialKeywords.get(region);
                        boolean containsSpecificKeyword = false;
                        for (String keyword : keywords) {
                            if (!keyword.equals("花蓮") && !keyword.equals("台東") && !keyword.equals("臺東") && normalizedAddress.contains(keyword)) {
                                containsSpecificKeyword = true;
                                break;
                            }
                        }
                        
                        if (containsSpecificKeyword) {
                            return region;
                        }
                    } else {
                        return region;
                    }
                }
            }
        }
        
        // 如果地址中沒有找到縣市名稱，則使用原始地區資訊
        return originalRegion != null ? originalRegion : "";
    }

    /**
     * 處理一批景點資料
     */
    private void processBatch(List<SpotVO> batch, ImportResult result) {
        try {
            for (SpotVO spot : batch) {
                try {
                    spotService.addSpot(spot);
                    result.incrementSuccessCount();
                } catch (Exception e) {
                    logger.error("儲存景點時發生錯誤: {}", spot.getSpotName(), e);
                    result.incrementErrorCount();
                }
            }
        } catch (Exception e) {
            logger.error("批次處理景點時發生錯誤", e);
            result.incrementErrorCount();
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

    /**
     * 修正已匯入景點的地區信息
     * 特別針對花蓮縣和台東縣的混淆問題
     * 
     * @return 修正的景點數量
     */
    @Transactional
    public int correctRegionInfo() {
        logger.info("開始修正已匯入景點的地區信息");
        int correctedCount = 0;
        
        try {
            // 獲取所有景點
            List<SpotVO> allSpots = spotService.getAllSpots();
            logger.info("共找到 {} 筆景點資料需要檢查", allSpots.size());
            
            for (SpotVO spot : allSpots) {
                String address = spot.getSpotLoc();
                String currentRegion = spot.getRegion();
                
                if (address != null && !address.trim().isEmpty()) {
                    // 使用更精確的地區判斷方法
                    String correctRegion = determineRegion(address, currentRegion);
                    
                    // 如果地區不同，則更新
                    if (!correctRegion.equals(currentRegion)) {
                        logger.info("修正景點 [{}] 的地區: {} -> {}, 地址: {}", 
                                   spot.getSpotName(), currentRegion, correctRegion, address);
                        
                        spot.setRegion(correctRegion);
                        spotService.updateSpot(spot);
                        correctedCount++;
                    }
                }
            }
            
            logger.info("地區信息修正完成，共修正 {} 筆景點資料", correctedCount);
            
        } catch (Exception e) {
            logger.error("修正地區信息時發生錯誤", e);
        }
        
        return correctedCount;
    }
} 