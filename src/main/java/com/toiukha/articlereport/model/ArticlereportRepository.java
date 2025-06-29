package com.toiukha.articlereport.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticlereportRepository extends JpaRepository<ArticlereportVO, Integer> {

	@Query(value = "FROM ArticlereportVO AS aVO WHERE memId = :memId")
	public List<ArticlereportVO> getByMem(Integer memId);
	
}
