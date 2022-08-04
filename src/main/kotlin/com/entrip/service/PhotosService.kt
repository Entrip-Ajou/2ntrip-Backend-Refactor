package com.entrip.service

import com.entrip.domain.entity.Photos
import com.entrip.repository.PhotosRepository
import org.springframework.stereotype.Service

@Service
class PhotosService (
    final val photosRepository : PhotosRepository
    ) {
    private fun findPhotos(photo_id : Long) : Photos
    = photosRepository.findById(photo_id).orElseThrow {
        IllegalArgumentException("Error raise at photoRepository.findById")
    }

}