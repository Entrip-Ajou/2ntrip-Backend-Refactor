package com.entrip.repository

import com.entrip.domain.entity.Posts
import org.springframework.data.jpa.repository.JpaRepository

interface PostsRepository : JpaRepository<Posts, Long> {
}