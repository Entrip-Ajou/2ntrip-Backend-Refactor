package com.entrip.service

import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.entity.Users
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sun.security.ec.point.ProjectivePoint.Mutable
import javax.transaction.Transactional
import com.entrip.domain.entity.Planners as Planners
import com.entrip.exception.NicknameOrUserIdNotValidException as NicknameOrUserIdNotValidException

@Service
class UsersService(
    private final val usersRepository: UsersRepository,

    @Autowired
    private val plannersRepository: PlannersRepository
) {

    private fun findUsers(user_id: String?): Users {
        val users = usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return users
    }

    private fun findPlanners(planner_id: Long?): Planners {
        val planners: Planners = plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }
        return planners
    }


    @Transactional
    public fun save(requestDto: UsersSaveRequestDto): String? {
        val users: Users = Users(
            user_id = requestDto.user_id,
            photoUrl = requestDto.photoUrl,
            gender = requestDto.gender,
            nickname = requestDto.nickname
        )
        val user_id: String? = usersRepository.save(users).user_id
        return user_id
    }

    public fun findByUserId(user_id: String?): UsersResponseDto {
        val entity: Users = findUsers(user_id)
        return UsersResponseDto(entity)
    }

    @Transactional
    public fun delete(user_id: String): String? {
        val users = findUsers(user_id)
        val plannersIterator = users.planners.iterator()
        while (plannersIterator.hasNext()) {
            val planners = plannersIterator.next()
            planners.users?.remove(users)
        }
        usersRepository.delete(users)
        return user_id
    }

    @Transactional
    public fun addPlanners(planner_id: Long, user_id: String): Long? {
        val planners: Planners = findPlanners(planner_id)
        val users: Users = findUsers(user_id)
        users.addPlanners(planners)
        planners.addUsers(users)
        return planners.planner_id
    }

    @Transactional
    public fun findAllPlannersWithUserId(user_id: String): MutableList<PlannersReturnDto> {
        val users: Users = findUsers(user_id)
        val plannersSet: MutableSet<Planners> = users.planners
        val plannersIterator = plannersSet.iterator()
        val plannersList: MutableList<PlannersReturnDto> = ArrayList<PlannersReturnDto>()
        while (plannersIterator.hasNext()) {
            val planners = plannersIterator.next()
            val plannersResponseDto: PlannersResponseDto = PlannersResponseDto(planners)
            val plannersReturnDto: PlannersReturnDto = PlannersReturnDto(plannersResponseDto)
            plannersList.add(plannersReturnDto)
        }
        return plannersList
    }

    public fun isExistNickname(nickname: String): Boolean {
        val usersList: List<Users> = usersRepository.findAll()
        for (users: Users in usersList) {
            val temp: String = users.nickname
            if (temp == nickname) return true
        }
        return false
    }

    public fun isExistUserId(user_id: String): Boolean {
        val usersList: List<Users> = usersRepository.findAll()
        for (users: Users in usersList) {
            val temp: String? = users.user_id
            if (temp == user_id) return true
        }
        return false
    }

    @Transactional
    public fun updateToken(user_id: String, token: String): String {
        val users: Users = findUsers(user_id)
        users.updateToken(token)
        return user_id
    }

    public fun findUserWithNicknameOrUserId(nicknameOrUserId: String): String? {
        val usersList: List<Users> = usersRepository.findAll()
        for (users: Users in usersList) {
            if (users.user_id == nicknameOrUserId || users.nickname == nicknameOrUserId)
                return users.user_id
        }
        return null
    }
}


