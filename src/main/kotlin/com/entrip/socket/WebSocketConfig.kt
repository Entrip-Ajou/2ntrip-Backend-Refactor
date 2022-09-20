package com.entrip.socket

import com.entrip.Application
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.socket.config.WebSocketMessageBrokerStats
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    //Endpoint for connecting websocket
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        //registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:8080").setAllowedOrigins("http://2ntrip.com")
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
            .withSockJS()
    }
    //Current connect url : ws://2ntrip.com/websocket

    //Set prefix for endpoint for message sending and receiving
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        //When destination is server (Client->Server endpoint)
        config.setApplicationDestinationPrefixes("/app")
        //When client subscribe (Server->Client endpoint)
        config.enableSimpleBroker("/topic")
    }

}