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
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> signup(@PathVariable Integer actId, @PathVariable Integer memId, HttpServletRequest request) {
        try {
            // 1. 基本參數驗證
            if (actId == null || actId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "無效的活動ID"
                ));
            }
            
            if (memId == null || memId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "無效的會員ID"
                ));
            }
            
            // 2. 權限驗證
            HttpSession session = request.getSession();
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入",
                    "redirectTo", "/members/login"
                ));
            }
            
            if (!memberInfo.getMemId().equals(memId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "您只能為自己報名活動"
                ));
            }
            
            // 3. 檢查活動是否存在
            ActVO activity = actRepository.findById(actId).orElse(null);
            if (activity == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "活動不存在"
                ));
            }
            
            // 4. 執行報名
            partSvc.signup(actId, memId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "報名成功",
                "data", Map.of(
                    "actId", actId,
                    "actName", activity.getActName()
                )
            ));
            
        } catch (IllegalStateException e) {
            // 業務邏輯錯誤（如已報名、人數已滿等）
            String errorMessage = e.getMessage();
            String userFriendlyMessage;
            
            // 錯誤訊息
            if (errorMessage.contains("團主無需報名")) {
                userFriendlyMessage = "您是活動團主，無需報名自己的活動";
            } else if (errorMessage.contains("已報名")) {
                userFriendlyMessage = "您已經報名此活動";
            } else if (errorMessage.contains("人數已滿")) {
                userFriendlyMessage = "活動人數已滿，無法報名";
            } else if (errorMessage.contains("未開放報名")) {
                userFriendlyMessage = "活動尚未開放報名或已結束報名";
            } else {
                userFriendlyMessage = errorMessage;
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", userFriendlyMessage,
                "errorType", "BUSINESS_LOGIC_ERROR"
            ));
            
        } catch (Exception e) {
            // 系統錯誤
            System.err.println("報名活動時發生系統錯誤 (actId: " + actId + ", memId: " + memId + "): " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統繁忙，請稍後再試",
                "errorType", "SYSTEM_ERROR"
            ));
        }
    }

    /**
     * 取消報名 
     * @param actId 活動ID
     * @param memId 會員ID
     * @return 取消結果
     */
    @DeleteMapping("/{actId}/signup/{memId}")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Integer actId, @PathVariable Integer memId, HttpServletRequest request) {
        try {
            // 1. 基本參數驗證
            if (actId == null || actId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "無效的活動ID"
                ));
            }
            
            if (memId == null || memId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "無效的會員ID"
                ));
            }
            
            // 2. 權限驗證
            HttpSession session = request.getSession();
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入",
                    "redirectTo", "/members/login"
                ));
            }
            
            if (!memberInfo.getMemId().equals(memId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "您只能取消自己的報名"
                ));
            }
            
            // 3. 檢查活動是否存在
            ActVO activity = actRepository.findById(actId).orElse(null);
            if (activity == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "活動不存在"
                ));
            }
            
            // 4. 執行取消報名
            partSvc.cancel(actId, memId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "取消報名成功",
                "data", Map.of(
                    "actId", actId,
                    "actName", activity.getActName()
                )
            ));
            
        } catch (IllegalStateException e) {
            // 業務邏輯錯誤
            String errorMessage = e.getMessage();
            String userFriendlyMessage;
            
            if (errorMessage.contains("團主不能退出")) {
                userFriendlyMessage = "團主不能退出自己的活動，如需取消活動請聯繫管理員";
            } else if (errorMessage.contains("尚未報名")) {
                userFriendlyMessage = "您尚未報名此活動";
            } else if (errorMessage.contains("不允許退出")) {
                userFriendlyMessage = "此活動不允許退出";
            } else {
                userFriendlyMessage = errorMessage;
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", userFriendlyMessage,
                "errorType", "BUSINESS_LOGIC_ERROR"
            ));
            
        } catch (Exception e) {
            // 系統錯誤
            System.err.println("取消報名時發生系統錯誤 (actId: " + actId + ", memId: " + memId + "): " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統繁忙，請稍後再試",
                "errorType", "SYSTEM_ERROR"
            ));
        }
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
