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
import com.entrip.domain.entity.Planners
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
        if (usersRepository.existsByNickname(nickname)) {
            return usersRepository.findUsersByNickname(nickname).get()
        }
        throw IllegalArgumentException("Error raise at usersRepository.findByNickname : $nickname")
    }

    private fun fixDate(date: String): String =
        "${date.subSequence(0, 4)}/${date.subSequence(4, 6)}/${date.subSequence(6, 8)}"

    @Transactional
    fun save(requestDto: PlannersSaveRequestDto): Long? {
        val users = findUsers(requestDto.user_id)
        val planners = Planners.createPlanners(users)

        val planner_id = plannersRepository.save(planners).planner_id
        logger.info("Planners is saved in Database with plannerId : '{}'", planner_id)

        return planner_id
    }

    @Transactional
    fun update(planner_id: Long, requestDto: PlannersUpdateRequestDto): Long? {
        val planners = findPlanners(planner_id)
        planners.update(requestDto.title, requestDto.start_date, requestDto.end_date)
        logger.info("Planners is updated in Database with plannerId : '{}'", planner_id)
        return planner_id
    }

    fun findByPlannerId(planner_id: Long): PlannersResponseDto = PlannersResponseDto(findPlanners(planner_id))

    fun findByPlannerIdWithDate(planner_id: Long, date: String): MutableList<PlansReturnDto> {
        val planners = findPlanners(planner_id)
        val plansList: MutableList<PlansReturnDto> = ArrayList<PlansReturnDto>()
        val fixedDate: String = fixDate(date)

        for (plans in planners.plans!!) {
            if (plans.date == fixedDate) {
                plansList.add(PlansReturnDto(PlansResponseDto(plans)))
            }
        }

        return plansList
    }

    fun findAllUsersWithPlannerId(planner_id: Long): MutableList<UsersResponseDto> {
        val planners = findPlanners(planner_id)
        val usersList: MutableList<UsersResponseDto> = ArrayList<UsersResponseDto>()

        for (user in planners.users) {
            usersList.add(UsersResponseDto(user))
        }

        return usersList
    }

    fun findAllPlansWithPlannerId(planner_id: Long): MutableList<PlansReturnDto> {
        val planners = findPlanners(planner_id)
        val plansList: MutableList<PlansReturnDto> = ArrayList<PlansReturnDto>()

        for (plan in planners.plans!!) {
            plansList.add(PlansReturnDto(PlansResponseDto(plan)))
        }

        return plansList
    }

    fun findAllNoticesWithPlannerId(planner_id: Long): MutableList<NoticesReturnDto> {
        val planners = findPlanners(planner_id)
        val noticesList: MutableList<NoticesReturnDto> = ArrayList<NoticesReturnDto>()

        for (notice in planners.notices) {
            noticesList.add(NoticesReturnDto(notice))
        }

        Collections.sort(noticesList, NoticesReturnDtoComparator())
        return noticesList
    }

    fun findAllVotesWithPlannerID(planner_id : Long) : MutableList<VotesReturnDto> {
        val planners: Planners = findPlanners(planner_id)
        val votesList: MutableList<VotesReturnDto> = ArrayList()

        for (vote in planners.votes) {
            val returnDto = votesService.findById(vote.vote_id!!)
            votesList.add(returnDto)
        }

        Collections.sort(votesList, VotesReturnDtoComparator())
        return votesList
    }

    fun plannerIsExistWithId(planner_id: Long): Boolean = plannersRepository.existsById(planner_id)

    //Check and Refactor Delete function
    @Transactional
    fun delete(planner_id: Long): Long {
        val planners = findPlanners(planner_id)

        // delete Plans
        for (plan in planners.plans!!) {
            for (comment in plan.comments) {
                commentsService.delete(comment.comment_id!!)
            }
            plansService.delete(plan.plan_id!!)
        }

        // delete Notices
        for (notice in planners.notices) {
            noticesService.delete(notice.notice_id!!)
        }

        // exit Users
        for (user in planners.users) {
            user.planners.remove(planners)
        }

        // delete Votes
        for (vote in planners.votes) {
            votesService.delete(vote.vote_id!!)
        }

        plannersRepository.delete(planners)
        logger.info("Planners is deleted in Database with plannerId : '{}'", planner_id)
        return planner_id
    }

    @Transactional
    fun addFriendToPlanner(planner_id: Long, user_id: String): String {
        val planners = findPlanners(planner_id)
        val friends = findUsers(user_id)

        if (planners.users.contains(friends)) return "이미 planner에 등록되어있는 회원입니다."

        planners.addUsers(friends)
        friends.planners.add(planners)

        logger.info("Planner with planner_id : '{}' is added with User with user_id : '{}'", planner_id, user_id)

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

        for (plans in planners.plans!!) {
            for (comments in plans.comments) {
                if (comments.author == users.user_id) {
                    commentsService.delete(comments.comment_id!!)
                }
            }
        }

        for (notice in planners.notices) {
            if (notice.author!!.user_id == users.user_id) {
                noticesService.delete(notice.notice_id!!)
            }
        }

        for (vote in planners.votes) {
            if (vote.author!!.user_id == users.user_id) {
                votesService.delete(vote.vote_id!!)
            }
        }

        logger.info("User with user_id : '{}' is exit with Planner with planner_id : '{}'", user_id, planner_id)

        users.planners.remove(planners)
        planners.users.remove(users)

        if (planners.users.isEmpty()) this.delete(planner_id)
        return true
    }
}