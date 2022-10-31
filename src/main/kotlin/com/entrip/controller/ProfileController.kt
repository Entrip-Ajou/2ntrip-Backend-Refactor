package com.entrip.controller

import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ProfileController(private final val environment: Environment) {

    @GetMapping("api/v2/profile")
    public fun profile(): String {
        return Arrays.stream(environment.activeProfiles)
            .findFirst()
            .orElse("")
    }
}