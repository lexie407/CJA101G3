package com.toiukha.spot.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import com.toiukha.spot.model.SpotFavoriteVO;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.repository.SpotFavoriteRepository;
import com.toiukha.spot.service.SpotService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 景點收藏服務類
 * 處理景點收藏相關的業務邏輯
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Service
public class SpotFavoriteService {

    private static final Logger log = LoggerFactory.getLogger(SpotFavoriteService.class);

    @Autowired
    private SpotFavoriteRepository spotFavoriteRepository;
    
    @Autowired
    private SpotService spotService;

    @PersistenceContext
    private EntityManager entityManager;

    // ========== 收藏操作 ==========

    /**
     * 新增收藏
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @return 收藏記錄
     * @throws RuntimeException 如果已經收藏過
     */
    @Transactional
    public SpotFavoriteVO addFavorite(Integer memId, Integer spotId) {
        // 檢查是否已收藏
        if (spotFavoriteRepository.existsByMemIdAndSpotId(memId, spotId)) {
            throw new RuntimeException("您已收藏過此景點");
        }

        SpotFavoriteVO favorite = new SpotFavoriteVO(spotId, memId);  // 注意參數順序：spotId, memId
        return spotFavoriteRepository.save(favorite);
    }

    /**
     * 取消收藏
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @throws RuntimeException 如果尚未收藏
     */
    @Transactional
    public void removeFavorite(Integer memId, Integer spotId) {
        if (!spotFavoriteRepository.existsByMemIdAndSpotId(memId, spotId)) {
            throw new RuntimeException("您尚未收藏此景點");
        }

        spotFavoriteRepository.deleteByMemIdAndSpotId(memId, spotId);
    }

    /**
     * 切換收藏狀態（收藏/取消收藏）
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @return 操作結果訊息
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public synchronized String toggleFavorite(Integer memId, Integer spotId) {
        try {
            // 先檢查參數
            if (memId == null || spotId == null) {
                throw new IllegalArgumentException("會員ID和景點ID不能為空");
            }
            
            // 使用原生SQL查詢當前狀態
            int count = spotFavoriteRepository.countBySpotIdAndMemId(spotId, memId);
            
            if (count > 0) {
                // 已收藏，刪除收藏
                int deleted = spotFavoriteRepository.deleteBySpotIdAndMemIdNative(spotId, memId);
                if (deleted <= 0) {
                    throw new RuntimeException("取消收藏失敗，請重試");
                }
                return "已取消收藏";
            } else {
                // 未收藏，插入收藏（使用 INSERT IGNORE 避免重複主鍵錯誤）
                int inserted = spotFavoriteRepository.insertIgnore(spotId, memId);
                if (inserted <= 0) {
                    // 嘗試檢查是否已經存在（可能是並發操作）
                    if (spotFavoriteRepository.countBySpotIdAndMemId(spotId, memId) > 0) {
                        return "已加入收藏";
                    } else {
                        throw new RuntimeException("加入收藏失敗，請重試");
                    }
                }
                return "已加入收藏";
            }
        } catch (DataIntegrityViolationException e) {
            // 處理資料完整性違規（如主鍵衝突）
            if (e.getMessage().contains("Duplicate entry")) {
                // 如果是重複主鍵，可能是並發操作，檢查當前狀態
                int currentCount = spotFavoriteRepository.countBySpotIdAndMemId(spotId, memId);
                if (currentCount > 0) {
                    return "已加入收藏";
                } else {
                    // 嘗試再次插入
                    try {
                        spotFavoriteRepository.insertIgnore(spotId, memId);
                        return "已加入收藏";
                    } catch (Exception innerEx) {
                        throw new RuntimeException("處理收藏狀態時發生錯誤: " + innerEx.getMessage(), innerEx);
                    }
                }
            } else {
                throw new RuntimeException("處理收藏狀態時發生錯誤: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("處理收藏狀態時發生錯誤: " + e.getMessage(), e);
        }
    }

    // ========== 查詢操作 ==========

    /**
     * 檢查會員是否已收藏指定景點
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Integer memId, Integer spotId) {
        if (memId == null || spotId == null) {
            return false;
        }
        return spotFavoriteRepository.existsByMemIdAndSpotId(memId, spotId);
    }

    /**
     * 查詢會員收藏的景點列表
     * 
     * @param memId 會員ID
     * @return 收藏的景點列表
     */
    public List<SpotVO> getFavoriteSpots(Integer memId) {
        List<SpotFavoriteVO> favorites = spotFavoriteRepository.findFavoriteSpotsByMemId(memId);
        return favorites.stream()
                .map(SpotFavoriteVO::getSpot)
                .collect(Collectors.toList());
    }

    /**
     * 查詢會員收藏記錄列表（含收藏時間）
     * 
     * @param memId 會員ID
     * @return 收藏記錄列表
     */
    public List<SpotFavoriteVO> getFavoriteRecords(Integer memId) {
        if (memId == null) {
            return new ArrayList<>();
        }
        
        try {
            return spotFavoriteRepository.findFavoriteSpotsByMemId(memId);
        } catch (Exception e) {
            log.error("獲取會員收藏記錄時發生錯誤: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 查詢會員收藏景點總數
     * 
     * @param memId 會員ID
     * @return 收藏總數
     */
    public Long getFavoriteCount(Integer memId) {
        if (memId == null) {
            return 0L;
        }
        return spotFavoriteRepository.countByMemId(memId);
    }
    
    /**
     * 查詢景點的收藏數量
     * 
     * @param spotId 景點ID
     * @return 收藏數量
     */
    public Long getSpotFavoriteCount(Integer spotId) {
        return spotFavoriteRepository.countBySpotId(spotId);
    }

    /**
     * 批量查詢景點的收藏狀態
     * 
     * @param memId 會員ID
     * @param spotIds 景點ID列表
     * @return 景點ID對應的收藏狀態Map
     */
    public Map<Integer, Boolean> getFavoriteStatusMap(Integer memId, List<Integer> spotIds) {
        if (memId == null || spotIds == null || spotIds.isEmpty()) {
            return Map.of();
        }

        Set<Integer> favoritedSpotIds = spotFavoriteRepository.findSpotIdsByMemId(memId)
                .stream().collect(Collectors.toSet());

        return spotIds.stream()
                .collect(Collectors.toMap(
                    spotId -> spotId,
                    favoritedSpotIds::contains
                ));
    }

    /**
     * 批量檢查收藏狀態
     * 
     * @param memId 會員ID
     * @param spotIds 景點ID列表
     * @return 景點ID到收藏狀態的映射
     */
    public Map<Integer, Boolean> batchCheckFavoriteStatus(Integer memId, List<Integer> spotIds) {
        if (memId == null || spotIds == null || spotIds.isEmpty()) {
            return Map.of();
        }

        Set<Integer> favoritedSpotIds = spotFavoriteRepository.findSpotIdsByMemId(memId)
                .stream().collect(Collectors.toSet());

        return spotIds.stream()
                .collect(Collectors.toMap(
                    spotId -> spotId,
                    favoritedSpotIds::contains
                ));
    }

    /**
     * 查詢會員收藏的景點ID列表
     * 
     * @param memId 會員ID
     * @return 景點ID列表
     */
    public List<Integer> getFavoriteSpotIds(Integer memId) {
        return spotFavoriteRepository.findSpotIdsByMemId(memId);
    }

    // ========== 統計操作 ==========

    /**
     * 查詢會員收藏景點總數
     * 
     * @param memId 會員ID
     * @return 收藏總數
     */
    public Long getMemberFavoriteCount(Integer memId) {
        return spotFavoriteRepository.countByMemId(memId);
    }

    /**
     * 查詢景點收藏者列表
     * 
     * @param spotId 景點ID
     * @return 收藏者ID列表
     */
    public List<Integer> getSpotFavoriterIds(Integer spotId) {
        return spotFavoriteRepository.findMemIdsBySpotId(spotId);
    }

    /**
     * 批量查詢多個景點的收藏數量
     * 
     * @param spotIds 景點ID列表
     * @return 景點ID對應的收藏數量Map
     */
    public Map<Integer, Long> getFavoriteCountMap(List<Integer> spotIds) {
        return spotIds.stream()
                .collect(Collectors.toMap(
                    spotId -> spotId,
                    this::getFavoriteCount
                ));
    }
    
    /**
     * 檢查景點是否存在
     * 
     * @param spotId 景點ID
     * @return 景點是否存在
     */
    public boolean checkSpotExists(Integer spotId) {
        if (spotId == null) {
            return false;
        }
        
        try {
            // 透過 SpotRepository 檢查景點是否存在
            // 這裡假設 SpotService 已經被注入
            return spotService.existsById(spotId);
        } catch (Exception e) {
            // 如果出錯，保守地返回 true，避免阻止收藏操作
            return true;
        }
    }
} 