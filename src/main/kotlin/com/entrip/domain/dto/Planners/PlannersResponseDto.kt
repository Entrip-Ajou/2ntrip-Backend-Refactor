package com.entrip.domain.dto.Planners

import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users
import java.util.*

class PlannersResponseDto(
    val planner_id: Long?,
    val title: String,
    val start_date: String,
    val end_date: String,
    val plans: MutableSet<Plans>? = TreeSet(),
    val users: MutableSet<Users>? = TreeSet(),
    val time_stamp: String,
    val comment_timeStamp: String
        ){
    constructor(entity : Planners) : this (
        entity.planner_id,
        entity.title,
        entity.start_date,
        entity.end_date,
        entity.plans,
        entity.users,
        entity.timestamp.toString(),
        entity.comment_timeStamp.toString()
    )
}