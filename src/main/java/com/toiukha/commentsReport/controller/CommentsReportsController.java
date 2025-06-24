package com.toiukha.commentsReport.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.comments.model.CommentsService;
import com.toiukha.comments.model.CommentsVO;
import com.toiukha.commentsReport.model.CommentsReportService;
import com.toiukha.commentsReport.model.CommentsReportVO;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/CommentsReports")
public class CommentsReportsController {
	
	@Autowired
	CommentsReportService commentsReportService;
	@Autowired
	CommentsService commentsService;

	//========== 前台 ==========//
	//新增檢舉
	@PostMapping("/addCommentsReports")
	public String addCommentsReports(
			@ModelAttribute CommentsReportVO commentsReportVO) {
		commentsReportService.addOne(commentsReportVO);
		return "";
	}
	
	//查看檢舉
	@GetMapping("/memberReportList")
	public String memberReportList(HttpServletRequest req, ModelMap model) {
//		等登入串接後使用
//		Integer memId = (Integer)req.getSession().getAttribute("member");
		Integer memId = 1;
		List<CommentsReportVO> list = commentsReportService.getMemList(memId);
		model.addAttribute("list", list);
		model.addAttribute("currentPage", "account");
		return "front-end/commentsreports/memberCommentsReportList";
	}
	
	//看詳情的頁面
	@PostMapping("/memberReportListDetsil")
	public String memberReportListDetsil(
			@RequestParam("commRepId") Integer commRepId, 
			ModelMap model) {
		CommentsReportVO commentsReportVO = commentsReportService.getOne(commRepId);
		model.addAttribute("commentsReportVO", commentsReportVO);
		model.addAttribute("currentPage", "account");
		return "front-end/commentsreports/memberCommentsReportDetail";
	}
	
	//========== 後台 ==========//
	//查看所有檢舉
	@GetMapping("/allReportList")
	public String allReportList(ModelMap model) {
		List<CommentsReportVO> list = commentsReportService.getAll();
		model.addAttribute("list", list);
		return "back-end/commentsreports/allCommentsReportList";
	}
	
	//編輯案件頁面
	@PostMapping("/editCommentsReportList")
	public String editCommentsReportList(
			@RequestParam("commRepId") Integer commRepId,
			ModelMap model) {
		CommentsReportVO commentsReportVO = commentsReportService.getOne(commRepId);
		CommentsVO commentsVO = commentsService.getOne(commentsReportVO.getCommId());
		
		model.addAttribute("commentsReportVO", commentsReportVO);
		model.addAttribute("commentsVO", commentsVO);
		model.addAttribute("currentPage", "account");
		return "back-end/commentsreports/editCommentsReport";
	}
	
	//案件成立處理
	@PostMapping("/established")
	public String established(
			@RequestParam("commId") Integer commId,
			@RequestParam("remarks") String remarks, 
			@RequestParam("commRepId") Integer commRepId,
			RedirectAttributes redirectAttributes) {
		//修改留言狀態
		commentsService.changeSta(commId, (byte)2);
		
		//修改檢舉狀態
		CommentsReportVO commentsReportVO = new CommentsReportVO();
		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());
		commentsReportVO.setCommRepId(commRepId);
		commentsReportVO.setRemarks(remarks);
		commentsReportVO.setRptSta((byte)1);
		commentsReportVO.setRevTime(now);
		commentsReportService.changeSta(commentsReportVO);
		
		redirectAttributes.addFlashAttribute("successMsg", "檢舉案件處理已完成!");
		
		return "redirect:/CommentsReports/allReportList";
	}
	
	//案件不成立處理
	@PostMapping("/unEstablished")
	public String unEstablished(
			@RequestParam("remarks") String remarks, 
			@RequestParam("commRepId") Integer commRepId,
			RedirectAttributes redirectAttributes) {
		//修改檢舉狀態
		System.out.println(commRepId);
		CommentsReportVO commentsReportVO = new CommentsReportVO();
		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());
		commentsReportVO.setCommRepId(commRepId);
		commentsReportVO.setRemarks(remarks);
		commentsReportVO.setRptSta((byte)2);
		commentsReportVO.setRevTime(now);
		System.out.println(commRepId+1);
		commentsReportService.changeSta(commentsReportVO);
		System.out.println(commRepId+2);
		redirectAttributes.addFlashAttribute("successMsg", "檢舉案件處理已完成!");
		
		return "redirect:/CommentsReports/allReportList";
	}
			
}
