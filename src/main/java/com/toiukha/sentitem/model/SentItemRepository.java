package com.toiukha.sentitem.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SentItemRepository extends JpaRepository<SentItemVO,Integer>{

//	List<SentItemVO> findBysentItemId(int sentItemId);

	List<SentItemVO> findByMemId(Integer memId);

	List<SentItemVO> findByStoreId(Integer storeId);
}
