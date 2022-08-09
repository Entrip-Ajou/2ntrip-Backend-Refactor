package com.entrip.domain.dto.Planners

class PlannersReturnDto(
    val planner_id: Long,
    val title: String,
    val start_date: String,
    val end_date: String,
    val time_stamp: String,
    val comment_timeStamp: String
) {
    constructor(responseDto: PlannersResponseDto) : this(
        responseDto.planner_id!!,
        responseDto.title,
        responseDto.start_date,
        responseDto.end_date,
        responseDto.time_stamp,
        responseDto.comment_timeStamp
    )
}