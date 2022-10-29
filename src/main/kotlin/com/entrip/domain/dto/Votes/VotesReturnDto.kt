package com.entrip.domain.dto.Votes

import com.entrip.domain.dto.VotesContents.VotesContentsReturnDto
import com.entrip.domain.entity.Votes

class VotesReturnDto(
    val vote_id : Long?,
    val title : String,
    val voting : Boolean,
    val host_id : String,
    val contents : MutableList<VotesContentsReturnDto>
) {
    constructor(votes: Votes, votesContentsList : MutableList<VotesContentsReturnDto>) : this(
        votes.vote_id,
        votes.title,
        votes.voting,
        votes.author?.user_id!!,
        votesContentsList
    )
}