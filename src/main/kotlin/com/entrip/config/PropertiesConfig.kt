package com.entrip.config

import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource

@Primary
@Configuration
class PropertiesConfig {
    @Bean(name = arrayOf("awsS3"))
    public fun propertiesFactoryBean(): PropertiesFactoryBean {
        val propertiesFactoryBean: PropertiesFactoryBean = PropertiesFactoryBean()
        val classPathResource: ClassPathResource = ClassPathResource("application-aws-s3.properties")

        propertiesFactoryBean.setLocation(classPathResource)
        return propertiesFactoryBean
    }
}