package com.toiukha.notification.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class NotificationService {
	
	@Autowired
	NotificationRepository repository;
	@Autowired
	private SessionFactory sessionFactory;
	
	//新增通知(notiId = null)
	public void addOneNoti(NotificationVO notificationVO) {
		repository.save(notificationVO);
	}
	
	//更新通知
	public void updateNoti(NotificationVO notificationVO) {
		repository.save(notificationVO);
	}
	
	//修改一個通知狀態
	public void updateNotiStatus(Integer notiId, Byte notiStatus) {
		Date nowDate = new Date();
		Timestamp nowTime = new Timestamp(nowDate.getTime());
		repository.updateSta(notiId, notiStatus, nowTime);
	}
	
	//修改複數通知狀態
	public void updateNotiStatuses(String[] notiIds, Byte notiStatus) {
		Date nowDate = new Date();
		Timestamp nowTime = new Timestamp(nowDate.getTime());
		
		for(String notiId : notiIds) {
		repository.updateSta(Integer.valueOf(notiId), notiStatus, nowTime);
		}
		
	}
	
	//查一個
	public NotificationVO getOneNoti(Integer notiId) {
		Optional<NotificationVO> optional = repository.findById(notiId);
		return optional.orElse(null);
	}
	
	//查全部
	public List<NotificationVO> getAll(){
		return repository.findAll();
	}
	
	//查單一會員的
	public List<NotificationVO> getMemNoti(Integer memId){
		return repository.searchByMemId(memId);
	}
	
	//複式查詢
	public List<NotificationVO> getByCriteria(Map map){
		Map<String, String> query = new HashMap<>();
		
		for (Object obj : map.entrySet()) {
			Map.Entry row = (Map.Entry) obj;
			
			String key = (String) row.getKey();
			Object rawValue = row.getValue();
			String finalValue = null;

			// 因為請求參數裡包含了action，做個去除動作
			if ("action".equals(key) || rawValue == null) {
				continue;
			}

			// 根據值的類型進行處理
			// Spring對於多選(如checkbox)會傳入String[], 對於單選(如text input)會傳入String
			if (rawValue instanceof String[]) {
				String[] valueArray = (String[]) rawValue;
				if (valueArray.length == 0 || (valueArray.length == 1 && valueArray[0].isEmpty())) {
					continue; // 跳過空陣列或只有一個空字串的陣列
				}
				
				if ("memIds".equals(key)) {
					finalValue = String.join(",", valueArray);
				} else {
					finalValue = valueArray[0];
				}
			} else if (rawValue instanceof String) {
				finalValue = (String) rawValue;
			}

			// 如果處理後的值是空的，就跳過
			if (finalValue == null || finalValue.isEmpty()) {
				continue;
			}
			
			// 特別處理日期時間格式
			if("notiSendAtStart".equals(key) || "notiSendAtEnd".equals(key)) {
				finalValue = finalValue.replace("T", " ");
			}
			
			query.put(key, finalValue);
		}
		
		System.out.println(query);
		
		List<NotificationVO> list = FindNotificationByCriteria.getCompositeQuery(query, sessionFactory.openSession(), repository);
		
		return list;
	}
	
	//待發送通知清單
	public List<NotificationVO> getDueNotifications(){
		Date nowTime = new Date();
		return repository.searchUnSendNotification(nowTime);
	}
	
}
