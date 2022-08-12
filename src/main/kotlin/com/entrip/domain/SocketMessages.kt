package com.entrip.domain

import com.entrip.common.MessageType

//Domain model for chat
data class SocketMessages(
    var type: MessageType,
    var content: Int, //0 : Planners, 1 : Plans
    var sender: String,
    var planner_id: Long = -1,
    var date: String? = "Planners"
)
