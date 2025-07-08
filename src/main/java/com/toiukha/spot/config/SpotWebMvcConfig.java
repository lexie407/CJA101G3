package com.toiukha.spot.config;

import com.toiukha.spot.config.SpotImageEnrichmentInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 景點模組Web MVC配置類
 * 用於註冊攔截器
 */
@Configuration
public class SpotWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SpotImageEnrichmentInterceptor spotImageEnrichmentInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 註冊景點圖片豐富攔截器，包含前台與後台景點頁面
        registry.addInterceptor(spotImageEnrichmentInterceptor)
                .addPathPatterns("/spot/list", "/spot/search", "/spot/index", "/spot/")
                .addPathPatterns("/admin/spot/**");
    }
}