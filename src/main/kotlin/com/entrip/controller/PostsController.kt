package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Posts.PostsSaveRequestDto
import com.entrip.service.PostsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset

@RestController
class PostsController(
    final val postsService: PostsService
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

    @PostMapping("api/v1/posts")
    public fun save(@RequestBody requestDto: PostsSaveRequestDto): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Posts is saved well", postsService.save(requestDto))

    @GetMapping("api/v1/posts/{post_id}")
    public fun findById(@PathVariable post_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Get posts with id : $post_id", postsService.findById(post_id))

    @DeleteMapping("api/v1/posts/{post_id}")
    public fun delete(@PathVariable post_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Delete posts with id : $post_id", postsService.delete(post_id))

    @GetMapping("api/v1/posts/{pageNumber}/all")
    public fun getPostsListWithPageNumber (@PathVariable pageNumber : Long) : ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Get list of posts with page number : $pageNumber", postsService.getPostsListWithPageNumber(pageNumber))

    @PutMapping("api/v1/posts/{post_id}/{user_id}/like")
    public fun raiseLikeNumber (@PathVariable post_id: Long, @PathVariable user_id : String) : ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("$user_id raise posts $post_id 's like number", postsService.raiseLikeNumber(post_id,user_id))

    @PutMapping("api/v1/posts/{post_id}/{user_id}/dislike")
    public fun decreaseLikeNumber (@PathVariable post_id: Long, @PathVariable user_id : String) : ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("$user_id raise posts $post_id 's like number", postsService.decreaseLikeNumber(post_id,user_id))

}