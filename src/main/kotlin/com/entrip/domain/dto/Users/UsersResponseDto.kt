package com.entrip.domain.dto.Users

import com.entrip.domain.Planners
import com.entrip.domain.Users

class UsersResponseDto (
    var user_id : String,
    var planners : HashSet<Planners> = HashSet(),
    var nickname : String,
    var gender : Int?,
    var photoUrl : String?,
    var token : String?
    ) {
    fun UsersResponseDto(entity: Users) {
        user_id = entity.user_id
        planners = entity.planners
        nickname = entity.nickname
        gender = entity.gender
        photoUrl = entity.photoUrl
        token = entity.token
    }
}
