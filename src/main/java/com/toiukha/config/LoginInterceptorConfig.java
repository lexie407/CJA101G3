package com.toiukha.config;

import com.toiukha.administrant.interceptor.AdministrantInterceptor;
import com.toiukha.members.interceptor.MembersInterceptor;
import com.toiukha.store.interceptor.StoreInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoginInterceptorConfig implements WebMvcConfigurer {

	@Autowired
	private MembersInterceptor membersInterceptor;

	@Autowired
	private StoreInterceptor storeInterceptor;
	
	@Autowired
    private AdministrantInterceptor administrantInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(membersInterceptor)
				// 要攔截的路徑模式：
				.addPathPatterns("/members/update", "/members/view",
						
						
						//通用
					    "/notification/getMemNoti", //使用者查詢
					    "/notification/readedNoti", //使用者讀取
					    "/report/memberReportList", //使用者回報清單
					    "/report/memberReportDetail", //使用者回報讀取
					    "/report/chat", //與我聊聊

						
						
						
						//景點模組




						//揪團模組
						        "/act/member/add",          // 前台新增活動
						        "/act/member/edit/*",       // 前台修改活動 (含路徑參數)
						        "/act/member/listMy/*",     // 前台 MyList 頁面 - 我揪的團
						        "/act/member/listMyJoin/*", // 前台 MyJoin 頁面 - 我跟的團
						        "/api/act/add",             // 新增活動 API
						        "/api/act/update",          // 修改活動 API
						        "/api/act/my/*",            // MyList API - 我揪的團
						        "/api/act/myJoin/*",        // MyJoin API - 我跟的團
						        "/api/act/*/status/*",      // 狀態變更 API
						        "/api/participate/*",        // 報名/取消 API




						//商城模組
						        "/item/add_to_shopCart", 			//加入購物車
						        "/item/get_cart_detail", 			//查看購物車
						        "/item/get_member_coupons",		//查看購物車
						        "/coupon/memAddCoupon",		//領取優惠券
						        "/memcoupons/memaddcoupons",   		//領取優惠券
						        "/memcoupons/listAllMemCoupons", 	//我的優惠券
						        "/member/itemList",			//我的票券
						        "/order/listCompletedOrders",		//我的訂單
						        
						        



						//文章模組
						        "/commentsreports/memberReportList",  //留言檢舉明細
						        "/commentsreports/memberReportListDetsil"  //讀取留言檢舉

						
						
						
						
						
						
						)
				
				
				
				
				
				// 排除靜態資源，以及登入與註冊頁面
				.excludePathPatterns("/css/**", // *只會攔截一層 **可攔截多層
						"/js/**", 
						"/images/**");

		// 商家專用攔截
		registry.addInterceptor(storeInterceptor)
				.addPathPatterns("/store/view",
						
						
						//商城模組
						"/item/addItem_back",	 	//新增商品
						"/item/listAllItem_back", 		//商品列表
						"/coupon/listAllCoupon_back",	//優惠券列表
						"/coupon/addCoupon", 		//新增優惠券
						"/order/store_listAllOrder",  			//訂單列表
						"/store/listAllSentItem",       			//電子票券管理
						"/item/getOne_For_Update",  			//商品修改
						"/item/getOne_For_Update_promo", 		//新增促銷           
						"/coupon/getOne_For_Update_Coupon" 		//修改優惠券

						
						
						
						
						)
				.excludePathPatterns("/css/**",
						"/js/**",
						"/images/**");
		
		
		
		
		// 管理員攔截
		registry.addInterceptor(administrantInterceptor)
        .addPathPatterns("/admins/",
        		//景點模組




        		//揪團模組
        		"/act/admin/*",             // 管理員功能


        		//商城模組
        		"/item/listAllItem_admin" //商品列表審核

        		//文章模組

        		
        		
        		
        		
        		
        		
        		)
        .excludePathPatterns(
            "/css/**", 
            "/js/**", 
            "/images/**"
        );
		
		
		

	}
}
