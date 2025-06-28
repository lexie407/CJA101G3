package com.toiukha.item.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<ItemVO,Integer>{
	
	//由廠商編號查商品
    @Query(value ="from ItemVO where storeId = ?1")
    List<ItemVO> findByStoreId(int storeId);
    //已上架商品
    List<ItemVO> findByItemStatus(int itemStatus);
    
}
