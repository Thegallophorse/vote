package com.hanger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket握手协议配置
 * @author hanger
 * 2019-09-06 22:55
 */
@Configuration
public class WebSocketConfig {
    //加载WebSocket
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
