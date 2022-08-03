//package com.entrip.events
//
//import com.entrip.controller.ChatController
//import com.entrip.socket.ChatMessage
//import com.entrip.socket.MessageType
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.context.event.EventListener
//import org.springframework.stereotype.Component
//
//@Component
//class EventHandler (
//    @Autowired
//    val chatController: ChatController
//){
//    @EventListener
//    public fun crudEventHandler (crudEvent: CrudEvent) {
//        chatController.sendMessage(ChatMessage(
//            MessageType.CHAT,
//            (crudEvent.message,
//            "server",
//            crudEvent.planner_id,
//        ))
//    }
//}