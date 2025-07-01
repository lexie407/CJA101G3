package com.toiukha.administrantauth.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministrantAuthService {
	
	@Autowired
    private AdministrantAuthRepository administrantAuthRepository;

}
