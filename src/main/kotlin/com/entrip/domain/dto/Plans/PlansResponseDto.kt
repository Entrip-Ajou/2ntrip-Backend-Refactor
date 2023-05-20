package com.entrip.domain.dto.Plans

import com.entrip.domain.entity.Plans

class PlansResponseDto(
    val plan_id: Long?,
    val date: String,
    val todo: String,
    val time: String,
    val location: String? = null,
    val rgb: Long,
    val planner_id: Long?,
    val isExistComments: Boolean
) {
    constructor(entity: Plans) : this(
        entity.plan_id,
        entity.date,
        entity.todo,
        entity.time,
        entity.location,
        entity.rgb,
        entity.planners!!.planner_id,
        entity.isExistComments()
    )
}