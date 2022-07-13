package com.entrip.domain.dto.Users

class UsersReturnDto (
    var user_id : String,
    var nickname : String,
    var gender : Int?,
    var photoUrl : String?,
    var token : String?
        ){
    constructor(responseDto: UsersResponseDto) : this (
        responseDto.user_id,
        responseDto.nickname,
        responseDto.gender,
        responseDto.photoUrl,
        responseDto.token
    )
}