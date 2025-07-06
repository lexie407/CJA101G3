package com.toiukha.members.controller;

import com.toiukha.members.model.MembersVO;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 會員模組 REST API 控制器
 * 處理會員相關的資料交換
 */
@RestController
@RequestMapping("/api/members")
public class MembersRestController {

    /**
     * 檢查用戶是否已登入
     * @param session HTTP Session
     * @return 包含登入狀態的Map
     */
    @GetMapping("/check-login")
    public Map<String, Object> checkLogin(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        MembersVO member = (MembersVO) session.getAttribute("member");
        
        if (member == null) {
            response.put("isLoggedIn", false);
            response.put("message", "尚未登入");
        } else {
            response.put("isLoggedIn", true);
            response.put("memId", member.getMemId());
            response.put("memName", member.getMemName());
        }
        
        return response;
    }
} 