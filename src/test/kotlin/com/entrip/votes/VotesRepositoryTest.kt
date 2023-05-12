package com.entrip.votes

import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.domain.entity.Votes
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.entrip.repository.VotesRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class VotesRepositoryTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var votesRepository: VotesRepository

    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var plannersRepository: PlannersRepository

    // final value for Test Users
    final val user_id = "test@gmail.com"
    final val nickname = "testNickname"
    final val gender = 1
    final val photoUrl = "testPhotoUrl.com"
    final val password = "testPassword"
    final val testUsers = Users(
        user_id = user_id,
        nickname = nickname,
        gender = gender,
        photoUrl = photoUrl,
        m_password = password
    )

    // final value for Test Planners
    final val planner_id = 1L
    final val plannersTitle = "testPlanners1"
    final val start_date = "2023/05/12"
    final val end_date = "2023/05/17"
    final val testPlanners = Planners(
        planner_id = planner_id,
        title = plannersTitle,
        start_date = start_date,
        end_date = end_date
    )

    // final value for Test Votes
    final val votesTitle = "testVotes1"
    final val multipleVote = false
    final val anonymousVote = false
    final val voting = false
    final val deadLine = LocalDateTime.now()

    init {

        // Votes 를 저장하기 전에 선행되어야 할 것
        // 1. Users 저장 : TestUsers
        // 2. Planners 저장 : TestPlanners

        beforeSpec { votesRepository.deleteAllInBatch() }

        afterSpec { votesRepository.deleteAllInBatch() }

        beforeEach {
            saveTestUsers()
            saveTestPlanners()
            joinUsersAndPlanners()
        }

        afterEach {
            disjoinUsersAndPlanners()
            deleteTestUsers()
            deleteTestPlanners()
        }

        given("Votes") {
            val votes = Votes(
                title = votesTitle,
                multipleVote = multipleVote,
                anonymousVote = anonymousVote,
                voting = voting,
                deadLine = deadLine
            )
            `when`("Votes 를 저장하면") {
                votesRepository.save(votes)
                val savedVotes = votesRepository.findAll().get(0)
                then("Votes 가 저장된다") {
                    savedVotes.vote_id shouldBe 1L
                    savedVotes.title shouldBe votesTitle
                    savedVotes.multipleVote shouldBe multipleVote
                    savedVotes.anonymousVote shouldBe anonymousVote
                    savedVotes.voting shouldBe voting
                    savedVotes.deadLine shouldBe deadLine
                }
            }
            `when`("Votes 를 findById 로 조회하면") {
                val foundVotes = votesRepository.findById(1L).orElseThrow { IllegalArgumentException() }
                then("Votes 가 조회된다") {
                    foundVotes.vote_id shouldBe 1L
                    foundVotes.title shouldBe votesTitle
                    foundVotes.multipleVote shouldBe multipleVote
                    foundVotes.anonymousVote shouldBe anonymousVote
                    foundVotes.voting shouldBe voting
                    foundVotes.deadLine shouldBe deadLine
                }
            }
            `when`("Votes 를 삭제하면") {
                votesRepository.delete(votes)
                then("Votes 가 삭제된다") {
                    shouldThrow<IllegalArgumentException> { votesRepository.findById(1L).orElseThrow { IllegalArgumentException() } }
                    votesRepository.findAll().size shouldBe 0L
                }
            }
        }


    }

    private fun saveTestUsers() =
        usersRepository.save(testUsers)

    private fun saveTestPlanners() =
        plannersRepository.save(testPlanners)

    private fun deleteTestUsers() =
        usersRepository.delete(testUsers)

    private fun deleteTestPlanners() =
        plannersRepository.delete(testPlanners)

    private fun joinUsersAndPlanners() {
        testUsers.planners.add(testPlanners)
        testPlanners.users.add(testUsers)
    }

    private fun disjoinUsersAndPlanners() {
        testUsers.planners.clear()
        testPlanners.users.clear()
    }

}