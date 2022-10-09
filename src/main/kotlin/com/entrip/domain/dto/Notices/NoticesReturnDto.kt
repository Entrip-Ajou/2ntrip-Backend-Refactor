package com.entrip.domain.dto.Notices

import com.entrip.domain.entity.Notices
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoticesReturnDto(
    val notice_id : Long?,
    val author : String?,
    val title : String,
    val content : String,
    val modifiedDate : String,
) {
    constructor(notices: Notices) : this(
        notices.notice_id,
        notices.author!!.user_id,
        notices.title,
        notices.content,
        notices.timestamp.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    )
}