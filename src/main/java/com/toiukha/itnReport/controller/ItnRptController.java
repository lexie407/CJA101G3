package com.toiukha.itnReport.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.toiukha.itnReport.model.OrderItemDTO;
import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;
import com.toiukha.members.model.MembersVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/reports")
public class ItnRptController {

	@Autowired
	private ItnRptService itnRptService;
	
	@Autowired
	private ItemService itemService;
	
	/**
	 * 檢查管理員是否已登入
	 */
	private boolean isAdminLoggedIn(HttpSession session) {
		// 檢查 session 中的管理員資訊
		Object adminObj = session.getAttribute("admin");
		return adminObj != null;
	}
	
	/**
	 * 檢查會員是否已登入
	 */
	private boolean isMemberLoggedIn(HttpSession session) {
		Object memberObj = session.getAttribute("member");
		return memberObj != null;
	}
	
	/**
	 * 檢查商家是否已登入
	 */
	private boolean isStoreLoggedIn(HttpSession session) {
		Object storeObj = session.getAttribute("store");
		return storeObj != null;
	}
	
	/**
	 * 獲取當前登入的會員
	 */
	private MembersVO getCurrentMember(HttpSession session) {
		Object memberObj = session.getAttribute("member");
		return memberObj != null ? (MembersVO) memberObj : null;
	}
	
	/**
	 * 管理員查看所有檢舉列表
	 */
	@GetMapping("/list")
	public String listAllReports(HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<ItnRptVO> reports = itnRptService.getAll();
		model.addAttribute("reports", reports);
		model.addAttribute("currentPage", "report");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看檢舉詳情
	 */
	@GetMapping("/detail")
	public String getReportDetail(@RequestParam Integer repId, HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
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
							   HttpSession session,
							   RedirectAttributes redirectAttributes) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
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
							  HttpSession session,
							  RedirectAttributes redirectAttributes) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
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
	public String listPendingReports(HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<ItnRptVO> pendingReports = itnRptService.getPendingReports();
		model.addAttribute("reports", pendingReports);
		model.addAttribute("status", "pending");
		model.addAttribute("currentPage", "report");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看已通過的檢舉
	 */
	@GetMapping("/approved")
	public String listApprovedReports(HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<ItnRptVO> approvedReports = itnRptService.getApprovedReports();
		model.addAttribute("reports", approvedReports);
		model.addAttribute("status", "approved");
		model.addAttribute("currentPage", "report");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看已駁回的檢舉
	 */
	@GetMapping("/rejected")
	public String listRejectedReports(HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<ItnRptVO> rejectedReports = itnRptService.getRejectedReports();
		model.addAttribute("reports", rejectedReports);
		model.addAttribute("status", "rejected");
		model.addAttribute("currentPage", "report");
		return "back-end/itnReport/listAllReports";
	}
	
	/**
	 * 管理員查看某個商品的所有檢舉記錄
	 */
	@GetMapping("/item/{itemId}")
	public String listReportsByItem(@PathVariable Integer itemId, HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		// 檢查商品是否存在
		if (itemService.getOneItem(itemId) == null) {
			model.addAttribute("errorMessage", "商品不存在！");
			return "error/404";
		}
		
		List<ItnRptVO> reports = itnRptService.getReportsByItemId(itemId);
		Long approvedCount = itnRptService.getApprovedReportCountByItemId(itemId);
		
		// 計算各種狀態的檢舉數量
		long pendingCount = reports.stream().filter(r -> r.getRepStatus() != null && r.getRepStatus() == 0).count();
		long rejectedCount = reports.stream().filter(r -> r.getRepStatus() != null && r.getRepStatus() == 2).count();
		
		model.addAttribute("reports", reports);
		model.addAttribute("itemId", itemId);
		model.addAttribute("approvedCount", approvedCount);
		model.addAttribute("pendingCount", pendingCount);
		model.addAttribute("rejectedCount", rejectedCount);
		model.addAttribute("totalCount", reports.size());
		model.addAttribute("currentPage", "report");
		return "back-end/itnReport/listReportsByItem";
	}
	
	/**
	 * 管理員查看某個會員的所有檢舉記錄
	 */
	@GetMapping("/member/{memId}")
	public String listReportsByMember(@PathVariable Integer memId, HttpSession session, Model model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<ItnRptVO> reports = itnRptService.getReportsByMemberId(memId);
		
		// 計算各種狀態的檢舉數量
		long approvedCount = reports.stream().filter(r -> r.getRepStatus() != null && r.getRepStatus() == 1).count();
		long pendingCount = reports.stream().filter(r -> r.getRepStatus() != null && r.getRepStatus() == 0).count();
		long rejectedCount = reports.stream().filter(r -> r.getRepStatus() != null && r.getRepStatus() == 2).count();
		
		model.addAttribute("reports", reports);
		model.addAttribute("memId", memId);
		model.addAttribute("approvedCount", approvedCount);
		model.addAttribute("pendingCount", pendingCount);
		model.addAttribute("rejectedCount", rejectedCount);
		model.addAttribute("totalCount", reports.size());
		model.addAttribute("currentPage", "report");
		return "back-end/itnReport/listReportsByMember";
	}
	
	/**
	 * 獲取檢舉圖片
	 */
	@GetMapping("/image/{repId}")
	public ResponseEntity<byte[]> getReportImage(@PathVariable Integer repId) {
		ItnRptVO report = itnRptService.getOneItnRpt(repId);
		if (report != null && report.getRepImg() != null) {
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_JPEG)
					.body(report.getRepImg());
		}
		return ResponseEntity.notFound().build();
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
								HttpSession session,
								Model model) {
		// 檢查會員登入狀態
		if (!isMemberLoggedIn(session)) {
			return "redirect:/members/login";
		}
		
		// 檢查商品是否存在
		if (itemService.getOneItem(itemId) == null) {
			model.addAttribute("errorMessage", "商品不存在！");
			return "error/404";
		}
		
		// 從 session 獲取會員資訊
		MembersVO member = getCurrentMember(session);
		
		// 檢查會員是否購買過該商品（簡化版本，暫時允許所有會員檢舉）
		boolean hasPurchased = itemService.hasMemberPurchasedItem(member.getMemId(), itemId);
		
		if (!hasPurchased) {
			model.addAttribute("errorMessage", "您必須購買過此商品才能進行檢舉！");
			return "redirect:/item/detail?itemId=" + itemId; // 重定向到商品詳情頁
		}
		
		// 檢查會員是否已經檢舉過該商品
		boolean hasReported = itnRptService.hasMemberReportedItem(member.getMemId(), itemId);
		
		if (hasReported) {
			model.addAttribute("errorMessage", "您已經檢舉過此商品，請勿重複檢舉！");
			return "redirect:/item/detail?itemId=" + itemId; // 重定向到商品詳情頁
		}
		
		model.addAttribute("itemId", itemId);
		model.addAttribute("memId", member.getMemId());
		return "front-end/itnReport/reportForm";
	}
	
	/**
	 * 檢查會員是否已登入
	 */
	private boolean isMemberLoggedIn(HttpSession session) {
		Object memberObj = session.getAttribute("member");
		return memberObj != null;
	}
	
	/**
	 * 獲取當前登入的會員
	 */
	private MembersVO getCurrentMember(HttpSession session) {
		Object memberObj = session.getAttribute("member");
		return memberObj != null ? (MembersVO) memberObj : null;
	}
	
	/**
	 * 提交商品檢舉
	 */
	@PostMapping("/submit")
	public String submitReport(@RequestParam Integer memId,
							  @RequestParam Integer itemId,
							  @RequestParam String repReason,
							  @RequestParam(required = false) MultipartFile repImg,
							  HttpSession session,
							  RedirectAttributes redirectAttributes) {
		
		// 檢查會員登入狀態
		if (!isMemberLoggedIn(session)) {
			return "redirect:/members/login";
		}
		
		// 驗證提交的會員ID是否與session中的一致（防止偽造）
		MembersVO currentMember = getCurrentMember(session);
		if (!currentMember.getMemId().equals(memId)) {
			redirectAttributes.addFlashAttribute("errorMessage", "無效的操作！");
			return "redirect:/members/login";
		}
		
		// 基本參數驗證
		if (itemId == null || repReason == null || repReason.trim().isEmpty()) {
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
		
		// 檢查會員是否已經檢舉過該商品
		boolean hasReported = itnRptService.hasMemberReportedItem(memId, itemId);
		
		if (hasReported) {
			redirectAttributes.addFlashAttribute("errorMessage", "您已經檢舉過此商品，請勿重複檢舉！");
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
	
	/**
	 * 顯示會員的訂單列表（用於檢舉）
	 */
	@GetMapping("/my-orders")
	public String showMyOrders(HttpSession session, Model model) {
		// 檢查會員登入狀態
		if (!isMemberLoggedIn(session)) {
			return "redirect:/members/login";
		}
		
		MembersVO member = getCurrentMember(session);
		Integer memId = member.getMemId();
		
		// 獲取會員的購買記錄
		List<OrderItemDTO> orderItems = getMemberOrderItems(memId);
		
		model.addAttribute("orderItems", orderItems);
		model.addAttribute("member", member);
		model.addAttribute("currentPage", "report");
		return "front-end/itnReport/myOrders";
	}
	
	/**
	 * 從訂單檢舉商品
	 */
	@GetMapping("/form-from-order")
	public String showReportFormFromOrder(@RequestParam Integer ordId,
										 @RequestParam Integer itemId,
										 HttpSession session,
										 Model model) {
		// 檢查會員登入狀態
		if (!isMemberLoggedIn(session)) {
			return "redirect:/members/login";
		}
		
		MembersVO member = getCurrentMember(session);
		
		// 檢查商品是否存在
		if (itemService.getOneItem(itemId) == null) {
			model.addAttribute("errorMessage", "商品不存在！");
			return "error/404";
		}
		
		// 檢查是否已經檢舉過此商品
		if (itnRptService.hasMemberReportedItem(member.getMemId(), itemId)) {
			model.addAttribute("errorMessage", "您已經檢舉過此商品！");
			return "front-end/itnReport/myOrders";
		}
		
		// 獲取商品資訊
		ItemVO item = itemService.getOneItem(itemId);
		
		model.addAttribute("item", item);
		model.addAttribute("member", member);
		model.addAttribute("ordId", ordId);
		model.addAttribute("currentPage", "report");
		return "front-end/itnReport/reportFormFromOrder";
	}
	
	/**
	 * 處理從訂單提交的檢舉
	 */
	@PostMapping("/submit-from-order")
	public String submitReportFromOrder(@RequestParam Integer memId,
									   @RequestParam Integer ordId,
									   @RequestParam Integer itemId,
									   @RequestParam String repReason,
									   @RequestParam(required = false) MultipartFile repImg,
									   HttpSession session,
									   RedirectAttributes redirectAttributes) {
		
		// 檢查會員登入狀態
		if (!isMemberLoggedIn(session)) {
			return "redirect:/members/login";
		}
		
		// 驗證提交的會員ID是否與session中的一致（防止偽造）
		MembersVO currentMember = getCurrentMember(session);
		if (!currentMember.getMemId().equals(memId)) {
			redirectAttributes.addFlashAttribute("errorMessage", "無效的操作！");
			return "redirect:/members/login";
		}
		
		try {
			// 檢查是否已經檢舉過
			if (itnRptService.hasMemberReportedItem(memId, itemId)) {
				redirectAttributes.addFlashAttribute("errorMessage", "您已經檢舉過此商品！");
				return "redirect:/item-report/my-orders";
			}
			
			// 創建檢舉記錄
			ItnRptVO report = new ItnRptVO();
			report.setMemId(memId);
			report.setOrdId(ordId);  // 設置訂單編號
			report.setItemId(itemId);
			report.setRepReason(repReason);
			report.setRepStatus((byte) 0); // 0 = 待處理
			report.setRepAt(LocalDateTime.now());
			
			// 處理圖片上傳
			if (repImg != null && !repImg.isEmpty()) {
				report.setRepImg(repImg.getBytes());
			}
			
			// 保存檢舉記錄
			itnRptService.addItnRpt(report);
			
			redirectAttributes.addFlashAttribute("successMessage", "檢舉已成功提交，等待審核中！");
			
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "圖片上傳失敗，請重試！");
			return "redirect:/item-report/form-from-order?ordId=" + ordId + "&itemId=" + itemId;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "提交檢舉失敗：" + e.getMessage());
			return "redirect:/item-report/form-from-order?ordId=" + ordId + "&itemId=" + itemId;
		}
		
		return "redirect:/item-report/my-orders";
	}
	
	/**
	 * 模擬獲取會員的購買記錄
	 * 這個方法應該根據實際的訂單系統來實現
	 */
	private List<OrderItemDTO> getMemberOrderItems(Integer memId) {
		// TODO: 這裡應該查詢實際的訂單資料庫
		// 目前提供模擬資料，實際應用中需要實現真實的訂單查詢
		
		List<OrderItemDTO> orderItems = new ArrayList<>();
		
		// 模擬資料 - 實際應該從資料庫查詢
		List<ItemVO> allItems = itemService.getAll();
		for (int i = 0; i < Math.min(5, allItems.size()); i++) {
			ItemVO item = allItems.get(i);
			OrderItemDTO orderItem = new OrderItemDTO();
			orderItem.setOrdId(1000 + i);
			orderItem.setItemId(item.getItemId());
			orderItem.setItemName(item.getItemName());
			orderItem.setQuantity(1 + (i % 3));
			orderItem.setPrice(item.getItemPrice());
			orderItem.setOrderDate(LocalDateTime.now().minusDays(i * 7));
			orderItem.setOrderStatus("已完成");
			orderItem.setItemImg(item.getItemImg());
			orderItems.add(orderItem);
		}
		
		return orderItems;
	}
}
