package com.entrip.socket

//Domain model for chat
data class ChatMessage (
    var type : MessageType,
    var content : String?,
    var sender : String
)
