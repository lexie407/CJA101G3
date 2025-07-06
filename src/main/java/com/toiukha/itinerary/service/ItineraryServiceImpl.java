package com.toiukha.itinerary.service;

import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.model.ItnSpotVO;
import com.toiukha.itinerary.repository.ItineraryRepository;
import com.toiukha.itinerary.repository.ItnSpotRepository;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ItineraryServiceImpl implements ItineraryService {

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private ItnSpotRepository itnSpotRepository;
    
    @Autowired
    private SpotRepository spotRepository;

    @Override
    public List<ItineraryVO> getAllItineraries() {
        return itineraryRepository.findAll();
    }
    
    @Override
    public Page<ItineraryVO> getAllItineraries(Pageable pageable) {
        return itineraryRepository.findAll(pageable);
    }

    @Override
    public ItineraryVO getItineraryById(Integer itnId) {
        return itineraryRepository.findById(itnId).orElse(null);
    }
    
    @Override
    public ItineraryVO getItineraryWithSpots(Integer itnId) {
        System.out.println("=== getItineraryWithSpots 調試 ===");
        System.out.println("查詢行程ID: " + itnId);
        
        Optional<ItineraryVO> result = itineraryRepository.findByIdWithSpots(itnId);
        if (result.isPresent()) {
            ItineraryVO itinerary = result.get();
            System.out.println("找到行程: " + itinerary.getItnName());
            System.out.println("景點列表是否為空: " + (itinerary.getItnSpots() == null ? "null" : itinerary.getItnSpots().isEmpty()));
            if (itinerary.getItnSpots() != null) {
                System.out.println("景點數量: " + itinerary.getItnSpots().size());
                for (int i = 0; i < itinerary.getItnSpots().size(); i++) {
                    var itnSpot = itinerary.getItnSpots().get(i);
                    System.out.println("  景點 " + (i + 1) + ": " + 
                        (itnSpot.getSpot() != null ? itnSpot.getSpot().getSpotName() : "null"));
                }
            }
            System.out.println("=== getItineraryWithSpots 完成 ===");
            return itinerary;
        } else {
            System.out.println("未找到行程ID: " + itnId);
            System.out.println("=== getItineraryWithSpots 完成 ===");
            return null;
        }
    }

    @Override
    @Transactional
    public ItineraryVO addItinerary(ItineraryVO itinerary) {
        return itineraryRepository.save(itinerary);
    }

    @Override
    @Transactional
    public ItineraryVO updateItinerary(ItineraryVO itinerary) {
        return itineraryRepository.save(itinerary);
    }

    @Override
    @Transactional
    public void deleteItinerary(Integer itnId) {
        itineraryRepository.deleteById(itnId);
    }

    @Override
    @Transactional
    public com.toiukha.itinerary.model.ItnSpotVO addSpotToItinerary(Integer itnId, Integer spotId) {
        System.out.println("=== addSpotToItinerary 調試 ===");
        System.out.println("行程ID: " + itnId + ", 景點ID: " + spotId);
        
        ItineraryVO itinerary = itineraryRepository.findById(itnId)
                .orElseThrow(() -> new RuntimeException("Itinerary not found with id: " + itnId));
        
        // 使用 SpotVO 而不是 Spot
        SpotVO spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found with id: " + spotId));
        
        ItnSpotVO itnSpot = new ItnSpotVO();
        // 設定複合主鍵的屬性
        itnSpot.setItnId(itnId);
        itnSpot.setSpotId(spotId);
        // 設定關聯物件
        itnSpot.setItinerary(itinerary);
        itnSpot.setSpot(spot);
        
        Integer maxSeq = itnSpotRepository.findMaxSeqByItnId(itnId);
        itnSpot.setSeq(maxSeq == null ? 1 : maxSeq + 1);
        
        System.out.println("準備保存 ItnSpotVO: itnId=" + itnSpot.getItnId() + ", spotId=" + itnSpot.getSpotId() + ", seq=" + itnSpot.getSeq());
        
        ItnSpotVO saved = itnSpotRepository.save(itnSpot);
        System.out.println("成功保存景點關聯");
        System.out.println("=== addSpotToItinerary 完成 ===");
        
        return saved;
    }

    @Override
    @Transactional
    public int addSpotsToItinerary(Integer itnId, List<Integer> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (Integer spotId : spotIds) {
            try {
                addSpotToItinerary(itnId, spotId);
                count++;
            } catch (Exception e) {
                // 記錄錯誤但繼續處理
            }
        }
        return count;
    }
    
    @Override
    @Transactional
    public void updateSpotsForItinerary(Integer itnId, List<Integer> spotIds) {
        System.out.println("=== updateSpotsForItinerary 調試 ===");
        System.out.println("行程ID: " + itnId);
        System.out.println("景點IDs: " + spotIds);
        
        // 1. 刪除現有行程的所有景點關聯
        long deletedCount = itnSpotRepository.countByItnId(itnId);
        System.out.println("刪除前景點數量: " + deletedCount);
        itnSpotRepository.deleteByItnId(itnId);
        System.out.println("已刪除現有景點關聯");

        // 2. 重新添加新的景點關聯
        if (spotIds != null && !spotIds.isEmpty()) {
            System.out.println("開始添加 " + spotIds.size() + " 個景點");
            int addedCount = addSpotsToItinerary(itnId, spotIds);
            System.out.println("成功添加 " + addedCount + " 個景點");
        } else {
            System.out.println("沒有景點需要添加");
        }
        System.out.println("=== updateSpotsForItinerary 完成 ===");
    }
    
    // 其他業務邏輯方法...
    @Override
    public List<ItineraryVO> getPublicItineraries() {
        return itineraryRepository.findPublicItineraries();
    }
    
    @Override
    public Page<ItineraryVO> getPublicItineraries(Pageable pageable) {
        return itineraryRepository.findPublicItineraries(pageable);
    }

    @Override
    public List<ItineraryVO> getItinerariesByCrtId(Integer crtId) {
        return itineraryRepository.findByCrtId(crtId);
    }
    
    @Override
    public Page<ItineraryVO> getItinerariesByCrtId(Integer crtId, Pageable pageable) {
        return itineraryRepository.findByCrtId(crtId, pageable);
    }
    
    @Override
    public List<ItineraryVO> searchPublicItineraries(String keyword) {
        return itineraryRepository.searchPublicItineraries(keyword);
    }
    
    @Override
    public Page<ItineraryVO> searchPublicItineraries(String keyword, Pageable pageable) {
        return itineraryRepository.searchPublicItineraries(keyword, pageable);
    }
    
    @Override
    public boolean isItineraryNameExists(String itnName) {
        return itineraryRepository.existsByItnName(itnName);
    }
    
    @Override
    public boolean isItineraryExists(Integer itnId) {
        return itineraryRepository.existsById(itnId);
    }
    
    @Override
    public Page<ItineraryVO> findItinerariesWithFilters(String keyword, Integer status, Integer isPublic, Pageable pageable) {
        return itineraryRepository.findWithFilters(keyword, status, isPublic, pageable);
    }

    @Override
    public List<ItineraryVO> getPublicItinerariesNotFromActivity() {
        return itineraryRepository.findPublicItinerariesNotFromActivity();
    }

    @Override
    public Page<ItineraryVO> getPublicItinerariesNotFromActivity(Pageable pageable) {
        return itineraryRepository.findPublicItinerariesNotFromActivity(pageable);
    }

    @Override
    public List<ItineraryVO> getPublicItinerariesByCrtId(Integer crtId) {
        return itineraryRepository.findPublicItinerariesByCrtId(crtId);
    }

    @Override
    public List<ItineraryVO> getPrivateItinerariesByCrtId(Integer crtId) {
        return itineraryRepository.findPrivateItinerariesByCrtId(crtId);
    }

    @Override
    @Transactional
    public void removeSpotFromItinerary(Integer itnId, Integer spotId) {
        itnSpotRepository.deleteByItnIdAndSpotId(itnId, spotId);
    }

    @Override
    public List<ItnSpotVO> getSpotsByItineraryId(Integer itnId) {
        return itnSpotRepository.findByItnIdOrderBySeqAsc(itnId);
    }

    @Override
    public Long getSpotCountByItineraryId(Integer itnId) {
        return itnSpotRepository.countByItnId(itnId);
    }

    @Override
    @Transactional
    public void clearItinerarySpots(Integer itnId) {
        itnSpotRepository.deleteByItnId(itnId);
    }

    @Override
    public long getItineraryCountByCrtId(Integer crtId) {
        return itineraryRepository.countByCrtId(crtId);
    }

    @Override
    public long getPublicItineraryCount() {
        return itineraryRepository.countPublicItineraries();
    }

    @Override
    public long getPrivateItineraryCount() {
        return itineraryRepository.countPrivateItineraries();
    }

    @Override
    public long getTotalItineraryCount() {
        return itineraryRepository.count();
    }

    @Override
    public List<ItineraryVO> searchPublicItinerariesNotFromActivity(String keyword) {
        return itineraryRepository.searchPublicItinerariesNotFromActivity(keyword);
    }

    @Override
    public Page<ItineraryVO> searchPublicItinerariesNotFromActivity(String keyword, Pageable pageable) {
        return itineraryRepository.searchPublicItinerariesNotFromActivity(keyword, pageable);
    }

    @Override
    public List<ItineraryVO> searchItineraries(String keyword, Integer isPublic) {
        if (isPublic != null) {
            return itineraryRepository.findByItnNameContainingAndIsPublic(keyword, isPublic.byteValue());
        } else {
            return itineraryRepository.findByItnNameContaining(keyword);
        }
    }

    @Override
    public List<ItineraryVO> getAllPublicItineraries() {
        return itineraryRepository.findByIsPublic((byte) 1);
    }

    @Override
    public List<ItineraryVO> findFavoriteItinerariesByMemId(Integer memId) {
        // 通過收藏關聯表查詢收藏的行程
        return itineraryRepository.findFavoriteItinerariesByMemId(memId);
    }

    @Override
    public List<ItineraryVO> searchItinerariesByName(String itnName) {
        return itineraryRepository.findByItnNameContaining(itnName);
    }

    @Override
    public List<ItineraryVO> getItinerariesByPublicStatus(Integer isPublic) {
        return itineraryRepository.findByIsPublic(isPublic.byteValue());
    }
} 