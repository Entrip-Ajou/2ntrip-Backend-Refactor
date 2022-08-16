package com.entrip.controller

import com.entrip.s3.S3Uploader
import com.entrip.domain.RestAPIMessages
import com.entrip.service.PhotosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.Charset

@RestController
class PhotosController(
    final val photosService: PhotosService,

    @Autowired
    final val s3Uploader: S3Uploader,
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

    @PostMapping("api/v1/photos/{priority}")
    public fun upload(
        @RequestParam("photos") multipartFile: MultipartFile,
        @PathVariable priority: Long = 1
    ): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Photo is saved well", photosService.uploadAtS3(multipartFile, priority)!!)

    @PutMapping("api/v1/photos/{photo_id}/{post_id}/addPosts")
    public fun addPostsToPhotos(
        @PathVariable photo_id: Long,
        @PathVariable post_id: Long
    ): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Add photos $photo_id to posts $post_id",
            photosService.addPostsToPhotos(photo_id, post_id)
        )

    @GetMapping("api/v1/photos/{photo_id}")
    public fun findById(@PathVariable photo_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Get photos with id : $photo_id", photosService.findById(photo_id))

    @DeleteMapping("api/v1/photos/{photo_id}")
    public fun delete(@PathVariable photo_id: Long): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Delete photos $photo_id", photosService.delete(photo_id))

}