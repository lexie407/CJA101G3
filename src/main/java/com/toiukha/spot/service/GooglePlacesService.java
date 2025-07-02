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
    
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private static final String PLACE_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    private static final String PLACE_PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo";
    
    @Value("${google.places.api.key:}")
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
            // 建構搜尋查詢字串
            String query = buildSearchQuery(spotName, address);
            
            String url = UriComponentsBuilder.fromUriString(PLACES_SEARCH_URL)
                .queryParam("query", query)
                .queryParam("key", apiKey)
                .queryParam("language", "zh-TW")
                .queryParam("region", "tw")
                .build()
                .toUriString();
            
            // 如果有經緯度，加入位置偏好
            if (lat != null && lng != null) {
                url += "&location=" + lat + "," + lng + "&radius=1000";
            }
            
            String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            return parseSearchResponse(response);
            
        } catch (Exception e) {
            logger.error("搜尋 Google Places 發生錯誤：{}, 景點：{}", e.getMessage(), spotName);
            return null;
        }
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
            String url = UriComponentsBuilder.fromUriString(PLACE_DETAILS_URL)
                .queryParam("place_id", placeId)
                .queryParam("key", apiKey)
                .queryParam("language", "zh-TW")
                .queryParam("fields", "photos,rating,user_ratings_total,reviews,opening_hours,website,formatted_phone_number")
                .build()
                .toUriString();
            
            String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            return parseDetailsResponse(response);
            
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
        
        return UriComponentsBuilder.fromUriString(PLACE_PHOTO_URL)
            .queryParam("photoreference", photoReference)
            .queryParam("maxwidth", maxWidth)
            .queryParam("key", apiKey)
            .build()
            .toUriString();
    }

    /**
     * 建構搜尋查詢字串
     */
    private String buildSearchQuery(String spotName, String address) {
        StringBuilder query = new StringBuilder();
        
        if (spotName != null && !spotName.trim().isEmpty()) {
            query.append(spotName.trim());
        }
        
        if (address != null && !address.trim().isEmpty()) {
            if (query.length() > 0) {
                query.append(" ");
            }
            // 只加入縣市和鄉鎮區的部分
            String[] addressParts = address.split("[縣市]");
            if (addressParts.length > 0) {
                String cityPart = addressParts[0];
                if (cityPart.length() > 2) {
                    query.append(cityPart).append("市");
                }
            }
        }
        
        return query.toString();
    }

    /**
     * 解析搜尋回應
     */
    private GooglePlaceInfo parseSearchResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String status = root.path("status").asText();
            
            if (!"OK".equals(status)) {
                logger.debug("Google Places API 搜尋狀態：{}", status);
                return null;
            }
            
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                
                GooglePlaceInfo placeInfo = new GooglePlaceInfo();
                placeInfo.setPlaceId(firstResult.path("place_id").asText());
                placeInfo.setName(firstResult.path("name").asText());
                placeInfo.setRating(firstResult.path("rating").asDouble(0.0));
                placeInfo.setUserRatingsTotal(firstResult.path("user_ratings_total").asInt(0));
                placeInfo.setFormattedAddress(firstResult.path("formatted_address").asText());
                
                return placeInfo;
            }
            
        } catch (Exception e) {
            logger.error("解析 Google Places 搜尋回應發生錯誤", e);
        }
        
        return null;
    }

    /**
     * 解析詳細資訊回應
     */
    private GooglePlaceDetails parseDetailsResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String status = root.path("status").asText();
            
            if (!"OK".equals(status)) {
                logger.debug("Google Places API 詳情狀態：{}", status);
                return null;
            }
            
            JsonNode result = root.path("result");
            if (!result.isMissingNode()) {
                GooglePlaceDetails details = new GooglePlaceDetails();
                
                // 照片
                JsonNode photos = result.path("photos");
                if (photos.isArray()) {
                    List<String> photoReferences = new ArrayList<>();
                    for (JsonNode photo : photos) {
                        String photoRef = photo.path("photo_reference").asText();
                        if (!photoRef.isEmpty()) {
                            photoReferences.add(photoRef);
                        }
                    }
                    details.setPhotoReferences(photoReferences);
                }
                
                // 營業時間
                JsonNode openingHours = result.path("opening_hours");
                if (!openingHours.isMissingNode()) {
                    JsonNode weekdayText = openingHours.path("weekday_text");
                    if (weekdayText.isArray()) {
                        List<String> hours = new ArrayList<>();
                        for (JsonNode hour : weekdayText) {
                            hours.add(hour.asText());
                        }
                        details.setOpeningHours(hours);
                    }
                }
                
                // 評論
                JsonNode reviews = result.path("reviews");
                if (reviews.isArray()) {
                    List<GoogleReview> reviewList = new ArrayList<>();
                    for (JsonNode review : reviews) {
                        GoogleReview googleReview = new GoogleReview();
                        googleReview.setAuthorName(review.path("author_name").asText());
                        googleReview.setRating(review.path("rating").asInt());
                        googleReview.setText(review.path("text").asText());
                        googleReview.setTime(review.path("time").asLong());
                        reviewList.add(googleReview);
                    }
                    details.setReviews(reviewList);
                }
                
                details.setWebsite(result.path("website").asText());
                details.setPhoneNumber(result.path("formatted_phone_number").asText());
                
                return details;
            }
            
        } catch (Exception e) {
            logger.error("解析 Google Places 詳情回應發生錯誤", e);
        }
        
        return null;
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