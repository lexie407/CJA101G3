package com.toiukha.item.model;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
}
