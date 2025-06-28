package com.toiukha.coupon.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.memcoupons.model.MemCouponsRepository;
import com.toiukha.memcoupons.model.MemCouponsVO;

@Service("couponService")
public class CouponService {

	@Autowired
	CouponRepository repository;
	
	@Autowired
	MemCouponsRepository memCouponsRepository;
	

	public void addCoupon(CouponVO couponVO) {
		repository.save(couponVO);
	}

	public void updateCoupon(CouponVO couponVO) {
		repository.save(couponVO);
	}

	public CouponVO getOneCoupon(Integer couId) {
		Optional<CouponVO> optional = repository.findById(couId);
		return optional.orElse(null);
	}

	public List<CouponVO> getAll() {
		return repository.findAll();
	}

	public List<CouponVO> findByStoreId(int storeId) {
		return repository.findByStoreId(storeId);
	}
	 public List<CouponVO> getCouponsForMember(Integer memId) {
        // 1. 取得所有還沒過期的優惠券
        LocalDateTime now = LocalDateTime.now();
        List<CouponVO> notExpired = repository.findAll()
            .stream()
            .filter(coupon -> coupon.getEndTime() != null && coupon.getEndTime().isAfter(now))
            .collect(Collectors.toList());

        // 2. 取得會員已領取的優惠券ID
        List<MemCouponsVO> memCouponsList = memCouponsRepository.findAll();
        List<Integer> claimedCouponIds = memCouponsList.stream()
        	    .filter(memCoupon -> memCoupon.getMemId().equals(memId))
        	    .map(memCoupon -> memCoupon.getCouId()) // 這裡要用 getCould()
        	    .collect(Collectors.toList());

        // 3. 排除已領取的
        return notExpired.stream()
            .filter(coupon -> !claimedCouponIds.contains(coupon.getCouId()))
            .collect(Collectors.toList());
    }
}
