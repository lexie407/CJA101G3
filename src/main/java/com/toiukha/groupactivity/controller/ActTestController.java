package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.members.model.MembersService;
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

	@Autowired
	private MembersService membersService;

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
	 * 開發模式：切換到真實會員（推薦，與新登入系統完全相容）
	 * 使用方式：/dev/login/38 切換到真實的 38 號會員
	 */
	@GetMapping("/login/{memId}")
	public ResponseEntity<Map<String, Object>> switchDevUser(
			@PathVariable Integer memId, 
			HttpSession session) {
		// 預設使用真實會員
		return switchToRealUser(memId, session);
	}

	/**
	 * 開發模式：切換到真實會員
	 * 使用方式：/dev/login/real/38 切換到真實的 38 號會員
	 */
	@GetMapping("/login/real/{memId}")
	public ResponseEntity<Map<String, Object>> switchToRealUser(
			@PathVariable Integer memId, 
			HttpSession session) {
		
		// 從資料庫取得真實會員資料
		MembersVO member = membersService.getOneMember(memId);
		
		Map<String, Object> response = new HashMap<>();
		
		if (member == null) {
			response.put("success", false);
			response.put("message", "找不到會員 ID " + memId);
			return ResponseEntity.badRequest().body(response);
		}
		
		// 設定真實會員物件到 session
		session.setAttribute("member", member);
		
		response.put("success", true);
		response.put("message", "已切換到真實會員：" + member.getMemName());
		response.put("memId", member.getMemId());
		response.put("memName", member.getMemName());
		response.put("type", "real");
		
		return ResponseEntity.ok(response);
	}

	/**
	 * 開發模式：切換到假用戶（保留相容性）
	 * 使用方式：/dev/login/fake/38 切換到假的 38 號用戶
	 */
	@GetMapping("/login/fake/{memId}")
	public ResponseEntity<Map<String, Object>> switchToFakeUser(
			@PathVariable Integer memId, 
			HttpSession session) {
		
		// 設定假用戶格式的 session（保留舊功能）
		String devUser = "DEV_USER_" + memId;
		session.setAttribute("member", devUser);
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "已切換到假用戶 " + memId);
		response.put("devUser", devUser);
		response.put("memId", memId);
		response.put("type", "fake");
		
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 升級版：查看當前用戶（支援兩種格式）
	 * 
	 * @param session HTTP Session
	 * @return 當前用戶資訊
	 */
	@GetMapping("/current")
	public ResponseEntity<Map<String, Object>> getCurrentDevUser(HttpSession session) {
		Object member = session.getAttribute("member");
		
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		
		if (member instanceof MembersVO) {
			// 真實會員物件
			MembersVO memberVO = (MembersVO) member;
			result.put("memId", memberVO.getMemId());
			result.put("memName", memberVO.getMemName());
			result.put("memEmail", memberVO.getMemEmail());
			result.put("memStatus", memberVO.getMemStatus());
			result.put("type", "real");
			result.put("message", "當前為真實會員：" + memberVO.getMemName());
		} else if (member instanceof String && ((String) member).startsWith("DEV_USER_")) {
			// 假用戶字串格式（相容舊系統）
			String devUser = (String) member;
			Integer memId = Integer.parseInt(devUser.substring(9));
			result.put("devUser", devUser);
			result.put("memId", memId);
			result.put("type", "fake");
			result.put("message", "當前為假用戶 " + memId);
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
