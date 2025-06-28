package com.toiukha.memcoupons.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemCouponsRepository extends JpaRepository<MemCouponsVO, MemCouponsVO.CompositeDetail> {
    
	public static class CompositeDetail implements Serializable {
        // ... 你的內容 ...
    }
	@Query("SELECT m FROM MemCouponsVO m LEFT JOIN FETCH m.coupon WHERE m.memId = :memId")
	List<MemCouponsVO> findByMemIdWithCoupon(@Param("memId") Integer memId);
}
