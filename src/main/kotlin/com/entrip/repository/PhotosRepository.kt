package com.entrip.repository

import com.entrip.domain.entity.Photos
import org.springframework.data.jpa.repository.JpaRepository

interface PhotosRepository : JpaRepository<Photos, Long> {
}