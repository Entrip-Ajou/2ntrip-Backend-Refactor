package com.entrip.auth

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
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
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@AutoConfigureRestDocs
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class AuthIntegrationTest() : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)
    private final val logger = LoggerFactory.getLogger(AuthIntegrationTest::class.java)

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    final val user_id = "test@gmail.com"
    final val nickname = "testNickname"
    final val gender = 1
    final val photoUrl = "testPhotoUrl.com"
    final val password = "testPassword"

    final val objectMapper = ObjectMapper().registerModule(KotlinModule())

    lateinit var accessToken: String
    lateinit var refreshToken: String

    init {
        beforeSpec {
            usersRepository.deleteAll()
        }

        beforeEach {
            saveTestUsers()
            getNewTokenValue()
        }

        afterEach {
            getNewTokenValue()
            deleteTestUsers()
            resetTokenValidTimeToNormalValue()
        }

        given("invalid Access Token 값으로") {
            val expectedResponse = RestAPIMessages(
                httpStatus = 400,
                message = "SignatureException",
                data = "Access Token is not valid!"
            )
            `when`("v1 메소드를 호출하는 경우") {
                then("SignatureException 이 throw 된다.") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/users/{user_id}", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken + "invalid")
                    )
                        .andExpect(status().isBadRequest)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Auth_with_InvalidAccessToken",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("Invalid AccessToken")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING),
                                )

                            )
                        )
                }
            }
        }

        given("AccessToken 이 만료된 상태로") {
            val expectedResponseWithExpiredAccessToken = RestAPIMessages(
                httpStatus = 400,
                message = "ExpiredAccessTokenException",
                data = "Access Token was expired! Please refresh!"
            )

            val expectedResponseWithReIssue = RestAPIMessages(
                httpStatus = 400,
                message = "Change Access Token",
                data = "Access Token was expired! Please refresh!"
            )

            `when`("v1 메소드를 호출하는 경우") {
                then("ExpiredAccessTokenException 이 throw 된다.") {
                    // Make AccessToken valid time to 1s
                    jwtTokenProvider.accessTokenValidTime = 1L

                    // Get New AccessToken with valid time is 1s
                    getNewTokenValue()

                    // Wait AccessToken being expired
                    Thread.sleep(1000L)

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/users/{user_id}", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isBadRequest)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponseWithExpiredAccessToken)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Auth_with_ExpiredAccessToken",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("Expired AccessToken")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING),
                                )

                            )
                        )
                }
            }

            `when`("reIssue 를 요청하면") {
                then("새로운 AccessToken 이 발급된다.") {
                    // Make AccessToken valid time to 1s
                    jwtTokenProvider.accessTokenValidTime = 1L

                    // Get New AccessToken with valid time is 1s
                    getNewTokenValue()

                    // Wait AccessToken being expired
                    Thread.sleep(1000L)

                    val result = mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v2/users/reIssue/{refreshToken}", refreshToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Auth_reIssue",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("refreshToken").description("refresh Token 값"),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING),
                                )

                            )
                        )
                        .andReturn()

                    val response = result.response.contentAsString
                    val restAPIMessages = objectMapper.readValue<RestAPIMessages>(response, RestAPIMessages::class.java)
                    accessToken = restAPIMessages.data.toString()

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/users/{user_id}", user_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)

                }
            }
        }

        given("AccessToken 이 만료되지 않은 상태에서") {
            val expectedResponseWithReIssueBeforeAccessTokenExpired = RestAPIMessages(
                httpStatus = 400,
                message = "ReIssueBeforeAccessTokenExpiredException",
                data = "ReIssue before Access Token Expired !!!"
            )
            `when`("reIssue 를 요청하면") {
                then("ReIssueBeforeAccessTokenExpiredException 이 throw 된다") {

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v2/users/reIssue/{refreshToken}", refreshToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isBadRequest)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponseWithReIssueBeforeAccessTokenExpired)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Auth_reIssue_beforeAccessTokenExpired",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("refreshToken").description("refresh Token 값"),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING),
                                )

                            )
                        )

                }
            }
        }

        given("RefreshToken 이 invalid 한 상태에서") {
            `when`("reIssue 를 요청하는 경우") {
                 then("Exception 이 throw 된다") {
                     mockMvc.perform(
                         RestDocumentationRequestBuilders.get("/api/v2/users/reIssue/{refreshToken}", refreshToken+"invalid")
                             .contentType(MediaType.APPLICATION_JSON)
                     )
                         .andExpect(status().isInternalServerError)
                         .andDo(
                             MockMvcRestDocumentation.document(
                                 "Auth_reIssue_withInvalidRefreshToken",
                                 Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                 Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                 pathParameters(
                                     parameterWithName("refreshToken").description("refresh Token 값"),
                                 ),
                                 responseFields(
                                     fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                     fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                     fieldWithPath("data").description("데이터").type(JsonFieldType.STRING),
                                 )

                             )
                         )
                }
            }
        }

        given("RefreshToken 이 expired 한 상태에서") {
            val expectedResponseWithExpiredRefreshToken = RestAPIMessages(
                httpStatus = 500,
                message = "Exception",
                data = "Request processing failed; nested exception is io.jsonwebtoken.SignatureException: Refresh token Signature is not valid"
            )

            `when`("reIssue 를 요청하는 경우") {
                then("Exception 이 throw 된다") {

                    // Make Token valid time to 1s
                    jwtTokenProvider.accessTokenValidTime = 1L
                    jwtTokenProvider.refreshTokenValidTime = 1L

                    // Get New AccessToken with valid time is 1s
                    getNewTokenValue()

                    // Wait AccessToken being expired
                    Thread.sleep(1000L)

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v2/users/reIssue/{refreshToken}", refreshToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isServiceUnavailable)
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Auth_reIssue_withExpiredRefreshToken",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                pathParameters(
                                    parameterWithName("refreshToken").description("refresh Token 값"),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.STRING),
                                )

                            )
                        )
                }
            }
        }

    }

    private fun getNewTokenValue() {
        val usersLoginRequestDto = UsersLoginRequestDto(
            user_id, password
        )
        val result = mockMvc.post("/api/v2/users/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(usersLoginRequestDto)
        }.andReturn()

        accessToken = getContent(result, "accessToken")
        refreshToken = getContent(result, "refreshToken")
    }

    private fun resetTokenValidTimeToNormalValue() {
        jwtTokenProvider.accessTokenValidTime = 10 * 60L
        jwtTokenProvider.refreshTokenValidTime = 3600 * 60L
    }

    private fun saveTestUsers() {
        val usersSaveRequestDto = UsersSaveRequestDto(
            user_id, nickname, gender, photoUrl, password
        )

        mockMvc.post("/api/v2/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(usersSaveRequestDto)
        }
    }

    private fun deleteTestUsers() {
        mockMvc.delete("/api/v1/users/{user_id}", user_id) {
            contentType = MediaType.APPLICATION_JSON
            header("AccessToken", accessToken)
        }
    }
}

fun BehaviorSpec.getContent(result: MvcResult, target: String): String {
    val objectMapper = ObjectMapper().registerModule(KotlinModule())

    // 1. MockMvcResult의 response를 String (JSON)으로 바꾼다
    val response = result.response.contentAsString

    // 2. String (JSON)을 RestAPIMessages로 변환한다
    val restAPIMessages = objectMapper.readValue<RestAPIMessages>(response, RestAPIMessages::class.java)

    // 3. RestAPIMessages의 data 필드를 String으로 다시 변환한다
    val str = restAPIMessages.data.toString()

    // 4. String으로 바꾼 data 필드를 전처리해서 target 값을 찾아낸다
    val pairs = str.substring(1, str.length - 1).split(", ")
    for (pair in pairs) {
        val keyValue = pair.split("=")
        if (keyValue[0] == target) {
            return keyValue[1]
        }
    }

    return ""
}
