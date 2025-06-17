package com.toiukha.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toiukha.report.model.ReportService;
import com.toiukha.report.model.ReportVO;

@Controller
@RequestMapping("/report")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@PostMapping("/submit")
	@ResponseBody
	public String submitReport(
		// 接收回傳的JSON
		@RequestBody ReportVO reportVO) {
		reportService.saveReport(reportVO);
		return "已收到";

	}
	
	@GetMapping("/chat") // 或者你希望的任何 URL
    public String showChatPage() {
        // 這會查找 src/main/resources/templates/front-end/report/test.html
        return "front-end/report/test"; 
    }
}
