package com.entrip.domain.dto.Comments

class CommentsUpdateRequestDto(
    var author: String,
    var content: String,
    var plans_id: Long
) {
}