package com.entrip.config

import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource

@Primary
@Configuration
class PropertiesConfig {

    // Have to change to reusable logic

    @Bean(name = arrayOf("awsS3"))
    public fun propertiesFactoryBeanForS3(): PropertiesFactoryBean {
        val propertiesFactoryBean: PropertiesFactoryBean = PropertiesFactoryBean()
        val classPathResource: ClassPathResource = ClassPathResource("application-aws-s3.properties")

        propertiesFactoryBean.setLocation(classPathResource)
        return propertiesFactoryBean
    }

    @Bean(name = arrayOf("redis"))
    public fun propertiesFactoryBeanForRedis(): PropertiesFactoryBean {
        val propertiesFactoryBean: PropertiesFactoryBean = PropertiesFactoryBean()
        val classPathResource: ClassPathResource = ClassPathResource("application-redis.properties")

        propertiesFactoryBean.setLocation(classPathResource)
        return propertiesFactoryBean
    }

    @Bean(name = arrayOf("security"))
    public fun propertiesFactoryBeanForSecurity(): PropertiesFactoryBean {
        val propertiesFactoryBean: PropertiesFactoryBean = PropertiesFactoryBean()
        val classPathResource: ClassPathResource = ClassPathResource("application-security.properties")

        propertiesFactoryBean.setLocation(classPathResource)
        return propertiesFactoryBean
    }

}