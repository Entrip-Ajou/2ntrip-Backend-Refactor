package com.entrip.planners

import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import com.entrip.repository.UsersRepository
import com.entrip.service.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.ApplicationEventPublisher
import java.util.*


class PlannersServiceTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    val plannersRepository = mockk<PlannersRepository>()
    val usersRepository = mockk<UsersRepository>()
    val plansRepository = mockk<PlansRepository>()
    val plansService = mockk<PlansService>()
    val commentsService = mockk<CommentsService>()
    val noticesService = mockk<NoticesService>()
    val votesService = mockk<VotesService>()
    val eventPublisher = mockk<ApplicationEventPublisher>()
    val plannersService = PlannersService(
        plannersRepository,
        usersRepository,
        plansRepository,
        plansService,
        commentsService,
        noticesService,
        votesService,
        eventPublisher
    )

    val users = Users(
        user_id = "hhgg0925@ajou.ac.kr",
        nickname = "egenieee",
        gender = 1,
        photoUrl = "test.com",
        token = "token",
        m_password = "test"
    )

    val planners = Planners(
        title = "test",
        start_date = "2023/05/05",
        end_date = "2023/05/10"
    )

    init {
        given("Planners") {

            every { usersRepository.save(any()) } returns users
            every { usersRepository.findById(users.user_id!!) } returns Optional.of(users)
            every { usersRepository.findAll() } returns emptyList()

            usersRepository.save(users)

            val plannersSaveRequestDto = PlannersSaveRequestDto(
                user_id = "hhgg0925@ajou.ac.kr"
            )

            val plannersUpdateRequestDto = PlannersUpdateRequestDto(
                title = "testPlanners1",
                start_date = "2023/05/05",
                end_date = "2023/05/10"
            )

            every { plannersRepository.save(any()) } returns planners
            every { plannersRepository.findAll() } returns listOf(planners)

            `when`("PlannersSaveRequestDto를 주고 저장하면") {
                val savedPlannersId = plannersService.save(plannersSaveRequestDto)

                then("저장된 planners의 id가 반환된다") {
                    savedPlannersId!! shouldBeGreaterThan 0L
                }
            }

            `when`("id와 PlannersUpdateRequestDto를 주고 수정하면") {
                val savedPlannersId = plannersService.save(plannersSaveRequestDto)
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
    }
}