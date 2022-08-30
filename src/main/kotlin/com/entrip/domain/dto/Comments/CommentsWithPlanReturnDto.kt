package com.entrip.domain.dto.Comments

import com.entrip.domain.dto.Plans.PlansReturnDto

class CommentsWithPlanReturnDto(
    val planReturnDto: PlansReturnDto?,
    val commentsList: MutableList<CommentsReturnDto>? = ArrayList<CommentsReturnDto>()
    ){

}