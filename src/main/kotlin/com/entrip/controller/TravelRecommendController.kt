package com.entrip.controller

import com.entrip.service.TravelRecommendService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TravelRecommendController(
    private val travelRecommendService: TravelRecommendService
) {

    @GetMapping("/pythonTest")
    fun exec_python() {
        travelRecommendService.call_python()
    }
}