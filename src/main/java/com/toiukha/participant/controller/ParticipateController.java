package com.toiukha.participant.controller;

import com.toiukha.participant.model.ParticipateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 參加者控制器，提供報名相關 API
 */
@RestController
@RequestMapping("/api/participate")
public class ParticipateController {

    @Autowired
    private ParticipateService participateSvc;

    /**
     * 報名活動
     * @param actId 活動ID
     * @param memId 會員ID
     * @return 報名結果
     */
    @PostMapping("/{actId}/signup/{memId}")
    public String signup(@PathVariable Integer actId, @PathVariable Integer memId, HttpServletRequest request) {
        // 取得session並檢查登入狀態，未來可配合Filter或AOP統一處理
        HttpSession session = request.getSession();
        Object member = session.getAttribute("member");
        // TODO: 若未登入可回傳未授權訊息
        // if (member == null) { return "unauthorized"; }
        
        // 測試用：印出session中的會員ID
        System.out.println("=== Test: Current Session Member ID ===");
        System.out.println("URI: /api/participate/" + actId + "/signup/" + memId);
        System.out.println("Member: " + (member != null ? member : "Not logged in"));
        System.out.println("=====================================");
        
        participateSvc.signup(actId, memId);
        return "ok";
    }

    /**
     * 取消報名
     * @param actId 活動ID
     * @param memId 會員ID
     * @return 取消結果
     */
    @DeleteMapping("/{actId}/signup/{memId}")
    public String cancel(@PathVariable Integer actId, @PathVariable Integer memId, HttpServletRequest request) {
        // 取得session並檢查登入狀態
        HttpSession session = request.getSession();
        Object member = session.getAttribute("member");
        // TODO: 若未登入可回傳未授權訊息
        // if (member == null) { return "unauthorized"; }
        
        // 測試用：印出session中的會員ID
        System.out.println("=== Test: Current Session Member ID ===");
        System.out.println("URI: /api/participate/" + actId + "/signup/" + memId);
        System.out.println("Member: " + (member != null ? member : "Not logged in"));
        System.out.println("=====================================");
        
        participateSvc.cancel(actId, memId);
        return "canceled";
    }

    /**
     * 取得活動參加者名單
     * @param actId 活動ID
     * @return 參加者會員ID列表
     */
    @GetMapping("/{actId}/members")
    public List<Integer> members(@PathVariable Integer actId) {
        return participateSvc.getParticipants(actId);
    }
}
