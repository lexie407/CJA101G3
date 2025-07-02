package com.toiukha.itinerary.service;

import com.toiukha.itinerary.model.ItnSpotVO;
import com.toiukha.itinerary.model.ItnSpotId;
import com.toiukha.itinerary.repository.ItnSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 行程景點關聯業務邏輯服務
 * 提供行程景點關聯相關的業務操作
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Service
public class ItnSpotService {

    @Autowired
    private ItnSpotRepository itnSpotRepository;

    // ========== 基本 CRUD 操作 ==========

    /**
     * 新增行程景點關聯
     * 
     * @param itnSpotVO 行程景點關聯資料
     * @return 儲存後的行程景點關聯資料
     */
    @Transactional
    public ItnSpotVO addItnSpot(ItnSpotVO itnSpotVO) {
        return itnSpotRepository.save(itnSpotVO);
    }

    /**
     * 更新行程景點關聯
     * 
     * @param itnSpotVO 行程景點關聯資料
     * @return 更新後的行程景點關聯資料
     */
    @Transactional
    public ItnSpotVO updateItnSpot(ItnSpotVO itnSpotVO) {
        return itnSpotRepository.save(itnSpotVO);
    }

    /**
     * 根據複合主鍵查詢行程景點關聯
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     * @return 行程景點關聯資料，不存在則返回null
     */
    public ItnSpotVO getItnSpotById(Integer itnId, Integer spotId) {
        ItnSpotId id = new ItnSpotId(itnId, spotId);
        Optional<ItnSpotVO> optional = itnSpotRepository.findById(id);
        return optional.orElse(null);
    }

    /**
     * 刪除行程景點關聯
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     */
    @Transactional
    public void deleteItnSpot(Integer itnId, Integer spotId) {
        ItnSpotId id = new ItnSpotId(itnId, spotId);
        itnSpotRepository.deleteById(id);
    }

    // ========== 行程景點管理 ==========

    /**
     * 根據行程ID查詢所有關聯的景點（按順序排列）
     * 
     * @param itnId 行程ID
     * @return 行程景點關聯列表
     */
    public List<ItnSpotVO> getSpotsByItnId(Integer itnId) {
        return itnSpotRepository.findByItnIdOrderBySeqAsc(itnId);
    }

    /**
     * 根據行程ID查詢所有關聯的景點ID（按順序排列）
     * 
     * @param itnId 行程ID
     * @return 景點ID列表
     */
    public List<Integer> getSpotIdsByItnId(Integer itnId) {
        return itnSpotRepository.findSpotIdsByItnIdOrderBySeqAsc(itnId);
    }

    /**
     * 為行程添加景點
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     * @return 新增的行程景點關聯資料
     */
    @Transactional
    public ItnSpotVO addSpotToItinerary(Integer itnId, Integer spotId) {
        // 檢查是否已存在
        if (itnSpotRepository.existsByItnIdAndSpotId(itnId, spotId)) {
            throw new IllegalArgumentException("景點已存在於行程中");
        }

        // 取得下一個順序號
        Integer nextSeq = getNextSeqForItinerary(itnId);
        
        ItnSpotVO itnSpot = new ItnSpotVO(itnId, spotId, nextSeq);
        return itnSpotRepository.save(itnSpot);
    }

    /**
     * 從行程中移除景點
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     */
    @Transactional
    public void removeSpotFromItinerary(Integer itnId, Integer spotId) {
        itnSpotRepository.deleteByItnIdAndSpotId(itnId, spotId);
        // 重新排序剩餘的景點
        reorderSpotsInItinerary(itnId);
    }

    /**
     * 清空行程中的所有景點
     * 
     * @param itnId 行程ID
     */
    @Transactional
    public void clearItinerarySpots(Integer itnId) {
        itnSpotRepository.deleteByItnId(itnId);
    }

    /**
     * 更新行程中景點的順序
     * 
     * @param itnId 行程ID
     * @param spotIds 新的景點ID順序列表
     */
    @Transactional
    public void updateSpotOrder(Integer itnId, List<Integer> spotIds) {
        // 先清空現有的關聯
        itnSpotRepository.deleteByItnId(itnId);
        
        // 重新建立關聯，按照新的順序
        for (int i = 0; i < spotIds.size(); i++) {
            ItnSpotVO itnSpot = new ItnSpotVO(itnId, spotIds.get(i), i + 1);
            itnSpotRepository.save(itnSpot);
        }
    }

    // ========== 查詢方法 ==========

    /**
     * 根據景點ID查詢所有關聯的行程
     * 
     * @param spotId 景點ID
     * @return 行程景點關聯列表
     */
    public List<ItnSpotVO> getItinerariesBySpotId(Integer spotId) {
        return itnSpotRepository.findBySpotId(spotId);
    }

    /**
     * 查詢行程中的景點數量
     * 
     * @param itnId 行程ID
     * @return 景點數量
     */
    public Long getSpotCountByItnId(Integer itnId) {
        return itnSpotRepository.countByItnId(itnId);
    }

    /**
     * 檢查行程是否包含特定景點
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     * @return true 如果行程包含該景點
     */
    public boolean isSpotInItinerary(Integer itnId, Integer spotId) {
        return itnSpotRepository.existsByItnIdAndSpotId(itnId, spotId);
    }

    // ========== 私有輔助方法 ==========

    /**
     * 取得行程的下一個順序號
     * 
     * @param itnId 行程ID
     * @return 下一個順序號
     */
    private Integer getNextSeqForItinerary(Integer itnId) {
        Integer maxSeq = itnSpotRepository.findMaxSeqByItnId(itnId);
        return maxSeq != null ? maxSeq + 1 : 1;
    }

    /**
     * 重新排序行程中的景點
     * 
     * @param itnId 行程ID
     */
    private void reorderSpotsInItinerary(Integer itnId) {
        List<ItnSpotVO> spots = itnSpotRepository.findByItnIdOrderBySeqAsc(itnId);
        
        for (int i = 0; i < spots.size(); i++) {
            ItnSpotVO spot = spots.get(i);
            spot.setSeq(i + 1);
            itnSpotRepository.save(spot);
        }
    }
} 