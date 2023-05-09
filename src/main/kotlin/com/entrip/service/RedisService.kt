package com.entrip.service

import com.entrip.domain.entity.AccessToken
import com.entrip.domain.entity.RefreshToken
import com.entrip.repository.AccessTokenRepository
import com.entrip.repository.RefreshTokenRepository
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun saveRefreshToken(key: String, value: String, duration: Long) {
        val refreshToken: RefreshToken = RefreshToken(
            usersId = key,
            refreshToken = value,
            expiredTime = duration
        )
        refreshTokenRepository.save(refreshToken)
    }

    fun saveAccessToken(key: String, value: String, duration: Long) {
        val accessToken = AccessToken(
            usersId = key,
            accessToken = value,
            expiredTime = duration
        )
        accessTokenRepository.save(accessToken)
    }

    fun findRefreshToken(key : String) : String? =
        refreshTokenRepository.findById(key).orElse(null)?.refreshToken

    fun findAccessToken(key : String) : String? =
        accessTokenRepository.findById(key).orElse(null)?.accessToken

    fun deleteRefreshToken(key : String) =
        refreshTokenRepository.deleteById(key)

    fun deleteAccessToken(key : String) =
        accessTokenRepository.deleteById(key)

}