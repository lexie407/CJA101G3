package com.toiukha.spot.service;

import com.toiukha.spot.model.SpotVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

/**
 * 景點服務介面
 * 定義所有景點相關的業務邏輯操作
 */
public interface SpotService {
    // 基本 CRUD 操作
    SpotVO findById(Integer id);
    SpotVO getSpotById(Integer id);
    List<SpotVO> getAllSpots();
    SpotVO save(SpotVO spot);
    void deleteById(Integer id);
    
    // 搜尋相關
    List<SpotVO> searchSpots(String keyword);
    List<SpotVO> searchPublicSpots(String keyword);
    List<SpotVO> getAllPublicSpots();
    List<SpotVO> getSpotsByStatus(byte spotStatus);
    List<SpotVO> getSpotsByRegion(String region);
    List<SpotVO> searchSpotsWithFilters(String keyword, String region);
    
    // 狀態管理
    boolean activateSpot(Integer id);
    boolean deactivateSpot(Integer id);
    boolean rejectSpot(Integer id, String reason, String remark);
    int batchUpdateStatus(List<Integer> spotIds, byte spotStatus);
    int batchActivateSpots(List<Integer> spotIds);
    int batchDeactivateSpots(List<Integer> spotIds);
    
    // 分頁查詢
    Page<SpotVO> searchReviewedSpotsForAdmin(String keyword, Integer spotStatus, String region, Pageable pageable);
    List<SpotVO> searchAllReviewedSpotsForAdmin(String keyword, Integer spotStatus, String region, Sort sort);
    List<SpotVO> getAllReviewedSpots();
    List<SpotVO> getPendingSpotsWithAutoCheck();
    
    // 統計相關
    long getTotalSpotCount();
    long getSpotCountByStatus(int spotStatus);
    List<String> getAllRegions();
    
    // 驗證相關
    boolean existsBySpotName(String spotName);
    boolean existsByGovtId(String govtId);
    boolean existsById(Integer id);
    
    // 座標相關
    List<SpotVO> getSpotsWithoutCoordinates();
    List<SpotVO> getSpotsWithCoordinates();
    List<SpotVO> getActiveSpotsWithCoordinates();
    
    // 相關景點
    List<SpotVO> getRelatedSpots(Integer spotId, int limit);
    
    // 新增和更新
    SpotVO addSpot(SpotVO spotVO);
    SpotVO updateSpot(SpotVO spotVO);
    void deleteSpot(Integer spotId);
    
    // 批次操作
    List<SpotVO> getSpotsByIds(List<Integer> spotIds);
    List<SpotVO> getActiveSpots();
    List<SpotVO> addSpotsInBatch(List<SpotVO> spots);
    void resetAutoIncrement();
    
    // 批次結果類別
    public static class BatchResult {
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<SpotVO> successSpots = new java.util.ArrayList<>();
        private List<String> errors = new java.util.ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        public List<SpotVO> getSuccessSpots() {
            return successSpots;
        }

        public void setSuccessSpots(List<SpotVO> successSpots) {
            this.successSpots = successSpots;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public void addSuccessSpot(SpotVO spot) {
            this.successSpots.add(spot);
        }

        public void addError(String error) {
            this.errors.add(error);
        }
    }
    
    // 批次匯入方法
    BatchResult addSpotsWithGeocoding(List<SpotVO> spots);
} 