package com.toiukha.administrant.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrantRepository extends JpaRepository<AdministrantVO, Integer> {
	
	Optional<AdministrantVO> findByAdminAcc(String adminAcc);
	
	boolean existsByAdminAcc(String adminAcc);

	
	// 名稱模糊查詢
    List<AdministrantVO> findByAdminNameContaining(String adminName);
    
    //查所有管理員
    List<AdministrantVO> findAll();
    
 
    
}
