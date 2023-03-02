package com.entrip.domain.dto.UsersTravelFavorite

import com.entrip.domain.entity.TravelFavorite

class TravelFavoriteDtoComparator : Comparator<TravelFavorite> {
    override fun compare(one: TravelFavorite?, two: TravelFavorite?): Int {
        if(one!!.score < two!!.score) return 1
        if(one.score > two.score) return -1
        return 0
    }
}