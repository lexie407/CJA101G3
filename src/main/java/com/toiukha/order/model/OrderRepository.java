package com.toiukha.order.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;



public interface OrderRepository extends JpaRepository<OrderVO, Integer>{
	
	@Transactional
	@Modifying
	@Query(value = "delete from orders where ORDID =?1", nativeQuery = true)
	void deleteByEmpno(int ordid);
	List<OrderVO> findByMemId(int memId);
	
	// 查詢會員的已完成訂單 (訂單狀態 = 1)
	List<OrderVO> findByMemIdAndOrdSta(int memId, int ordSta);
}
