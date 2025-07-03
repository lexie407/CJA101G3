package com.toiukha.groupactivity.service;

import com.toiukha.groupactivity.model.ActRepository;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;
import com.toiukha.participant.model.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活動狀態自動排程器
 * 負責處理活動狀態的自動變更和相關通知
 */
@Component
public class ActStatusScheduler {

    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private PartService partSvc;
    
    @Autowired
    private NotificationService notiSvc;

    /**
     * 每30分鐘檢查一次活動狀態
     * 處理：公開設定、成團狀態、活動提醒、結束提醒
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void checkActStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<ActVO> allActs = actRepo.findAll();
        
        for (ActVO act : allActs) {
            try {
                // 1. 檢查是否需要設定為公開
                if (act.getIsPublic() == 0 && now.isAfter(act.getSignupStart())) {
                    act.setIsPublic((byte) 1);
                    actRepo.save(act);
                    System.out.println("活動 " + act.getActId() + " 已自動設定為公開");
                }

                // 2. 檢查報名截止，自動成團
                if (act.getRecruitStatus() == ActStatus.OPEN.getValue() && 
                    now.isAfter(act.getSignupEnd())) {
                    act.setRecruitStatus(ActStatus.FULL.getValue());
                    actRepo.save(act);
                    notifyMembers(act, "活動成團通知", 
                        "活動「" + act.getActName() + "」已達報名截止時間，自動成團！");
                    System.out.println("活動 " + act.getActId() + " 已自動成團");
                }

                // 3. 活動開始前一天提醒
                LocalDateTime oneDayBefore = act.getActStart().minusDays(1);
                if (now.isAfter(oneDayBefore) && now.isBefore(act.getActStart()) && 
                    act.getRecruitStatus() == ActStatus.FULL.getValue()) {
                    // 避免重複發送，檢查是否已經發送過提醒
                    if (!hasSentReminder(act.getActId(), "明天活動開始")) {
                        notifyMembers(act, "活動提醒", 
                            "活動「" + act.getActName() + "」明天就要開始了，請準時參加！");
                        System.out.println("已發送活動開始提醒給活動 " + act.getActId() + " 的團員");
                    }
                }

                // 4. 活動結束後提醒團主設定狀態
                if (now.isAfter(act.getActEnd()) && 
                    act.getRecruitStatus() != ActStatus.ENDED.getValue()) {
                    // 避免重複發送，檢查是否已經發送過提醒
                    if (!hasSentReminder(act.getActId(), "活動結束提醒")) {
                        sendHostReminder(act);
                        System.out.println("已發送結束提醒給活動 " + act.getActId() + " 的團主");
                    }
                }
                
            } catch (Exception e) {
                System.err.println("處理活動 " + act.getActId() + " 狀態時發生錯誤: " + e.getMessage());
            }
        }
    }

    /**
     * 通知活動成員
     */
    private void notifyMembers(ActVO act, String title, String message) {
        List<Integer> memberIds = partSvc.getParticipants(act.getActId());
        
        for (Integer memId : memberIds) {
            NotificationVO noti = new NotificationVO(
                title, 
                message, 
                memId, 
                Timestamp.valueOf(LocalDateTime.now())
            );
            notiSvc.addOneNoti(noti);
        }
    }

    /**
     * 發送團主提醒
     */
    private void sendHostReminder(ActVO act) {
        NotificationVO noti = new NotificationVO(
            "活動結束提醒", 
            "活動「" + act.getActName() + "」已結束，請記得將活動狀態設定為「結束」", 
            act.getHostId(),
            Timestamp.valueOf(LocalDateTime.now())
        );
        notiSvc.addOneNoti(noti);
    }

    /**
     * 檢查是否已經發送過特定提醒（簡單防重複機制）
     * 這裡可以根據實際需求實作更複雜的重複檢查邏輯
     */
    private boolean hasSentReminder(Integer actId, String reminderType) {
        // 簡單實作：檢查最近1小時內是否有相同類型的通知
        // 實際應用中可以加入更精確的重複檢查邏輯
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        // 這裡可以實作檢查邏輯，暫時回傳 false 允許發送
        return false;
    }
} 