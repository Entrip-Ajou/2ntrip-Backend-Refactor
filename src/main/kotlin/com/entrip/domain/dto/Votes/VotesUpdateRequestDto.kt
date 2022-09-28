package com.entrip.domain.dto.Votes

import java.sql.Timestamp

class VotesUpdateRequestDto(
    val vote_id : Long,
    val title : String,
    val multipleVote : Boolean,
    val anonymousVote : Boolean,
    val deadLine : Timestamp
) {
}