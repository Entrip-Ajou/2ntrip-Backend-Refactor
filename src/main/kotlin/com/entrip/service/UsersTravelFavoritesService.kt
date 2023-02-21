package com.entrip.service

import com.entrip.domain.dto.UsersTravelFavorite.EachTravelFavoriteSaveRequestDto
import com.entrip.domain.dto.UsersTravelFavorite.UsersTravelFavoriteSaveRequestDto
import com.entrip.domain.entity.UsersTravelFavorites
import com.entrip.repository.UsersTravelFavoritesRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class UsersTravelFavoritesService(
    private final val usersTravelFavoritesRepository: UsersTravelFavoritesRepository,
    private val objectMapper: ObjectMapper
) {
    fun addUsersTravelFavorite(user_id: String, requestDto: UsersTravelFavoriteSaveRequestDto): UsersTravelFavorites {
        // 유저가 추천받기 위한 usersTravelFavorite 엔티티를 가지고 않을 경우 새로 하나 만든다.
        if (!usersTravelFavoritesRepository.existsById(user_id)) {
            saveUsersTravelFavorite(user_id)
        }

        val target = usersTravelFavoritesRepository.findById(user_id).get()

        // for문 돌려가며 list에서 값 하나씩 빼와서 travelFavorite dto 만들어서 유저에 붙인다.
        for (index in requestDto.regions.indices) {
            val region : String = requestDto.regions[index]
            val score : Int = requestDto.scores[index]

            val travelFavoriteSaveRequestDto : EachTravelFavoriteSaveRequestDto = EachTravelFavoriteSaveRequestDto(region, score)

            target.addTravelFavorite(travelFavoriteSaveRequestDto.toEntity());
        }

        usersTravelFavoritesRepository.save(target)

        return usersTravelFavoritesRepository.findById(user_id).get()
    }

    private fun saveUsersTravelFavorite(user_id: String) {
        val usersTravelFavorites = UsersTravelFavorites(user_id)
        usersTravelFavoritesRepository.save(usersTravelFavorites)
    }

    fun getAllUsersTravelFavorite(): MutableList<UsersTravelFavorites> {
        return usersTravelFavoritesRepository.findAll()
    }
}