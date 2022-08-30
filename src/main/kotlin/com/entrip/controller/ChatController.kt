package com.entrip.controller

import com.entrip.domain.SocketMessages
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.RestController
import sun.reflect.annotation.ExceptionProxy

@RestController
class ChatController(final val template: SimpMessagingTemplate) {


//    //Endpoint from client to server "/chat.sendMessage
//    @MessageMapping("/chat.sendMessage")
//    //Publish chat message to subscribing client
//    @SendTo("/topic/public")
//    fun sendMessage (@Payload chatMessage: ChatMessage?) : ChatMessage? {
//        return chatMessage
//    }

    //Endpoint from client to server "/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    //Publish chat message to subscribing client
    fun sendMessage(@Payload socketMessages: SocketMessages): SocketMessages? {
        template.convertAndSend("/topic/public/${socketMessages.planner_id}", socketMessages)

        return socketMessages
    }

//    //Method when client connect to socket, add user to session
//    @MessageMapping("/chat.addUser")
//    //Publish chat message to subscribing client
//    @SendTo("/topic/public")
//    fun addUser (@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor) : ChatMessage? {
//        headerAccessor.sessionAttributes!!["username"] = chatMessage.sender
//        return chatMessage
//    }

    //ChatController에서 기존에 사용하던 것과는 다르게 SimpMessagingTemplate를 이용해서 destination endpoint를 숫자를 붙여서 보내기

    //Method when client connect to socket, add user to session
    @MessageMapping("/chat.addUser")
    //Publish chat message to subscribing client
    //@SendTo("/topic/public")
    fun addUser(@Payload socketMessages: SocketMessages, headerAccessor: SimpMessageHeaderAccessor): SocketMessages? {
        //chatMessage에 planner_id라는 Long 타입의 변수를 붙여서 보냄
        //app/chat.sendMessage 엔드포인트로 서버에게 메세지 요청을 보내면
        //서버는 topic/public/${chatMessage.planner_id} 엔드포인트로 메세지를 발행
        template.convertAndSend("/topic/public/${socketMessages.planner_id}", socketMessages)
//      headerAccessor.sessionAttributes!!["username"] = chatMessage.sender
        return socketMessages
    }
}