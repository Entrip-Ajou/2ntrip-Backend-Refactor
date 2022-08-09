package com.entrip.repository

import com.entrip.domain.entity.Comments
import org.springframework.data.jpa.repository.JpaRepository

interface CommentsRepository : JpaRepository<Comments, Long> {
}