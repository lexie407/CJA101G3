package com.toiukha.comments.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommentsRepository extends JpaRepository<CommentsVO, Integer> {

	@Query(value = "FROM CommentsVO WHERE commArt = :commArt AND commSta != 2")
	public List<CommentsVO> getbyArt(Integer commArt);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE CommentsVO as cVO SET cVO.commSta = :commSta WHERE cVO.commId = :commId ")
	public void upadteSta(Integer commId, Byte commSta);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE CommentsVO as cVO SET cVO.commCon = :commCon, cVO.commImg = :commImg WHERE cVO.commId = :commId ")
	public void upadteComm(Integer commId, String commCon, byte[] commImg);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE CommentsVO as cVO SET cVO.commCon = :commCon WHERE cVO.commId = :commId ")
	public void upadteComm(Integer commId, String commCon);
	
}
