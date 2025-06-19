package com.toiukha.report.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toiukha.report.model.ReportService;
import com.toiukha.report.model.ReportVO;

import jakarta.servlet.http.HttpServletRequest;

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
	
	//與我聊聊
	@GetMapping("/chat")
    public String showChatPage(ModelMap model) {
		model.addAttribute("currentPage", "chatWithMe");
		model.addAttribute("currentPage2", "chatWithMe");
		return "front-end/report/chatWithMe"; 
    }
	
	//會員回報明細
	@GetMapping("/memberReportList")
	public String memberReportList(HttpServletRequest req, ModelMap model) {
//		要記得放入會員資料
//		Integer memId = (Integer)req.getSession().getAttribute(???);
		Integer memId = 1;
		List<ReportVO> list = reportService.getMemberReports(memId);
		model.addAttribute("currentPage", "chatWithMe");
		model.addAttribute("currentPage2", "memberReportList");
		model.addAttribute("list", list);
		return "front-end/report/memberReportList";
	}
	
	//會員回報詳情
	@PostMapping("/memberReportDetail")
	public String memberReportDetail(
		@RequestParam("repId") Integer repId, 
		ModelMap model) {
		ReportVO reportVO = reportService.getOne(repId);
		model.addAttribute("reportVO", reportVO);
		return "front-end/report/memberReportDetail";
	}
}
