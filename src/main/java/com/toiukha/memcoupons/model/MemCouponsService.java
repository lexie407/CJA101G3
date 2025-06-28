package com.toiukha.memcoupons.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("memcouponsService")
public class MemCouponsService {
	
	@Autowired
	MemCouponsRepository repository;
	
	public void addMemCoupons(MemCouponsVO memCouponsVO) {
		repository.save(memCouponsVO);
	}
	
	public void updateMemCoupons(MemCouponsVO memCouponsVO) {
		repository.save(memCouponsVO);
	}
	
	// 依照複合主鍵（會員ID與優惠券ID）查詢單一會員優惠券
	public MemCouponsVO finById(Integer memId, Integer couId) {
		// 建立複合主鍵物件
		MemCouponsVO.CompositeDetail key = new MemCouponsVO.CompositeDetail(memId, couId);
		// 用複合主鍵查詢，找不到則回傳 null
		return repository.findById(key).orElse(null);
	}
	
	// 查詢某會員所擁有的所有優惠券
	public List<MemCouponsVO> findByMemId(Integer memId) {
		// 取得所有會員優惠券，過濾出 memId 符合的，並以 List 回傳
		return repository.findAll()
			.stream()
			.filter(mc -> mc.getMemId().equals(memId))
			.collect(java.util.stream.Collectors.toList());
	}
}
