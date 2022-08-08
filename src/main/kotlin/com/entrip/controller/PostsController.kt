package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.Posts.PostsSaveRequestDto
import com.entrip.service.PostsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset

@RestController
class PostsController (
    final val postsService: PostsService
        ){
    private fun sendResponseHttpByJson (message : String, data : Any) : ResponseEntity<Messages> {
        val messages : Messages = Messages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers : HttpHeaders = HttpHeaders()
        headers.contentType = MediaType ("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<Messages>(messages, headers, HttpStatus.OK)
    }

    @PostMapping("api/v1/posts")
    public fun save(@RequestBody requestDto : PostsSaveRequestDto) : ResponseEntity<Messages>
    = sendResponseHttpByJson("Posts is saved well", postsService.save(requestDto))

    @GetMapping("api/v1/posts/{post_id}")
    public fun findById (@PathVariable post_id : Long) : ResponseEntity<Messages>
    = sendResponseHttpByJson("Get posts with id : $post_id", postsService.findById(post_id))
}