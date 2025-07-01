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
						        
						        "advertisment/listAllAd",                // 瀏覽所有廣告
						        "advertisment/getOne_For_Display",        // 查看廣告詳情
						        "item-report/form",                      // 顯示檢舉表單
						        "item-report/submit",                    // 提交檢舉
						        "item-report/my-orders",                 // 我的訂單列表
						        "item-report/form-from-order",           // 從訂單檢舉表單
						        "item-report/submit-from-order",          // 從訂單提交檢舉

						        
						        



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
        .addPathPatterns("/admins/*",
        		"/manageFunction/*",	
        		
        		//景點模組




        		//揪團模組
        		"/act/admin/*",             // 管理員功能


        		//商城模組
        		"/item/listAllItem_admin", //商品列表審核
        		
        		"advertisment/admin/dashboard",        // 管理員儀表板
        		"advertisment/admin/pending",         // 待審核廣告列表
        		"advertisment/admin/reviewed",        // 已審核廣告列表
        		"advertisment/admin/approve",          // 審核通過廣告
        		"advertisment/admin/reject",            // 駁回廣告
        		"advertisment/admin/deactivate",          // 停用廣告

        		"admin/reports/list",                    // 查看所有檢舉列表
        		"admin/reports/detail",                  // 查看檢舉詳情
        		"admin/reports/approve",                 // 通過檢舉
        		"admin/reports/reject",                  // 駁回檢舉
        		"admin/reports/pending",                 // 待處理檢舉列表
        		"admin/reports/approved",                // 已通過檢舉列表
        		"admin/reports/rejected",                // 已駁回檢舉列表
        		"admin/reports/item/**",                 // 查看某商品的檢舉記錄
        		"admin/reports/member/**",               // 查看某會員的檢舉記錄
        		"admin/reports/image/**",                 // 查看檢舉圖片


        		//文章模組
        		"/Articlereport/allReportList",  // 文章檢舉明細
        		"/CommentsReports/allReportList",  // 留言檢舉明細


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
