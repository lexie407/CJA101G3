package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.security.AuthService;
import com.toiukha.groupactivity.service.ActService;
import com.toiukha.members.model.MembersService;
import com.toiukha.participant.model.PartDTO;
import com.toiukha.participant.model.PartService;
import com.toiukha.participant.model.PartRepository;
import com.toiukha.participant.model.PartVO;
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
    @Autowired private ActService actSvc;
    @Autowired private PartService partSvc;
    @Autowired private AuthService authSvc;
    //    @Autowired private ExpenseSheetService expenseSheetService;
    @Autowired private MembersService membersSvc;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private PartRepository partRepo;

    @GetMapping("/{actId}/home")
    public String groupHome(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        ActVO act = actSvc.getOneAct(actId);

        if (act == null) {
            return "redirect:/act/member/search";
        }

        // 檢查活動是否被凍結
        if (act.getRecruitStatus() == 4) {
            return "redirect:/act/member/search?error=frozen";
        }

        // 檢查活動是否為公開活動（僅非團主且非參加者時禁止進入）
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(request.getSession());
        if (!memberInfo.isLoggedIn()) {
            return "redirect:/members/login";
        }
        boolean isHost = act.getHostId().equals(memberInfo.getMemId());
        boolean isParticipant = partSvc.getParticipants(actId).contains(memberInfo.getMemId());
        // 僅允許團主或未被剔除的團員進入
        if (!isHost) {
            java.util.Optional<PartVO> partOpt = partRepo.findByActIdAndMemId(actId, memberInfo.getMemId());
            if (partOpt.isEmpty() || partOpt.get().getJoinStatus() == null || partOpt.get().getJoinStatus() != 1) {
                // 非團主且不是已參加狀態，導回搜尋
                return "redirect:/act/member/search";
            }
        }
        if (act.getIsPublic() != 1 && !isHost && !isParticipant) {
            return "redirect:/act/member/search";
        }

        // 查詢所有參加者詳細資料（含狀態與姓名）
        List<PartDTO> participantList = partSvc.getParticipantsAsDTO(actId);
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
        model.addAttribute("currentMemberId", memberInfo.getMemId());
//        model.addAttribute("expenseSheetUrl", expenseSheetService.getSheetUrl(actId));
        return "front-end/groupactivity/groupHome";
    }
    
    /**
     * 團隊聊天室頁面
     */
    @GetMapping("/{actId}/chat")
    public String groupChatRoom(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        ActVO act = actSvc.getOneAct(actId);

        if (act == null) {
            return "redirect:/act/member/search";
        }

        // 檢查活動是否被凍結
        if (act.getRecruitStatus() == 4) {
            return "redirect:/act/member/search?error=frozen";
        }

        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(request.getSession());

        if (!memberInfo.isLoggedIn()) {
            return "redirect:/members/login";
        }

        boolean isHost = act.getHostId().equals(memberInfo.getMemId());
        boolean isParticipant = partSvc.getParticipants(actId).contains(memberInfo.getMemId());

        // 權限檢查：團主或參加者都可以進入聊天室
        if (!isHost && !isParticipant) {
            return "redirect:/act/member/search";
        }

        model.addAttribute("act", act);
        model.addAttribute("isHost", isHost);
        model.addAttribute("currentMemberId", memberInfo.getMemId());
        
        // 獲取當前用戶姓名
        String currentMemberName = "";
        try {
            var currentMember = membersSvc.getOneMember(memberInfo.getMemId());
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
            AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(request.getSession());
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of("error", "請先登入"));
            }

            ActVO act = actSvc.getOneAct(actId);
            if (act == null) {
                return ResponseEntity.status(404).body(Map.of("error", "活動不存在"));
            }

            // 檢查活動是否被凍結
            if (act.getRecruitStatus() == 4) {
                return ResponseEntity.status(403).body(Map.of("error", "活動已被凍結，無法進入"));
            }

            boolean isHost = act.getHostId().equals(memberInfo.getMemId());
            boolean isParticipant = partSvc.getParticipants(actId).contains(memberInfo.getMemId());

            if (!isHost && !isParticipant) {
                return ResponseEntity.status(403).body(Map.of("error", "無權限查看此聊天室"));
            }

            // 從 Redis 獲取歷史記錄
            String key = "chat:history:" + actId;
            List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
            
            if (messages == null) {
                messages = new ArrayList<>();
            }
            
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
    
    /**
     * 分帳紀錄頁面
     */
    @GetMapping("/{actId}/expense")
    public String expenseRecord(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        ActVO act = actSvc.getOneAct(actId);

        if (act == null) {
            return "redirect:/act/member/search";
        }

        // 檢查活動是否被凍結
        if (act.getRecruitStatus() == 4) {
            return "redirect:/act/member/search?error=frozen";
        }

        // 檢查活動是否為公開活動（僅非團主且非參加者時禁止進入）
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(request.getSession());
        if (!memberInfo.isLoggedIn()) {
            return "redirect:/members/login";
        }
        boolean isHost = act.getHostId().equals(memberInfo.getMemId());
        boolean isParticipant = partSvc.getParticipants(actId).contains(memberInfo.getMemId());
        if (act.getIsPublic() != 1 && !isHost && !isParticipant) {
            return "redirect:/act/member/search";
        }

        // 查詢所有參加者詳細資料（含狀態與姓名）
        List<PartDTO> participantList = partSvc.getParticipantsAsDTO(actId);
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
        
        return "front-end/groupactivity/expenseRecord";
    }
} 