package com.toiukha.notification.model;

import java.sql.Timestamp;
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
		// Map.Entry即代表一組key-value，回傳一個Set裡面就是一組key-value
		Set<Map.Entry<String, String[]>> entry = map.entrySet();
		
		for (Map.Entry<String, String[]> row : entry) {
			String key = row.getKey();
			// 因為請求參數裡包含了action，做個去除動作
			if ("action".equals(key)) {
				continue;
			}
			
			if("memIds".equals(key)) {
				
				String[] valueArray = row.getValue();
				if(valueArray == null || valueArray.length == 0) {
					continue;
				}
				
				String value = String.join(",", valueArray);
				
				
				query.put(key, value);
				
			}else {
				// 若是value為空即代表沒有查詢條件，做個去除動作
				String value = row.getValue()[0]; // getValue拿到一個String陣列, 接著[0]取得第一個元素檢查
				
				if (value == null || value.isEmpty()) {
					continue;
				}
				
				if("notiSendAtStart".equals(key) || "notiSendAtEnd".equals(key)) {
					value = value.replace("T", " ");
				}
				
				query.put(key, value);
			}
			
		}
		
		System.out.println(query);
		
		List<NotificationVO> list = FindNotificationByCriteria.getCompositeQuery(query, sessionFactory.openSession(), repository);
		
		return list;
	}
	
}
