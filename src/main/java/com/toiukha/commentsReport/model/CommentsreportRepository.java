package com.toiukha.commentsReport.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentsreportRepository extends JpaRepository<CommentsReportVO, Integer> {
	
	@Query(value = "FROM CommentsReportVO AS cVO WHERE memId = :memId")
	public List<CommentsReportVO> getbyMem(Integer memId);
	
}
