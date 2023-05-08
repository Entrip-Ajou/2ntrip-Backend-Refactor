package com.entrip.service

import com.entrip.auth.jwt.JwtTokenProvider
import com.entrip.domain.dto.Planners.PlannersResponseDto
import com.entrip.domain.dto.Users.UsersLoginRequestDto
import com.entrip.domain.dto.Users.UsersLoginResReturnDto
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.domain.entity.Planners
import com.entrip.domain.entity.Users
import com.entrip.exception.FailToFindNicknameOrIdException
import com.entrip.exception.NotAcceptedException
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import io.jsonwebtoken.SignatureException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    val logger: Logger = LoggerFactory.getLogger(UsersService::class.java)

    private fun findUsers(user_id: String?): Users =
        usersRepository.findById(user_id!!).orElseThrow {
            IllegalArgumentException("Error raise at UsersRepository.findById$user_id")
        }

    // Select Users with join Fetch Users.Planners
    private fun findUsersWithFetchPlanner (user_id : String) : Users =
        usersRepository.findUsersByUser_idFetchPlanners(user_id).orElseThrow {
            IllegalArgumentException("Error raise at UsersRepository.findById$user_id")
        }

    // Select Users with Lazy strategy
    private fun findUsersWithLazy (user_id : String) : Users =
        usersRepository.findUsersByUser_idWithLazy(user_id).orElseThrow {
            IllegalArgumentException("Error raise at UsersRepository.findById$user_id")
        }

    private fun findPlanners(planner_id: Long?): Planners =
        plannersRepository.findById(planner_id!!).orElseThrow {
            IllegalArgumentException("Error raise at PlannersRepository.findById$planner_id")
        }

    @Transactional
    fun save(requestDto: UsersSaveRequestDto): String? {
        // Check if User is already exist. If exists, throw NotAcceptedException
        if (isExistUserId(requestDto.user_id))
            throw NotAcceptedException(UsersResponseDto("", "", -1, "", ""))
        // Convert saveDto to Entity
        val users = requestDto.toEntity()
        // Encode Password before saving user
        users.m_password = passwordEncoder.encode(requestDto.password)
        // Save Users in DB
        usersRepository.save(users)
        logger.info("User is saved in Database with userid : '{}'", users.user_id)
        return users.user_id
    }

    fun findByUserIdAndReturnResponseDto(user_id: String?): UsersResponseDto =
        UsersResponseDto(findUsersWithLazy(user_id!!))

    @Transactional
    fun delete(user_id: String): String? {
        val users = findUsersWithFetchPlanner(user_id)
        for (p : Planners in users.planners)
            p.users.remove(users)
        usersRepository.delete(users)
        logger.info("User is deleted from Database with userid : '{}'", user_id)
        return user_id
    }

    @Transactional
    fun addPlanners(planner_id: Long, user_id: String): Long? {
        val planners: Planners = findPlanners(planner_id)
        val users: Users = findUsersWithFetchPlanner(user_id)
        users.addPlanners(planners)
        planners.addUsers(users)
        logger.info("Planner with planner_id : '{}' is added with User with user_id : '{}'", planner_id, user_id)
        return planners.planner_id
    }

    @Transactional
    fun findAllPlannersWithUserId(user_id: String): MutableList<PlannersResponseDto> {
        val users: Users = findUsersWithFetchPlanner(user_id)
        val plannersList: MutableList<PlannersResponseDto> = ArrayList()
        for (p: Planners in users.planners)
            plannersList.add(PlannersResponseDto(p))
        return plannersList
    }

    fun isExistNickname(nickname: String) : Boolean =
        usersRepository.existsByNickname(nickname)

    fun isExistUserId(user_id: String): Boolean =
        usersRepository.existsByUser_id(user_id)

    @Transactional
    fun updateToken(user_id: String, token: String): String {
        val users = findUsersWithLazy(user_id)
        users.updateToken(token)
        logger.info("Update users' token with user_id : '{}' with token : '{}'", user_id, token)
        return user_id
    }

    fun findUserWithNicknameOrUserId(nicknameOrUserId: String): String? {
        if (usersRepository.existsByUser_id(nicknameOrUserId)) {
            val users = usersRepository.findUsersByUser_idWithLazy(nicknameOrUserId).get()
            return users.user_id
        }
        if (usersRepository.existsByNickname(nicknameOrUserId)) {
            val users = usersRepository.findUsersByNickname(nicknameOrUserId).get()
            return users.user_id
        }
        logger.warn("Fail to find User with Nickname or UserId value : '{}'", nicknameOrUserId)
        throw FailToFindNicknameOrIdException("Fail To Find Nickname Or Id matched Users!")
    }

    fun login(usersLoginRequestDto: UsersLoginRequestDto): UsersLoginResReturnDto {
        // Match email first. If failed, throw NotAcceptedException
        if (!isExistUserId(usersLoginRequestDto.user_id)) {
            logger.warn("Fail to Login because email is not valid with email value : '{}'", usersLoginRequestDto.user_id)
            throw NotAcceptedException(DummyUsersLoginResReturnDto("Email is not valid"))
        }
        val users = findUsersWithLazy(usersLoginRequestDto.user_id)
        // Match password second with passwordEncoder. If failed, throw NotAcceptedException
        if (!passwordEncoder.matches(
                usersLoginRequestDto.password,
                users.password
            )
        ) {
            logger.warn("Fail to Login because password is not valid")
            throw NotAcceptedException(DummyUsersLoginResReturnDto("Password is not valid"))
        }
        // Create accessToken and refreshToken via jwtTokenProvider
        val accessToken: String = jwtTokenProvider.createAccessToken(usersLoginRequestDto.user_id)
        val refreshToken: String = jwtTokenProvider.createRefreshToken(usersLoginRequestDto.user_id)
        logger.info("Success to Login. Return UsersLoginResReturnDto with accessToken and refreshToken")
        return UsersLoginResReturnDto(users.user_id, accessToken, users.nickname, refreshToken)
    }

    private class DummyUsersLoginResReturnDto(val detailedMessage: String) : UsersLoginResReturnDto("", "", "", "")

    fun reIssue(refreshToken: String): String {
        // Get userPK from token first. If failed, throw SignatureException
        // The only reason is SignatureException because expiration doesn't matter with reIssue
        try {
            jwtTokenProvider.getUserPk(refreshToken)
        } catch (e: SignatureException) {
            logger.error("Fail to reIssue because jwtToken signature is not valid")
            throw SignatureException("Refresh token Signature is not valid")
        }
        // ReIssue with jwtTokenProvider
        val reIssuedRefreshToken = jwtTokenProvider.reIssue(refreshToken)
        logger.info("ReIssue refresh Token")
        return reIssuedRefreshToken
    }

    fun logout(user_id: String): String {
        // Logout through expiring all token made with user_id
        val result = jwtTokenProvider.expireAllTokensWithUserPk(user_id)
        logger.info("Logout with user_id value : '{}'", result)
        return result
    }
}


