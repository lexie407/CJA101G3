package com.toiukha.config;

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

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(membersInterceptor)
				// 要攔截的路徑模式：
				.addPathPatterns("/members/update", "/members/view",
						
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
						        "/api/participate/*"        // 報名/取消 API




						//商城模組




						//文章模組
		
						
						
						
						
						
						
						)
				
				
				
				
				
				// 排除靜態資源，以及登入與註冊頁面
				.excludePathPatterns("/css/**", // *只會攔截一層 **可攔截多層
						"/js/**", 
						"/images/**");

		// 商家專用攔截
		registry.addInterceptor(storeInterceptor)
				.addPathPatterns("/store/view")
				.excludePathPatterns("/css/**",
						"/js/**",
						"/images/**");

	}
}
