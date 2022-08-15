package com.entrip.service

import com.entrip.domain.dto.PostsNestedComments.PostsNestedCommentsReturnDto
import com.entrip.domain.dto.PostsNestedComments.PostsNestedCommentsSaveRequestDto
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
import javax.transaction.Transactional

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

    @Transactional
    public fun save(requestDto: PostsNestedCommentsSaveRequestDto): Long {
        var postsNestedComments: PostsNestedComments = requestDto.toEntity()
        val users = findUsers(requestDto.author)
        val postsComments = findPostsComments(requestDto.postComment_id)
        postsNestedComments.setAuthorWithJoin(users)
        postsNestedComments.setPostsCommentsWithJoin(postsComments)
        postsNestedCommentsRepository.save(postsNestedComments)
        postsComments.posts!!.raiseCommentsNumber()
        return postsNestedComments.postNestedComment_id!!
    }

    public fun findById(postNestedComment_id: Long): PostsNestedCommentsReturnDto =
        PostsNestedCommentsReturnDto(findPostsNestedComments(postNestedComment_id))

    public fun getAllNestedCommentsWithPostCommentId(postComment_id: Long): MutableList<PostsNestedCommentsReturnDto> {
        val postsComments = findPostsComments(postComment_id)
        val postsNestedCommentsSet: MutableSet<PostsNestedComments> = postsComments.postsNestedComments!!
        val postsNestedCommentsList: MutableList<PostsNestedCommentsReturnDto> =
            ArrayList<PostsNestedCommentsReturnDto>()
        val iterator = postsNestedCommentsSet.iterator()
        while (iterator.hasNext()) {
            val postsNestedComments = iterator.next()
            val returnDto = PostsNestedCommentsReturnDto(postsNestedComments)
            postsNestedCommentsList.add(returnDto)
        }
        postsNestedCommentsList.sort()
        return postsNestedCommentsList
    }

    @Transactional
    public fun delete(postNestedComment_id: Long): Long {
        var postsNestedComments = findPostsNestedComments(postNestedComment_id)
        var users = postsNestedComments.author!!
        var postsComments = postsNestedComments.postsComments!!
        users.postsNestedComments.remove(postsNestedComments)
        postsComments.postsNestedComments!!.remove(postsNestedComments)
        postsNestedCommentsRepository.delete(postsNestedComments)
        postsComments.posts!!.decreaseCommentsNumber()
        return postNestedComment_id
    }

}