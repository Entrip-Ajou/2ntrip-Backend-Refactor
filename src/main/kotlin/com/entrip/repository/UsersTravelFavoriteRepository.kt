package com.entrip.repository

import com.entrip.domain.entity.UsersTravelFavorites
import org.springframework.data.mongodb.repository.MongoRepository

interface UsersTravelFavoriteRepository : MongoRepository<UsersTravelFavorites, String> {
}
