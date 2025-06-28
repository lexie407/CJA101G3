package com.toiukha.administrant.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrantRepository extends JpaRepository<AdministrantVO, Integer> {
	
	Optional<AdministrantVO> findByAdminAcc(String adminAcc);

}
