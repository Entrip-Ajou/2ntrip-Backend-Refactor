package com.entrip.planners

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Notices.NoticesReturnDto
import com.entrip.domain.dto.Notices.NoticesSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsReturnDto
import com.entrip.repository.*
import com.entrip.service.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
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
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@AutoConfigureRestDocs
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class PlannersIntegrationTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)
    val logger = LoggerFactory.getLogger(PlannersIntegrationTest::class.java)

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var plannersRepository: PlannersRepository

    @Autowired
    lateinit var plannersService: PlannersService

    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var usersService: UsersService

    @Autowired
    lateinit var plansRepository: PlansRepository

    @Autowired
    lateinit var plansService: PlansService

    @Autowired
    lateinit var noticesRepository: NoticesRepository

    @Autowired
    lateinit var noticesService: NoticesService

    @Autowired
    lateinit var votesService: VotesService

    private final val userOneId = "testOne@2ntrip.com"
    private final val userTwoId = "testTwo@2ntrip.com"
    private final val userOneNickname = "nicknameOne"
    private final val userTwoNickname = "nicknameTwo"

    final val objectMapper = ObjectMapper().registerModule(KotlinModule())

    lateinit var accessToken: String
    lateinit var refreshToken: String

    var plannerOneId: Long = 0L
    var plannerTwoId: Long = 0L

    init {
        beforeSpec {
            plannersRepository.deleteAll()
        }

        afterSpec {
            plannersService.exitPlanner(plannerOneId, userOneId) // user가 없다면 delete
            usersService.delete(userOneId)
            usersService.delete(userTwoId)
        }

        given("Users") {
            `when`("save하면") {
                then("accessToken 반환된다") {
                    // planner v1 메서드를 사용하기 위해서 user를 저장한 후, accessToken을 받아온다.

                    val usersSaveRequestDto = UsersSaveRequestDto(
                        user_id = userOneId,
                        nickname = userOneNickname,
                        gender = 0,
                        password = "password",
                        photoUrl = "2ntrip.com"
                    )

                    val usersLoginRequestDto = UsersLoginRequestDto(
                        user_id = userOneId,
                        password = "password"
                    )

                    mockMvc.post("/api/v2/users") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(usersSaveRequestDto)
                    }

                    val result = mockMvc.perform(
                        post("/api/v2/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usersLoginRequestDto))
                    ).andReturn()

                    accessToken = getContent(result, "accessToken")
                    refreshToken = getContent(result, "refreshToken")
                }
            }
        }


        given("user_id가 주어졌을 때") {
            `when`("플래너 생성을 요청하면") {
                then("플래너가 생성되고 plannersReturnDto가 반환된다") {
                    val result = mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/planners/{user_id}", userOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk())
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_save",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("user_id").description("사용자 이메일")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.planner_id").description("플래너 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.title").description("플래너 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.start_date").description("플래너 시작 날짜")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.end_date").description("플래너 종료 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.time_stamp").description("플래너 생성 시간")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.comment_timeStamp").description("플래너 댓글 생성 시간")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        ).andReturn()

                    plannerOneId = getContent(result, "planner_id").toLong()

                    // plannersRepository, plannerService를 통해서 제대로 저장이 되었는지 확인할 필요가 있을까?
                    plannersRepository.findById(plannerOneId).get().planner_id shouldBe 1
                }
            }
        }

        given("플래너가 저장된 상태에서") {
            val wrongPlannerId = 100L

            `when`("PlannersUpdateRequestDto로 플래너 수정을 요청하면") {
                val plannersUpdateRequestDto = PlannersUpdateRequestDto(
                    title = "부산 여행",
                    start_date = "2023/05/05",
                    end_date = "2023/05/10"
                )

                then("플래너가 수정되고 plannersReturnDto가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put("/api/v1/planners/{planner_id}", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(plannersUpdateRequestDto))
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_update",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("수정할 플래너 아이디")
                                ),
                                requestFields(
                                    fieldWithPath("title").description("수정할 플래너 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("start_date").description("수정할 플래너 시작 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("end_date").description("수정할 플래너 종료 날짜").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.planner_id").description("플래너 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.title").description("플래너 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.start_date").description("플래너 시작 날짜")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.end_date").description("플래너 종료 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.time_stamp").description("플래너 생성 시간")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.comment_timeStamp").description("플래너 댓글 생성 시간")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("findById를 요청하면") {
                val plannersResponseDto = PlannersResponseDto(
                    plannersRepository.findById(plannerOneId).get()
                )

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load planner with $plannerOneId",
                    data = plannersResponseDto
                )

                then("plannersReturnDto가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_findByid",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.planner_id").description("플래너 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.title").description("플래너 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.start_date").description("플래너 시작 날짜")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.end_date").description("플래너 종료 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.time_stamp").description("플래너 생성 시간")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.comment_timeStamp").description("플래너 댓글 생성 시간")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("올바른 id로 plannerIsExistWithId를 요청하면") {
                then("true가 반환된다") {
                    val successExpectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Find if planner is exist with specific planner id : $plannerOneId",
                        data = true
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}/exist", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_isExistWithValidId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 여부").type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }
            `when`("올바르지 않은 id로 plannerIsExistWithId를 요청하면") {
                then("NotAcceptedException가 반환된다") {
                    val failExpectedResponse = RestAPIMessages(
                        httpStatus = 202,
                        message = "NotAcceptedException\n",
                        data = false
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}/exist", wrongPlannerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isAccepted())
                        .andExpect(content().json(objectMapper.writeValueAsString(failExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_isExistWithInvalidId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
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

        given("플래너와 플랜이 저장된 상태에서") {
            // Plans -> 리포지토리 통해서 바로 저장하는게 나은가, mockMvc post로 저장하는게 나은가
            val plansSaveRequestDto = PlansSaveRequestDto(
                planner_id = plannerOneId,
                date = "2023/05/05",
                todo = "국밥 먹기",
                time = "10:30",
                location = "부산광역시 서면",
                rgb = 1234
            )


            val planId = plansService.save(plansSaveRequestDto)

            val plansResponseDto = PlansResponseDto(plansRepository.findById(planId!!).get())

            val plansList = mutableListOf(plansResponseDto)

            `when`("플래너에 있는 모든 플랜 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load all plans with specific planner id : $plannerOneId",
                    data = plansList
                )

                then("planList 반환된다") {
                    val result = mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}/all", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_getAllPlans",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.[].plan_id").description("플랜 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].date").description("플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].todo").description("플랜 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].time").description("플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].location").description("플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].rgb").description("플랜 색상").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].planner_id").description("플래너 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].isExistComments").description("플랜 댓글 존재 여부")
                                        .type(JsonFieldType.BOOLEAN)
                                )
                            )
                        ).andReturn()
                }
            }
            `when`("Date로 플랜 조회를 요청하면") {
                val date = "20230505"

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Get all plans with specific date $date, planner_id $plannerOneId",
                    data = plansList
                )

                then("해당 날짜와 같은 플랜 리스트를 반환한다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/planners/{planner_id}/{date}/find",
                            plannerOneId,
                            date
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_getAllPlansWithDate",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("조회할 플래너 아이디"),
                                    parameterWithName("date").description("찾을 플랜 날짜")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.[].plan_id").description("플랜 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].date").description("플랜 날짜").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].todo").description("플랜 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].time").description("플랜 시간").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].location").description("플랜 장소").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].rgb").description("플랜 색상").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].planner_id").description("플래너 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].isExistComments").description("플랜 댓글 존재 여부")
                                        .type(JsonFieldType.BOOLEAN)
                                )
                            )
                        )
                }
            }
        }

        given("플래너와 공지가 저장된 상태에서") {
            val noticesSaveRequestDto = NoticesSaveRequestDto(
                author = userOneId,
                title = "공지 제목",
                content = "공지 내용",
                planner_id = plannerOneId
            )

            val noticeId = noticesService.save(noticesSaveRequestDto)

            val noticesReturnDto = NoticesReturnDto(
                noticesRepository.findById(noticeId!!).get()
            )

            val noticesList = mutableListOf(noticesReturnDto)

            `when`("플래너에 있는 모든 공지 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Get all notices with planner id : $plannerOneId",
                    data = noticesList
                )

                then("noticeList가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}/allNotices", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_getAllNotices",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.[].notice_id").description("공지 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].author").description("공지 작성자").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].nickname").description("공지 작성자 닉네임")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].title").description("공지 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].content").description("공지 내용").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].modifiedDate").description("공지 수정 날짜")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
        }

        given("플래너와 투표가 저장된 상태에서") {
            val votesSaveRequestDto = VotesSaveRequestDto(
                title = "투표 제목",
                contents = mutableListOf("1", "2", "3"),
                multipleVotes = true,
                anonymousVotes = false,
                deadLine = null,
                planner_id = plannerOneId,
                author = userOneId
            )

            val voteId = votesService.save(votesSaveRequestDto)

            val votesReturnDto = VotesReturnDto(
                vote_id = voteId,
                title = "투표 제목",
                voting = true,
                host_id = userOneId,
                contents = mutableListOf(
                    VotesContentsReturnDto(votesContents_id = 1, content = "1", selectedCount = 0),
                    VotesContentsReturnDto(votesContents_id = 2, content = "2", selectedCount = 0),
                    VotesContentsReturnDto(votesContents_id = 3, content = "3", selectedCount = 0)
                )
            )

            val votesList = mutableListOf(votesReturnDto)

            `when`("플래너에 있는 모든 투표 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load votes with planner id : $plannerOneId",
                    data = votesList
                )

                then("voteList가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}/allVotes", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_getAllVotes",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.[].vote_id").description("투표 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].title").description("투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].voting").description("투표중 여부").type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.[].host_id").description("투표 작성자 이메일")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].contents.[].votesContents_id").description("투표 항목 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].contents.[].content").description("투표 항목 내용")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].contents.[].selectedCount").description("투표 항목 투표받은 수")
                                        .type(JsonFieldType.NUMBER),
                                )
                            )
                        )
                }
            }
        }

        given("플래너가 하나 저장되어 있고, 유저가 두 명 저장된 상태에서 (1)") {
            // save new user
            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = userTwoId,
                nickname = userTwoNickname,
                gender = 0,
                password = "password",
                photoUrl = "2ntrip.com"
            )

            usersService.save(usersSaveRequestDto)

            usersService.updateToken(userOneId, "token")
            usersService.updateToken(userTwoId, "token")

            val usersReturnDtoOne = UsersResponseDto(usersRepository.findById(userOneId).get())
            val usersReturnDtoTwo = UsersResponseDto(usersRepository.findById(userTwoId).get())

            val usersList = mutableListOf(usersReturnDtoOne, usersReturnDtoTwo)

            `when`("새로운 유저를 플래너에 추가하도록 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "$plannerOneId 번 플래너에 $userTwoId 사용자 등록 완료.",
                    data = plannerOneId
                )
                then("플래너에 유저가 등록되고, 플래너 아이디를 반환한다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put(
                            "/api/v1/planners/{planner_id}/{user_id}",
                            plannerOneId,
                            userTwoId
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_addUsersToPlanner",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("조회할 플래너 아이디"),
                                    parameterWithName("user_id").description("추가할 유저 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("플래너 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                }
            }
            `when`("이미 추가된 유저를 플래너에 추가하도록 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "이미 planner에 등록되어있는 회원입니다.",
                    data = plannerOneId
                )

                then("이미 추가되었다는 메시지를 반환받는다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put(
                            "/api/v1/planners/{planner_id}/{user_id}",
                            plannerOneId,
                            userTwoId
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_addExistUsersToPlanner",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("조회할 플래너 아이디"),
                                    parameterWithName("user_id").description("추가할 유저 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("플래너 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                }
            }
            `when`("플래너에 있는 모든 유저 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load all users with planner id : $plannerOneId",
                    data = usersList
                )

                then("유저 리스트를 반환받는다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/planners/{planner_id}/getAllUser", plannerOneId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_getAllUsers",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("조회할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.[].user_id").description("유저 이메일").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].nickname").description("유저 닉네임").type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].gender").description("유저 성별").type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.[].photoUrl").description("유저 프로필 사진 url")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.[].token").description("유저 토큰").type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }
            `when`("플래너에서 유저를 exit하도록 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "User ${userTwoId} exit planner ${plannerOneId}",
                    data = true
                )
                then("true값을 반환받는다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.delete(
                            "/api/v1/planners/{planner_id}/{user_id}/exit",
                            plannerOneId,
                            userTwoId
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_exitUser",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("조회할 플래너 아이디"),
                                    parameterWithName("user_id").description("exit할 유저 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.BOOLEAN),
                                )
                            )
                        )
                }
            }
        }

        given("플래너가 하나 저장되어 있고, 유저가 두 명 저장된 상태에서 (2)") {
            `when`("플래너에 있는 유저가 존재하는지 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Check if user : $userOneId is exist at planner : $plannerOneId",
                    data = true
                )

                then("true값을 반환받는다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/planners/{planner_id}/{user_id}/exist",
                            plannerOneId,
                            userOneId
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_isExistInPlannerWithValidUserId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("플래너 아이디"),
                                    parameterWithName("user_id").description("존재하는 지 조회할 유저 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 유무 값").type(JsonFieldType.BOOLEAN),
                                )
                            )
                        )
                }
            }
            `when`("플래너에 없는 유저가 존재하는지 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 202,
                    message = "NotAcceptedException\n",
                    data = false
                )

                then("NotAcceptedException가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/planners/{planner_id}/{user_id}/exist",
                            plannerOneId,
                            userTwoId
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isAccepted)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_isExistInPlannerWithInvalidUserId",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("플래너 아이디"),
                                    parameterWithName("user_id").description("존재하는 지 조회할 유저 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 유무 값").type(JsonFieldType.BOOLEAN),
                                )
                            )
                        )
                }
            }
            `when`("플래너에 있는 유저의 닉네임으로 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Check if user : $userOneNickname is exist at planner : $plannerOneId",
                    data = true
                )

                then("true값을 반환받는다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/planners/{planner_id}/{nickname}/exist/nickname",
                            plannerOneId,
                            userOneNickname
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_isExistInPlannerWithValidNickname",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("플래너 아이디"),
                                    parameterWithName("nickname").description("존재하는 지 조회할 유저 닉네임")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 유무 값").type(JsonFieldType.BOOLEAN),
                                )
                            )
                        )
                }
            }
            `when`("플래너에 없는 유저의 닉네임으로 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 202,
                    message = "NotAcceptedException\n",
                    data = false
                )

                then("NotAcceptedException값을 반환받는다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get(
                            "/api/v1/planners/{planner_id}/{nickname}/exist/nickname",
                            plannerOneId,
                            userTwoNickname
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isAccepted)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_isExistInPlannerWithInvalidNickname",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("플래너 아이디"),
                                    parameterWithName("nickname").description("존재하는 지 조회할 유저 닉네임")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("존재 유무 값").type(JsonFieldType.BOOLEAN),
                                )
                            )
                        )
                }
            }
        }

        given("플래너가 두 개 저장된 상태에서") {

            val plannerIdToDelete = plannersService.save(userOneId)

            `when`("플래너 삭제를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Delete planner with id : $plannerIdToDelete",
                    data = plannerIdToDelete!!
                )

                then("플래너가 삭제되고, 삭제된 플래너 아이디가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/planners/{planner_id}", plannerIdToDelete)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_delete",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(parameterWithName("planner_id").description("삭제할 플래너 아이디")),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("삭제된 플래너 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                }
            }
        }

        given("플래너가 두 개와 유저가 두 명 저장된 상태에서") {

            val plannerIdToDelete = plannersService.save(userOneId)

            `when`("플래너 삭제 및 exit를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Delete planner $plannerIdToDelete",
                    data = plannerIdToDelete!!
                )

                then("플래너가 삭제되고, 삭제된 플래너 아이디가 반환된다") {
                    mockMvc.perform(
                        RestDocumentationRequestBuilders.delete(
                            "/api/v1/planners/{planner_id}/{user_id}/delete",
                            plannerIdToDelete,
                            userTwoId
                        )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    ).andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(successExpectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Planners_deleteWithExit",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("planner_id").description("삭제할 플래너 아이디"),
                                    parameterWithName("user_id").description("확인할 유저 이메일")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("삭제된 플래너 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                }
            }
        }
    }

    fun getContent(result: MvcResult, target: String): String {
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
}