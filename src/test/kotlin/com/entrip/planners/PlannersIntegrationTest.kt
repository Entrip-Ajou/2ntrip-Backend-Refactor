package com.entrip.planners

import com.entrip.domain.RestAPIMessages
import com.entrip.domain.dto.Notices.NoticesReturnDto
import com.entrip.domain.dto.Notices.NoticesSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsReturnDto
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.junit.platform.commons.logging.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class PlannersIntegrationTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)
    private final val logger = LoggerFactory.getLogger(PlannersIntegrationTest::class.java)

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var plannersRepository: PlannersRepository

    @Autowired
    lateinit var usersRepository: UsersRepository

    final val user_id = "hhgg0925@ajou.ac.kr"

    final val objectMapper = ObjectMapper().registerModule(KotlinModule())

    lateinit var accessToken: String
    lateinit var refreshToken: String

    init {
        beforeSpec {
            plannersRepository.deleteAll()

            // planner 기능을 사용하기 위해서 user를 저장한 후, accessToken을 받아온다.

            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = "hhgg0925@ajou.ac.kr",
                nickname = "egenieee",
                gender = 0,
                password = "password",
                photoUrl = "2ntrip.com"
            )

            val usersLoginRequestDto = UsersLoginRequestDto(
                user_id = "hhgg0925@ajou.ac.kr",
                password = "password"
            )

            mockMvc.post("/api/v2/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(usersSaveRequestDto)
            }.andReturn()

            val result = mockMvc.post("/api/v2/users/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(usersLoginRequestDto)
            }.andReturn()

            accessToken = getContent(result, "accessToken")
            refreshToken = getContent(result, "refreshToken")
        }

        given("PlannersSaveRequestDto가 주어졌을 때") {
            val plannersSaveRequestDto = PlannersSaveRequestDto(
                user_id = user_id
            )

            `when`("플래너 생성을 요청하면") {
                then("플래너가 생성되고 plannersReturnDto가 반환된다") {
                    val result = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(plannersSaveRequestDto)
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                    }.andReturn()

                    val planner_id = getContent(result, "planner_id").toLong()

                    // plnnersRepository, plannerService를 통해서 제대로 저장이 되었는지 확인할 필요가 있을까?
                    plannersRepository.findById(planner_id).get().planner_id shouldBe 1
                }
            }
        }

        given("플래너가 저장된 상태에서") {
            val plannersSaveRequestDto = PlannersSaveRequestDto(
                user_id = user_id
            )

            val result = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_id = getContent(result, "planner_id").toLong()
            val wrong_planner_id = 100L

            `when`("PlannersUpdateRequestDto로 플래너 수정을 요청하면") {
                val plannersUpdateRequestDto = PlannersUpdateRequestDto(
                    title = "부산 여행",
                    start_date = "2023/05/05",
                    end_date = "2023/05/10"
                )

                then("플래너가 수정되고 plannersReturnDto가 반환된다") {
                    mockMvc.put("/api/v1/planners/{planner_id}", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(plannersUpdateRequestDto)
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                    }
                }
            }
            `when`("findById를 요청하면") {
                val plannersReturnDto = PlannersReturnDto(
                    PlannersResponseDto(
                        plannersRepository.findById(planner_id).get()
                    )
                )

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load planner with $planner_id",
                    data = plannersReturnDto
                )

                then("plannersReturnDto가 반환된다") {
                    mockMvc.get("/api/v1/planners/{planner_id}", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("올바른 id로 plannerIsExistWithId를 요청하면") {
                then("true가 반환된다") {
                    val successExpectedResponse = RestAPIMessages(
                        httpStatus = 200,
                        message = "Find if planner is exist with specific planner id : $planner_id",
                        data = true
                    )

                    mockMvc.get("/api/v1/planners/{planner_id}/exist", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("올바르지 않은 id로 plannerIsExistWithId를 요청하면") {
                then("NotAcceptedException가 반환된다") {
                    val failExpectedResponse = RestAPIMessages(
                        httpStatus = 202,
                        message = "NotAcceptedException\n",
                        data = false
                    )

                    mockMvc.get("/api/v1/planners/{planner_id}/exist", wrong_planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(failExpectedResponse)) }
                    }
                }
            }
        }

        given("플래너와 플랜이 저장된 상태에서") {
            // Planners
            val plannersSaveRequestDto = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannerSavedResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_id = getContent(plannerSavedResult, "planner_id").toLong()

            // Plans -> 리포지토리 통해서 바로 저장하는게 나은가, mockMvc post로 저장하는게 나은가

            val plansSaveRequestDto = PlansSaveRequestDto(
                planner_id = planner_id,
                date = "2023/05/05",
                todo = "국밥 먹기",
                time = "10:30",
                location = "부산광역시 서면",
                rgb = 1234
            )

            val planSavedResult = mockMvc.post("/api/v1/plans") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plansSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val plan_id = getContent(planSavedResult, "plan_id").toLong()

            val plansReturnDto = PlansReturnDto(
                plan_id = plan_id,
                date = "2023/05/05",
                todo = "국밥 먹기",
                time = "10:30",
                location = "부산광역시 서면",
                rgb = 1234,
                planner_id = planner_id,
                isExistComments = false
            )

            val plansList = mutableListOf(plansReturnDto)

            `when`("플래너에 있는 모든 플랜 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load all plans with specific planner id : $planner_id",
                    data = plansList
                )

                then("planList 반환된다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/all", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("Date로 플랜 조회를 요청하면") {
                val date = "20230505"

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Get all plans with specific date $date, planner_id $planner_id",
                    data = plansList
                )

                then("해당 날짜와 같은 플랜 리스트를 반환한다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/{date}/find", planner_id, date) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
        }

        given("플래너와 공지가 저장된 상태에서") {
            // Planners
            val plannersSaveRequestDto = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannerSavedResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_id = getContent(plannerSavedResult, "planner_id").toLong()

            // Notices

            val noticesSaveRequestDto = NoticesSaveRequestDto(
                author = "hhgg0925@ajou.ac.kr",
                title = "공지 제목",
                content = "공지 내용",
                planner_id = planner_id
            )

            val noticeSavedResult = mockMvc.post("/api/v1/notices") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(noticesSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val notice_id = getContent(noticeSavedResult, "notice_id").toLong()

            val noticesReturnDto = NoticesReturnDto(
                notice_id = notice_id,
                author = "hhgg0925@ajou.ac.kr",
                nickname = "egenieee",
                title = "공지 제목",
                content = "공지 내용",
                modifiedDate = "2023-04-17"
            )

            val noticesList = mutableListOf(noticesReturnDto)

            `when`("플래너에 있는 모든 공지 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Get all notices with planner id : $planner_id",
                    data = noticesList
                )

                then("noticeList가 반환된다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/allNotices", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
        }

        given("플래너와 투표가 저장된 상태에서") {
            // Planners
            val plannersSaveRequestDto = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannerSavedResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_id = getContent(plannerSavedResult, "planner_id").toLong()

            // Votes

            val votesSaveRequestDto = VotesSaveRequestDto(
                title = "투표 제목",
                contents = mutableListOf("1", "2", "3"),
                multipleVotes = true,
                anonymousVotes = false,
                deadLine = null,
                planner_id = planner_id,
                author = "hhgg0925@ajou.ac.kr"
            )

            val voteSavedResult = mockMvc.post("/api/v1/votes") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(votesSaveRequestDto)
                header("AccessToken", accessToken)
            }.andReturn()

            val vote_id = getContent(voteSavedResult, "vote_id").toLong()

            val votesReturnDto = VotesReturnDto(
                vote_id = vote_id,
                title = "투표 제목",
                voting = true,
                host_id = "hhgg0925@ajou.ac.kr",
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
                    message = "Load votes with planner id : $planner_id",
                    data = votesList
                )

                then("voteList가 반환된다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/allVotes", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
        }

        given("플래너가 두 개 저장된 상태에서 (1)") {
            val plannersSaveRequestDtoOne = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannersSaveRequestDtoTwo = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannersOneResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDtoOne)
                header("AccessToken", accessToken)
            }.andReturn()

            val plannersTwoResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDtoTwo)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_one_id = getContent(plannersOneResult, "planner_id").toLong()
            val planner_two_id = getContent(plannersTwoResult, "planner_id").toLong()

            `when`("플래너 삭제를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Delete planner with id : $planner_one_id",
                    data = planner_one_id
                )

                then("플래너가 삭제되고, 삭제된 플래너 아이디가 반환된다") {
                    mockMvc.delete("/api/v1/planners/{planner_id}", planner_one_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
        }

        given("플래너가 하나 저장되어 있고, 유저가 두 명 저장된 상태에서 (1)") {
            // New User
            val new_user_id = "nachokang@ajou.ac.kr"
            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = new_user_id,
                nickname = "nachokang",
                gender = 1,
                password = "test",
                photoUrl = "2ntrip.com"
            )

            mockMvc.post("/api/v2/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(usersSaveRequestDto)
            }.andReturn()

            val usersReturnDtoOne = UsersReturnDto(
                UsersResponseDto(
                    usersRepository.findById(user_id).get()
                )
            )

            val usersReturnDtoTwo = UsersReturnDto(
                UsersResponseDto(
                    usersRepository.findById(new_user_id).get()
                )
            )

            val usersList = mutableListOf(usersReturnDtoOne, usersReturnDtoTwo)


            val plannersSaveRequestDtoOne = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannersOneResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDtoOne)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_id = getContent(plannersOneResult, "planner_id").toLong()

            `when`("새로운 유저를 플래너에 추가하도록 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "$planner_id 번 플래너에 $new_user_id 사용자 등록 완료.",
                    data = planner_id
                )
                then("플래너에 유저가 등록되고, 플래너 아이디를 반환한다") {
                    mockMvc.put("/api/v1/planners/{planner_id}/{user_id}", planner_id, new_user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("이미 추가된 유저를 플래너에 추가하도록 요청하면") {
                mockMvc.put("/api/v1/planners/{planner_id}/{user_id}", planner_id, new_user_id) {
                    contentType = MediaType.APPLICATION_JSON
                    header("AccessToken", accessToken)
                }

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "이미 planner에 등록되어있는 회원입니다.",
                    data = planner_id
                )

                then("이미 추가되었다는 메시지를 반환받는다") {
                    mockMvc.put("/api/v1/planners/{planner_id}/{user_id}", planner_id, new_user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("플래너에 있는 모든 유저 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Load all users with planner id : $planner_id",
                    data = usersList
                )

                then("유저 리스트를 반환받는다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/getAllUser", planner_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("플래너에서 유저를 exit하도록 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "User ${new_user_id} exit planner ${planner_id}",
                    data = true
                )
                then("true값을 반환받는다") {
                    mockMvc.delete("/api/v1/planners/{planner_id}/{user_id}/exit", planner_id, new_user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
        }

        given("플래너가 하나 저장되어 있고, 유저가 두 명 저장된 상태에서 (2)") {
            // New User
            val new_user_id = "nachokang@ajou.ac.kr"
            val usersSaveRequestDto = UsersSaveRequestDto(
                user_id = new_user_id,
                nickname = "nachokang",
                gender = 1,
                password = "test",
                photoUrl = "2ntrip.com"
            )

            mockMvc.post("/api/v2/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(usersSaveRequestDto)
            }.andReturn()

            val plannersSaveRequestDtoOne = PlannersSaveRequestDto(
                user_id = user_id
            )

            val plannersOneResult = mockMvc.post("/api/v1/planners/{user_id}", user_id) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(plannersSaveRequestDtoOne)
                header("AccessToken", accessToken)
            }.andReturn()

            val planner_id = getContent(plannersOneResult, "planner_id").toLong()

            `when`("플래너에 있는 유저가 존재하는지 조회를 요청하면") {
                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Check if user : $user_id is exist at planner : $planner_id",
                    data = true
                )

                then("true값을 반환받는다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/{user_id}/exist", planner_id, user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("플래너에 없는 유저가 존재하는지 조회를 요청하면") {
                val wrong_user_id = "nachokang@ajou.ac.kr"

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 202,
                    message = "NotAcceptedException\n",
                    data = false
                )

                then("NotAcceptedException가 반환된다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/{user_id}/exist", planner_id, wrong_user_id) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("플래너에 있는 유저의 닉네임으로 조회를 요청하면") {
                val nickname = "egenieee"

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 200,
                    message = "Check if user : $nickname is exist at planner : $planner_id",
                    data = true
                )

                then("true값을 반환받는다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/{nickname}/exist/nickname", planner_id, nickname) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isOk() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
                }
            }
            `when`("플래너에 없는 유저의 닉네임으로 조회를 요청하면") {
                val wrong_nickname = "nachokang"

                val successExpectedResponse = RestAPIMessages(
                    httpStatus = 202,
                    message = "NotAcceptedException\n",
                    data = false
                )

                then("NotAcceptedException값을 반환받는다") {
                    mockMvc.get("/api/v1/planners/{planner_id}/{nickname}/exist/nickname", planner_id, wrong_nickname) {
                        contentType = MediaType.APPLICATION_JSON
                        header("AccessToken", accessToken)
                    }.andExpect {
                        status { isAccepted() }
                        content { json(objectMapper.writeValueAsString(successExpectedResponse)) }
                    }
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