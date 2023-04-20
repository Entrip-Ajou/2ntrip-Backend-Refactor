package com.entrip.repository

import com.entrip.domain.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UsersRepository : JpaRepository<Users, String> {
    fun existsByNickname (user_id : String) : Boolean
    @Query("select (count(u) > 0) from Users u where u.user_id = ?1")
    fun existsByUser_id (user_id : String) : Boolean
}