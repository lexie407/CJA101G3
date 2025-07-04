package com.toiukha.groupactivity.service;

import com.toiukha.groupactivity.model.ActTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活動標籤服務
 * 負責處理活動標籤的Redis存儲和查詢
 */
@Service
public class ActTagService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // 精簡Redis Key設計
    private static final String ACT_TAGS = "groupactivity:act:tags:"; // groupactivity:act:tags:{actId} -> Set<tag>
    private static final String TAG_ACTS = "groupactivity:tag:acts:"; // groupactivity:tag:acts:{tag} -> Set<actId>
    
    /**
     * 儲存活動標籤（類型+縣市）
     */
    public void saveActTags(Integer actId, ActTag typeTag, ActTag cityTag) {
        String key = ACT_TAGS + actId;
        
        // 清除舊標籤
        Set<String> oldTags = redisTemplate.opsForSet().members(key);
        if (oldTags != null) {
            for (String oldTag : oldTags) {
                redisTemplate.opsForSet().remove(TAG_ACTS + oldTag, actId.toString());
            }
        }
        redisTemplate.delete(key);
        
        // 儲存新標籤
        Set<ActTag> newTags = Set.of(typeTag, cityTag);
        for (ActTag tag : newTags) {
            redisTemplate.opsForSet().add(key, tag.name());
            redisTemplate.opsForSet().add(TAG_ACTS + tag.name(), actId.toString());
        }
    }
    
    /**
     * 獲取活動標籤
     */
    public Map<String, ActTag> getActTags(Integer actId) {
        String key = ACT_TAGS + actId;
        Set<String> tagNames = redisTemplate.opsForSet().members(key);
        
        Map<String, ActTag> result = new HashMap<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                try {
                    ActTag tag = ActTag.valueOf(tagName);
                    result.put(tag.getCategory(), tag);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // 設定預設值
        result.putIfAbsent("type", ActTag.OUTDOOR);
        result.putIfAbsent("city", ActTag.TAIPEI);
        
        return result;
    }
    
    /**
     * 按標籤獲取活動ID
     */
    public Set<Integer> getActsByTag(ActTag tag) {
        Set<String> actIds = redisTemplate.opsForSet().members(TAG_ACTS + tag.name());
        return actIds != null ? 
            actIds.stream().map(Integer::parseInt).collect(Collectors.toSet()) : 
            Set.of();
    }
    
    /**
     * 按多個標籤獲取活動ID（交集）
     */
    public Set<Integer> getActsByTags(Set<ActTag> tags) {
        if (tags.isEmpty()) return Set.of();
        
        Set<Integer> result = null;
        for (ActTag tag : tags) {
            Set<Integer> tagActIds = getActsByTag(tag);
            if (result == null) {
                result = new HashSet<>(tagActIds);
            } else {
                result.retainAll(tagActIds); // 求交集
            }
        }
        
        return result != null ? result : Set.of();
    }
    
    /**
     * 刪除活動標籤
     */
    public void removeActTags(Integer actId) {
        String key = ACT_TAGS + actId;
        Set<String> oldTags = redisTemplate.opsForSet().members(key);
        
        if (oldTags != null) {
            // 移除反向索引
            for (String oldTag : oldTags) {
                redisTemplate.opsForSet().remove(TAG_ACTS + oldTag, actId.toString());
            }
            // 移除標籤記錄
            redisTemplate.delete(key);
        }
    }
    
    /**
     * 獲取各標籤的活動數量統計
     */
    public Map<ActTag, Long> getTagStatistics() {
        Map<ActTag, Long> stats = new HashMap<>();
        
        for (ActTag tag : ActTag.values()) {
            String tagActsKey = TAG_ACTS + tag.name();
            Long count = redisTemplate.opsForSet().size(tagActsKey);
            stats.put(tag, count != null ? count : 0L);
        }
        
        return stats;
    }
} 