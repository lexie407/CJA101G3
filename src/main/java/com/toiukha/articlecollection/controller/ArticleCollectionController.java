package com.toiukha.articlecollection.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.articlecollection.model.ArticleCollectionCompositePrimaryKey;
import com.toiukha.articlecollection.model.ArticleCollectionDTO;
import com.toiukha.articlecollection.model.ArticleCollectionService;
import com.toiukha.articlecollection.model.ArticleCollectionVO;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticleServiceImpl;

@Controller
@RequestMapping("/articleCollection")
public class ArticleCollectionController {
	
	@Autowired
	ArticleCollectionService articleCollectionService;
	@Autowired
	ArticleServiceImpl articleServiceImpl;

	//收藏明細頁面
	@GetMapping("/allList")
	public String allList(
//			@RequestParam("memId")Integer memId,
			ModelMap model) {
//		記得串會員後要改
		List<ArticleCollectionVO> list = articleCollectionService.getByMem(1);
		List<ArticleCollectionDTO> nList = new ArrayList<>();
		for(ArticleCollectionVO aVO : list) {
			Article article = articleServiceImpl.getArticleById(aVO.getId().getArtId());
			String artTit = article.getArtTitle();
			Integer artId = aVO.getId().getArtId();
			Integer HolId = article.getArtHol();
			
			ArticleCollectionDTO DTO = new ArticleCollectionDTO(artId, artTit, HolId);
			
			nList.add(DTO);
		}
		model.addAttribute("list", nList);
		model.addAttribute("currentPage", "account");
		return "front-end/articlecollection/memberArticleCollection";
	}
	
	//在明細刪除收藏
	@PostMapping("/deleteLine")
	public String deleteLine(
			@RequestBody List<ArticleCollectionCompositePrimaryKey> list,
			RedirectAttributes redirectAttributes) {
		for(ArticleCollectionCompositePrimaryKey aPK : list)
			articleCollectionService.deleteOne(aPK);
		redirectAttributes.addAttribute("currentPage", "account");
		redirectAttributes.addAttribute("successMsg", "收藏刪除完成!");
		return "redirect:/articleCollection/allList";
	}
	
}
