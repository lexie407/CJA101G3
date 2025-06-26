package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 開發測試用 Controller
 * 提供開發階段的便利功能，如切換假用戶等
 */
@RestController
@RequestMapping("/dev")
public class ActTestController {

	@Autowired
	private ActService actService;

	// 導向首頁畫面 (templates/index.html)
	@GetMapping("/")
	public String testMethod() {
		return "thymeleaf_template";
	}

	// 測試寫入DB，回傳 JSON 結果
	@GetMapping("/test")
	@ResponseBody
	public ActVO testInsert() {
		return actService.saveTestAct();
	}

	/**
	 * 開發模式：切換 session 中的假用戶
	 * 使用方式：/dev/login/38 切換到 38 號用戶
	 * 
	 * @param memId 要切換的會員ID
	 * @param session HTTP Session
	 * @return 切換結果
	 */
	@GetMapping("/login/{memId}")
	public ResponseEntity<Map<String, Object>> switchDevUser(
			@PathVariable Integer memId, 
			HttpSession session) {
		
		// 設定假用戶格式的 session
		String devUser = "DEV_USER_" + memId;
		session.setAttribute("member", devUser);
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "已切換到開發用戶 " + memId);
		response.put("devUser", devUser);
		response.put("memId", memId);
		
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 開發模式：查看當前 session 用戶
	 * 
	 * @param session HTTP Session
	 * @return 當前用戶資訊
	 */
	@GetMapping("/current")
	public ResponseEntity<Map<String, Object>> getCurrentDevUser(HttpSession session) {
		Object member = session.getAttribute("member");
		
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		
		if (member instanceof String && ((String) member).startsWith("DEV_USER_")) {
			// 開發模式：假用戶格式
			String devUser = (String) member;
			Integer memId = Integer.parseInt(devUser.substring(9));
			result.put("devUser", devUser);
			result.put("memId", memId);
			result.put("type", "dev");
			result.put("message", "當前為開發用戶 " + memId);
		} else if (member instanceof MembersVO) {
			// 生產模式：真實會員物件
			MembersVO memberVO = (MembersVO) member;
			result.put("memId", memberVO.getMemId());
			result.put("memName", memberVO.getMemName());
			result.put("type", "production");
			result.put("message", "當前為正式會員 " + memberVO.getMemName());
		} else {
			// 未登入
			result.put("type", "not_logged_in");
			result.put("message", "未登入");
		}
		
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 開發模式：清除 session 用戶（登出）
	 * 
	 * @param session HTTP Session
	 * @return 登出結果
	 */
	@GetMapping("/logout")
	public ResponseEntity<Map<String, Object>> logoutDevUser(HttpSession session) {
		session.removeAttribute("member");
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "已登出開發用戶");
		
		return ResponseEntity.ok(response);
	}
}
