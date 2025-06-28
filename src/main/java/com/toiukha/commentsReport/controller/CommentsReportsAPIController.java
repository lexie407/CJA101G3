package com.toiukha.commentsReport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.commentsReport.model.CommentsReportService;
import com.toiukha.commentsReport.model.CommentsReportVO;

@RestController
@RequestMapping("/CommentsReportsAPI")
public class CommentsReportsAPIController {
	
	@Autowired
	CommentsReportService commentsReportService;
	
	//新增檢舉
	@PostMapping("/addCommentsReports")
	public void addCommentsReports(
			@RequestBody CommentsReportVO commentsReportVO) {
		commentsReportService.addOne(commentsReportVO);
	}

}
