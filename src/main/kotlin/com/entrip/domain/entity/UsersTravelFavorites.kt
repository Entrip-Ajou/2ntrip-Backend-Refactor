package com.entrip.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


class TravelFavorite(
    val region: String,
    val score: Int
) {}

@Document(collection = "UsersTravelFavorite")
class UsersTravelFavorites(
    @Id
    val user_id: String,

    val travelFavorite_array: MutableList<TravelFavorite> = ArrayList<TravelFavorite>()

) {
    fun addTravelFavorite(travelFavorite: TravelFavorite) =
        travelFavorite_array.add(travelFavorite)
}