package ute.nhom27.chatserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt message broker đơn giản với prefix "/topic" và "/user"
        config.enableSimpleBroker("/topic", "/user");
        // Đặt prefix cho các tin nhắn từ client gửi đến server
        config.setApplicationDestinationPrefixes("/app");
        // Đặt prefix cho các tin nhắn user-specific
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint WebSocket mà client sẽ kết nối
        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
    }
}