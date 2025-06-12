package com.toiukha.notification.controller;

import java.sql.Timestamp;
import java.util.List;

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
	
	@GetMapping("/test")
	public String test(ModelMap model) {
		return "front-end/notification/thymeleaf_template";
	}
}
