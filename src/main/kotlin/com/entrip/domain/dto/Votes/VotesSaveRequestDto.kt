package com.entrip.domain.dto.Votes

import com.entrip.domain.entity.Votes
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VotesSaveRequestDto(
    val title : String,
    val contents : MutableList<String>,
    val multipleVotes : Boolean,
    val anonymousVotes : Boolean,
    val deadLine : String,
    val planner_id : Long,
    val author : String,
) {
    fun toEntity() : Votes {
        val formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val deadLineToDateTime : LocalDateTime = LocalDateTime.parse(deadLine, formatter)
        return Votes(
            title = title,
            multipleVote = multipleVotes,
            anonymousVote = anonymousVotes,
            deadLine = deadLineToDateTime,
            voting = true
        )
    }
}