package com.entrip.domain.dto.Comments

import com.entrip.domain.entity.Comments

class CommentsSaveRequestDto(
    val author: String,
    val content: String,
    val plans_id: Long
) {
    public fun toEntity(): Comments {
        return Comments(
            author = author,
            content = content
        )
    }
}