package com.toiukha.articlecollection.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.articlecollection.model.ArticleCollectionCompositePrimaryKey;
import com.toiukha.articlecollection.model.ArticleCollectionService;
import com.toiukha.articlecollection.model.ArticleCollectionVO;

@RestController
@RequestMapping("/ArticleCollectionAPI")
public class ArticleCollectionAPIController {

	@Autowired
	private ArticleCollectionService articleCollectionService;
	
	@PostMapping("/api")
	public void api(
			@RequestBody ArticleCollectionCompositePrimaryKey aPK) {
		 
		ArticleCollectionVO aVO = new ArticleCollectionVO(aPK);
		if(articleCollectionService.getOne(aPK) != null) {
			articleCollectionService.deleteOne(aPK);
		}else {
			articleCollectionService.addOne(aVO);
		}
		
	}
	
}
