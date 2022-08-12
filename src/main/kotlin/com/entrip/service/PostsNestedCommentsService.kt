package com.entrip.service

import com.entrip.domain.entity.PostsComments
import com.entrip.domain.entity.PostsNestedComments
import com.entrip.domain.entity.Users
import com.entrip.repository.PostsCommentsRepository
import com.entrip.repository.PostsNestedCommentsRepository
import com.entrip.repository.UsersRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PostsNestedCommentsService(
    final var postsNestedCommentsRepository: PostsNestedCommentsRepository,

    @Autowired
    var postsCommentsRepository: PostsCommentsRepository,

    @Autowired
    var usersRepository: UsersRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(PostsCommentsService::class.java)

    private fun findPostsNestedComments(postNestedComment_id: Long): PostsNestedComments {
        val postsNestedComments: PostsNestedComments =
            postsNestedCommentsRepository.findById(postNestedComment_id).orElseThrow {
                IllegalArgumentException("Error raie at postsNestedCommentsRepository.findById$postNestedComment_id")
            }
        return postsNestedComments
    }

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
    //CRUD Method

}