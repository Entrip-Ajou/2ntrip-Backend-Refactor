package com.entrip.domain.dto.Photos

import com.entrip.domain.entity.Photos

class PhotosReturnDto(
    val photo_id: Long?,
    val photoUrl: String,
    val fileName: String,
    var priority: Long
) {
    constructor(photos: Photos) : this(
        photo_id = photos.photo_id,
        photoUrl = photos.photoUrl,
        fileName = photos.fileName,
        priority = photos.priority
    )
}