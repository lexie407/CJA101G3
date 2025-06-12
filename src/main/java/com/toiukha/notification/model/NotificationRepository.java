package com.toiukha.notification.model;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<NotificationVO, Integer> {

	@Transactional
	@Modifying
	@Query(value = "UPDATE NotificationVO as nVO SET nVO.notiStatus = :notiStatus, nVO.notiUpdatedAt = :notiUpdatedAt WHERE nVO.notiId = :notiId")
	void updateSta(Integer notiId, Byte notiStatus, Timestamp notiUpdatedAt);
	
	@Query(value = "FROM NotificationVO WHERE memId = :memId ORDER BY notiSendAt")
	List<NotificationVO> searchByMemId(Integer memId);
	
}
