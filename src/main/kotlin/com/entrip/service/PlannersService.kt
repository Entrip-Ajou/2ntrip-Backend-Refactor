package com.entrip.service

import com.entrip.domain.dto.Notices.NoticesReturnDto
import com.entrip.domain.dto.Notices.NoticesReturnDtoComparator
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersReturnDto
import com.entrip.domain.entity.Notices
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users
import com.entrip.events.CrudEvent
import com.entrip.repository.PlannersRepository
import com.entrip.repository.PlansRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Service
class PlannersService(
    private final val plannersRepository: PlannersRepository,

    @Autowired
    private val usersRepository: UsersRepository,

    @Autowired
    private val plansRepository: PlansRepository,

    @Autowired
    private val plansService: PlansService,

    @Autowired
    private val commentsService: CommentsService,

    @Autowired
    private val noticesService: NoticesService,

    @Autowired
    private val eventPublisher: ApplicationEventPublisher

) {
    private fun findPlanners(planner_id: Long): Planners {
        val planners: Planners = plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }
        return planners
    }

    private fun findUsers(user_id: String?): Users {
        val users: Users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findUsersWithNickname(nickname: String): Users {
        val usersList = usersRepository.findAll()
        for (users in usersList) if (users.nickname == nickname) return users
        throw IllegalArgumentException("Error raise at usersRepository.findByNickname : $nickname")
    }

    private fun publishCrudEvents(message: String, planner_id: Long) {
        //eventPublisher.publishEvent(CrudEvent(message, planner_id))
    }

    private fun fixDate(date: String): String =
        "${date.subSequence(0, 4)}/${date.subSequence(4, 6)}/${date.subSequence(6, 8)}"

    @Transactional
    public fun save(requestDto: PlannersSaveRequestDto): Long? {
        val users = findUsers(requestDto.user_id)
        val planners = requestDto.toEntity()
        planners.setComment_timeStamp()
        users.addPlanners(planners)
        planners.addUsers(users)
        plannersRepository.save(planners).planner_id
        //eventPublisher.publishEvent(CrudEvent("save", planners.planner_id!!))
        publishCrudEvents("Planner Save", planners.planner_id!!)
        return planners.planner_id
    }

    @Transactional
    public fun update(planner_id: Long, requestDto: PlannersUpdateRequestDto): Long? {
        val planners = findPlanners(planner_id)
        planners.update(requestDto.title!!, requestDto.start_date!!, requestDto.end_date!!)
        publishCrudEvents("Planner Update", planners.planner_id!!)
        return planner_id
    }

    public fun findByPlannerId(planner_id: Long): PlannersResponseDto {
        val entity = findPlanners(planner_id)
        return PlannersResponseDto(entity)
    }

    public fun findByPlannerIdWithDate(planner_id: Long, date: String): MutableList<PlansReturnDto> {
        val planners = findPlanners(planner_id)
        val plansSet: MutableSet<Plans>? = planners.plans
        val plansList: MutableList<PlansReturnDto> = ArrayList<PlansReturnDto>()
        val iterator = plansSet!!.iterator()
        val fixedDate: String = fixDate(date)

        while (iterator.hasNext()) {
            val plans = iterator.next()
            if (plans.date == fixedDate) {
                val responseDto = PlansResponseDto(plans)
                val returnDto = PlansReturnDto(responseDto)
                plansList.add(returnDto)
            }
        }
        return plansList
    }

    public fun findAllUsersWithPlannerId(planner_id: Long): MutableList<UsersReturnDto> {
        val planners = findPlanners(planner_id)
        val usersSet: MutableSet<Users>? = planners.users
        val usersList: MutableList<UsersReturnDto> = ArrayList<UsersReturnDto>()
        val iterator = usersSet!!.iterator()
        while (iterator.hasNext()) {
            val users = iterator.next()
            val responseDto = UsersResponseDto(users)
            val returnDto = UsersReturnDto(responseDto)
            usersList.add(returnDto)
        }
        return usersList
    }

    public fun findAllPlansWithPlannerId(planner_id: Long): MutableList<PlansReturnDto> {
        val planners = findPlanners(planner_id)
        val plansSet: MutableSet<Plans>? = planners.plans
        val plansList: MutableList<PlansReturnDto> = ArrayList<PlansReturnDto>()
        val plansIterator = plansSet?.iterator()
        while (plansIterator?.hasNext() == true) {
            val plans = plansIterator.next()
            val plansResponseDto = PlansResponseDto(plans)
            val plansReturnDto = PlansReturnDto(plansResponseDto)
            plansList.add(plansReturnDto)
        }
        return plansList
    }

    fun findAllNoticesWithPlannerId(planner_id: Long): MutableList<NoticesReturnDto> {
        val planners = findPlanners(planner_id)
        val noticesSet : MutableSet<Notices> = planners.notices
        val noticesList : MutableList<NoticesReturnDto> = ArrayList<NoticesReturnDto>()
        val noticesIterator = noticesSet.iterator()

        while (noticesIterator.hasNext()) {
            val notices = noticesIterator.next()
            val returnDto = NoticesReturnDto(notices)
            noticesList.add(returnDto)
        }
        Collections.sort(noticesList, NoticesReturnDtoComparator())
        return noticesList
    }

    public fun plannerIsExistWithId(planner_id: Long): Boolean {
        return plannersRepository.existsById(planner_id)
    }

    //Check and Refactor Delete function
    @Transactional
    public fun delete(planner_id: Long): Long {
        val planners = findPlanners(planner_id)
        val plansIterator = planners.plans!!.iterator()
        while (plansIterator.hasNext()) {
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
        while (usersIterator.hasNext()) {
            val users = usersIterator.next()
            users.planners.remove(planners)
        }
        plannersRepository.delete(planners)
        publishCrudEvents("Planner Delete", planners.planner_id!!)
        return planner_id
    }

    @Transactional
    public fun addFriendToPlanner(planner_id: Long, user_id: String): String {
        val planners = findPlanners(planner_id)
        val friends = findUsers(user_id)
        if (planners.users!!.contains(friends)) return "이미 planner에 등록되어있는 회원입니다."
        planners.addUsers(friends)
        friends.planners.add(planners)
        publishCrudEvents("Add Friend To Planner", planners.planner_id!!)
        return "$planner_id 번 플래너에 $user_id 사용자 등록 완료."
    }

    public fun userIsExistInPlannerWithUserId(planner_id: Long, user_id: String): Boolean {
        val planners = findPlanners(planner_id)
        val users = findUsers(user_id)
        if (planners.users.contains(users)) return true
        return false
    }

    public fun userIsExistInPlannerWithNickname(planner_id: Long, nickname: String): Boolean {
        val planners = findPlanners(planner_id)
        val users = findUsersWithNickname(nickname)
        if (planners.users.contains(users)) return true
        return false
    }

    @Transactional
    public fun deleteWithExit(planner_id: Long, user_id: String): Long {
        val planners = findPlanners(planner_id)
        val users = findUsers(user_id)
        if (planners.users.contains(users)) throw Exception()
        delete(planner_id)
        return planner_id
    }

    @Transactional
    public fun exitPlanner(planner_id: Long, user_id: String): Boolean {
        val planners = findPlanners(planner_id)
        val users = findUsers(user_id)

        if (planners.plans != null) {
            for (plans in planners.plans!!) {
                for (comments in plans.comments) {
                    if (comments.author == users.user_id) commentsService.delete(comments.comment_id!!)
                }
            }
        }

        users.planners.remove(planners)
        planners.users.remove(users)

        if (planners.users.isEmpty()) this.delete(planner_id)
        publishCrudEvents("User ${user_id} exit Planner", planners.planner_id!!)
        return true
    }
}