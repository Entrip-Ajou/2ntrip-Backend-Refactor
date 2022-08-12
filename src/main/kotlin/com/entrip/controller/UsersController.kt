package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.exception.NotAcceptedException
import com.entrip.service.UsersService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class UsersController(
    final val usersService: UsersService
) {
    private fun sendResponseHttpByJson(message: String, data: Any): ResponseEntity<RestAPIMessages> {
        val restAPIMessages: RestAPIMessages = RestAPIMessages(
            httpStatus = 200,
            message = message,
            data = data
        )
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType("application", "json", Charset.forName("UTF-8"))
        return ResponseEntity<RestAPIMessages>(restAPIMessages, headers, HttpStatus.OK)
    }

    @PostMapping("/api/v1/users")
    public fun save(@RequestBody requestDto: UsersSaveRequestDto): ResponseEntity<RestAPIMessages> {
        val user_id: String? = usersService.save(requestDto)
        val responseDto: UsersResponseDto = usersService.findByUserId(user_id)
        val returnDto: UsersReturnDto = UsersReturnDto(responseDto = responseDto)
        return sendResponseHttpByJson("User is saved well", returnDto)
    }

    @PutMapping("/api/v1/users/{planner_id}/{user_id}")
    public fun addPlanners(
        @PathVariable planner_id: Long,
        @PathVariable user_id: String
    ): ResponseEntity<RestAPIMessages> {
        usersService.addPlanners(planner_id, user_id)
        return sendResponseHttpByJson("Add planner, id : $planner_id with user, id : $user_id", user_id)
    }

    @GetMapping("/api/v1/users/{user_id}")
    public fun findById(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val responseDto: UsersResponseDto = usersService.findByUserId(user_id)
        val returndto: UsersReturnDto = UsersReturnDto(responseDto)
        return sendResponseHttpByJson("Load user with id : $user_id", returndto)
    }

    @GetMapping("api/v1/users/{user_id}/all")
    public fun findAllPlannersWithUsersId(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val plannersList: List<PlannersReturnDto> = usersService.findAllPlannersWithUserId(user_id)
        return sendResponseHttpByJson("Load all planners with user, id : $user_id", plannersList)
    }

    @GetMapping("api/v1/users/{nickname}/nickname/exist")
    public fun isExistNickname(@PathVariable nickname: String): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = usersService.isExistNickname(nickname)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if nickname $nickname is Exist", isExist)
    }

    @GetMapping("api/v1/users/{user_id}/user_id/exist")
    public fun isExistUserId(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = usersService.isExistUserId(user_id)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if user_id $user_id is Exist", isExist)
    }

    @DeleteMapping("/api/v1/users/{user_id}")
    public fun delete(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val deletedUserId: String? = usersService.delete(user_id)
        return sendResponseHttpByJson("Delete user with id : $user_id", deletedUserId!!)
    }

    @PutMapping("api/v1/users/token/{user_id}/{token}")
    public fun addToken(@PathVariable user_id: String, @PathVariable token: String): ResponseEntity<RestAPIMessages> {
        val updatedUserId: String = usersService.updateToken(user_id, token)
        val responseDto: UsersResponseDto = usersService.findByUserId(user_id)
        val returnDto: UsersReturnDto = UsersReturnDto(responseDto)
        return sendResponseHttpByJson("Update user $user_id's token : $token", returnDto)
    }

    @GetMapping("api/v1/users/findUserWithNicknameOrUserId/{nicknameOrUserId}")
    public fun findUserWithNicknameOrUserId(@PathVariable nicknameOrUserId: String): ResponseEntity<RestAPIMessages> {
        val targetUserId = usersService.findUserWithNicknameOrUserId(nicknameOrUserId)
        val responseDto: UsersResponseDto = usersService.findByUserId(targetUserId)
        val returnDto: UsersReturnDto = UsersReturnDto(responseDto)
        return sendResponseHttpByJson("Get user with nicknameOrUserId : $nicknameOrUserId", returnDto)
    }
}