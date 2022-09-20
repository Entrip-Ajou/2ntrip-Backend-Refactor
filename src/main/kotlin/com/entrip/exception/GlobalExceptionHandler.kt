package com.entrip.exception

import com.entrip.domain.RestAPIMessages
import com.entrip.exception.authException.ReIssueBeforeAccessTokenExpiredException
import com.entrip.socket.WebSocketEventListener
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.Forbidden
import java.nio.charset.Charset
import java.security.SignatureException

@ControllerAdvice
class GlobalExceptionHandler {

    val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    class dummy(val e: String) {
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            400,
            "MethodArgumentNotValidException\n",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            405,
            "HttpRequestMethodNotSupportedException\n",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            500,
            "IllegalArgumentException\n",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NicknameOrUserIdNotValidException::class)
    fun handleNicknameOrUserIdNotValidException(e: NicknameOrUserIdNotValidException): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            404,
            "NicknameOrUserIdNotValidException\n",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NotAcceptedException::class)
    fun handleNotAcceptedException(e: NotAcceptedException): ResponseEntity<RestAPIMessages> {
        val jsonData = JSONObject()
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            202,
            "NotAcceptedException\n",
            e.data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.ACCEPTED)
    }

    @ExceptionHandler(FailToFindNicknameOrIdException::class)
    fun handleFailToFindNicknameOrIdException(e: FailToFindNicknameOrIdException): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            202,
            "NotAcceptedException\n",
            dummy(e.message!!)
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.ACCEPTED)
    }

    @ExceptionHandler(Forbidden::class)
    fun handleForbidden (e: Forbidden) : ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            404,
            "Forbidden",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.ACCEPTED)
    }

    @ExceptionHandler(io.jsonwebtoken.SignatureException::class)
    fun handleSignatureException(e: SignatureException): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            400,
            "SignatureException",
            "handleSignatureException"
            //e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<RestAPIMessages> {
        logger.error(e.message, e)
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            520,
            "handleEntityNotFoundException : unknown exception\n",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.SERVICE_UNAVAILABLE)
    }

    @ExceptionHandler(ReIssueBeforeAccessTokenExpiredException::class)
    fun handleReIssueBeforeAccessTokenExpiredException(e: Exception): ResponseEntity<RestAPIMessages> {
        logger.error(e.message, e)
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            400,
            "ReIssueBeforeAccessTokenExpiredException",
            e.message!!
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        logger.error(e.message)
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.BAD_REQUEST)
    }

}