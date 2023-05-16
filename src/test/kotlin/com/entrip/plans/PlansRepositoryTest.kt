package com.entrip.plans

import com.entrip.domain.entity.Plans
import com.entrip.repository.PlansRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class PlansRepositoryTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var plansRepository: PlansRepository

    init {
        beforeSpec {
            plansRepository.deleteAll()
        }

        given("plans이 주어졌을 때") {
            val plansOne = Plans(
                date = "2023/06/10",
                todo = "todo",
                time = "time",
                location = "location",
                rgb = 1234
            )

            val plansTwo = Plans(
                date = "2023/06/15",
                todo = "todo2",
                time = "time2",
                location = "location2",
                rgb = 4321
            )

            `when`("plans을 저장하면") {
                plansRepository.save(plansOne)
                plansRepository.save(plansTwo)

                val savedPlans = plansRepository.findAll()[0]

                then("plans가 저장된다") {
                    plansRepository.findAll().size shouldBe 2

                    savedPlans.plan_id shouldBe plansOne.plan_id
                    savedPlans.date shouldBe plansOne.date
                    savedPlans.todo shouldBe plansOne.todo
                    savedPlans.time shouldBe plansOne.time
                    savedPlans.location shouldBe plansOne.location
                    savedPlans.rgb shouldBe plansOne.rgb
                }
            }

            `when`("plans을 조회하면") {
                val savedPlansId = plansOne.plan_id

                val savedPlans = plansRepository.findById(savedPlansId!!)
                    .orElseThrow { IllegalArgumentException("Cannot find plans with plan id $savedPlansId") }

                then("plans이 조회된다") {
                    savedPlans.plan_id shouldBe plansOne.plan_id
                    savedPlans.date shouldBe plansOne.date
                    savedPlans.todo shouldBe plansOne.todo
                    savedPlans.time shouldBe plansOne.time
                    savedPlans.location shouldBe plansOne.location
                    savedPlans.rgb shouldBe plansOne.rgb
                }
            }

            `when`("plans을 삭제하면") {
                val savedPlans = plansRepository.findById(plansOne.plan_id!!)
                    .orElseThrow { IllegalArgumentException("Cannot find plans with plan id ${plansOne.plan_id}") }

                plansRepository.delete(savedPlans)

                then("plans가 삭제된다") {
                    plansRepository.findAll().size shouldBe 1
                }
            }
        }
    }
}