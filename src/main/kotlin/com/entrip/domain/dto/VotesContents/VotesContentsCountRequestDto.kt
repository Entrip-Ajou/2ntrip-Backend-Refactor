package com.entrip.domain.dto.VotesContents

class VotesContentsCountRequestDto(
    val vote_id : Long,
    val voteContents_id : MutableList<Long>,
    val user_id : String
) {
}