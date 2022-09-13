package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Notices.NoticesSaveRequestDto
import com.entrip.domain.dto.Notices.NoticesUpdateRequestDto
import com.entrip.service.NoticesService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    // 공지 조회 리턴 NoticesReturnDto
    @GetMapping("api/v1/notices/{notice_id}")
    fun findById(@PathVariable notice_id: Long) : ResponseEntity<RestAPIMessages> {
        val returnDto = noticesService.findById(notice_id)
        return sendResponseHttpByJson("Load notices with id : $notice_id", returnDto)
    }

    // 공지 작성 리턴 NoticesReturnDto
    @PostMapping("/api/v1/notices")
    fun save(@RequestBody requestDto: NoticesSaveRequestDto) : ResponseEntity<RestAPIMessages> {
        val noticeId : Long = noticesService.save(requestDto)!!
        val returnDto = noticesService.findById(noticeId)
        return sendResponseHttpByJson("Notices is saved well", returnDto)
    }

    // 공지 수정 리턴 NoticesReturnDto
    @PutMapping("/api/v1/notices/{notice_id}")
    fun update(@PathVariable notice_id : Long, @RequestBody requestDto: NoticesUpdateRequestDto) : ResponseEntity<RestAPIMessages> {
        val noticeId : Long = noticesService.update(notice_id, requestDto)!!
        val returnDto = noticesService.findById(noticeId)
        return sendResponseHttpByJson("Notices is updated well", returnDto)
    }

    // 공지 삭제 리턴 Long
    @DeleteMapping("/api/v1/notices/{notice_id}")
    fun delete(@PathVariable notice_id : Long) : ResponseEntity<RestAPIMessages> {
        val noticeId = noticesService.delete(notice_id)
        return sendResponseHttpByJson("Delete notices with id : $notice_id", noticeId)
    }
}