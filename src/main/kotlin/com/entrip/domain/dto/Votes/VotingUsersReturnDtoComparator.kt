package com.entrip.domain.dto.Votes

class VotingUsersReturnDtoComparator : Comparator<UsersAndContentsReturnDto> {
    override fun compare(one: UsersAndContentsReturnDto, two: UsersAndContentsReturnDto): Int {
        if (one.content_id > two.content_id) return 1
        if (one.content_id < two.content_id) return -1
        return 0
    }
}