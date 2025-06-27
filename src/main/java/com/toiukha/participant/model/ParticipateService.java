package com.toiukha.participant.model;

import java.util.List;

/**
 * 參加者服務介面
 */
public interface ParticipateService {

    /**
     * 報名活動
     * @param actId 活動ID
     * @param memId 會員ID
     */
    void signup(Integer actId, Integer memId);

    /**
     * 取消報名
     * @param actId 活動ID
     * @param memId 會員ID
     */
    void cancel(Integer actId, Integer memId);

    /**
     * 取得活動的所有參加者會員ID
     * @param actId 活動ID
     * @return 參加者會員ID列表
     */
    List<Integer> getParticipants(Integer actId);
    
    /**
     * 取得活動的所有參加者詳細資料（DTO格式，避免序列化問題）
     * @param actId 活動ID
     * @return 參加者 DTO 列表
     */
    List<ParticipantDTO> getParticipantsAsDTO(Integer actId);
    
    /**
     * 取得特定參加記錄（DTO格式）
     * @param actId 活動ID
     * @param memId 會員ID
     * @return 參加者 DTO，若不存在則回傳 null
     */
    ParticipantDTO getParticipantAsDTO(Integer actId, Integer memId);
    
    /**
     * 取得會員參加的所有活動ID
     * @param memId 會員ID
     * @return 活動ID列表
     */
    List<Integer> getJoinedActivities(Integer memId);
    
    /**
     * 取得會員的所有參加記錄（DTO格式）
     * @param memId 會員ID
     * @return 參加記錄 DTO 列表
     */
    List<ParticipantDTO> getJoinedActivitiesAsDTO(Integer memId);

    /**
     * 團主更新成員狀態
     */
    void updateJoinStatus(Integer actId, Integer memId, Byte joinStatus);
}
