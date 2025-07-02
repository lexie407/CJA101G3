package com.toiukha.productfav.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ProductFav Repository 接口
 * 提供商品收藏的資料存取方法
 */
@Repository
public interface ProductFavRepository extends JpaRepository<ProductFavVO, ProductFavId> {
    
    /**
     * 根據會員ID查詢該會員的所有收藏
     * @param memId 會員ID
     * @return 該會員的收藏列表
     */
    List<ProductFavVO> findByMemId(Integer memId);
    
    /**
     * 根據商品ID查詢收藏該商品的所有會員
     * @param itemId 商品ID
     * @return 收藏該商品的記錄列表
     */
    List<ProductFavVO> findByItemId(Integer itemId);
    
    /**
     * 檢查特定會員是否收藏了特定商品
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return 收藏記錄（如果存在）
     */
    Optional<ProductFavVO> findByMemIdAndItemId(Integer memId, Integer itemId);
    
    /**
     * 檢查特定會員是否收藏了特定商品（返回布林值）
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return true 如果已收藏，false 如果未收藏
     */
    boolean existsByMemIdAndItemId(Integer memId, Integer itemId);
    
    /**
     * 統計某個會員的收藏總數
     * @param memId 會員ID
     * @return 收藏總數
     */
    long countByMemId(Integer memId);
    
    /**
     * 統計某個商品被收藏的次數
     * @param itemId 商品ID
     * @return 被收藏次數
     */
    long countByItemId(Integer itemId);
    
    /**
     * 根據會員ID和收藏時間排序查詢收藏列表
     * @param memId 會員ID
     * @return 按收藏時間倒序排列的收藏列表
     */
    List<ProductFavVO> findByMemIdOrderByFavAtDesc(Integer memId);
    
    /**
     * 查詢會員的收藏列表，包含商品資訊
     * @param memId 會員ID
     * @return 包含商品資訊的收藏列表
     */
    @Query("SELECT pf FROM ProductFavVO pf JOIN FETCH pf.item WHERE pf.memId = :memId ORDER BY pf.favAt DESC")
    List<ProductFavVO> findMemberFavoritesWithItems(@Param("memId") Integer memId);
    
    /**
     * 查詢熱門收藏商品（被收藏次數最多的商品）
     * @param limit 限制返回數量
     * @return 熱門收藏商品列表
     */
    @Query("SELECT pf.itemId, COUNT(pf) as favCount FROM ProductFavVO pf GROUP BY pf.itemId ORDER BY favCount DESC")
    List<Object[]> findPopularItems();
} 