package com.entrip.domain.dto.Plans

import com.entrip.domain.entity.Plans

class PlansSaveRequestDto (
    var planner_id : Long,
    var date : String,
    var todo : String,
    var time : String,
    var location : String,
    var rgb : Long
        ){
    public fun toEntity() : Plans {
        return Plans(
            date = date,
            todo = todo,
            time = time,
            location = location,
            rgb = rgb
        )
    }
}