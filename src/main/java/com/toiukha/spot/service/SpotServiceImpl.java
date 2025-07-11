package com.toiukha.spot.service;

import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class SpotServiceImpl implements SpotService {

    private static final Logger logger = LoggerFactory.getLogger(SpotServiceImpl.class);

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private GooglePlacesService googlePlacesService;

    @Override
    public SpotVO findById(Integer id) {
        return spotRepository.findById(id).orElse(null);
    }

    @Override
    public SpotVO getSpotById(Integer id) {
        return spotRepository.findById(id).orElse(null);
    }

    @Override
    public List<SpotVO> getAllSpots() {
        return spotRepository.findAll();
    }

    @Override
    public SpotVO save(SpotVO spot) {
        return spotRepository.save(spot);
    }

    @Override
    public void deleteById(Integer id) {
        spotRepository.deleteById(id);
    }

    @Override
    public List<SpotVO> searchSpots(String keyword) {
        return spotRepository.findBySpotNameContaining(keyword);
    }

    @Override
    public List<SpotVO> searchPublicSpots(String keyword) {
        List<SpotVO> spots;
        if (keyword == null || keyword.trim().isEmpty()) {
            spots = spotRepository.findBySpotStatus((byte) 1);
        } else {
        String trimmedKeyword = keyword.trim();
        List<SpotVO> nameResults = spotRepository.findBySpotNameContainingAndSpotStatus(trimmedKeyword, (byte) 1);
        if (nameResults.size() >= 3) {
                spots = nameResults;
            } else {
        List<SpotVO> addressResults = spotRepository.findBySpotLocContainingAndSpotStatus(trimmedKeyword, (byte) 1);
        List<SpotVO> combinedResults = new java.util.ArrayList<>(nameResults);
        for (SpotVO addressResult : addressResults) {
            boolean alreadyExists = combinedResults.stream()
                    .anyMatch(spot -> spot.getSpotId().equals(addressResult.getSpotId()));
            if (!alreadyExists) {
                combinedResults.add(addressResult);
            }
        }
                spots = combinedResults;
            }
        }
        return spots;
    }

    @Override
    public List<SpotVO> getAllPublicSpots() {
        List<SpotVO> spots = spotRepository.findBySpotStatus((byte) 1);
        return spots;
    }

    @Override
    public List<SpotVO> getSpotsByStatus(byte spotStatus) {
        return spotRepository.findBySpotStatus(spotStatus);
    }

    @Override
    public List<SpotVO> getSpotsByRegion(String region) {
        List<SpotVO> activeSpots = getActiveSpots();
        if (region == null || region.trim().isEmpty() || region.equals("所有地區")) {
            return activeSpots;
        }
        List<SpotVO> filtered = activeSpots.stream()
                .filter(spot -> {
                    String spotRegion = getSpotRegion(spot.getSpotLoc());
                    return region.equals(spotRegion);
                })
                .collect(Collectors.toList());
        return filtered;
    }
    
    /**
     * 根據地址判斷景點所在地區
     * @param address 地址
     * @return 地區名稱（北部、中部、南部、東部、離島）
     */
    private String getSpotRegion(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        
        // 北部縣市
        if (address.contains("台北") || address.contains("臺北") ||
            address.contains("新北") || address.contains("基隆") ||
            address.contains("桃園") || address.contains("新竹") ||
            address.contains("宜蘭")) {
            return "北部";
        }
        
        // 中部縣市
        if (address.contains("台中") || address.contains("臺中") ||
            address.contains("苗栗") || address.contains("彰化") ||
            address.contains("南投") || address.contains("雲林")) {
            return "中部";
        }
        
        // 南部縣市
        if (address.contains("高雄") || address.contains("台南") ||
            address.contains("臺南") || address.contains("嘉義") ||
            address.contains("屏東")) {
            return "南部";
        }
        
        // 東部縣市
        if (address.contains("花蓮") || address.contains("台東") ||
            address.contains("臺東")) {
            return "東部";
        }

        // 離島地區
        if (address.contains("金門") || address.contains("澎湖") ||
            address.contains("馬祖") || address.contains("連江") ||
            address.contains("綠島") || address.contains("蘭嶼")) {
            return "離島";
        }
        
        return "";
    }

    @Override
    public boolean activateSpot(Integer id) {
        SpotVO spot = findById(id);
        if (spot != null) {
            spot.setSpotStatus((byte) 1);
            spotRepository.save(spot);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateSpot(Integer id) {
        SpotVO spot = findById(id);
        if (spot != null) {
            spot.setSpotStatus((byte) 0);
            spotRepository.save(spot);
            return true;
        }
        return false;
    }

    @Override
    public boolean rejectSpot(Integer id, String reason, String remark) {
        SpotVO spot = findById(id);
        if (spot != null) {
            spot.setSpotStatus((byte) 2);
            spot.setRejectReason(reason);
            spot.setRejectRemark(remark);
            spotRepository.save(spot);
            return true;
        }
        return false;
    }

    @Override
    public int batchUpdateStatus(List<Integer> spotIds, byte spotStatus) {
        int count = 0;
        for (Integer id : spotIds) {
            SpotVO spot = findById(id);
            if (spot != null) {
                spot.setSpotStatus(spotStatus);
                spotRepository.save(spot);
                count++;
            }
        }
        return count;
    }

    @Override
    public int batchActivateSpots(List<Integer> spotIds) {
        return batchUpdateStatus(spotIds, (byte) 1);
    }

    @Override
    public int batchDeactivateSpots(List<Integer> spotIds) {
        return batchUpdateStatus(spotIds, (byte) 0);
    }

    @Override
    public Page<SpotVO> searchReviewedSpotsForAdmin(String keyword, Integer spotStatus, String region, Pageable pageable) {
        // 只針對有 keyword 的情況做名稱/地址分開搜尋
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            // 如果指定了具體狀態，使用該狀態；如果是null（全部狀態），使用特殊處理
            if (spotStatus != null) {
                // 只搜尋名稱
                List<SpotVO> nameResults = spotRepository.findBySpotNameContainingAndSpotStatus(trimmedKeyword, spotStatus.byteValue());
                if (nameResults.size() >= 3) {
                    // 修復：正確處理分頁邏輯
                    return createPageFromList(nameResults, pageable);
                }
                // 名稱少於3個再搜尋地址
                List<SpotVO> addressResults = spotRepository.findBySpotLocContainingAndSpotStatus(trimmedKeyword, spotStatus.byteValue());
                // 合併去重
                List<SpotVO> combinedResults = new java.util.ArrayList<>(nameResults);
                for (SpotVO addressResult : addressResults) {
                    boolean alreadyExists = combinedResults.stream().anyMatch(spot -> spot.getSpotId().equals(addressResult.getSpotId()));
                    if (!alreadyExists) {
                        combinedResults.add(addressResult);
                    }
                }
                // 修復：正確處理分頁邏輯
                return createPageFromList(combinedResults, pageable);
            } else {
                // 全部狀態：搜尋所有已審核景點（排除待審核的0狀態）
                List<SpotVO> nameResults = spotRepository.findBySpotNameContainingAndSpotStatusNot(trimmedKeyword, (byte) 0);
                if (nameResults.size() >= 3) {
                    return createPageFromList(nameResults, pageable);
                }
                // 名稱少於3個再搜尋地址
                List<SpotVO> addressResults = spotRepository.findBySpotLocContainingAndSpotStatusNot(trimmedKeyword, (byte) 0);
                // 合併去重
                List<SpotVO> combinedResults = new java.util.ArrayList<>(nameResults);
                for (SpotVO addressResult : addressResults) {
                    boolean alreadyExists = combinedResults.stream().anyMatch(spot -> spot.getSpotId().equals(addressResult.getSpotId()));
                    if (!alreadyExists) {
                        combinedResults.add(addressResult);
                    }
                }
                return createPageFromList(combinedResults, pageable);
            }
        }
        // 沒有 keyword 則維持原本查詢，但需要正確處理全部狀態
        return spotRepository.findByKeywordAndStatusAndRegion(keyword, spotStatus, region, pageable);
    }

    /**
     * 從完整列表創建分頁結果
     * @param fullList 完整的資料列表
     * @param pageable 分頁參數
     * @return 分頁結果
     */
    private Page<SpotVO> createPageFromList(List<SpotVO> fullList, Pageable pageable) {
        int totalElements = fullList.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), totalElements);
        
        // 確保索引範圍有效
        if (start >= totalElements) {
            return new org.springframework.data.domain.PageImpl<>(new java.util.ArrayList<>(), pageable, totalElements);
        }
        
        List<SpotVO> pageContent = fullList.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, totalElements);
    }

    @Override
    public List<SpotVO> searchAllReviewedSpotsForAdmin(String keyword, Integer spotStatus, String region, Sort sort) {
        // 修改：當 spotStatus 為 null 時，查詢所有已審核景點（不包括待審核的狀態0）
        return spotRepository.findBySpotStatusAndRegionContaining(spotStatus, region, sort);
    }

    @Override
    public List<SpotVO> getAllReviewedSpots() {
        return spotRepository.findAll().stream()
                .filter(spot -> spot.getSpotStatus() != 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpotVO> getPendingSpotsWithAutoCheck() {
        return spotRepository.findBySpotStatus((byte) 0);
    }

    @Override
    public long getTotalSpotCount() {
        return spotRepository.count();
    }

    @Override
    public long getSpotCountByStatus(int spotStatus) {
        return spotRepository.countBySpotStatus(spotStatus);
    }

    @Override
    public List<String> getAllRegions() {
        return spotRepository.getAllRegions();
    }

    @Override
    public boolean existsBySpotName(String spotName) {
        return spotRepository.existsBySpotName(spotName);
    }

    @Override
    public boolean existsByGovtId(String govtId) {
        return spotRepository.existsByGovtId(govtId);
    }

    @Override
    public boolean existsBySpotNameAndSpotLoc(String spotName, String spotLoc) {
        return spotRepository.existsBySpotNameAndSpotLoc(spotName, spotLoc);
    }

    @Override
    public List<String> findExistingGovtIds(List<String> govtIds) {
        if (govtIds == null || govtIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        try {
            List<String> existingIds = spotRepository.findExistingGovtIds(govtIds);
            logger.debug("批量查詢結果：輸入 {} 個 govtId，找到 {} 個已存在", govtIds.size(), existingIds.size());
            return existingIds;
        } catch (Exception e) {
            logger.error("批量查詢 govtId 時發生錯誤，回退到逐個查詢: {}", e.getMessage());
            // 回退到逐個查詢
            return govtIds.stream()
                    .filter(this::existsByGovtId)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    @Override
    public boolean existsById(Integer id) {
        return spotRepository.existsById(id);
    }

    @Override
    public List<SpotVO> getSpotsWithoutCoordinates() {
        return spotRepository.findBySpotLatIsNullOrSpotLngIsNull();
    }

    @Override
    public List<SpotVO> getSpotsWithCoordinates() {
        return spotRepository.findBySpotLatIsNotNullAndSpotLngIsNotNull();
    }

    @Override
    public List<SpotVO> getActiveSpotsWithCoordinates() {
        return spotRepository.findActiveSpotsWithCoordinates();
    }

    @Override
    public List<SpotVO> getRelatedSpots(Integer spotId, int limit) {
        List<SpotVO> allRelated = spotRepository.findRelatedSpots(spotId, limit);
        return allRelated.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SpotVO addSpot(SpotVO spotVO) {
        spotVO.setSpotCreateAt(LocalDateTime.now());
        // 新增：自動補 Place ID (但在批量匯入時跳過以提升效能)
        // 檢查是否為批量匯入模式（透過 ThreadLocal 或其他方式）
        boolean isBatchImport = Boolean.TRUE.equals(ThreadLocal.withInitial(() -> false).get());
        
        if (!isBatchImport && (spotVO.getGooglePlaceId() == null || spotVO.getGooglePlaceId().isEmpty())) {
            try {
                var placeInfo = googlePlacesService.searchPlace(spotVO.getSpotName(), spotVO.getSpotLoc(), spotVO.getSpotLat(), spotVO.getSpotLng());
                if (placeInfo != null && placeInfo.getPlaceId() != null && !placeInfo.getPlaceId().isEmpty()) {
                    spotVO.setGooglePlaceId(placeInfo.getPlaceId());
                }
            } catch (Exception e) {
                // 可加 log.warn("新增景點自動補 Place ID 失敗: " + spotVO.getSpotName(), e);
            }
        }
        return spotRepository.save(spotVO);
    }

    @Override
    @Transactional
    public SpotVO updateSpot(SpotVO spotVO) {
        if (spotVO.getCreatorType() == null) {
            spotVO.setCreatorType((byte) 1); // 設定預設值為會員建立
        }
        spotVO.setSpotUpdatedAt(LocalDateTime.now());
        return spotRepository.save(spotVO);
    }

    @Override
    @Transactional
    public void deleteSpot(Integer spotId) {
        spotRepository.deleteById(spotId);
    }

    @Override
    public List<SpotVO> searchSpotsWithFilters(String keyword, String region) {
        List<SpotVO> searchResults;
        
        // 先使用新的搜尋邏輯進行關鍵字搜尋
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = searchPublicSpots(keyword.trim());
        } else {
            searchResults = getActiveSpots();
        }
        
        // 再應用地區篩選
        if (region != null && !region.trim().isEmpty() && !region.equals("所有地區")) {
            searchResults = searchResults.stream()
                    .filter(spot -> {
                        String spotRegion = getSpotRegion(spot.getSpotLoc());
                        return region.equals(spotRegion);
                    })
                    .collect(Collectors.toList());
        }
        
        return searchResults;
    }

    @Override
    public List<SpotVO> getSpotsByIds(List<Integer> spotIds) {
        return spotRepository.findAllById(spotIds);
    }

    @Override
    public List<SpotVO> getActiveSpots() {
        return spotRepository.findActiveSpots();
    }

    @Override
    @Transactional
    public List<SpotVO> addSpotsInBatch(List<SpotVO> spots) {
        return spotRepository.saveAll(spots);
    }

    @Override
    @Transactional
    public List<SpotVO> addSpotsInBatchOptimized(List<SpotVO> spots) {
        if (spots == null || spots.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        logger.info("開始批量插入優化處理，共 {} 筆景點", spots.size());
        long startTime = System.currentTimeMillis();
        
        // 設定建立時間
        LocalDateTime now = LocalDateTime.now();
        spots.forEach(spot -> {
            if (spot.getSpotCreateAt() == null) {
                spot.setSpotCreateAt(now);
            }
        });
        
        try {
            // 使用 JPA 的 saveAll 方法進行批量插入
            List<SpotVO> savedSpots = spotRepository.saveAll(spots);
            
            long insertTime = System.currentTimeMillis();
            logger.info("批量插入完成，共 {} 筆，耗時 {} 毫秒", 
                       savedSpots.size(), (insertTime - startTime));
            
            // 在插入後補充圖片 - 只針對有 Google Place ID 的景點
            logger.info("開始補充景點圖片...");
            int enrichedCount = 0;
            for (SpotVO spot : savedSpots) {
                try {
                    if (spot.getGooglePlaceId() != null && !spot.getGooglePlaceId().trim().isEmpty() 
                        && (spot.getFirstPictureUrl() == null || spot.getFirstPictureUrl().trim().isEmpty())) {
                        
                        // 獲取 Google Places 圖片
                        var placeDetails = googlePlacesService.getPlaceDetails(spot.getGooglePlaceId());
                        if (placeDetails != null && placeDetails.getPhotoReferences() != null 
                            && !placeDetails.getPhotoReferences().isEmpty()) {
                            
                            String photoUrl = googlePlacesService.getPhotoUrl(
                                placeDetails.getPhotoReferences().get(0), 800);
                            if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                                spot.setFirstPictureUrl(photoUrl);
                                spotRepository.save(spot);  // 更新圖片URL
                                enrichedCount++;
                                logger.debug("已為景點 '{}' 補充圖片", spot.getSpotName());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("為景點 '{}' 補充圖片時發生錯誤: {}", spot.getSpotName(), e.getMessage());
                    // 繼續處理下一個，不中斷整個流程
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("圖片補充完成，共補充 {} 筆圖片，總耗時 {} 毫秒（插入：{} 毫秒，圖片：{} 毫秒）", 
                       enrichedCount, 
                       (endTime - startTime),
                       (insertTime - startTime),
                       (endTime - insertTime));
            
            return savedSpots;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("批量插入失敗，耗時 {} 毫秒: {}", (endTime - startTime), e.getMessage());
            throw new RuntimeException("批量插入景點失敗: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void resetAutoIncrement() {
        spotRepository.resetAutoIncrement();
    }

    @Override
    @Transactional
    public SpotService.BatchResult addSpotsWithGeocoding(List<SpotVO> spots) {
        SpotService.BatchResult result = new SpotService.BatchResult();
        result.setTotalCount(spots.size());
        
        int successCount = 0;
        for (SpotVO spot : spots) {
            try {
                spot.setSpotCreateAt(LocalDateTime.now());
                SpotVO saved = spotRepository.save(spot);
                result.getSuccessSpots().add(saved);
                successCount++;
            } catch (Exception e) {
                result.getErrors().add("景點 " + spot.getSpotName() + " 儲存失敗: " + e.getMessage());
            }
        }
        
        result.setSuccessCount(successCount);
        result.setFailCount(spots.size() - successCount);
        return result;
    }

    // Helper method for filtering spots
    private boolean matchesFilter(SpotVO spot, String keyword, String region) {
        boolean matchesKeyword = keyword == null || keyword.isEmpty() ||
                (spot.getSpotName() != null && spot.getSpotName().contains(keyword)) ||
                (spot.getSpotLoc() != null && spot.getSpotLoc().contains(keyword));
        
        boolean matchesRegion = region == null || region.isEmpty() || region.equals("all") ||
                (spot.getRegion() != null && spot.getRegion().equals(region));
        
        return matchesKeyword && matchesRegion;
    }

    @Override
    public List<SpotVO> findBySearchCriteria(String keyword, String region, String sortBy, String sortDirection) {
        return spotRepository.findBySearchCriteria(keyword, region, sortBy, sortDirection);
    }

    /**
     * 自動補全所有沒有 Google Place ID 的景點，並存回 MySQL
     * 回傳補全的筆數
     */
    @Transactional
    public int enrichAllSpotsWithGooglePlaceId() {
        List<SpotVO> spots = spotRepository.findAllWithoutPlaceId();
        int updated = 0;
        for (SpotVO spot : spots) {
            // 用名稱+地址查詢 Google Place ID
            String placeId = null;
            try {
                var placeInfo = googlePlacesService.searchPlace(spot.getSpotName(), spot.getSpotLoc(), spot.getSpotLat(), spot.getSpotLng());
                if (placeInfo != null && placeInfo.getPlaceId() != null && !placeInfo.getPlaceId().isEmpty()) {
                    placeId = placeInfo.getPlaceId();
                    // 更新到 MySQL
                    int result = spotRepository.updatePlaceId(spot.getSpotId(), placeId);
                    if (result > 0) updated++;
                }
            } catch (Exception e) {
                // 可加 log.warn("查詢/更新 Place ID 失敗: " + spot.getSpotName(), e);
            }
        }
        return updated;
    }

    @Override
    public List<SpotVO> getSpotsByCities(List<String> cities) {
        if (cities == null || cities.isEmpty()) {
            return getActiveSpots();
        }
        List<SpotVO> all = getActiveSpots();
        return all.stream()
            .filter(spot -> {
                String loc = spot.getSpotLoc();
                return loc != null && cities.stream().anyMatch(loc::contains);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SpotVO> searchPublicSpotsByCities(String keyword, List<String> cities) {
        if (cities == null || cities.isEmpty()) {
            // 沒有指定城市，直接用原本的 searchPublicSpots
            return searchPublicSpots(keyword);
        }
        List<SpotVO> all = getSpotsByCities(cities);
        if (keyword == null || keyword.trim().isEmpty()) {
            return all;
        }
        String trimmedKeyword = keyword.trim();
        // 先名稱搜尋
        List<SpotVO> nameResults = all.stream()
            .filter(spot -> spot.getSpotName() != null && spot.getSpotName().contains(trimmedKeyword))
            .collect(Collectors.toList());
        if (nameResults.size() >= 3) {
            return nameResults;
        }
        // 名稱少於3個再加地址搜尋
        List<SpotVO> addressResults = all.stream()
            .filter(spot -> spot.getSpotLoc() != null && spot.getSpotLoc().contains(trimmedKeyword))
            .collect(Collectors.toList());
        // 合併去重
        List<SpotVO> combinedResults = new java.util.ArrayList<>(nameResults);
        for (SpotVO addressResult : addressResults) {
            boolean alreadyExists = combinedResults.stream()
                .anyMatch(spot -> spot.getSpotId().equals(addressResult.getSpotId()));
            if (!alreadyExists) {
                combinedResults.add(addressResult);
            }
        }
        return combinedResults;
    }
} 