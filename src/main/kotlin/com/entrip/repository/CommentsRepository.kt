package com.entrip.repository

import com.entrip.domain.Comments
import org.springframework.data.jpa.repository.JpaRepository

interface CommentsRepository : JpaRepository <Comments, Long> {
}