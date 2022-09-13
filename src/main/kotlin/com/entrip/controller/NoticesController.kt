package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Notices.NoticesSaveRequestDto
import com.entrip.service.NoticesService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset


@RestController
class NoticesController(
    val noticesService: NoticesService
) {
    private fun sendResponseHttpByJson(message: String, data: Any) : ResponseEntity<RestAPIMessages> {
        val restAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

    // 공지 작성 리턴 Unit
    @PostMapping("/api/v1/notices")
    fun save(@RequestBody requestDto: NoticesSaveRequestDto) : ResponseEntity<RestAPIMessages> {
        val noticeId : Long = noticesService.save(requestDto)!!
        val returnDto = noticesService.findById(noticeId)
        return sendResponseHttpByJson("Notices is saved well", returnDto)
    }
}