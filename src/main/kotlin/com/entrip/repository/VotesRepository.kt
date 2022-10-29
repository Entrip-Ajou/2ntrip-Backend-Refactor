package com.entrip.repository

import com.entrip.domain.entity.Votes
import org.springframework.data.jpa.repository.JpaRepository

interface VotesRepository : JpaRepository<Votes, Long> {
}