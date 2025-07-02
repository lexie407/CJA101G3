package com.toiukha.spot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 快取配置
 * 配置 Google Places API 回應的快取機制
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Caffeine 快取管理器
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 預設快取配置
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofHours(24))
            .recordStats());
        
        // 預先建立快取名稱
        cacheManager.setCacheNames(java.util.Arrays.asList("googlePlaces", "googlePlaceDetails"));
        
        return cacheManager;
    }

    /**
     * Google Places 搜尋結果快取
     * @return Caffeine 配置
     */
    @Bean("googlePlacesCache")
    public Caffeine<Object, Object> googlePlacesCache() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofDays(7)) // 7天過期
            .recordStats();
    }

    /**
     * Google Places 詳細資訊快取
     * @return Caffeine 配置
     */
    @Bean("googlePlaceDetailsCache")
    public Caffeine<Object, Object> googlePlaceDetailsCache() {
        return Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(Duration.ofDays(3)) // 3天過期
            .recordStats();
    }
} 