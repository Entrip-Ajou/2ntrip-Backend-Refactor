package com.entrip.service

import com.entrip.domain.entity.Votes
import com.entrip.domain.entity.VotesContents
import com.entrip.repository.VotesContentsRepository
import com.entrip.repository.VotesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class VotesContentsService(
    val votesContentsRepository: VotesContentsRepository,

    @Autowired
    private val votesRepository: VotesRepository,
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
}