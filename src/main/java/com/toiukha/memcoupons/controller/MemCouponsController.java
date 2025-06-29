package com.toiukha.memcoupons.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.members.model.MembersVO;
import com.toiukha.memcoupons.model.MemCouponsService;
import com.toiukha.memcoupons.model.MemCouponsVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/memcoupons")
public class MemCouponsController {
	@Autowired
	MemCouponsService memcouponssvc;

	/**
	 * 會員領取優惠券。
	 *
	 * @param model   用於傳遞數據到視圖的模型
	 * @param session HTTP 會話，用於獲取會員ID
	 * @param scouId  從請求中獲取的優惠券ID（字串形式）
	 * @param sdiscValue 從請求中獲取的折扣金額（字串形式）
	 * @return 重定向到優惠券領取頁面或登入頁面
	 */
	@PostMapping("memaddcoupons")
	public String memaddcoupons(ModelMap model, HttpSession session,
								@RequestParam("couId") String scouId,
								@RequestParam("discValue") String sdiscValue) {
		
		// 從 session 中獲取會員ID，如果不存在則要求登入
		MembersVO member = (MembersVO) session.getAttribute("member");
		 if (member == null) {
		        return "redirect:/login"; // 如果沒有登入，重定向到登入頁
		    }
		int memId = member.getMemId();
		session.setAttribute("memId", memId);	
		int couId = Integer.parseInt(scouId);
		int discValue = Integer.parseInt(sdiscValue);

		// 創建一個新的 MemCouponsVO 物件來儲存會員優惠券資料
		MemCouponsVO memcouponsVO = new MemCouponsVO();
		memcouponsVO.setMemId(memId);
		memcouponsVO.setCouId(couId);
		memcouponsVO.setDisctVal(discValue);
		memcouponsVO.setCoupSta(0); // 設定優惠券狀態為 0 (未使用)
		
		// 調用 service 層將新的會員優惠券資料添加到資料庫
		memcouponssvc.addMemCoupons(memcouponsVO);
		
		// 完成後重定向回會員領取優惠券的頁面
		return "redirect:/coupon/memAddCoupon";
	}
	//會員擁有優惠券列表
	@GetMapping("listAllMemCoupons")
	public String memCouponList(Model model) {
		model.addAttribute("currentPage", "store");
		model.addAttribute("activeItem", "allcoupons");

		return "front-end/memCoupons/listAllMemCoupons";
	}
	@ModelAttribute("memCouponList")
	protected List<MemCouponsVO> referenceListData(Model model, HttpSession session) {
		Object memIdObj = session.getAttribute("memId");
		if (memIdObj == null) {
			return new ArrayList<>(); // 或回傳 null，看你需求
		}
		int memId = Integer.parseInt(memIdObj.toString());
		List<MemCouponsVO> list = memcouponssvc.findByMemId(memId);
		return list;
	}
}
