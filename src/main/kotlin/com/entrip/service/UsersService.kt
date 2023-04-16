package com.entrip.service

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Planners.PlannersReturnDto
import com.entrip.domain.dto.Users.*
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.exception.FailToFindNicknameOrIdException
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import io.jsonwebtoken.SignatureException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UsersService(
    private val usersRepository: UsersRepository,

    private val plannersRepository: PlannersRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {

    private fun findUsers(user_id: String?): Users =
        usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at UsersRepository.findById$user_id")
        }

    private fun findPlanners(planner_id: Long?): Planners =
        plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }

    @Transactional
    fun save(requestDto: UsersSaveRequestDto): String? {
        if (isExistUserId(requestDto.user_id))
            throw NotAcceptedException(UsersReturnDto("", "", -1, "", ""))
        val users = requestDto.toEntity()
        // Encode Password before saving user
        users.m_password = passwordEncoder.encode(requestDto.password)
        return usersRepository.save(users).user_id
    }

    fun findByUserIdAndReturnResponseDto(user_id: String?): UsersResponseDto =
        UsersResponseDto(findUsers(user_id))

    @Transactional
    fun delete(user_id: String): String? {
        val users = findUsers(user_id)
        val plannersIterator = users.planners.iterator()
        while (plannersIterator.hasNext()) {
            val planners = plannersIterator.next()
            planners.users.remove(users)
        }
        usersRepository.delete(users)
        return user_id
    }

    @Transactional
    fun addPlanners(planner_id: Long, user_id: String): Long? {
        val planners: Planners = findPlanners(planner_id)
        val users: Users = findUsers(user_id)
        users.addPlanners(planners)
        planners.addUsers(users)
        return planners.planner_id
    }

    @Transactional
    fun findAllPlannersWithUserId(user_id: String): MutableList<PlannersReturnDto> {
        val users: Users = findUsers(user_id)
        val plannersSet: MutableSet<Planners> = users.planners
        val plannersIterator = plannersSet.iterator()
        val plannersList: MutableList<PlannersReturnDto> = ArrayList()
        while (plannersIterator.hasNext()) {
            val planners = plannersIterator.next()
            val plannersResponseDto = PlannersResponseDto(planners)
            val plannersReturnDto = PlannersReturnDto(plannersResponseDto)
            plannersList.add(plannersReturnDto)
        }
        return plannersList
    }

    fun isExistNickname(nickname: String) : Boolean =
        usersRepository.existsByNickname(nickname)

    fun isExistUserId(user_id: String): Boolean =
        usersRepository.existsByUser_id(user_id)

    @Transactional
    fun updateToken(user_id: String, token: String): String {
        val users = findUsers(user_id)
        users.updateToken(token)
        return user_id
    }

    fun findUserWithNicknameOrUserId(nicknameOrUserId: String): String? {
        val usersList: List<Users> = usersRepository.findAll()
        for (users: Users in usersList) {
            if (users.user_id == nicknameOrUserId || users.nickname == nicknameOrUserId)
                return users.user_id
        }
        throw FailToFindNicknameOrIdException("Fail To Find Nickname Or Id matched Users!")
    }

    fun login(usersLoginRequestDto: UsersLoginRequestDto): UsersLoginResReturnDto {
        if (!isExistUserId(usersLoginRequestDto.user_id)) throw NotAcceptedException(DummyUsersLoginResReturnDto("Email is not valid"))
        val users = findUsers(usersLoginRequestDto.user_id)
        if (!passwordEncoder.matches(
                usersLoginRequestDto.password,
                users.password
            )
        ) throw NotAcceptedException(DummyUsersLoginResReturnDto("Password is not valid"))
        val accessToken: String = jwtTokenProvider.createAccessToken(usersLoginRequestDto.user_id)
        val refreshToken: String = jwtTokenProvider.createRefreshToken(usersLoginRequestDto.user_id)
        return UsersLoginResReturnDto(users.user_id!!, accessToken, users.nickname, refreshToken)
    }

    private class DummyUsersLoginResReturnDto(val detailedMessage: String) : UsersLoginResReturnDto("", "", "", "")

    fun reIssue(refreshToken: String): String {
        try {
            jwtTokenProvider.getUserPk(refreshToken)
        } catch (e: SignatureException) {
            throw SignatureException("Refresh token Signature is not valid.")
        }
        return jwtTokenProvider.reIssue(refreshToken)
    }

    fun logout(user_id: String): String =
        jwtTokenProvider.expireAllTokensWithUserPk(user_id)
}


