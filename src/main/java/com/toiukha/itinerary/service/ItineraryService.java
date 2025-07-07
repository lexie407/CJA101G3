package com.toiukha.itinerary.service;

import com.toiukha.itinerary.model.ItineraryVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 行程業務邏輯服務
 * 提供行程相關的業務操作
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
public interface ItineraryService {

    // ========== 1. 行程基本 CRUD 操作 ==========

    /**
     * 新增行程
     * @param itineraryVO 行程資料
     * @return 儲存後的行程資料
     */
    @Transactional
    ItineraryVO addItinerary(ItineraryVO itineraryVO);

    /**
     * 更新行程資料
     * @param itineraryVO 要更新的行程資料
     * @return 更新後的行程資料
     */
    @Transactional
    ItineraryVO updateItinerary(ItineraryVO itineraryVO);

    /**
     * 根據ID查詢行程
     * @param itnId 行程ID
     * @return 行程資料，不存在則返回null
     */
    ItineraryVO getItineraryById(Integer itnId);

    /**
     * 根據ID查詢行程（包含景點資料）
     * @param itnId 行程ID
     * @return 行程資料，包含景點列表，不存在則返回null
     */
    @Transactional(readOnly = true)
    ItineraryVO getItineraryWithSpots(Integer itnId);

    /**
     * 根據ID刪除行程
     * @param itnId 行程ID
     */
    @Transactional
    void deleteItinerary(Integer itnId);

    // ========== 2. 查詢操作 ==========

    /**
     * 查詢所有行程 (後台管理用)
     * @return 所有行程列表
     */
    List<ItineraryVO> getAllItineraries();

    /**
     * 查詢所有行程（分頁）
     * @param pageable 分頁參數
     * @return 行程的分頁結果
     */
    Page<ItineraryVO> getAllItineraries(Pageable pageable);

    /**
     * 查詢所有公開行程
     * @return 公開行程列表
     */
    List<ItineraryVO> getPublicItineraries();

    /**
     * 查詢所有非揪團模組的公開行程
     * @return 非揪團模組的公開行程列表
     */
    List<ItineraryVO> getPublicItinerariesNotFromActivity();

    /**
     * 查詢非揪團模組的公開行程（分頁）
     * @param pageable 分頁參數
     * @return 非揪團模組的公開行程分頁結果
     */
    Page<ItineraryVO> getPublicItinerariesNotFromActivity(Pageable pageable);
    
    /**
     * 批量添加景點到行程中
     * @param itnId 行程ID
     * @param spotIds 景點ID列表
     * @return 添加的景點數量
     */
    @Transactional
    int addSpotsToItinerary(Integer itnId, List<Integer> spotIds);

    /**
     * 查詢公開行程（分頁）
     * @param pageable 分頁參數
     * @return 公開行程分頁結果
     */
    Page<ItineraryVO> getPublicItineraries(Pageable pageable);

    /**
     * 根據建立者查詢行程
     * @param crtId 建立者ID
     * @return 行程列表
     */
    List<ItineraryVO> getItinerariesByCrtId(Integer crtId);

    /**
     * 根據建立者查詢行程（分頁）
     * @param crtId 建立者ID
     * @param pageable 分頁參數
     * @return 行程分頁結果
     */
    Page<ItineraryVO> getItinerariesByCrtId(Integer crtId, Pageable pageable);

    /**
     * 根據建立者查詢公開行程
     * @param crtId 建立者ID
     * @return 公開行程列表
     */
    List<ItineraryVO> getPublicItinerariesByCrtId(Integer crtId);

    /**
     * 根據建立者查詢私人行程
     * @param crtId 建立者ID
     * @return 私人行程列表
     */
    List<ItineraryVO> getPrivateItinerariesByCrtId(Integer crtId);

    /**
     * 搜尋非揪團模組的公開行程
     * @param keyword 搜尋關鍵字
     * @return 符合條件的非揪團模組公開行程列表
     */
    List<ItineraryVO> searchPublicItinerariesNotFromActivity(String keyword);

    /**
     * 搜尋非揪團模組的公開行程（分頁）
     * @param keyword 搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的非揪團模組公開行程分頁結果
     */
    Page<ItineraryVO> searchPublicItinerariesNotFromActivity(String keyword, Pageable pageable);

    // ========== 3. 搜尋功能 ==========

    /**
     * 根據名稱搜尋行程
     * @param itnName 行程名稱
     * @return 符合條件的行程列表
     */
    List<ItineraryVO> searchItinerariesByName(String itnName);

    /**
     * 搜尋公開行程
     * @param keyword 搜尋關鍵字
     * @return 符合條件的公開行程列表
     */
    List<ItineraryVO> searchPublicItineraries(String keyword);

    /**
     * 搜尋公開行程（分頁）
     * @param keyword 搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的公開行程分頁結果
     */
    Page<ItineraryVO> searchPublicItineraries(String keyword, Pageable pageable);

    /**
     * 搜尋行程
     */
    List<ItineraryVO> searchItineraries(String keyword, Integer isPublic);

    /**
     * 根據公開狀態查詢行程
     */
    List<ItineraryVO> getItinerariesByPublicStatus(Integer isPublic);

    /**
     * 獲取所有公開行程
     */
    List<ItineraryVO> getAllPublicItineraries();

    /**
     * 根據會員ID查詢收藏的行程
     */
    List<ItineraryVO> findFavoriteItinerariesByMemId(Integer memId);

    // ========== 4. 統計功能 ==========

    /**
     * 取得總行程數量
     * @return 總行程數量
     */
    long getTotalItineraryCount();

    /**
     * 取得公開行程數量
     * @return 公開行程數量
     */
    long getPublicItineraryCount();

    /**
     * 取得私人行程數量
     * @return 私人行程數量
     */
    long getPrivateItineraryCount();

    /**
     * 根據建立者取得行程數量
     * @param crtId 建立者ID
     * @return 行程數量
     */
    long getItineraryCountByCrtId(Integer crtId);

    /**
     * 清除行程的所有景點關聯
     * @param itnId 行程ID
     */
    @Transactional
    void clearItinerarySpots(Integer itnId);

    // ========== 5. 驗證功能 ==========

    /**
     * 檢查行程名稱是否已存在
     * @param itnName 行程名稱
     * @return true 如果名稱已存在
     */
    boolean isItineraryNameExists(String itnName);

    /**
     * 檢查行程是否存在
     * @param itnId 行程ID
     * @return true 如果行程存在
     */
    boolean isItineraryExists(Integer itnId);

    // ========== 6. 行程景點管理 ==========

    /**
     * 為行程添加景點
     * @param itnId 行程ID
     * @param spotId 景點ID
     * @return 新增的行程景點關聯資料
     */
    @Transactional
    com.toiukha.itinerary.model.ItnSpotVO addSpotToItinerary(Integer itnId, Integer spotId);

    /**
     * 從行程中移除景點
     * @param itnId 行程ID
     * @param spotId 景點ID
     */
    @Transactional
    void removeSpotFromItinerary(Integer itnId, Integer spotId);

    /**
     * 取得行程的所有景點
     * @param itnId 行程ID
     * @return 行程景點關聯列表
     */
    List<com.toiukha.itinerary.model.ItnSpotVO> getSpotsByItineraryId(Integer itnId);

    /**
     * 取得行程的景點數量
     * @param itnId 行程ID
     * @return 景點數量
     */
    Long getSpotCountByItineraryId(Integer itnId);

    // ========== 7. 後台管理專用方法 ==========

    /**
     * 根據條件查詢行程（後台管理用）
     * @param keyword 搜尋關鍵字
     * @param status 狀態篩選
     * @param isPublic 公開狀態篩選
     * @param pageable 分頁參數
     * @return 符合條件的行程分頁結果
     */
    Page<ItineraryVO> findItinerariesWithFilters(String keyword, Integer status, Integer isPublic, Pageable pageable);

    /**
     * 更新行程的景點
     * @param itnId 行程ID
     * @param spotIds 景點ID列表
     */
    @Transactional
    void updateSpotsForItinerary(Integer itnId, List<Integer> spotIds);

    /**
     * 為行程列表設置建立者顯示名稱
     * @param itineraryList 行程列表
     * @param currentMemId 當前登入會員ID
     */
    void setCreatorDisplayNames(List<ItineraryVO> itineraryList, Integer currentMemId);
} 
