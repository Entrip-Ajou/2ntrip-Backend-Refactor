package com.entrip.domain.dto.Votes

import com.entrip.domain.entity.Votes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VotesSaveRequestDto(
    val title : String,
    val contents : MutableList<String>,
    val multipleVotes : Boolean,
    val anonymousVotes : Boolean,
    val deadLine : String?,
    var planner_id : Long,
    val author : String,
) {
    fun toEntity() : Votes {
        val deadLineToDateTime : LocalDateTime? = if (deadLine != null) {
            val formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            LocalDateTime.parse(deadLine, formatter)
        } else {
            null
        }

        return Votes(
            title = title,
            multipleVote = multipleVotes,
            anonymousVote = anonymousVotes,
            deadLine = deadLineToDateTime,
            voting = true
        )
    }
}