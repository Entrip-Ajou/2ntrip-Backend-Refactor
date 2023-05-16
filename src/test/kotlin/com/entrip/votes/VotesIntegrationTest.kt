package com.entrip.votes

import com.entrip.auth.getContent
import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.domain.dto.Votes.*
import com.entrip.domain.dto.VotesContents.PreviousVotesContentsRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsCountRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsReturnDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Votes
import com.entrip.repository.PlannersRepository
import com.entrip.repository.VotesRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.json.JSONObject
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
import org.springframework.restdocs.headers.HeaderDocumentation.*
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
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
@AutoConfigureRestDocs
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@Sql("classpath:/schema.sql")
class VotesIntegrationTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)
    private final val logger = LoggerFactory.getLogger(VotesIntegrationTest::class.java)

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var plannersRepository: PlannersRepository

    @Autowired
    lateinit var votesRepository: VotesRepository

    // final value for Test Users
    final val user_id = "test@gmail.com"
    final val nickname = "testNickname"
    final val gender = 1
    final val photoUrl = "testPhotoUrl.com"
    final val password = "testPassword"

    // test Planners
    // planner_id and testPlanners can be changeable with each testCase (Because of PK's Generation Type)
    // so private method "saveTestPlanners" can change testPlanners and planner_id with auto_increment PK
    var planner_id = -1L
    lateinit var testPlanners: Planners

    // final value for Test Votes
    // vote_id and votePlanners can be changeable with each testCase (Because of PK's Generation Type)
    // private method "saveTestVotes" can change testVotes and vote_id with auto_increment PK
    var vote_id = -1L
    lateinit var testVotes: Votes

    private final var votesTitle = "testVotes1"
    private final var multipleVote = false
    private final var anonymousVote = false
    final var voting = true
    private final var deadLine = "2023-05-12 14:27"
    private final val content1 = "content1"
    private final val content2 = "content2"
    private final val contents = mutableListOf(content1, content2)

    // voteContentsId can be changeable with each testCase (Because of PK's Generation Type)
    // private method "saveTestVotes" can change votesContentsIdList which is the id of votesContents in testVotes
    var votesContentsIdList: MutableList<Long> = ArrayList()

    // final ObjectMapper
    private final val objectMapper = ObjectMapper().registerModule(KotlinModule())

    // Token value
    lateinit var accessToken: String
    lateinit var refreshToken: String

    init {
        // Save 1 Users, 1 Planners, 1 Votes with 2 VotesContents before each TestCase
        beforeEach {
            saveTestUsers()
            getNewTokenValue()
            saveTestPlanners()
            saveVotes()
        }
        // Delete All the Users, Planners, Votes, VotesContents after each TestCase
        afterEach {
            deleteTestPlanners()
            deleteTestUsers()
        }

        given("VotesSaveRequestDto가 주어지고") {
            val votesContentsReturnDto1 = VotesContentsReturnDto(
                votesContents_id = 3L,
                content = content1,
                selectedCount = 0
            )

            val votesContentsReturnDto2 = VotesContentsReturnDto(
                votesContents_id = 4L,
                content = content2,
                selectedCount = 0
            )

            val votesContentsReturnDtoList = mutableListOf(votesContentsReturnDto1, votesContentsReturnDto2)
            val votesReturnDto = VotesReturnDto(
                vote_id = 2L,
                title = votesTitle,
                voting = true,
                host_id = user_id,
                contents = votesContentsReturnDtoList
            )
            val expectedResponse = RestAPIMessages(
                httpStatus = 200,
                message = "Votes is saved well",
                data = votesReturnDto
            )


            `when`("votes 저장을 요청하면") {
                then("votes가 저장되고 votesReturnDto가 리턴된다") {

                    val votesSaveRequestDto = VotesSaveRequestDto(
                        title = votesTitle,
                        contents = contents,
                        multipleVotes = multipleVote,
                        anonymousVotes = anonymousVote,
                        deadLine = deadLine,
                        planner_id = planner_id,
                        author = user_id
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/votes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                            .content(objectMapper.writeValueAsString(votesSaveRequestDto))
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_save",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                requestFields(
                                    fieldWithPath("title").description("저장할 투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("contents").description("저장할 투표의 세부 항목 리스트, MutableList<String>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("multipleVotes").description("다중 투표 가능 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("anonymousVotes").description("익명 투표 가능 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("deadLine").description("마감 기한, yyyy-MM-dd HH:mm 형식 문자열")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("planner_id").description("투표 상위 플래너 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("author").description("투표 게시자").type(JsonFieldType.STRING),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.vote_id").description("저장된 투표 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.title").description("저장된 투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.voting").description("투표 마감 여부").type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.host_id").description("투표 게시자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contents").description("투표 세부 항목, MutableList<VotesContentsReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contents.[].votesContents_id").description("투표 세부 항목 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.contents.[].content").description("투표 세부 항목 내용")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contents.[].selectedCount").description("항목 투표 개수")
                                        .type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                }
            }
        }

        given("Votes가 하나 저장되어 있을 떄") {
            val votesUsersReturnDto = VotesUserReturnDto(
                user_id = user_id,
                nickname = nickname,
                photo_url = photoUrl
            )

            `when`("doVote를 요청하면") {
                then("투표한 결과와 함께 VotesFullInfoReturnDto가 리턴된다") {

                    val contentsAndUsers1 = UsersAndContentsReturnDto(
                        content_id = votesContentsIdList[0],
                        content = content1,
                        users = mutableListOf(votesUsersReturnDto)
                    )

                    val contentsAndUsers2 = UsersAndContentsReturnDto(
                        content_id = votesContentsIdList[1],
                        content = content2,
                        users = mutableListOf()
                    )

                    val votesFullInfoReturnDto = VotesFullInfoReturnDto(
                        title = votesTitle,
                        contentsAndUsers = mutableListOf(contentsAndUsers1, contentsAndUsers2),
                        multipleVotes = multipleVote,
                        anonymousVote = anonymousVote,
                        host_id = user_id,
                        voting = voting
                    )

                    val votesContentsCountRequestDto = VotesContentsCountRequestDto(
                        vote_id = vote_id,
                        voteContents_id = mutableListOf(votesContentsIdList[0]),
                        user_id = user_id
                    )

                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "successfully voted at ${votesContentsIdList[0]}",
                        data = votesFullInfoReturnDto
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/votes/doVote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                            .content(objectMapper.writeValueAsString(votesContentsCountRequestDto))
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_doVote",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                requestFields(
                                    fieldWithPath("vote_id").description("투표할 투표 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("voteContents_id").description("투표할 세부 항목의 아이디 리스트")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("user_id").description("투표 게시자").type(JsonFieldType.STRING),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.title").description("저장된 투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.multipleVotes").description("중복 투표 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.anonymousVote").description("익명 투표 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.voting").description("투표 마감 여부").type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.host_id").description("투표 게시자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers").description("항목 별 투표 현황 리스트, MutableList<UsersAndContentsReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contentsAndUsers.[].content_id").description("투표 세부 항목 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.contentsAndUsers.[].content").description("투표 세부 항목 내용")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users").description("세부 항목 투표 사용자 리스트, MutableList<VotesUserReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contentsAndUsers.[].users.[].user_id").description("세부 항목 투표 사용자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users.[].nickname").description("세부 항목 투표 사용자 닉네임")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users.[].photo_url").description("세부 항목 투표 사용자 사진 URL")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }

            `when`("findById를 요청하면") {
                then("찾고자 하는 votes의 VotesFullInfoReturnDto가 리턴된다") {

                    val contentsAndUsers1 = UsersAndContentsReturnDto(
                        content_id = votesContentsIdList[0],
                        content = content1,
                        users = mutableListOf(votesUsersReturnDto)
                    )

                    val contentsAndUsers2 = UsersAndContentsReturnDto(
                        content_id = votesContentsIdList[1],
                        content = content2,
                        users = mutableListOf()
                    )

                    val votesFullInfoReturnDto = VotesFullInfoReturnDto(
                        title = votesTitle,
                        contentsAndUsers = mutableListOf(contentsAndUsers1, contentsAndUsers2),
                        multipleVotes = multipleVote,
                        anonymousVote = anonymousVote,
                        host_id = user_id,
                        voting = voting
                    )

                    val votesContentsCountRequestDto = VotesContentsCountRequestDto(
                        vote_id = vote_id,
                        voteContents_id = mutableListOf(votesContentsIdList[0]),
                        user_id = user_id
                    )

                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Load votes with id : $vote_id",
                        data = votesFullInfoReturnDto
                    )

                    mockMvc.post("/api/v1/votes/doVote") {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                        content = objectMapper.writeValueAsString(votesContentsCountRequestDto)
                    }
                        .andExpect { status { isOk() } }

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/v1/votes/{vote_id}", vote_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_findById",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("vote_id").description("찾고자 하는 투표 아이디")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.title").description("저장된 투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.multipleVotes").description("중복 투표 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.anonymousVote").description("익명 투표 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.voting").description("투표 마감 여부").type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.host_id").description("투표 게시자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers").description("항목 별 투표 현황 리스트, MutableList<UsersAndContentsReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contentsAndUsers.[].content_id").description("투표 세부 항목 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.contentsAndUsers.[].content").description("투표 세부 항목 내용")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users").description("세부 항목 투표 사용자 리스트, MutableList<VotesUserReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contentsAndUsers.[].users.[].user_id").description("세부 항목 투표 사용자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users.[].nickname").description("세부 항목 투표 사용자 닉네임")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users.[].photo_url").description("세부 항목 투표 사용자 사진 URL")
                                        .type(JsonFieldType.STRING)
                                )
                            )
                        )
                }
            }

            `when`("투표한 다음 undoVote를 요청하면") {
                then("투표 취소한 결과와 함께 VotesFullInfoReturnDto가 리턴된다") {

                    val contentsAndUsers1 = UsersAndContentsReturnDto(
                        content_id = votesContentsIdList[0],
                        content = content1,
                        users = mutableListOf()
                    )

                    val contentsAndUsers2 = UsersAndContentsReturnDto(
                        content_id = votesContentsIdList[1],
                        content = content2,
                        users = mutableListOf()
                    )

                    val votesFullInfoReturnDto = VotesFullInfoReturnDto(
                        title = votesTitle,
                        contentsAndUsers = mutableListOf(contentsAndUsers1, contentsAndUsers2),
                        multipleVotes = multipleVote,
                        anonymousVote = anonymousVote,
                        host_id = user_id,
                        voting = voting
                    )

                    val votesContentsCountRequestDtoForVote = VotesContentsCountRequestDto(
                        vote_id = vote_id,
                        voteContents_id = mutableListOf(votesContentsIdList[0], votesContentsIdList[1]),
                        user_id = user_id
                    )

                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "successfully undoVoted at $vote_id",
                        data = votesFullInfoReturnDto
                    )

                    // doVote First
                    mockMvc.post("/api/v1/votes/doVote") {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                        content = objectMapper.writeValueAsString(votesContentsCountRequestDtoForVote)
                    }
                        .andExpect { status { isOk() } }

                    val votesContentsCountRequestDtoForUndoVote = VotesContentsCountRequestDto(
                        vote_id = vote_id,
                        voteContents_id = mutableListOf(votesContentsIdList[1]),
                        user_id = user_id
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/votes/undoVote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                            .content(objectMapper.writeValueAsString(votesContentsCountRequestDtoForUndoVote))
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_undoVote",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                requestFields(
                                    fieldWithPath("vote_id").description("투표할 투표 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("voteContents_id").description("투표할 세부 항목의 아이디 리스트")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("user_id").description("투표 게시자").type(JsonFieldType.STRING),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.title").description("저장된 투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.multipleVotes").description("중복 투표 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.anonymousVote").description("익명 투표 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.voting").description("투표 마감 여부").type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.host_id").description("투표 게시자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers").description("항목 별 투표 현황 리스트, MutableList<UsersAndContentsReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contentsAndUsers.[].content_id").description("투표 세부 항목 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.contentsAndUsers.[].content").description("투표 세부 항목 내용")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contentsAndUsers.[].users").description("세부 항목 투표 사용자 리스트, MutableList<VotesUserReturnDto>")
                                        .type(JsonFieldType.ARRAY)
                                )
                            )
                        )
                }
            }

            `when`("votes getPreviousVotes를 요청하면") {
                then("PreviousVotesContentsRequestDto가 리턴된다") {

                    val votesContentsCountRequestDtoForVote = VotesContentsCountRequestDto(
                        vote_id = vote_id,
                        voteContents_id = mutableListOf(votesContentsIdList[0], votesContentsIdList[1]),
                        user_id = user_id
                    )

                    // doVote First
                    mockMvc.post("/api/v1/votes/doVote") {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                        content = objectMapper.writeValueAsString(votesContentsCountRequestDtoForVote)
                    }
                        .andExpect { status { isOk() } }

                    val previousVotesContentsRequestDto = PreviousVotesContentsRequestDto(
                        user_id = user_id,
                        vote_id = vote_id
                    )

                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Load previous votesContents with $user_id",
                        data = listOf(1, 2)
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/votes/getPreviousVotes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                            .content(objectMapper.writeValueAsString(previousVotesContentsRequestDto))
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_getPreviousVotes",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                requestFields(
                                    fieldWithPath("user_id").description("투표한 사용자의 아이디").type(JsonFieldType.STRING),
                                    fieldWithPath("vote_id").description("조회할 투표의 아이디").type(JsonFieldType.NUMBER),
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("사용자가 투표한 항목 (VotesContents)의 아이디 리스트, MutableList<Long>")
                                        .type(JsonFieldType.ARRAY)
                                )
                            )
                        )

                }
            }

            `when`("votes terminateVote를 요청하면") {
                then("votes의 voting이 false가 되고 vote_id가 리턴된다") {
                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Terminate vote with id : $vote_id",
                        data = vote_id
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/v1/votes/{vote_id}", vote_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_terminateVote",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("vote_id").description("종료하고자 하는 투표 아이디")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("종료된 투표 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                    val votes = votesRepository.findById(vote_id).orElseThrow { IllegalArgumentException("") }
                    votes.voting shouldBe false
                }
            }

            `when`("votes delete를 요청하면") {
                then("해당 Votes가 삭제된다") {
                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Delete votes with id : $vote_id",
                        data = vote_id
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/votes/{vote_id}", vote_id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_delete",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                pathParameters(
                                    parameterWithName("vote_id").description("삭제하고자 하는 투표 아이디")
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("삭제된 투표 아이디").type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                    votesRepository.findById(vote_id) shouldBe Optional.empty()
                }

            }
        }


        given("VotesUpdateRequestDto가 주어지고") {
            val updatedTitle = "updatedTitle"
            val updatedDeadline = "1111-01-01 00:00"
            val votesUpdateRequestDto = VotesUpdateRequestDto(
                vote_id = vote_id,
                title = updatedTitle,
                multipleVote = true,
                anonymousVote = true,
                deadLine = updatedDeadline
            )

            val votesContentsReturnDto1 = VotesContentsReturnDto(
                votesContents_id = -1L,
                content = content1,
                selectedCount = 0
            )

            val votesContentsReturnDto2 = VotesContentsReturnDto(
                votesContents_id = -1L,
                content = content2,
                selectedCount = 0
            )

            val votesContentsReturnDtoList = mutableListOf(votesContentsReturnDto1, votesContentsReturnDto2)
            val votesReturnDto = VotesReturnDto(
                vote_id = vote_id,
                title = updatedTitle,
                voting = true,
                host_id = user_id,
                contents = votesContentsReturnDtoList
            )

            `when`("votes update를 요청하면") {
                then("update된 VotesReturnDto가 리턴된다") {
                    // Votes 가 각 테스트 케이스마다 새롭게 저장되기 때문에
                    // votesReturnDto 안에 들어있는 votesContentsReturnDtoList의 votesContents_id를
                    // 새롭게 생성된 값으로 바꿔준다
                    for (i in 0 until votesReturnDto.contents.size) {
                        val eachContent = votesReturnDto.contents[i]
                        val eachId = votesContentsIdList[i]
                        eachContent.votesContents_id = eachId
                    }

                    val expectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Votes is updated well",
                        data = votesReturnDto
                    )

                    mockMvc.perform(
                        RestDocumentationRequestBuilders.put("/api/v1/votes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("AccessToken", accessToken)
                            .content(objectMapper.writeValueAsString(votesUpdateRequestDto))
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(
                            MockMvcRestDocumentation.document(
                                "Votes_update",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                requestHeaders(
                                    headerWithName("AccessToken").description("사용자 Access Token")
                                ),
                                requestFields(
                                    fieldWithPath("vote_id").description("업데이트할 투표 아이디").type(JsonFieldType.NUMBER),
                                    fieldWithPath("title").description("업데이트할 투표 제목").type(JsonFieldType.STRING),

                                    fieldWithPath("multipleVote").description("다중 투표 가능 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("anonymousVote").description("익명 투표 가능 여부")
                                        .type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("deadLine").description("마감 기한, yyyy-MM-dd HH:mm 형식 문자열")
                                        .type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                    fieldWithPath("httpStatus").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                    fieldWithPath("message").description("메시지").type(JsonFieldType.STRING),
                                    fieldWithPath("data").description("데이터").type(JsonFieldType.OBJECT),
                                    fieldWithPath("data.vote_id").description("저장된 투표 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.title").description("저장된 투표 제목").type(JsonFieldType.STRING),
                                    fieldWithPath("data.voting").description("투표 마감 여부").type(JsonFieldType.BOOLEAN),
                                    fieldWithPath("data.host_id").description("투표 게시자 아이디")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contents").description("투표 세부 항목, MutableList<VotesContentsReturnDto>")
                                        .type(JsonFieldType.ARRAY),
                                    fieldWithPath("data.contents.[].votesContents_id").description("투표 세부 항목 아이디")
                                        .type(JsonFieldType.NUMBER),
                                    fieldWithPath("data.contents.[].content").description("투표 세부 항목 내용")
                                        .type(JsonFieldType.STRING),
                                    fieldWithPath("data.contents.[].selectedCount").description("항목 투표 개수")
                                        .type(JsonFieldType.NUMBER)
                                )
                            )
                        )
                }
            }
        }
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

    private fun saveTestPlanners() {
        val result = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
            contentType = MediaType.APPLICATION_JSON
            header("AccessToken", accessToken)
        }.andReturn()

        planner_id = getContent(result, "planner_id").toLong()

        testPlanners = plannersRepository.findById(planner_id)
            .orElseThrow { IllegalArgumentException("Error at VotesIntegrationTest.saveTestPlanners() : Cannot find Planners with planner_id : '$planner_id'") }
    }

    private fun deleteTestPlanners() {
        mockMvc.delete("/api/v1/planners/{planner_id}", planner_id) {
            contentType = MediaType.APPLICATION_JSON
            header("AccessToken", accessToken)
        }
    }

    private fun saveVotes() {
        val votesSaveRequestDto = VotesSaveRequestDto(
            votesTitle, contents, multipleVote, anonymousVote, deadLine, planner_id, user_id
        )
        val result = mockMvc.post("/api/v1/votes") {
            contentType = MediaType.APPLICATION_JSON
            header("AccessToken", accessToken)
            content = objectMapper.writeValueAsString(votesSaveRequestDto)
        }.andReturn()

        vote_id = getContent(result, "vote_id").toLong()
        testVotes = votesRepository.findById(vote_id)
            .orElseThrow { IllegalArgumentException("Error at VotesIntegrationTest.saveVotes() : Cannot find Votes with vote_id : '$vote_id'") }

        getNewVotesContentsIdList(result)
    }

    private fun getNewVotesContentsIdList(result: MvcResult) {
        val response = result.response.contentAsString
        val jsonObject = JSONObject(response)
        val contentsArray = jsonObject.getJSONObject("data").getJSONArray("contents")

        votesContentsIdList.clear()
        for (i in 0 until contentsArray.length()) {
            val contentObject = contentsArray.getJSONObject(i)
            val votesContentsId = contentObject.getLong("votesContents_id")
            votesContentsIdList.add(votesContentsId)
        }
        logger.info("votesContentsIdList : $votesContentsIdList")
    }

}