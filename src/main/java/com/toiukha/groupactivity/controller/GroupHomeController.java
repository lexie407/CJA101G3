package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.security.AuthService;
//import com.toiukha.groupactivity.service.ExpenseSheetService;
import com.toiukha.members.model.MembersService;
import com.toiukha.participant.model.ParticipantDTO;
import com.toiukha.participant.model.ParticipateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/act/group")
public class GroupHomeController {
    @Autowired private ActService actService;
    @Autowired private ParticipateService participateService;
    @Autowired private AuthService authService;
//    @Autowired private ExpenseSheetService expenseSheetService;
    @Autowired private MembersService membersService;

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
        System.out.println("Expected Status: " + ActStatus.FULL);
        
        if (act.getRecruitStatus() != ActStatus.FULL) {
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
        boolean isParticipant = participateService.getParticipants(actId).contains(memberInfo.getMemId());
        
        System.out.println("Is Host: " + isHost);
        System.out.println("Is Participant: " + isParticipant);
        System.out.println("Act HostId: " + act.getHostId());
        System.out.println("Current MemberId: " + memberInfo.getMemId());
        System.out.println("Participants: " + participateService.getParticipants(actId));
        
        if (!isHost && !isParticipant) {
            System.out.println("Not host or participant, redirecting to search");
            return "redirect:/act/member/search";
        }
        
        System.out.println("All checks passed, rendering groupHome");
        System.out.println("================================");
        
        // 查詢所有參加者詳細資料（含狀態與姓名）
        List<ParticipantDTO> participantList = participateService.getParticipantsAsDTO(actId);
        List<ParticipantDTO> filteredList;
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
} 