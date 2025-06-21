package com.toiukha.members.model;


import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;



public interface MembersRepository extends JpaRepository<MembersVO, Integer>,JpaSpecificationExecutor<MembersVO> {
	
	boolean existsByMemAcc(String memAcc);
	
	Optional<MembersVO> findByMemAcc(String memAcc); 
	
	boolean existsByMemEmail(String memEmail);
	
	Optional<MembersVO> findByMemEmail(String memEmail);
	
	@Modifying
	@Transactional
	@Query(
	  value = "UPDATE members SET MEMLOGERRCOUNT = MEMLOGERRCOUNT + 1, MEMLOGERRTIME = :now WHERE MEMID = :id",
	  nativeQuery = true
	)
	int incrementLoginError(@Param("id") Integer memId,
	                        @Param("now") Timestamp now);
	
	 @Modifying
	    @Transactional
	    @Query(value="UPDATE members SET MEMLOGERRCOUNT = 0 WHERE MEMID = :memId", nativeQuery=true)
	    int resetLoginErrorNative(@Param("memId") Integer memId);

}
