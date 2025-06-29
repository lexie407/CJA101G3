package com.toiukha.administrant.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministrantService {
	
	@Autowired
    private AdministrantRepository administrantRepository;
	
	
	public Optional<AdministrantVO> findByAdminAcc(String adminAcc) {
        return administrantRepository.findByAdminAcc(adminAcc);
    }
	
	//依 ID 查單筆
	 public AdministrantVO getById(Integer adminId) {
	        return administrantRepository.findById(adminId)
	                                     .orElse(null);
	    }

	 //依名稱模糊查詢
	 public List<AdministrantVO> findByNameLike(String adminName) {
	        return administrantRepository.findByAdminNameContaining(adminName);
	    }
	
	// 取得所有管理員清單
	 public List<AdministrantVO> getAll() {
	        return administrantRepository.findAll();
	    }
	 
	 //查詢帳號是否存在
	 public boolean existsByAdminAcc(String adminAcc) {
		    return administrantRepository.existsByAdminAcc(adminAcc);
		}
	 
	// 建立新管理員
	 public void create(AdministrantVO vo) {
	     administrantRepository.save(vo);
	 }
	 
	// 更新既有管理員
	 public void update(AdministrantVO vo) {
	     administrantRepository.save(vo);
	 }

}
