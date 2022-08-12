package com.entrip.repository

import com.entrip.domain.entity.PostsNestedComments
import org.springframework.data.jpa.repository.JpaRepository

interface PostsNestedCommentsRepository : JpaRepository<PostsNestedComments, Long> {
}