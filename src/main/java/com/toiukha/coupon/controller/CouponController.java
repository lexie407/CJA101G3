package com.toiukha.coupon.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.coupon.model.CouponService;
import com.toiukha.coupon.model.CouponVO;
import com.toiukha.members.model.MembersVO;
import com.toiukha.store.model.StoreVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/coupon")
public class CouponController {
	
	@Autowired
	CouponService couponSvc;
	
	@GetMapping("addCoupon")
	public String addcoupon(ModelMap model,HttpSession session) {
		
		Object storeObj = session.getAttribute("store");
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
		CouponVO couponVO = new CouponVO();
		couponVO.setStoreId(storeId);
		model.addAttribute("couponVO",couponVO);
		model.addAttribute("currentPage", "coupon");
		model.addAttribute("activeItem", "addCoupon");
		return "back-end/coupon/addCoupon_back";
	
	}
	
	@PostMapping("insert_store")
	public String insert (@Valid CouponVO couponVO,BindingResult result,ModelMap model,HttpSession session) {
		if(result.hasErrors()) {
			return "back-end/coupon/addCoupon_back";
		}
		
		Object storeObj = session.getAttribute("store");
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
		couponSvc.addCoupon(couponVO);
		List<CouponVO> list = couponSvc.getAll();
		model.addAttribute("couponListData",list);
		return "redirect:/coupon/listAllCoupon_back";
	}
	//優惠券修改
	@PostMapping("getOne_For_Update_Coupon")
	public String  getOne_For_Update_Coupon(@RequestParam("couId") String couId, ModelMap model) {
		CouponVO couponVO = couponSvc.getOneCoupon(Integer.valueOf(couId));
		model.addAttribute("couponVO",couponVO);
		model.addAttribute("currentPage", "coupon");
		return "back-end/coupon/update_coupon_store";
	}
	@PostMapping("update")
	public String update(@Valid CouponVO couponVO,BindingResult result, ModelMap model) {
		if(result.hasErrors()) {
			return"back-end/coupon/update_coupon_store";
		}
//		couponVO.setStoreId();
		couponSvc.updateCoupon(couponVO);
		model.addAttribute("currentPage", "coupon");
		return "back-end/coupon/listOneCoupon_store";
	}
	
	//===============廠商查看優惠券===============
	@GetMapping("listAllCoupon_back")
	public String listAllCoupon_back(Model model) {
		model.addAttribute("currentPage", "coupon");
		model.addAttribute("activeItem", "listAllCoupon");
		return "back-end/coupon/listAllCoupon_back";
	}
	//查詢單一上商店的優惠券
	@ModelAttribute("couponListData")
	protected List<CouponVO> referenceListData_byStoreId(Model model, HttpSession session) {
		
		Object storeObj = session.getAttribute("store");
		if (storeObj == null) {
			return new ArrayList<>(); // 或回傳 null，看你需求
		}
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
		List<CouponVO> list = couponSvc.findByStoreId(storeId);
		return list;
	}
	
	//會員領取優惠券
	@GetMapping("memAddCoupon")
	public String memAddCoupon(Model model) {
		model.addAttribute("currentPage", "store");
		model.addAttribute("activeItem", "addcoupon");
		return "front-end/memCoupons/memAddCoupon";
	}
	@ModelAttribute("listAddCoupons")
	protected List<CouponVO>referenceListData_member(Model model,HttpSession session){
		MembersVO member = (MembersVO) session.getAttribute("member");
		int memId = member.getMemId();
		session.setAttribute("memId", memId);
		List<CouponVO> list = couponSvc.getCouponsForMember(memId);
		return list;
	}
}
