package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.TravelRecommend.TravelRecommendRequestDto
import com.entrip.domain.dto.TravelRecommend.TravelRecommendResponseDto
import com.entrip.service.TravelRecommendService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset

@RestController
class TravelRecommendController(
    private val travelRecommendService: TravelRecommendService
) {

    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

    @GetMapping("/api/v2/travelRecommend")
    fun execPython(@RequestBody requestDto: TravelRecommendRequestDto): ResponseEntity<RestAPIMessages> {
        val responseDto : TravelRecommendResponseDto = travelRecommendService.callPython(requestDto)

        return sendResponseHttpByJson("TravelRecommend!", responseDto)
    }
}