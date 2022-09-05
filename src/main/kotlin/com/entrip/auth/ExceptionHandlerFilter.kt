package com.entrip.auth

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.domain.RestAPIMessages
import com.entrip.exception.ExpiredAccessTokenException
import com.entrip.exception.ExpiredRefreshTokenException
import io.jsonwebtoken.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class ExceptionHandlerFilter() : OncePerRequestFilter() {

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        try {
            chain.doFilter(request, response)
        } catch (e: ExpiredAccessTokenException) {
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e)
        } catch (e: SignatureException) {
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e)
        } catch (e: ExpiredRefreshTokenException) {
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e)
        }
    }

    public fun setErrorResponse(status: HttpStatus, response: HttpServletResponse, e: Throwable) {
        val restAPIMessages = RestAPIMessages(
            status.value(),
            e.message.toString(),
            e.message!!
        )
        response.status = status.value()
        response.contentType = "application/json"

        try {
            val json: String = restAPIMessages.convertToJson()
            response.writer.write(json)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}