package com.entrip.service

import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsReturnDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.domain.entity.Votes
import com.entrip.domain.entity.VotesContents
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import com.entrip.repository.VotesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.TreeSet
import javax.transaction.Transactional

@Service
class VotesService(
    val votesRepository: VotesRepository,

    @Autowired
    private val usersRepository: UsersRepository,

    @Autowired
    private val plannersRepository: PlannersRepository,

    @Autowired
    private val votesContentsService: VotesContentsService,
) {

    @Transactional
    fun save(requestDto: VotesSaveRequestDto) : Long? {
        val votesId = saveVotes(requestDto)
        val contents = votesContentsService.saveVotesContents(votesId!!, requestDto.contents)

        val votes : Votes = findVotes(votesId)
        votes.contents = contents

        return votes.vote_id
    }

    @Transactional
    fun saveVotes(requestDto: VotesSaveRequestDto) : Long? {
        val plannerId = requestDto.planner_id
        val planners : Planners = findPlanners(plannerId)
        val author = findUsers(requestDto.author)
        val votes = requestDto.toEntity()

        votes.author = author
        votes.planners = planners

        author.votes.add(votes)
        planners.votes.add(votes)

        // 투표 저장
        return votesRepository.save(votes).vote_id
    }

    private fun findUsers(user_id: String?): Users {
        val users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findPlanners(plannerId: Long): Planners {
        val planners: Planners = plannersRepository.findById(plannerId).orElseThrow {
            IllegalArgumentException("Error raise at plannersRepository.findById$plannerId")
        }
        return planners
    }

    fun findById(vote_id: Long): VotesReturnDto {
        val votes = findVotes(vote_id)
        val votesContents = votes.contents
        var votesContentsList : MutableList<VotesContentsReturnDto> = ArrayList()
        for (content in votesContents) {
            votesContentsList.add(VotesContentsReturnDto(content))
        }

        return VotesReturnDto(votes, votesContentsList)
    }

    private fun findVotes(vote_id: Long) : Votes {
        val votes = votesRepository.findById(vote_id).orElseThrow {
            IllegalArgumentException("Error raise at votesRepository.findById$vote_id")
        }
        return votes
    }
}