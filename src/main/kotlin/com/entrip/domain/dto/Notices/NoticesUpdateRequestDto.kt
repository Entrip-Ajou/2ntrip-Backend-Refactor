package com.entrip.domain.dto.Notices

import com.entrip.domain.entity.Notices

class NoticesUpdateRequestDto(
    val title : String,
    val content : String,
) {
    fun toEntity(): Notices {
        return Notices(
            title = title,
            content = content,
        )
    }
}