package com.toiukha.sentitem.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.coupon.model.CouponVO;
import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;

@Service("sentItemService")
public class SentItemService {

	@Autowired
	SentItemRepository repository;

	@Autowired
	ItemService itemService;

	public void addSentItem(SentItemVO sentitemVO) {
		repository.save(sentitemVO);
	}

	public void updateSentItem(SentItemVO sentitemVO) {
		repository.save(sentitemVO);
	}

	public SentItemVO getOneSentItem(Integer sentItemId) {
		Optional<SentItemVO> optional = repository.findById(sentItemId);
		return optional.orElse(null);
	}

	public List<SentItemVO> getAll() {
		return repository.findAll();
	}

	public List<SentItemVO> findByMemId(Integer memId) {
		List<SentItemVO> list = repository.findByMemId(memId);
		for (SentItemVO ticket : list) {
			ItemVO item = itemService.getOneItem(ticket.getItemId());
			if (item != null) {
				ticket.setTicketName(item.getItemName());
			}
		}
		return list;
	}

	public List<SentItemVO> findByStoreId(Integer storeId) {
		List<SentItemVO> list = repository.findByStoreId(storeId);
		for (SentItemVO ticket : list) {
			ItemVO item = itemService.getOneItem(ticket.getItemId());
			if (item != null) {
				ticket.setTicketName(item.getItemName());
			}
		}
		return list;
	}
}
