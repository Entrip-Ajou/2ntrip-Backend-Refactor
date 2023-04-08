package com.entrip.planners

import com.entrip.domain.entity.Planners
import com.entrip.repository.PlannersRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class PlannersRepositoryTest : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var plannersRepository: PlannersRepository

    init {
        beforeSpec {
            plannersRepository.deleteAllInBatch()
        }
        given("Planners") {
            val plannersOne = Planners(
                title = "testPlanners1",
                start_date = "2023/04/05",
                end_date = "2023/04/10"
            )

            val plannersTwo = Planners(
                title = "testPlanners2",
                start_date = "2023/04/10",
                end_date = "2023/04/15"
            )

            `when`("Planners를 저장하면") {
                plannersRepository.save(plannersOne)
                plannersRepository.save(plannersTwo)

                val savedPlanners = plannersRepository.findAll()[0]

                then("Planners가 저장된다") {
                    savedPlanners.planner_id shouldBe plannersOne.planner_id
                    savedPlanners.title shouldBe plannersOne.title
                    savedPlanners.start_date shouldBe plannersOne.start_date
                    savedPlanners.end_date shouldBe plannersOne.end_date
                }
            }

            `when`("Planners를 findById로 찾으면") {
                val savedPlannersId = plannersOne.planner_id

                val savedPlanners = plannersRepository.findById(savedPlannersId!!)
                    .orElseThrow { IllegalArgumentException("Cannot find planners with planner id $savedPlannersId") }

                then("Planners가 조회된다") {
                    savedPlanners.planner_id shouldBe plannersOne.planner_id
                    savedPlanners.title shouldBe plannersOne.title
                    savedPlanners.start_date shouldBe plannersOne.start_date
                    savedPlanners.end_date shouldBe plannersOne.end_date
                }
            }

            `when`("Planners를 삭제하면") {
                val savedPlannersOne = plannersRepository.findById(plannersOne.planner_id!!)
                    .orElseThrow { IllegalArgumentException("Cannot find planners with planner id ${plannersOne.planner_id}") }

                plannersRepository.delete(savedPlannersOne)

                val savedPlanners = plannersRepository.findAll()[0]

                then("Planners가 삭제된다") {
                    savedPlanners.planner_id shouldNotBe plannersOne.planner_id
                    savedPlanners.title shouldNotBe plannersOne.title
                    savedPlanners.start_date shouldNotBe plannersOne.start_date
                    savedPlanners.end_date shouldNotBe plannersOne.end_date

                    savedPlanners.planner_id shouldBe plannersTwo.planner_id
                    savedPlanners.title shouldBe plannersTwo.title
                    savedPlanners.start_date shouldBe plannersTwo.start_date
                    savedPlanners.end_date shouldBe plannersTwo.end_date
                }
            }
        }
    }
}