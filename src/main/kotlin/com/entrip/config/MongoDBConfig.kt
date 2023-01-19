package com.entrip.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory

@Configuration
class MongoDBConfig {
    @Value("#{mongodb['spring.data.mongodb.uri']}")
    private val connectionString: String? = null

    @Bean
    public fun mongoDatabaseFactory(): MongoDatabaseFactory =
        SimpleMongoClientDatabaseFactory(connectionString!!)

    @Bean
    public fun mongoTemplate(): MongoTemplate =
        MongoTemplate(mongoDatabaseFactory())

}