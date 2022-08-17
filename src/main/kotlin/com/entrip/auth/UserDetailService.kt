package com.entrip.auth

import com.entrip.repository.UsersRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class UserDetailService(private val usersRepository: UsersRepository): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return usersRepository.findById(username).orElseThrow{
            IllegalArgumentException("Error raised at usersRepository.findById $username")
        }
    }
}