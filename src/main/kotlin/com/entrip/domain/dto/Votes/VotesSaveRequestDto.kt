package com.entrip.domain.dto.Votes

import com.entrip.domain.entity.Votes
import java.sql.Timestamp

class VotesSaveRequestDto(
    val title : String,
    val contents : MutableList<String>,
    val multipleVotes : Boolean,
    val anonymousVotes : Boolean,
    val deadLine : Timestamp,
    val planner_id : Long,
    val author : String,
) {
    fun toEntity() : Votes {
        return Votes(
            title = title,
            multipleVote = multipleVotes,
            anonymousVote = anonymousVotes,
            deadLine = deadLine,
            voting = true
        )
    }
}