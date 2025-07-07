package com.toiukha.report.controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.members.model.MembersService;
import com.toiukha.members.model.MembersVO;
import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;
import com.toiukha.report.model.ReportService;
import com.toiukha.report.model.ReportVO;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/report")
public class ReportController {

	@Autowired
	private ReportService reportService;
	@Autowired
	private MembersService membersService;
	@Autowired
	NotificationService notificationService;

	//========== DIYI用 ==========//
	//新增回報案件
	@PostMapping("/submit")
	@ResponseBody
	public String submitReport(
		// 接收回傳的JSON
		@RequestBody ReportVO reportVO) {
		reportService.saveReport(reportVO);
		notificationService.addOneNoti(new NotificationVO(
				"[系統]系統異常回報新增",
				"你好，稍早AI夥伴已協助你回報異常，可至「與我聊聊」 > 「回報明細」追蹤進度。",
				reportVO.getMemId(),
				getNowTime()));
		return "已收到";

	}
	
	//========== 前台 ==========//
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
		Integer memId = ((MembersVO)req.getSession().getAttribute("member")).getMemId();
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
	
	//========== 後台 ==========//
	//觀看所有案件
	@GetMapping("/allReportList")
	public String allReportList(ModelMap model) {
		List<ReportVO> list = reportService.getAll();
		model.addAttribute("list", list);
		model.addAttribute("currentPage", "support_agent");
		model.addAttribute("currentPage2", "allReportList");
		return "back-end/report/allReportList";
	}
	
	//新增回報案件頁面
	@GetMapping("/addReportbyAdmin")
	public String addReportbyAdmin(
			HttpServletRequest req, 
			ModelMap model) {
		List<MembersVO> memList = membersService.findAllMembers();
		model.addAttribute("memList", memList);
		model.addAttribute("currentPage", "support_agent");
		model.addAttribute("currentPage2", "addReportbyAdmin");
		return "back-end/report/addReportbyAdmin";
	}
	
	//新增回報案件
	@PostMapping("/doAddReportbyAdmin")
	public String doAddReportbyAdmin(
			@ModelAttribute ReportVO reportVO, 
			HttpServletRequest req, 
			ModelMap model) {
		reportService.saveReport(reportVO);
		model.addAttribute("currentPage", "support_agent");
		model.addAttribute("currentPage2", "addReportbyAdmin");
		model.addAttribute("successMsg", "案件新增完成!");
		return "back-end/report/addReportbyAdmin";
	}
	
	
	
	//編輯案件頁面
	@PostMapping("/editReport")
	public String editReoprt(
			@RequestParam("repId") Integer repId,
			ModelMap model) {
		ReportVO reportVO = reportService.getOne(repId);
		if(reportVO.getRepStatus() == 0) {
			reportVO.setRepStatus((byte)1);
			reportService.saveReport(reportVO);
		}
		model.addAttribute("reportVO", reportVO);
		model.addAttribute("currentPage", "support_agent");
		return "back-end/report/editReport";
	}
	
	//更新案件
	@PostMapping("/updateReport")
	public String updateReport(
			@ModelAttribute ReportVO reportVO,
			ModelMap model,
			RedirectAttributes redirectAttributes) {
		reportService.saveReport(reportVO);
		if(reportVO.getRepStatus() == 2) {
			redirectAttributes.addFlashAttribute("successMsg", "案件結案完成!");
			notificationService.addOneNoti(new NotificationVO(
					"[系統]系統異常回報結案",
					"你好，你回報的異常已結案，可至「與我聊聊」 > 「回報明細」查看詳情。",
					reportVO.getMemId(),
					getNowTime()));
		}else {
			redirectAttributes.addFlashAttribute("successMsg", "案件更新完成!");
			notificationService.addOneNoti(new NotificationVO(
					"[系統]系統異常回報更新",
					"你好，你回報的異常有更新，可至「與我聊聊」 > 「回報明細」查看。",
					reportVO.getMemId(),
					getNowTime()));
		}
		model.addAttribute("currentPage", "support_agent");
		return "redirect:/report/allReportList";
	}
	
	Timestamp getNowTime() {
		return Timestamp.from(Instant.now());
	}
}
