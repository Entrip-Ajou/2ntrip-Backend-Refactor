package com.entrip.service

import com.entrip.domain.entity.TravelFavorite
import com.entrip.domain.entity.UsersTravelFavorites
import com.entrip.repository.UsersTravelFavoritesRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class UsersTravelFavoritesService(
    private final val usersTravelFavoritesRepository: UsersTravelFavoritesRepository,
    private val objectMapper: ObjectMapper
) {
    fun addUsersTravelFavorite(user_id: String, travelFavorite: TravelFavorite): UsersTravelFavorites {
        if (!usersTravelFavoritesRepository.existsById(user_id)) saveUsersTravelFavorite(user_id)
        val target = usersTravelFavoritesRepository.findById(user_id).get()
        target.addTravelFavorite(travelFavorite)
        usersTravelFavoritesRepository.save(target)
        return usersTravelFavoritesRepository.findById(user_id).get()
    }

    private fun saveUsersTravelFavorite(user_id: String) {
        val usersTravelFavorites = UsersTravelFavorites(user_id)
        usersTravelFavoritesRepository.save(usersTravelFavorites)
    }
}