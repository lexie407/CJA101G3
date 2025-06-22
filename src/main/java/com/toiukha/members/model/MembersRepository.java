package com.toiukha.members.model;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface MembersRepository extends JpaRepository<MembersVO, Integer> {
	
	boolean existsByMemAcc(String memAcc);
	
	Optional<MembersVO> findByMemAcc(String memAcc); 
	
	boolean existsByMemEmail(String memEmail);
	
	Optional<MembersVO> findByMemEmail(String memEmail);

}
