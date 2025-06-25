package com.toiukha.itnReport.model;

import java.util.*;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("itnRptService")
public class ItnRptService {
	
	@Autowired
	ItnRptRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void addItnRpt(ItnRptVO itnRptVO) {
		repository.save(itnRptVO);
	}
	public void updateItnRpt(ItnRptVO itnRptVO) {
		repository.save(itnRptVO);
	}
	public void deleteItnRpt(Integer repId) {
		if(repository.existsById(repId))
			repository.deleteById(repId);
	}
	
	public ItnRptVO getOneItnRpt(Integer repId) {
		Optional<ItnRptVO> optional = repository.findById(repId);
		return optional.orElse(null);
	}
	
	public List<ItnRptVO> getAll(){
		return repository.findAll();
	}
	
		

}
