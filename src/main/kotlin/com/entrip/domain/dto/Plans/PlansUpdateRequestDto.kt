package com.entrip.domain.dto.Plans

class PlansUpdateRequestDto(
    val date: String,
    val todo: String,
    var time: String,
    var location: String? = null,
    var rgb: Long
) {
}