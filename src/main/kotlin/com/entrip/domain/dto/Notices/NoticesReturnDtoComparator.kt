package com.entrip.domain.dto.Notices

class NoticesReturnDtoComparator : Comparator<NoticesReturnDto>{
    override fun compare(one: NoticesReturnDto, two: NoticesReturnDto): Int {
        if (one.notice_id!! > two.notice_id!!) return -1
        if (one.notice_id!! < two.notice_id!!) return 1
        return 0
    }
}