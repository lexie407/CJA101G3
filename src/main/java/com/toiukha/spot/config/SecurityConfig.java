package com.toiukha.spot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfig {
    
    /**
     * 配置 RestTemplate Bean
     * 用於 HTTP 客戶端調用外部 API
     * @return RestTemplate 實例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public SecurityFilterChain spotSecurityFilterChain(HttpSecurity http) throws Exception {
        http
//            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/spot/login", "/oauth2/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/spot/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/admin/spot/login")
                .defaultSuccessUrl("/admin/spot/list", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/admin/spot/login")
                .defaultSuccessUrl("/admin/spot/list", true)
            )
            .logout(logout -> logout
                .logoutUrl("/admin/spot/logout")
                .logoutSuccessUrl("/admin/spot/login?logout")
                .permitAll()
            );
        return http.build();
    }
} 