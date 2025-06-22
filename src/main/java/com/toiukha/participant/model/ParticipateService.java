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
}
