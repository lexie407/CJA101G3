package com.toiukha.articlecollection.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleCollectionService {

	@Autowired
	private ArticleCollectionRepository articleCollectionRepository;
	
	//查詢會員全部
	public List<ArticleCollectionVO> getByMem(Integer memId){
		return articleCollectionRepository.findById_MemId(memId);
	}
	
	//查詢單一
	public ArticleCollectionVO getOne(ArticleCollectionCompositePrimaryKey aPK) {
		Optional<ArticleCollectionVO> option = articleCollectionRepository.findById(aPK);
		return option.orElse(null);
	}
	
	//新增
	public void addOne(ArticleCollectionVO aVO) {
		articleCollectionRepository.save(aVO);
	}
	
	//刪除
	public void deleteOne(ArticleCollectionCompositePrimaryKey aPK) {
		articleCollectionRepository.deleteById(aPK);
	}
}
