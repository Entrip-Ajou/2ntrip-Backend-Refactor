package com.entrip.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.entrip.common.S3Uploader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AmazonS3Config(
    @Value("#{awsS3['cloud.aws.credentials.access-key']}")
    private val accessKey: String,

    @Value("#{awsS3['cloud.aws.credentials.secret-key']}")
    private val secretKey: String,

    @Value("#{awsS3['cloud.aws.region.static']}")
    private val region: String
) {
    @Bean
    public fun amazonS3(): AmazonS3 {
        val basicAWSCredentials: BasicAWSCredentials = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
            .build()
    }

    @Bean
    public fun amazonS3Client(): AmazonS3Client {
        val basicAWSCredentials: BasicAWSCredentials = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
            .withRegion(region)
            .build() as AmazonS3Client
    }
}