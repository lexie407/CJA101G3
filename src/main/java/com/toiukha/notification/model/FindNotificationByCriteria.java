package com.toiukha.notification.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class FindNotificationByCriteria {

	public static List<NotificationVO> getCompositeQuery(Map<String, String> map, Session session, NotificationRepository repository) {
		if (map.size() == 0) {
			
			return repository.findAll();
			
 		}
			
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<NotificationVO> criteria = builder.createQuery(NotificationVO.class);
		Root<NotificationVO> root = criteria.from(NotificationVO.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (map.containsKey("notificationTimeStart") && map.containsKey("notificationTimeEnd"))
			predicates.add(builder.between(root.get("notiSendAt"), Timestamp.valueOf(map.get("notificationTimeStart")), Timestamp.valueOf(map.get("notificationTimeEnd"))));
		
		for (Map.Entry<String, String> row : map.entrySet()) {
			//查詢有該會員編號的通知(複數查詢)
			if ("memIds".equals(row.getKey())) {
				String[] memIdStrings = row.getValue().split(",");
				List<Integer> memIdList = new ArrayList<Integer>();
				
				for(String memId : memIdStrings) {
					memIdList.add(Integer.valueOf(memId));
				}
				
				if(!memIdList.isEmpty()) {
					predicates.add(root.get("memId").in(memIdList));
				}
			}
			
			//如果只填開始時間
			if ("notiSendAtStart".equals(row.getKey())) {
				if(!map.containsKey("notiSendAtEnd")) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("notiSendAt"), Timestamp.valueOf(row.getValue())));
				}
			}
			
			//如果只填結束時間
			if ("notiSendAtEnd".equals(row.getKey())) {
				if(!map.containsKey("notiSendAtStart")) {
					predicates.add(builder.lessThanOrEqualTo(root.get("notiSendAt"), Timestamp.valueOf(row.getValue())));
				}
			}
			
			//標題
			if ("notiTitle".equals(row.getKey())) {
				predicates.add(builder.like(root.get("notiTitle"), "%" + row.getValue() + "%"));
			}
			
			//內容
			if ("notiCont".equals(row.getKey())) {
				predicates.add(builder.like(root.get("notiCont"), "%" + row.getValue() + "%"));
			}
			
			//狀態
			if("notiStatus".equals(row.getKey())) {
					if(row.getValue() == "4") {
						Date date = new Date();
						Timestamp nowTime = new Timestamp(date.getTime());
						predicates.add(builder.greaterThanOrEqualTo(root.get("notiSendAt"), nowTime));
				} else {
					predicates.add(builder.equal(root.get("notiStatus"), row.getValue()));
				}
			}
			
		}
		
		criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		criteria.orderBy(builder.asc(root.get("memId")), builder.asc(root.get("notiSendAt")));
		TypedQuery<NotificationVO> query = session.createQuery(criteria);
		
		return query.getResultList();
		
	}
	
}
