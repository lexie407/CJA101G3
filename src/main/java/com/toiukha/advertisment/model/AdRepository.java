package com.toiukha.advertisment.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.*;

public interface AdRepository extends JpaRepository<AdVO, Integer>{
	
	//  (自訂) 刪除一筆廣告資料 By ADID (使用原生SQL)
	// 透過廣告ID刪除一筆廣告資料
	@Transactional
	@Modifying
	@Query(value = "delete from advertisment where adId =?1", nativeQuery = true)	
								// 原生SQL語法。 ?1 代表方法參數中的第一個參數 (adId)。
	void deleteByAdId(int adId);
	
	// 根據 ADTITLE、ADSTATUS 查詢，或根據時間範圍查詢
	// 假設你可能需要根據標題和狀態來查詢廣告
	@Query(value = "from AdVO where adTitle like %?1% and adStatus =?2 order by adId desc")
	List<AdVO> findByTitleContainingAndStatus(String adTitle, Byte adStatus);
	
	

	    // JPA 會根據方法名稱自動實作 SQL：SELECT * FROM advertisment WHERE storeId = ?
	    List<AdVO> findByStoreId(Integer storeId);
	    
	    // 根據狀態查詢廣告
	    List<AdVO> findByAdStatus(Byte adStatus);
	    
	    // 根據狀態排序查詢廣告
	    List<AdVO> findByAdStatusOrderByAdCreatedTimeDesc(Byte adStatus);
	}

	
	// 有需要自訂查詢再往下補
