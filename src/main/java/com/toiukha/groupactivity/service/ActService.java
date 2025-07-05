package com.toiukha.groupactivity.service;

import com.toiukha.groupactivity.model.ActCardDTO;
import com.toiukha.groupactivity.model.ActDTO;
import com.toiukha.groupactivity.model.ActTag;
import com.toiukha.groupactivity.model.ActVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服務層介面:新增或修改時會由 DTO 轉成 VO 操作資料庫
 */
public interface ActService {

    // ========== 行程相關方法 ==========

    /**
     * 驗證行程是否存在
     * @param itnId 行程ID
     * @return true 如果行程存在
     */
    boolean validateItinerary(Integer itnId);

    /**
     * 取得行程詳情
     * @param itnId 行程ID
     * @return 行程VO，不存在則返回null
     */
    com.toiukha.itinerary.model.ItineraryVO getItineraryById(Integer itnId);

    /**
     * 取得所有公開行程
     * @return 公開行程列表
     */
    List<com.toiukha.itinerary.model.ItineraryVO> getPublicItineraries();

    //新增活動
    void addAct(ActDTO actDto);

    //更新活動
    void updateAct(ActDTO actDto);
    void deleteAct(Integer actId);

    // 會員刪除活動（僅限未公開活動）
    void memDelete(Integer actId, Integer hostId);

    //查詢活動
    ActVO getOneAct(Integer actId);
    List<ActVO> getAll();
    List<ActVO> getByHost(Integer hostId);
    // ---未使用--- List<ActVO> getJoinedActs(Integer memId);
    List<ActCardDTO> getJoinedActsAsCard(Integer memId);

    // 查詢圖片（單獨處理）
    byte[] getActImageOnly(Integer actId);

    //===========分頁查詢===========
    // ---未使用--- 後台複合查詢（不分頁）
    // ---未使用--- List<ActVO> searchActsAll(Specification<ActVO> spec);

    // ---未使用--- 前台查詢（分頁）
    // ---未使用--- Page<ActVO> searchActs(Specification<ActVO> spec, Pageable pageable);
    // ---未使用--- Page<ActCardDTO> searchActsAsCard(Specification<ActVO> spec, Pageable pageable);

    //查詢已公開活動
    Page<ActCardDTO> searchPublicActs(Byte recruitStatus, String actName, 
                                     Integer hostId, LocalDateTime actStart, 
                                     Integer maxCap, Pageable pageable);

    //查詢已公開活動（含當前用戶參與狀態）
    Page<ActCardDTO> searchPublicActs(Byte recruitStatus, String actName, 
                                     Integer hostId, LocalDateTime actStart, 
                                     Integer maxCap, Integer currentUserId, 
                                     Pageable pageable);

    //===========活動狀態===========

    //取得活動狀態名稱
    String getRecruitStatusDisplayName(ActVO actVo);

    //取得活動狀態CSS類別
    String getRecruitStatusCssClass(ActVO actVo);

    //變更活動狀態
    void changeStatus(Integer actId, Byte status, Integer operatorId, boolean admin);

    //檢查活動狀態（招募中,成團,可報名,是否公開,是否允許退出）
    boolean isRecruiting(ActVO actVo);
    boolean isFull(ActVO actVo);
    boolean canSignUp(ActVO actVo);
    boolean isPublic(ActVO actVo);
    boolean allowCancel(ActVO actVo);

    //===========活動標籤(redis)===========

    //儲存活動標籤
    void saveActTags(Integer actId, ActTag typeTag, ActTag cityTag);

    //獲取活動標籤
    Map<String, ActTag> getActTags(Integer actId);

    //根據標籤搜尋活動
    Page<ActCardDTO> searchByTags(List<ActTag> typeTags, List<ActTag> cityTags, Pageable pageable);


    // ---未使用--- ===========測試寫入DB===========
    // ---未使用--- ActVO saveTestAct();

}
