package com.entrip.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    //Endpoint for connecting websocket
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:8080")
            .setAllowedOrigins("http://2ntrip.com")
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