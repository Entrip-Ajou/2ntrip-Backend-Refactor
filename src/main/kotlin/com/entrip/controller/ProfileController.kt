package com.entrip.controller

import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sun.security.ec.point.ProjectivePoint.Mutable
import java.util.Arrays

@RestController
class ProfileController(
    private val env: Environment
) {
    @RequestMapping("api/v2/profile")
    fun profile(): String {
        val profiles: List<String> = env.activeProfiles.asList()
        val realProfiles: List<String> = listOf("real", "real1", "real2")
        val defaultProfile: String = profiles.get(0)
        return profiles.stream()
            .filter(realProfiles::contains)
            .findAny()
            .orElse(defaultProfile)
    }
}