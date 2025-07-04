package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.GroupChatMsgDTO;
import com.toiukha.groupactivity.service.GroupChatService;
import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/*
 * Spring WebSocket STOMP 訊息控制器
 * 處理聊天室的訊息發送和接收
 */
@Controller
public class GroupChatController {

    @Autowired
    private GroupChatService groupChatService;

    // 處理聊天訊息發送 - 客戶端: /app/chat.sendMessage/{actId} -> 廣播: /topic/public/{actId}
    @MessageMapping("/chat.sendMessage/{actId}")
    @SendTo("/topic/public/{actId}")
    public GroupChatMsgDTO sendMessage(@DestinationVariable Integer actId,
                                       GroupChatMsgDTO groupChatMsgDTO,
                                       SimpMessageHeaderAccessor headerAccessor) {
        try {
            MembersVO member = getMemberVO(headerAccessor);
            if (member == null) {
                return GroupChatMsgDTO.createSystemMessage("請先登入", actId);
            }

            // 驗證用戶權限
            if (!groupChatService.checkTeamMembership(actId, member.getMemId())) {
                return GroupChatMsgDTO.createSystemMessage("無權限訪問此聊天室", actId);
            }

            // 設置訊息信息
            groupChatMsgDTO.setUserName(member.getMemName());
            groupChatMsgDTO.setActId(actId);
            groupChatMsgDTO.setMemberId(member.getMemId());
            groupChatMsgDTO.setTimestamp(System.currentTimeMillis());
            groupChatMsgDTO.setType("chat");

            groupChatService.saveMessageToRedis(actId, groupChatMsgDTO); // 儲存到Redis

            return groupChatMsgDTO;

        } catch (Exception e) {
            System.err.println("處理聊天訊息錯誤: " + e.getMessage());
            e.printStackTrace();
            return GroupChatMsgDTO.createSystemMessage("訊息發送失敗", actId);
        }
    }

    // 處理用戶加入聊天室 - 客戶端: /app/chat.addUser/{actId} -> 廣播: /topic/public/{actId}
    @MessageMapping("/chat.addUser/{actId}")
    @SendTo("/topic/public/{actId}")
    public GroupChatMsgDTO addUser(@DestinationVariable Integer actId,
                                   GroupChatMsgDTO groupChatMsgDTO,
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            MembersVO member = getMemberVO(headerAccessor);
            if (member == null) {
                return GroupChatMsgDTO.createSystemMessage("用戶未登入", actId);
            }

            // 驗證用戶權限
            if (!groupChatService.checkTeamMembership(actId, member.getMemId())) {
                return GroupChatMsgDTO.createSystemMessage("無權限訪問此聊天室", actId);
            }

            // 在WebSocket session中存儲用戶信息
            headerAccessor.getSessionAttributes().put("username", member.getMemName());
            headerAccessor.getSessionAttributes().put("actId", actId);
            headerAccessor.getSessionAttributes().put("memberId", member.getMemId());

            GroupChatMsgDTO joinMessage = GroupChatMsgDTO.createJoinLeaveMessage(member.getMemName(), true, actId);

            return joinMessage;

        } catch (Exception e) {
            System.err.println("處理用戶加入錯誤: " + e.getMessage());
            e.printStackTrace();
            return GroupChatMsgDTO.createSystemMessage("加入聊天室失敗", actId);
        }
    }

    // 優先從WebSocket session attributes取得memberVO
    private MembersVO getMemberVO(SimpMessageHeaderAccessor headerAccessor) {
        try {
            Object memberObj = headerAccessor.getSessionAttributes().get("memberVO");
            if (memberObj instanceof MembersVO) {
                return (MembersVO) memberObj;
            }
            // 相容舊邏輯：從HttpSession取得
            HttpSession httpSession = (HttpSession) headerAccessor.getSessionAttributes().get("HTTP_SESSION");
            if (httpSession != null) {
                Object sessionMember = httpSession.getAttribute("member");
                if (sessionMember instanceof MembersVO) {
                    return (MembersVO) sessionMember;
                }
            }
        } catch (Exception e) {
            System.err.println("獲取會員資訊錯誤: " + e.getMessage());
        }
        return null;
    }
} 