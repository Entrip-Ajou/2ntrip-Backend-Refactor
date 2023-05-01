package com.entrip.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("RefreshToken")
class RefreshToken(
    @Id
    val usersId: String,
    val refreshToken: String,
    @TimeToLive
    var expiredTime: Long
)