package com.entrip.domain.dto.PostsComments

import com.entrip.domain.entity.PostsComments

class PostsCommentsSaveRequestDto(
    val author: String,
    val content: String,
    val post_id: Long
) {
    public fun toEntity(): PostsComments {
        return PostsComments(
            content = content
        )
    }
}