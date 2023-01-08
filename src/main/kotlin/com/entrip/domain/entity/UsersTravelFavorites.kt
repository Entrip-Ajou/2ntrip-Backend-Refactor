package com.entrip.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*


class TravelFavorite(
    val region: String,
    val score: Int
) : Comparable<TravelFavorite> {
    override fun compareTo(other: TravelFavorite): Int {
        if (this.region > other.region) return 1
        return -1
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TravelFavorite) return false
        if (other.region == this.region && other.score == this.score) return true
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(region, score)
    }
}

@Document(collection = "UsersTravelFavorite")
class UsersTravelFavorites(
    @Id
    val user_id: String,

    val travelFavorite_set: MutableSet<TravelFavorite> = TreeSet<TravelFavorite>()

) {
    fun addTravelFavorite(travelFavorite: TravelFavorite) =
        travelFavorite_set.add(travelFavorite)

}