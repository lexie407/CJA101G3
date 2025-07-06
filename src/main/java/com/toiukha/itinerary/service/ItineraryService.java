package com.toiukha.itinerary.service;

import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.repository.ItineraryRepository;
import com.toiukha.favItn.model.FavItnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

/**
 * 行程業務邏輯服務
 * 提供行程相關的業務操作
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Service
public class ItineraryService {

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    @Qualifier("itnSpotService")
    private ItnSpotService itnSpotService;

    @Autowired
    private FavItnService favItnService;

    // ========== 1. 行程基本 CRUD 操作 ==========

    /**
     * 新增行程
     * @param itineraryVO 行程資料
     * @return 儲存後的行程資料
     */
    @Transactional
    public ItineraryVO addItinerary(ItineraryVO itineraryVO) {
        // 設定預設值
        if (itineraryVO.getIsPublic() == null) {
            itineraryVO.setIsPublic((byte) 1); // 預設為公開
        }
        if (itineraryVO.getItnStatus() == null) {
            itineraryVO.setItnStatus((byte) 1); // 預設為上架
        }
        if (itineraryVO.getCreatorType() == null) {
            itineraryVO.setCreatorType((byte) 1); // 預設為會員建立
        }
        return itineraryRepository.save(itineraryVO);
    }

    /**
     * 更新行程資料
     * @param itineraryVO 要更新的行程資料
     * @return 更新後的行程資料
     */
    @Transactional
    public ItineraryVO updateItinerary(ItineraryVO itineraryVO) {
        return itineraryRepository.save(itineraryVO);
    }

    /**
     * 根據ID查詢行程
     * @param itnId 行程ID
     * @return 行程資料，不存在則返回null
     */
    public ItineraryVO getItineraryById(Integer itnId) {
        Optional<ItineraryVO> optional = itineraryRepository.findById(itnId);
        return optional.orElse(null);
    }

    /**
     * 根據ID查詢行程（包含景點資料）
     * @param itnId 行程ID
     * @return 行程資料，包含景點列表，不存在則返回null
     */
    @Transactional(readOnly = true)
    public ItineraryVO getItineraryWithSpots(Integer itnId) {
        Optional<ItineraryVO> optional = itineraryRepository.findByIdWithSpots(itnId);
        return optional.orElse(null);
    }

    /**
     * 根據ID刪除行程
     * @param itnId 行程ID
     */
    @Transactional
    public void deleteItinerary(Integer itnId) {
        // 先刪除行程景點關聯
        itnSpotService.clearItinerarySpots(itnId);
        // 再刪除行程
        itineraryRepository.deleteById(itnId);
    }

    // ========== 2. 查詢操作 ==========

    /**
     * 查詢所有行程 (後台管理用)
     * @return 所有行程列表
     */
    public List<ItineraryVO> getAllItineraries() {
        return itineraryRepository.findAll();
    }

    /**
     * 查詢所有行程（分頁）
     * @param pageable 分頁參數
     * @return 行程的分頁結果
     */
    public Page<ItineraryVO> getAllItineraries(Pageable pageable) {
        return itineraryRepository.findAll(pageable);
    }

    /**
     * 查詢所有公開行程
     * @return 公開行程列表
     */
    public List<ItineraryVO> getPublicItineraries() {
        return itineraryRepository.findPublicItineraries();
    }

    /**
     * 查詢所有非揪團模組的公開行程
     * @return 非揪團模組的公開行程列表
     */
    public List<ItineraryVO> getPublicItinerariesNotFromActivity() {
        return itineraryRepository.findPublicItinerariesNotFromActivity();
    }

    /**
     * 查詢非揪團模組的公開行程（分頁）
     * @param pageable 分頁參數
     * @return 非揪團模組的公開行程分頁結果
     */
    public Page<ItineraryVO> getPublicItinerariesNotFromActivity(Pageable pageable) {
        return itineraryRepository.findPublicItinerariesNotFromActivity(pageable);
    }
    
    /**
     * 批量添加景點到行程中
     * @param itnId 行程ID
     * @param spotIds 景點ID列表
     * @return 添加的景點數量
     */
    @Transactional
    public int addSpotsToItinerary(Integer itnId, List<Integer> spotIds) {
        if (itnId == null || spotIds == null || spotIds.isEmpty()) {
            return 0;
        }
        
        // 先清空現有的關聯（如果有）
        itnSpotService.clearItinerarySpots(itnId);
        
        // 按順序添加景點
        int count = 0;
        for (Integer spotId : spotIds) {
            try {
                itnSpotService.addSpotToItinerary(itnId, spotId);
                count++;
            } catch (Exception e) {
                // 忽略添加失敗的景點
            }
        }
        
        return count;
    }

    /**
     * 查詢公開行程（分頁）
     * @param pageable 分頁參數
     * @return 公開行程分頁結果
     */
    public Page<ItineraryVO> getPublicItineraries(Pageable pageable) {
        return itineraryRepository.findPublicItineraries(pageable);
    }

    /**
     * 根據建立者查詢行程
     * @param crtId 建立者ID
     * @return 行程列表
     */
    public List<ItineraryVO> getItinerariesByCrtId(Integer crtId) {
        return itineraryRepository.findByCrtId(crtId);
    }

    /**
     * 根據建立者查詢行程（分頁）
     * @param crtId 建立者ID
     * @param pageable 分頁參數
     * @return 行程分頁結果
     */
    public Page<ItineraryVO> getItinerariesByCrtId(Integer crtId, Pageable pageable) {
        return itineraryRepository.findByCrtId(crtId, pageable);
    }

    /**
     * 根據建立者查詢公開行程
     * @param crtId 建立者ID
     * @return 公開行程列表
     */
    public List<ItineraryVO> getPublicItinerariesByCrtId(Integer crtId) {
        return itineraryRepository.findPublicItinerariesByCrtId(crtId);
    }

    /**
     * 根據建立者查詢私人行程
     * @param crtId 建立者ID
     * @return 私人行程列表
     */
    public List<ItineraryVO> getPrivateItinerariesByCrtId(Integer crtId) {
        return itineraryRepository.findPrivateItinerariesByCrtId(crtId);
    }

    /**
     * 搜尋非揪團模組的公開行程
     * @param keyword 搜尋關鍵字
     * @return 符合條件的非揪團模組公開行程列表
     */
    public List<ItineraryVO> searchPublicItinerariesNotFromActivity(String keyword) {
        return itineraryRepository.searchPublicItinerariesNotFromActivity(keyword);
    }

    /**
     * 搜尋非揪團模組的公開行程（分頁）
     * @param keyword 搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的非揪團模組公開行程分頁結果
     */
    public Page<ItineraryVO> searchPublicItinerariesNotFromActivity(String keyword, Pageable pageable) {
        return itineraryRepository.searchPublicItinerariesNotFromActivity(keyword, pageable);
    }

    // ========== 3. 搜尋功能 ==========

    /**
     * 根據名稱搜尋行程
     * @param itnName 行程名稱
     * @return 符合條件的行程列表
     */
    public List<ItineraryVO> searchItinerariesByName(String itnName) {
        return itineraryRepository.findByItnNameContaining(itnName);
    }

    /**
     * 搜尋公開行程
     * @param keyword 搜尋關鍵字
     * @return 符合條件的公開行程列表
     */
    public List<ItineraryVO> searchPublicItineraries(String keyword) {
        return itineraryRepository.searchPublicItineraries(keyword);
    }

    /**
     * 搜尋公開行程（分頁）
     * @param keyword 搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的公開行程分頁結果
     */
    public Page<ItineraryVO> searchPublicItineraries(String keyword, Pageable pageable) {
        return itineraryRepository.searchPublicItineraries(keyword, pageable);
    }

    /**
     * 搜尋行程
     */
    public List<ItineraryVO> searchItineraries(String keyword, Integer isPublic) {
        if (isPublic != null) {
            return itineraryRepository.findByItnNameContainingAndIsPublic(keyword, isPublic.byteValue());
        }
        return itineraryRepository.findByItnNameContaining(keyword);
    }

    /**
     * 根據公開狀態查詢行程
     */
    public List<ItineraryVO> getItinerariesByPublicStatus(Integer isPublic) {
        return itineraryRepository.findByIsPublic(isPublic.byteValue());
    }

    /**
     * 獲取所有公開行程
     */
    public List<ItineraryVO> getAllPublicItineraries() {
        return itineraryRepository.findByIsPublic((byte) 1);
    }

    /**
     * 根據會員ID查詢收藏的行程
     */
    public List<ItineraryVO> findFavoriteItinerariesByMemId(Integer memId) {
        List<Integer> favoriteIds = favItnService.findFavoriteIdsByMemId(memId);
        if (favoriteIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return itineraryRepository.findAllById(favoriteIds);
    }

    // ========== 4. 統計功能 ==========

    /**
     * 取得總行程數量
     * @return 總行程數量
     */
    public long getTotalItineraryCount() {
        return itineraryRepository.count();
    }

    /**
     * 取得公開行程數量
     * @return 公開行程數量
     */
    public long getPublicItineraryCount() {
        return itineraryRepository.countByIsPublicTrue();
    }

    /**
     * 取得私人行程數量
     * @return 私人行程數量
     */
    public long getPrivateItineraryCount() {
        return itineraryRepository.countByIsPublicFalse();
    }

    /**
     * 根據建立者取得行程數量
     * @param crtId 建立者ID
     * @return 行程數量
     */
    public long getItineraryCountByCrtId(Integer crtId) {
        return itineraryRepository.countByCrtId(crtId);
    }

    /**
     * 清除行程的所有景點關聯
     * @param itnId 行程ID
     */
    @Transactional
    public void clearItinerarySpots(Integer itnId) {
        itnSpotService.clearItinerarySpots(itnId);
    }

    // ========== 5. 驗證功能 ==========

    /**
     * 檢查行程名稱是否已存在
     * @param itnName 行程名稱
     * @return true 如果名稱已存在
     */
    public boolean isItineraryNameExists(String itnName) {
        return itineraryRepository.existsByItnName(itnName);
    }

    /**
     * 檢查行程是否存在
     * @param itnId 行程ID
     * @return true 如果行程存在
     */
    public boolean isItineraryExists(Integer itnId) {
        return itineraryRepository.existsById(itnId);
    }

    // ========== 6. 行程景點管理 ==========

    /**
     * 為行程添加景點
     * @param itnId 行程ID
     * @param spotId 景點ID
     * @return 新增的行程景點關聯資料
     */
    @Transactional
    public com.toiukha.itinerary.model.ItnSpotVO addSpotToItinerary(Integer itnId, Integer spotId) {
        return itnSpotService.addSpotToItinerary(itnId, spotId);
    }

    /**
     * 從行程中移除景點
     * @param itnId 行程ID
     * @param spotId 景點ID
     */
    @Transactional
    public void removeSpotFromItinerary(Integer itnId, Integer spotId) {
        itnSpotService.removeSpotFromItinerary(itnId, spotId);
    }

    /**
     * 取得行程的所有景點
     * @param itnId 行程ID
     * @return 行程景點關聯列表
     */
    public List<com.toiukha.itinerary.model.ItnSpotVO> getSpotsByItineraryId(Integer itnId) {
        return itnSpotService.getSpotsByItnId(itnId);
    }

    /**
     * 取得行程的景點數量
     * @param itnId 行程ID
     * @return 景點數量
     */
    public Long getSpotCountByItineraryId(Integer itnId) {
        return itnSpotService.getSpotCountByItnId(itnId);
    }

    // ========== 7. 後台管理專用方法 ==========

    /**
     * 根據條件查詢行程（後台管理用）
     * @param keyword 搜尋關鍵字
     * @param status 狀態篩選
     * @param isPublic 公開狀態篩選
     * @param pageable 分頁參數
     * @return 符合條件的行程分頁結果
     */
    public Page<ItineraryVO> findItinerariesWithFilters(String keyword, Integer status, Integer isPublic, Pageable pageable) {
        return itineraryRepository.findWithFilters(keyword, status, isPublic, pageable);
    }


} 
