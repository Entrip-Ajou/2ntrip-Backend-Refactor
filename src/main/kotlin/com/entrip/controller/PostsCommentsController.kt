package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.PostsComments.PostsCommentsSaveRequestDto
import com.entrip.service.PostsCommentsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class PostsCommentsController(
    final val postsCommentsService: PostsCommentsService
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

    @PostMapping("api/v1/postsComments")
    public fun save(@RequestBody requestDto: PostsCommentsSaveRequestDto): ResponseEntity<Messages> =
        sendResponseHttpByJson("PostsComments is saved well", postsCommentsService.save(requestDto))

    @GetMapping("api/v1/postsComments/{postComment_id}")
    public fun findById(@PathVariable postComment_id: Long): ResponseEntity<Messages> =
        sendResponseHttpByJson(
            "Get PostsComments with id : $postComment_id",
            postsCommentsService.findById(postComment_id)
        )

    @DeleteMapping("api/v1/postsComments/{postComment_id}")
    public fun delete(@PathVariable postComment_id: Long): ResponseEntity<Messages> =
        sendResponseHttpByJson(
            "Delete PostsComments with id : $postComment_id",
            postsCommentsService.delete(postComment_id)
        )

    @GetMapping("api/v1/postsComments/{post_id}/all")
    public fun getAllCommentsWithPostId(@PathVariable post_id: Long): ResponseEntity<Messages> =
        sendResponseHttpByJson(
            "Get all comments with post id : $post_id",
            postsCommentsService.getAllCommentsWithPostId(post_id)
        )
}