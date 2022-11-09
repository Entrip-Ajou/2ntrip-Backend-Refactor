package com.entrip.config

import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.PathResource

@Primary
@Configuration
class TempPropertiesConfig {

    private fun propertiesFactoryBean(path: String): PropertiesFactoryBean {
        val propertiesFactoryBean: PropertiesFactoryBean = PropertiesFactoryBean()
        val pathResource: PathResource = PathResource(path)

        propertiesFactoryBean.setLocation(pathResource)
        return propertiesFactoryBean
    }

    @Bean(name = arrayOf("awsS3"))
    public fun propertiesFactoryBeanForS3(): PropertiesFactoryBean =
        propertiesFactoryBean("file:./application-aws-s3.properties")

    @Bean(name = arrayOf("redis"))
    public fun propertiesFactoryBeanForRedis(): PropertiesFactoryBean =
        propertiesFactoryBean("file:./application-redis.properties")

    @Bean(name = arrayOf("security"))
    public fun propertiesFactoryBeanForSecurity(): PropertiesFactoryBean =
        propertiesFactoryBean("file:./application-security.properties")
}