package com.toiukha.notification.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<NotificationVO, Integer> {

	//修改狀態
	@Transactional
	@Modifying
	@Query(value = "UPDATE NotificationVO as nVO SET nVO.notiStatus = :notiStatus, nVO.notiUpdatedAt = :notiUpdatedAt WHERE nVO.notiId = :notiId")
	void updateSta(Integer notiId, Byte notiStatus, Timestamp notiUpdatedAt);
	
	//會員編號查詢
	@Query(value = "FROM NotificationVO as nVO WHERE nVO.memId = :memId ORDER BY nVO.notiSendAt")
	List<NotificationVO> searchByMemId(Integer memId);
	
	//查詢待推播通知
	@Query(value = "FROM NotificationVO as nVO WHERE nVO.notiStatus = 9 AND nVO.notiSendAt <= :nowTime ORDER BY nVO.notiSendAt")
	List<NotificationVO> searchUnSendNotification(Date nowTime);
	
}
