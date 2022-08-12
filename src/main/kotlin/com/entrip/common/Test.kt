package com.entrip.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.socket.config.WebSocketMessageBrokerStats

@Service
class Test(
    @Autowired
    private val webSocketMessageBrokerStats: WebSocketMessageBrokerStats
) {
    private val logger: Logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)

    @Scheduled(fixedDelay = 10000)
    public fun test() {
        logger.info(webSocketMessageBrokerStats.sockJsTaskSchedulerStatsInfo.toString())
        logger.info(webSocketMessageBrokerStats.stompBrokerRelayStatsInfo.toString())
        logger.info(webSocketMessageBrokerStats.sockJsTaskSchedulerStatsInfo.toString())
        logger.info(webSocketMessageBrokerStats.webSocketSessionStatsInfo.toString())
        logger.info(webSocketMessageBrokerStats.clientInboundExecutorStatsInfo.toString())
        logger.info(webSocketMessageBrokerStats.clientOutboundExecutorStatsInfo.toString())
    }
}