package com.entrip.domain

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.Data

@Data
class RestAPIMessages(
    val httpStatus: Int,
    val message: String,
    val data: Any
) {
    public fun convertToJson(): String =
        ObjectMapper().writeValueAsString(this)

}