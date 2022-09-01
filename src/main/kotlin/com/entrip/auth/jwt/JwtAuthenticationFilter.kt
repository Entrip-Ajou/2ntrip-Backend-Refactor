package com.entrip.auth.jwt

import com.entrip.exception.ExpiredJwtCustomException
import com.entrip.exception.NotAcceptedException
import com.entrip.s3.S3Uploader
import io.jsonwebtoken.ExpiredJwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider): GenericFilterBean() {

    private val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    @Throws(IOException::class, ServletException::class, ExpiredJwtCustomException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        logger.info("JwtAuthenticationFilter")
        // 헤더에서 JWT 를 받아옵니다.
        val token: String? = jwtTokenProvider.resolveToken((request as HttpServletRequest))
        // 유효한 토큰인지 확인합니다.
        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옵니다.
                val authentication = jwtTokenProvider.getAuthentication(token)
                // SecurityContext 에 Authentication 객체를 저장합니다.
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: ExpiredJwtCustomException) {
            throw ExpiredJwtCustomException("Token was expired!")
        }
        chain.doFilter(request, response)
    }
}