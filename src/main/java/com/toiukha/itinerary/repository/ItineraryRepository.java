package com.toiukha.itinerary.repository;

import com.toiukha.itinerary.model.ItineraryVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 行程資料存取介面
 * 提供基本的 CRUD 操作和自訂查詢方法
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Repository
public interface ItineraryRepository extends JpaRepository<ItineraryVO, Integer> {

    /**
     * 根據ID查詢行程，包含景點資料
     * @param itnId 行程ID
     * @return 包含景點資料的行程
     */
    @EntityGraph(attributePaths = {"itnSpots", "itnSpots.spot"})
    @Query("SELECT i FROM ItineraryVO i WHERE i.itnId = :itnId")
    Optional<ItineraryVO> findByIdWithSpots(@Param("itnId") Integer itnId);

    /**
     * 根據行程名稱查詢 (模糊搜尋)
     * @param itnName 行程名稱
     * @return 符合條件的行程列表
     */
    List<ItineraryVO> findByItnNameContaining(String itnName);

    /**
     * 根據建立者查詢行程
     * @param crtId 建立者ID
     * @return 符合條件的行程列表
     */
    List<ItineraryVO> findByCrtId(Integer crtId);

    /**
     * 根據建立者查詢行程（分頁）
     * @param crtId 建立者ID
     * @param pageable 分頁資訊
     * @return 行程分頁結果
     */
    Page<ItineraryVO> findByCrtId(Integer crtId, Pageable pageable);

    /**
     * 查詢所有公開行程
     * @return 公開行程列表，按建立時間倒序排列
     */
    @Query("SELECT i FROM ItineraryVO i WHERE i.isPublic = 1 ORDER BY i.itnCreateDat DESC")
    List<ItineraryVO> findPublicItineraries();

    /**
     * 查詢公開行程（分頁）
     * @param pageable 分頁資訊
     * @return 公開行程分頁結果
     */
    @Query("SELECT i FROM ItineraryVO i WHERE i.isPublic = 1")
    Page<ItineraryVO> findPublicItineraries(Pageable pageable);

    /**
     * 複合搜尋：根據名稱搜尋公開行程
     * @param keyword 搜尋關鍵字
     * @return 符合條件的公開行程列表
     */
    @Query("SELECT i FROM ItineraryVO i WHERE i.isPublic = 1 AND " +
           "i.itnName LIKE %:keyword% ORDER BY i.itnCreateDat DESC")
    List<ItineraryVO> searchPublicItineraries(@Param("keyword") String keyword);

    /**
     * 複合搜尋：根據名稱搜尋公開行程（分頁）
     * @param keyword 搜尋關鍵字
     * @param pageable 分頁資訊
     * @return 符合條件的公開行程分頁結果
     */
    @Query("SELECT i FROM ItineraryVO i WHERE i.isPublic = 1 AND " +
           "i.itnName LIKE %:keyword%")
    Page<ItineraryVO> searchPublicItineraries(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 檢查行程名稱是否已存在
     * @param itnName 行程名稱
     * @return true 如果名稱已存在
     */
    boolean existsByItnName(String itnName);

    /**
     * 根據建立者查詢行程數量
     * @param crtId 建立者ID
     * @return 行程數量
     */
    Long countByCrtId(Integer crtId);

    /**
     * 查詢公開行程數量
     * @return 公開行程數量
     */
    Long countByIsPublicTrue();

    /**
     * 查詢私人行程數量
     * @return 私人行程數量
     */
    Long countByIsPublicFalse();

    /**
     * 根據建立者查詢公開行程
     * @param crtId 建立者ID
     * @return 公開行程列表
     */
    @Query("SELECT i FROM ItineraryVO i WHERE i.crtId = :crtId AND i.isPublic = 1 ORDER BY i.itnCreateDat DESC")
    List<ItineraryVO> findPublicItinerariesByCrtId(@Param("crtId") Integer crtId);

    /**
     * 根據建立者查詢私人行程
     * @param crtId 建立者ID
     * @return 私人行程列表
     */
    @Query("SELECT i FROM ItineraryVO i WHERE i.crtId = :crtId AND i.isPublic = 0 ORDER BY i.itnCreateDat DESC")
    List<ItineraryVO> findPrivateItinerariesByCrtId(@Param("crtId") Integer crtId);
} 