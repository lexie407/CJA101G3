package com.toiukha.config;

import com.toiukha.administrant.interceptor.AdministrantInterceptor;
import com.toiukha.administrantauth.interceptor.AdministrantAuthInterceptor;
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
	
	@Autowired
	private AdministrantAuthInterceptor administrantAuthInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(membersInterceptor)
				// 要攔截的路徑模式：
				.addPathPatterns("/members/update", //會員修改
								 "/members/view", //會員個人資料檢視
						
						
						//通用
					    "/notification/getMemNoti", //使用者查詢
					    "/notification/readedNoti", //使用者讀取
					    "/report/memberReportList", //使用者回報清單
					    "/report/memberReportDetail", //使用者回報讀取
					    "/report/chat", //與我聊聊

						
						
						
						//景點模組
					    "/spot/add", //新增景點
					    "/api/spot/favorites",	//景點收藏api
					    "/spot/favorites",              //景點收藏
					    "/itinerary/add",               //新增行程
					    "/itinerary/edit/**",           //編輯行程
					    "/itinerary/my",                //我的行程
					    "/itinerary/favorites",         //我的收藏行程
					    "/api/itinerary/add",           //新增行程API
					    "/api/itinerary/my",            //我的行程API
					    "/api/itinerary/favorites",     //我的收藏API
					    "/api/itinerary/*/favorite",    //收藏切換API
					    "/api/itinerary/*/copy",         //複製行程API



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
						        
						        "item-report/form",             	      // 顯示檢舉表單
						        "item-report/submit",            	      // 提交檢舉
						        "item-report/my-orders",          	      // 我的訂單列表
						        "item-report/form-from-order",   	      // 從訂單檢舉表單
						        "item-report/submit-from-order",        // 從訂單提交檢舉
						        "/productfav/add/*",           // 新增收藏
						        "/productfav/remove/*",        // 移除收藏  
						        "/productfav/toggle/*",        // 切換收藏狀態
						        "/productfav/myFavorites",     // 收藏清單頁面

						        
						        



						//文章模組
						        "/commentsreports/memberReportList",  //留言檢舉明細
						        "/commentsreports/memberReportListDetsil",  //讀取留言檢舉
						        "/commentsAPI/addComments",  //新增留言
						        "/commentsAPI/bestAnswer",  //選最佳解
						        "/CommentsReportsAPI/addCommentsReports",  //檢舉留言
						        "/commentsAPI/deleteComments",  //刪除留言
						        "/likeAPI/dolike",  //按讚
						        "/commentsAPI/getOneComment",  //編輯留言-查
						        "/commentsAPI/updateComments",  //編輯留言-更新
						        "/articleCollection/allList",  //收藏文章-明細
						        "/ArticleCollectionAPI/api",  //收藏文章
						        "/ArticlereportI/memberReportList",  //檢舉文章明細
						        "/CommentsReports/memberReportList"  //檢舉留言明細

						
						
						
						
						
						
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
						"/coupon/getOne_For_Update_Coupon", 		//修改優惠券
						
						"advertisment/addAd",                    // 顯示新增廣告表單
						"advertisment/insert",                   // 新增廣告處理
						"advertisment/getOne_For_Update",        // 獲取廣告資料進行修改
						"advertisment/update",                   // 更新廣告處理
						"advertisment/delete",                   // 刪除廣告處理
						"advertisment/myAds"                     // 查看我的廣告


						
						
						
						
						)
				.excludePathPatterns("/css/**",
						"/js/**",
						"/images/**");
		
		
		
		
		// 管理員攔截
		registry.addInterceptor(administrantInterceptor)
        .addPathPatterns("/members/selectPage",
        		"/members/searchResults",
        		"/members/listAll",
        		"/members/editMembers",
        		"/store/selectPage",
        		"/store/reviewStore",
        		"/store/listOne",
        		"/store/listAll",
        		"/store/editStore",
        		"/admins/*",
        		"/manageFunction/*",	
        		
        		//景點模組
        		"/admin/spot/",		//管理員功能
        		"/admin/itinerary/",	//行程管理員功能


        		//揪團模組
        		"/act/admin/*",             // 管理員功能


        		//商城模組
        		"/item/listAllItem_admin", //商品列表審核
        		
        		"advertisment/admin/**",
        		"admin/reports/**",



        		//文章模組
        		"/Articlereport/allReportList",  // 文章檢舉明細
        		"/CommentsReports/allReportList",  // 留言檢舉明細
        		
        		"/commentsAPI/adminDeleteComments",
        		"/forum/admin/**", // 文章後台管理員功能



        		//通知模組
        		"/notification/unSendNotification",  // 待發送通知明細
        		"/notification/searchNotification",  // 查詢通知明細
        		"/notification/addNotification",  // 新增通知明細



        		//回報模組
        		"/report/allReportList",  // 新增通知明細
        		"/report/addReportbyAdmin"  // 新增通知明細

        		
        		
        		
        		
        		
        		
        		)
        .excludePathPatterns(
        		"/admins/login",
            "/css/**", 
            "/js/**", 
            "/images/**"
        );
		
		
		//權限攔截器
//		registry.addInterceptor(administrantAuthInterceptor)
//        .addPathPatterns(
//            "/admins/listAll",
//            "/admins/selectPage",
//            "/admins/search",
//            "/admins/add",
//            "/admins/edit",
//            "/admins/dashboard"
//            // … 對應 Map 裡的所有 key
//        )
//        .excludePathPatterns("/css/**","/js/**","/images/**");
		
		
		

	}
}
