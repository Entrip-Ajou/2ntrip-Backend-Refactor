package com.entrip.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Profile("test")
@Configuration
class EmbeddedRedisConfig(
    @Value("#{redis['spring.redis.port']}")
    private val redisPort: Int

) {

    private final val logger = LoggerFactory.getLogger(EmbeddedRedisConfig::class.java)

    private lateinit var redisServer : RedisServer

    @PostConstruct
    fun redisServer() {
        redisServer = RedisServer(redisPort)
        logger.info("Current working redis server's port is : $redisPort")
        redisServer.start()
    }

    @PreDestroy
    fun stopRedis() {
        if (redisServer == null) {
            redisServer.stop()
        }
    }

}