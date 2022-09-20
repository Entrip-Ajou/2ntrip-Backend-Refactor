package com.entrip.auth

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.domain.RestAPIMessages
import com.entrip.exception.authException.ExpiredAccessTokenException
import com.entrip.exception.authException.ExpiredRefreshTokenException
import io.jsonwebtoken.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class ExceptionHandlerFilter(val jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        try {
            chain.doFilter(request, response)
        } catch (e: ExpiredAccessTokenException) {
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e, "ExpiredAccessTokenException")
        } catch (e: SignatureException) {
            logger.error("SignatureException at ExceptionHandlerFilter!")
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e, "SignatureException")
        } catch (e: ExpiredRefreshTokenException) {
            setErrorResponse(HttpStatus.BAD_REQUEST, response, e, "ExpiredRefreshTokenException")
        } catch (e: Exception) {
            logger.error("Exception at ExceptionHandlerFilter!")
            setErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, response, e, "Exception")
        }
    }

    public fun setErrorResponse(status: HttpStatus, response: HttpServletResponse, e: Throwable, eName: String) {
        val restAPIMessages = RestAPIMessages(
            status.value(),
            eName,
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