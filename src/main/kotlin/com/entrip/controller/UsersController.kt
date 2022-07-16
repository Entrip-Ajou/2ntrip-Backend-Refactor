package com.entrip.controller

import com.entrip.domain.Messages
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.service.UsersService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset

@RestController
class UsersController (
    val usersService: UsersService
        ){

    @PostMapping("/api/v1/users")
    fun save (@RequestBody requestDto: UsersSaveRequestDto) : ResponseEntity<Messages> {
        val user_id : String = usersService.save(requestDto)
        val responseDto : UsersResponseDto = usersService.findById(user_id)
        val returnDto : UsersReturnDto = UsersReturnDto(responseDto = responseDto)
        val messages : Messages = Messages(
            httpStatus = 200,
            message = "User is saved well",
            data = returnDto
        )
        val headers : HttpHeaders = HttpHeaders()
        headers.contentType = MediaType ("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<Messages>(messages, headers, HttpStatus.OK)
    }
}