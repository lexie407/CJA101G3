package com.toiukha.itinerary.repository;

import com.toiukha.itinerary.model.ItnSpotVO;
import com.toiukha.itinerary.model.ItnSpotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 行程景點關聯資料存取介面
 * 對應 itnspot 表格，使用複合主鍵
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Repository("itnSpotRepository")
public interface ItnSpotRepository extends JpaRepository<ItnSpotVO, ItnSpotId> {

    /**
     * 根據行程ID查詢所有關聯的景點（按順序排列）
     * 
     * @param itnId 行程ID
     * @return 行程景點關聯列表
     */
    List<ItnSpotVO> findByItnIdOrderBySeqAsc(Integer itnId);

    /**
     * 根據行程ID查詢所有關聯的景點ID（按順序排列）
     * 
     * @param itnId 行程ID
     * @return 景點ID列表
     */
    @Query("SELECT is.spotId FROM ItnSpotVO is WHERE is.itnId = :itnId ORDER BY is.seq ASC")
    List<Integer> findSpotIdsByItnIdOrderBySeqAsc(@Param("itnId") Integer itnId);

    /**
     * 根據景點ID查詢所有關聯的行程
     * 
     * @param spotId 景點ID
     * @return 行程景點關聯列表
     */
    List<ItnSpotVO> findBySpotId(Integer spotId);

    /**
     * 根據行程ID查詢景點數量
     * 
     * @param itnId 行程ID
     * @return 景點數量
     */
    Long countByItnId(Integer itnId);

    /**
     * 根據行程ID刪除所有關聯的景點
     * 
     * @param itnId 行程ID
     */
    void deleteByItnId(Integer itnId);

    /**
     * 根據行程ID和景點ID刪除特定關聯
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     */
    void deleteByItnIdAndSpotId(Integer itnId, Integer spotId);

    /**
     * 檢查行程是否包含特定景點
     * 
     * @param itnId 行程ID
     * @param spotId 景點ID
     * @return true 如果行程包含該景點
     */
    boolean existsByItnIdAndSpotId(Integer itnId, Integer spotId);

    /**
     * 取得行程中景點的最大順序號
     * 
     * @param itnId 行程ID
     * @return 最大順序號，如果沒有景點則返回 null
     */
    @Query("SELECT MAX(is.seq) FROM ItnSpotVO is WHERE is.itnId = :itnId")
    Integer findMaxSeqByItnId(@Param("itnId") Integer itnId);
} 