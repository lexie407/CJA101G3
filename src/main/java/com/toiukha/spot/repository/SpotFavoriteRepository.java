package com.toiukha.spot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.toiukha.spot.model.SpotFavoriteVO;
import com.toiukha.spot.model.SpotFavoriteId;

/**
 * 景點收藏資料存取介面
 * 對應 favspot 表格，使用複合主鍵
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Repository
public interface SpotFavoriteRepository extends JpaRepository<SpotFavoriteVO, SpotFavoriteId> {

    /**
     * 查詢會員是否已收藏指定景點
     * 
     * @param memId 會員ID
     * @param favSpotId 景點ID
     * @return 收藏記錄
     */
    Optional<SpotFavoriteVO> findByMemIdAndFavSpotId(Integer memId, Integer favSpotId);

    /**
     * 查詢會員的所有收藏景點
     * 
     * @param memId 會員ID
     * @return 收藏列表
     */
    List<SpotFavoriteVO> findByMemIdOrderByFavCreateAtDesc(Integer memId);

    /**
     * 查詢景點的收藏數量
     * 
     * @param favSpotId 景點ID
     * @return 收藏數量
     */
    Long countByFavSpotId(Integer favSpotId);

    /**
     * 查詢會員收藏景點總數
     * 
     * @param memId 會員ID
     * @return 收藏總數
     */
    Long countByMemId(Integer memId);

    /**
     * 刪除會員對指定景點的收藏
     * 
     * @param memId 會員ID
     * @param favSpotId 景點ID
     */
    void deleteByMemIdAndFavSpotId(Integer memId, Integer favSpotId);

    /**
     * 查詢會員收藏的景點ID列表
     * 
     * @param memId 會員ID
     * @return 景點ID列表
     */
    @Query("SELECT sf.favSpotId FROM SpotFavoriteVO sf WHERE sf.memId = :memId")
    List<Integer> findSpotIdsByMemId(@Param("memId") Integer memId);

    /**
     * 查詢會員收藏景點詳細資訊（含景點資料）
     * 
     * @param memId 會員ID
     * @return 收藏列表（含景點資料）
     */
    @Query("SELECT sf FROM SpotFavoriteVO sf " +
           "JOIN FETCH sf.spot s " +
           "WHERE sf.memId = :memId " +
           "AND s.spotStatus = 1 " +
           "ORDER BY sf.favCreateAt DESC")
    List<SpotFavoriteVO> findFavoriteSpotsByMemId(@Param("memId") Integer memId);

    /**
     * 檢查會員是否已收藏指定景點
     * 
     * @param memId 會員ID
     * @param favSpotId 景點ID
     * @return 是否已收藏
     */
    boolean existsByMemIdAndFavSpotId(Integer memId, Integer favSpotId);

    /**
     * 查詢景點的收藏者ID列表
     * 
     * @param favSpotId 景點ID
     * @return 會員ID列表
     */
    @Query("SELECT sf.memId FROM SpotFavoriteVO sf WHERE sf.favSpotId = :favSpotId")
    List<Integer> findMemIdsBySpotId(@Param("favSpotId") Integer favSpotId);

    // ========== 為了向後相容而保留的方法 ==========

    /**
     * 查詢會員是否已收藏指定景點 (向後相容方法)
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @return 收藏記錄
     */
    default Optional<SpotFavoriteVO> findByMemIdAndSpotId(Integer memId, Integer spotId) {
        return findByMemIdAndFavSpotId(memId, spotId);
    }

    /**
     * 查詢景點的收藏數量 (向後相容方法)
     * 
     * @param spotId 景點ID
     * @return 收藏數量
     */
    default Long countBySpotId(Integer spotId) {
        return countByFavSpotId(spotId);
    }

    /**
     * 刪除會員對指定景點的收藏 (向後相容方法)
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     */
    default void deleteByMemIdAndSpotId(Integer memId, Integer spotId) {
        deleteByMemIdAndFavSpotId(memId, spotId);
    }

    /**
     * 檢查會員是否已收藏指定景點 (向後相容方法)
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @return 是否已收藏
     */
    default boolean existsByMemIdAndSpotId(Integer memId, Integer spotId) {
        return existsByMemIdAndFavSpotId(memId, spotId);
    }
    
    /**
     * 刪除會員對指定景點的收藏，並返回刪除的記錄數
     * 
     * @param memId 會員ID
     * @param spotId 景點ID
     * @return 刪除的記錄數
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SpotFavoriteVO sf WHERE sf.memId = :memId AND sf.favSpotId = :spotId")
    int deleteByMemIdAndSpotIdReturnCount(@Param("memId") Integer memId, @Param("spotId") Integer spotId);
    
    /**
     * 使用原生SQL查詢會員是否已收藏指定景點
     * 
     * @param spotId 景點ID
     * @param memId 會員ID
     * @return 收藏記錄數量
     */
    @Query(value = "SELECT COUNT(*) FROM favspot WHERE favspotid = :spotId AND memid = :memId", nativeQuery = true)
    int countBySpotIdAndMemId(@Param("spotId") Integer spotId, @Param("memId") Integer memId);
    
    /**
     * 使用INSERT IGNORE插入收藏記錄，避免主鍵衝突
     * 
     * @param spotId 景點ID
     * @param memId 會員ID
     * @return 影響的記錄數
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO favspot (favspotid, memid, favcreatedat) VALUES (:spotId, :memId, NOW())", nativeQuery = true)
    int insertIgnore(@Param("spotId") Integer spotId, @Param("memId") Integer memId);
    
    /**
     * 使用原生SQL刪除收藏記錄
     * 
     * @param spotId 景點ID
     * @param memId 會員ID
     * @return 影響的記錄數
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM favspot WHERE favspotid = :spotId AND memid = :memId", nativeQuery = true)
    int deleteBySpotIdAndMemIdNative(@Param("spotId") Integer spotId, @Param("memId") Integer memId);
} 