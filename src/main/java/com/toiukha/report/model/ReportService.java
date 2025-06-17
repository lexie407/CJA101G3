package com.toiukha.report.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

	@Autowired
	ReportRepository repository;
	
	
    public void saveReport(ReportVO reportVO) {
        repository.save(reportVO);
    }
    
    public ReportVO getOne(Integer repoId) {
    	Optional<ReportVO> optional = repository.findById(repoId);
    	return optional.orElse(null);
    }
    
    public List<ReportVO> getAll(){
    	return repository.findAll();
    }
	
}
