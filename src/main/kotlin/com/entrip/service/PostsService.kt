package com.entrip.service

import com.entrip.domain.dto.Posts.PostsRequestDto
import com.entrip.domain.dto.Posts.PostsReturnDto
import com.entrip.domain.dto.Posts.PostsSaveRequestDto
import com.entrip.domain.dto.Posts.PostsUpdateRequestDto
import com.entrip.domain.entity.Photos
import com.entrip.domain.entity.Posts
import com.entrip.domain.entity.Users
import com.entrip.repository.PhotosRepository
import com.entrip.repository.PostsRepository
import com.entrip.repository.UsersRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PostsService(
    private final val postsRepository: PostsRepository,

    @Autowired
    val usersRepository: UsersRepository,

    @Autowired
    val photosRepository: PhotosRepository,

    @Autowired
    val photosService: PhotosService
) {

    private val logger: Logger = LoggerFactory.getLogger(PostsService::class.java)

    private fun findUsers(user_id: String?): Users {
        val users: Users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findPhotos(photo_id: Long): Photos {
        val photos: Photos = photosRepository.findById(photo_id).orElseThrow {
            IllegalArgumentException("Error raise at photosRepository.findById${photo_id}")
        }
        return photos
    }

    private fun findPosts(post_id: Long): Posts = postsRepository.findById(post_id).orElseThrow {
        IllegalArgumentException("Error raise at postsRepository.findById $post_id")
    }

    @Transactional
    public fun save(requestDto: PostsSaveRequestDto): Long {
        var posts: Posts = requestDto.toEntity()
        posts.author = findUsers(requestDto.author)
        posts.author!!.posts.add(posts)
        for (photoId: Long in requestDto.photoIdList) {
            val photos: Photos = findPhotos(photoId)
            photos.posts = posts
            posts.photoSet!!.add(photos)
        }
        postsRepository.save(posts)
        return posts.post_id!!
    }

    @Transactional
    public fun update(post_id: Long, requestDto: PostsUpdateRequestDto): Long? {
        val posts: Posts = findPosts(post_id)
        posts.update(requestDto.title, requestDto.content)
        return posts.post_id
    }

    public fun findById(post_id: Long): PostsReturnDto {
        val postsRequestDto = PostsRequestDto(findPosts(post_id))
        postsRequestDto.sortPhotoListWithPriority()
        return PostsReturnDto(postsRequestDto)
    }

    @Transactional
    public fun delete(post_id: Long): Long {
        val posts = findPosts(post_id)

        if (posts.photoSet != null) {
            val iterator = posts.photoSet!!.iterator()
            while (iterator.hasNext()) {
                val photos = iterator.next()
                iterator.remove()
                photosService.delete(photos.photo_id!!)
            }
        }

        posts.author!!.posts.remove(posts)
        postsRepository.delete(posts)
        return post_id
    }

}