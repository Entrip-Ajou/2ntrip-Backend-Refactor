package com.entrip.domain.dto.PostsNestedComments

import com.entrip.domain.entity.PostsNestedComments

class PostsNestedCommentsSaveRequestDto(
    val author: String,
    val content: String,
    val postComment_id: Long
) {
    public fun toEntity(): PostsNestedComments {
        return PostsNestedComments(
            content = content
        )
    }
}