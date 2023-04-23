package com.entrip.Users

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.controller.UsersController
import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.service.RedisService
import com.entrip.service.UsersService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@MockkBean(JpaMetamodelMappingContext::class)
@WebMvcTest(UsersController::class)
class UsersControllerTest() : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var usersController: UsersController

    @MockkBean
    lateinit var usersService: UsersService

    @MockkBean
    lateinit var redisService: RedisService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @MockkBean
    lateinit var authenticationManager: AuthenticationManager

    @MockkBean
    lateinit var userDetailsService: UserDetailsService

    final val objectMapper = ObjectMapper()


    val validUserId = "test@gmail.com"
    val invalidUserId = "invalid@gmail.com"
    val validNickname = "testNickname"
    val invalidNickname = "invalidTestNickname"
    val validPassword = "password"
    val invalidPassword = "invalidPassword"

    init {
        given("UsersSaveRequestDto를 주고") {
            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = validUserId,
                nickname = validNickname,
                gender = 1,
                photoUrl = "testPhotoUrl.com",
                password = validPassword
            )

            val users = usersSaveRequestDto.toEntity()
            val usersResponseDto = UsersResponseDto(users)
            val expected = RestAPIMessages(
                httpStatus = 200,
                message = "User is saved well",
                data = usersResponseDto
            )

            every { usersService.save(any()) } returns validUserId
            every { usersService.findByUserIdAndReturnResponseDto(validUserId) } returns usersResponseDto

            `when`("/api/v2/users POST 요청 보내면") {
                then("HttpStatus.OK, UsersReturnDto가 REST로 응답") {

                    mockMvc.post("/api/v2/users") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(usersSaveRequestDto)
                    }.andExpect {
                        status { isOk() }
                        content { objectMapper.writeValueAsString(expected) }
                    }

                }
            }
        }
        given("validNickname, invalidNickname 이 존재하는 상황에서") {
            val validExpected = RestAPIMessages(
                httpStatus = 200,
                message = "Check if nickname $validNickname is Exist",
                data = true
            )

            val invalidExpected = RestAPIMessages(
                httpStatus = 202,
                message = "NotAcceptedException\n",
                data = false
            )

            `when`("/api/v2/users/{nickname}/nickname/exist, validNickname GET 요청 보내면") {
                every { usersService.isExistNickname(validNickname) } returns true
                then("HttpStatus.OK, true가 REST로 응답") {
                    mockMvc.get("/api/v2/users/{nickname}/nickname/exist", validNickname) {
                        contentType = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isOk() }
                        content { objectMapper.writeValueAsString(validExpected) }
                    }
                }
            }

            `when`("/api/v2/users/{nickname}/nickname/exist, invalidNickname GET 요청 보내면") {
                every { usersService.isExistNickname(invalidNickname) } returns false
                then("HttpStatus.Accepted, false가 REST로 응답") {
                    mockMvc.get("/api/v2/users/{nickname}/nickname/exist", invalidNickname) {
                        contentType = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isAccepted() }
                        content { objectMapper.writeValueAsString(invalidExpected) }
                    }
                }
            }
        }

        given("validUserId, invalidUserId가 존재하는 상황에서") {
            val validExpected = RestAPIMessages(
                httpStatus = 200,
                message = "Check if user_id $validUserId is Exist",
                data = true
            )

            val invalidExpected = RestAPIMessages(
                httpStatus = 202,
                message = "NotAcceptedException\n",
                data = false
            )

            `when`("/api/v2/users/{user_id}/user_id/exist, validUserId GET 요청 보내면") {
                every { usersService.isExistUserId(validUserId) } returns true
                then("HttpStatus.OK, true가 REST로 응답") {
                    mockMvc.get("/api/v2/users/{user_id}/user_id/exist", validUserId) {
                        contentType = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isOk() }
                        content { objectMapper.writeValueAsString(validExpected) }
                    }
                }
            }

            `when`("/api/v2/users/{user_id}/user_id/exist, invalidUserId GET 요청 보내면") {
                every { usersService.isExistUserId(invalidUserId) } returns false
                then("HttpStatus.Accepted, false가 REST로 응답") {
                    mockMvc.get("/api/v2/users/{user_id}/user_id/exist", invalidUserId) {
                        contentType = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isAccepted() }
                        content { objectMapper.writeValueAsString(invalidExpected) }
                    }
                }
            }
        }
    }

}