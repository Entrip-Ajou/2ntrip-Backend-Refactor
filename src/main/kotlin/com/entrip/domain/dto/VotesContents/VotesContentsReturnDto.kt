package com.entrip.domain.dto.VotesContents

import com.entrip.domain.entity.VotesContents

class VotesContentsReturnDto(
    var votesContents_id : Long?,
    val content : String,
    val selectedCount : Int,
) {
    constructor(votesContents : VotesContents) : this (
        votesContents.votesContent_id,
        votesContents.contents,
        votesContents.selectedCount,
    )

}