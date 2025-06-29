package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.security.AuthService;
import com.toiukha.members.model.MembersService;
import com.toiukha.participant.model.PartDTO;
import com.toiukha.participant.model.PartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/act/group")
public class GroupHomeController {
    @Autowired private ActService actService;
    @Autowired private PartService partService;
    @Autowired private AuthService authService;
    //    @Autowired private ExpenseSheetService expenseSheetService;
    @Autowired private MembersService membersService;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/{actId}/home")
    public String groupHome(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        System.out.println("=== GroupHome Debug ===");
        System.out.println("Requested ActId: " + actId);

        ActVO act = actService.getOneAct(actId);
        System.out.println("Found Act: " + (act != null ? "Yes" : "No"));

        if (act == null) {
            System.out.println("Act not found, redirecting to search");
            return "redirect:/act/member/search";
        }

        System.out.println("Act Status: " + act.getRecruitStatus());
        System.out.println("Expected Status: " + ActStatus.FULL.getValue());

        if (act.getRecruitStatus() != ActStatus.FULL.getValue()) {
            System.out.println("Act status is not FULL, redirecting to search");
            return "redirect:/act/member/search";
        }

        AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
        System.out.println("Member Info: " + memberInfo.getMemId() + ", LoggedIn: " + memberInfo.isLoggedIn());

        if (!memberInfo.isLoggedIn()) {
            System.out.println("Not logged in, redirecting to login");
            return "redirect:/members/login";
        }

        boolean isHost = act.getHostId().equals(memberInfo.getMemId());
        boolean isParticipant = partService.getParticipants(actId).contains(memberInfo.getMemId());

        System.out.println("Is Host: " + isHost);
        System.out.println("Is Participant: " + isParticipant);
        System.out.println("Act HostId: " + act.getHostId());
        System.out.println("Current MemberId: " + memberInfo.getMemId());
        System.out.println("Participants: " + partService.getParticipants(actId));

        // 修改權限檢查：團主或參加者都可以進入
        if (!isHost && !isParticipant) {
            System.out.println("Not host or participant, redirecting to search");
            return "redirect:/act/member/search";
        }

        System.out.println("All checks passed, rendering groupHome");
        System.out.println("================================");

        // 查詢所有參加者詳細資料（含狀態與姓名）
        List<PartDTO> participantList = partService.getParticipantsAsDTO(actId);
        List<PartDTO> filteredList;
        if (isHost) {
            // 團主：顯示所有未退出成員
            filteredList = participantList.stream()
                    .filter(dto -> dto.getJoinStatus() != null && dto.getJoinStatus() != 3)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // 團員：只顯示已參加成員
            filteredList = participantList.stream()
                    .filter(dto -> dto.getJoinStatus() != null && dto.getJoinStatus() == 1)
                    .collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("memberList", filteredList);
        model.addAttribute("act", act);
        model.addAttribute("isHost", isHost);
//        model.addAttribute("expenseSheetUrl", expenseSheetService.getSheetUrl(actId));
        return "front-end/groupactivity/groupHome";
    }
    
    /**
     * 團隊聊天室頁面
     */
    @GetMapping("/{actId}/chat")
    public String groupChatRoom(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        System.out.println("=== GroupChatRoom Debug ===");
        System.out.println("Requested ActId: " + actId);

        ActVO act = actService.getOneAct(actId);
        System.out.println("Found Act: " + (act != null ? "Yes" : "No"));

        if (act == null) {
            System.out.println("Act not found, redirecting to search");
            return "redirect:/act/member/search";
        }

        AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
        System.out.println("Member Info: " + memberInfo.getMemId() + ", LoggedIn: " + memberInfo.isLoggedIn());

        if (!memberInfo.isLoggedIn()) {
            System.out.println("Not logged in, redirecting to login");
            return "redirect:/members/login";
        }

        boolean isHost = act.getHostId().equals(memberInfo.getMemId());
        boolean isParticipant = partService.getParticipants(actId).contains(memberInfo.getMemId());

        System.out.println("Is Host: " + isHost);
        System.out.println("Is Participant: " + isParticipant);

        // 權限檢查：團主或參加者都可以進入聊天室
        if (!isHost && !isParticipant) {
            System.out.println("Not host or participant, redirecting to search");
            return "redirect:/act/member/search";
        }

        System.out.println("All checks passed, rendering groupChatRoom");
        System.out.println("================================");

        model.addAttribute("act", act);
        model.addAttribute("isHost", isHost);
        model.addAttribute("currentMemberId", memberInfo.getMemId());
        
        // 獲取當前用戶姓名
        String currentMemberName = "";
        try {
            var currentMember = membersService.getOneMember(memberInfo.getMemId());
            if (currentMember != null) {
                currentMemberName = currentMember.getMemName();
            }
        } catch (Exception e) {
            System.err.println("獲取用戶姓名錯誤: " + e.getMessage());
        }
        model.addAttribute("currentMemberName", currentMemberName);
        
        return "front-end/groupactivity/groupChatRoom";
    }
    
    /**
     * 獲取聊天歷史記錄 API
     */
    @GetMapping("/{actId}/chat/history")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(@PathVariable Integer actId, HttpServletRequest request) {
        try {
            // 權限檢查
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of("error", "請先登入"));
            }

            ActVO act = actService.getOneAct(actId);
            if (act == null) {
                return ResponseEntity.status(404).body(Map.of("error", "活動不存在"));
            }

            boolean isHost = act.getHostId().equals(memberInfo.getMemId());
            boolean isParticipant = partService.getParticipants(actId).contains(memberInfo.getMemId());

            if (!isHost && !isParticipant) {
                return ResponseEntity.status(403).body(Map.of("error", "無權限查看此聊天室"));
            }

            // 從 Redis 獲取歷史記錄
            String key = "chat:history:" + actId;
            List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
            
            if (messages == null) {
                messages = new ArrayList<>();
            }
            
            System.out.println("載入團隊 " + actId + " 的歷史記錄，共 " + messages.size() + " 條");
            
            return ResponseEntity.ok(Map.of(
                "actId", actId,
                "messages", messages,
                "count", messages.size()
            ));

        } catch (Exception e) {
            System.err.println("獲取聊天歷史錄錯誤: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "伺服器錯誤"));
        }
    }
} 