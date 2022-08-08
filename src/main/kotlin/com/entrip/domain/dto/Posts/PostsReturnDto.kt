package com.entrip.domain.dto.Posts

import com.entrip.domain.entity.Posts

class PostsReturnDto(
    val post_id: Long?,
    val title: String,
    val content: String,
    val author: String?,
    val photoList: MutableList<String> = ArrayList<String>()
        ) {

    constructor(postsRequestDto: PostsRequestDto) : this (
        post_id = postsRequestDto.post_id,
        title = postsRequestDto.title,
        content = postsRequestDto.content,
        author = postsRequestDto.author,
        photoList = postsRequestDto.getPhotoListFromPostsRequestDto()
    )

}