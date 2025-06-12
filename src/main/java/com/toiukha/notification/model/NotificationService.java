package com.toiukha.notification.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	
	public List<NotificationVO> getMemNoti(Integer memId){
		return repository.searchByMemId(memId);
	}
	
}
