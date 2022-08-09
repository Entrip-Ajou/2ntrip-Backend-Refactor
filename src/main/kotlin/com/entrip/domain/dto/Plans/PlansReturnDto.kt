package com.entrip.domain.dto.Plans

class PlansReturnDto(
    val plan_id: Long?,
    val date: String,
    val todo: String,
    val time: String,
    val location: String? = null,
    val rgb: Long,
    val planner_id: Long?,
    val isExistComments: Boolean
) {
    constructor(responseDto: PlansResponseDto) : this(
        responseDto.plan_id,
        responseDto.date,
        responseDto.todo,
        responseDto.time,
        responseDto.location,
        responseDto.rgb,
        responseDto.planners?.planner_id,
        responseDto.isExistComments
    )
}