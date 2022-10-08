package com.entrip.domain.dto.Votes

import com.entrip.domain.entity.Users

class VotesUserReturnDto(
    val userId : String,
    val nickname : String,
    val photo_url : String?
) {
    constructor(users : Users) : this(
        userId = users.user_id!!,
        nickname = users.nickname,
        photo_url = users.photoUrl
    )
}