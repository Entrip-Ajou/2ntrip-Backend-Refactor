package com.entrip.service

import com.entrip.domain.dto.PostsComments.PostsCommentsReturnDto
import com.entrip.domain.dto.PostsComments.PostsCommentsSaveRequestDto
import com.entrip.domain.entity.Posts
import com.entrip.domain.entity.PostsComments
import com.entrip.domain.entity.Users
import com.entrip.repository.PostsCommentsRepository
import com.entrip.repository.PostsRepository
import com.entrip.repository.UsersRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PostsCommentsService(
    final val postsCommentsRepository: PostsCommentsRepository,

    @Autowired
    val usersRepository: UsersRepository,

    @Autowired
    val postsRepository: PostsRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(PostsCommentsService::class.java)

    private fun findPostsComments(postComment_id: Long): PostsComments {
        val postsComments: PostsComments = postsCommentsRepository.findById(postComment_id).orElseThrow {
            IllegalArgumentException("Error raise at postsCommentsRepository.findById$postComment_id")
        }
        return postsComments
    }

    private fun findUsers(user_id: String?): Users {
        val users: Users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findPosts(post_id: Long): Posts = postsRepository.findById(post_id).orElseThrow {
        IllegalArgumentException("Error raise at postsRepository.findById $post_id")
    }

    @Transactional
    public fun save(requestDto: PostsCommentsSaveRequestDto): Long {
        var postsComments: PostsComments = requestDto.toEntity()
        val users = findUsers(requestDto.author)
        val posts = findPosts(requestDto.post_id)
        postsComments.setAuthorWithJoin(users)
        postsComments.setPostsWithJoin(posts)
        postsCommentsRepository.save(postsComments)
        return postsComments.postComment_id!!
    }

    public fun findById(postComment_id: Long): PostsCommentsReturnDto =
        PostsCommentsReturnDto(findPostsComments(postComment_id))

    public fun getAllCommentsWithPostId(post_id: Long): MutableList<PostsCommentsReturnDto> {
        val posts = findPosts(post_id)
        val postsCommentsSet: MutableSet<PostsComments> = posts.postsCommentsSet!!
        val postsCommentsList: MutableList<PostsCommentsReturnDto> = ArrayList<PostsCommentsReturnDto>()
        val iterator = postsCommentsSet.iterator()
        while (iterator.hasNext()) {
            val postsComments = iterator.next()
            val returnDto = PostsCommentsReturnDto(postsComments)
            postsCommentsList.add(returnDto)
        }
        postsCommentsList.sort()
        return postsCommentsList
    }

    public fun delete(postComment_id: Long): Long {
        var postsComments = findPostsComments(postComment_id)
        var users = postsComments.author
        var posts = postsComments.posts
        users!!.postsComments.remove(postsComments)
        posts!!.postsCommentsSet!!.remove(postsComments)
        postsCommentsRepository.delete(postsComments)
        return postComment_id
    }
}