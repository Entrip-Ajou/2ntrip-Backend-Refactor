package com.entrip.domain.dto.Votes

import com.entrip.domain.entity.Users

class VotingUsersReturnDto(
    val content : String,
    val users : MutableList<Users>
) {
}