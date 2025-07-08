package com.toiukha.spot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SpotImageEnrichmentInterceptor spotImageEnrichmentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spotImageEnrichmentInterceptor)
                .addPathPatterns("/back-end/spot/**", "/front-end/spot/**")
                .excludePathPatterns("/back-end/spot/api/**");
    }
} 