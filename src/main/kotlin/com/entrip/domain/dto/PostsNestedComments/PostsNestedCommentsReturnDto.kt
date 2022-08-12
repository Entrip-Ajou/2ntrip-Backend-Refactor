package com.entrip.domain.dto.PostsNestedComments

import com.entrip.domain.entity.PostsNestedComments

class PostsNestedCommentsReturnDto(
    val postNestedComment_id: Long,
    val content: String,
    val author: String,
    val nickname: String
) : Comparable<PostsNestedCommentsReturnDto> {
    constructor(postsNestedComments: PostsNestedComments) : this(
        postNestedComment_id = postsNestedComments.postNestedComment_id!!,
        content = postsNestedComments.content,
        author = postsNestedComments.author!!.user_id!!,
        nickname = postsNestedComments.author!!.nickname
    )

    override fun compareTo(other: PostsNestedCommentsReturnDto): Int =
        if (this.postNestedComment_id > other.postNestedComment_id) 1 else -1
}