package com.entrip.controller

import com.entrip.socket.InfoWebSocketSessionStatsInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ProfileController(
    final val context: ApplicationContext
) {
    @GetMapping("/profile")
    fun profile(): String {
        val env: Environment = context.environment

        val logger: Logger = LoggerFactory.getLogger(ProfileController::class.java)

        val profiles: MutableList<String> = env.activeProfiles.toMutableList()
        val realProfiles: MutableList<String> = mutableListOf("real1", "real2")
        val defaultProfile: String = if (profiles.isEmpty()) "default" else profiles[0]


        System.out.println("getDefaultProfiles          : " + Arrays.toString(env.getDefaultProfiles()));
        System.out.println("getActiveProfiles           : " + Arrays.toString(env.getActiveProfiles()));

        for (s in profiles) if (realProfiles.contains(s)) return s

        return defaultProfile
    }
}