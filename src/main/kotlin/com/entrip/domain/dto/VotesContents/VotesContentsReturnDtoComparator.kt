package com.entrip.domain.dto.VotesContents

class VotesContentsReturnDtoComparator : Comparator<VotesContentsReturnDto> {
    override fun compare(one: VotesContentsReturnDto, two: VotesContentsReturnDto?): Int {
        if (one.votesContents_id!! > two?.votesContents_id!!) return 1
        if (one.votesContents_id!! < two.votesContents_id!!) return -1
        return 0
    }
}