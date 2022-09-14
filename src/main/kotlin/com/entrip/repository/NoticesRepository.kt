package com.entrip.repository

import com.entrip.domain.entity.Notices
import org.springframework.data.jpa.repository.JpaRepository

interface NoticesRepository : JpaRepository<Notices, Long> {
}