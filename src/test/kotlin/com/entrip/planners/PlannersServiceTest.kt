package com.entrip.planners

import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.VotesContents.VotesContentsReturnDto
import com.entrip.domain.entity.*
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.entrip.service.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.*


class PlannersServiceTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    val plannersRepository = mockk<PlannersRepository>()
    val usersRepository = mockk<UsersRepository>()
    val plansService = mockk<PlansService>()
    val commentsService = mockk<CommentsService>()
    val noticesService = mockk<NoticesService>()
    val votesService = mockk<VotesService>()
    val plannersService = PlannersService(
        plannersRepository,
        usersRepository,
        plansService,
        commentsService,
        noticesService,
        votesService,
    )

    val users = Users(
        user_id = "hhgg0925@ajou.ac.kr",
        nickname = "egenieee",
        gender = 0,
        photoUrl = "test.com",
        token = "token",
        m_password = "test"
    )

    val planners = Planners(
        planner_id = 1L,
        title = "test",
        start_date = "2023/05/05",
        end_date = "2023/05/10"
    )

    val plans = Plans(
        plan_id = 1L,
        date = "2023/05/10",
        todo = "test",
        rgb = 1234,
        time = "22:30"
    )

    val notices = Notices(
        notice_id = 1L,
        author = users,
        planners = planners,
        title = "test",
        content = "test"
    )

    val votes = Votes(
        vote_id = 1L,
        title = "test",
        multipleVote = false,
        anonymousVote = false,
        planners = planners,
        author = users,
        voting = true,
        deadLine = LocalDateTime.now()
    )

    init {
        given("Planners save") {

            every { usersRepository.save(any()) } returns users
            every { usersRepository.findById(users.user_id) } returns Optional.of(users)
            every { usersRepository.findUsersByUser_idFetchPlanners(users.user_id) } returns Optional.of(users)
            every { usersRepository.findAll() } returns listOf(users)

            usersRepository.save(users)

            every { plannersRepository.save(any()) } returns planners
            every { plannersRepository.save(any()).planner_id } returns planners.planner_id
            every { plannersRepository.findAll() } returns listOf(planners)

            `when`("user_id를 주고 저장하면") {
                val savedPlannersId = plannersService.save("hhgg0925@ajou.ac.kr")

                then("저장된 planners의 id가 반환된다") {
                    savedPlannersId!! shouldBe 1L
                }
            }
        }

        given("Planners Update") {

            val plannersUpdateRequestDto = PlannersUpdateRequestDto(
                title = "testPlanners1",
                start_date = "2023/05/05",
                end_date = "2023/05/10",
            )

            every { plannersRepository.findPlannersByPlanner_idWithLazy(planners.planner_id!!) } returns Optional.of(
                planners
            )
            every { plannersRepository.findById(planners.planner_id!!) } returns Optional.of(planners)

            every { usersRepository.findUsersByUser_idFetchPlanners(users.user_id) } returns Optional.of(users)

            `when`("id와 PlannersUpdateRequestDto를 주고 수정하면") {
                val savedPlannersId = plannersService.save("hhgg0925@ajou.ac.kr")
                plannersService.update(savedPlannersId!!, plannersUpdateRequestDto)

                val plannersResponseDto = plannersService.findByPlannerId(savedPlannersId)

                then("Planners의 제목, 날짜가 수정된다") {
                    plannersResponseDto.planner_id shouldBe savedPlannersId
                    plannersResponseDto.title shouldBe plannersUpdateRequestDto.title
                    plannersResponseDto.start_date shouldBe plannersUpdateRequestDto.start_date
                    plannersResponseDto.end_date shouldBe plannersUpdateRequestDto.end_date
                }
            }
        }

        given("Planners find") {

            val validPlannerId = 1L
            val invalidPlannerId = 3L
            val validUserId = "hhgg0925@ajou.ac.kr"
            val invalidUserId = "hhgg0925@naver.com"
            val validUserNickname = "egenieee"
            val invalidUserNickname = "bbung"

            every { plannersRepository.save(any()) } returns planners
            every { plannersRepository.save(any()).planner_id } returns planners.planner_id
            every { plannersRepository.findAll() } returns listOf(planners)
            every { plannersRepository.findById(1L) } returns Optional.of(planners)
            every { plannersRepository.findPlannersByPlanner_idFetchUsers(planners.planner_id!!) } returns Optional.of(
                planners
            )
            every { plannersRepository.findPlannersByPlanner_idFetchNotices(planners.planner_id!!) } returns Optional.of(
                planners
            )
            every { plannersRepository.findPlannersByPlanner_idFetchPlans(planners.planner_id!!) } returns Optional.of(
                planners
            )
            every { plannersRepository.findPlannersByPlanner_idFetchVotes(planners.planner_id!!) } returns Optional.of(
                planners
            )

            every { plannersRepository.existsById(validPlannerId) } returns true
            every { plannersRepository.existsById(invalidPlannerId) } returns false

            every { usersRepository.save(any()) } returns users
            every { usersRepository.findById(users.user_id!!) } returns Optional.of(users)

            every { usersRepository.findUsersByUser_idFetchPlanners(users.user_id) } returns Optional.of(users)
            every { usersRepository.findAll() } returns listOf(users)
            every { usersRepository.existsById(validUserId) } returns true
            every { usersRepository.existsById(invalidUserId) } returns false
            every { usersRepository.existsByUser_id(validUserId) } returns true
            every { usersRepository.existsByUser_id(invalidUserId) } returns false
            every { usersRepository.existsByNickname(validUserNickname) } returns true
            every { usersRepository.existsByNickname(invalidUserNickname) } returns false

            every { plansService.plansRepository.save(any()) } returns plans
            every { noticesService.noticesRepository.save(any()) } returns notices
            every { votesService.votesRepository.save(any()) } returns votes
            every { votesService.findById(any()) } returns VotesReturnDto(
                votes,
                mutableListOf(VotesContentsReturnDto(VotesContents("test")))
            )


            val savedPlannersId = plannersService.save("hhgg0925@ajou.ac.kr")

            plansService.plansRepository.save(plans)
            noticesService.noticesRepository.save(notices)
            votesService.votesRepository.save(votes)

            planners.users.add(users)
            users.planners.add(planners)
            plans.setPlanners(planners)
            planners.plans?.add(plans)
            planners.notices.add(notices)
            planners.votes.add(votes)

            `when`("planner_id를 주고 findById를 하면") {
                val plannersResponseDto = plannersService.findByPlannerId(savedPlannersId!!)

                then("Planner가 조회된다") {
                    plannersResponseDto.planner_id shouldBe planners.planner_id
                    plannersResponseDto.title shouldBe planners.title
                    plannersResponseDto.start_date shouldBe planners.start_date
                    plannersResponseDto.end_date shouldBe planners.end_date
                }
            }

            `when`("findByPlannerIdWithDate를 호출하면") {
                val date = "20230510"

                val plansList = plannersService.findByPlannerIdWithDate(savedPlannersId!!, date)
                val findPlans = plansList[0]

                then("해당 date을 가진 plan을 모아 list로 반환한다") {
                    findPlans.plan_id shouldBe plans.plan_id
                    findPlans.date shouldBe plans.date
                    findPlans.rgb shouldBe plans.rgb
                    findPlans.time shouldBe plans.time
                    findPlans.todo shouldBe plans.todo
                }
            }

            `when`("planner_id를 주고 findAllUsersWithPlannerId를 호출하면") {
                val usersList = plannersService.findAllUsersWithPlannerId(savedPlannersId!!)
                val findUser = usersList[0]

                then("planners의 Users가 조회된다") {
                    findUser.user_id shouldBe users.user_id
                    findUser.nickname shouldBe users.nickname
                    findUser.gender shouldBe users.gender
                    findUser.token shouldBe users.token
                    findUser.photoUrl shouldBe users.photoUrl
                }
            }

            `when`("planner_id를 주고 findAllPlansWithPlannerId를 호출하면") {
                val plansList = plannersService.findAllPlansWithPlannerId(savedPlannersId!!)

                then("planners에 속한 Plans이 반환된다") {
                    plansList[0].plan_id shouldBe plans.plan_id
                    plansList[0].date shouldBe plans.date
                    plansList[0].rgb shouldBe plans.rgb
                    plansList[0].time shouldBe plans.time
                    plansList[0].todo shouldBe plans.todo
                }
            }

            `when`("planner_id를 주고 findAllNoticesWithPlannerId를 호출하면") {
                val noticesList = plannersService.findAllNoticesWithPlannerId(savedPlannersId!!)

                then("planners에 속한 Notices가 반환된다.") {
                    noticesList[0].notice_id shouldBe notices.notice_id
                    noticesList[0].title shouldBe notices.title
                    noticesList[0].content shouldBe notices.content
                    noticesList[0].author shouldBe notices.author?.user_id
                }
            }

            `when`("planner_id를 주고 findAllVotesWithPlannerId를 호출하면") {
                val votesList = plannersService.findAllVotesWithPlannerID(savedPlannersId!!)

                then("planners에 속한 Votes가 반환된다") {
                    votesList[0].vote_id shouldBe votes.vote_id
                    votesList[0].title shouldBe votes.title
                    votesList[0].voting shouldBe votes.voting
                    votesList[0].host_id shouldBe votes.author?.user_id
                }
            }

            `when`("유효한 planner_id를 주고 plannerIsExistWithId를 호출하면") {
                val isExist = plannersService.plannerIsExistWithId(validPlannerId)

                then("true가 반환된다") {
                    isExist shouldBe true
                }
            }

            `when`("유효하지 않은 planner_id를 주고 plannerIsExistWithId를 호출하면") {
                val isExist = plannersService.plannerIsExistWithId(invalidPlannerId)

                then("false가 반환된다") {
                    isExist shouldBe false
                }
            }

//            `when`("유효한 user_id를 주고 userIsExistInPlannerWithUserId를 호출하면") {
//                val isExist = plannersService.userIsExistInPlannerWithUserId(validPlannerId, validUserId)
//
//                then("true가 반환된다") {
//                    isExist shouldBe true
//                }
//            }
//
//            `when`("유효하지 않은 user_id를 주고 userIsExistInPlannerWithUserId를 호출하면") {
//                val isExist = plannersService.userIsExistInPlannerWithUserId(validPlannerId, invalidUserId)
//
//                then("false가 반환된다") {
//                    isExist shouldBe false
//                }
//            }
//
//            `when`("유효한 nickname를 주고 userIsExistInPlannerWithUserNickname를 호출하면") {
//                val isExist = plannersService.userIsExistInPlannerWithNickname(validPlannerId, validUserNickname)
//
//                then("true가 반환된다") {
//                    isExist shouldBe true
//                }
//            }

            `when`("유효하지 않은 nickname를 주고 userIsExistInPlannerWithUserNickname를 호출하면") {
                then("IllegalArgumentException이 throw된다") {
                    shouldThrow<IllegalArgumentException> {
                        plannersService.userIsExistInPlannerWithNickname(
                            validPlannerId,
                            invalidUserNickname
                        )
                    }
                }
            }
        }
    }
}