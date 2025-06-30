package com.toiukha.itnReport.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ItnRptRepository extends JpaRepository<ItnRptVO, Integer>{
	
	/**
	 * 根據會員ID查詢檢舉記錄
	 * @param memId 會員ID
	 * @return 該會員的檢舉記錄列表
	 */
	@Query("FROM ItnRptVO WHERE memId = :memId ORDER BY repAt DESC")
	List<ItnRptVO> findByMemId(@Param("memId") Integer memId);
	
	/**
	 * 根據商品ID查詢檢舉記錄
	 * @param itemId 商品ID
	 * @return 該商品的檢舉記錄列表
	 */
	@Query("FROM ItnRptVO WHERE itemId = :itemId ORDER BY repAt DESC")
	List<ItnRptVO> findByItemId(@Param("itemId") Integer itemId);
	
	/**
	 * 根據檢舉狀態查詢檢舉記錄
	 * @param repStatus 檢舉狀態 (0:待處理, 1:通過, 2:駁回)
	 * @return 指定狀態的檢舉記錄列表
	 */
	@Query("FROM ItnRptVO WHERE repStatus = :repStatus ORDER BY repAt DESC")
	List<ItnRptVO> findByRepStatus(@Param("repStatus") Byte repStatus);
	
	/**
	 * 查詢某個會員對某個商品的檢舉記錄
	 * @param memId 會員ID
	 * @param itemId 商品ID
	 * @return 檢舉記錄列表
	 */
	@Query("FROM ItnRptVO WHERE memId = :memId AND itemId = :itemId ORDER BY repAt DESC")
	List<ItnRptVO> findByMemIdAndItemId(@Param("memId") Integer memId, @Param("itemId") Integer itemId);
	
	/**
	 * 統計某個商品被檢舉的次數（已通過的檢舉）
	 * @param itemId 商品ID
	 * @return 已通過的檢舉次數
	 */
	@Query("SELECT COUNT(r) FROM ItnRptVO r WHERE r.itemId = :itemId AND r.repStatus = 1")
	Long countApprovedReportsByItemId(@Param("itemId") Integer itemId);

}
