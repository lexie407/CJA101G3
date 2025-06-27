package com.toiukha.coupon.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.toiukha.item.model.ItemVO;

public interface CouponRepository extends JpaRepository<CouponVO,Integer>{
	List<CouponVO> findByStoreId(int storeId);
}
