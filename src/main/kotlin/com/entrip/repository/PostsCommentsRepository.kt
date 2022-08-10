package com.entrip.repository

import com.entrip.domain.entity.PostsComments
import org.springframework.data.jpa.repository.JpaRepository

interface PostsCommentsRepository : JpaRepository<PostsComments, Long> {
}