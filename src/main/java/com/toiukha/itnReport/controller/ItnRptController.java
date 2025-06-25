package com.toiukha.itnReport.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.itnReport.model.ItnRptService;
import com.toiukha.itnReport.model.ItnRptVO;

@Controller
@RequestMapping("/admin/reports")
public class ItnRptController {

	@Autowired
	private ItnRptService itnRptService;
	
	/**
	 * 管理員查看所有檢舉列表
	 */
	@GetMapping("/list")
	public String listAllReports(Model model) {
		List<ItnRptVO> reports = itnRptService.getAll();
		model.addAttribute("reports", reports);
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看檢舉詳情
	 */
	@GetMapping("/detail")
	public String getReportDetail(@RequestParam Integer repId, Model model) {
		ItnRptVO report = itnRptService.getOneItnRpt(repId);
		if (report != null) {
			model.addAttribute("report", report);
			return "back-end/itnReport/reportDetail";
		} else {
			return "redirect:/admin/reports/list";
		}
	}
	
	/**
	 * 處理檢舉 - 檢舉通過
	 */
	@PostMapping("/approve")
	public String approveReport(@RequestParam Integer repId, 
							   @RequestParam(required = false) Integer adminId,
							   RedirectAttributes redirectAttributes) {
		ItnRptVO report = itnRptService.getOneItnRpt(repId);
		if (report != null) {
			// 更新檢舉狀態為通過 (狀態 1)
			report.setRepStatus((byte) 1);
			report.setAdminId(adminId);
			report.setRptProcTime(LocalDateTime.now());
			
			itnRptService.updateItnRpt(report);
			
			redirectAttributes.addFlashAttribute("successMessage", "檢舉已通過處理！");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "找不到指定的檢舉記錄！");
		}
		
		return "redirect:/admin/reports/detail?repId=" + repId;
	}
	
	/**
	 * 處理檢舉 - 檢舉未通過
	 */
	@PostMapping("/reject")
	public String rejectReport(@RequestParam Integer repId, 
							  @RequestParam(required = false) Integer adminId,
							  RedirectAttributes redirectAttributes) {
		ItnRptVO report = itnRptService.getOneItnRpt(repId);
		if (report != null) {
			// 更新檢舉狀態為未通過 (狀態 2)
			report.setRepStatus((byte) 2);
			report.setAdminId(adminId);
			report.setRptProcTime(LocalDateTime.now());
			
			itnRptService.updateItnRpt(report);
			
			redirectAttributes.addFlashAttribute("successMessage", "檢舉已駁回！");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "找不到指定的檢舉記錄！");
		}
		
		return "redirect:/admin/reports/detail?repId=" + repId;
	}
	
	/**
	 * 管理員查看待處理的檢舉
	 */
	@GetMapping("/pending")
	public String listPendingReports(Model model) {
		List<ItnRptVO> allReports = itnRptService.getAll();
		// 篩選狀態為待處理的檢舉 (狀態 0)
		List<ItnRptVO> pendingReports = allReports.stream()
				.filter(report -> report.getRepStatus() != null && report.getRepStatus() == 0)
				.toList();
		model.addAttribute("reports", pendingReports);
		model.addAttribute("status", "pending");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看已通過的檢舉
	 */
	@GetMapping("/approved")
	public String listApprovedReports(Model model) {
		List<ItnRptVO> allReports = itnRptService.getAll();
		// 篩選狀態為已通過的檢舉 (狀態 1)
		List<ItnRptVO> approvedReports = allReports.stream()
				.filter(report -> report.getRepStatus() != null && report.getRepStatus() == 1)
				.toList();
		model.addAttribute("reports", approvedReports);
		model.addAttribute("status", "approved");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看已駁回的檢舉
	 */
	@GetMapping("/rejected")
	public String listRejectedReports(Model model) {
		List<ItnRptVO> allReports = itnRptService.getAll();
		// 篩選狀態為已駁回的檢舉 (狀態 2)
		List<ItnRptVO> rejectedReports = allReports.stream()
				.filter(report -> report.getRepStatus() != null && report.getRepStatus() == 2)
				.toList();
		model.addAttribute("reports", rejectedReports);
		model.addAttribute("status", "rejected");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 顯示檢舉圖片
	 */
	@GetMapping("/image/{repId}")
	public ResponseEntity<byte[]> getReportImage(@PathVariable Integer repId) {
		ItnRptVO report = itnRptService.getOneItnRpt(repId);
		if (report != null && report.getRepImg() != null && report.getRepImg().length > 0) {
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_JPEG)
					.body(report.getRepImg());
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
