package com.entrip.controller

import com.entrip.common.S3Uploader
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class S3Controller (
    final val s3Uploader: S3Uploader
) {
    @PostMapping("api/v1/images")
    public fun upload (@RequestParam("images") multipartFile: MultipartFile) : String {
        s3Uploader.upload(multipartFile, "static")
        return "test"
    }
}