package com.entrip.service

import com.entrip.domain.dto.Posts.PostsRequestDto
import com.entrip.domain.dto.Posts.PostsReturnDto
import com.entrip.domain.dto.Posts.PostsSaveRequestDto
import com.entrip.domain.dto.Posts.PostsUpdateRequestDto
import com.entrip.domain.entity.Photos
import com.entrip.domain.entity.Posts
import com.entrip.domain.entity.Users
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.PhotosRepository
import com.entrip.repository.PostsRepository
import com.entrip.repository.UsersRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.Exception
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
        val posts = findPosts(post_id)
        val postsRequestDto = PostsRequestDto(posts)
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

    public fun getPostsListWithPageNumber(pageNumber: Long) : MutableList<PostsReturnDto> {
        val postsList = postsRepository.findAll()
        postsList.reverse()
        val iterator = postsList.iterator()
        val returnPostsList : MutableList<PostsReturnDto> = ArrayList<PostsReturnDto>()
        for (i in 1..(pageNumber-1)*15) {
            iterator.next()
            if (!iterator.hasNext()) throw NotAcceptedException("Page number is not valid!")
        }
        for (i in 1..15) {
            val posts = iterator.next()
            val requestDto = PostsRequestDto(posts)
            val returnDto = PostsReturnDto (requestDto)
            returnPostsList.add(returnDto)
            if (!iterator.hasNext()) break;
        }
        return returnPostsList
    }

    @Transactional
    public fun raiseLikeNumber(post_id: Long, user_id: String) : Long {
        val posts = findPosts(post_id)
        val users = findUsers(user_id)
        if (posts.likeUsers.contains(users) || users.likePosts.contains(posts)) throw NotAcceptedException("Users $user_id is already like post $post_id !")
        users.likePosts.add(posts)
        posts.likeUsers.add(users)
        posts.raiseLikeNumber()
        return posts.likeNumber
    }

    @Transactional
    public fun decreaseLikeNumber(post_id: Long, user_id: String): Long {
        val posts = findPosts(post_id)
        val users = findUsers(user_id)
        if (!posts.likeUsers.contains(users) || !users.likePosts.contains(posts)) throw NotAcceptedException("Users $user_id is already dislike post $post_id !")
        users.likePosts.remove(posts)
        posts.likeUsers.remove(users)
        posts.decreaseLikeNumber()
        return posts.likeNumber
    }

    public fun getOnePhotoUrlFromPosts(post_id: Long): String {
        val posts = findPosts(post_id)
        val defaultPhotoUrl =
            "https://user-images.githubusercontent.com/84431962/187360732-ed3917a8-49c6-41de-b030-e22172889f4b.png"
        if (posts.photoSet.isNullOrEmpty()) return defaultPhotoUrl
        val iterator = posts.photoSet!!.iterator()
        return iterator.next().photoUrl
    }

}