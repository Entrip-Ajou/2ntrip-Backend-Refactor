package com.entrip.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.socket.config.WebSocketMessageBrokerStats

class InfoWebSocketSessionStatsInfo(
    @Autowired
    private val webSocketMessageBrokerStats: WebSocketMessageBrokerStats
) {
    private val logger: Logger = LoggerFactory.getLogger(InfoWebSocketSessionStatsInfo::class.java)

    @Scheduled(fixedDelay = 30000)
    public fun infoWebSocketSessionStatsInfo() {
        logger.info(webSocketMessageBrokerStats.webSocketSessionStatsInfo.toString())
    }
}