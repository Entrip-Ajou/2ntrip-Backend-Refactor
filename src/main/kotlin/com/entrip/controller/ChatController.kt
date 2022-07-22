package com.entrip.controller

import com.entrip.socket.ChatMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatController {
    //Endpoint from client to server "/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    //Publish chat message to subscribing client
    @SendTo("/topic/public")
    fun sendMessage (@Payload chatMessage: ChatMessage?) : ChatMessage? {
        return chatMessage
    }

    //Method when client connect to socket, add user to session
    @MessageMapping("/chat.addUser")
    //Publish chat message to subscribing client
    @SendTo("/topic/public")
    fun addUser (@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor) : ChatMessage? {
        headerAccessor.sessionAttributes!!["username"] = chatMessage.sender
        return chatMessage
    }
}