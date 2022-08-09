package com.entrip.domain.dto.Comments

class CommentsReturnDtoComparator : Comparator<CommentsReturnDto> {
    public override fun compare(a: CommentsReturnDto, b: CommentsReturnDto): Int {
        if (a.comment_id!! > b.comment_id!!) return 1
        if (a.comment_id!! < b.comment_id!!) return -1
        return 0
    }
}