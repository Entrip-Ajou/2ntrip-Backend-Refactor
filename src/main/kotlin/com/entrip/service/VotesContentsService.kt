package com.entrip.service

import com.entrip.domain.dto.Votes.VotesUserReturnDto
import com.entrip.domain.dto.Votes.UsersAndContentsReturnDto
import com.entrip.domain.dto.VotesContents.PreviousVotesContentsRequestDto
import com.entrip.domain.dto.VotesContents.VotesContentsCountRequestDto
import com.entrip.domain.entity.Users
import com.entrip.domain.entity.Votes
import com.entrip.domain.entity.VotesContents
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.UsersRepository
import com.entrip.repository.VotesContentsRepository
import com.entrip.repository.VotesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Service
class VotesContentsService(
    val votesContentsRepository: VotesContentsRepository,

    @Autowired
    private val votesRepository: VotesRepository,

    @Autowired
    private val usersRepository: UsersRepository,
) {

    fun saveVotesContents(votesId : Long, contents: MutableList<String>) : MutableSet<VotesContents> {
        // VotesContents Entity 하나씩 만들어서 리스트로 묶어서 리턴
        val votesSet : MutableSet<VotesContents> = TreeSet()
        for (content in contents) {
            val votesContentsId = save(votesId, content)
            val votesContents = findVotesContents(votesContentsId!!)
            votesSet.add(votesContents)
        }

        return votesSet
    }

    @Transactional
    fun save(vote_id : Long, content : String) : Long? {
        val votes : Votes = findVotes(vote_id)

        val votesContents = VotesContents(content)
        votesContents.votes = votes

        return votesContentsRepository.save(votesContents).votesContent_id
    }

    @Transactional
    fun delete(vote_content_id : Long?) : Long {
        val votesContents = findVotesContents(vote_content_id!!)

        val usersSet : MutableSet<Users> = votesContents.usersSet
        for (user in usersSet) {
            user.votesContents.remove(votesContents)
        }

        votesContents.votes!!.contents.remove(votesContents)
        votesContentsRepository.delete(votesContents)
        return vote_content_id
    }

    fun findVotes(vote_id: Long) : Votes {
        val votes = votesRepository.findById(vote_id).orElseThrow {
            IllegalArgumentException("Error raise at votesRepository.findById$vote_id")
        }
        return votes
    }

    fun findVotesContents(vote_content_id: Long): VotesContents {
        val votesContents = votesContentsRepository.findById(vote_content_id).orElseThrow {
            IllegalArgumentException("Error raise at votesContentsRepository.findById$vote_content_id")
        }
        return votesContents
    }

    fun getVotingUsersReturnDto(content: VotesContents): UsersAndContentsReturnDto {
        val usersIterator = content.usersSet.iterator()
        val usersList : MutableList<VotesUserReturnDto> = ArrayList()
        while (usersIterator.hasNext()) {
            val usersReturnDto = VotesUserReturnDto(usersIterator.next())
            usersList.add(usersReturnDto)
        }
        return UsersAndContentsReturnDto(content.votesContent_id!!, content.contents, usersList)
    }

    @Transactional
    fun vote(requestDto : VotesContentsCountRequestDto) : Long? {
        val users : Users = findUsers(requestDto.user_id)
        val votes = checkValidVotes(requestDto.vote_id)
        for (votesContentsId in requestDto.voteContents_id) {
            val votesContents : VotesContents = findVotesContents(votesContentsId)
            votesContents.usersSet.add(users)
            users.votesContents.add(votesContents)
            votesContents.vote()
        }
        return votes.vote_id
    }

    @Transactional
    fun undoVote(requestDto : VotesContentsCountRequestDto) : Long? {
        val users : Users = findUsers(requestDto.user_id)
        val votes = checkValidVotes(requestDto.vote_id)
        val votesContents : MutableSet<VotesContents> = votes.contents
        for (votesContent in votesContents) {
            if (votesContent.usersSet.contains(users)) {
                votesContent.usersSet.remove(users)
                users.votesContents.remove(votesContent)
                votesContent.undoVote()
            }
        }
        return votes.vote_id
    }

    fun getPreviousVoteContents(requestDto: PreviousVotesContentsRequestDto): MutableList<Long> {
        val previousVotesContents : MutableList<Long> = ArrayList()
        val votes : Votes = findVotes(requestDto.vote_id)
        val user : Users = findUsers(requestDto.user_id)
        val votesContents : MutableSet<VotesContents> = votes.contents

        for (voteContent in votesContents) {
            if (voteContent.usersSet.contains(user)) {
                val contentId = voteContent.votesContent_id
                previousVotesContents.add(contentId!!)
            }
        }

        return previousVotesContents
    }

    private fun checkValidVotes(votesId: Long) : Votes {
        val votes : Votes = votesRepository.findById(votesId).orElseThrow {
            NotAcceptedException("votes is not exist")
        }
        if (!votes.voting) {
            throw NotAcceptedException("votes is terminated")
        }

        return votes
    }

    private fun findUsers(user_id: String?): Users {
        val users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }
}