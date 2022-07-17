package com.entrip.repository

import com.entrip.domain.entity.Planners
import org.springframework.data.jpa.repository.JpaRepository

interface PlannersRepository : JpaRepository<Planners, Long> {
}