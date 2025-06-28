package com.toiukha.itnReport.controller;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.itnReport.model.ItnRptService;
import com.toiukha.itnReport.model.ItnRptVO;
import com.toiukha.item.model.ItemService;

@Controller
@RequestMapping("/admin/reports")
public class ItnRptController {

	@Autowired
	private ItnRptService itnRptService;
	
	@Autowired
	private ItemService itemService;
	
	/**
	 * 管理員查看所有檢舉列表
	 */
	@GetMapping("/list")
	public String listAllReports(Model model) {
		List<ItnRptVO> reports = itnRptService.getAll();
		model.addAttribute("reports", reports);
		model.addAttribute("currentPage", "report");
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
			model.addAttribute("currentPage", "report");
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
			
			// 如果檢舉的是商品，則更新商品的檢舉次數
			if (report.getItemId() != null) {
				boolean itemRemoved = itemService.incrementReportCount(report.getItemId());
				if (itemRemoved) {
					redirectAttributes.addFlashAttribute("successMessage", "檢舉已通過處理！商品因檢舉次數過多已自動下架。");
				} else {
					redirectAttributes.addFlashAttribute("successMessage", "檢舉已通過處理！商品檢舉次數已更新。");
				}
			} else {
				redirectAttributes.addFlashAttribute("successMessage", "檢舉已通過處理！");
			}
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
		model.addAttribute("currentPage", "report");
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
		model.addAttribute("currentPage", "report");
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
		model.addAttribute("currentPage", "report");
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

/**
 * 前端商品檢舉控制器
 */
@Controller
@RequestMapping("/item-report")
class ItemReportController {
	
	@Autowired
	private ItnRptService itnRptService;
	
	@Autowired
	private ItemService itemService;
	
	/**
	 * 顯示商品檢舉表單
	 */
	@GetMapping("/form")
	public String showReportForm(@RequestParam(required = true) Integer itemId, 
								Model model) {
		// 檢查商品是否存在
		if (itemService.getOneItem(itemId) == null) {
			model.addAttribute("errorMessage", "商品不存在！");
			return "error/404";
		}
		
		// 這裡應該從 session 獲取會員 ID
		Integer memId = 1; // 暫時寫死，實際應該從 session 獲取
		
		// 檢查會員是否購買過該商品（簡化版本，暫時允許所有會員檢舉）
		boolean hasPurchased = itemService.hasMemberPurchasedItem(memId, itemId);
		
		if (!hasPurchased) {
			model.addAttribute("errorMessage", "您必須購買過此商品才能進行檢舉！");
			return "redirect:/item/detail?itemId=" + itemId; // 重定向到商品詳情頁
		}
		
		model.addAttribute("itemId", itemId);
		model.addAttribute("memId", memId);
		return "front-end/item/reportForm";
	}
	
	/**
	 * 提交商品檢舉
	 */
	@PostMapping("/submit")
	public String submitReport(@RequestParam Integer memId,
							  @RequestParam Integer itemId,
							  @RequestParam String repReason,
							  @RequestParam(required = false) MultipartFile repImg,
							  RedirectAttributes redirectAttributes) {
		
		// 基本參數驗證
		if (memId == null || itemId == null || repReason == null || repReason.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "請填寫完整的檢舉資訊！");
			return "redirect:/item-report/form?itemId=" + itemId;
		}
		
		// 檢查商品是否存在
		if (itemService.getOneItem(itemId) == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "商品不存在！");
			return "redirect:/item/list";
		}
		
		// 檢查會員是否購買過該商品（簡化版本，暫時允許所有會員檢舉）
		boolean hasPurchased = itemService.hasMemberPurchasedItem(memId, itemId);
		
		if (!hasPurchased) {
			redirectAttributes.addFlashAttribute("errorMessage", "您必須購買過此商品才能進行檢舉！");
			return "redirect:/item/detail?itemId=" + itemId;
		}
		
		// 創建檢舉記錄
		ItnRptVO report = new ItnRptVO();
		report.setMemId(memId);
		report.setItemId(itemId);
		report.setRepReason(repReason);
		report.setRepStatus((byte) 0); // 待處理
		report.setRepAt(LocalDateTime.now());
		
		// 處理檔案上傳
		if (repImg != null && !repImg.isEmpty()) {
			try {
				report.setRepImg(repImg.getBytes());
			} catch (IOException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "檔案上傳失敗，請重試！");
				return "redirect:/item-report/form?itemId=" + itemId;
			}
		}
		
		itnRptService.addItnRpt(report);
		
		redirectAttributes.addFlashAttribute("successMessage", "檢舉已提交，我們會盡快處理！");
		return "redirect:/item/detail?itemId=" + itemId;
	}
}
