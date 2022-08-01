package com.entrip.service

import com.entrip.domain.dto.Posts.PostsSaveRequestDto
import com.entrip.domain.entity.Photos
import com.entrip.domain.entity.Posts
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

    public fun save (requestDto : PostsSaveRequestDto) : Long {
        var posts : Posts = requestDto.toEntity()
        posts.author = findUsers(requestDto.author)
        for (photoId : Long in requestDto.photoIdList) {
            val photos : Photos = findPhotos(photoId)
            photos.posts = posts
            posts.photoSet!!.add(photos)
        }
        postsRepository.save(posts)
        return posts.post_id!!
    }
}