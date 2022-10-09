package com.entrip.domain.dto.Votes

class VotesUpdateRequestDto(
    val vote_id : Long,
    val title : String,
    val multipleVote : Boolean,
    val anonymousVote : Boolean,
    val deadLine : String
) {
}