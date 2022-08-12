package com.entrip.domain.dto.PostsNestedComments

class PostsNestedCommentsSaveRequestDto(
    val author: String,
    val content: String,
    val postComment_id: Long
) {
}