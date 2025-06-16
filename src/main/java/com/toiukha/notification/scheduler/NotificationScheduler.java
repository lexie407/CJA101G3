package com.toiukha.notification.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;

@Component
public class NotificationScheduler {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private SimpMessagingTemplate messagingTemplate; // 用於 WebSocket 推播

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Scheduled(fixedRate = 600000) // 每 10 分執行一次
	public void checkAndSendNotifications() {
		System.out.println("NotificationScheduler 正在運行... 當前時間: " + LocalDateTime.now().format(formatter));

		List<NotificationVO> dueNotifications = notificationService.getDueNotifications();

		if (!dueNotifications.isEmpty()) {
			System.out.println("發現 " + dueNotifications.size() + " 個到期的通知，準備推播。");
			for (NotificationVO notificationVO : dueNotifications) {
				try {
					// **關鍵點：使用 convertAndSendToUser 發送點對點訊息！**
					// notification.getRecipientUserId() 是目標用戶的識別符
					// "/queue/notifications" 是該用戶訂閱的私人通知佇列
					messagingTemplate.convertAndSend("/topic/notifications/" + notificationVO.getMemId(), notificationVO);
					System.out.println("成功推播排程通知給用戶 '" + notificationVO.getMemId() + "' (ID: "
							+ notificationVO.getNotiId() + ", 標題: '" + notificationVO.getNotiTitle() + "')");
					
					// 推播成功後，更新通知狀態為已發送
                    notificationService.updateNotiStatus(notificationVO.getNotiId(), (byte)0);

				} catch (Exception e) {
					System.err
							.println("推播通知失敗 (用戶: " + notificationVO.getMemId() + ", ID: " + notificationVO.getNotiId()
									+ ", 內容: '" + notificationVO.getNotiTitle() + "'): " + e.getMessage());
					// 這裡可以添加錯誤日誌、重試機制或將通知狀態設置為 '發送失敗'
				}
			}
		} else {
			System.out.println("沒有通知需要推播。");
		}

	}

}
