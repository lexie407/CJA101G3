package com.toiukha.participant.controller;

import com.toiukha.groupactivity.model.ActRepository;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.security.AuthService;
import com.toiukha.participant.model.PartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 參加者控制器，提供報名相關 API
 */
@RestController
@RequestMapping("/api/participate")
public class ParticipateController {

    @Autowired
    private PartService partSvc;

    @Autowired
    private ActRepository actRepository;

    @Autowired
    private AuthService authService;

    /**
     * 報名活動
     * @param actId 活動ID
     * @param memId 會員ID
     * @return 報名結果
     */
    @PostMapping("/{actId}/signup/{memId}")
    public String signup(@PathVariable Integer actId, @PathVariable Integer memId, HttpServletRequest request) {
        // 取得session並檢查登入狀態
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        // TODO: 若未登入可回傳未授權訊息
        // if (!memberInfo.isLoggedIn()) { return "unauthorized"; }
        
        // 測試用：印出session中的會員ID
        System.out.println("=== Test: Current Session Member ID ===");
        System.out.println("URI: /api/participate/" + actId + "/signup/" + memId);
        System.out.println("Member ID: " + (memberInfo.isLoggedIn() ? memberInfo.getMemId() : "Not logged in"));
        System.out.println("Member Name: " + (memberInfo.isLoggedIn() ? memberInfo.getMemName() : "N/A"));
        System.out.println("=====================================");
        
        partSvc.signup(actId, memId);
        return "signed up";
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
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        // TODO: 若未登入可回傳未授權訊息
        // if (!memberInfo.isLoggedIn()) { return "unauthorized"; }
        
        // 測試用：印出session中的會員ID
        System.out.println("=== Test: Current Session Member ID ===");
        System.out.println("URI: /api/participate/" + actId + "/signup/" + memId);
        System.out.println("Member ID: " + (memberInfo.isLoggedIn() ? memberInfo.getMemId() : "Not logged in"));
        System.out.println("Member Name: " + (memberInfo.isLoggedIn() ? memberInfo.getMemName() : "N/A"));
        System.out.println("=====================================");
        
        partSvc.cancel(actId, memId);
        return "canceled";
    }

    /**
     * 取得活動參加者名單
     * @param actId 活動ID
     * @return 參加者會員ID列表
     */
    @GetMapping("/{actId}/members")
    public List<Integer> members(@PathVariable Integer actId) {
        return partSvc.getParticipants(actId);
    }

    /**
     * 團主更新成員狀態
     */
    @PutMapping("/{actId}/members/{memId}/status")
    public ResponseEntity<String> updateMemberStatus(
            @PathVariable Integer actId,
            @PathVariable Integer memId,
            @RequestParam Byte joinStatus,
            HttpServletRequest request) {
        
        // 檢查權限：只有團主可以更新成員狀態
        ActVO act = actRepository.findById(actId).orElse(null);
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        
        if (act == null || !memberInfo.isLoggedIn() || !act.getHostId().equals(memberInfo.getMemId())) {
            return ResponseEntity.badRequest().body("無權限執行此操作");
        }
        
        partSvc.updateJoinStatus(actId, memId, joinStatus);
        return ResponseEntity.ok("成員狀態更新成功");
    }

}
