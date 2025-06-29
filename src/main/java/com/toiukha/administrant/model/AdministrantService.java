package com.toiukha.administrant.model;

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

}
