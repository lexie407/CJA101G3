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
        return spotRepository.findBySpotNameContainingAndSpotStatus(keyword, (byte) 1);
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
        return spotRepository.findByRegion(region);
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
        return spotRepository.findAll().stream()
                .filter(spot -> matchesFilter(spot, keyword, region))
                .collect(Collectors.toList());
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
} 