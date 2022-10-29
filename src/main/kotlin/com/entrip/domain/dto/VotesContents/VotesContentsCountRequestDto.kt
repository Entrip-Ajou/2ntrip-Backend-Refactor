package com.entrip.domain.dto.VotesContents

class VotesContentsCountRequestDto(
    val votesId : Long,
    val voteContentIds : MutableList<Long>,
    val userId : String
) {
}