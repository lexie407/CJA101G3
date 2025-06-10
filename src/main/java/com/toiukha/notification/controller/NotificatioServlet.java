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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;

@Controller
@Validated
@RequestMapping("/notification")
public class NotificatioServlet {

	@Autowired
	NotificationService notificationService;
	
	////=====前台=====////
	//取得該會員的通知資料
	@PostMapping("getMemNoti")
	public String getAllNoti(
		//取得請求資料
		@RequestParam("memId") String memId, 
		ModelMap model) {
		
		//查詢資料
		List<NotificationVO> list = notificationService.getMemNoti(Integer.valueOf(memId));
		
		//轉交資料
		model.addAttribute("list", list);
		return "back-end/notification/notificationofMember";
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
		model.addAttribute("notificationVO", notificationVO);
		return "back-end/notification/notificationDetail";
	}
	
	//使用者移除通知
	@PostMapping("removeNotibyMember")
	public String removeNotibyMember(
		//取得請求資料
		@RequestParam("notiId") String notiNo,
		ModelMap model) {
		
		//查詢資料後並刪除資料
		Integer notiId = Integer.valueOf(notiNo);
		NotificationVO notificationVO = notificationService.getOneNoti(notiId);
		notificationService.updateNotiStatus(notiId, (byte)2);
		
		//轉交資料
		model.addAttribute("notificationVO", notificationVO);
		return "back-end/notification/notificationofMember";
		
	}
}
