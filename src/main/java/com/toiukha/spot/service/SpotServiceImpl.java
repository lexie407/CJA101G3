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
import java.util.stream.Collectors;

@Service
@Transactional
public class SpotServiceImpl implements SpotService {

    @Autowired
    private SpotRepository spotRepository;

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
        if (keyword == null || keyword.trim().isEmpty()) {
            return spotRepository.findBySpotStatus((byte) 1);
        }
        
        String trimmedKeyword = keyword.trim();
        
        // 第一步：只搜尋名稱
        List<SpotVO> nameResults = spotRepository.findBySpotNameContainingAndSpotStatus(trimmedKeyword, (byte) 1);
        
        // 如果名稱搜尋結果大於等於3個，直接返回名稱搜尋結果
        if (nameResults.size() >= 3) {
            return nameResults;
        }
        
        // 如果名稱搜尋結果少於3個，加入地址搜尋
        List<SpotVO> addressResults = spotRepository.findBySpotLocContainingAndSpotStatus(trimmedKeyword, (byte) 1);
        
        // 合併結果，避免重複
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

    @Override
    public List<SpotVO> getAllPublicSpots() {
        return spotRepository.findBySpotStatus((byte) 1);
    }

    @Override
    public List<SpotVO> getSpotsByStatus(byte spotStatus) {
        return spotRepository.findBySpotStatus(spotStatus);
    }

    @Override
    public List<SpotVO> getSpotsByRegion(String region) {
        // 獲取所有上架景點
        List<SpotVO> activeSpots = getActiveSpots();
        
        // 如果地區為空或是"所有地區"，返回所有上架景點
        if (region == null || region.trim().isEmpty() || region.equals("所有地區")) {
            return activeSpots;
        }
        
        // 根據地區篩選
        return activeSpots.stream()
                .filter(spot -> {
                    String spotRegion = getSpotRegion(spot.getSpotLoc());
                    return region.equals(spotRegion);
                })
                .collect(Collectors.toList());
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
        return spotRepository.findByKeywordAndStatusAndRegion(keyword, spotStatus, region, pageable);
    }

    @Override
    public List<SpotVO> searchAllReviewedSpotsForAdmin(String keyword, Integer spotStatus, String region, Sort sort) {
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
    public List<SpotVO> findBySearchCriteria(String keyword, String region, Double rating, String sortBy) {
        return spotRepository.findBySearchCriteria(keyword, region, rating, sortBy, "desc");
    }

    @Override
    public List<SpotVO> findBySearchCriteria(String keyword, String region, Double rating, String sortBy, String sortDirection) {
        return spotRepository.findBySearchCriteria(keyword, region, rating, sortBy, sortDirection);
    }
} 