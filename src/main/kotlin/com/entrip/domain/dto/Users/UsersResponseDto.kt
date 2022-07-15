package com.entrip.domain.dto.Users

import com.entrip.domain.Planners
import com.entrip.domain.Users
import java.util.*

class UsersResponseDto(
    var user_id: String,
    var planners: MutableSet<Planners> = TreeSet(),
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
