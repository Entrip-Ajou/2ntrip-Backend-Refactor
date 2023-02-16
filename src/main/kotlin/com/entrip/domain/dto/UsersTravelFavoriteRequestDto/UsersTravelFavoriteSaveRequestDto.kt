package com.entrip.domain.dto.UsersTravelFavoriteRequestDto

class UsersTravelFavoriteSaveRequestDto(
    val regions : MutableList<String>,
    val scores : MutableList<Int>
) {
}