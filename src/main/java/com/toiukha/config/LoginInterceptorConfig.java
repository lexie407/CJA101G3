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
				.addPathPatterns("/members/update", "/members/view")
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
