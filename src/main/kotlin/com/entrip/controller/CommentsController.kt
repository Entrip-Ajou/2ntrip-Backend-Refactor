package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.Comments.CommentsReturnDto
import com.entrip.domain.dto.Comments.CommentsSaveRequestDto
import com.entrip.domain.dto.Comments.CommentsUpdateRequestDto
import com.entrip.service.CommentsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class CommentsController(
    final val commentsService: CommentsService
) {

    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<Messages> {
        val messages: Messages = Messages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<Messages>(messages, headers, HttpStatus.OK)
    }

    @PostMapping("/api/v1/comments")
    public fun save (@RequestBody requestDto : CommentsSaveRequestDto) : ResponseEntity<Messages> {
        val returnDtoList : MutableList<CommentsReturnDto> = commentsService.save(requestDto)
        return sendResponseHttpByJson("Comments is saved well", returnDtoList)
    }

    @PutMapping("/api/v1/comments/{comment_id}")
    public fun update (@PathVariable comment_id : Long, @RequestBody requestDto : CommentsUpdateRequestDto) : ResponseEntity<Messages> {
        val returnDtoList : MutableList<CommentsReturnDto> = commentsService.update(comment_id, requestDto)
        return sendResponseHttpByJson("Comments is updated well", returnDtoList)
    }

    @GetMapping("/api/v1/comments/{comment_id}")
    public fun findById (@PathVariable comment_id: Long) : ResponseEntity<Messages> {
        val responseDto = commentsService.findById(comment_id)
        val returnDto = CommentsReturnDto (responseDto)
        return sendResponseHttpByJson("Load comments with id : $comment_id", returnDto)
    }

    @DeleteMapping("/api/v1/comments/{comment_id}")
    public fun delete (@PathVariable comment_id : Long) : ResponseEntity<Messages> {
        val returnDtoList : MutableList<CommentsReturnDto> = commentsService.delete(comment_id)
        return sendResponseHttpByJson("Delete comments with id : $comment_id", returnDtoList)
    }

    @GetMapping("/api/v1/comments/{plan_id}/getAllComments")
    public fun getAllCommentsWithPlanId (@PathVariable plan_id : Long): ResponseEntity<Messages> {
        val returnDtoList : MutableList<CommentsReturnDto> = commentsService.getAllCommentsWithPlanId(plan_id)
        return sendResponseHttpByJson("Get all comments with plan id : $plan_id", returnDtoList)
    }
}