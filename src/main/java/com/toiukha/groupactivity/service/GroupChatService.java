package com.toiukha.groupactivity.service;

import com.google.gson.Gson;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.model.GroupChatMsgDTO;
import com.toiukha.participant.model.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * 聊天服務類
 * 處理聊天相關的業務邏輯、Redis操作和權限驗證
 */
@Service
public class GroupChatService {

    @Autowired
    private ActService actService; // 注入 ActServiceImpl

    @Autowired
    private PartService participantService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final Gson gson = new Gson();

    // 檢查用戶是否為團隊成員（團主或參加者）
    public boolean checkTeamMembership(Integer actId, Integer memberId) {
        try {
            ActVO activity = actService.getOneAct(actId);
            if (activity != null && activity.getHostId().equals(memberId)) {
                return true; // 是團主
            }

            return participantService.getParticipants(actId).contains(memberId); // 檢查是否為參加者

        } catch (Exception e) {
            System.err.println("檢查團隊成員權限錯誤: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 儲存聊天訊息到Redis
    public void saveMessageToRedis(Integer actId, GroupChatMsgDTO groupChatMsgDTO) {
        try {
            String key = "chat:history:" + actId;
            String messageJson = gson.toJson(groupChatMsgDTO);
            
            redisTemplate.opsForList().rightPush(key, messageJson);
            redisTemplate.opsForList().trim(key, -1000, -1); // 保留最近1000條訊息
            
        } catch (Exception e) {
            System.err.println("儲存訊息到Redis錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 從Redis載入聊天歷史記錄
    public List<GroupChatMsgDTO> loadChatHistory(Integer actId) {
        List<GroupChatMsgDTO> messages = new ArrayList<>();
        
        try {
            String key = "chat:history:" + actId;
            List<String> messageJsonList = redisTemplate.opsForList().range(key, 0, -1);
            
            if (messageJsonList != null) {
                for (String messageJson : messageJsonList) {
                    try {
                        GroupChatMsgDTO message = gson.fromJson(messageJson, GroupChatMsgDTO.class);
                        messages.add(message);
                    } catch (Exception e) {
                        System.err.println("解析單條訊息錯誤: " + e.getMessage()); // 靜默處理個別訊息解析錯誤
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("載入聊天歷史記錄錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }

    // 驗證活動是否存在且狀態正確
    public boolean validateActivity(Integer actId) {
        try {
            ActVO activity = actService.getOneAct(actId);
            return activity != null;
        } catch (Exception e) {
            System.err.println("驗證活動錯誤: " + e.getMessage());
            return false;
        }
    }

    // 獲取活動信息
    public ActVO getActivity(Integer actId) {
        try {
            return actService.getOneAct(actId);
        } catch (Exception e) {
            System.err.println("獲取活動信息錯誤: " + e.getMessage());
            return null;
        }
    }
} 