package com.entrip.domain.dto.Votes

class VotingUsersReturnDtoComparator : Comparator<UsersAndContentsReturnDto> {
    override fun compare(one: UsersAndContentsReturnDto, two: UsersAndContentsReturnDto): Int {
        if (one.contentId > two.contentId) return 1
        if (one.contentId < two.contentId) return -1
        return 0
    }
}