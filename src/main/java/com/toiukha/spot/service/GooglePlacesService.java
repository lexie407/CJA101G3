package com.toiukha.spot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Places API 服務
 * 用於取得景點的額外資訊，如照片、評分、評論等
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Service
public class GooglePlacesService {
    
    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);
    
    // 新版 Places API 端點
    private static final String PLACES_SEARCH_URL = "https://places.googleapis.com/v1/places:searchText";
    private static final String PLACE_DETAILS_URL = "https://places.googleapis.com/v1/places/";
    private static final String PLACE_PHOTO_URL = "https://places.googleapis.com/v1/places/";
    
    @Value("${google.api.key:}")
    private String apiKey;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public GooglePlacesService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 根據景點名稱和地址搜尋 Google Places
     * @param spotName 景點名稱
     * @param address 地址
     * @return Google Places 資訊
     */
    @Cacheable(value = "googlePlaces", key = "#spotName + '_' + #address")
    public GooglePlaceInfo searchPlace(String spotName, String address, Double lat, Double lng) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Google Places API Key 未設定");
            return null;
        }
        
        try {
            // 嘗試多種搜索策略
            GooglePlaceInfo result = null;
            
            // 策略1：使用景點名稱+地址
            logger.info("嘗試策略1：使用景點名稱+地址搜索");
            String query = buildSearchQuery(spotName, address);
            result = performSearch(query, lat, lng);
            
            // 策略2：如果策略1失敗，只使用景點名稱
            if (result == null && spotName != null && !spotName.trim().isEmpty()) {
                logger.info("嘗試策略2：僅使用景點名稱搜索");
                result = performSearch(spotName.trim(), lat, lng);
            }
            
            // 策略3：如果策略2失敗，使用景點名稱+縣市
            if (result == null && address != null && !address.trim().isEmpty()) {
                logger.info("嘗試策略3：使用景點名稱+縣市搜索");
                String cityQuery = extractCityQuery(spotName, address);
                if (!cityQuery.equals(query) && !cityQuery.equals(spotName.trim())) {
                    result = performSearch(cityQuery, lat, lng);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("搜尋 Google Places 發生錯誤：{}, 景點：{}", e.getMessage(), spotName);
            return null;
        }
    }
    
    /**
     * 執行 Google Places API 搜索
     * @param query 搜索查詢字串
     * @param lat 緯度（可選）
     * @param lng 經度（可選）
     * @return 搜索結果
     */
    private GooglePlaceInfo performSearch(String query, Double lat, Double lng) {
        try {
            logger.debug("執行搜索，查詢字串: '{}'", query);
            
            // 新版 Places API 使用 JSON 請求體
            String requestBody = String.format(
                "{\"textQuery\": \"%s\", \"languageCode\": \"zh-TW\", \"regionCode\": \"TW\"}",
                query.replace("\"", "\\\"")
            );
            
            // 如果有經緯度，添加到請求體
            if (lat != null && lng != null) {
                requestBody = requestBody.replace("}", 
                    String.format(", \"locationBias\": {\"circle\": {\"center\": {\"latitude\": %f, \"longitude\": %f}, \"radius\": 10000.0}}", lat, lng)
                + "}");
            }
            
            logger.debug("Places API 請求體: {}", requestBody);
            
            // 構建請求 URL 和頭部
            String url = PLACES_SEARCH_URL;
            
            String response = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.id,places.rating,places.userRatingCount")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            GooglePlaceInfo result = parseSearchResponseNew(response);
            
            if (result != null) {
                logger.info("搜索成功，查詢字串: '{}', 找到: {}", query, result.getName());
            } else {
                logger.warn("搜索無結果，查詢字串: '{}'", query);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("執行搜索時發生錯誤，查詢字串: '{}', 錯誤: {}", query, e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析新版 Places API 的搜索響應
     */
    private GooglePlaceInfo parseSearchResponseNew(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode places = root.path("places");
            
            if (places.isArray() && places.size() > 0) {
                JsonNode firstPlace = places.get(0);
                
                GooglePlaceInfo placeInfo = new GooglePlaceInfo();
                placeInfo.setPlaceId(firstPlace.path("id").asText());
                
                // 獲取顯示名稱
                JsonNode displayName = firstPlace.path("displayName");
                if (!displayName.isMissingNode()) {
                    placeInfo.setName(displayName.path("text").asText());
                }
                
                // 特別處理評分，確保即使為0也能正確處理
                JsonNode ratingNode = firstPlace.path("rating");
                if (!ratingNode.isMissingNode() && !ratingNode.isNull()) {
                    placeInfo.setRating(ratingNode.asDouble());
                    logger.debug("解析到評分：{}", placeInfo.getRating());
                } else {
                    placeInfo.setRating(null);
                    logger.debug("未解析到評分");
                }
                
                // 處理評分總數
                JsonNode ratingsNode = firstPlace.path("userRatingCount");
                if (!ratingsNode.isMissingNode() && !ratingsNode.isNull()) {
                    placeInfo.setUserRatingsTotal(ratingsNode.asInt());
                } else {
                    placeInfo.setUserRatingsTotal(0);
                }
                
                // 獲取格式化地址
                placeInfo.setFormattedAddress(firstPlace.path("formattedAddress").asText());
                
                logger.info("成功解析Google Places搜索結果：{}", placeInfo.getName());
                return placeInfo;
            } else {
                logger.warn("Google Places API 返回空結果數組");
            }
            
        } catch (Exception e) {
            logger.error("解析 Google Places 搜尋回應發生錯誤", e);
        }
        
        return null;
    }

    /**
     * 取得 Place 詳細資訊
     * @param placeId Google Place ID
     * @return 詳細資訊
     */
    @Cacheable(value = "googlePlaceDetails", key = "#placeId")
    public GooglePlaceDetails getPlaceDetails(String placeId) {
        if (apiKey == null || apiKey.trim().isEmpty() || placeId == null) {
            return null;
        }
        
        try {
            // 新版 Places API 使用不同的端點和請求方式
            String url = PLACE_DETAILS_URL + placeId;
            
            logger.info("請求Google Place詳情，Place ID: {}, URL: {}", placeId, url);
            
            String response = webClient.get()
                .uri(url)
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "photos,rating,userRatingCount,websiteUri,nationalPhoneNumber,internationalPhoneNumber,regularOpeningHours")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            logger.debug("Google Place詳情響應: {}", response);
            
            return parseDetailsResponseNew(response, placeId);
            
        } catch (Exception e) {
            logger.error("取得 Place 詳情發生錯誤：{}, PlaceID：{}", e.getMessage(), placeId);
            return null;
        }
    }

    /**
     * 取得照片 URL
     * @param photoReference 照片參考
     * @param maxWidth 最大寬度
     * @return 照片 URL
     */
    public String getPhotoUrl(String photoReference, int maxWidth) {
        if (apiKey == null || apiKey.trim().isEmpty() || photoReference == null) {
            return null;
        }
        
        // 新版 Places API 的照片 URL 格式
        return photoReference + "/media?key=" + apiKey + "&maxWidth=" + maxWidth;
    }

    /**
     * 建構搜尋查詢字串
     */
    private String buildSearchQuery(String spotName, String address) {
        if (spotName == null || spotName.trim().isEmpty()) {
            return address != null ? address.trim() : "";
        }
        
        if (address == null || address.trim().isEmpty()) {
            return spotName.trim();
        }
        
        // 組合景點名稱和地址，但避免重複
        String name = spotName.trim();
        String addr = address.trim();
        
        // 如果地址已經包含景點名稱，則直接使用地址
        if (addr.contains(name)) {
            return addr;
        }
        
        // 否則組合名稱和地址
        return name + " " + addr;
    }
    
    /**
     * 從地址中提取縣市，並與景點名稱組合
     */
    private String extractCityQuery(String spotName, String address) {
        if (spotName == null || address == null) {
            return spotName != null ? spotName.trim() : "";
        }
        
        String name = spotName.trim();
        String addr = address.trim();
        
        // 嘗試提取縣市
        String city = "";
        String[] cityPatterns = {"台北市", "臺北市", "新北市", "桃園市", "台中市", "臺中市", 
                                "台南市", "臺南市", "高雄市", "基隆市", "新竹市", "嘉義市", 
                                "新竹縣", "苗栗縣", "彰化縣", "南投縣", "雲林縣", "嘉義縣", 
                                "屏東縣", "宜蘭縣", "花蓮縣", "台東縣", "臺東縣", "澎湖縣", 
                                "金門縣", "連江縣"};
        
        for (String pattern : cityPatterns) {
            if (addr.contains(pattern)) {
                city = pattern;
                break;
            }
        }
        
        if (city.isEmpty()) {
            return name;
        }
        
        // 如果名稱中已包含縣市，則直接使用名稱
        if (name.contains(city)) {
            return name;
        }
        
        // 組合名稱和縣市
        return name + " " + city;
    }
    
    /**
     * 解析新版 Places API 的詳細資訊響應
     */
    private GooglePlaceDetails parseDetailsResponseNew(String response, String placeId) {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            logger.debug("解析Google Place詳情，Place ID: {}", placeId);
            logger.debug("響應根節點: {}", root.toString());
            
            GooglePlaceDetails details = new GooglePlaceDetails();
            
            // 照片
            JsonNode photos = root.path("photos");
            if (photos.isArray()) {
                List<String> photoReferences = new ArrayList<>();
                for (JsonNode photo : photos) {
                    String photoName = photo.path("name").asText();
                    if (!photoName.isEmpty()) {
                        // 新版 API 中，照片引用是一個完整的路徑
                        photoReferences.add(photoName);
                    }
                }
                details.setPhotoReferences(photoReferences);
                logger.debug("解析到 {} 張照片", photoReferences.size());
            } else {
                logger.debug("未找到照片節點或非數組");
            }
            
            // 營業時間
            JsonNode openingHours = root.path("regularOpeningHours");
            if (!openingHours.isMissingNode()) {
                JsonNode periods = openingHours.path("periods");
                if (periods.isArray()) {
                    List<String> hours = new ArrayList<>();
                    for (int i = 0; i < periods.size(); i++) {
                        JsonNode period = periods.get(i);
                        JsonNode open = period.path("open");
                        JsonNode close = period.path("close");
                        
                        if (!open.isMissingNode() && !close.isMissingNode()) {
                            String day = getDayOfWeek(open.path("day").asInt());
                            String openTime = open.path("hour").asText() + ":" + open.path("minute").asText();
                            String closeTime = close.path("hour").asText() + ":" + close.path("minute").asText();
                            hours.add(day + " " + openTime + " - " + closeTime);
                        }
                    }
                    details.setOpeningHours(hours);
                    logger.debug("解析到 {} 條營業時間信息", hours.size());
                } else {
                    logger.debug("未找到營業時間periods節點或非數組");
                }
            } else {
                logger.debug("未找到營業時間節點");
            }
            
            // 網站 - 嘗試多個可能的字段名
            String website = "";
            if (!root.path("websiteUri").isMissingNode()) {
                website = root.path("websiteUri").asText("");
                logger.debug("從websiteUri字段獲取網站: {}", website);
            } else if (!root.path("website").isMissingNode()) {
                website = root.path("website").asText("");
                logger.debug("從website字段獲取網站: {}", website);
            } else {
                logger.debug("未找到網站相關字段");
            }
            details.setWebsite(website);
            
            // 電話 - 嘗試多個可能的字段名
            String phoneNumber = "";
            if (!root.path("nationalPhoneNumber").isMissingNode()) {
                phoneNumber = root.path("nationalPhoneNumber").asText("");
                logger.debug("從nationalPhoneNumber字段獲取電話: {}", phoneNumber);
            } else if (!root.path("internationalPhoneNumber").isMissingNode()) {
                phoneNumber = root.path("internationalPhoneNumber").asText("");
                logger.debug("從internationalPhoneNumber字段獲取電話: {}", phoneNumber);
            } else if (!root.path("formattedPhoneNumber").isMissingNode()) {
                phoneNumber = root.path("formattedPhoneNumber").asText("");
                logger.debug("從formattedPhoneNumber字段獲取電話: {}", phoneNumber);
            } else if (!root.path("phoneNumber").isMissingNode()) {
                phoneNumber = root.path("phoneNumber").asText("");
                logger.debug("從phoneNumber字段獲取電話: {}", phoneNumber);
            } else {
                logger.debug("未找到電話相關字段");
            }
            details.setPhoneNumber(phoneNumber);
            
            logger.info("成功解析Google Place詳情，Place ID: {}, 網站: {}, 電話: {}", 
                       placeId, 
                       website.isEmpty() ? "無" : website, 
                       phoneNumber.isEmpty() ? "無" : phoneNumber);
            
            return details;
            
        } catch (Exception e) {
            logger.error("解析 Google Places 詳情回應發生錯誤: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 將數字日期轉換為文字
     */
    private String getDayOfWeek(int day) {
        switch (day) {
            case 0: return "星期日";
            case 1: return "星期一";
            case 2: return "星期二";
            case 3: return "星期三";
            case 4: return "星期四";
            case 5: return "星期五";
            case 6: return "星期六";
            default: return "未知";
        }
    }

    /**
     * 檢查 API Key 是否有效
     * @return 如果 API Key 有效則返回 true，否則返回 false
     */
    public boolean isApiKeyValid() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Google Places API Key 未設定");
            return false;
        }
        
        try {
            // 執行一個簡單的測試查詢來驗證 API Key
            String requestBody = "{\"textQuery\": \"台北101\", \"languageCode\": \"zh-TW\", \"regionCode\": \"TW\"}";
            
            String response = webClient.post()
                .uri(PLACES_SEARCH_URL)
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.displayName")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode places = root.path("places");
            
            if (places.isMissingNode() || !places.isArray()) {
                String errorMessage = root.path("error").path("message").asText("未知錯誤");
                logger.error("API Key 無效: {}", errorMessage);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("驗證 API Key 時發生錯誤: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Google Place 基本資訊 DTO
     */
    public static class GooglePlaceInfo {
        private String placeId;
        private String name;
        private Double rating;
        private Integer userRatingsTotal;
        private String formattedAddress;

        // Getter 和 Setter
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public Integer getUserRatingsTotal() { return userRatingsTotal; }
        public void setUserRatingsTotal(Integer userRatingsTotal) { this.userRatingsTotal = userRatingsTotal; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
    }

    /**
     * Google Place 詳細資訊 DTO
     */
    public static class GooglePlaceDetails {
        private List<String> photoReferences;
        private List<String> openingHours;
        private List<GoogleReview> reviews;
        private String website;
        private String phoneNumber;

        // Getter 和 Setter
        public List<String> getPhotoReferences() { return photoReferences; }
        public void setPhotoReferences(List<String> photoReferences) { this.photoReferences = photoReferences; }
        
        public List<String> getOpeningHours() { return openingHours; }
        public void setOpeningHours(List<String> openingHours) { this.openingHours = openingHours; }
        
        public List<GoogleReview> getReviews() { return reviews; }
        public void setReviews(List<GoogleReview> reviews) { this.reviews = reviews; }
        
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    /**
     * Google 評論 DTO
     */
    public static class GoogleReview {
        private String authorName;
        private Integer rating;
        private String text;
        private Long time;

        // Getter 和 Setter
        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public Long getTime() { return time; }
        public void setTime(Long time) { this.time = time; }
    }
} 