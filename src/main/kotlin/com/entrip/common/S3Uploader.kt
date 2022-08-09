package com.entrip.common

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.PutObjectRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

@Component
class S3Uploader(
    final val amazonS3Client: AmazonS3Client,

    @Value("#{awsS3['cloud.aws.s3.bucket']}")
    private val bucket: String
) {

    private val logger: Logger = LoggerFactory.getLogger(S3Uploader::class.java)

    private fun convert(file: MultipartFile): Optional<File> {
        val convertFile: File = File(System.getProperty("user.dir") + "/" + file.originalFilename)
        if (convertFile.createNewFile()) {
            try {
                val fos: FileOutputStream = FileOutputStream(convertFile)
                fos.write(file.bytes)

            } catch (e: IOException) {
            }
            return Optional.of(convertFile)
        }
        return Optional.empty()
    }

    public fun upload(multipartFile: MultipartFile, dirName: String): UploadedPhotoInformation {
        val uploadFile: File = convert(multipartFile).orElseThrow {
            IllegalArgumentException("Error : MultipartFile -> File convert fail")
        }
        return upload(uploadFile, dirName)
    }

    public fun upload(uploadFile: File, dirName: String): UploadedPhotoInformation {
        //val filename : String = dirName + "/" + UUID.randomUUID() + uploadFile.name
        val filename: String = dirName + "/" + uploadFile.name
        val uploadImageUrl: String = putS3(uploadFile, filename)
        removeNewFile(uploadFile)
        return UploadedPhotoInformation(uploadImageUrl, filename)
    }

    public fun putS3(uploadFile: File, fileName: String): String {
        amazonS3Client.putObject(
            PutObjectRequest(
                bucket,
                fileName,
                uploadFile
            ).withCannedAcl(CannedAccessControlList.PublicRead)
        )
        return amazonS3Client.getUrl(bucket, fileName).toString()
    }

    private fun removeNewFile(targetFile: File): Unit {
        if (targetFile.delete()) {
            return
        }
    }
}