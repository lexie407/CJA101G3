package com.toiukha.notification.controller;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@Validated
@RequestMapping("/notification")
public class NotificatioServlet {

	@Autowired
	NotificationService notificationService;
	
	////=====前台=====////
	//取得該會員的通知資料
	@GetMapping("/getMemNoti")
	public String getAllNoti(
		//取得請求資料
		HttpServletRequest req,
		ModelMap model) {
		
//		登入功能放入要改!
		Integer memId = ((Integer)req.getSession().getAttribute(null));
		
		//查詢資料
		List<NotificationVO> list = notificationService.getMemNoti(1);
		
		//轉交資料
		model.addAttribute("currentPage", "account");
		model.addAttribute("list", list);
		return "front-end/notification/memberNotification";
	}
	
	//顯示通知詳細資訊並更改讀取狀態
	@PostMapping("readedNoti")
	public String readedNoti(
		//取得請求資料
		@RequestParam("notiId") String notiNo,
		ModelMap model) {
		
		//查詢資料並修改狀態
		Integer notiId = Integer.valueOf(notiNo);
		NotificationVO notificationVO = notificationService.getOneNoti(notiId);
		notificationService.updateNotiStatus(notiId, (byte)1);
		
		//轉交資料
		model.addAttribute("currentPage", "account");
		model.addAttribute("notificationVO", notificationVO);
		return "front-end/notification/memberNotificationDetail";
	}
	
	//使用者移除通知
	@PostMapping("removeNotibyMember")
	public String removeNotibyMember(
		//取得請求資料
		@RequestParam("notiIds") String[] notiNos,
		ModelMap model) {
		
		//查詢資料後並刪除資料
//		登入功能放入要改!
		notificationService.updateNotiStatuses(notiNos, (byte)2);
		List<NotificationVO> list = notificationService.getMemNoti(1);
		
		//轉交資料
		model.addAttribute("currentPage", "account");
		model.addAttribute("list", list);
		return "front-end/notification/memberNotification";
		
	}
	
	////=====後台=====////
	//取得待發送的通知資料
	@GetMapping("/unSendNotification")
	public String unSendNotification(ModelMap model, HttpServletRequest req) {
		//查詢資料
		List<NotificationVO> list = notificationService.getAll();
		
		//轉交資料
		Date nowTime = new Date();
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("currentPage2", "unSendNotification");
		model.addAttribute("nowTime", nowTime);
		model.addAttribute("list", list);
		req.getSession().setAttribute("originalPage", "/notification/unSendNotification");
		return "back-end/notification/unSendNotification";
	}
	
	//通知資料編輯頁面
	@PostMapping("editNotification")
	public String editNotification(
		//取得請求資料
		@RequestParam("notiId") String notiNo,
		ModelMap model) {
		
		//查詢資料
		NotificationVO notificationVO = notificationService.getOneNoti(Integer.valueOf(notiNo));
		
		//轉交資料
		Date nowTime = new Date();
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("nowTime", nowTime);
		model.addAttribute("notificationVO", notificationVO);
		return "back-end/notification/editNotification";
	}
	
	//刪除通知
	@PostMapping("deleteNotification")
	public String deleteNotification(
		//取得請求資料
		@RequestParam("notiId") String notiNo,
		ModelMap model,
		HttpServletRequest req) {
		
		//刪除資料
		notificationService.updateNotiStatus(Integer.valueOf(notiNo), (byte)3);
		
		//轉交資料
		String originalPage = (String)req.getSession().getAttribute("originalPage");
				
		req.getSession().removeAttribute("originalPage"); // 完成後移除 Session 屬性
		
		// 根據 originalPage 進行重定向
        if ("/notification/unSendNotification".equals(originalPage)) {
            // 重定向到 /notification/unSendNotification，這會觸發 unSendNotification 方法並重新載入列表
            return "redirect:/notification/unSendNotification"; 
        } else if ("/notification/searchNotification".equals(originalPage)) {
            // 重定向到 /notification/searchNotification
            // 如果 searchNotification 的 GET 方法需要 session 中的 searchMap，
            // 則不需要特別傳遞參數，因為之前已經存入 session。
            // 但為了確保數據重新載入，通常會重定向到處理列表的 GET 請求。
            return "redirect:/notification/backDoSearchNotification";
        } else {
            // 如果 originalPage 不符合預期，可以重定向到一個預設頁面，例如首頁或錯誤頁面
            // 或者拋出異常，這取決於您的業務邏輯。
            return "redirect:/notification/unSendNotification"; // 預設重定向到待發送通知列表
        }
	}
	
	//搜尋頁面
	@GetMapping("searchNotification")
	public String searchNotification(HttpServletRequest req, ModelMap model) {
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("currentPage2", "searchNotification");
		return "back-end/notification/searchNotification";
	}
	
	//開始搜尋
	@PostMapping("doSearchNotification")
	public String doSearchNotification(HttpServletRequest req, ModelMap model) {
		req.getSession().setAttribute("originalPage", "/notification/searchNotification");
		req.getSession().removeAttribute("searchMap");
		
		Map<String, String[]> map = req.getParameterMap();

		List<NotificationVO> list = notificationService.getByCriteria(map);
		Date nowTime = new Date();
		model.addAttribute("nowTime", nowTime);
		model.addAttribute("list", list);
		model.addAttribute("map", map);
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("currentPage2", "searchNotification");
		req.getSession().setAttribute("searchMap", map);
		return "back-end/notification/searchNotification";
	}
	
	//返回查詢使用
	@GetMapping("backDoSearchNotification")
	public String backDoSearchNotification(HttpServletRequest req, ModelMap model) {
		req.getSession().setAttribute("originalPage", "/notification/searchNotification");
		Map<String, String[]> map = req.getParameterMap();
		List<NotificationVO> list = notificationService.getByCriteria(map);
		Date nowTime = new Date();
		model.addAttribute("nowTime", nowTime);
		model.addAttribute("list", list);
		model.addAttribute("map", map);
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("currentPage2", "searchNotification");
		req.getSession().setAttribute("searchMap", map);
		return "back-end/notification/searchNotification";
	}
		
	//修改通知資訊
	@PostMapping("updateNotification")
	public String updateNotification(
		//取得請求資料將資料組合成VO
		@RequestParam("notiId") Integer notiId,
		@RequestParam("memId") Integer memId,
		@RequestParam("notiStatus") Byte notiStatus,
		@RequestParam("notiSendAt") String notiSendAt,
		@RequestParam("notiTitle") String notiTitle,
		@RequestParam("notiCont") String notiCont,
		HttpServletRequest req,
		ModelMap model
			) {
		
		Timestamp now = new Timestamp(new Date().getTime());
//		Integer adminId = Integer.valueOf(req.getSession().getAttribute(管理員));
		Integer adminId = 1;
		
		NotificationVO notificationVO = new NotificationVO(notiId, notiTitle, notiCont, memId, notiStatus, adminId, now, datePrase(notiSendAt));
		
		//修改資訊
		notificationService.updateNoti(notificationVO);
		
		//轉交資料
		String originalPage = (String)req.getSession().getAttribute("originalPage");
		
		req.getSession().removeAttribute("originalPage"); // 完成後移除 Session 屬性

        // 根據 originalPage 進行重定向
        if ("/notification/unSendNotification".equals(originalPage)) {
            // 重定向到 /notification/unSendNotification，這會觸發 unSendNotification 方法並重新載入列表
            return "redirect:/notification/unSendNotification"; 
        } else if ("/notification/searchNotification".equals(originalPage)) {
            // 重定向到 /notification/searchNotification
            // 如果 searchNotification 的 GET 方法需要 session 中的 searchMap，
            // 則不需要特別傳遞參數，因為之前已經存入 session。
            // 但為了確保數據重新載入，通常會重定向到處理列表的 GET 請求。
            return "redirect:/notification/backDoSearchNotification";
        } else {
            // 如果 originalPage 不符合預期，可以重定向到一個預設頁面，例如首頁或錯誤頁面
            // 或者拋出異常，這取決於您的業務邏輯。
            return "redirect:/notification/unSendNotification"; // 預設重定向到待發送通知列表
        }
		
	}
	
	//新增通知頁面
	@GetMapping("addNotification")
	public String addNotification (ModelMap model) {
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("currentPage2", "addNotification");
		return "back-end/notification/addNotification";
	}
	
	//新增通知
	@PostMapping("doAddNotification")
	public String doAddNotification (
		//取得請求資料
		@RequestParam("memIds") Integer[] memIds,
		@RequestParam("notiSendAt") String notiSendAt,
		@RequestParam("notiTitle") String notiTitle,
		@RequestParam("notiCont") String notiCont,
		HttpServletRequest req,
		ModelMap model) {
	
//			等會員功能後記得加進去
//			Integer adminId = (Integer)req.getSession().getAttribute("??");
		Integer adminId = 1;
		
	
		//將資料組合成VO後新增通知
		for(Integer memId : memIds) {
			NotificationVO notificationVO = new NotificationVO(notiTitle, notiCont, memId, adminId, datePrase(notiSendAt));
			notificationService.addOneNoti(notificationVO);
		}
		
		model.addAttribute("suessesMsg", "成功新增" + memIds.length + "筆通知!");
		model.addAttribute("currentPage", "notifications");
		model.addAttribute("currentPage2", "addNotification");
		return "back-end/notification/addNotification";
	}
	
	//時間轉換String to Timestamp
	public Timestamp datePrase(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		Date prase = null;
		try {
			prase = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Timestamp time = new Timestamp(prase.getTime());
		return time;
	}
	
}
