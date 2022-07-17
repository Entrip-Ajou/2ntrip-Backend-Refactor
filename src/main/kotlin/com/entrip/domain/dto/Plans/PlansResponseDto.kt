package com.entrip.domain.dto.Plans

import com.entrip.domain.entity.Comments
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans

class PlansResponseDto (
    val plan_id : Long?,
    val date : String,
    val todo : String,
    val time : String,
    val location : String,
    val rgb : Long,
    val planners : Planners?,
    val comments : MutableSet<Comments>,
    val isExistComments : Boolean
        ){
    constructor(entity : Plans) : this(
        entity.plan_id,
        entity.date,
        entity.todo,
        entity.time,
        entity.location,
        entity.rgb,
        entity.planners,
        entity.comments,
        entity.isExistComments()
    )
}