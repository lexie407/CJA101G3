package com.toiukha.item.model;

import java.util.List;
import java.util.Optional;
import java.sql.Timestamp;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toiukha.order.model.OrderVO;

@Service("itemService")
public class ItemService {

	@Autowired
	ItemRepository repository;


	public void addItem(ItemVO itemVO) {
		repository.save(itemVO);
	}

	public void updateItem(ItemVO itemVO) {
		repository.save(itemVO);
	}
	//刪除不寫
	
	public ItemVO getOneItem(Integer itemId) {
		Optional<ItemVO> optional = repository.findById(itemId);
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}
	
	public List<ItemVO> getAll() {
		return repository.findAll();
	}
	
	//已上架商品
	public List<ItemVO> findByItemStatus(int itemStatus) {
		return repository.findByItemStatus(itemStatus);
	}
	
	//由廠商編號查商品
	public List<ItemVO> findByStoreId(int storeId) {
		return repository.findByStoreId(storeId);
	}
	
	/**
	 * 增加商品檢舉次數，當檢舉次數超過3次時自動下架商品
	 * @param itemId 商品ID
	 * @return true表示商品已被下架，false表示商品檢舉次數增加但未下架
	 */
	@Transactional
	public boolean incrementReportCount(Integer itemId) {
		ItemVO item = getOneItem(itemId);
		if (item == null) {
			return false;
		}
		
		// 增加檢舉次數
		Integer currentCount = item.getRepCount() == null ? 0 : item.getRepCount();
		item.setRepCount(currentCount + 1);
		item.setUpdAt(new Timestamp(System.currentTimeMillis()));
		
		// 檢查是否需要下架商品
		boolean shouldRemove = item.getRepCount() > 3;
		if (shouldRemove) {
			item.setItemStatus(2); // 2 = 停售/下架
		}
		
		updateItem(item);
		return shouldRemove;
	}
	
	/**
	 * 檢查會員是否購買過指定商品
	 * 這個方法需要根據實際的訂單系統來實現
	 * 目前提供簡化版本，可根據需要調整
	 * @param memId 會員ID
	 * @param itemId 商品ID
	 * @return true表示已購買過，false表示未購買過
	 */
	public boolean hasMemberPurchasedItem(Integer memId, Integer itemId) {
		// TODO: 根據實際的訂單系統實現此方法
		// 目前返回true允許所有會員檢舉，實際應該查詢訂單記錄
		// 可以通過以下方式實現：
		// 1. 查詢訂單表(orders)找到該會員的所有訂單
		// 2. 查詢訂單明細表(order_details)找到包含該商品的訂單
		// 3. 確認訂單狀態為已完成/已付款
		
		// 簡化實現：暫時允許所有會員檢舉
		return true;
		
		// 真實實現應該像這樣：
		/*
		@Query("SELECT COUNT(od) > 0 FROM OrderDetail od JOIN od.order o " +
		       "WHERE o.memId = :memId AND od.itemId = :itemId AND o.orderStatus IN (2, 3)")
		boolean existsByMemIdAndItemIdAndOrderStatus(@Param("memId") Integer memId, 
		                                           @Param("itemId") Integer itemId);
		*/
	}
	
}
