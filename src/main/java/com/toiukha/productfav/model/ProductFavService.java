package com.toiukha.productfav.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductFav Service 類
 * 提供商品收藏的業務邏輯方法
 */
@Service
@Transactional
public class ProductFavService {
    
    @Autowired
    private ProductFavRepository productFavRepository;
    
    /**
     * 新增商品收藏
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return 收藏記錄
     */
    public ProductFavVO addFavorite(Integer memId, Integer itemId) {
        // 檢查是否已經收藏過
        if (isFavorite(memId, itemId)) {
            throw new RuntimeException("商品已經在收藏清單中");
        }
        
        ProductFavVO favorite = new ProductFavVO();
        favorite.setMemId(memId);
        favorite.setItemId(itemId);
        favorite.setFavAt(LocalDateTime.now());
        
        return productFavRepository.save(favorite);
    }
    
    /**
     * 移除商品收藏
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return true 如果成功移除，false 如果收藏不存在
     */
    public boolean removeFavorite(Integer memId, Integer itemId) {
        ProductFavId id = new ProductFavId(memId, itemId);
        if (productFavRepository.existsById(id)) {
            productFavRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * 切換收藏狀態（如果已收藏則移除，如果未收藏則新增）
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return true 如果新增收藏，false 如果移除收藏
     */
    public boolean toggleFavorite(Integer memId, Integer itemId) {
        if (isFavorite(memId, itemId)) {
            removeFavorite(memId, itemId);
            return false;
        } else {
            addFavorite(memId, itemId);
            return true;
        }
    }
    
    /**
     * 檢查是否已收藏
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return true 如果已收藏，false 如果未收藏
     */
    public boolean isFavorite(Integer memId, Integer itemId) {
        return productFavRepository.existsByMemIdAndItemId(memId, itemId);
    }
    
    /**
     * 獲取會員的所有收藏
     * @param memId 會員ID
     * @return 收藏列表
     */
    public List<ProductFavVO> getMemberFavorites(Integer memId) {
        return productFavRepository.findByMemIdOrderByFavAtDesc(memId);
    }
    

    
    /**
     * 獲取收藏某商品的所有會員
     * @param itemId 商品ID
     * @return 收藏記錄列表
     */
    public List<ProductFavVO> getItemFavorites(Integer itemId) {
        return productFavRepository.findByItemId(itemId);
    }
    
    /**
     * 統計會員的收藏總數
     * @param memId 會員ID
     * @return 收藏總數
     */
    public long getMemberFavoriteCount(Integer memId) {
        return productFavRepository.countByMemId(memId);
    }
    
    /**
     * 統計商品被收藏的次數
     * @param itemId 商品ID
     * @return 被收藏次數
     */
    public long getItemFavoriteCount(Integer itemId) {
        return productFavRepository.countByItemId(itemId);
    }
    
    /**
     * 獲取特定收藏記錄
     * @param memId 會員ID
     * @param itemId 商品ID
     * @return 收藏記錄（如果存在）
     */
    public Optional<ProductFavVO> getFavorite(Integer memId, Integer itemId) {
        return productFavRepository.findByMemIdAndItemId(memId, itemId);
    }
    
    /**
     * 獲取所有收藏記錄
     * @return 所有收藏記錄列表
     */
    public List<ProductFavVO> getAllFavorites() {
        return productFavRepository.findAll();
    }
    
    /**
     * 獲取熱門收藏商品
     * @return 熱門商品統計列表 [商品ID, 收藏次數]
     */
    public List<Object[]> getPopularItems() {
        return productFavRepository.findPopularItems();
    }
    

} 