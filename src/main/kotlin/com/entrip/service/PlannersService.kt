package com.entrip.service

import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sun.security.ec.point.ProjectivePoint.Mutable
import javax.transaction.Transactional

@Service
class PlannersService (
    private final val plannersRepository : PlannersRepository,

    @Autowired
    private val usersRepository: UsersRepository,

    @Autowired
    private val plansRepository: PlansRepository,

    @Autowired
    private val plansService : PlansService,

    @Autowired
    private val commentsService: CommentsService
    ) {
    private fun findPlanners(planner_id: Long) : Planners {
        val planners : Planners = plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }
        return planners
    }

    private fun findUsers(user_id : String?) : Users {
        val users : Users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    @Transactional
    public fun save (requestDto : PlannersSaveRequestDto) : Long? {
        val users = findUsers(requestDto.user_id)
        val planners = requestDto.toEntity()
        //planners.setComment_timeStamp()
        users.addPlanners(planners)
        planners.addUsers(users)
        return plannersRepository.save(planners).planner_id
    }

    @Transactional
    public fun update (planner_id : Long, requestDto: PlannersUpdateRequestDto) : Long? {
        val planners = findPlanners(planner_id)
        planners.update(requestDto.title!!, requestDto.start_date!!, requestDto.end_date!!)
        return planner_id
    }

    public fun findByPlannerId (planner_id : Long) : PlannersResponseDto {
        val entity = findPlanners(planner_id)
        return PlannersResponseDto(entity)
    }

    public fun findByPlannerIdWithDate (planner_id : Long, date : String) : MutableList<PlansReturnDto> {
        val planners = findPlanners(planner_id)
        val plansSet : MutableSet<Plans>? = planners.plans
        val plansList : MutableList<PlansReturnDto> = ArrayList<PlansReturnDto>()
        val iterator = plansSet!!.iterator()
        while(iterator.hasNext()) {
            val plans = iterator.next()
            if (plans.date == date) {
                val responseDto = PlansResponseDto(plans)
                val returnDto = PlansReturnDto(responseDto)
                plansList.add(returnDto)
            }
        }
        return plansList
    }

    public fun findAllUsersWithPlannerId (planner_id : Long) : MutableList<UsersReturnDto> {
        val planners = findPlanners(planner_id)
        val usersSet : MutableSet<Users>? = planners.users
        val usersList : MutableList<UsersReturnDto> = ArrayList<UsersReturnDto>()
        val iterator = usersSet!!.iterator()
        while(iterator.hasNext()) {
            val users = iterator.next()
            val responseDto = UsersResponseDto(users)
            val returnDto = UsersReturnDto(responseDto)
            usersList.add(returnDto)
        }
        return usersList
    }

    public fun findAllPlansWithPlannerId (planner_id: Long) : MutableList<PlansReturnDto> {
        val planners = findPlanners(planner_id)
        val plansSet : MutableSet<Plans>? = planners.plans
        val plansList : MutableList<PlansReturnDto> = ArrayList<PlansReturnDto>()
        val plansIterator = plansSet?.iterator()
        while(plansIterator?.hasNext() == true) {
            val plans = plansIterator.next()
            val plansResponseDto = PlansResponseDto(plans)
            val plansReturnDto = PlansReturnDto(plansResponseDto)
            plansList.add(plansReturnDto)
        }
        return plansList
    }

    public fun plannerIsExistWithId (planner_id : Long) : Boolean {
        return plannersRepository.existsById(planner_id)
    }

    //Check and Refactor Delete function
    @Transactional
    public fun delete (planner_id : Long) : Long {
        val planners = findPlanners(planner_id)
        val plansIterator = planners.plans!!.iterator()
        while(plansIterator.hasNext()) {
            val plans = plansIterator.next()
            val commentsIterator = plans.comments.iterator()
            while (commentsIterator.hasNext()) {
                val comments = commentsIterator.next()
                commentsIterator.remove()
                commentsService.delete(comments.comment_id!!)
            }
            plansIterator.remove()
            plansService.delete(plans.plan_id!!)
        }
        val usersIterator = planners.users!!.iterator()
        while(usersIterator.hasNext()) {
            val users = usersIterator.next()
            users.planners.remove(planners)
        }
        plannersRepository.delete(planners)
        return planner_id
    }

    @Transactional
    public fun addFriendToPlanner (planner_id: Long, user_id: String) : String {
        val planners = findPlanners(planner_id)
        val friends = findUsers(user_id)
        if (planners.users!!.contains(friends)) return "이미 planner에 등록되어있는 회원입니다."
        planners.addUsers(friends)
        friends.planners.add(planners)
        return "$planner_id 번 플래너에 $user_id 사용자 등록 완료."
    }

    public fun userIsExistWithPlanner (planner_id: Long, user_id : String) : Boolean {
        val planners = findPlanners(planner_id)
        val users = findUsers(user_id)
        if (planners.users.contains(users)) return true;
        return false;
    }
}