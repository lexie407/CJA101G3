package com.toiukha.reportmem.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ReportMemService {
	
	@Autowired
	ReportMemRepository repo;
	
	
//	public void save(ReportMemVO rmVO) {
//        if(rmVO.getClosExplan() == "" || rmVO.getClosExplan() == null) {	        	repository.save(rmVO);
//	       }else {
//	       	rmVO.setRepStatus((byte)2);
//        	repo.save(rmVO);	        }
//	}
	
	public ReportMemVO getOne(Integer rptId) {
    	Optional<ReportMemVO> optional = repo.findById(rptId);
    	return optional.orElse(null);
    }
    
    public List<ReportMemVO> getAll(){
    	return repo.findAll();
    }
    
    public List<ReportMemVO> getByTgtMemId(Integer tgtMemId){
    	return repo.getByTgtMemId(tgtMemId);
    }
	
	
	
}
