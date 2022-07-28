package com.entrip.service

import com.entrip.domain.entity.Photos
import com.entrip.domain.entity.Users
import com.entrip.repository.PhotosRepository
import com.entrip.repository.PostsRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PostsService (
    final val postsRepository: PostsRepository,

    @Autowired
    val usersRepository: UsersRepository,

    @Autowired
    val photosRepository: PhotosRepository
        ) {

    private fun findUsers(user_id : String?) : Users {
        val users : Users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findPhotos(photo_id : Long) : Photos {
        val photos : Photos = photosRepository.findById(photo_id).orElseThrow {
            IllegalArgumentException("Error raise at photosRepository.findById${photo_id}")
        }
        return photos
    }
}