package com.entrip.domain.dto.Planners

import com.entrip.domain.entity.Planners
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PlannersSaveRequestDto (
    val user_id : String
        ){
    fun toEntity() : Planners {
        val time : String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        return Planners(
            title = "제목 없음",
            start_date = time,
            end_date = time
        )
    }
}