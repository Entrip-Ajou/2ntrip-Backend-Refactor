package com.entrip.auth

import com.entrip.repository.UsersRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import javax.servlet.http.HttpSession

class CutsomOauth2UserService(
    private val usersRepository: UsersRepository,
    private val httpSession: HttpSession
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    public override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val delegate: OAuth2UserService<OAuth2UserRequest, OAuth2User> = DefaultOAuth2UserService()
        var oAuth2User: OAuth2User = delegate.loadUser(userRequest)
        TODO("Complete Social Login Part, using jwt token")
    }
}