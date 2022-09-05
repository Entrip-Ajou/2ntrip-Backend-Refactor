package com.entrip.auth.jwt

import com.entrip.exception.ExpiredAccessTokenException
import com.entrip.exception.ExpiredRefreshTokenException
import io.jsonwebtoken.SignatureException
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

    @Throws(
        IOException::class,
        ServletException::class,
        ExpiredAccessTokenException::class,
        ExpiredRefreshTokenException::class,
        SignatureException::class
    )
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        // 헤더에서 JWT 를 받아옵니다.
        val accessToken: String? = jwtTokenProvider.resolveAccessToken((request as HttpServletRequest))

        // Check Access Token is valid
        try {
            if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
                // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옵니다.
                val authentication = jwtTokenProvider.getAuthentication(accessToken)
                // SecurityContext 에 Authentication 객체를 저장합니다.
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: ExpiredAccessTokenException) {
            throw ExpiredAccessTokenException("Access Token was expired!")
        } catch (e: SignatureException) {
            throw SignatureException("Access Token is not valid!")
        }
        chain.doFilter(request, response)
    }
}