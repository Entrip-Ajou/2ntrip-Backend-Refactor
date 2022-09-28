package com.entrip.service

import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.Votes.VotesReturnDtoComparator
import com.entrip.domain.dto.Votes.VotesSaveRequestDto
import com.entrip.domain.dto.Votes.VotesUpdateRequestDto
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
import java.util.Collections
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

    @Transactional
    fun update(requestDto: VotesUpdateRequestDto) : Long? {
        val votes = findVotes(requestDto.vote_id)
        votes.updateTitle(requestDto.title)
        votes.updateAnonymousVote(requestDto.anonymousVote)
        votes.updateMultipleVote(requestDto.multipleVote)
        votes.updateDeadLine(requestDto.deadLine)

        return votes.vote_id
    }

    @Transactional
    fun delete(vote_id: Long) : Long {
        val votes = findVotes(vote_id)

        votes.planners!!.votes.remove(votes)
        votes.author!!.votes.remove(votes)

        val votesContentsSet = votes.contents
        val votesContentsIterator = votesContentsSet.iterator()

        while (votesContentsIterator.hasNext()) {
            val votesContent = votesContentsIterator.next()
            votesContentsIterator.remove()
            votesContentsService.delete(votesContent.votesContent_id)
        }

        votesRepository.delete(votes)

        return vote_id
    }

    private fun findUsers(user_id: String?): Users {
        val users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    fun findPlanners(plannerId: Long): Planners {
        val planners: Planners = plannersRepository.findById(plannerId).orElseThrow {
            IllegalArgumentException("Error raise at plannersRepository.findById$plannerId")
        }
        return planners
    }

    fun findById(vote_id: Long): VotesReturnDto {
        val votes = findVotes(vote_id)
        val votesContents = votes.contents
        val votesContentsList : MutableList<VotesContentsReturnDto> = ArrayList()
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

    fun findAllVotesWithPlannerID(planner_id : Long) : MutableList<VotesReturnDto> {
        val planners : Planners = findPlanners(planner_id)
        val votesListReturnDto : MutableList<VotesReturnDto> = ArrayList()

        for (vote in planners.votes) {
            val returnDto = findById(vote.vote_id!!)
            votesListReturnDto.add(returnDto)
        }

        Collections.sort(votesListReturnDto, VotesReturnDtoComparator())

        return votesListReturnDto
    }
}