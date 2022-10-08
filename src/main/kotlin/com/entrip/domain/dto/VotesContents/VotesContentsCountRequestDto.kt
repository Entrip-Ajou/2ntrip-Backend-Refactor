package com.entrip.domain.dto.VotesContents

class VotesContentsCountRequestDto(
    val voteContentIds : MutableList<Long>,
    val userId : String
) {
}