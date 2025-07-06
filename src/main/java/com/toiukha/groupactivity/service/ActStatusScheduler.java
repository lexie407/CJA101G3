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
     * 每1分鐘檢查一次活動狀態（測試用，正式環境可改回30分鐘）
     * 處理：公開設定、成團狀態、活動提醒、結束提醒
     */
    @Scheduled(cron = "0 */1 * * * *")
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

                // 2. 檢查報名截止，根據人數判斷成團或未成團
                if (act.getRecruitStatus() == ActStatus.OPEN.getValue() && 
                    now.isAfter(act.getSignupEnd())) {
                    
                    // 取得實際報名人數（包含團主）
                    int actualParticipants = partSvc.getParticipants(act.getActId()).size() + 1; // +1 包含團主
                    
                    // 根據人數與maxCap比較判斷成團或未成團
                    if (actualParticipants >= act.getMaxCap()) {
                        act.setRecruitStatus(ActStatus.FULL.getValue());
                        notifyMembers(act, "活動成團通知", 
                            "活動「" + act.getActName() + "」已達報名截止時間，成團成功！");
                        System.out.println("活動 " + act.getActId() + " 已自動成團，報名人數：" + actualParticipants + "/" + act.getMaxCap());
                    } else {
                        act.setRecruitStatus(ActStatus.FAILED.getValue());
                        notifyMembers(act, "活動未成團通知", 
                            "活動「" + act.getActName() + "」已達報名截止時間，但因人數不足未能成團。");
                        System.out.println("活動 " + act.getActId() + " 未成團，報名人數：" + actualParticipants + "/" + act.getMaxCap());
                    }
                    
                    actRepo.save(act);
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
     * 檢查是否已經發送過特定提醒（使用現有NotificationService方法）
     */
    private boolean hasSentReminder(Integer actId, String reminderType) {
        try {
            // 使用現有的getAll方法查詢所有通知
            List<NotificationVO> allNotifications = notiSvc.getAll();
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            
            // 檢查最近1小時內是否有相同類型的通知
            for (NotificationVO noti : allNotifications) {
                if (noti.getNotiTitle() != null && noti.getNotiTitle().contains(reminderType) &&
                    noti.getNotiSendAt() != null && 
                    noti.getNotiSendAt().toLocalDateTime().isAfter(oneHourAgo)) {
                    return true; // 找到最近1小時內發送過的相同類型通知
                }
            }
            
            return false; // 沒有找到重複通知
        } catch (Exception e) {
            // 如果查詢失敗，為了安全起見，假設已經發送過
            System.err.println("檢查重複通知時發生錯誤: " + e.getMessage());
            return true;
        }
    }
} 