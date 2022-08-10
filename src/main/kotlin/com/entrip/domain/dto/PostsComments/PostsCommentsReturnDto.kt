package com.entrip.domain.dto.PostsComments

import com.entrip.domain.entity.PostsComments

class PostsCommentsReturnDto(
    val postComment_id: Long,
    val content: String,
    val author: String,
    val nickname: String
) : Comparable<PostsCommentsReturnDto> {
    constructor(postsComments: PostsComments) : this(
        postComment_id = postsComments.postComment_id!!,
        content = postsComments.content,
        author = postsComments.author!!.user_id!!,
        nickname = postsComments.author!!.nickname
    )

    override fun compareTo(other: PostsCommentsReturnDto): Int =
        if (this.postComment_id > other.postComment_id) 1 else -1
}