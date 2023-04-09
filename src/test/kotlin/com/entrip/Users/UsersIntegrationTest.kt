package com.entrip.Users

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersLoginResReturnDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@AutoConfigureRestDocs
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class UsersIntegrationTest() : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)
    private final val logger = LoggerFactory.getLogger(UsersIntegrationTest::class.java)

    @Autowired
    lateinit var mockMvc: MockMvc


    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var plannersRepository: PlannersRepository

    final val user_id = "test@gmail.com"
    final val nickname = "testNickname"
    final val gender = 1
    final val photoUrl = "testPhotoUrl.com"
    final val password = "testPassword"
    final val tokenValue = "tokenValue"

    final val objectMapper = ObjectMapper().registerModule(KotlinModule())

    lateinit var accessToken: String
    lateinit var refreshToken: String

    init {
        beforeSpec {
            usersRepository.deleteAll()
            plannersRepository.deleteAll()
        }

        given("usersSaveRequestDto가 주어졌을 때") {
            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = user_id, nickname = nickname, gender = gender, password = password, photoUrl = photoUrl
            )
            val usersReturnDto = UsersReturnDto(
                user_id = user_id, nickname = nickname, gender = gender, photoUrl = photoUrl, token = null
            )

            val successExpectedResponse = RestAPIMessages(
                httpStatus = 200, message = "User is saved well", data = usersReturnDto
            )

            val failExpectedResponse = RestAPIMessages(
                httpStatus = 202, message = "NotAcceptedException\n", data = UsersReturnDto("", "", -1, "", "")
            )
            `when`("회원 가입을 요청하면") {
                then("회원 가입이 완료되고 usersReturnDto가 리턴된다") {
                    mockMvc.post("/api/v2/users") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(usersSaveRequestDto)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }.andDo {
                        handle(
                            MockMvcRestDocumentation.document(
                                "Users_save",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestFields(
                                    fieldWithPath("user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("gender").description("사용자 성별, 1은 남성 0은 여성")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("password").description("사용자 암호").type(JsonFieldType.STRING),
                                    fieldWithPath("photoUrl").description("사용자 프로필사진 url").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.gender").description("사용자 성별, 1은 남성 0은 여성")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.photoUrl").description("사용자 프로필사진 url")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.token").description("사용자 FCM 토큰").type(JsonFieldType.NULL)
                                )
                            )
                        )
                    }
                }
            }
            `when`("같은 정보로 한 번 더 요청하면") {
                then("이미 저장된 유저이기 때문에 HttpStatus.Accepted()를 리턴한다") {
                    mockMvc.post("/api/v2/users") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(usersSaveRequestDto)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(failExpectedResponse)) }
                    }
                }
            }
        }

        given("존재하는 user_id와 nickname으로") {
            val successExpectedResponseForNickname = RestAPIMessages(
                httpStatus = 200, message = "Check if nickname $nickname is Exist", data = true
            )
            val successExpectedResponseForUserId = RestAPIMessages(
                httpStatus = 200, message = "Check if user_id $user_id is Exist", data = true
            )
            `when`("nickname의 존재 여부를 확인하면") {
                then("true가 리턴된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v2/users/{nickname}/nickname/exist", nickname)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponseForNickname)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_isExistNickname",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("nickname").description("중복 검사 대상 닉네임")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 여부").type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }
            `when`("user_id의 존재 여부를 확인하면") {
                then("true가 리턴된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v2/users/{user_id}/user_id/exist", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponseForUserId)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_isExistUserId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("user_id").description("중복 검사 대상 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 여부").type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }

            }
        }

        given("저장되지 않은 nickname과 user_id가 주어졌을 때") {
            val expectedErrorResponse = RestAPIMessages(
                httpStatus = 202, message = "NotAcceptedException\n", data = false
            )
            `when`("nickname의 존재 여부를 확인하면") {
                then("HttpStatus.Accepted()를 리턴한다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v2/users/{nickname}/nickname/exist",
                            "invalid" + nickname
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isAccepted)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedErrorResponse))).andDo(
                            MockMvcRestDocumentation.document(
                                "Users_isExistNickname_failCase",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("nickname").description("중복 검사 대상 닉네임")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 여부").type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }

            `when`("user_id의 존재 여부를 확인하면") {
                then("HttpStatus.Accepted()를 리턴한다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v2/users/{user_id}/user_id/exist",
                            "invalid" + user_id
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isAccepted)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedErrorResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_isExistNickname_failCase",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("user_id").description("중복 검사 대상 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 여부").type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }
        }

        given("회원가입한 유저의 올바른 아이디와 패스워드가 주어졌을 때")
        {
            val usersLoginRequestDto = UsersLoginRequestDto(
                user_id = user_id, password = password
            )
            `when`("로그인을 시도하면") {
                then("로그인에 성공하고 accessToken, refreshToken 을 받아온다") {
                    val result = mockMvc.post("/api/v2/users/login") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(usersLoginRequestDto)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        handle(
                            MockMvcRestDocumentation.document(
                                "Users_login",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestFields(
                                    fieldWithPath("user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("password").description("사용자 패스워드").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.accessToken").description("사용자 accessToken 값")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.refreshToken").description("사용자 refreshToken 값")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING)
                                )
                            )
                        )
                    }.andReturn()

                    // 그리고 로그인 결과의 AccessToken와 RefreshToken의 값을 따로 저장한다

                    // 1. MockMvcResult의 response를 String (JSON)으로 바꾼다
                    val content = result.response.contentAsString

                    // 2. String (JSON)을 RestAPIMessages로 변환한다
                    val restAPIMessages = objectMapper.readValue<RestAPIMessages>(content, RestAPIMessages::class.java)

                    // 3. RestAPIMessages의 data 필드를 String으로 다시 변환한다
                    val str = restAPIMessages.data.toString()

                    // 4. String으로 바꾼 data 필드를 전처리해서 accessToken, refreshToken 값을 찾아낸
                    val pairs = str.substring(1, str.length - 1).split(", ")
                    for (pair in pairs) {
                        val keyValue = pair.split("=")
                        if (keyValue[0] == "accessToken") accessToken = keyValue[1]
                        if (keyValue[0] == "refreshToken") refreshToken = keyValue[1]
                    }

                }
            }
        }

        given("올바르지 않은 아이디로")
        {
            val UsersLoginRequestDtoWithInvalidId = UsersLoginRequestDto(
                user_id = "invalid" + user_id, password = password
            )
            val expectedResponse = RestAPIMessages(
                httpStatus = 202, message = "NotAcceptedException\n", data = UsersLoginResReturnDto("", "", "", "")
            )
            `when`("로그인을 시도하면") {
                then("HttpStatus.Accepted를 리턴한다") {
                    mockMvc.post("/api/v2/users/login") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(UsersLoginRequestDtoWithInvalidId)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(expectedResponse)) }
                    }.andDo {
                        handle(
                            MockMvcRestDocumentation.document(
                                "Users_login_failById",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestFields(
                                    fieldWithPath("user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("password").description("사용자 패스워드").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.accessToken").description("사용자 accessToken 값")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.refreshToken").description("사용자 refreshToken 값")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.detailedMessage").description("상세 메시지")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                    }
                }
            }
        }

        given("올바르지 않은 패스워드로")
        {
            val UsersLoginRequestDtoWithInvalidPassword = UsersLoginRequestDto(
                user_id = user_id, password = "invalid" + password
            )
            val expectedResponse = RestAPIMessages(
                httpStatus = 202, message = "NotAcceptedException\n", data = UsersLoginResReturnDto("", "", "", "")
            )
            `when`("로그인을 시도하면") {
                then("HttpStatus.Accepted를 리턴한다") {
                    mockMvc.post("/api/v2/users/login") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(UsersLoginRequestDtoWithInvalidPassword)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(expectedResponse)) }
                    }.andDo {
                        handle(
                            MockMvcRestDocumentation.document(
                                "Users_login_failByPassword",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestFields(
                                    fieldWithPath("user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("password").description("사용자 패스워드").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.accessToken").description("사용자 accessToken 값")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.refreshToken").description("사용자 refreshToken 값")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.detailedMessage").description("상세 메시지")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                    }
                }
            }
        }

        given("올바른 accessToken을 Http Header에 실어서 (1)")
        {

            val usersReturnDto = UsersReturnDto(
                user_id = user_id, nickname = nickname, gender = gender, photoUrl = photoUrl, token = tokenValue
            )
            val expectedResponse = RestAPIMessages(
                httpStatus = 200, message = "Update user $user_id's token : $tokenValue", data = usersReturnDto
            )
            `when`("addToken을 실행하면") {
                then("token이 더해진 UsersReturnDto가 리턴된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put(
                            "/api/v1/users/token/{user_id}/{token}",
                            user_id,
                            "tokenValue"
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_addToken",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("user_id").description("토큰 추가할 사용자 이메일"),
                                    parameterWithName("token").description("추가할 토근 값")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.gender").description("사용자 성별, 1은 남성 0은 여성")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.photoUrl").description("사용자 프로필사진 url")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.token").description("사용자 FCM 토큰").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
        }


        given("올바른 accessToken을 Http Header에 실어서 (2)")
        {
            val usersReturnDto = UsersReturnDto(
                user_id = user_id, nickname = nickname, gender = gender, photoUrl = photoUrl, token = tokenValue
            )
            val successExpectedResponse = RestAPIMessages(
                httpStatus = 200, message = "Load user with id : $user_id", data = usersReturnDto
            )
            val failExpectedResponse = RestAPIMessages(
                httpStatus = 500,
                message = "IllegalArgumentException\n",
                data = "Error raise at UsersRepository.findByIdinvalid$user_id"
            )
            `when`("존재하는 아이디로 findById를 실행하면") {
                then("user_id로 검색해서 나온 UsersReturnDto가 리턴된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/users/{user_id}", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_findById",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("user_id").description("검색할 사용자 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.gender").description("사용자 성별, 1은 남성 0은 여성")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.photoUrl").description("사용자 프로필사진 url")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.token").description("사용자 FCM 토큰").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("존재하지 않는 아이디로 findById를 실행하면") {
                then("HttpStatus.Internal_Server_Error를 리턴한다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/users/{user_id}", "invalid" + user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isInternalServerError)
                        .andExpect(content().json(objectMapper.writeValueAsString(failExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_findById_failCase",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("user_id").description("검색할 사용자 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
        }

        given("올바른 accessToken을 Http Header에 실어서 (3)")
        {
            val usersReturnDto = UsersReturnDto(
                user_id = user_id, nickname = nickname, gender = gender, photoUrl = photoUrl, token = tokenValue
            )

            class dummy(val e: String) {}

            val successExpectedResponseWithUserId = RestAPIMessages(
                httpStatus = 200, message = "Get user with nicknameOrUserId : $user_id", data = usersReturnDto
            )
            val successExpectedResponseWithNickname = RestAPIMessages(
                httpStatus = 200, message = "Get user with nicknameOrUserId : $nickname", data = usersReturnDto
            )
            val failExpectedResponse = RestAPIMessages(
                httpStatus = 202,
                message = "NotAcceptedException\n",
                data = dummy("Fail To Find Nickname Or Id matched Users!")
            )

            `when`("존재하는 아이디로 findUserWithNicknameOrUserId를 실행하면") {
                then("해당 아이디 사용자의 UsersReturnDto를 반환한다") {

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/users/findUserWithNicknameOrUserId/{nicknameOrUserId}", user_id
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponseWithUserId)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_findUsersWithNicknameOrUserId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("nicknameOrUserId").description("찾고자 하는 사용자의 닉네임 또는 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.user_id").description("사용자 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.nickname").description("사용자 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.gender").description("사용자 성별, 1은 남성 0은 여성")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.photoUrl").description("사용자 프로필사진 url")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.token").description("사용자 FCM 토큰").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("존재하는 닉네임으로 findUserWithNicknameOrUserId를 실행하면") {
                then("해당 아이디 사용자의 UsersReturnDto를 반환한다") {
                    mockMvc.get("/api/v1/users/findUserWithNicknameOrUserId/{nicknameOrUserId}", nickname) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponseWithNickname)) }
                    }
                }
            }
            `when`("존재하는 않는 아이디로 findUserWithNicknameOrUserId를 실행하면") {
                then("HttpStatus.Accepted를 리턴한다") {

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/users/findUserWithNicknameOrUserId/{nicknameOrUserId}", "invalid" + user_id
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isAccepted)
                        .andExpect(content().json(objectMapper.writeValueAsString(failExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_findUsersWithNicknameOrUserId_failCase",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("nicknameOrUserId").description("찾고자 하는 사용자의 닉네임 또는 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.e").description("오류 메시지").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("존재하는 않는 닉네임으로 findUserWithNicknameOrUserId를 실행하면") {
                then("HttpStatus.Accepted를 리턴한다") {
                    mockMvc.get(
                        "/api/v1/users/findUserWithNicknameOrUserId/{nicknameOrUserId}", "invalid" + nickname
                    ) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(failExpectedResponse)) }
                    }
                }
            }
        }

        given("planner id가 1,2인 두 개의 플래너가 저장되어 있을 때")
        {
            val planners1 = Planners(
                title = "플래너 1번", start_date = "20230405", end_date = "20230407"
            )
            val planners2 = Planners(
                title = "플래너 2번", start_date = "20230405", end_date = "20230407"
            )

            plannersRepository.save(planners1)
            plannersRepository.save(planners2)

            logger.info(planners1.planner_id.toString())
            logger.info(planners2.planner_id.toString())

            val plannersResponseDto1 = PlannersResponseDto(plannersRepository.findById(1).get())
            val plannersReturnDto1 = PlannersReturnDto(plannersResponseDto1)
            val plannersResponseDto2 = PlannersResponseDto(plannersRepository.findById(2).get())
            val plannersReturnDto2 = PlannersReturnDto(plannersResponseDto2)

            val expectedResponse1 = RestAPIMessages(
                httpStatus = 200, message = "Add planner, id : 1 with user, id : $user_id", data = user_id
            )

            val expectedResponse2 = RestAPIMessages(
                httpStatus = 200, message = "Add planner, id : 2 with user, id : $user_id", data = user_id
            )

            val expectedList = ArrayList<PlannersReturnDto>()
            expectedList.add(plannersReturnDto1)
            expectedList.add(plannersReturnDto2)
            val expectedResponseForFindAll = RestAPIMessages(
                httpStatus = 200, message = "Load all planners with user, id : $user_id", data = expectedList
            )

            `when`("addPlanner로 플래너 1번을 등록하면") {
                then("플래너가 사용자에게 등록되고 planner_id=1가 리턴된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put("/api/v1/users/{planner_id}/{user_id}", 1, user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse1)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_addPlanners",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("등록하고자 하는 플래너 아이디"),
                                    parameterWithName("user_id").description("등록하고자 하는 사용자 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }

            `when`("addPlanner로 플래너 2번을 등록하면") {
                then("플래너가 사용자에게 등록되고 planner_id=2가 리턴된다") {
                    mockMvc.put("/api/v1/users/{planner_id}/{user_id}", 2, user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(expectedResponse2)) }
                    }
                }
            }

            `when`("이후에 올바른 userId로 findAllPlannersWithUserId를 실행하면") {
                then("planner 1번, 2번이 list로 리턴된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/users/{user_id}/all", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponseForFindAll)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_findAllPlannersWithUserId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("user_id").description("찾고자하는 사용자 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.[].planner_id").description("플래너 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].title").description("플래너 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].start_date").description("플래너 시작 일자")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].end_date").description("플래너 종료 일자")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].time_stamp").description("플래너 마지막 변경 시각")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].comment_timeStamp").description("플래너 댓글 마지막 변경 시각")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )

                }
            }
        }

        given("올바른 accessToken을 Http Header에 실어서 (4)")
        {
            val expectedResponse = RestAPIMessages(
                httpStatus = 200, message = "Logout ${user_id}", data = user_id
            )
            `when`("로그아웃을 시도하면") {
                then("모든 토큰을 만료시키고 로그아웃한 사용자의 user_id를 리턴한다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/users/{user_id}/logout", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Users_Logout",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("user_id").description("로그아웃 하고자 하는 사용자 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("동일한 토큰으로 다시 다른 기능 사용을 시도하면") {
                then("만료된 토큰이기 때문에 Forbidden이 동작한다") {
                    mockMvc.delete("/api/v1/users/{user_id}/logout", user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isBadRequest() }
                    }
                }
            }
        }

    }
}