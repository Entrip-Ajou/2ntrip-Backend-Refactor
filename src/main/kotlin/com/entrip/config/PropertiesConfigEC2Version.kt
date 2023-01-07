//package com.entrip.config
//
//import org.springframework.beans.factory.config.PropertiesFactoryBean
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import org.springframework.core.io.PathResource
//
//@Primary
//@Configuration
//class PropertiesConfigEC2Version {
//
//    private val defaultEC2Path: String = "/home/ec2-user/app/step3/properties/"
//
//    private fun propertiesFactoryBean(path: String): PropertiesFactoryBean {
//        val propertiesFactoryBean: PropertiesFactoryBean = PropertiesFactoryBean()
//        val pathResource: PathResource = PathResource(path)
//
//        propertiesFactoryBean.setLocation(pathResource)
//        return propertiesFactoryBean
//    }
//
//    @Bean(name = arrayOf("awsS3"))
//    public fun propertiesFactoryBeanForS3(): PropertiesFactoryBean =
//        propertiesFactoryBean("${defaultEC2Path}application-aws-s3.properties")
//
//    @Bean(name = arrayOf("redis"))
//    public fun propertiesFactoryBeanForRedis(): PropertiesFactoryBean =
//        propertiesFactoryBean("${defaultEC2Path}application-redis.properties")
//
//    @Bean(name = arrayOf("security"))
//    public fun propertiesFactoryBeanForSecurity(): PropertiesFactoryBean =
//        propertiesFactoryBean("${defaultEC2Path}application-security.properties")
//}