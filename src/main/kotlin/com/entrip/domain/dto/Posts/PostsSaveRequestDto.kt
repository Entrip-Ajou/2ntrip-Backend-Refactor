package com.entrip.domain.dto.Posts

import com.entrip.domain.entity.Posts

class PostsSaveRequestDto(
    val title: String,
    val content: String,
    val author: String,
    val photoIdList: MutableList<Long> = ArrayList<Long>(),
    val postTag: String = ""
) {
    public fun toEntity(): Posts {
        return Posts(
            title = title,
            content = content,
            postTag = postTag
        )
    }
}