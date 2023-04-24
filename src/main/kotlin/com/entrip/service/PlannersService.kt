package com.entrip.service

import com.entrip.domain.dto.Notices.NoticesReturnDto
import com.entrip.domain.dto.Notices.NoticesReturnDtoComparator
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersSaveRequestDto
import com.entrip.domain.dto.Planners.PlannersUpdateRequestDto
import com.entrip.domain.dto.Plans.PlansResponseDto
import com.entrip.domain.dto.Plans.PlansReturnDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Votes.VotesReturnDto
import com.entrip.domain.dto.Votes.VotesReturnDtoComparator
import com.entrip.domain.entity.Notices
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Plans
import com.entrip.domain.entity.Users
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class PlannersService(
    private val plannersRepository: PlannersRepository,

    @Autowired
    private val usersRepository: UsersRepository,

    @Autowired
    private val plansService: PlansService,

    @Autowired
    private val commentsService: CommentsService,

    @Autowired
    private val noticesService: NoticesService,

    @Autowired
    private val votesService: VotesService,

) {

    val logger: Logger = LoggerFactory.getLogger(PlannersService::class.java)

    private fun findPlanners(planner_id: Long): Planners {
        val planners: Planners = plannersRepository.findById(planner_id).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }
        return planners
    }

    private fun findUsers(user_id: String?): Users {
        val users: Users = usersRepository.findUsersByUser_idFetchPlanners(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at UsersRepository.findById$user_id")
        }
        return users
    }

    private fun findUsersWithNickname(nickname: String): Users {
        val usersList = usersRepository.findAll()
        for (users in usersList) if (users.nickname == nickname) return users
        throw IllegalArgumentException("Error raise at usersRepository.findByNickname : $nickname")
    }

    private fun fixDate(date: String): String =
        "${date.subSequence(0, 4)}/${date.subSequence(4, 6)}/${date.subSequence(6, 8)}"

    @Transactional
    fun save(requestDto: PlannersSaveRequestDto): Long? {
        val users = findUsers(requestDto.user_id)
        val planners = requestDto.toEntity()
        logger.info("**********************************")
        planners.setComment_timeStamp()
        users.addPlanners(planners)
        planners.addUsers(users)
        return plannersRepository.save(planners).planner_id
    }

    @Transactional
    fun update(planner_id: Long, requestDto: PlannersUpdateRequestDto): Long? {
        val planners = findPlanners(planner_id)
        planners.update(requestDto.title!!, requestDto.start_date!!, requestDto.end_date!!)
        return planner_id
    }

    fun findByPlannerId(planner_id: Long): PlannersResponseDto {
        val entity = findPlanners(planner_id)
        return PlannersResponseDto(entity)
    }

    fun findByPlannerIdWithDate(planner_id: Long, date: String): MutableList<PlansReturnDto> {
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

    fun findAllUsersWithPlannerId(planner_id: Long): MutableList<UsersResponseDto> {
        val planners = findPlanners(planner_id)
        val usersSet: MutableSet<Users>? = planners.users
        val usersList: MutableList<UsersResponseDto> = ArrayList<UsersResponseDto>()
        val iterator = usersSet!!.iterator()
        while (iterator.hasNext()) {
            val users = iterator.next()
            val responseDto = UsersResponseDto(users)
            usersList.add(responseDto)
        }
        return usersList
    }

    fun findAllPlansWithPlannerId(planner_id: Long): MutableList<PlansReturnDto> {
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

    fun findAllVotesWithPlannerID(planner_id : Long) : MutableList<VotesReturnDto> {
        val planners : Planners = findPlanners(planner_id)
        val votesListReturnDto : MutableList<VotesReturnDto> = ArrayList()

        for (vote in planners.votes) {
            val returnDto = votesService.findById(vote.vote_id!!)
            votesListReturnDto.add(returnDto)
        }

        Collections.sort(votesListReturnDto, VotesReturnDtoComparator())

        return votesListReturnDto
    }

    fun plannerIsExistWithId(planner_id: Long): Boolean {
        return plannersRepository.existsById(planner_id)
    }

    //Check and Refactor Delete function
    @Transactional
    fun delete(planner_id: Long): Long {
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
        val noticesIterator = planners.notices.iterator()
        while (noticesIterator.hasNext()) {
            val notices = noticesIterator.next()
            noticesIterator.remove()
            noticesService.delete(notices.notice_id!!)
        }
        val usersIterator = planners.users!!.iterator()
        while (usersIterator.hasNext()) {
            val users = usersIterator.next()
            users.planners.remove(planners)
        }
        val votesIterator = planners.votes.iterator()
        while (votesIterator.hasNext()) {
            val votes = votesIterator.next()
            votesIterator.remove()
            votesService.delete(votes.vote_id!!)
        }
        plannersRepository.delete(planners)
        return planner_id
    }

    @Transactional
    fun addFriendToPlanner(planner_id: Long, user_id: String): String {
        val planners = findPlanners(planner_id)
        val friends = findUsers(user_id)
        if (planners.users!!.contains(friends)) return "이미 planner에 등록되어있는 회원입니다."
        planners.addUsers(friends)
        friends.planners.add(planners)
        return "$planner_id 번 플래너에 $user_id 사용자 등록 완료."
    }

    fun userIsExistInPlannerWithUserId(planner_id: Long, user_id: String): Boolean {
        val planners = findPlanners(planner_id)
        val users = findUsers(user_id)
        if (planners.users.contains(users)) return true
        return false
    }

    fun userIsExistInPlannerWithNickname(planner_id: Long, nickname: String): Boolean {
        val planners = findPlanners(planner_id)
        val users = findUsersWithNickname(nickname)
        if (planners.users.contains(users)) return true
        return false
    }

    @Transactional
    fun deleteWithExit(planner_id: Long, user_id: String): Long {
        val planners = findPlanners(planner_id)
        val users = findUsers(user_id)
        if (planners.users.contains(users)) throw Exception()
        delete(planner_id)
        return planner_id
    }

    @Transactional
    fun exitPlanner(planner_id: Long, user_id: String): Boolean {
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
        return true
    }
}