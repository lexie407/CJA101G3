package com.toiukha.groupactivity.config;

import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
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

import java.util.Map;

/*
 * Spring WebSocket + STOMP 配置
 * 用於團隊聊天室功能
 */
@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 啟用簡單消息代理
        config.setApplicationDestinationPrefixes("/app"); // 設置客戶端消息前綴
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允許所有來源（生產環境請設定具體域名）
                .addInterceptors(new HttpHandshakeInterceptor()) // 添加握手攔截器
                .withSockJS(); // 啟用SockJS支援
    }

    /*
     * HTTP握手攔截器
     * 用於將HttpSession傳遞到WebSocket session
     */
    public class HttpHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                HttpSession httpSession = servletRequest.getServletRequest().getSession(false);

                if (httpSession != null) {
                    attributes.put("HTTP_SESSION", httpSession); // 將HttpSession存儲到WebSocket session
                    Object memberObj = httpSession.getAttribute("member");
                    if (memberObj instanceof MembersVO) {
                        attributes.put("memberVO", memberObj); // 直接存入會員物件
                    }
                }
            }

            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
            // 握手完成後的處理（如果需要）
        }
    }
} 