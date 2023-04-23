package com.entrip.domain.dto.Users

import com.entrip.domain.entity.Users

class UsersResponseDto(
    var user_id: String?,
    var nickname: String,
    var gender: Int?,
    var photoUrl: String?,
    var token: String?
) {
    constructor(users : Users) : this(
        user_id = users.user_id,
        nickname = users.nickname,
        gender = users.gender,
        photoUrl = users.photoUrl,
        token = users.token
    )
}