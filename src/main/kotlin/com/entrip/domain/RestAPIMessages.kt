package com.entrip.domain

import lombok.Data

@Data
class RestAPIMessages(
    val httpStatus: Int,
    val message: String,
    val data: Any
)