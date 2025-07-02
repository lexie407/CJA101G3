package com.toiukha.spot.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toiukha.spot.model.SpotFavoriteVO;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.repository.SpotFavoriteRepository;

/**
 * 景點收藏服務類
 * 處理景點收藏相關的業務邏輯
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Service
public class SpotFavoriteService {

    @Autowired
    private SpotFavoriteRepository spotFavoriteRepository;

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
    @Transactional
    public String toggleFavorite(Integer memId, Integer spotId) {
        boolean isFavorited = spotFavoriteRepository.existsByMemIdAndSpotId(memId, spotId);
        
        if (isFavorited) {
            removeFavorite(memId, spotId);
            return "已取消收藏";
        } else {
            addFavorite(memId, spotId);
            return "已加入收藏";
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
        return spotFavoriteRepository.findFavoriteSpotsByMemId(memId);
    }

    /**
     * 查詢景點的收藏數量
     * 
     * @param spotId 景點ID
     * @return 收藏數量
     */
    public Long getFavoriteCount(Integer spotId) {
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
} 