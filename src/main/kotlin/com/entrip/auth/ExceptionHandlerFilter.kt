package com.entrip.auth

import com.entrip.domain.RestAPIMessages
import com.entrip.exception.ExpiredJwtCustomException
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
        logger.info("ExceptionHandlerFilter()")
        try {
            chain.doFilter(request, response)
        } catch (e: ExpiredJwtCustomException) {
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
        logger.info("setErrorResponse")
    }
}