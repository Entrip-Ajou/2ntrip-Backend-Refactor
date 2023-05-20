package com.entrip.service

import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansSaveRequestDto
import com.entrip.domain.dto.Plans.PlansUpdateRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class PlansService(
    val plansRepository: PlansRepository,

    @Autowired
    val plannersRepository: PlannersRepository,

    ) {

    val logger: Logger = LoggerFactory.getLogger(PlansService::class.java)

    private fun findPlanners(planner_id: Long): Planners {
        val planners: Planners = plannersRepository.findPlannersByPlanner_idFetchPlans(planner_id).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findPlannersByPlanner_idFetchPlans$planner_id")
        }
        return planners
    }

    private fun findPlans(plan_id: Long): Plans {
        val plans: Plans = plansRepository.findById(plan_id).orElseThrow {
            IllegalArgumentException("Error raise at plansRepository.findById$plan_id")
        }
        return plans
    }

    private fun findPlansWithFetchPlanners(plan_id: Long): Plans {
        val plans: Plans = plansRepository.findPlansByPlan_idFetchPlanners(plan_id).orElseThrow {
            IllegalArgumentException("Error raise at plansRepository.findPlansByPlans_idFetchPlanners$plan_id")
        }
        return plans
    }

    private fun findPlansWithFetchComment(plan_id: Long): Plans {
        val plans: Plans = plansRepository.findPlansByPlan_idFetchComments(plan_id).orElseThrow {
            IllegalArgumentException("Error raise at plansRepository.findPlansByPlans_idFetchComments$plan_id")
        }
        return plans
    }

    @Transactional
    fun save(requestDto: PlansSaveRequestDto): Long? {
        val planner_id: Long = requestDto.planner_id
        val planners = findPlanners(planner_id)
        val plans = Plans.createPlans(planners, requestDto.toEntity())

        val plans_id = plansRepository.save(plans).plan_id
        logger.info("Plans is saved in Database with planId : '{}'", plans_id)

        return plans_id
    }

    @Transactional
    fun update(plan_id: Long, requestDto: PlansUpdateRequestDto): Long? {
        val plans = findPlans(plan_id)
        plans.planners!!.setTimeStamp(LocalDateTime.now())
        plans.update(requestDto.date, requestDto.todo, requestDto.time, requestDto.location, requestDto.rgb)
        logger.info("Plans is updated in Database with planId : '{}'", plan_id)
        return plan_id
    }

    fun findById(plan_id: Long): PlansResponseDto {
        val plans = findPlansWithFetchPlanners(plan_id)
        val isExistComments = findPlansWithFetchComment(plan_id).isExistComments()
        return PlansResponseDto(plans, isExistComments)
    }

    @Transactional
    fun delete(plan_id: Long): Long {
        val plans = findPlansWithFetchPlanners(plan_id)
        val planners = plans.planners

        planners?.plans?.remove(plans)
        planners?.setTimeStamp(LocalDateTime.now())

        plansRepository.delete(plans)
        logger.info("Plans is deleted in Database with planId : '{}'", plan_id)

        return plan_id
    }
}