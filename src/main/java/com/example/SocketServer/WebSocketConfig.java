package com.example.SocketServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TCPServer tcpServer;
    public WebSocketConfig(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/websocket")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        WebSocketHandler handler = new WebSocketHandler();
        handler.setTcpServer(tcpServer);
        tcpServer.setWebSocketHandler(handler);
        return handler;
    }
}
