package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.PostsNestedComments.PostsNestedCommentsSaveRequestDto
import com.entrip.service.PostsNestedCommentsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset

@RestController //
class PostsNestedCommentsController(
    private final val postsNestedCommentsService: PostsNestedCommentsService
) {
    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<RestAPIMessages> {
        val restAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

    @PostMapping("api/v1/postsNestedComments")
    public fun save(@RequestBody requestDto: PostsNestedCommentsSaveRequestDto): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("PostsNestedComments is saved well", postsNestedCommentsService.save(requestDto))

    @GetMapping("api/v1/postsNestedComments/{postNestedComment_id}")
    public fun findById(@PathVariable postNestedComment_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Get PostsNestedComments with id : $postNestedComment_id",
            postsNestedCommentsService.findById(postNestedComment_id)
        )

    @DeleteMapping("api/v1/postsNestedComments/{postNestedComment_id}")
    public fun delete(@PathVariable postNestedComment_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Delete PostsNestedComments with id : $postNestedComment_id",
            postsNestedCommentsService.delete(postNestedComment_id)
        )

    @GetMapping("api/v1/postsNestedComments/{postComment_id}/all")
    public fun getAllNestedCommentsWithPostCommentId(@PathVariable postComment_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Get all nestedComments with postComment id : $postComment_id",
            postsNestedCommentsService.getAllNestedCommentsWithPostCommentId(postComment_id)
        )
}