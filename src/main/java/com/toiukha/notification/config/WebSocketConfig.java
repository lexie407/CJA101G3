package com.toiukha.notification.config;

import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.toiukha.members.model.MembersVO;

import jakarta.servlet.http.HttpSession;

@Configuration // 標記為 Spring 配置類別
@EnableWebSocketMessageBroker // **啟用 WebSocket 訊息代理功能，這個是關鍵！**
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // 廣播和點對點主題前綴
        config.setApplicationDestinationPrefixes("/app"); // 應用程式目的地前綴
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
        		.setAllowedOriginPatterns("*")
        		.addInterceptors(new HttpHandshakeInterceptor())
        		.withSockJS(); // WebSocket 連接端點，啟用 SockJS 兼容性
    }
	
}

class HttpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession httpSession = servletRequest.getServletRequest().getSession(false);
            if (httpSession != null) {
                attributes.put("HTTP_SESSION", httpSession);
                Object memberObj = httpSession.getAttribute("member");
                if (memberObj instanceof MembersVO member) {
                    attributes.put("memberVO", member); // ✅ 存入會員資訊，供後續使用
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 握手後可選擇記錄或驗證（目前無需處理）
    }
}
