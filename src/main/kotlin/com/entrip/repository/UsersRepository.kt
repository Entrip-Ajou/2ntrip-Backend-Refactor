package com.entrip.repository

import com.entrip.domain.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UsersRepository : JpaRepository<Users, String> {
    fun existsByNickname (user_id : String) : Boolean
    @Query("select (count(u) > 0) from Users u where u.user_id = ?1")
    fun existsByUser_id (user_id : String) : Boolean

    @Query("select u from Users u left join fetch u.planners p where u.user_id = :user_id")
    fun findUsersByUser_idFetchPlanners (@Param("user_id")user_id : String) : Optional<Users>

    @Query("select u from Users u where u.user_id = ?1")
    fun findUsersByUser_idWithLazy (user_id : String) : Optional<Users>

}