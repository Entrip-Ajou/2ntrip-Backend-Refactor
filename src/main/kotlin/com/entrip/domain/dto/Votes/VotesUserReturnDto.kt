package com.entrip.domain.dto.Votes

import com.entrip.domain.entity.Users

class VotesUserReturnDto(
    val user_id : String,
    val nickname : String,
    val photo_url : String?
) {
    constructor(users : Users) : this(
        user_id = users.user_id!!,
        nickname = users.nickname,
        photo_url = users.photoUrl
    )
}