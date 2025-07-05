package com.toiukha.spot.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 景點資料存取介面
 * 提供基本的 CRUD 操作和自訂查詢方法
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
public interface SpotRepository extends JpaRepository<SpotVO, Integer> {
    
    /**
     * 根據景點名稱查詢 (模糊搜尋)
     * @param spotName 景點名稱
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotNameContaining(String spotName);
    
    /**
     * 根據地點查詢 (模糊搜尋)
     * @param spotLoc 地點名稱
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotLocContaining(String spotLoc);
    
    /**
     * 根據狀態查詢景點
     * @param spotStatus 景點狀態 (0=待審核, 1=上架, 2=退回)
     * @return 符合條件的景點列表
     */
    List<SpotVO> findBySpotStatus(Byte spotStatus);
    
    /**
     * 根據狀態查詢景點 (分頁)
     * @param spotStatus 景點狀態
     * @param pageable 分頁資訊
     * @return 景點分頁結果
     */
    Page<SpotVO> findBySpotStatus(Byte spotStatus, Pageable pageable);
    
    /**
     * 根據建立者查詢景點
     * @param crtId 建立者ID
     * @return 符合條件的景點列表
     */
    List<SpotVO> findByCrtId(Integer crtId);
    
    /**
     * 查詢所有上架景點 (前台用)
     * @return 上架景點列表，按建立時間倒序排列
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 ORDER BY s.spotCreateAt DESC")
    List<SpotVO> findActiveSpots();
    
    /**
     * 複合搜尋：根據名稱或地點搜尋上架景點
     * @param keyword 搜尋關鍵字
     * @return 符合條件的上架景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 AND " +
           "(s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "ORDER BY s.spotCreateAt DESC")
    List<SpotVO> searchActiveSpots(@Param("keyword") String keyword);
    
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
     * 根據縣市查詢上架景點
     * @param region 縣市名稱
     * @return 符合條件的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 AND s.region = :region ORDER BY s.spotCreateAt DESC")
    List<SpotVO> findActiveSpotsByRegion(@Param("region") String region);
    
    /**
     * 查詢有經緯度的上架景點 (用於地圖顯示)
     * @return 有座標的上架景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 AND s.spotLat IS NOT NULL AND s.spotLng IS NOT NULL")
    List<SpotVO> findActiveSpotsWithCoordinates();
    
    /**
     * 查詢沒有座標的景點 (用於地理編碼)
     * @return 沒有座標的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotLat IS NULL OR s.spotLng IS NULL ORDER BY s.spotCreateAt DESC")
    List<SpotVO> findSpotsWithoutCoordinates();
    
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

    long countBySpotStatus(Integer status);
    
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
     * 後台複合搜尋 - 支援關鍵字、狀態、地區篩選 (分頁版本)
     * @param keyword 搜尋關鍵字 (景點名稱或地點)
     * @param status 景點狀態 (可為null表示不限)
     * @param region 地區 (可為null表示不限)
     * @param pageable 分頁資訊
     * @return 符合條件的景點分頁結果
     */
    @Query("SELECT s FROM SpotVO s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    Page<SpotVO> findByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region, 
        Pageable pageable);

    /**
     * 後台複合搜尋 - 查詢所有符合條件的資料（不分頁，供前端JavaScript使用）
     * @param keyword 搜尋關鍵字 (景點名稱或地點)
     * @param status 景點狀態 (可為null表示不限)
     * @param region 地區 (可為null表示不限)
     * @param sort 排序資訊
     * @return 符合條件的所有景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    List<SpotVO> findAllByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region, 
        org.springframework.data.domain.Sort sort);
    
    /**
     * 後台複合搜尋 - 計算總數用
     * @param keyword 搜尋關鍵字
     * @param status 景點狀態
     * @param region 地區
     * @return 符合條件的總筆數
     */
    @Query("SELECT COUNT(s) FROM SpotVO s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    long countByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region);

    /**
     * 查詢所有地區列表 (用於下拉選單)
     * @return 地區列表
     */
    @Query("SELECT DISTINCT s.region FROM SpotVO s WHERE s.region IS NOT NULL AND s.region != '' ORDER BY s.region")
    List<String> findDistinctRegions();

    /**
     * 從地址中提取地區資訊 (備用方案)
     * 提取地址開頭的縣市資訊 (例如: "台北市信義區" -> "台北市")
     * @return 地區列表
     */
    @Query("SELECT DISTINCT SUBSTRING(s.spotLoc, 1, 3) FROM SpotVO s WHERE s.spotLoc IS NOT NULL AND LENGTH(s.spotLoc) >= 3 ORDER BY SUBSTRING(s.spotLoc, 1, 3)")
    List<String> findDistinctRegionsFromLocation();

    /**
     * 查詢所有已通過審核的景點（狀態1）- 用於景點列表
     * @return 已通過審核的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 ORDER BY s.spotCreateAt DESC")
    List<SpotVO> findApprovedSpots();

    /**
     * 查詢所有已審核的景點（狀態1、2、3）- 用於景點列表
     * @return 已審核的景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus IN (1, 2, 3) ORDER BY s.spotCreateAt DESC")
    List<SpotVO> findReviewedSpots();

    /**
     * 後台景點列表 - 只查詢已通過審核的景點（狀態1），過濾掉待審核（狀態0）和退回（狀態2）的景點
     * @param keyword 搜尋關鍵字 (景點名稱或地點)
     * @param status 景點狀態 (只能是1，null表示不限)
     * @param region 地區 (可為null表示不限)
     * @param pageable 分頁資訊
     * @return 符合條件的已通過審核景點分頁結果
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 " +
           "AND (:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    Page<SpotVO> findApprovedByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region, 
        Pageable pageable);

    /**
     * 後台景點列表 - 查詢所有符合條件的已通過審核景點（不分頁，供前端JavaScript使用）
     * @param keyword 搜尋關鍵字 (景點名稱或地點)
     * @param status 景點狀態 (只能是1，null表示不限)
     * @param region 地區 (可為null表示不限)
     * @param sort 排序資訊
     * @return 符合條件的所有已通過審核景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus = 1 " +
           "AND (:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    List<SpotVO> findAllApprovedByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region, 
        org.springframework.data.domain.Sort sort);

    /**
     * 後台複合搜尋 - 只查詢已審核的景點（狀態1、2、3），過濾掉待審核的景點（狀態0）
     * @param keyword 搜尋關鍵字 (景點名稱或地點)
     * @param status 景點狀態 (只能是1、2、3，null表示不限)
     * @param region 地區 (可為null表示不限)
     * @param pageable 分頁資訊
     * @return 符合條件的已審核景點分頁結果
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus IN (1, 2, 3) " +
           "AND (:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    Page<SpotVO> findReviewedByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region, 
        Pageable pageable);

    /**
     * 後台複合搜尋 - 查詢所有符合條件的已審核景點（狀態1、2、3）（不分頁，供前端JavaScript使用）
     * @param keyword 搜尋關鍵字 (景點名稱或地點)
     * @param status 景點狀態 (只能是1、2、3，null表示不限)
     * @param region 地區 (可為null表示不限)
     * @param sort 排序資訊
     * @return 符合條件的所有已審核景點列表
     */
    @Query("SELECT s FROM SpotVO s WHERE s.spotStatus IN (1, 2, 3) " +
           "AND (:keyword IS NULL OR :keyword = '' OR s.spotName LIKE %:keyword% OR s.spotLoc LIKE %:keyword%) " +
           "AND (:status IS NULL OR s.spotStatus = :status) " +
           "AND (:region IS NULL OR :region = '' OR :region = 'all' OR s.region = :region)")
    List<SpotVO> findAllReviewedByKeywordAndStatusAndRegion(
        @Param("keyword") String keyword, 
        @Param("status") Integer status, 
        @Param("region") String region, 
        org.springframework.data.domain.Sort sort);
        
    // 移除 findByIsPublic 和 findBySpotStatusAndIsPublic 方法
} 