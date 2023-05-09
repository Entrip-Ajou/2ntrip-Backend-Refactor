package com.entrip.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive


@RedisHash("AccessToken")
class AccessToken(
    @Id
    val usersId: String,
    val accessToken: String,
    @TimeToLive
    var expiredTime: Long
) {
}