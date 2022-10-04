//package com.entrip.config
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.web.servlet.config.annotation.EnableWebMvc
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
//import springfox.documentation.builders.ApiInfoBuilder
//import springfox.documentation.builders.PathSelectors
//import springfox.documentation.builders.RequestHandlerSelectors
//import springfox.documentation.service.ApiInfo
//import springfox.documentation.spi.DocumentationType
//import springfox.documentation.spring.web.plugins.Docket
//import springfox.documentation.swagger2.annotations.EnableSwagger2
//
//@Configuration
//@EnableSwagger2
//@EnableWebMvc
//class SwaggerConfig : WebMvcConfigurer {
//
//    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
//        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//        registry.addResourceHandler("/index.html").addResourceLocations("classpath:/static")
//        super.addResourceHandlers(registry)
//    }
//
//    @Bean
//    fun productApi(): Docket {
//        return Docket(DocumentationType.SWAGGER_2)
//            .select()
//            .apis(RequestHandlerSelectors.basePackage("com.entrip"))
//            .build()
//            .apiInfo(this.metaInfo())
//    }
//
//    private fun metaInfo(): ApiInfo {
//        return ApiInfoBuilder()
//            .title("2ntrip-api")
//            .description("2ntrip API 명세서입니다.")
//            .version("1.0")
//            .build()
//    }
//}