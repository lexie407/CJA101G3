package com.toiukha.reportmem.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.toiukha.reportmem.model.ReportMemVO;

public interface ReportMemRepository extends JpaRepository<ReportMemVO, Integer>{

	//被檢舉人查詢
	@Query(value = "FROM ReportMemVO as rmVO WHERE rmVO.tgtMemId = :memId ORDER BY rmVO.repStatus, rmVO.rptTime DESC")
	public List<ReportMemVO> getByTgtMemId(Integer tgtMemId);
}
