package com.entrip.repository

import com.entrip.domain.entity.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UsersRepository : JpaRepository<Users, String> {
}