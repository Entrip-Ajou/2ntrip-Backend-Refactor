package com.entrip.controller

import com.entrip.common.S3Uploader
import com.entrip.domain.Messages
import com.entrip.service.PhotosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.Charset

@RestController
class S3Controller (
    final val s3Uploader: S3Uploader,

    @Autowired
    val photosService: PhotosService
) {
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

    @PostMapping("api/v1/photos")
    public fun upload (@RequestParam("photos") multipartFile: MultipartFile) : ResponseEntity<Messages> {
        val uploadedPhotoUrl : String = s3Uploader.upload(multipartFile, "static")
        val savedPhotoId = photosService.save(uploadedPhotoUrl)
        return sendResponseHttpByJson("Photo is saved well", savedPhotoId!!)
    }
}