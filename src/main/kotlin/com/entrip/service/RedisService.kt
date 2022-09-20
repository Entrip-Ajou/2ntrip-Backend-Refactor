package com.entrip.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration


@Service
class RedisService(private final val redisTemplate: RedisTemplate<String, String>) {
    fun setValues(key: String, data: String) {
        val values = redisTemplate.opsForValue()
        values[key] = data
    }

    fun setValues(key: String, data: String, duration: Duration) {
        val values = redisTemplate.opsForValue()
        values.set(key, data, duration)
    }

    fun getValues(key: String?): String? {
        val values = redisTemplate.opsForValue()
        return values[key!!]
    }

    fun deleteValues(key: String) {
        redisTemplate.delete(key)
    }
}