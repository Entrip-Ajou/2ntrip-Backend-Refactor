package com.entrip.domain.dto.UsersTravelFavoriteRequestDto

import com.entrip.domain.entity.TravelFavorite

class EachTravelFavoriteSaveRequestDto(
    private val region : String,
    private val score : Int
) {
    fun toEntity(): TravelFavorite {
        return TravelFavorite(
            region = region,
            score = score
        )
    }
}