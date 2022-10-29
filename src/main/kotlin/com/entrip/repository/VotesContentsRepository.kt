package com.entrip.repository

import com.entrip.domain.entity.VotesContents
import org.springframework.data.jpa.repository.JpaRepository

interface VotesContentsRepository : JpaRepository<VotesContents, Long> {
}