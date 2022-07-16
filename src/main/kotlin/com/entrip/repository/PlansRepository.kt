package com.entrip.repository

import com.entrip.domain.Plans
import org.springframework.data.jpa.repository.JpaRepository

interface PlansRepository : JpaRepository<Plans, Long> {
}