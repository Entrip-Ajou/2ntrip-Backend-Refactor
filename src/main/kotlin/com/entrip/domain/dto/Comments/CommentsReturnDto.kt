package com.entrip.domain.dto.Comments

class CommentsReturnDto(
    val comment_id: Long?,
    val author: String,
    val content: String,
    val plan_id: Long?,
    val photoUrl: String?,
    val nickname: String?
        ){
    constructor(responseDto: CommentsResponseDto) : this (
        responseDto.comment_id,
        responseDto.author,
        responseDto.content,
        responseDto.plans?.plan_id,
        responseDto.users?.photoUrl,
        responseDto.users?.nickname
            )
}