package com.entrip.repository

import com.entrip.domain.entity.Votes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface VotesRepository : JpaRepository<Votes, Long> {

    @Query("select v from Votes v left join fetch v.author left join fetch v.planners where v.vote_id = :vote_id")
    fun findVotesByVote_idFetchAuthorAndPlanners(@Param("vote_id") vote_id: Long): Optional<Votes>

    @Query("select v from Votes v left join fetch v.contents where v.vote_id = :vote_id")
    fun findVotesByVotd_idFetchContents(@Param("vote_id") vote_id: Long): Optional<Votes>
}