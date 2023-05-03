package com.entrip.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
@EnableRedisRepositories
//@PropertySource("/home/ec2-user/app/step3/properties/application-redis.properties")
//@PropertySource("classpath:/application-redis.properties")
class RedisConfig {
    private final val logger = LoggerFactory.getLogger(RedisConfig::class.java)

    @Value("#{redis['spring.redis.host']}")
    private val host: String? = null

    @Value("#{redis['spring.redis.port']}")
    private val port: Int? = null

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        logger.info("Current working port is : $port")
        return LettuceConnectionFactory(host!!, port!!)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        return redisTemplate
    }
}