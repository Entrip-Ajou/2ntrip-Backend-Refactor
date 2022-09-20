package com.entrip.domain.dto.Notices

import com.entrip.domain.entity.Notices

class NoticesSaveRequestDto(
    val author: String,
    val title: String,
    val content: String,
    val planner_id: Long
) {
    fun toEntity(): Notices {
        return Notices(
            title = title,
            content = content,
        )
    }
}