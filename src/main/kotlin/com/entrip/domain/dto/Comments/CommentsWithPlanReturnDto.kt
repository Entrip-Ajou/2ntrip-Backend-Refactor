package com.entrip.domain.dto.Comments

import com.entrip.domain.dto.Plans.PlansResponseDto

class CommentsWithPlanReturnDto(
    val plansResponseDto: PlansResponseDto?,
    val commentsList: MutableList<CommentsReturnDto>? = ArrayList<CommentsReturnDto>()
){

}