package com.toiukha.spot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.toiukha.spot.model.SpotVO;

/**
 * 景點資料存取介面
 * 提供基本的 CRUD 操作和自訂查詢方法
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Repository
public interface SpotRepository extends JpaRepository<SpotVO, Integer> {
    
    /**
     * 根據景點名稱查詢 (模糊搜尋)
     * @param spotName 景點名稱
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotNameContaining(String spotName);
    
    /**
     * 根據景點名稱查詢 (模糊搜尋)
     * @param spotName 景點名稱
     * @param spotStatus 景點狀態
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotNameContainingAndSpotStatus(String spotName, byte spotStatus);
    
    /**
     * 根據景點地址查詢 (模糊搜尋)
     * @param spotLoc 景點地址
     * @param spotStatus 景點狀態
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotLocContainingAndSpotStatus(String spotLoc, byte spotStatus);
    
    /**
     * 根據狀態查詢景點
     * @param spotStatus 景點狀態 (0=待審核, 1=上架, 2=退回)
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotStatus(byte spotStatus);
    
    /**
     * 根據縣市查詢上架景點
     * @param region 縣市名稱
     * @return 符合條件的景點列表
     */
    List<SpotVO> findByRegion(String region);
    
    /**
     * 查詢所有上架景點 (前台用)
     * @return 上架景點列表，按建立時間倒序排列
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 ORDER BY s.spotCreateAt DESC")
    List<SpotVO> findActiveSpots();
    
    /**
     * 查詢有經緯度的上架景點 (用於地圖顯示)
     * @return 有座標的上架景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 AND s.spotLat IS NOT NULL AND s.spotLng IS NOT NULL")
    List<SpotVO> findActiveSpotsWithCoordinates();
    
    /**
     * 查詢有經緯度的景點 (不限狀態)
     * @return 有座標的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotLat IS NOT NULL AND s.spotLng IS NOT NULL")
    List<SpotVO> findBySpotLatIsNotNullAndSpotLngIsNotNull();
    
    /**
     * 複合搜尋：根據名稱或地點搜尋景點
     * @param keyword 搜尋關鍵字
     * @param spotStatus 景點狀態
     * @param region 地區
     * @param pageable 分頁資訊
     * @return 符合條件的景點分頁結果
     */
    @Query("SELECT s FROM SpotVO s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:spotStatus IS NULL OR s.spotStatus = :spotStatus) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    Page<SpotVO> findByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("spotStatus") Integer spotStatus, 
        @Param("region") String region, 
        Pageable pageable);
    
    /**
     * 檢查景點名稱是否已存在
     * @param spotName 景點名稱
     * @return true 如果名稱已存在
     */
    boolean existsBySpotName(String spotName);
    
    /**
     * 根據政府資料ID檢查景點是否已存在
     * @param govtId 政府資料ID
     * @return true 如果景點已存在
     */
    boolean existsByGovtId(String govtId);
    
    /**
     * 查詢沒有座標的景點 (用於地理編碼)
     * @return 沒有座標的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotLat IS NULL OR s.spotLng IS NULL")
    List<SpotVO> findBySpotLatIsNullOrSpotLngIsNull();
    
    /**
     * 根據景點名稱模糊搜索，並指定狀態
     * @param spotName 景點名稱（模糊匹配）
     * @param spotStatus 景點狀態
     * @return 符合條件的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE LOWER(s.spotName) LIKE :spotName AND s.spotStatus = :spotStatus")
    List<SpotVO> findBySpotNameLikeAndSpotStatus(String spotName, Byte spotStatus);
    
    /**
     * 根據景點名稱模糊搜索，排除指定狀態
     * @param spotName 景點名稱（模糊匹配）
     * @param spotStatus 要排除的景點狀態
     * @return 符合條件的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE LOWER(s.spotName) LIKE :spotName AND s.spotStatus != :spotStatus")
    List<SpotVO> findBySpotNameLikeAndSpotStatusNot(String spotName, Byte spotStatus);

    /**
     * 根據狀態統計景點數量
     * @param spotStatus 景點狀態
     * @return 景點數量
     */
    long countBySpotStatus(int spotStatus);
    
    /**
     * 批量更新景點狀態
     * @param spotIds 要更新的景點ID列表
     * @param spotStatus 新的狀態
     * @return 更新的筆數
     */
    @Modifying
    @Query("UPDATE SpotVO s SET s.spotStatus = :spotStatus WHERE s.spotId IN :spotIds")
    int updateSpotStatusBatch(@Param("spotIds") List<Integer> spotIds, @Param("spotStatus") Byte spotStatus);
    
    /**
     * 查詢所有地區
     * @return 地區列表
     */
    @Query("SELECT DISTINCT s.region FROM SpotVO s WHERE s.region IS NOT NULL ORDER BY s.region")
    List<String> getAllRegions();
    
    /**
     * 根據狀態和地區查詢景點 (分頁)
     * @param spotStatus 景點狀態
     * @param region 地區
     * @param pageable 分頁資訊
     * @return 景點分頁結果
     */
    @Query("SELECT s FROM SpotVO s WHERE " +
           "(:spotStatus IS NULL OR s.spotStatus = :spotStatus) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region LIKE %:region%)")
    Page<SpotVO> findBySpotStatusAndRegionContaining(
        @Param("spotStatus") Integer spotStatus, 
        @Param("region") String region, 
        Pageable pageable);
    
    /**
     * 根據狀態和地區查詢景點 (排序)
     * @param spotStatus 景點狀態
     * @param region 地區
     * @param sort 排序條件
     * @return 景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE " +
           "(:spotStatus IS NULL OR s.spotStatus = :spotStatus) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region LIKE %:region%)")
    List<SpotVO> findBySpotStatusAndRegionContaining(
        @Param("spotStatus") Integer spotStatus, 
        @Param("region") String region, 
        Sort sort);
    
    /**
     * 重置自動遞增值
     */
    @Modifying
    @Query(value = "ALTER TABLE spot AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
    
    /**
     * 查詢相關景點
     * @param spotId 景點ID
     * @param limit 限制數量
     * @return 相關景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 " +
           "AND s.spotId != :spotId " +
           "AND s.region = (SELECT s2.region FROM SpotVO s2 WHERE s2.spotId = :spotId)")
    List<SpotVO> findRelatedSpots(@Param("spotId") Integer spotId, @Param("limit") int limit);

    @Query("SELECT s FROM SpotVO s WHERE " +
           "s.spotStatus = 1 AND " +
           "(:keyword IS NULL OR LOWER(s.spotName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.spotLoc) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:region IS NULL OR :region = '' OR s.region = :region) AND " +
           "(:rating IS NULL OR (s.googleRating IS NOT NULL AND s.googleRating >= :rating)) " +
           "ORDER BY " +
           "CASE " +
           "    WHEN :sortBy = 'rating' AND :sortDirection = 'desc' THEN s.googleRating " +
           "    WHEN :sortBy = 'rating' AND :sortDirection = 'asc' THEN -s.googleRating " +
           "    ELSE NULL " +
           "END DESC NULLS LAST, " +
           "CASE " +
           "    WHEN :sortBy = 'date' AND :sortDirection = 'desc' THEN s.spotCreateAt " +
           "    WHEN :sortBy = 'date' AND :sortDirection = 'asc' THEN s.spotCreateAt " +
           "    ELSE NULL " +
           "END DESC NULLS LAST")
    List<SpotVO> findBySearchCriteria(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("rating") Double rating,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection);
} 