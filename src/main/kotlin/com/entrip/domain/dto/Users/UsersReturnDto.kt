package com.entrip.domain.dto.Users

class UsersReturnDto (
    var user_id : String,
    var nickname : String,
    var gender : Int?,
    var photoUrl : String?,
    var token : String?
        ){
    fun UsersReturnDto(responseDto: UsersResponseDto) {
        this.user_id = responseDto.user_id
        this.nickname = responseDto.nickname
        this.gender = responseDto.gender
        this.photoUrl = responseDto.photoUrl
        this.token = responseDto.token
    }
}