package com.entrip.service

import com.entrip.domain.dto.Comments.*
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.entity.Comments
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.CommentsRepository
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class CommentsService(
    final val commentsRepository: CommentsRepository,

    @Autowired
    private val plannersRepository: PlannersRepository,

    @Autowired
    private val plansRepository: PlansRepository,

    @Autowired
    private val usersRepository: UsersRepository,

    @Autowired
    private val eventPublisher: ApplicationEventPublisher,
) {

    private fun findUsers(user_id: String?): Users {
        val users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findPlanners(planner_id: Long): Planners {
        val planners: Planners = plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }
        return planners
    }

    private fun findPlans(plan_id: Long): Plans {
        val plans: Plans = plansRepository.findById(plan_id!!).orElseThrow {
            IllegalArgumentException("Error raise at plansRepository.findById$plan_id")
        }
        return plans
    }

    private fun findComments(comment_id: Long): Comments {
        val comments: Comments = commentsRepository.findById(comment_id!!).orElseThrow {
            IllegalArgumentException("Error raise at commentsRepository.findById$comment_id")
        }
        return comments
    }

    private fun publishCrudEvents(message: String, planner_id: Long) {
        //eventPublisher.publishEvent(CrudEvent(message, planner_id))
    }

    public fun getAllCommentsWithPlanId(plan_id: Long): MutableList<CommentsReturnDto> {
        val plans = findPlans(plan_id)
        val commentsSet: MutableSet<Comments> = plans.comments
        val commentsList: MutableList<CommentsReturnDto> = ArrayList<CommentsReturnDto>()
        val commentsIterator = commentsSet.iterator()
        while (commentsIterator.hasNext()) {
            val comments = commentsIterator.next()
            val responseDto = CommentsResponseDto(comments)
            val returnDto = CommentsReturnDto(responseDto)
            commentsList.add(returnDto)
        }
        Collections.sort(commentsList, CommentsReturnDtoComparator())
        return commentsList
    }

    fun getAllCommentsAndPlanWithPlanId(plan_id: Long) : CommentsWithPlanReturnDto {
        val plans = findPlans(plan_id)
        val plansResponseDto = PlansResponseDto(plans)
        val planReturnDto = PlansReturnDto(plansResponseDto)
        val commentsList : MutableList<CommentsReturnDto> = getAllCommentsWithPlanId(plan_id)

        return CommentsWithPlanReturnDto(planReturnDto, commentsList)
    }

    @Transactional
    public fun save(requestDto: CommentsSaveRequestDto): CommentsWithPlanReturnDto {
        val plans = findPlans(requestDto.plans_id)
        val users = findUsers(requestDto.author)
        val comments = requestDto.toEntity()
        comments.plans = plans
        comments.users = users
        commentsRepository.save(comments)
        plans.comments.add(comments)
        plans.planners?.setComment_timeStamp()

        publishCrudEvents("Comments Save", plans.planners!!.planner_id!!)
        return getAllCommentsAndPlanWithPlanId(requestDto.plans_id)
    }

    public fun update(comment_id: Long, requestDto: CommentsUpdateRequestDto): MutableList<CommentsReturnDto> {
        val comments = findComments(comment_id)
        comments.update(requestDto.author, requestDto.content)
        comments.plans?.planners?.setComment_timeStamp()
        return getAllCommentsWithPlanId(requestDto.plans_id)
    }

    public fun findById(comment_id: Long): CommentsResponseDto {
        val comments = findComments(comment_id)
        return CommentsResponseDto(comments)
    }

    @Transactional

    public fun delete(comment_id: Long): CommentsWithPlanReturnDto {
        val comments = findComments(comment_id)
        val plan_id = comments.plans?.plan_id
        comments.plans?.planners?.setComment_timeStamp()
        comments.plans?.comments?.remove(comments)
        comments.users?.comments?.remove(comments)
        commentsRepository.delete(comments)
        publishCrudEvents("Delete Comments", comments.plans!!.planners!!.planner_id!!)
        return getAllCommentsAndPlanWithPlanId(plan_id!!)
    }
}