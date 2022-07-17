package com.entrip.domain.dto.Comments

import com.entrip.domain.entity.Comments
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users

class CommentsResponseDto(
    val comment_id: Long?,
    val author: String,
    val content: String,
    val plans: Plans?,
    val users: Users?
        ) {
    constructor(entity : Comments) : this(
        entity.comment_id,
        entity.author,
        entity.content,
        entity.plans,
        entity.users
    )
}