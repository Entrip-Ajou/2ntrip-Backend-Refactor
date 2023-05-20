package com.entrip.plans

import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Plans.PlansUpdateRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import com.entrip.service.CommentsService
import com.entrip.service.PlansService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import java.util.*

class PlansServiceTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    val plansRepository = mockk<PlansRepository>()
    val plannersRepository = mockk<PlannersRepository>()
    val commentsService = mockk<CommentsService>()

    val plansService = PlansService(
        plansRepository,
        plannersRepository,
    )

    val planners = Planners(
        planner_id = 1L,
        title = "test",
        start_date = "2023/06/10",
        end_date = "2023/06/15"
    )

    val plans = Plans(
        plan_id = 1L,
        date = "2023/06/10",
        todo = "todo",
        rgb = 1234,
        time = "10:30"
    )

    init {

        beforeSpec {
            every { plannersRepository.save(planners) } returns planners

            plannersRepository.save(planners)
        }

        given("PlansSaveRequestDto가 주어진 상태에서") {
            val plansSaveRequestDto = PlansSaveRequestDto(
                planner_id = 1L,
                date = "2023/06/10",
                todo = "todo",
                rgb = 1234,
                time = "10:30"
            )

            every { plansRepository.save(any()) } returns plans
            every { plannersRepository.findPlannersByPlanner_idFetchPlans(any()) } returns Optional.of(planners)

            `when`("저장하면") {
                val savedPlansId = plansService.save(plansSaveRequestDto)

                then("plans가 저장된다") {
                    savedPlansId shouldBe 1L
                }
            }
        }

        given("PlansUpdateRequestDto가 주어진 상태에서") {
            val changedTime = "11:00"

            val plansUpdateRequestDto = PlansUpdateRequestDto(
                date = "2023/06/11",
                todo = "todo",
                time = changedTime,
                rgb = 1234
            )

            every { plansRepository.findById(plans.plan_id!!) } returns Optional.of(plans)
            every { plansRepository.findPlansByPlan_idFetchPlanners(plans.plan_id!!) } returns Optional.of(plans)
            every { plansRepository.findPlansByPlan_idFetchComments(plans.plan_id!!) } returns Optional.of(plans)

            plans.setPlanners(planners)

            `when`("수정하면") {
                val updatedPlansId = plansService.update(plans.plan_id!!, plansUpdateRequestDto)

                val updatedPlans = plansService.findById(updatedPlansId!!)

                then("plans가 수정된다") {
                    updatedPlans.plan_id shouldBe 1L
                    updatedPlans.time shouldBe changedTime
                }
            }
        }

        given("Plans의 Id가 주어졌을 때 (1)") {
            val validPlansId = 1L
            val invalidPlansId = 100L

            every { plansRepository.findPlansByPlan_idFetchPlanners(validPlansId) } returns Optional.of(plans)
            every { plansRepository.findPlansByPlan_idFetchComments(validPlansId) } returns Optional.of(plans)

            every { plansRepository.findPlansByPlan_idFetchPlanners(invalidPlansId) } returns Optional.empty()
            every { plansRepository.findPlansByPlan_idFetchComments(invalidPlansId) } returns Optional.empty()

            `when`("올바른 Id값으로 findById를 호출하면") {
                val savedPlans = plansService.findById(validPlansId)

                then("plans이 조회된다") {
                    savedPlans.plan_id shouldBe validPlansId
                }
            }

            `when`("올바르지 않은 Id값으로 findById를 호출하면") {
                then("IllegalArgumentException이 throw된다") {
                    shouldThrow<IllegalArgumentException> {
                        plansService.findById(invalidPlansId)
                    }
                }
            }
        }

        given("Plans의 Id가 주어졌을 때 (2)") {
            val plansId = 1L

            justRun { plansRepository.delete(plans) }
            every { plansRepository.findPlansByPlan_idFetchPlanners(plans.plan_id!!) } returns Optional.of(plans)
            every { plansRepository.findAll() } returns emptyList()

            `when`("plans을 삭제하면") {
                val deleteId = plansService.delete(plansId)

                then("plans가 삭제된다") {
                    deleteId shouldBe plansId
                    plansRepository.findAll().size shouldBe 0
                }
            }
        }
    }
}