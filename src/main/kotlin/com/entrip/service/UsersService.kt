package com.entrip.service

import com.entrip.domain.Users
import com.entrip.domain.dto.Users.UsersResponseDto
import com.entrip.domain.dto.Users.UsersSaveRequestDto
import com.entrip.repository.PlannersRepository
import com.entrip.repository.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UsersService (
    private val usersRepository: UsersRepository,

    @Autowired
    private val plannersRepository: PlannersRepository
    ){
    @Transactional
    fun save(requestDto: UsersSaveRequestDto): String {
        val users: Users = Users(
            user_id = requestDto.user_id,
            photoUrl = requestDto.photoUrl,
            gender = requestDto.gender,
            nickname = requestDto.nickname
        )
        return usersRepository.save(users).user_id
    }

    fun findById(user_id : String) : UsersResponseDto{
        val entity : Users = usersRepository.findById(user_id).orElseThrow {
            IllegalArgumentException("Error raise at usersRepository.findById$user_id")
        }
        return UsersResponseDto(entity)
    }
}


