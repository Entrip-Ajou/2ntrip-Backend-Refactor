package com.entrip.domain.dto.TravelRecommend

class TravelRecommendResponseDto(
    private val user_id : String,
    private val recommendRegions : MutableList<String>
) {
}