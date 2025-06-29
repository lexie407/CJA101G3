package com.toiukha.store.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository  extends JpaRepository<StoreVO, Integer> {
	
	boolean existsByStoreAcc(String storeAcc);    // 帳號是否已存在
	boolean existsByStoreGui(String storeGui);    // 統編是否已存在
	boolean existsByStoreEmail(String storeEmail); // 信箱是否已存在
	
	Optional<StoreVO> findByStoreAcc(String storeAcc);     // 登入用
	Optional<StoreVO> findByStoreEmail(String storeEmail); // 忘記密碼或驗證用
	
	List<StoreVO> findByStoreStatus(Byte storeStatus); //找未審核商家用
	
	
    List<StoreVO> findByStoreAccContaining(String storeAcc);    // 帳號模糊查詢
    List<StoreVO> findByStoreNameContaining(String storeName);  // 名稱模糊查詢
    

}
