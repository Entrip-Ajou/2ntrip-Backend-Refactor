package com.entrip.domain.dto.Users

class UsersLoginResReturnDto (
    val user_id: String,
    val accessToken: String,
    val nickname: String,
    val refreshToken: String = "DummyTokenValue"
) {
}