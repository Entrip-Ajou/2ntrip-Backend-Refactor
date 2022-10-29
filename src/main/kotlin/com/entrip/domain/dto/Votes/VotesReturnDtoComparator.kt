package com.entrip.domain.dto.Votes

class VotesReturnDtoComparator : Comparator<VotesReturnDto> {
    override fun compare(one: VotesReturnDto, two: VotesReturnDto): Int {
        if (one.vote_id!! > two.vote_id!!) return 1
        if (one.vote_id!! < two.vote_id!!) return -1
        return 0
    }
}