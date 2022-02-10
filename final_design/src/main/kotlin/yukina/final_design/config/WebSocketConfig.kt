package yukina.final_design.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import org.springframework.messaging.simp.config.MessageBrokerRegistry

import org.springframework.web.socket.config.annotation.StompEndpointRegistry

import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean




@Configuration
@EnableWebSocket
class WebSocketConfig {
    @Bean
    fun serverEndpointExporter(): ServerEndpointExporter {
        return ServerEndpointExporter()
    }
  
    @Bean
    fun createWebSocketContainer(): ServletServerContainerFactoryBean {
        return ServletServerContainerFactoryBean().apply {
            setMaxTextMessageBufferSize(512000)
            setMaxBinaryMessageBufferSize(512000)
            setMaxSessionIdleTimeout(15 * 60000L)
        }
    }

    
}