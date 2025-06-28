package com.toiukha.articlereport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.articlereport.model.ArticlereportService;
import com.toiukha.articlereport.model.ArticlereportVO;
import com.toiukha.commentsReport.model.CommentsReportService;
import com.toiukha.commentsReport.model.CommentsReportVO;

@RestController
@RequestMapping("/ArticleReportsAPI")
public class ArticleReportsAPIController {
	
	@Autowired
	ArticlereportService articlereportService;
	
	//新增檢舉
	@PostMapping("/addArticleReports")
	public void addCommentsReports(
			@RequestBody ArticlereportVO articlereportVO) {
		articlereportService.addOne(articlereportVO);
	}

}
