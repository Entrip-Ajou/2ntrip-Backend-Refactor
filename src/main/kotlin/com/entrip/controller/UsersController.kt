package com.entrip.controller

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.exception.NotAcceptedException
import com.entrip.service.UsersService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
class UsersController(
    private final val usersService: UsersService,
    private val passwordEncoder: PasswordEncoder
) : BaseController() {
    @PostMapping("/api/v2/users")
    public fun save(@RequestBody requestDto: UsersSaveRequestDto): ResponseEntity<RestAPIMessages> {
        val user_id: String? = usersService.save(requestDto)
        val responseDto = usersService.findByUserIdAndReturnResponseDto(user_id)
        return sendResponseHttpByJson("User is saved well", responseDto)
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
    public fun findById(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Load user with id : $user_id", usersService.findByUserIdAndReturnResponseDto(user_id))


    @GetMapping("api/v1/users/{user_id}/all")
    public fun findAllPlannersWithUsersId(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson(
            "Load all planners with user, id : $user_id",
            usersService.findAllPlannersWithUserId(user_id)
        )


    @GetMapping("api/v2/users/{nickname}/nickname/exist")
    public fun isExistNickname(@PathVariable nickname: String): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = usersService.isExistNickname(nickname)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if nickname $nickname is Exist", isExist)
    }

    @GetMapping("api/v2/users/{user_id}/user_id/exist")
    public fun isExistUserId(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> {
        val isExist: Boolean = usersService.isExistUserId(user_id)
        if (!isExist) throw NotAcceptedException(false)
        return sendResponseHttpByJson("Check if user_id $user_id is Exist", isExist)
    }

    @DeleteMapping("/api/v1/users/{user_id}")
    public fun delete(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Delete user with id : $user_id", usersService.delete(user_id)!!)


    @PutMapping("api/v1/users/token/{user_id}/{token}")
    public fun addToken(@PathVariable user_id: String, @PathVariable token: String): ResponseEntity<RestAPIMessages> {
        val updatedUserId: String = usersService.updateToken(user_id, token)
        val returnDto = usersService.findByUserIdAndReturnResponseDto(user_id)
        return sendResponseHttpByJson("Update user $user_id's token : $token", returnDto)
    }

    @GetMapping("api/v1/users/findUserWithNicknameOrUserId/{nicknameOrUserId}")
    public fun findUserWithNicknameOrUserId(@PathVariable nicknameOrUserId: String): ResponseEntity<RestAPIMessages> {
        val targetUserId = usersService.findUserWithNicknameOrUserId(nicknameOrUserId)
        val returnDto = usersService.findByUserIdAndReturnResponseDto(targetUserId)
        return sendResponseHttpByJson("Get user with nicknameOrUserId : $nicknameOrUserId", returnDto)
    }

    @PostMapping("api/v2/users/login")
    public fun login(@RequestBody requestDto: UsersLoginRequestDto): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Success to Login!", usersService.login(requestDto))

    @GetMapping("api/v2/users/reIssue/{refreshToken}")
    public fun reIssue(@PathVariable refreshToken: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Change Access Token", usersService.reIssue(refreshToken))

    @DeleteMapping("api/v1/users/{user_id}/logout")
    fun logout(@PathVariable user_id: String): ResponseEntity<RestAPIMessages> =
        sendResponseHttpByJson("Logout ${user_id}", usersService.logout(user_id))
}