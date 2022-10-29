package com.entrip.domain.dto.Votes

class UsersAndContentsReturnDto(
    val contentId : Long,
    val content : String,
    val users : MutableList<VotesUserReturnDto>
) {
}