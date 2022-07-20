package com.entrip.service

import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Plans.PlansUpdateRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users
import com.entrip.repository.CommentsRepository
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class PlansService (
    final val plansRepository: PlansRepository,

    @Autowired
    val plannersRepository: PlannersRepository,

    @Autowired
    val commentsRepository: CommentsRepository,

    @Autowired
    val commentsService: CommentsService
        ){
    private fun findPlanners(planner_id: Long) : Planners {
        val planners : Planners = plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }
        return planners
    }

    private fun findPlans(plan_id : Long) : Plans {
        val plans : Plans = plansRepository.findById(plan_id!!).orElseThrow {
            IllegalArgumentException("Error raise at plansRepository.findById$plan_id")
        }
        return plans
    }

    @Transactional
    public fun save (requestDto : PlansSaveRequestDto) : Long? {
        val planner_id : Long = requestDto.planner_id
        val planners = findPlanners(planner_id)
        val plans = requestDto.toEntity()
        planners.plans!!.add(plans)
        planners.setTimeStamp(LocalDateTime.now())

        plans.setPlanners(planners)
        return plansRepository.save(plans).plan_id
    }

    @Transactional
    public fun update (plan_id : Long, requestDto: PlansUpdateRequestDto) : Long? {
        val plans = findPlans(plan_id)
        plans.planners!!.setTimeStamp(LocalDateTime.now())
        plans.update(requestDto.date, requestDto.todo, requestDto.time, requestDto.location, requestDto.rgb)
        return plan_id
    }

    public fun findById (plan_id: Long) : PlansResponseDto {
        val plans = findPlans(plan_id)
        return PlansResponseDto(plans)
    }

    @Transactional
    public fun delete (plan_id: Long) : Long {
        val plans = findPlans(plan_id)
        plans.planners?.plans?.remove(plans)

        val commentsIterator = plans.comments.iterator()
        while(commentsIterator.hasNext()) {
            val comments = commentsIterator.next()
            commentsIterator.remove()
            commentsService.delete(comments.comment_id!!)
        }

        val planners = plans.planners
        planners?.plans?.remove(plans)
        planners?.setTimeStamp(LocalDateTime.now())
        plansRepository.delete(plans)
        return plan_id
    }
}