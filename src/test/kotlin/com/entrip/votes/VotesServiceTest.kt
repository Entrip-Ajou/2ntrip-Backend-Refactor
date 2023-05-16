package com.entrip.votes

import com.entrip.domain.dto.Votes.UsersAndContentsReturnDto
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.Votes.VotesUpdateRequestDto
import com.entrip.domain.dto.Votes.VotesUserReturnDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.domain.entity.Votes
import com.entrip.domain.entity.VotesContents
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.entrip.repository.VotesRepository
import com.entrip.service.VotesContentsService
import com.entrip.service.VotesService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class VotesServiceTest : BehaviorSpec() {

    val votesRepository = mockk<VotesRepository>()
    val usersRepository = mockk<UsersRepository>()
    val plannersRepository = mockk<PlannersRepository>()
    val votesContentsService = mockk<VotesContentsService>()

    val votesService = VotesService(votesRepository, usersRepository, plannersRepository, votesContentsService)

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
    final var votesTitle = "testVotes1"
    final var multipleVote = false
    final var anonymousVote = false
    final var voting = true
    final var deadLine = "2023-05-12 14:27"
    final val content1 = "content1"
    final val content2 = "content2"
    final val contents = mutableListOf<String>(content1, content2)

    // final value for Test VotesContents
    final val votesContents1 = VotesContents(
        votesContent_id = 1L,
        contents = content1,
        selectedCount = 0
    )

    final val votesContents2 = VotesContents(
        votesContent_id = 2L,
        contents = content2,
        selectedCount = 0
    )

    final val votesContents = mutableListOf<VotesContents>(votesContents1, votesContents2)

    final val testVotes = Votes(
        vote_id = 1L,
        title = votesTitle,
        multipleVote = multipleVote,
        anonymousVote = anonymousVote,
        voting = voting,
        deadLine = LocalDateTime.parse(deadLine, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        author = testUsers,
        planners = testPlanners,
        contents = votesContents
    )


    init {
        given("VotesSaveRequestDto를 주고") {
            val votesSaveRequestDto = VotesSaveRequestDto(
                title = votesTitle,
                contents = contents,
                multipleVotes = multipleVote,
                anonymousVotes = anonymousVote,
                deadLine = deadLine.toString(),
                planner_id = planner_id,
                author = user_id
            )
            every { votesContentsService.saveVotesContents(any(), any()) } returns votesContents
            every { plannersRepository.findById(any()) } returns Optional.of(testPlanners)
            every { usersRepository.findById(any()) } returns Optional.of(testUsers)
            every { votesRepository.save(any()) } returns testVotes
            every { votesRepository.findById(any()) } returns Optional.of(testVotes)

            `when`("save하면") {
                val result = votesService.save(votesSaveRequestDto)
                then("save된 votes의 아이디가 (1L) 리턴된다") {
                    result shouldBe 1L
                }
            }
        }

        given("Votes가 Save된 상황에서") {


            every { votesRepository.findById(1L) } returns Optional.of(testVotes)
            every { votesRepository.findById(2L) } returns Optional.empty()


            `when`("findById(2L)하면 : 2L은 invalid Votes Id") {
                then("IllegalArgumentException이 throw된다") {
                    shouldThrow<IllegalArgumentException> {
                        votesService.findById(2L)
                    }
                }
            }
            `when`("update(1L)하면") {
                val votesUpdateRequestDto = VotesUpdateRequestDto(
                    vote_id = 1L,
                    title = "updatedTitle",
                    multipleVote = true,
                    anonymousVote = true,
                    deadLine = "1111-01-01 00:00"
                )
                val result = votesService.update(votesUpdateRequestDto)
                val updatedVotes = votesService.findById(1L)
                then("Votes(1L) 가 업데이트된다") {
                    result shouldBe 1L
                    updatedVotes.title shouldBe "updatedTitle"
                }
                votesTitle = "updatedTitle"
                multipleVote = true
                anonymousVote = true
                deadLine = "1111-01-01 00:00"
            }

            val mockUsersAndContentsReturnDto = UsersAndContentsReturnDto(
                content_id = 1L,
                content = content1,
                users = mutableListOf()
            )
            every { votesContentsService.getVotingUsersReturnDto(any()) } returns mockUsersAndContentsReturnDto

            `when`("getVotesInfoReturnDto 하면") {
                val result = votesService.getVotesInfoReturnDto(1L)
                then("VotesFullInfoReturnDto 를 리턴한다") {
                    result.title shouldBe "updatedTitle"
                    result.contentsAndUsers.size shouldBe 2
                    result.contentsAndUsers.get(0).content_id shouldBe 1
                    result.contentsAndUsers.get(0).content shouldBe content1
                    result.contentsAndUsers.get(0).users shouldBe emptyList<VotesUserReturnDto>()
                    result.contentsAndUsers.get(1).content_id shouldBe 1
                    result.contentsAndUsers.get(1).content shouldBe content1
                    result.contentsAndUsers.get(1).users shouldBe emptyList<VotesUserReturnDto>()
                    result.multipleVotes shouldBe true
                    result.anonymousVote shouldBe true
                    result.host_id shouldBe user_id
                    result.voting shouldBe voting
                }
            }

            `when`("terminateVote(1L) 하면") {
                votesService.terminateVote(1L)
                val terminatedVotes = votesService.getVotesInfoReturnDto(1L)
                then("Votes(1L)의 voting이 false가 된다") {
                    terminatedVotes.voting shouldBe false
                }
            }

            justRun { votesRepository.delete(any()) }
            every { votesContentsService.delete(any()) } returns 1L
            `when`("delete(1L) 하면") {
                val result = votesService.delete(1L)
                then("1L이 리턴되며 Votes가 삭제된다") {
                    result shouldBe 1L
                }
            }
        }

        given("2개의 Votes의 deadline이 하나는 beforeNow, 하나는 afterNow 일 떄") {
            val beforeNowDeadLine = "1000-05-12 14:27"
            val afterNowDeadLine = "3000-05-12 14:27"

            val testVotes1 = Votes(
                vote_id = 1L,
                title = votesTitle,
                multipleVote = multipleVote,
                anonymousVote = anonymousVote,
                voting = voting,
                deadLine = LocalDateTime.parse(beforeNowDeadLine, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                author = testUsers,
                planners = testPlanners,
                contents = votesContents
            )

            val testVotes2 = Votes(
                vote_id = 2L,
                title = votesTitle,
                multipleVote = multipleVote,
                anonymousVote = anonymousVote,
                voting = voting,
                deadLine = LocalDateTime.parse(afterNowDeadLine, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                author = testUsers,
                planners = testPlanners,
                contents = votesContents
            )

            every { votesRepository.findAll() } returns listOf(testVotes1, testVotes2)
            `when`("terminateOverDueVotes를 실행하면") {
                votesService.terminateOverDueVotes()
                then("testVotes1은 voting이 false, testVotes2는 voting이 true가 된다") {
                    testVotes1.voting shouldBe false
                    testVotes2.voting shouldBe true
                }
            }
        }

    }
}