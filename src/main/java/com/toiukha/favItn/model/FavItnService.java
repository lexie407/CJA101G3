package com.toiukha.favItn.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavItnService {

    @Autowired
    private FavItnRepository favItnRepository;

    public List<FavItnVO> findAll() {
        return favItnRepository.findAll();
    }

    public Optional<FavItnVO> findById(FavItnId favItnId) {
        return favItnRepository.findById(favItnId);
    }

    public FavItnVO save(FavItnVO favItnVO) {
        return favItnRepository.save(favItnVO);
    }

    public void deleteById(FavItnId favItnId) {
        favItnRepository.deleteById(favItnId);
    }

    /**
     * 查詢會員收藏的行程ID列表
     */
    public List<Integer> findFavoriteIdsByMemId(Integer memId) {
        return favItnRepository.findByMemId(memId)
                .stream()
                .map(fav -> fav.getId().getFavItnId())
                .collect(Collectors.toList());
    }

    /**
     * 切換收藏狀態（使用原生SQL，線程安全）
     * @param favItnId 收藏ID
     * @return true表示已收藏，false表示已取消收藏
     */
    @Transactional
    public synchronized boolean toggleFavorite(FavItnId favItnId) {
        Integer itnId = favItnId.getFavItnId();
        Integer memId = favItnId.getMemId();
        
        // 檢查當前狀態
        int count = favItnRepository.countByItnIdAndMemId(itnId, memId);
        
        if (count > 0) {
            // 已收藏，刪除收藏
            favItnRepository.deleteByItnIdAndMemId(itnId, memId);
            return false; // 已取消收藏
        } else {
            // 未收藏，插入收藏（使用 INSERT IGNORE 避免重複主鍵錯誤）
            favItnRepository.insertIgnore(itnId, memId);
            return true; // 已加入收藏
        }
    }
    
    /**
     * 檢查是否已收藏（使用原生SQL）
     * @param favItnId 收藏ID
     * @return true表示已收藏，false表示未收藏
     */
    public boolean isFavorited(FavItnId favItnId) {
        return favItnRepository.countByItnIdAndMemId(favItnId.getFavItnId(), favItnId.getMemId()) > 0;
    }
}
