package com.entrip.domain.dto.Posts

class PostsReturnDto(
    val post_id: Long?,
    val title: String,
    val content: String,
    val author: String?,
    val likeNumber : Long,
    val commentsNumber : Long,
    val photoList: MutableList<String> = ArrayList<String>()
) {

    constructor(postsRequestDto: PostsRequestDto) : this(
        post_id = postsRequestDto.post_id,
        title = postsRequestDto.title,
        content = postsRequestDto.content,
        author = postsRequestDto.author,
        likeNumber = postsRequestDto.likeNumber,
        commentsNumber = postsRequestDto.commentsNumber,
        photoList = postsRequestDto.getPhotoListFromPostsRequestDto()
    )

}