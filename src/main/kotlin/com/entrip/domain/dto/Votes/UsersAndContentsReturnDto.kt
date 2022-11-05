package com.entrip.domain.dto.Votes

class UsersAndContentsReturnDto(
    val content_id : Long,
    val content : String,
    val users : MutableList<VotesUserReturnDto>
) {
}