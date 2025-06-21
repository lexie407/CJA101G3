package com.toiukha.comments.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentsRepository extends JpaRepository<CommentsVO, Integer> {

	@Query(value = "FROM CommentsVO WHERE commArt = :commArt")
	public List<CommentsVO> getbyArt(Integer commArt);
	
}
