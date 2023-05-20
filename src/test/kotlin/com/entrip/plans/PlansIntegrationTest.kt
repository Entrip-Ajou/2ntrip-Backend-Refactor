package com.entrip.plans

import com.entrip.auth.getContent
import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Plans.PlansUpdateRequestDto
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.repository.PlansRepository
import com.entrip.service.PlannersService
import com.entrip.service.PlansService
import com.entrip.service.UsersService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.extension.ExtendWith
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
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@AutoConfigureRestDocs
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@Sql("classpath:/schema.sql")
class PlansIntegrationTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var plansRepository: PlansRepository

    @Autowired
    lateinit var plansService: PlansService

    @Autowired
    lateinit var usersService: UsersService

    @Autowired
    lateinit var plannersService: PlannersService

    final val objectMapper = ObjectMapper().registerModule(KotlinModule())

    final val usersId = "test@2ntrip.link"
    final val userPassword = "password"

    final val userNickname = "nickname"
    final val userGender = 0
    final val userPhotoUrl = "2ntrip.link/photo/1"

    final val planDate = "2023/06/10"
    final val planTodo = "todo"
    final val planLocation = "location"
    final val planRgb = 1234L
    final val planTime = "10:30"

    final val changePlanDate = "2023/06/11"
    final val changePlanTime = "11:00"

    var plannersId: Long = 1L
    var plansId: Long = 1L

    lateinit var accessToken: String

    init {
        beforeSpec {
            plansRepository.deleteAll()
        }

        beforeEach {
            saveUsers()
            savePlannersAndGetId()
            savePlansAndGetId()
            getAccessToken()
        }

        afterEach {
            deleteAllPlans()
            deletePlanners()
            deleteUsers()
        }

        given("PlansSaveRequestDto가 주어졌을 때") {
            val plansSaveRequestDto = PlansSaveRequestDto(
                planner_id = plannersId,
                date = planDate,
                todo = planTodo,
                location = planLocation,
                rgb = planRgb,
                time = planTime
            )

            `when`("저장하면") {
                then("Plans가 저장된다") {
                    val plansResponseDto = PlansResponseDto(
                        plan_id = 2L,
                        date = planDate,
                        todo = planTodo,
                        time = planTime,
                        location = planLocation,
                        rgb = planRgb,
                        planner_id = plannersId,
                        isExistComments = false
                    )

                    val successExpectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Plan is saved well",
                        data = plansResponseDto
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/plans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(plansSaveRequestDto))
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Plans_save",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 AccessToken")
                                ),
                                requestFields(
                                    fieldWithPath("planner_id").description("플래너 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("date").description("플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("todo").description("플랜 내용").type(JsonFieldType.STRING),
                                    fieldWithPath("time").description("플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("location").description("플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("rgb").description("플랜 색상").type(JsonFieldType.NUMBER)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.plan_id").description("플랜 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.date").description("플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.todo").description("플랜 내용").type(JsonFieldType.STRING),
                                    fieldWithPath("data.time").description("플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("data.location").description("플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("data.rgb").description("플랜 색상").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.planner_id").description("플래너 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.isExistComments").description("플랜 댓글 존재 여부")
                                        .type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }
        }

        given("Plans가 한 개 저장되어 있는 상태에서 PlansUpdateRequestDto가 주어졌을 때") {
            val plansUpdateRequestDto = PlansUpdateRequestDto(
                date = changePlanDate,
                todo = planTodo,
                time = changePlanTime,
                rgb = planRgb,
                location = planLocation
            )

            `when`("수정을 요청하면") {
                then("Plans가 수정되고 plansResponseDto가 반환된다.") {
                    val plansResponseDto = PlansResponseDto(
                        plan_id = plansId,
                        date = changePlanDate,
                        todo = planTodo,
                        time = changePlanTime,
                        location = planLocation,
                        rgb = planRgb,
                        planner_id = plannersId,
                        isExistComments = false
                    )

                    val successExpectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Plan is updated well",
                        data = plansResponseDto
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put("/api/v1/plans/{plan_id}", plansId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(plansUpdateRequestDto))
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Plans_update",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 AccessToken")
                                ),
                                requestFields(
                                    fieldWithPath("date").description("수정할 플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("todo").description("수정할 플랜 내용").type(JsonFieldType.STRING),
                                    fieldWithPath("time").description("수정할 플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("location").description("수정할 플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("rgb").description("수정할 플랜 색상").type(JsonFieldType.NUMBER)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.plan_id").description("플랜 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.date").description("플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.todo").description("플랜 내용").type(JsonFieldType.STRING),
                                    fieldWithPath("data.time").description("플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("data.location").description("플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("data.rgb").description("플랜 색상").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.planner_id").description("플래너 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.isExistComments").description("플랜 댓글 존재 여부")
                                        .type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }
        }

        given("Plans이 한 개 저장되어있는 상태에서 (1) ") {
            `when`("올바른 아이디로 findById를 호출하면") {
                then("plansResponseDto가 반환된다") {
                    val plansResponseDto = plansService.findById(plansId)

                    val successExpectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Load plan with id : $plansId",
                        data = plansResponseDto
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/plans/{plan_id}", plansId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Plans_findById : success case",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 AccessToken")
                                ),
                                pathParameters(parameterWithName("plan_id").description("조회할 플랜 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.plan_id").description("플랜 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.date").description("플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.todo").description("플랜 내용").type(JsonFieldType.STRING),
                                    fieldWithPath("data.time").description("플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("data.location").description("플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("data.rgb").description("플랜 색상").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.planner_id").description("플래너 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.isExistComments").description("플랜 댓글 존재 여부")
                                        .type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }

            `when`("올바르지 않은 아이디로 findById를 호출하면") {
                val wrongPlansId = 100L

                val failExpectedResponse = RestAPIMessages(
                    httpStatus = 500,
                    message = "IllegalArgumentException\n",
                    data = "Error raise at plansRepository.findById$wrongPlansId"
                )

                then("Exception이 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/plans/{plan_id}", wrongPlansId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().is5xxServerError)
                        .andExpect(content().json(objectMapper.writeValueAsString(failExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Plans_findById : fail case",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 AccessToken")
                                ),
                                pathParameters(parameterWithName("plan_id").description("조회할 플랜 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("에러 메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("세부 에러 메시지").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
        }

        given("Plans이 한 개 저장되어 있는 상태에서 (2) ") {
            `when`("plans을 삭제하면") {
                then("plans이 삭제되고, 삭제된 플랜 아이디가 반환된다") {
                    val successExpectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Delete plan with id : $plansId",
                        data = plansId
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/plans/{plan_id}", plansId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Plans_delete",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 AccessToken")
                                ),
                                pathParameters(parameterWithName("plan_id").description("삭제할 플랜 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("삭제된 플랜 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )

                    plansRepository.findAll().size shouldBe 0
                }
            }
        }
    }

    private fun saveUsers() {
        val usersSaveRequestDto = UsersSaveRequestDto(
            usersId, userNickname, userGender, userPhotoUrl, userPassword
        )

        usersService.save(usersSaveRequestDto)
    }

    private fun getAccessToken() {
        val usersLoginRequestDto = UsersLoginRequestDto(
            usersId, userPassword
        )

        val result = mockMvc.post("/api/v2/users/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(usersLoginRequestDto)
        }.andReturn()

        accessToken = getContent(result, "accessToken")
    }

    private fun savePlannersAndGetId() {
        plannersId = plannersService.save(usersId)!!
    }

    private fun savePlansAndGetId() {
        val plansSaveRequestDto = PlansSaveRequestDto(
            planner_id = plannersId,
            date = planDate,
            todo = planTodo,
            location = planLocation,
            rgb = planRgb,
            time = planTime
        )

        plansId = plansService.save(plansSaveRequestDto)!!
    }

    private fun deleteUsers() = usersService.delete(usersId)

    private fun deletePlanners() = plannersService.delete(plannersId)

    private fun deleteAllPlans() = plansRepository.deleteAll()
}
