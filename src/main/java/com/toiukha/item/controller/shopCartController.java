package com.toiukha.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;
import com.toiukha.memcoupons.model.MemCouponsRepository;
import com.toiukha.memcoupons.model.MemCouponsVO;
import com.toiukha.coupon.model.CouponService;
import com.toiukha.coupon.model.CouponVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/item")
public class shopCartController {

	@Autowired
	ItemService itemSvc;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	MemCouponsRepository memCouponsRepository;
	
	@Autowired
	CouponService couponSvc;

	@PostMapping("getOne_For_shoppCart")
	public String getOne_For_Update(@RequestParam("itemId") String itemId, @RequestParam("memId") String memId,
			ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		// EmpService empSvc = new EmpService();
		ItemVO itemVO = itemSvc.getOneItem(Integer.valueOf(itemId));
		System.out.println(Integer.valueOf(Integer.valueOf(itemId)));
		System.out.println(Integer.valueOf(Integer.valueOf(memId)));
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("itemVO", itemVO);
		return "redirect:/item/listAllItem"; // 查詢完成後轉交update_emp_input.html
	}
	@PostMapping("/add_to_shopCart")
	@ResponseBody
	public String addToShoppCart(@RequestParam("itemId") String itemId,
	                             @RequestParam("memId") String memId,
	                             @RequestParam("quantity") String quantityStr) {
	    String redisKey = "cart:" + memId;
	    System.out.println("1111");
	    try {
	        int quantity = Integer.parseInt(quantityStr);
	        // 檢查是否已存在，存在則累加
	        Object oldQtyObj = redisTemplate.opsForHash().get(redisKey, itemId);
	        int oldQty = oldQtyObj != null ? Integer.parseInt(oldQtyObj.toString()) : 0;
	        int newQty = oldQty + quantity;
	        redisTemplate.opsForHash().put(redisKey, itemId, String.valueOf(newQty));
	        return "success";
	    } catch (Exception e) {
	        return "error";
	    }
	}

	@PostMapping("/get_cart_detail")
	@ResponseBody
	public List<Map<String, Object>> getCartDetail(@RequestParam("memId") String memId) {
		String redisKey = "cart:" + memId;
		Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map.Entry<Object, Object> entry : entries.entrySet()) {
			String itemId = entry.getKey().toString();
			String quantity = entry.getValue().toString();
			// 查商品
			ItemVO item = itemSvc.getOneItem(Integer.valueOf(itemId));
			if (item != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("itemId", itemId);
				map.put("quantity", quantity);
				map.put("itemName", item.getItemName());
				map.put("storeId", item.getStoreId());
				// 判斷促銷價
				Integer price = item.getItemPrice();
				Integer discPrice = item.getDiscPrice();
				LocalDateTime now = LocalDateTime.now();
				if (discPrice != null && item.getStaTime() != null && item.getEndTime() != null
						&& now.isAfter(item.getStaTime()) && now.isBefore(item.getEndTime())) {
					map.put("price", discPrice);
					map.put("isPromo", true);
					map.put("originalPrice", price);
				} else {
					map.put("price", price);
					map.put("isPromo", false);
					map.put("originalPrice", price);
				}
				result.add(map);
			}
		}
		return result;
	}
	//移除購物車
	@PostMapping("/remove_from_cart")
	@ResponseBody
	public String removeFromCart(@RequestParam("memId") String memId, @RequestParam("itemId") String itemId) {
	    String redisKey = "cart:" + memId;
	    redisTemplate.opsForHash().delete(redisKey, itemId);
	    return "success";
	}
	//更改數量
	@PostMapping("/update_cart_qty")
	@ResponseBody
	public String updateCartQty(@RequestParam("memId") String memId, @RequestParam("itemId") String itemId, @RequestParam("quantity") String quantityStr) {
	    String redisKey = "cart:" + memId;
	    try {
	        int quantity = Integer.parseInt(quantityStr);
	        if (quantity <= 0) {
	            redisTemplate.opsForHash().delete(redisKey, itemId);
	        } else {
	            redisTemplate.opsForHash().put(redisKey, itemId, String.valueOf(quantity));
	        }
	        return "success";
	    } catch (Exception e) {
	        return "error";
	    }
	}

	@PostMapping("/get_member_coupons")
	@ResponseBody
	public List<Map<String, Object>> getMemberCoupons(@RequestParam("memId") String memId) {
		List<Map<String, Object>> result = new ArrayList<>();
		
		try {
			int memberId = Integer.parseInt(memId);
			
			// 獲取會員擁有的優惠券
			List<MemCouponsVO> memCoupons = memCouponsRepository.findAll()
				.stream()
				.filter(mc -> mc.getMemId().equals(memberId) && mc.getCoupSta() == 0) // 只取可使用的優惠券
				.collect(Collectors.toList());
			
			LocalDateTime now = LocalDateTime.now();
			
			for (MemCouponsVO memCoupon : memCoupons) {
				CouponVO coupon = couponSvc.getOneCoupon(memCoupon.getCouId());
				if (coupon != null && coupon.getEndTime() != null && coupon.getEndTime().isAfter(now)) {
					Map<String, Object> couponMap = new HashMap<>();
					couponMap.put("couId", coupon.getCouId());
					couponMap.put("couName", coupon.getCouName());
					couponMap.put("discValue", coupon.getDiscValue());
					couponMap.put("storeId", coupon.getStoreId());
					couponMap.put("startTime", coupon.getStartTime());
					couponMap.put("endTime", coupon.getEndTime());
					result.add(couponMap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
