package com.toiukha.advertisment.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adService")
public class AdService {
	
	@Autowired
	AdRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Transactional
	public void addAd(AdVO adVO) {
		repository.save(adVO);
	}
	@Transactional
	public void updateAd(AdVO adVO) {
		repository.save(adVO);
	}
	@Transactional
	public void deleteAd(Integer adId) {
		if(repository.existsById(adId)) {
//			repository.deleteByAdId(adId);
			repository.deleteById(adId);
		}
	}
	
	public AdVO getOneAd(Integer adId) {
		Optional<AdVO> optional = repository.findById(adId);	 // 根據 ID 查詢單一廣告
//		return optional.get();
		return optional.orElse(null);	// public T orElse(T other)如果值存在就回傳AdVO，否則回傳other的值
	}
	
	public List<AdVO> getAll(){
		return repository.findAll();	// 取得所有廣告
	}
	
//	// 針對 AdVO 的複合查詢工具。
//	public List<AdVO> getAll(Map<String, String[]> map){
////		return HibernateUtil_CompositeQuery_Ad.getAllC(map,sessionFactory.openSession());
//		return null;	// 暫時回傳 null，待實現複合查詢工具
//	}

    // 根據標題和狀態查詢廣告 (對應AdRepository 的自訂方法)
    public List<AdVO> getAdsByTitleAndStatus(String adTitle, Byte adStatus) {
        return repository.findByTitleContainingAndStatus(adTitle, adStatus);
    }
    @Autowired
    private AdRepository adRepo;

    public List<AdVO> getAllByStoreId(Integer storeId) {
        return adRepo.findByStoreId(storeId);
    }

    // 審核相關方法
    public List<AdVO> getPendingAds() {
        return adRepo.findByAdStatus(AdVO.STATUS_PENDING);
    }
    
    public List<AdVO> getApprovedAds() {
        return adRepo.findActiveAdsByStatus(AdVO.STATUS_APPROVED);
    }
    
    public List<AdVO> getRejectedAds() {
        return adRepo.findByAdStatus(AdVO.STATUS_REJECTED);
    }
    
    @Transactional
    public void approveAd(Integer adId) {
        updateAdStatus(adId, AdVO.STATUS_APPROVED);
    }
    
    @Transactional
    public void rejectAd(Integer adId) {
        updateAdStatus(adId, AdVO.STATUS_REJECTED);
    }
    
    @Transactional
    public void deactivateAd(Integer adId) {
        updateAdStatus(adId, AdVO.STATUS_INACTIVE);
    }
    
    /**
     * 專門用於更新廣告狀態的方法
     * 這個方法會直接更新狀態而不進行完整的Bean Validation
     */
    @Transactional
    public void updateAdStatus(Integer adId, Byte newStatus) {
        AdVO ad = getOneAd(adId);
        if (ad != null) {
            ad.setAdStatus(newStatus);
            try {
                repository.save(ad);
            } catch (Exception e) {
                throw new RuntimeException("更新廣告狀態失敗 ID: " + adId + ", 新狀態: " + newStatus, e);
            }
        } else {
            throw new RuntimeException("找不到廣告 ID: " + adId);
        }
    }

	
    public List<AdVO> getTodayAdList(){
    	return repository.getTodayAds(Timestamp.from(Instant.now()));
    }
	
	
	

}
