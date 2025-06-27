package com.toiukha.articlecollection.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleCollectionRepository extends JpaRepository<ArticleCollectionVO, ArticleCollectionCompositePrimaryKey> {

	public List<ArticleCollectionVO> findById_MemId(Integer memId);
	
}
