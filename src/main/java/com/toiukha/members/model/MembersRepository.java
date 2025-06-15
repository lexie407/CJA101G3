package com.toiukha.members.model;

import org.springframework.data.jpa.repository.JpaRepository;



public interface MembersRepository extends JpaRepository<MembersVO, Integer> {
	
	boolean existsByMemAcc(String memAcc);

}
