package com.toiukha.report.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ReportRepository extends JpaRepository<ReportVO, Integer> {
	
	@Query(value = "FROM ReportVO as rVO WHERE rVO.memId = :memId ORDER BY rVO.repStatus, rVO.repAt DESC")
	public List<ReportVO> getByMemId(Integer memId);
	
}
