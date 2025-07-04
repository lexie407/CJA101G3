package com.toiukha.forum.controller;

import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 這個Controller負責處理與Session相關的請求，主要是用來獲取當前登入用戶的資訊
@RestController
@RequestMapping("/api/session")
public class SessionInfoController {

    // 獲取當前登入會員的id跟name，以及是否有登入(success)
    @GetMapping("/currentMember")
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        MembersVO member = (MembersVO) session.getAttribute("member");

        if (member == null) {
            response.put("success", false);
            response.put("message", "尚未登入");
        } else {
            response.put("success", true);
            response.put("memId", member.getMemId());
            response.put("memName", member.getMemName());
        }

        return response;
    }
}
