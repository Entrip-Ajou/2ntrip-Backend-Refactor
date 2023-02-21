package com.entrip.domain.dto.UsersTravelFavorite

class UsersTravelFavoriteSaveRequestDto(
    val regions : MutableList<String>,
    val scores : MutableList<Int>
) {
}