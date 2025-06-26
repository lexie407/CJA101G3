package com.toiukha.groupactivity.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服務層介面:新增或修改時會由 DTO 轉成 VO 操作資料庫
 */
public interface ActService {

    //新增活動
    void addAct(ActDTO actDto);

    //更新活動
    void updateAct(ActDTO actDto);
    void deleteAct(Integer actId);

    //查詢活動
    ActVO getOneAct(Integer actId);
    List<ActVO> getAll();
    List<ActVO> getByHost(Integer hostId);
    
    //查詢會員參加的活動
    List<ActVO> getJoinedActs(Integer memId);
    List<ActCardDTO> getJoinedActsAsCard(Integer memId);

    //===========複雜方法===========
    //以關鍵字搜尋活動名稱
    //List<ActVO> searchByName(String keyword);
    //->改成
    //List<ActVO> searchActs(String keyword, LocalDate startDate, LocalDate endDate, Integer recruitStatus);
    Page<ActVO> searchActs(Specification<ActVO> spec, Pageable pageable);
    
    // [優化] 回傳不含圖片的 DTO
    Page<ActCardDTO> searchActsAsCard(Specification<ActVO> spec, Pageable pageable);
    
    // [商業邏輯] 搜尋公開活動：強制 isPublic=1，防止前端篡改
    Page<ActCardDTO> searchPublicActs(Byte recruitStatus, String actName, 
                                     Integer hostId, LocalDateTime actStart, 
                                     Integer maxCap, Pageable pageable);

    //不分頁搜尋
    List<ActVO> searchActsAll(Specification<ActVO> spec);

    //變更活動狀態
    void changeStatus(Integer actId, Byte status, Integer operatorId, boolean admin);


    //===========測試寫入DB===========
    ActVO saveTestAct();

}
