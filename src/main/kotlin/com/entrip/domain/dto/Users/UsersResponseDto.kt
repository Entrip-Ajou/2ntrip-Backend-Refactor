package com.entrip.domain.dto.Users

import com.entrip.domain.Planners
import com.entrip.domain.Users
import lombok.Builder

class UsersResponseDto(
    var user_id: String,
    var planners: HashSet<Planners> = HashSet(),
    var nickname: String,
    var gender: Int?,
    var photoUrl: String?,
    var token: String?
    ) {
    constructor(entity:Users) : this(
        entity.user_id,
        entity.planners,
        entity.nickname,
        entity.gender,
        entity.photoUrl,
        entity.token
    )
}
